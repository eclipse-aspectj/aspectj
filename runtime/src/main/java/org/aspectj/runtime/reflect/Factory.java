/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002-2018 Palo Alto Research Center, Incorporated (PARC), Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *      Xerox/PARC    initial implementation 
 *    Alex Vasseur    new factory methods for variants of JP
 *  Abraham Nevado    new factory methods for collapsed SJPs
 *    Andy Clement    new factory methods that rely on LDC <class>
 * ******************************************************************/

package org.aspectj.runtime.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.AdviceSignature;
import org.aspectj.lang.reflect.CatchClauseSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.InitializerSignature;
import org.aspectj.lang.reflect.LockSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.lang.reflect.UnlockSignature;

public final class Factory {
	Class lexicalClass;
	ClassLoader lookupClassLoader;
	String filename;
	int count;
	
	private static final Class[] NO_TYPES = new Class[0];
	private static final String[] NO_STRINGS = new String[0];

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

	static Class makeClass(String s, ClassLoader loader) {
		if (s.equals("*"))
			return null;
		Class ret = (Class)prims.get(s);
		if (ret != null)
			return ret;
		try {
			/*
			 * The documentation of Class.forName explains why this is the right thing better than I could here.
			 */
			if (loader == null) {
				return Class.forName(s);
			} else {
				// used to be 'return loader.loadClass(s)' but that didn't cause
				// array types to be created and loaded correctly. (pr70404)
				return Class.forName(s, false, loader);
			}
		} catch (ClassNotFoundException e) {
			// System.out.println("null for: " + s);
			// XXX there should be a better return value for this
			return ClassNotFoundException.class;
		}
	}

	public Factory(String filename, Class lexicalClass) {
		// System.out.println("making
		this.filename = filename;
		this.lexicalClass = lexicalClass;
		this.count = 0;
		lookupClassLoader = lexicalClass.getClassLoader();
	}

	
	//
	// Create a signature and build a JoinPoint in one step.  Prior to 1.6.10 this was done as a two step operation in the generated
	// code but merging these methods in the runtime library enables the generated code to be shorter.  Generating code that
	// uses this method requires the weaver to be invoked with <tt>-Xset:targetRuntime1_6_10=true</tt>.
	// @since 1.6.10
	public JoinPoint.StaticPart makeSJP(String kind, String modifiers, String methodName, String declaringType, String paramTypes,
			String paramNames, String exceptionTypes, String returnType, int l) {
		Signature sig = this.makeMethodSig(modifiers, methodName, declaringType, paramTypes, paramNames, exceptionTypes, returnType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(l, -1));
	}
	
	// Create a signature and build a JoinPoint in one step.  Prior to 1.6.10 this was done as a two step operation in the generated
	// code but merging these methods in the runtime library enables the generated code to be shorter.  Generating code that
	// uses this method requires the weaver to be invoked with <tt>-Xset:targetRuntime1_6_10=true</tt>.
	// This method differs from the previous one in that it includes no exceptionTypes parameter - it is an optimization for the
	// case where there are no exceptions.  The generated code won't build an empty string and will not pass it into here.
	// 
	// @since 1.6.10
	public JoinPoint.StaticPart makeSJP(String kind, String modifiers, String methodName, String declaringType, String paramTypes,
			String paramNames, String returnType, int l) {
		Signature sig = this.makeMethodSig(modifiers, methodName, declaringType, paramTypes, paramNames, "", returnType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(l, -1));
	}
	
	// These are direct routes to creating thisJoinPoint and thisEnclosingJoinPoint objects
	// added in 1.9.1
	
	public JoinPoint.StaticPart makeMethodSJP(String kind, int modifiers, String methodName, Class declaringType, Class[] paramTypes, String[] paramNames, Class[] exceptionTypes, Class returnType, int line) {
		Signature sig = this.makeMethodSig(modifiers, methodName, declaringType, paramTypes==null?NO_TYPES:paramTypes, 
			paramNames==null?NO_STRINGS:paramNames, exceptionTypes==null?NO_TYPES:exceptionTypes, returnType == null?Void.TYPE:returnType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));		
	}

	public JoinPoint.EnclosingStaticPart makeMethodESJP(String kind, int modifiers, String methodName, Class declaringType, Class[] paramTypes, String[] paramNames, Class[] exceptionTypes, Class returnType, int line) {
		Signature sig = this.makeMethodSig(modifiers, methodName, declaringType, paramTypes==null?NO_TYPES:paramTypes,
				paramNames==null?NO_STRINGS:paramNames, exceptionTypes==null?NO_TYPES:exceptionTypes, returnType == null?Void.TYPE:returnType);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));		
	}

	public JoinPoint.StaticPart makeConstructorSJP(String kind, int modifiers, Class declaringType, Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes, int line) {
		ConstructorSignatureImpl sig = new ConstructorSignatureImpl(modifiers, declaringType, parameterTypes==null?NO_TYPES:parameterTypes, parameterNames==null?NO_STRINGS:parameterNames,
				exceptionTypes==null?NO_TYPES:exceptionTypes);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.EnclosingStaticPart makeConstructorESJP(String kind, int modifiers, Class declaringType, Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes, int line) {
		ConstructorSignatureImpl sig = new ConstructorSignatureImpl(modifiers, declaringType, parameterTypes==null?NO_TYPES:parameterTypes, parameterNames==null?NO_STRINGS:parameterNames,
				exceptionTypes==null?NO_TYPES:exceptionTypes);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.StaticPart makeCatchClauseSJP(String kind, Class declaringType, Class parameterType, String parameterName, int line) {
		CatchClauseSignatureImpl sig = new CatchClauseSignatureImpl(declaringType, parameterType, parameterName==null?"":parameterName);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.EnclosingStaticPart makeCatchClauseESJP(String kind, Class declaringType, Class parameterType, String parameterName, int line) {
		CatchClauseSignatureImpl sig = new CatchClauseSignatureImpl(declaringType, parameterType, parameterName==null?"":parameterName);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.StaticPart makeFieldSJP(String kind, int modifiers, String name, Class declaringType, Class fieldType, int line) {
		FieldSignatureImpl sig = new FieldSignatureImpl(modifiers, name, declaringType, fieldType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.EnclosingStaticPart makeFieldESJP(String kind, int modifiers, String name, Class declaringType, Class fieldType, int line) {
		FieldSignatureImpl sig = new FieldSignatureImpl(modifiers, name, declaringType, fieldType);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}
	
	public JoinPoint.StaticPart makeInitializerSJP(String kind, int modifiers, Class declaringType, int line) {
		InitializerSignatureImpl sig = new InitializerSignatureImpl(modifiers, declaringType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.EnclosingStaticPart makeInitializerESJP(String kind, int modifiers, Class declaringType, int line) {
		InitializerSignatureImpl sig = new InitializerSignatureImpl(modifiers, declaringType);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}
	
	public JoinPoint.StaticPart makeLockSJP(String kind, Class declaringType, int line) {
		LockSignatureImpl sig = new LockSignatureImpl(declaringType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.EnclosingStaticPart makeLockESJP(String kind, Class declaringType, int line) {
		LockSignatureImpl sig = new LockSignatureImpl(declaringType);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.StaticPart makeUnlockSJP(String kind, Class declaringType, int line) {
		UnlockSignatureImpl sig = new UnlockSignatureImpl(declaringType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.EnclosingStaticPart makeUnlockESJP(String kind, Class declaringType, int line) {
		UnlockSignatureImpl sig = new UnlockSignatureImpl(declaringType);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.StaticPart makeAdviceSJP(String kind, int modifiers, String name, Class declaringType, Class[] parameterTypes,
			String[] parameterNames, Class[] exceptionTypes, Class returnType, int line) {
		AdviceSignatureImpl sig = new AdviceSignatureImpl(modifiers, name, declaringType,
				parameterTypes==null?NO_TYPES:parameterTypes,
				parameterNames==null?NO_STRINGS:parameterNames,
				exceptionTypes==null?NO_TYPES:exceptionTypes,
				returnType==null?Void.TYPE:returnType);
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}

	public JoinPoint.EnclosingStaticPart makeAdviceESJP(String kind, int modifiers, String name, Class declaringType, Class[] parameterTypes,
			String[] parameterNames, Class[] exceptionTypes, Class returnType, int line) {
		AdviceSignatureImpl sig = new AdviceSignatureImpl(modifiers, name, declaringType,
				parameterTypes==null?NO_TYPES:parameterTypes,
				parameterNames==null?NO_STRINGS:parameterNames,
				exceptionTypes==null?NO_TYPES:exceptionTypes,
				returnType==null?Void.TYPE:returnType);
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(line, -1));	
	}
		
	// ---

	public JoinPoint.StaticPart makeSJP(String kind, Signature sig, SourceLocation loc) {
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, loc);
	}

	public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l, int c) {
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(l, c));
	}

	public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l) {
		return new JoinPointImpl.StaticPartImpl(count++, kind, sig, makeSourceLoc(l, -1));
	}

	public JoinPoint.EnclosingStaticPart makeESJP(String kind, Signature sig, SourceLocation loc) {
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, loc);
	}

	public JoinPoint.EnclosingStaticPart makeESJP(String kind, Signature sig, int l, int c) {
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(l, c));
	}

	public JoinPoint.EnclosingStaticPart makeESJP(String kind, Signature sig, int l) {
		return new JoinPointImpl.EnclosingStaticPartImpl(count++, kind, sig, makeSourceLoc(l, -1));
	}

	public static JoinPoint.StaticPart makeEncSJP(Member member) {
		Signature sig = null;
		String kind = null;
		if (member instanceof Method) {
			Method method = (Method) member;
			sig = new MethodSignatureImpl(method.getModifiers(), method.getName(), method.getDeclaringClass(), method
					.getParameterTypes(), new String[method.getParameterTypes().length], method.getExceptionTypes(), method
					.getReturnType());
			kind = JoinPoint.METHOD_EXECUTION;
		} else if (member instanceof Constructor) {
			Constructor cons = (Constructor) member;
			sig = new ConstructorSignatureImpl(cons.getModifiers(), cons.getDeclaringClass(), cons.getParameterTypes(),
					new String[cons.getParameterTypes().length], cons.getExceptionTypes());
			kind = JoinPoint.CONSTRUCTOR_EXECUTION;
		} else {
			throw new IllegalArgumentException("member must be either a method or constructor");
		}
		return new JoinPointImpl.EnclosingStaticPartImpl(-1, kind, sig, null);
	}

	private static Object[] NO_ARGS = new Object[0];

	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, Object _this, Object target) {
		return new JoinPointImpl(staticPart, _this, target, NO_ARGS);
	}

	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, Object _this, Object target, Object arg0) {
		return new JoinPointImpl(staticPart, _this, target, new Object[] { arg0 });
	}

	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, Object _this, Object target, Object arg0, Object arg1) {
		return new JoinPointImpl(staticPart, _this, target, new Object[] { arg0, arg1 });
	}

	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, Object _this, Object target, Object[] args) {
		return new JoinPointImpl(staticPart, _this, target, args);
	}

	public MethodSignature makeMethodSig(String stringRep) {
		MethodSignatureImpl ret = new MethodSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}
	
	public MethodSignature makeMethodSig(String modifiers, String methodName, String declaringType, String paramTypes,
			String paramNames, String exceptionTypes, String returnType) {
		Class declaringTypeClass = makeClass(declaringType, lookupClassLoader);
		return makeMethodSig(modifiers, methodName, declaringTypeClass, paramTypes, paramNames, exceptionTypes, returnType);
	}
	
	public MethodSignature makeMethodSig(String modifiers, String methodName, Class declaringTypeClass, String paramTypes,
			String paramNames, String exceptionTypes, String returnType) {
		int modifiersAsInt = Integer.parseInt(modifiers, 16);

		StringTokenizer st = new StringTokenizer(paramTypes, ":");
		int numParams = st.countTokens();
		Class[] paramTypeClasses = new Class[numParams];
		for (int i = 0; i < numParams; i++)
			paramTypeClasses[i] = makeClass(st.nextToken(), lookupClassLoader);

		st = new StringTokenizer(paramNames, ":");
		numParams = st.countTokens();
		String[] paramNamesArray = new String[numParams];
		for (int i = 0; i < numParams; i++)
			paramNamesArray[i] = st.nextToken();

		st = new StringTokenizer(exceptionTypes, ":");
		numParams = st.countTokens();
		Class[] exceptionTypeClasses = new Class[numParams];
		for (int i = 0; i < numParams; i++)
			exceptionTypeClasses[i] = makeClass(st.nextToken(), lookupClassLoader);

		Class returnTypeClass = makeClass(returnType, lookupClassLoader);

		MethodSignatureImpl ret = new MethodSignatureImpl(modifiersAsInt, methodName, declaringTypeClass, paramTypeClasses,
				paramNamesArray, exceptionTypeClasses, returnTypeClass);

		return ret;
	}

	public MethodSignature makeMethodSig(int modifiers, String name, Class declaringType, Class[] parameterTypes,
			String[] parameterNames, Class[] exceptionTypes, Class returnType) {
		MethodSignatureImpl ret = new MethodSignatureImpl(modifiers, name, declaringType, parameterTypes==null?NO_TYPES:parameterTypes, parameterNames,
				exceptionTypes == null?NO_TYPES:exceptionTypes, returnType);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public ConstructorSignature makeConstructorSig(String stringRep) {
		ConstructorSignatureImpl ret = new ConstructorSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public ConstructorSignature makeConstructorSig(String modifiers, String declaringType, String paramTypes, String paramNames,
			String exceptionTypes) {
		int modifiersAsInt = Integer.parseInt(modifiers, 16);

		Class declaringTypeClass = makeClass(declaringType, lookupClassLoader);

		StringTokenizer st = new StringTokenizer(paramTypes, ":");
		int numParams = st.countTokens();
		Class[] paramTypeClasses = new Class[numParams];
		for (int i = 0; i < numParams; i++)
			paramTypeClasses[i] = makeClass(st.nextToken(), lookupClassLoader);

		st = new StringTokenizer(paramNames, ":");
		numParams = st.countTokens();
		String[] paramNamesArray = new String[numParams];
		for (int i = 0; i < numParams; i++)
			paramNamesArray[i] = st.nextToken();

		st = new StringTokenizer(exceptionTypes, ":");
		numParams = st.countTokens();
		Class[] exceptionTypeClasses = new Class[numParams];
		for (int i = 0; i < numParams; i++)
			exceptionTypeClasses[i] = makeClass(st.nextToken(), lookupClassLoader);

		ConstructorSignatureImpl ret = new ConstructorSignatureImpl(modifiersAsInt, declaringTypeClass, paramTypeClasses,
				paramNamesArray, exceptionTypeClasses);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public ConstructorSignature makeConstructorSig(int modifiers, Class declaringType, Class[] parameterTypes,
			String[] parameterNames, Class[] exceptionTypes) {
		ConstructorSignatureImpl ret = new ConstructorSignatureImpl(modifiers, declaringType, parameterTypes, parameterNames,
				exceptionTypes);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public FieldSignature makeFieldSig(String stringRep) {
		FieldSignatureImpl ret = new FieldSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public FieldSignature makeFieldSig(String modifiers, String name, String declaringType, String fieldType) {
		int modifiersAsInt = Integer.parseInt(modifiers, 16);
		Class declaringTypeClass = makeClass(declaringType, lookupClassLoader);
		Class fieldTypeClass = makeClass(fieldType, lookupClassLoader);

		FieldSignatureImpl ret = new FieldSignatureImpl(modifiersAsInt, name, declaringTypeClass, fieldTypeClass);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public FieldSignature makeFieldSig(int modifiers, String name, Class declaringType, Class fieldType) {
		FieldSignatureImpl ret = new FieldSignatureImpl(modifiers, name, declaringType, fieldType);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public AdviceSignature makeAdviceSig(String stringRep) {
		AdviceSignatureImpl ret = new AdviceSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public AdviceSignature makeAdviceSig(String modifiers, String name, String declaringType, String paramTypes, String paramNames,
			String exceptionTypes, String returnType) {
		int modifiersAsInt = Integer.parseInt(modifiers, 16);

		Class declaringTypeClass = makeClass(declaringType, lookupClassLoader);

		StringTokenizer st = new StringTokenizer(paramTypes, ":");
		int numParams = st.countTokens();
		Class[] paramTypeClasses = new Class[numParams];
		for (int i = 0; i < numParams; i++)
			paramTypeClasses[i] = makeClass(st.nextToken(), lookupClassLoader);

		st = new StringTokenizer(paramNames, ":");
		numParams = st.countTokens();
		String[] paramNamesArray = new String[numParams];
		for (int i = 0; i < numParams; i++)
			paramNamesArray[i] = st.nextToken();

		st = new StringTokenizer(exceptionTypes, ":");
		numParams = st.countTokens();
		Class[] exceptionTypeClasses = new Class[numParams];
		for (int i = 0; i < numParams; i++)
			exceptionTypeClasses[i] = makeClass(st.nextToken(), lookupClassLoader);
		;

		Class returnTypeClass = makeClass(returnType, lookupClassLoader);

		AdviceSignatureImpl ret = new AdviceSignatureImpl(modifiersAsInt, name, declaringTypeClass, paramTypeClasses,
				paramNamesArray, exceptionTypeClasses, returnTypeClass);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public AdviceSignature makeAdviceSig(int modifiers, String name, Class declaringType, Class[] parameterTypes,
			String[] parameterNames, Class[] exceptionTypes, Class returnType) {
		AdviceSignatureImpl ret = new AdviceSignatureImpl(modifiers, name, declaringType, parameterTypes, parameterNames,
				exceptionTypes, returnType);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public InitializerSignature makeInitializerSig(String stringRep) {
		InitializerSignatureImpl ret = new InitializerSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public InitializerSignature makeInitializerSig(String modifiers, String declaringType) {
		int modifiersAsInt = Integer.parseInt(modifiers, 16);
		Class declaringTypeClass = makeClass(declaringType, lookupClassLoader);

		InitializerSignatureImpl ret = new InitializerSignatureImpl(modifiersAsInt, declaringTypeClass);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public InitializerSignature makeInitializerSig(int modifiers, Class declaringType) {
		InitializerSignatureImpl ret = new InitializerSignatureImpl(modifiers, declaringType);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public CatchClauseSignature makeCatchClauseSig(String stringRep) {
		CatchClauseSignatureImpl ret = new CatchClauseSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public CatchClauseSignature makeCatchClauseSig(String declaringType, String parameterType, String parameterName) {
		Class declaringTypeClass = makeClass(declaringType, lookupClassLoader);

		StringTokenizer st = new StringTokenizer(parameterType, ":");
		Class parameterTypeClass = makeClass(st.nextToken(), lookupClassLoader);

		st = new StringTokenizer(parameterName, ":");
		String parameterNameForReturn = st.nextToken();

		CatchClauseSignatureImpl ret = new CatchClauseSignatureImpl(declaringTypeClass, parameterTypeClass, parameterNameForReturn);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public CatchClauseSignature makeCatchClauseSig(Class declaringType, Class parameterType, String parameterName) {
		CatchClauseSignatureImpl ret = new CatchClauseSignatureImpl(declaringType, parameterType, parameterName);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public LockSignature makeLockSig(String stringRep) {
		LockSignatureImpl ret = new LockSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public LockSignature makeLockSig() {
		Class declaringTypeClass = makeClass("Ljava/lang/Object;", lookupClassLoader);
		LockSignatureImpl ret = new LockSignatureImpl(declaringTypeClass);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public LockSignature makeLockSig(Class declaringType) {
		LockSignatureImpl ret = new LockSignatureImpl(declaringType);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public UnlockSignature makeUnlockSig(String stringRep) {
		UnlockSignatureImpl ret = new UnlockSignatureImpl(stringRep);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public UnlockSignature makeUnlockSig() {
		Class declaringTypeClass = makeClass("Ljava/lang/Object;", lookupClassLoader);
		UnlockSignatureImpl ret = new UnlockSignatureImpl(declaringTypeClass);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public UnlockSignature makeUnlockSig(Class declaringType) {
		UnlockSignatureImpl ret = new UnlockSignatureImpl(declaringType);
		ret.setLookupClassLoader(lookupClassLoader);
		return ret;
	}

	public SourceLocation makeSourceLoc(int line, int col) {
		return new SourceLocationImpl(lexicalClass, this.filename, line);
	}
}
