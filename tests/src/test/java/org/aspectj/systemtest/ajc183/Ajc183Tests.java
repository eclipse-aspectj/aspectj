/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc183;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc183Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testSuperCalls_443355() {
		runTest("super calls");
	}

	public void testSuppressTypeNotFoundAbstract_436653_2() {
		runTest("suppress type not found - abstract 2");
	}
	
	public void testSuppressTypeNotFoundAbstract_436653_1() {
		runTest("suppress type not found - abstract 1");
	}

	public void testSuppressTypeNotFound_436653() {
		runTest("suppress type not found");
	}

	public void testSuppressTypeNotFound_436653_2() {
		runTest("suppress type not found 2");
	}

	public void testSuppressTypeNotFound_436653_3() {
		runTest("suppress type not found 3");
	}
	
	public void testSuppressTypeNotFound_436653_4() {
		runTest("suppress type not found 4");
	}

	public void testSuppressTypeNotFound_436653_5() {
		runTest("suppress type not found 5");
	}

	public void testSuppressTypeNotFound_436653_6() {
		runTest("suppress type not found 6");
	}

	public void testSuppressTypeNotFound_436653_7() {
		runTest("suppress type not found 7");
	}
	
	public void testSuppressTypeNotFound_436653_8() {
		runTest("suppress type not found 8");
	}
	
	public void testConstantPool_445395_0() {
		runTest("constant pool 0");
	}

	public void testConstantPool_445395() {
		runTest("constant pool");
	}
	
	public void testAbstractAspectNPE_444398() {
		runTest("abstract aspect npe");
	}
	
	public void testVerifyError_443447() {
		runTest("verifyerror");
	}
//	
//	public void testAnnoStyleDecp_442425() {
//		runTest("anno style decp");
//	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc183Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
        return getClassResource("ajc183.xml");
	}

}
