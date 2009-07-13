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
package org.aspectj.systemtest.ajc166;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc166Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	// fix is too disruptive for 1.6.5
	// public void testGenericsBridge_pr279983() {
	// runTest("generics bridge");
	// }

	public void testInterfacesSerializable_pr283229() {
		runTest("interfaces and serializable");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc166Tests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc166/ajc166.xml");
	}

}