/*******************************************************************************
 * Copyright (c) 2016 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc190;

import java.io.File;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc190Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testVarious_SettingFinalStatic() {
		runTest("setting static final outside clinit");
	}

	public void testAnnotMethodHasMember_pr156962_1() { // From similar in Ajc153Tests
		runTest("Test Annot Method Has Member 1");
	}

	public void testAnnotMethodHasMember_pr156962_2() { // From similar in Ajc153Tests
		runTest("Test Annot Method Has Member 1");
	}
	
	public void testFunnySignature() {
		runTest("funny signature with method reference");
	}
	
	// Weave a module with code that isn't in a module
	public void testWeaveModule() throws Exception {
		runTest("weave module");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc190Tests.class);
	}

	@Override
	protected File getSpecFile() {
        return getClassResource("ajc190.xml");
	}

}
