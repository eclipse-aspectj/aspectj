/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AllTestsJava5_binaryWeaving {

	public static Test suite() {
		TestSuite suite = new TestSuite("Java5 - binary weaving");
		//$JUnit-BEGIN$
		suite.addTest(Ajc150Tests.suite());
		suite.addTest(AccBridgeMethods.suite());
		suite.addTestSuite(CovarianceTests.class);
		suite.addTestSuite(Enums.class);
		suite.addTestSuite(Annotations.class);
		suite.addTestSuite(AnnotationPointcutsTests.class);
		suite.addTestSuite(VarargsTests.class);
		suite.addTestSuite(AnnotationRuntimeTests.class);
		//$JUnit-END$
		return suite;
	}
}
