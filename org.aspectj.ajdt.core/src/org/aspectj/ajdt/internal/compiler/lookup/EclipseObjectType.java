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


package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.weaver.CrosscuttingMembers;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedTypeX;

public abstract class EclipseObjectType extends ResolvedTypeX.Name {
	protected ResolvedPointcutDefinition[] declaredPointcuts = null;
	protected ResolvedMember[] declaredMethods = null;
	protected ResolvedMember[] declaredFields = null;
	

	public EclipseObjectType(String signature, EclipseWorld world, boolean isExposedToWeaver) {
		super(signature, world, isExposedToWeaver);
	}
	
	protected EclipseWorld eclipseWorld() {
		return (EclipseWorld)world;
	}


	public abstract boolean isAspect();
	
	public abstract ResolvedTypeX getSuperclass();
	
	public abstract ResolvedTypeX[] getDeclaredInterfaces();

	public ResolvedMember[] getDeclaredFields() {
		if (declaredFields == null) fillDeclaredMembers();
		return declaredFields;
	}

	public ResolvedMember[] getDeclaredMethods() {
		if (declaredMethods == null) fillDeclaredMembers();
		return declaredMethods;
	}

	public ResolvedMember[] getDeclaredPointcuts() {
		if (declaredPointcuts == null) fillDeclaredMembers();
		return declaredPointcuts;
	}

	protected abstract void fillDeclaredMembers();

	
	public abstract int getModifiers();
	

	public CrosscuttingMembers collectCrosscuttingMembers() {
		return crosscuttingMembers;
	}

}
