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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.tools.ajdoc.ErrPrinter;

/**
 * An abstract class for a javadoc rootmaker.
 * Provides some error handling and misc. functionality.
 *
 * @author Jeff Palm
 */
public abstract class Javadoc {

    /** Subclasses may want this ;). */
    protected ErrPrinter err;

    /** Default ctor. */
    public Javadoc() {}
    

    /**
     * Returns the result of invoking <code>method</code>,
     * on target <code>owner</code>, with arguments <code>args</code>.
     * This method handles errors that arise.
     *
     * @param method method to invoke.
     * @param owner  target of the invocation.
     * @param args   arguments to pass the method.
     * @return       result of invoking the method on
     *               the passed in owner with the passed in
     *               parameters.
     */
    protected final Object invoke(Method method, Object owner, Object[] args) {
        if (method == null || owner == null) return null;
        String classname = owner.getClass().getName();
        String methodName = method.getName();
        try {
            Thread.currentThread().setContextClassLoader
                (owner.getClass().getClassLoader());
            return method.invoke(owner, args);
        } catch (InvocationTargetException e) {
            err.invocationTargetException(e, classname, methodName);
        } catch (IllegalAccessException e) {
            err.ex(e, "method_not_accessible", classname, methodName);
        } catch (Exception e) {
            err.ex(e, "exception_thrown", "List",
                   classname, methodName, e != null ? e.getMessage() : e+"");
        }
        return null;
    }

    /**
     * Returns the <code>Class</code> with name <code>classname</code>.
     * This may return null if <code>Class.forName</code> returns null,
     * but otherwise, this method handles resulting errors.
     *
     * @param classname name of the class to get.
     * @return          Class named <code>clasname</code>.
     */
    protected final Class type(String classname) {
        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) {
            err.ex(e, "class_not_found", "Hashtable", classname);
        }
        return null;
    }

    /**
     * Returns the method named <code>name</code>, whose parameters
     * are declared <code>params</code>, and which is declared
     * in <code>type</code>.
     *
     * @param name   name of the method.
     * @param params type of the parameters.
     * @param type   type in which the resulting method is declared.
     * @return       the method named <code>name</code>, whose parameters
     *               are declared <code>params</code>, and which is declared
     *               in <code>type</code>.  This may be null.
     */
    protected final Method method(String name, Class[] params, Class type) {
        if (type == null) return null;
        try {
            return type.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            err.ex(e, "method_not_found", type.getClass().getName(), name);
        }
        return null;
    }

    /**
     * Returns the Object resulting from default construction
     * of a class named <code>classname</code>.  This method
     * handles all errors that arise.
     *
     * @param classname class name of the resulting Object.
     * @return          new instance made from the default
     *                  construction of a class named
     *                  <code>classname</code>.  This may be null.
     * @see             #newInstance(Class)
     */
    protected final Object newInstance(String classname) {
        return newInstance(type(classname));
    }
    
    /**
     * Returns the Object resulting from default construction
     * of a class <code>type</code>.  This method
     * handles all errors that arise.
     *
     * @param type Class of the resulting Object.
     * @return     new instance made from the default
     *             construction of a class named
     *             <code>classname</code>.  This may be null.
     */
    protected final Object newInstance(Class type) {
        if (type == null) return null;
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            err.ex(e, "must_have_default_ctor", type.getClass().getName());
            return null;
        } catch (IllegalAccessException e) {
            err.ex(e, "method_not_accessible", type.getClass().getName(), "new()");
            return null;
        }
    }
}
