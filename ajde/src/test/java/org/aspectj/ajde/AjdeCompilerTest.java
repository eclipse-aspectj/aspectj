/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde;

import org.aspectj.ajde.core.AjCompiler;

/**
 * Tests ajde's management of the AjCompiler instances. Expect
 * there to be a different one for each .lst file and for ajde
 * to only remember the compiler for the last .lst file.
 */
public class AjdeCompilerTest extends AjdeTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("LstBuildConfigManagerTest");
	}
	
	// Expect to get a different compiler instance for each
	// different config file
	public void testGetSameAjCompilerForSameConfigFiles() {
		AjCompiler c1 = getCompilerForConfigFileWithName("bad-injar.lst");
		AjCompiler c2 = getCompilerForConfigFileWithName("bad-injar.lst");
		assertEquals("expected the same AjCompiler instance to be returned" +
				" for the same configFile but found different ones", c1, c2);
	}
	
	// Expect to get a different compiler instance for each
	// different config file
	public void testGetDifferentAjCompilerForDifferentConfigFiles() {
		AjCompiler c1 = getCompilerForConfigFileWithName("bad-injar.lst");
		AjCompiler c2 = getCompilerForConfigFileWithName("dir-entry.lst");
		assertNotSame("expected different AjCompiler instances to be returned" +
				" for different configFiles but found the smae", c1, c2);
	}
	
	// want to keep the same setting regardless of the configFile
	// being built - therefore the same instance should be passed
	// from one AjCompiler instance to the next
	public void testSameCompilerConfigForDifferentConfigFiles() { 
		AjCompiler c1 = getCompilerForConfigFileWithName("bad-injar.lst");
		AjCompiler c2 = getCompilerForConfigFileWithName("dir-entry.lst");
		assertEquals("expected the same compilerConfig instance to be associated" +
				" with the different AjCompiler's however found different ones", 
				c1.getCompilerConfiguration(), c2.getCompilerConfiguration());
	}
	
	// want to have a different messageHandler instance for the different
	// config files - or we can just reset?!?! Resetting would be easier
	public void testSameMessageHandlerForDifferentConfigFiles() {
		AjCompiler c1 = getCompilerForConfigFileWithName("bad-injar.lst");
		AjCompiler c2 = getCompilerForConfigFileWithName("dir-entry.lst");
		assertEquals("expected the same messageHandler instance to be associated" +
				" with the different AjCompiler's however found different ones", 
				c1.getMessageHandler(), c2.getMessageHandler());
	}
	
	// can have the same buildProgressMonitor for the different configFiles
	// because it holds no state
	public void testSameBuildProgressMonitorForDifferentConfigFiles() {
		AjCompiler c1 = getCompilerForConfigFileWithName("bad-injar.lst");
		AjCompiler c2 = getCompilerForConfigFileWithName("dir-entry.lst");
		assertEquals("expected the same buildProgressMonitor instance to be associated" +
				" with the different AjCompiler's however found different ones", 
				c1.getBuildProgressMonitor(), c2.getBuildProgressMonitor());
	}
	
}
