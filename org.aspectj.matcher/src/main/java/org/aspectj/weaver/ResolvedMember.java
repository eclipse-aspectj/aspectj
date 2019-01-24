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

	public static final ResolvedMember[] NONE = new ResolvedMember[0];

	public int getModifiers(World world);

	public int getModifiers();

	public UnresolvedType[] getExceptions(World world);

	public UnresolvedType[] getExceptions();

	public ShadowMunger getAssociatedShadowMunger();

	public boolean isAjSynthetic();

	public boolean isCompatibleWith(Member am);

	public boolean hasAnnotation(UnresolvedType ofType);

	public AnnotationAJ[] getAnnotations();

	public ResolvedType[] getAnnotationTypes();

	public void setAnnotationTypes(ResolvedType[] annotationtypes);

	public void addAnnotation(AnnotationAJ annotation);

	public boolean isBridgeMethod();

	public boolean isVarargsMethod();

	public boolean isSynthetic();

	public void write(CompressingDataOutputStream s) throws IOException;

	public ISourceContext getSourceContext(World world);

	public String[] getParameterNames();

	public void setParameterNames(String[] names);

	public AnnotationAJ[][] getParameterAnnotations();

	public ResolvedType[][] getParameterAnnotationTypes();

	public String getAnnotationDefaultValue();

	public String getParameterSignatureErased();

	public String getSignatureErased();

	public String[] getParameterNames(World world);

	public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature();

	public ISourceLocation getSourceLocation();

	public int getStart();

	public int getEnd();

	public ISourceContext getSourceContext();

	public void setPosition(int sourceStart, int sourceEnd);

	public void setSourceContext(ISourceContext sourceContext);

	public boolean isAbstract();

	public boolean isPublic();

	public boolean isDefault();

	public boolean isVisible(ResolvedType fromType);

	public void setCheckedExceptions(UnresolvedType[] checkedExceptions);

	public void setAnnotatedElsewhere(boolean b);

	public boolean isAnnotatedElsewhere();

	// like toString but include generic signature info
	public String toGenericString();

	public String toDebugString();

	public boolean hasBackingGenericMember();

	public ResolvedMember getBackingGenericMember();

	/**
	 * Get the UnresolvedType for the return type, taking generic signature into account
	 */
	public UnresolvedType getGenericReturnType();

	/**
	 * Get the TypeXs of the parameter types, taking generic signature into account
	 */
	public UnresolvedType[] getGenericParameterTypes();

	public boolean equalsApartFromDeclaringType(Object other);

	// return a resolved member in which all type variables in the signature of
	// this member have been replaced with the given bindings. the isParameterized flag tells us whether we are creating a raw type
	// version or not
	// if isParameterized List<T> will turn into List<String> (for example),
	// but if !isParameterized List<T> will turn into List.
	public ResolvedMemberImpl parameterizedWith(UnresolvedType[] typeParameters, ResolvedType newDeclaringType,
			boolean isParameterized);

	// this variant allows for aliases for type variables (i.e. allowing them to
	// have another name)
	// this is used for processing ITDs that share type variables with their
	// target generic type
	public ResolvedMemberImpl parameterizedWith(UnresolvedType[] typeParameters, ResolvedType newDeclaringType,
			boolean isParameterized, List<String> aliases);

	public void setTypeVariables(TypeVariable[] types);

	public TypeVariable[] getTypeVariables();

	/**
	 * Returns true if this member matches the other. The matching takes into account name and parameter types only. When comparing
	 * parameter types, we allow any type variable to match any other type variable regardless of bounds.
	 */
	public boolean matches(ResolvedMember aCandidateMatch, boolean ignoreGenerics);

	public void evictWeavingState();

	public ResolvedMember parameterizedWith(Map<String, UnresolvedType> m, World w);

	public boolean isDefaultConstructor();

	public void setAnnotations(AnnotationAJ[] annotations);

}