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


public interface Member {

    public static final Member[]   NONE                  = new Member[0];
	public static final MemberKind METHOD                = new MemberKind("METHOD", 1);
	public static final MemberKind FIELD                 = new MemberKind("FIELD", 2);
	public static final MemberKind CONSTRUCTOR           = new MemberKind("CONSTRUCTOR", 3);
	public static final MemberKind STATIC_INITIALIZATION = new MemberKind("STATIC_INITIALIZATION", 4);
	public static final MemberKind POINTCUT              = new MemberKind("POINTCUT", 5);
	public static final MemberKind ADVICE                = new MemberKind("ADVICE", 6);
	public static final MemberKind HANDLER               = new MemberKind("HANDLER", 7);
	public static final MemberKind MONITORENTER          = new MemberKind("MONITORENTER", 8);
	public static final MemberKind MONITOREXIT           = new MemberKind("MONITOREXIT", 9);

	public static final AnnotationX[][] NO_PARAMETER_ANNOTATIONXS = new AnnotationX[][]{};
	public static final ResolvedType[][] NO_PARAMETER_ANNOTATION_TYPES = new ResolvedType[][]{};

	public MemberKind getKind();
	
	public ResolvedMember resolve(World world);

	public String getName();

	public UnresolvedType getDeclaringType();

    public AnnotationX[] getAnnotations();
	public UnresolvedType getReturnType();	
	public UnresolvedType getType(); 
	public UnresolvedType getGenericReturnType();

	public String[] getParameterNames(World world);
	public UnresolvedType[] getParameterTypes();
	public UnresolvedType[] getGenericParameterTypes();
	
	/**
	 * Return full signature, including return type, e.g. "()LFastCar;" for a signature without the return type,
	 * use getParameterSignature() - it is important to choose the right one in the face of covariance.
	 */
	public String getSignature();
	
    /**
     * All the signatures that a join point with this member as its signature has.
     */
    public Iterator getJoinPointSignatures(World world);

	public int getArity();

	/**
	 * Return signature without return type, e.g. "()" for a signature *with* the return type,
	 * use getSignature() - it is important to choose the right one in the face of covariance.
	 */
	public String getParameterSignature();

	public boolean isCompatibleWith(Member am);

	public int getModifiers(World world);
	
	public int getModifiers();

	public UnresolvedType[] getExceptions(World world);

	public boolean isStatic();

	public boolean isInterface();

	public boolean isPrivate();

	public int getCallsiteModifiers();

	public String getExtractableName();

//	public AnnotationX[] getAnnotations();

	public Collection/*ResolvedType*/getDeclaringTypes(World world);

	// ---- reflective thisJoinPoint related methods
	public String getSignatureMakerName();

	public String getSignatureType();

	public String getSignatureString(World world);

}