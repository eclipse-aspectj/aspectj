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
import org.eclipse.jdt.internal.compiler.lookup.*;

public class PostfixExpression extends CompoundAssignment {

	public PostfixExpression(Expression l, Expression e, int op, int pos) {
		
		super(l, e, op, pos);
		this.sourceStart = l.sourceStart;
		this.sourceEnd = pos;
	}
	
	/**
	 * Code generation for PostfixExpression
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		// various scenarii are possible, setting an array reference, 
		// a field reference, a blank final field reference, a field of an enclosing instance or 
		// just a local variable.

		int pc = codeStream.position;
		lhs.generatePostIncrement(currentScope, codeStream, this, valueRequired);
		if (valueRequired) {
			codeStream.generateImplicitConversion(implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public String operatorToString() {
		switch (operator) {
			case PLUS :
				return "++"; //$NON-NLS-1$
			case MINUS :
				return "--"; //$NON-NLS-1$
		} 
		return "unknown operator"; //$NON-NLS-1$
	}
	
	public boolean restrainUsageToNumericTypes() {

		return true;
	}
	
	public String toStringExpressionNoParenthesis() {

		return lhs.toStringExpression() + " " + operatorToString(); //$NON-NLS-1$
	} 

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			lhs.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}