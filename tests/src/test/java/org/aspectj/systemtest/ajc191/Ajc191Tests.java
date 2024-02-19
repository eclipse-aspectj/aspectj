/*******************************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc191;

import junit.framework.Test;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc191Tests extends JavaVersionSpecificXMLBasedAjcTestCase {
	public Ajc191Tests() {
		super(10);
	}

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
