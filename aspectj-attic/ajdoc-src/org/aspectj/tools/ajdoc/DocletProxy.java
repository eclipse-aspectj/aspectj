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
import org.aspectj.tools.doclets.standard.AbstractStandard;
import org.aspectj.tools.doclets.standard.Standard;

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;

import java.io.IOException;
import java.util.List;

/**
 */
public interface DocletProxy {
    public static final DocletProxy STANDARD 
        = JavadocStandardProxy.SINGLETON;
    public static final DocletProxy DEFAULT 
        = StandardProxy.SINGLETON;
    public int optionLength(String arg);
    public boolean start(RootDoc root) 
        throws IOException;
    public boolean validOptions(List options, DocErrorReporter handler) 
        throws IOException;
}

/**
 * This proxy delegates to Standard singleton,
 * but delays alerts about it being unavailable until first use
 */
class StandardProxy implements DocletProxy {
    public static final DocletProxy SINGLETON = new StandardProxy();
    private StandardProxy() { }

    /** check and delegate to standard */
    public int optionLength(String arg) {
        return Standard.optionLength(arg);
    }
    /** check and delegate to standard */
    public boolean validOptions(List options, DocErrorReporter handler) 
        throws IOException {
        return AbstractStandard.validOptions(docletOptions(options), handler);
    }
    // todo: validate that this is the expected format for doclet options
    protected String[][] docletOptions(List options) {
        if ((null == options) || (1 > options.size())) {
            return new String[][]{};
        }
        Object[] ra = options.toArray();
        String[] strs = new String[ra.length];
        for (int i = 0; i < ra.length; i++) {
            strs[i] = (null == ra[i] ? null : ra[i].toString());
        }
        return new String[][] {strs};
    }

    /** check and delegate to standard */
    public boolean start(RootDoc root) throws IOException {
        return Standard.start(root);
    }
} // StandardProxy

/**
 * This proxy delegates to javadoc Standard singleton.
 */
class JavadocStandardProxy implements DocletProxy {
    public static final DocletProxy SINGLETON = new JavadocStandardProxy();
    private JavadocStandardProxy() { }

    /** check and delegate to standard */
    public int optionLength(String arg) {
        return com.sun.tools.doclets.standard.Standard.optionLength(arg);
    }
    /** check and delegate to standard */
    public boolean validOptions(List options, DocErrorReporter handler) 
        throws IOException {
        return com.sun.tools.doclets.standard.Standard.validOptions(docletOptions(options), handler);
    }
    // todo: validate that this is the expected format for doclet options
    protected String[][] docletOptions(List options) {
        if ((null == options) || (1 > options.size())) {
            return new String[][]{};
        }
        Object[] ra = options.toArray();
        String[] strs = new String[ra.length];
        for (int i = 0; i < ra.length; i++) {
            strs[i] = (null == ra[i] ? null : ra[i].toString());
        }
        return new String[][] {strs};
    }

    /** check and delegate to standard */
    public boolean start(RootDoc root) throws IOException {
        return com.sun.tools.doclets.standard.Standard.start(root);
    }
} // JavadocStandardProxy

/** Wrap a Throwable as a RuntimeException 
 * This will render stack trace as the delegate stack trace,
 * notwithstanding any call to fillInStackTrace.
class ExceptionWrapper extends RuntimeException {
    Throwable delegate;
    public ExceptionWrapper(Throwable t) {
        delegate = (null == t ? new Error("null") : t);
    }
    public void printStackTrace() {
        delegate.printStackTrace();
    }
    public void printStackTrace(PrintStream stream) {
        delegate.printStackTrace(stream);
    }
    public void printStackTrace(PrintWriter stream) {
        delegate.printStackTrace(stream);
    }
    public String getMessage() {
        return delegate.getMessage();
    }
    public String getLocalizedMessage() {
        return delegate.getLocalizedMessage();
    }
    public Throwable fillInStackTrace() {
        return delegate.fillInStackTrace();
    }
}
*/
