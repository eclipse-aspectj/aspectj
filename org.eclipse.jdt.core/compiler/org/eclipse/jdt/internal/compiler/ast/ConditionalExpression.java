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

public class ConditionalExpression extends OperatorExpression {

	public Expression condition, valueIfTrue, valueIfFalse;
	private int returnTypeSlotSize = 1;

	// for local variables table attributes
	int thenInitStateIndex = -1;
	int elseInitStateIndex = -1;
	int mergedInitStateIndex = -1;
	
	public ConditionalExpression(
		Expression condition,
		Expression valueIfTrue,
		Expression valueIfFalse) {
		this.condition = condition;
		this.valueIfTrue = valueIfTrue;
		this.valueIfFalse = valueIfFalse;
		sourceStart = condition.sourceStart;
		sourceEnd = valueIfFalse.sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		Constant conditionConstant = condition.conditionalConstant();

		flowInfo = condition.analyseCode(currentScope, flowContext, flowInfo, conditionConstant == NotAConstant);

		if (conditionConstant != NotAConstant) {
			if (conditionConstant.booleanValue() == true) {
				// TRUE ? left : right
				FlowInfo resultInfo =
					valueIfTrue.analyseCode(currentScope, flowContext, flowInfo.initsWhenTrue().unconditionalInits());
				// analyse valueIfFalse, but do not take into account any of its infos
				valueIfFalse.analyseCode(
					currentScope,
					flowContext,
					flowInfo.initsWhenFalse().copy().unconditionalInits().markAsFakeReachable(true));
				mergedInitStateIndex =
					currentScope.methodScope().recordInitializationStates(resultInfo);
				return resultInfo;
			} else {
				// FALSE ? left : right
				// analyse valueIfTrue, but do not take into account any of its infos			
				valueIfTrue.analyseCode(
					currentScope,
					flowContext,
					flowInfo.initsWhenTrue().copy().unconditionalInits().markAsFakeReachable(true));
				FlowInfo mergeInfo =
					valueIfFalse.analyseCode(currentScope, flowContext, flowInfo.initsWhenFalse().unconditionalInits());
				mergedInitStateIndex =
					currentScope.methodScope().recordInitializationStates(mergeInfo);
				return mergeInfo;
			}
		}

		// store a copy of the merged info, so as to compute the local variable attributes afterwards
		FlowInfo trueInfo = flowInfo.initsWhenTrue();
		thenInitStateIndex =
			currentScope.methodScope().recordInitializationStates(trueInfo);
		FlowInfo falseInfo = flowInfo.initsWhenFalse();
		elseInitStateIndex =
			currentScope.methodScope().recordInitializationStates(falseInfo);

		// propagate analysis
		trueInfo = valueIfTrue.analyseCode(currentScope, flowContext, trueInfo.copy());
		falseInfo =
			valueIfFalse.analyseCode(currentScope, flowContext, falseInfo.copy());

		// merge back using a conditional info -  1GK2BLM
		// if ((t && (v = t)) ? t : t && (v = f)) r = v;  -- ok
		FlowInfo mergedInfo =
			FlowInfo.conditional(
				trueInfo.initsWhenTrue().copy().unconditionalInits().mergedWith( // must copy, since could be shared with trueInfo.initsWhenFalse()...
					falseInfo.initsWhenTrue().copy().unconditionalInits()),
				trueInfo.initsWhenFalse().unconditionalInits().mergedWith(
					falseInfo.initsWhenFalse().unconditionalInits()));
		/*			
			FlowInfo mergedInfo = valueIfTrue.analyseCode(
				currentScope,
				flowContext,
				flowInfo.initsWhenTrue().copy()).
					unconditionalInits().
						mergedWith(
							valueIfFalse.analyseCode(
								currentScope,
								flowContext,
								flowInfo.initsWhenFalse().copy()).
									unconditionalInits());
		*/
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * Code generation for the conditional operator ?:
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
		Label endifLabel, falseLabel;
		if (constant != NotAConstant) {
			if (valueRequired)
				codeStream.generateConstant(constant, implicitConversion);
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		Constant cst = condition.constant;
		Constant condCst = condition.conditionalConstant();
		boolean needTruePart =
			!(((cst != NotAConstant) && (cst.booleanValue() == false))
				|| ((condCst != NotAConstant) && (condCst.booleanValue() == false)));
		boolean needFalsePart =
			!(((cst != NotAConstant) && (cst.booleanValue() == true))
				|| ((condCst != NotAConstant) && (condCst.booleanValue() == true)));
		endifLabel = new Label(codeStream);

		// Generate code for the condition
		boolean needConditionValue = (cst == NotAConstant) && (condCst == NotAConstant);
		condition.generateOptimizedBoolean(
			currentScope,
			codeStream,
			null,
			(falseLabel = new Label(codeStream)),
			needConditionValue);

		if (thenInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				thenInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, thenInitStateIndex);
		}
		// Then code generation
		if (needTruePart) {
			valueIfTrue.generateCode(currentScope, codeStream, valueRequired);
			if (needFalsePart) {
				// Jump over the else part
				int position = codeStream.position;
				codeStream.goto_(endifLabel);
				codeStream.updateLastRecordedEndPC(position);
				// Tune codestream stack size
				if (valueRequired) {
					codeStream.decrStackSize(returnTypeSlotSize);
				}
			}
		}
		if (needFalsePart) {
			falseLabel.place();
			if (elseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					elseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, elseInitStateIndex);
			}
			valueIfFalse.generateCode(currentScope, codeStream, valueRequired);
			// End of if statement
			endifLabel.place();
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
		// implicit conversion
		if (valueRequired)
			codeStream.generateImplicitConversion(implicitConversion);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	/**
	 * Optimized boolean code generation for the conditional operator ?:
	*/
	public void generateOptimizedBoolean(
		BlockScope currentScope,
		CodeStream codeStream,
		Label trueLabel,
		Label falseLabel,
		boolean valueRequired) {

		if ((constant != Constant.NotAConstant) && (constant.typeID() == T_boolean) // constant
			|| (valueIfTrue.implicitConversion >> 4) != T_boolean) { // non boolean values
			super.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			return;
		}
		int pc = codeStream.position;
		Constant cst = condition.constant;
		Constant condCst = condition.conditionalConstant();
		boolean needTruePart =
			!(((cst != NotAConstant) && (cst.booleanValue() == false))
				|| ((condCst != NotAConstant) && (condCst.booleanValue() == false)));
		boolean needFalsePart =
			!(((cst != NotAConstant) && (cst.booleanValue() == true))
				|| ((condCst != NotAConstant) && (condCst.booleanValue() == true)));

		Label internalFalseLabel, endifLabel = new Label(codeStream);

		// Generate code for the condition
		boolean needConditionValue = (cst == NotAConstant) && (condCst == NotAConstant);
		condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				null,
				internalFalseLabel = new Label(codeStream),
				needConditionValue);

		if (thenInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				thenInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, thenInitStateIndex);
		}
		// Then code generation
		if (needTruePart) {
			valueIfTrue.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);
			
			if (needFalsePart) {
				// Jump over the else part
				int position = codeStream.position;
				codeStream.goto_(endifLabel);
				codeStream.updateLastRecordedEndPC(position);
				// Tune codestream stack size
				//if (valueRequired) {
				//	codeStream.decrStackSize(returnTypeSlotSize);
				//}
			}
		}
		if (needFalsePart) {
			internalFalseLabel.place();
			if (elseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					elseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, elseInitStateIndex);
			}
			valueIfFalse.generateOptimizedBoolean(currentScope, codeStream, trueLabel, falseLabel, valueRequired);

			// End of if statement
			endifLabel.place();
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
		// no implicit conversion for boolean values
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding resolveType(BlockScope scope) {
		// specs p.368
		constant = NotAConstant;
		TypeBinding conditionType = condition.resolveTypeExpecting(scope, BooleanBinding);
		TypeBinding valueIfTrueType = valueIfTrue.resolveType(scope);
		TypeBinding valueIfFalseType = valueIfFalse.resolveType(scope);
		if (conditionType == null || valueIfTrueType == null || valueIfFalseType == null)
			return null;

		// Propagate the constant value from the valueIfTrue and valueIFFalse expression if it is possible
		if (condition.constant != NotAConstant
			&& valueIfTrue.constant != NotAConstant
			&& valueIfFalse.constant != NotAConstant) {
			// all terms are constant expression so we can propagate the constant
			// from valueIFTrue or valueIfFalse to teh receiver constant
			constant =
				(condition.constant.booleanValue())
					? valueIfTrue.constant
					: valueIfFalse.constant;
		}
		if (valueIfTrueType == valueIfFalseType) { // harmed the implicit conversion 
			valueIfTrue.implicitWidening(valueIfTrueType, valueIfTrueType);
			valueIfFalse.implicitConversion = valueIfTrue.implicitConversion;
			if (valueIfTrueType == LongBinding || valueIfTrueType == DoubleBinding) {
				returnTypeSlotSize = 2;
			}
			this.typeBinding = valueIfTrueType;
			return valueIfTrueType;
		}
		// Determine the return type depending on argument types
		// Numeric types
		if (valueIfTrueType.isNumericType() && valueIfFalseType.isNumericType()) {
			// (Short x Byte) or (Byte x Short)"
			if ((valueIfTrueType == ByteBinding && valueIfFalseType == ShortBinding)
				|| (valueIfTrueType == ShortBinding && valueIfFalseType == ByteBinding)) {
				valueIfTrue.implicitWidening(ShortBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(ShortBinding, valueIfFalseType);
				this.typeBinding = ShortBinding;
				return ShortBinding;
			}
			// <Byte|Short|Char> x constant(Int)  ---> <Byte|Short|Char>   and reciprocally
			if ((valueIfTrueType == ByteBinding || valueIfTrueType == ShortBinding || valueIfTrueType == CharBinding)
				&& (valueIfFalseType == IntBinding
					&& valueIfFalse.isConstantValueOfTypeAssignableToType(valueIfFalseType, valueIfTrueType))) {
				valueIfTrue.implicitWidening(valueIfTrueType, valueIfTrueType);
				valueIfFalse.implicitWidening(valueIfTrueType, valueIfFalseType);
				this.typeBinding = valueIfTrueType;
				return valueIfTrueType;
			}
			if ((valueIfFalseType == ByteBinding
				|| valueIfFalseType == ShortBinding
				|| valueIfFalseType == CharBinding)
				&& (valueIfTrueType == IntBinding
					&& valueIfTrue.isConstantValueOfTypeAssignableToType(valueIfTrueType, valueIfFalseType))) {
				valueIfTrue.implicitWidening(valueIfFalseType, valueIfTrueType);
				valueIfFalse.implicitWidening(valueIfFalseType, valueIfFalseType);
				this.typeBinding = valueIfFalseType;
				return valueIfFalseType;
			}
			// Manual binary numeric promotion
			// int
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_int)
				&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_int)) {
				valueIfTrue.implicitWidening(IntBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(IntBinding, valueIfFalseType);
				this.typeBinding = IntBinding;
				return IntBinding;
			}
			// long
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_long)
				&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_long)) {
				valueIfTrue.implicitWidening(LongBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(LongBinding, valueIfFalseType);
				returnTypeSlotSize = 2;
				this.typeBinding = LongBinding;
				return LongBinding;
			}
			// float
			if (BaseTypeBinding.isNarrowing(valueIfTrueType.id, T_float)
				&& BaseTypeBinding.isNarrowing(valueIfFalseType.id, T_float)) {
				valueIfTrue.implicitWidening(FloatBinding, valueIfTrueType);
				valueIfFalse.implicitWidening(FloatBinding, valueIfFalseType);
				this.typeBinding = FloatBinding;
				return FloatBinding;
			}
			// double
			valueIfTrue.implicitWidening(DoubleBinding, valueIfTrueType);
			valueIfFalse.implicitWidening(DoubleBinding, valueIfFalseType);
			returnTypeSlotSize = 2;
			this.typeBinding = DoubleBinding;
			return DoubleBinding;
		}
		// Type references (null null is already tested)
		if ((valueIfTrueType.isBaseType() && valueIfTrueType != NullBinding)
			|| (valueIfFalseType.isBaseType() && valueIfFalseType != NullBinding)) {
			scope.problemReporter().conditionalArgumentsIncompatibleTypes(
				this,
				valueIfTrueType,
				valueIfFalseType);
			return null;
		}
		if (scope.areTypesCompatible(valueIfFalseType, valueIfTrueType)) {
			valueIfTrue.implicitWidening(valueIfTrueType, valueIfTrueType);
			valueIfFalse.implicitWidening(valueIfTrueType, valueIfFalseType);
			this.typeBinding = valueIfTrueType;
			return valueIfTrueType;
		}
		if (scope.areTypesCompatible(valueIfTrueType, valueIfFalseType)) {
			valueIfTrue.implicitWidening(valueIfFalseType, valueIfTrueType);
			valueIfFalse.implicitWidening(valueIfFalseType, valueIfFalseType);
			this.typeBinding = valueIfFalseType;
			return valueIfFalseType;
		}
		scope.problemReporter().conditionalArgumentsIncompatibleTypes(
			this,
			valueIfTrueType,
			valueIfFalseType);
		return null;
	}
	
	public String toStringExpressionNoParenthesis() {
		return condition.toStringExpression() + " ? " + //$NON-NLS-1$
		valueIfTrue.toStringExpression() + " : " + //$NON-NLS-1$
		valueIfFalse.toStringExpression();
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			condition.traverse(visitor, scope);
			valueIfTrue.traverse(visitor, scope);
			valueIfFalse.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}