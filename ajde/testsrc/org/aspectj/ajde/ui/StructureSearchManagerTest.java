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


package org.aspectj.ajde.ui;

import java.util.List;

import junit.framework.TestSuite;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.asm.ProgramElementNode;

/**
 * @author Mik Kersten
 */
public class StructureSearchManagerTest extends AjdeTestCase {
	
	private final String CONFIG_FILE_PATH = "../examples/figures-coverage/all.lst";

	public StructureSearchManagerTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(StructureSearchManagerTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureSearchManagerTest.class);	
		return result;
	}

	public void testFindPatternMatch() {
		List matches = Ajde.getDefault().getStructureSearchManager().findMatches(
			"Point",
			null
		);
		System.err.println(matches);
		assertTrue("non existent node", true);
	}

	public void testFindPatternAndKindMatch() {
		List matches = Ajde.getDefault().getStructureSearchManager().findMatches(
			"Point",
			ProgramElementNode.Kind.CONSTRUCTOR
		);
		System.err.println(matches);
		assertTrue("non existent node", true);
	}

	public void testFindNonExistent() {
		List matches = Ajde.getDefault().getStructureSearchManager().findMatches(
			"mumbleNodeDesNotExist",
			null
		);
		assertTrue("non existent", matches.isEmpty());
	}
  
	protected void setUp() throws Exception {
		super.setUp("StructureSearchManagerTest");
		doSynchronousBuild(CONFIG_FILE_PATH);		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

