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

public class ArrayInitializer extends Expression {
	public Expression[] expressions;
	public ArrayBinding binding; //the type of the { , , , }

/**
 * ArrayInitializer constructor comment.
 */
public ArrayInitializer() {
	super();
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	if (expressions != null) {
		for (int i = 0, max = expressions.length; i < max; i++) {
			flowInfo = expressions[i].analyseCode(currentScope, flowContext, flowInfo).unconditionalInits();
		}
	}
	return flowInfo;
}
/**
 * Code generation for a array initializer
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	// Flatten the values and compute the dimensions, by iterating in depth into nested array initializers

	int pc = codeStream.position;
	int expressionLength = (expressions == null) ? 0: expressions.length;
	codeStream.generateInlinedValue(expressionLength);
	codeStream.newArray(currentScope, binding);
	if (expressions != null) {
		// binding is an ArrayType, so I can just deal with the dimension
		int elementsTypeID = binding.dimensions > 1 ? -1 : binding.leafComponentType.id;
		for (int i = 0; i < expressionLength; i++) {
			Expression expr;
			if ((expr = expressions[i]).constant != NotAConstant) {
				switch (elementsTypeID) { // filter out initializations to default values
					case T_int :
					case T_short :
					case T_byte :
					case T_char :
					case T_float :
					case T_long :
					case T_double :
						if (expr.constant.doubleValue() != 0) {
							codeStream.dup();
							codeStream.generateInlinedValue(i);
							expr.generateCode(currentScope, codeStream, true);
							codeStream.arrayAtPut(elementsTypeID, false);
						}
						break;
					case T_boolean :
						if (expr.constant.booleanValue() != false) {
							codeStream.dup();
							codeStream.generateInlinedValue(i);
							expr.generateCode(currentScope, codeStream, true);
							codeStream.arrayAtPut(elementsTypeID, false);
						}
						break;
					default :
						if (expr.constant != NullConstant.Default) {
							codeStream.dup();
							codeStream.generateInlinedValue(i);
							expr.generateCode(currentScope, codeStream, true);
							codeStream.arrayAtPut(elementsTypeID, false);
						}
				}
			} else {
				codeStream.dup();
				codeStream.generateInlinedValue(i);
				expr.generateCode(currentScope, codeStream, true);
				codeStream.arrayAtPut(elementsTypeID, false);
			}
		}
	}
	if (!valueRequired) {
		codeStream.pop();
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public TypeBinding resolveTypeExpecting(BlockScope scope, TypeBinding expectedTb) {
	// Array initializers can only occur on the right hand side of an assignment
	// expression, therefore the expected type contains the valid information
	// concerning the type that must be enforced by the elements of the array initializer.

	// this method is recursive... (the test on isArrayType is the stop case)

	constant = NotAConstant;
	if (expectedTb.isArrayType()) {
		binding = (ArrayBinding) expectedTb;
		if (expressions == null)
			return binding;
		TypeBinding expectedElementsTb = binding.elementsType(scope);
		if (expectedElementsTb.isBaseType()) {
			for (int i = 0, length = expressions.length; i < length; i++) {
				Expression expression = expressions[i];
				TypeBinding expressionTb =
					(expression instanceof ArrayInitializer)
						? expression.resolveTypeExpecting(scope, expectedElementsTb)
						: expression.resolveType(scope);
				if (expressionTb == null)
					return null;

				// Compile-time conversion required?
				if (expression.isConstantValueOfTypeAssignableToType(expressionTb, expectedElementsTb)) {
					expression.implicitWidening(expectedElementsTb, expressionTb);
				} else if (BaseTypeBinding.isWidening(expectedElementsTb.id, expressionTb.id)) {
					expression.implicitWidening(expectedElementsTb, expressionTb);
				} else {
					scope.problemReporter().typeMismatchErrorActualTypeExpectedType(expression, expressionTb, expectedElementsTb);
					return null;
				}
			}
		} else {
			for (int i = 0, length = expressions.length; i < length; i++)
				if (expressions[i].resolveTypeExpecting(scope, expectedElementsTb) == null)
					return null;
		}
		return binding;
	}
	
	// infer initializer type for error reporting based on first element
	TypeBinding leafElementType = null;
	int dim = 1;
	if (expressions == null) {
		leafElementType = scope.getJavaLangObject();
	} else {
		Expression currentExpression = expressions[0];
		while(currentExpression != null && currentExpression instanceof ArrayInitializer) {
			dim++;
			Expression[] subExprs = ((ArrayInitializer) currentExpression).expressions;
			if (subExprs == null){
				leafElementType = scope.getJavaLangObject();
				currentExpression = null;
				break;
			}
			currentExpression = ((ArrayInitializer) currentExpression).expressions[0];
		}
		if (currentExpression != null) {
			leafElementType = currentExpression.resolveType(scope);
		}
	}
	if (leafElementType != null) {
		TypeBinding probableTb = scope.createArray(leafElementType, dim);
		scope.problemReporter().typeMismatchErrorActualTypeExpectedType(this, probableTb, expectedTb);
	}
	return null;
}
public String toStringExpression() {

	String s = "{" ; //$NON-NLS-1$
	if (expressions != null)
	{ 	int j = 20 ; 
		for (int i = 0 ; i < expressions.length ; i++)
		{	s = s + expressions[i].toStringExpression() + "," ; //$NON-NLS-1$
			j -- ;
			if (j == 0)
			{	s = s + "\n                "; j = 20;}}}; //$NON-NLS-1$
	s = s + "}"; //$NON-NLS-1$
	return s;}

public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		if (expressions != null) {
			int expressionsLength = expressions.length;
			for (int i = 0; i < expressionsLength; i++)
				expressions[i].traverse(visitor, scope);
		}
	}
	visitor.endVisit(this, scope);
}
}
