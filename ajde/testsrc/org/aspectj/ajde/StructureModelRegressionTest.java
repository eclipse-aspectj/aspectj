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

import java.io.File;

import junit.framework.TestSuite;

import org.aspectj.asm.StructureModel;

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
		String testLstFile = "StructureModelRegressionTest/example.lst";
        File f = new File(testLstFile);
        assertTrue(testLstFile, f.canRead());
        assertTrue("saved model: " + testLstFile, verifyAgainstSavedModel(testLstFile));    
	}

	public boolean verifyAgainstSavedModel(String lstFile) {
		File modelFile = new File(genStructureModelExternFilePath(lstFile));
		StructureModel model = getModelForFile(lstFile);
		System.out.println(">> model: " + model.getRoot());	
		
		if (modelFile.exists()) {
			Ajde.getDefault().getStructureModelManager().readStructureModel(lstFile);
			StructureModel savedModel = Ajde.getDefault().getStructureModelManager().getStructureModel();
			//System.err.println( savedModel.getRoot().getClass() + ", " +  savedModel.getRoot());
			
			return savedModel.getRoot().equals(model.getRoot());
		} else {
			Ajde.getDefault().getStructureModelManager().writeStructureModel(lstFile);
			return true;
		}
		//return true;
	}

	private StructureModel getModelForFile(String lstFile) {
		Ajde.getDefault().getConfigurationManager().setActiveConfigFile(lstFile);
		Ajde.getDefault().getBuildManager().buildStructure();
		while(!testerBuildListener.getBuildFinished()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException ie) { } 
		}
		return Ajde.getDefault().getStructureModelManager().getStructureModel();	
	}

	protected void setUp() throws Exception {
		super.setUp("StructureModelRegressionTest");
		Ajde.getDefault().getStructureModelManager().setShouldSaveModel(false);
	}
	
	public void testModelExists() {
		assertTrue(Ajde.getDefault().getStructureModelManager().getStructureModel() != null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

