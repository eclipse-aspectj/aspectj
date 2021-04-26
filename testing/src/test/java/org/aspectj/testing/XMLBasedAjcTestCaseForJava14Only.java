/* *******************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * ******************************************************************/
package org.aspectj.testing;

/**
 * Makes sure tests are running on the right level of JDK.
 *
 * @author Alexander Kriegisch
 */
public abstract class XMLBasedAjcTestCaseForJava14Only extends XMLBasedAjcTestCase {

	@Override
	public void setUp() throws Exception {
		throw new IllegalStateException(
			"These tests need a Java 14 level AspectJ compiler " +
				"(e.g. because they use version-specific preview features). " +
				"This compiler does not support preview features of a previous version anymore."
		);
		/*
		if (!LangUtil.is14VMOrGreater() || LangUtil.is15VMOrGreater()) {
			throw new IllegalStateException(
				"These tests should be run on Java 14 only " +
				"(e.g. because they use version-specific preview features)"
			);
		}
		super.setUp();
		*/
	}

}
