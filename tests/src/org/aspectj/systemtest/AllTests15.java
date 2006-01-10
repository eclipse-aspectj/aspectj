/*
 * Created on 19-01-2005
 */
package org.aspectj.systemtest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.aspectj.systemtest.ajc150.AllTestsAspectJ150;
import org.aspectj.systemtest.ajc150.ataspectj.AtAjAnnotationGenTests;
import org.aspectj.systemtest.ajc151.AllTestsAspectJ151;

public class AllTests15 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - JDK 1.5");
		//$JUnit-BEGIN$
		suite.addTest(AllTests14.suite());
		suite.addTest(AllTestsAspectJ150.suite());
		suite.addTest(AllTestsAspectJ151.suite());
		suite.addTest(AtAjAnnotationGenTests.suite());
		//$JUnit-END$
		return suite;
	}
}
