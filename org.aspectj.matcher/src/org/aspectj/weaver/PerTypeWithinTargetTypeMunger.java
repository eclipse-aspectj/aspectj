/* *******************************************************************
 * Copyright (c) 2005 IBM, Contributors.
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

import java.io.IOException;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.patterns.PerTypeWithin;
import org.aspectj.weaver.patterns.Pointcut;

// PTWIMPL Target type munger adds the localAspectOf() method
public class PerTypeWithinTargetTypeMunger extends ResolvedTypeMunger {
	private UnresolvedType aspectType;
	private PerTypeWithin testPointcut;

	public PerTypeWithinTargetTypeMunger(UnresolvedType aspectType, PerTypeWithin testPointcut) {
		super(PerTypeWithinInterface, null);
		this.aspectType = aspectType;
		this.testPointcut = testPointcut;
	}

	public boolean equals(Object other) {
		if (!(other instanceof PerTypeWithinTargetTypeMunger)) {
			return false;
		}
		PerTypeWithinTargetTypeMunger o = (PerTypeWithinTargetTypeMunger) other;
		return ((o.testPointcut == null) ? (testPointcut == null) : testPointcut.equals(o.testPointcut))
				&& ((o.aspectType == null) ? (aspectType == null) : aspectType.equals(o.aspectType));
	}

	private volatile int hashCode = 0;

	public int hashCode() {
		if (hashCode == 0) {
			int result = 17;
			result = 37 * result + ((testPointcut == null) ? 0 : testPointcut.hashCode());
			result = 37 * result + ((aspectType == null) ? 0 : aspectType.hashCode());
			hashCode = result;
		}
		return hashCode;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		throw new RuntimeException("shouldn't be serialized");
	}

	public UnresolvedType getAspectType() {
		return aspectType;
	}

	public Pointcut getTestPointcut() {
		return testPointcut;
	}

	// This is a lexical within() so if you say PerTypeWithin(Test) and matchType is an
	// inner type (e.g. Test$NestedType) then it should match successfully
	// Does not match if the target is an interface
	public boolean matches(ResolvedType matchType, ResolvedType aspectType) {
		return isWithinType(matchType).alwaysTrue() && !matchType.isInterface();
	}

	private FuzzyBoolean isWithinType(ResolvedType type) {
		while (type != null) {
			if (testPointcut.getTypePattern().matchesStatically(type)) {
				return FuzzyBoolean.YES;
			}
			type = type.getDeclaringType();
		}
		return FuzzyBoolean.NO;
	}

}
