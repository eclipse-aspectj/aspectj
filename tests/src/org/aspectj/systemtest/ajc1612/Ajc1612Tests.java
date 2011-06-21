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
package org.aspectj.systemtest.ajc1612;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc1612Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// public void testAnnoCopying_345515() {
	// runTest("anno copying");
	// }

	public void testOrdering_pr349961() {
		runTest("ordering");
	}

	public void testOrdering_pr349961_2() {
		runTest("ordering - 2");
	}
/*
	public void testVerifyError_pr347395() {
		runTest("verifyerror - inline");
	}
	*/

	public void testDuplicateMethods_349398() {
		runTest("duplicate methods");
	}

	public void testBindingInts_347684() {
		runTest("binding ints");
	}

	public void testBindingInts_347684_2() {
		runTest("binding ints - 2");
	}

	public void testBindingInts_347684_3() {
		runTest("binding ints - 3");
	}

	public void testBindingInts_347684_4() {
		runTest("binding ints - 4");
	}

	public void testBindingInts_347684_5() {
		runTest("binding ints - 5");
	}

	public void testBindingInts_347684_6() {
		runTest("binding ints - 6");
	}

	public void testIncorrectAnnos_345172() {
		runTest("incorrect annos");
	}

	public void testIncorrectAnnos_345172_2() {
		runTest("incorrect annos 2");
	}

	public void testIncorrectAnnos_345172_3() {
		runTest("incorrect annos 3");
	}

	public void testSyntheticMethods_327867() {
		runTest("synthetic methods");
	}

	// public void testSignedJarLtw_328099() {
	// runTest("signed jar ltw");
	// }

	public void testVerifyError_315398() {
		runTest("verifyerror");
	}

	public void testVerifyError_315398_2() {
		runTest("verifyerror - 2");
	}

	public void testRawTypePointcut_327134() {
		runTest("rawtype pointcut");
	}

	public void testRawTypeWarning_335810() {
		runTest("rawtype warning");
	}

	// public void testDecpGenerics_344005() {
	// runTest("decp generics");
	// }

	public void testIllegalAccessError_343051() {
		runTest("illegalaccesserror");
	}

	public void testItitNpe_339974() {
		runTest("itit npe");
	}

	// public void testNoImportError_342605() {
	// runTest("noimporterror");
	// }

	public void testClashingLocalTypes_342323() {
		runTest("clashing local types");
	}

	public void testITIT_338175() {
		runTest("itit");
	}

	public void testThrowsClause_292239() {
		runTest("throws clause");
	}

	public void testThrowsClause_292239_2() {
		runTest("throws clause - 2");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1612Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc1612/ajc1612.xml");
	}

}