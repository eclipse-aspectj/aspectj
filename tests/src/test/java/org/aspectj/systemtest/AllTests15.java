/*
 * Created on 19-01-2005
 */
package org.aspectj.systemtest;

import org.aspectj.systemtest.ajc150.AllTestsAspectJ150;
import org.aspectj.systemtest.ajc150.ataspectj.AtAjAnnotationGenTests;
import org.aspectj.systemtest.ajc151.AllTestsAspectJ151;
import org.aspectj.systemtest.ajc152.AllTestsAspectJ152;
import org.aspectj.systemtest.ajc153.AllTestsAspectJ153;
import org.aspectj.systemtest.ajc154.AllTestsAspectJ154;
import org.aspectj.systemtest.incremental.tools.IncrementalCompilationTests;
import org.aspectj.systemtest.incremental.tools.IncrementalOutputLocationManagerTests;
import org.aspectj.systemtest.incremental.tools.IncrementalPerformanceTests;
import org.aspectj.systemtest.incremental.tools.MoreOutputLocationManagerTests;
import org.aspectj.systemtest.incremental.tools.MultiProjectIncrementalTests;
import org.aspectj.systemtest.model.Model5Tests;
import org.aspectj.systemtest.xlint.XLint5Tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests15 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - JDK 1.5");
		// $JUnit-BEGIN$
		suite.addTest(AllTests14.suite());
		suite.addTest(AllTestsAspectJ150.suite());
		suite.addTest(AllTestsAspectJ151.suite());
		suite.addTest(AllTestsAspectJ152.suite());
		suite.addTest(AllTestsAspectJ153.suite());
		suite.addTest(AllTestsAspectJ154.suite());
		suite.addTest(AtAjAnnotationGenTests.suite());
		suite.addTest(Model5Tests.suite());
		/*
		 * FIXME maw Many of these tests do not depend on Java 5 but they cannot be executed in Eclipse with 1.3 because of XML
		 * issues and are excluded on the build machine so moving them here loses nothing for the moment.
		 */
		suite.addTestSuite(MultiProjectIncrementalTests.class);
		suite.addTestSuite(IncrementalCompilationTests.class);
		suite.addTestSuite(IncrementalPerformanceTests.class);
		suite.addTestSuite(MoreOutputLocationManagerTests.class);
		suite.addTestSuite(IncrementalOutputLocationManagerTests.class);
		suite.addTest(XLint5Tests.suite());
		// $JUnit-END$
		return suite;
	}
}
