package org.aspectj.build;
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


// default package

import org.aspectj.internal.tools.ant.taskdefs.Checklics;
import org.aspectj.internal.tools.build.Builder;
import org.aspectj.internal.tools.build.Util;
import org.aspectj.internal.tools.build.UtilsTest;
import org.aspectj.internal.build.BuildModuleTest;
import org.aspectj.internal.build.ModulesTest;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.*;

/**
 * Master suite for build module
 * and test of all source directories for correct licenses and known file types.
 */
public class BuildModuleTests extends TestCase {

    /** if true, then replace old headers with new first */
    private static final boolean replacing = false; // XXX never to enable again...
    
    /** replace commented out below - if any replace failed, halt all */
    private static boolean replaceFailed = false;
    
    private static final String BASE_DIR = ".." + File.separator;
    private static final String[] JDT_SOURCE_DIRS = new String[] {};

    public static Test suite() { 
        TestSuite suite = new TestSuite("Build module tests");
        suite.addTestSuite(BuildModuleTests.class); 
        suite.addTestSuite(BuildModuleTest.class); 
        suite.addTestSuite(ModulesTest.class); 
        suite.addTestSuite(UtilsTest.class); 
        return suite;
    }

    /** @return String tag of license if not default */
    public static String getLicense(String module) {
        return null; // use permissive default
    }

    final static List SOURCE_NAMES = Collections.unmodifiableList(
            Arrays.asList(new String[]{"src", "testsrc", "java5-src", "java5-testsrc", "aspectj-src"}));

    /**
     * @param moduleDir
     * @return
     */
    private static File[] findSourceRoots(File moduleDir) {
        ArrayList result = new ArrayList();
        for (Iterator iter = SOURCE_NAMES.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            File srcDir = new File(moduleDir, name);
            if (srcDir.canRead() && srcDir.isDirectory()) {
                result.add(srcDir);
            }
        }
        return (File[]) result.toArray(new File[0]);
    }

    public BuildModuleTests(String name) { super(name); }

    public void testSuffixList() {
        if (!UnknownFileCheck.STATIC_ERRORS.isEmpty()) {
            fail("" + UnknownFileCheck.STATIC_ERRORS);
        }
    }
    public void testLicense_ajbrowser() {
        checkLicense("ajbrowser");    
    }
    public void testLicense_ajde() {
        checkLicense("ajde");    
    }
    public void testLicense_aspectj5rt() {
        checkLicense("aspectj5rt");    
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
    public void testLicense_org_aspectj_lib() {
        checkLicense("org.aspectj.lib");    
    }
    public void testLicense_org_eclipse_jdt_core() {
        final String mod = "org.eclipse.jdt.core";
        final String pre = BASE_DIR + mod + File.separator;
        for (int i = 0; i < JDT_SOURCE_DIRS.length; i++) {
            checkSourceDirectory(new File(pre + JDT_SOURCE_DIRS[i]), mod);    
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
        // skip (testdata) packages fluffy, reflect
        checkSourceDirectory(new File(Util.path(new String[] {"..", module, "src"})), module);
        checkSourceDirectory(new File(Util.path(new String[] {"..", module, "testsrc", "org"})), module);
    }
    
    public void testLicense_ajdoc() {
        checkLicense("ajdoc");
    }
    
    public void testLicense_loadtime() {
        checkLicense("loadtime");
    }
    
    public void testLicense_loadtime5() {
        checkLicense("loadtime5");
    }
    
    public void testLicense_weaver5() {
        checkLicense("weaver5");
    }

    void checkLicense(String module) {
        File moduleDir = new File(Util.path("..", module));
        File[] srcDirs = findSourceRoots(moduleDir);
        for (int i = 0; i < srcDirs.length; i++) {
            checkSourceDirectory(srcDirs[i], module);
        }
    }
    
    void checkSourceDirectory(File srcDir, String module) {
        final String label = "source dir " + srcDir + " (module " + module + ")";
        assertTrue(label,  (srcDir.exists() && srcDir.isDirectory()));
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
        int fails = Checklics.runDirect(srcDir.getPath(), license, true);
        if (0 != fails) {
            if (replacing) {
                BuildModuleTests.replaceFailed = true;
            }
            assertTrue(label + " fails", !BuildModuleTests.replaceFailed);
        }
        
        // separate check to verify all file types (suffixes) are known
        if (!"testsrc".equals(srcDir.getName())) {
            ArrayList unknownFiles = new ArrayList();
            UnknownFileCheck.SINGLETON.unknownFiles(srcDir, unknownFiles);
            if (!unknownFiles.isEmpty()) {
                String s = "unknown files (see readme-build-module.html to "
                    + "update Builder.properties resource patterns): ";
                fail(s + unknownFiles);
            }
        }
    }
    /**
     * Check tree for files not managed by the build system
     * (either source files or managed as resources).  
     * This should pick up situations where new kinds of resources are added
     * to the tree without updating the build script patterns to pick them
     * up.
     * @see Builder#BINARY_SOURCE_PATTERN  
     * @see Builder#RESOURCE_PATTERN
     * @see org.aspectj.util.FileUtil#SOURCE_SUFFIXES
     */
    static class UnknownFileCheck implements FileFilter {
        private static final UnknownFileCheck SINGLETON = new UnknownFileCheck();
        private static final ArrayList STATIC_ERRORS = new ArrayList();
        // Builder.BINARY_SOURCE_PATTERN and Builder.RESOURCE_PATTERN
        public static final List KNOWN_SUFFIXES;

        static {
            List suffixes = new ArrayList();
            // sources from org.aspectj.util.FileUtil.SOURCE_SUFFIXES
            suffixes.add(".aj");
            suffixes.add(".java");
            
            // just because we know...
            suffixes.add(".html");

            // others from Builder
            final String input = Builder.BINARY_SOURCE_PATTERN 
                + "," + Builder.RESOURCE_PATTERN;
            StringTokenizer st = new StringTokenizer(input, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (0 == token.length()) {
                    continue;
                }
                if (token.startsWith("**/*.")) {
                    token = token.substring(4);
                } else if (token.startsWith("*.")) {
                    token = token.substring(1);
                } else {
                    String s = input + " at \"" + token + "\"";
                    STATIC_ERRORS.add("unable to read pattern: " + s);
                }
                suffixes.add(token);
            }            
            KNOWN_SUFFIXES = Collections.unmodifiableList(suffixes);
        }
        
        private UnknownFileCheck() {
            
        }
        /**
         * Return true if input File file is a valid path to a directory 
         * or to a file
         * which is not hidden (starts with .)
         * and does not have a known suffix.
         * Caller is responsible for pruning CVS directories
         * @return true iff unknown or a directory
         */
        public boolean accept(File file) {
            if (null == file) {
                return false;
            }            
            if (file.isDirectory()) {
                return file.canRead();
            }

            String name = file.getName();
            if ("CVS".equals(name) || name.startsWith(".")) {
                return false;
            }
            // to do not accepting uppercase suffixes...
            for (Iterator iter = KNOWN_SUFFIXES.iterator(); iter.hasNext();) {
                String suffix = (String) iter.next();
                if (name.endsWith(suffix)) {
                    return false;
                }
            }
            return true;
            
        }
        void unknownFiles(File dir, ArrayList results) {
            File[] files = dir.listFiles(this);
            for (int j = 0; j < files.length; j++) {
                File file = files[j];
                if (file.isDirectory()) {
                    String name = file.getName();
                    if (!("CVS".equals(name))) {
                        unknownFiles(file, results);
                    }
                } else {
                    results.add(file);
                }
            }
        }
        
    }
}  

