/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
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
import java.util.*;

import org.aspectj.ajde.*;
import org.aspectj.ajdt.ajc.*;
import org.aspectj.ajdt.internal.core.builder.*;
import org.aspectj.bridge.*;
import org.aspectj.util.LangUtil;
//import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class CompilerAdapter {

//	private Map optionsMap;
	private AjBuildManager buildManager = null;
    private MessageHandlerAdapter messageHandler = null;
    private BuildNotifierAdapter currNotifier = null;
	private boolean initialized = false;
	private boolean structureDirty = true;
    private boolean showInfoMessages = false;
    // set to false in incremental mode to re-do initial build
	private boolean nextBuild = false; 
	
	public CompilerAdapter() {
		super();
	}

    public void showInfoMessages(boolean show) { // XXX surface in GUI
        showInfoMessages = show;
    }
    public boolean getShowInfoMessages() {
        return showInfoMessages;
    }

    public void nextBuildFresh() {
        if (nextBuild) {
            nextBuild = false;
        }
    }

	public void requestCompileExit() {
		if (currNotifier != null) {
            currNotifier.cancelBuild();
        } else {
            signalText("unable to cancel build process"); 
        }
	}

	public boolean isStructureDirty() {
		return structureDirty;
	}

	public void setStructureDirty(boolean structureDirty) {
		this.structureDirty = structureDirty;
	}

	public boolean compile(String configFile, BuildProgressMonitor progressMonitor, boolean buildModel) {
		if (configFile == null) {
			Ajde.getDefault().getErrorHandler().handleError(
				"Tried to build null config file."
			);
		}
		init();
		try { 
			AjBuildConfig buildConfig = genBuildConfig(configFile);
			buildConfig.setGenerateModelMode(buildModel);
			if (null == buildConfig) {
                return false;
			}
			currNotifier = new BuildNotifierAdapter(progressMonitor);		
			buildManager.setProgressListener(currNotifier);
			messageHandler.setBuildNotifierAdapter(currNotifier);
			
			String rtInfo = buildManager.checkRtJar(buildConfig); // !!! will get called twice
			if (rtInfo != null) {
				Ajde.getDefault().getErrorHandler().handleWarning(
                	  "AspectJ Runtime error: " + rtInfo
		            + "  Please place a valid aspectjrt.jar on the classpath.");
	            return false;
			}
			boolean incrementalEnabled = 
                buildConfig.isIncrementalMode()
                || buildConfig.isIncrementalFileMode();
            if (incrementalEnabled && nextBuild) {
                return buildManager.incrementalBuild(buildConfig, messageHandler);
            } else {
                if (incrementalEnabled) {
                    nextBuild = incrementalEnabled;
                } 
                return buildManager.batchBuild(buildConfig, messageHandler); 
            }
//        } catch (OperationCanceledException ce) {
//			Ajde.getDefault().getErrorHandler().handleWarning(
//				"build cancelled by user");
//            return false;
		} catch (AbortException e) {
            final IMessage message = e.getIMessage();
            if (null == message) {
                signalThrown(e);
            } else if (null != message.getMessage()) {
				Ajde.getDefault().getErrorHandler().handleWarning(message.getMessage());
            } else if (null != message.getThrown()) {
                signalThrown(message.getThrown());
            } else {
                signalThrown(e);
            }
			return false;
		} catch (Throwable t) {
            signalThrown(t);
			return false; 
		} 
	}
    
    /**
     * Generate AjBuildConfig from the local configFile parameter
     * plus global project and build options.
     * Errors signalled using signal... methods.
     * @param configFile	
     * @return null if invalid configuration, 
     *   corresponding AjBuildConfig otherwise
     */
	public AjBuildConfig genBuildConfig(String configFilePath) {
        init();
	    File configFile = new File(configFilePath);
        if (!configFile.exists()) {
			Ajde.getDefault().getErrorHandler().handleError(
				"Config file \"" + configFile + "\" does not exist."
			);
            return null;
        }
        String[] args = new String[] { "@" + configFile.getAbsolutePath() };
        CountingMessageHandler handler 
            = CountingMessageHandler.makeCountingMessageHandler(messageHandler);
		BuildArgParser parser = new BuildArgParser(handler);
		
        AjBuildConfig config = parser.genBuildConfig(args, false, configFile);  
		configureProjectOptions(config, Ajde.getDefault().getProjectProperties());  // !!! not what the API intended

		// -- get globals, treat as defaults used if no local values
		AjBuildConfig global = new AjBuildConfig();
		// AMC refactored into two methods to populate buildConfig from buildOptions and
		// project properties - bugzilla 29769.
		BuildOptionsAdapter buildOptions 
			= Ajde.getDefault().getBuildManager().getBuildOptions();
		if (!configureBuildOptions(global, buildOptions, handler)) {
			return null;
		}
		ProjectPropertiesAdapter projectOptions =
			Ajde.getDefault().getProjectProperties();
		configureProjectOptions(global, projectOptions);
		config.installGlobals(global);

		ISourceLocation location = null;
		if (config.getConfigFile() != null) {
			location = new SourceLocation(config.getConfigFile(), 0); 
		}
        
		String message = parser.getOtherMessages(true);
		if (null != message) {  
			IMessage m = new Message(message, IMessage.ERROR, null, location);            
			handler.handleMessage(m);
		}
        
        // always force model generation in AJDE
        config.setGenerateModelMode(true);       
		if (Ajde.getDefault().getBuildManager().getBuildOptions().getJavaOptionsMap() != null) {
			config.getJavaOptions().putAll(Ajde.getDefault().getBuildManager().getBuildOptions().getJavaOptionsMap());
		}
		return config;
//        return fixupBuildConfig(config);
	}

//    /**
//     * Fix up build configuration just before using to compile.
//     * This should be delegated to a BuildAdapter callback (XXX)
//     * for implementation-specific value checks
//     * (e.g., to force use of project classpath rather
//     * than local config classpath).
//     * This implementation does no checks and returns local.
//     * @param local the AjBuildConfig generated to validate
//     * @param global
//     * @param buildOptions
//     * @param projectOptions
//     * @return null if unable to fix problems or fixed AjBuildConfig if no errors
//     * 
//     */
//    protected AjBuildConfig fixupBuildConfig(AjBuildConfig local) {
//		if (Ajde.getDefault().getBuildManager().getBuildOptions().getJavaOptionsMap() != null) {
//			local.getJavaOptions().putAll(Ajde.getDefault().getBuildManager().getBuildOptions().getJavaOptionsMap());
//		}
//        return local;
//    }

//    /** signal error text to user */
//    protected void signalError(String text) {
        
//    } 
//    /** signal warning text to user */
//    protected void signalWarning(String text) {
//        
//    }

    /** signal text to user */
    protected void signalText(String text) {
        Ajde.getDefault().getIdeUIAdapter().displayStatusInformation(text);
    }

    /** signal Throwable to user (summary in GUI, trace to stdout). */
    protected void signalThrown(Throwable t) { // nothing to error handler?
        String text = LangUtil.unqualifiedClassName(t) 
            + " thrown: " 
            + t.getMessage();
        Ajde.getDefault().getErrorHandler().handleError(text, t);
    }

	/**
	 * Populate options in a build configuration, using the Ajde BuildOptionsAdapter.
	 * Added by AMC 01.20.2003, bugzilla #29769
	 */
	private static boolean configureBuildOptions( AjBuildConfig config, BuildOptionsAdapter options, IMessageHandler handler) {
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
			// default is all options on, so just need to selectively
			// disable
			boolean sourceLine = false;
			boolean varAttr = false;
			boolean lineNo = false;
			Iterator it = debugOptions.iterator();
			while (it.hasNext()){
				String debug = (String) it.next();
				if ( debug.equals( BuildOptionsAdapter.DEBUG_ALL )) {
					sourceLine = true;
					varAttr = true;
					lineNo = true;
				} else if ( debug.equals( BuildOptionsAdapter.DEBUG_LINES )) {
					lineNo = true;
				}  else if ( debug.equals( BuildOptionsAdapter.DEBUG_SOURCE )) {
					sourceLine = true;
				}  else if ( debug.equals( BuildOptionsAdapter.DEBUG_VARS)) {
					varAttr = true;
				}
			}
			if (sourceLine) javaOptions.put(CompilerOptions.OPTION_SourceFileAttribute,
											CompilerOptions.GENERATE);
			if (varAttr) javaOptions.put(CompilerOptions.OPTION_LocalVariableAttribute,
											CompilerOptions.GENERATE);		
			if (lineNo)  javaOptions.put(CompilerOptions.OPTION_LineNumberAttribute,
											CompilerOptions.GENERATE);
		}
		//XXX we can't turn off import errors in 3.0 stream
//		if ( options.getNoImportError() ) {
//			javaOptions.put( CompilerOptions.OPTION_ReportInvalidImport,
//				CompilerOptions.WARNING);	
//		}
				
		if ( options.getPreserveAllLocals() ) {
			javaOptions.put( CompilerOptions.OPTION_PreserveUnusedLocal,
				CompilerOptions.PRESERVE);		
		}
        if ( !config.isIncrementalMode()
            && options.getIncrementalMode() ) {
                config.setIncrementalMode(true);
        }
        				
		config.setJavaOptions( javaOptions );
		String toAdd = options.getNonStandardOptions();
        return LangUtil.isEmpty(toAdd) 
            ? true
            : configureNonStandardOptions( config, toAdd, handler );
        // ignored: lenient, porting, preprocess, strict, usejavac, workingdir
	}
	
	/**
	 * Helper method for configureBuildOptions
	 */
	private static void disableWarnings( Map options ) {
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
	private static void enableWarnings( Map options, Set warnings ) {
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
	 * Helper method for configure build options.
     * This reads all command-line options specified
     * in the non-standard options text entry and
     * sets any corresponding unset values in config.
     * @return false if config failed
	 */
	private static boolean configureNonStandardOptions(
        AjBuildConfig config, 
        String nonStdOptions,
        IMessageHandler messageHandler ) {

        if (LangUtil.isEmpty(nonStdOptions)) {
            return true;
        }
		
		StringTokenizer tok = new StringTokenizer( nonStdOptions );
		String[] args = new String[ tok.countTokens() ];
		int argCount = 0;
		while ( tok.hasMoreTokens() ) {
			args[argCount++] = tok.nextToken();	
		}

		// set the non-standard options in an alternate build config
		// (we don't want to lose the settings we already have)
        CountingMessageHandler counter 
            = CountingMessageHandler.makeCountingMessageHandler(messageHandler);
		AjBuildConfig altConfig = AjdtCommand.genBuildConfig(args, counter);
		if (counter.hasErrors()) {
            return false;
        }
        // copy globals where local is not set
        config.installGlobals(altConfig);
        return true;
    }

	/**
	 * Add new options from the ProjectPropertiesAdapter to the configuration.
     * <ul>
     * <li>New list entries are added if not duplicates in,
     *     for classpath, aspectpath, injars, inpath and sourceroots</li>
     * <li>New bootclasspath entries are ignored XXX</li>
     * <li>Set only one new entry for output dir or output jar
     *     only if there is no output dir/jar entry in the config</li>
     * </ul>
     * Subsequent changes to the ProjectPropertiesAdapter will not affect
     * the configuration.
	 * <p>Added by AMC 01.20.2003, bugzilla #29769
	 */
	private void configureProjectOptions( AjBuildConfig config, ProjectPropertiesAdapter properties ) {
        // XXX no error handling in copying project properties
        String propcp = properties.getClasspath(); // XXX omitting bootclasspath...
        if (!LangUtil.isEmpty(propcp)) {
            StringTokenizer st = new StringTokenizer(propcp, File.pathSeparator);
            List configClasspath = config.getClasspath();
            ArrayList toAdd = new ArrayList();
            while (st.hasMoreTokens()) {
                String entry = st.nextToken();
                if (!configClasspath.contains(entry)) {
                    toAdd.add(entry);
                }
            }
            if (0 < toAdd.size()) {
                ArrayList both = new ArrayList(configClasspath.size() + toAdd.size());
                both.addAll(configClasspath);
                both.addAll(toAdd);
                config.setClasspath(both);
                Ajde.getDefault().logEvent("building with classpath: " + both);
            }
        }

        // set outputdir and outputjar only if both not set
        if ((null == config.getOutputDir() && (null == config.getOutputJar()))) {
            String outPath = properties.getOutputPath();
            if (!LangUtil.isEmpty(outPath)) {
                config.setOutputDir(new File(outPath));
            } 
            String outJar = properties.getOutJar();
            if (!LangUtil.isEmpty(outJar)) {
                config.setOutputJar(new File( outJar ) );  
            }
        }

        join(config.getSourceRoots(), properties.getSourceRoots());
        join(config.getInJars(), properties.getInJars());
        join(config.getInpath(),properties.getInpath());
		config.setSourcePathResources(properties.getSourcePathResources());
        join(config.getAspectpath(), properties.getAspectPath());
	}

    void join(Collection target, Collection source) {  // XXX dup Util
        if ((null == target) || (null == source)) {
            return;
        }
        for (Iterator iter = source.iterator(); iter.hasNext();) {
            Object next = iter.next();
            if (! target.contains(next)) {
                target.add(next);
            }
        }
    }

	private void init() {
		if (!initialized) {  // XXX plug into AJDE initialization
//			Ajde.getDefault().setErrorHandler(new DebugErrorHandler());
			this.messageHandler = new MessageHandlerAdapter();
			buildManager = new AjBuildManager(messageHandler);
            // XXX need to remove the properties file each time!
			initialized = true;
		}
	}
	
	class MessageHandlerAdapter extends MessageHandler {
		private TaskListManager taskListManager;
		private BuildNotifierAdapter buildNotifierAdapter;
		
		public MessageHandlerAdapter() {
			this.taskListManager = Ajde.getDefault().getTaskListManager();
		}	

		public boolean handleMessage(IMessage message) throws AbortException {
            IMessage.Kind kind = message.getKind(); 
            if (isIgnoring(kind) 
                || (!showInfoMessages && IMessage.INFO.equals(kind))) {
                    return true;
                }
			
			taskListManager.addSourcelineTask(message);
			return super.handleMessage(message); // also store...	
		}
        // --------------- adje methods
		public void setBuildNotifierAdapter(BuildNotifierAdapter buildNotifierAdapter) {
			this.buildNotifierAdapter = buildNotifierAdapter;
		}
	}
} 
