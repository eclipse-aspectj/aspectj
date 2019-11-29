/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.incremental.model;

import org.aspectj.asm.AsmManager;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.util.StructureModelUtil;

import junit.framework.Test;

public class IncrementalModelTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(IncrementalModelTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("incremental-model.xml");
	}

	// This first test doesnt do a lot currently, but is ready too...
	public void test001() throws Exception {
		runTest("Testing incremental structure model: Intertype declarations (and a declare parents)");
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/CloneablePoint.20.java", "src/introduction/CloneablePoint.java");
		nextIncrement(true);
		copyFile("changes/Point.30.java", "src/introduction/Point.java");
		copyFileAndDoIncrementalBuild("changes/HashablePoint.30.java", "src/introduction/HashablePoint.java");
		StructureModelUtil.checkModel("declare parents=2");
	}

	public void test002() throws Exception {
		runTest("Testing incremental structure model: Intertype field declarations");

		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/BetaA.20.java", "src/secondary/BetaA.java");
		StructureModelUtil.checkModel("inter-type field=2,RelationshipMapSize=3");

		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/BetaA.30.java", "src/secondary/BetaA.java");
		// TODO Andy - fix this test, what should the real results be in the model?
		// when we go slow it seems to be relmapsize=0
		// StructureModelUtil.checkModel("inter-type field=1,RelationshipMapSize=2");
	}

	public void test003() throws Exception {
		runTest("Testing incremental structure model: Weaving handlers");

		// <!-- BetaA has a new piece of handler advice added -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/primary/BetaA.20.java", "src/primary/BetaA.java");
		// if (AsmHierarchyBuilder.shouldAddUsesPointcut) {
		// StructureModelUtil.checkModel("code=1,advice=1,RelationshipMapSize=3");
		// } else {
		StructureModelUtil.checkModel("code=1,advice=1,RelationshipMapSize=2");
		// }

		// <!-- secondary.GammaA added, also advises the same handler -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/GammaA.30.java", "src/secondary/GammaA.java");
		// if (AsmHierarchyBuilder.shouldAddUsesPointcut) {
		// StructureModelUtil.checkModel("code=1,advice=2,RelationshipMapSize=5");
		// } else {
		StructureModelUtil.checkModel("code=1,advice=2,RelationshipMapSize=3");
		// }

		// <!-- primary.BetaA deleted -->
		nextIncrement(true);
		deleteFileAndDoIncrementalBuild("src/primary/BetaA.java");
		// if (AsmHierarchyBuilder.shouldAddUsesPointcut) {
		// StructureModelUtil.checkModel("code=1,advice=1,RelationshipMapSize=3");
		// } else {
		StructureModelUtil.checkModel("code=1,advice=1,RelationshipMapSize=2");
		// }

	}

	public void test004() throws Exception {
		runTest("Testing incremental structure model: Weaving");

		// <!-- BetaA has a new piece of advice added -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/primary/BetaA.20.java", "src/primary/BetaA.java");
		// if (AsmHierarchyBuilder.shouldAddUsesPointcut) {
		// StructureModelUtil.checkModel("code=2,advice=2,java source file=3,RelationshipMapSize=6");
		// } else {
		StructureModelUtil.checkModel("code=2,advice=2,java source file=3,RelationshipMapSize=4");
		// }

		// <!-- BetaA has a piece of advice removed -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/primary/BetaA.30.java", "src/primary/BetaA.java");
		// if (AsmHierarchyBuilder.shouldAddUsesPointcut) {
		// StructureModelUtil.checkModel("code=1,advice=1,RelationshipMapSize=3");
		// } else {
		StructureModelUtil.checkModel("code=1,advice=1,RelationshipMapSize=2");
		// }

		// <!-- BetaA other piece of advice removed (now empty) -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/primary/BetaA.40.java", "src/primary/BetaA.java");
		StructureModelUtil.checkModel("code=0,RelationshipMapSize=0,advice=0");
	}

	public void test005() throws Exception {
		runTest("Testing incremental structure model: Updating files");

		// <!-- Beta changed, method added -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/primary/Beta.20.java", "src/primary/Beta.java");
		StructureModelUtil.checkModel("java source file=5,method=4,class=3,FileMapSize=4");

		// <!-- Delta changed, class added -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/Delta.30.java", "src/secondary/Delta.java");
		StructureModelUtil.checkModel("java source file=5,method=4,class=4,advice=1");

		// <!-- Gamma changed, advice added -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/Gamma.40.java", "src/secondary/Gamma.java");
		StructureModelUtil.checkModel("advice=2");

		// <!-- Gamma changed, pointcut added -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/Gamma.50.java", "src/secondary/Gamma.java");
		StructureModelUtil.checkModel("advice=2,pointcut=1");

		// <!-- Gamma changed, both advice removed -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/Gamma.60.java", "src/secondary/Gamma.java");
		StructureModelUtil.checkModel("advice=0,pointcut=1");

	}

	public void test006() throws Exception {
		runTest("Testing incremental structure model: Adding and removing files");

		// <!-- Adds primary.Beta class -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/primary/Beta.20.java", "src/primary/Beta.java");
		StructureModelUtil.checkModel("java source file=3,FileMapSize=2");

		// <!-- Adds secondary.Gamma aspect -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/Gamma.30.java", "src/secondary/Gamma.java");
		StructureModelUtil.checkModel("java source file=4");

		// <!-- Adds secondary.Delta class -->
		nextIncrement(true);
		copyFileAndDoIncrementalBuild("changes/secondary/Delta.40.java", "src/secondary/Delta.java");
		StructureModelUtil.checkModel("java source file=5,package=2,FileMapSize=4");

		// <!-- Deletes Gamma aspect -->
		nextIncrement(true);
		deleteFileAndDoIncrementalBuild("src/secondary/Gamma.java");
		StructureModelUtil.checkModel("java source file=4,package=2");

		// <!-- Deletes Beta and Delta classes -->
		nextIncrement(true);
		deleteFile("src/primary/Beta.java");
		deleteFileAndDoIncrementalBuild("src/secondary/Delta.java");
		StructureModelUtil.checkModel("java source file=2,FileMapSize=1");
	}

	protected void setUp() throws Exception {
		super.setUp();
		AsmManager.attemptIncrementalModelRepairs = true;
	}

	protected void tearDown() throws Exception {
		// To see the model after a test, uncomment these lines...
		// AsmManager.ModelInfo mi = AsmManager.ModelInfo.summarizeModel();
		// System.err.println(mi.toString());
		super.tearDown();

		AsmManager.attemptIncrementalModelRepairs = false;
	}
}
