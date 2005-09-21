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

import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

/**
 * Subtype of ResolvedMemberImpl used in reflection world.
 * Knows how to get annotations from a java.lang.reflect.Member
 *
 */
public class ReflectionBasedResolvedMemberImpl extends ResolvedMemberImpl {

	private static AnnotationFinder annotationFinder = null;
	
	static {
		try {
			Class java15AnnotationFinder = Class.forName("org.aspectj.weaver.reflect.Java15AnnotationFinder");
			annotationFinder = (AnnotationFinder) java15AnnotationFinder.newInstance();
		} catch(Exception ex) {
			// must be on 1.4 or earlier
		}
	}
	
	private Member reflectMember;
	
	/**
	 * @param kind
	 * @param declaringType
	 * @param modifiers
	 * @param returnType
	 * @param name
	 * @param parameterTypes
	 */
	public ReflectionBasedResolvedMemberImpl(Kind kind,
			UnresolvedType declaringType, int modifiers,
			UnresolvedType returnType, String name,
			UnresolvedType[] parameterTypes,
			Member reflectMember) {
		super(kind, declaringType, modifiers, returnType, name, parameterTypes);
		this.reflectMember = reflectMember;
	}

	/**
	 * @param kind
	 * @param declaringType
	 * @param modifiers
	 * @param returnType
	 * @param name
	 * @param parameterTypes
	 * @param checkedExceptions
	 */
	public ReflectionBasedResolvedMemberImpl(Kind kind,
			UnresolvedType declaringType, int modifiers,
			UnresolvedType returnType, String name,
			UnresolvedType[] parameterTypes, UnresolvedType[] checkedExceptions,
			Member reflectMember) {
		super(kind, declaringType, modifiers, returnType, name, parameterTypes,
				checkedExceptions);
		this.reflectMember = reflectMember;
	}

	/**
	 * @param kind
	 * @param declaringType
	 * @param modifiers
	 * @param returnType
	 * @param name
	 * @param parameterTypes
	 * @param checkedExceptions
	 * @param backingGenericMember
	 */
	public ReflectionBasedResolvedMemberImpl(Kind kind,
			UnresolvedType declaringType, int modifiers,
			UnresolvedType returnType, String name,
			UnresolvedType[] parameterTypes,
			UnresolvedType[] checkedExceptions,
			ResolvedMember backingGenericMember,
			Member reflectMember) {
		super(kind, declaringType, modifiers, returnType, name, parameterTypes,
				checkedExceptions, backingGenericMember);
		this.reflectMember = reflectMember;
	}

	/**
	 * @param kind
	 * @param declaringType
	 * @param modifiers
	 * @param name
	 * @param signature
	 */
	public ReflectionBasedResolvedMemberImpl(Kind kind,
			UnresolvedType declaringType, int modifiers, String name,
			String signature, Member reflectMember) {
		super(kind, declaringType, modifiers, name, signature);
		this.reflectMember = reflectMember;
	}

	public boolean hasAnnotation(UnresolvedType ofType) {
		unpackAnnotations();
		return super.hasAnnotation(ofType);
	}
	
	public boolean hasAnnotations() {
		unpackAnnotations();
		return super.hasAnnotations();
	}
	
	public ResolvedType[] getAnnotationTypes() {
		unpackAnnotations();
		return super.getAnnotationTypes();
	}
	
	private void unpackAnnotations() {
		if (annotationTypes == null && annotationFinder != null) {
			annotationTypes = annotationFinder.getAnnotations(reflectMember);
		}
	}
}
