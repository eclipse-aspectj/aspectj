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
package org.aspectj.tools.ajdoc;

import org.aspectj.compiler.base.ErrorHandler;
import org.aspectj.compiler.base.InternalCompilerError;

import com.sun.javadoc.DocErrorReporter;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A class to handler errors in ajdoc.
 *
 * @author Jeff Palm
 */
public class ErrPrinter
    extends ErrorHandler
    implements DocErrorReporter
{

    /** The global instance that anyone can use. */
    public final static ErrPrinter instance = new ErrPrinter();

    private String programName;
    private PrintWriter out;
    private PrintWriter err;
    private ResourceBundle bundle;
    private List keys = new ArrayList();
    private List msgs = new ArrayList();

    static int cnt = 0;

    /**
     * Construct with program name <code>programName</code>
     * and printstreams <code>err</code> and <code>out</code>.
     *
     * @param programName program name.
     * @param err         error stream.
     * @param out         output stream.
     */
    public ErrPrinter(String programName,
                      PrintWriter err,
                      PrintWriter out) {
        super(err);
        this.programName = programName;
        this.err = err;
        this.out = out;
      	try {
            bundle = ResourceBundle.getBundle
                ("org.aspectj.tools.ajdoc.resources.ajdoc");
	} catch (MissingResourceException e) {
	    throw new Error("Can't find ajdoc.properties: " + e);
	}  
    }
    
    /**
     * Construct with program name <code>programName</code>
     * and printstreams <code>System.err</code> and <code>System.out</code>
     *
     * @param programName program name.
     */
    public ErrPrinter(String programName) {
        this(programName,
             new PrintWriter(System.err, true),
             new PrintWriter(System.out, true));
    }
    
    /**
     * Construct with program name <code>"ajdoc"</code>
     * and printstreams <code>System.err</code> and <code>System.out</code>
     */
    public ErrPrinter() {
        this("ajdoc");
    }

    /**
     * Print <code>error</code> and increment the error count.
     *
     * @param error error message to print.
     */
    public void printError(String error) {
        errors++;
        err.println(error);
        err.flush();
    }
    
    /**
     * Print <code>warning</code> and increment the warning count.
     *
     * @param warning warning message to print.
     */
    public void printWarning(String warning) {
        warnings++;
        err.println(warning);
        err.flush();
    }

    /**
     * Print <code>notice</code>.
     *
     * @param notice notice message to print.
     */
    public void printNotice(String notice) {
        out.println(notice);
        out.flush();
    }

    /**
     * Returns the number of errors.
     *
     * @return number of errors.
     */
    public int getNumErrors() {
        return errors;
    }

    /**
     * Returns the number of warnings.
     *
     * @return number of warnings.
     */
    public int getNumWarnings() {
        return warnings;
    }

    /**
     * Returns the keys in the resource bundle.
     *
     * @return keys in the resource bundle.
     */
    public List getKeys() {
        return new ArrayList(keys);
    }

    /**
     * Returns the messages in the resource bundle.
     *
     * @return messages in the resource bundle.
     */
    public List getMsgs() {
        return new ArrayList(msgs);
    }

    /**
     * Handles InvocationTargetExceptions.
     *
     * @param e          the exception.
     * @param classname  the class on which the method was trying
     *                   to be invoked.
     * @param methodName the name of the method trying to be invoked.
     * @return           the exception.
     */
    public synchronized Throwable invocationTargetException
        (InvocationTargetException e,
         String classname,
         String methodName) {
        Throwable t = e.getTargetException();
        if (t != null) {
            if (t instanceof OutOfMemoryError) {
                error("out_of_memory");
            } else {
                error("exception_thrown", "", classname, methodName, t+"");
                t.printStackTrace();
            }
        }
        return t != null ? t : e;
    }

    /**
     * Handles an internal error.
     *
     * @param key key of the message to use.
     * @param t   exception thrown.
     */
    public synchronized void internalError(String key, Throwable t) {
        internalError(key, "", t);
    }

    /**
     * Handles an internal error.
     *
     * @param key key of the message to use.
     * @param s0  first argument in the message.
     * @param t   exception thrown.
     */
    public synchronized void internalError(String key, String s0, Throwable t) {
        if (t instanceof InternalCompilerError) {
            t = ((InternalCompilerError)t).uncaughtThrowable;
        }
        error(key, s0, t != null ? t.getMessage() : "");
        if (t != null) t.printStackTrace();
        internalError(t, null);
    }

    
    /**
     * Prints an error message for key <code>key</code>
     * ,and returns the number of errors.
     *
     * @param key           key of the message.
     * @return              number of errors.
     */
    public final int error(String key) {
        printError(text(key));
        return errors;
    }

    /**
     * Prints an error message for key <code>key</code>
     * and argument <code>s0</code>,
     * and returns the number of errors.
     *
     * @param key           key of the message.
     * @param s0            argument to message.
     * @return              number of errors.
     */
    public final int error(String key, String s0) {
        printError(text(key,s0));
        return errors;
    }

    /**
     * Prints an error message for key <code>key</code>
     * and arguments <code>s0,s1</code>,
     * and returns the number of errors.
     *
     * @param key           key of the message.
     * @param s0            argument to message.
     * @param s1            argument to message.
     * @return              number of errors.
     */
    public final int error(String key, String s0, String s1) {
        printError(text(key,s0,s1));
        return errors;
    }

    /**
     * Prints an error message for key <code>key</code>
     * and arguments <code>s0,s1,s2</code>,
     * and returns the number of errors.
     *
     * @param key           key of the message.
     * @param s0            argument to message.
     * @param s1            argument to message.
     * @param s2            argument to message.
     * @return              number of errors.
     */
    public final int error(String key, String s0, String s1,
                            String s2) {
        printError(text(key,s0,s1,s2));
        return errors;
    }
    /**
     * Prints an error message for key <code>key</code>
     * and arguments <code>s0,s1,s2,cookieMonster</code>,
     * and returns the number of errors.
     *
     * @param key            key of the message.
     * @param s0             argument to message.
     * @param s1             argument to message.
     * @param s2             argument to message.
     * @param cookieMonster  argument to message.
     * @return               number of errors.
     */
    public final int error(String key, String s0, String s1,
                            String s2, String cookieMonster) {
        printError(text(key,s0,s1,s2,cookieMonster));
        return errors;
    }

    /**
     * Handles the thrown exception <code>t</code>
     * with message key <code>key</code>, and returns
     * the number of errors.
     *
     * @param t             thrown exception.
     * @param key           message key.
     * @return              number of errors.
     */
    public final int ex(Throwable t, String key) {
        error(key);
        if (t != null) t.printStackTrace();
        return errors;
    }

    /**
     * Handles the thrown exception <code>t</code>
     * with message key <code>key</code> and
     * argument <code>s0</code>, and returns
     * the number of errors.
     *
     * @param t             thrown exception.
     * @param key           message key.
     * @param s0            argument to message.
     * @return              number of errors.
     */
    public final int ex(Throwable t, String key, String s0) {
        error(key,s0);
        if (t != null) t.printStackTrace();
        return errors;
    }

    /**
     * Handles the thrown exception <code>t</code>
     * with message key <code>key</code> and
     * arguments <code>s0,s1</code>, and returns
     * the number of errors.
     *
     * @param t             thrown exception.
     * @param key           message key.
     * @param s0            argument to message.
     * @param s1            argument to message.
     * @return              number of errors.
     */
    public final int ex(Throwable t, String key, String s0, String s1) {
        error(key,s0,s1);
        if (t != null) t.printStackTrace();
        return errors;
    }

    /**
     * Handles the thrown exception <code>t</code>
     * with message key <code>key</code> and
     * arguments <code>s0,s1,s2</code>, and returns
     * the number of errors.
     *
     * @param t             thrown exception.
     * @param key           message key.
     * @param s0            argument to message.
     * @param s1            argument to message.
     * @param s2            argument to message.
     * @return              number of errors.
     */
    public final int ex(Throwable t, String key, String s0, String s1,
                            String s2) {
        error(key,s0,s1,s2);
        if (t != null) t.printStackTrace();
        return errors;
    }

    /**
     * Handles the thrown exception <code>t</code>
     * with message key <code>key</code> and
     * arguments <code>s0,s1,s2,snuffulufugus</code>, and returns
     * the number of errors.
     *
     * @param t             thrown exception.
     * @param key           message key.
     * @param s0            argument to message.
     * @param s1            argument to message.
     * @param s2            argument to message.
     * @param snuffulufugus argument to message.
     * @return              number of errors.
     */
    public final int ex(Throwable t, String key, String s0, String s1,
                            String s2, String snuffulufugus) {
        error(key,s0,s1,s2,snuffulufugus);
        if (t != null) t.printStackTrace();
        return errors;
    }

    /**
     * Prints the warning with key <code>key</code>
     * and returns the number of warnings.
     *
     * @param key message key.
     * @return    number of warnings.
     */
    public final int warning(String key) {
        printWarning(text(key));
        return warnings;
    }

    /**
     * Prints the warning with key <code>key</code> and
     * argument <code>s0</code>,
     * and returns the number of warnings.
     *
     * @param key           message key.
     * @param s0            argument to message.
     * @return              number of warnings.
     */
    public final int warning(String key, String s0) {
        printWarning(text(key,s0));
        return warnings;
    }

    /**
     * Prints the warning with key <code>key</code> and
     * arguments <code>s0,s1</code>,
     * and returns the number of warnings.
     *
     * @param key           message key.
     * @param s0            argument to message.
     * @param s1            argument to message.
     * @return              number of warnings.
     */
    public final int warning(String key, String s0, String s1) {
        printWarning(text(key,s0,s1));
        return warnings;
    }

    /**
     * Prints the warning with key <code>key</code> and
     * arguments <code>s0,s1,s2</code>,
     * and returns the number of warnings.
     *
     * @param key           message key.
     * @param s0            argument to message.
     * @param s1            argument to message.
     * @param s2            argument to message.
     * @return              number of warnings.
     */
    public final int warning(String key, String s0, String s1,
                            String s2) {
        printWarning(text(key,s0,s1,s2));
        return warnings;
    }

    /**
     * Prints the warning with key <code>key</code> and
     * arguments <code>s0,s1,s2,josefStalin</code>,
     * and returns the number of warnings.
     *
     * @param key          message key.
     * @param s0           argument to message.
     * @param s1           argument to message.
     * @param s2           argument to message.
     * @param josefStalin  argument to message.
     * @return             number of warnings.
     */
    public final int warning(String key, String s0, String s1,
                            String s2, String josefStalin) {
        printWarning(text(key,s0,s1,s2,josefStalin));
        return warnings;
    }

    /**
     * Print a notice with message key <code>key</code>.
     *
     * @param key      message key.
     */
    public final void notice(String key) {
        printNotice(text(key));
    }

    /**
     * Print a notice with message key <code>key</code>
     * and argument <code>s0</code>.
     *
     * @param key      message key.
     * @param s0       argument to message.
     */
    public final void notice(String key, String s0) {
        printNotice(text(key,s0));
    }

    /**
     * Print a notice with message key <code>key</code>
     * and arguments <code>s0,s1</code>.
     *
     * @param key      message key.
     * @param s0       argument to message.
     * @param s1       argument to message.
     */
    public final void notice(String key, String s0, String s1) {
        printNotice(text(key,s0,s1));
    }

    /**
     * Print a notice with message key <code>key</code>
     * and arguments <code>s0,s1,s2</code>.
     *
     * @param key      message key.
     * @param s0       argument to message.
     * @param s1       argument to message.
     * @param s2       argument to message.
     */
    public final void notice(String key, String s0, String s1,
                            String s2) {
        printNotice(text(key,s0,s1,s2));
    }

    /**
     * Print a notice with message key <code>key</code>
     * and arguments <code>s0,s1,s2,bigbird</code>.
     *
     * @param key      message key.
     * @param s0       argument to message.
     * @param s1       argument to message.
     * @param s2       argument to message.
     * @param bigbird  argument to message.
     */
    public final void notice(String key, String s0, String s1,
                            String s2, String bigbird) {
        printNotice(text(key,s0,s1,s2,bigbird));
    }
    
    /**
     * Returns the String for message key <code>key</code>.
     *
     * @return               String for message
     *                       key <code>key</code>.
     */
    protected final String text(String key) {
        return text(key, "");
    }

    /**
     * Returns the String for message key <code>key</code>
     * and argument <code>s0</code>
     *
     * @param  s0             argument to message.
     * @return                String for message
     *                        key <code>key</code>. 
     */
    protected final String text(String key, String s0) {
        return text(key, s0, "");
    }

    /**
     * Returns the String for message key <code>key</code>
     * and arguments <code>s0,s1</code>
     *
     * @param  s0             argument to message.
     * @param  s1             argument to message.
     * @return                String for message
     *                        key <code>key</code>.
     */
    protected final String text(String key, String s0, String s1) {
        return text(key, s0, s1, "");
    }

    /**
     * Returns the String for message key <code>key</code>
     * and arguments <code>s0,s1,s2</code>
     *
     * @param  s0             argument to message.
     * @param  s1             argument to message.
     * @param  s2             argument to message.
     * @return                String for message
     *                        key <code>key</code>.
     */
    protected final String text(String key, String s0, String s1,
                              String s2) {
        return text(key, s0, s1, s2, "");
    }

    /**
     * Returns the String for message key <code>key</code>
     * and arguments <code>s0,s1,s2,oscarTheGrouch</code>
     *
     * @param  s0             argument to message.
     * @param  s1             argument to message.
     * @param  s2             argument to message.
     * @param  oscarTheGrouch argument to message.
     * @return                String for message
     *                        key <code>key</code>.
     */
    protected final String text(String key, String s0, String s1,
                              String s2, String oscarTheGrouch) {
        return text(key, new String[]{s0,s1,s2,oscarTheGrouch});
    }

    /**
     * Returns the String for the message key <code>key</code>
     * with arguments contained in <code>args</code>.
     *
     * @param key  message key.
     * @param args array of arguments to substitute.
     * @return     String for message with key
     *             <code>key</code> and arguments
     *             <code>args</code>
     */
    protected final String text(String key, String[] args) {
        String msg = MessageFormat.format(string(key), args);
        msgs.add(msg);
        return msg;
    }

    /**
     * Returns the String with message <code>key</code>.
     *
     * @param key message key.
     * @return String for message with key <code>key</code>.
     */
    protected final String string(String key) {
        keys.add(key);
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            throw new Error("Can't find " + key + " in " + bundle);
        }
    }
    PrintWriter getErr() { 
        return err;
    }
    PrintWriter getOut() { 
        return out;
    }
}
