/* *******************************************************************
 * Copyright (c) 2005
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

import org.aspectj.weaver.patterns.PerTypeWithin;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;

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
	
	public boolean matches(ResolvedTypeX matchType, ResolvedTypeX aspectType) {
		return testPointcut.getTypePattern().matches(matchType,TypePattern.STATIC).alwaysTrue();
	}

}
