/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

// default package
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.aspectj.ajde.AjdeModuleTests;
import org.aspectj.ajde.core.AjdeCoreModuleTests;
import org.aspectj.ajdt.EajcModuleTests;
import org.aspectj.asm.AsmModuleTests;
import org.aspectj.bridge.BridgeModuleTests;
import org.aspectj.build.BuildModuleTests;
import org.aspectj.loadtime.Loadtime5ModuleTests;
import org.aspectj.loadtime.LoadtimeModuleTests;
import org.aspectj.runtime.Aspectj5rtModuleTests;
import org.aspectj.runtime.RuntimeModuleTests;
import org.aspectj.testing.TestingClientModuleTests;
import org.aspectj.testing.TestingDriversModuleTests;
import org.aspectj.testing.TestingModuleTests;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.testingutil.TestingUtilModuleTests;
import org.aspectj.tools.ajbrowser.AjbrowserModuleTests;
import org.aspectj.tools.ajdoc.AjdocModuleTests;
import org.aspectj.tools.ant.TaskdefsModuleTests;
import org.aspectj.util.LangUtil;
import org.aspectj.util.UtilModuleTests;
import org.aspectj.weaver.BcweaverModuleTests;
import org.aspectj.weaver.Weaver5ModuleTests;

public class AllTests extends TestCase {
    public static final boolean skipSupportModules = false;
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        suite.addTest(AjbrowserModuleTests.suite());
        suite.addTest(AjdeModuleTests.suite());
        suite.addTest(AjdeCoreModuleTests.suite());
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
            suite.addTest(Aspectj5rtModuleTests.suite());
            suite.addTest(Loadtime5ModuleTests.suite());
            suite.addTest(Weaver5ModuleTests.suite());
        } else {
            suite.addTest(TestUtil.skipTest("for 1.5"));
        } 
        return suite;
    }

    public AllTests(String name) {
        super(name);
    }

}
