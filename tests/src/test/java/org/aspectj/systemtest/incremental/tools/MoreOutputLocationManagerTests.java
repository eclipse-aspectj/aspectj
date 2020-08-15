/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.util.FileUtil;
import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * Similar to OutputLocationManagerTests, however, tests the different scenarios when no outputDir is set but instead there is an
 * OutputLocationManager which returns the same output location for all files and resources.
 * 
 * There are eight places where AjBuildConfig.getOutputDir() is called that are tested here:
 * 
 * AjBuildManager.getOutputClassFileName(..) - testCorrectInfoWhenNoOutputPath AjBuildManager.initBcelWorld(..) -
 * testPathResolutionWithInpathDirAndNoOutputPath testPathResolutionWithInpathJarAndNoOutputPath AjBuildManager.writeManifest(..) -
 * testCopyManifest AjBuildManager.writeOutxml(..) - testOutxml - testOutXmlForAspectsWithDifferentOutputDirs
 * AjState.createUnwovenClassFile(..) - testPathResolutionAfterChangeInClassOnInpath AjState.deleteResources(..) -
 * testAjStateDeleteResources AjState.maybeDeleteResources(..) - testAjStateDeleteResourcesInInputDir
 * AjState.removeAllResultsOfLastBuild(..) - testAllResourcesAreDeletedCorrectlyOnPathChange
 * IncrementalStateManager.findStateManagingOutputLocation(..) - testFindStateManagingOutputLocation
 * 
 * The other three places are not tested here because they were implemented when OutputLocationManager was introduced.
 * 
 */
public class MoreOutputLocationManagerTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	private String inpathTestingDir;
	private String expectedOutputDir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("inpathTesting");
		inpathTestingDir = getWorkingDir() + File.separator + "inpathTesting";
		expectedOutputDir = inpathTestingDir + File.separator + "bin";
		configureOutputLocationManager("inpathTesting", new SingleDirOutputLocMgr(inpathTestingDir));
	}

	/**
	 * Tests that the UnwovenClassFiles have the correct path when there is no outputDir but there is an OutputLocationManager. Is a
	 * simple project that has no inpath setting
	 */
	public void testCorrectInfoWhenNoOutputPath() {
		build("inpathTesting");
		AjState state = getState();

		Map<String,File> classNameToFileMap = state.getClassNameToFileMap();
		assertFalse("expected there to be classes ", classNameToFileMap.isEmpty());
		Set<Map.Entry<String,File>> entrySet = classNameToFileMap.entrySet();
		for (Map.Entry<String, File> entry : entrySet) {
			String className = entry.getKey();
			String fullClassName = expectedOutputDir + File.separator + className.replace('.', File.separatorChar) + ".class";
			File file = entry.getValue();
			assertEquals("expected file to have path \n" + fullClassName + ", but" + " found path \n" + file.getAbsolutePath(),
					fullClassName, file.getAbsolutePath());
		}
	}

	/**
	 * Tests that can retieve the state that manages a given output location when there is no outputDir set
	 */
	public void testFindStateManagingOutputLocation() {
		build("inpathTesting");
		AjState state = IncrementalStateManager.findStateManagingOutputLocation(new File(expectedOutputDir));
		assertNotNull("Expected to find a state that managed output location " + expectedOutputDir + ", but did not", state);

	}

	/**
	 * Tests that the UnwovenClassFiles corresponding to classes on the inpath have the correct class name when there is no output
	 * directory (ultimately tests AjBuildManager.initBcelWorld() when there is a jar on the inpath). Only does one build.
	 */
	public void testPathResolutionWithInpathDirAndNoOutputPath() {
		String inpathDir = inpathTestingDir + File.separator + "injarBin" + File.separator + "pkg";
		addInpathEntry(inpathDir);
		build("inpathTesting");

		// expect to compile the aspect in 'inpathTesting' project and weave
		// both the aspect and the class on the inpath.
		checkCompileWeaveCount("inpathTesting", 1, 2);

		// get hold of the state for this project - expect to find one
		AjState state = getState();

		// the classes onthe inpath are recorded against the AjBuildManager
		// (they are deleted from the ajstate whilst cleaning up after a build)
		Map<String,List<UnwovenClassFile>> binarySources = state.getAjBuildManager().getBinarySourcesForThisWeave();
		assertFalse("expected there to be binary sources from the inpath setting but didn't find any", binarySources.isEmpty());

		List<UnwovenClassFile> unwovenClassFiles = binarySources.get(inpathDir + File.separator + "InpathClass.class");
		List<String> fileNames = new ArrayList<>();
		// the unwovenClassFiles should have filenames that point to the output dir
		// (which in this case is the sandbox dir) and not where they came from.
		for (UnwovenClassFile ucf: unwovenClassFiles) {
			if (!ucf.getFilename().contains(expectedOutputDir)) {
				fileNames.add(ucf.getFilename());
			}
		}
		assertTrue("expected to find UnwovenClassFile from directory\n" + expectedOutputDir + ", \n but found files " + fileNames,
				fileNames.isEmpty());
	}

	/**
	 * Tests that the UnwovenClassFiles corresponding to classes on the inpath have the correct class name when there is no output
	 * directory (ultimately tests AjState.createUnwovenClassFile(BinarySourceFile) and ensures the unwovenClassFile has the correct
	 * name. Makes a change to a class file on the inpath to ensure we enter this method (there is a check that says are we the
	 * first build))
	 */
	public void testPathResolutionAfterChangeInClassOnInpath() throws Exception {
		String inpathDir = inpathTestingDir + File.separator + "injarBin" + File.separator + "pkg";
		addInpathEntry(inpathDir);
		build("inpathTesting");

		// build again so that we enter
		// AjState.createUnwovenClassFile(BinarySourceFile)
		File from = new File(testdataSrcDir + File.separatorChar + "inpathTesting" + File.separatorChar + "newInpathClass"
				+ File.separatorChar + "InpathClass.class");
		File destination = new File(inpathDir + File.separatorChar + "InpathClass.class");
		FileUtil.copyFile(from, destination);

		// get hold of the state for this project - expect to find one
		AjState state = getState();
		AjBuildConfig buildConfig = state.getBuildConfig();
		state.prepareForNextBuild(buildConfig);

		Map<String, List<UnwovenClassFile>> binarySources = state.getBinaryFilesToCompile(true);
		assertFalse("expected there to be binary sources from the inpath setting but didn't find any", binarySources.isEmpty());

		List<UnwovenClassFile> unwovenClassFiles = binarySources.get(inpathDir + File.separator + "InpathClass.class");
		List<String> fileNames = new ArrayList<>();
		// the unwovenClassFiles should have filenames that point to the output dir
		// (which in this case is the sandbox dir) and not where they came from.
		for (UnwovenClassFile ucf: unwovenClassFiles) {
			if (!ucf.getFilename().contains(expectedOutputDir)) {
				fileNames.add(ucf.getFilename());
			}
		}
		assertTrue("expected to find UnwovenClassFile from directory\n" + expectedOutputDir + ", \n but found files " + fileNames,
				fileNames.isEmpty());
	}

	/**
	 * Tests that the UnwovenClassFiles corresponding to jars on the inpath have the correct class name when there is no output path
	 * (ultimately tests AjBuildManager.initBcelWorld() when there is a jar on the inpath). Only does one build.
	 */
	public void testPathResolutionWithInpathJarAndNoOutputPath() {
		String inpathDir = inpathTestingDir + File.separator + "inpathJar.jar";
		addInpathEntry(inpathDir);
		build("inpathTesting");
		// expect to compile the aspect in 'inpathTesting' project and weave
		// both the aspect and the class in the jar on the inpath.
		checkCompileWeaveCount("inpathTesting", 1, 2);

		AjState state = getState();

		// tests AjState.createUnwovenClassFile(BinarySourceFile)
		Map<String,List<UnwovenClassFile>> binarySources = state.getAjBuildManager().getBinarySourcesForThisWeave();
		assertFalse("expected there to be binary sources from the inpath setting but didn't find any", binarySources.isEmpty());

		List<UnwovenClassFile> unwovenClassFiles = binarySources.get(inpathDir);
		List<String> fileNames = new ArrayList<>();

		for (UnwovenClassFile ucf: unwovenClassFiles) {
			if (!ucf.getFilename().contains(expectedOutputDir)) {
				fileNames.add(ucf.getFilename());
			}
		}
		assertTrue("expected to find UnwovenClassFile from directory\n" + expectedOutputDir + ", \n but found files " + fileNames,
				fileNames.isEmpty());

	}

	/**
	 * A manifest file is in the jar on the inpath - check that it's copied to the correct place
	 */
	public void testCopyManifest() {
		String inpathDir = inpathTestingDir + File.separator + "inpathJar.jar";
		addInpathEntry(inpathDir);

		build("inpathTesting");
		String resource = expectedOutputDir + File.separator + "META-INF" + File.separator + "MANIFEST.MF";
		File f = new File(resource);
		assertTrue("expected file " + resource + " to exist but it did not", f.exists());
	}

	/**
	 * "resources" are contained within inpath jars - check that a text file contained within a jar is copied and then deleted
	 * correctly. Essentially tests AjState.deleteResources().
	 */
	// see 243376: for now don't do this, waste of cpu - ajdt better for handling resources - but is that true for inpath resources?
	// public void testAjStateDeleteResources() {
	// String inpathDir = inpathTestingDir + File.separator + "inpathJar.jar";
	// addInpathEntry(inpathDir);
	//		
	// build("inpathTesting");
	//		
	// AjState state = getState();
	//		
	// String resource = expectedOutputDir + File.separator + "inpathResource.txt";
	// File f = new File(resource);
	// assertTrue("expected file " + resource + " to exist but it did not",f.exists());
	// // this call should delete the resources
	// state.getFilesToCompile(true);
	// assertFalse("did not expect the file " + resource + " to exist but it does",f.exists());
	// }
	/**
	 * Can set to copy resources that are in inpath dirs - check that a text file contained within such a dir is copied and then
	 * deleted correctly. Essentially tests AjState.maybeDeleteResources().
	 */
	// see 243376: for now don't do this, waste of cpu - ajdt better for handling resources - but is that true for inpath resources?
	// public void testAjStateDeleteResourcesInInputDir() {
	// // temporary problem with this on linux, think it is a filesystem lastmodtime issue
	// if (System.getProperty("os.name","").toLowerCase().equals("linux")) return;
	// if (System.getProperty("os.name","").toLowerCase().indexOf("mac")!=-1) return;
	//
	// AjBuildManager.COPY_INPATH_DIR_RESOURCES = true;
	// try {
	// String inpathDir = inpathTestingDir + File.separator + "injarBin"
	// + File.separator + "pkg";
	// addInpathEntry(inpathDir);
	// build("inpathTesting");
	// AjState state = getState();
	// String resource = "inDirResource.txt";
	// assertTrue("expected state to have resource " + resource + "but it did not",
	// state.hasResource(resource));
	// // this call should delete the resources - tests AjState.deleteResources()
	// state.getFilesToCompile(true);
	// assertFalse("did not expect state to have resource " + resource +
	// " but found that it did", state.hasResource(resource));
	// } finally {
	// AjBuildManager.COPY_INPATH_DIR_RESOURCES = false;
	// }
	//		
	// }
	/**
	 * Changing inpath entry from a jar to a directory between builds means that AjState should realise somethings changed. This
	 * causes all resources (Manifest and txt files) to be deleted. Also should be a full build. Essentially tests
	 * AjState.removeAllResultsFromLastBuild().
	 */
	public void testAllResourcesAreDeletedCorrectlyOnPathChange() {
		String inpathJar = inpathTestingDir + File.separator + "inpathJar.jar";

		addInpathEntry(inpathJar);
		build("inpathTesting");

		String resource = expectedOutputDir + File.separator + "inpathResource.txt";
		File f = new File(resource);
		assertTrue("expected file " + resource + " to exist but it did not", f.exists());

		// this should force a change and the file is deleted
		// tests AjState.removeAllResultsFromLastBuild()
		addInpathEntry(null);
		build("inpathTesting");
		assertFalse("did not expect the file " + resource + " to exist but it does", f.exists());

		checkWasFullBuild();
	}

	public void testOutxml() {
		configureNonStandardCompileOptions("inpathTesting", "-outxml");
		build("inpathTesting");
		String resource = expectedOutputDir + File.separator + "META-INF" + File.separator + "aop-ajc.xml";
		File f = new File(resource);
		assertTrue("expected file " + resource + " to exist but it did not", f.exists());
	}

	public void testAspectsRecordedOnlyOnceInState() {
		configureNonStandardCompileOptions("inpathTesting", "-outxml");
		build("inpathTesting");
		AjState state = getState();
		Map<String,char[]> m = state.getAspectNamesToFileNameMap();
		assertEquals("Expected only one aspect recored in the state but found " + m.size(), 1, m.size());
		build("inpathTesting");
		m = state.getAspectNamesToFileNameMap();
		assertEquals("Expected only one aspect recored in the state but found " + m.size(), 1, m.size());
	}

	private AjState getState() {
		// get hold of the state for this project - expect to find one
		AjState state = IncrementalStateManager.retrieveStateFor(inpathTestingDir);
		assertNotNull("expected to find AjState for build config " + inpathTestingDir + " but didn't", state);
		return state;
	}

	private void addInpathEntry(String entry) {
		if (entry == null) {
			configureInPath("inpathTesting", (Set)null);
			return;
		}
		File f = new File(entry);
		Set<File> s = new HashSet<>();
		s.add(f);
		configureInPath("inpathTesting", s);
	}

	/**
	 * Sends all output to the same directory
	 */
	private static class SingleDirOutputLocMgr implements IOutputLocationManager {

		private File classOutputLoc;
		private File resourceOutputLoc;
		private String testProjectOutputPath;
		private List<File> allOutputLocations;
		private File outputLoc;

		public SingleDirOutputLocMgr(String testProjectPath) {
			this.testProjectOutputPath = testProjectPath + File.separator + "bin";
			outputLoc = new File(testProjectOutputPath);

			allOutputLocations = new ArrayList<>();
			allOutputLocations.add(outputLoc);
		}

		@Override
		public File getOutputLocationForClass(File compilationUnit) {
			return outputLoc;
		}
		
		@Override
		public Map<File,String> getInpathMap() {
			return Collections.emptyMap();
		}


		@Override
		public File getOutputLocationForResource(File resource) {
			return outputLoc;
		}

		@Override
		public List<File> getAllOutputLocations() {
			return allOutputLocations;
		}

		@Override
		public File getDefaultOutputLocation() {
			return outputLoc;
		}

		@Override
		public void reportFileWrite(String outputfile, int filetype) {
		}

		@Override
		public void reportFileRemove(String outputfile, int filetype) {
		}

		@Override
		public String getSourceFolderForFile(File sourceFile) {
			return null; // no impl
		}

		@Override
		public int discoverChangesSince(File dir, long buildtime) {
			return 0; // no impl
		}
	}

}
