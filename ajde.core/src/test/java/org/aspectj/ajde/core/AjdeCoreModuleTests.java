/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.ajde.core;

import org.aspectj.ajde.core.tests.AjConfigTest;
import org.aspectj.ajde.core.tests.BuildCancellingTest;
import org.aspectj.ajde.core.tests.CompilerMessagesTest;
import org.aspectj.ajde.core.tests.DuplicateManifestTest;
import org.aspectj.ajde.core.tests.InpathTest;
import org.aspectj.ajde.core.tests.JarManifestTest;
import org.aspectj.ajde.core.tests.OutxmlTest;
import org.aspectj.ajde.core.tests.ResourceCopyTest;
import org.aspectj.ajde.core.tests.ReweavableTest;
import org.aspectj.ajde.core.tests.ShowWeaveMessagesTest;
import org.aspectj.ajde.core.tests.model.AsmDeclarationsTest;
import org.aspectj.ajde.core.tests.model.AsmRelationshipsTest;
import org.aspectj.ajde.core.tests.model.SavedModelConsistencyTest;
import org.aspectj.ajde.core.tests.model.StructureModelTest;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AjdeCoreModuleTests extends TestCase {

	static boolean verbose = System.getProperty("aspectj.tests.verbose", "false").equalsIgnoreCase("true");

	public static TestSuite suite() {
		TestSuite suite = new TestSuite(AjdeCoreModuleTests.class.getName());
		suite.addTestSuite(ShowWeaveMessagesTest.class);
		suite.addTestSuite(DuplicateManifestTest.class);
		suite.addTestSuite(StructureModelTest.class);
		suite.addTestSuite(CompilerMessagesTest.class);
		suite.addTestSuite(AsmDeclarationsTest.class);
		suite.addTestSuite(AsmRelationshipsTest.class);
		suite.addTestSuite(InpathTest.class);
		suite.addTestSuite(ReweavableTest.class);
		suite.addTestSuite(ResourceCopyTest.class);
		suite.addTestSuite(SavedModelConsistencyTest.class);
		suite.addTestSuite(BuildCancellingTest.class);
		suite.addTestSuite(JarManifestTest.class);
		suite.addTestSuite(OutxmlTest.class);
		suite.addTestSuite(AjConfigTest.class);
		return suite;
	}


	public AjdeCoreModuleTests(String name) {
		super(name);
	}

}
