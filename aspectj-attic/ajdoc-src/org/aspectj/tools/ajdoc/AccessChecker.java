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

import org.aspectj.compiler.base.ast.Modifiers;
import org.aspectj.compiler.base.ast.FieldDec;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.CodeDec;
import org.aspectj.compiler.crosscuts.ast.PointcutDec;

/**
 * This utility tells whether a declaration is accessible
 * based on its access modifiers (and that of its declaring
 * class, if it is a member (i.e., including inner classes).
 * <p><u>Instantiation and subclassing</u>: 
 * The constants should suffice for most uses, but subclassing
 * is permitted if you need to implement new functionality or
 * make new instances. 
 */
public abstract class AccessChecker {
    // constants open doCanAccess to public to permit direct use

    /** return true only for public elements */
    public static final AccessChecker PUBLIC = new AccessChecker("public") {
            public boolean doCanAccess(Modifiers mods, Object object) {
                return mods.isPublic();
            }
        };

    /** return true for public and protected elements */
    public static final AccessChecker PROTECTED = new AccessChecker("protected") {
            public boolean doCanAccess(Modifiers mods, Object object) {
                return mods.isPublic() || mods.isProtected();
            }
        };

    /** return true unless private elements */
    public static final AccessChecker PACKAGE = new AccessChecker("package") {
            public boolean doCanAccess(Modifiers mods, Object object) {
                return !mods.isPrivate();
            }
        };

    /** return true for all elements */
    public static final AccessChecker PRIVATE = new AccessChecker("private") {
            public boolean doCanAccess(Modifiers mods, Object object) {
                return true;
            }
        };

    /** lowercase option without - */
    protected final String optionName;

    /** 
     * Encourage use of static constants by prohibiting construction 
     * but permit new subclasses.
     * Subclasses should ensure optionName is lowercase and
     * doCanAccess is public if need be.
     */
    protected AccessChecker(String optionName){
        this.optionName = optionName;
    }

    /** @return true if modifiers permitted for self and declaring type */
    public boolean canAccess(FieldDec dec) {
        if (null == dec) return false;
        if (!canAccess(dec.getModifiers(), dec)) {
            return false;
        } else {
            return canAccess(dec.getBytecodeTypeDec());
        }
    }

    /** @return true if modifiers permitted for self and declaring type */
    public boolean canAccess(CodeDec dec) {
        if (null == dec) return false;
        if (!canAccess(dec.getModifiers(), dec)) {
            return false;
        } else {
            return canAccess(dec.getBytecodeTypeDec());
        }
    }

    /** @return true if modifiers permitted for self and declaring type */
    public boolean canAccess(PointcutDec dec) {
        if (null == dec) return false;
        if (!canAccess(dec.getModifiers(), dec)) {
            return false;
        } else {
            return canAccess(dec.getBytecodeTypeDec());
        }
    }

    /** decode dec modifiers and return whether access is permitted 
     *  Access is permitted if it is permitted to the dec.
     * The caller must prohibit access when displaying in the aspect
     * (i.e., <code>canAccess(dec.getLexicalType())</code> or in
     * the target class
     * (i.e., <code>canAccess(dec.getDeclaringType())</code>)
     * and to the enclosing lexical type (i.e,. the enclosing aspect).
     */
    /*
    public boolean canAccess(IntroducedDec dec) { // todo: users
        if (null == dec) return false;
        if (!canAccess(dec.getModifiers(), dec)) {
            return false;
        } else {
            return canAccess(dec.getLexicalType());
        }
    }
    */

    /** @return true if modifiers permitted for self and any enclosing type */
    public boolean canAccess(TypeDec dec) {
        if (null == dec) return false; 
        boolean result = canAccess(dec.getModifiers(), dec);
        if (result) {
            // avoiding NPE in getEnclosingInstanceTypeDec
            NameType outerType = dec.getEnclosingInstanceType();
            TypeDec outer = (null == outerType? null 
                             : outerType.getTypeDec()); // todo: typeDec?
            result = ((null == outer) || canAccess(outer));
        }
        return result;
    }

    /** 
     * This is called from <code>canAccess</code> to log any results 
     * of <code>doCanAccess</code>
     * and should return the result or a principled variant thereof.
     */
    protected boolean canAccessLog(Modifiers mods, Object object,
                                    boolean result) {
        return result;
    }

    /**
     * Check whether client has access to the object
     * based on the modifiers.
     * @param object the Object with the modifier flags - may be null
     * @param modifiers the Modifiers to check
     * @return false if modifiers are null or true if access is permitted
     */
    // todo: object unused but useful for logging
    public final boolean canAccess(Modifiers mods, Object object) {
        boolean result = (null == mods? false : doCanAccess(mods, object));
        return canAccessLog(mods, object, result);
    }

    /** @return lowercase variant of option name (e.g., "private" for -private) */
    public final String getOption() { return optionName; }

    /** @return UPPERCASE variant of option name (e.g., "PRIVATE" for -private) */
    public final String toString() { return optionName.toUpperCase(); }

    /** subclasses implement semantics here. */
    abstract protected boolean doCanAccess(Modifiers mods, Object object);
}
