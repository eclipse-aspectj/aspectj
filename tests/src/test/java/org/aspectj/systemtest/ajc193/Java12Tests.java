/*******************************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc193; 

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava12OrLater;

import junit.framework.Test;

/**
 * @author Andy Clement
 */ 
public class Java12Tests extends XMLBasedAjcTestCaseForJava12OrLater {

	public void testSwitch1() {
		runTest("switch 1");
		checkVersion("Switch1", Constants.MAJOR_12, Constants.PREVIEW_MINOR_VERSION);
	}

	public void testSwitch2() {
		runTest("switch 2");
		checkVersion("Switch2", Constants.MAJOR_12, Constants.PREVIEW_MINOR_VERSION);
	}

	public void testSwitch3() {
		runTest("switch 3");
		checkVersion("Switch3", Constants.MAJOR_12, Constants.PREVIEW_MINOR_VERSION);
	}
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Java12Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc193.xml");
	}

}
