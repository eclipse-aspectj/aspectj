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


package org.aspectj.ajde;

import junit.framework.TestSuite;

import org.aspectj.bridge.Version;

/**
 * @author Mik Kersten
 */
public class VersionTest extends AjdeTestCase {

	public VersionTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(VersionTest.class);	
		return result;
	}
	
	public void testVersionMatch() {
		String ajdeVersion = Ajde.getDefault().getVersion();
		String compilerVersion = Version.text;
		assertTrue("version check", ajdeVersion.equals(compilerVersion));
	}
	
	protected void setUp() throws Exception {
		super.setUp("");
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
