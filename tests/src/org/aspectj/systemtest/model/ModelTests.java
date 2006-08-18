/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.systemtest.model;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * Tests the model when there is no requirement on Java5 features.
 * @see org.aspectj.systemtest.model.ModelTestCase 
 */
public class ModelTests extends ModelTestCase {
	
	static {
		// Switch this to true for a single iteration if you want to reconstruct the
		// 'expected model' files.
		regenerate = false;
		// Switch this to true if you want to debug the comparison
		debugTest = false;
	}
	
	public void testSourceLocationAndJarFile_pr145963() {runModelTest("sourcelocation and jar file","pr145963_1");}
	public void testSourceLocationAndClassFile_pr145963() {runModelTest("sourcelocation and class file","pr145963_2");}
	public void testAspectInDefaultPackage_pr145963() {runModelTest("aspect in default package", "pr145963_3");}
	public void testAspectInJavaFile_pr145963() {runModelTest("aspect in java file", "pr145963_4");}
	
	/////////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(ModelTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/model/model.xml");
	}

}
