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

import org.aspectj.ajdoc.ClassDoc;
import org.aspectj.ajdoc.PointcutDoc;
import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.Formals;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.TypeD;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.base.ast.TypeDs;
import org.aspectj.compiler.crosscuts.ast.PointcutDec;

import java.util.Collection;
import java.util.Collections;

public class PointcutDocImpl
    extends ExecutableMemberDocImpl
    implements PointcutDoc {

    /** The pointcut to which we delegate. */
    private final PointcutDec pointcutDec;

    public PointcutDocImpl(ClassDoc containingClass, PointcutDec pointcutDec) {
        super(containingClass);
        this.pointcutDec = pointcutDec;
    }

    /**
     * Returns a empty list because advice cannot
     * be placed on a pointcut.
     *
     * @return Collection.EMPTY_LIST;
     */
    protected Collection createAdvice() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns the underlying Dec -- a PointcutDec.
     *
     * @return the underlying Dec -- a PointcutDec.
     */
    protected Dec dec() {
        return pointcutDec;
    }

    /**
     * Returns the Formals of the underlying PointcutDec.
     *
     * @return the Formals of the underlying PointcutDec.
     */
    protected Formals getFormals() {
        return pointcutDec.getFormals();
    }

    /**
     * Returns null because pointcut cannot throw execptions.
     *
     * @return null.
     */
    public TypeDs getThrows() {
        return null;
    }

    /**
     * Returns the return type of this method.
     *
     * @return the Type representing the type this
     *         method returns.
     */
    public com.sun.javadoc.Type resultType() {
        TypeD typed = pointcutDec.getResultTypeD();
        if (typed == null) return null; //TODO: maybe return VOID
        return null; //TODOtyped.getType();
    }

    /**
     * Returns the type that nearest super class that defines
     * this method.
     *
     * @return the type that nearest super class that defines
     *         this method.
     */
    public com.sun.javadoc.ClassDoc overriddenClass() {
        //TODO: This sucks!!!
        TypeDec where = pointcutDec.getDeclaringType().getTypeDec();
        NameType superType = (NameType)where.getSuperClassType();
        while (superType != null) {
            PointcutDec pc = Util.pointcutDec(superType,
                                              pointcutDec.getId(),
                                              pointcutDec.getFormals());
            if (pc != null && !pc.getId().equals("NOT_FOUND")) {
                // XXX TypeDec result = superType.getTypeDec();
                return null; //TODOresult;
            }
            if (superType.getTypeDec().getFullName().
                equals("java.lang.Object")) {
                return null;
            }
            superType = (NameType)superType.getTypeDec().getSuperClassType();
        }
        return null;
    }
    
    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>.
     */
    public boolean isPointcut() {
        return true;
    }
    
    /**
     * Returns <code>true</code> if this method is <code>abstract</code>.
     *
     * @return <code>true</code> if this method is <code>abstract</code>.
     */
    public boolean isAbstract() {
        return pointcutDec.isAbstract();
    }
}
