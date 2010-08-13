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

	/**
	 * Equivalence can be true for an EclipseTypeMunger and a BcelTypeMunger that represent the same transformation (just at
	 * different points in the pipeline).
	 */
	public boolean equivalentTo(Object other) {
		if (!(other instanceof ConcreteTypeMunger)) {
			return false;
		}
		ConcreteTypeMunger o = (ConcreteTypeMunger) other;
		ResolvedTypeMunger otherTypeMunger = o.getMunger();
		ResolvedTypeMunger thisTypeMunger = getMunger();
		if (thisTypeMunger instanceof NewConstructorTypeMunger && otherTypeMunger instanceof NewConstructorTypeMunger) {
			return (((NewConstructorTypeMunger) otherTypeMunger).equivalentTo(thisTypeMunger))
					&& ((o.getAspectType() == null) ? (getAspectType() == null) : o.getAspectType().equals(getAspectType()));
		} else {
			return ((otherTypeMunger == null) ? (thisTypeMunger == null) : otherTypeMunger.equals(thisTypeMunger))
					&& ((o.getAspectType() == null) ? (getAspectType() == null) : o.getAspectType().equals(getAspectType()));
		}
	}

	// public abstract boolean munge(LazyClassGen gen);

	/**
	 * returns null for mungers that are used internally, but were not part of a declared thing in source code.
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
		if (munger == null) {
			return null;
		}
		return munger.getSourceLocation(); // XXX
	}

	public boolean matches(ResolvedType onType) {
		if (munger == null) {
			throw new RuntimeException("huh: " + this);
		}
		return munger.matches(onType, aspectType);
	}

	public ResolvedMember getMatchingSyntheticMember(Member member) {
		return munger.getMatchingSyntheticMember(member, aspectType);
	}

	public int compareTo(Object other) {
		ConcreteTypeMunger o = (ConcreteTypeMunger) other;

		ResolvedType otherAspect = o.aspectType;

		if (aspectType.equals(otherAspect)) {
			return getSignature().getStart() < o.getSignature().getStart() ? -1 : +1;
		} else if (aspectType.isAssignableFrom(o.aspectType)) {
			return +1;
		} else if (o.aspectType.isAssignableFrom(aspectType)) {
			return -1;
		} else {
			return 0;
		}
	}

	public int fallbackCompareTo(Object other) {
		// ConcreteTypeMunger o = (ConcreteTypeMunger) other;
		return 0;
	}

	/**
	 * returns true if the ITD target type used type variables, for example I<T>. When they are specified like this, the ITDs
	 * 'share' type variables with the generic type. Usually this method is called because we need to know whether to tailor the
	 * munger for addition to a particular type. For example: <code>
	 *   interface I<T> {}
	 *   
	 *   aspect X implements I<String> {
	 *     List<T> I<T>.foo { return null; }
	 *   }
	 * </code> In this case the munger matches X but it matches with the form <code>
	 *   List<String> foo() { return null; }
	 * </code>
	 */
	public boolean isTargetTypeParameterized() {
		if (munger == null) {
			return false;
		}
		return munger.sharesTypeVariablesWithGenericType();
	}

	/**
	 * For an ITD made on a generic type that shares type variables with that target type, this method will tailor the ITD for a
	 * particular usage of the generic type - either in its raw or parameterized form.
	 */
	public abstract ConcreteTypeMunger parameterizedFor(ResolvedType targetType);

	public boolean isLateMunger() {
		if (munger == null) {
			return false;
		}
		return munger.isLateMunger();
	}

	public abstract ConcreteTypeMunger parameterizeWith(Map<String, UnresolvedType> parameterizationMap, World world);

	/**
	 * Some type mungers are created purely to help with the implementation of shadow mungers. For example to support the cflow()
	 * pointcut we create a new cflow field in the aspect, and that is added via a BcelCflowCounterFieldAdder.
	 * 
	 * During compilation we need to compare sets of type mungers, and if some only come into existence after the 'shadowy' type
	 * things have been processed, we need to ignore them during the comparison.
	 * 
	 * Returning true from this method indicates the type munger exists to support 'shadowy' stuff - and so can be ignored in some
	 * comparison.
	 */
	public boolean existsToSupportShadowMunging() {
		if (munger != null) {
			return munger.existsToSupportShadowMunging();
		}
		return false;
	}

	public boolean shouldOverwrite() {
		return true;
	}
}
