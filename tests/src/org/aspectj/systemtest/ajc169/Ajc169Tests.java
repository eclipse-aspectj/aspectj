/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc169;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc169Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	/*public void testAmbiguousMethod() {
		runTest("ambiguous method");
	}*/

	public void testChecker() {
		runTest("inserts in messages");
	}
/*
	public void testVerifyError() {
		runTest("verifyerror on atAj");
	}

	public void testDeclareTypeWarning1() {
		runTest("declare type warning - 1");
	}

	public void testDeclareTypeWarning2() {
		runTest("declare type warning - 2");
	}

	public void testDeclareTypeWarning3() {
		runTest("declare type warning - 3");
	}

	public void testDeclareTypeError1() {
		runTest("declare type error - 1");
	}*/

	public void testPr298388() {
		runTest("declare mixin and generics");
	}

	public void testPr292584() {
		runTest("annotation around advice verifyerror");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc169Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc169/ajc169.xml");
	}

}