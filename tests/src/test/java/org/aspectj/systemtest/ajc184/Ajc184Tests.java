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
package org.aspectj.systemtest.ajc184;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc184Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// The thisAspectInstance tests here are a copy from Ajc1612Tests but we are compiling with -1.8 and not -1.5
	
	public void testThisAspectInstance_239649_1() throws Exception {
		// simple case
		runTest("thisAspectInstance - 1");
	}

	public void testThisAspectInstance_239649_2() throws Exception {
		// before advice toggling on/off through if called method
		runTest("thisAspectInstance - 2");
	}

	public void testThisAspectInstance_239649_3() throws Exception {
		// after advice toggling on/off through if called method
		runTest("thisAspectInstance - 3");
	}

	public void testThisAspectInstance_239649_4() throws Exception {
		// before advice, also using thisJoinPointStaticPart
		runTest("thisAspectInstance - 4");
	}

	public void testThisAspectInstance_239649_5() throws Exception {
		// before advice, also using thisJoinPoint
		runTest("thisAspectInstance - 5");
	}

	public void testThisAspectInstance_239649_6() throws Exception {
		// before advice, also using thisEnclosingJoinPointStaticPart
		runTest("thisAspectInstance - 6");
	}

	public void testThisAspectInstance_239649_7() throws Exception {
		// before advice, also using thisJoinPoint and thisJoinPointStaticPart
		runTest("thisAspectInstance - 7");
	}

	public void testThisAspectInstance_239649_8() throws Exception {
		// before advice, also using abstract aspects
		runTest("thisAspectInstance - 8");
	}

	public void testThisAspectInstance_239649_9() throws Exception {
		// before advice, also using abstract aspects 2
		runTest("thisAspectInstance - 9");
	}

	public void testThisAspectInstance_239649_10() throws Exception {
		// aspects in a package
		runTest("thisAspectInstance - 10");
	}

	public void testThisAspectInstance_239649_11() throws Exception {
		// non-singleton aspect - should be an error for now
		runTest("thisAspectInstance - 11");
	}

	public void testThisAspectInstance_239649_12() throws Exception {
		// arg binding and tjpsp
		runTest("thisAspectInstance - 12");
	}

	public void testThisAspectInstance_239649_13() throws Exception {
		// pass instance
		runTest("thisAspectInstance - 13");
	}

	public void testThisAspectInstance_239649_14() throws Exception {
		// multiple ifs
		runTest("thisAspectInstance - 14");
	}

	public void testThisAspectInstance_239649_15() throws Exception {
		// abstract aspects
		runTest("thisAspectInstance - 15");
	}
	
	public void testIsFinal_449739() {
		runTest("is final");
	}

	public void testIsFinal_449739_2() {
		runTest("is final - 2");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc184Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
        return getClassResource("ajc184.xml");
	}

}
