/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CodeSnippetMessageSend extends MessageSend implements ProblemReasons, EvaluationConstants {
	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
/**
 * CodeSnippetMessageSend constructor comment.
 */
public CodeSnippetMessageSend(EvaluationContext evaluationContext) {
	this.evaluationContext = evaluationContext;
}
/**
 * MessageSend code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(
	BlockScope currentScope,
	CodeStream codeStream,
	boolean valueRequired) {

	int pc = codeStream.position;

	if (binding.canBeSeenBy(receiverType, this, currentScope)) {
		// generate receiver/enclosing instance access
		boolean isStatic = binding.isStatic();
		// outer access ?
		if (!isStatic && ((bits & DepthMASK) != 0)) {
			// outer method can be reached through emulation
			Object[] path =
				currentScope.getExactEmulationPath(
					currentScope.enclosingSourceType().enclosingTypeAt(
						(bits & DepthMASK) >> DepthSHIFT));
			if (path == null) {
				// emulation was not possible (should not happen per construction)
				currentScope.problemReporter().needImplementation();
			} else {
				codeStream.generateOuterAccess(path, this, currentScope);
			}
		} else {
			receiver.generateCode(currentScope, codeStream, !isStatic);
		}
		// generate arguments
		if (arguments != null) {
			for (int i = 0, max = arguments.length; i < max; i++) {
				arguments[i].generateCode(currentScope, codeStream, true);
			}
		}
		// actual message invocation
		if (isStatic) {
			codeStream.invokestatic(binding);
		} else {
			if (receiver.isSuper()) {
				codeStream.invokespecial(binding);
			} else {
				if (binding.declaringClass.isInterface()) {
					codeStream.invokeinterface(binding);
				} else {
					codeStream.invokevirtual(binding);
				}
			}
		}
	} else {
		((CodeSnippetCodeStream) codeStream).generateEmulationForMethod(currentScope, binding);
		// generate receiver/enclosing instance access
		boolean isStatic = binding.isStatic();
		// outer access ?
		if (!isStatic && ((bits & DepthMASK) != 0)) {
			// not supported yet
			currentScope.problemReporter().needImplementation();
		} else {
			receiver.generateCode(currentScope, codeStream, !isStatic);
		}
		if (isStatic) {
			// we need an object on the stack which is ignored for the method invocation
			codeStream.aconst_null();
		}
		// generate arguments
		if (arguments != null) {
			int argsLength = arguments.length;
			codeStream.generateInlinedValue(argsLength);
			codeStream.newArray(currentScope, new ArrayBinding(currentScope.getType(TypeBinding.JAVA_LANG_OBJECT), 1));
			codeStream.dup();
			for (int i = 0; i < argsLength; i++) {
				codeStream.generateInlinedValue(i);
				arguments[i].generateCode(currentScope, codeStream, true);
				TypeBinding parameterBinding = binding.parameters[i];
				if (parameterBinding.isBaseType() && parameterBinding != NullBinding) {
					((CodeSnippetCodeStream)codeStream).generateObjectWrapperForType(binding.parameters[i]);
				}
				codeStream.aastore();
				if (i < argsLength - 1) {
					codeStream.dup();
				}	
			}
		} else {
			codeStream.generateInlinedValue(0);
			codeStream.newArray(currentScope, new ArrayBinding(currentScope.getType(TypeBinding.JAVA_LANG_OBJECT), 1));			
		}
		((CodeSnippetCodeStream) codeStream).invokeJavaLangReflectMethodInvoke();

		// convert the return value to the appropriate type for primitive types
		if (binding.returnType.isBaseType()) {
			int typeID = binding.returnType.id;
			if (typeID == T_void) {
				// remove the null from the stack
				codeStream.pop();
			}
			((CodeSnippetCodeStream) codeStream).checkcast(typeID);
			((CodeSnippetCodeStream) codeStream).getBaseTypeValue(typeID);
		} else {
			codeStream.checkcast(binding.returnType);
		}
	}
	// operation on the returned value
	if (valueRequired) {
		// implicit conversion if necessary
		codeStream.generateImplicitConversion(implicitConversion);
	} else {
		// pop return value if any
		switch (binding.returnType.id) {
			case T_long :
			case T_double :
				codeStream.pop2();
				break;
			case T_void :
				break;
			default :
				codeStream.pop();
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {
}
public void manageSyntheticAccessIfNecessary(BlockScope currentScope) {

	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from 1.4 on, method's declaring class is touched if any different from receiver type
	// and not from Object or implicit static method call.	
	if (binding.declaringClass != this.qualifyingType
		&& !this.qualifyingType.isArrayType()
		&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
				&& (receiver != ThisReference.ThisImplicit || !binding.isStatic())
				&& binding.declaringClass.id != T_Object) // no change for Object methods
			|| !binding.declaringClass.canBeSeenBy(currentScope))) {
		codegenBinding = currentScope.enclosingSourceType().getUpdatedMethodBinding(binding, (ReferenceBinding) this.qualifyingType);
	}	
}
public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature return type
	// Base type promotion

	constant = NotAConstant;
	this.qualifyingType = this.receiverType = receiver.resolveType(scope); 
	// will check for null after args are resolved
	TypeBinding[] argumentTypes = NoParameters;
	if (arguments != null) {
		boolean argHasError = false; // typeChecks all arguments 
		int length = arguments.length;
		argumentTypes = new TypeBinding[length];
		for (int i = 0; i < length; i++)
			if ((argumentTypes[i] = arguments[i].resolveType(scope)) == null)
				argHasError = true;
		if (argHasError)
			return null;
	}
	if (receiverType == null) 
		return null;

	// base type cannot receive any message
	if (receiverType.isBaseType()) {
		scope.problemReporter().errorNoMethodFor(this, receiverType, argumentTypes);
		return null;
	}

	binding = 
		receiver == ThisReference.ThisImplicit
			? scope.getImplicitMethod(selector, argumentTypes, this)
			: scope.getMethod(receiverType, selector, argumentTypes, this); 
	if (!binding.isValidBinding()) {
		if (binding instanceof ProblemMethodBinding
			&& ((ProblemMethodBinding) binding).problemId() == NotVisible) {
			if (this.evaluationContext.declaringTypeName != null) {
				delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
				if (delegateThis == null){ ; // if not found then internal error, field should have been found
					constant = NotAConstant;
					scope.problemReporter().invalidMethod(this, binding);
					return null;
				}
			} else {
				constant = NotAConstant;
				scope.problemReporter().invalidMethod(this, binding);
				return null;
			}
			CodeSnippetScope localScope = new CodeSnippetScope(scope);			
			MethodBinding privateBinding = 
				receiver instanceof CodeSnippetThisReference && ((CodeSnippetThisReference) receiver).isImplicit
					? localScope.getImplicitMethod((ReferenceBinding)delegateThis.type, selector, argumentTypes, this)
					: localScope.getMethod(delegateThis.type, selector, argumentTypes, this); 
			if (!privateBinding.isValidBinding()) {
				if (binding.declaringClass == null) {
					if (receiverType instanceof ReferenceBinding) {
						binding.declaringClass = (ReferenceBinding) receiverType;
					} else { // really bad error ....
						scope.problemReporter().errorNoMethodFor(this, receiverType, argumentTypes);
						return null;
					}
				}
				scope.problemReporter().invalidMethod(this, binding);
				return null;
			} else {
				binding = privateBinding;
			}
		} else {
			if (binding.declaringClass == null) {
				if (receiverType instanceof ReferenceBinding) {
					binding.declaringClass = (ReferenceBinding) receiverType;
				} else { // really bad error ....
					scope.problemReporter().errorNoMethodFor(this, receiverType, argumentTypes);
					return null;
				}
			}
			scope.problemReporter().invalidMethod(this, binding);
			return null;
		}
	}
	if (!binding.isStatic()) {
		// the "receiver" must not be a type, i.e. a NameReference that the TC has bound to a Type
		if (receiver instanceof NameReference) {
			if ((((NameReference) receiver).bits & BindingIds.TYPE) != 0) {
				scope.problemReporter().mustUseAStaticMethod(this, binding);
				return null;
			}
		}
	}
	if (arguments != null)
		for (int i = 0; i < arguments.length; i++)
			arguments[i].implicitWidening(binding.parameters[i], argumentTypes[i]);

	//-------message send that are known to fail at compile time-----------
	if (binding.isAbstract()) {
		if (receiver.isSuper()) {
			scope.problemReporter().cannotDireclyInvokeAbstractMethod(this, binding);
			return null;
		}
		// abstract private methods cannot occur nor abstract static............
	}
	if (isMethodUseDeprecated(binding, scope))
		scope.problemReporter().deprecatedMethod(binding, this);

	return binding.returnType;
}
}
