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

import java.util.*;

import junit.framework.TestSuite;

import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.asm.ProgramElementNode;

/**
 * @author Mik Kersten
 */
public class StructureModelUtilTest extends AjdeTestCase {
	
	private final String CONFIG_FILE_PATH = "../examples/figures-coverage/all.lst";

	public StructureModelUtilTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(StructureModelUtilTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(StructureModelUtilTest.class);	
		return result;
	}

	public void testPackageViewUtil() {
		List packages = StructureModelUtil.getPackagesInModel(); 
		assertTrue("packages list not null", packages != null);
        assertTrue("packages list not empty", !packages.isEmpty());
		// System.err.println("> packages: " + packages);
	
		ProgramElementNode packageNode = (ProgramElementNode)((Object[])packages.get(0))[0];
		assertTrue("package node not null", packageNode != null);
		// System.err.println("> package node: " + packageNode);
		
		List files = StructureModelUtil.getFilesInPackage(packageNode);
		assertTrue("fle list not null", files != null);
		// System.err.println("> file list: " + files);
		
		Map lineAdviceMap = StructureModelUtil.getLinesToAspectMap(
			((ProgramElementNode)files.get(0)).getSourceLocation().getSourceFile().getAbsolutePath()
		);
		assertTrue("line->advice map not null", lineAdviceMap != null);
		// System.err.println("> line->advice map: " + lineAdviceMap);			
		
		Set aspects = StructureModelUtil.getAspectsAffectingPackage(packageNode);
		assertTrue("aspect list not null", aspects != null);
		// System.err.println("> aspects affecting package: " + aspects);			
	}

  
	protected void setUp() throws Exception {
		super.setUp("StructureModelUtilTest");
		doSynchronousBuild(CONFIG_FILE_PATH);		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}

