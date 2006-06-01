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


package org.aspectj.ajde;

import java.io.*;

import org.aspectj.asm.AsmManager;

import junit.framework.TestCase;

public class AjdeTestCase extends TestCase {

//	private static final String TEST_DATA_PATH = AjdeTests.testDataPath(null);

	protected NullIdeManager ideManager = NullIdeManager.getIdeManager();
	protected TestBuildListener testerBuildListener = new TestBuildListener();
	protected String currTestDataPath;

	public AjdeTestCase() {
		super("");
	}
	
	public AjdeTestCase(String name) {
		super(name);
	}
    public void testEmptyAddedForAntJUnit() {}

	protected File openFile(String path) {
		return new File(currTestDataPath + File.separatorChar + path);
	}

	/**
	 * Waits on the build complete.
	 */
	protected boolean doSynchronousBuild(String configFilePath) {
		testerBuildListener.reset();
		File configFile = openFile(configFilePath);
		Ajde.getDefault().getBuildManager().build(configFile.getAbsolutePath());
		while(!testerBuildListener.getBuildFinished()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException ie) { } 
		}	
		return testerBuildListener.getBuildSucceeded();
	}	

	protected void setUp(String testDataPath) throws Exception {
		currTestDataPath = AjdeTests.testDataPath(testDataPath);
		// AMC - added this next line as a temporary workaround for 
		// listener leakage in AsmManager induced by the Ajde test suite.
		AsmManager.getDefault().removeAllListeners();
		ideManager.init(currTestDataPath);
		super.setUp();
		assertTrue(NullIdeManager.getIdeManager().isInitialized()); 
		Ajde.getDefault().getBuildManager().addListener(testerBuildListener);
	}
    
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    protected String genStructureModelExternFilePath(String configFilePath) {
        return configFilePath.substring(0, configFilePath.lastIndexOf(".lst")) + ".ajsym";
    }

}
