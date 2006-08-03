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


package org.aspectj.ajde.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildListener;
import org.aspectj.ajde.BuildManager;
import org.aspectj.ajde.BuildOptionsAdapter;
import org.aspectj.ajde.BuildProgressMonitor;
import org.aspectj.ajde.ProjectPropertiesAdapter;
import org.aspectj.ajde.TaskListManager;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.util.ConfigParser;

/**
 * Responsible for the build process, including compiler invocation, threading, and error
 * reporting.
 *
 * @author Mik Kersten
 */
public class AspectJBuildManager implements BuildManager {
	
	private CompilerAdapter compilerAdapter = null;
    private TaskListManager compilerMessages = null;
    private BuildProgressMonitor progressMonitor = null;
    private BuildOptionsAdapter buildOptions = null;
    private ArrayList compilerListeners = new ArrayList();
    private String configFile = "";
    private String lastConfigFile = null;
    private int lastCompileTime = 50;
//    private boolean buildStructureOnly = false;
	private boolean buildModelMode = true;

    public AspectJBuildManager(
    	TaskListManager compilerMessages, 
    	BuildProgressMonitor progressMonitor,
    	BuildOptionsAdapter buildOptions) {
        this.compilerMessages = compilerMessages;
        this.progressMonitor = progressMonitor;
        this.buildOptions = buildOptions;
        this.compilerAdapter = new CompilerAdapter();
    }

    public void buildFresh() {
        dobuild(true);
    }
    
    /** this implementation just builds all */
    public void buildStructure() {
        dobuild(true);
    }
    
    public void build() {
        dobuild(false);
    }
    
    protected void dobuild(boolean fresh) {
    	dobuild(Ajde.getDefault().getConfigurationManager().getActiveConfigFile(), fresh);
    }

    public void buildFresh(String configFile) {
        dobuild(configFile, true);
    }

    public void build(String configFile) {
    	dobuild(configFile, false);
    }
	
//	public void setReportInfoMessages(boolean b) {
//		if (compilerAdapter!=null) compilerAdapter.showInfoMessages(b);
//	}
	

    protected void dobuild(String configFile, boolean fresh) {
        if (configFile == null) {
            Ajde.getDefault().getErrorHandler().handleWarning("Please select a build configuration file.");
        } else {            
            // enforce batch builds after switching configurations
            // even in incremental mode
            lastConfigFile = this.configFile;
            this.configFile = configFile;
            if (!fresh) {
				// Check if we need to dig out an old incremental state
				if (!configFile.equals(lastConfigFile)) {
					AjState correctStateForConfig = IncrementalStateManager.retrieveStateFor(configFile);
					if (correctStateForConfig==null)  fresh = true; // have to full build
					else {
						compilerAdapter.setState(correctStateForConfig);
						AsmManager.getDefault().setRelationshipMap(correctStateForConfig.getRelationshipMap());
						AsmManager.getDefault().setHierarchy(correctStateForConfig.getStructureModel());
					}
				}
            }
            if (fresh) {
                this.compilerAdapter.nextBuildFresh();
            }
            CompilerThread compilerThread = new CompilerThread();
            compilerThread.start();
        }
    }

    public void abortBuild() {
        if (compilerAdapter != null) {
            compilerAdapter.requestCompileExit();
        }
    }

    public boolean isStructureDirty() {
        if (compilerAdapter != null) {
            return compilerAdapter.isStructureDirty();
        } else {
            return false;
        }
    }

    public void setStructureDirty(boolean structureDirty) {
        if (compilerAdapter != null) {
            compilerAdapter.setStructureDirty(structureDirty);
        }
    }

    public void addListener(BuildListener compilerListener) {
        compilerListeners.add(compilerListener);
    }

    public void removeListener(BuildListener compilerListener) {
        compilerListeners.remove(compilerListener);
    }

    private void notifyCompileFinished(String configFile, int buildTime, boolean succeeded, boolean warnings) {
        Ajde.getDefault().logEvent("build finished, succeeded: " + succeeded);
        for (Iterator it = compilerListeners.iterator(); it.hasNext(); ) {
            ((BuildListener)it.next()).compileFinished(configFile, buildTime, succeeded, warnings);
        }
    }

    private void notifyCompileStarted(String configFile) {
    	Ajde.getDefault().logEvent("build started: " + configFile);
        for (Iterator it = compilerListeners.iterator(); it.hasNext(); ) {
            ((BuildListener)it.next()).compileStarted(configFile);
        }
    }

//    private void notifyCompileAborted(String configFile, String message) {
//        for (Iterator it = compilerListeners.iterator(); it.hasNext(); ) {
//            ((BuildListener)it.next()).compileAborted(configFile, message);
//        }
//    }


    public BuildOptionsAdapter getBuildOptions() {
        return buildOptions;    
    }  

    /**
     * run compiler in a separate thread
     */
    public class CompilerThread extends Thread {

        public void run() {
        	boolean succeeded = true;
        	boolean warnings = false;
            try {
            	long timeStart = System.currentTimeMillis();
            	notifyCompileStarted(configFile);
                progressMonitor.start(configFile);
                compilerMessages.clearTasks();
  
				if (Ajde.getDefault().isLogging())
       			  Ajde.getDefault().logEvent("building with options: " 
       				+ getFormattedOptionsString(buildOptions, Ajde.getDefault().getProjectProperties()));
                
                succeeded = compilerAdapter.compile(configFile, progressMonitor, buildModelMode);
                
                long timeEnd = System.currentTimeMillis();
                lastCompileTime = (int)(timeEnd - timeStart);
            } catch (ConfigParser.ParseException pe) {
                    Ajde.getDefault().getErrorHandler().handleWarning(
                    	"Config file entry invalid, file: " 
                    	+ pe.getFile().getPath() 
                    	+ ", line number: " 
                    	+ pe.getLine());
            } catch (Throwable e) {
                Ajde.getDefault().getErrorHandler().handleError("Compile error, caught Throwable: " + e.toString(), e);
            } finally {
                warnings = compilerMessages.hasWarning();
				progressMonitor.finish(compilerAdapter.wasFullBuild());
            }
            notifyCompileFinished(configFile, lastCompileTime, succeeded, warnings);
        }
  
  		// AMC - updated for AspectJ 1.1 options
		private String getFormattedOptionsString(BuildOptionsAdapter buildOptions, ProjectPropertiesAdapter properties) {
			return "Building with settings: "
				+ "\n-> output path: " + properties.getOutputPath()
				+ "\n-> classpath: " + properties.getClasspath()
				+ "\n-> bootclasspath: " + properties.getBootClasspath()
				+ "\n-> -injars " + formatSet(properties.getInJars())
				+ "\n-> -inpath " + formatSet(properties.getInpath())
				+ "\n-> -outjar " + formatOptionalString(properties.getOutJar())
				+ "\n-> -sourceroots " + formatSet(properties.getSourceRoots())
				+ "\n-> -aspectpath " + formatSet(properties.getAspectPath())
				+ "\n-> -" + buildOptions.getComplianceLevel()
				+ "\n-> -source " + buildOptions.getSourceCompatibilityLevel()
				+ "\n-> -g:" + formatSet(buildOptions.getDebugLevel())
				+ "\n-> -warn:" + formatSet(buildOptions.getWarnings())
				+ "\n-> noImportError: " + buildOptions.getNoImportError()
				+ "\n-> preserveAllLocals:" + buildOptions.getPreserveAllLocals()
				+ "\n-> non-standard options: " + buildOptions.getNonStandardOptions();
//				+ "\n-> [ignored-deprecated in AspectJ1.1] porting mode: " + buildOptions.getPortingMode()
//				+ "\n-> [ignored-deprecated in AspectJ1.1] source 1.4 mode: " + buildOptions.getSourceOnePointFourMode()
//				+ "\n-> [ignored-deprecated in AspectJ1.1] strict spec mode: " + buildOptions.getStrictSpecMode()
//				+ "\n-> [ignored-deprecated in AspectJ1.1] lenient spec mode: " + buildOptions.getLenientSpecMode()
//				+ "\n-> [ignored-deprecated in AspectJ1.1] use javac mode: " + buildOptions.getUseJavacMode()
//				+ "\n-> [ignored-deprecated in AspectJ1.1] preprocess mode: " + buildOptions.getPreprocessMode()
//				+ "\n-> [ignored-deprecated in AspectJ1.1] working dir: " + buildOptions.getWorkingOutputPath();
		}
		
		private String formatSet( Set options ) {
			if ( options == null ) return "<default>";
			if ( options.isEmpty() ) return "none";
			
			StringBuffer formattedOptions = new StringBuffer();
			Iterator it = options.iterator();
			while (it.hasNext()) {
				String o = it.next().toString();
				if (formattedOptions.length() > 0) formattedOptions.append(", ");
				formattedOptions.append( o );
			}
			return formattedOptions.toString();
		}
		
		private String formatOptionalString( String s ) {
			if ( s == null ) { return ""	; }
			else { return s; }
		}
    }
    
	public void setBuildModelMode(boolean b) {
		buildModelMode = b;
	}
	
	public CompilerAdapter getCompilerAdapter() {
		return compilerAdapter;
	}

}

