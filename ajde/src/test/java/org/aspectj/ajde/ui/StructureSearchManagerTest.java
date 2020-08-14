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

import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.asm.IProgramElement;

import junit.framework.TestSuite;

/**
 * @author Mik Kersten
 */
public class StructureSearchManagerTest extends AjdeTestCase {

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureSearchManagerTest.class);	
		return result;
	}

	public void testFindPatternMatch() {
		Ajde.getDefault().getStructureSearchManager().findMatches(
			"Point",
			null
		);
		assertTrue("non existent node", true);
	}

	public void testFindPatternAndKindMatch() {
		Ajde.getDefault().getStructureSearchManager().findMatches(
			"Point",
			IProgramElement.Kind.CONSTRUCTOR
		);
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
		super.setUp();
		initialiseProject("StructureSearchManagerTest");
		doBuild("all.lst");		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

