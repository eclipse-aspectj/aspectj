/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1921;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.aspectj.util.LangUtil;

// AspectJ_JDK_Update
//  - Copy 'ajc*' package with all classes to a new package, incrementing the version number in the package
//  - Rename all classes, incrementing version numbers
//  - Add this class to the suite in class AllTests19
//  - Increment version numbers in strings, method calls and constants to the appropriate values, creating necessary
//    methods and constants classes providing them, if they do not exist yet
//  - Also increment references to 'ajc*.xml' and 'sanity-tests-*.xml' test definition, copying the previous
//    tests/src/test/resources/org/aspectj/systemtest/ajc* directory, incrementing all names and adjusting the XML
//    file contents appropriately
//  - Search for other 'AspectJ_JDK_Update' hints in the repository, also performing the necessary to-dos there
//  - Remove this comment from the previous class version after copying this one

/**
 * @author Alexander Kriegisch
 */
public class AllTestsAspectJ1921 {

	public static Test suite() {
		TestSuite suite = new TestSuite("AspectJ 1.9.21 tests");
		suite.addTest(Bugs1921Tests.suite());
		if (LangUtil.isVMGreaterOrEqual(21)) {
			suite.addTest(SanityTestsJava21.suite());
			suite.addTest(Ajc1921TestsJava.suite());
		}
		// AspectJ_JDK_Update
		// Do not run tests using a previous compiler's preview features anymore. They would all fail.
		// TODO: Comment out the following block when upgrading JDT Core to Java 22
		if (LangUtil.isVMGreaterOrEqual(21) && !LangUtil.isVMGreaterOrEqual(22)) {
			suite.addTest(Java21PreviewFeaturesTests.suite());
		}
		return suite;
	}
}
