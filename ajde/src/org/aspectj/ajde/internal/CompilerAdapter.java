/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.util.LangUtil;
//import org.eclipse.core.runtime.OperationCanceledException;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class CompilerAdapter {
	
	private static final Set DEFAULT__AJDE_WARNINGS;
	
	static {
		DEFAULT__AJDE_WARNINGS = new HashSet();
		DEFAULT__AJDE_WARNINGS.add(BuildOptionsAdapter.WARN_ASSERT_IDENITIFIER);
		DEFAULT__AJDE_WARNINGS.add(BuildOptionsAdapter.WARN_CONSTRUCTOR_NAME);
		DEFAULT__AJDE_WARNINGS.add(BuildOptionsAdapter.WARN_DEPRECATION);
		DEFAULT__AJDE_WARNINGS.add(BuildOptionsAdapter.WARN_MASKED_CATCH_BLOCKS);
		DEFAULT__AJDE_WARNINGS.add(BuildOptionsAdapter.WARN_PACKAGE_DEFAULT_METHOD);
		DEFAULT__AJDE_WARNINGS.add(BuildOptionsAdapter.WARN_UNUSED_IMPORTS);
//		DEFAULT__AJDE_WARNINGS.put(BuildOptionsAdapter.WARN_);
//		DEFAULT__AJDE_WARNINGS.put(BuildOptionsAdapter.WARN_);
	}
	
//	private Map optionsMap;
	private AjBuildManager buildManager = null;
    private IMessageHandler messageHandler = null;
    private BuildNotifierAdapter currNotifier = null;
	private boolean initialized = false;
	private boolean structureDirty = true;
    // set to false in incremental mode to re-do initial build
	private boolean nextBuild = false; 
	
	public CompilerAdapter() {
		super();
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
			Ajde.getDefault().getErrorHandler().handleError("Tried to build null config file.");
		}
		init();
		try { 
			CompilationAndWeavingContext.reset();
			AjBuildConfig buildConfig = genBuildConfig(configFile);
			if (buildConfig == null) {
                return false;
			}
			buildConfig.setGenerateModelMode(buildModel);
			currNotifier = new BuildNotifierAdapter(progressMonitor, buildManager);		
			buildManager.setProgressListener(currNotifier);
			
			boolean incrementalEnabled = 
                buildConfig.isIncrementalMode()
                || buildConfig.isIncrementalFileMode();
			boolean successfulBuild;
            if (incrementalEnabled && nextBuild) {
				successfulBuild = buildManager.incrementalBuild(buildConfig, messageHandler);
            } else {
                if (incrementalEnabled) {
                    nextBuild = incrementalEnabled;
                } 
				successfulBuild = buildManager.batchBuild(buildConfig, messageHandler); 
            }
			IncrementalStateManager.recordSuccessfulBuild(configFile,buildManager.getState());
			return successfulBuild;
//        } catch (OperationCanceledException ce) {
//			Ajde.getDefault().getErrorHandler().handleWarning(
//				"build cancelled by user");
//            return false;
		} catch (AbortException e) {
            final IMessage message = e.getIMessage();
            if (message == null) {
            	signalThrown(e);
            } else {
            	String messageText = message.getMessage() + "\n" + CompilationAndWeavingContext.getCurrentContext();
            	Ajde.getDefault().getErrorHandler().handleError(messageText, message.getThrown());
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
		
		AjBuildConfig config = new AjBuildConfig();
        parser.populateBuildConfig(config, args, false, configFile);  
		configureBuildOptions(config,Ajde.getDefault().getBuildManager().getBuildOptions(),handler);
		configureProjectOptions(config, Ajde.getDefault().getProjectProperties());  // !!! not what the API intended

//		// -- get globals, treat as defaults used if no local values
//		AjBuildConfig global = new AjBuildConfig();
//		// AMC refactored into two methods to populate buildConfig from buildOptions and
//		// project properties - bugzilla 29769.
//		BuildOptionsAdapter buildOptions 
//			= Ajde.getDefault().getBuildManager().getBuildOptions();
//		if (!configureBuildOptions(/* global */ config, buildOptions, handler)) {
//			return null;
//		}
//		ProjectPropertiesAdapter projectOptions =
//			Ajde.getDefault().getProjectProperties();
//		configureProjectOptions(global, projectOptions);
//		config.installGlobals(global);

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
			config.getOptions().set(Ajde.getDefault().getBuildManager().getBuildOptions().getJavaOptionsMap());
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
	private boolean configureBuildOptions( AjBuildConfig config, BuildOptionsAdapter options, IMessageHandler handler) {
        LangUtil.throwIaxIfNull(options, "options");
        LangUtil.throwIaxIfNull(config, "config");
		Map optionsToSet = new HashMap();
        LangUtil.throwIaxIfNull(optionsToSet, "javaOptions");
        
        checkNotAskedForJava6Compliance(options);

        if (options.getSourceCompatibilityLevel() != null && options.getSourceCompatibilityLevel().equals(CompilerOptions.VERSION_1_5)) {
		    optionsToSet.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		    optionsToSet.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5); 
		    config.setBehaveInJava5Way(true);
		} else if (options.getSourceCompatibilityLevel() != null && options.getSourceCompatibilityLevel().equals(CompilerOptions.VERSION_1_4)) {
		    optionsToSet.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);	 
			optionsToSet.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
		} 
		
		String enc = options.getCharacterEncoding();
		if (!LangUtil.isEmpty(enc)) {
			optionsToSet.put(CompilerOptions.OPTION_Encoding, enc );
		}

		String compliance = options.getComplianceLevel();
		if (!LangUtil.isEmpty(compliance)) {
			String version = CompilerOptions.VERSION_1_4;
			if ( compliance.equals( BuildOptionsAdapter.VERSION_13 ) ) {
				version = CompilerOptions.VERSION_1_3;
			} else if (compliance.equals(BuildOptionsAdapter.VERSION_15)) {
				version = CompilerOptions.VERSION_1_5;
				config.setBehaveInJava5Way(true);
			}
			optionsToSet.put(CompilerOptions.OPTION_Compliance, version );	
			optionsToSet.put(CompilerOptions.OPTION_Source, version );
		}
				
		String sourceLevel = options.getSourceCompatibilityLevel();
		if (!LangUtil.isEmpty(sourceLevel)) {
			String slVersion = CompilerOptions.VERSION_1_4;
			if ( sourceLevel.equals( BuildOptionsAdapter.VERSION_13 ) ) {
				slVersion = CompilerOptions.VERSION_1_3;
			}
			// never set a lower source level than compliance level
			// Mik: prepended with 1.5 check
			if (sourceLevel.equals(CompilerOptions.VERSION_1_5)) {
			    optionsToSet.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
			    config.setBehaveInJava5Way(true);
			} else {
				if (optionsToSet.containsKey(CompilerOptions.OPTION_Compliance)) {
					String setCompliance = (String) optionsToSet.get(CompilerOptions.OPTION_Compliance);
					if (setCompliance.equals(CompilerOptions.VERSION_1_5)) {
						optionsToSet.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
						config.setBehaveInJava5Way(true);
					} else if ( ! (setCompliance.equals(CompilerOptions.VERSION_1_4) 
							&& slVersion.equals(CompilerOptions.VERSION_1_3)) ) {
					    optionsToSet.put(CompilerOptions.OPTION_Source, slVersion);		
					} 
				}
			}
		}
	
		Set warnings = options.getWarnings();
		if (!LangUtil.isEmpty(warnings)) {
			// turn off all warnings	
			disableWarnings( optionsToSet );
			// then selectively enable those in the set
			enableWarnings( optionsToSet, warnings );
		} else if (warnings == null) {
			// set default warnings on...
			enableWarnings( optionsToSet, DEFAULT__AJDE_WARNINGS);
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
			if (sourceLine) optionsToSet.put(CompilerOptions.OPTION_SourceFileAttribute,
											CompilerOptions.GENERATE);
			if (varAttr) optionsToSet.put(CompilerOptions.OPTION_LocalVariableAttribute,
											CompilerOptions.GENERATE);		
			if (lineNo)  optionsToSet.put(CompilerOptions.OPTION_LineNumberAttribute,
											CompilerOptions.GENERATE);
		}
		//XXX we can't turn off import errors in 3.0 stream
//		if ( options.getNoImportError() ) {
//			javaOptions.put( CompilerOptions.OPTION_ReportInvalidImport,
//				CompilerOptions.WARNING);	
//		}
				
		if ( options.getPreserveAllLocals() ) {
			optionsToSet.put( CompilerOptions.OPTION_PreserveUnusedLocal,
				CompilerOptions.PRESERVE);		
		}
        if ( !config.isIncrementalMode()
            && options.getIncrementalMode() ) {
                config.setIncrementalMode(true);
        }
        				
		Map jom = options.getJavaOptionsMap();
		if (jom!=null) {
			String version = (String)jom.get(CompilerOptions.OPTION_Compliance);
			if (version!=null && version.equals(CompilerOptions.VERSION_1_5)) {
				config.setBehaveInJava5Way(true);
			}
		}
		
		config.getOptions().set(optionsToSet);
		String toAdd = options.getNonStandardOptions();
        return LangUtil.isEmpty(toAdd) 
            ? true
            : configureNonStandardOptions( config, toAdd, handler );
        // ignored: lenient, porting, preprocess, strict, usejavac, workingdir
	}
	
	/**
	 * Check that the user hasn't specified Java 6 for the compliance, source and
	 * target levels. If they have then an error is thrown. 
	 */
	private void checkNotAskedForJava6Compliance(BuildOptionsAdapter options) {
		// bug 164384 - Throwing an IMessage.ERRROR rather than an IMessage.ABORT 
		// means that we'll continue to try to compile the code. This means that
		// the user may see other errors (for example, if they're using annotations
		// then they'll get errors saying that they require 5.0 compliance).
		// Throwing IMessage.ABORT would prevent this, however, 'abort' is really
		// for compiler exceptions.
		String compliance = options.getComplianceLevel();
		if (!LangUtil.isEmpty(compliance) 
				&& compliance.equals(BuildOptionsAdapter.VERSION_16)){
			String msg = "Java 6.0 compliance level is unsupported";
			IMessage m = new Message(msg, IMessage.ERROR, null, null);            
			messageHandler.handleMessage(m);
			return;
		}
		String source = options.getSourceCompatibilityLevel();
		if (!LangUtil.isEmpty(source) 
				&& source.equals(BuildOptionsAdapter.VERSION_16)){
			String msg = "Java 6.0 source level is unsupported";
			IMessage m = new Message(msg, IMessage.ERROR, null, null);            
			messageHandler.handleMessage(m);
			return;
		}
		Map javaOptions = options.getJavaOptionsMap();
		if (javaOptions != null){
			String version = (String)javaOptions.get(CompilerOptions.OPTION_Compliance);
			String sourceVersion = (String)javaOptions.get(CompilerOptions.OPTION_Source);
			String targetVersion = (String)javaOptions.get(CompilerOptions.OPTION_TargetPlatform);
			if (version!=null && version.equals(BuildOptionsAdapter.VERSION_16)) {
				String msg = "Java 6.0 compliance level is unsupported";
				IMessage m = new Message(msg, IMessage.ERROR, null, null);            
				messageHandler.handleMessage(m);
			} else if (sourceVersion!=null && sourceVersion.equals(BuildOptionsAdapter.VERSION_16)) {
				String msg = "Java 6.0 source level is unsupported";
				IMessage m = new Message(msg, IMessage.ERROR, null, null);            
				messageHandler.handleMessage(m);				
			} else if (targetVersion!=null && targetVersion.equals(BuildOptionsAdapter.VERSION_16)) {
				String msg = "Java 6.0 target level is unsupported";
				IMessage m = new Message(msg, IMessage.ERROR, null, null);            
				messageHandler.handleMessage(m);	
			}
		}
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


	/** Local helper method for splitting option strings */
	private static List tokenizeString(String str) {
		List tokens = new ArrayList();
		StringTokenizer tok = new StringTokenizer(str);
		while ( tok.hasMoreTokens() ) {
			tokens.add(tok.nextToken());	
		}
		return tokens;
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
		
		// Break a string into a string array of non-standard options.
		// Allows for one option to include a ' '.   i.e. assuming it has been quoted, it
		// won't accidentally get treated as a pair of options (can be needed for xlint props file option)
		List tokens = new ArrayList();
		int ind = nonStdOptions.indexOf('\"');
		int ind2 = nonStdOptions.indexOf('\"',ind+1);
		if ((ind > -1) && (ind2 > -1)) { // dont tokenize within double quotes
			String pre = nonStdOptions.substring(0,ind);
			String quoted = nonStdOptions.substring(ind+1,ind2);
			String post = nonStdOptions.substring(ind2+1,nonStdOptions.length());
			tokens.addAll(tokenizeString(pre));
			tokens.add(quoted);
			tokens.addAll(tokenizeString(post));
		} else {
			tokens.addAll(tokenizeString(nonStdOptions));
		}
		String[] args = (String[])tokens.toArray(new String[]{});
		
		
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
     * <li>Set only one new entry for output dir or output jar
     *     only if there is no output dir/jar entry in the config</li>
     * </ul>
     * Subsequent changes to the ProjectPropertiesAdapter will not affect
     * the configuration.
	 * <p>Added by AMC 01.20.2003, bugzilla #29769
	 */
	private void configureProjectOptions( AjBuildConfig config, ProjectPropertiesAdapter properties ) {
        // XXX no error handling in copying project properties
		// Handle regular classpath
        String propcp = properties.getClasspath();
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

        // Handle boot classpath
        propcp = properties.getBootClasspath();
        if (!LangUtil.isEmpty(propcp)) {
            StringTokenizer st = new StringTokenizer(propcp, File.pathSeparator);
            List configClasspath = config.getBootclasspath();
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
                config.setBootclasspath(both);
                Ajde.getDefault().logEvent("building with boot classpath: " + both);
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
        
        // set compilation result destination manager if not set
        OutputLocationManager outputLocationManager = properties.getOutputLocationManager();
        if (config.getCompilationResultDestinationManager() == null &&
        	outputLocationManager != null) {
        	config.setCompilationResultDestinationManager(new OutputLocationAdapter(outputLocationManager));
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
			if (Ajde.getDefault().getMessageHandler() != null) {
				this.messageHandler = Ajde.getDefault().getMessageHandler();
			} else {
				this.messageHandler = new AjdeMessageHandler();
			}
			buildManager = new AjBuildManager(messageHandler);
			buildManager.environmentSupportsIncrementalCompilation(true);
            // XXX need to remove the properties file each time!
			initialized = true;
		}
	}
	
	public void setState(AjState buildState) {
		buildManager.setState(buildState);	
		buildManager.setStructureModel(buildState.getStructureModel());
	}
	
	public IMessageHandler getMessageHandler() {
		if (messageHandler == null) {
			init();
		}
		return messageHandler;
	}

	public boolean wasFullBuild() {
		return buildManager.wasFullBuild();
	}
} 
