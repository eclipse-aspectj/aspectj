/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.lang.reflect;

import java.lang.annotation.Annotation;

/**
 * The AspectJ runtime representation of a declare annotation member in an aspect.
 *
 */
public interface DeclareAnnotation {

	enum Kind { Field, Method, Constructor, Type };

	/**
	 * @return the aspect that declared this member.
	 */
	AjType<?> getDeclaringType();

	/**
	 * @return the target element kind
	 */
	Kind getKind();

	/**
	 * @return the target signature pattern. Returns null if getKind() == Type
	 */
	SignaturePattern getSignaturePattern();

	/**
	 * @return the target type pattern. Returns null if getKind() != Type
	 */
	TypePattern getTypePattern();

	/**
	 * @return the declared annotation. If the declared annotation does not have runtime retention,
	 * this method returns null.
	 */
	Annotation getAnnotation();

	/**
	 * @return the text of the annotation as declared in this member. Available for
	 * both runtime and class-file retention annotations
	 */
	String getAnnotationAsText();

}
