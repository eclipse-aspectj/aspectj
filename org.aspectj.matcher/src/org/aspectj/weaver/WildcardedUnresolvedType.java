/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Represents a wildcarded bound for a generic type, this can be unbounded '?' or bounded via extends '? extends Foo' or super '?
 * super Foo'. The signature for a ? is in fact "*" and the erasure signature is the upper bound which defaults to java.lang.Object
 * if nothing is specified. On resolution, this becomes a BoundedReferenceType
 * 
 * @author Andy Clement
 */
public class WildcardedUnresolvedType extends UnresolvedType {

	// TODO does not cope with extra bounds '? extends A & B & C'

	public static final int UNBOUND = 0;
	public static final int EXTENDS = 1;
	public static final int SUPER = 2;

	public static final WildcardedUnresolvedType QUESTIONMARK = new WildcardedUnresolvedType("*", UnresolvedType.OBJECT, null);

	private int boundKind = UNBOUND; // UNBOUND, EXTENDS, SUPER

	private UnresolvedType lowerBound;

	private UnresolvedType upperBound;

	public WildcardedUnresolvedType(String signature, UnresolvedType upperBound, UnresolvedType lowerBound) {
		super(signature, (upperBound == null ? UnresolvedType.OBJECT.signature : upperBound.signatureErasure));
		this.typeKind = TypeKind.WILDCARD;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
		if (signature.charAt(0) == '-') {
			boundKind = SUPER;
		}
		if (signature.charAt(0) == '+') {
			boundKind = EXTENDS;
		}
	}

	public UnresolvedType getUpperBound() {
		return upperBound;
	}

	public UnresolvedType getLowerBound() {
		return lowerBound;
	}

	public boolean isExtends() {
		return boundKind == EXTENDS;
	}

	public boolean isSuper() {
		return boundKind == SUPER;
	}

	public boolean isUnbound() {
		return boundKind == UNBOUND;
	}

}
