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


package org.aspectj.ajde;

import junit.framework.TestSuite;

import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.ajde.ui.internal.AjcBuildOptions;
import org.aspectj.ajde.ui.internal.UserPreferencesStore;

public class BuildOptionsTest extends AjdeTestCase {

	private AjcBuildOptions buildOptions = null;
	private UserPreferencesAdapter preferencesAdapter = null;

	public BuildOptionsTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(BuildOptionsTest.class);
	}

	public static TestSuite suite() {
		TestSuite result = new TestSuite();
		result.addTestSuite(BuildOptionsTest.class);	
		return result;
	}

	public void testCharacterEncoding() {
		buildOptions.setCharacterEncoding("mumble");
		assertTrue("character encoding", buildOptions.getCharacterEncoding().equals("mumble"));
	}
	
	public void testPortingMode() {
		buildOptions.setPortingMode(true);
		assertTrue("porting mode", buildOptions.getPortingMode());
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		preferencesAdapter = new UserPreferencesStore();
		buildOptions = new AjcBuildOptions(preferencesAdapter);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		buildOptions.setCharacterEncoding("");
		buildOptions.setPortingMode(true);
	}
}

