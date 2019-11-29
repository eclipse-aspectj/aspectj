/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.aspectj.weaver.WeaverTestCase;

import junit.framework.TestCase;

public class ZipTestCase extends TestCase {

	File outDir;

	/**
	 * Constructor for ZipTestCase.
	 * 
	 * @param arg0
	 */
	public ZipTestCase(String arg0) {
		super(arg0);
	}

	public void setUp() {
		outDir = WeaverTestCase.getOutdir();
	}

	public void tearDown() {
		WeaverTestCase.removeOutDir();
		outDir = null;
	}

	public void zipTest(String fileName, String aspectjar) throws IOException {
		zipTest(fileName, aspectjar, false);
	}

	public void zipTest(String fileName, String aspectjar, boolean isInJar) throws IOException {
		File inFile = new File(fileName);
		File outFile = new File(outDir, inFile.getName());
		BcelWorld world = new BcelWorld();
		BcelWeaver weaver = new BcelWeaver(world);

		long startTime = System.currentTimeMillis();
		// ensure that a fast cpu doesn't complete file write within 1000ms of start
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		weaver.addJarFile(inFile, new File("."), false);

		if (aspectjar != null) {
			if (isInJar) {
				weaver.addJarFile(new File(aspectjar), new File("."), false);
			} else {
				weaver.addLibraryJarFile(new File(aspectjar));
				world.addPath(new File(aspectjar).toString());
			}
		}
		weaver.addLibraryJarFile(new File(WeaverTestCase.TESTDATA_PATH + "/Regex.jar")); // ???
		world.addPath(new File(WeaverTestCase.TESTDATA_PATH + "/Regex.jar").getPath());

		Collection<String> woven = weaver.weave(outFile);
		long stopTime = System.currentTimeMillis();

		System.out.println("handled " + woven.size() + " entries, in " + (stopTime - startTime) / 1000. + " seconds");
		// last mod times on linux (at least) are only accurate to the second.
		// with fast disks and a fast cpu the following test can fail if the write completes less than
		// 1000 milliseconds after the start of the test, hence the 1000ms delay added above.
		assertTrue(outFile.lastModified() > startTime);
	}

	public void testSmall() throws IOException {
		zipTest(WeaverTestCase.TESTDATA_PATH + "/Regex.jar", null);
	}

	public void testSmallWithAspects() throws IOException {
		System.out.println("could take 4 seconds...");
		zipTest(WeaverTestCase.TESTDATA_PATH + "/Regex.jar", WeaverTestCase.TESTDATA_PATH + "/megatrace.jar");
	}

	public void testSmallWithAspectsNoWeave() throws IOException {
		System.out.println("could take 4 seconds...");
		zipTest(WeaverTestCase.TESTDATA_PATH + "/Regex.jar", WeaverTestCase.TESTDATA_PATH + "/megatraceNoweave.jar", true);
	}

	public void testBig() throws IOException {
		System.out.println("could take 4 seconds...");
		zipTest("../lib/bcel/bcel.jar", null);
	}

	public void testBigWithEasyNoTrace() throws IOException {
		System.out.println("could take 4 seconds...");
		zipTest("../lib/bcel/bcel.jar", WeaverTestCase.TESTDATA_PATH + "/megatrace0easy.jar");
	}

	// this is something we test every now and again.
	public void xtestBigWithHardNoTrace() throws IOException {
		System.out.println("could take 24 seconds...");
		zipTest("../lib/bcel/bcel.jar", WeaverTestCase.TESTDATA_PATH + "/megatrace0hard.jar");
	}

	public void xtestBigWithAspects() throws IOException {
		System.out.println("could take 40 seconds...");
		zipTest("../lib/bcel/bcel.jar", WeaverTestCase.TESTDATA_PATH + "/megatrace.jar");
	}

}
