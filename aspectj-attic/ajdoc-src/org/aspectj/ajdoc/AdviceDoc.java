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
package org.aspectj.ajdoc;

import com.sun.javadoc.Type;

/**
 * Documentation for a piece of advice.
 *
 * @author Jeff Palm
 */
public interface AdviceDoc extends ExecutableMemberDoc,
                                   com.sun.javadoc.ExecutableMemberDoc {
    /**
     * Returns <code>true</code> if the advice is <code>abstract</code>.
     *
     * @return <code>true</code> if the advice is <code>abstract</code>.
     */
    public boolean isAbstract();

    /**
     * Returns a {@link #AspectDoc} representing the aspect
     * that is overriden by this advice.
     *
     * @return a AspectDoc representing the aspect
     *         that is overriden by this advice.
     */
    public AspectDoc overriddenAspect();

    /**
     * Returns the return type of this advice -- it may be null.
     *
     * @return the return type of this advice -- it may be null.
     */
    public Type returnType();

    /**
     * Returns the array of docs this advice crosscuts.
     *
     * @return an array of docs this advice crosscuts.
     */
    public com.sun.javadoc.ExecutableMemberDoc[] crosscuts();

    /**
     * Returns <code>true</code> if this is <code>throwing</code> advice.
     *
     * @return <code>true</code> if this is <code>throwing</code> advice.
     */
    public boolean isThrowing();
    
    /**
     * Returns <code>true</code> if this is <code>returning</code> advice.
     *
     * @return <code>true</code> if this is <code>returning</code> advice.
     */
    public boolean isReturning();

    /**
     * Returns the extra formal type that's the optional type
     * to <code>after returning</code> or <code>after throwing</code>
     * advice.
     *
     * @return an instance of Type that represents the the extra formal type
     *         that's the optional type to <code>after returning</code> or
     *         <code>after throwing</code> advice.
     */
    public Type extraType();
}
