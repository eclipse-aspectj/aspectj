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
public class SingleNameReference extends NameReference implements OperatorIds {
	public char[] token;

	public MethodBinding[] syntheticAccessors; // [0]=read accessor [1]=write accessor
	public static final int READ = 0;
	public static final int WRITE = 1;
	
public SingleNameReference(char[] source, long pos) {
	super();
	token = source;
	sourceStart = (int) (pos >>> 32);
	sourceEnd = (int) pos;
}
public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {

	// compound assignment extra work
	if (isCompound) { // check the variable part is initialized if blank final
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // reading a field
				FieldBinding fieldBinding;
				if ((fieldBinding = (FieldBinding) binding).isFinal() && currentScope.allowBlankFinalFieldAssignment(fieldBinding)) {
					if (!flowInfo.isDefinitelyAssigned(fieldBinding)) {
						currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
						// we could improve error msg here telling "cannot use compound assignment on final blank field"
					}
				}
				manageSyntheticReadAccessIfNecessary(currentScope);
				break;
			case LOCAL : // reading a local variable
				// check if assigning a final blank field
				LocalVariableBinding localBinding;
				if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding) binding)) {
					currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
					// we could improve error msg here telling "cannot use compound assignment on final local variable"
				}
				if (!flowInfo.isFakeReachable()) localBinding.used = true;
		}
	}
	if (assignment.expression != null) {
		flowInfo = assignment.expression.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
	}
	switch (bits & RestrictiveFlagMASK) {
		case FIELD : // assigning to a field
			manageSyntheticWriteAccessIfNecessary(currentScope);

			// check if assigning a final field
			FieldBinding fieldBinding;
			if ((fieldBinding = (FieldBinding) binding).isFinal()) {
				// inside a context where allowed
				if (currentScope.allowBlankFinalFieldAssignment(fieldBinding)) {
					if (flowInfo.isPotentiallyAssigned(fieldBinding)) {
						currentScope.problemReporter().duplicateInitializationOfBlankFinalField(fieldBinding, this);
					}
					flowInfo.markAsDefinitelyAssigned(fieldBinding);
					flowContext.recordSettingFinal(fieldBinding, this);						
				} else {
					currentScope.problemReporter().cannotAssignToFinalField(fieldBinding, this);
				}
			}
			break;
		case LOCAL : // assigning to a local variable 
			LocalVariableBinding localBinding = (LocalVariableBinding) binding;
			if (!flowInfo.isDefinitelyAssigned(localBinding)){// for local variable debug attributes
				bits |= FirstAssignmentToLocalMASK;
			} else {
				bits &= ~FirstAssignmentToLocalMASK;
			}
			if (localBinding.isFinal()) {
				if ((bits & DepthMASK) == 0) {
					if (flowInfo.isPotentiallyAssigned(localBinding)) {
						currentScope.problemReporter().duplicateInitializationOfFinalLocal(localBinding, this);
					}
					flowContext.recordSettingFinal(localBinding, this);								
				} else {
					currentScope.problemReporter().cannotAssignToFinalOuterLocal(localBinding, this);
				}
			}
			flowInfo.markAsDefinitelyAssigned(localBinding);
	}
	manageEnclosingInstanceAccessIfNecessary(currentScope);
	return flowInfo;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return analyseCode(currentScope, flowContext, flowInfo, true);
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, boolean valueRequired) {

	switch (bits & RestrictiveFlagMASK) {
		case FIELD : // reading a field
			if (valueRequired) {
				manageSyntheticReadAccessIfNecessary(currentScope);
			}
			// check if reading a final blank field
			FieldBinding fieldBinding;
			if ((fieldBinding = (FieldBinding) binding).isFinal() && currentScope.allowBlankFinalFieldAssignment(fieldBinding)) {
				if (!flowInfo.isDefinitelyAssigned(fieldBinding)) {
					currentScope.problemReporter().uninitializedBlankFinalField(fieldBinding, this);
				}
			}
			break;
		case LOCAL : // reading a local variable
			LocalVariableBinding localBinding;
			if (!flowInfo.isDefinitelyAssigned(localBinding = (LocalVariableBinding) binding)) {
				currentScope.problemReporter().uninitializedLocalVariable(localBinding, this);
			}
			if (!flowInfo.isFakeReachable()) localBinding.used = true;			
	}
	if (valueRequired) {
		manageEnclosingInstanceAccessIfNecessary(currentScope);
	}
	return flowInfo;
}
public TypeBinding checkFieldAccess(BlockScope scope) {

	FieldBinding fieldBinding = (FieldBinding) binding;
	
	bits &= ~RestrictiveFlagMASK; // clear bits
	bits |= FIELD;
	if (!((FieldBinding) binding).isStatic()) {
		// must check for the static status....
		if (scope.methodScope().isStatic) {
			scope.problemReporter().staticFieldAccessToNonStaticVariable(
				this,
				fieldBinding);
			constant = NotAConstant;
			return null;
		}
	}
	constant = FieldReference.getConstantFor(fieldBinding, true, this, scope, 0);
	if (isFieldUseDeprecated(fieldBinding, scope))
		scope.problemReporter().deprecatedField(fieldBinding, this);

	//===============================================
	//cycle are forbidden ONLY within the same class...why ?????? (poor javac....)
	//Cycle can be done using cross class ref but not direct into a same class reference ????
	//class A {	static int k = B.k+1;}
	//class B {	static int k = A.k+2;}
	//The k-cycle in this example is valid.

	//class C { static int k = k + 1 ;}
	//here it is forbidden ! ????
	//but the next one is valid !!!
	//class C { static int k = C.k + 1;}

	//notice that the next one is also valid ?!?!
	//class A {	static int k = foo().k+1 ; static A foo(){return new A();}}

	//for all these reasons, the next piece of code is only here and not
	//commun for all FieldRef and QualifiedNameRef....(i.e. in the getField(..) API.....

	//instance field may refer to forward static field, like in
	//int i = staticI;
	//static int staticI = 2 ;

	MethodScope ms = scope.methodScope();
	if (ms.enclosingSourceType() == fieldBinding.declaringClass
		&& ms.fieldDeclarationIndex != ms.NotInFieldDecl
		&& fieldBinding.id >= ms.fieldDeclarationIndex) {
		//if the field is static and ms is not .... then it is valid
		if (!fieldBinding.isStatic() || ms.isStatic)
			scope.problemReporter().forwardReference(this, 0, scope.enclosingSourceType());
	}
	//====================================================

	return fieldBinding.type;

}
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {

	// optimizing assignment like: i = i + 1 or i = 1 + i
	if (assignment.expression.isCompactableOperation()) {
		BinaryExpression operation = (BinaryExpression) assignment.expression;
		SingleNameReference variableReference;
		if ((operation.left instanceof SingleNameReference) && ((variableReference = (SingleNameReference) operation.left).binding == binding)) {
			// i = i + value, then use the variable on the right hand side, since it has the correct implicit conversion
			variableReference.generateCompoundAssignment(currentScope, codeStream, syntheticAccessors == null ? null : syntheticAccessors[WRITE], operation.right, (operation.bits & OperatorMASK) >> OperatorSHIFT, operation.left.implicitConversion /*should be equivalent to no conversion*/, valueRequired);
			return;
		}
		int operator = (operation.bits & OperatorMASK) >> OperatorSHIFT;
		if ((operation.right instanceof SingleNameReference)
			&& ((operator == PLUS) || (operator == MULTIPLY)) // only commutative operations
			&& ((variableReference = (SingleNameReference) operation.right).binding == binding)
			&& (operation.left.constant != NotAConstant) // exclude non constant expressions, since could have side-effect
			&& ((operation.left.implicitConversion >> 4) != T_String) // exclude string concatenation which would occur backwards
			&& ((operation.right.implicitConversion >> 4) != T_String)) { // exclude string concatenation which would occur backwards
			// i = value + i, then use the variable on the right hand side, since it has the correct implicit conversion
			variableReference.generateCompoundAssignment(currentScope, codeStream, syntheticAccessors == null ? null : syntheticAccessors[WRITE], operation.left, operator, operation.right.implicitConversion /*should be equivalent to no conversion*/, valueRequired);
			return;
		}
	}
	switch (bits & RestrictiveFlagMASK) {
		case FIELD : // assigning to a field
			FieldBinding fieldBinding;
			if (!(fieldBinding = (FieldBinding) this.codegenBinding).isStatic()) { // need a receiver?
				if ((bits & DepthMASK) != 0) {
					Object[] emulationPath = currentScope.getExactEmulationPath(currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT));
					if (emulationPath == null) {
						// internal error, per construction we should have found it
						currentScope.problemReporter().needImplementation();
					} else {
						codeStream.generateOuterAccess(emulationPath, this, currentScope);
					}
				} else {
					this.generateReceiver(codeStream);
				}
			}
			assignment.expression.generateCode(currentScope, codeStream, true);
			fieldStore(codeStream, fieldBinding, syntheticAccessors == null ? null : syntheticAccessors[WRITE], valueRequired);
			if (valueRequired) {
				codeStream.generateImplicitConversion(assignment.implicitConversion);
			}
			return;
		case LOCAL : // assigning to a local variable
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			if (localBinding.resolvedPosition != -1) {
				assignment.expression.generateCode(currentScope, codeStream, true);
			} else {
				if (assignment.expression.constant != NotAConstant) {
					// assigning an unused local to a constant value = no actual assignment is necessary
					if (valueRequired) {
						codeStream.generateConstant(assignment.expression.constant, assignment.implicitConversion);
					}
				} else {
					assignment.expression.generateCode(currentScope, codeStream, true);
					/* Even though the value may not be required, we force it to be produced, and discard it later
					on if it was actually not necessary, so as to provide the same behavior as JDK1.2beta3.	*/
					if (valueRequired) {
						codeStream.generateImplicitConversion(assignment.implicitConversion); // implicit conversion
					} else {
						if ((localBinding.type == LongBinding) || (localBinding.type == DoubleBinding)) {
							codeStream.pop2();
						} else {
							codeStream.pop();
						}
					}
				}
				return;
			}
			// normal local assignment (since cannot store in outer local which are final locations)
			codeStream.store(localBinding, valueRequired);
			if ((bits & FirstAssignmentToLocalMASK) != 0) { // for local variable debug attributes
				localBinding.recordInitializationStartPC(codeStream.position);
			}
			// implicit conversion
			if (valueRequired) {
				codeStream.generateImplicitConversion(assignment.implicitConversion);
			}
	}
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (constant != NotAConstant) {
		if (valueRequired) {
			codeStream.generateConstant(constant, implicitConversion);
		}
	} else {
		switch (bits & RestrictiveFlagMASK) {
			case FIELD : // reading a field
				FieldBinding fieldBinding;
				if (valueRequired) {
					if ((fieldBinding = (FieldBinding) this.codegenBinding).constant == NotAConstant) { // directly use inlined value for constant fields
						boolean isStatic;
						if (!(isStatic = fieldBinding.isStatic())) {
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
						// managing private access							
						if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
							if (isStatic) {
								codeStream.getstatic(fieldBinding);
							} else {
								codeStream.getfield(fieldBinding);
							}
						} else {
							codeStream.invokestatic(syntheticAccessors[READ]);
						}
						codeStream.generateImplicitConversion(implicitConversion);
					} else { // directly use the inlined value
						codeStream.generateConstant(fieldBinding.constant, implicitConversion);
					}
				}
				break;
			case LOCAL : // reading a local
				LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
				if (valueRequired) {
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
						// regular local variable read
						codeStream.load(localBinding);
					}
					codeStream.generateImplicitConversion(implicitConversion);
				}
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
/*
 * Regular API for compound assignment, relies on the fact that there is only one reference to the
 * variable, which carries both synthetic read/write accessors.
 * The APIs with an extra argument is used whenever there are two references to the same variable which
 * are optimized in one access: e.g "a = a + 1" optimized into "a++".
 */
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {

	this.generateCompoundAssignment(
		currentScope, 
		codeStream, 
		syntheticAccessors == null ? null : syntheticAccessors[WRITE],
		expression,
		operator, 
		assignmentImplicitConversion, 
		valueRequired);
}
/*
 * The APIs with an extra argument is used whenever there are two references to the same variable which
 * are optimized in one access: e.g "a = a + 1" optimized into "a++".
 */
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, MethodBinding writeAccessor, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	switch (bits & RestrictiveFlagMASK) {
		case FIELD : // assigning to a field
			FieldBinding fieldBinding;
			if ((fieldBinding = (FieldBinding) this.codegenBinding).isStatic()) {
				if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
					codeStream.getstatic(fieldBinding);
				} else {
					codeStream.invokestatic(syntheticAccessors[READ]);
				}
			} else {
				if ((bits & DepthMASK) != 0) {
					Object[] emulationPath = currentScope.getExactEmulationPath(currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT));
					if (emulationPath == null) {
						// internal error, per construction we should have found it
						currentScope.problemReporter().needImplementation();
					} else {
						codeStream.generateOuterAccess(emulationPath, this, currentScope);
					}
				} else {
					codeStream.aload_0();
				}
				codeStream.dup();
				if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
					codeStream.getfield(fieldBinding);
				} else {
					codeStream.invokestatic(syntheticAccessors[READ]);
				}
			}
			break;
		case LOCAL : // assigning to a local variable (cannot assign to outer local)
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			Constant assignConstant;
			int increment;
			// using incr bytecode if possible
			switch (localBinding.type.id) {
				case T_String :
					codeStream.generateStringAppend(currentScope, this, expression);
					if (valueRequired) {
						codeStream.dup();
					}
					codeStream.store(localBinding, false);
					return;
				case T_int :
					if (((assignConstant = expression.constant) != NotAConstant) 
						&& (assignConstant.typeID() != T_float) // only for integral types
						&& (assignConstant.typeID() != T_double)
						&& ((increment = assignConstant.intValue()) == (short) increment)) { // 16 bits value
						switch (operator) {
							case PLUS :
								codeStream.iinc(localBinding.resolvedPosition, increment);
								if (valueRequired) {
									codeStream.load(localBinding);
								}
								return;
							case MINUS :
								codeStream.iinc(localBinding.resolvedPosition, -increment);
								if (valueRequired) {
									codeStream.load(localBinding);
								}
								return;
						}
					}
				default :
					codeStream.load(localBinding);
			}
	}
	// perform the actual compound operation
	int operationTypeID;
	if ((operationTypeID = implicitConversion >> 4) == T_String || operationTypeID == T_Object) {
		// we enter here if the single name reference is a field of type java.lang.String or if the type of the 
		// operation is java.lang.Object
		// For example: o = o + ""; // where the compiled type of o is java.lang.Object.
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
	// store the result back into the variable
	switch (bits & RestrictiveFlagMASK) {
		case FIELD : // assigning to a field
			fieldStore(codeStream, (FieldBinding) this.codegenBinding, writeAccessor, valueRequired);
			return;
		case LOCAL : // assigning to a local variable
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			if (valueRequired) {
				if ((localBinding.type == LongBinding) || (localBinding.type == DoubleBinding)) {
					codeStream.dup2();
				} else {
					codeStream.dup();
				}
			}
			codeStream.store(localBinding, false);
	}
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	switch (bits & RestrictiveFlagMASK) {
		case FIELD : // assigning to a field
			FieldBinding fieldBinding;
			if ((fieldBinding = (FieldBinding) this.codegenBinding).isStatic()) {
				if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
					codeStream.getstatic(fieldBinding);
				} else {
					codeStream.invokestatic(syntheticAccessors[READ]);
				}
			} else {
				if ((bits & DepthMASK) != 0) {
					Object[] emulationPath = currentScope.getExactEmulationPath(currentScope.enclosingSourceType().enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT));
					if (emulationPath == null) {
						// internal error, per construction we should have found it
						currentScope.problemReporter().needImplementation();
					} else {
						codeStream.generateOuterAccess(emulationPath, this, currentScope);
					}
				} else {
					codeStream.aload_0();
				}
				codeStream.dup();
				if ((syntheticAccessors == null) || (syntheticAccessors[READ] == null)) {
					codeStream.getfield(fieldBinding);
				} else {
					codeStream.invokestatic(syntheticAccessors[READ]);
				}
			}
			if (valueRequired) {
				if (fieldBinding.isStatic()) {
					if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
						codeStream.dup2();
					} else {
						codeStream.dup();
					}
				} else { // Stack:  [owner][old field value]  ---> [old field value][owner][old field value]
					if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
						codeStream.dup2_x1();
					} else {
						codeStream.dup_x1();
					}
				}
			}
			codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
			codeStream.sendOperator(postIncrement.operator, fieldBinding.type.id);
			codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);
			fieldStore(codeStream, fieldBinding, syntheticAccessors == null ? null : syntheticAccessors[WRITE], false);
			return;
		case LOCAL : // assigning to a local variable
			LocalVariableBinding localBinding = (LocalVariableBinding) this.codegenBinding;
			// using incr bytecode if possible
			if (localBinding.type == IntBinding) {
				if (valueRequired) {
					codeStream.load(localBinding);
				}
				if (postIncrement.operator == PLUS) {
					codeStream.iinc(localBinding.resolvedPosition, 1);
				} else {
					codeStream.iinc(localBinding.resolvedPosition, -1);
				}
			} else {
				codeStream.load(localBinding);
				if (valueRequired){
					if ((localBinding.type == LongBinding) || (localBinding.type == DoubleBinding)) {
						codeStream.dup2();
					} else {
						codeStream.dup();
					}
				}
				codeStream.generateConstant(postIncrement.expression.constant, implicitConversion);
				codeStream.sendOperator(postIncrement.operator, localBinding.type.id);
				codeStream.generateImplicitConversion(postIncrement.assignmentImplicitConversion);

				codeStream.store(localBinding, false);
			}
	}
}
public void generateReceiver(CodeStream codeStream) {
	codeStream.aload_0();
}
public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {

	//If inlinable field, forget the access emulation, the code gen will directly target it
	if (((bits & DepthMASK) == 0) || (constant != NotAConstant)) return;

	switch (bits & RestrictiveFlagMASK) {
		case FIELD :
			FieldBinding fieldBinding;
			if ((fieldBinding = (FieldBinding)binding).isStatic() || (fieldBinding.constant != NotAConstant)) return;
			ReferenceBinding compatibleType = currentScope.enclosingSourceType();
			// the declaringClass of the target binding must be compatible with the enclosing
			// type at <depth> levels outside
			for (int i = 0, depth = (bits & DepthMASK) >> DepthSHIFT; i < depth; i++) {
				compatibleType = compatibleType.enclosingType();
			}
			currentScope.emulateOuterAccess(compatibleType, false); // request cascade of accesses
			break;
		case LOCAL :
			currentScope.emulateOuterAccess((LocalVariableBinding) binding);
	}
}
public void manageSyntheticReadAccessIfNecessary(BlockScope currentScope) {

	//If inlinable field, forget the access emulation, the code gen will directly target it
	if (constant != NotAConstant)
		return;

	//System.err.println("manage synthetic access: " + this);// + " scope: " + currentScope);
	//System.err.println("depth: "  +  ((bits & DepthMASK) >> DepthSHIFT));
	//System.err.println("type: "  +  currentScope.invocationType());
	

	if ((bits & FIELD) != 0) {
		FieldBinding fieldBinding = (FieldBinding) binding;
		if (fieldBinding.alwaysNeedsAccessMethod(true)) {
			if (syntheticAccessors == null)
				syntheticAccessors = new MethodBinding[2];
			syntheticAccessors[READ] = fieldBinding.getAccessMethod(true);
			return;
		}
		if (((bits & DepthMASK) != 0)
			&& (fieldBinding.isPrivate() // private access
				|| (fieldBinding.isProtected() // implicit protected access
						&& fieldBinding.declaringClass.getPackage() 
							!= currentScope.invocationType().getPackage()))) {
			if (syntheticAccessors == null)
				syntheticAccessors = new MethodBinding[2];
			syntheticAccessors[READ] = 
				((SourceTypeBinding)currentScope.invocationType().
					enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT)).
						addSyntheticMethod(fieldBinding, true);
			currentScope.problemReporter().needToEmulateFieldReadAccess(fieldBinding, this);
			return;
		}
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		// and not from Object or implicit static field access.	
		if (fieldBinding.declaringClass != this.actualReceiverType
			&& !this.actualReceiverType.isArrayType()	
			&& fieldBinding.declaringClass != null
			&& fieldBinding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4 
					&& !fieldBinding.isStatic()
					&& fieldBinding.declaringClass.id != T_Object) // no change for Object fields (if there was any)
				|| !fieldBinding.declaringClass.canBeSeenBy(currentScope))){
			this.codegenBinding = currentScope.enclosingSourceType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)this.actualReceiverType);
		}
	}
}
public void manageSyntheticWriteAccessIfNecessary(BlockScope currentScope) {

	if ((bits & FIELD) != 0) {
		FieldBinding fieldBinding = (FieldBinding) binding;
		if (fieldBinding.alwaysNeedsAccessMethod(false)) {
			if (syntheticAccessors == null)
				syntheticAccessors = new MethodBinding[2];
			syntheticAccessors[WRITE] = fieldBinding.getAccessMethod(false);
			return;
		}
		
		if (((bits & DepthMASK) != 0) 
			&& (fieldBinding.isPrivate() // private access
				|| (fieldBinding.isProtected() // implicit protected access
						&& fieldBinding.declaringClass.getPackage() 
							!= currentScope.invocationType().getPackage()))) {
			if (syntheticAccessors == null)
				syntheticAccessors = new MethodBinding[2];
			syntheticAccessors[WRITE] = 
				((SourceTypeBinding)currentScope.invocationType().
					enclosingTypeAt((bits & DepthMASK) >> DepthSHIFT)).
						addSyntheticMethod(fieldBinding, false);
			currentScope.problemReporter().needToEmulateFieldWriteAccess(fieldBinding, this);
			return;
		}
		// if the binding declaring class is not visible, need special action
		// for runtime compatibility on 1.2 VMs : change the declaring class of the binding
		// NOTE: from 1.4 on, field's declaring class is touched if any different from receiver type
		// and not from Object or implicit static field access.	
		if (fieldBinding.declaringClass != this.actualReceiverType
			&& !this.actualReceiverType.isArrayType()	
			&& fieldBinding.declaringClass != null
			&& fieldBinding.constant == NotAConstant
			&& ((currentScope.environment().options.complianceLevel >= CompilerOptions.JDK1_4 
					&& !fieldBinding.isStatic()
					&& fieldBinding.declaringClass.id != T_Object) // no change for Object fields (if there was any)
				|| !fieldBinding.declaringClass.canBeSeenBy(currentScope))){
			this.codegenBinding = currentScope.invocationType().getUpdatedFieldBinding(fieldBinding, (ReferenceBinding)this.actualReceiverType);
		}
	}
}
public TypeBinding reportError(BlockScope scope) {
	//=====error cases=======
	constant = Constant.NotAConstant;
	if (binding instanceof ProblemFieldBinding) {
		scope.problemReporter().invalidField(this, (FieldBinding) binding);
	} else if (binding instanceof ProblemReferenceBinding) {
		scope.problemReporter().invalidType(this, (TypeBinding) binding);
	} else {
		scope.problemReporter().unresolvableReference(this, binding);
	}
	return null;
}
public TypeBinding resolveType(BlockScope scope) {
	// for code gen, harm the restrictiveFlag 	

	this.actualReceiverType = this.receiverType = scope.enclosingSourceType();
	
	if ((this.codegenBinding = this.binding = scope.getBinding(token, bits & RestrictiveFlagMASK, this)).isValidBinding()) {
		switch (bits & RestrictiveFlagMASK) {
			case VARIABLE : // =========only variable============
			case VARIABLE | TYPE : //====both variable and type============
				if (binding instanceof VariableBinding) {
					VariableBinding vb = (VariableBinding) binding;
					if (binding instanceof LocalVariableBinding) {
						bits &= ~RestrictiveFlagMASK;  // clear bits
						bits |= LOCAL;
						constant = vb.constant;
						if ((!vb.isFinal()) && ((bits & DepthMASK) != 0))
							scope.problemReporter().cannotReferToNonFinalOuterLocal((LocalVariableBinding)vb, this);
						return vb.type;
					}
					// a field
					return checkFieldAccess(scope);
				}

				// thus it was a type
				bits &= ~RestrictiveFlagMASK;  // clear bits
				bits |= TYPE;
			case TYPE : //========only type==============
				constant = Constant.NotAConstant;
				//deprecated test
				if (isTypeUseDeprecated((TypeBinding) binding, scope))
					scope.problemReporter().deprecatedType((TypeBinding) binding, this);
				return (TypeBinding) binding;
		}
	}

	// error scenarii
	return this.reportError(scope);
}
public String toStringExpression(){

	return new String(token);}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
public String unboundReferenceErrorName(){

	return new String(token);}
}
