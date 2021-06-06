/*******************************************************************************
 * Copyright (c) 2005-2019 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.loadtime;

import org.aspectj.weaver.loadtime.AjTest;
import org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptorTest;
import org.aspectj.weaver.loadtime.LoadtimeTest;
import org.aspectj.weaver.loadtime.WeavingContextTest;
import org.aspectj.weaver.loadtime.WeavingURLClassLoaderTest;
import org.aspectj.weaver.loadtime.test.DocumentParserTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Andy Clement
 * @author Alexandre Vasseur
 */
public class LoadtimeModuleTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(LoadtimeModuleTests.class.getName());
		suite.addTestSuite(DocumentParserTest.class);
		suite.addTestSuite(AjTest.class);
		suite.addTestSuite(ClassLoaderWeavingAdaptorTest.class);
		suite.addTestSuite(LoadtimeTest.class);
		suite.addTestSuite(WeavingContextTest.class);
		suite.addTestSuite(WeavingURLClassLoaderTest.class);
		return suite;
	}

	public static void main(String args[]) throws Throwable {
		TestRunner.run(suite());
	}

}
