/* *******************************************************************
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.bridge.IMessage;

public class ReweavableTest extends AjdeCoreTestCase {

	private static final boolean debugTests = false;

	public static final String binDir = "bin";

	public static final String indir1Name = "indir1";
	public static final String indir2Name = "indir2";
	public static final String injarName = "injar.jar";
	public static final String outjarName = "/bin/output.jar";

	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("ReweavableTest");
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		handler.dontIgnore(IMessage.INFO);
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}

	/**
	 * Aim: Check we haven't damaged 'normal compilation' when not supplying -Xreweavable. Also determines baseline sizes for the
	 * compiled class files for later comparison.
	 * 
	 * Inputs to the compiler: NonReweavable1.lst -> CalculatePI.java -> Logger.aj -> -verbose -> -noExit
	 * 
	 * Expected result = Compile successful, the types will not be reweavable and the weaver should not report it is running in
	 * reweavable mode.
	 */
	public void testNonReweavableCompile() {
		if (debugTests)
			System.out.println("testNonReweavableCompile: Building with NonReweavable1.lst");
		String[] files = new String[] { "CalculatePI.java", "Logger.aj" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-verbose -noExit -XnotReweavable");

		doBuild(true);

		assertFalse("Did not expect to find a message about the weaver operating " + "in reweavable mode",
				checkFor("weaver operating in reweavable mode"));

		File fCalc = openFile("bin/CalculatePI.class");
		File fLog = openFile("bin/Logger.class");
		assertTrue("bin/CalculatePI.class should exist?!?", fCalc.exists());
		assertTrue("bin/Logger.class should exist?!?", fLog.exists());
		if (debugTests)
			System.out.println("CalculatePI.class is of size: " + fCalc.length());
		if (debugTests)
			System.out.println("Logger.class is of size: " + fLog.length());
		if (debugTests)
			System.out.println("\n\n\n");
		/* nonreweavesize_CalculatePI = (int) */fCalc.length();
		/* nonreweavesize_Logger = (int) */fLog.length();
	}

	/**
	 * Aim: Basic call to -Xreweavable. Weaver should report it is in reweavable mode and the classes produced should be much larger
	 * than normal classes (those produced in the first test).
	 * 
	 * Inputs to the compiler: Reweavable1.lst -> CalculatePI.java -> Logger.aj -> -Xreweavable -> -verbose -> -noExit
	 * 
	 * Expected result = Compile successful, the types will be reweavable and the weaver should report it is running in reweavable
	 * mode. The files produced should be larger than those created during the last test.
	 */
	public void testReweavableCompile() {
		if (debugTests)
			System.out.println("testReweavableCompile: Building with Reweavable1.lst");
		String[] files = new String[] { "CalculatePI.java", "Logger.aj" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-verbose -noExit");

		doBuild(true);

		assertTrue("Expected a message about operating in reweavable mode, but " + "didn't get one",
				checkFor("weaver operating in reweavable mode"));

		File fCalc = openFile("bin/CalculatePI.class");
		File fLog = openFile("bin/Logger.class");
		assertTrue("bin/CalculatePI.class should exist?!?", fCalc.exists());
		assertTrue("bin/Logger.class should exist?!?", fLog.exists());
		if (debugTests)
			System.out.println("CalculatePI.class is of size: " + fCalc.length());
		if (debugTests)
			System.out.println("Logger.class is of size: " + fLog.length());
		// Temporarily remove these tests - it seems the order in which the
		// testXXX methods are run cannot be relied upon
		// so reweavablesize_XXX fields might not have been set yet.
		// assertTrue("Reweavable version should be larger than non-reweavable
		// version of CalculatePI",
		// fCalc.length()>nonreweavesize_CalculatePI);
		// assertTrue("Reweavable version should be larger than non-reweavable
		// version of Logger",
		// fLog.length()>nonreweavesize_Logger);

		/* reweavablesize_CalculatePI = (int) */fCalc.length();
		/* reweavablesize_Logger = (int) */fLog.length();

		if (debugTests)
			System.out.println("\n\n\n");
	}

	/**
	 * Aim: Use the optional ':compress' modifier on -Xreweavable. This causes some of the meta-data for use in reweaving to be
	 * compressed. It should succeed and produce class files smaller than straight -Xreweavable but larger than without specifying
	 * -Xreweavable.
	 * 
	 * Inputs to the compiler: ReweavableCompress1.lst -> CalculatePI.java -> Logger.aj -> -Xreweavable:compress -> -verbose ->
	 * -noExit
	 * 
	 * Expected result = Compile successful, the types will be reweavable and the weaver should report it is running in reweavable
	 * mode. The files created should have a size between the non-reweavable versions and the reweavable (without compression)
	 * versions.
	 */
	public void testReweavableCompressCompile() {
		if (debugTests)
			System.out.println("testReweavableCompressCompile: Building with ReweavableCompress1.lst");

		String[] files = new String[] { "CalculatePI.java", "Logger.aj" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-Xreweavable:compress -verbose -noExit");

		doBuild(true);

		assertTrue("Expected a message about operating in reweavable mode, but didn't get one",
				checkFor("weaver operating in reweavable mode"));

		File fCalc = openFile("bin/CalculatePI.class");
		File fLog = openFile("bin/Logger.class");
		assertTrue("bin/CalculatePI.class should exist?!?", fCalc.exists());
		assertTrue("bin/Logger.class should exist?!?", fLog.exists());
		int calclen = (int) fCalc.length();
		int loglen = (int) fLog.length();
		if (debugTests)
			System.out.println("CalculatePI.class is of size: " + calclen);
		if (debugTests)
			System.out.println("Logger.class is of size: " + loglen);
		// Temporarily remove these tests - it seems the order in which the
		// testXXX methods are run cannot be relied upon
		// so reweavablesize_XXX fields might not have been set yet.
		// assertTrue("Reweavable version should be larger than non-reweavable
		// version of CalculatePI",
		// calclen>nonreweavesize_CalculatePI);
		// assertTrue("Reweavable version should be larger than non-reweavable
		// version of Logger",
		// loglen>nonreweavesize_Logger);

		// Temporarily remove these tests - it seems the order in which the
		// testXXX methods are run cannot be relied upon
		// so reweavablesize_XXX fields might not have been set yet.
		// assertTrue("Reweavable (with compression) version should be smaller
		// than reweavable (without compression) version of CalculatePI:" +
		// " Compressed version:"+calclen+"bytes Non-compressed
		// version:"+reweavablesize_CalculatePI+"bytes",
		// calclen<reweavablesize_CalculatePI);
		// assertTrue("Reweavable (with compression) version should be smaller
		// than reweavable (without compression) version of Logger"+
		// " Compressed version:"+loglen+"bytes Non-compressed
		// version:"+reweavablesize_Logger+"bytes",
		// loglen<reweavablesize_Logger);

		if (debugTests)
			System.out.println("\n\n\n");
	}

	/**
	 * Aim: The tests above have determined that reweaving appears to be behaving in terms of the .class files it is creating. Now
	 * lets actually attempt a reweave. For this, we build two files as reweavable and then build a single file whilst specifying an
	 * inpath that contains the .class files from the first compile. This should succeed.
	 * 
	 * Inputs to the first compile: Reweavable1.lst -> CalculatePI.java -> Logger.aj -> -Xreweavable -> -verbose -> -noExit
	 * 
	 * Input to the second compile: Reweavable2.lst -> SecondAspect.aj -> -Xreweavable -> -verbose -> -noExit -inpath bin\.
	 * 
	 * Expected result = Both compiles will succeed.
	 */
	public void testReweavableSimpleCompile() {
		if (debugTests)
			System.out.println("testReweavableSimpleCompile: Building with Reweavable1.lst");

		String[] files = new String[] { "CalculatePI.java", "Logger.aj" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-verbose -noExit");

		doBuild(true);

		assertTrue("Expected a message about operating in reweavable mode, but didn't get one",
				checkFor("weaver operating in reweavable mode"));

		if (debugTests)
			System.out.println("\ntestReweavableSimpleCompile: Building with Reweavable2.lst");
		Set<File> paths = new HashSet<>();
		paths.add(openFile(binDir));
		compilerConfig.setInpath(paths);
		String[] newFiles = new String[] { "SecondAspect.aj" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(newFiles));

		doBuild(true);

		String expMessage = "successfully verified type Logger exists";
		assertTrue("Expected message '" + expMessage + "' but did not find it", checkFor(expMessage));

		File fCalc = openFile("bin/CalculatePI.class");
		File fLog = openFile("bin/Logger.class");
		File fSec = openFile("bin/SecondAspect.class");
		assertTrue("bin/CalculatePI.class should exist?!?", fCalc.exists());
		assertTrue("bin/Logger.class should exist?!?", fLog.exists());
		assertTrue("bin/SecondAspect.class should exist?!?", fSec.exists());

		if (debugTests)
			System.out.println("\n\n\n");
	}

	/**
	 * Aim: Based on the test above, if we delete Logger.class between the first and second compiles the second compile should fail
	 * because there is not enough information to reweave CalculatePI
	 * 
	 * Inputs to the first compile: Reweavable1.lst -> CalculatePI.java -> Logger.aj -> -Xreweavable -> -verbose -> -noExit
	 * 
	 * Input to the second compile: Reweavable2.lst -> SecondAspect.aj -> -Xreweavable -> -verbose -> -noExit -inpath bin\.
	 * 
	 * Expected result = Second compile will fail - reporting that Logger is missing (it 'touched' in the first compile CalculatePI)
	 */
	public void testForReweavableSimpleErrorCompile() {
		if (debugTests)
			System.out.println("testForReweavableSimpleErrorCompile: Building with Reweavable2.lst");

		String[] files = new String[] { "CalculatePI.java", "Logger.aj" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-verbose -noExit");

		doBuild(true);

		assertTrue("Expected a message about operating in reweavable mode, but didn't get one",
				checkFor("weaver operating in reweavable mode"));

		assertTrue("Could not delete bin/Logger.class??", openFile("bin/Logger.class").delete());

		if (debugTests)
			System.out.println("\ntestForReweavableSimpleErrorCompile: Building with Reweavable2.lst");
		Set<File> paths = new HashSet<>();
		paths.add(openFile(binDir));
		compilerConfig.setInpath(paths);
		String[] newFiles = new String[] { "SecondAspect.aj" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(newFiles));

		doBuild(true);

		String expMessage = "aspect Logger cannot be found when reweaving CalculatePI";
		assertTrue("Expected message '" + expMessage + "' but did not find it", checkFor(expMessage));

		File fCalc = openFile("bin/CalculatePI.class");
		File fLog = openFile("bin/Logger.class");
		File fSec = openFile("bin/SecondAspect.class");
		assertTrue("bin/CalculatePI.class should exist!", fCalc.exists());
		assertTrue("bin/Logger.class should not exist!", !fLog.exists());
		assertTrue("bin/SecondAspect.class should not exist!", fSec.exists());

		if (debugTests)
			System.out.println("\n\n\n");
	}

	/**
	 * Aim: Based on the test above, if we delete Logger.class between the first and second compiles the second compile should fail
	 * because there is not enough information to reweave CalculatePI
	 * 
	 * Inputs to the first compile: TJP1.lst -> tjp/Demo.java -> tjp/GetInfo.java -> -Xreweavable -> -verbose -> -noExit
	 * 
	 * Now, delete bin\tjp\GetInfo.class and do a compile with: TJP2.lst -> -Xreweavable -> -verbose -> -noExit -inpath bin\.
	 * 
	 * Expected result = Second compile will fail - reporting that tjp.GetInfo is missing (it 'touched' in the first compile
	 * tjp.Demo)
	 */
	public void testErrorScenario2Compile() {
		if (debugTests)
			System.out.println("testErrorScenario2: Building with TJP1.lst");

		String[] files = new String[] { "tjp" + File.separator + "Demo.java", "tjp" + File.separator + "GetInfo.java" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-verbose -noExit");

		doBuild(true);

		assertTrue("Expected a message about operating in reweavable mode, but didn't get one",
				checkFor("weaver operating in reweavable mode"));

		assertTrue("Could not delete bin/tjp/GetInfo.class??", openFile("bin/tjp/GetInfo.class").delete());

		if (debugTests)
			System.out.println("\ntestErrorScenario2: Building with TJP2.lst");
		Set<File> paths = new HashSet<>();
		paths.add(openFile(binDir));
		compilerConfig.setInpath(paths);
		compilerConfig.setProjectSourceFiles(new ArrayList<>());
		doBuild(true);

		String expMessage = "aspect tjp.GetInfo cannot be found when reweaving tjp.Demo";
		assertTrue("Expected message '" + expMessage + "' but did not find it", checkFor(expMessage));

		File fDemo = openFile("bin/tjp/Demo.class");
		File fGetInfo = openFile("bin/tjp/GetInfo.class");
		assertTrue("bin/tjp/Demo.class should exist!", fDemo.exists());
		assertTrue("bin/tjp/GetInfo.class should not exist!", !fGetInfo.exists());

		if (debugTests)
			System.out.println("\n\n\n");
	}

	public void testWorkingScenario2Compile() {
		if (debugTests)
			System.out.println("testWorkingScenario2: Building with TJP1.lst");
		String[] files = new String[] { "tjp" + File.separator + "Demo.java", "tjp" + File.separator + "GetInfo.java" };
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-Xreweavable:compress -verbose -noExit");

		doBuild(true);

		assertTrue("Expected a message about operating in reweavable mode, but didn't get one",
				checkFor("weaver operating in reweavable mode"));

		if (debugTests)
			System.out.println("\ntestWorkingScenario2: Building with TJP2.lst");
		Set<File> paths = new HashSet<>();
		paths.add(openFile(binDir));
		compilerConfig.setInpath(paths);
		compilerConfig.setProjectSourceFiles(new ArrayList<>());
		doBuild(true);

		String expMessage = "successfully verified type tjp.GetInfo exists";
		assertTrue("Expected message '" + expMessage + "' but did not find it", checkFor(expMessage));

		File fGetInfo = openFile("bin/tjp/GetInfo.class");
		File fDemo = openFile("bin/tjp/Demo.class");
		assertTrue("bin/tjp/GetInfo.class should exist!", fGetInfo.exists());
		assertTrue("bin/tjp/Demo.class should not exist!", fDemo.exists());

		if (debugTests)
			System.out.println("\n\n\n");
	}
}
