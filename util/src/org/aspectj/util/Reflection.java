/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.util;

import java.lang.reflect.*;
import java.lang.reflect.Method;

public class Reflection {

	private Reflection() {
	}
	
	public static Object invokestatic(Class class_, String name) {
		return invokestaticN(class_, name, new Object[0]);
	}
	
	public static Object invokestatic(Class class_, String name, Object arg1) {
		return invokestaticN(class_, name, new Object[] { arg1 });
	}
	
	public static Object invokestatic(Class class_, String name, Object arg1, Object arg2) {
		return invokestaticN(class_, name, new Object[] { arg1, arg2 });
	}
	
	public static Object invokestatic(Class class_, String name, Object arg1, Object arg2, Object arg3) {
		return invokestaticN(class_, name, new Object[] { arg1, arg2, arg3 });
	}
	
	
	public static Object invokestaticN(Class class_, String name, Object[] args) {
		return invokeN(class_, name, null, args);
	}


	public static Object invoke(Class class_, Object target, String name, Object arg1) {
		return invokeN(class_, name, target, new Object[] { arg1 });
	}
	
	public static Object invoke(Class class_, Object target, String name, Object arg1, Object arg2) {
		return invokeN(class_, name, target, new Object[] { arg1, arg2 });
	}
	
	public static Object invoke(Class class_, Object target, String name, Object arg1, Object arg2, Object arg3) {
		return invokeN(class_, name, target, new Object[] { arg1, arg2, arg3 });
	}
	


	
	public static Object invokeN(Class class_, String name, Object target, Object[] args) {
		Method meth = getMatchingMethod(class_, name, args);
		try {
			return meth.invoke(target, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.toString());			
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof Error) throw (Error)t;
			if (t instanceof RuntimeException) throw (RuntimeException)t;
			t.printStackTrace();
			throw new RuntimeException(t.toString());
		}
	}


	public static Method getMatchingMethod(Class class_, String name, Object[] args) {
		Method[] meths = class_.getMethods();
		for (int i=0; i < meths.length; i++) {
			Method meth = meths[i];
			if (meth.getName().equals(name) && isCompatible(meth, args)) {
				return meth;
			}
		}
		return null;
	}

	private static boolean isCompatible(Method meth, Object[] args) {
		// ignore methods with overloading other than lengths
		return meth.getParameterTypes().length == args.length;
	}


	

	public static Object getStaticField(Class class_, String name) {
		try {
			return class_.getField(name).get(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("unimplemented");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("unimplemented");
		}
	}

}
