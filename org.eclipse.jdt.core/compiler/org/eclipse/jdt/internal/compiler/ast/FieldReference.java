/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * AspectJ - support for FieldBinding.alwaysNeedsAccessMethod
 */
public class FieldReference extends Reference implements InvocationSite {

	public Expression receiver;
	public char[] token;
	public FieldBinding binding, codegenBinding;
	public long nameSourcePosition; //(start<<32)+end
	MethodBinding syntheticReadAccessor, syntheticWriteAccessor;
	public TypeBinding receiverType;

	public FieldReference(char[] source, long pos) {

		token = source;
		nameSourcePosition = pos;
		//by default the position are the one of the field (not true for super access)
		sourceStart = (int) (pos >>> 32);
		sourceEnd = (int) (pos & 0x00000000FFFFFFFFL);
		bits |= BindingIds.FIELD;

	}

	public FlowInfo analyseAssignment(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		Assignment assignment,
		boolean isCompound) {

		// compound assignment extra work
		if (isCompound) { // check the variable part is initialized if blank final
			if (binding.isFinal()
				&& receiver.isThis()
				&& currentScope.allowBlankFinalFieldAssignment(binding)
				&& (!flowInfo.isDefinitelyAssigned(binding))) {
				currentScope.problemReporter().uninitializedBlankFinalField(binding, this);
				// we could improve error msg here telling "cannot use compound assignment on final blank field"
			}
			manageSyntheticReadAccessIfNecessary(currentScope);
		}
		if (assignment.expression != null) {
			flowInfo =
				assignment
					.expression
					.analyseCode(currentScope, flowContext, flowInfo)
					.unconditionalInits();
		}
		flowInfo =
			receiver
				.analyseCode(currentScope, flowContext, flowInfo, !binding.isStatic())
				.unconditionalInits();
		manageSyntheticWriteAccessIfNecessary(currentScope);

		// check if assigning a final field 
		if (binding.isFinal()) {
			// in a context where it can be assigned?
			if (receiver.isThis()
				&& !(receiver instanceof QualifiedThisReference)
				&& currentScope.allowBlankFinalFieldAssignment(binding)) {
				if (flowInfo.isPotentiallyAssigned(binding)) {
					currentScope.problemReporter().duplicateInitializationOfBlankFinalField(
						binding,
						this);
				}
				flowInfo.markAsDefinitelyAssigned(binding);
				flowContext.recordSettingFinal(binding, this);
			} else {
				// assigning a final field outside an initializer or constructor
				currentScope.problemReporter().cannotAssignToFinalField(binding, this);
			}
		}
		return flowInfo;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return analyseCode(currentScope, flowContext, flowInfo, true);
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		boolean valueRequired) {

		receiver.analyseCode(currentScope, flowContext, flowInfo, !binding.isStatic());
		if (valueRequired) {
			manageSyntheticReadAccessIfNecessary(currentScope);
		}
		return flowInfo;
	}

	public FieldBinding fieldBinding() {

		return binding;
	}

	public void generateAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Assignment assignment,
		boolean valueRequired) {

		receiver.generateCode(
			currentScope,
			codeStream,
			!this.codegenBinding.isStatic());
		assignment.expression.generateCode(currentScope, codeStream, true);
		fieldStore(
			codeStream,
			this.codegenBinding,
			syntheticWriteAccessor,
			valueRequired);
		if (valueRequired) {
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
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		if (constant != NotAConstant) {
			if (valueRequired) {
				codeStream.generateConstant(constant, implicitConversion);
			}
		} else {
			boolean isStatic = this.codegenBinding.isStatic();
			receiver.generateCode(
				currentScope,
				codeStream,
				valueRequired && (!isStatic) && (this.codegenBinding.constant == NotAConstant));
			if (valueRequired) {
				if (this.codegenBinding.constant == NotAConstant) {
					if (this.codegenBinding.declaringClass == null) { // array length
						codeStream.arraylength();
					} else {
						if (syntheticReadAccessor == null) {
							if (isStatic) {
								codeStream.getstatic(this.codegenBinding);
							} else {
								codeStream.getfield(this.codegenBinding);
							}
						} else {
							codeStream.invokestatic(syntheticReadAccessor);
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

	public void generateCompoundAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Expression expression,
		int operator,
		int assignmentImplicitConversion,
		boolean valueRequired) {

		boolean isStatic;
		receiver.generateCode(
			currentScope,
			codeStream,
			!(isStatic = this.codegenBinding.isStatic()));
		if (isStatic) {
			if (syntheticReadAccessor == null) {
				codeStream.getstatic(this.codegenBinding);
			} else {
				codeStream.invokestatic(syntheticReadAccessor);
			}
		} else {
			codeStream.dup();
			if (syntheticReadAccessor == null) {
				codeStream.getfield(this.codegenBinding);
			} else {
				codeStream.invokestatic(syntheticReadAccessor);
			}
		}
		int operationTypeID;
		if ((operationTypeID = implicitConversion >> 4) == T_String) {
			codeStream.generateStringAppend(currentScope, null, expression);
		} else {
			// promote the array reference to the suitable operation type
			codeStream.generateImplicitConversion(implicitConversion);
			// generate the increment value (will by itself  be promoted to the operation value)
			if (expression == IntLiteral.One) { // prefix operation
				codeStream.generateConstant(expression.constant, implicitConversion);
			} else {
				expression.generateCode(currentScope, codeStream, true);
			}
			// perform the operation
			codeStream.sendOperator(operator, operationTypeID);
			// cast the value back to the array reference type
			codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		fieldStore(
			codeStream,
			this.codegenBinding,
			syntheticWriteAccessor,
			valueRequired);
	}

	public void generatePostIncrement(
		BlockScope currentScope,
		CodeStream codeStream,
		CompoundAssignment postIncrement,
		boolean valueRequired) {

		boolean isStatic;
		receiver.generateCode(
			currentScope,
			codeStream,
			!(isStatic = this.codegenBinding.isStatic()));
		if (isStatic) {
			if (syntheticReadAccessor == null) {
				codeStream.getstatic(this.codegenBinding);
			} else {
				codeStream.invokestatic(syntheticReadAccessor);
			}
		} else {
			codeStream.dup();
			if (syntheticReadAccessor == null) {
				codeStream.getfield(this.codegenBinding);
			} else {
				codeStream.invokestatic(syntheticReadAccessor);
			}
		}
		if (valueRequired) {
			if (isStatic) {
				if ((this.codegenBinding.type == LongBinding)
					|| (this.codegenBinding.type == DoubleBinding)) {
					codeStream.dup2();
				} else {
					codeStream.dup();
				}
			} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
				if ((this.codegenBinding.type == LongBinding)
					|| (this.codegenBinding.type == DoubleBinding)) {
					codeStream.dup2_x1();
				} else {
					codeStream.dup_x1();
				}
			}
		}
		codeStream.generateConstant(
			postIncrement.expression.constant,
			implicitConversion);
		codeStream.sendOperator(postIncrement.operator, this.codegenBinding.type.id);
		codeStream.generateImplicitConversion(
			postIncrement.assignmentImplicitConversion);
		fieldStore(codeStream, this.codegenBinding, syntheticWriteAccessor, false);
	}

	public static final Constant getConstantFor(
		FieldBinding binding,
		boolean implicitReceiver,
		Reference reference,
		Scope referenceScope,
		int indexInQualification) {

		//propagation of the constant.

		//ref can be a FieldReference, a SingleNameReference or a QualifiedNameReference
		//indexInQualification may have a value greater than zero only for QualifiednameReference
		//if ref==null then indexInQualification==0 AND implicitReceiver == false. This case is a 
		//degenerated case where a fake reference field (null) 
		//is associted to a real FieldBinding in order 
		//to allow its constant computation using the regular path (i.e. find the fieldDeclaration
		//and proceed to its type resolution). As implicitReceiver is false, no error reporting
		//against ref will be used ==> no nullPointerException risk .... 

		//special treatment for langage-built-in  field (their declaring class is null)
		if (binding.declaringClass == null) {
			//currently only one field "length" : the constant computation is never done
			return NotAConstant;
		}
		if (!binding.isFinal()) {
			return binding.constant = NotAConstant;
		}
		if (binding.constant != null) {
			if (indexInQualification == 0) {
				return binding.constant;
			}
			//see previous comment for the (sould-always-be) valid cast
			QualifiedNameReference qualifiedReference = (QualifiedNameReference) reference;
			if (indexInQualification == (qualifiedReference.indexOfFirstFieldBinding - 1)) {
				return binding.constant;
			}
			return NotAConstant;
		}

		//The field has not been yet type checked.
		//It also means that the field is not coming from a class that
		//has already been compiled. It can only be from a class within
		//compilation units to process. Thus the field is NOT from a BinaryTypeBinbing

		SourceTypeBinding typeBinding = (SourceTypeBinding) binding.declaringClass;
		TypeDeclaration typeDecl = typeBinding.scope.referenceContext;
		FieldDeclaration fieldDecl = typeDecl.declarationOf(binding.getFieldBindingForLookup());
		//System.err.println(typeDecl + " and " + fieldDecl + ", " + binding);
		//what scope to use (depend on the staticness of the field binding)
		MethodScope fieldScope =
			binding.isStatic()
				? typeDecl.staticInitializerScope
				: typeDecl.initializerScope;

		if (implicitReceiver) { //Determine if the ref is legal in the current class of the field
			//i.e. not a forward reference .... (they are allowed when the receiver is explicit ! ... Please don't ask me why !...yet another java mystery...)
			if (fieldScope.fieldDeclarationIndex == MethodScope.NotInFieldDecl) {
				// no field is currently being analysed in typeDecl
				fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
				return binding.constant;
			}
			//We are re-entering the same class fields analysing
			if ((reference != null)
				&& (binding.declaringClass == referenceScope.enclosingSourceType()) // only complain for access inside same type
				&& (binding.id > fieldScope.fieldDeclarationIndex)) {
				//forward reference. The declaration remains unresolved.
				referenceScope.problemReporter().forwardReference(reference, indexInQualification, typeBinding);
				return NotAConstant;
			}
			fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
			return binding.constant;
		}
		//the field reference is explicity. It has to be a "simple" like field reference to get the
		//constant propagation. For example in Packahe.Type.field1.field2 , field1 may have its
		//constant having a propagation where field2 is always not propagating its
		if (indexInQualification == 0) {
			fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
			return binding.constant;
		}
		// Side-effect on the field binding may not be propagated out for the qualified reference
		// unless it occurs in first place of the name sequence
		fieldDecl.resolve(fieldScope); //side effect on binding :-) ... 
		//see previous comment for the cast that should always be valid
		QualifiedNameReference qualifiedReference = (QualifiedNameReference) reference;
		if (indexInQualification == (qualifiedReference.indexOfFirstFieldBinding - 1)) {
			return binding.constant;
		} else {
			return NotAConstant;
		}
	}

	public boolean isSuperAccess() {

		return receiver.isSuper();
	}

	public boolean isTypeAccess() {

		return receiver != null && receiver.isTypeReference();
	}

	/*
	 * No need to emulate access to protected fields since not implicitly accessed
	 */
	public void manageSyntheticReadAccessIfNecessary(BlockScope currentScope) {
		if (binding.alwaysNeedsAccessMethod(true)) {
			syntheticReadAccessor = binding.getAccessMethod(true);
			return;
		}


		if (binding.isPrivate()) {
			if ((currentScope.enclosingSourceType() != binding.declaringClass)
				&& (binding.constant == NotAConstant)) {
				syntheticReadAccessor =
					((SourceTypeBinding) binding.declaringClass).addSyntheticMethod(binding, true);
				currentScope.problemReporter().needToEmulateFieldReadAccess(binding, this);
				return;
			}

		} else if (receiver instanceof QualifiedSuperReference) { // qualified super

			// qualified super need emulation always
			SourceTypeBinding destinationType =
				(SourceTypeBinding) (((QualifiedSuperReference) receiver)
					.currentCompatibleType);
			syntheticReadAccessor = destinationType.addSyntheticMethod(binding, true);
			currentScope.problemReporter().needToEmulateFieldReadAccess(binding, this);
			return;

		} else if (binding.isProtected()) {

			SourceTypeBinding enclosingSourceType;
			if (((bits & DepthMASK) != 0)
				&& binding.declaringClass.getPackage()
					!= (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()) {

				SourceTypeBinding currentCompatibleType =
					(SourceTypeBinding) enclosingSourceType.enclosingTypeAt(
						(bits & DepthMASK) >> DepthSHIFT);
				syntheticReadAccessor = currentCompatibleType.addSyntheticMethod(binding, true);
				currentScope.problemReporter().needToEmulateFieldReadAccess(binding, this);
				return;
			}
		}
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		if (binding.declaringClass != this.receiverType
			&& !this.receiverType.isArrayType()
			&& binding.declaringClass != null // array.length
			&& binding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
				&& binding.declaringClass.id != T_Object)
			//no change for Object fields (in case there was)
				|| !binding.declaringClass.canBeSeenBy(currentScope))) {
			this.codegenBinding =
				currentScope.enclosingSourceType().getUpdatedFieldBinding(
					binding,
					(ReferenceBinding) this.receiverType);
		}
	}

	/*
	 * No need to emulate access to protected fields since not implicitly accessed
	 */
	public void manageSyntheticWriteAccessIfNecessary(BlockScope currentScope) {
		//System.err.println("manage synthetic: " + this + " with " + binding + ", " + binding.getClass());
		if (binding.alwaysNeedsAccessMethod(false)) {
			syntheticWriteAccessor = binding.getAccessMethod(false);
			return;
		}


		if (binding.isPrivate()) {
			if (currentScope.enclosingSourceType() != binding.declaringClass) {
				syntheticWriteAccessor =
					((SourceTypeBinding) binding.declaringClass).addSyntheticMethod(binding, false);
				currentScope.problemReporter().needToEmulateFieldWriteAccess(binding, this);
				return;
			}

		} else if (receiver instanceof QualifiedSuperReference) { // qualified super

			// qualified super need emulation always
			SourceTypeBinding destinationType =
				(SourceTypeBinding) (((QualifiedSuperReference) receiver)
					.currentCompatibleType);
			syntheticWriteAccessor = destinationType.addSyntheticMethod(binding, false);
			currentScope.problemReporter().needToEmulateFieldWriteAccess(binding, this);
			return;

		} else if (binding.isProtected()) {

			SourceTypeBinding enclosingSourceType;
			if (((bits & DepthMASK) != 0)
				&& binding.declaringClass.getPackage()
					!= (enclosingSourceType = currentScope.enclosingSourceType()).getPackage()) {

				SourceTypeBinding currentCompatibleType =
					(SourceTypeBinding) enclosingSourceType.enclosingTypeAt(
						(bits & DepthMASK) >> DepthSHIFT);
				syntheticWriteAccessor =
					currentCompatibleType.addSyntheticMethod(binding, false);
				currentScope.problemReporter().needToEmulateFieldWriteAccess(binding, this);
				return;
			}
		}
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		if (binding.declaringClass != this.receiverType
			&& !this.receiverType.isArrayType()
			&& binding.declaringClass != null // array.length
			&& binding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4
				&& binding.declaringClass.id != T_Object)
			//no change for Object fields (in case there was)
				|| !binding.declaringClass.canBeSeenBy(currentScope))) {
			this.codegenBinding =
				currentScope.enclosingSourceType().getUpdatedFieldBinding(
					binding,
					(ReferenceBinding) this.receiverType);
		}
	}

	public TypeBinding resolveType(BlockScope scope) {

		// Answer the signature type of the field.
		// constants are propaged when the field is final
		// and initialized with a (compile time) constant 

		// regular receiver reference 
		this.receiverType = receiver.resolveType(scope);
		if (this.receiverType == null) {
			constant = NotAConstant;
			return null;
		}
		// the case receiverType.isArrayType and token = 'length' is handled by the scope API
		this.codegenBinding =
			this.binding = scope.getField(this.receiverType, token, this);
		if (!binding.isValidBinding()) {
			constant = NotAConstant;
			scope.problemReporter().invalidField(this, this.receiverType);
			return null;
		}

		if (isFieldUseDeprecated(binding, scope))
			scope.problemReporter().deprecatedField(binding, this);

		// check for this.x in static is done in the resolution of the receiver
		constant =
			FieldReference.getConstantFor(
				binding,
				receiver == ThisReference.ThisImplicit,
				this,
				scope,
				0);
		if (receiver != ThisReference.ThisImplicit)
			constant = NotAConstant;

		return binding.type;
	}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}

	public void setDepth(int depth) {

		if (depth > 0) {
			bits &= ~DepthMASK; // flush previous depth if any			
			bits |= (depth & 0xFF) << DepthSHIFT; // encoded on 8 bits
		}
	}

	public void setFieldIndex(int index) {
		// ignored
	}

	public String toStringExpression() {

		return receiver.toString() + "." //$NON-NLS-1$
		+ new String(token);
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			receiver.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}