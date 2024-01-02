/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc199;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava18OrLater;

/*
 * Some very trivial tests that help verify things are OK.
 * These are a copy of the earlier Sanity Tests created for 1.6 but these supply the -18 option
 * to check code generation and modification with that version specified.
 *
 * @author Alexander Kriegisch
 */
public class SanityTestsJava18 extends XMLBasedAjcTestCaseForJava18OrLater {

	public static final int bytecode_version_for_JDK_level = Constants.MAJOR_18;

	// Incredibly trivial test programs that check the compiler works at all (these are easy-ish to debug)
	public void testSimpleJava_A() {
		runTest("simple - a");
	}

	public void testSimpleJava_B() {
		runTest("simple - b");
	}

	public void testSimpleCode_C() {
		runTest("simple - c");
	}

	public void testSimpleCode_D() {
		runTest("simple - d");
	}

	public void testSimpleCode_E() {
		runTest("simple - e");
	}

	public void testSimpleCode_F() {
		runTest("simple - f");
	}

	public void testSimpleCode_G() {
		runTest("simple - g");
	}

	public void testSimpleCode_H() {
		runTest("simple - h", true);
	}

	public void testSimpleCode_I() {
		runTest("simple - i");
	}

	public void testVersionCorrect1() {
		runTest("simple - j");
		checkVersion("A", bytecode_version_for_JDK_level, 0);
	}

	public void testVersionCorrect2() {
		runTest("simple - k");
		checkVersion("A", bytecode_version_for_JDK_level, 0);
	}

	public void testVersionCorrect4() {
		runTest("simple - m");
		// Must be 49.0 when -1.5 is specified
		checkVersion("A", Constants.MAJOR_1_5, 0);
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(SanityTestsJava18.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("sanity-tests-18.xml");
	}

}
