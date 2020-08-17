/* *******************************************************************
 * Copyright (c) 2002-2010
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Collection;

/**
 * Abstract representation of a member (field/constructor/method) within a type.
 *
 * @author PARC
 * @author Adrian Colyer
 * @author Andy Clement
 */
public interface Member extends Comparable<Member> {

	Member[] NONE = new Member[0];

	MemberKind METHOD = new MemberKind("METHOD", 1);
	MemberKind FIELD = new MemberKind("FIELD", 2);
	MemberKind CONSTRUCTOR = new MemberKind("CONSTRUCTOR", 3);
	MemberKind STATIC_INITIALIZATION = new MemberKind("STATIC_INITIALIZATION", 4);
	MemberKind POINTCUT = new MemberKind("POINTCUT", 5);
	MemberKind ADVICE = new MemberKind("ADVICE", 6);
	MemberKind HANDLER = new MemberKind("HANDLER", 7);
	MemberKind MONITORENTER = new MemberKind("MONITORENTER", 8);
	MemberKind MONITOREXIT = new MemberKind("MONITOREXIT", 9);

	AnnotationAJ[][] NO_PARAMETER_ANNOTATIONXS = new AnnotationAJ[][] {};
	ResolvedType[][] NO_PARAMETER_ANNOTATION_TYPES = new ResolvedType[][] {};

	/**
	 * @return the kind of member from those listed as MemberKind instances
	 */
	MemberKind getKind();

	String getName();

	UnresolvedType getDeclaringType();

	UnresolvedType[] getParameterTypes();

	UnresolvedType[] getGenericParameterTypes();

	UnresolvedType getType();

	UnresolvedType getReturnType();

	UnresolvedType getGenericReturnType();

	/**
	 * Return full signature, including return type, e.g. "()LFastCar;". For a signature without the return type, use
	 * getParameterSignature() - it is important to choose the right one in the face of covariance.
	 */
	String getSignature();

	JoinPointSignatureIterator getJoinPointSignatures(World world);

	int getArity();

	/**
	 * Return signature without return type, e.g. "()" for a signature *with* the return type, use getSignature() - it is important
	 * to choose the right one in the face of covariance.
	 */
	String getParameterSignature();

	int getModifiers(World world);

	int getModifiers();

	/**
	 * Returns true iff the member is generic (NOT parameterized)
	 */
	boolean canBeParameterized();

	AnnotationAJ[] getAnnotations();

	Collection<ResolvedType> getDeclaringTypes(World world);

	String[] getParameterNames(World world);

	UnresolvedType[] getExceptions(World world);

	ResolvedMember resolve(World world);

	int compareTo(Member other);

}
