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


package org.aspectj.ajdt.internal.compiler.ast;

import java.util.HashSet;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeMethodBinding;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Takes a method that already has the three extra parameters
 * thisJoinPointStaticPart, thisJoinPoint and thisEnclosingJoinPointStaticPart
 */

public class SuperFixerVisitor extends ASTVisitor {
	Set superMethodsCalled = new HashSet();
	AbstractMethodDeclaration method;
	ReferenceBinding targetClass;

	SuperFixerVisitor(AbstractMethodDeclaration method, ReferenceBinding targetClass) {
		this.method = method;
		this.targetClass = targetClass;
	}


	//XXX does this walk into inners
	public void endVisit(MessageSend call, BlockScope scope) {
		//System.out.println("endVisit: " + call);
		// an error has already occurred
		if (call.codegenBinding == null) return; 
		
		MethodBinding superBinding = call.codegenBinding;
		if (superBinding instanceof ProblemMethodBinding) {
			return;
		}
		// InterTypeMethodBindings are always statically bound, so there's no
		// need to treat super calls specially here
		if (superBinding instanceof InterTypeMethodBinding) {
			return;
//			InterTypeMethodBinding m = (InterTypeMethodBinding)superBinding;
//			if (m.postDispatchMethod != null) {
//				call.binding = m.postDispatchMethod;
//			}
//			return;
		}
		
		char[] accessName;
		if (call.isSuperAccess() && !call.binding.isStatic()) {
			call.receiver = new ThisReference(call.receiver.sourceStart, call.receiver.sourceEnd);
			accessName =
				NameMangler.superDispatchMethod(EclipseFactory.fromBinding(targetClass), 
							new String(superBinding.selector)).toCharArray();
		} else if (call.receiver.isThis() && call.binding.isProtected() && !call.binding.isStatic()) {
			//XXX this is a hack that violates some binary compatibility rules
			if (superBinding.declaringClass.equals(targetClass)) {
				accessName =
					NameMangler.protectedDispatchMethod(EclipseFactory.fromBinding(targetClass), 
								new String(superBinding.selector)).toCharArray();
			} else {
				accessName =
				NameMangler.superDispatchMethod(EclipseFactory.fromBinding(targetClass), 
							new String(superBinding.selector)).toCharArray();
			}
		} else {
			return;
		}
		
		//??? do we want these to be unique
		MethodBinding superAccessBinding =
			new MethodBinding(ASTNode.AccPublic, accessName, 
			superBinding.returnType, superBinding.parameters, superBinding.thrownExceptions,
			targetClass);
			
		AstUtil.replaceMethodBinding(call, superAccessBinding);
		
		ResolvedMember targetMember = EclipseFactory.makeResolvedMember(superBinding);
		superMethodsCalled.add(targetMember);
	}
}
