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

/**
 * A simple wrapper exception to be thrown when we
 * can't make a RootDoc.
 *
 * @see    RootDocMaker#makeRootDoc
 * @author Jeff Palm
 */
public class CannotMakeRootDocException  extends RuntimeException {

    /**
     * Constructs an empty exception.
     */
    public CannotMakeRootDocException() {
        super();
    }

    /**
     * Constructs an exception with message <code>message</code>
     * and a <code>null</code> contained Throwable
     *
     * @param message Message to use.
     */
    public CannotMakeRootDocException(String message) {
        this(message, null);
    }

    /**
     * Constructs an exception with message <code>message</code>
     * an contained Throwable <code>throwable</code>.
     *
     * @param message   Message to use.
     * @param throwable Throwable to use.
     */
    public CannotMakeRootDocException(String message, Throwable throwable) {
        super((message != null ? message : "") +
              (throwable != null ? ":" + throwable : ""));
    }

    /**
     * Constructs an exception with <code>null</code> message
     * an contained Throwable <code>throwable</code>.
     *
     * @param throwable Throwable to use.
     */
    public CannotMakeRootDocException(Throwable t) {
        this(null, t);
    }

}
