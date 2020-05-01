/*******************************************************************************
 * Copyright (c) 2020 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc196;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava14OrLater;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc196Tests extends XMLBasedAjcTestCaseForJava14OrLater {

	public void testNPE_558995() {
		runTest("early resolution of supporting interfaces");
	}

	public void testRecords() {
		runTest("simple record");
		checkVersion("Person", Constants.MAJOR_14, Constants.PREVIEW_MINOR_VERSION);
	}

	public void testRecords2() {
		runTest("using a record");
	}

	public void testInstanceofPatterns() {
		runTest("instanceof patterns");
	}

	public void testAdvisingRecords() {
		runTest("advising records");
	}

	public void testSwitch1() {
		runTest("switch 1");
		checkVersion("Switch1", Constants.MAJOR_14, 0);
	}

	public void testSwitch2() {
		runTest("switch 2");
		checkVersion("Switch2", Constants.MAJOR_14, 0);
	}

	public void testSwitch3() {
		runTest("switch 3");
		checkVersion("Switch3", Constants.MAJOR_14, 0);
	}

	public void testTextBlock1() {
		runTest("textblock 1");
	}

	public void testTextBlock2() {
		runTest("textblock 2");
	}
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc196Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc196.xml");
	}

}
