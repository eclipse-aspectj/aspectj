/*******************************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc192;

import java.io.File;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc192Tests extends XMLBasedAjcTestCase {

	public void testCflowFinal() {
		runTest("no final on cflow elements");
	}
	
	public void testAroundAdvice_AnnoStyle() {
		runTest("around advice");
	}

	public void testAroundAdvice_CodeStyle() {
		runTest("around advice - 2");
	}

	public void testPTW_nonPrivileged() {
		runTest("ptw");
	}

	public void testPTW_nonPrivilegedSamePackage() {
		runTest("ptw - same package");
	}
	
	public void testPTW_privileged() {
		runTest("ptw - privileged");
	}

	public void testPTWW_privilegedSamePackage() {
		runTest("ptw - privileged same package");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc192Tests.class);
	}

	@Override
	protected File getSpecFile() {
        return getClassResource("ajc192.xml");
	}

}
