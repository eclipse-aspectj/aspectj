/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - June 2005 - separated out from ResolvedTypeX
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Collection;

import org.aspectj.weaver.patterns.PerClause;

/**
 * Abstraction over a type.  Abstract implementation provided by 
 * AbstractReferenceTypeDelegate.
 */
public interface ReferenceTypeDelegate {
	
	// TODO asc move to proxy
	public void addAnnotation(AnnotationX annotationX);

	public boolean isAspect();
    public boolean isAnnotationStyleAspect();
    public boolean isInterface();
    public boolean isEnum();
    public boolean isAnnotation();
    public boolean isAnnotationWithRuntimeRetention();
	public boolean isClass();
	public boolean isGeneric();
	public boolean isExposedToWeaver();
	
	public boolean hasAnnotation(TypeX ofType);
	
	public AnnotationX[]    getAnnotations();
    public ResolvedTypeX[]  getAnnotationTypes();
	public ResolvedMember[] getDeclaredFields();
	public ResolvedTypeX[]  getDeclaredInterfaces();
	public ResolvedMember[] getDeclaredMethods();
	public ResolvedMember[] getDeclaredPointcuts();
	public TypeVariable[] getTypeVariables();

	public PerClause  getPerClause();
	public Collection getDeclares() ;
	public Collection getTypeMungers();
	public Collection getPrivilegedAccesses();
	public int getModifiers();
	public ResolvedTypeX getSuperclass();		
	public WeaverStateInfo getWeaverState();
	public ReferenceType getResolvedTypeX();
	public boolean doesNotExposeShadowMungers();
	
	public String getDeclaredGenericSignature();
}