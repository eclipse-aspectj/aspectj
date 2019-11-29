/*******************************************************************************
 * Copyright (c) 2013 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc174;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc174Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
/* wip
	public void testAjdtMarkers() throws Exception {
		runTest("ajdt markers");
	}
*/
	
	public void testExtraInserts() throws Exception {
		runTest("extra inserts");
	}
	
	public void testMoreConfigurableLint_419279() throws Exception {
		runTest("more configurable lint");
	}
	
	public void testAnnotatedItd_418129() throws Exception {
		runTest("annotated itd");
	}
	
	public void testAnnotatedItd_418129_2() throws Exception {
		runTest("annotated itd 2");
	}
	
	public void testAnnotatedItd_418129_3() throws Exception {
		runTest("annotated itd 3");
	}
	
	public void testAnnotatedItd_418129_4() throws Exception {
		runTest("annotated itd 4");
	}

	public void testSuperItdCtor_413378() throws Exception {
		runTest("super itd ctor");
	}
	
	// no exclusion, this is how it should work
	public void testCLExclusion_pr368046_1_noskippedloaders() {
		runTest("classloader exclusion - 1");
	}

	public void testCLExclusion_pr368046_1_syspropset() {
		try {
			System.setProperty("aj.weaving.loadersToSkip", "foo");
			runTest("classloader exclusion - 2");
		} finally {
			System.setProperty("aj.weaving.loadersToSkip", "");
		}
	}

	// final repeat this test, to confirm no lingering static
	public void testCLExclusion_pr368046_1_again_noskippedloaders() {
		runTest("classloader exclusion - 3");
	}
	
	public void testCLExclusion_pr368046_2_usingaopxml() {
		runTest("classloader exclusion - 4");
	}

	public void testCLExclusion_pr368046_2_usingaopxmlReal() {
		runTest("classloader exclusion - 5");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc174Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc174.xml");
	}

}
