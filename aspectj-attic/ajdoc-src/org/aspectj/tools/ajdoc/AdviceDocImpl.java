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


import org.aspectj.ajdoc.AdviceDoc;
import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.ajdoc.ClassDoc;
import org.aspectj.ajdoc.ExecutableMemberDoc;
import org.aspectj.compiler.base.ast.CodeDec;
import org.aspectj.compiler.base.ast.FormalDec;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.crosscuts.ast.AdviceDec;
import org.aspectj.compiler.crosscuts.ast.AfterReturningAdviceDec;
import org.aspectj.compiler.crosscuts.ast.AfterThrowingAdviceDec;
import org.aspectj.compiler.crosscuts.ast.AroundAdviceDec;

import com.sun.javadoc.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AdviceDocImpl extends CodeDocImpl implements AdviceDoc {

    /** Crosscuts this advice affects. */
    private final Collection crosscuts;

    /**
     * Constructrs an AdviceDoc with the containing ClassDoc
     * and underlying AdviceDec.
     *
     * @param containingClass containing ClassDoc.
     * @param adviceDec       underlying AdviceDec.
     */
     public AdviceDocImpl(ClassDoc containingClass, AdviceDec adviceDec) {
        super(containingClass, adviceDec);
        crosscuts = createCrosscuts();
    }

    /**
     * Returns the underlying Dec -- an AdviceDec.
     *
     * @return the underlying Dec -- an AdviceDec.
     */
    protected AdviceDec adviceDec() {
        return (AdviceDec)codeDec();
    }

    /**
     * Return the ExecutableMemberDocs this advice crosscuts.
     *
     * @return an array of ExecutableMemberDocs representing
     *         the members this advice crosscuts.
     */
    public com.sun.javadoc.ExecutableMemberDoc[] crosscuts() {
        return (ExecutableMemberDoc[])crosscuts.toArray
            (new ExecutableMemberDoc[crosscuts.size()]);
    }

    /**
     * Returns <code>null</code>, because advice can't override
     * other advice.
     *
     * @return <code>null</code>, because advice can't override
     *         other advice.
     */
    public AspectDoc overriddenAspect() {
        return null;
    }

    /**
     * Returns the return type of the advice -- it may be null.
     *
     * @return the return type of the advice -- it may be null.
     */
    public com.sun.javadoc.Type returnType() {
        if (adviceDec() instanceof AroundAdviceDec) {
            return TypeImpl.getInstance(adviceDec().getReturnType());
        } else {
            return null;
        }
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>.
     */
    public boolean isAdvice() {
        return true;
    }

    /**
     * Returns <code>true</code> if this advice is <code>abstract</code>.
     *
     * @return <code>true</code> if this advice is <code>abstract</code>.
     */
    public boolean isAbstract() {
        return adviceDec().isAbstract();
    }

    
    /**
     * Returns <code>true</code> if this is <code>throwing</code> advice.
     *
     * @return <code>true</code> if this is <code>throwing</code> advice.
     */
    public boolean isThrowing() {
        return adviceDec() instanceof AfterThrowingAdviceDec;
    }

    /**
     * Returns <code>true</code> if this is <code>returning</code> advice.
     *
     * @return <code>true</code> if this is <code>returning</code> advice.
     */
    public boolean isReturning() {
        return adviceDec() instanceof AfterReturningAdviceDec;
    }

    /**
     * Returns the extra formal type that's the optional type
     * to <code>after returning</code> or <code>after throwing</code>
     * advice.
     *
     * @return an instance of Type that represents the the extra formal type
     *         that's the optional type to <code>after returning</code> or
     *         <code>after throwing</code> advice.
     */
    public Type extraType() {
        FormalDec fd = adviceDec().getExtraFormal();
        if (fd != null) {
            return TypeImpl.getInstance(fd.getType());
        }
        return null;
    }


    /**
     * Returns a Collection of CodeDocImpl representing the
     * crosscuts the underlying TypeDec declares.
     *
     * @return a Collection of CodeDocImpl representing the
     *         crosscuts the underlying TypeDec declares.
     */
    private Collection createCrosscuts() {
        Set affects = ajc().getCorrespondences().getAffects(adviceDec());
        if (affects.size() < 1) return Collections.EMPTY_LIST;
        List list = new ArrayList();
        for (Iterator i = affects.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof CodeDec) {
                CodeDec cdec = (CodeDec)o;
                TypeDec owner = ((NameType)cdec.getDeclaringType()).getTypeDec();
                ClassDocImpl cd = ClassDocImpl.getInstance(owner);
                CodeDocImpl cdoc = cd.docForDec(cdec);
                if (cdoc != null) { // todo: silent introduced members
                    list.add(cdoc);
                }
            }
        }
        return list;
    }

    /**
     * Returns the simple name of this advice.  Need to override
     * this so we don't print afterThrowing or afterReturning.
     *
     * @return one of after, before, around.
     */
    public String name() {
        if (isThrowing() || isReturning())  return "after";
        return super.name();
    }
}
