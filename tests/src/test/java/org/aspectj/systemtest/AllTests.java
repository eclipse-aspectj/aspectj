/*
 * Created on 03-Aug-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.aspectj.systemtest;

import junit.framework.Test;
import junit.framework.TestSuite;

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

/**
 * @author colyer
 */
public class AllTests {

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
