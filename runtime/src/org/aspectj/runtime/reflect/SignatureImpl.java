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

import org.aspectj.lang.Signature;

import java.util.Hashtable;
import java.util.StringTokenizer;

abstract class SignatureImpl implements Signature {
    int modifiers = -1;
    String name;
    Class declaringType;
    
    SignatureImpl(int modifiers, String name, Class declaringType) {
        this.modifiers = modifiers;
        this.name = name;
        this.declaringType = declaringType;
    }
    
    abstract String toString(StringMaker sm);
    
    public final String toString() { return toString(StringMaker.middleStringMaker); }
    public final String toShortString() { return toString(StringMaker.shortStringMaker); }
    public final String toLongString() { return toString(StringMaker.longStringMaker); }

    public int getModifiers() {
        if (modifiers == -1) modifiers = extractInt(0);
        return modifiers;
    }
    public String getName() {
        if (name == null) name = extractString(1);
        return name;
    }
    public Class getDeclaringType() {
        if (declaringType == null) declaringType = extractType(2);
        return declaringType;
    }
    
    
    String fullTypeName(Class type) {
        if (type == null) return "ANONYMOUS";
        if (type.isArray()) return fullTypeName(type.getComponentType()) + "[]";
        return type.getName().replace('$', '.');
    }
    
    String stripPackageName(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1) return name;
        return name.substring(dot+1);
    }
    
    String shortTypeName(Class type) {
        if (type == null) return "ANONYMOUS";
        if (type.isArray()) return shortTypeName(type.getComponentType()) + "[]";
        return stripPackageName(type.getName()).replace('$', '.');
    }
    
    void addFullTypeNames(StringBuffer buf, Class[] types) {
        for (int i = 0; i < types.length; i++) {
            if (i > 0) buf.append(", ");
            buf.append(fullTypeName(types[i]));
        }
    }    
    void addShortTypeNames(StringBuffer buf, Class[] types) {
        for (int i = 0; i < types.length; i++) {
            if (i > 0) buf.append(", ");
            buf.append(shortTypeName(types[i]));
        }
    }
    
    void addTypeArray(StringBuffer buf, Class[] types) {
        addFullTypeNames(buf, types);
    }
    
    // lazy version
    String stringRep;
    ClassLoader lookupClassLoader = null;
    
    public void setLookupClassLoader(ClassLoader loader) {
        this.lookupClassLoader = loader;
    }
    
    private ClassLoader getLookupClassLoader() {
        if (lookupClassLoader == null) lookupClassLoader = this.getClass().getClassLoader();
        return lookupClassLoader;
    }
    
    public SignatureImpl(String stringRep) {
        this.stringRep = stringRep;
    }
    
    static final char SEP = '-';
   
    String extractString(int n) {
        //System.out.println(n + ":  from " + stringRep);        
        
        int startIndex = 0;
        int endIndex = stringRep.indexOf(SEP);
        while (n-- > 0) {
            startIndex = endIndex+1;
            endIndex = stringRep.indexOf(SEP, startIndex);
        }
        if (endIndex == -1) endIndex = stringRep.length();
        
        //System.out.println("    " + stringRep.substring(startIndex, endIndex));
        
        return stringRep.substring(startIndex, endIndex);  
    }
    
    int extractInt(int n) {
        String s = extractString(n);
        return Integer.parseInt(s, 16);
    }
    
    Class extractType(int n) {
        String s = extractString(n);
        return makeClass(s);
    }
    
    static Hashtable prims = new Hashtable();
    static {
        prims.put("void", Void.TYPE);
        prims.put("boolean", Boolean.TYPE);
        prims.put("byte", Byte.TYPE);
        prims.put("char", Character.TYPE);
        prims.put("short", Short.TYPE);
        prims.put("int", Integer.TYPE);
        prims.put("long", Long.TYPE);
        prims.put("float", Float.TYPE);
        prims.put("double", Double.TYPE);
    }
        
    Class makeClass(String s) {
        if (s.equals("*")) return null;
        Class ret = (Class)prims.get(s);
        if (ret != null) return ret;
        try {
            /* The documentation of Class.forName explains why this is the right thing
             * better than I could here.
             */
            ClassLoader loader = getLookupClassLoader();
            if (loader == null) {
                return Class.forName(s);
            } else {
                return loader.loadClass(s);
            }
        } catch (ClassNotFoundException e) {
            //System.out.println("null for: " + s);
            //XXX there should be a better return value for this
            return ClassNotFoundException.class;
        }
    }
    
    static String[] EMPTY_STRING_ARRAY = new String[0];
    static Class[] EMPTY_CLASS_ARRAY = new Class[0];
    
    static final String INNER_SEP = ":";
    
    String[] extractStrings(int n) {
        String s = extractString(n);
        StringTokenizer st = new StringTokenizer(s, INNER_SEP);
        final int N = st.countTokens();
        String[] ret = new String[N];
        for (int i = 0; i < N; i++) ret[i]= st.nextToken();
        return ret;
    }
    Class[] extractTypes(int n) {
        String s = extractString(n);
        StringTokenizer st = new StringTokenizer(s, INNER_SEP);
        final int N = st.countTokens();
        Class[] ret = new Class[N];
        for (int i = 0; i < N; i++) ret[i]= makeClass(st.nextToken());
        return ret;
    }
}
