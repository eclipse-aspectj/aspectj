/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.PartialOrder;

public abstract class ConcreteTypeMunger implements PartialOrder.PartialComparable {
	protected ResolvedTypeMunger munger;
	protected ResolvedTypeX aspectType;

	public ConcreteTypeMunger(ResolvedTypeMunger munger, ResolvedTypeX aspectType) {
		this.munger = munger;
		this.aspectType = aspectType;
	}

	//public abstract boolean munge(LazyClassGen gen);


	/** returns null for mungers that are used internally, but were not part of a declared
	 * thing in source code.
	 */
	public ResolvedTypeMunger getMunger() {
		return munger;
	}

	public ResolvedTypeX getAspectType() {
		return aspectType;
	}
	
	public ResolvedMember getSignature() {
		return munger.getSignature();
	}
	
	public ISourceLocation getSourceLocation() {
		return null; //XXX
	}

	public boolean matches(ResolvedTypeX onType) {
		if (munger == null) throw new RuntimeException("huh: " + this);
		return munger.matches(onType, aspectType);
	}

	public ResolvedMember getMatchingSyntheticMember(Member member) {
		return munger.getMatchingSyntheticMember(member, aspectType);
	}

	public int compareTo(Object other) {
		ConcreteTypeMunger o = (ConcreteTypeMunger) other;

		ResolvedTypeX otherAspect = o.aspectType;
		
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
		ConcreteTypeMunger o = (ConcreteTypeMunger) other;
		return 0;
	}

}
