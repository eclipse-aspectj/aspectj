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
import org.aspectj.compiler.base.ast.Formals;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.TypeDs;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ThrowsTag;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class ExecutableMemberDocImpl
    extends MemberDocImpl
    implements ExecutableMemberDoc {

    /*
     * These three fields should be final, but can't because
     * they contain calls to abstract methods that aren't
     * valid until after the subclasses constructor is run.
     * To compensate the accessor methods lazily evaluate
     * them, and these methods are final.
     */

    /** The collection of advice placed on each ExecutableMemberDoc. */
    private Collection advice;

    /** The List of parameters. */
    private Collection parameters;

    /** The List of thrown exceptions. */
    private Collection thrownExceptions;

    /** The full signature. */
    private String signature;

    /** The flat signature. */
    private String flatSignature;

    /**
     * Constructs a new Doc with the enclosing ClassDoc.
     *
     * @param containingClass enclosing ClassDoc.
     */
    public ExecutableMemberDocImpl(ClassDoc containingClass) {
        super(containingClass);
    }
    
    /**
     * Returns a non-null Collection of AdviceDoc
     * representing the advice placed on the underlying Dec.
     *
     * @return a non-null Collection of AdviceDoc
     *         representing the advice placed on the
     *         underlying Dec.
     */
    protected abstract Collection createAdvice();

    /**
     * Returns the Formals of the underlying Dec.
     *
     * @return the Formals of the underlying Dec.
     */
    protected abstract Formals getFormals();

    /**
     * Returns the TypeDs representing the exceptions
     * thrown by the underlying Dec.
     *
     * @return the TypeDs representing the exceptions
     *         thrown by the underlying Dec.
     */
    protected abstract TypeDs getThrows();


    /**
     * Converts the passed in Formals to a Collection of
     * Parameter and sets our parameters to this value.
     *
     * @param formals the Formals to use.
     */
    public void makeParameters(Formals formals) {
        parameters = createParameters(formals);
    }

    /**
     * Converts the passed in TypeDs to a Collection of
     * ClassDoc and sets our thrownExceptions to this value.
     *
     * @param thrown the TypeDs to use.
     */
    public void makeThrownExceptions(TypeDs thrown) {
        thrownExceptions = createThrownExceptions(thrown);
    }

    /**
     * Returns the advice affecting this member.
     *
     * @return an array of AdviceDoc representing the advice
     *         affecting this member.
     */
    public final AdviceDoc[] advice() {
        if (advice == null) advice = createAdvice();
        return (AdviceDoc[])advice.toArray(new AdviceDoc[advice.size()]);
    }

    /**
     * Returns the exceptions this code declares to throw.
     *
     * @return an array of ClassDoc representing the exceptions
     *         this code declares to throw.
     */
    public final ClassDoc[] thrownExceptions() {
        if (thrownExceptions == null) makeThrownExceptions(getThrows());
        return (ClassDoc[])thrownExceptions.toArray
            (new ClassDoc[thrownExceptions.size()]);
    }

    /**
     * Returns the parameters taken by this code.
     *
     * @return an array of Parameter representing the
     *         parameters this code takes.
     */
    public final Parameter[] parameters() {
        if (parameters == null) makeParameters(getFormals());
        return (Parameter[])parameters.toArray
            (new Parameter[parameters.size()]);
    }

    /**
     * Returns the flat signature.
     *
     * @return the flat signature with all types unqualified.
     */
    public String flatSignature() {
        if (flatSignature == null) {
            flatSignature = Util.flatSignature(parameters());
        }
        return flatSignature;
    }

    /**
     * Returns the full signature.
     *
     * @return the full signature with all types qualified.
     */
    public String signature() {
        if (signature == null) {
            signature = Util.signature(parameters());
        }
        return signature;
    }

    /**
     * Returns <code>true</code> if this code is <code>synchronized</code>.
     *
     * @return <code>true</code> if this code is <code>synchronized</code>.
     */
    public boolean isSynchronized() {
        //return getModifiers().isSynchronized();
        return Modifier.isSynchronized(modifierSpecifier());
    }
    
    /**
     * Returns <code>true</code>if this code is <code>native</code>.
     *
     * @return <code>true</code>if this code is <code>native</code>.
     */
    public boolean isNative() {
        //return (modifierSpecifier() & Modifiers.NATIVE) != 0;
        return Modifier.isNative(modifierSpecifier());
    }

    /**
     * Returns the throw tags describing exceptions thrown
     * declared by this code.
     *
     * @return an array of ThrowsTag representing the exception
     *         this code declares to throw.
     */
    public ThrowsTag[] throwsTags() {
        return getComment().throwsTags();
    }

    /**
     * Returns the param tags describing parameters taken
     * by this code.
     *
     * @return an array of ParamTag representing the
     *         parameters taken by this code.
     */
    public ParamTag[] paramTags() {
        return getComment().paramTags();
    }

    /**
     * Returns the simple name followed the parameters
     * enclosed in parens.
     *
     * @return the simple name followed the parameters
     *         enclosed in parens.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(name());
        sb.append('(');
        Parameter[] params = parameters();
        for (int i = 0, N = params.length; i < N; i++) {
            if (i > 0) sb.append(",");
            sb.append(params[i].type().qualifiedTypeName());
            sb.append(params[i].type().dimension());
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Returns a Collection of Parameter corresponding to
     * the Formals passed in.
     *
     * @param formals the Formals to use.
     * @return        a Collection of Parameter corresponding to
     *                the Formals passed in.
     */
    private Collection createParameters(Formals formals) {
        if (formals == null) return Collections.EMPTY_LIST;
        List list = new ArrayList(formals.size());
        for (int i = 0, N = formals.size(); i < N; i++) {
            list.add(new ParameterImpl(formals.get(i)));
        }
        return list;
    }

    /**
     * Returns a Collection of ClassDoc corresponding to
     * the TypeDs passed in.
     *
     * @param thrown the TypeDs to use
     * @return        a Collection of ClassDoc corresponding to
     *                the TypeDs passed in.
     */
    private Collection createThrownExceptions(TypeDs typeds) {
        if (typeds == null) return Collections.EMPTY_LIST;
        List list = new ArrayList(typeds.size());
        for (int i = 0, N = typeds.size(); i < N; i++) {
            list.add(ClassDocImpl.getInstance
                     (((NameType)typeds.get(i).getType()).getTypeDec()));
        }
        return list;
    }

    /**
     * Returns <code>true</code> if the passed in Object is
     * an ExecutableMemberDocImpl, its name equals ours, and
     * its parameters <code>equals</code> ours.
     *
     * @return equality based on name and parameters.
     */
    public boolean weakEquals(Object md) {
        if (!(md instanceof ExecutableMemberDocImpl)) {
            return false;
        }
        ExecutableMemberDocImpl emdi = (ExecutableMemberDocImpl)md;
        if (!name().equals(emdi.name())) {
            return false;
        }
        Parameter[] ourPds = this.parameters();
        Parameter[] edsPds = emdi.parameters();
        if (ourPds.length != edsPds.length) {
            return false;
        }
        for (int i = 0, N = ourPds.length; i < N; i++) {
            if (!ourPds[i].equals(edsPds[i])) {
                return false;
            }
        }
        return true;
    }
}
