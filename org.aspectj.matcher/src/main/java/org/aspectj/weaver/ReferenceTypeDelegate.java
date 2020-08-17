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

	boolean isAspect();

	/**
	 * @return true if the type is an annotation style aspect (a type marked @Aspect)
	 */
	boolean isAnnotationStyleAspect();

	boolean isInterface();

	boolean isEnum();

	boolean isAnnotation();

	String getRetentionPolicy();

	/**
	 * @return true if this annotation type can be on a regular type (ie. it doesn't specify anything or it specifies TYPE)
	 */
	boolean canAnnotationTargetType();

	/**
	 * @return all the possible targets that this annotation can be placed upon
	 */
	AnnotationTargetKind[] getAnnotationTargetKinds();

	/**
	 * @return true if this annotation type has a retention policy of RUNTIME
	 */
	boolean isAnnotationWithRuntimeRetention();

	boolean isClass();

	boolean isGeneric();

	boolean isAnonymous();

	/**
	 * @return true if this class is nested (this includes: member classes, local classes, anonymous classes)
	 */
	boolean isNested();

	boolean hasAnnotation(UnresolvedType ofType);

	AnnotationAJ[] getAnnotations();

	ResolvedType[] getAnnotationTypes();

	ResolvedMember[] getDeclaredFields();

	ResolvedType[] getDeclaredInterfaces();

	ResolvedMember[] getDeclaredMethods();

	ResolvedMember[] getDeclaredPointcuts();

	TypeVariable[] getTypeVariables();

	int getModifiers();

	// aspect declaration related members
	/**
	 * @return for an aspect declaration, return the
	 */
	PerClause getPerClause();

	Collection<Declare> getDeclares();

	Collection<ConcreteTypeMunger> getTypeMungers();

	Collection<ResolvedMember> getPrivilegedAccesses();

	// end of aspect declaration related members

	ResolvedType getSuperclass();

	WeaverStateInfo getWeaverState();

	ReferenceType getResolvedTypeX();

	// needs renaming isWeavable or removing from here
	boolean isExposedToWeaver();

	boolean doesNotExposeShadowMungers();

	ISourceContext getSourceContext();

	String getSourcefilename();

	String getDeclaredGenericSignature();

	ResolvedType getOuterClass();

	boolean copySourceContext();

	/**
	 * TODO Caching of methods besides getDeclaredInterfaces() may also be dependent on this flag - which?
	 *
	 * @return true if something the result of getDeclaredInterfaces() can be cached by the caller
	 */
	boolean isCacheable();

	/**
	 * If known, return the compiler/weaver version used to build this delegate. Default is the most recent level as specified in
	 * {@link WeaverVersionInfo}.
	 *
	 * @return the major version
	 */
	int getCompilerVersion();

	/**
	 * Implementations need to clear state
	 */
	void ensureConsistent();

	boolean isWeavable();

	boolean hasBeenWoven();

	boolean hasAnnotations();

}
