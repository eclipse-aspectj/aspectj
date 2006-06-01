/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde.internal;

import junit.framework.*;
import org.aspectj.ajde.*;
import org.aspectj.asm.AsmManager;

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

	public void testSequence() {
		AsmManager.dumpModelPostBuild=true; // or you wont get a .ajsym file
		try {
		assertTrue("initialization", ideManager != null);
        assertTrue("compile of non-existing build config success", !testerBuildListener.getBuildSucceeded());   
        // XXX should fail? empty configs fail b/c no sources specified
        doSynchronousBuild("empty.lst");
        assertTrue("compile of empty build config", testerBuildListener.getBuildSucceeded());   
        // TODO-path
        doSynchronousBuild("../examples/figures-coverage/all.lst");
        assertTrue("compile success", testerBuildListener.getBuildSucceeded());
		File file = new File(Ajde.getDefault().getProjectProperties().getOutputPath() + "/figures/Main.class");
        if (file.exists()) {
            file.delete();
        } else {
            assertTrue("expected class " + file, false);
		}			
        
        // TODO-path
		file = openFile("../examples/figures-coverage/all.ajsym");
        if (file.exists()) {
            file.delete();
        } else {
            assertTrue("expected .ajsym" + file, false);
        }    
		} finally {
			AsmManager.dumpModelPostBuild=false;
		}
	}

	protected void setUp() throws Exception {
		super.setUp("AspectJBuildManagerTest"); 
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
