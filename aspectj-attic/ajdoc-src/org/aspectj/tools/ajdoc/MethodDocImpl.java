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

import org.aspectj.compiler.base.ast.MethodDec;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.TypeDec;

import com.sun.javadoc.ClassDoc;

import java.lang.reflect.Modifier;

public class MethodDocImpl
    extends    CodeDocImpl
    implements org.aspectj.ajdoc.MethodDoc {

    /*
     * This is a hack because the compiler isn't resolving
     * introduced types.
     */
    private org.aspectj.compiler.base.ast.Type type;
    public void setType(org.aspectj.compiler.base.ast.Type type) {
        this.type = type;
    }

    public MethodDocImpl(ClassDoc containingClass, MethodDec methodDec) {
        super(containingClass, methodDec);
        setType(codeDec().getResultTypeD().getType());
    }
    
    protected MethodDec methodDec() {
        return (MethodDec)codeDec();
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>.
     */
    public boolean isMethod() {
        return true;
    }

    /**
     * Returns <code>true</code> if this method is <code>abstract</code>.
     *
     * @return <code>true</code> if this method is <code>abstract</code>.
     */
    public boolean isAbstract() {
        return methodDec().isAbstract();
    }
    
    /**
     * Returns the return type of this method.
     *
     * @return the Type representing the type this
     *         method returns.
     */
    public com.sun.javadoc.Type returnType() {
        return TypeImpl.getInstance(type);
        //return null; //TODO getResultTypeD().getType();
    }

    /**
     * Returns the type that nearest super class that defines
     * this method.
     *
     * @return the type that nearest super class that defines
     *         this method.
     */
    public ClassDoc overriddenClass() {
        //Exprs params = getFormals().makeExprs(); // do this for side-effect?XXX
        TypeDec where = methodDec().getDeclaringType().getTypeDec();
        NameType superType = (NameType)where.getSuperClassType();
        while (superType != null) {
            MethodDec method = Util.methodDec(superType,
                                              methodDec().getId(),
                                              methodDec().getFormals());
            if (method != null && !method.getId().equals("not$found")) {
                if (method.getDeclaringType().equals(superType)) {
                    return null;
                }
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
     * Returns the int modifiers for this method.
     *
     * @return the int modifiers for this method.
     * @see    java.lang.reflect.Modifier
     */
    public int modifierSpecifier() {
        //XXX interface methods have the ABSTRACT bit set
        if (containingClass().isInterface()) {
            return super.modifierSpecifier() & ~Modifier.ABSTRACT;
        }
        return super.modifierSpecifier();
    }
}
