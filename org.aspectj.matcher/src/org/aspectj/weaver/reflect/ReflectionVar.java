/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.reflect.Member;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ast.Var;

/**
 * A variable at a reflection shadow, used by the residual tests.
 */
public final class ReflectionVar extends Var {

	static final int THIS_VAR = 0;
	static final int TARGET_VAR = 1;
	static final int ARGS_VAR = 2;
	static final int AT_THIS_VAR = 3;
	static final int AT_TARGET_VAR = 4;
	static final int AT_ARGS_VAR = 5;
	static final int AT_WITHIN_VAR = 6;
	static final int AT_WITHINCODE_VAR = 7;
	static final int AT_ANNOTATION_VAR = 8;
	
	private AnnotationFinder annotationFinder = null;
	
//	static {
//		try {
//			Class java15AnnotationFinder = Class.forName("org.aspectj.weaver.reflect.Java15AnnotationFinder");
//			annotationFinder = (AnnotationFinder) java15AnnotationFinder.newInstance();
//		} catch(ClassNotFoundException ex) {
//			// must be on 1.4 or earlier
//		} catch(IllegalAccessException ex) {
//			// not so good
//			throw new RuntimeException("AspectJ internal error",ex);
//		} catch(InstantiationException ex) {
//			throw new RuntimeException("AspectJ internal error",ex);
//		}
//	}
	
	private int argsIndex = 0;
	private int varType;
	
	public static ReflectionVar createThisVar(ResolvedType type,AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(type,finder);
		ret.varType = THIS_VAR;
		return ret;
	}
	
	public static ReflectionVar createTargetVar(ResolvedType type, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(type,finder);
		ret.varType = TARGET_VAR;
		return ret;		
	}
	
	public static ReflectionVar createArgsVar(ResolvedType type, int index, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(type,finder);
		ret.varType = ARGS_VAR;
		ret.argsIndex = index;
		return ret;		
	}
	
	public static ReflectionVar createThisAnnotationVar(ResolvedType type, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(type,finder);
		ret.varType = AT_THIS_VAR;
		return ret;
	}
	
	public static ReflectionVar createTargetAnnotationVar(ResolvedType type, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(type,finder);
		ret.varType = AT_TARGET_VAR;
		return ret;		
	}
	
	public static ReflectionVar createArgsAnnotationVar(ResolvedType type, int index, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(type,finder);
		ret.varType = AT_ARGS_VAR;
		ret.argsIndex = index;
		return ret;		
	}
	
	public static ReflectionVar createWithinAnnotationVar(ResolvedType annType, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(annType,finder);
		ret.varType = AT_WITHIN_VAR;
		return ret;
	}
	
	public static ReflectionVar createWithinCodeAnnotationVar(ResolvedType annType, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(annType,finder);
		ret.varType = AT_WITHINCODE_VAR;
		return ret;
	}

	public static ReflectionVar createAtAnnotationVar(ResolvedType annType, AnnotationFinder finder) {
		ReflectionVar ret = new ReflectionVar(annType,finder);
		ret.varType = AT_ANNOTATION_VAR;
		return ret;
	}

	private ReflectionVar(ResolvedType type,AnnotationFinder finder) {
		super(type);
		this.annotationFinder = finder;
	}
			
	
	public Object getBindingAtJoinPoint(Object thisObject, Object targetObject, Object[] args) {
		return getBindingAtJoinPoint(thisObject,targetObject,args,null,null,null);
	}
	/**
	 * At a join point with the given this, target, and args, return the object to which this
	 * var is bound.
	 * @param thisObject
	 * @param targetObject
	 * @param args
	 * @return
	 */
	public Object getBindingAtJoinPoint(
			Object thisObject, 
			Object targetObject, 
			Object[] args,
			Member subject,
			Member withinCode,
			Class withinType) {
		switch( this.varType) {
		case THIS_VAR: return thisObject;
		case TARGET_VAR: return targetObject;
		case ARGS_VAR:
			if (this.argsIndex > (args.length - 1)) return null;
			return args[argsIndex];
		case AT_THIS_VAR:
			if (annotationFinder != null) {
				return annotationFinder.getAnnotation(getType(), thisObject);
			} else return null;
		case AT_TARGET_VAR:
			if (annotationFinder != null) {
				return annotationFinder.getAnnotation(getType(), targetObject);
			} else return null;
		case AT_ARGS_VAR:
			if (this.argsIndex > (args.length - 1)) return null;
			if (annotationFinder != null) {
				return annotationFinder.getAnnotation(getType(), args[argsIndex]);
			} else return null;
		case AT_WITHIN_VAR:
			if (annotationFinder != null) {
				return annotationFinder.getAnnotationFromClass(getType(), withinType);
			} else return null;
		case AT_WITHINCODE_VAR:
			if (annotationFinder != null) {
				return annotationFinder.getAnnotationFromMember(getType(), withinCode);
			} else return null;
		case AT_ANNOTATION_VAR:
			if (annotationFinder != null) {
				return annotationFinder.getAnnotationFromMember(getType(), subject);
			} else return null;
		}
			
		return null;
	}

}
