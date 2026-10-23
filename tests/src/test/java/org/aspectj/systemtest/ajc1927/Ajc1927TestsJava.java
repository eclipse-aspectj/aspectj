/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.systemtest.ajc1927;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1927TestsJava extends JavaVersionSpecificXMLBasedAjcTestCase {

	@SuppressWarnings("unused")
	private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(27);

	public Ajc1927TestsJava() {
		super(27);
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1927TestsJava.class);
	}

	public void testJep530PrimitivePatternsSwitch2() {
		runTest("primitive types patterns - switch - with advice");
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1927.xml");
	}

}
