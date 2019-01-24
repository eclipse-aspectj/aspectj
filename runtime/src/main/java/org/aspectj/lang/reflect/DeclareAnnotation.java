/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
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
	
	public enum Kind { Field, Method, Constructor, Type };
	
	/**
	 * The aspect that declared this member.
	 */
	AjType<?> getDeclaringType();
	
	/**
	 * The target element kind
	 */
	Kind getKind();
	
	/**
	 * The target signature pattern. Returns null if getKind() == Type 
	 */
	SignaturePattern getSignaturePattern();
	
	/**
	 * The target type pattern. Returns null if getKind() != Type
	 */
	TypePattern getTypePattern();
	
	/**
	 * The declared annotation. If the declared annotation does not have runtime retention,
	 * this method returns null.
	 */
	Annotation getAnnotation();
	
	/**
	 * Returns the text of the annotation as declared in this member. Available for
	 * both runtime and class-file retention annotations
	 */
	String getAnnotationAsText();
	
}
