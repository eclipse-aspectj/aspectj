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

import java.util.Collection;
import java.util.Iterator;

/**
 * Abstract representation of a member within a type.
 */
public interface Member extends Comparable {

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

	public static final AnnotationX[][] NO_PARAMETER_ANNOTATIONXS = new AnnotationX[][]{};
	public static final ResolvedType[][] NO_PARAMETER_ANNOTATION_TYPES = new ResolvedType[][]{};
	
	public MemberKind getKind();
	
	public ResolvedMember resolve(World world);

    public int compareTo(Object other);

	public String toLongString();

	public UnresolvedType getDeclaringType();

	public UnresolvedType getReturnType();
	
	public UnresolvedType getGenericReturnType();
	public UnresolvedType[] getGenericParameterTypes();

	public UnresolvedType getType();

	public String getName();

	public UnresolvedType[] getParameterTypes();

	/**
	 * Return full signature, including return type, e.g. "()LFastCar;". For a signature without the return type,
	 * use getParameterSignature() - it is important to choose the right one in the face of covariance.
	 */
	public String getSignature();
	
    public Iterator getJoinPointSignatures(World world);

	public int getArity();

	/**
	 * Return signature without return type, e.g. "()" for a signature *with* the return type,
	 * use getSignature() - it is important to choose the right one in the face of covariance.
	 */
	public String getParameterSignature();

	public int getModifiers(World world);
	
	public int getModifiers();

    public boolean isStatic();

    public boolean isInterface();

	public boolean isPrivate();

	/**
	 * Returns true iff the member is generic (NOT parameterized)
	 */
	public boolean canBeParameterized();

	public String getExtractableName();

    public AnnotationX[] getAnnotations();

	// ---- reflective thisJoinPoint stuff
	public String getSignatureMakerName();

	public String getSignatureType();
	
    public Collection/* ResolvedType */getDeclaringTypes(World world);
    
	public String getSignatureString(World world);

	public String[] getParameterNames(World world);

    public UnresolvedType[] getExceptions(World world);
}