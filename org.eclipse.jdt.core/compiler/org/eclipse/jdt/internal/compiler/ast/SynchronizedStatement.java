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

public class SynchronizedStatement extends Statement {

	public Expression expression;
	public Block block;
	public BlockScope scope;

	boolean blockExit;
	public LocalVariableBinding synchroVariable;
	static final char[] SecretLocalDeclarationName = " syncValue".toCharArray(); //$NON-NLS-1$

	public SynchronizedStatement(
		Expression expression,
		Block statement,
		int s,
		int e) {

		this.expression = expression;
		this.block = statement;
		sourceEnd = e;
		sourceStart = s;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// mark the synthetic variable as being used
		synchroVariable.used = true;

		// simple propagation to subnodes
		flowInfo =
			block.analyseCode(
				scope,
				new InsideSubRoutineFlowContext(flowContext, this),
				expression.analyseCode(scope, flowContext, flowInfo));

		// optimizing code gen
		if ((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable()) {
			blockExit = true;
		}
		return flowInfo;
	}

	/**
	 * Synchronized statement code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;

		// generate the synchronization expression
		expression.generateCode(scope, codeStream, true);
		if (block.isEmptyBlock()) {
			if ((synchroVariable.type == LongBinding)
				|| (synchroVariable.type == DoubleBinding)) {
				codeStream.dup2();
			} else {
				codeStream.dup();
			}
			// only take the lock
			codeStream.monitorenter();
			codeStream.monitorexit();
		} else {
			// enter the monitor
			codeStream.store(synchroVariable, true);
			codeStream.monitorenter();

			// generate  the body of the synchronized block
			ExceptionLabel anyExceptionHandler = new ExceptionLabel(codeStream, null);
			//'null' denotes any kind of exception
			block.generateCode(scope, codeStream);
			Label endLabel = new Label(codeStream);
			if (!blockExit) {
				codeStream.load(synchroVariable);
				codeStream.monitorexit();
				codeStream.goto_(endLabel);
			}
			// generate the body of the exception handler
			anyExceptionHandler.placeEnd();
			anyExceptionHandler.place();
			codeStream.incrStackSize(1);
			codeStream.load(synchroVariable);
			codeStream.monitorexit();
			codeStream.athrow();
			if (!blockExit) {
				endLabel.place();
			}
		}
		if (scope != currentScope) {
			codeStream.exitUserScope(scope);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope upperScope) {

		// special scope for secret locals optimization.
		scope = new BlockScope(upperScope);
		TypeBinding type = expression.resolveType(scope);
		if (type == null)
			return;
		switch (type.id) {
			case (T_boolean) :
			case (T_char) :
			case (T_float) :
			case (T_double) :
			case (T_byte) :
			case (T_short) :
			case (T_int) :
			case (T_long) :
				scope.problemReporter().invalidTypeToSynchronize(expression, type);
				break;
			case (T_void) :
				scope.problemReporter().illegalVoidExpression(expression);
				break;
			case (T_null) :
				scope.problemReporter().invalidNullToSynchronize(expression);
				break; 
		}
		//continue even on errors in order to have the TC done into the statements
		synchroVariable = new LocalVariableBinding(SecretLocalDeclarationName, type, AccDefault, false);
		scope.addLocalVariable(synchroVariable);
		synchroVariable.constant = NotAConstant; // not inlinable
		expression.implicitWidening(type, type);
		block.resolveUsing(scope);
	}

	public String toString(int tab) {

		String s = tabString(tab);
		s = s + "synchronized (" + expression.toStringExpression() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
		s = s + "\n" + block.toString(tab + 1); //$NON-NLS-1$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			expression.traverse(visitor, scope);
			block.traverse(visitor, scope);
		}
		visitor.endVisit(this, blockScope);
	}
}