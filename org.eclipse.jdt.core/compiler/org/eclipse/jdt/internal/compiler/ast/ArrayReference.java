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

public class ArrayReference extends Reference {
	
	public Expression receiver;
	public Expression position;

	public TypeBinding arrayElementBinding;

	public ArrayReference(Expression rec, Expression pos) {
		this.receiver = rec;
		this.position = pos;
		sourceStart = rec.sourceStart;
	}

	public FlowInfo analyseAssignment(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		Assignment assignment,
		boolean compoundAssignment) {

		if (assignment.expression == null) {
			return analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		} else {
			return assignment
				.expression
				.analyseCode(
					currentScope,
					flowContext,
					analyseCode(currentScope, flowContext, flowInfo).unconditionalInits())
				.unconditionalInits();
		}
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return position.analyseCode(
			currentScope,
			flowContext,
			receiver.analyseCode(currentScope, flowContext, flowInfo));
	}

	public void generateAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Assignment assignment,
		boolean valueRequired) {

		receiver.generateCode(currentScope, codeStream, true);
		position.generateCode(currentScope, codeStream, true);
		assignment.expression.generateCode(currentScope, codeStream, true);
		codeStream.arrayAtPut(arrayElementBinding.id, valueRequired);
		if (valueRequired) {
			codeStream.generateImplicitConversion(assignment.implicitConversion);
		}
	}

	/**
	 * Code generation for a array reference
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		receiver.generateCode(currentScope, codeStream, true);
		position.generateCode(currentScope, codeStream, true);
		codeStream.arrayAt(arrayElementBinding.id);
		// Generating code for the potential runtime type checking
		if (valueRequired) {
			codeStream.generateImplicitConversion(implicitConversion);
		} else {
			if (arrayElementBinding == LongBinding
				|| arrayElementBinding == DoubleBinding) {
				codeStream.pop2();
			} else {
				codeStream.pop();
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

		receiver.generateCode(currentScope, codeStream, true);
		position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(arrayElementBinding.id);
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
		codeStream.arrayAtPut(arrayElementBinding.id, valueRequired);
	}

	public void generatePostIncrement(
		BlockScope currentScope,
		CodeStream codeStream,
		CompoundAssignment postIncrement,
		boolean valueRequired) {

		receiver.generateCode(currentScope, codeStream, true);
		position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(arrayElementBinding.id);
		if (valueRequired) {
			if ((arrayElementBinding == LongBinding)
				|| (arrayElementBinding == DoubleBinding)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		codeStream.generateConstant(
			postIncrement.expression.constant,
			implicitConversion);
		codeStream.sendOperator(postIncrement.operator, arrayElementBinding.id);
		codeStream.generateImplicitConversion(
			postIncrement.assignmentImplicitConversion);
		codeStream.arrayAtPut(arrayElementBinding.id, false);
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = Constant.NotAConstant;
		TypeBinding arrayTb = receiver.resolveType(scope);
		if (arrayTb == null)
			return null;
		if (!arrayTb.isArrayType()) {
			scope.problemReporter().referenceMustBeArrayTypeAt(arrayTb, this);
			return null;
		}
		TypeBinding positionTb = position.resolveTypeExpecting(scope, IntBinding);
		if (positionTb == null)
			return null;
		position.implicitWidening(IntBinding, positionTb);
		return arrayElementBinding = ((ArrayBinding) arrayTb).elementsType(scope);
	}

	public String toStringExpression() {

		return receiver.toStringExpression() + "[" //$NON-NLS-1$
		+position.toStringExpression() + "]"; //$NON-NLS-1$
	} 

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		
		if (visitor.visit(this, scope)) {
			receiver.traverse(visitor, scope);
			position.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}