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


package org.aspectj.ajde.internal; 

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.ajde.ui.BuildConfigModel;
import org.aspectj.ajde.ui.utils.TestMessageHandler.TestMessage;

import junit.framework.TestSuite;

public class LstBuildConfigManagerTest extends AjdeTestCase {
	
	private BuildConfigManager buildConfigManager = new LstBuildConfigManager();

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(LstBuildConfigManagerTest.class);	
		return result;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("LstBuildConfigManagerTest");
	}

	public void testConfigParserErrorMessages() {
		doBuild("dir-entry.lst");
		List messages = getMessages("dir-entry.lst");
		TestMessage message = (TestMessage)messages.get(0);
		
		assertEquals(message.getContainedMessage().getSourceLocation().getSourceFile().getAbsolutePath(), openFile("dir-entry.lst").getAbsolutePath());

		doBuild("bad-injar.lst");
		messages = getMessages("bad-injar.lst");
		message = (TestMessage)messages.get(0);
		assertTrue(message.getContainedMessage().getMessage().contains("skipping missing, empty or corrupt inpath entry"));
	}

	public void testErrorMessages() throws IOException {
		doBuild("invalid-entry.lst");
		assertFalse("expected there to be error messages because the build failed but didn't" +
				" find any", getErrorMessages("invalid-entry.lst").isEmpty());
		  
		List messages = getMessages("invalid-entry.lst");
		TestMessage message = (TestMessage)messages.get(0);	
		assertTrue(message.getContainedMessage().getMessage(), message.getContainedMessage().getMessage().contains("aaa.bbb"));
	
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
	
}

