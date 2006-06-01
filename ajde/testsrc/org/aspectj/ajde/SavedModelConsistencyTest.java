

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
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;

/**
 * @author Mik Kersten
 */
public class SavedModelConsistencyTest extends AjdeTestCase {
	
    // TODO-path
	private final String CONFIG_FILE_PATH = "../examples/coverage/coverage.lst";

	public SavedModelConsistencyTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(SavedModelConsistencyTest.class);
	}
	
	public void testInterfaceIsSameInBoth() {
		File configFile = openFile(CONFIG_FILE_PATH);	
		Ajde.getDefault().getStructureModelManager().readStructureModel(configFile.getAbsolutePath());
		
        IHierarchy model = Ajde.getDefault().getStructureModelManager().getHierarchy();
        assertTrue("model exists", model != null);
        
		assertTrue("root exists", model.getRoot() != null);
        // TODO-path
		File testFile = openFile("../examples/coverage/ModelCoverage.java");
		assertTrue(testFile.exists());
		
		IProgramElement nodePreBuild = model.findElementForSourceLine(testFile.getAbsolutePath(), 5);	
		
		doSynchronousBuild(CONFIG_FILE_PATH);	
		
		IProgramElement nodePostBuild = model.findElementForSourceLine(testFile.getAbsolutePath(), 5);	
		
		assertTrue("Nodes should be identical: Prebuild kind = "+nodePreBuild.getKind()+
				   "   Postbuild kind = "+nodePostBuild.getKind(),nodePreBuild.getKind().equals(nodePostBuild.getKind()));
		
	}

	public void testModelIsSamePreAndPostBuild() {
		File configFile = openFile(CONFIG_FILE_PATH);	
		Ajde.getDefault().getStructureModelManager().readStructureModel(configFile.getAbsolutePath());
		
        IHierarchy model = Ajde.getDefault().getStructureModelManager().getHierarchy();
        assertTrue("model exists", model != null);
	
        final List preBuildKinds = new ArrayList();
		HierarchyWalker walker = new HierarchyWalker() {
  		    public void preProcess(IProgramElement node) {
  		    	preBuildKinds.add(node.getKind());
  		    }
  		};
  		Ajde.getDefault().getStructureModelManager().getHierarchy().getRoot().walk(walker);

		doSynchronousBuild(CONFIG_FILE_PATH);
		
        final List postBuildKinds = new ArrayList();
		HierarchyWalker walker2 = new HierarchyWalker() {
  		    public void preProcess(IProgramElement node) {
  		    	postBuildKinds.add(node.getKind());
  		    }
  		};
  		Ajde.getDefault().getStructureModelManager().getHierarchy().getRoot().walk(walker2);
			
//		System.err.println(preBuildKinds);
//		System.err.println(postBuildKinds);
		
		assertTrue("Lists should be the same: PRE"+preBuildKinds.toString()+"  POST"+postBuildKinds.toString(),preBuildKinds.equals(postBuildKinds));
		
	}
	  
	protected void setUp() throws Exception {
		super.setUp("examples");
		// In order to get a model on the disk to read in, do a build with the right flag set !
		try {
			AsmManager.dumpModelPostBuild=true;
			doSynchronousBuild(CONFIG_FILE_PATH);
		} finally {
			AsmManager.dumpModelPostBuild=false;
		}

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

