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
package org.aspectj.systemtest.ajc164;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc164Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testRogueErrors_pr246393_1() {
		runTest("rogue errors - 1");
	}

//	public void testNameClash_pr262257() {
//		runTest("name clash");
//		fail("incomplete");
//	}

	public void testCompilingSpring_pr260384() {
		runTest("compiling spring");
	}

	public void testCompilingSpring_pr260384_2() {
		runTest("compiling spring - 2");
	}

	public void testCompilingSpring_pr260384_3() {
		runTest("compiling spring - 3");
	}

	public void testCompilingSpring_pr260384_4() {
		runTest("compiling spring - 4");
	}

	public void testAtAspectJDecp_pr164016() {
		runTest("ataspectj decp 164016");
	}

	public void testAtAspectJDecp_pr258788() {
		runTest("ataspectj decp 258788");
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc164Tests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc164/ajc164.xml");
	}

}