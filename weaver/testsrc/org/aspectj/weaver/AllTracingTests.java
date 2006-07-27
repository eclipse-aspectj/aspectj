/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTracingTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTracingTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TraceFactoryTest.class);
		suite.addTestSuite(DefaultTraceFactoryTest.class);
		suite.addTestSuite(DefaultTraceTest.class);
		suite.addTestSuite(CommonsTraceFactoryTest.class);
		suite.addTestSuite(CommonsTraceTest.class);
		//$JUnit-END$
		return suite;
	}

}
