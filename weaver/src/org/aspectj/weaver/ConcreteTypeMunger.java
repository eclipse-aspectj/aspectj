/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.PartialOrder;

public abstract class ConcreteTypeMunger implements PartialOrder.PartialComparable {
	protected ResolvedTypeMunger munger;
	protected ResolvedType aspectType;

	public ConcreteTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
		this.munger = munger;
		this.aspectType = aspectType;
	}

    // An EclipseTypeMunger and a BcelTypeMunger may say TRUE for equivalentTo()...
	public boolean equivalentTo(Object other) {
        if (! (other instanceof ConcreteTypeMunger))  return false;
        ConcreteTypeMunger o = (ConcreteTypeMunger) other;
        return ((o.getMunger() == null) ? (getMunger() == null) : o.getMunger().equals(getMunger()))
               && ((o.getAspectType() == null) ? (getAspectType() == null) : o.getAspectType().equals(getAspectType()));
    }

	//public abstract boolean munge(LazyClassGen gen);


	/** returns null for mungers that are used internally, but were not part of a declared
	 * thing in source code.
	 */
	public ResolvedTypeMunger getMunger() {
		return munger;
	}

	public ResolvedType getAspectType() {
		return aspectType;
	}
	
	public ResolvedMember getSignature() {
		return munger.getSignature();
	}
	
	public World getWorld() {
		return aspectType.getWorld();
	}
	
	public ISourceLocation getSourceLocation() {
		if (munger == null) return null;
		return munger.getSourceLocation(); //XXX
	}

	public boolean matches(ResolvedType onType) {
		if (munger == null) throw new RuntimeException("huh: " + this);
		return munger.matches(onType, aspectType);
	}

	public ResolvedMember getMatchingSyntheticMember(Member member) {
		return munger.getMatchingSyntheticMember(member, aspectType);
	}

	public int compareTo(Object other) {
		ConcreteTypeMunger o = (ConcreteTypeMunger) other;

		ResolvedType otherAspect = o.aspectType;
		
		if (aspectType.equals(otherAspect)) {
			return getSignature().getStart() < o.getSignature().getStart() ? -1: +1;
		} else if (aspectType.isAssignableFrom(o.aspectType)) {
			return +1;
		} else if (o.aspectType.isAssignableFrom(aspectType)) {
			return -1;
		} else {
			return 0;
		}
	}

	public int fallbackCompareTo(Object other) {
//		ConcreteTypeMunger o = (ConcreteTypeMunger) other;
		return 0;
	}
	
	/**
	 * returns true if the ITD target type used type variables, for example I<T>.
	 * When they are specified like this, the ITDs 'share' type variables with
	 * the generic type.  Usually this method is called because we need to know
	 * whether to tailor the munger for addition to a particular type. For example:
	 * <code>
	 *   interface I<T> {}
	 *   
	 *   aspect X implements I<String> {
	 *     List<T> I<T>.foo { return null; }
	 *   }
	 * </code>
	 * In this case the munger matches X but it matches with the form
	 * <code>
	 *   List<String> foo() { return null; }
	 * </code>
	 */
	public boolean isTargetTypeParameterized() {
		if (munger==null) return false; 
		return munger.sharesTypeVariablesWithGenericType();
	}

    /**
     * For an ITD made on a generic type that shares type variables with
     * that target type, this method will tailor the ITD for a particular usage
     * of the generic type - either in its raw or parameterized form.
     */
	public abstract ConcreteTypeMunger parameterizedFor(ResolvedType targetType);
	
	public boolean isLateMunger() {
		if (munger==null) return false;
		return munger.isLateMunger();
	}

	public abstract ConcreteTypeMunger parameterizeWith(Map parameterizationMap, World world);
}
