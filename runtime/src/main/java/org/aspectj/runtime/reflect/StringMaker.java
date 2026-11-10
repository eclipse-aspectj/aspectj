/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.runtime.reflect;

import java.lang.reflect.Modifier;

class StringMaker {
    boolean shortTypeNames = true;
    boolean includeArgs = true;
    boolean includeThrows = false;
    boolean includeModifiers = false;
    boolean shortPrimaryTypeNames = false;

    boolean includeJoinPointTypeName = true;
    boolean includeEnclosingPoint = true;
    boolean shortKindName = true;
    int cacheOffset;

    static StringMaker shortStringMaker;
    static {
        shortStringMaker = new StringMaker();
        shortStringMaker.shortTypeNames = true;
        shortStringMaker.includeArgs = false;
        shortStringMaker.includeThrows = false;
        shortStringMaker.includeModifiers = false;
        shortStringMaker.shortPrimaryTypeNames = true;

        shortStringMaker.includeJoinPointTypeName = false;
        shortStringMaker.includeEnclosingPoint = false;

		shortStringMaker.cacheOffset = 0;
    }

    static StringMaker middleStringMaker;
    static {
        middleStringMaker = new StringMaker();
        middleStringMaker.shortTypeNames = true;
        middleStringMaker.includeArgs = true;
        middleStringMaker.includeThrows = false;
        middleStringMaker.includeModifiers = false;
        middleStringMaker.shortPrimaryTypeNames = false;

		shortStringMaker.cacheOffset = 1;
    }

    static StringMaker longStringMaker;
    static {
        longStringMaker = new StringMaker();
        longStringMaker.shortTypeNames = false;
        longStringMaker.includeArgs = true;
        longStringMaker.includeThrows = false;
        longStringMaker.includeModifiers = true;
        longStringMaker.shortPrimaryTypeNames = false;
        longStringMaker.shortKindName = false;

		longStringMaker.cacheOffset = 2;
    }

    String makeKindName(String name) {
        int dash = name.lastIndexOf('-');
        if (dash == -1) return name;
        return name.substring(dash+1);
    }

    String makeModifiersString(int modifiers) {
        if (!includeModifiers) return "";
        String str = Modifier.toString(modifiers);
        if (str.length() == 0) return "";
        return str + " ";
    }

    String stripPackageName(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1) return name;
        return name.substring(dot+1);
    }

    String makeTypeName(Class<?> type, String typeName, boolean shortName) {
        if (type == null) return "ANONYMOUS";
        if (type.isArray()) {
        	Class<?> componentType = type.getComponentType();
        	return makeTypeName(componentType, componentType.getName(), shortName) + "[]";
        }
        String name = nestingSeparatorsAsDots(type, typeName);
        return shortName ? stripPackageName(typeName, name) : name;
    }

    /** As stripPackageName(String) but for name, which is typeName with some '$' replaced, so the package is found in typeName. */
    private static String stripPackageName(String typeName, String name) {
        return name.substring(typeName.lastIndexOf('.') + 1);
    }

    /**
     * The binary name of a nested class is the binary name of the class it is nested in, then a '$', then its own name
     * (Outer$Inner is shown as Outer.Inner). Those are the only '$' that are replaced: a '$' can be anywhere else in a name too
     * (package names, the names of the classes, generated classes such as JDK and CGLIB proxies and lambdas) and has to be kept.
     * <p>
     * Only the class itself can tell what is nested in what. Names without a '$' (nearly all of them) do not need to ask, and
     * generated classes have no enclosing class so asking does not load anything. For a nested class the classes it is nested
     * in are loaded (not initialized), if they were not already.
     */
    private static String nestingSeparatorsAsDots(Class<?> type, String name) {
        if (name.indexOf('$') == -1) return name;
        try {
            char[] chars = null;
            for (Class<?> c = type.getEnclosingClass(); c != null; c = c.getEnclosingClass()) {
                // the name should be the name of c, a '$', then the rest of the name: the '$' there is the one to replace
                String outerName = c.getName();
                int separator = outerName.length();
                boolean nameIsOuterNameThenDollar = name.startsWith(outerName) && name.length() > separator
                        && name.charAt(separator) == '$';
                if (!nameIsOuterNameThenDollar) {
                    return name.replace('$', '.'); // the name is not that of the class: do what has always been done
                }
                if (chars == null) chars = name.toCharArray();
                chars[separator] = '.';
            }
            return chars == null ? name : new String(chars);
        } catch (LinkageError | SecurityException e) {
            return name.replace('$', '.'); // cannot tell (an outer class is not available, say): do what has always been done
        }
    }

    public String makeTypeName(Class<?> type) {
        return makeTypeName(type, type.getName(),shortTypeNames);
    }

    public String makePrimaryTypeName(Class<?> type, String typeName) {
        return makeTypeName(type, typeName, shortPrimaryTypeNames);
    }

    public void addTypeNames(StringBuffer buf, Class[] types) {
        for (int i = 0; i < types.length; i++) {
            if (i > 0) buf.append(", ");
            buf.append(makeTypeName(types[i]));
        }
    }

    public void addSignature(StringBuffer buf, Class[] types) {
        if (types == null) return;
        if (!includeArgs) {
            if (types.length == 0) {
                buf.append("()");
                return;
            } else {
                buf.append("(..)");
                return;
            }
        }
        buf.append("(");
        addTypeNames(buf, types);
        buf.append(")");
    }

    public void addThrows(StringBuffer buf, Class[] types) {
        if (!includeThrows || types == null || types.length == 0) return;

        buf.append(" throws ");
        addTypeNames(buf, types);
    }
}
