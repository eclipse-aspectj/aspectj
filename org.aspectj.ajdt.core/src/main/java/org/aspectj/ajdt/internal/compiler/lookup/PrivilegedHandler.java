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

package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AstUtil;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.IPrivilegedHandler;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedFieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.PrivilegedAccessMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

public class PrivilegedHandler implements IPrivilegedHandler {
	private AspectDeclaration inAspect;
	private Map accessors = new HashMap();

	public PrivilegedHandler(AspectDeclaration inAspect) {
		this.inAspect = inAspect;
	}

	@Override
	public boolean definesPrivilegedAccessToField(FieldBinding field) {
		if (field instanceof ParameterizedFieldBinding) {
			field = ((ParameterizedFieldBinding) field).originalField;
		}
		ResolvedMember key = inAspect.factory.makeResolvedMember(field);
		return (accessors.containsKey(key));
	}

	@Override
	public FieldBinding getPrivilegedAccessField(FieldBinding baseField, ASTNode location) {
		if (baseField instanceof ParameterizedFieldBinding) {
			baseField = ((ParameterizedFieldBinding) baseField).originalField;
		}
		ResolvedMember key = inAspect.factory.makeResolvedMember(baseField);
		if (accessors.containsKey(key))
			return (FieldBinding) accessors.get(key);
		FieldBinding ret = new PrivilegedFieldBinding(inAspect, baseField);
		checkWeaveAccess(key.getDeclaringType(), location);
		if (!baseField.alwaysNeedsAccessMethod(true))
			accessors.put(key, ret);
		// 307120
		ResolvedType rt = inAspect.factory.fromEclipse(baseField.declaringClass);
		if (rt != null) {
			// ReferenceTypeDelegate rtd = ((ReferenceType) rt).getDelegate();
			// if (rtd instanceof EclipseSourceType) {
			rt.addInterTypeMunger(new EclipseTypeMunger(inAspect.factory, new PrivilegedAccessMunger(key, true), inAspect.typeX,
					null), true);
			// }
		}
		return ret;
	}

	@Override
	public MethodBinding getPrivilegedAccessMethod(MethodBinding baseMethod, ASTNode location) {
		if (baseMethod.alwaysNeedsAccessMethod())
			return baseMethod;

		ResolvedMember key = null;

		if (baseMethod instanceof ParameterizedMethodBinding) {
			key = inAspect.factory.makeResolvedMember(((ParameterizedMethodBinding) baseMethod).original());
		} else {
			key = inAspect.factory.makeResolvedMember(baseMethod);
		}
		if (accessors.containsKey(key))
			return (MethodBinding) accessors.get(key);

		MethodBinding ret;
		if (baseMethod.isConstructor()) {
			ret = new MethodBinding(baseMethod, baseMethod.declaringClass);
			ret.modifiers = AstUtil.makePublic(ret.modifiers);
			baseMethod.modifiers = ret.modifiers;
		} else {
			ret = inAspect.factory.makeMethodBinding(AjcMemberMaker.privilegedAccessMethodForMethod(inAspect.typeX, key));
		}
		checkWeaveAccess(key.getDeclaringType(), location);
		accessors.put(key, ret);
		// if (!baseMethod.isConstructor()) {
		// ResolvedType rt = inAspect.factory.fromEclipse(baseMethod.declaringClass);
		// if (rt!=null) {
		// ReferenceTypeDelegate rtd = ((ReferenceType)rt).getDelegate();
		// if (rtd instanceof EclipseSourceType) {
		// rt.addInterTypeMunger(new EclipseTypeMunger(inAspect.factory,new PrivilegedAccessMunger(key, true),inAspect.typeX,null),
		// true);
		// }
		// }
		// }
		return ret;
	}

	@Override
	public void notePrivilegedTypeAccess(ReferenceBinding type, ASTNode location) {
		ResolvedMember key = new ResolvedMemberImpl(Member.STATIC_INITIALIZATION, inAspect.factory.fromEclipse(type), 0,
				UnresolvedType.VOID, "", UnresolvedType.NONE);

		checkWeaveAccess(key.getDeclaringType(), location);
		accessors.put(key, key);
	}

	private void checkWeaveAccess(UnresolvedType typeX, ASTNode location) {
		World world = inAspect.factory.getWorld();
		Lint.Kind check = world.getLint().typeNotExposedToWeaver;
		if (check.isEnabled()) {
			if (!world.resolve(typeX).isExposedToWeaver()) {
				ISourceLocation loc = null;
				if (location != null) {
					loc = new EclipseSourceLocation(inAspect.compilationResult, location.sourceStart, location.sourceEnd);
				}
				check.signal(typeX.getName() + " (needed for privileged access)", loc);
			}
		}
	}

	public ResolvedMember[] getMembers() {
		Collection m = accessors.keySet();
		int len = m.size();
		ResolvedMember[] ret = new ResolvedMember[len];
		int index = 0;
		for (Object o : m) {
			ret[index++] = (ResolvedMember) o;
		}
		return ret;
	}
}
