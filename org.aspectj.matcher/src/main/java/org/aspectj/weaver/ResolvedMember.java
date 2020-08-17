/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *               2005 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 *     AMC      extracted as interface
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;

public interface ResolvedMember extends Member, AnnotatedElement, TypeVariableDeclaringElement {

	ResolvedMember[] NONE = new ResolvedMember[0];

	int getModifiers(World world);

	int getModifiers();

	UnresolvedType[] getExceptions(World world);

	UnresolvedType[] getExceptions();

	ShadowMunger getAssociatedShadowMunger();

	boolean isAjSynthetic();

	boolean isCompatibleWith(Member am);

	boolean hasAnnotation(UnresolvedType ofType);

	AnnotationAJ[] getAnnotations();

	ResolvedType[] getAnnotationTypes();

	void setAnnotationTypes(ResolvedType[] annotationtypes);

	void addAnnotation(AnnotationAJ annotation);

	boolean isBridgeMethod();

	boolean isVarargsMethod();

	boolean isSynthetic();

	void write(CompressingDataOutputStream s) throws IOException;

	ISourceContext getSourceContext(World world);

	String[] getParameterNames();

	void setParameterNames(String[] names);

	AnnotationAJ[][] getParameterAnnotations();

	ResolvedType[][] getParameterAnnotationTypes();

	String getAnnotationDefaultValue();

	String getParameterSignatureErased();

	String getSignatureErased();

	String[] getParameterNames(World world);

	AjAttribute.EffectiveSignatureAttribute getEffectiveSignature();

	ISourceLocation getSourceLocation();

	int getStart();

	int getEnd();

	ISourceContext getSourceContext();

	void setPosition(int sourceStart, int sourceEnd);

	void setSourceContext(ISourceContext sourceContext);

	boolean isAbstract();

	boolean isPublic();

	boolean isDefault();

	boolean isVisible(ResolvedType fromType);

	void setCheckedExceptions(UnresolvedType[] checkedExceptions);

	void setAnnotatedElsewhere(boolean b);

	boolean isAnnotatedElsewhere();

	// like toString but include generic signature info
	String toGenericString();

	String toDebugString();

	boolean hasBackingGenericMember();

	ResolvedMember getBackingGenericMember();

	/**
	 * Get the UnresolvedType for the return type, taking generic signature into account
	 */
	UnresolvedType getGenericReturnType();

	/**
	 * Get the TypeXs of the parameter types, taking generic signature into account
	 */
	UnresolvedType[] getGenericParameterTypes();

	boolean equalsApartFromDeclaringType(Object other);

	// return a resolved member in which all type variables in the signature of
	// this member have been replaced with the given bindings. the isParameterized flag tells us whether we are creating a raw type
	// version or not
	// if isParameterized List<T> will turn into List<String> (for example),
	// but if !isParameterized List<T> will turn into List.
	ResolvedMemberImpl parameterizedWith(UnresolvedType[] typeParameters, ResolvedType newDeclaringType,
										 boolean isParameterized);

	// this variant allows for aliases for type variables (i.e. allowing them to
	// have another name)
	// this is used for processing ITDs that share type variables with their
	// target generic type
	ResolvedMemberImpl parameterizedWith(UnresolvedType[] typeParameters, ResolvedType newDeclaringType,
										 boolean isParameterized, List<String> aliases);

	void setTypeVariables(TypeVariable[] types);

	TypeVariable[] getTypeVariables();

	/**
	 * Returns true if this member matches the other. The matching takes into account name and parameter types only. When comparing
	 * parameter types, we allow any type variable to match any other type variable regardless of bounds.
	 */
	boolean matches(ResolvedMember aCandidateMatch, boolean ignoreGenerics);

	void evictWeavingState();

	ResolvedMember parameterizedWith(Map<String, UnresolvedType> m, World w);

	boolean isDefaultConstructor();

	void setAnnotations(AnnotationAJ[] annotations);

}
