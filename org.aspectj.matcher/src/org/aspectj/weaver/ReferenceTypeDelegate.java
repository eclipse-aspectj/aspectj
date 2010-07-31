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

import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;

/**
 * Abstraction over a type - a reference type is Object or a descendant of Object, other types (int/etc) are considered primitive
 * types. Abstract implementation provided by AbstractReferenceTypeDelegate.
 */

public interface ReferenceTypeDelegate {

	public boolean isAspect();

	/**
	 * @return true if the type is an annotation style aspect (a type marked @Aspect)
	 */
	public boolean isAnnotationStyleAspect();

	public boolean isInterface();

	public boolean isEnum();

	public boolean isAnnotation();

	public String getRetentionPolicy();

	/**
	 * @return true if this annotation type can be on a regular type (ie. it doesn't specify anything or it specifies TYPE)
	 */
	public boolean canAnnotationTargetType();

	/**
	 * @return all the possible targets that this annotation can be placed upon
	 */
	public AnnotationTargetKind[] getAnnotationTargetKinds();

	/**
	 * @return true if this annotation type has a retention policy of RUNTIME
	 */
	public boolean isAnnotationWithRuntimeRetention();

	public boolean isClass();

	public boolean isGeneric();

	public boolean isAnonymous();

	/**
	 * @return true if this class is nested (this includes: member classes, local classes, anonymous classes)
	 */
	public boolean isNested();

	public boolean hasAnnotation(UnresolvedType ofType);

	public AnnotationAJ[] getAnnotations();

	public ResolvedType[] getAnnotationTypes();

	public ResolvedMember[] getDeclaredFields();

	public ResolvedType[] getDeclaredInterfaces();

	public ResolvedMember[] getDeclaredMethods();

	public ResolvedMember[] getDeclaredPointcuts();

	public TypeVariable[] getTypeVariables();

	public int getModifiers();

	// aspect declaration related members
	/**
	 * @return for an aspect declaration, return the
	 */
	public PerClause getPerClause();

	public Collection<Declare> getDeclares();

	public Collection<ConcreteTypeMunger> getTypeMungers();

	public Collection<ResolvedMember> getPrivilegedAccesses();

	// end of aspect declaration related members

	public ResolvedType getSuperclass();

	public WeaverStateInfo getWeaverState();

	public ReferenceType getResolvedTypeX();

	// needs renaming isWeavable or removing from here
	public boolean isExposedToWeaver();

	public boolean doesNotExposeShadowMungers();

	public ISourceContext getSourceContext();

	public String getSourcefilename();

	public String getDeclaredGenericSignature();

	public ResolvedType getOuterClass();

	public boolean copySourceContext();

	/**
	 * TODO Caching of methods besides getDeclaredInterfaces() may also be dependent on this flag - which?
	 * 
	 * @return true if something the result of getDeclaredInterfaces() can be cached by the caller
	 */
	public boolean isCacheable();

	/**
	 * If known, return the compiler/weaver version used to build this delegate. Default is the most recent level as specified in
	 * {@link WeaverVersionInfo}.
	 * 
	 * @return the major version
	 */
	public int getCompilerVersion();

	/**
	 * Implementations need to clear state
	 */
	public void ensureConsistent();

	public boolean isWeavable();

	public boolean hasBeenWoven();

}