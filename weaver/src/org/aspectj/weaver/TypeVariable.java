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
	private TypeX upperBound = TypeX.OBJECT;
	
	/**
	 * any additional upper (interface) bounds.
	 * from the extends clause, e.g. T extends Number & Comparable
	 */
	private TypeX[] additionalInterfaceBounds = new TypeX[0];
	
	/**
	 * any lower bound.
	 * from the super clause, eg T super Foo
	 */
	private TypeX lowerBound = null;
	
	public TypeVariable(String aName) {
		this.name = aName;
	}
	
	public TypeVariable(String aName, TypeX anUpperBound) {
		this(aName);
		this.upperBound = anUpperBound;
	}
	
	public TypeVariable(String aName, TypeX anUpperBound, 
			                        TypeX[] someAdditionalInterfaceBounds) {
		this(aName,anUpperBound);
		this.additionalInterfaceBounds = someAdditionalInterfaceBounds;
	}
	
	public TypeVariable(String aName, TypeX anUpperBound, 
            TypeX[] someAdditionalInterfaceBounds, TypeX aLowerBound) {
		this(aName,anUpperBound,someAdditionalInterfaceBounds);
		this.lowerBound = aLowerBound;
	}
	
	public TypeX getUpperBound() {
		return upperBound;
	}
	
	public TypeX[] getAdditionalInterfaceBounds() {
		return additionalInterfaceBounds;
	}
	
	public TypeX getLowerBound() {
		return lowerBound;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * resolve all the bounds of this type variable
	 */
	public void resolve(World inSomeWorld) {
		if (isResolved) throw new IllegalStateException("already resolved!");
		
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
	public boolean canBeBoundTo(ResolvedTypeX aCandidateType) {
		if (!isResolved) throw new IllegalStateException("Can't answer binding questions prior to resolving");
		// can be bound iff...
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
	
	private boolean isASubtypeOf(TypeX candidateSuperType, TypeX candidateSubType) {
		ResolvedTypeX superType = (ResolvedTypeX) candidateSuperType;
		ResolvedTypeX subType = (ResolvedTypeX) candidateSubType;
		return superType.isAssignableFrom(subType);
	}

	// only used when resolving circular dependencies
	public void setUpperBound(TypeX aTypeX) {
		this.upperBound = aTypeX;
	}
	
	// good enough approximation
	public String toString() {
		return "T" + upperBound.getSignature();
	}
}
