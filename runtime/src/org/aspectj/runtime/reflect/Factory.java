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
 *    Alex Vasseur    new factory methods for variants of JP
 * ******************************************************************/


package org.aspectj.runtime.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

public final class Factory {    
    Class lexicalClass;
    ClassLoader lookupClassLoader;
    String filename;
    public Factory(String filename, Class lexicalClass) {
        //System.out.println("making
        this.filename = filename;  
        this.lexicalClass = lexicalClass;
        lookupClassLoader = lexicalClass.getClassLoader();
    }
    
    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, SourceLocation loc) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, loc);
    }
    
    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l, int c) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, makeSourceLoc(l, c));
    }
    
    public JoinPoint.StaticPart makeSJP(String kind, Signature sig, int l) {
        return new JoinPointImpl.StaticPartImpl(kind, sig, makeSourceLoc(l, -1));
    }

    public JoinPoint.EnclosingStaticPart makeESJP(String kind, Signature sig, SourceLocation loc) {
        return new JoinPointImpl.EnclosingStaticPartImpl(kind, sig, loc);
    }

    public JoinPoint.EnclosingStaticPart makeESJP(String kind, Signature sig, int l, int c) {
        return new JoinPointImpl.EnclosingStaticPartImpl(kind, sig, makeSourceLoc(l, c));
    }

    public JoinPoint.EnclosingStaticPart makeESJP(String kind, Signature sig, int l) {
        return new JoinPointImpl.EnclosingStaticPartImpl(kind, sig, makeSourceLoc(l, -1));
    }

    public static JoinPoint.StaticPart makeEncSJP(Member member) {
    	Signature sig = null;
    	String kind = null;
    	if (member instanceof Method) {
    		Method method = (Method) member;
    		sig = new MethodSignatureImpl(method.getModifiers(),method.getName(),
    				method.getDeclaringClass(),method.getParameterTypes(),
					new String[method.getParameterTypes().length],
					method.getExceptionTypes(),method.getReturnType());
    		kind = JoinPoint.METHOD_EXECUTION;
    	} else if (member instanceof Constructor) {
    		Constructor cons = (Constructor) member;
    		sig = new ConstructorSignatureImpl(cons.getModifiers(),cons.getDeclaringClass(),
    				cons.getParameterTypes(),
					new String[cons.getParameterTypes().length],
					cons.getExceptionTypes());
    		kind = JoinPoint.CONSTRUCTOR_EXECUTION;
    	} else {
    		throw new IllegalArgumentException("member must be either a method or constructor");
    	}
        return new JoinPointImpl.EnclosingStaticPartImpl(kind,sig,null);
    }
    
    private static Object[] NO_ARGS = new Object[0];
	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, 
						Object _this, Object target)
	{
		return new JoinPointImpl(staticPart, _this, target, NO_ARGS);
	}
    
	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, 
						Object _this, Object target, Object arg0)
	{
		return new JoinPointImpl(staticPart, _this, target, new Object[] {arg0});
	}
    
	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, 
						Object _this, Object target, Object arg0, Object arg1)
	{
		return new JoinPointImpl(staticPart, _this, target, new Object[] {arg0, arg1});
	}
    
    
	public static JoinPoint makeJP(JoinPoint.StaticPart staticPart, 
						Object _this, Object target, Object[] args)
	{
		return new JoinPointImpl(staticPart, _this, target, args);
	}
    
    public MethodSignature makeMethodSig(String stringRep) {
        MethodSignatureImpl ret = new MethodSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    
    public MethodSignature makeMethodSig(int modifiers, String name, Class declaringType, 
            Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes,
	        Class returnType) {
        MethodSignatureImpl ret = new MethodSignatureImpl(modifiers,name,declaringType,parameterTypes,parameterNames,exceptionTypes,returnType);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;   
    }

    public ConstructorSignature makeConstructorSig(String stringRep) {
        ConstructorSignatureImpl ret = new ConstructorSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    
    public ConstructorSignature makeConstructorSig(int modifiers, Class declaringType, 
            Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes) {
        ConstructorSignatureImpl ret = new ConstructorSignatureImpl(modifiers,declaringType,parameterTypes,parameterNames,exceptionTypes);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;  	
    }

    public FieldSignature makeFieldSig(String stringRep) {
        FieldSignatureImpl ret = new FieldSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    
    public FieldSignature makeFieldSig(int modifiers, String name, Class declaringType, 
            Class fieldType) {
        FieldSignatureImpl ret = new FieldSignatureImpl(modifiers,name,declaringType,fieldType);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;    	
    }

    public AdviceSignature makeAdviceSig(String stringRep) {
        AdviceSignatureImpl ret = new AdviceSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public AdviceSignature makeAdviceSig(int modifiers, String name, Class declaringType, 
            Class[] parameterTypes, String[] parameterNames, Class[] exceptionTypes,
	        Class returnType) {
        AdviceSignatureImpl ret = new AdviceSignatureImpl(modifiers,name,declaringType,parameterTypes,parameterNames,exceptionTypes,returnType);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public InitializerSignature makeInitializerSig(String stringRep) {
        InitializerSignatureImpl ret = new InitializerSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }

    public InitializerSignature makeInitializerSig(int modifiers, Class declaringType) {
        InitializerSignatureImpl ret = new InitializerSignatureImpl(modifiers,declaringType);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    
    public CatchClauseSignature makeCatchClauseSig(String stringRep) {
        CatchClauseSignatureImpl ret = new CatchClauseSignatureImpl(stringRep);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;
    }
    
    public CatchClauseSignature makeCatchClauseSig(Class declaringType, 
            Class parameterType, String parameterName) {
        CatchClauseSignatureImpl ret = new CatchClauseSignatureImpl(declaringType,parameterType,parameterName);
        ret.setLookupClassLoader(lookupClassLoader);
        return ret;    	
    }
    

    public SourceLocation makeSourceLoc(int line, int col)
    {
        return new SourceLocationImpl(lexicalClass, this.filename, line);
    }
}
