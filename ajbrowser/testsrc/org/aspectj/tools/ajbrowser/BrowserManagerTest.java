/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.tools.ajbrowser;

import java.io.File;
import javax.swing.*;
import junit.framework.*;
//import org.aspectj.asm.*;
import org.aspectj.bridge.*;
import org.aspectj.bridge.IMessage;
import org.aspectj.ajde.*;

/**
 * @author Mik Kersten
 */
public class BrowserManagerTest extends TestCase {

	public BrowserManagerTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(BrowserManagerTest.class);	
		return result;
	}

	public void testInitNoArgs() {
		//String[] args = { "C:/Dev/aspectj/modules/ajde/testdata/examples/figures-coverage/all.lst" };	
		String[] args = { };
		BrowserManager.getDefault().init(args, true);	
	}

	public void testAddProjectTask() {
		BrowserManager.getDefault().init(new String[]{}, true);	
		Ajde.getDefault().getTaskListManager().addProjectTask(
			"project-level task",
			IMessage.ERROR);

		assertTrue("confirmation result", verifySuccess("Project task is visible."));
	}
	
	public void testAddSourceLineTasks() {
		BrowserManager.getDefault().init(new String[]{}, true);	
		ISourceLocation dummyLocation = new SourceLocation(new File("<file>"), -1, -1);
		Ajde.getDefault().getTaskListManager().addSourcelineTask(
			"error task",
			dummyLocation,
			IMessage.ERROR);

		Ajde.getDefault().getTaskListManager().addSourcelineTask(
			"warning task",
			dummyLocation,
			IMessage.WARNING);
		
		Ajde.getDefault().getTaskListManager().addSourcelineTask(
			"info task",
			dummyLocation,
			IMessage.INFO);

		assertTrue("confirmation result", verifySuccess("3 kinds of sourceline tasks are visible."));
	}
	
	
	private boolean verifySuccess(String message) {
		int result = JOptionPane.showConfirmDialog(
			BrowserManager.getDefault().getRootFrame(),
			"Verify Results",
			message,
			JOptionPane.YES_NO_OPTION);		
		return result == JOptionPane.YES_OPTION;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
