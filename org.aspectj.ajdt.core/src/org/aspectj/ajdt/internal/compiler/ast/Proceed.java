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

import org.aspectj.weaver.AdviceKind;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;


public class Proceed extends MessageSend {
	public boolean inInner = false;
	
	public Proceed(MessageSend parent) {
		super();
		
		this.receiver = parent.receiver;
		this.selector  = parent.selector;
		this.arguments  = parent.arguments;
		this.binding  = parent.binding;
		this.codegenBinding = parent.codegenBinding;

		this.nameSourcePosition = parent.nameSourcePosition;
		this.receiverType = parent.receiverType;
		this.qualifyingType = qualifyingType;
		
		this.sourceStart = parent.sourceStart;
		this.sourceEnd = parent.sourceEnd;
	}

	public TypeBinding resolveType(BlockScope scope) {
		// find out if I'm really in an around body or not
		//??? there is a small performance issue here
		AdviceDeclaration aroundDecl = findEnclosingAround(scope);
		
		if (aroundDecl == null) {
			return super.resolveType(scope);
		}
		


		constant = NotAConstant;
		binding = codegenBinding = aroundDecl.proceedMethodBinding;
		
		this.qualifyingType = this.receiverType = binding.declaringClass;
		
		int baseArgCount = 0;
		if (arguments != null) {
			baseArgCount = arguments.length;
			Expression[] newArguments = new Expression[baseArgCount + 1];
			System.arraycopy(arguments, 0, newArguments, 0, baseArgCount);
			arguments = newArguments;
		} else {
			arguments = new Expression[1];
		}
		
		arguments[baseArgCount] = AstUtil.makeLocalVariableReference(aroundDecl.extraArgument.binding);
		
		int declaredParameterCount = aroundDecl.getDeclaredParameterCount();
		if (baseArgCount < declaredParameterCount) {
			scope.problemReporter().signalError(this.sourceStart, this.sourceEnd, 
								"too few arguments to proceed, expected " + declaredParameterCount);
			aroundDecl.ignoreFurtherInvestigation = true;
			return null; //binding.returnType;
		}
		
		if (baseArgCount > declaredParameterCount) {
			scope.problemReporter().signalError(this.sourceStart, this.sourceEnd, 
								"too many arguments to proceed, expected " + declaredParameterCount);
			aroundDecl.ignoreFurtherInvestigation = true;
			return null; //binding.returnType;
		}

		
		for (int i=0, len=arguments.length; i < len; i++) {
			Expression arg = arguments[i];
			TypeBinding argType = arg.resolveType(scope);
			if (argType != null) {
				TypeBinding paramType = binding.parameters[i];
				if (!scope.areTypesCompatible(argType, paramType)) {
					scope.problemReporter().typeMismatchError(argType, paramType, arg);
				}
				arg.implicitWidening(binding.parameters[i], argType);
			}
		}
	
		
		return binding.returnType;
	}

	private AdviceDeclaration findEnclosingAround(Scope scope) {
		if (scope == null) return null;
		
		if (scope instanceof MethodScope) {
			MethodScope methodScope = (MethodScope)scope;
			ReferenceContext context = methodScope.referenceContext;
			if (context instanceof AdviceDeclaration) {
				AdviceDeclaration adviceDecl = (AdviceDeclaration)context;
				if (adviceDecl.kind == AdviceKind.Around) {
					adviceDecl.proceedCalls.add(this);
					return adviceDecl;
				} else {
					return null;
				}
			}
		} else if (scope instanceof ClassScope) {
			inInner = true;
		}
		
		return findEnclosingAround(scope.parent);
	}



}
