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

import org.aspectj.weaver.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class InterTypeMethodBinding extends MethodBinding {
	//private ReferenceBinding withinType;
	private ReferenceBinding targetType;
	private MethodBinding syntheticMethod;
		
	//public MethodBinding introducedMethod;
	
	public AbstractMethodDeclaration sourceMethod;
	
	public InterTypeMethodBinding(EclipseWorld world, ResolvedMember signature, TypeX withinType,
									AbstractMethodDeclaration sourceMethod)
	{
		super(world.makeMethodBinding(signature), null);
		this.sourceMethod = sourceMethod;
		
		targetType = (ReferenceBinding)world.makeTypeBinding(signature.getDeclaringType());
		this.declaringClass = (ReferenceBinding)world.makeTypeBinding(withinType);
		
		if (signature.getKind() == Member.METHOD) {			
			syntheticMethod = 
				world.makeMethodBinding(AjcMemberMaker.interMethodDispatcher(signature, withinType));
		} else {
			syntheticMethod = world.makeMethodBinding(
				AjcMemberMaker.interConstructor(world.resolve(signature.getDeclaringType()),
							signature, withinType));
		}

		
	}
		
//		
//		this.declaringClass = 
//		
//		
//		
//		char[] dispatch2Name;
//		if (Modifier.isPublic(modifiers)) {
//			dispatch2Name = name;
//		} else if (Modifier.isPrivate(modifiers)) {
//			dispatch2Name = 
//			AstUtil.makeAjcMangledName("dispatch2".toCharArray(), withinType, selector);
//		} else {
//			// package visible
//			//??? optimize both in same package
//			dispatch2Name = 
//				AstUtil.makeAjcMangledName("dispatch2".toCharArray(), 
//								withinType.qualifiedPackageName(), selector);
//		}
//		
//		introducedMethod =
//			new MethodBinding(AstUtil.makePublic(modifiers), dispatch2Name, 
//							type, args, exceptions, declaringClass);
//		
//		this.dispatchMethod =
//				new DispatchMethodBinding(introducedMethod, withinType, mangledParams);
//	}


	//XXX this is identical to InterTypeFieldBinding
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


	public MethodBinding getAccessMethod() {
		return syntheticMethod;
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
