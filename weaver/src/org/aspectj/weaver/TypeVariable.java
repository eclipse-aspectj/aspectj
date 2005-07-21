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
package org.aspectj.weaver;

/**
 * Represents a type variable with bounds
 */
public class TypeVariable {
	
	/**
	 * whether or not the bounds of this type variable have been 
	 * resolved
	 */
	private boolean isResolved = false;
	
	/**
	 * the name of the type variable as recorded in the generic signature
	 */
	private String name;
	
	/**
	 * the upper bound of the type variable (default to Object).
	 * From the extends clause, eg. T extends Number.
	 */
	private UnresolvedType upperBound = UnresolvedType.OBJECT;
	
	/**
	 * any additional upper (interface) bounds.
	 * from the extends clause, e.g. T extends Number & Comparable
	 */
	private UnresolvedType[] additionalInterfaceBounds = new UnresolvedType[0];
	
	/**
	 * any lower bound.
	 * from the super clause, eg T super Foo
	 */
	private UnresolvedType lowerBound = null;
	
	public TypeVariable(String aName) {
		this.name = aName;
	}
	
	public TypeVariable(String aName, UnresolvedType anUpperBound) {
		this(aName);
		this.upperBound = anUpperBound;
	}
	
	public TypeVariable(String aName, UnresolvedType anUpperBound, 
			                        UnresolvedType[] someAdditionalInterfaceBounds) {
		this(aName,anUpperBound);
		this.additionalInterfaceBounds = someAdditionalInterfaceBounds;
	}
	
	public TypeVariable(String aName, UnresolvedType anUpperBound, 
            UnresolvedType[] someAdditionalInterfaceBounds, UnresolvedType aLowerBound) {
		this(aName,anUpperBound,someAdditionalInterfaceBounds);
		this.lowerBound = aLowerBound;
	}
	
	public UnresolvedType getUpperBound() {
		return upperBound;
	}
	
	public UnresolvedType[] getAdditionalInterfaceBounds() {
		return additionalInterfaceBounds;
	}
	
	public UnresolvedType getLowerBound() {
		return lowerBound;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * resolve all the bounds of this type variable
	 */
	public void resolve(World inSomeWorld) {
		if (isResolved) return;
		
		upperBound = upperBound.resolve(inSomeWorld);
		if (lowerBound != null) lowerBound = lowerBound.resolve(inSomeWorld);
		
		for (int i = 0; i < additionalInterfaceBounds.length; i++) {
			additionalInterfaceBounds[i] = additionalInterfaceBounds[i].resolve(inSomeWorld);
		}
		
		isResolved = true;
	}
	
	/**
	 * answer true if the given type satisfies all of the bound constraints of this
	 * type variable.
	 * If type variable has not been resolved then throws IllegalStateException
	 */
	public boolean canBeBoundTo(ResolvedType aCandidateType) {
		if (!isResolved) throw new IllegalStateException("Can't answer binding questions prior to resolving");
		if (aCandidateType.isTypeVariableReference()) {
			return matchingBounds((TypeVariableReferenceType)aCandidateType);
		}
		
		// otherwise can be bound iff...
		//  aCandidateType is a subtype of upperBound
		if (!isASubtypeOf(upperBound,aCandidateType)) {
			return false;
		}
		//  aCandidateType is a subtype of all additionalInterfaceBounds
		for (int i = 0; i < additionalInterfaceBounds.length; i++) {
			if (!isASubtypeOf(additionalInterfaceBounds[i], aCandidateType)) {
				return false;
			}
		}
		//  lowerBound is a subtype of aCandidateType
		if ((lowerBound != null) && (!isASubtypeOf(aCandidateType,lowerBound))) {
			return false;
		}
		return true;
	}
	
	// can match any type in the range of the type variable...
	// XXX what about interfaces?
	private boolean matchingBounds(TypeVariableReferenceType tvrt) {
		boolean upperMatch = canBeBoundTo(tvrt.getUpperBound());
		boolean lowerMatch = true;
		if (tvrt.hasLowerBound()) lowerMatch = canBeBoundTo(tvrt.getLowerBound());
		return upperMatch && lowerMatch;
	}
	
	private boolean isASubtypeOf(UnresolvedType candidateSuperType, UnresolvedType candidateSubType) {
		ResolvedType superType = (ResolvedType) candidateSuperType;
		ResolvedType subType = (ResolvedType) candidateSubType;
		return superType.isAssignableFrom(subType);
	}

	// only used when resolving 
	public void setUpperBound(UnresolvedType aTypeX) {
		this.upperBound = aTypeX;
	}
	
	// only used when resolving
	public void setLowerBound(UnresolvedType aTypeX) {
		this.lowerBound = aTypeX;
	}
	
	// only used when resolving
	public void setAdditionalInterfaceBounds(UnresolvedType[] someTypeXs) {
		this.additionalInterfaceBounds = someTypeXs;
	}
	
	public String getDisplayName() {
		StringBuffer ret = new StringBuffer();
		ret.append(name);
		if (!upperBound.getName().equals("java.lang.Object")) {
			ret.append(" extends ");
			ret.append(upperBound.getName());
			if (additionalInterfaceBounds != null) {
				for (int i = 0; i < additionalInterfaceBounds.length; i++) {
					ret.append(" & ");
					ret.append(additionalInterfaceBounds[i].getName());
				}
			}
		}
		if (lowerBound != null) {
			ret.append(" super ");
			ret.append(lowerBound.getName());
		}
		return ret.toString();
	}
	
	// good enough approximation
	public String toString() {
		return "TypeVar " + getDisplayName();
	}
	
	/**
	 * Return *full* signature for insertion in signature attribute, e.g. "T extends Number" would return "T:Ljava/lang/Number;"
	 */
	public String getSignature() {
	  	StringBuffer sb = new StringBuffer();
	  	sb.append(name);
	  	sb.append(":");
	  	sb.append(upperBound.getSignature());
	  	if (additionalInterfaceBounds!=null) {
		  	for (int i = 0; i < additionalInterfaceBounds.length; i++) {
				UnresolvedType iBound = additionalInterfaceBounds[i];
				sb.append(iBound.getSignature());
			}
	  	}
		return sb.toString();
	}
	
}
