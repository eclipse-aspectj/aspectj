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

import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.FieldDec;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.SerialFieldTag;

import java.lang.reflect.Modifier;

public class FieldDocImpl
    extends    MemberDocImpl
    implements org.aspectj.ajdoc.FieldDoc {

    /** The FieldDec that corresponds to this FieldDoc. */
    private final FieldDec field;

    /**
     * The type of this field.  This can't be set initially
     * because the compiler doesn't resolve the types for
     * introductions, yet, so we have to allow others to set it.
     */
    private org.aspectj.compiler.base.ast.Type type;

    /**
     * Sets the org.apectj.compiler.base.ast.Type used to return
     * the com.sun.javadoc.Type.
     *
     * @param type the new org.aspectj.compiler.base.ast.Type used
     *             to find the com.sun.javadoc.Type.
     * @hack       This is only needed because of unresolved
     *             introduced fields.
     */
    public void setType(org.aspectj.compiler.base.ast.Type type) {
        this.type = type;
    }

    public FieldDocImpl(ClassDoc containingClass, FieldDec field) {
        super(containingClass);
        this.field = field;
        setType(field.getType());
    }

    protected Dec dec() {
        return field;
    }

    
    protected FieldDec fieldDec() {
        return field;
    }

    /**
     * Returns <code>true</code>.
     *
     * @return <code>true</code>.
     */
    public boolean isField() {
        return true;
    }

    /**
     * Returns the type of this field.
     *
     * @return the type of this field.
     */
    public com.sun.javadoc.Type type() {
        return TypeImpl.getInstance(type);
    }

    /**
     * Return <code>true</code> is this field is <code>volatile</code>.
     *
     * @return <code>true</code> is this field is <code>volatile</code>.
     */
    public boolean isVolatile() {
        return Modifier.isVolatile(modifierSpecifier());
    }

    /**
     * Return <code>true</code> is this field is <code>transient</code>.
     *
     * @return <code>true</code> is this field is <code>transient</code>.
     */
    public boolean isTransient() {
        return Modifier.isTransient(modifierSpecifier());
    }

    /**
     * Returns the serial field tags for this field.
     *
     * @return an array of SerialFieldTag representing the
     *         serial field tags for this field.
     */
    public SerialFieldTag[] serialFieldTags() {
        return getComment().serialFieldTags();
    }

    /**
     * Returns the name of the field.
     *
     * @return the name of the field.
     */
    public String toString() {
        return name();
    }

    /**
     * Returns <code>true</code> is <code>md</code> is a
     * FieldDocImpl and has the same name.
     *
     * @return <code>true</code> is <code>md</code> is a
     *         FieldDocImpl and has the same name.
     */
    public boolean weakEquals(Object md) {
        if (!(md instanceof FieldDocImpl)) return false;
        return name().equals(((FieldDocImpl)md).name());
    }
}
