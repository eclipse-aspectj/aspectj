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


package org.aspectj.ajde.ui; 

import java.io.File;
import java.util.Iterator;

import junit.framework.TestSuite;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.asm.*;

/**
 * @author Mik Kersten
 */
public class StructureViewManagerTest extends AjdeTestCase {
	
    // TODO-path
	private final String CONFIG_FILE_PATH = "../examples/figures-coverage/all.lst";
	private final String CONFIG_FILE_PATH_2 = "../examples/inheritance/inheritance.lst";
	
	private FileStructureView currentView;
	private NullIdeStructureViewRenderer renderer = new NullIdeStructureViewRenderer();
	private File testFile;
	private StructureViewProperties properties;
	
	public StructureViewManagerTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(StructureViewManagerTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureViewManagerTest.class);	
		return result;
	}

	public void testModelExists() {
		assertTrue(Ajde.getDefault().getStructureModelManager().getHierarchy() != null);
	}

	public void testNotificationAfterConfigFileChange() {
		doSynchronousBuild(CONFIG_FILE_PATH_2);
		renderer.setHasBeenNotified(false);
		assertTrue("not yet notified", !renderer.getHasBeenNotified());
		Ajde.getDefault().getConfigurationManager().setActiveConfigFile(CONFIG_FILE_PATH_2);			
		assertTrue("notified", renderer.getHasBeenNotified());
		renderer.setHasBeenNotified(false);
		Ajde.getDefault().getConfigurationManager().setActiveConfigFile("MumbleDoesNotExist.lst");			
		assertTrue("notified", renderer.getHasBeenNotified());		
		
		assertTrue(
			"no structure", 
			currentView.getRootNode().getStructureNode().getChildren().get(0) 
			== IHierarchy.NO_STRUCTURE
		);			
	}

	/**
	 * @todo	this should be moved to a StructureModelManager test
	 */
	public void testFreshStructureModelCreation() {
		renderer.setHasBeenNotified(false);
		String modelPath = genStructureModelExternFilePath(CONFIG_FILE_PATH);
		openFile(modelPath).delete();
		
		Ajde.getDefault().getStructureModelManager().readStructureModel(CONFIG_FILE_PATH);
		
		assertTrue("notified", renderer.getHasBeenNotified());	
		// AMC should this be currentView, or should we recreate the root... do the latter	
		//IProgramElement n = currentView.getRootNode().getIProgramElement();
		IProgramElement n = Ajde.getDefault().getStructureModelManager().getHierarchy().getRoot();
		assertTrue(
			"no structure", 
			//currentView.getRootNode().getIProgramElement().getChildren().get(0) 
			n == IHierarchy.NO_STRUCTURE
		);	
	}

	public void testModelIntegrity() {
		doSynchronousBuild(CONFIG_FILE_PATH);
		IProgramElement modelRoot = Ajde.getDefault().getStructureModelManager().getHierarchy().getRoot();
		assertTrue("root exists", modelRoot != null);	
		
		try {
			testModelIntegrityHelper(modelRoot);
		} catch (Exception e) {
			assertTrue(e.toString(), false);	
		}
	}

	private void testModelIntegrityHelper(IProgramElement node) throws Exception {
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement child = (IProgramElement)it.next();
			if (node == child.getParent()) {
				testModelIntegrityHelper(child);
			} else {
				throw new Exception("parent-child check failed for child: " + child.toString());
			}
		}		
	}

	public void testNotificationAfterBuild() {
		renderer.setHasBeenNotified(false);
		doSynchronousBuild(CONFIG_FILE_PATH);
		assertTrue("notified", renderer.getHasBeenNotified());
	}

	public void testViewCreationWithNullSourceFileAndProperties() {
		currentView = Ajde.getDefault().getStructureViewManager().createViewForSourceFile(null, null);	
		assertTrue(
			"no structure", 
			currentView.getRootNode().getStructureNode() 
			== IHierarchy.NO_STRUCTURE
		);
	}
  
	protected void setUp() throws Exception {
		super.setUp("StructureViewManagerTest");
		doSynchronousBuild(CONFIG_FILE_PATH);		
		
		properties = Ajde.getDefault().getStructureViewManager().getDefaultViewProperties();
        // TODO-path
		testFile = openFile("../examples/figures-coverage/figures/Figure.java");
		currentView = Ajde.getDefault().getStructureViewManager().createViewForSourceFile(testFile.getAbsolutePath(), properties);
		currentView.setRenderer(renderer);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
//	public void testViewListenerRegistrations() {
//		Ajde.getDefault().getBuildManager().build("C:/Dev/aspectj/tests/ajde/examples/coverage-figures/src/AllFiles.lst");
//		while(!testerBuildListener.getBuildFinished()) {
//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException ie) { } 
//		}
//		List renderers = Ajde.getDefault().getStructureViewManager().getDefaultFileStructureView().getRenderers();
//		
//		testerBuildListener.reset();
//		Ajde.getDefault().getBuildManager().build("C:/Dev/aspectj/tests/ajde/examples/coverage-figures/src/AllFiles.lst");
//		while(!testerBuildListener.getBuildFinished()) {
//			try {
//				Thread.sleep(300);
//			} catch (InterruptedException ie) { } 
//		}  
//		assertTrue("checking renderers", true);
//	} 
}

