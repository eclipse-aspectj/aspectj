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
package org.aspectj.systemtest.ajc1611;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.systemtest.ajc150.GenericsTests;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1611Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// error without the fix:
	// Second.java:8:0::0 Bound mismatch: The generic method foo(R, Class<T>) of type II is not applicable for the arguments (E1,
	// Class<capture#1-of ? extends E2>). The inferred type capture#1-of ? extends E2 is not a valid substitute for the bounded
	// parameter <R extends I1>
	public void testBoundsChecking_pr336880() {
		runTest("bounds check confusion");
	}

	public void testClashingItds_pr336774() {
		runTest("clashing itds");
	}

	public void testBadGenericSigAttribute_pr336745() {
		runTest("incorrect signature");
		JavaClass jc = GenericsTests.getClass(ajc, "C");
		assertNotNull(jc);
		Method m = getMethod(jc, "m");
		Method mitd = getMethod(jc, "mitd");
		assertEquals("<T::LI;>(TT;)V", m.getGenericSignature());
		assertEquals("<T::LI;>(TT;)V", mitd.getGenericSignature());
	}

	private Method getMethod(JavaClass jc, String name) {
		for (Method m : jc.getMethods()) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		return null;
	}

	public void testESJP_336471() {
		runTest("esjp");
	}

	public void testITIT_336136() {
		runTest("itit");
	}

	public void testITIT_336136_2() {
		runTest("itit - 2");
	}

	public void testDeserialization_335682() {
		runTest("pr335682");
	}

	public void testDeserialization_335682_2() {
		runTest("pr335682 - 2");
	}

	public void testDeserialization_335682_3() {
		runTest("pr335682 - 3");
	}

	public void testDeserialization_335682_5() {
		runTest("pr335682 - 5");
	}

	public void testNPEAddSerialVersionUID_bug335783() {
		runTest("pr335783");
	}

	public void testGenericsAndItds_333469() {
		runTest("pr333469");
	}

	public void testMissingType_332388() {
		runTest("pr332388");
	}

	public void testMissingType_332388_2() {
		runTest("pr332388 - 2");
	}

	public void testDeclareField_328840() {
		runTest("pr328840");
	}

	 public void testAnnoStyleAdviceChain_333274() {
		 runTest("anno style advice chain");
	 }
	
	 public void testAnnoStyleAdviceChain_333274_2() {
		 runTest("code style advice chain");
	 }
	
	 public void testAnnoStyleAdviceChain_333274_3() {
		 runTest("code style advice chain - no inline");
	 }

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1611Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1611.xml");
	}

}