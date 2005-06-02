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

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        suite.addTest(AjbrowserModuleTests.suite());
        suite.addTest(AjdeModuleTests.suite());
        suite.addTest(AsmModuleTests.suite());
        suite.addTest(BridgeModuleTests.suite());
        suite.addTest(EajcModuleTests.suite());
        suite.addTest(RuntimeModuleTests.suite());
        suite.addTest(TaskdefsModuleTests.suite());
        suite.addTest(TestingModuleTests.suite());
        suite.addTest(TestingDriversModuleTests.suite());
        suite.addTest(UtilModuleTests.suite());
        suite.addTest(BcweaverModuleTests.suite());
        if (LangUtil.is15VMOrGreater()) {
            suite.addTest(Aspectj5rtModuleTests.suite());
            suite.addTest(Loadtime5ModuleTests.suite());
        } else {
            System.err.println("Warning: not running 1.5 tests");
        }
        return suite;
    }

    public AllTests(String name) {
        super(name);
    }

}
