/* *******************************************************************
 * Copyright (c) 2005 IBM, Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.patterns.PerTypeWithin;
import org.aspectj.weaver.patterns.Pointcut;

// PTWIMPL Target type munger adds the localAspectOf() method
public class PerTypeWithinTargetTypeMunger extends ResolvedTypeMunger {
	private ResolvedMember localAspectOfMethod;
	private TypeX aspectType;
	private PerTypeWithin testPointcut;


	public PerTypeWithinTargetTypeMunger(TypeX aspectType, PerTypeWithin testPointcut) {
		super(PerTypeWithinInterface, null);
		this.aspectType    = aspectType;
		this.testPointcut  = testPointcut;
	}
	

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("shouldn't be serialized");
	}
	
	public TypeX getAspectType() {
		return aspectType;
	}

	public Pointcut getTestPointcut() {
		return testPointcut;
	}
	
	// This is a lexical within() so if you say PerTypeWithin(Test) and matchType is an
	// inner type (e.g. Test$NestedType) then it should match successfully
	// Does not match if the target is an interface
	public boolean matches(ResolvedTypeX matchType, ResolvedTypeX aspectType) {
		return isWithinType(matchType).alwaysTrue() && 
		       !matchType.isInterface();
	}
	
	private FuzzyBoolean isWithinType(ResolvedTypeX type) {
		while (type != null) {
			if (testPointcut.getTypePattern().matchesStatically(type)) {
				return FuzzyBoolean.YES;
			}
			type = type.getDeclaringType();
		}
		return FuzzyBoolean.NO;
	}

}
