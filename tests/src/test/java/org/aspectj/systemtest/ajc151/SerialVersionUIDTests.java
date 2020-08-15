/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc151;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class SerialVersionUIDTests extends XMLBasedAjcTestCase {

	public void testTheBasics() {
		runTest("basic");
	}

	public void testTheBasicsWithLint() {
		runTest("basic - lint");
	}

	public void testHorrible() {
		runTest("horrible");
	}

	public void testAbstractClass() {
		runTest("abstract class");
	}

	//
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(SerialVersionUIDTests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("serialversionuid.xml");
	}

}
