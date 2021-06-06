/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
