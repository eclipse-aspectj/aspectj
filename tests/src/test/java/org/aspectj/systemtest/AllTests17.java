/*
 * Created on 19-01-2005
 */
package org.aspectj.systemtest;

import org.aspectj.systemtest.ajc170.AllTestsAspectJ170;
import org.aspectj.systemtest.ajc171.AllTestsAspectJ171;
import org.aspectj.systemtest.ajc172.AllTestsAspectJ172;
import org.aspectj.systemtest.ajc173.AllTestsAspectJ173;
import org.aspectj.systemtest.ajc174.AllTestsAspectJ174;
import org.aspectj.systemtest.ajc175.AllTestsAspectJ175;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests17 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - 1.7");
		// $JUnit-BEGIN$
		suite.addTest(AllTestsAspectJ175.suite()); 
		suite.addTest(AllTestsAspectJ174.suite()); 
		suite.addTest(AllTestsAspectJ173.suite()); 
		suite.addTest(AllTestsAspectJ172.suite());
		suite.addTest(AllTestsAspectJ171.suite());
		suite.addTest(AllTestsAspectJ170.suite());
		suite.addTest(AllTests16.suite());
		suite.addTest(AllTests15.suite());
		// $JUnit-END$
		return suite;
	}
}
