/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC      initial implementation 
 *     AMC 01.20.2003  extended to support new AspectJ 1.1 options,
 * 				       bugzilla #29769
 * ******************************************************************/


package org.aspectj.ajde.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildOptionsAdapter;
import org.aspectj.ajde.BuildProgressMonitor;
import org.aspectj.ajde.ProjectPropertiesAdapter;
import org.aspectj.ajde.TaskListManager;
import org.aspectj.ajdt.ajc.BuildArgParser;
import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.util.ConfigParser;
import org.aspectj.util.LangUtil;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class CompilerAdapter {

	private AjBuildManager buildManager = null;
    private MessageHandlerAdapter messageHandler = null;
    private BuildNotifierAdapter currNotifier = null;
	private boolean initialized = false;
	private boolean structureDirty = true;
	private boolean firstBuild = true;
	
	public CompilerAdapter() {
		super();
	}

	public void requestCompileExit() {
		if (currNotifier != null) currNotifier.cancelBuild();
//		buildManager.getJavaBuilder().notifier.setCancelling(true);
	}

	public boolean isStructureDirty() {
		return structureDirty;
	}

	public void setStructureDirty(boolean structureDirty) {
		this.structureDirty = structureDirty;
	}

	public boolean compile(String configFile, BuildProgressMonitor progressMonitor) {
		init();
		try {	 
			AjBuildConfig buildConfig = genBuildConfig(configFile);
			buildConfig.setGenerateModelMode(true);
			currNotifier = new BuildNotifierAdapter(
				AjBuildManager.DEFAULT_PROJECT,
				progressMonitor,
				buildConfig.getFiles().size());			
			buildManager.setBuildNotifier(currNotifier);
			messageHandler.setBuildNotifierAdapter(currNotifier);
			
			String rtInfo = buildManager.checkRtJar(buildConfig); // !!! will get called twice
			if (rtInfo != null) {
				Ajde.getDefault().getErrorHandler().handleWarning(
					"AspectJ Runtime error: " + rtInfo
		            + "  Please place a valid aspectjrt.jar in the lib/ext directory.");
	            return false;
			}
			
			if (firstBuild) {
				firstBuild = false;
				return buildManager.batchBuild(buildConfig, messageHandler);  
			} else {
				return buildManager.batchBuild(buildConfig, messageHandler);  // XXX incremental not implemented
//				return buildManager.incrementalBuild(buildConfig);
			}
		} catch (OperationCanceledException ce) {
			Ajde.getDefault().getIdeUIAdapter().displayStatusInformation("build cancelled by user");
			return false;
		} catch (Throwable t) {
			t.printStackTrace();
			return false; 
//			messageHandler.handleMessage(new Message(t.toString(), Message.ERROR, t, null));
		} 
	}

	public AjBuildConfig genBuildConfig(String configFile) {
	    AjBuildConfig buildConfig = new AjBuildConfig();
	    File config = new File(configFile);
        if (!config.exists()) {
            Ajde.getDefault().getErrorHandler().handleWarning("Config file \"" + configFile + "\" does not exist."); 
        } else {
	        ConfigParser configParser = new ConfigParser();
	        configParser.parseConfigFile(config);
			buildConfig.setFiles(configParser.getFiles());
			buildConfig.setConfigFile(config);
        }
		
		// AMC refactored into two methods to populate buildConfig from buildOptions and
		// project properties - bugzilla 29769.
		configureBuildOptions(buildConfig, Ajde.getDefault().getBuildManager().getBuildOptions());
		configureProjectOptions(buildConfig, Ajde.getDefault().getProjectProperties());
		
		buildConfig.setGenerateModelMode(true);		
		return buildConfig;
	}

	/**
	 * Populate options in a build configuration, using the Ajde BuildOptionsAdapter.
	 * Added by AMC 01.20.2003, bugzilla #29769
	 */
	private void configureBuildOptions( AjBuildConfig config, BuildOptionsAdapter options ) {
        LangUtil.throwIaxIfNull(options, "options");
        LangUtil.throwIaxIfNull(config, "config");
		Map javaOptions = config.getJavaOptions();
        LangUtil.throwIaxIfNull(javaOptions, "javaOptions");

		if (options.getSourceOnePointFourMode()) {
			javaOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);	 
			javaOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
		} 
		
		String enc = options.getCharacterEncoding();
		if (!LangUtil.isEmpty(enc)) {
			javaOptions.put(CompilerOptions.OPTION_Encoding, enc );
		}

		String compliance = options.getComplianceLevel();
		if (!LangUtil.isEmpty(compliance)) {
			String version = CompilerOptions.VERSION_1_4;
			if ( compliance.equals( BuildOptionsAdapter.VERSION_13 ) ) {
				version = CompilerOptions.VERSION_1_3;
			}
			javaOptions.put(CompilerOptions.OPTION_Compliance, version );	
			javaOptions.put(CompilerOptions.OPTION_Source, version );
		}
				
		String sourceLevel = options.getSourceCompatibilityLevel();
		if (!LangUtil.isEmpty(sourceLevel)) {
			String slVersion = CompilerOptions.VERSION_1_4;
			if ( sourceLevel.equals( BuildOptionsAdapter.VERSION_13 ) ) {
				slVersion = CompilerOptions.VERSION_1_3;
			}
			// never set a lower source level than compliance level
			String setCompliance = (String) javaOptions.get( CompilerOptions.OPTION_Compliance);
			if ( ! (setCompliance.equals( CompilerOptions.VERSION_1_4 )
			         && slVersion.equals(CompilerOptions.VERSION_1_3)) ) {
				javaOptions.put(CompilerOptions.OPTION_Source, slVersion);							
			}
		}
	
		Set warnings = options.getWarnings();
		if (!LangUtil.isEmpty(warnings)) {
			// turn off all warnings	
			disableWarnings( javaOptions );
			// then selectively enable those in the set
			enableWarnings( javaOptions, warnings );
		}

		Set debugOptions = options.getDebugLevel();
		if (!LangUtil.isEmpty(debugOptions)) {
			// default is all options off, so just need to selectively
			// enable
			Iterator it = debugOptions.iterator();
			while (it.hasNext()){
				String debug = (String) it.next();
				if ( debug.equals( BuildOptionsAdapter.DEBUG_ALL )) {
					javaOptions.put( CompilerOptions.OPTION_LineNumberAttribute,
									 CompilerOptions.GENERATE);
					javaOptions.put( CompilerOptions.OPTION_SourceFileAttribute,
									 CompilerOptions.GENERATE);
					javaOptions.put( CompilerOptions.OPTION_LocalVariableAttribute,
									 CompilerOptions.GENERATE);									 
				} else if ( debug.equals( BuildOptionsAdapter.DEBUG_LINES )) {
					javaOptions.put( CompilerOptions.OPTION_LineNumberAttribute,
									 CompilerOptions.GENERATE);					
				}  else if ( debug.equals( BuildOptionsAdapter.DEBUG_SOURCE )) {
					javaOptions.put( CompilerOptions.OPTION_SourceFileAttribute,
									 CompilerOptions.GENERATE);					
				}  else if ( debug.equals( BuildOptionsAdapter.DEBUG_VARS)) {
					javaOptions.put( CompilerOptions.OPTION_LocalVariableAttribute,
									 CompilerOptions.GENERATE);					
				}
			}
		}

		if ( options.getNoImportError() ) {
			javaOptions.put( CompilerOptions.OPTION_ReportInvalidImport,
				CompilerOptions.WARNING);	
		}
				
		if ( options.getPreserveAllLocals() ) {
			javaOptions.put( CompilerOptions.OPTION_PreserveUnusedLocal,
				CompilerOptions.PRESERVE);		
		}
				
		config.setJavaOptions( javaOptions );
		
		configureNonStandardOptions( config, options );
	}
	
	/**
	 * Helper method for configureBuildOptions
	 */
	private void disableWarnings( Map options ) {
		options.put(
			CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportMethodWithConstructorName,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportDeprecation, 
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportHiddenCatchBlock,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportUnusedLocal, 
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportUnusedParameter,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportAssertIdentifier,
			CompilerOptions.IGNORE);
		options.put(
			CompilerOptions.OPTION_ReportUnusedImport,
			CompilerOptions.IGNORE);		
	}

	/**
	 * Helper method for configureBuildOptions
	 */
	private void enableWarnings( Map options, Set warnings ) {
		Iterator it = warnings.iterator();
		while (it.hasNext() ) {
			String thisWarning = (String) it.next();
			if ( thisWarning.equals( BuildOptionsAdapter.WARN_ASSERT_IDENITIFIER )) {
				options.put( CompilerOptions.OPTION_ReportAssertIdentifier,
							 CompilerOptions.WARNING );				
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_CONSTRUCTOR_NAME )) {
				options.put( CompilerOptions.OPTION_ReportMethodWithConstructorName,
							 CompilerOptions.WARNING );								
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_DEPRECATION )) {
				options.put( CompilerOptions.OPTION_ReportDeprecation,
							 CompilerOptions.WARNING );					
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_MASKED_CATCH_BLOCKS )) {
				options.put( CompilerOptions.OPTION_ReportHiddenCatchBlock,
							 CompilerOptions.WARNING );	
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_PACKAGE_DEFAULT_METHOD )) {
				options.put( CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
							 CompilerOptions.WARNING );					
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_SYNTHETIC_ACCESS )) {
				options.put( CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
							 CompilerOptions.WARNING );					
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_UNUSED_ARGUMENTS )) {
				options.put( CompilerOptions.OPTION_ReportUnusedParameter,
							 CompilerOptions.WARNING );					
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_UNUSED_IMPORTS )) {
				options.put( CompilerOptions.OPTION_ReportUnusedImport,
							 CompilerOptions.WARNING );					
			} else if ( thisWarning.equals( BuildOptionsAdapter.WARN_UNUSED_LOCALS )) {
				options.put( CompilerOptions.OPTION_ReportUnusedLocal,
							 CompilerOptions.WARNING );					
			}  else if ( thisWarning.equals( BuildOptionsAdapter.WARN_NLS )) {
				options.put( CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
							 CompilerOptions.WARNING );					
			}
		}		
	}


	/**
	 * Helper method for configure build options
	 */
	private void configureNonStandardOptions( AjBuildConfig config, BuildOptionsAdapter options ) {
		String nonStdOptions = options.getNonStandardOptions();
		if ( null == nonStdOptions || (nonStdOptions.length() == 0)) return;
		
		StringTokenizer tok = new StringTokenizer( nonStdOptions );
		String[] args = new String[ tok.countTokens() ];
		int argCount = 0;
		while ( tok.hasMoreTokens() ) {
			args[argCount++] = tok.nextToken();	
		}

		// set the non-standard options in an alternate build config
		// (we don't want to lose the settings we already have)
		BuildArgParser argParser = new BuildArgParser();
		AjBuildConfig altConfig = argParser.genBuildConfig( args, messageHandler );
		
		// copy the answers across
		config.setNoWeave( altConfig.isNoWeave() );
		config.setXnoInline( altConfig.isXnoInline() );
		config.setXserializableAspects( altConfig.isXserializableAspects());
		config.setLintMode( altConfig.getLintMode() );
		config.setLintSpecFile( altConfig.getLintSpecFile() );		
	}
	/**
	 * Populate options in a build configuration, using the ProjectPropertiesAdapter.
	 * Added by AMC 01.20.2003, bugzilla #29769
	 */
	private void configureProjectOptions( AjBuildConfig config, ProjectPropertiesAdapter properties ) {

		// set the classpath
		String classpathString = 
			properties.getBootClasspath()
			+ File.pathSeparator
			+ properties.getClasspath();
			
		StringTokenizer st = new StringTokenizer(
			classpathString,
			File.pathSeparator
		);
		
		List classpath = new ArrayList();
		while (st.hasMoreTokens()) classpath.add(st.nextToken());

		config.setClasspath(classpath);  
		Ajde.getDefault().logEvent("building with classpath: " + classpath);

		config.setOutputDir(
			new File(properties.getOutputPath())
		);

		// new 1.1 options added by AMC

		Set roots = properties.getSourceRoots();
		if ( null != roots && !roots.isEmpty() ) {		
			List sourceRoots = new ArrayList( roots );
			config.setSourceRoots(sourceRoots);
		}
		
		Set jars = properties.getInJars();
		if ( null != jars && !jars.isEmpty() ) {		
			List inJars = new ArrayList( jars );
			config.setInJars(inJars);
		}
		
		String outJar = properties.getOutJar();
		if ( null != outJar && (outJar.length() > 0) ) {
			config.setOutputJar( new File( outJar ) );	
		}

		Set aspects = properties.getAspectPath();
		if ( null != aspects && !aspects.isEmpty() ) {		
			List aPath = new ArrayList( aspects );
			config.setAspectpath( aPath);
		}

					
	}

	private void init() {
		if (!initialized) {  // XXX plug into AJDE initialization
//			Ajde.getDefault().setErrorHandler(new DebugErrorHandler());
			this.messageHandler = new MessageHandlerAdapter();
			buildManager = new AjBuildManager(messageHandler);
			initialized = true;
		}
	}
	
	class MessageHandlerAdapter implements IMessageHandler {
		private TaskListManager taskListManager;
		private BuildNotifierAdapter buildNotifierAdapter;
		
		public MessageHandlerAdapter() {
			this.taskListManager = Ajde.getDefault().getTaskListManager();
		}	
		
		public boolean handleMessage(IMessage message) throws AbortException {
			if (isIgnoring(message.getKind())) return true;
			
			// ??? relies on only info messages being class-file written messages
			if (message.getKind().equals(IMessage.INFO)) {
				if (buildNotifierAdapter != null) {
					buildNotifierAdapter.generatedBytecode(message.getMessage());
				}
			} else {
				taskListManager.addSourcelineTask(
					message.getMessage(),
					message.getISourceLocation(),
					message.getKind()
				);	
			}
			return true;	
		}
	
		public boolean isIgnoring(IMessage.Kind kind) {
			// XXX implement for INFO, DEBUG?
			return false;
		}
		
		public void setBuildNotifierAdapter(BuildNotifierAdapter buildNotifierAdapter) {
			this.buildNotifierAdapter = buildNotifierAdapter;
		}

	}
}
