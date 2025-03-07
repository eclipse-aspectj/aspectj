/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1923;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.tools.ant.taskdefs.AjcTask;
import org.aspectj.util.LangUtil;

//AspectJ_JDK_Update
//- Copy 'ajc*' package with all classes to a new package, incrementing the version number in the package
//- Rename all classes, incrementing version numbers
//- Add this class to the suite in class AllTests19
//- Increment version numbers in strings, method calls and constants to the appropriate values, creating necessary
// methods and constants classes providing them, if they do not exist yet
//- Also increment references to 'ajc*.xml' and 'sanity-tests-*.xml' test definition, copying the previous
// tests/src/test/resources/org/aspectj/systemtest/ajc* directory, incrementing all names and adjusting the XML
// file contents appropriately
//- Search for other 'AspectJ_JDK_Update' hints in the repository, also performing the necessary to-dos there
//- Remove this comment from the previous class version after copying this one

/**
 * @author Alexander Kriegisch
 * @author Andy Clement
 */
public class AllTestsAspectJ1923 {

	private static final int JAVA_VERSION = 23;

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.23 tests");
		suite.addTest(Bugs1923Tests.suite());
		if (LangUtil.isVMGreaterOrEqual(JAVA_VERSION)) {
			suite.addTest(SanityTestsJava23.suite());
			suite.addTest(Ajc1923TestsJava.suite());
		}
		// Do not run tests using a previous compiler's preview features anymore. They
		// would all fail.
		if (AjcTask.JAVA_VERSION_MAX == JAVA_VERSION) {
			if (LangUtil.isVMGreaterOrEqual(JAVA_VERSION) && LangUtil.isVMLessOrEqual(JAVA_VERSION)) {
				suite.addTest(Java23PreviewFeaturesTests.suite());
			}
		}
		return suite;
	}

}
