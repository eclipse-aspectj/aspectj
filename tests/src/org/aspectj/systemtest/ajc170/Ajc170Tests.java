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
package org.aspectj.systemtest.ajc170;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc170Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testPerThis() {
		runTest("perthis");
	}

	public void testPerTarget() {
		runTest("pertarget");
	}

	public void testPerCflow() {
		runTest("percflow");
	}

	public void testPerTypeWithin() {
		runTest("pertypewithin");
	}

	// not specifying -1.7
	public void testDiamond1() {
		runTest("diamond 1");
	}

	public void testDiamond2() {
		runTest("diamond 2");
	}

	public void testDiamondItd1() {
		runTest("diamond itd 1");
	}

	public void testLiterals1() {
		runTest("literals 1");
	}

	public void testLiterals2() {
		runTest("literals 2");
	}

	public void testLiteralsItd1() {
		runTest("literals itd 1");
	}

	public void testStringSwitch1() {
		runTest("string switch 1");
	}

	public void testStringSwitch2() {
		runTest("string switch 2");
	}

	public void testMultiCatch1() {
		runTest("multi catch 1");
	}

	public void testMultiCatch2() {
		runTest("multi catch 2");
	}

	public void testMultiCatchWithHandler1() {
		runTest("multi catch with handler 1");
	}

	public void testMultiCatchAspect1() {
		runTest("multi catch aspect 1");
	}

	// public void testMultiCatchWithHandler2() {
	// runTest("multi catch with handler 2");
	// }

	public void testSanity1() {
		runTest("sanity 1");
	}

	public void testMissingImpl_363979() {
		runTest("missing impl");
	}

	public void testMissingImpl_363979_2() {
		runTest("missing impl 2");
	}

	public void testStackOverflow_364380() {
		runTest("stackoverflow");
	}

	// public void testTryResources1() {
	// runTest("try resources 1");
	// }
	//
	// public void testTryResources2() {
	// runTest("try resources 2");
	// }

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc170Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc170/ajc170.xml");
	}

}