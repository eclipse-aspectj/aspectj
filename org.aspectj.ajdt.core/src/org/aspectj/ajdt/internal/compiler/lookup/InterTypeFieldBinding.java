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

import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.TypeX;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.SyntheticAccessMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class InterTypeFieldBinding extends FieldBinding {
	public ReferenceBinding targetType;
	public SyntheticAccessMethodBinding reader;
	public SyntheticAccessMethodBinding writer;
	public AbstractMethodDeclaration sourceMethod;
	
	public InterTypeFieldBinding(EclipseWorld world, ResolvedMember signature, TypeX withinType,
									AbstractMethodDeclaration sourceMethod)
	{
		super(world.makeFieldBinding(signature), null);
		this.sourceMethod = sourceMethod;
		
		targetType = (ReferenceBinding)world.makeTypeBinding(signature.getDeclaringType());
		this.declaringClass = (ReferenceBinding)world.makeTypeBinding(withinType);
		
		reader = new SimpleSyntheticAccessMethodBinding(world.makeMethodBinding(
			AjcMemberMaker.interFieldGetDispatcher(signature, withinType)
		));
		
		writer = new SimpleSyntheticAccessMethodBinding(world.makeMethodBinding(
			AjcMemberMaker.interFieldSetDispatcher(signature, withinType)
		));
	}
	
	public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
		if (isPublic()) return true;	
	
		SourceTypeBinding invocationType = scope.invocationType();
		//System.out.println("receiver: " + receiverType + ", " + invocationType);
		
		if (invocationType == declaringClass) return true;
	
	
	//	if (invocationType.isPrivileged) {
	//		System.out.println("privileged access to: " + this);
	//		return true;
	//	}
		
		if (isProtected()) {
			throw new RuntimeException("unimplemented");
		}
	
		//XXX make sure this walks correctly
		if (isPrivate()) {
			// answer true if the receiverType is the declaringClass
			// AND the invocationType and the declaringClass have a common enclosingType
			if (receiverType != declaringClass) return false;
	
			if (invocationType != declaringClass) {
				ReferenceBinding outerInvocationType = invocationType;
				ReferenceBinding temp = outerInvocationType.enclosingType();
				while (temp != null) {
					outerInvocationType = temp;
					temp = temp.enclosingType();
				}
	
				ReferenceBinding outerDeclaringClass = declaringClass;
				temp = outerDeclaringClass.enclosingType();
				while (temp != null) {
					outerDeclaringClass = temp;
					temp = temp.enclosingType();
				}
				if (outerInvocationType != outerDeclaringClass) return false;
			}
			return true;
		}
	
		// isDefault()
		if (invocationType.fPackage == declaringClass.fPackage) return true;
		return false;
	}


	public SyntheticAccessMethodBinding getAccessMethod(boolean isReadAccess) {
		if (isReadAccess) return reader;
		else return writer;
	}
	
	public boolean alwaysNeedsAccessMethod(boolean isReadAccess) { return true; }

	

	public ReferenceBinding getTargetType() {
		return targetType;
	}

}
