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
import org.aspectj.weaver.patterns.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class EclipseBinaryType extends EclipseObjectType {
	private BinaryTypeBinding binding;
	private ResolvedTypeX delegate;

	public EclipseBinaryType(ResolvedTypeX delegate, EclipseWorld world, BinaryTypeBinding binding) {
		super(delegate.getSignature(), world, delegate.isExposedToWeaver());
		this.delegate = delegate;
		this.binding = binding;
	}
	

	protected void fillDeclaredMembers() {
		this.declaredPointcuts = copyResolvedPointcutDefinitions(delegate.getDeclaredPointcuts());
		this.declaredFields = copyResolvedMembers(delegate.getDeclaredFields());
		this.declaredMethods = copyResolvedMembers(delegate.getDeclaredMethods());
	}

	//XXX doesn't actually copy
	private ResolvedPointcutDefinition[] copyResolvedPointcutDefinitions(ResolvedMember[] in) {
		//System.err.println("defs: " + this + " are " + Arrays.asList(in));
		return (ResolvedPointcutDefinition[])in;
	}



	private ResolvedMember[] copyResolvedMembers(ResolvedMember[] in) {
		int len = in.length;
		ResolvedMember[] out = new ResolvedMember[len];
		for (int i=0; i < len; i++) {
			out[i] = copyResolvedMember(in[i]);
		}
		
		return out;
	}

	private ResolvedMember copyResolvedMember(ResolvedMember in) {
		ResolvedMember ret = new ResolvedMember(
			in.getKind(), forceTypeX(in.getDeclaringType()), in.getModifiers(),
			forceTypeX(in.getReturnType()), in.getName(),
			forceTypeXs(in.getParameterTypes()));
		ret.setPosition(in.getStart(), in.getEnd());
		ret.setSourceContext(in.getSourceContext());
		ret.setCheckedExceptions(forceTypeXs(in.getExceptions()));
		return ret;
	}

	private TypeX forceTypeX(TypeX typeX) {
		return TypeX.forSignature(typeX.getSignature());
	}

	private TypeX[] forceTypeXs(TypeX[] in) {
		int len = in.length;
		if (len == 0) return TypeX.NONE;
		TypeX[] ret = new TypeX[len];
		for (int i=0; i < len; i++) {
			ret[i] = forceTypeX(in[i]);
		}
		return ret;
	}


	public ResolvedTypeX[] getDeclaredInterfaces() {
		return world.resolve(delegate.getDeclaredInterfaces());
	}

	public int getModifiers() {
		return delegate.getModifiers();
	}

	public ResolvedTypeX getSuperclass() {
		if (delegate.getSuperclass() == null) return null;
		return world.resolve(delegate.getSuperclass());
	}

	public boolean isAspect() {
		return delegate.isAspect();
	}
	
	public String toString() {
		return "EclipseBinaryType(" + getClassName() + ")";
	}

}
