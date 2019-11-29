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
 *     Helen Hawkins  Converted to new interface (bug 148190) 
 * ******************************************************************/


package org.aspectj.ajde.internal;

import java.io.File;

import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.asm.AsmManager;

import junit.framework.TestSuite;

/**
 * @author Mik Kersten
 */
public class AspectJBuildManagerTest extends AjdeTestCase {

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(AspectJBuildManagerTest.class);	
		return result;
	}

	public void testSequence() {
		AsmManager.dumpModelPostBuild=true; // or you wont get a .ajsym file
		try {
        // XXX should fail? empty configs fail b/c no sources specified
		initialiseProject("AspectJBuildManagerTest");
        doBuild("empty.lst");
		assertTrue("Expected there to be no error messages from the build but found that" +
				" there were some " + getErrorMessages("empty.lst"),getErrorMessages("empty.lst").isEmpty());
        // TODO-path
		initialiseProject("figures-coverage");
		doBuild("all.lst");
		assertTrue("Expected there to be no error messages from the build but found that" +
				" there were some " + getErrorMessages("empty.lst"),getErrorMessages("empty.lst").isEmpty());
		File file = new File(getCompilerForConfigFileWithName("all.lst").getCompilerConfiguration().getOutputLocationManager().getDefaultOutputLocation() + "/figures/Main.class");
        if (file.exists()) {
            file.delete();
        } else {
            assertTrue("expected class " + file, false);
		}			
        
        // TODO-path
		file = openFile("all.ajsym");
        if (file.exists()) {
            file.delete();
        } else {
            assertTrue("expected .ajsym: " + file, false);
        }    
		} finally {
			AsmManager.dumpModelPostBuild=false;
		}
	}
}
