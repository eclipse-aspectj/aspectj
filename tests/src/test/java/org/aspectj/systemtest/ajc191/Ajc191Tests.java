/*******************************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc191;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava10OrLater;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc191Tests extends XMLBasedAjcTestCaseForJava10OrLater {

	public void testVar1() {
		runTest("var 1");
	}

	public void testVar2() {
		runTest("var 2");
	}

	public void testVarIncludesAspect3() {
		runTest("var 3");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc191Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
        return getClassResource("ajc191.xml");
	}

}
