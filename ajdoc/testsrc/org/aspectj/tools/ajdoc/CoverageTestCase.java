/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 package org.aspectj.tools.ajdoc;

import java.io.File;

import junit.framework.TestCase;

/**
 * A long way to go until full coverage, but this is the place to add more.
 * 
 * @author Mik Kersten
 */
public class CoverageTestCase extends TestCase {
	
	public void testCoverage() {
		  
//		System.err.println(new File("testdata/figures-demo").exists());
		File file1 = new File("testdata/coverage/foo/ClassA.java");
		File aspect1 = new File("testdata/coverage/foo/UseThisAspectForLinkCheck.java");
		File file2 = new File("testdata/coverage/foo/InterfaceI.java");
		File file3 = new File("testdata/coverage/foo/PlainJava.java");
		File file4 = new File("testdata/coverage/foo/ModelCoverage.java");
		File file5 = new File("testdata/coverage/fluffy/Fluffy.java");
		File file6 = new File("testdata/coverage/fluffy/bunny/Bunny.java");
		File file7 = new File("testdata/coverage/fluffy/bunny/rocks/Rocks.java");
		File file8 = new File("testdata/coverage/fluffy/bunny/rocks/UseThisAspectForLinkCheckToo.java");
		File outdir = new File("testdata/coverage/doc");
		
		String[] args = { 
//			"-XajdocDebug",
			"-source", 
			"1.4",
			"-private",
			"-d", 
			outdir.getAbsolutePath(),
			aspect1.getAbsolutePath(),
			file1.getAbsolutePath(), 
			file2.getAbsolutePath(),
			file3.getAbsolutePath(),
			file4.getAbsolutePath(),
			file5.getAbsolutePath(),
			file6.getAbsolutePath(),
			file7.getAbsolutePath(),
			file8.getAbsolutePath()
		};
		
		org.aspectj.tools.ajdoc.Main.main(args);
	}

//	public void testPlainJava() {
//		File file1 = new File("testdata/coverage/foo/PlainJava.java");
//		File outdir = new File("testdata/coverage/doc");
//		
//		String[] args = { "-d", 
//				outdir.getAbsolutePath(),
//				file1.getAbsolutePath() };
//		
//		org.aspectj.tools.ajdoc.Main.main(args);
//	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
