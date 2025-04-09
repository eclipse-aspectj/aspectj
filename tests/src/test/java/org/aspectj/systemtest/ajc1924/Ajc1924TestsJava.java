/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1924;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc1924TestsJava extends JavaVersionSpecificXMLBasedAjcTestCase {

	private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(24);

	public Ajc1924TestsJava() {
		super(24);
	}

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc1924TestsJava.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc1924.xml");
	}

	public void testJep455PrimitivePatternsSwitch2() {
		runTest("primitive types patterns - switch - with advice");
	}

}
