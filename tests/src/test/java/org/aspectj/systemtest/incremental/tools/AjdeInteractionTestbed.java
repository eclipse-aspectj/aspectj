/********************************************************************
 * Copyright (c) 2005 Contributors.All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement      initial implementation
 *     Helen Hawkins     Converted to new interface (bug 148190)
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajde.core.IBuildMessageHandler;
import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajdt.internal.core.builder.AbstractStateListener;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.AsmManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.testing.util.TestUtil;

import junit.framework.TestCase;

/**
 * This class uses Ajde in the same way that an IDE (e.g. AJDT) does.
 *
 * The build is driven through 'doBuild(projectName)' but the build can be configured by the methods beginning 'configure***'.
 * Information about what happened during a build is accessible through the get*, was*, print* public methods...
 *
 */
public class AjdeInteractionTestbed extends TestCase {

	public final static boolean VERBOSE = System.getProperty("aspectj.tests.verbose", "true").equalsIgnoreCase("true");

	public static String testdataSrcDir = "../tests/multiIncremental";
	protected static File sandboxDir;

	private static boolean buildModel;

	// Methods for configuring the build
	public void configureNewProjectDependency(String fromProjectName, String projectItDependsOn) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + fromProjectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).addDependancy(projectItDependsOn);
	}

	public void addSourceFolderForSourceFile(String projectName, File f, String sourceFolder) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestOutputLocationManager) ((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration())
				.getOutputLocationManager()).setSourceFolderFor(f, sourceFolder);
	}

	public void setNextChangeResponse(String projName, int flags) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).changed = flags;
	}

	public void setProjectEncoding(String projName, String encoding) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setProjectEncoding(encoding);
	}

	public void addProjectSourceFileChanged(String projectName, File changedFile) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).addProjectSourceFileChanged(changedFile);
	}

	public void addXmlConfigFile(String projectName, String xmlfile) {
		List<String> l = new ArrayList<>();
		l.add(xmlfile);
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setProjectXmlConfigFiles(l);
	}

	public void addClasspathEntry(String projectName, File classpathEntry) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		MultiProjTestCompilerConfiguration config = ((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration());
		config.setClasspath(config.getClasspath() + File.pathSeparator + classpathEntry.toString());
	}

	public void addClasspathEntryChanged(String projectName, String changedDir) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).addClasspathEntryChanged(changedDir);
	}

	public void configureNonStandardCompileOptions(String projectName, String options) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setNonStandardOptions(options);
	}

	public void configureAspectPath(String projectName, Set<File> aspectpath) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setAspectPath(aspectpath);
	}

	public void configureProcessor(String projectName, String processor) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setProcessor(processor);
	}

	public void configureProcessorPath(String projectName, String processorPath) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setProcessorPath(processorPath);
	}

	public void configureAspectPath(String projectName, File aspectpath) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		Set<File> s = new HashSet<>();
		s.add(aspectpath);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setAspectPath(s);
	}

	public void configureResourceMap(String projectName, Map<String,File> resourcesMap) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setSourcePathResources(resourcesMap);
	}

	public void configureJavaOptionsMap(String projectName, Map<String,String> options) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setJavaOptions(options);
	}

	public static void configureInPath(String projectName, Set<File> inpath) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setInpath(inpath);
	}

	public static void configureInPath(String projectName, File inpath) {
		Set<File> s = new HashSet<>();
		s.add(inpath);
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setInpath(s);
	}

	public static void configureOutputLocationManager(String projectName, IOutputLocationManager mgr) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		((MultiProjTestCompilerConfiguration) compiler.getCompilerConfiguration()).setOutputLocationManager(mgr);
	}

	public void configureShowWeaveInfoMessages(String projectName, boolean showWeaveInfo) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		IBuildMessageHandler handler = compiler.getMessageHandler();
		if (showWeaveInfo) {
			handler.dontIgnore(IMessage.WEAVEINFO);
		} else {
			handler.ignore(IMessage.WEAVEINFO);
		}
	}

	// End of methods for configuring the build

	public AjCompiler getCompilerForProjectWithName(String projectName) {
		return CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
	}

	protected File getWorkingDir() {
		return sandboxDir;
	}

	protected void setUp() throws Exception {
		super.setUp();
		// need this line because otherwise reset in previous tests
		AsmManager.attemptIncrementalModelRepairs = true;
		if (AjState.stateListener == null) {
			AjState.stateListener = MyStateListener.getInstance();

		}
		MyStateListener.reset();
		// Create a sandbox in which to work
		sandboxDir = TestUtil.createEmptySandbox();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		AjState.stateListener = null;
		CompilerFactory.clearCompilerMap();
		IncrementalStateManager.clearIncrementalStates();
	}

	/** Drives a build */
	public boolean doBuild(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		resetCompilerRecords(compiler);
		addSourceFilesToBuild(projectName, compiler);
		// addXmlConfigFilesToBuild(projectName, compiler);
		pause(1000); // delay to allow previous runs build stamps to be OK
		lognoln("Building project '" + projectName + "'");
		compiler.build();
		log("");
		checkForErrors(compiler);
		log("Build finished, time taken = "
				+ ((MultiProjTestBuildProgressMonitor) compiler.getBuildProgressMonitor()).getTimeTaken() + "ms");
		return true;
	}

	// public AsmManager getStructureModelFor(String projectName) {
	// AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
	// returnc compiler.getStructureModelFor(projectName)
	// }

	/** Drives a full build **/
	public boolean doFullBuild(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		resetCompilerRecords(compiler);
		addSourceFilesToBuild(projectName, compiler);
		addXmlConfigFilesToBuild(projectName, compiler);
		pause(1000); // delay to allow previous runs build stamps to be OK
		lognoln("Building project '" + projectName + "'");
		compiler.buildFresh();
		log("");
		checkForErrors(compiler);
		log("Build finished, time taken = "
				+ ((MultiProjTestBuildProgressMonitor) compiler.getBuildProgressMonitor()).getTimeTaken() + "ms");
		return true;
	}

	/**
	 * Clears any maps associated with the compiler
	 */
	private void resetCompilerRecords(AjCompiler compiler) {
		((MultiProjTestBuildProgressMonitor) compiler.getBuildProgressMonitor()).reset();
		((MultiProjTestMessageHandler) compiler.getMessageHandler()).reset();
	}

	/**
	 * Find the source files associated with the given project and add them to the list of projectSourceFiles in the
	 * MultiProjTestCompilerConfiguration to be used in the subsequent build
	 */
	private void addSourceFilesToBuild(String pname, AjCompiler compiler) {
		File projectBase = new File(sandboxDir, pname);
		ICompilerConfiguration icc = compiler.getCompilerConfiguration();
		List<String> currentFiles = icc.getProjectSourceFiles();
		List<String> filesForCompilation = new ArrayList<>();
		collectUpFiles(projectBase, projectBase, filesForCompilation);
		boolean changed = false;
		for (String s : filesForCompilation) {
			if (!currentFiles.contains(s)) {
				changed = true;
			}
		}
		for (String currentFile : currentFiles) {
			if (!filesForCompilation.contains(currentFile)) {
				changed = true;
			}
		}
		if (changed) {
			((MultiProjTestCompilerConfiguration) icc).setProjectSourceFiles(filesForCompilation);
		}
	}

	private void addXmlConfigFilesToBuild(String pname, AjCompiler compiler) {
		File projectBase = new File(sandboxDir, pname);
		ICompilerConfiguration icc = compiler.getCompilerConfiguration();
		List<String> currentXmlFiles = icc.getProjectXmlConfigFiles();
		List<String> collector = new ArrayList<>();
		collectUpXmlFiles(projectBase, projectBase, collector);
		boolean changed = false;
		for (String s : collector) {
			if (!currentXmlFiles.contains(s)) {
				changed = true;
			}
		}
		for (String currentXmlFile : currentXmlFiles) {
			if (!collector.contains(currentXmlFile)) {
				changed = true;
			}
		}
		if (changed) {
			((MultiProjTestCompilerConfiguration) icc).setProjectXmlConfigFiles(collector);
		}
	}

	private void collectUpFiles(File location, File base, List<String> collectionPoint) {
		String contents[] = location.list();
		if (contents == null) {
			return;
		}
		for (String string : contents) {
			File f = new File(location, string);
			if (f.isDirectory()) {
				collectUpFiles(f, base, collectionPoint);
			} else if (f.isFile() && (f.getName().endsWith(".aj") || f.getName().endsWith(".java"))) {
				String fileFound;
				try {
					fileFound = f.getCanonicalPath();
					collectionPoint.add(fileFound);
					// String toRemove = base.getCanonicalPath();
					// if (!fileFound.startsWith(toRemove)) throw new RuntimeException("eh? "+fileFound+"   "+toRemove);
					// collectionPoint.add(fileFound.substring(toRemove.length()+1));//+1 captures extra separator
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void collectUpXmlFiles(File location, File base, List<String> collectionPoint) {
		String contents[] = location.list();
		if (contents == null) {
			return;
		}
		for (String string : contents) {
			File f = new File(location, string);
			if (f.isDirectory()) {
				collectUpXmlFiles(f, base, collectionPoint);
			} else if (f.isFile() && f.getName().endsWith(".xml")) {
				String fileFound;
				try {
					fileFound = f.getCanonicalPath();
					collectionPoint.add(fileFound);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Make sure no errors have been recorded
	 */
	private void checkForErrors(AjCompiler compiler) {
		if (AjdeInteractionTestbed.VERBOSE) {
			MultiProjTestMessageHandler handler = (MultiProjTestMessageHandler) compiler.getMessageHandler();
			if (handler.hasErrorMessages()) {
				System.err.println("Build errors:");
				for (IMessage message : handler.getErrorMessages()) {
					System.err.println(message);
				}
				System.err.println("---------");
			}
		}
	}

	public List<IMessage> getErrorMessages(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return ((MultiProjTestMessageHandler) compiler.getMessageHandler()).getErrorMessages();
	}

	public List<IMessage> getWarningMessages(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return ((MultiProjTestMessageHandler) compiler.getMessageHandler()).getWarningMessages();
	}

	public List<IMessage> getWeavingMessages(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return ((MultiProjTestMessageHandler) compiler.getMessageHandler()).getWeavingMessages();
	}

	public List<String> getCompilerErrorMessages(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return ((MultiProjTestMessageHandler) compiler.getMessageHandler()).getCompilerErrors();
	}

	public void checkForError(String projectName, String anError) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		List<IMessage> messages = ((MultiProjTestMessageHandler) compiler.getMessageHandler()).getErrorMessages();
		for (IMessage element : messages) {
			if (element.getMessage().contains(anError)) {
				return;
			}
		}
		fail("Didn't find the error message:\n'" + anError + "'.\nErrors that occurred:\n" + messages);
	}

	private void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ie) {
		}
	}

	// Methods for querying what happened during a build and accessing information
	// about the build:

	/**
	 * Helper method for dumping info about which files were compiled and woven during the last build.
	 */
	public String printCompiledAndWovenFiles(String projectName) {
		StringBuffer sb = new StringBuffer();
		if (getCompiledFiles(projectName).size() == 0 && getWovenClasses(projectName).size() == 0) {
			sb.append("No files were compiled or woven\n");
		}
		for (String element: getCompiledFiles(projectName)) {
			sb.append("compiled: " + element + "\n");
		}
		for (String element: getWovenClasses(projectName)) {
			sb.append("woven: " + element + "\n");
		}
		return sb.toString();
	}

	/**
	 * Summary report on what happened in the most recent build
	 */
	public void printBuildReport(String projectName) {
		System.out.println("\n====== BUILD REPORT (Project " + projectName + ") ===========");
		System.out.println("Build took: " + getTimeTakenForBuild(projectName) + "ms");
		List<String> compiled = getCompiledFiles(projectName);
		System.out.println("Compiled: " + compiled.size() + " files");
		for (String value : compiled) {
			System.out.println("        :" + value);
		}
		List<String> woven = getWovenClasses(projectName);
		System.out.println("Wove: " + woven.size() + " files");
		for (String s : woven) {
			System.out.println("    :" + s);
		}
		if (wasFullBuild()) {
			System.out.println("It was a batch (full) build");
		}
		System.out.println("=============================================");
	}

	/**
	 * Check we compiled/wove the right number of files, passing '-1' indicates you don't care about that number.
	 */
	public void checkCompileWeaveCount(String projectName, int expCompile, int expWoven) {
		if (expCompile != -1 && getCompiledFiles(projectName).size() != expCompile) {
			fail("Expected compilation of " + expCompile + " files but compiled " + getCompiledFiles(projectName).size() + "\n"
					+ printCompiledAndWovenFiles(projectName));
		}
		if (expWoven != -1 && getWovenClasses(projectName).size() != expWoven) {
			fail("Expected weaving of " + expWoven + " files but wove " + getWovenClasses(projectName).size() + "\n"
					+ printCompiledAndWovenFiles(projectName));
		}
	}

	public void checkWasntFullBuild() {
		assertTrue("Shouldn't have been a full (batch) build", !wasFullBuild());
	}

	public void checkWasFullBuild() {
		assertTrue("Should have been a full (batch) build", wasFullBuild());
	}

	public boolean wasFullBuild() {
		// alternatives: statelistener is debug interface, progressmonitor is new proper interface (see pr145689)
		// return MyBuildProgressMonitor.wasFullBuild();
		return MyStateListener.wasFullBuild();
	}

	public long getTimeTakenForBuild(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return ((MultiProjTestBuildProgressMonitor) compiler.getBuildProgressMonitor()).getTimeTaken();
	}

	public List<String> getCompiledFiles(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return ((MultiProjTestBuildProgressMonitor) compiler.getBuildProgressMonitor()).getCompiledFiles();
	}

	public AsmManager getModelFor(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return compiler.getModel();
	}

	public List<String> getWovenClasses(String projectName) {
		AjCompiler compiler = CompilerFactory.getCompilerForProjectWithDir(sandboxDir + File.separator + projectName);
		return ((MultiProjTestBuildProgressMonitor) compiler.getBuildProgressMonitor()).getWovenClasses();
	}

	// Infrastructure below here

	private static void log(String msg) {
		if (VERBOSE) {
			System.out.println(msg);
		}
	}

	private static void lognoln(String msg) {
		if (VERBOSE) {
			System.out.print(msg);
		}
	}

	/** Return the *full* path to this file which is taken relative to the project dir */
	protected static String getFile(String projectName, String path) {
		return new File(sandboxDir, projectName + File.separatorChar + path).getAbsolutePath();
	}

	static class MyStateListener extends AbstractStateListener {

		private static MyStateListener _instance = new MyStateListener();

		private MyStateListener() {
			reset();
		}

		public static MyStateListener getInstance() {
			return _instance;
		}

		public static boolean informedAboutKindOfBuild;
		public static boolean fullBuildOccurred;
		public static List<String> detectedDeletions = new ArrayList<>();
		public static StringBuffer decisions = new StringBuffer();

		public static void reset() {
			informedAboutKindOfBuild = false;
			decisions = new StringBuffer();
			fullBuildOccurred = false;
			if (detectedDeletions != null) {
				detectedDeletions.clear();
			}
		}

		public boolean pathChange = false;

		public void pathChangeDetected() {
			pathChange = true;
		}

		public void aboutToCompareClasspaths(List oldClasspath, List newClasspath) {
		}

		public void detectedClassChangeInThisDir(File f) {
			recordDecision("Detected class change in this directory: " + f.toString());
		}

		public void detectedAspectDeleted(File f) {
			detectedDeletions.add(f.toString());
		}

		public void buildSuccessful(boolean wasFullBuild) {
			informedAboutKindOfBuild = true;
			fullBuildOccurred = wasFullBuild;
		}

		public static String getDecisions() {
			return decisions.toString();
		}

		public static boolean wasFullBuild() {
			if (!informedAboutKindOfBuild) {
				throw new RuntimeException("I never heard about what kind of build it was!!");
			}
			return fullBuildOccurred;
		}

		// not needed just yet...
		// public void recordInformation(String s) { decisions.append(s).append("\n");}
		public void recordDecision(String s) {
			decisions.append(s).append("\n");
			log(s);
		}
	}
}