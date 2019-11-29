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

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * Testing whether AspectJ can overweave. Overweaving is where we attempt to weave something that has already been woven. The simple
 * rule followed is that aspects that applied to the type before are not applied this time around (if they are visible to the
 * weaver).
 * 
 * @author Andy Clement
 */
public class OverweavingTests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testGetSet1() {
		runTest("getset - 1"); // testing what should happen for get/set
	}

	public void testGetSet2() {
		runTest("getset - 2"); // testing what actually happens when overweaving
	}

	public void testGetSetTjp1() {
		runTest("getset - tjp - 1");
	}

	public void testCalls1() {
		runTest("calls - 1"); // testing what should happen for calls
	}

	public void testCalls2() {
		runTest("calls - 2"); // testing what actually happens when overweaving
	}

	public void testCallsTjp1() {
		runTest("calls - tjp - 1");
	}

	public void testComplex() {
		runTest("really messy");
	}

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

	protected java.net.URL getSpecFile() {
		return getClassResource("overweaving.xml");
	}

}