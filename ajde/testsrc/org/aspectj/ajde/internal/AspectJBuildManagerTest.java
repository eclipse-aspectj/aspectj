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
        // XXX should fail? empty configs fail b/c no sources specified
		doSynchronousBuild("empty.lst");
		assertTrue("compile of empty build config", testerBuildListener.getBuildSucceeded());	
	}  
	
	public void testCompileSuccess() {
		doSynchronousBuild("../examples/figures-coverage/all.lst");
		assertTrue("compile success", testerBuildListener.getBuildSucceeded());
	}

	public void testCompileOutput() {
		File file = new File(Ajde.getDefault().getProjectProperties().getOutputPath() + "/figures/Main.class");
        if (file.exists()) {
            file.delete();
        } else {
            assertTrue("expected class " + file, false);
		}			
	}

	public void testSymbolFileGenerated() {
		File file = createFile("../examples/figures-coverage/all.ajsym");
        if (file.exists()) {
            file.delete();
        } else {
            assertTrue("expected .ajsym" + file, false);
        }           
	}

	protected void setUp() throws Exception {
		super.setUp("AspectJBuildManagerTest"); 
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
