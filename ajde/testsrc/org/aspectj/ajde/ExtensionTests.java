/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.ajde;

import java.util.List;
import java.io.File;

import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Tests the 'extensions' to AJDE:
 * 1) ID is now available on messages to allow you to see what 'kind' of 
 *    message it is - this activates quick fixes/etc in Eclipse.
 */
public class ExtensionTests extends AjcTestCase {

	public static final String PROJECT_DIR = "extensions";

	private File baseDir;
	
	protected void setUp() throws Exception {
		super.setUp();
		baseDir = new File("../ajde/testdata",PROJECT_DIR);
	}
	
	/**
	 * Aim: Check that the ID of certain message kinds are correct
	 * 
	 *   ajc -warn:unusedImport UnusedImport.java
	 * 
	 * Expected result = id 
	 */
	public void testOutjarInInjars () {
		String[] args = new String[] {"UnusedImport.java","-warn:unusedImport"};
		CompilationResult result = ajc(baseDir,args);
		List l = result.getWarningMessages();
		IMessage m = ((IMessage)l.get(0));
		assertTrue("Expected ID of message to be "+IProblem.UnusedImport+" (UnusedImport) but found an ID of "+m.getID(),
			m.getID()==IProblem.UnusedImport);
	}
	
	

}
