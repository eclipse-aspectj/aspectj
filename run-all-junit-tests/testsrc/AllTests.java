/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

// default package
import org.aspectj.util.LangUtil;
import org.aspectj.testing.util.TestUtil;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {
    public static final boolean skipSupportModules = false;
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        suite.addTest(AjbrowserModuleTests.suite());
        suite.addTest(AjdeModuleTests.suite());
        suite.addTest(AjdocModuleTests.suite());
        suite.addTest(AsmModuleTests.suite());
        suite.addTest(BridgeModuleTests.suite());
        suite.addTest(LoadtimeModuleTests.suite());
        suite.addTest(EajcModuleTests.suite());
        //suite.addTest(LibModuleTests.suite());
        suite.addTest(RuntimeModuleTests.suite());
        suite.addTest(TaskdefsModuleTests.suite());
        if (!skipSupportModules) {
            suite.addTest(BuildModuleTests.suite());
            suite.addTest(TestingModuleTests.suite());
            suite.addTest(TestingClientModuleTests.suite());
            suite.addTest(TestingDriversModuleTests.suite());
            suite.addTest(TestingUtilModuleTests.suite());
        }
        suite.addTest(UtilModuleTests.suite());
        suite.addTest(BcweaverModuleTests.suite());
        if (LangUtil.is15VMOrGreater()) {
            // these only require 1.3, but in Eclipse they are built 
            // with 1.5, i.e., wrong class version to load under 1.3
            // so the class name can only be used reflectively
            TestUtil.loadTestsReflectively(suite, "Aspectj5rtModuleTests", false);
            TestUtil.loadTestsReflectively(suite, "Loadtime515ModuleTests", false);
        } else {
            suite.addTest(TestUtil.skipTest("for 1.5"));
        } 
        return suite;
    }

    public AllTests(String name) {
        super(name);
    }

}
