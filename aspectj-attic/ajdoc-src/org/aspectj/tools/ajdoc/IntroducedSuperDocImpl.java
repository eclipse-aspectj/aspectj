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

import org.aspectj.ajdoc.IntroducedSuperDoc;
import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.TypeDs;
import org.aspectj.compiler.crosscuts.ast.IntroducedSuperDec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class IntroducedSuperDocImpl
    extends    IntroductionDocImpl
    implements IntroducedSuperDoc {

    /** The introduction to which we delegate. */
    private final IntroducedSuperDec introducedSuperDec;

    /** The types we introduce onto our targets. */
    private final Collection types;

    public IntroducedSuperDocImpl(com.sun.javadoc.ClassDoc containingClass,
                                  IntroducedSuperDec introducedSuperDec) {
        super(containingClass);
        this.introducedSuperDec = introducedSuperDec;
        types = createTypes();
    }

    protected Dec dec() {
        return introducedSuperDec;
    }

    /**
     * Returns <code>true</code> is this introduction
     * places <code>implements</code> introductions on its
     * targets.
     *
     * @return <code>true</code> is this introduction
     *         places <code>implements</code> introductions
     *         on its targets.
     */
    public boolean isImplements() {
        return introducedSuperDec.getIsImplements();
    }

    /**
     * Returns the types that this introduction introduces
     * into it's targets type hierarchy.
     *
     * @return an array of org.aspectj.ajdoc.Type representing
     *         the types this introduction has introducted onto
     *         its targets type hierarchy.
     */
    public org.aspectj.ajdoc.Type[] types() {
        return (org.aspectj.ajdoc.Type[])types.toArray
            (new org.aspectj.ajdoc.Type[types.size()]);
    }

    /**
     * Returns the name of the type introduction. 
     *
     * @return the name.
     */
    public String name() { if (true) return ""; // XXX unimplemented
        return (introducedSuperDec.getTypeDs().size() != 0)
            ?  ((org.aspectj.ajdoc.Type)introducedSuperDec.getTypeDs().
                get(0).getType()).typeName()
            :  "";
        //TODO: This could fuck us up!!!
    }

    private final Collection createTypes() {
        TypeDs typeds = introducedSuperDec.getTypeDs();
        if (typeds == null) return Collections.EMPTY_LIST;
        List list = new ArrayList();
        for (int i = 0, N = typeds.size(); i < N; i++) {
            list.add(ClassDocImpl.getInstance(typeds.get(i).getType().getTypeDec()));
        }
        return list;
    }
}
