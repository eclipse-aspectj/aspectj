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

public class BinaryExpression extends OperatorExpression {

	public Expression left, right;
	public Constant optimizedBooleanConstant;

	public BinaryExpression(Expression left, Expression right, int operator) {

		this.left = left;
		this.right = right;
		this.bits |= operator << OperatorSHIFT; // encode operator
		this.sourceStart = left.sourceStart;
		this.sourceEnd = right.sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return right
			.analyseCode(
				currentScope,
				flowContext,
				left.analyseCode(currentScope, flowContext, flowInfo).unconditionalInits())
			.unconditionalInits();
	}

	public void computeConstant(BlockScope scope, int leftId, int rightId) {

		//compute the constant when valid
		if ((left.constant != Constant.NotAConstant)
			&& (right.constant != Constant.NotAConstant)) {
			try {
				constant =
					Constant.computeConstantOperation(
						left.constant,
						leftId,
						(bits & OperatorMASK) >> OperatorSHIFT,
						right.constant,
						rightId);
			} catch (ArithmeticException e) {
				constant = Constant.NotAConstant;
				// 1.2 no longer throws an exception at compile-time
				//scope.problemReporter().compileTimeConstantThrowsArithmeticException(this);
			}
		} else {
			constant = Constant.NotAConstant;
			//add some work for the boolean operators & |  
			optimizedBooleanConstant(
				leftId,
				(bits & OperatorMASK) >> OperatorSHIFT,
				rightId);
		}
	}

	public Constant conditionalConstant() {

		return optimizedBooleanConstant == null ? constant : optimizedBooleanConstant;
	}

	/**
	 * Code generation for a binary operation
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		Label falseLabel, endLabel;
		if (constant != Constant.NotAConstant) {
			if (valueRequired)
				codeStream.generateConstant(constant, implicitConversion);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		bits |= OnlyValueRequiredMASK;
		switch ((bits & OperatorMASK) >> OperatorSHIFT) {
			case PLUS :
				switch (bits & ReturnTypeIDMASK) {
					case T_String :
						codeStream.generateStringAppend(currentScope, left, right);
						if (!valueRequired)
							codeStream.pop();
						break;
					case T_int :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.iadd();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.ladd();
						break;
					case T_double :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.dadd();
						break;
					case T_float :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.fadd();
						break;
				}
				break;
			case MINUS :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.isub();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.lsub();
						break;
					case T_double :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.dsub();
						break;
					case T_float :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.fsub();
						break;
				}
				break;
			case MULTIPLY :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.imul();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.lmul();
						break;
					case T_double :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.dmul();
						break;
					case T_float :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.fmul();
						break;
				}
				break;
			case DIVIDE :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						left.generateCode(currentScope, codeStream, true);
						right.generateCode(currentScope, codeStream, true);
						codeStream.idiv();
						if (!valueRequired)
							codeStream.pop();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, true);
						right.generateCode(currentScope, codeStream, true);
						codeStream.ldiv();
						if (!valueRequired)
							codeStream.pop2();
						break;
					case T_double :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.ddiv();
						break;
					case T_float :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.fdiv();
						break;
				}
				break;
			case REMAINDER :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						left.generateCode(currentScope, codeStream, true);
						right.generateCode(currentScope, codeStream, true);
						codeStream.irem();
						if (!valueRequired)
							codeStream.pop();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, true);
						right.generateCode(currentScope, codeStream, true);
						codeStream.lrem();
						if (!valueRequired)
							codeStream.pop2();
						break;
					case T_double :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.drem();
						break;
					case T_float :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.frem();
						break;
				}
				break;
			case AND :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						// 0 & x
						if ((left.constant != Constant.NotAConstant)
							&& (left.constant.typeID() == T_int)
							&& (left.constant.intValue() == 0)) {
							right.generateCode(currentScope, codeStream, false);
							if (valueRequired)
								codeStream.iconst_0();
						} else {
							// x & 0
							if ((right.constant != Constant.NotAConstant)
								&& (right.constant.typeID() == T_int)
								&& (right.constant.intValue() == 0)) {
								left.generateCode(currentScope, codeStream, false);
								if (valueRequired)
									codeStream.iconst_0();
							} else {
								left.generateCode(currentScope, codeStream, valueRequired);
								right.generateCode(currentScope, codeStream, valueRequired);
								if (valueRequired)
									codeStream.iand();
							}
						}
						break;
					case T_long :
						// 0 & x
						if ((left.constant != Constant.NotAConstant)
							&& (left.constant.typeID() == T_long)
							&& (left.constant.longValue() == 0L)) {
							right.generateCode(currentScope, codeStream, false);
							if (valueRequired)
								codeStream.lconst_0();
						} else {
							// x & 0
							if ((right.constant != Constant.NotAConstant)
								&& (right.constant.typeID() == T_long)
								&& (right.constant.longValue() == 0L)) {
								left.generateCode(currentScope, codeStream, false);
								if (valueRequired)
									codeStream.lconst_0();
							} else {
								left.generateCode(currentScope, codeStream, valueRequired);
								right.generateCode(currentScope, codeStream, valueRequired);
								if (valueRequired)
									codeStream.land();
							}
						}
						break;
					case T_boolean : // logical and
						generateOptimizedLogicalAnd(
							currentScope,
							codeStream,
							null,
							(falseLabel = new Label(codeStream)),
							valueRequired);
						/* improving code gen for such a case: boolean b = i < 0 && false;
						 * since the label has never been used, we have the inlined value on the stack. */
						if (falseLabel.hasForwardReferences()) {
							if (valueRequired) {
								codeStream.iconst_1();
								if ((bits & ValueForReturnMASK) != 0) {
									codeStream.ireturn();
									falseLabel.place();
									codeStream.iconst_0();
								} else {
									codeStream.goto_(endLabel = new Label(codeStream));
									codeStream.decrStackSize(1);
									falseLabel.place();
									codeStream.iconst_0();
									endLabel.place();
								}
							} else {
								falseLabel.place();
							}
						}
				}
				break;
			case OR :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						// 0 | x
						if ((left.constant != Constant.NotAConstant)
							&& (left.constant.typeID() == T_int)
							&& (left.constant.intValue() == 0)) {
							right.generateCode(currentScope, codeStream, valueRequired);
						} else {
							// x | 0
							if ((right.constant != Constant.NotAConstant)
								&& (right.constant.typeID() == T_int)
								&& (right.constant.intValue() == 0)) {
								left.generateCode(currentScope, codeStream, valueRequired);
							} else {
								left.generateCode(currentScope, codeStream, valueRequired);
								right.generateCode(currentScope, codeStream, valueRequired);
								if (valueRequired)
									codeStream.ior();
							}
						}
						break;
					case T_long :
						// 0 | x
						if ((left.constant != Constant.NotAConstant)
							&& (left.constant.typeID() == T_long)
							&& (left.constant.longValue() == 0L)) {
							right.generateCode(currentScope, codeStream, valueRequired);
						} else {
							// x | 0
							if ((right.constant != Constant.NotAConstant)
								&& (right.constant.typeID() == T_long)
								&& (right.constant.longValue() == 0L)) {
								left.generateCode(currentScope, codeStream, valueRequired);
							} else {
								left.generateCode(currentScope, codeStream, valueRequired);
								right.generateCode(currentScope, codeStream, valueRequired);
								if (valueRequired)
									codeStream.lor();
							}
						}
						break;
					case T_boolean : // logical or
						generateOptimizedLogicalOr(
							currentScope,
							codeStream,
							null,
							(falseLabel = new Label(codeStream)),
							valueRequired);
						/* improving code gen for such a case: boolean b = i < 0 || true;
						 * since the label has never been used, we have the inlined value on the stack. */
						if (falseLabel.hasForwardReferences()) {
							if (valueRequired) {
								codeStream.iconst_1();
								if ((bits & ValueForReturnMASK) != 0) {
									codeStream.ireturn();
									falseLabel.place();
									codeStream.iconst_0();
								} else {
									codeStream.goto_(endLabel = new Label(codeStream));
									codeStream.decrStackSize(1);
									falseLabel.place();
									codeStream.iconst_0();
									endLabel.place();
								}
							} else {
								falseLabel.place();
							}
						}
				}
				break;
			case XOR :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						// 0 ^ x
						if ((left.constant != Constant.NotAConstant)
							&& (left.constant.typeID() == T_int)
							&& (left.constant.intValue() == 0)) {
							right.generateCode(currentScope, codeStream, valueRequired);
						} else {
							// x ^ 0
							if ((right.constant != Constant.NotAConstant)
								&& (right.constant.typeID() == T_int)
								&& (right.constant.intValue() == 0)) {
								left.generateCode(currentScope, codeStream, valueRequired);
							} else {
								left.generateCode(currentScope, codeStream, valueRequired);
								right.generateCode(currentScope, codeStream, valueRequired);
								if (valueRequired)
									codeStream.ixor();
							}
						}
						break;
					case T_long :
						// 0 ^ x
						if ((left.constant != Constant.NotAConstant)
							&& (left.constant.typeID() == T_long)
							&& (left.constant.longValue() == 0L)) {
							right.generateCode(currentScope, codeStream, valueRequired);
						} else {
							// x ^ 0
							if ((right.constant != Constant.NotAConstant)
								&& (right.constant.typeID() == T_long)
								&& (right.constant.longValue() == 0L)) {
								left.generateCode(currentScope, codeStream, valueRequired);
							} else {
								left.generateCode(currentScope, codeStream, valueRequired);
								right.generateCode(currentScope, codeStream, valueRequired);
								if (valueRequired)
									codeStream.lxor();
							}
						}
						break;
					case T_boolean :
						generateOptimizedLogicalXor(
							currentScope,
							codeStream,
							null,
							(falseLabel = new Label(codeStream)),
							valueRequired);
						/* improving code gen for such a case: boolean b = i < 0 ^ bool;
						 * since the label has never been used, we have the inlined value on the stack. */
						if (falseLabel.hasForwardReferences()) {
							if (valueRequired) {
								codeStream.iconst_1();
								if ((bits & ValueForReturnMASK) != 0) {
									codeStream.ireturn();
									falseLabel.place();
									codeStream.iconst_0();
								} else {
									codeStream.goto_(endLabel = new Label(codeStream));
									codeStream.decrStackSize(1);
									falseLabel.place();
									codeStream.iconst_0();
									endLabel.place();
								}
							} else {
								falseLabel.place();
							}
						}
				}
				break;
			case LEFT_SHIFT :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.ishl();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.lshl();
				}
				break;
			case RIGHT_SHIFT :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.ishr();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.lshr();
				}
				break;
			case UNSIGNED_RIGHT_SHIFT :
				switch (bits & ReturnTypeIDMASK) {
					case T_int :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.iushr();
						break;
					case T_long :
						left.generateCode(currentScope, codeStream, valueRequired);
						right.generateCode(currentScope, codeStream, valueRequired);
						if (valueRequired)
							codeStream.lushr();
				}
				break;
			case GREATER :
				generateOptimizedGreaterThan(
					currentScope,
					codeStream,
					null,
					(falseLabel = new Label(codeStream)),
					valueRequired);
				if (valueRequired) {
					codeStream.iconst_1();
					if ((bits & ValueForReturnMASK) != 0) {
						codeStream.ireturn();
						falseLabel.place();
						codeStream.iconst_0();
					} else {
						codeStream.goto_(endLabel = new Label(codeStream));
						codeStream.decrStackSize(1);
						falseLabel.place();
						codeStream.iconst_0();
						endLabel.place();
					}
				}
				break;
			case GREATER_EQUAL :
				generateOptimizedGreaterThanOrEqual(
					currentScope,
					codeStream,
					null,
					(falseLabel = new Label(codeStream)),
					valueRequired);
				if (valueRequired) {
					codeStream.iconst_1();
					if ((bits & ValueForReturnMASK) != 0) {
						codeStream.ireturn();
						falseLabel.place();
						codeStream.iconst_0();
					} else {
						codeStream.goto_(endLabel = new Label(codeStream));
						codeStream.decrStackSize(1);
						falseLabel.place();
						codeStream.iconst_0();
						endLabel.place();
					}
				}
				break;
			case LESS :
				generateOptimizedLessThan(
					currentScope,
					codeStream,
					null,
					(falseLabel = new Label(codeStream)),
					valueRequired);
				if (valueRequired) {
					codeStream.iconst_1();
					if ((bits & ValueForReturnMASK) != 0) {
						codeStream.ireturn();
						falseLabel.place();
						codeStream.iconst_0();
					} else {
						codeStream.goto_(endLabel = new Label(codeStream));
						codeStream.decrStackSize(1);
						falseLabel.place();
						codeStream.iconst_0();
						endLabel.place();
					}
				}
				break;
			case LESS_EQUAL :
				generateOptimizedLessThanOrEqual(
					currentScope,
					codeStream,
					null,
					(falseLabel = new Label(codeStream)),
					valueRequired);
				if (valueRequired) {
					codeStream.iconst_1();
					if ((bits & ValueForReturnMASK) != 0) {
						codeStream.ireturn();
						falseLabel.place();
						codeStream.iconst_0();
					} else {
						codeStream.goto_(endLabel = new Label(codeStream));
						codeStream.decrStackSize(1);
						falseLabel.place();
						codeStream.iconst_0();
						endLabel.place();
					}
				}
		}
		if (valueRequired) {
			codeStream.generateImplicitConversion(implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * Boolean operator code generation
	 *	Optimized operations are: <, <=, >, >=, &, |, ^
	 */
	public void generateOptimizedBoolean(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean)) {
			super.generateOptimizedBoolean(
				currentScope,
				codeStream,
				trueLabel,
				falseLabel,
				valueRequired);
			return;
		}
		switch ((bits & OperatorMASK) >> OperatorSHIFT) {
			case LESS :
				generateOptimizedLessThan(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				return;
			case LESS_EQUAL :
				generateOptimizedLessThanOrEqual(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				return;
			case GREATER :
				generateOptimizedGreaterThan(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				return;
			case GREATER_EQUAL :
				generateOptimizedGreaterThanOrEqual(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				return;
			case AND :
				generateOptimizedLogicalAnd(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				return;
			case OR :
				generateOptimizedLogicalOr(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				return;
			case XOR :
				generateOptimizedLogicalXor(
					currentScope,
					codeStream,
					trueLabel,
					falseLabel,
					valueRequired);
				return;
		}
		super.generateOptimizedBoolean(
			currentScope,
			codeStream,
			trueLabel,
			falseLabel,
			valueRequired);
	}

	/**
	 * Boolean generation for >
	 */
	public void generateOptimizedGreaterThan(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		int pc = codeStream.position;
		int promotedTypeID = left.implicitConversion >> 4;
		// both sides got promoted in the same way
		if (promotedTypeID == T_int) {
			// 0 > x
			if ((left.constant != NotAConstant) && (left.constant.intValue() == 0)) {
				right.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.iflt(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.ifge(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
			// x > 0
			if ((right.constant != NotAConstant) && (right.constant.intValue() == 0)) {
				left.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.ifgt(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.ifle(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		// default comparison
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			if (falseLabel == null) {
				if (trueLabel != null) {
					// implicit falling through the FALSE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmpgt(trueLabel);
							break;
						case T_float :
							codeStream.fcmpl();
							codeStream.ifgt(trueLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifgt(trueLabel);
							break;
						case T_double :
							codeStream.dcmpl();
							codeStream.ifgt(trueLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				}
			} else {
				if (trueLabel == null) {
					// implicit falling through the TRUE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmple(falseLabel);
							break;
						case T_float :
							codeStream.fcmpl();
							codeStream.ifle(falseLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifle(falseLabel);
							break;
						case T_double :
							codeStream.dcmpl();
							codeStream.ifle(falseLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				} else {
					// no implicit fall through TRUE/FALSE --> should never occur
				}
			}
		}
	}

	/**
	 * Boolean generation for >=
	 */
	public void generateOptimizedGreaterThanOrEqual(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		int pc = codeStream.position;
		int promotedTypeID = left.implicitConversion >> 4;
		// both sides got promoted in the same way
		if (promotedTypeID == T_int) {
			// 0 >= x
			if ((left.constant != NotAConstant) && (left.constant.intValue() == 0)) {
				right.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.ifle(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.ifgt(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
			// x >= 0
			if ((right.constant != NotAConstant) && (right.constant.intValue() == 0)) {
				left.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.ifge(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.iflt(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		// default comparison
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			if (falseLabel == null) {
				if (trueLabel != null) {
					// implicit falling through the FALSE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmpge(trueLabel);
							break;
						case T_float :
							codeStream.fcmpl();
							codeStream.ifge(trueLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifge(trueLabel);
							break;
						case T_double :
							codeStream.dcmpl();
							codeStream.ifge(trueLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				}
			} else {
				if (trueLabel == null) {
					// implicit falling through the TRUE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmplt(falseLabel);
							break;
						case T_float :
							codeStream.fcmpl();
							codeStream.iflt(falseLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.iflt(falseLabel);
							break;
						case T_double :
							codeStream.dcmpl();
							codeStream.iflt(falseLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				} else {
					// no implicit fall through TRUE/FALSE --> should never occur
				}
			}
		}
	}

	/**
	 * Boolean generation for <
	 */
	public void generateOptimizedLessThan(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		int pc = codeStream.position;
		int promotedTypeID = left.implicitConversion >> 4;
		// both sides got promoted in the same way
		if (promotedTypeID == T_int) {
			// 0 < x
			if ((left.constant != NotAConstant) && (left.constant.intValue() == 0)) {
				right.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.ifgt(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.ifle(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
			// x < 0
			if ((right.constant != NotAConstant) && (right.constant.intValue() == 0)) {
				left.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.iflt(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.ifge(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		// default comparison
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			if (falseLabel == null) {
				if (trueLabel != null) {
					// implicit falling through the FALSE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmplt(trueLabel);
							break;
						case T_float :
							codeStream.fcmpg();
							codeStream.iflt(trueLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.iflt(trueLabel);
							break;
						case T_double :
							codeStream.dcmpg();
							codeStream.iflt(trueLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				}
			} else {
				if (trueLabel == null) {
					// implicit falling through the TRUE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmpge(falseLabel);
							break;
						case T_float :
							codeStream.fcmpg();
							codeStream.ifge(falseLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifge(falseLabel);
							break;
						case T_double :
							codeStream.dcmpg();
							codeStream.ifge(falseLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				} else {
					// no implicit fall through TRUE/FALSE --> should never occur
				}
			}
		}
	}
	
	/**
	 * Boolean generation for <=
	 */
	public void generateOptimizedLessThanOrEqual(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		int pc = codeStream.position;
		int promotedTypeID = left.implicitConversion >> 4;
		// both sides got promoted in the same way
		if (promotedTypeID == T_int) {
			// 0 <= x
			if ((left.constant != NotAConstant) && (left.constant.intValue() == 0)) {
				right.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.ifge(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.iflt(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
			// x <= 0
			if ((right.constant != NotAConstant) && (right.constant.intValue() == 0)) {
				left.generateCode(currentScope, codeStream, valueRequired);
				if (valueRequired) {
					if (falseLabel == null) {
						if (trueLabel != null) {
							// implicitly falling through the FALSE case
							codeStream.ifle(trueLabel);
						}
					} else {
						if (trueLabel == null) {
							// implicitly falling through the TRUE case
							codeStream.ifgt(falseLabel);
						} else {
							// no implicit fall through TRUE/FALSE --> should never occur
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		// default comparison
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			if (falseLabel == null) {
				if (trueLabel != null) {
					// implicit falling through the FALSE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmple(trueLabel);
							break;
						case T_float :
							codeStream.fcmpg();
							codeStream.ifle(trueLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifle(trueLabel);
							break;
						case T_double :
							codeStream.dcmpg();
							codeStream.ifle(trueLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				}
			} else {
				if (trueLabel == null) {
					// implicit falling through the TRUE case
					switch (promotedTypeID) {
						case T_int :
							codeStream.if_icmpgt(falseLabel);
							break;
						case T_float :
							codeStream.fcmpg();
							codeStream.ifgt(falseLabel);
							break;
						case T_long :
							codeStream.lcmp();
							codeStream.ifgt(falseLabel);
							break;
						case T_double :
							codeStream.dcmpg();
							codeStream.ifgt(falseLabel);
					}
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				} else {
					// no implicit fall through TRUE/FALSE --> should never occur
				}
			}
		}
	}
	
	/**
	 * Boolean generation for &
	 */
	public void generateOptimizedLogicalAnd(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {
			
		int pc = codeStream.position;
		Constant condConst;
		if ((left.implicitConversion & 0xF) == T_boolean) {
			if ((condConst = left.conditionalConstant()) != NotAConstant) {
				if (condConst.booleanValue() == true) {
					// <something equivalent to true> & x
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					if ((bits & OnlyValueRequiredMASK) != 0) {
						right.generateCode(currentScope, codeStream, valueRequired);
					} else {
						right.generateOptimizedBoolean(
							currentScope,
							codeStream,
							trueLabel,
							falseLabel,
							valueRequired);
					}
				} else {
					// <something equivalent to false> & x
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					if (valueRequired) {
						if ((bits & OnlyValueRequiredMASK) != 0) {
							codeStream.iconst_0();
						} else {
							if (falseLabel != null) {
								// implicit falling through the TRUE case
								codeStream.goto_(falseLabel);
							}
						}
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
			if ((condConst = right.conditionalConstant()) != NotAConstant) {
				if (condConst.booleanValue() == true) {
					// x & <something equivalent to true>
					if ((bits & OnlyValueRequiredMASK) != 0) {
						left.generateCode(currentScope, codeStream, valueRequired);
					} else {
						left.generateOptimizedBoolean(
							currentScope,
							codeStream,
							trueLabel,
							falseLabel,
							valueRequired);
					}
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
				} else {
					// x & <something equivalent to false>
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					if (valueRequired) {
						if ((bits & OnlyValueRequiredMASK) != 0) {
							codeStream.iconst_0();
						} else {
							if (falseLabel != null) {
								// implicit falling through the TRUE case
								codeStream.goto_(falseLabel);
							}
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
			codeStream.iand();
			if ((bits & OnlyValueRequiredMASK) == 0) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifne(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifeq(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	
	/**
	 * Boolean generation for |
	 */
	public void generateOptimizedLogicalOr(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {
			
		int pc = codeStream.position;
		Constant condConst;
		if ((left.implicitConversion & 0xF) == T_boolean) {
			if ((condConst = left.conditionalConstant()) != NotAConstant) {
				if (condConst.booleanValue() == true) {
					// <something equivalent to true> | x
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					if (valueRequired) {
						if ((bits & OnlyValueRequiredMASK) != 0) {
							codeStream.iconst_1();
						} else {
							if (trueLabel != null) {
								codeStream.goto_(trueLabel);
							}
						}
					}
				} else {
					// <something equivalent to false> | x
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					if ((bits & OnlyValueRequiredMASK) != 0) {
						right.generateCode(currentScope, codeStream, valueRequired);
					} else {
						right.generateOptimizedBoolean(
							currentScope,
							codeStream,
							trueLabel,
							falseLabel,
							valueRequired);
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
			if ((condConst = right.conditionalConstant()) != NotAConstant) {
				if (condConst.booleanValue() == true) {
					// x | <something equivalent to true>
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					if (valueRequired) {
						if ((bits & OnlyValueRequiredMASK) != 0) {
							codeStream.iconst_1();
						} else {
							if (trueLabel != null) {
								codeStream.goto_(trueLabel);
							}
						}
					}
				} else {
					// x | <something equivalent to false>
					if ((bits & OnlyValueRequiredMASK) != 0) {
						left.generateCode(currentScope, codeStream, valueRequired);
					} else {
						left.generateOptimizedBoolean(
							currentScope,
							codeStream,
							trueLabel,
							falseLabel,
							valueRequired);
					}
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		// default case
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			codeStream.ior();
			if ((bits & OnlyValueRequiredMASK) == 0) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifne(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifeq(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	
	/**
	 * Boolean generation for ^
	 */
	public void generateOptimizedLogicalXor(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {
			
		int pc = codeStream.position;
		Constant condConst;
		if ((left.implicitConversion & 0xF) == T_boolean) {
			if ((condConst = left.conditionalConstant()) != NotAConstant) {
				if (condConst.booleanValue() == true) {
					// <something equivalent to true> ^ x
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						falseLabel,
						trueLabel,
						valueRequired);
				} else {
					// <something equivalent to false> ^ x
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
					if ((bits & OnlyValueRequiredMASK) != 0) {
						right.generateCode(currentScope, codeStream, valueRequired);
					} else {
						right.generateOptimizedBoolean(
							currentScope,
							codeStream,
							trueLabel,
							falseLabel,
							valueRequired);
					}
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
			if ((condConst = right.conditionalConstant()) != NotAConstant) {
				if (condConst.booleanValue() == true) {
					// x ^ <something equivalent to true>
					left.generateOptimizedBoolean(
						currentScope,
						codeStream,
						falseLabel,
						trueLabel,
						valueRequired);
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
				} else {
					// x ^ <something equivalent to false>
					if ((bits & OnlyValueRequiredMASK) != 0) {
						left.generateCode(currentScope, codeStream, valueRequired);
					} else {
						left.generateOptimizedBoolean(
							currentScope,
							codeStream,
							trueLabel,
							falseLabel,
							valueRequired);
					}
					right.generateOptimizedBoolean(
						currentScope,
						codeStream,
						trueLabel,
						falseLabel,
						false);
				}
				codeStream.recordPositionsFrom(pc, this.sourceStart);
				return;
			}
		}
		// default case
		left.generateCode(currentScope, codeStream, valueRequired);
		right.generateCode(currentScope, codeStream, valueRequired);
		if (valueRequired) {
			codeStream.ixor();
			if ((bits & OnlyValueRequiredMASK) == 0) {
				if (falseLabel == null) {
					if (trueLabel != null) {
						// implicit falling through the FALSE case
						codeStream.ifne(trueLabel);
					}
				} else {
					// implicit falling through the TRUE case
					if (trueLabel == null) {
						codeStream.ifeq(falseLabel);
					} else {
						// no implicit fall through TRUE/FALSE --> should never occur
					}
				}
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}
	
	public void generateOptimizedStringBuffer(
		BlockScope blockScope,
		CodeStream codeStream,
		int typeID) {
			
		/* In the case trying to make a string concatenation, there is no need to create a new
		 * string buffer, thus use a lower-level API for code generation involving only the
		 * appending of arguments to the existing StringBuffer
		 */

		if ((((bits & OperatorMASK) >> OperatorSHIFT) == PLUS)
			&& ((bits & ReturnTypeIDMASK) == T_String)) {
			if (constant != NotAConstant) {
				codeStream.generateConstant(constant, implicitConversion);
				codeStream.invokeStringBufferAppendForType(implicitConversion & 0xF);
			} else {
				int pc = codeStream.position;
				left.generateOptimizedStringBuffer(
					blockScope,
					codeStream,
					left.implicitConversion & 0xF);
				codeStream.recordPositionsFrom(pc, left.sourceStart);
				pc = codeStream.position;
				right.generateOptimizedStringBuffer(
					blockScope,
					codeStream,
					right.implicitConversion & 0xF);
				codeStream.recordPositionsFrom(pc, right.sourceStart);
			}
		} else {
			super.generateOptimizedStringBuffer(blockScope, codeStream, typeID);
		}
	}
	
	public void generateOptimizedStringBufferCreation(
		BlockScope blockScope,
		CodeStream codeStream,
		int typeID) {
			
		/* In the case trying to make a string concatenation, there is no need to create a new
		 * string buffer, thus use a lower-level API for code generation involving only the 
		 * appending of arguments to the existing StringBuffer
		 */

		if ((((bits & OperatorMASK) >> OperatorSHIFT) == PLUS)
			&& ((bits & ReturnTypeIDMASK) == T_String)) {
			if (constant != NotAConstant) {
				codeStream.newStringBuffer(); // new: java.lang.StringBuffer
				codeStream.dup();
				codeStream.ldc(constant.stringValue());
				codeStream.invokeStringBufferStringConstructor();
				// invokespecial: java.lang.StringBuffer.<init>(Ljava.lang.String;)V
			} else {
				int pc = codeStream.position;
				left.generateOptimizedStringBufferCreation(
					blockScope,
					codeStream,
					left.implicitConversion & 0xF);
				codeStream.recordPositionsFrom(pc, left.sourceStart);
				pc = codeStream.position;
				right.generateOptimizedStringBuffer(
					blockScope,
					codeStream,
					right.implicitConversion & 0xF);
				codeStream.recordPositionsFrom(pc, right.sourceStart);
			}
		} else {
			super.generateOptimizedStringBufferCreation(blockScope, codeStream, typeID);
		}
	}
	
	public boolean isCompactableOperation() {
		
		return true;
	}
	
	public void optimizedBooleanConstant(int leftId, int operator, int rightId) {

		switch (operator) {
			case AND :
				if ((leftId != T_boolean) || (rightId != T_boolean))
					return;
			case AND_AND :
				Constant cst;
				if ((cst = left.conditionalConstant()) != NotAConstant) {
					if (cst.booleanValue() == false) { // left is equivalent to false
						optimizedBooleanConstant = cst; // constant(false)
						return;
					} else { //left is equivalent to true
						if ((cst = right.conditionalConstant()) != NotAConstant) {
							optimizedBooleanConstant = cst;
							// the conditional result is equivalent to the right conditional value
						}
						return;
					}
				}
				if ((cst = right.conditionalConstant()) != NotAConstant) {
					if (cst.booleanValue() == false) { // right is equivalent to false
						optimizedBooleanConstant = cst; // constant(false)
					}
				}
				return;
			case OR :
				if ((leftId != T_boolean) || (rightId != T_boolean))
					return;
			case OR_OR :
				if ((cst = left.conditionalConstant()) != NotAConstant) {
					if (cst.booleanValue() == true) { // left is equivalent to true
						optimizedBooleanConstant = cst; // constant(true)
						return;
					} else { //left is equivalent to false
						if ((cst = right.conditionalConstant()) != NotAConstant) {
							optimizedBooleanConstant = cst;
						}
						return;
					}
				}
				if ((cst = right.conditionalConstant()) != NotAConstant) {
					if (cst.booleanValue() == true) { // right is equivalent to true
						optimizedBooleanConstant = cst; // constant(true)
					}
				}
		}
	}
	
	public TypeBinding resolveType(BlockScope scope) {

		// use the id of the type to navigate into the table
		TypeBinding leftTb = left.resolveType(scope);
		TypeBinding rightTb = right.resolveType(scope);
		if (leftTb == null || rightTb == null) {
			constant = Constant.NotAConstant;
			return null;
		}
		int leftId = leftTb.id;
		int rightId = rightTb.id;
		if (leftId > 15
			|| rightId > 15) { // must convert String + Object || Object + String
			if (leftId == T_String) {
				rightId = T_Object;
			} else if (rightId == T_String) {
				leftId = T_Object;
			} else {
				constant = Constant.NotAConstant;
				scope.problemReporter().invalidOperator(this, leftTb, rightTb);
				return null;
			}
		}
		if (((bits & OperatorMASK) >> OperatorSHIFT) == PLUS) {
			if (leftId == T_String
				&& rightTb.isArrayType()
				&& ((ArrayBinding) rightTb).elementsType(scope) == CharBinding)
				scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(
					right);
			else if (
				rightId == T_String
					&& leftTb.isArrayType()
					&& ((ArrayBinding) leftTb).elementsType(scope) == CharBinding)
				scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(
					left);
		}

		// the code is an int
		// (cast)  left   Op (cast)  rigth --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8    <<4       <<0

		// Don't test for result = 0. If it is zero, some more work is done.
		// On the one hand when it is not zero (correct code) we avoid doing the test	
		int result =
			ResolveTypeTables[(bits & OperatorMASK) >> OperatorSHIFT][(leftId << 4)
				+ rightId];
		left.implicitConversion = result >>> 12;
		right.implicitConversion = (result >>> 4) & 0x000FF;

		bits |= result & 0xF;
		switch (result & 0xF) { // record the current ReturnTypeID
			// only switch on possible result type.....
			case T_boolean :
				this.typeBinding = BooleanBinding;
				break;
			case T_byte :
				this.typeBinding = ByteBinding;
				break;
			case T_char :
				this.typeBinding = CharBinding;
				break;
			case T_double :
				this.typeBinding = DoubleBinding;
				break;
			case T_float :
				this.typeBinding = FloatBinding;
				break;
			case T_int :
				this.typeBinding = IntBinding;
				break;
			case T_long :
				this.typeBinding = LongBinding;
				break;
			case T_String :
				this.typeBinding = scope.getJavaLangString();
				break;
			default : //error........
				constant = Constant.NotAConstant;
				scope.problemReporter().invalidOperator(this, leftTb, rightTb);
				return null;
		}

		// compute the constant when valid
		computeConstant(scope, leftId, rightId);
		return this.typeBinding;
	}
	
	public String toStringExpressionNoParenthesis() {

		return left.toStringExpression() + " " + //$NON-NLS-1$
		operatorToString() + " " + //$NON-NLS-1$
		right.toStringExpression();
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			left.traverse(visitor, scope);
			right.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}