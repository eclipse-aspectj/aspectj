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

import org.aspectj.bridge.Version;

/**
 * @author Mik Kersten
 */
public class ExecutionTestCase extends TestCase {
	
	public void testVersionMatch() {
		String ajdocVersion = Main.getVersion();
		String compilerVersion = Version.text;
		assertTrue("version check", ajdocVersion.endsWith(compilerVersion));
	}
	
	public void testFailingBuild() {
		File file1 = new File("testdata/failing-build/Fail.java");
		String[] args = { file1.getAbsolutePath() };
		
		org.aspectj.tools.ajdoc.Main.main(args);
		assertTrue(Main.hasAborted());
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
