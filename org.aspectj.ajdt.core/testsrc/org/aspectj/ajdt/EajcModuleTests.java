package org.aspectj.ajdt;
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
 *     PARC     initial implementation 
 * ******************************************************************/


// default package


import org.aspectj.testing.util.TestUtil;

import junit.framework.*;

public class EajcModuleTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(EajcModuleTests.class.getName());
        suite.addTest(org.aspectj.ajdt.ajc.AjdtAjcTests.suite()); 
        suite.addTest(org.aspectj.ajdt.internal.compiler.batch.AjdtBatchTests.suite()); 
        suite.addTest(org.aspectj.ajdt.internal.core.builder.AjdtBuilderTests.suite()); 
        suite.addTest(org.aspectj.tools.ajc.AjcTests.suite());

        /* FIXME maw move these Java 5 dependent tests to a separate project */
        if (TestUtil.is15VMOrGreater()) {
            TestUtil.loadTestsReflectively(suite, "Eajc515ModuleTests", true);
        } else {
            suite.addTest(TestUtil.testNamed("Eajc515ModuleTests require 1.5"));
        }
        return suite;
    }

    public EajcModuleTests(String name) { super(name); }

}  
