/* *******************************************************************
 * Copyright (c) 2010 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Map;

/**
 * A BoundedReferenceType is the result of a generics wildcard expression ? extends String, ? super Foo etc..
 * 
 * The "signature" for a bounded reference type follows the generic signature specification in section 4.4 of JVM spec: *,+,- plus
 * signature strings.
 * 
 * The bound may be a type variable (e.g. ? super T)
 * 
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class BoundedReferenceType extends ReferenceType {

	private ResolvedType lowerBound;

	private ResolvedType upperBound;

	protected ReferenceType[] additionalInterfaceBounds = ReferenceType.EMPTY_ARRAY;

	protected boolean isExtends = true;

	protected boolean isSuper = false;

	public UnresolvedType getUpperBound() {
		return upperBound;
	}

	public UnresolvedType getLowerBound() {
		return lowerBound;
	}

	public BoundedReferenceType(ReferenceType aBound, boolean isExtends, World world) {
		super((isExtends ? "+" : "-") + aBound.signature, aBound.signatureErasure, world);
		this.isExtends = isExtends;
		this.isSuper = !isExtends;
		if (isExtends) {
			upperBound = aBound;
		} else {
			lowerBound = aBound;
			upperBound = world.resolve(UnresolvedType.OBJECT);
		}
		setDelegate(new BoundedReferenceTypeDelegate((ReferenceType) getUpperBound()));
	}

	public BoundedReferenceType(ReferenceType aBound, boolean isExtends, World world, ReferenceType[] additionalInterfaces) {
		this(aBound, isExtends, world);
		this.additionalInterfaceBounds = additionalInterfaces;
	}

	public ReferenceType[] getAdditionalBounds() {
		return additionalInterfaceBounds;
	}

	public UnresolvedType parameterize(Map typeBindings) {
		ReferenceType[] parameterizedAdditionalInterfaces = new ReferenceType[additionalInterfaceBounds == null ? 0
				: additionalInterfaceBounds.length];
		for (int i = 0; i < parameterizedAdditionalInterfaces.length; i++) {
			parameterizedAdditionalInterfaces[i] = (ReferenceType) additionalInterfaceBounds[i].parameterize(typeBindings);
		}
		if (isExtends) {
			return new BoundedReferenceType((ReferenceType) getUpperBound().parameterize(typeBindings), isExtends, world,
					parameterizedAdditionalInterfaces);
		} else {
			return new BoundedReferenceType((ReferenceType) getLowerBound().parameterize(typeBindings), isExtends, world,
					parameterizedAdditionalInterfaces);
		}
	}

	/**
	 * only for use when resolving GenericsWildcardTypeX or a TypeVariableReferenceType
	 */
	protected BoundedReferenceType(String sig, String sigErasure, World world) {
		super(sig, sigErasure, world);
		upperBound = world.resolve(UnresolvedType.OBJECT);
		setDelegate(new BoundedReferenceTypeDelegate((ReferenceType) getUpperBound()));
	}

	public ReferenceType[] getInterfaceBounds() {
		return additionalInterfaceBounds;
	}

	public boolean hasLowerBound() {
		return getLowerBound() != null;
	}

	public boolean isExtends() {
		return (isExtends && !getUpperBound().getSignature().equals("Ljava/lang/Object;"));
	}

	public boolean isSuper() {
		return isSuper;
	}

	public boolean alwaysMatches(ResolvedType aCandidateType) {
		if (isExtends()) {
			// aCandidateType must be a subtype of upperBound
			return ((ReferenceType) getUpperBound()).isAssignableFrom(aCandidateType);
		} else if (isSuper()) {
			// aCandidateType must be a supertype of lowerBound
			return aCandidateType.isAssignableFrom((ReferenceType) getLowerBound());
		} else {
			return true; // straight '?'
		}
	}

	// this "maybe matches" that
	public boolean canBeCoercedTo(ResolvedType aCandidateType) {
		if (alwaysMatches(aCandidateType))
			return true;
		if (aCandidateType.isGenericWildcard()) {
			BoundedReferenceType boundedRT = (BoundedReferenceType) aCandidateType;
			ResolvedType myUpperBound = (ResolvedType) getUpperBound();
			ResolvedType myLowerBound = (ResolvedType) getLowerBound();
			if (isExtends()) {
				if (boundedRT.isExtends()) {
					return myUpperBound.isAssignableFrom((ResolvedType) boundedRT.getUpperBound());
				} else if (boundedRT.isSuper()) {
					return myUpperBound == boundedRT.getLowerBound();
				} else {
					return true; // it's '?'
				}
			} else if (isSuper()) {
				if (boundedRT.isSuper()) {
					return ((ResolvedType) boundedRT.getLowerBound()).isAssignableFrom(myLowerBound);
				} else if (boundedRT.isExtends()) {
					return myLowerBound == boundedRT.getUpperBound();
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public String getSimpleName() {
		if (!isExtends() && !isSuper())
			return "?";
		if (isExtends()) {
			return ("? extends " + getUpperBound().getSimpleName());
		} else {
			return ("? super " + getLowerBound().getSimpleName());
		}
	}

	// override to include additional interface bounds...
	public ResolvedType[] getDeclaredInterfaces() {
		ResolvedType[] interfaces = super.getDeclaredInterfaces();
		if (additionalInterfaceBounds.length > 0) {
			ResolvedType[] allInterfaces = new ResolvedType[interfaces.length + additionalInterfaceBounds.length];
			System.arraycopy(interfaces, 0, allInterfaces, 0, interfaces.length);
			System.arraycopy(additionalInterfaceBounds, 0, allInterfaces, interfaces.length, additionalInterfaceBounds.length);
			return allInterfaces;
		} else {
			return interfaces;
		}
	}

	public boolean isGenericWildcard() {
		return true;
	}
}
