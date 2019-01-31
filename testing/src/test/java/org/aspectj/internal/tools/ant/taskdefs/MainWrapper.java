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



package org.aspectj.internal.tools.ant.taskdefs;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aspectj.testing.util.LangUtil;

/** 
 * Wrapper to invoke class identified by setting VM argument.
 * Caller must set a system property "MainWrapper.classname"
 * to the fully-qualified name of the target class to invoke,
 * and the target class must be resolvable from the defining
 * class loader of this class.
 * VM argument name is available as <code>PROP_NAME</code>, but
 * is set by adding the following to the command line:
 * <code>-DMainWrapper.classname="fully.qualified.Classname"</code>.
 * This returns -1 if unable to load the main method,
 * 1 if the invoked method throws an exception, and 0 otherwise.
 */
public class MainWrapper {
    /** MUST set the fully-qualified name of class to invoke using
     * a VM property of this name 
     * tracked in Ajctest.java */
    public static final String PROP_NAME = "MainWrapper.classname";
    /** May set the path to a classes diretory, 
     * to interpret class names and load classes.
     * Tracked in Ajctest.java */
    public static final String CLASSDIR_NAME = "MainWrapper.classdir";

    /** to disable returning int via System.exit, set to boolean true value (todo: ignored) */
    public static final String SIGNAL_EXCEPTION_NAME = "MainWrapper.signalException";

    /** to disable returning via System.exit on first Throwable, set to boolean true value (todo: ignored) */
    public static final String FAIL_ON_EXCEPTION_NAME = "MainWrapper.failOnException";

    /** quit on first exception */ // todo public class controls - yuck 
    public static boolean FAIL_ON_EXCEPTION = true;

    /** signal number of exceptions with int return value */ 
    public static boolean SIGNAL_EXCEPTION = true;

    /** redirect messages for exceptions; if null, none printed */ 
    public static PrintStream OUT_STREAM = System.err;

    /** result accumulated, possibly from multiple threads */ 
    private static int result;

    /**
     * Run target class's main(args), doing a System.exit() with
     * a value > 0 for the number of Throwable that 
     *  the target class threw that
     * makes it through to a top-level ThreadGroup. (This is 
     * strictly speaking not correct since applications can live
     * after their exceptions stop a thread.)
     * Exit with a value < 0 if there were exceptions in loading
     * the target class.  Messages are printed to OUT_STREAM.
     */
    public static void main(String[] args) {
        String classname = "no property : " + PROP_NAME;
        Method main = null;
        // setup: this try block is for loading main method - return -1 if fail
        try {
            // access classname from jvm arg 
            classname = System.getProperty(PROP_NAME);
            // this will fail if the class is not available from this classloader
            Class<?> cl = Class.forName(classname);
            final Class<?>[] argTypes = new Class[] {String[].class};
            // will fail if no main method
            main = cl.getMethod("main", argTypes);
            if (!Modifier.isStatic(main.getModifiers())) {
                PrintStream outStream = OUT_STREAM;
                if (null != outStream) outStream.println("main is not static");
                result = -1;
            }
            // if user also request loading of all classes...
            String classesDir = System.getProperty(CLASSDIR_NAME);
            if ((null != classesDir) && (0 < classesDir.length())) {
                MainWrapper.loadAllClasses(new File(classesDir));
            }
        } catch (Throwable t) {
            if (1 != result) result--;
            reportException("setup Throwable invoking class " + classname, t);
        }
        // run: this try block is for running things - get Throwable from our thread here
        if ((null != main) && (0 == result)) {
            try {
                runInOurThreadGroup(main, args);
            } catch (Throwable t) {
                if (result > -1) {
                    result++;
                }
                reportException("run Throwable invoking class " + classname, t);
            }
        }
        if ((0 != result) && (SIGNAL_EXCEPTION)) {
            System.exit(result);
        }
    }

    static void runInOurThreadGroup(final Method main, final String[] args) {
        final String classname = main.getDeclaringClass().getName();
        ThreadGroup ourGroup = new ThreadGroup("MainWrapper ThreadGroup") {
                public void uncaughtException(Thread t, Throwable e) {
                    reportException("uncaughtException invoking " + classname, e);
                    result++;
                    if (FAIL_ON_EXCEPTION) {
                        System.exit((SIGNAL_EXCEPTION ? result : 0));
                    }
                }
            };
        Runnable runner = new Runnable() {
                public void run() {
                    try {
                        main.invoke(null, new Object[] {args});
                    } catch (InvocationTargetException e) {
                        result = -1;
                        reportException("InvocationTargetException invoking " + classname, e);
                    } catch (IllegalAccessException e) {
                        result = -1;
                        reportException("IllegalAccessException invoking " + classname, e);
                    }
                }
            };
        Thread newMain = new Thread(ourGroup, runner, "pseudo-main");
        newMain.start();
        try {
            newMain.join();
        } catch (InterruptedException e) {
            result = -1; // todo: InterruptedException might be benign - retry?
            reportException("Interrupted while waiting for to join " + newMain, e);
        }
    }

    /** 
     * Try to load all classes in a directory.
     * @throws Error if any failed
     */
    static protected void loadAllClasses(File classesDir) {
        if (null != classesDir) {
            String[] names = LangUtil.classesIn(classesDir);
            StringBuffer err = new StringBuffer();
            LangUtil.loadClasses(names, null, err);
            if (0 < err.length()) {
                throw new Error("MainWrapper Errors loading classes: " 
                                 + err.toString());
            }
        }
    }

    static void reportException(String context, Throwable t) {
        PrintStream outStream = OUT_STREAM;
        if (null != outStream) {
            while ((null != t) && 
                   (InvocationTargetException.class.isAssignableFrom(t.getClass()))) {
                t = ((InvocationTargetException) t).getTargetException();
            }
            outStream.println("  context: " + context);
            outStream.println("  message: " + t.getMessage());
            t.printStackTrace(outStream);
        }
    }

} // MainWrapper
