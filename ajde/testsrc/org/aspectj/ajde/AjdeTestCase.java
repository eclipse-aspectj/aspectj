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


package org.aspectj.ajde;

import java.io.*;
import junit.framework.TestCase;

public class AjdeTestCase extends TestCase {

	private static final String TEST_DATA_PATH = "testdata";
	//private static final String TEST_DATA_PATH = "C:/Dev/aspectj/modules/ajde/testdata";

	protected NullIdeManager ideManager = new NullIdeManager();
	protected TestBuildListener testerBuildListener = new TestBuildListener();
	protected String currTestDataPath;

	public AjdeTestCase(String name) {
		super(name);
	}

	protected File createFile(String path) {
		return new File(currTestDataPath + File.separatorChar + path);
	}

	/**
	 * Waits on the build complete.
	 */
	protected void doSynchronousBuild(String configFilePath) {
		testerBuildListener.reset();
		File configFile = createFile(configFilePath);
		Ajde.getDefault().getBuildManager().build(configFile.getAbsolutePath());
		while(!testerBuildListener.getBuildFinished()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException ie) { } 
		}	
	}	

	protected void setUp(String testDataPath) throws Exception {
		currTestDataPath = TEST_DATA_PATH + File.separatorChar + testDataPath;
		ideManager.init(currTestDataPath);
		super.setUp();
		Ajde.getDefault().getBuildManager().addListener(testerBuildListener);
	}
    
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    protected String genStructureModelExternFilePath(String configFilePath) {
        return configFilePath.substring(0, configFilePath.lastIndexOf(".lst")) + ".ajsym";
    }

}
