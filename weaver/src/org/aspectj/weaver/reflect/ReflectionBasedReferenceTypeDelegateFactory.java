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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aspectj.util.LangUtil;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * @author colyer
 * Creates the appropriate ReflectionBasedReferenceTypeDelegate according to
 * the VM level we are running at. Uses reflection to avoid 1.5 dependencies in
 * 1.4 and 1.3 code base.
 */
public class ReflectionBasedReferenceTypeDelegateFactory {

	public static ReflectionBasedReferenceTypeDelegate
		createDelegate(ReferenceType forReferenceType, World inWorld) {
		try {
			Class c = Class.forName(forReferenceType.getName());
			if (LangUtil.is15VMOrGreater()) {
				ReflectionBasedReferenceTypeDelegate rbrtd = create15Delegate(forReferenceType,c,inWorld);
				if (rbrtd!=null) return rbrtd; // can be null if we didn't find the class the delegate logic loads
			}
			return new ReflectionBasedReferenceTypeDelegate(c,inWorld,forReferenceType);
		} catch (ClassNotFoundException cnfEx) {
			return null;
		}
	}
	
	private static ReflectionBasedReferenceTypeDelegate create15Delegate(ReferenceType forReferenceType, Class forClass, World inWorld) {
		try {
			Class delegateClass = Class.forName("org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate");
			ReflectionBasedReferenceTypeDelegate ret = (ReflectionBasedReferenceTypeDelegate) delegateClass.newInstance();
			ret.initialize(forReferenceType,forClass,inWorld);
			return ret;
		} catch (ClassNotFoundException cnfEx) {
			return null;
		} catch (InstantiationException insEx) {
			return null;
		} catch (IllegalAccessException illAccEx) {
			return null;
		}
	}
	
	/**
	 * convert a java.lang.reflect.Member into a resolved member in the world
	 * @param reflectMember
	 * @param inWorld
	 * @return
	 */
	public static ResolvedMember createResolvedMember(Member reflectMember, World inWorld) {
		if (reflectMember instanceof Method) {
			return createResolvedMethod((Method)reflectMember,inWorld);
		} else if (reflectMember instanceof Constructor) {
			return createResolvedConstructor((Constructor)reflectMember,inWorld);
		} else {
			return createResolvedField((Field)reflectMember,inWorld);
		}
	}
	
	public static ResolvedMember createResolvedMethod(Method aMethod, World inWorld) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
				inWorld.resolve(aMethod.getDeclaringClass().getName()),
				aMethod.getModifiers(),
				inWorld.resolve(aMethod.getReturnType().getName()),
				aMethod.getName(),
				toResolvedTypeArray(aMethod.getParameterTypes(),inWorld),
				toResolvedTypeArray(aMethod.getExceptionTypes(),inWorld),
				aMethod
				);
		return ret;
	}
	
	public static ResolvedMember createResolvedAdviceMember(Method aMethod, World inWorld) {
		return new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.ADVICE,
				inWorld.resolve(aMethod.getDeclaringClass().getName()),
				aMethod.getModifiers(),
				inWorld.resolve(aMethod.getReturnType().getName()),
				aMethod.getName(),
				toResolvedTypeArray(aMethod.getParameterTypes(),inWorld),
				toResolvedTypeArray(aMethod.getExceptionTypes(),inWorld),
				aMethod
				);
	}
	
	public static ResolvedMember createStaticInitMember(Class forType, World inWorld) {
		return new ResolvedMemberImpl(org.aspectj.weaver.Member.STATIC_INITIALIZATION,
				inWorld.resolve(forType.getName()),
				Modifier.STATIC,
				ResolvedType.VOID,
				"<clinit>",
				new UnresolvedType[0],
				new UnresolvedType[0]
				);
	}

	public static ResolvedMember createResolvedConstructor(Constructor aConstructor, World inWorld) {
		return new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.CONSTRUCTOR,
				inWorld.resolve(aConstructor.getDeclaringClass().getName()),
				aConstructor.getModifiers(),
				inWorld.resolve(aConstructor.getDeclaringClass().getName()),
				"init",
				toResolvedTypeArray(aConstructor.getParameterTypes(),inWorld),
				toResolvedTypeArray(aConstructor.getExceptionTypes(),inWorld),
				aConstructor
				);
	}

	public static ResolvedMember createResolvedField(Field aField, World inWorld) {
		return new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.FIELD,
				inWorld.resolve(aField.getDeclaringClass().getName()),
				aField.getModifiers(),
				inWorld.resolve(aField.getType().getName()),
				aField.getName(),
				new UnresolvedType[0],
				aField);
	}
	
	public static ResolvedMember createHandlerMember(Class exceptionType, Class inType,World inWorld) {
		return new ResolvedMemberImpl(
				org.aspectj.weaver.Member.HANDLER,
				inWorld.resolve(inType.getName()),
				Modifier.STATIC,
				"<catch>",
				"(" + inWorld.resolve(exceptionType.getName()).getSignature() + ")V");
	}

	private static ResolvedType[] toResolvedTypeArray(Class[] classes, World inWorld) {
		ResolvedType[] ret = new ResolvedType[classes.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = inWorld.resolve(classes[i].getName());
		}
		return ret;
	}
}
