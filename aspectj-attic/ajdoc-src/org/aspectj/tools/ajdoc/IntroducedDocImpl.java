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
import org.aspectj.ajdoc.IntroducedDoc;
import org.aspectj.ajdoc.MemberDoc;
import org.aspectj.compiler.base.ast.ConstructorDec;
import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.FieldDec;
import org.aspectj.compiler.base.ast.MethodDec;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.crosscuts.ast.GenTypeName;
import org.aspectj.compiler.crosscuts.ast.IntroducedDec;

import java.util.Iterator;
import java.util.Set;

public class IntroducedDocImpl extends IntroductionDocImpl implements IntroducedDoc {

    /** The introduction to which we delegate. */
    private final IntroducedDec introducedDec;

    /** The member this introduction introduced. */
    private final MemberDocImpl member;

    public IntroducedDocImpl(com.sun.javadoc.ClassDoc containingClass,
                             IntroducedDec introducedDec) {
        super(containingClass);
        this.introducedDec = introducedDec; // used by findMember
        (member = findMember()).setIntroduced(this);
        createTargets();
    }

    protected Dec dec() {
        return introducedDec;
    }

    protected void createTargets() {
        /*
         * HACK:
         * Because the compiler doesn't resolve the types
         * of introductions, yet, we have to set the introduced
         * doc (member) with the appropriate fields, whether it's a
         *  - field
         *  - method
         *  - constructor
         */
        Set affects = ajc().getCorrespondences().getAffects(introducedDec);
        if (affects.size() < 1) return;
    nextType:
        for (Iterator it = affects.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof TypeDec) {
                TypeDec owner = (TypeDec)o;
                ClassDoc cd = ClassDocImpl.getInstance(owner);
                com.sun.javadoc.FieldDoc[] fs = cd.fields();
                for (int i = 0; i < fs.length; i++) {
                    if (member.weakEquals(fs[i])) { // XXX weakEquals is unimplemented
                        ((FieldDocImpl)fs[i]).setIntroduced(this);
                        addTarget(cd);
                        ((FieldDocImpl)member).setType(((FieldDocImpl)fs[i]).
                                                       fieldDec().getType());
                        // why fixup only fields?
                        continue nextType;
                    }
                }
                com.sun.javadoc.MethodDoc[] ms = cd.methods();
                for (int i = 0; i < ms.length; i++) {
                    if (member.weakEquals(ms[i])) {
                        ((MethodDocImpl)ms[i]).setIntroduced(this);
                        addTarget(cd);
                        ((MethodDocImpl)member).setType(((MethodDocImpl)ms[i]).
                                                        codeDec().getResultTypeD().
                                                        getType());
                        ((ExecutableMemberDocImpl)member).
                            makeParameters(((MethodDocImpl)ms[i]).
                                           codeDec().getFormals());
                        continue nextType;
                    }
                }
                com.sun.javadoc.ConstructorDoc[] cs = cd.constructors();
                for (int i = 0; i < cs.length; i++) {
                    if (member.weakEquals(cs[i])) {
                        ((ConstructorDocImpl)cs[i]).setIntroduced(this);
                        addTarget(cd);
                        ((ExecutableMemberDocImpl)member).
                            makeParameters(((ConstructorDocImpl)cs[i]).
                                           codeDec().getFormals());
                        continue nextType;
                    }
                }
            }
        }
    }

    public MemberDoc member() {
        return member;
    }

    /**
     * Returns the name of the member introduction.
     *
     * @return the name.
     */
    public String name() { // XXX unused?
        Dec indec = introducedDec.getDec();
        if (indec != null) {
            return "" + indec.getId(); // XXX
        } else {
            return "";
        }
    }
    
    private MemberDocImpl findMember() {
        Dec dec = introducedDec.getDec();
        // fix applied all, though bug was only in methods and constructors
        // verified working in all, including fields
        dec.setSourceLocation(introducedDec.getSourceLocation()); // PR790, 712
        //TODO: a little hacky now
        if (dec instanceof FieldDec) {
            return new FieldDocImpl(containingClass(),
                                      (FieldDec)dec);

        } else if (dec instanceof ConstructorDec) {
            return new ConstructorDocImpl(containingClass(),
                                          (ConstructorDec)dec);
        } else if (dec instanceof MethodDec) {
            return new MethodDocImpl(containingClass(),
                                     (MethodDec)dec);
        } else {
            return null;
        }
        // should print type pattern for type of introduced member,
        // but it messes up source/target associations
//      GenTypeName gtn = introducedDec.getTargets();
//      if (null != gtn) {
//         name = gtn.toShortString() + name;
//      }
    }
 
     /**
      * Returns the toString() of the member.
      *
      * @return the toString() of the member.
      */
     public String toString() {
         return member.toString();
     }
}
