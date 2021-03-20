/* *******************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * ******************************************************************/
package org.aspectj.testing;

import org.aspectj.util.LangUtil;

/**
 * Makes sure tests are running on the right level of JDK.
 *
 * @author Alexander Kriegisch
 */
public abstract class XMLBasedAjcTestCaseForJava16Only extends XMLBasedAjcTestCase {

	@Override
	public void setUp() throws Exception {
		// Activate this block after upgrading to JDT Core Java 17
		/*
		throw new IllegalStateException(
			"These tests need a Java 16 level AspectJ compiler " +
				"(e.g. because they use version-specific preview features). " +
				"This compiler does not support preview features of a previous version anymore."
		);
		*/
		if (!LangUtil.is16VMOrGreater() || LangUtil.is17VMOrGreater()) {
			throw new IllegalStateException(
				"These tests should be run on Java 16 only " +
				"(e.g. because they use version-specific preview features)"
			);
		}
		super.setUp();
	}

}
