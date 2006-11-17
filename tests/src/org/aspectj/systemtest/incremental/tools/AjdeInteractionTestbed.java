/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 * Andy Clement          initial implementation
 * ******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.BuildOptionsAdapter;
import org.aspectj.ajde.BuildProgressMonitor;
import org.aspectj.ajde.ErrorHandler;
import org.aspectj.ajde.OutputLocationManager;
import org.aspectj.ajde.ProjectPropertiesAdapter;
import org.aspectj.ajde.TaskListManager;
import org.aspectj.ajde.internal.AspectJBuildManager;
import org.aspectj.ajdt.internal.core.builder.AbstractStateListener;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.tools.ajc.Ajc;

/**
 * This class uses Ajde in the same way that an IDE (e.g. AJDT) does.
 * 
 * The build is driven through 'build(projectName,configFile)' but the
 * build can be configured by the methods beginning 'configure***'.
 * Information about what happened during a build is accessible
 * through the get*, was*, print* public methods...
 * 
 * There are many methods across the multiple listeners that communicate
 * info with Ajde - not all are implemented.  Those that are are
 * task tagged DOESSOMETHING :)
 */
public class AjdeInteractionTestbed extends TestCase {

	public  static boolean VERBOSE         = false; // do you want the gory details?
	
	public    static String   testdataSrcDir = "../tests/multiIncremental";
	protected static    File       sandboxDir;
	
	private static boolean buildModel;
	
	// Methods for configuring the build
	public static void configureBuildStructureModel(boolean b) { buildModel = b;}
	
	public static void configureNewProjectDependency(String fromProject, String projectItDependsOn) {
		MyProjectPropertiesAdapter.addDependancy(fromProject,projectItDependsOn);
	}
	
	public static void configureNonStandardCompileOptions(String options) {
		MyBuildOptionsAdapter.setNonStandardOptions(options);
	}
	
	public static void configureAspectPath(Set aspectpath) {
		MyProjectPropertiesAdapter.setAspectpath(aspectpath);
	} 
	
	public static void configureOutputLocationManager(OutputLocationManager mgr) {
		MyProjectPropertiesAdapter.setOutputLocationManager(mgr);
	}
	
	public static void configureResourceMap(Map resourcesMap) {
		MyProjectPropertiesAdapter.setSourcePathResources(resourcesMap);
	}
	// End of methods for configuring the build
	
	
	protected File getWorkingDir() { return sandboxDir; }
	
	protected void setUp() throws Exception {
		super.setUp();
		if (AjState.stateListener==null) AjState.stateListener=MyStateListener.getInstance();
		MyStateListener.reset();
		MyBuildProgressMonitor.reset();
		MyTaskListManager.reset();
		MyProjectPropertiesAdapter.reset();
		
		// Create a sandbox in which to work
		sandboxDir = Ajc.createEmptySandbox();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		AjState.stateListener=null;
	}
	
	/** Drives a build */
	public boolean build(String projectName,String configFile) {
		return AjdeManager.build(projectName,configFile);
	}

	public boolean fullBuild(String projectName,String configFile) {
		return AjdeManager.fullBuild(projectName,configFile);
	}
	
	/** Looks after communicating with the singleton Ajde instance */
	public static class AjdeManager {
		
		static {
			Ajde.init(null,
					  MyTaskListManager.getInstance(),
					  MyBuildProgressMonitor.getInstance(),
					  MyProjectPropertiesAdapter.getInstance(),
					  MyBuildOptionsAdapter.getInstance(),
					  null,null,
					  MyErrorHandler.getInstance());

			  MyStateListener sl = MyStateListener.getInstance();
			  AjState.stateListener = sl;
		}

		/**
		 * Builds a specified project using a specified config file.  Subsequent 
		 * calls to build the same project should result in incremental builds.
		 */
		private static boolean build(String projectName,String configFile) {
			pause(1000); // delay to allow previous runs build stamps to be OK
			lognoln("Building project '"+projectName+"'");
			
			// Ajde.getDefault().enableLogging(System.out);
			
			//Ajde.getDefault().getBuildManager().setReportInfoMessages(true); 
			
			// Configure the necessary providers and listeners for this compile
			MyBuildProgressMonitor.reset();
			MyTaskListManager.reset();
			MyStateListener.reset();
			// MyBuildOptionsAdapter.reset(); needs manually resetting in a test
			
			MyProjectPropertiesAdapter.setActiveProject(projectName);
			AsmManager.attemptIncrementalModelRepairs=true;
			IncrementalStateManager.recordIncrementalStates=true;
			
			Ajde.getDefault().getBuildManager().setBuildModelMode(buildModel);
			
			// Do the compile
			Ajde.getDefault().getBuildManager().build(getFile(projectName,configFile));
			
			// Wait for it to complete
			while (!MyBuildProgressMonitor.hasFinished()) {
				lognoln(".");
				pause(100);
			} 
			log("");
		    
			// What happened?
			if (MyTaskListManager.hasErrorMessages()) {
				System.err.println("Build errors:");
				for (Iterator iter = MyTaskListManager.getErrorMessages().iterator(); iter.hasNext();) {
					IMessage element = (IMessage) iter.next();
					System.err.println(element);
				}
				System.err.println("---------");
			}
			log("Build finished, time taken = "+MyBuildProgressMonitor.getTimeTaken()+"ms");
			return true;
		}
		
		private static boolean fullBuild(String projectName,String configFile) {
			pause(1000); // delay to allow previous runs build stamps to be OK
			lognoln("Building project '"+projectName+"'");
			
			// Ajde.getDefault().enableLogging(System.out);
			
			//Ajde.getDefault().getBuildManager().setReportInfoMessages(true); 
			
			// Configure the necessary providers and listeners for this compile
			MyBuildProgressMonitor.reset();
			MyTaskListManager.reset();
			MyStateListener.reset();
			
			MyProjectPropertiesAdapter.setActiveProject(projectName);
			//AsmManager.attemptIncrementalModelRepairs=true;
			//IncrementalStateManager.recordIncrementalStates=true;
			
			Ajde.getDefault().getBuildManager().setBuildModelMode(buildModel);
			
			// Do the compile
			Ajde.getDefault().getBuildManager().buildFresh(getFile(projectName,configFile));
			
			// Wait for it to complete
			while (!MyBuildProgressMonitor.hasFinished()) {
				lognoln(".");
				pause(100);
			} 
			log("");
		    
			// What happened?
			if (MyTaskListManager.hasErrorMessages()) {
				System.err.println("Build errors:");
				for (Iterator iter = MyTaskListManager.getErrorMessages().iterator(); iter.hasNext();) {
					IMessage element = (IMessage) iter.next();
					System.err.println(element);
				}
				System.err.println("---------");
			}
			log("Build finished, time taken = "+MyBuildProgressMonitor.getTimeTaken()+"ms");
			return true;
		}
		
		private static void pause(int millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException ie) {}
		}
		
		public static void setMessageHandler(IMessageHandler handler) {
			Ajde.getDefault().setMessageHandler(handler);
		}
		
		public static IMessageHandler getMessageHandler() {
			AspectJBuildManager buildManager = (AspectJBuildManager) Ajde.getDefault().getBuildManager();
			return buildManager.getCompilerAdapter().getMessageHandler();
		}
//		public static boolean lastCompileDefaultedToBatch() {
//			return MyTaskListManager.defaultedToBatch();
//		}
	}
	

	// Methods for querying what happened during a build and accessing information
	// about the build:
	
	/**
	 * Helper method for dumping info about which files were compiled and
	 * woven during the last build.
	 */
	public String printCompiledAndWovenFiles() {
		StringBuffer sb = new StringBuffer();
		if (getCompiledFiles().size()==0 && getWovenClasses().size()==0)
			sb.append("No files were compiled or woven\n");
		for (Iterator iter = getCompiledFiles().iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			sb.append("compiled: "+element+"\n");
		}
		for (Iterator iter = getWovenClasses().iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			sb.append("woven: "+element+"\n");
		}
		return sb.toString();
	}
	
	/**
	 * Summary report on what happened in the most recent build
	 */
	public void printBuildReport() {
		System.out.println("\n============== BUILD REPORT =================");
		System.out.println("Build took: "+getTimeTakenForBuild()+"ms");
		List compiled=getCompiledFiles();
		System.out.println("Compiled: "+compiled.size()+" files");
		for (Iterator iter = compiled.iterator(); iter.hasNext();) {
			System.out.println("        :"+iter.next());			
		}
		List woven=getWovenClasses();
		System.out.println("Wove: "+woven.size()+" files");
		for (Iterator iter = woven.iterator(); iter.hasNext();) {
			System.out.println("    :"+iter.next());			
		}
		if (wasFullBuild()) System.out.println("It was a batch (full) build");
		System.out.println("=============================================");
	}
	
	
	public boolean wasFullBuild() {
	// alternatives: statelistener is debug interface, progressmonitor is new proper interface (see pr145689)
//		return MyBuildProgressMonitor.wasFullBuild();
		return MyStateListener.wasFullBuild();
	}
	

	public long getTimeTakenForBuild() {
		return MyBuildProgressMonitor.getTimeTaken();
	}
	
	public List getCompiledFiles() {
		return MyBuildProgressMonitor.getCompiledFiles();
	}

	public List getWovenClasses() {
		return MyBuildProgressMonitor.getWovenClasses();
	}
	
	// Infrastructure below here
	
	private static void log(String msg) {
		if (VERBOSE) System.out.println(msg);
	}
	
	private static void lognoln(String msg) {
		if (VERBOSE) System.out.print(msg);
	}
	
	/** Return the *full* path to this file which is taken relative to the project dir*/
	protected static String getFile(String projectName, String path) {
		return new File(sandboxDir,projectName+File.separatorChar + path).getAbsolutePath();
	}

    // Helper classes that communicate with Ajde
	
	static class MyErrorHandler implements ErrorHandler {
		static MyErrorHandler _instance = new MyErrorHandler();
		private List errorMessages = new ArrayList();
		
		private MyErrorHandler() {}
		
		public static ErrorHandler getInstance() { 
			return _instance;
		}

		public void handleWarning(String message) {
			log("ErrorHandler.handleWarning("+message+")");
		}

		public void handleError(String message) {
			log("ErrorHandler.handleWarning("+message+")");
			errorMessages.add(message);
		}

		public void handleError(String message, Throwable t) {
			log("ErrorHandler.handleError("+message+","+t+")");
			if (VERBOSE) t.printStackTrace();
			errorMessages.add(message+","+t+")");
		}
		
		public static List/*String*/ getErrorMessages() {
			return _instance.errorMessages;
		}
		
	}
	
	// -----------------
	
	static class MyProjectPropertiesAdapter implements ProjectPropertiesAdapter {
		
		private final static boolean VERBOSE = false;

		static MyProjectPropertiesAdapter _instance = new MyProjectPropertiesAdapter();
		private MyProjectPropertiesAdapter() {}
		
		public static MyProjectPropertiesAdapter getInstance() { 
			return _instance;
		}
		
		public static void reset() {
			_instance.aspectPath=null;
			_instance.sourcePathResources=null;
			_instance.outputLocationManager=null;
		}
		
		private String projectName = null;
		private String classPath = "";
		private Set aspectPath = null;
		private Map sourcePathResources = null;
		private OutputLocationManager outputLocationManager = null;
		
		public static void setActiveProject(String n) {
			_instance.projectName = n;
		}

		private static Hashtable dependants = new Hashtable();
		
		public static void addDependancy(String project, String projectItDependsOn) {
			List l = (List)dependants.get(project);
			if (l == null) {
				List ps = new ArrayList();
				ps.add(projectItDependsOn);
				dependants.put(project,ps);
			} else {
				l.add(projectItDependsOn);
			}
		}
		
		public static void setSourcePathResources(Map m) {
			_instance.sourcePathResources = m;
		}

		public void setClasspath(String path) {
			this.classPath = path;
		}
		
		public static void setAspectpath(Set path) {
			_instance.aspectPath = path;
		}
		
		// interface impl below
		
		// DOESSOMETHING
		public String getProjectName() {
			log("MyProjectProperties.getProjectName() [returning "+projectName+"]");
			return projectName;
		}

		// DOESSOMETHING
		public String getRootProjectDir() {
			String dir = testdataSrcDir+File.separatorChar+projectName;
			log("MyProjectProperties.getRootProjectDir() [returning "+dir+"]");
			return dir;
		}

		public List getBuildConfigFiles() {
			log("MyProjectProperties.getBuildConfigFiles()");
			return null;
		}

		public String getDefaultBuildConfigFile() {
			log("MyProjectProperties.getDefaultBuildConfigFile()");
			return null;
		}

		public String getLastActiveBuildConfigFile() {
			log("MyProjectProperties.getLastActiveBuildConfigFile()");
			return null;
		}

		public List getProjectSourceFiles() {
			log("MyProjectProperties.getProjectSourceFiles()");
			return null;
		}

		public String getProjectSourcePath() {
			log("MyProjectProperties.getProjectSourcePath()");
			return null;
		}

		// DOESSOMETHING
		public String getClasspath() {
			log("MyProjectProperties.getClasspath()");
			String cp =  
			  new File(testdataSrcDir) + File.pathSeparator +
    		  System.getProperty("sun.boot.class.path") + 
    		  File.pathSeparator + "../runtime/bin" +
    		  File.pathSeparator + this.classPath + 
    		  File.pathSeparator +  System.getProperty("aspectjrt.path") +
    		  File.pathSeparator +  "../lib/junit/junit.jar" +
    		  "c:/batik/batik-1.6/lib/batik-util.jar;"+
    		  "c:/batik/batik-1.6/lib/batik-awt-util.jar;"+
    		  "c:/batik/batik-1.6/lib/batik-dom.jar;"+
    		  "c:/batik/batik-1.6/lib/batik-svggen.jar;"+
    		  File.pathSeparator+".."+File.separator+"lib" + File.separator+"test"+File.separator+"aspectjrt.jar";
			
			// look at dependant projects
			List projects = (List)dependants.get(projectName);
			if (projects!=null) {
				for (Iterator iter = projects.iterator(); iter.hasNext();) {
					cp = getFile((String)iter.next(),"bin")+File.pathSeparator+cp;
				}
			}
			//System.err.println("For project "+projectName+" getClasspath() returning "+cp);
			return cp;
		}
		
		public String getOutputPath() {
			String dir = getFile(projectName,"bin");
			log("MyProjectProperties.getOutputPath() [returning "+dir+"]");
			return dir;
		}
		
		public static void setOutputLocationManager(OutputLocationManager mgr) {
			_instance.outputLocationManager = mgr;
		}
		
	    public OutputLocationManager getOutputLocationManager() {
	    	return this.outputLocationManager;
	    }

		public String getBootClasspath() {
			log("MyProjectProperties.getBootClasspath()");
			return null;
		}

		public String getClassToExecute() {
			log("MyProjectProperties.getClassToExecute()");
			return null;
		}

		public String getExecutionArgs() {
			log("MyProjectProperties.getExecutionArgs()");
			return null;
		}

		public String getVmArgs() {
			log("MyProjectProperties.getVmArgs()");
			return null;
		}

		public Set getInJars() {
			log("MyProjectProperties.getInJars()");
			return null;
		}

		public Set getInpath() {
			log("MyProjectProperties.getInPath()");
			return null;
		}

		public Map getSourcePathResources() {
			log("MyProjectProperties.getSourcePathResources()");
			return sourcePathResources;
		}

		public String getOutJar() {
			log("MyProjectProperties.getOutJar()");
			return null;
		}

		public Set getSourceRoots() {
			log("MyProjectProperties.getSourceRoots()");
			return null;
		}

		public Set getAspectPath() {
			log("MyProjectProperties.getAspectPath("+aspectPath+")");
			return aspectPath;
		}
		
		public static void log(String s) {
			if (VERBOSE) System.out.println(s);
		}
		
	}
	
	// -----------------------
	static class MyBuildProgressMonitor implements BuildProgressMonitor {

		public static boolean VERBOSE = false;
		private static MyBuildProgressMonitor _instance = new MyBuildProgressMonitor();
		private MyBuildProgressMonitor() {}
		
		private List compiledFiles=new ArrayList();
		private List wovenClasses=new ArrayList();
		

		public static BuildProgressMonitor getInstance() { 
			return _instance;
		}
		
		public static void reset() {
			_instance.finished = false;
			_instance.wasFullBuild=true;
			_instance.compiledFiles.clear();
			_instance.wovenClasses.clear();
		}
		
		public static boolean hasFinished() {
			return _instance.finished;
		}
		
		public static List getCompiledFiles() { return _instance.compiledFiles;}
		public static List getWovenClasses()  { return _instance.wovenClasses; }
		
		//---
		
		private long starttime = 0;
		private long totaltimetaken = 0;
		private boolean finished = false;
		private boolean wasFullBuild = true;

		public void start(String configFile) {
			starttime = System.currentTimeMillis();
			log("BuildProgressMonitor.start("+configFile+")");
		}

		public void setProgressText(String text) {
			log("BuildProgressMonitor.setProgressText("+text+")");
			if (text.startsWith("compiled: ")) {
				compiledFiles.add(text.substring(10));
			} else if (text.startsWith("woven class ")) {
				wovenClasses.add(text.substring(12));	
			} else if (text.startsWith("woven aspect ")) {
				wovenClasses.add(text.substring(13));
			}
		}

		public void setProgressBarVal(int newVal) {
			log("BuildProgressMonitor.setProgressBarVal("+newVal+")");
		}

		public void incrementProgressBarVal() {
			log("BuildProgressMonitor.incrementProgressBarVal()");
		}

		public void setProgressBarMax(int maxVal) {
			log("BuildProgressMonitor.setProgressBarMax("+maxVal+")");
		}

		public int getProgressBarMax() {
			log("BuildProgressMonitor.getProgressBarMax() [returns 100]");
			return 100;
		}

		public void finish(boolean b) {
			log("BuildProgressMonitor.finish()");
			_instance.finished=true;
			_instance.wasFullBuild = b;
			_instance.totaltimetaken=(System.currentTimeMillis()-starttime);
		}
		
		public static long getTimeTaken() {
			return _instance.totaltimetaken;
		}
		
		public static void log(String s) {
			if (VERBOSE) System.out.println(s);
		}
		
		public static boolean wasFullBuild() {
			return _instance.wasFullBuild;
		}
		

	}
	
	// ----
	
	static class MyTaskListManager implements TaskListManager {

		private static final String CANT_BUILD_INCREMENTAL_INDICATION = "Unable to perform incremental build";
		private static final String DOING_BATCH_BUILD_INDICATION = "Performing batch build for config";
		
		private final static boolean VERBOSE = false;
		static MyTaskListManager _instance = new MyTaskListManager();
		private MyTaskListManager() {}
		
		private boolean receivedNonIncrementalBuildMessage = false;
		private boolean receivedBatchBuildMessage = false;
		private List errorMessages = new ArrayList();
		private List warningMessages = new ArrayList();
		private List weavingMessages = new ArrayList();
		
		public static void reset() {
			_instance.receivedNonIncrementalBuildMessage=false;
			_instance.receivedBatchBuildMessage=false;
			_instance.errorMessages.clear();
			_instance.warningMessages.clear();
			_instance.weavingMessages.clear();
		}
		
//		public static boolean defaultedToBatch() {
//			return _instance.receivedNonIncrementalBuildMessage;
//		}
//		
//		public static boolean didBatchBuild() {
//			return _instance.receivedBatchBuildMessage;
//		}
		
		public static boolean hasErrorMessages() {
			return !_instance.errorMessages.isEmpty();
		}
		
		public static List/*IMessage*/ getErrorMessages() {
			return _instance.errorMessages;
		}
		
		public static List/*IMessage*/ getWarningMessages() {
			return _instance.warningMessages;
		}
		
		public static List/*IMessage*/ getWeavingMessages() {
			return _instance.weavingMessages;
		}
		
		public static TaskListManager getInstance() { 
			return _instance;
		}

		public void addSourcelineTask(String message, ISourceLocation sourceLocation, Kind kind) {
			log("TaskListManager.addSourcelineTask("+message+","+sourceLocation+","+kind+")");
		}

		// DOESSOMETHING
		public void addSourcelineTask(IMessage message) {
//			if (message.getKind()==IMessage.INFO) {
//				if (message.getMessage().startsWith(CANT_BUILD_INCREMENTAL_INDICATION)) _instance.receivedNonIncrementalBuildMessage=true;
//				if (message.getMessage().startsWith(DOING_BATCH_BUILD_INDICATION)) _instance.receivedBatchBuildMessage=true;
//			}
			if (message.getKind()==IMessage.ERROR) errorMessages.add(message);
			if (message.getKind()==IMessage.WARNING) warningMessages.add(message);
			if (message.getKind()==IMessage.WEAVEINFO) weavingMessages.add(message);
			log("TaskListManager.addSourcelineTask("+message+")");
		}

		public boolean hasWarning() {
			log("TaskListManager.hasWarning() [returning "+(!warningMessages.isEmpty())+"]");
			return !warningMessages.isEmpty();
		}

		public void addProjectTask(String message, Kind kind) {
			log("TaskListManager.addProjectTask("+message+","+kind+")");
		}

		public void clearTasks() {
			log("TaskListManager.clearTasks()");
		}
		
		public static void log(String s) {
			if (VERBOSE) System.out.println(s);
		}
	}

	// ----
	
	static class MyBuildOptionsAdapter implements BuildOptionsAdapter {
		static MyBuildOptionsAdapter _instance = new MyBuildOptionsAdapter();
		private MyBuildOptionsAdapter() {}
		
		private Map javaOptionsMap;
		
		public static void setJavaOptionsMap(Map options) {
			_instance.javaOptionsMap = options;
		}
		
		public static void setNonStandardOptions(String options) {
			_instance.nonstandardoptions = options;
		}

		private String nonstandardoptions=null;
		
		public static void reset() {
			_instance.nonstandardoptions=null;
			_instance.javaOptionsMap = null;
		}
		
		public static BuildOptionsAdapter getInstance() { 
			return _instance;
		}

		public Map getJavaOptionsMap() {
			if (javaOptionsMap != null && !javaOptionsMap.isEmpty() ) return javaOptionsMap;
			
			Hashtable ht = new Hashtable();
			ht.put("org.eclipse.jdt.core.compiler.compliance","1.5");
			ht.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform","1.5");
			ht.put("org.eclipse.jdt.core.compiler.source","1.5");
			return ht;				
		}

		public boolean getUseJavacMode() {
			return false;
		}

		public String getWorkingOutputPath() {
			return null;
		}

		public boolean getPreprocessMode() {
			return false;
		}

		public String getCharacterEncoding() {
			return null;
		}

		public boolean getSourceOnePointFourMode() {
			return false;
		}

		// DOESSOMETHING
		public boolean getIncrementalMode() {
			return true;
		}

		public boolean getLenientSpecMode() {
			return false;
		}

		public boolean getStrictSpecMode() {
			return false;
		}

		public boolean getPortingMode() {
			return false;
		}

		public String getNonStandardOptions() {
			return nonstandardoptions;
		}

		public String getComplianceLevel() {
			// AJDT doesn't set the compliance level directly
			// instead it relies on the javaOptionsMap
			return null;
		}

		public String getSourceCompatibilityLevel() {
			// AJDT doesn't set the source compatibility level
			// instead it relies on the javaOptionsMap
			return null;
		}

		public Set getWarnings() {
			return null;
		}

		public Set getDebugLevel() {
			return null;
		}

		public boolean getNoImportError() {
			return false;
		}

		public boolean getPreserveAllLocals() {
			return false;
		}
	}
	
	static class MyStateListener extends AbstractStateListener {
		
		private static MyStateListener _instance = new MyStateListener();
		private MyStateListener() {reset();}
		
		public static MyStateListener getInstance() { return _instance;}
		
		public static boolean informedAboutKindOfBuild;
		public static boolean fullBuildOccurred;
		public static List detectedDeletions = new ArrayList();
		public static StringBuffer decisions = new StringBuffer();
		
		public static void reset() {
			informedAboutKindOfBuild=false;
			decisions = new StringBuffer();
			fullBuildOccurred=false;
			if (detectedDeletions!=null) detectedDeletions.clear();
		}
		
  	    public boolean pathChange = false;
		public void pathChangeDetected() {pathChange = true;}
		public void aboutToCompareClasspaths(List oldClasspath, List newClasspath) {}
		public void detectedClassChangeInThisDir(File f) {}
		
		public void detectedAspectDeleted(File f) {
			detectedDeletions.add(f.toString());
		}

		public void buildSuccessful(boolean wasFullBuild) {
			informedAboutKindOfBuild= true;
			fullBuildOccurred=wasFullBuild;
		}
		
		public static String getDecisions() {
			return decisions.toString();
		}
		
		public static boolean wasFullBuild() {
			if (!informedAboutKindOfBuild) throw new RuntimeException("I never heard about what kind of build it was!!");
			return fullBuildOccurred;
		}

		// not needed just yet...
//		public void recordInformation(String s) { decisions.append(s).append("\n");}
		public void recordDecision(String s) {
			decisions.append(s).append("\n");
		}
	};
}