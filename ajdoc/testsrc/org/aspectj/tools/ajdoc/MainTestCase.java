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
 * @author Mik Kersten
 */
public class MainTestCase extends TestCase {
	
	public void testSimpleExample() {
		
//		System.err.println(new File("testdata/figures-demo").exists());
		File file1 = new File("testdata/simple/foo/ClassA.java");
		File file2 = new File("testdata/simple/foo/InterfaceI.java");
		File outdir = new File("testdata/simple/doc");
		
		String[] args = { "-d", outdir.getAbsolutePath(), file1.getAbsolutePath(), file2.getAbsolutePath() };
		org.aspectj.tools.ajdoc.Main.main(args);
		
		assertTrue(true);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
