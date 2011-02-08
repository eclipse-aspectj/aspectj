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

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc1611Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

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

	// public void testAnnoStyleAdviceChain_333274() {
	// runTest("anno style advice chain");
	// }
	//
	// public void testAnnoStyleAdviceChain_333274_2() {
	// runTest("code style advice chain");
	// }
	//
	// public void testAnnoStyleAdviceChain_333274_3() {
	// runTest("code style advice chain - no inline");
	// }

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1611Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc1611/ajc1611.xml");
	}

}