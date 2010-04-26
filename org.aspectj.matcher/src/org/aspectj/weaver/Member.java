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

	public static final Member[] NONE = new Member[0];

	public static final MemberKind METHOD = new MemberKind("METHOD", 1);
	public static final MemberKind FIELD = new MemberKind("FIELD", 2);
	public static final MemberKind CONSTRUCTOR = new MemberKind("CONSTRUCTOR", 3);
	public static final MemberKind STATIC_INITIALIZATION = new MemberKind("STATIC_INITIALIZATION", 4);
	public static final MemberKind POINTCUT = new MemberKind("POINTCUT", 5);
	public static final MemberKind ADVICE = new MemberKind("ADVICE", 6);
	public static final MemberKind HANDLER = new MemberKind("HANDLER", 7);
	public static final MemberKind MONITORENTER = new MemberKind("MONITORENTER", 8);
	public static final MemberKind MONITOREXIT = new MemberKind("MONITOREXIT", 9);

	public static final AnnotationAJ[][] NO_PARAMETER_ANNOTATIONXS = new AnnotationAJ[][] {};
	public static final ResolvedType[][] NO_PARAMETER_ANNOTATION_TYPES = new ResolvedType[][] {};

	/**
	 * @return the kind of member from those listed as MemberKind instances
	 */
	public MemberKind getKind();

	public String getName();

	public UnresolvedType getDeclaringType();

	public UnresolvedType[] getParameterTypes();

	public UnresolvedType[] getGenericParameterTypes();

	public UnresolvedType getType();

	public UnresolvedType getReturnType();

	public UnresolvedType getGenericReturnType();

	/**
	 * Return full signature, including return type, e.g. "()LFastCar;". For a signature without the return type, use
	 * getParameterSignature() - it is important to choose the right one in the face of covariance.
	 */
	public String getSignature();

	public JoinPointSignatureIterator getJoinPointSignatures(World world);

	public int getArity();

	/**
	 * Return signature without return type, e.g. "()" for a signature *with* the return type, use getSignature() - it is important
	 * to choose the right one in the face of covariance.
	 */
	public String getParameterSignature();

	public int getModifiers(World world);

	public int getModifiers();

	/**
	 * Returns true iff the member is generic (NOT parameterized)
	 */
	public boolean canBeParameterized();

	public AnnotationAJ[] getAnnotations();

	public Collection<ResolvedType> getDeclaringTypes(World world);

	public String[] getParameterNames(World world);

	public UnresolvedType[] getExceptions(World world);

	public ResolvedMember resolve(World world);

	public int compareTo(Member other);

}