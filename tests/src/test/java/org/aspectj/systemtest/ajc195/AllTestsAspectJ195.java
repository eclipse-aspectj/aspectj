/*******************************************************************************
 * Copyright (c) 2018-2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc195;

import org.aspectj.util.LangUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsAspectJ195 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.5 tests");
		suite.addTest(Ajc195Tests.suite());
		if (LangUtil.is13VMOrGreater()) {
			suite.addTest(SanityTestsJava13.suite());
		}
		return suite;
	}
}
