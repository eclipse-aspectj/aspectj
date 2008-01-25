/*
 * Created on 19-01-2005
 */
package org.aspectj.systemtest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.systemtest.ajc160.AllTestsAspectJ160;

public class AllTests16 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - JDK 1.6");
		//$JUnit-BEGIN$
		suite.addTest(AllTestsAspectJ160.suite()); // dont require a 1.6 JRE to run but checks 1.6 compiler behaviour
		suite.addTest(AllTests15.suite());
		//$JUnit-END$
		return suite;
	}
}
