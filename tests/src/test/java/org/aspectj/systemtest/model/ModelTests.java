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

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.World;

import junit.framework.Test;

/**
 * Tests the model when there is no requirement on Java5 features.
 * 
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

	public void testAdviceInStructureModelWithAnonymousInnerClass_pr77269() {
		runModelTest("advice in structure model with anonymous inner class", "pr77269_1");
	}

	public void testAdviceInStructureModelWithNamedInnerClass_pr77269() {
		runModelTest("advice in structure model with named inner class", "pr77269_2");
	}

	public void testDWInStructureModelWithAnonymousInnerClass_pr77269() {
		runModelTest("declare warning in structure model with anonymous inner class", "pr77269_3");
	}

	public void testNewIProgramElementMethods_pr141730() {
		runModelTest("new iprogramelement methods", "pr141730_1");
	}

	// if not filling in the model for aspects contained in jar files then
	// want to ensure that the relationship map is correct and has nodes
	// which can be used in AJDT - ensure no NPE occurs for the end of
	// the relationship with aspectpath
	public void testAspectPathRelWhenNotFillingInModel_pr141730() {
		World.createInjarHierarchy = false;
		try {
			runModelTest("ensure aspectpath injar relationships are correct when not filling in model", "pr141730_3");
		} finally {
			World.createInjarHierarchy = true;
		}
	}

	//
	// public void testPCDInClassAppearsInModel_pr148027() {
	// boolean b = AsmHierarchyBuilder.shouldAddUsesPointcut;
	// AsmHierarchyBuilder.shouldAddUsesPointcut = true;
	// World.createInjarHierarchy = false;
	// try {
	// runModelTest("ensure pcd declare in class appears in model", "pr148027");
	// } finally {
	// World.createInjarHierarchy = true;
	// AsmHierarchyBuilder.shouldAddUsesPointcut = b;
	// }
	// }

	// public void testInpathAdvisedCode_prX() { runModelTest("inpath advised elements","prX"); }

	public void testSourceLocationAndJarFile_pr145963() {
		runModelTest("sourcelocation and jar file", "pr145963_1");
	}

	public void testSourceLocationAndClassFile_pr145963() {
		runModelTest("sourcelocation and class file", "pr145963_2");
	}

	public void testAspectInDefaultPackage_pr145963() {
		runModelTest("aspect in default package", "pr145963_3");
	}

	public void testAspectInJavaFile_pr145963() {
		runModelTest("aspect in java file", "pr145963_4");
	}

	public void testAbstractAspectsAndAdvice_pr160469() {
		runModelTest("ensure advice from abstract aspects appear correctly in the model", "pr160469_1");
	}

	public void testAbstractAspectsAndDeow_pr160469() {
		runModelTest("ensure deow from abstract aspects appear correctly in the model", "pr160469_2");
	}

	// public void testMultipleIdenticalJpsOnOneLine_pr238054() { runModelTest("multiple identical jps on one line","pr238054");}

	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(ModelTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("model.xml");
	}

}
