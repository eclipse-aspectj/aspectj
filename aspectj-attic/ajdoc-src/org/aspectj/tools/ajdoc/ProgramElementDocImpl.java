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
import org.aspectj.compiler.base.ast.Modifiers;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.crosscuts.AspectJCompiler;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;

import java.lang.reflect.Modifier;

public abstract class ProgramElementDocImpl
    extends    DocImpl
    implements org.aspectj.ajdoc.ProgramElementDoc {

    /** The containing ClassDoc. */
    private final ClassDoc containingClass;
    
    /*
     construction of ProgramElementDocImpl must not recursively 
     invoke construction of instances of subclass ClassDocImpl 
     or endless recursion will be possible
    */
    public ProgramElementDocImpl(ClassDoc containingClass) {
        this.containingClass = containingClass;
    }
    
    /**
     * Returns the underlying Dec used for a number of methods.
     *
     * @return the underlying Dec used for a number of methods.
     */
    protected abstract Dec dec();

    /**
     * Returns the AspectJCompiler that this tree.  Delegates
     * to the underlying {@link #dec()}.
     *
     * @return the AspectJCompiler that compiled this tree.
     */
    protected final AspectJCompiler ajc() {
        return (AspectJCompiler)dec().getCompiler();
    }

    /**
     * Returns this's Comment.
     *
     * @return the Comment for this Doc.
     */
    public Comment getComment() {
        if (super.getComment() == null) {
            setComment(new Comment(this, dec().getFormalComment(), err()));
        }
        return super.getComment();
    }
   
    /**
     * Returns the nearest enclosing class.  The returned value
     * is <code>null</code> if this is a top-level entity.
     *
     * @return a ClassDoc representing the nearest
     *         enclosing class.  This can be null for
     *         top-level entities.
     */
    public ClassDoc containingClass() {
        return containingClass;
    }

    /**
     * Returns the package in which this Dec was declared.
     *
     * @return a PackageDoc representing the package
     *         in which this Dec was declared.
     */
    public PackageDoc containingPackage() {
        return PackageDocImpl.getPackageDoc(nonNullTypeDec());
    }

    /**
     */
    public TypeDec nonNullTypeDec() {
        if (dec().getDeclaringType() == null) return null;
        return ((NameType)dec().getDeclaringType()).getTypeDec();
    }

    /**
     * An int form of this Dec's modifiers.
     *
     * @return an int form this Dec's modifiers.
     * @see    java.lang.reflect.Modifier
     */
    public int modifierSpecifier() {
        return dec().getModifiers().getValue();
    }
    
    /**
     * Returns the modifiers as a String.
     *
     * @return a String representing to modifiers.
     */
    public String modifiers() {
        return Modifier.toString(modifierSpecifier());
    }

    /**
     * Returns <code>true</code> if this is <code>public</code>.
     *
     * @return <code>true</code> if this is <code>public</code>.
     */
    public boolean isPublic() {
        return dec().isPublic();
    }

    /**
     * Returns <code>true</code> if this is <code>protected</code>.
     *
     * @return <code>true</code> if this is <code>protected</code>.
     */
    public boolean isProtected() {
        return dec().isProtected();
    }

    /**
     * Returns <code>true</code> if this is <i>package private</i>.
     *
     * @return <code>true</code> if this is <i>package private</i>.
     */
    public boolean isPackagePrivate() {
        Modifiers mods = dec().getModifiers();
        // todo: consider creating Dec.isPackagePrivate()
        // todo: consider NPE if mods null
        return ((null != mods) && mods.isPackagePrivate());
    }

    /**
     * Returns <code>true</code> if this is <code>private</code>.
     *
     * @return <code>true</code> if this is <code>private</code>.
     */
    public boolean isPrivate() {
        return dec().isPrivate();
    }

    /**
     * Returns <code>true</code> if this is <code>static</code>.
     *
     * @return <code>true</code> if this is <code>static</code>.
     */
    public boolean isStatic() {
        return dec().isStatic();
    }

    /**
     * Returns <code>true</code> if this is <code>final</code>.
     *
     * @return <code>true</code> if this is <code>final</code>.
     */
    public boolean isFinal() {
        return dec().isFinal();
    }

    /**
     * Returns the fully-qualified type name of this Dec (not member name).
     *
     * @return the fully-qualified name.
     */
    public String qualifiedName() {
        return name();
    }
    
    /**
     * Returns the name -- e.g. ID.
     *
     * @return the name of the Dec.
     */
    public String name() {
        return dec().getId();
    }  
    
}
