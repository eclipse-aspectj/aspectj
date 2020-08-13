/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.weaver.BoundedReferenceType;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.TypeVariableReferenceType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * Handles the translation of java.lang.reflect.Type objects into AspectJ UnresolvedTypes.
 * 
 * @author Adrian Colyer
 */
public class JavaLangTypeToResolvedTypeConverter {

	// Used to prevent recursion - we record what we are working on and return it if asked again *whilst* working on it
	private Map<Type, TypeVariableReferenceType> typeVariablesInProgress = new HashMap<>();
	private final World world;

	public JavaLangTypeToResolvedTypeConverter(World aWorld) {
		this.world = aWorld;
	}

	private World getWorld() {
		return this.world;
	}

	public ResolvedType fromType(Type type) {
		if (type instanceof Class) {
			Class clazz = (Class) type;
			String name = clazz.getName();
			/**
			 * getName() can return:
			 * 
			 * 1. If this class object represents a reference type that is not an 
			 * array type then the binary name of the class is returned 
			 * 2. If this class object represents a primitive type or void, then
			 * the name returned is a String equal to the Java language keyword
			 * corresponding to the primitive type or void. 
			 * 3. If this class object represents a class of arrays, then the internal
			 * form of the name consists of the name of the element type preceded by 
			 * one or more '[' characters representing the depth of the array nesting.
			 */
			if (clazz.isArray()) {
				UnresolvedType ut = UnresolvedType.forSignature(name.replace('.', '/'));
				return getWorld().resolve(ut);
			} else {
				return getWorld().resolve(name);
			}
		} else if (type instanceof ParameterizedType) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=509327
			// TODO should deal with the ownerType if it set, indicating this is possibly an inner type of a parameterized type
			Type ownerType = ((ParameterizedType) type).getOwnerType();
			ParameterizedType parameterizedType = (ParameterizedType) type;
			ResolvedType baseType = fromType(parameterizedType.getRawType());
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			if (baseType.isSimpleType() && typeArguments.length == 0 && ownerType != null) {
				// 'type' is an inner type of some outer parameterized type
				// For now just return the base type - in future create the parameterized form of the outer
				// and use it with the inner. We return the base type to be compatible with what the
				// code does that accesses the info from the bytecode (unlike this code which accesses it
				// reflectively).
				return baseType;
			}
			ResolvedType[] resolvedTypeArguments = fromTypes(typeArguments);
			return TypeFactory.createParameterizedType(baseType, resolvedTypeArguments, getWorld());
		} else if (type instanceof java.lang.reflect.TypeVariable) {
			TypeVariableReferenceType inprogressVar = typeVariablesInProgress.get(type);
			if (inprogressVar != null) {
				return inprogressVar;
			}
			java.lang.reflect.TypeVariable tv = (java.lang.reflect.TypeVariable) type;
			TypeVariable rt_tv = new TypeVariable(tv.getName());
			TypeVariableReferenceType tvrt = new TypeVariableReferenceType(rt_tv, getWorld());
			typeVariablesInProgress.put(type, tvrt); // record what we are working on, for recursion case
			Type[] bounds = tv.getBounds();
			ResolvedType[] resBounds = fromTypes(bounds);
			ResolvedType upperBound = resBounds[0];
			ResolvedType[] additionalBounds = new ResolvedType[0];
			if (resBounds.length > 1) {
				additionalBounds = new ResolvedType[resBounds.length - 1];
				System.arraycopy(resBounds, 1, additionalBounds, 0, additionalBounds.length);
			}
			rt_tv.setUpperBound(upperBound);
			rt_tv.setAdditionalInterfaceBounds(additionalBounds);
			typeVariablesInProgress.remove(type); // we have finished working on it
			return tvrt;
		} else if (type instanceof WildcardType) {
			WildcardType wildType = (WildcardType) type;
			Type[] lowerBounds = wildType.getLowerBounds();
			Type[] upperBounds = wildType.getUpperBounds();
			ResolvedType bound = null;
			boolean isExtends = lowerBounds.length == 0;
			if (isExtends) {
				bound = fromType(upperBounds[0]);
			} else {
				bound = fromType(lowerBounds[0]);
			}
			return new BoundedReferenceType((ReferenceType) bound, isExtends, getWorld());
		} else if (type instanceof GenericArrayType) {
			GenericArrayType genericArrayType = (GenericArrayType) type;
			Type componentType = genericArrayType.getGenericComponentType();
			return UnresolvedType.makeArray(fromType(componentType), 1).resolve(getWorld());
		}
		return ResolvedType.MISSING;
	}

	public ResolvedType[] fromTypes(Type[] types) {
		ResolvedType[] ret = new ResolvedType[types.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = fromType(types[i]);
		}
		return ret;
	}

}
