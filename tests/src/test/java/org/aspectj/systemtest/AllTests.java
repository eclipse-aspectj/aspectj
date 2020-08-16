/* *******************************************************************
 * Copyright (c) 2004-2019 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * ******************************************************************/
package org.aspectj.systemtest;

import org.aspectj.systemtest.ajc10x.Ajc10xTests;
import org.aspectj.systemtest.ajc11.Ajc11Tests;
import org.aspectj.systemtest.ajc120.Ajc120Tests;
import org.aspectj.systemtest.ajc121.Ajc121Tests;
import org.aspectj.systemtest.aspectpath.AspectPathTests;
import org.aspectj.systemtest.base.BaseTests;
import org.aspectj.systemtest.design.DesignTests;
import org.aspectj.systemtest.incremental.IncrementalTests;
import org.aspectj.systemtest.incremental.model.IncrementalModelTests;
import org.aspectj.systemtest.incremental.tools.OutputLocationManagerTests;
import org.aspectj.systemtest.inpath.InPathTests;
import org.aspectj.systemtest.model.ModelTests;
import org.aspectj.systemtest.options.OptionsTests;
import org.aspectj.systemtest.pre10x.AjcPre10xTests;
import org.aspectj.systemtest.serialVerUID.SUIDTests;
import org.aspectj.systemtest.tracing.TracingTests;
import org.aspectj.systemtest.xlint.XLintTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Andy Clement
 * @author Adrian Colyer
 * @author IBM
 */
public class AllTests {

	public final static boolean VERBOSE = System.getProperty("aspectj.tests.verbose", "true").equals("true");

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - JDK 1.3");
		//$JUnit-BEGIN$
		suite.addTest(Ajc121Tests.suite());
		suite.addTest(Ajc120Tests.suite());
		suite.addTest(Ajc11Tests.suite());
		suite.addTest(Ajc10xTests.suite());
		suite.addTest(AspectPathTests.suite());
		suite.addTest(InPathTests.suite());
		suite.addTest(BaseTests.suite());
		suite.addTest(DesignTests.suite());
		suite.addTest(IncrementalTests.suite());
		suite.addTestSuite(OutputLocationManagerTests.class);
		suite.addTest(IncrementalModelTests.suite());
		//suite.addTest(KnownLimitationsTests.class);
		suite.addTest(OptionsTests.suite());
		suite.addTest(AjcPre10xTests.suite());
		//suite.addTest(PureJavaTests.class);
		suite.addTest(SUIDTests.suite());
		suite.addTest(XLintTests.suite());
		suite.addTest(TracingTests.suite());
		suite.addTest(ModelTests.suite());
		//$JUnit-END$

		return suite;
	}
}
