/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import junit.framework.*;

public class BcelTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(BcelTests.class.getName());
        // abstract 
        //suite.addTestSuite(WeaveTestCase.class); 
        //$JUnit-BEGIN$
        suite.addTestSuite(AfterReturningWeaveTestCase.class); 
        suite.addTestSuite(AfterThrowingWeaveTestCase.class); 
        suite.addTestSuite(AfterWeaveTestCase.class); 
        suite.addTestSuite(ArgsWeaveTestCase.class); 
        suite.addTestSuite(AroundArgsWeaveTestCase.class); 
        suite.addTestSuite(AroundWeaveTestCase.class); 
        suite.addTestSuite(BeforeWeaveTestCase.class); 
        suite.addTestSuite(CheckerTestCase.class); 
        suite.addTestSuite(FieldSetTestCase.class); 
        suite.addTestSuite(HierarchyDependsTestCase.class); 
        suite.addTestSuite(IdWeaveTestCase.class); 
        suite.addTestSuite(MoveInstructionsWeaveTestCase.class); 
        suite.addTestSuite(NonstaticWeaveTestCase.class); 
        suite.addTestSuite(PatternWeaveTestCase.class); 
        suite.addTestSuite(PointcutResidueTestCase.class); 
        suite.addTestSuite(TraceJarWeaveTestCase.class); 
        suite.addTestSuite(TjpWeaveTestCase.class); 
        suite.addTestSuite(UtilityTestCase.class); 
        suite.addTestSuite(WeaveOrderTestCase.class); 
        suite.addTestSuite(WorldTestCase.class);  
        suite.addTestSuite(ZipTestCase.class); 
        //$JUnit-END$
        return suite;
    }

    public BcelTests(String name) { super(name); }

}  
