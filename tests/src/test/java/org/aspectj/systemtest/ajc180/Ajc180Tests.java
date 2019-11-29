/*******************************************************************************
 * Copyright (c) 2013-2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc180;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc180Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testPercflowLtw_432178() {
		runTest("percflow ltw");
	}
	
	public void testStackmapframe_431976() {
		runTest("stackmapframe");
	}
	
	public void testThisJoinPointNotInitialized_431976() {
		runTest("thisJoinPoint not initialized");
	}
	
	public void testNullAnnotationMatching_431541() {
		runTest("NullAnnotationMatching exception");
	}
	
	public void testAnnosWith18Flags_415957() {
		runTest("annotations with 1.8 flags");
	}
	
	public void testJava8Code() throws Exception {
		runTest("first advised java 8 code");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc180Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc180.xml");
	}

}
