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
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ThrowStatement extends Statement {
	public Expression exception;
	public TypeBinding exceptionType;

	public ThrowStatement(Expression exception, int startPosition) {
		this.exception = exception;
		this.sourceStart = startPosition;
		this.sourceEnd = exception.sourceEnd;
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {

		exception.analyseCode(currentScope, flowContext, flowInfo);
		// need to check that exception thrown is actually caught somewhere
		flowContext.checkExceptionHandlers(exceptionType, this, flowInfo, currentScope);
		return FlowInfo.DeadEnd;
	}

	/**
	 * Throw code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0)
			return;
		int pc = codeStream.position;
		exception.generateCode(currentScope, codeStream, true);
		codeStream.athrow();
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope scope) {
		
		exceptionType = exception.resolveTypeExpecting(scope, scope.getJavaLangThrowable());
		
		if (exceptionType == NullBinding
				&& scope.environment().options.complianceLevel <= CompilerOptions.JDK1_3){
			// if compliant with 1.4, this problem will not be reported
			scope.problemReporter().cannotThrowNull(this);
	 	}
		exception.implicitWidening(exceptionType, exceptionType);
	}

	public String toString(int tab) {
		String s = tabString(tab);
		s = s + "throw "; //$NON-NLS-1$
		s = s + exception.toStringExpression();
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope))
			exception.traverse(visitor, blockScope);
		visitor.endVisit(this, blockScope);
	}
}