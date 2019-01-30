/********************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement     Initial version
 *    Helen Hawkins    Converted to new interface (bug 148190)
 * ******************************************************************/
package org.aspectj.ajde.core.tests;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestBuildProgressMonitor;
import org.aspectj.ajde.core.TestCompilerConfiguration;

/**
 * It is now possible to cancel the compiler during either the compilation or
 * weaving phases - this testcase verifies a few cases, making sure the process
 * stops when expected. It can check the disk contents, but it doesn't right
 * now.
 * 
 * Two different .lst files are used during these tests: LoadsaCode.lst and
 * EvenMoreCode.lst which contain mixes of aspects and classes
 * 
 * Here are some things to think about that will help you understand what is on
 * the disk when we cancel the compiler.
 * 
 * There are 3 important phases worth remembering : - Compile all the types -
 * Weave all the aspects - Weave all the classes
 * 
 * Each of those steps goes through all the types. This means during the 'weave
 * all the aspects' step we are jumping over classes and during the 'weave all
 * the classes ' step we are jumping over aspects. Why is this important?
 * 
 * 
 * We only write bytes out during the 'weave all the classes ' phase and it is
 * even during that phase that we write out the bytes for aspects. This means if
 * you cancel during compilation or during the weaving of aspects - there will
 * be nothing on the disk. If you cancel whilst in the 'weave all the classes '
 * phase then the disk will contain anything finished with by the cancellation
 * point.
 */
public class BuildCancellingTest extends AjdeCoreTestCase {

	private final boolean debugTests = false;
	private TestBuildProgressMonitor programmableBPM;
	private TestCompilerConfiguration compilerConfig;

	private String[] loadsaCode = { "A1.aj", "A2.aj", "HW.java", "A3.aj",
			"A4.aj" };
	private String[] evenMoreCode = { "A1.aj", "Cl1.java", "A2.aj", "Cl2.java",
			"HW.java", "A3.aj", "Cl3.java", "A4.aj" };

	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("BuildCancelling");
		programmableBPM = (TestBuildProgressMonitor) getCompiler()
				.getBuildProgressMonitor();
		compilerConfig = (TestCompilerConfiguration) getCompiler()
				.getCompilerConfiguration();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		programmableBPM = null;
		compilerConfig = null;
	}

	/**
	 * After first compilation message, get it to cancel, there should be one
	 * more warning message about cancelling the compile and their should be
	 * nothing on the disk.
	 */
	public void testCancelFirstCompile() {
		if (debugTests)
			System.out
					.println("\n\n\ntestCancelFirstCompile: Building with LoadsaCode");

		compilerConfig.setProjectSourceFiles(getSourceFileList(loadsaCode));
		programmableBPM.cancelOn("compiled:", 1); // Force a cancel after the
													// first compile occurs

		doBuild(true);

		assertTrue("Should have cancelled after first compile?:"
				+ programmableBPM.numCompiledMessages,
				programmableBPM.numCompiledMessages == 1);
		// Comment out to check the disk contents

		// assertTrue("As weaving was cancelled, no files should have been
		// written out, but I found:"+wovenClassesFound(),
		// wovenClassesFound()==0);

		boolean expectedCancelMessageFound = checkFor("Compilation cancelled as requested");
		if (!expectedCancelMessageFound)
			dumpTaskData(); // Useful for debugging
		assertTrue(
				"Failed to get warning message about compilation being cancelled!",
				expectedCancelMessageFound);
	}

	/**
	 * After third compilation message, get it to cancel, there should be one
	 * more warning message about cancelling the compile and their should be
	 * nothing on the disk.
	 */
	public void testCancelThirdCompile() {
		if (debugTests)
			System.out
					.println("\n\n\ntestCancelThirdCompile: Building with LoadsaCode");

		compilerConfig.setProjectSourceFiles(getSourceFileList(loadsaCode));
		programmableBPM.cancelOn("compiled:", 3); // Force a cancel after the
													// third compile occurs

		doBuild(true);

		assertTrue("Should have cancelled after third compile?:"
				+ programmableBPM.numCompiledMessages,
				programmableBPM.numCompiledMessages == 3);
		// Comment out to check the disk contents
		// assertTrue("As weaving was cancelled, no files should have been
		// written out, but I found:"+wovenClassesFound(),
		// wovenClassesFound()==0);

		boolean expectedCancelMessageFound = checkFor("Compilation cancelled as requested");
		if (!expectedCancelMessageFound)
			dumpTaskData(); // Useful for debugging
		assertTrue(
				"Failed to get warning message about compilation being cancelled!",
				expectedCancelMessageFound);
	}

	/**
	 * After first weave aspect message, get it to cancel, there should be one
	 * more warning message about cancelling the weave and their should be
	 * nothing on the disk.
	 */
	public void testCancelFirstAspectWeave() {
		if (debugTests)
			System.out
					.println("\n\n\ntestCancelFirstAspectWeave: Building with LoadsaCode");

		compilerConfig.setProjectSourceFiles(getSourceFileList(loadsaCode));
		programmableBPM.cancelOn("woven aspect ", 1); // Force a cancel after
														// the first weave
														// aspect occurs

		doBuild(true);

		assertTrue("Should have cancelled after first aspect weave?:"
				+ programmableBPM.numWovenAspectMessages,
				programmableBPM.numWovenAspectMessages == 1);
		// Comment out to check the disk contents
		// assertTrue("As weaving was cancelled, no files should have been
		// written out?:"+wovenClassesFound(),
		// wovenClassesFound()==0);

		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound)
			dumpTaskData(); // Useful for debugging
		assertTrue(
				"Failed to get warning message about weaving being cancelled!",
				expectedCancelMessageFound);
	}

	/**
	 * After third weave aspect message, get it to cancel, there should be one
	 * more warning message about cancelling the weave and their should be
	 * nothing on the disk.
	 */
	public void testCancelThirdAspectWeave() {
		if (debugTests)
			System.out
					.println("\n\n\ntestCancelThirdAspectWeave: Building with LoadsaCode");
		compilerConfig.setProjectSourceFiles(getSourceFileList(loadsaCode));
		// Force a cancel after the third weave occurs.
		// This should leave two class files on disk - I think?
		programmableBPM.cancelOn("woven aspect ", 3);

		doBuild(true);
		assertTrue("Should have cancelled after third weave?:"
				+ programmableBPM.numWovenAspectMessages,
				programmableBPM.numWovenAspectMessages == 3);

		// Comment out to check disk contents
		// assertTrue("As weaving was cancelled, no files should have been
		// written out?:"+wovenClassesFound(),
		// wovenClassesFound()==0);

		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound)
			dumpTaskData(); // Useful for debugging
		assertTrue(
				"Failed to get warning message about weaving being cancelled!",
				expectedCancelMessageFound);

	}

	/**
	 * After first weave class message, get it to cancel, there should be one
	 * more warning message about cancelling the weave and their should be
	 * nothing on the disk.
	 * 
	 * EvenMoreCode.lst contains: A1.aj Cl1.java A2.aj Cl2.java HW.java A3.aj
	 * Cl3.java A4.aj
	 * 
	 */
	public void testCancelFirstClassWeave() {
		if (debugTests)
			System.out
					.println("testCancelFirstClassWeave: Building with EvenMoreCode");
		compilerConfig.setProjectSourceFiles(getSourceFileList(evenMoreCode));
		programmableBPM.cancelOn("woven class", 1);

		doBuild(true);

		// Should just be A1 on the disk - uncomment this line to verify that!
		// (and uncomment diskContents())
		// assertTrue("Incorrect disk contents found",diskContents("A1"));

		assertTrue("Should have cancelled after first class weave?:"
				+ programmableBPM.numWovenClassMessages,
				programmableBPM.numWovenClassMessages == 1);

		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound)
			dumpTaskData(); // Useful for debugging
		assertTrue(
				"Failed to get warning message about weaving being cancelled!",
				expectedCancelMessageFound);
	}

	/**
	 * After first weave aspect message, get it to cancel, there should be one
	 * more warning message about cancelling the weave and their should be
	 * nothing on the disk.
	 * 
	 * EvenMoreCode.lst contains: A1.aj Cl1.java A2.aj Cl2.java HW.java A3.aj
	 * Cl3.java A4.aj
	 * 
	 */
	public void testCancelSecondClassWeave() {
		if (debugTests)
			System.out
					.println("testCancelSecondClassWeave: Building with EvenMoreCode");
		compilerConfig.setProjectSourceFiles(getSourceFileList(evenMoreCode));
		programmableBPM.cancelOn("woven class", 2);

		doBuild(true);

		// Uncomment this line to verify disk contents(and uncomment
		// diskContents())
		// assertTrue("Incorrect disk contents found",diskContents("A1 Cl1
		// A2"));

		assertTrue("Should have cancelled after first class weave?:"
				+ programmableBPM.numWovenClassMessages,
				programmableBPM.numWovenClassMessages == 2);

		boolean expectedCancelMessageFound = checkFor("Weaving cancelled as requested");
		if (!expectedCancelMessageFound)
			dumpTaskData(); // Useful for debugging
		assertTrue(
				"Failed to get warning message about weaving being cancelled!",
				expectedCancelMessageFound);

	}
}
