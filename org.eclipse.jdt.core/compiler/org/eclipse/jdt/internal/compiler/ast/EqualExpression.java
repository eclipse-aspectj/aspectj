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

public class EqualExpression extends BinaryExpression {

public EqualExpression(Expression left, Expression right,int operator) {
	super(left,right,operator);
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (((bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
		if ((left.constant != NotAConstant) && (left.constant.typeID() == T_boolean)) {
			if (left.constant.booleanValue()) { //  true == anything
				//  this is equivalent to the right argument inits 
				return right.analyseCode(currentScope, flowContext, flowInfo);
			} else { // false == anything
				//  this is equivalent to the right argument inits negated
				return right.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
			}
		}
		if ((right.constant != NotAConstant) && (right.constant.typeID() == T_boolean)) {
			if (right.constant.booleanValue()) { //  anything == true
				//  this is equivalent to the right argument inits 
				return left.analyseCode(currentScope, flowContext, flowInfo);
			} else { // anything == false
				//  this is equivalent to the right argument inits negated
				return left.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
			}
		}
		return right.analyseCode(
			currentScope, flowContext, 
			left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits()).unconditionalInits();
	} else { //NOT_EQUAL :
		if ((left.constant != NotAConstant) && (left.constant.typeID() == T_boolean)) {
			if (!left.constant.booleanValue()) { //  false != anything
				//  this is equivalent to the right argument inits 
				return right.analyseCode(currentScope, flowContext, flowInfo);
			} else { // true != anything
				//  this is equivalent to the right argument inits negated
				return right.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
			}
		}
		if ((right.constant != NotAConstant) && (right.constant.typeID() == T_boolean)) {
			if (!right.constant.booleanValue()) { //  anything != false
				//  this is equivalent to the right argument inits 
				return left.analyseCode(currentScope, flowContext, flowInfo);
			} else { // anything != true
				//  this is equivalent to the right argument inits negated
				return left.analyseCode(currentScope, flowContext, flowInfo).asNegatedCondition();
			}
		}
		return right.analyseCode(
			currentScope, flowContext, 
			left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits()).asNegatedCondition().unconditionalInits();
	}
}
public final boolean areTypesCastCompatible(BlockScope scope, TypeBinding castTb, TypeBinding expressionTb) {
	//see specifications p.68
	//A more complete version of this method is provided on
	//CastExpression (it deals with constant and need runtime checkcast)


	//========ARRAY===============
	if (expressionTb.isArrayType()) {
		if (castTb.isArrayType()) { //------- (castTb.isArray) expressionTb.isArray -----------
			TypeBinding expressionEltTb = ((ArrayBinding) expressionTb).elementsType(scope);
			if (expressionEltTb.isBaseType())
				// <---stop the recursion------- 
				return ((ArrayBinding) castTb).elementsType(scope) == expressionEltTb;
			//recursivly on the elts...
			return areTypesCastCompatible(scope, ((ArrayBinding) castTb).elementsType(scope), expressionEltTb);
		}
		if (castTb.isBaseType()) {
			return false;
		}
		if (castTb.isClass()) { //------(castTb.isClass) expressionTb.isArray ---------------	
			if (scope.isJavaLangObject(castTb))
				return true;
			return false;
		}
		if (castTb.isInterface()) { //------- (castTb.isInterface) expressionTb.isArray -----------
			if (scope.isJavaLangCloneable(castTb) || scope.isJavaIoSerializable(castTb)) {
				return true;
			}
			return false;
		}

		return false;
	}

	//------------(castType) null--------------
	if (expressionTb == NullBinding) {
		return !castTb.isBaseType();
	}

	//========BASETYPE==============
	if (expressionTb.isBaseType()) {
		return false;
	}


	//========REFERENCE TYPE===================

	if (expressionTb.isClass()) {
		if (castTb.isArrayType()) { // ---- (castTb.isArray) expressionTb.isClass -------
			if (scope.isJavaLangObject(expressionTb))
				return true;
		}
		if (castTb.isBaseType()) {
			return false;
		}
		if (castTb.isClass()) { // ----- (castTb.isClass) expressionTb.isClass ------ 
			if (scope.areTypesCompatible(expressionTb, castTb))
				return true;
			else {
				if (scope.areTypesCompatible(castTb, expressionTb)) {
					return true;
				}
				return false;
			}
		}
		if (castTb.isInterface()) { // ----- (castTb.isInterface) expressionTb.isClass -------  
			if (((ReferenceBinding) expressionTb).isFinal()) { //no subclass for expressionTb, thus compile-time check is valid
				if (scope.areTypesCompatible(expressionTb, castTb))
					return true;
				return false;
			} else {
				return true;
			}
		}

		return false;
	}
	if (expressionTb.isInterface()) {
		if (castTb.isArrayType()) { // ----- (castTb.isArray) expressionTb.isInterface ------
			if (scope.isJavaLangCloneable(expressionTb) || scope.isJavaIoSerializable(expressionTb))
				//potential runtime error
				{
				return true;
			}
			return false;
		}
		if (castTb.isBaseType()) {
			return false;
		}
		if (castTb.isClass()) { // ----- (castTb.isClass) expressionTb.isInterface --------
			if (scope.isJavaLangObject(castTb))
				return true;
			if (((ReferenceBinding) castTb).isFinal()) { //no subclass for castTb, thus compile-time check is valid
				if (scope.areTypesCompatible(castTb, expressionTb)) {
					return true;
				}
				return false;
			}
			return true;
		}
		if (castTb.isInterface()) { // ----- (castTb.isInterface) expressionTb.isInterface -------
			if (castTb != expressionTb && (Scope.compareTypes(castTb, expressionTb) == NotRelated)) {
				MethodBinding[] castTbMethods = ((ReferenceBinding) castTb).methods();
				int castTbMethodsLength = castTbMethods.length;
				MethodBinding[] expressionTbMethods = ((ReferenceBinding) expressionTb).methods();
				int expressionTbMethodsLength = expressionTbMethods.length;
				for (int i = 0; i < castTbMethodsLength; i++) {
					for (int j = 0; j < expressionTbMethodsLength; j++) {
						if (castTbMethods[i].selector == expressionTbMethods[j].selector) {
							if (castTbMethods[i].returnType != expressionTbMethods[j].returnType) {
								if (castTbMethods[i].areParametersEqual(expressionTbMethods[j])) {
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}

		return false;
	}

	return false;
}
public final void computeConstant(TypeBinding leftTb, TypeBinding rightTb) {
	if ((left.constant != NotAConstant) && (right.constant != NotAConstant)) {
		constant =
			Constant.computeConstantOperationEQUAL_EQUAL(
				left.constant,
				leftTb.id,
				EQUAL_EQUAL,
				right.constant,
				rightTb.id);
		if (((bits & OperatorMASK) >> OperatorSHIFT) == NOT_EQUAL)
			constant = Constant.fromValue(!constant.booleanValue());
	} else {
		constant = NotAConstant;
	}
}
/**
 * Normal == or != code generation.
 *
 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 * @param valueRequired boolean
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	if (constant != NotAConstant) {
		int pc = codeStream.position;
		if (valueRequired) 
			codeStream.generateConstant(constant, implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		return;
	}
	Label falseLabel;
	generateOptimizedBoolean(
		currentScope, 
		codeStream, 
		null, 
		falseLabel = new Label(codeStream), 
		valueRequired);
	if (falseLabel.hasForwardReferences()) {
		if (valueRequired){
			// comparison is TRUE 
			codeStream.iconst_1();
			if ((bits & ValueForReturnMASK) != 0){
				codeStream.ireturn();
				// comparison is FALSE
				falseLabel.place();
				codeStream.iconst_0();
			} else {
				Label endLabel = new Label(codeStream);
				codeStream.goto_(endLabel);
				codeStream.decrStackSize(1);
				// comparison is FALSE
				falseLabel.place();
				codeStream.iconst_0();
				endLabel.place();
			}
		} else {
			falseLabel.place();
		}	
	}	
}
/**
 * Boolean operator code generation
 *	Optimized operations are: == and !=
 */
public void generateOptimizedBoolean(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean)) {
		super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
		return;
	}
	int pc = codeStream.position;
	if (((bits & OperatorMASK) >> OperatorSHIFT) == EQUAL_EQUAL) {
		if ((left.implicitConversion & 0xF) /*compile-time*/ == T_boolean) {
			generateOptimizedBooleanEqual(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
		} else {
			generateOptimizedNonBooleanEqual(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
		}
	} else {
		if ((left.implicitConversion & 0xF) /*compile-time*/ == T_boolean) {
			generateOptimizedBooleanEqual(currentScope, codeStream, falseLabel, trueLabel, valueRequired);
		} else {
			generateOptimizedNonBooleanEqual(currentScope, codeStream, falseLabel, trueLabel, valueRequired);
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
/**
 * Boolean generation for == with boolean operands
 *
 * Note this code does not optimize conditional constants !!!!
 */
public void generateOptimizedBooleanEqual(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	int pc = codeStream.position;
	// optimized cases: true == x, false == x
	if (left.constant != NotAConstant) {
		boolean inline = left.constant.booleanValue();
		right.generateOptimizedBoolean(currentScope, codeStream, (inline ? trueLabel : falseLabel), (inline ? falseLabel : trueLabel), valueRequired);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		return;
	} // optimized cases: x == true, x == false
	if (right.constant != NotAConstant) {
		boolean inline = right.constant.booleanValue();
		left.generateOptimizedBoolean(currentScope, codeStream, (inline ? trueLabel : falseLabel), (inline ? falseLabel : trueLabel), valueRequired);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		return;
	}
	// default case
	left.generateCode(currentScope, codeStream, valueRequired);
	right.generateCode(currentScope, codeStream, valueRequired);
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				codeStream.if_icmpeq(trueLabel);
			}
		} else {
			// implicit falling through the TRUE case
			if (trueLabel == null) {
				codeStream.if_icmpne(falseLabel);
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
/**
 * Boolean generation for == with non-boolean operands
 *
 */
public void generateOptimizedNonBooleanEqual(BlockScope currentScope, CodeStream codeStream, Label trueLabel, Label falseLabel, boolean valueRequired) {
	int pc = codeStream.position;
	Constant inline;
	if ((inline = right.constant) != NotAConstant) {
		// optimized case: x == null
		if (right.constant == NullConstant.Default) {
			left.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifnull(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifnonnull(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		// optimized case: x == 0
		if (((left.implicitConversion >> 4) == T_int) && (inline.intValue() == 0)) {
			left.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifeq(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifne(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
	}
	if ((inline = left.constant) != NotAConstant) {
		// optimized case: null == x
		if (left.constant == NullConstant.Default) {
			right.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifnull(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifnonnull(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		// optimized case: 0 == x
		if (((left.implicitConversion >> 4) == T_int)
			&& (inline.intValue() == 0)) {
			right.generateCode(currentScope, codeStream, valueRequired);
			if (valueRequired) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifeq(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifne(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
	}
	// default case
	left.generateCode(currentScope, codeStream, valueRequired);
	right.generateCode(currentScope, codeStream, valueRequired);
	if (valueRequired) {
		if (falseLabel == null) {
			if (trueLabel != null) {
				// implicit falling through the FALSE case
				switch (left.implicitConversion >> 4) { // operand runtime type
					case T_int :
						codeStream.if_icmpeq(trueLabel);
						break;
					case T_float :
						codeStream.fcmpl();
						codeStream.ifeq(trueLabel);
						break;
					case T_long :
						codeStream.lcmp();
						codeStream.ifeq(trueLabel);
						break;
					case T_double :
						codeStream.dcmpl();
						codeStream.ifeq(trueLabel);
						break;
					default :
						codeStream.if_acmpeq(trueLabel);
				}
			}
		} else {
			// implicit falling through the TRUE case
			if (trueLabel == null) {
				switch (left.implicitConversion >> 4) { // operand runtime type
					case T_int :
						codeStream.if_icmpne(falseLabel);
						break;
					case T_float :
						codeStream.fcmpl();
						codeStream.ifne(falseLabel);
						break;
					case T_long :
						codeStream.lcmp();
						codeStream.ifne(falseLabel);
						break;
					case T_double :
						codeStream.dcmpl();
						codeStream.ifne(falseLabel);
						break;
					default :
						codeStream.if_acmpne(falseLabel);
				}
			} else {
				// no implicit fall through TRUE/FALSE --> should never occur
			}
		}
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public boolean isCompactableOperation() {
	return false;
}
public TypeBinding resolveType(BlockScope scope) {
	// always return BooleanBinding
	TypeBinding leftTb = left.resolveType(scope);
	TypeBinding rightTb = right.resolveType(scope);
	if (leftTb == null || rightTb == null){
		constant = NotAConstant;		
		return null;
	}

	// both base type
	if (leftTb.isBaseType() && rightTb.isBaseType()) {
		// the code is an int
		// (cast)  left   == (cast)  rigth --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8    <<4       <<0
		int result = ResolveTypeTables[EQUAL_EQUAL][ (leftTb.id << 4) + rightTb.id];
		left.implicitConversion = result >>> 12;
		right.implicitConversion = (result >>> 4) & 0x000FF;
		bits |= result & 0xF;		
		if ((result & 0x0000F) == T_undefined) {
			constant = Constant.NotAConstant;
			scope.problemReporter().invalidOperator(this, leftTb, rightTb);
			return null;
		}
		computeConstant(leftTb, rightTb);
		this.typeBinding = BooleanBinding;
		return BooleanBinding;
	}

	// Object references 
	// spec 15.20.3
	if (areTypesCastCompatible(scope, rightTb, leftTb) || areTypesCastCompatible(scope, leftTb, rightTb)) {
		// (special case for String)
		if ((rightTb.id == T_String) && (leftTb.id == T_String))
			computeConstant(leftTb, rightTb);
		else
			constant = NotAConstant;
		if (rightTb.id == T_String)
			right.implicitConversion = String2String;
		if (leftTb.id == T_String)
			left.implicitConversion = String2String;
		this.typeBinding = BooleanBinding;
		return BooleanBinding;
	}
	constant = NotAConstant;
	scope.problemReporter().notCompatibleTypesError(this, leftTb, rightTb);
	return null;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		left.traverse(visitor, scope);
		right.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
