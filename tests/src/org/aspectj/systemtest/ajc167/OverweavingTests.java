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
package org.aspectj.systemtest.ajc167;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * 
 * @author Andy Clement
 */
public class OverweavingTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testSimple() {
		runTest("simple");
	}

	/**
	 * Now an aspect used on the original weave is mentioned in the aop.xml - we shouldn't apply it again!
	 */
	public void testMessy() {
		runTest("messy");
	}

	/**
	 * Testing a shadow munger created to support cflow
	 */
	public void testCflow() {
		runTest("cflow");
	}

	// --

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(OverweavingTests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc167/overweaving.xml");
	}

}