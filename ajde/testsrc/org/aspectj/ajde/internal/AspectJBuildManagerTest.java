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


package org.aspectj.ajde.internal;

import junit.framework.*;
import org.aspectj.ajde.*;
import java.io.*;

/**
 * @author Mik Kersten
 */
public class AspectJBuildManagerTest extends AjdeTestCase {

	public AspectJBuildManagerTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(AspectJBuildManagerTest.class);	
		return result;
	}

	public void testInit() {
		assertTrue("initialization", ideManager != null);
	}
	
	public void testCompileNonExistingConfig() {
		assertTrue("compile of non-existing build config success", !testerBuildListener.getBuildSucceeded());	
	}
	
	public void testCompileEmptyConfig() {
		doSynchronousBuild("empty.lst");
		assertTrue("compile of empty build config success", testerBuildListener.getBuildSucceeded());	
	}  
	
	public void testCompileSuccess() {
		doSynchronousBuild("../examples/figures-coverage/all.lst");
		assertTrue("compile success", testerBuildListener.getBuildSucceeded());
	}

	public void testCompileOutput() {
		File classFile = new File(Ajde.getDefault().getProjectProperties().getOutputPath() + "/figures/Main.class");
		if (classFile != null) {  
			assertTrue("class file generated", classFile.exists());
			classFile.delete();
		} else {
			assertTrue("class file generated", false);
		}			
	}

	public void testSymbolFileGenerated() {
		File symFile = createFile("../examples/figures-coverage/all.ajsym");
		if (symFile != null) {
			assertTrue("sym file generated", symFile.exists());
			symFile.delete();
		} else {
			assertTrue("sym file generated", false);
		}	
	}

	protected void setUp() throws Exception {
		super.setUp("AspectJBuildManagerTest"); 
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
