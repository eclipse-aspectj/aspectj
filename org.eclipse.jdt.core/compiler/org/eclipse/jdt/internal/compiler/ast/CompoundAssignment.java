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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompoundAssignment extends Assignment implements OperatorIds {
	public int operator;
	public int assignmentImplicitConversion;

	//  var op exp is equivalent to var = (varType) var op exp
	// assignmentImplicitConversion stores the cast needed for the assignment

public CompoundAssignment(Expression lhs, Expression expression,int operator, int sourceEnd) {
	//lhs is always a reference by construction ,
	//but is build as an expression ==> the checkcast cannot fail

	super(lhs, expression, sourceEnd);
	this.operator = operator ;
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// record setting a variable: various scenarii are possible, setting an array reference, 
	// a field reference, a blank final field reference, a field of an enclosing instance or 
	// just a local variable.

	return lhs.analyseAssignment(currentScope, flowContext, flowInfo, this, true).unconditionalInits();
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

	// various scenarii are possible, setting an array reference, 
	// a field reference, a blank final field reference, a field of an enclosing instance or 
	// just a local variable.

	int pc = codeStream.position;
	lhs.generateCompoundAssignment(currentScope, codeStream, expression, operator, assignmentImplicitConversion, valueRequired);
	if (valueRequired) {
		codeStream.generateImplicitConversion(implicitConversion);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public String operatorToString() {
	switch (operator) {
		case PLUS :
			return "+="; //$NON-NLS-1$
		case MINUS :
			return "-="; //$NON-NLS-1$
		case MULTIPLY :
			return "*="; //$NON-NLS-1$
		case DIVIDE :
			return "/="; //$NON-NLS-1$
		case AND :
			return "&="; //$NON-NLS-1$
		case OR :
			return "|="; //$NON-NLS-1$
		case XOR :
			return "^="; //$NON-NLS-1$
		case REMAINDER :
			return "%="; //$NON-NLS-1$
		case LEFT_SHIFT :
			return "<<="; //$NON-NLS-1$
		case RIGHT_SHIFT :
			return ">>="; //$NON-NLS-1$
		case UNSIGNED_RIGHT_SHIFT :
			return ">>>="; //$NON-NLS-1$
	};
	return "unknown operator"; //$NON-NLS-1$
}
public TypeBinding resolveType(BlockScope scope) {
	constant = NotAConstant;
	TypeBinding lhsType = lhs.resolveType(scope);
	TypeBinding expressionType = expression.resolveType(scope);
	if (lhsType == null || expressionType == null)
		return null;

	int lhsId = lhsType.id;
	int expressionId = expressionType.id;
	if (restrainUsageToNumericTypes() && !lhsType.isNumericType()) {
		scope.problemReporter().operatorOnlyValidOnNumericType(this, lhsType, expressionType);
		return null;
	}
	if (lhsId > 15 || expressionId > 15) {
		if (lhsId != T_String) { // String += Object is valid wheraas Object -= String is not
			scope.problemReporter().invalidOperator(this, lhsType, expressionType);
			return null;
		}
		expressionId = T_Object; // use the Object has tag table
	}

	// the code is an int
	// (cast)  left   Op (cast)  rigth --> result 
	//  0000   0000       0000   0000      0000
	//  <<16   <<12       <<8     <<4        <<0

	// the conversion is stored INTO the reference (info needed for the code gen)
	int result = OperatorExpression.ResolveTypeTables[operator][ (lhsId << 4) + expressionId];
	if (result == T_undefined) {
		scope.problemReporter().invalidOperator(this, lhsType, expressionType);
		return null;
	}
	if (operator == PLUS){
		if(scope.isJavaLangObject(lhsType)) {
			// <Object> += <String> is illegal
			scope.problemReporter().invalidOperator(this, lhsType, expressionType);
			return null;
		} else if ((lhsType.isNumericType() || lhsId == T_boolean) && !expressionType.isNumericType()){
			// <int | boolean> += <String> is illegal
			scope.problemReporter().invalidOperator(this, lhsType, expressionType);
			return null;
		}
	}
	lhs.implicitConversion = result >>> 12;
	expression.implicitConversion = (result >>> 4) & 0x000FF;
	assignmentImplicitConversion = (lhsId << 4) + (result & 0x0000F);
	return lhsType;
}
public boolean restrainUsageToNumericTypes(){
	return false ;}
public String toStringExpressionNoParenthesis() {

	return 	lhs.toStringExpression() + " " + //$NON-NLS-1$
			operatorToString() + " " + //$NON-NLS-1$
			expression.toStringExpression() ; }
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	if (visitor.visit(this, scope)) {
		lhs.traverse(visitor, scope);
		expression.traverse(visitor, scope);
	}
	visitor.endVisit(this, scope);
}
}
