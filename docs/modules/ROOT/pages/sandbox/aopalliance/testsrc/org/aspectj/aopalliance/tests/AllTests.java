package org.aspectj.aopalliance.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.aspectj.aopalliance.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(JoinPointClosureTest.class);
		suite.addTestSuite(InvocationJoinPointClosureTest.class);
		suite.addTestSuite(MethodInvocationClosureTest.class);
		suite.addTestSuite(ConstructorInvocationClosureTest.class);
		suite.addTestSuite(AOPAllianceAdapterTest.class);
		//$JUnit-END$
		return suite;
	}
}
