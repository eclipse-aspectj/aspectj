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


import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;

/**
 * Tests the AJC ant task.
 */
public class AjcTaskTester extends AntTaskTester {

    protected final static String TEST_CLASSES = "test-classes";
    protected final static String TEST_SOURCES = "../src";
    protected File buildDir = null;

    /**
     * We use <code>"tests/ant/etc/ajc.xml"</code>.
     */
    public String getAntFile() {
        return "tests/ant/etc/ajc.xml";
    }

    /**
     * Put {@link #TEST_CLASSES} and {@link #TEST_SOURCES}
     * into the user properties.
     */
    protected Map getUserProperties() {
        Map userProps = new HashMap();
        userProps.put("ant.test.classes", TEST_CLASSES);
        userProps.put("ant.test.sources", TEST_SOURCES);        
        return userProps;
    }

    ////// Begin tests //////////////////////////////////////////////

    public void test1() { wantClasses("One"); }
    public void test2() { wantClasses("One,Two"); }
    public void test3() { wantClasses("One,Two,Three"); }
    public void test4() { wantClasses("One"); }
    public void test4b() { wantClasses("One"); }
    public void test5() { wantClasses("One,Two"); }
    public void test5b() { wantClasses("One,Two"); }
    public void test6() { wantClasses("One,Two,Three"); }
    public void test6b() { wantClasses("One,Two,Three"); }
    public void test8() { wantClasses("One"); }
    public void test9() { wantClasses("One"); }
    public void test10() { wantClasses("One"); }
    public void test11() { wantClasses("One"); }
    public void test12() { wantClasses(""); }
    public void test13() { wantClasses("One"); }
    public void fail1(BuildException be) {}
    public void fail2(BuildException be) {}
    public void fail3(BuildException be) {}

    public void test1_fork() { wantClasses("One"); }
    public void test2_fork() { wantClasses("One,Two"); }
    public void test3_fork() { wantClasses("One,Two,Three"); }
    public void test4_fork() { wantClasses("One"); }
    public void test4b_fork() { wantClasses("One"); }
    public void test5_fork() { wantClasses("One,Two"); }
    public void test5b_fork() { wantClasses("One,Two"); }
    public void test6_fork() { wantClasses("One,Two,Three"); }
    public void test6b_fork() { wantClasses("One,Two,Three"); }
    public void test8_fork() { wantClasses("One"); }
    public void test9_fork() { wantClasses("One"); }
    public void test10_fork() { wantClasses("One"); }
    public void test11_fork() { wantClasses("One"); }
    public void test12_fork() { wantClasses(""); }
    public void test13_fork() { wantClasses("One"); }
    public void fail1_fork(BuildException be) {}
    public void fail2_fork(BuildException be) {}
    public void fail3_fork(BuildException be) {}  

    ////// End tests ////////////////////////////////////////////////

    /**
     * Make the build dir -- e.g. call {@link #makeBuildDir}
     */
    protected void beforeEveryTask() {
        makeBuildDir();
    }

    /**
     * Assert classes and clear build dir.
     *
     * @see #checkClasses()
     * @see #clearBuildDir()
     */
    protected void afterEveryTask() {
        checkClasses();
        clearBuildDir();
    }

    /**
     * Expect the classes found in
     * <code>classNamesWithoutExtensions</code>
     *
     * @param classNamesWithoutExtensions Array of class names without
     *                                    extensions we want to see.
     * @see   #wantClasses(List)
     */
    protected void wantClasses(String[] classNamesWithoutExtensions) {
        List list = new Vector();
        for (int i = 0; i < classNamesWithoutExtensions.length; i++) {
            list.add(classNamesWithoutExtensions[i]);
        }
        wantClasses(list);
    }
    
    /**
     * Expect the classes found in
     * <code>classNamesWithoutExtensions</code>
     *
     * @param classNamesWithoutExtensions String of class names without
     *                                    extensions we want to see separated
     *                                    by <code> </code>, <code>,</code>, or
     *                                    <code>;</code>.
     * @see   #wantClasses(List)
     */
    protected void wantClasses(String classNamesWithoutExtensions) {
        StringTokenizer tok = new StringTokenizer(classNamesWithoutExtensions, " ,;");
        List list = new Vector();
        while (tok.hasMoreTokens()) {
            list.add(tok.nextToken());
        }
        wantClasses(list);
    }

    /**
     * Expected each class name found in
     * <code>classNamesWithoutExtensions</code>.
     *
     * @param classNamesWithoutExtensions List of class names without
     *                                    exntensions.
     * @see   #want(Object)
     */
    protected void wantClasses(List classNamesWithoutExtensions) {
        Iterator iter = classNamesWithoutExtensions.iterator();
        while (iter.hasNext()) {
            String className = iter.next() + "";
            className = className.replace('.', '/').replace('\\', '/');
            want(className + ".class");
        }
    }

    /**
     * Assert that all classes in {@link #wants} were found.
     */
    protected void checkClasses() {
        Iterator iter = wants.iterator();
        while (iter.hasNext()) {
            String className = iter.next() + "";
            File file = new File(buildDir, className);
            if (file != null && file.exists()) {
                have(className);
            }
        }
    }

    /**
     * Create a new build dir.
     */
    protected void init() {
        buildDir = new File(project.getBaseDir(), TEST_CLASSES);
    }

    /**
     * Make a new build dir using ANT.
     */
    protected void makeBuildDir() {
        try {
            Mkdir mkdir = (Mkdir)project.createTask("mkdir");
            mkdir.setDir(buildDir);
            mkdir.execute();
        } catch (BuildException be) {
            be.printStackTrace();
        }
    }

    /**
     * Clear the build dir using ANT.
     */
    protected void clearBuildDir() {
        try {
            Delete delete = (Delete)project.createTask("delete");
            FileSet fileset = new FileSet();
            fileset.setDir(buildDir);
            fileset.setIncludes("**");
            delete.addFileset(fileset);
            delete.execute();
        } catch (BuildException be) {
            be.printStackTrace();
        }        
    }

    /**
     * Invoke {@link #runTests(String[])} on a
     * new instanceof {@link #AjcTaskTester}.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new AjcTaskTester().runTests(args);
    }
}
