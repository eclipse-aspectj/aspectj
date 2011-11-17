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
package org.aspectj.systemtest.ajc170;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc170Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testMissingImpl_363979() {
		runTest("missing impl");
	}

	// public void testMissingImpl_363979_2() {
	// runTest("missing impl 2");
	// }

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc170Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc170/ajc170.xml");
	}

}