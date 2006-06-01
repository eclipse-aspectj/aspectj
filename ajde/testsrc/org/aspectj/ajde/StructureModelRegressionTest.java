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

import java.io.File;
import java.util.List;

import junit.framework.TestSuite;

import org.aspectj.asm.*;

public class StructureModelRegressionTest extends AjdeTestCase {

	public StructureModelRegressionTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(StructureModelRegressionTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureModelRegressionTest.class);	
		return result;
	}

	public void test() {
		String testLstFile = AjdeTests.testDataPath("StructureModelRegressionTest/example.lst");
        File f = new File(testLstFile);
        assertTrue(testLstFile, f.canRead());
        // TODO: enable when model is verified.
//        assertTrue("saved model: " + testLstFile, verifyAgainstSavedModel(testLstFile));    
	}

	public boolean verifyAgainstSavedModel(String lstFile) {
		File modelFile = new File(genStructureModelExternFilePath(lstFile));
		IHierarchy model = getModelForFile(lstFile);
		
		if (modelFile.exists()) {
			Ajde.getDefault().getStructureModelManager().readStructureModel(lstFile);
			IHierarchy savedModel = Ajde.getDefault().getStructureModelManager().getHierarchy();
			// AMC This test will not pass as written until IProgramElement defines
			// equals. The equals loic is commented out in the IProgramElement
			// class - adding it back in could have unforeseen system-wide
			// consequences, so I've defined a IProgramElementsEqual( ) helper
			// method here instead.
			IProgramElement rootNode = model.getRoot();
			IProgramElement savedRootNode = savedModel.getRoot();
			return IProgramElementsEqual( rootNode, savedRootNode );
		} else {
			Ajde.getDefault().getStructureModelManager().writeStructureModel(lstFile);
			return true;
		}
		//return true;
	}

	private boolean IProgramElementsEqual( IProgramElement s1, IProgramElement s2 ) {
	  final boolean equal = true;
	  	if ( s1 == s2 ) return equal;
	  	if ( null == s1 || null == s2 ) return !equal;

		if (!s1.getName( ).equals(s2.getName())) return !equal;
		if (!s1.getKind( ).equals(s2.getKind())) return !equal;
		
		// check child nodes
		List s1Kids = s1.getChildren();
		List s2Kids = s2.getChildren();
		
		if ( s1Kids != null && s2Kids != null ) {
			if (s1Kids == null || s2Kids == null) return !equal;			
			if (s1Kids.size() != s2Kids.size() ) return !equal;
			for ( int k=0; k<s1Kids.size(); k++ ) {
				IProgramElement k1 = (IProgramElement) s1Kids.get(k);
				IProgramElement k2 = (IProgramElement) s2Kids.get(k);	
				if (!IProgramElementsEqual( k1, k2 )) return !equal;
			}
		}
	  return equal;		
	}

	private IHierarchy getModelForFile(String lstFile) {
		Ajde.getDefault().getConfigurationManager().setActiveConfigFile(lstFile);
		Ajde.getDefault().getBuildManager().build(); // was buildStructure...
		while(!testerBuildListener.getBuildFinished()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException ie) { } 
		}
		return Ajde.getDefault().getStructureModelManager().getHierarchy();	
	}

	protected void setUp() throws Exception {
		super.setUp("StructureModelRegressionTest");
//		Ajde.getDefault().getStructureModelManager().setShouldSaveModel(false);
	}
	
	public void testModelExists() {
		assertTrue(Ajde.getDefault().getStructureModelManager().getHierarchy() != null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

