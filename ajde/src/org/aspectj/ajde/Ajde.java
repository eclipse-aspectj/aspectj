/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde;

import org.aspectj.ajde.internal.AspectJBuildManager;
import org.aspectj.ajde.internal.LstBuildConfigManager;
import org.aspectj.ajde.ui.IdeUIAdapter;
import org.aspectj.ajde.ui.StructureSearchManager;
import org.aspectj.ajde.ui.StructureViewManager;
import org.aspectj.ajde.ui.StructureViewNodeFactory;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Version;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.util.LangUtil;
import org.aspectj.util.Reflection;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Singleton class responsible for AJDE initialization, and the main point of access to
 * Ajde functionality. 
 *
 * @author Mik Kersten
 */
public class Ajde {    

	private static final Ajde INSTANCE = new Ajde();
	private static final String NOT_INITIALIZED_MESSAGE = "Ajde is not initialized.";
	private static boolean isInitialized = false;
	private static int compatibilityLevel = 1; // Used by org.aspectj.ajde upgrade task
    
	private BuildManager buildManager;
//	private EditorManager editorManager;
	private EditorAdapter editorAdapter;
	private StructureViewManager structureViewManager;
	private StructureSearchManager structureSearchManager;
	private BuildConfigManager configurationManager ;
	private ProjectPropertiesAdapter projectProperties;
	private TaskListManager taskListManager;
	private IdeUIAdapter ideUIAdapter;
	private ErrorHandler errorHandler;
	private PrintStream logPrintStream = null;
	private IMessageHandler messageHandler = null; // allow provision of custom handler
	
	/**
	 * This class can only be constructured by itself (as a singleton) or by sub-classes. 
	 */ 
	protected Ajde() {
		configurationManager = new LstBuildConfigManager();
	}

	/**
	 * This method must be called before using Ajde.  A <CODE>RuntimeException</CODE> will
	 * be thrown if use is attempted before initialization.
	 */
	public static void init(
			EditorAdapter editorAdapter,
			TaskListManager taskListManager,
			BuildProgressMonitor compileProgressMonitor,
			ProjectPropertiesAdapter projectProperties,
			BuildOptionsAdapter buildOptionsAdapter,
			StructureViewNodeFactory structureViewNodeFactory,
			IdeUIAdapter ideUIAdapter,
			ErrorHandler errorHandler) {
		try {
			INSTANCE.projectProperties = projectProperties;
			INSTANCE.errorHandler = errorHandler;
			INSTANCE.taskListManager = taskListManager;
//			INSTANCE.editorManager = new EditorManager(editorAdapter);
			INSTANCE.editorAdapter = editorAdapter;
			INSTANCE.buildManager = new AspectJBuildManager(
				taskListManager, 
				compileProgressMonitor,
				buildOptionsAdapter);
 
			INSTANCE.buildManager.addListener(INSTANCE.BUILD_STATUS_LISTENER);
			INSTANCE.configurationManager.addListener(INSTANCE.STRUCTURE_UPDATE_CONFIG_LISTENER);
			INSTANCE.ideUIAdapter = ideUIAdapter;
			
			INSTANCE.structureSearchManager = new StructureSearchManager();	
			INSTANCE.structureViewManager = new StructureViewManager(structureViewNodeFactory);
			
			isInitialized = true;
//			INSTANCE.enableLogging(System.out); 
		} catch (Throwable t) {
			System.err.println("AJDE ERROR: could not initialize Ajde.");
			t.printStackTrace();	
		}
	}   

	/**
	 * @return	the default singleton instance of <CODE>Ajde</CODE>
	 */
	public static Ajde getDefault() {
		if (!isInitialized) throw new RuntimeException(NOT_INITIALIZED_MESSAGE);
		return INSTANCE;	
	}

	/**
	 * Set a <CODE>ConfigurationManager</CODE> to use instead of the default one.
	 */ 
	public void setConfigurationManager(BuildConfigManager configurationManager) {
		this.configurationManager = configurationManager;	
	}
	
	/**
	 * Call this method with a custom IMessageHandler to override the default message
	 * handling.
	 * @param aHandler
	 */
	public void setMessageHandler(IMessageHandler aHandler) {
		this.messageHandler = aHandler;
	}
	
	public IMessageHandler getMessageHandler() {
		return messageHandler;
	}

	public BuildManager getBuildManager() {
		return buildManager;
	}
	
//	public EditorManager getEditorManager() {
//		return editorManager;
//	}	
	
	public EditorAdapter getEditorAdapter() {
		return editorAdapter;
	}
	
	public StructureViewManager getStructureViewManager() {
		return structureViewManager;	
	}

	public StructureSearchManager getStructureSearchManager() {
		return structureSearchManager;
	}
	
	public BuildConfigManager getConfigurationManager() {
		return configurationManager;
	}
	
	public ProjectPropertiesAdapter getProjectProperties() {
		return projectProperties;
	}
	
	public TaskListManager getTaskListManager() {
		return taskListManager;
	}

	public IdeUIAdapter getIdeUIAdapter() {
		return ideUIAdapter;
	}

	public void setIdeUIAdapter(IdeUIAdapter ideUIAdapter) {
		this.ideUIAdapter = ideUIAdapter;
	}
	
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	public String getVersion() {
		return Version.text;
	}

	public void enableLogging(PrintStream logPrintStream) {
		this.logPrintStream = logPrintStream;
	}
	
	public void disableLogging() {
		this.logPrintStream = null;	
	}
	
	public boolean isLogging() {
		return (this.logPrintStream!=null);
	}
	
	/**
	 * The structure manager is not a part of the public API and its
	 * use should be avoided.  Used <CODE>getStructureViewManager()</CODE>
	 * instead.
	 */
	public AsmManager getStructureModelManager() {
		return AsmManager.getDefault();	
	}
	
	public void logEvent(String message) {
		if (logPrintStream != null) {
			logPrintStream.println("<AJDE> " + message);	
		}	
	}

    /**
     * Utility to run the project main class from the project
     * properties in the same VM
     * using a class loader populated with the classpath
     * and output path or jar.
     * Errors are logged to the ErrorHandler.
     * @param project the ProjectPropertiesAdapter specifying the
     * main class, classpath, and executable arguments.
     * @return Thread running with process, or null if unable to start
     */
    public Thread runInSameVM() {
        final RunProperties props 
            = new RunProperties(getProjectProperties(), getErrorHandler());
        if (!props.valid) {
            return null; // error already handled
        }
        Runnable runner = new Runnable() {
            public void run() {
                try {            
                    Reflection.runMainInSameVM(
                        props.classpath, 
                        props.mainClass, 
                        props.args); 
                } catch(Throwable e) {
                    Ajde.getDefault().getErrorHandler().handleError("Error running " + props.mainClass, e);
                }
            }
        };
        Thread result = new Thread(runner, props.mainClass);
        result.start();
        return result;
    }

    /**
     * Utility to run the project main class from the project
     * properties in a new VM.
     * Errors are logged to the ErrorHandler.
     * @return LangUtil.ProcessController running with process, 
     *         or null if unable to start
     */
    public LangUtil.ProcessController runInNewVM() {
        final RunProperties props 
            = new RunProperties(getProjectProperties(), getErrorHandler());
        if (!props.valid) {
            return null; // error already handled
        }
        // setup to run asynchronously, pipe streams through, and report errors
        final StringBuffer command = new StringBuffer();
        LangUtil.ProcessController controller
            = new LangUtil.ProcessController() {
                public void doCompleting(Throwable thrown, int result) {
                    LangUtil.ProcessController.Thrown any = getThrown(); 
                    if (!any.thrown && (null == thrown) && (0 == result)) {
                        return; // no errors
                    }
                    // handle errors
                    String context = props.mainClass 
                        + " command \"" 
                        + command 
                        + "\"";
                    if (null != thrown) {
                        String m = "Exception running " + context;
                        getErrorHandler().handleError(m, thrown);
                    } else if (0 != result) {
                        String m = "Result of running " + context;
                        getErrorHandler().handleError(m + ": " + result);
                    }
                    if (null != any.fromInPipe) {
                        String m = "Error processing input pipe for " + context;
                        getErrorHandler().handleError(m, any.fromInPipe);
                    }
                    if (null != any.fromOutPipe) {
                        String m = "Error processing output pipe for " + context;
                        getErrorHandler().handleError(m, any.fromOutPipe);
                    }
                    if (null != any.fromErrPipe) {
                        String m = "Error processing error pipe for " + context;
                        getErrorHandler().handleError(m, any.fromErrPipe);
                    }
                }
            };
            
        controller = LangUtil.makeProcess(
                        controller, 
                        props.classpath, 
                        props.mainClass, 
                        props.args);
                        
        command.append(Arrays.asList(controller.getCommand()).toString());

        // now run the process
        controller.start();
        return controller;
    }

	private final BuildConfigListener STRUCTURE_UPDATE_CONFIG_LISTENER = new BuildConfigListener() {
		public void currConfigChanged(String configFilePath) {
			if (configFilePath != null) Ajde.getDefault().getStructureModelManager().readStructureModel(configFilePath);
		}
		
		public void configsListUpdated(List configsList) { }
	};
	
	private final BuildListener BUILD_STATUS_LISTENER = new BuildListener() {
    	
    	/**
    	 * Writes the default configuration file if it has been selected for compilation
    	 */
    	public void compileStarted(String buildConfig) { 
    		String configFilePath = projectProperties.getDefaultBuildConfigFile();
    		if (buildConfig.equals(configFilePath)) {
	    		configurationManager.writePaths(configFilePath, projectProperties.getProjectSourceFiles());	
	    		logEvent("wrote default build config: " + configFilePath);
    		}
    	} 
    	
    	/**
    	 * The strucutre model is annotated with error messages after an unsuccessful compile.
    	 */
        public void compileFinished(String buildConfig, int buildTime, boolean succeeded, boolean warnings) { 
//        	String configFilePath = projectProperties.getDefaultBuildConfigFile();
        	if (!succeeded) {
	        	AsmManager.getDefault().fireModelUpdated();	
    	    }
        }
        
        /**
         * Ignored.
         */
        public void compileAborted(String buildConfigFile, String message) { }
    };
    
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

    /** struct class to interpret project properties */
    private static class RunProperties {
        final String mainClass;
        final String classpath;
        final String[] args;
        final boolean valid;
        RunProperties(
            ProjectPropertiesAdapter project, 
            ErrorHandler handler) {
            // XXX really run arbitrary handler in constructor? hmm.
            LangUtil.throwIaxIfNull(project, "project");
            LangUtil.throwIaxIfNull(handler, "handler");
            String mainClass = null;
            String classpath = null;            
            String[] args = null;
            boolean valid = false;
            
            mainClass = project.getClassToExecute();
            if (LangUtil.isEmpty(mainClass)) {
                handler.handleWarning("No main class specified");
            } else {
                classpath = LangUtil.makeClasspath(
                    project.getBootClasspath(),
                    project.getClasspath(),
                    project.getOutputPath(),
                    project.getOutJar());
                if (LangUtil.isEmpty(classpath)) {
                    handler.handleWarning("No classpath specified");
                } else {
                    args = LangUtil.split(project.getExecutionArgs());
                    valid = true;
                }
            }
            this.mainClass = mainClass;
            this.classpath = classpath;
            this.args = args;
            this.valid = valid;
        }
    }

    /** 
     * Returns true if the compiler is compatible with Java 6
     * (which it will do when the compiler is upgraded to the
     * jdt 3.2 compiler) and false otherwise
	 */
    public boolean compilerIsJava6Compatible() {
        // If it doesn't understand the jdklevel, versionToJdkLevel returns 0
    	return CompilerOptions.versionToJdkLevel(BuildOptionsAdapter.VERSION_16) != 0;
    }


}


