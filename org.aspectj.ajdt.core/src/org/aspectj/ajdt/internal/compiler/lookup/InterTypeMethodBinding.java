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
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.TypeX;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class InterTypeMethodBinding extends MethodBinding {
	private ReferenceBinding targetType;
	private MethodBinding syntheticMethod;
		
	public MethodBinding postDispatchMethod;
	
	public AbstractMethodDeclaration sourceMethod;
	
	public InterTypeMethodBinding(EclipseFactory world, ResolvedMember signature, TypeX withinType,
									AbstractMethodDeclaration sourceMethod)
	{
		super(world.makeMethodBinding(signature), null);
		this.sourceMethod = sourceMethod;
		
		targetType = (ReferenceBinding)world.makeTypeBinding(signature.getDeclaringType());
		this.declaringClass = (ReferenceBinding)world.makeTypeBinding(withinType);
		
		if (signature.getKind() == Member.METHOD) {			
			syntheticMethod = 
				world.makeMethodBinding(AjcMemberMaker.interMethodDispatcher(signature, withinType));
			postDispatchMethod = 
				world.makeMethodBinding(AjcMemberMaker.interMethodBody(signature, withinType));
		} else {
			syntheticMethod = world.makeMethodBinding(
				AjcMemberMaker.interConstructor(world.getWorld().resolve(signature.getDeclaringType()),
							signature, withinType));
			postDispatchMethod = syntheticMethod;
		}

		
	}

	//XXX this is identical to InterTypeFieldBinding
	public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
		scope.compilationUnitScope().recordTypeReference(declaringClass);
		
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
			//if (receiverType != declaringClass) return false;
	
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
				
				//System.err.println("outer dec: " + 
				if (outerInvocationType != outerDeclaringClass) return false;
			}
			return true;
		}
	
		// isDefault()
		if (invocationType.fPackage == declaringClass.fPackage) return true;
		return false;
	}


	public MethodBinding getAccessMethod(boolean staticReference) {
		if (staticReference) return postDispatchMethod;
		else return syntheticMethod;
	}
	
	public boolean alwaysNeedsAccessMethod() { return true; }

	public AbstractMethodDeclaration sourceMethod() {
		return sourceMethod;
	}
	
	public ReferenceBinding getTargetType() {
		return targetType;
	}
	
	public String toString() {
		return "InterTypeMethodBinding(" + super.toString() + ", " + getTargetType() +")";
	}
}
