/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
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
        
    }
    
    static StringMaker middleStringMaker;
    static {
        middleStringMaker = new StringMaker();
        middleStringMaker.shortTypeNames = true;
        middleStringMaker.includeArgs = true;
        middleStringMaker.includeThrows = false;
        middleStringMaker.includeModifiers = false;
        middleStringMaker.shortPrimaryTypeNames = false;
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
    
    String makeTypeName(Class type, boolean shortName) {
        if (type == null) return "ANONYMOUS";
        if (type.isArray()) return makeTypeName(type.getComponentType(), shortName) + "[]";
        if (shortName) {
            return stripPackageName(type.getName()).replace('$', '.');
        } else {
            return type.getName().replace('$', '.');
        }
    }
       
    public String makeTypeName(Class type) {
        return makeTypeName(type, shortTypeNames);
    }
    
    public String makePrimaryTypeName(Class type) {
        return makeTypeName(type, shortPrimaryTypeNames);
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
