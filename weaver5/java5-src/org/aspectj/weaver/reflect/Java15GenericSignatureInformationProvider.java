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
import java.lang.reflect.Type;

import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * Uses Java 1.5 reflection APIs to determine generic signatures
 */
public class Java15GenericSignatureInformationProvider implements
		GenericSignatureInformationProvider {

	private final World world;
	
	public Java15GenericSignatureInformationProvider(World forWorld) {
		this.world = forWorld;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#getGenericParameterTypes(org.aspectj.weaver.reflect.ReflectionBasedResolvedMemberImpl)
	 */
	public UnresolvedType[] getGenericParameterTypes(
			ReflectionBasedResolvedMemberImpl resolvedMember) {
		JavaLangTypeToResolvedTypeConverter typeConverter = new JavaLangTypeToResolvedTypeConverter(world);
		Type[] pTypes = new Type[0];
		Member member = resolvedMember.getMember();
		if (member instanceof Method) {
			pTypes = ((Method)member).getGenericParameterTypes();
		} else if (member instanceof Constructor) {
			pTypes = ((Constructor)member).getGenericParameterTypes();
		}
		return typeConverter.fromTypes(pTypes);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#getGenericReturnType(org.aspectj.weaver.reflect.ReflectionBasedResolvedMemberImpl)
	 */
	public UnresolvedType getGenericReturnType(
			ReflectionBasedResolvedMemberImpl resolvedMember) {
		JavaLangTypeToResolvedTypeConverter typeConverter = new JavaLangTypeToResolvedTypeConverter(world);
		Member member = resolvedMember.getMember();
		if (member instanceof Field) {
			return typeConverter.fromType(((Field)member).getGenericType());
		} else if (member instanceof Method) {
			return typeConverter.fromType(((Method)member).getGenericReturnType());
		} else if (member instanceof Constructor) {
			return typeConverter.fromType(((Constructor)member).getDeclaringClass());			
		} else {
			throw new IllegalStateException("unexpected member type: " + member); 
		}
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#isBridge()
	 */
	public boolean isBridge(ReflectionBasedResolvedMemberImpl resolvedMember) {
		Member member =  resolvedMember.getMember();
		if (member instanceof Method) {
			return ((Method)member).isBridge();
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#isVarArgs()
	 */
	public boolean isVarArgs(ReflectionBasedResolvedMemberImpl resolvedMember) {
		Member member =  resolvedMember.getMember();
		if (member instanceof Method) {
			return ((Method)member).isVarArgs();
		} else if (member instanceof Constructor) {
			return ((Constructor)member).isVarArgs();			
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.GenericSignatureInformationProvider#isSynthetic()
	 */
	public boolean isSynthetic(ReflectionBasedResolvedMemberImpl resolvedMember) {
		Member member =  resolvedMember.getMember();
		return member.isSynthetic();
	}

}
