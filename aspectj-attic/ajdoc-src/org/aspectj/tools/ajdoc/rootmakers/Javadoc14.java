/* -*- Mode: JDE; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the debugger and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 */
package org.aspectj.tools.ajdoc.rootmakers;

//import com.sun.javadoc.RootDoc;
//import com.sun.javadoc.DocErrorReporter;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Method;
//import java.util.Iterator;
//import java.util.List;
//
//import org.aspectj.tools.ajdoc.CannotMakeRootDocException;
//import org.aspectj.tools.ajdoc.ErrPrinter;
//import org.aspectj.tools.ajdoc.RootDocMaker;
//import org.aspectj.tools.ajdoc.AccessChecker;

//XXX
//import com.sun.tools.javac.v8.util.Hashtable;

/**
 * Makes a RootDoc using javadoc from SDK 1.4 as an example.
 *
 * @author Jeff Palm
 */
//public class Javadoc14 extends Javadoc implements RootDocMaker {
//
//    public Javadoc14() {}
//
//    public RootDoc makeRootDoc(String sourcepath,
//                               String classpath,
//                               String bootclasspath,
//                               String extdirs,
//                               long flags,
//                               String encoding,
//                               String locale,
//                               String source,
//                               List classesAndPackages,
//                               List options,
//                               DocErrorReporter docErrorReporter,
//                               String programName,
//                               AccessChecker filter)
//        throws CannotMakeRootDocException {
//        this.err = (ErrPrinter)docErrorReporter;
//        Class hashtableClass = null;
//        String classname = "com.sun.tools.javac.v8.util.Hashtable";
//        try {
//            hashtableClass = Class.forName(classname);
//        } catch (ClassNotFoundException e) {
//            err.ex(e, "class_not_found", "Hashtable", classname);
//            return null;
//        }
//        Object hashtable = null;
//        try {
//            hashtable = hashtableClass.newInstance();
//            Method put = hashtableClass.getMethod("put",
//                                                  new Class[]{Object.class,
//                                                              Object.class});
//            if (sourcepath != null) {
//                put.invoke(hashtable, new Object[]{"-sourcepath",
//                                                   sourcepath});
//            }
//            if (classpath != null) {
//                put.invoke(hashtable, new Object[]{"-classpath",
//                                                   classpath});
//            }
//            if (bootclasspath != null) {
//                put.invoke(hashtable, new Object[]{"-bootclasspath",
//                                                   bootclasspath});
//            }
//            if (extdirs != null) {
//                put.invoke(hashtable, new Object[]{"-extdirs",
//                                                   extdirs});
//            }
//            if (encoding != null) {
//                put.invoke(hashtable, new Object[]{"-encoding",
//                                                   encoding});
//            }
//            if (true || (flags & 0x1) != 0) {
//                put.invoke(hashtable, new Object[]{"-verbose",
//                                                   ""});
//            }
//            if (source != null) {
//                put.invoke(hashtable, new Object[]{"-source",
//                                                   source});
//            }
//            if (filter != null) {
//                put.invoke(hashtable, new Object[]{"-" + filter.getOption(),
//                                                   ""});
//            }
//            Hashtable h = (Hashtable)hashtable;
//            
//        } catch (NoSuchMethodException e) {
//            err.ex(e, "method_not_found", classname, "put");
//            return null;
//        } catch (InvocationTargetException e) {
//            err.invocationTargetException(e, classname, "put");
//            return null;
//        } catch (InstantiationException e) {
//            err.ex(e, "must_have_default_ctor", classname);
//            return null;
//        } catch (IllegalAccessException e) {
//            err.ex(e, "method_not_accessible", classname, "new()");
//            return null;
//        }
//        Class messagerClass = null;
//        classname = "com.sun.tools.javadoc.Messager";
//        try {
//            messagerClass = Class.forName(classname);
//        } catch (ClassNotFoundException e) {
//            err.ex(e, "class_not_found", "Messager", classname);
//            return null;
//        }
//        Object messager = null;
//        try {
//            Constructor ctor =
//                messagerClass.getConstructor(new Class[]{String.class});
//            messager = ctor.newInstance(new Object[]{programName});
//        } catch (InstantiationException e) {
//            err.ex(e, "cant_construct_object", classname);
//            return null;            
//        } catch (NoSuchMethodException e) {
//            err.ex(e, "method_not_found", classname, "new(String)");
//            return null;
//        } catch (IllegalAccessException e) {
//            err.ex(e, "method_not_accessible", classname, "new(String)");
//            return null;
//        } catch (InvocationTargetException e) {
//            err.invocationTargetException(e, classname, "new(String)");
//            return null;
//        }
//        Class javadocToolClass = null;
//        classname = "com.sun.tools.javadoc.JavadocTool";
//        try {
//            javadocToolClass = Class.forName(classname);
//        } catch (ClassNotFoundException e) {
//            err.ex(e, "class_not_found", "JavadocTool", classname);
//            return null;
//        }
//        Object javadocTool = null;
//        try {
//            Method make = javadocToolClass.getMethod("make",
//                                                     new Class[]{messagerClass,
//                                                                 hashtableClass});
//            javadocTool = make.invoke(null, new Object[]{messager,
//                                                         hashtable});
//        } catch (NoSuchMethodException e) {
//            err.ex(e, "method_not_found", classname, "make");
//            return null;
//        } catch (InvocationTargetException e) {
//            err.invocationTargetException(e, classname, "make");
//            return null;
//        } catch (IllegalAccessException e) {
//            err.ex(e, "method_not_accessible", classname, "make");
//            return null;
//        } catch (Exception e) {
//            err.ex(e, "exception_thrown", "JavadocTool",
//                      classname, "make", e != null ? e.getMessage() : e+"");
//            return null;
//        }
//        Class modifierFilterClass = null;
//        classname = "com.sun.tools.javadoc.ModifierFilter";
//        try {
//            modifierFilterClass = Class.forName(classname);
//        } catch (ClassNotFoundException e) {
//            err.ex(e, "class_not_found", "ModifierFilter", classname);
//            return null;
//        }
//        Object modifierFilter = null;
//        classname = "com.sun.tools.javadoc.ModifierFilter";
//        try {
//            Constructor ctor =
//                modifierFilterClass.getConstructor(new Class[]{long.class});
//            modifierFilter = ctor.newInstance(new Object[]{new Long(flags)});
//        } catch (InstantiationException e) {
//            err.ex(e, "cant_construct_object", classname);
//            return null;            
//        } catch (NoSuchMethodException e) {
//            err.ex(e, "method_not_found", classname, "new()");
//            return null;
//        } catch (IllegalAccessException e) {
//            err.ex(e, "method_not_accessible", classname, "new()");
//            return null;
//        } catch (InvocationTargetException e) {
//            err.invocationTargetException(e, classname, "new()");
//            return null;
//        }
//        Class listClass = null;
//        classname = "com.sun.tools.javac.v8.util.List";
//        try {
//            listClass = Class.forName(classname);
//        } catch (ClassNotFoundException e) {
//            err.ex(e, "class_not_found", "List", classname);
//            return null;
//        }
//        RootDoc rootDoc = null;
//        classname = "com.sun.tools.javadoc.JavadocTool";
//        try {
//            Method getRootDocImpl =
//                javadocToolClass.getMethod("getRootDocImpl",
//                                           new Class[]{String.class,
//                                                       modifierFilterClass,
//                                                       listClass,
//                                                       listClass});
//            Object classesOrPackageList = list(classesAndPackages, listClass);
//            Object optionsList = list(options, listClass);
//
//            rootDoc =
//                (RootDoc)getRootDocImpl.invoke(javadocTool,
//                                               new Object[]{locale,
//                                                            modifierFilter,
//                                                            classesOrPackageList,
//                                                            optionsList});
//        } catch (NoSuchMethodException e) {
//            err.ex(e, "method_not_found", classname, "getRootDocImpl");
//            return null;
//        } catch (InvocationTargetException e) {
//            err.invocationTargetException(e, classname, "getRootDocImpl");
//            return null;
//        } catch (IllegalAccessException e) {
//            err.ex(e, "method_not_accessible", classname, "getRootDocImpl");
//            return null;
//        } catch (ClassCastException e) {
//            err.ex(e, "class_cast_exception", "getRootDocImpl", classname,
//                      "com.sun.javadoc.RootDoc",
//                      rootDoc == null ? "" : rootDoc.getClass().getName());
//            return null;
//        }
//        return rootDoc;
//    }
//
//
//    private final Object list(List list, Class listClass) {
//        if (listClass == null) return null;
//        Object newlist = newInstance(listClass);
//        if (newlist == null) return null;
//        if (list == null) return newlist;
//        Class listBufferClass = type("com.sun.tools.javac.v8.util.ListBuffer");
//        if (listBufferClass == null) return newlist;
//        Object listBuffer = newInstance(listBufferClass);
//        if (listBuffer == null) return newlist;
//        Method append = method("append", new Class[]{Object.class},listBufferClass);
//        if (append == null) return newlist;
//        for (Iterator i = list.iterator(); i.hasNext();) {
//            invoke(append, listBuffer, new Object[]{i.next()});
//        }
//
//        Method toList = method("toList", new Class[]{}, listBufferClass);
//        if (toList == null) return newlist;
//        newlist = invoke(toList, listBuffer, new Object[]{});
//        return newlist;
//    }
//}
