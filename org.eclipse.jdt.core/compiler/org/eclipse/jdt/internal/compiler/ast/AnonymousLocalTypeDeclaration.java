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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;

public class AnonymousLocalTypeDeclaration extends LocalTypeDeclaration {

	public static final char[] ANONYMOUS_EMPTY_NAME = new char[] {};
	public QualifiedAllocationExpression allocation;

	public AnonymousLocalTypeDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
		modifiers = AccDefault;
		name = ANONYMOUS_EMPTY_NAME;
	} 
	
	// use a default name in order to th name lookup 
	// to operate juat like a regular type (which has a name)
	//without checking systematically if the naem is null .... 
	public MethodBinding createsInternalConstructorWithBinding(MethodBinding inheritedConstructorBinding) {

		//Add to method'set, the default constuctor that just recall the
		//super constructor with the same arguments
		String baseName = "$anonymous"; //$NON-NLS-1$
		TypeBinding[] argumentTypes = inheritedConstructorBinding.parameters;
		int argumentsLength = argumentTypes.length;
		//the constructor
		ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationResult);
		cd.selector = new char[] { 'x' }; //no maining
		cd.sourceStart = sourceStart;
		cd.sourceEnd = sourceEnd;
		cd.modifiers = modifiers & AccVisibilityMASK;
		cd.isDefaultConstructor = true;

		if (argumentsLength > 0) {
			Argument[] arguments = (cd.arguments = new Argument[argumentsLength]);
			for (int i = argumentsLength; --i >= 0;) {
				arguments[i] = new Argument((baseName + i).toCharArray(), 0L, null /*type ref*/, AccDefault);
			}
		}

		//the super call inside the constructor
		cd.constructorCall =
			new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);
		cd.constructorCall.sourceStart = sourceStart;
		cd.constructorCall.sourceEnd = sourceEnd;

		if (argumentsLength > 0) {
			Expression[] args;
			args = cd.constructorCall.arguments = new Expression[argumentsLength];
			for (int i = argumentsLength; --i >= 0;) {
				args[i] = new SingleNameReference((baseName + i).toCharArray(), 0L);
			}
		}

		//adding the constructor in the methods list
		if (methods == null) {
			methods = new AbstractMethodDeclaration[] { cd };
		} else {
			AbstractMethodDeclaration[] newMethods;
			System.arraycopy(
				methods,
				0,
				newMethods = new AbstractMethodDeclaration[methods.length + 1],
				1,
				methods.length);
			newMethods[0] = cd;
			methods = newMethods;
		}

		//============BINDING UPDATE==========================
		cd.binding = new MethodBinding(
				cd.modifiers, //methodDeclaration
				argumentsLength == 0 ? NoParameters : argumentTypes, //arguments bindings
				inheritedConstructorBinding.thrownExceptions, //exceptions
				binding); //declaringClass
				
		cd.scope = new MethodScope(scope, this, true);
		cd.bindArguments();
		cd.constructorCall.resolve(cd.scope);

		if (binding.methods == null) {
			binding.methods = new MethodBinding[] { cd.binding };
		} else {
			MethodBinding[] newMethods;
			System.arraycopy(
				binding.methods,
				0,
				newMethods = new MethodBinding[binding.methods.length + 1],
				1,
				binding.methods.length);
			newMethods[0] = cd.binding;
			binding.methods = newMethods;
		}
		//===================================================

		return cd.binding;

	}
	public void resolve(BlockScope scope) {

		// scope and binding are provided in updateBindingSuperclass 
		resolve();
		updateMaxFieldCount();
	}

	public String toString(int tab) {

		return toStringBody(tab);
	}

	/**
	 *	Iteration for a local anonymous innertype
	 *
	 */
	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, blockScope)) {

				int fieldsLength;
				int methodsLength;
				int memberTypesLength;

				// <superclass> is bound to the actual type from the allocation expression
				// therefore it has already been iterated at this point.

				if (memberTypes != null) {
					memberTypesLength = memberTypes.length;
					for (int i = 0; i < memberTypesLength; i++)
						memberTypes[i].traverse(visitor, scope);
				}
				if (fields != null) {
					fieldsLength = fields.length;
					for (int i = 0; i < fieldsLength; i++) {
						FieldDeclaration field;
						if ((field = fields[i]).isStatic()) {
							// local type cannot have static fields
						} else {
							field.traverse(visitor, initializerScope);
						}
					}
				}
				if (methods != null) {
					methodsLength = methods.length;
					for (int i = 0; i < methodsLength; i++)
						methods[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, blockScope);
		} catch (AbortType e) {
		}
	}
}