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

import org.aspectj.compiler.base.ast.ConstructorDec;
import org.aspectj.compiler.base.ast.Dec;
import org.aspectj.compiler.base.ast.FieldDec;
import org.aspectj.compiler.base.ast.MethodDec;
import org.aspectj.compiler.base.ast.Type;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.crosscuts.ast.PointcutDec;

import java.util.ArrayList;

/**
* Factory collection with Type-specific "add" methods which 
* checks if ..Dec should be included and if so constructs and adds.
* A Dec should be included if AccessChecker.canAccess({Dec variants}) 
* and the type is the same (if type is specified).
* This consolidates construction of DocImpl for classes and class members.
*/
public class FilteredDecList extends ArrayList {
    final protected AccessChecker filter ; 
    final protected ClassDocImpl classDocImpl ; 
    final protected Type declaringType ; 

    /**
     * Create a declaration list enforced by a filter and
     * optionally a ClassDocImpl.  If the ClassDocImpl is not null,
     * then you cannot add members outside its type; if it is null,
     * attempting to add members has undefined results.  (You may
     * still create ClassDoc.)
     * @param filter the AccessChecker used to test for inclusion
     *           (use AccessChecker.PUBLIC if null)
     * @param type the classDocImpl used to construct added members
     *             and ensure they are declared in this type
     *             (ignore test if null)
     */
    FilteredDecList(AccessChecker filter, ClassDocImpl classDocImpl) {
        this.filter = (null != filter ? filter
                       : AccessChecker.PUBLIC);
        this.classDocImpl = classDocImpl;
        TypeDec td = (null == classDocImpl? null
                              : classDocImpl.typeDec());
        this.declaringType = (null == td? null : td.getType());
    }

    /** 
     * Check for match with our type (if set)
     * @param dec the Dec to get the incoming declaring type from
     * @return true unless our type is set and does not equals dec declaring type 
     */
    protected final boolean sameType(Dec dec) {
        Type other = (null == dec ? null : dec.getDeclaringType());
        boolean result = ((null != dec) 
                          && ((declaringType == null) 
                              || declaringType.equals(other))) ;
        /*
          System.err.println("sameType("+dec+") " + declaringType 
          + " ?= " + other);
          if (!result) {
          System.err.println("false FilteredDecList.sameType(" + dec 
          + ") us " + declaringType);
          }
        */
        return result;
    }

    /**
     * Implements policy on incoming TypeDec.
     * @param dec the TypeDec to check
     * @throws IllegalArgumentException if null == dec
     */
    protected void checkDec(Dec dec) {
        if (null == dec) throw new IllegalArgumentException("null dec");
    }

    /**
     * Construct and add inner class from dec
     * if outer is included and dec should be included.
     * @param outer the ClassDocImpl which encloses this dec
     * @param dec the TypeDec for the inner class to add to this list, enclosed by outer
     * @return false if dec is null or true if added
     * @throws IllegalArgumentException if outer is null or dec is null
     */
    public boolean add(ClassDocImpl outer, TypeDec dec) {
        checkDec(dec);
        if (null == outer) throw new IllegalArgumentException("null outer");
        if ((filter.canAccess(outer.typeDec()) && filter.canAccess(dec))) {
            ClassDocImpl doc = ClassDocImpl.getInstance(outer, dec);
            if (null != doc) {
                doc.setIncluded(true);
                return add((Object) doc);
            }
        }
        denied(outer, dec);
        return false;
    }
            
    /** 
     * Add ClassDocImpl if dec should be included 
     * and ClassDocImpl.getInstance(..) returns something.
     * Also sets the included property to true;
     * @param dec the TypeDec for the class to add
     * @return false if dec is null or true if added
     * @throws IllegalArgumentException if outer is null or dec is null
     */
    public boolean add(TypeDec dec) {
        checkDec(dec);
        if (filter.canAccess(dec)) {
            ClassDocImpl doc = ClassDocImpl.getInstance(dec);
            if (null != doc) {
                doc.setIncluded(true);
                return add((Object) doc);
            }
        }
        denied(dec);
        return false;
    }

    /** 
     * Add MethodDocImpl to this list if dec should be included 
     * @param dec the MethodDoc for the method to add
     * @return true if added
     * @throws IllegalArgumentException if dec is null
     */
    public boolean add(MethodDec dec) {
        checkDec(dec);
        if (sameType(dec) && filter.canAccess(dec)) {
            return add((Object) new MethodDocImpl(classDocImpl, dec));
        }
        denied(dec);
        return false;
    }

    /** 
     * Add ConstructorDocImpl to this list if dec should be included 
     * @param dec the ConstructorDoc for the constructor to add
     * @return true if added
     * @throws IllegalArgumentException if dec is null
     */
    public boolean add(ConstructorDec dec) {
        checkDec(dec);
        if (sameType(dec) && filter.canAccess(dec)) {
            return add((Object) new ConstructorDocImpl(classDocImpl, dec));
        }
        denied(dec);
        return false;
    }

    /** 
     * Add FieldDocImpl to this list if dec should be included 
     * @param dec the FieldDoc for the field to add
     * @return true if added
     * @throws IllegalArgumentException if dec is null
     */
    public boolean add(FieldDec dec) {
        checkDec(dec);
        if (sameType(dec) && filter.canAccess(dec)) {
            return add((Object) new FieldDocImpl(classDocImpl, dec));
        }
        denied(dec);
        return false;
    }

    /** 
     * Add PointcutDocImpl to this list if dec should be included 
     * @param dec the PointcutDoc for the pointcut to add
     * @return true if added
     * @throws IllegalArgumentException if dec is null
     */
    public boolean add(PointcutDec dec) {
        checkDec(dec);
        if (sameType(dec) && filter.canAccess(dec)) {
            return add((Object) new PointcutDocImpl(classDocImpl, dec));
        }
        denied(dec);
        return false;
    }

    /** 
     * Called when some dec and outer is denied addition. 
     * Currently does nothing. 
     * @param outer the ClassDocImpl which encloses this dec
     * @param dec the TypeDec for the inner class to add to this list, enclosed by outer
     */
    protected void denied(ClassDocImpl outer, TypeDec dec) {
        // System.err.println(this + " denied " + o + " with " + p);
    }

    /** 
     * signalled when some dec is denied addition. 
     * Currently does nothing. 
     * @param dec the Dec denied addition
     */
    protected void denied(Dec dec) {
        // System.err.println(this + " denied " + o);
    }
} // class FilteredDecList

