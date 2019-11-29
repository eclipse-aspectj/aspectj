/*******************************************************************************
 * Copyright (c) 2010 Lucierna 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abraham Nevado - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc1611;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class NewFeatures extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testAddingRemoving() {
		runTest("adding and removing");
	}

	public void testAddingRemoving2() {
		runTest("adding and removing - 2");
	}

	public void testAddingRemoving3() {
		runTest("adding and removing - 3");
	}

	public void testDeclareMinus() {
		runTest("declare minus - 1");
	}

	public void testDeclareMinusItd() {
		runTest("declare minus - itd");
	}

	public void testDeclareMinus2annos() {
		runTest("declare minus - 2 annos");
	}

	public void testDeclareMinusmultifiles() {
		runTest("declare minus - multifiles");
	}

	public void testDeclareMinusmultifiles2() {
		runTest("declare minus - multifiles - 2");
	}

	public void testDeclareMinusmultifiles3() {
		runTest("declare minus - multifiles - 3");
	}

	public void testDeclareMinusWithValues() {
		runTest("declare minus - with values");
	}

	public void testDeclareMinusUnsupported() {
		runTest("declare minus - unsupported");
	}

	public void testBinaryWeaving() {
		runTest("binary weaving");
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(NewFeatures.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("newfeatures-tests.xml");
	}

}