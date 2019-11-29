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
package org.aspectj.systemtest.ajc1610;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc1610Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	
	public void testNPE_pr363962() {
		runTest("pr363962");
	}
	
	public void testOffset0_bug324932() {
		runTest("pr324932");
	}

	public void testOffset0_bug324932_2() {
		runTest("pr324932 - 2");
	}

	public void testAbstractAspectDeclareParents_322446() {
		runTest("declare parents abstract aspect");
	}

	public void testAbstractAspectAndDeclares_322272_2() {
		runTest("abstract aspects and declares - 2");
	}

	public void testAbstractAspectAndDeclares_322272() {
		runTest("abstract aspects and declares");
	}

	// Interesting new behaviour on AspectJ 1.6.9 - probably due to initial inner type changes.
	// Looks a real error (creating two annotations the same on a type is a bad thing)
	// public void testDuplicateAnnotations() {
	// runTest("duplicate annotation");
	// }

	public void testLoadingOldCode_319431() {
		runTest("loading old code");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1610Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1610.xml");
	}

}