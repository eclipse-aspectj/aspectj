/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
    
    String makeTypeName(Class type, String typeName, boolean shortName) {
        if (type == null) return "ANONYMOUS";
        if (type.isArray()) {
        	Class componentType = type.getComponentType();
        	return makeTypeName(componentType, componentType.getName(), shortName) + "[]";
        }
        if (shortName) {
            return stripPackageName(typeName).replace('$', '.');
        } else {
            return typeName.replace('$', '.');
        }
    }
       
    public String makeTypeName(Class type) {
        return makeTypeName(type, type.getName(),shortTypeNames);
    }
    
    public String makePrimaryTypeName(Class type, String typeName) {
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
