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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.ajde.core.tests.AjConfigTests;
import org.aspectj.ajde.core.tests.BuildCancellingTests;
import org.aspectj.ajde.core.tests.CompilerMessagesTests;
import org.aspectj.ajde.core.tests.DuplicateManifestTests;
import org.aspectj.ajde.core.tests.InpathTests;
import org.aspectj.ajde.core.tests.JarManifestTests;
import org.aspectj.ajde.core.tests.OutxmlTests;
import org.aspectj.ajde.core.tests.ResourceCopyTests;
import org.aspectj.ajde.core.tests.ReweavableTests;
import org.aspectj.ajde.core.tests.ShowWeaveMessagesTests;
import org.aspectj.ajde.core.tests.model.AsmDeclarationsTests;
import org.aspectj.ajde.core.tests.model.AsmRelationshipsTests;
import org.aspectj.ajde.core.tests.model.SavedModelConsistencyTests;
import org.aspectj.ajde.core.tests.model.StructureModelTests;

public class AjdeCoreTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(AjdeCoreTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(ShowWeaveMessagesTests.class);
		suite.addTestSuite(DuplicateManifestTests.class);
		suite.addTestSuite(StructureModelTests.class);
		suite.addTestSuite(CompilerMessagesTests.class);
		suite.addTestSuite(AsmDeclarationsTests.class);
		suite.addTestSuite(AsmRelationshipsTests.class);
		suite.addTestSuite(InpathTests.class);
		suite.addTestSuite(ReweavableTests.class);
		suite.addTestSuite(ResourceCopyTests.class);
		suite.addTestSuite(SavedModelConsistencyTests. class);
		suite.addTestSuite(BuildCancellingTests.class);
		suite.addTestSuite(JarManifestTests.class);
		suite.addTestSuite(OutxmlTests.class);
		suite.addTestSuite(AjConfigTests.class);

		// $JUnit-END$
		return suite;
	}

	public AjdeCoreTests(String name) {
		super(name);
	}

}
