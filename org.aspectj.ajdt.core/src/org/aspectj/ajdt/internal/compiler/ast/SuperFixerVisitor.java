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

import java.util.*;
import java.util.Arrays;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseWorld;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ShadowMunger;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Takes a method that already has the three extra parameters
 * thisJoinPointStaticPart, thisJoinPoint and thisEnclosingJoinPointStaticPart
 */

public class SuperFixerVisitor extends AbstractSyntaxTreeVisitorAdapter {
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
		char[] accessName;
		if (call.isSuperAccess() && !call.binding.isStatic()) {
			call.receiver = new ThisReference();
			accessName =
				NameMangler.superDispatchMethod(EclipseWorld.fromBinding(targetClass), 
							new String(superBinding.selector)).toCharArray();
		} else if (call.receiver.isThis() && call.binding.isProtected() && !call.binding.isStatic()) {
			//XXX this is a hack that violates some binary compatibility rules
			if (superBinding.declaringClass.equals(targetClass)) {
				accessName =
					NameMangler.protectedDispatchMethod(EclipseWorld.fromBinding(targetClass), 
								new String(superBinding.selector)).toCharArray();
			} else {
				accessName =
				NameMangler.superDispatchMethod(EclipseWorld.fromBinding(targetClass), 
							new String(superBinding.selector)).toCharArray();
			}
		} else {
			return;
		}
		
		//??? do we want these to be unique
		MethodBinding superAccessBinding =
			new MethodBinding(AstNode.AccPublic, accessName, 
			superBinding.returnType, superBinding.parameters, superBinding.thrownExceptions,
			targetClass);
		
		call.codegenBinding = superAccessBinding;
		
		ResolvedMember targetMember = EclipseWorld.makeResolvedMember(superBinding);
		superMethodsCalled.add(targetMember);
	}
}
