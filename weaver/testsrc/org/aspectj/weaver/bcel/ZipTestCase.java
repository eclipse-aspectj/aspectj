/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.*;
import java.util.Collection;

import junit.framework.TestCase;

public class ZipTestCase extends TestCase {

	/**
	 * Constructor for ZipTestCase.
	 * @param arg0
	 */
	public ZipTestCase(String arg0) {
		super(arg0);
	}
	
	
	public void zipTest(String fileName, String aspectjar) throws IOException {
		zipTest(fileName, aspectjar, false);
	}
	
	public void zipTest(String fileName, String aspectjar, boolean isInJar) throws IOException {
		File inFile = new File(fileName);
		File outFile = new File("out", inFile.getName());
		
		
		BcelWorld world = new BcelWorld();
		//BcelWeaver weaver1 = new BcelWeaver(world);
		BcelWeaver weaver = new BcelWeaver(world);
		
		long startTime = System.currentTimeMillis();
		weaver.addJarFile(inFile, new File("."));
		if (aspectjar != null) {
			if (isInJar) {
				weaver.addJarFile(new File(aspectjar), new File("."));
			} else {
				weaver.addLibraryJarFile(new File(aspectjar));
			}
		}
		
		
		Collection woven = weaver.weave(outFile);
		long stopTime = System.currentTimeMillis();
		
		
		System.out.println("handled " + woven.size() + " entries, in " + 
				(stopTime-startTime)/1000. + " seconds");
		assertTrue(outFile.lastModified() > startTime);
	}	
	
	public void testSmall() throws IOException {
		zipTest("testdata/Regex.jar", null);
	}

	public void testSmallWithAspects() throws IOException {
		System.out.println("could take 4 seconds...");
		zipTest("testdata/Regex.jar", "testdata/megatrace.jar");
	}

	public void testSmallWithAspectsNoWeave() throws IOException {
		System.out.println("could take 4 seconds...");
		zipTest("testdata/Regex.jar", "testdata/megatraceNoweave.jar", true);
	}

	// this is something we test every now and again.
	public void testBig() throws IOException {
		System.out.println("could take 4 seconds...");
		zipTest("../lib/bcel/bcel.jar", null);
	}

	public void xtestBigWithAspects() throws IOException {
		System.out.println("could take 40 seconds...");
		zipTest("../lib/bcel/bcel.jar", "testdata/megatrace.jar");
	}

}
