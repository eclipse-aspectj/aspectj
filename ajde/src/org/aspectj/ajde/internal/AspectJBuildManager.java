/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde.internal;

import java.io.*;
import java.util.*;

import org.aspectj.ajde.*;
import org.aspectj.asm.StructureNode;
import org.aspectj.bridge.*;
import org.aspectj.util.ConfigParser;

/**
 * Responsible for the build process, including compiler invocation, threading, and error
 * reporting.
 *
 * @author Mik Kersten
 */
public class AspectJBuildManager implements BuildManager {
	
	private CompilerAdapter compiler = null;
    private TaskListManager compilerMessages = null;
    private BuildProgressMonitor progressMonitor = null;
    private BuildOptionsAdapter buildOptions = null;
    private ArrayList compilerListeners = new ArrayList();
    private String configFile = "";
    private int lastCompileTime = 50;
    private boolean buildStrucutreOnly = false;

    public AspectJBuildManager(
    	TaskListManager compilerMessages, 
    	BuildProgressMonitor progressMonitor,
    	BuildOptionsAdapter buildOptions) {
        this.compilerMessages = compilerMessages;
        this.progressMonitor = progressMonitor;
        this.buildOptions = buildOptions;
        this.compiler = new CompilerAdapter();
    }

    public void build() {
    	if (Ajde.getDefault().getConfigurationManager().getActiveConfigFile() == null) {
            Ajde.getDefault().getErrorHandler().handleWarning("Nothing to compile, please add a \".lst\" file.");
            return;
        } else {
            build(Ajde.getDefault().getConfigurationManager().getActiveConfigFile());
        }  
    }

	public void buildStructure() {
		buildStrucutreOnly = true;
		build();		
	}

    public void build(String configFile) {
    	buildStrucutreOnly = false;
        if (configFile == null) {
            Ajde.getDefault().getErrorHandler().handleWarning("Please add a configuration file to compile.");
        } else {
            this.configFile = configFile;
            CompilerThread compilerThread = new CompilerThread();
            compilerThread.start();
        }
    }

    public void abortBuild() {
        if (compiler != null) {
            compiler.requestCompileExit();
        }
    }

//    public CompilerAdapter getCurrCompiler() {
//        return currCompiler;
//    }

    public boolean isStructureDirty() {
        if (compiler != null) {
            return compiler.isStructureDirty();
        } else {
            return false;
        }
    }

    public void setStructureDirty(boolean structureDirty) {
        if (compiler != null) {
            compiler.setStructureDirty(structureDirty);
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

    private void notifyCompileAborted(String configFile, String message) {
        for (Iterator it = compilerListeners.iterator(); it.hasNext(); ) {
            ((BuildListener)it.next()).compileAborted(configFile, message);
        }
    }

	/**
	 * @todo	use structured error messages instead
	 */
    private void displayMessages(CompileResult compileResult) {
		String[]  descriptions = compileResult.getDescriptions();
        String[]  files        = compileResult.getfiles();
        Integer[] lineNumbers  = compileResult.getLineNumbers();
        if (descriptions.length == 0 && compileResult.getResult().trim() != "") {
            //compilerMessages.addSourcelineTask(compileResult.getResult(), "", 0, 0, TaskListManager.ERROR_MESSAGE);
            compilerMessages.addSourcelineTask(
            	compileResult.getResult(), 
            	new SourceLocation(null, 0, 0),
            	IMessage.ERROR);
			return;
        }
        
		for ( int i = 0; i < descriptions.length &&
						 i < files.length &&
						 i < lineNumbers.length; i++ ) {
            String message = "";
            if (files[i] != "") {
                message += "\"" + files[i] + "\": ";
            }
            if (lineNumbers[i].intValue() != -1 && lineNumbers[i].intValue() != 0) {
                message += descriptions[i] + ", at line: " + lineNumbers[i];
            } else {
                message += descriptions[i];
            }

            if (message.startsWith("Nothing to compile.")) {
                message = "Nothing to compile, please select the project, package(s), or class(es) to compile.";
            }


            IMessage.Kind kind = IMessage.ERROR;
            if (descriptions[i].endsWith("(warning)")) kind = IMessage.WARNING;
            	
            compilerMessages.addSourcelineTask(
            	message, 
            	new SourceLocation(new File(files[i]), lineNumbers[i].intValue(), 0),
            	kind);
            
     		StructureNode node = Ajde.getDefault().getStructureModelManager().getStructureModel().findNodeForSourceLine(
     			files[i],
     			lineNumbers[i].intValue()
     		);
     		
     		if (node != null) {
     			node.setMessage(new Message(message, kind, null, null));	
     		}
		}
    }

    /**
     * @todo    clean up this mess.
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
  
       			Ajde.getDefault().logEvent("building with options: " 
       				+ getFormattedOptionsString(buildOptions, Ajde.getDefault().getProjectProperties()));
                
                succeeded = compiler.compile(configFile, progressMonitor);
                
                long timeEnd = System.currentTimeMillis();
                lastCompileTime = (int)(timeEnd - timeStart);
            } catch (ConfigParser.ParseException pe) {
                    Ajde.getDefault().getErrorHandler().handleWarning(
                    	"Config file entry invalid, file: " 
                    	+ pe.getFile().getPath() 
                    	+ ", line number: " 
                    	+ pe.getLine());
//            } 
//            catch (ExitRequestException ere) {
//                if (ere.getValue() == 0) {
//                	notifyCompileAborted(configFile, "Build cancelled by user.");
//                } else {
//                    Ajde.getDefault().getErrorHandler().handleWarning("Compile could not complete. See the console for more details. "
//                        + "If no console is available re-launch the application from the command line.");
//                }  
//            } catch (InternalCompilerError compilerError) {
//                if (compilerError.uncaughtThrowable instanceof OutOfMemoryError) {
//                    Ajde.getDefault().getErrorHandler().handleError("Out of memory.  "
//                        + "Increase memory by setting the -Xmx parameter that this VM was launched with.\n"
//                        + "Note that some AJDE structure persists across compiles." ,
//                        compilerError.uncaughtThrowable);
//                } else  if (compilerError.uncaughtThrowable instanceof MissingRuntimeError) {
//                    Ajde.getDefault().getErrorHandler().handleWarning("Compilation aborted because the AspectJ runtime was not found. "
//                        + "Please place aspectjrt.jar in the lib/ext directory.");
//                } else  if (compilerError.uncaughtThrowable instanceof BadRuntimeError) {
//                    Ajde.getDefault().getErrorHandler().handleWarning("Compilation aborted because an out-of-date version of " +
//                        "the AspectJ runtime was found.  "
//                        + "Please place a current version of aspectjrt.jar in the lib/ext directory.");
//                } else {
//                    Ajde.getDefault().getErrorHandler().handleError("Compile error.", compilerError.uncaughtThrowable);
//                }  
            } catch (Throwable e) {
                Ajde.getDefault().getErrorHandler().handleError("Compile error, caught Throwable: " + e.toString(), e);
            } finally {
				progressMonitor.finish();
            }
            notifyCompileFinished(configFile, lastCompileTime, succeeded, warnings);
        }
  
		private String getFormattedOptionsString(BuildOptionsAdapter buildOptions, ProjectPropertiesAdapter properties) {
			return "Building with settings: "
				+ "\n-> output path: " + properties.getOutputPath()
				+ "\n-> classpath: " + properties.getClasspath()
				+ "\n-> bootclasspath: " + properties.getBootClasspath()
				+ "\n-> non-standard options: " + buildOptions.getNonStandardOptions()
				+ "\n-> porting mode: " + buildOptions.getPortingMode()
				+ "\n-> source 1.4 mode: " + buildOptions.getSourceOnePointFourMode()
				+ "\n-> strict spec mode: " + buildOptions.getStrictSpecMode()
				+ "\n-> lenient spec mode: " + buildOptions.getLenientSpecMode()
				+ "\n-> use javac mode: " + buildOptions.getUseJavacMode()
				+ "\n-> preprocess mode: " + buildOptions.getPreprocessMode()
				+ "\n-> working dir: " + buildOptions.getWorkingOutputPath();
		}
    }

	public BuildOptionsAdapter getBuildOptions() {
		return buildOptions;	
	}  

//    private void setCompilerOptions(AjdeCompiler compiler) {
//        String nonstandardOptions = buildOptions.getNonStandardOptions();
//        if (nonstandardOptions != null && !nonstandardOptions.trim().equals("")) {
//            StringTokenizer st = new StringTokenizer(nonstandardOptions, " ");
//            while (st.hasMoreTokens()) {
//                String flag = (String)st.nextToken();
//                compiler.getOptions().set(flag.substring(1, flag.length()), Boolean.TRUE);
//            }
//        }  
//  
//        if (Ajde.getDefault().getProjectProperties().getOutputPath() != null
//        	&& !compiler.getOptions().XtargetNearSource) {
//        	compiler.getOptions().outputDir = new File(Ajde.getDefault().getProjectProperties().getOutputPath());
//        }
//        if (Ajde.getDefault().getProjectProperties().getBootClasspath() != null) {
//        	compiler.getOptions().bootclasspath = Ajde.getDefault().getProjectProperties().getBootClasspath();
//        }
//        if (Ajde.getDefault().getProjectProperties().getClasspath() != null) {
//        	compiler.getOptions().classpath = Ajde.getDefault().getProjectProperties().getClasspath();
//        }
//        if (buildOptions.getWorkingOutputPath() != null) {
//        	compiler.getOptions().workingDir = new File(buildOptions.getWorkingOutputPath());
//        }
////        if (buildOptions.getCharacterEncoding() != null) {
////        	compiler.getOptions().encoding = buildOptions.getCharacterEncoding();
////        }
//        
//        compiler.getOptions().lenient = buildOptions.getLenientSpecMode();
//        compiler.getOptions().strict = buildOptions.getStrictSpecMode();
//        compiler.getOptions().usejavac = buildOptions.getUseJavacMode();
//        compiler.getOptions().porting = buildOptions.getPortingMode();
//        compiler.getOptions().preprocess = buildOptions.getPreprocessMode();
//        
//        if (buildOptions.getSourceOnePointFourMode()) {
//	        compiler.getOptions().source = "1.4";
//        }
//    }

    static class CompileResult {
        private String[]  files        = null;
        private Integer[] lineNumbers  = null;
        private String[]  descriptions = null;
        private String resultString = "";
        private boolean resultContainsErrors = false;

        /**
         * Parses out warning messages, error messages, "file not found" messages, javac "Note:" messages.
         *
         * @todo    get error message structure directly from compiler
         */
        public CompileResult( String result )
        {
            resultString = result;

            BufferedReader reader = new BufferedReader( new StringReader( result ) );
            Vector fileV = new Vector();
            Vector lineV = new Vector();
            Vector descV = new Vector();
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    String originalLine = line;
                    String  description = "";
                    String  file = "";
                    Integer lineNo = new Integer(0);
                    int index = line.indexOf( ":", 2 ); // @todo    skip the initial drive ":" (fix, Windows only)
                    try {
                        if (line.indexOf("Note: ") != -1) {
                            int index1 = line.indexOf(".java");
                            if (index1 != -1) {
                                description = line.substring(index1+5) + " (warning)";
                                file = line.substring("Note: ".length(), index1+5);
                                lineNo = new Integer(0);
                            } else {
                                description = line + " (warning)";
                                file = "";
                                lineNo = new Integer(-1);
                            }
                        }
                        else if (line.indexOf("file not found: ") != -1) {
                            description = "file not found: ";
                            file = line.substring("file not found: ".length());
                            lineNo = new Integer(0);
                        }
                        else if (index != -1 && line.indexOf( "java" ) != -1) {
                            file =  line.substring( 0, index );
                            line = line.substring( index+1 );

                            index = line.indexOf( ":" );
                            lineNo = new Integer( Integer.parseInt( line.substring( 0, index ) ) ) ;
                            line = line.substring( index+1 );

                            if (!resultContainsErrors) {
                                if (!line.endsWith("(warning)")) {
                                    resultContainsErrors = true;
                                }
                            }
                            description = line.substring( line.indexOf( ":" ) + 2 );
                        }
                    } catch (Exception e) {
                        description = "Internal ajc message: " + originalLine;
                        file = "";
                        lineNo = new Integer(-1);
                    }
                    if (description.trim() != "") {
                        descV.addElement(description);
                        fileV.addElement(file);
                        lineV.addElement(lineNo);
                    }
                }
            }
            catch ( IOException ioe ) {
                resultString = "ERROR: could not parse result at line for string: " + result;
            }
            files        = new String[fileV.size()];
            lineNumbers  = new Integer[lineV.size()];
            descriptions = new String[descV.size()];
            fileV.copyInto(files);
            lineV.copyInto(lineNumbers);
            descV.copyInto(descriptions);
        }

        public String toString()
        {
            return resultString;
        }

        public String[] getfiles()
        {
            return files;
        }

        public Integer[] getLineNumbers()
        {
            return lineNumbers;
        }

        public String[] getDescriptions()
        {
            return descriptions;
        }

        public String getResult()
        {
            return resultString;
        }

        public boolean containsErrors() {
            return resultContainsErrors;
        }
    }
}

class ConfigFileDoesNotExistException extends Exception { 
	public ConfigFileDoesNotExistException(String filePath) {
		super(filePath);	
	}
}
