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

import java.util.*;

import org.aspectj.ajdt.internal.compiler.ast.*;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.bridge.*;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;

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
