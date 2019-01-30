/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 *    Helen Hawkins    Converted to new interface (bug 148190)
 * ******************************************************************/
package org.aspectj.ajde.core.tests.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;

public class SavedModelConsistencyTest extends AjdeCoreTestCase {

	private final String[] files = new String[] { "ModelCoverage.java", "pkg" + File.separator + "InPackage.java" };

	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("coverage");
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		// In order to get a model on the disk to read in, do a build with the right flag set !
		try {
			AsmManager.dumpModelPostBuild = true;
			doBuild();
		} finally {
			AsmManager.dumpModelPostBuild = false;
		}
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}

	public void testInterfaceIsSameInBoth() {
		AsmManager asm = AsmManager.createNewStructureModel(Collections.<File,String>emptyMap());
		asm.readStructureModel(getAbsoluteProjectDir());

		IHierarchy model = asm.getHierarchy();
		assertTrue("model exists", model != null);

		assertTrue("root exists", model.getRoot() != null); // TODO-path
		File testFile = openFile("ModelCoverage.java");
		assertTrue("Expected " + testFile.getAbsolutePath() + " to exist, but it did not", testFile.exists());

		IProgramElement nodePreBuild = model.findElementForSourceLine(testFile.getAbsolutePath(), 5);

		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());

		IProgramElement nodePostBuild = model.findElementForSourceLine(testFile.getAbsolutePath(), 5);

		assertTrue(
				"Nodes should be identical: Prebuild kind = " + nodePreBuild.getKind() + "   Postbuild kind = "
						+ nodePostBuild.getKind(), nodePreBuild.getKind().equals(nodePostBuild.getKind()));

	}

	public void testModelIsSamePreAndPostBuild() {
		AsmManager asm = AsmManager.createNewStructureModel(Collections.<File,String>emptyMap());
		asm.readStructureModel(getAbsoluteProjectDir());
		// AsmManager.getDefault().readStructureModel(getAbsoluteProjectDir());
		IHierarchy model = asm.getHierarchy();
		assertTrue("model exists", model != null);

		final List<IProgramElement.Kind> preBuildKinds = new ArrayList<>();
		HierarchyWalker walker = new HierarchyWalker() {
			@Override
			public void preProcess(IProgramElement node) {
				preBuildKinds.add(node.getKind());
			}
		};
		asm.getHierarchy().getRoot().walk(walker);
		assertFalse("Expected there to be build kinds but didn't " + "find any", preBuildKinds.isEmpty());

		doBuild();
		assertTrue("Expected no compiler errors but found " + handler.getErrors(), handler.getErrors().isEmpty());

		final List<IProgramElement.Kind> postBuildKinds = new ArrayList<>();
		HierarchyWalker walker2 = new HierarchyWalker() {
			@Override
			public void preProcess(IProgramElement node) {
				postBuildKinds.add(node.getKind());
			}
		};
		asm.getHierarchy().getRoot().walk(walker2);
		assertFalse("Expected there to be build kinds but didn't " + "find any", preBuildKinds.isEmpty());

		assertTrue("Lists should be the same: PRE" + preBuildKinds.toString() + "  POST" + postBuildKinds.toString(),
				preBuildKinds.equals(postBuildKinds));

	}

}
