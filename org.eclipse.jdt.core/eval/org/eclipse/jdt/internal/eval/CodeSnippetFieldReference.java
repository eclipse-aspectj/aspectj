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
 
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class CodeSnippetFieldReference extends FieldReference implements ProblemReasons, EvaluationConstants {

	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
/**
 * CodeSnippetFieldReference constructor comment.
 * @param source char[]
 * @param pos long
 */
public CodeSnippetFieldReference(char[] source, long pos, EvaluationContext evaluationContext) {
	super(source, pos);
	this.evaluationContext = evaluationContext;
}
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {

	if (this.codegenBinding.canBeSeenBy(receiverType, this, currentScope)) {
		receiver.generateCode(currentScope, codeStream, !this.codegenBinding.isStatic());
		assignment.expression.generateCode(currentScope, codeStream, true);
		fieldStore(codeStream, this.codegenBinding, null, valueRequired);
	} else {
		((CodeSnippetCodeStream) codeStream).generateEmulationForField(this.codegenBinding);
		receiver.generateCode(currentScope, codeStream, !this.codegenBinding.isStatic());
		if (this.codegenBinding.isStatic()) { // need a receiver?
			codeStream.aconst_null();
		}
		assignment.expression.generateCode(currentScope, codeStream, true);
		if (valueRequired) {
			if ((this.codegenBinding.type == LongBinding) || (this.codegenBinding.type == DoubleBinding)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		((CodeSnippetCodeStream) codeStream).generateEmulatedWriteAccessForField(this.codegenBinding);
	}
	if (valueRequired){
		codeStream.generateImplicitConversion(assignment.implicitConversion);
	}
}
/**
 * Field reference code generation
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (constant != NotAConstant) {
		if (valueRequired) {
			codeStream.generateConstant(constant, implicitConversion);
		}
	} else {
		boolean isStatic = this.codegenBinding.isStatic();
		receiver.generateCode(currentScope, codeStream, valueRequired && (!isStatic) && (this.codegenBinding.constant == NotAConstant));
		if (valueRequired) {
			if (this.codegenBinding.constant == NotAConstant) {
				if (this.codegenBinding.declaringClass == null) { // array length
					codeStream.arraylength();
				} else {
					if (this.codegenBinding.canBeSeenBy(receiverType, this, currentScope)) {
						if (isStatic) {
							codeStream.getstatic(this.codegenBinding);
						} else {
							codeStream.getfield(this.codegenBinding);
						}
					} else {
						if (isStatic) {
							// we need a null on the stack to use the reflect emulation
							codeStream.aconst_null();
						}
						((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(this.codegenBinding);
					}
				}
				codeStream.generateImplicitConversion(implicitConversion);
			} else {
				codeStream.generateConstant(this.codegenBinding.constant, implicitConversion);
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	
	boolean isStatic;
	if (this.codegenBinding.canBeSeenBy(receiverType, this, currentScope)) {
		receiver.generateCode(currentScope, codeStream, !(isStatic = this.codegenBinding.isStatic()));
		if (isStatic) {
			codeStream.getstatic(this.codegenBinding);
		} else {
			codeStream.dup();
			codeStream.getfield(this.codegenBinding);
		}
		int operationTypeID;
		if ((operationTypeID = implicitConversion >> 4) == T_String) {
			codeStream.generateStringAppend(currentScope, null, expression);
		} else {
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One){ // prefix operation
				codeStream.generateConstant(expression.constant, implicitConversion);			
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}		
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		fieldStore(codeStream, this.codegenBinding, null, valueRequired);
	} else {
		receiver.generateCode(currentScope, codeStream, !(isStatic = this.codegenBinding.isStatic()));
		if (isStatic) {
			// used to store the value
			((CodeSnippetCodeStream) codeStream).generateEmulationForField(this.codegenBinding);
			codeStream.aconst_null();

			// used to retrieve the actual value
			codeStream.aconst_null();
			((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(this.codegenBinding);
		} else {
			// used to store the value
			((CodeSnippetCodeStream) codeStream).generateEmulationForField(binding);
			receiver.generateCode(currentScope, codeStream, !(isStatic = this.codegenBinding.isStatic()));

			// used to retrieve the actual value
			codeStream.dup();
			((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(this.codegenBinding);
							
		}
		int operationTypeID;
		if ((operationTypeID = implicitConversion >> 4) == T_String) {
			codeStream.generateStringAppend(currentScope, null, expression);
		} else {
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One){ // prefix operation
				codeStream.generateConstant(expression.constant, implicitConversion);			
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}		
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		// current stack is:
		// field receiver value
		if (valueRequired) {
			if ((this.codegenBinding.type == LongBinding) || (this.codegenBinding.type == DoubleBinding)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		// current stack is:
		// value field receiver value				
		((CodeSnippetCodeStream) codeStream).generateEmulatedWriteAccessForField(this.codegenBinding);
	}
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	boolean isStatic;
	if (this.codegenBinding.canBeSeenBy(receiverType, this, currentScope)) {
		receiver.generateCode(currentScope, codeStream, !(isStatic = this.codegenBinding.isStatic()));
		if (isStatic) {
			codeStream.getstatic(this.codegenBinding);
		} else {
			codeStream.dup();
			codeStream.getfield(this.codegenBinding);
		}
		if (valueRequired) {
			if (isStatic) {
				if ((this.codegenBinding.type == LongBinding) || (this.codegenBinding.type == DoubleBinding)) {
					codeStream.dup2();
				} else {
					codeStream.dup();
				}
			} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
				if ((this.codegenBinding.type == LongBinding) || (this.codegenBinding.type == DoubleBinding)) {
					codeStream.dup2_x1();
				} else {
					codeStream.dup_x1();
				}
			}
		}
		codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
		codeStream.sendOperator(postIncrement.operator, this.codegenBinding.type.id);
		codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
		fieldStore(codeStream, this.codegenBinding, null, false);
	} else {
		receiver.generateCode(currentScope, codeStream, !(isStatic = this.codegenBinding.isStatic()));
		if (this.codegenBinding.isStatic()) {
			codeStream.aconst_null();
		}
		// the actual stack is: receiver
		codeStream.dup();
		// the actual stack is: receiver receiver
		((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(this.codegenBinding);
		// the actual stack is: receiver value
		// receiver value
		// value receiver value 							dup_x1 or dup2_x1 if value required
		// value value receiver value						dup_x1 or dup2_x1
		// value value receiver								pop or pop2
		// value value receiver field						generateEmulationForField
		// value value field receiver 						swap
		// value field receiver value field receiver 		dup2_x1 or dup2_x2
		// value field receiver value 				 		pop2
		// value field receiver newvalue 				 	generate constant + op
		// value 											store
		if (valueRequired) {
			if ((this.codegenBinding.type == LongBinding) || (this.codegenBinding.type == DoubleBinding)) {
				codeStream.dup2_x1();
			} else {
				codeStream.dup_x1();
			}
		}
		if ((this.codegenBinding.type == LongBinding) || (this.codegenBinding.type == DoubleBinding)) {
			codeStream.dup2_x1();
			codeStream.pop2();
		} else {
			codeStream.dup_x1();
			codeStream.pop();
		}
		((CodeSnippetCodeStream) codeStream).generateEmulationForField(this.codegenBinding);
		codeStream.swap();
		
		if ((this.codegenBinding.type == LongBinding) || (this.codegenBinding.type == DoubleBinding)) {
			codeStream.dup2_x2();
		} else {
			codeStream.dup2_x1();
		}
		codeStream.pop2();

		codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
		codeStream.sendOperator(postIncrement.operator, this.codegenBinding.type.id);
		codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
		((CodeSnippetCodeStream) codeStream).generateEmulatedWriteAccessForField(this.codegenBinding);
	}
}
/*
 * No need to emulate access to protected fields since not implicitly accessed
 */
public void manageSyntheticReadAccessIfNecessary(BlockScope currentScope){
	// The private access will be managed through the code generation

	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
	if (binding.declaringClass != this.receiverType
		&& !this.receiverType.isArrayType()
		&& binding.declaringClass != null // array.length
		&& binding.constant == NotAConstant
		&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
				&& binding.declaringClass.id != T_Object) //no change for Object fields (in case there was)
			|| !binding.declaringClass.canBeSeenBy(currentScope))){
			this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(binding, (ReferenceBinding) this.receiverType);
	}
}
/*
 * No need to emulate access to protected fields since not implicitly accessed
 */
public void manageSyntheticWriteAccessIfNecessary(BlockScope currentScope){
	// The private access will be managed through the code generation

	// if the binding declaring class is not visible, need special action
	// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
	// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
	if (binding.declaringClass != this.receiverType
		&& !this.receiverType.isArrayType()
		&& binding.declaringClass != null // array.length
		&& binding.constant == NotAConstant
		&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
				&& binding.declaringClass.id != T_Object) //no change for Object fields (in case there was)
			|| !binding.declaringClass.canBeSeenBy(currentScope))){
			this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(binding, (ReferenceBinding) this.receiverType);
	}
}
public TypeBinding resolveType(BlockScope scope) {
	// Answer the signature type of the field.
	// constants are propaged when the field is final
	// and initialized with a (compile time) constant 

	// regular receiver reference 
	receiverType = receiver.resolveType(scope);
	if (receiverType == null){
		constant = NotAConstant;
		return null;
	}
	// the case receiverType.isArrayType and token = 'length' is handled by the scope API
	this.codegenBinding = this.binding = scope.getField(receiverType, token, this);
	FieldBinding firstAttempt = binding;
	boolean isNotVisible = false;
	if (!binding.isValidBinding()) {
		if (binding instanceof ProblemFieldBinding
			&& ((ProblemFieldBinding) binding).problemId() == NotVisible) {
				isNotVisible = true;
				if (this.evaluationContext.declaringTypeName != null) {
					delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
					if (delegateThis == null){ ; // if not found then internal error, field should have been found
						constant = NotAConstant;
						scope.problemReporter().invalidField(this, receiverType);
						return null;
					}
				} else {
					constant = NotAConstant;
					scope.problemReporter().invalidField(this, receiverType);
					return null;
				}
			CodeSnippetScope localScope = new CodeSnippetScope(scope);
			this.codegenBinding = this.binding = localScope.getFieldForCodeSnippet(delegateThis.type, token, this);
		}
	}

	if (!binding.isValidBinding()) {
		constant = NotAConstant;
		if (isNotVisible) {
			this.codegenBinding = this.binding = firstAttempt;
		}
		scope.problemReporter().invalidField(this, receiverType);
		return null;
	}

	if (isFieldUseDeprecated(binding, scope))
		scope.problemReporter().deprecatedField(binding, this);

	// check for this.x in static is done in the resolution of the receiver
	constant = FieldReference.getConstantFor(binding, receiver == ThisReference.ThisImplicit, this, scope, 0);
	if (!receiver.isThis())
		constant = NotAConstant;

	return binding.type;
}
}
