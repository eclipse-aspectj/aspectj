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


package org.aspectj.ajde.internal; 

import java.io.*;
import java.util.*;

import junit.framework.TestSuite;

import org.aspectj.ajde.*;
import org.aspectj.ajde.NullIdeTaskListManager.SourceLineTask;
import org.aspectj.ajde.ui.BuildConfigModel;
//import org.aspectj.ajde.ui.internal.AjcBuildOptions;
//import org.aspectj.bridge.Message;

public class LstBuildConfigManagerTest extends AjdeTestCase {
	
//	private AjcBuildOptions buildOptions = null;
	private BuildConfigManager buildConfigManager = new LstBuildConfigManager();
//	private LstBuildConfigFileUpdater fileUpdater = new LstBuildConfigFileUpdater();

	public LstBuildConfigManagerTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(LstBuildConfigManagerTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(LstBuildConfigManagerTest.class);	
		return result;
	}

	public void testConfigParserErrorMessages() {
		doSynchronousBuild("dir-entry.lst");
		List messages = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		NullIdeTaskListManager.SourceLineTask message = (NullIdeTaskListManager.SourceLineTask)messages.get(0);
		
		assertEquals(message.getContainedMessage().getSourceLocation().getSourceFile().getAbsolutePath(), openFile("dir-entry.lst").getAbsolutePath());

		doSynchronousBuild("bad-injar.lst");
		messages = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		message = (NullIdeTaskListManager.SourceLineTask)messages.get(0);
		assertTrue(message.getContainedMessage().getMessage().indexOf("skipping missing, empty or corrupt inpath entry") != -1);
	}

	public void testErrorMessages() throws IOException {
		doSynchronousBuild("invalid-entry.lst");
		assertTrue("compile failed", testerBuildListener.getBuildSucceeded());
		  
		List messages = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		SourceLineTask message = (SourceLineTask)messages.get(0);	
		assertTrue(message.getContainedMessage().getMessage(), message.getContainedMessage().getMessage().indexOf("aaa.bbb") != -1);		
	
	}

	public void testNonExistentConfigFile() throws IOException {
		File file = openFile("mumbleDoesNotExist.lst");
		assertTrue("valid non-existing file", !file.exists());
		BuildConfigModel model = buildConfigManager.buildModel(file.getCanonicalPath());
		assertTrue("root: " + model.getRoot(), model.getRoot() != null);		
	}

	public void testFileRelativePathSameDir() throws IOException {
		File file = openFile("file-relPath-sameDir.lst");
		buildConfigManager.buildModel(file.getCanonicalPath());
		assertTrue("single file", true);
	}  
	
//	private void verifyFile(String configFile, String fileContents) {
//		StringTokenizer st = new StringTokenizer(fileContents, ";");
//		BuildConfigModel model1 = buildConfigManager.buildModel(configFile);
//		File testFile = new File(configFile + "-test.lst");
//		model1.setSourceFile(testFile.getPath());
//		buildConfigManager.writeModel(model1);
//		List newList = fileUpdater.readConfigFile(testFile.getPath());
//		testFile.delete();
//		
//		assertTrue("contents: " + newList, verifyLists(st, newList));
//	}
//	
//	private boolean verifyLists(StringTokenizer st, List list) {
//		Iterator it = list.iterator();
//		while (st.hasMoreElements()) {
//			String s1 = (String)st.nextElement();
//			String s2 = (String)it.next();
//			if (!s1.equals(s2)) return false;
//		}
//		if (it.hasNext()) {
//			return false;
//		} else {
//			return true;
//		}
//	}
	
	protected void setUp() throws Exception {
		super.setUp("LstBuildConfigManagerTest");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
//	private static final String WILDCARDS_FILE = "C:/Dev/aspectj/tests/ajde/examples/figures-coverage/test-config.lst";
//	private static final String BAD_PATHS_FILE = "C:/Dev/aspectj/tests/ajde/examples/figures-coverage/test-error.lst";
//	private static final String INCLUDES_FILE = "C:/Dev/aspectj/tests/ajde/examples/spacewar/spacewar/demo.lst";

//	private static final String WILDCARDS_FILE_CONTENTS;
//	private static final String BAD_PATHS_FILE_CONTENTS;
//	private static final String INCLUDES_FILE_CONTENTS;
	
	static {
//		WILDCARDS_FILE_CONTENTS = 
//			"figures/Debug.java;" +
//			"figures/Figure.java;" +
//			"figures/FigureElement.java;" +
//			"figures/Main.java;" +
//			"figures/composites/Line.java;" +
//			"figures/composites/Square.java;" +
//			"figures/primitives/planar/Point.java;" +
//			"figures/primitives/solid/SolidPoint.java;";

//		BAD_PATHS_FILE_CONTENTS = WILDCARDS_FILE_CONTENTS;
//
//        // TODO-path
//		INCLUDES_FILE_CONTENTS =
//			"../coordination/Condition.java;" +
//			"../coordination/CoordinationAction.java;" +
//			"../coordination/Coordinator.java;" +
//			"../coordination/Exclusion.java;" +
//			"../coordination/MethodState.java;" +
//			"../coordination/Mutex.java;" +
//			"../coordination/Selfex.java;" +
//			"../coordination/TimeoutException.java;" +
//			"Bullet.java;" +
//			"Display.java;" +
//			"Display1.java;" +
//			"Display2.java;" +
//			"EnergyPacket.java;" +
//			"EnergyPacketProducer.java;" +
//			"EnsureShipIsAlive.java;" +
//			"Game.java;" +
//			"GameSynchronization.java;" +
//			"Pilot.java;" +
//			"Player.java;" +
//			"Registry.java;" +
//			"RegistrySynchronization.java;" +
//			"Robot.java;" +
//			"SWFrame.java;" +
//			"Ship.java;" +
//			"SpaceObject.java;" +
//			"Timer.java;";
	}
}


//public void testWildcards() {
//	verifyFile(WILDCARDS_FILE, WILDCARDS_FILE_CONTENTS);
//}
//
//public void testIncludes() {
//	verifyFile(INCLUDES_FILE, INCLUDES_FILE_CONTENTS);
//}

