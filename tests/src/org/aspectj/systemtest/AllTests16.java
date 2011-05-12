/*
 * Created on 19-01-2005
 */
package org.aspectj.systemtest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests16 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - JDK 1.6");
		// $JUnit-BEGIN$
		// These 16X tests do not require 1.6 jre to run
		// wibble
		// suite.addTest(AllTestsAspectJ160.suite());
		// suite.addTest(AllTestsAspectJ161.suite());
		// suite.addTest(AllTestsAspectJ162.suite());
		// suite.addTest(AllTestsAspectJ163.suite());
		// suite.addTest(AllTestsAspectJ164.suite());
		// suite.addTest(AllTestsAspectJ165.suite());
		// suite.addTest(AllTestsAspectJ166.suite());
		// suite.addTest(AllTestsAspectJ167.suite());
		// suite.addTest(AllTestsAspectJ169.suite());
		// suite.addTest(AllTestsAspectJ1610.suite());
		// suite.addTest(AllTestsAspectJ1611.suite());
		// suite.addTest(AllTestsAspectJ1612.suite());
		suite.addTest(AllTests15.suite());
		// $JUnit-END$
		return suite;
	}
}
