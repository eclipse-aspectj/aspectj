/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UtilModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(UtilModuleTests.class.getName());
		suite.addTestSuite(FileUtilTest.class);
		suite.addTestSuite(LangUtilTest.class);
		suite.addTestSuite(GenericSignatureParserTest.class);
        return suite;
    }

    public UtilModuleTests(String name) { super(name); }

}  
