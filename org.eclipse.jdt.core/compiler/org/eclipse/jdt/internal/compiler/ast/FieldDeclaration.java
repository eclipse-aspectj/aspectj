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

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class FieldDeclaration extends AbstractVariableDeclaration {
	public FieldBinding binding;
	boolean hasBeenResolved = false;

	//allows to retrieve both the "type" part of the declaration (part1)
	//and also the part that decribe the name and the init and optionally
	//some other dimension ! .... 
	//public int[] a, b[] = X, c ;
	//for b that would give for 
	// - part1 : public int[]
	// - part2 : b[] = X,

	public int endPart1Position;
	public int endPart2Position;

	public FieldDeclaration() {
	}

	public FieldDeclaration(
		Expression initialization,
		char[] name,
		int sourceStart,
		int sourceEnd) {

		this.initialization = initialization;
		this.name = name;

		//due to some declaration like 
		// int x, y = 3, z , x ;
		//the sourceStart and the sourceEnd is ONLY on  the name
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public FlowInfo analyseCode(
		MethodScope initializationScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// cannot define static non-constant field inside nested class
		if (binding != null
			&& binding.isValidBinding()
			&& binding.isStatic()
			&& binding.constant == NotAConstant
			&& binding.declaringClass.isNestedType()
			&& binding.declaringClass.isClass()
			&& !binding.declaringClass.isStatic()) {
			initializationScope.problemReporter().unexpectedStaticModifierForField(
				(SourceTypeBinding) binding.declaringClass,
				this);
		}

		if (initialization != null) {
			flowInfo =
				initialization
					.analyseCode(initializationScope, flowContext, flowInfo)
					.unconditionalInits();
			flowInfo.markAsDefinitelyAssigned(binding);
		} else {
			flowInfo.markAsDefinitelyNotAssigned(binding);
			// clear the bit in case it was already set (from enclosing info)
		}
		return flowInfo;
	}

	/**
	 * Code generation for a field declaration:
	 *	i.e.&nbsp;normal assignment to a field 
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		// do not generate initialization code if final and static (constant is then
		// recorded inside the field itself).
		int pc = codeStream.position;
		boolean isStatic;
		if (initialization != null
			&& !((isStatic = binding.isStatic()) && binding.constant != NotAConstant)) {
			// non-static field, need receiver
			if (!isStatic)
				codeStream.aload_0();
			// generate initialization value
			initialization.generateCode(currentScope, codeStream, true);
			// store into field
			if (isStatic) {
				codeStream.putstatic(binding);
			} else {
				codeStream.putfield(binding);
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding getTypeBinding(Scope scope) {

		return type.getTypeBinding(scope);
	}

	public boolean isField() {

		return true;
	}

	public boolean isStatic() {

		if (binding != null)
			return binding.isStatic();
		return (modifiers & AccStatic) != 0;
	}

	public String name() {

		return String.valueOf(name);
	}

	public void resolve(MethodScope initializationScope) {

		// the two <constant = Constant.NotAConstant> could be regrouped into
		// a single line but it is clearer to have two lines while the reason of their
		// existence is not at all the same. See comment for the second one.

		//--------------------------------------------------------
		if (!this.hasBeenResolved && binding != null && this.binding.isValidBinding()) {

			this.hasBeenResolved = true;

			if (isTypeUseDeprecated(this.binding.type, initializationScope))
				initializationScope.problemReporter().deprecatedType(this.binding.type, this.type);

			this.type.binding = this.binding.type; // update binding for type reference

			// the resolution of the initialization hasn't been done
			if (this.initialization == null) {
				this.binding.constant = Constant.NotAConstant;
			} else {
				int previous = initializationScope.fieldDeclarationIndex;
				try {
					initializationScope.fieldDeclarationIndex = this.binding.id;

					// break dead-lock cycles by forcing constant to NotAConstant
					this.binding.constant = Constant.NotAConstant;
					
					TypeBinding typeBinding = this.binding.type;
					TypeBinding initializationTypeBinding;
					
					if (initialization instanceof ArrayInitializer) {

						if ((initializationTypeBinding = this.initialization.resolveTypeExpecting(initializationScope, typeBinding)) 	!= null) {
							((ArrayInitializer) this.initialization).binding = (ArrayBinding) initializationTypeBinding;
							this.initialization.implicitWidening(typeBinding, initializationTypeBinding);
						}
					} else if ((initializationTypeBinding = initialization.resolveType(initializationScope)) != null) {

						if (this.initialization.isConstantValueOfTypeAssignableToType(initializationTypeBinding, typeBinding)
							|| (typeBinding.isBaseType() && BaseTypeBinding.isWidening(typeBinding.id, initializationTypeBinding.id))) {

							this.initialization.implicitWidening(typeBinding, initializationTypeBinding);

						}	else if (initializationScope.areTypesCompatible(initializationTypeBinding, typeBinding)) {
							this.initialization.implicitWidening(typeBinding, initializationTypeBinding);

						} else {
							initializationScope.problemReporter().typeMismatchError(initializationTypeBinding, typeBinding, this);
						}
						if (this.binding.isFinal()){ // cast from constant actual type to variable type
							this.binding.constant =
								this.initialization.constant.castTo(
									(this.binding.type.id << 4) + this.initialization.constant.typeID());
						}
					} else {
						this.binding.constant = NotAConstant;
					}
				} finally {
					initializationScope.fieldDeclarationIndex = previous;
					if (this.binding.constant == null)
						this.binding.constant = Constant.NotAConstant;
				}
			}
		}
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, MethodScope scope) {

		if (visitor.visit(this, scope)) {
			type.traverse(visitor, scope);
			if (initialization != null)
				initialization.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}