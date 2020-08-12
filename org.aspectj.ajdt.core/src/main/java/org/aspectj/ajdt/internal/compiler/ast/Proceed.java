/* *******************************************************************
 * Copyright (c) 2002-2014 Palo Alto Research Center, Incorporated (PARC) 
 *               and Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation
 *     IBM      ongoing maintenance 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AdviceKind;

/**
 * Used to represent any method call to a method named <code>proceed</code>.  During
 * <code>resolvedType</code> it will be determined if this is actually in the body
 * of an <code>around</code> advice and has no receiver (must be a bare proceed call, 
 * see pr 53981), and if not this will be treated like any other
 * MessageSend.
 * 
 * @author Jim Hugunin
 */
public class Proceed extends MessageSend {
	public boolean inInner = false;
	
	public Proceed(MessageSend parent) {
		super();
		
		this.receiver = parent.receiver;
		this.selector  = parent.selector;
		this.arguments  = parent.arguments;
		this.binding  = parent.binding;
		//this.codegenBinding = parent.codegenBinding;
		this.syntheticAccessor = parent.syntheticAccessor;
		this.expectedType = parent.expectedType;

		this.nameSourcePosition = parent.nameSourcePosition;
		this.actualReceiverType = parent.actualReceiverType;
		//this.qualifyingType = parent.qualifyingType;
		
		this.valueCast = parent.valueCast;
		this.typeArguments = parent.typeArguments;
		this.genericTypeArguments = parent.genericTypeArguments;
		
		this.sourceStart = parent.sourceStart;
		this.sourceEnd = parent.sourceEnd;
	}

	public TypeBinding resolveType(BlockScope scope) {
		// find out if I'm really in an around body or not
		//??? this could in theory be done by the parser, but that appears to be hard
		AdviceDeclaration aroundDecl = findEnclosingAround(scope);
		
		if (aroundDecl == null) {
			return super.resolveType(scope);
		}
		
		constant = Constant.NotAConstant;
		binding =/* codegenBinding = */aroundDecl.proceedMethodBinding;
		
		this.actualReceiverType = binding.declaringClass;
		
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

		boolean argsContainCast = false;
		for (Expression argument : arguments) {
			if (argument instanceof CastExpression) argsContainCast = true;
			//	if (arguments[i].constant==null) arguments[i].constant=Constant.NotAConstant;
		}
//		TypeBinding[] argumentTypes = Binding.NO_PARAMETERS;
//		if (this.arguments != null) {
//			boolean argHasError = false; // typeChecks all arguments 
//			int length = this.arguments.length;
//			argumentTypes = new TypeBinding[length];
//			for (int i = 0; i < length; i++){
//				Expression argument = this.arguments[i];
//				if (argument instanceof CastExpression) {
//					argument.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
//					argsContainCast = true;
//				}
//				if ((argumentTypes[i] = argument.resolveType(scope)) == null){
//					argHasError = true;
//				}
//			}
//			if (argHasError) {
//				if (this.actualReceiverType instanceof ReferenceBinding) {
//					//  record a best guess, for clients who need hint about possible method match
//					TypeBinding[] pseudoArgs = new TypeBinding[length];
//					for (int i = length; --i >= 0;)
//						pseudoArgs[i] = argumentTypes[i] == null ? TypeBinding.NULL : argumentTypes[i]; // replace args with errors with null type
//					this.binding = 
//						this.receiver.isImplicitThis()
//							? scope.getImplicitMethod(this.selector, pseudoArgs, this)
//							: scope.findMethod((ReferenceBinding) this.actualReceiverType, this.selector, pseudoArgs, this);
//					if (this.binding != null && !this.binding.isValidBinding()) {
//						MethodBinding closestMatch = ((ProblemMethodBinding)this.binding).closestMatch;
//						// record the closest match, for clients who may still need hint about possible method match
//						if (closestMatch != null) {
//							if (closestMatch.original().typeVariables != Binding.NO_TYPE_VARIABLES) { // generic method
//								// shouldn't return generic method outside its context, rather convert it to raw method (175409)
//								closestMatch = scope.environment().createParameterizedGenericMethod(closestMatch.original(), (RawTypeBinding)null);
//							}
//							this.binding = closestMatch;
//							MethodBinding closestMatchOriginal = closestMatch.original();
//							if ((closestMatchOriginal.isPrivate() || closestMatchOriginal.declaringClass.isLocalType()) && !scope.isDefinedInMethod(closestMatchOriginal)) {
//								// ignore cases where method is used from within inside itself (e.g. direct recursions)
//								closestMatchOriginal.modifiers |= ExtraCompilerModifiers.AccLocallyUsed;
//							}
//						}
//					}
//				}
//				return null;
//			}
//		}
//


	//	checkInvocationArguments(scope, this.receiver, this.actualReceiverType, this.binding, this.arguments, argumentTypes, argsContainCast, this);
		int len = arguments.length;
		this.argumentTypes = (len == 0? TypeBinding.NO_TYPES:new TypeBinding[len]);
		for (int i=0; i < len; i++) {
			Expression arg = arguments[i];
			argumentTypes[i] = arg.resolveType(scope);
			if (argumentTypes[i] != null) {
				TypeBinding paramType = binding.parameters[i];
				if (!argumentTypes[i].isCompatibleWith(paramType)) {
					scope.problemReporter().typeMismatchError(argumentTypes[i], paramType, arg, null);
				}
			}
		}
		checkInvocationArguments(scope,null,this.actualReceiverType,binding,
				this.arguments,binding.parameters,argsContainCast,this);

		this.resolvedType = binding.returnType;
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
					// pr 53981 only match "bare" calls to proceed
					if((receiver != null) && (!receiver.isThis())) { return null; }
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
