/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - June 2005 - separated out from ResolvedType
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
	public void ensureDelegateConsistent(); // Required evil because of mutator methods in delegates :(  (see pr85132)
	
	public boolean isAspect();
    public boolean isAnnotationStyleAspect();
    public boolean isInterface();
    public boolean isEnum();
    public boolean isAnnotation();
    public String getRetentionPolicy();
    public boolean canAnnotationTargetType();
    public AnnotationTargetKind[] getAnnotationTargetKinds();
    public boolean isAnnotationWithRuntimeRetention();
	public boolean isClass();
	public boolean isGeneric();
	public boolean isAnonymous();
	public boolean isNested();
	public boolean isExposedToWeaver();
	
	public boolean hasAnnotation(UnresolvedType ofType);
	
	public AnnotationX[]    getAnnotations();
    public ResolvedType[]  getAnnotationTypes();
	public ResolvedMember[] getDeclaredFields();
	public ResolvedType[]  getDeclaredInterfaces();
	public ResolvedMember[] getDeclaredMethods();
	public ResolvedMember[] getDeclaredPointcuts();
	public TypeVariable[] getTypeVariables();

	public PerClause  getPerClause();
	public Collection getDeclares() ;
	public Collection getTypeMungers();
	public Collection getPrivilegedAccesses();
	public int getModifiers();
	public ResolvedType getSuperclass();		
	public WeaverStateInfo getWeaverState();
	public ReferenceType getResolvedTypeX();
	public boolean doesNotExposeShadowMungers();
	
	public ISourceContext getSourceContext();
	
	public String getSourcefilename();
	
	public String getDeclaredGenericSignature();
	public ResolvedType getOuterClass();
	
}