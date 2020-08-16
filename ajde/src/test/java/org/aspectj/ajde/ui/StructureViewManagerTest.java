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
 *     Helen Hawkins  Converted to new interface (bug 148190) 
 * ******************************************************************/

package org.aspectj.ajde.ui;

import java.io.File;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;

import junit.framework.TestSuite;

/**
 * @author Mik Kersten
 */
public class StructureViewManagerTest extends AjdeTestCase {

	// TODO-path
	private final String CONFIG_FILE_PATH = "all.lst";
	private final String CONFIG_FILE_PATH_2 = "inheritance.lst";

	private FileStructureView currentView;
	private final NullIdeStructureViewRenderer renderer = new NullIdeStructureViewRenderer();
	private File testFile;
	private StructureViewProperties properties;

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureViewManagerTest.class);
		return result;
	}

	public void testModelExists() {
		assertTrue(Ajde.getDefault().getModel().getHierarchy() != null);
	}

	public void testNotificationAfterConfigFileChange() {
		initialiseProject("inheritance");
		doBuild(CONFIG_FILE_PATH_2);
		renderer.setHasBeenNotified(false);
		assertTrue("not yet notified", !renderer.getHasBeenNotified());
		Ajde.getDefault().getBuildConfigManager().setActiveConfigFile(CONFIG_FILE_PATH_2);
		assertTrue("notified", renderer.getHasBeenNotified());
		renderer.setHasBeenNotified(false);
		Ajde.getDefault().getBuildConfigManager().setActiveConfigFile("MumbleDoesNotExist.lst");
		assertTrue("notified", renderer.getHasBeenNotified());

		assertTrue("no structure", currentView.getRootNode().getStructureNode().getChildren().get(0) == IHierarchy.NO_STRUCTURE);
	}

	/**
	 * @todo this should be moved to a StructureModelManager test
	 */
	public void testFreshStructureModelCreation() {
		renderer.setHasBeenNotified(false);
		String modelPath = genStructureModelExternFilePath(CONFIG_FILE_PATH);
		openFile(modelPath).delete();

		Ajde.getDefault().getModel().readStructureModel(CONFIG_FILE_PATH);

		assertTrue("notified", renderer.getHasBeenNotified());
		// AMC should this be currentView, or should we recreate the root... do the latter
		// IProgramElement n = currentView.getRootNode().getIProgramElement();
		IProgramElement n = Ajde.getDefault().getModel().getHierarchy().getRoot();
		assertTrue("no structure",
		// currentView.getRootNode().getIProgramElement().getChildren().get(0)
				n == IHierarchy.NO_STRUCTURE);
	}

	public void testModelIntegrity() {
		doBuild(CONFIG_FILE_PATH);
		IProgramElement modelRoot = Ajde.getDefault().getModel().getHierarchy().getRoot();
		assertTrue("root exists", modelRoot != null);

		try {
			testModelIntegrityHelper(modelRoot);
		} catch (Exception e) {
			assertTrue(e.toString(), false);
		}
	}

	private void testModelIntegrityHelper(IProgramElement node) throws Exception {
		for (IProgramElement child : node.getChildren()) {
			if (node == child.getParent()) {
				testModelIntegrityHelper(child);
			} else {
				throw new Exception("parent-child check failed for child: " + child.toString());
			}
		}
	}

	public void testNotificationAfterBuild() {
		renderer.setHasBeenNotified(false);
		doBuild(CONFIG_FILE_PATH);
		assertTrue("notified", renderer.getHasBeenNotified());
	}

	public void testViewCreationWithNullSourceFileAndProperties() {
		currentView = Ajde.getDefault().getStructureViewManager().createViewForSourceFile(null, null);
		assertTrue("no structure", currentView.getRootNode().getStructureNode() == IHierarchy.NO_STRUCTURE);
	}

	protected void setUp() throws Exception {
		super.setUp();

		AsmManager.forceSingletonBehaviour = true;
		initialiseProject("figures-coverage");
		doBuild(CONFIG_FILE_PATH);

		properties = Ajde.getDefault().getStructureViewManager().getDefaultViewProperties();
		// TODO-path
		testFile = openFile("../examples/figures-coverage/figures/Figure.java");
		currentView = Ajde.getDefault().getStructureViewManager().createViewForSourceFile(testFile.getAbsolutePath(), properties);
		currentView.setRenderer(renderer);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		AsmManager.forceSingletonBehaviour = false;
	}
}
