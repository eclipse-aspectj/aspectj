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
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

public class CodeSnippetQualifiedNameReference extends QualifiedNameReference implements EvaluationConstants, InvocationSite, ProblemReasons {

	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
/**
 * CodeSnippetQualifiedNameReference constructor comment.
 * @param sources char[][]
 * @param sourceStart int
 * @param sourceEnd int
 */
public CodeSnippetQualifiedNameReference(char[][] sources, int sourceStart, int sourceEnd, EvaluationContext evaluationContext) {
	super(sources, sourceStart, sourceEnd);
	this.evaluationContext = evaluationContext;	
}
/**
 * Check and/or redirect the field access to the delegate receiver if any
 */
public TypeBinding checkFieldAccess(BlockScope scope) {
	// check for forward references
	bits &= ~RestrictiveFlagMASK; // clear bits
	bits |= FIELD;
	return getOtherFieldBindings(scope);
}
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
	generateReadSequence(currentScope, codeStream, true);
	if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
		// the last field access is a write access
		assignment.expression.generateCode(currentScope, codeStream, true);
		fieldStore(codeStream, lastFieldBinding, null, valueRequired);
	} else {
		((CodeSnippetCodeStream) codeStream).generateEmulationForField(lastFieldBinding);
		codeStream.swap();
		assignment.expression.generateCode(currentScope, codeStream, true);
		if (valueRequired) {
			if ((lastFieldBinding.type == LongBinding) || (lastFieldBinding.type == DoubleBinding)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		((CodeSnippetCodeStream) codeStream).generateEmulatedWriteAccessForField(lastFieldBinding);	
	}
	if (valueRequired) {
		codeStream.generateImplicitConversion(assignment.implicitConversion);
	}
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (constant != NotAConstant) {
		if (valueRequired) {
			codeStream.generateConstant(constant, implicitConversion);
		}
	} else {
		generateReadSequence(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			if (lastFieldBinding.declaringClass == null) { // array length
				codeStream.arraylength();
				codeStream.generateImplicitConversion(implicitConversion);
			} else {
				if (lastFieldBinding.constant != NotAConstant) {
					// inline the last field constant
					codeStream.generateConstant(lastFieldBinding.constant, implicitConversion);
				} else {	
					if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
						if (lastFieldBinding.isStatic()) {
							codeStream.getstatic(lastFieldBinding);
						} else {
							codeStream.getfield(lastFieldBinding);
						}
					} else {
						((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(lastFieldBinding);
					}	
					codeStream.generateImplicitConversion(implicitConversion);
				}
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	
	generateReadSequence(currentScope, codeStream, true);
	if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
		if (lastFieldBinding.isStatic()){
			codeStream.getstatic(lastFieldBinding);
		} else {
			codeStream.dup();
			codeStream.getfield(lastFieldBinding);
		}
		// the last field access is a write access
		// perform the actual compound operation
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
		// actual assignment
		fieldStore(codeStream, lastFieldBinding, null, valueRequired);
	} else {
		if (lastFieldBinding.isStatic()){
			((CodeSnippetCodeStream) codeStream).generateEmulationForField(lastFieldBinding);
			codeStream.swap();
			codeStream.aconst_null();
			codeStream.swap();

			((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(lastFieldBinding);
		} else {
			((CodeSnippetCodeStream) codeStream).generateEmulationForField(lastFieldBinding);
			codeStream.swap();
			codeStream.dup();

			((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(lastFieldBinding);
		}
		// the last field access is a write access
		// perform the actual compound operation
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
		// actual assignment

		// current stack is:
		// field receiver value
		if (valueRequired) {
			if ((lastFieldBinding.type == LongBinding) || (lastFieldBinding.type == DoubleBinding)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		// current stack is:
		// value field receiver value				
		((CodeSnippetCodeStream) codeStream).generateEmulatedWriteAccessForField(lastFieldBinding);
	}
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	generateReadSequence(currentScope, codeStream, true);

	if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
		if (lastFieldBinding.isStatic()){
			codeStream.getstatic(lastFieldBinding);
		} else {
			codeStream.dup();
			codeStream.getfield(lastFieldBinding);
		}	
		// duplicate the old field value
		if (valueRequired) {
			if (lastFieldBinding.isStatic()) {
				if ((lastFieldBinding.type == LongBinding) || (lastFieldBinding.type == DoubleBinding)) {
					codeStream.dup2();
				} else {
					codeStream.dup();
				}
			} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
				if ((lastFieldBinding.type == LongBinding) || (lastFieldBinding.type == DoubleBinding)) {
					codeStream.dup2_x1();
				} else {
					codeStream.dup_x1();
				}
			}
		}
		codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
		codeStream.sendOperator(postIncrement.operator, lastFieldBinding.type.id);
		codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
		
		fieldStore(codeStream, lastFieldBinding, null, false);
	} else {
		((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(lastFieldBinding);
		if (valueRequired) {
			if ((lastFieldBinding.type == LongBinding) || (lastFieldBinding.type == DoubleBinding)) {
				codeStream.dup2();
			} else {
				codeStream.dup();
			}
		}
		((CodeSnippetCodeStream) codeStream).generateEmulationForField(lastFieldBinding);
		if ((lastFieldBinding.type == LongBinding) || (lastFieldBinding.type == DoubleBinding)) {
			codeStream.dup_x2();
			codeStream.pop();
			if (lastFieldBinding.isStatic()) {
				codeStream.aconst_null();
			} else {
				generateReadSequence(currentScope, codeStream, true);
			}
			codeStream.dup_x2();
			codeStream.pop();					
		} else {
			codeStream.dup_x1();
			codeStream.pop();
			if (lastFieldBinding.isStatic()) {
				codeStream.aconst_null();
			} else {
				generateReadSequence(currentScope, codeStream, true);
			}
			codeStream.dup_x1();
			codeStream.pop();					
		}
		codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
		codeStream.sendOperator(postIncrement.operator, lastFieldBinding.type.id);
		codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
		((CodeSnippetCodeStream) codeStream).generateEmulatedWriteAccessForField(lastFieldBinding);
	}
}
/*
 * Generate code for all bindings (local and fields) excluding the last one, which may then be generated code
 * for a read or write access.
 */
public void generateReadSequence(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	// determine the rank until which we now we do not need any actual value for the field access
	int otherBindingsCount = this.otherCodegenBindings == null ? 0 : otherCodegenBindings.length;
	int indexOfFirstValueRequired;
	if (valueRequired) {
		indexOfFirstValueRequired = otherBindingsCount;
		while (indexOfFirstValueRequired > 0) {
			FieldBinding otherBinding = this.otherCodegenBindings[indexOfFirstValueRequired - 1];
			if (otherBinding.isStatic() || otherBinding.constant != NotAConstant)
				break; // no longer need any value before this point
			indexOfFirstValueRequired--;
		}
	} else {
		indexOfFirstValueRequired = otherBindingsCount + 1;
	}
	if (indexOfFirstValueRequired == 0) {
		switch (bits & RestrictiveFlagMASK) {
			case FIELD :
				lastFieldBinding = (FieldBinding) binding;
				// if first field is actually constant, we can inline it
				if (lastFieldBinding.constant != NotAConstant) {
					codeStream.generateConstant(lastFieldBinding.constant, 0); // no implicit conversion
					lastFieldBinding = null; // will not generate it again
					break;
				}
				if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
					if (!lastFieldBinding.isStatic()) {
						if ((bits & DepthMASK) != 0) {
							Object[] emulationPath = currentScope.getExactEmulationPath(currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT));
							if (emulationPath == null) {
								// internal error, per construction we should have found it
								currentScope.problemReporter().needImplementation();
							} else {
								codeStream.generateOuterAccess(emulationPath, this, currentScope);
							}
						} else {
							generateReceiver(codeStream);
						}
					}
				} else {
					if (!lastFieldBinding.isStatic()) {
						if ((bits & DepthMASK) != 0) {
							// internal error, per construction we should have found it
							// not yet supported
							currentScope.problemReporter().needImplementation();
						} else {
							generateReceiver(codeStream);
						}
					} else {
						codeStream.aconst_null();
					}
				}
				break;
			case LOCAL : // reading the first local variable
				lastFieldBinding = null;
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;

				// regular local variable read
				if (localBinding.constant != NotAConstant) {
					codeStream.generateConstant(localBinding.constant, 0); // no implicit conversion
				} else {
					// outer local?
					if ((bits & DepthMASK) != 0) {
						// outer local can be reached either through a synthetic arg or a synthetic field
						VariableBinding[] path = currentScope.getEmulationPath(localBinding);
						if (path == null) {
							// emulation was not possible (should not happen per construction)
							currentScope.problemReporter().needImplementation();
						} else {
							codeStream.generateOuterAccess(path, this, currentScope);
						}
					} else {
						codeStream.load(localBinding);
					}
				}
		}
	} else {
		lastFieldBinding = null;
	}
	// all intermediate field accesses are read accesses
	// only the last field binding is a write access
	if (otherBindings != null) {
		int start = indexOfFirstValueRequired == 0 ? 0 : indexOfFirstValueRequired - 1;
		for (int i = start; i < otherBindingsCount; i++) {
			if (lastFieldBinding != null) {
				if (lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
					if (lastFieldBinding.isStatic())
						codeStream.getstatic(lastFieldBinding);
					else
						codeStream.getfield(lastFieldBinding);
				} else {
					((CodeSnippetCodeStream) codeStream).generateEmulatedReadAccessForField(lastFieldBinding);
				}
			}

			lastFieldBinding = this.otherCodegenBindings[i];
			if (lastFieldBinding != null && !lastFieldBinding.canBeSeenBy(getReceiverType(currentScope), this, currentScope)) {
				if (lastFieldBinding.isStatic()) {
					codeStream.aconst_null();
				}
			}
		}
	}
}
public void generateReceiver(CodeStream codeStream) {
	codeStream.aload_0();
	if (delegateThis != null) codeStream.getfield(delegateThis); // delegated field access
}
public TypeBinding getOtherFieldBindings(BlockScope scope) {
	// At this point restrictiveFlag may ONLY have two potential value : FIELD LOCAL (i.e cast <<(VariableBinding) binding>> is valid)

	if ((bits & FIELD) != 0) {
		if (!((FieldBinding) binding).isStatic()) { //must check for the static status....
			if (indexOfFirstFieldBinding == 1) {
				//the field is the first token of the qualified reference....
				if (scope.methodScope().isStatic) {
					scope.problemReporter().staticFieldAccessToNonStaticVariable(this, (FieldBinding) binding);
					return null;
				}
			} else { //accessing to a field using a type as "receiver" is allowed only with static field	
				scope.problemReporter().staticFieldAccessToNonStaticVariable(this, (FieldBinding) binding);
				return null;
			}
		}
		if (isFieldUseDeprecated((FieldBinding) binding, scope))
			scope.problemReporter().deprecatedField((FieldBinding) binding, this);
	}

	TypeBinding type = ((VariableBinding) binding).type;
	int index = indexOfFirstFieldBinding;
	int length = tokens.length;
	if (index == length) { //	restrictiveFlag == FIELD
		constant = FieldReference.getConstantFor((FieldBinding) binding, false, this, scope, index - 1);
		return type;
	}

	// allocation of the fieldBindings array	and its respective constants
	int otherBindingsLength = length - index;
	this.otherCodegenBindings = this.otherBindings = new FieldBinding[otherBindingsLength];
	
	// fill the first constant (the one of the binding)
	constant =
		((bits & FIELD) != 0)
			? FieldReference.getConstantFor((FieldBinding) binding, false, this, scope, index - 1)
			: ((VariableBinding) binding).constant;

	// iteration on each field	
	while (index < length) {
		char[] token = tokens[index];
		if (type == null) return null; // could not resolve type prior to this point
		FieldBinding field = scope.getField(type, token, this);
		int place = index - indexOfFirstFieldBinding;
		otherBindings[place] = field;
		if (!field.isValidBinding()) {
			// try to retrieve the field as private field
			CodeSnippetScope localScope = new CodeSnippetScope(scope);
			if (delegateThis == null) {
				if (this.evaluationContext.declaringTypeName != null) {
					delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
					if (delegateThis == null){ ; // if not found then internal error, field should have been found
						return super.reportError(scope);
					}
				} else {
					return super.reportError(scope);
				}
			}
			field = localScope.getFieldForCodeSnippet(delegateThis.type, token, this);
			otherBindings[place] = field;
		}
		if (field.isValidBinding()) {
			if (isFieldUseDeprecated(field, scope))
				scope.problemReporter().deprecatedField(field, this);
			Constant someConstant = FieldReference.getConstantFor(field, false, this, scope, place);
			// constant propagation can only be performed as long as the previous one is a constant too.
			if (constant != NotAConstant){
				constant = someConstant;
			}
			type = field.type;
			index++;
		} else {
			constant = NotAConstant; //don't fill other constants slots...
			scope.problemReporter().invalidField(this, field, index, type);
			return null;
		}
	}
	return (otherBindings[otherBindingsLength - 1]).type;
}
/**
 * Check and/or redirect the field access to the delegate receiver if any
 */
public TypeBinding getReceiverType(BlockScope currentScope) {
	if (receiverType != null) return receiverType;
	Scope scope = currentScope.parent;
	while (true) {
			switch (scope.kind) {
				case Scope.CLASS_SCOPE :
					return receiverType = ((ClassScope) scope).referenceContext.binding;
				default:
					scope = scope.parent;
			}
	}
}
		
	public void manageSyntheticReadAccessIfNecessary(
		BlockScope currentScope,
		FieldBinding fieldBinding,
		TypeBinding lastReceiverType,
		int index) {

		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		boolean useDelegate = index == 0 && this.delegateThis != null;
		if (useDelegate) lastReceiverType = this.delegateThis.type;

		if (fieldBinding.declaringClass != lastReceiverType
			&& !lastReceiverType.isArrayType()			
			&& fieldBinding.declaringClass != null
			&& fieldBinding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
					&& (index > 0 || indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())
					&& fieldBinding.declaringClass.id != T_Object)
				|| !(useDelegate
						? new CodeSnippetScope(currentScope).canBeSeenByForCodeSnippet(fieldBinding.declaringClass, (ReferenceBinding) this.delegateThis.type)
						: fieldBinding.declaringClass.canBeSeenBy(currentScope)))){
			if (index == 0){
				this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			} else {
				if (this.otherCodegenBindings == this.otherBindings){
					int l = this.otherBindings.length;
					System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[l], 0, l);
				}
				this.otherCodegenBindings[index-1] = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			}
		}
	}
	/*
	 * No need to emulate access to protected fields since not implicitly accessed
	 */
	public void manageSyntheticWriteAccessIfNecessary(
		BlockScope currentScope,
		FieldBinding fieldBinding,
		TypeBinding lastReceiverType) {

		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		boolean useDelegate = fieldBinding == binding && this.delegateThis != null;
		if (useDelegate) lastReceiverType = this.delegateThis.type;

		if (fieldBinding.declaringClass != lastReceiverType
			&& !lastReceiverType.isArrayType()			
			&& fieldBinding.declaringClass != null
			&& fieldBinding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
					&& (fieldBinding != binding || indexOfFirstFieldBinding > 1 || !fieldBinding.isStatic())
					&& fieldBinding.declaringClass.id != T_Object)
				|| !(useDelegate
						? new CodeSnippetScope(currentScope).canBeSeenByForCodeSnippet(fieldBinding.declaringClass, (ReferenceBinding) this.delegateThis.type)
						: fieldBinding.declaringClass.canBeSeenBy(currentScope)))){
			if (fieldBinding == binding){
				this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			} else {
				if (this.otherCodegenBindings == this.otherBindings){
					int l = this.otherBindings.length;
					System.arraycopy(this.otherBindings, 0, this.otherCodegenBindings = new FieldBinding[l], 0, l);
				}
				this.otherCodegenBindings[this.otherCodegenBindings.length-1] = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)lastReceiverType);
			}
		}
	}

/**
 * Normal field binding did not work, try to bind to a field of the delegate receiver.
 */
public TypeBinding reportError(BlockScope scope) {

	if (this.evaluationContext.declaringTypeName != null) {
		delegateThis = scope.getField(scope.enclosingSourceType(), DELEGATE_THIS, this);
		if (delegateThis == null){ ; // if not found then internal error, field should have been found
			return super.reportError(scope);
		}
	} else {
		return super.reportError(scope);
	}

	if ((binding instanceof ProblemFieldBinding && ((ProblemFieldBinding) binding).problemId() == NotFound)
		|| (binding instanceof ProblemBinding && ((ProblemBinding) binding).problemId() == NotFound)){
		// will not support innerclass emulation inside delegate
		FieldBinding fieldBinding = scope.getField(delegateThis.type, this.tokens[0], this);
		if (!fieldBinding.isValidBinding()) {
			if (((ProblemFieldBinding) fieldBinding).problemId() == NotVisible) {
				// manage the access to a private field of the enclosing type
				CodeSnippetScope localScope = new CodeSnippetScope(scope);
				this.codegenBinding = this.binding = localScope.getFieldForCodeSnippet(delegateThis.type, this.tokens[0], this);
				if (binding.isValidBinding()) {
					return checkFieldAccess(scope);						
				} else {
					return super.reportError(scope);
				}
			} else {
				return super.reportError(scope);
			}
		}
		this.codegenBinding = binding = fieldBinding;
		return checkFieldAccess(scope);
	}

	TypeBinding result;
	if (binding instanceof ProblemFieldBinding
		&& ((ProblemFieldBinding) binding).problemId() == NotVisible) {
		result = resolveTypeVisibility(scope);
		if (result == null)
			return super.reportError(scope);
		if (result.isValidBinding()) {
			return result;
		}
	}

	return super.reportError(scope);
}
public TypeBinding resolveTypeVisibility(BlockScope scope) {
	// field and/or local are done before type lookups

	// the only available value for the restrictiveFlag BEFORE
	// the TC is Flag_Type Flag_LocalField and Flag_TypeLocalField 

	CodeSnippetScope localScope = new CodeSnippetScope(scope);
	if ((this.codegenBinding = binding = localScope.getBinding(tokens, bits & RestrictiveFlagMASK, this, (ReferenceBinding) delegateThis.type)).isValidBinding()) {
		bits &= ~RestrictiveFlagMASK; // clear bits
		bits |= FIELD;
		return getOtherFieldBindings(scope);
	}
	//========error cases===============
	return super.reportError(scope);
}
}
