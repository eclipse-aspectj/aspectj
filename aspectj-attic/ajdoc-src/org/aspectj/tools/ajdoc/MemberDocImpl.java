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

import org.aspectj.ajdoc.IntroducedDoc;
import org.aspectj.ajdoc.MemberDoc;

public abstract class MemberDocImpl
    extends ProgramElementDocImpl
    implements MemberDoc {
    
    /**
     * The introduction that introduces this member
     * to its enclosing type.
     */
    private IntroducedDoc introduced;

    public MemberDocImpl(com.sun.javadoc.ClassDoc containingClass) {
        super(containingClass);
    }

    /**
     * Returns whether the passed in Object is equals
     * based on type <b>names</b> and the name
     * of the declaration.
     *
     * @return <code>true</code> if the passed in Object
     *         is equal to this based on type names
     *         and field names.
     */
    public abstract boolean weakEquals(Object other);

    /**
     * Sets this member's introduction.
     *
     * @param introduced the new introduction.
     */
    protected void setIntroduced(IntroducedDoc introduced) {
        this.introduced = introduced;
    }

    /**
     * Returns the introduction that introduced this member onto
     * its enclosing type -- this value may be <code>null</code>
     * if the member wasn't introduced.
     *
     * @return an IntroducedDoc representing the introduction
     *         that introduced this member onto its enclosing
     *         type.  The return value may be <code>null</code>.
     */
    public IntroducedDoc introduced() {
        return introduced;
    }

    /**
     * Returns <code>true</code>if this code is <i>synthetic</i>.
     *
     * @return <code>true</code>if this code is <i>synthetic</i>.
     */
    public boolean isSynthetic() {
        return (!dec().isLanguageVisible());
    }
}
