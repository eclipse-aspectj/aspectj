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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * @author colyer Creates the appropriate ReflectionBasedReferenceTypeDelegate according to the VM level we are running at. Uses
 *         reflection to avoid 1.5 dependencies in 1.4 and 1.3 code base.
 */
public class ReflectionBasedReferenceTypeDelegateFactory {

	public static ReflectionBasedReferenceTypeDelegate createDelegate(ReferenceType forReferenceType, World inWorld,
			ClassLoader usingClassLoader) {
		try {
			Class c = Class.forName(forReferenceType.getName(), false, usingClassLoader);
			ReflectionBasedReferenceTypeDelegate rbrtd = create15Delegate(forReferenceType, c, usingClassLoader, inWorld);
			if (rbrtd != null) {
				return rbrtd; // can be null if we didn't find the class the delegate logic loads
			}
			return new ReflectionBasedReferenceTypeDelegate(c, usingClassLoader, inWorld, forReferenceType);
		} catch (ClassNotFoundException cnfEx) {
			return null;
		}
	}
	
	public static ReflectionBasedReferenceTypeDelegate createDelegate(ReferenceType forReferenceType, World inWorld,
			Class<?> clazz) {
		ReflectionBasedReferenceTypeDelegate rbrtd = create15Delegate(forReferenceType, clazz, clazz.getClassLoader(), inWorld);
		if (rbrtd != null) {
			return rbrtd; // can be null if we didn't find the class the delegate logic loads
		}
		return new ReflectionBasedReferenceTypeDelegate(clazz, clazz.getClassLoader(), inWorld, forReferenceType);
	}

	public static ReflectionBasedReferenceTypeDelegate create14Delegate(ReferenceType forReferenceType, World inWorld,
			ClassLoader usingClassLoader) {
		try {
			Class c = Class.forName(forReferenceType.getName(), false, usingClassLoader);
			return new ReflectionBasedReferenceTypeDelegate(c, usingClassLoader, inWorld, forReferenceType);
		} catch (ClassNotFoundException cnfEx) {
			return null;
		}
	}

	// can return 'null' if we can't find the class
	private static ReflectionBasedReferenceTypeDelegate create15Delegate(ReferenceType forReferenceType, Class forClass,
			ClassLoader usingClassLoader, World inWorld) {
		try {
			Class delegateClass = Class.forName("org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate");
			ReflectionBasedReferenceTypeDelegate ret = (ReflectionBasedReferenceTypeDelegate) delegateClass.newInstance();
			ret.initialize(forReferenceType, forClass, usingClassLoader, inWorld);
			return ret;
		} catch (ClassNotFoundException cnfEx) {
			throw new IllegalStateException(
					"Attempted to create Java 1.5 reflection based delegate but org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate was not found on classpath");
		} catch (InstantiationException insEx) {
			throw new IllegalStateException("Attempted to create Java 1.5 reflection based delegate but InstantiationException: "
					+ insEx + " occured");
		} catch (IllegalAccessException illAccEx) {
			throw new IllegalStateException("Attempted to create Java 1.5 reflection based delegate but IllegalAccessException: "
					+ illAccEx + " occured");
		}
	}

	private static GenericSignatureInformationProvider createGenericSignatureProvider(World inWorld) {
		try {
			Class providerClass = Class.forName("org.aspectj.weaver.reflect.Java15GenericSignatureInformationProvider");
			Constructor cons = providerClass.getConstructor(new Class[] { World.class });
			GenericSignatureInformationProvider ret = (GenericSignatureInformationProvider) cons
					.newInstance(new Object[] { inWorld });
			return ret;
		} catch (ClassNotFoundException cnfEx) {
			// drop through and create a 14 provider...
			// throw new
			// IllegalStateException("Attempted to create Java 1.5 generic signature provider but org.aspectj.weaver.reflect.Java15GenericSignatureInformationProvider was not found on classpath");
		} catch (NoSuchMethodException nsmEx) {
			throw new IllegalStateException("Attempted to create Java 1.5 generic signature provider but: " + nsmEx
					+ " occured");
		} catch (InstantiationException insEx) {
			throw new IllegalStateException("Attempted to create Java 1.5 generic signature provider but: " + insEx
					+ " occured");
		} catch (InvocationTargetException invEx) {
			throw new IllegalStateException("Attempted to create Java 1.5 generic signature provider but: " + invEx
					+ " occured");
		} catch (IllegalAccessException illAcc) {
			throw new IllegalStateException("Attempted to create Java 1.5 generic signature provider but: " + illAcc
					+ " occured");
		}
		return new Java14GenericSignatureInformationProvider();
	}

	/**
	 * convert a java.lang.reflect.Member into a resolved member in the world
	 * 
	 * @param reflectMember
	 * @param inWorld
	 * @return
	 */
	public static ResolvedMember createResolvedMember(Member reflectMember, World inWorld) {
		if (reflectMember instanceof Method) {
			return createResolvedMethod((Method) reflectMember, inWorld);
		} else if (reflectMember instanceof Constructor) {
			return createResolvedConstructor((Constructor) reflectMember, inWorld);
		} else {
			return createResolvedField((Field) reflectMember, inWorld);
		}
	}

	public static ResolvedMember createResolvedMethod(Method aMethod, World inWorld) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.METHOD,
				toResolvedType(aMethod.getDeclaringClass(), (IReflectionWorld) inWorld), aMethod.getModifiers(), toResolvedType(
						aMethod.getReturnType(), (IReflectionWorld) inWorld), aMethod.getName(), toResolvedTypeArray(
						aMethod.getParameterTypes(), inWorld), toResolvedTypeArray(aMethod.getExceptionTypes(), inWorld), aMethod);
		if (inWorld instanceof IReflectionWorld) {
			ret.setAnnotationFinder(((IReflectionWorld) inWorld).getAnnotationFinder());
		}
		ret.setGenericSignatureInformationProvider(createGenericSignatureProvider(inWorld));
		return ret;
	}

	public static ResolvedMember createResolvedAdviceMember(Method aMethod, World inWorld) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.ADVICE,
				toResolvedType(aMethod.getDeclaringClass(), (IReflectionWorld) inWorld), aMethod.getModifiers(), toResolvedType(
						aMethod.getReturnType(), (IReflectionWorld) inWorld), aMethod.getName(), toResolvedTypeArray(
						aMethod.getParameterTypes(), inWorld), toResolvedTypeArray(aMethod.getExceptionTypes(), inWorld), aMethod);
		if (inWorld instanceof IReflectionWorld) {
			ret.setAnnotationFinder(((IReflectionWorld) inWorld).getAnnotationFinder());
		}
		ret.setGenericSignatureInformationProvider(createGenericSignatureProvider(inWorld));
		return ret;
	}

	public static ResolvedMember createStaticInitMember(Class forType, World inWorld) {
		return new ResolvedMemberImpl(org.aspectj.weaver.Member.STATIC_INITIALIZATION, toResolvedType(forType,
				(IReflectionWorld) inWorld), Modifier.STATIC, UnresolvedType.VOID, "<clinit>", new UnresolvedType[0],
				new UnresolvedType[0]);
	}

	public static ResolvedMember createResolvedConstructor(Constructor aConstructor, World inWorld) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.CONSTRUCTOR,
				toResolvedType(aConstructor.getDeclaringClass(), (IReflectionWorld) inWorld), aConstructor.getModifiers(),
				// to return what BCEL returns, the return type for ctor is void
				UnresolvedType.VOID,// toResolvedType(aConstructor.getDeclaringClass(),(IReflectionWorld)inWorld),
				"<init>", toResolvedTypeArray(aConstructor.getParameterTypes(), inWorld), toResolvedTypeArray(
						aConstructor.getExceptionTypes(), inWorld), aConstructor);
		if (inWorld instanceof IReflectionWorld) {
			ret.setAnnotationFinder(((IReflectionWorld) inWorld).getAnnotationFinder());
		}
		ret.setGenericSignatureInformationProvider(createGenericSignatureProvider(inWorld));
		return ret;
	}

	public static ResolvedMember createResolvedField(Field aField, World inWorld) {
		ReflectionBasedResolvedMemberImpl ret = new ReflectionBasedResolvedMemberImpl(org.aspectj.weaver.Member.FIELD,
				toResolvedType(aField.getDeclaringClass(), (IReflectionWorld) inWorld), aField.getModifiers(), toResolvedType(
						aField.getType(), (IReflectionWorld) inWorld), aField.getName(), new UnresolvedType[0], aField);
		if (inWorld instanceof IReflectionWorld) {
			ret.setAnnotationFinder(((IReflectionWorld) inWorld).getAnnotationFinder());
		}
		ret.setGenericSignatureInformationProvider(createGenericSignatureProvider(inWorld));
		return ret;
	}

	public static ResolvedMember createHandlerMember(Class exceptionType, Class inType, World inWorld) {
		return new ResolvedMemberImpl(org.aspectj.weaver.Member.HANDLER, toResolvedType(inType, (IReflectionWorld) inWorld),
				Modifier.STATIC, "<catch>", "(" + inWorld.resolve(exceptionType.getName()).getSignature() + ")V");
	}

	public static ResolvedType resolveTypeInWorld(Class aClass, World aWorld) {
		// classes that represent arrays return a class name that is the signature of the array type, ho-hum...
		String className = aClass.getName();
		if (aClass.isArray()) {
			return aWorld.resolve(UnresolvedType.forSignature(className.replace('.', '/')));
		} else {
			return aWorld.resolve(className);
		}
	}

	private static ResolvedType toResolvedType(Class aClass, IReflectionWorld aWorld) {
		return aWorld.resolve(aClass);
	}

	private static ResolvedType[] toResolvedTypeArray(Class[] classes, World inWorld) {
		ResolvedType[] ret = new ResolvedType[classes.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = ((IReflectionWorld) inWorld).resolve(classes[i]);
		}
		return ret;
	}
}
