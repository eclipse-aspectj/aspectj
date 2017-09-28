/* *******************************************************************
 * Copyright (c) 2005, 2017 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.reflect.Member;

import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

/**
 * Subtype of ResolvedMemberImpl used in reflection world. Knows how to get annotations from a java.lang.reflect.Member
 * 
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class ReflectionBasedResolvedMemberImpl extends ResolvedMemberImpl {

	private AnnotationFinder annotationFinder = null;
	private GenericSignatureInformationProvider gsigInfoProvider = new Java14GenericSignatureInformationProvider();
	
	/**
	 * If true then only runtime visible annotations have been resolved via reflection. If class retention
	 * annotations are also required (later) then the cache will have to be rebuilt using a more detailed
	 * dig into the class file.
	 */
	private boolean onlyRuntimeAnnotationsCached;

	private Member reflectMember;

	public ReflectionBasedResolvedMemberImpl(MemberKind kind, UnresolvedType declaringType, int modifiers,
			UnresolvedType returnType, String name, UnresolvedType[] parameterTypes, Member reflectMember) {
		super(kind, declaringType, modifiers, returnType, name, parameterTypes);
		this.reflectMember = reflectMember;
	}

	public ReflectionBasedResolvedMemberImpl(MemberKind kind, UnresolvedType declaringType, int modifiers,
			UnresolvedType returnType, String name, UnresolvedType[] parameterTypes, UnresolvedType[] checkedExceptions,
			Member reflectMember) {
		super(kind, declaringType, modifiers, returnType, name, parameterTypes, checkedExceptions);
		this.reflectMember = reflectMember;
	}

	public ReflectionBasedResolvedMemberImpl(MemberKind kind, UnresolvedType declaringType, int modifiers,
			UnresolvedType returnType, String name, UnresolvedType[] parameterTypes, UnresolvedType[] checkedExceptions,
			ResolvedMember backingGenericMember, Member reflectMember) {
		super(kind, declaringType, modifiers, returnType, name, parameterTypes, checkedExceptions, backingGenericMember);
		this.reflectMember = reflectMember;
	}

	public ReflectionBasedResolvedMemberImpl(MemberKind kind, UnresolvedType declaringType, int modifiers, String name,
			String signature, Member reflectMember) {
		super(kind, declaringType, modifiers, name, signature);
		this.reflectMember = reflectMember;
	}

	public Member getMember() {
		return this.reflectMember;
	}

	public void setGenericSignatureInformationProvider(GenericSignatureInformationProvider gsigProvider) {
		this.gsigInfoProvider = gsigProvider;
	}

	@Override
	public UnresolvedType[] getGenericParameterTypes() {
		return this.gsigInfoProvider.getGenericParameterTypes(this);
	}

	@Override
	public UnresolvedType getGenericReturnType() {
		return this.gsigInfoProvider.getGenericReturnType(this);
	}

	@Override
	public boolean isSynthetic() {
		return this.gsigInfoProvider.isSynthetic(this);
	}

	@Override
	public boolean isVarargsMethod() {
		return this.gsigInfoProvider.isVarArgs(this);
	}

	@Override
	public boolean isBridgeMethod() {
		return this.gsigInfoProvider.isBridge(this);
	}

	public void setAnnotationFinder(AnnotationFinder finder) {
		this.annotationFinder = finder;
	}

	@Override
	public boolean hasAnnotation(UnresolvedType ofType) {
		boolean areRuntimeRetentionAnnotationsSufficient = false;
		if (ofType instanceof ResolvedType) {
			areRuntimeRetentionAnnotationsSufficient = ((ResolvedType)ofType).isAnnotationWithRuntimeRetention();
		}
		unpackAnnotations(areRuntimeRetentionAnnotationsSufficient);
		return super.hasAnnotation(ofType);
	}

	@Override
	public boolean hasAnnotations() {
		unpackAnnotations(false);
		return super.hasAnnotations();
	}

	@Override
	public ResolvedType[] getAnnotationTypes() {
		unpackAnnotations(false);
		return super.getAnnotationTypes();
	}

	@Override
	public AnnotationAJ getAnnotationOfType(UnresolvedType ofType) {
		unpackAnnotations(false);
		if (annotationFinder == null || annotationTypes == null) {
			return null;
		}
		for (ResolvedType type : annotationTypes) {
			if (type.getSignature().equals(ofType.getSignature())) {
				return annotationFinder.getAnnotationOfType(ofType, reflectMember);
			}
		}
		return null;
	}

	@Override
	public String getAnnotationDefaultValue() {
		if (annotationFinder == null) {
			return null;
		}
		return annotationFinder.getAnnotationDefaultValue(reflectMember);
	}

	@Override
	public ResolvedType[][] getParameterAnnotationTypes() {
		if (parameterAnnotationTypes == null && annotationFinder != null) {
			parameterAnnotationTypes = annotationFinder.getParameterAnnotationTypes(reflectMember);
		}
		return parameterAnnotationTypes;
	}

	private void unpackAnnotations(boolean areRuntimeRetentionAnnotationsSufficient) {
		if (annotationFinder != null && (annotationTypes == null || (!areRuntimeRetentionAnnotationsSufficient && onlyRuntimeAnnotationsCached))) {
			annotationTypes = annotationFinder.getAnnotations(reflectMember, areRuntimeRetentionAnnotationsSufficient);
			onlyRuntimeAnnotationsCached = areRuntimeRetentionAnnotationsSufficient;
		}
	}
}
