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

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.weaver.patterns.Pointcut;

public class PerObjectInterfaceTypeMunger extends ResolvedTypeMunger {
	private ResolvedMember getMethod;
	private ResolvedMember setMethod;
	private TypeX aspectType;
	private TypeX interfaceType;
	private Pointcut testPointcut;


	public PerObjectInterfaceTypeMunger(TypeX aspectType, Pointcut testPointcut) {
		super(PerObjectInterface, null);
		this.aspectType = aspectType;
		this.testPointcut = testPointcut;
		this.interfaceType = AjcMemberMaker.perObjectInterfaceType(aspectType);
		this.getMethod = AjcMemberMaker.perObjectInterfaceGet(aspectType);
		this.setMethod = AjcMemberMaker.perObjectInterfaceSet(aspectType);
	}
	

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("shouldn't be serialized");
	}
	public TypeX getAspectType() {
		return aspectType;
	}

	public ResolvedMember getGetMethod() {
		return getMethod;
	}

	public TypeX getInterfaceType() {
		return interfaceType;
	}

	public ResolvedMember getSetMethod() {
		return setMethod;
	}

	public Pointcut getTestPointcut() {
		return testPointcut;
	}
	
	public boolean matches(ResolvedTypeX matchType, ResolvedTypeX aspectType) {
		//??? this matches many more types than are needed
		return !matchType.isInterface();
	}

}
