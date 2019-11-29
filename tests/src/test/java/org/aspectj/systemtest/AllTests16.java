/*
 * Created on 19-01-2005
 */
package org.aspectj.systemtest;

import org.aspectj.systemtest.ajc160.AllTestsAspectJ160;
import org.aspectj.systemtest.ajc161.AllTestsAspectJ161;
import org.aspectj.systemtest.ajc1610.AllTestsAspectJ1610;
import org.aspectj.systemtest.ajc1611.AllTestsAspectJ1611;
import org.aspectj.systemtest.ajc1612.AllTestsAspectJ1612;
import org.aspectj.systemtest.ajc162.AllTestsAspectJ162;
import org.aspectj.systemtest.ajc163.AllTestsAspectJ163;
import org.aspectj.systemtest.ajc164.AllTestsAspectJ164;
import org.aspectj.systemtest.ajc165.AllTestsAspectJ165;
import org.aspectj.systemtest.ajc166.AllTestsAspectJ166;
import org.aspectj.systemtest.ajc167.AllTestsAspectJ167;
import org.aspectj.systemtest.ajc169.AllTestsAspectJ169;
import org.aspectj.systemtest.incremental.tools.AnnotationProcessingTests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests16 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - JDK 1.6");
		// $JUnit-BEGIN$
		// These 16X tests do not require 1.6 jre to run
		suite.addTest(AllTestsAspectJ160.suite());
		suite.addTest(AllTestsAspectJ161.suite());
		suite.addTest(AllTestsAspectJ162.suite());
		suite.addTest(AllTestsAspectJ163.suite());
		suite.addTest(AllTestsAspectJ164.suite());
		suite.addTest(AllTestsAspectJ165.suite());
		suite.addTest(AllTestsAspectJ166.suite());
		suite.addTest(AllTestsAspectJ167.suite());
		suite.addTest(AllTestsAspectJ169.suite());
		suite.addTest(AllTestsAspectJ1610.suite());
		suite.addTest(AllTestsAspectJ1611.suite());
		suite.addTest(AllTestsAspectJ1612.suite());
		suite.addTestSuite(AnnotationProcessingTests.class);
		// $JUnit-END$
		return suite;
	}
}
