/* *******************************************************************
 * Copyright (c) 2020 Contributors
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
 * @author Andy Clement
 */
public abstract class XMLBasedAjcTestCaseForJava14OrLater extends XMLBasedAjcTestCase {

	@Override
	public void runTest(String title) {
		if (!LangUtil.is14VMOrGreater()) {
			throw new IllegalStateException("These tests should be run on Java 14 or later");
		}
		super.runTest(title);
	}

}
