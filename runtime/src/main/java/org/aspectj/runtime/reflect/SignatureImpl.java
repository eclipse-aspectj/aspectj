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

import org.aspectj.lang.Signature;

import java.util.StringTokenizer;

abstract class SignatureImpl implements Signature {

	private static boolean useCache = true;

    int modifiers = -1;
    String name;
    String declaringTypeName;
    Class declaringType;
    Cache stringCache;

    SignatureImpl(int modifiers, String name, Class declaringType) {
        this.modifiers = modifiers;
        this.name = name;
        this.declaringType = declaringType;
    }

    protected abstract String createToString (StringMaker sm);

    /* Use a soft cache for the short, middle and long String representations */
	String toString (StringMaker sm) {
		String result = null;
		if (useCache) {
			if (stringCache == null) {
				try {
					stringCache = new CacheImpl();
				} catch (Throwable t) {
					useCache = false;
				}
			} else {
				result = stringCache.get(sm.cacheOffset);
			}
		}
		if (result == null) {
			result = createToString(sm);
		}
		if (useCache) {
			stringCache.set(sm.cacheOffset, result);
		}
		return result;
	}

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
    public String getDeclaringTypeName() {
    	if (declaringTypeName == null) {
    		declaringTypeName = getDeclaringType().getName();
    	}
    	return declaringTypeName;
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
    private String stringRep;
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
        return Factory.makeClass(s,getLookupClassLoader());
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
        for (int i = 0; i < N; i++) ret[i]= Factory.makeClass(st.nextToken(),getLookupClassLoader());
        return ret;
    }

	/*
	 * Used for testing
	 */
	static void setUseCache (boolean b) {
		useCache = b;
	}

	static boolean getUseCache () {
		return useCache;
	}

	private interface Cache {

		String get(int cacheOffset);

		void set(int cacheOffset, String result);

	}

	// separate implementation so we don't need SoftReference to hold the field...
	private static final class CacheImpl implements Cache {
		private java.lang.ref.SoftReference toStringCacheRef;

		public CacheImpl() {
			makeCache();
		}

		public String get(int cacheOffset) {
			String[] cachedArray = array();
			if (cachedArray == null) {
				return null;
			}
			return cachedArray[cacheOffset];
		}

		public void set(int cacheOffset, String result) {
			String[] cachedArray = array();
			if (cachedArray == null) {
				cachedArray = makeCache();
			}
			cachedArray[cacheOffset] = result;
		}

		private String[] array() {
			return (String[]) toStringCacheRef.get();
		}

		private String[] makeCache() {
			String[] array = new String[3];
			toStringCacheRef = new java.lang.ref.SoftReference(array);
			return array;
		}

	}
}
