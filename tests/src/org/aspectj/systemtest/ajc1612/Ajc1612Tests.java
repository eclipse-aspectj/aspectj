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

	public void testIllegalAccessError_343051() {
		runTest("illegalaccesserror");
	}
	
	public void testItitNpe_339974() {
		runTest("itit npe");
	}
	
//	public void testNoImportError_342605() {
//		runTest("noimporterror");
//	}
	
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