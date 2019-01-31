/*
 * Created on 03-Aug-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.aspectj.systemtest;

import org.aspectj.systemtest.java14.Java14Tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AllTests14 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ System Test Suite - JDK 1.4");
		//$JUnit-BEGIN$
		suite.addTest(AllTests.suite());
		suite.addTest(Java14Tests.suite());
		//$JUnit-END$
		return suite;
	}
}
