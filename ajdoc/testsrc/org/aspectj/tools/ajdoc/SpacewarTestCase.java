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
public class SpacewarTestCase extends TestCase {
	
	public void testSimpleExample() {
		
//		System.err.println(new File("testdata/figures-demo").exists());
		File outdir = new File("testdata/spacewar/doc");
		File sourcepath = new File("testdata/spacewar");
		
		String[] args = { "-d", 
				outdir.getAbsolutePath(),
				"-sourcepath",
				sourcepath.getAbsolutePath(),
				"spacewar",
				"coordination" };
		
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
