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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.IPrivilegedHandler;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;



public class PrivilegedHandler implements IPrivilegedHandler {
	private AspectDeclaration inAspect;
	private Map accessors = new HashMap();

	public PrivilegedHandler(AspectDeclaration inAspect) {
		this.inAspect = inAspect;
	}

	public FieldBinding getPrivilegedAccessField(FieldBinding baseField, AstNode location) {
		ResolvedMember key = inAspect.world.makeResolvedMember(baseField);
		if (accessors.containsKey(key)) return (FieldBinding)accessors.get(key);
		FieldBinding ret = new PrivilegedFieldBinding(inAspect, baseField);
		checkWeaveAccess(key.getDeclaringType(), location);
		accessors.put(key, ret);
		return ret;
	}

	public MethodBinding getPrivilegedAccessMethod(MethodBinding baseMethod, AstNode location) {
		ResolvedMember key = inAspect.world.makeResolvedMember(baseMethod);
		if (accessors.containsKey(key)) return (MethodBinding)accessors.get(key);
		
		MethodBinding ret;
		if (baseMethod.isConstructor()) {
			ret = baseMethod;
		} else {
			ret = inAspect.world.makeMethodBinding(
			AjcMemberMaker.privilegedAccessMethodForMethod(inAspect.typeX, key)
			);
		}
		checkWeaveAccess(key.getDeclaringType(), location);
		//new PrivilegedMethodBinding(inAspect, baseMethod);
		accessors.put(key, ret);
		return ret;
	}
	
	public void notePrivilegedTypeAccess(ReferenceBinding type, AstNode location) {
		ResolvedMember key =
			new ResolvedMember(Member.STATIC_INITIALIZATION,
				inAspect.world.fromEclipse(type), 0, ResolvedTypeX.VOID, "", TypeX.NONE);
		
		checkWeaveAccess(key.getDeclaringType(), location);
		accessors.put(key, key);
	}

	private void checkWeaveAccess(TypeX typeX, AstNode location) {
		World world = inAspect.world;
		Lint.Kind check = world.getLint().typeNotExposedToWeaver;
		if (check.isEnabled()) {
			if (!world.resolve(typeX).isExposedToWeaver()) {
				ISourceLocation loc = null;
				if (location != null) {
					loc = new EclipseSourceLocation(inAspect.compilationResult, 
							location.sourceStart, location.sourceEnd);
				}
				check.signal(typeX.getName() + " (needed for privileged access)",
							loc);
			}
		}
	}
	
	public ResolvedMember[] getMembers() {
		Collection m = accessors.keySet();
		int len = m.size();
		ResolvedMember[] ret = new ResolvedMember[len];
		int index = 0;
		for (Iterator i = m.iterator(); i.hasNext(); ) {
			ret[index++] = (ResolvedMember)i.next();
		}
		return ret;
	}
}
