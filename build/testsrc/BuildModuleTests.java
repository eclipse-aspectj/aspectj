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
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


// default package

import org.aspectj.internal.tools.ant.taskdefs.Checklics;
import org.aspectj.internal.build.BuildModuleTest;
import org.aspectj.internal.build.ModulesTest;

import java.io.File;

import junit.framework.*;

public class BuildModuleTests extends TestCase {

    /** if true, then replace old headers with new first */
    private static final boolean replacing = false; // XXX never to enable again...
    
    /** replace commented out below - if any replace failed, halt all */
    private static boolean replaceFailed = false;
    
    private static final String BASE_DIR = "../";
    private static final String[] JDT_SOURCE_DIRS = new String[] {};
    // sources moved to shadow/ directory
//        {"antadapter", "batch", "codeassist", "compiler", 
//         "dom", "eval", "formatter", "model", "search" };

    public static Test suite() { 
        TestSuite suite = new TestSuite("Build module tests");
        suite.addTestSuite(BuildModuleTests.class); 
        suite.addTestSuite(BuildModuleTest.class); 
        suite.addTestSuite(ModulesTest.class); 
        return suite;
    }

    /** @return String tag of license if not default */
    public static String getLicense(String module) {
        if ("org.eclipse.jdt.core".equals(module)) {
            return Checklics.CPL_IBM_PARC_TAG;
        }
        return null;
    }

    public BuildModuleTests(String name) { super(name); }

    public void testLicense_ajbrowser() {
        checkLicense("ajbrowser");    
    }
    public void testLicense_ajde() {
        checkLicense("ajde");    
    }
    public void testLicense_asm() {
        checkLicense("asm");    
    }

    public void testLicense_bridge() {
        checkLicense("bridge");    
    }
    public void testLicense_build() {
        checkLicense("build");    
    }
    public void testLicense_org_aspectj_ajdt_core() {
        checkLicense("org.aspectj.ajdt.core");    
    }
    public void testLicense_org_eclipse_jdt_core() {
        final String mod = "org.eclipse.jdt.core";
        final String pre = BASE_DIR + mod + "/";
        for (int i = 0; i < JDT_SOURCE_DIRS.length; i++) {
            checkSourceDirectory(pre + JDT_SOURCE_DIRS[i], mod);    
		}
    }
    
    public void testLicense_runtime() {
        checkLicense("runtime");    
    }
    public void testLicense_taskdefs() {
        checkLicense("taskdefs");    
    }
    public void testLicense_testing() {
        checkLicense("testing");    
    }
    public void testLicense_testing_client() {
        checkLicense("testing-client");    
    }
    public void testLicense_testing_drivers() {
        checkLicense("testing-drivers");    
    }
    public void testLicense_testing_util() {
        checkLicense("testing-util");    
    }
    public void testLicense_util() {
        checkLicense("util");    
    }
    public void testLicense_weaver() {
        String module = "weaver";
        checkSourceDirectory("../" + module + "/src", module);
        checkSourceDirectory("../" + module + "/testsrc/org", module);
    }
    
    void checkLicense(String module) {
        checkSourceDirectory("../" + module + "/src", module);
        checkSourceDirectory("../" + module + "/testsrc", module);
    }
    
    void checkSourceDirectory(String path, String module) {
        File moduleDir = new File(path);
        final String label = "source dir " + moduleDir + " (module " + module + ")";
        assertTrue(label,  (moduleDir.exists() && moduleDir.isDirectory()));
        String license = getLicense(module);
//        if (replacing) {
//            if (replacing && true) {
//                throw new Error("replacing done - code left for other replaces");
//            }
//            assertTrue("aborting - replace failed", !replaceFailed);
//            // do the replace
//            int fails = Checklics.runDirect(moduleDir.getPath(), "replace-headers");
//            replaceFailed = (0 != fails);
//            assertTrue(!replaceFailed);
//            license = Checklics.CPL_IBM_PARC_XEROX_TAG;
//        }
        int fails = Checklics.runDirect(moduleDir.getPath(), license);
        if (0 != fails) {
            if (replacing) {
                BuildModuleTests.replaceFailed = true;
            }
            assertTrue(label + " fails", !BuildModuleTests.replaceFailed);
        }
    }

}  
