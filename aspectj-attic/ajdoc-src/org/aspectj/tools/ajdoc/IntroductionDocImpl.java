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

import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.ajdoc.ClassDoc;
import org.aspectj.ajdoc.IntroductionDoc;
import org.aspectj.compiler.crosscuts.ast.IntroducedDec;
import org.aspectj.compiler.crosscuts.ast.IntroducedSuperDec;

import java.util.ArrayList;
import java.util.Collection;

public abstract class IntroductionDocImpl
    extends    MemberDocImpl
    implements IntroductionDoc {

    /** Creats a new instance of IntroductionDoc from <code>o</code>. */
    public static IntroductionDocImpl getInstance(AspectDoc ad, Object o) {
        return factory.getInstance(ad, o);
    }

    /** The factory in charge of creating instances of IntroductionDocImpl. */
    private final static Factory factory = new Factory();

    private final Collection targets = new ArrayList();
    
    public void addTarget(ClassDoc cd) { targets.add(cd); }

    protected IntroductionDocImpl(com.sun.javadoc.ClassDoc containingClass) {
        super(containingClass);
    }

    //protected abstract Collection createTargets();

    /**
     * Returns the classes that are affected by this introduction.
     *
     * @return an array of ClassDoc representing the classes
     *         affected by this introduction.
     */
    public final ClassDoc[] targets() {
        //if (targets == null) targets = createTargets();
        return (ClassDoc[])targets.toArray(new ClassDoc[targets.size()]);
    }

    /**
     * The class is in charge of creating
     * instances of IntroductionDocImpl.
     */
    private final static class Factory {
        public static IntroductionDocImpl getInstance(AspectDoc ad, Object o) {
            if (o instanceof IntroducedSuperDec) {
                return new IntroducedSuperDocImpl(ad, (IntroducedSuperDec)o);
            }
            if (o instanceof IntroducedDec) {
                return new IntroducedDocImpl(ad, (IntroducedDec)o);
            }
            return null;
        }
    }

    /** TODO */
    public boolean weakEquals(Object md) {
        return false; // TODO
    }
}
