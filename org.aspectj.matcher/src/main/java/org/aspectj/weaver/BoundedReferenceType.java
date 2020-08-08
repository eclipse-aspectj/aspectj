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

	// possible kinds of BoundedReferenceType
	public static final int UNBOUND = 0;
	public static final int EXTENDS = 1;
	public static final int SUPER = 2;

	public int kind;

	private ResolvedType lowerBound;

	private ResolvedType upperBound;

	protected ReferenceType[] additionalInterfaceBounds = ReferenceType.EMPTY_ARRAY;

	public BoundedReferenceType(ReferenceType aBound, boolean isExtends, World world) {
		super((isExtends ? "+" : "-") + aBound.signature, aBound.signatureErasure, world);
		if (isExtends) {
			this.kind = EXTENDS;
		} else {
			this.kind = SUPER;
		}
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

	/**
	 * only for use when resolving GenericsWildcardTypeX or a TypeVariableReferenceType
	 */
	protected BoundedReferenceType(String signature, String erasedSignature, World world) {
		super(signature, erasedSignature, world);
		if (signature.equals("*")) {
			// pure wildcard
			this.kind = UNBOUND;
			upperBound = world.resolve(UnresolvedType.OBJECT);
		} else {
			upperBound = world.resolve(forSignature(erasedSignature));
		}
		setDelegate(new BoundedReferenceTypeDelegate((ReferenceType) upperBound));
	}

	/**
	 * Constructs the BoundedReferenceType representing an unbounded wildcard '?'. In this situation the signature is '*' and the
	 * erased signature is Ljava/lang/Object;
	 */
	public BoundedReferenceType(World world) {
		super("*", "Ljava/lang/Object;", world);
		this.kind = UNBOUND;
		upperBound = world.resolve(UnresolvedType.OBJECT);
		setDelegate(new BoundedReferenceTypeDelegate((ReferenceType) upperBound));
	}

	public UnresolvedType getUpperBound() {
		return upperBound;
	}

	public UnresolvedType getLowerBound() {
		return lowerBound;
	}

	public ReferenceType[] getAdditionalBounds() {
		return additionalInterfaceBounds;
	}

	@Override
	public UnresolvedType parameterize(Map<String, UnresolvedType> typeBindings) {
		if (this.kind == UNBOUND) {
			return this;
		}
		ReferenceType[] parameterizedAdditionalInterfaces = new ReferenceType[additionalInterfaceBounds == null ? 0
				: additionalInterfaceBounds.length];
		for (int i = 0; i < parameterizedAdditionalInterfaces.length; i++) {
			parameterizedAdditionalInterfaces[i] = (ReferenceType) additionalInterfaceBounds[i].parameterize(typeBindings);
		}
		if (this.kind == EXTENDS) {
			UnresolvedType parameterizedUpperBound = getUpperBound().parameterize(typeBindings);
			if (!(parameterizedUpperBound instanceof ReferenceType)) {
				throw new IllegalStateException("DEBUG551732: Unexpected problem processing bounds. Parameterizing "+getUpperBound()+" produced "+parameterizedUpperBound+
						" (Type: "+parameterizedUpperBound==null?"null":parameterizedUpperBound.getClass().getName()+") (typeBindings="+typeBindings+")");
			}
			return new BoundedReferenceType((ReferenceType) parameterizedUpperBound, true, world,
					parameterizedAdditionalInterfaces);
		} else {
			// (this.kind == SUPER)
			UnresolvedType parameterizedLowerBound = getLowerBound().parameterize(typeBindings);
			if (!(parameterizedLowerBound instanceof ReferenceType)) {
				throw new IllegalStateException("PR543023: Unexpectedly found a non reference type: "+
						parameterizedLowerBound.getClass().getName()+" with signature "+parameterizedLowerBound.getSignature());
			}
			return new BoundedReferenceType((ReferenceType)parameterizedLowerBound , false, world,
					parameterizedAdditionalInterfaces);
		}
	}

	@Override
	public String getSignatureForAttribute() {
		StringBuilder ret = new StringBuilder();
		if (kind==SUPER){
			ret.append("-");
			ret.append(lowerBound.getSignatureForAttribute());
			for (ReferenceType additionalInterfaceBound : additionalInterfaceBounds) {
				ret.append(additionalInterfaceBound.getSignatureForAttribute());
			}
		} else if (kind==EXTENDS) {
			ret.append("+");
			ret.append(upperBound.getSignatureForAttribute());
			for (ReferenceType additionalInterfaceBound : additionalInterfaceBounds) {
				ret.append(additionalInterfaceBound.getSignatureForAttribute());
			}
		} else if (kind==UNBOUND) {
			ret.append("*");
		}
		return ret.toString();
	}


	public boolean hasLowerBound() {
		return lowerBound != null;
	}

	public boolean isExtends() {
		return this.kind == EXTENDS;
	}

	public boolean isSuper() {
		return this.kind == SUPER;
	}

	public boolean isUnbound() {
		return this.kind == UNBOUND;
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
		if (alwaysMatches(aCandidateType)) {
			return true;
		}
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

	@Override
	public String getSimpleName() {
		if (!isExtends() && !isSuper()) {
			return "?";
		}
		if (isExtends()) {
			return ("? extends " + getUpperBound().getSimpleName());
		} else {
			return ("? super " + getLowerBound().getSimpleName());
		}
	}

	// override to include additional interface bounds...
	@Override
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

	@Override
	public boolean isGenericWildcard() {
		return true;
	}
}
