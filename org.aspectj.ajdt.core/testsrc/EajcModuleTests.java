/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


// default package


import junit.framework.*;
import junit.framework.Test;

public class EajcModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(EajcModuleTests.class.getName());
        suite.addTest(org.aspectj.ajdt.ajc.AjdtAjcTests.suite()); 
        suite.addTest(org.aspectj.ajdt.internal.compiler.batch.AjdtBatchTests.suite()); 
        suite.addTest(org.aspectj.ajdt.internal.core.builder.AjdtBuilderTests.suite()); 
        suite.addTestSuite(org.aspectj.tools.ajc.MainTest.class);
        return suite;
    }

    public EajcModuleTests(String name) { super(name); }

}  
