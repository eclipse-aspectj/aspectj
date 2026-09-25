/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1926;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1926TestsJava extends JavaVersionSpecificXMLBasedAjcTestCase {

	@SuppressWarnings("unused")
	private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(26);

	public Ajc1926TestsJava() {
		super(26);
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1926TestsJava.class);
	}

	public void testJep530PrimitivePatternsSwitch2() {
		runTest("primitive types patterns - switch - with advice");
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1926.xml");
	}

}
