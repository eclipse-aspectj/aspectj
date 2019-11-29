/*******************************************************************************
 * Copyright (c) 2015 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc188;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc188Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testClassCast() throws Exception {
		runTest("classcast");
	}
	
	public void testAnnotationDiscoveryNpe() throws Exception {
		runTest("annotation discovery npe");
	}
	
	public void testDefaultMethodsWithXnoInline() throws Exception {
		runTest("default methods 1");
	}

	public void testDefaultMethodsWithoutXnoInline() throws Exception {
		runTest("default methods 2");
	}
	
	public void testCompileError_478003() throws Exception {
		runTest("compile error");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc188Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc188.xml");
	}

}
