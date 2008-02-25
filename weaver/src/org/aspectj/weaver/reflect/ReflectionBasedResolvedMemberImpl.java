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
import java.util.Iterator;

import org.aspectj.weaver.AnnotationX;
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

	private AnnotationFinder annotationFinder = null;
	private GenericSignatureInformationProvider gsigInfoProvider = 
		new Java14GenericSignatureInformationProvider();
	
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

	public Member getMember() {
		return this.reflectMember;
	}
	
	// generic signature support
	
	public void setGenericSignatureInformationProvider(GenericSignatureInformationProvider gsigProvider) {
		this.gsigInfoProvider = gsigProvider;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedMemberImpl#getGenericParameterTypes()
	 */
	public UnresolvedType[] getGenericParameterTypes() {
		return this.gsigInfoProvider.getGenericParameterTypes(this);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedMemberImpl#getGenericReturnType()
	 */
	public UnresolvedType getGenericReturnType() {
		return this.gsigInfoProvider.getGenericReturnType(this);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedMemberImpl#isSynthetic()
	 */
	public boolean isSynthetic() {
		return this.gsigInfoProvider.isSynthetic(this);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedMemberImpl#isVarargsMethod()
	 */
	public boolean isVarargsMethod() {
		return this.gsigInfoProvider.isVarArgs(this);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.ResolvedMemberImpl#isBridgeMethod()
	 */
	public boolean isBridgeMethod() {
		return this.gsigInfoProvider.isBridge(this);
	}
	
	// annotation support
	
	public void setAnnotationFinder(AnnotationFinder finder) {
		this.annotationFinder = finder;
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
	
	public AnnotationX getAnnotationOfType(UnresolvedType ofType) {
		unpackAnnotations();
		if (annotationFinder==null) return null;
		for (Iterator iterator = annotationTypes.iterator(); iterator.hasNext();) {
			ResolvedType type = (ResolvedType) iterator.next();
			if (type.getSignature().equals(ofType.getSignature())) {
				return annotationFinder.getAnnotationOfType(ofType, reflectMember);
			}
		}
		return null;
	}

	public String getAnnotationDefaultValue() {
		if (annotationFinder==null) return null;
		return annotationFinder.getAnnotationDefaultValue(reflectMember);
	}

	public ResolvedType[][] getParameterAnnotationTypes() {
    	if (parameterAnnotationTypes==null && annotationFinder!=null) {
    		parameterAnnotationTypes = annotationFinder.getParameterAnnotationTypes(reflectMember);
    	}
    	return parameterAnnotationTypes;
    }
	
	private void unpackAnnotations() {
		if (annotationTypes == null && annotationFinder != null) {
			annotationTypes = annotationFinder.getAnnotations(reflectMember);
		}
	}
}
