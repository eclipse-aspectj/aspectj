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

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AjdeCoreModuleTests extends TestCase {

	public static TestSuite suite() {
		TestSuite suite = new TestSuite(AjdeCoreModuleTests.class.getName());
		suite.addTest(org.aspectj.ajde.core.AjdeCoreTests.suite());
		return suite;
	}

	public AjdeCoreModuleTests(String name) {
		super(name);
	}

}
