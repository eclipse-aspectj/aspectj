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
package org.aspectj.systemtest.ajc164;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * 
 * @author Andy Clement
 */
public class JointpointIdTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testCaseA() {
		runTest("casea");
	}

	// --

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(JointpointIdTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc164/joinpointid.xml");
	}

}