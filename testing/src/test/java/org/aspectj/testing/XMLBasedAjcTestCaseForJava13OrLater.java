/* *******************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Andy Clement
 * ******************************************************************/
package org.aspectj.testing;

import org.aspectj.util.LangUtil;

/**
 * Makes sure tests are running on the right level of JDK.
 *
 * @author Andy Clement
 */
public abstract class XMLBasedAjcTestCaseForJava13OrLater extends XMLBasedAjcTestCase {

	@Override
	public void setUp() throws Exception {
		if (!LangUtil.is13VMOrGreater())
			throw new IllegalStateException("These tests should be run on Java 13 or later");
		super.setUp();
	}

}
