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

public class IfStatement extends Statement {
	
	//this class represents the case of only one statement in 
	//either else and/or then branches.

	public Expression condition;
	public Statement thenStatement;
	public Statement elseStatement;

	boolean thenExit;

	// for local variables table attributes
	int thenInitStateIndex = -1;
	int elseInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public IfStatement(
		Expression condition,
		Statement thenStatement,
		int s,
		int e) {

		this.condition = condition;
		this.thenStatement = thenStatement;
		sourceStart = s;
		sourceEnd = e;
	}

	public IfStatement(
		Expression condition,
		Statement thenStatement,
		Statement elseStatement,
		int s,
		int e) {

		this.condition = condition;
		this.thenStatement = thenStatement;
		this.elseStatement = elseStatement;
		sourceEnd = e;
		sourceStart = s;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		FlowInfo thenFlowInfo, elseFlowInfo;

		// process the condition
		flowInfo = condition.analyseCode(currentScope, flowContext, flowInfo);

		// process the THEN part
		if (thenStatement == null) {
			thenFlowInfo = flowInfo.initsWhenTrue();
		} else {
			Constant cst;
			thenFlowInfo =
				((((cst = condition.constant) != NotAConstant)
					&& (cst.booleanValue() == false))
					|| (((cst = condition.conditionalConstant()) != NotAConstant)
						&& (cst.booleanValue() == false)))
					? (flowInfo.initsWhenTrue().copy().markAsFakeReachable(true))
					: flowInfo.initsWhenTrue().copy();
			// Save info for code gen
			thenInitStateIndex =
				currentScope.methodScope().recordInitializationStates(thenFlowInfo);
			if (!thenFlowInfo.complainIfUnreachable(thenStatement, currentScope)) {
				thenFlowInfo =
					thenStatement.analyseCode(currentScope, flowContext, thenFlowInfo);
			}
		};
		// optimizing the jump around the ELSE part
		thenExit = (thenFlowInfo == FlowInfo.DeadEnd) || thenFlowInfo.isFakeReachable();

		// process the ELSE part
		if (elseStatement == null) {
			elseFlowInfo = flowInfo.initsWhenFalse();
		} else {
			Constant cst;
			elseFlowInfo =
				((((cst = condition.constant) != NotAConstant) && (cst.booleanValue() == true))
					|| (((cst = condition.conditionalConstant()) != NotAConstant)
						&& (cst.booleanValue() == true)))
					? (flowInfo.initsWhenFalse().copy().markAsFakeReachable(true))
					: flowInfo.initsWhenFalse().copy();
			// Save info for code gen
			elseInitStateIndex =
				currentScope.methodScope().recordInitializationStates(elseFlowInfo);
			if (!elseFlowInfo.complainIfUnreachable(elseStatement, currentScope)) {
				elseFlowInfo =
					elseStatement.analyseCode(currentScope, flowContext, elseFlowInfo);
			}
		}

		// merge THEN & ELSE initializations
		FlowInfo mergedInfo;
		if ((condition.constant != NotAConstant)
			&& (condition.constant.booleanValue() == true)) {
			// IF (TRUE)
			if (thenExit) {
				mergedInfo = elseFlowInfo.markAsFakeReachable(true);
				mergedInitStateIndex =
					currentScope.methodScope().recordInitializationStates(mergedInfo);
				return mergedInfo;
			} else {
				mergedInitStateIndex =
					currentScope.methodScope().recordInitializationStates(thenFlowInfo);
				return thenFlowInfo;
			}
		} else {
			// IF (FALSE)
			if ((condition.constant != NotAConstant)
				&& (condition.constant.booleanValue() == false)) {
				if (elseFlowInfo.isDeadEnd()) {
					mergedInfo = thenFlowInfo.markAsFakeReachable(true);
					mergedInitStateIndex =
						currentScope.methodScope().recordInitializationStates(mergedInfo);
					return mergedInfo;
				} else {
					mergedInitStateIndex =
						currentScope.methodScope().recordInitializationStates(elseFlowInfo);
					return elseFlowInfo;
				}
			}
		}
		mergedInfo = thenFlowInfo.mergedWith(elseFlowInfo.unconditionalInits());
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * If code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		Label endifLabel = new Label(codeStream);

		// optimizing the then/else part code gen
		Constant cst, condCst;
		boolean hasThenPart =
			!((((cst = condition.constant) != NotAConstant)
				&& (cst.booleanValue() == false))
				|| (thenStatement == null)
				|| (thenStatement.isEmptyBlock())
				|| (((condCst = condition.conditionalConstant()) != NotAConstant)
					&& (condCst.booleanValue() == false)));
		boolean hasElsePart =
			!(((cst != NotAConstant) && (cst.booleanValue() == true))
				|| (elseStatement == null)
				|| (elseStatement.isEmptyBlock())
				|| (((condCst = condition.conditionalConstant()) != NotAConstant)
					&& (condCst.booleanValue() == true)));

		if (hasThenPart) {
			Label falseLabel;
			// generate boolean condition
			condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				null,
				(falseLabel = new Label(codeStream)),
				true);
			// May loose some local variable initializations : affecting the local variable attributes
			if (thenInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					thenInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, thenInitStateIndex);
			}
			// generate then statement
			thenStatement.generateCode(currentScope, codeStream);
			// jump around the else statement
			if (hasElsePart && !thenExit) {
				thenStatement.branchChainTo(endifLabel);
				int position = codeStream.position;
				codeStream.goto_(endifLabel);
				codeStream.updateLastRecordedEndPC(position);
				//goto is tagged as part of the thenAction block
			}
			falseLabel.place();
		} else {
			if (hasElsePart) {
				// generate boolean condition
				condition.generateOptimizedBoolean(
					currentScope,
					codeStream,
					endifLabel,
					null,
					true);
			} else {
				// generate condition side-effects
				condition.generateCode(currentScope, codeStream, false);
				codeStream.recordPositionsFrom(pc, this.sourceStart);
			}
		}
		// generate else statement
		if (hasElsePart) {
			// May loose some local variable initializations : affecting the local variable attributes
			if (elseInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					elseInitStateIndex);
				codeStream.addDefinitelyAssignedVariables(currentScope, elseInitStateIndex);
			}
			elseStatement.generateCode(currentScope, codeStream);
		}
		endifLabel.place();
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void resolve(BlockScope scope) {

		TypeBinding type = condition.resolveTypeExpecting(scope, BooleanBinding);
		condition.implicitWidening(type, type);
		if (thenStatement != null)
			thenStatement.resolve(scope);
		if (elseStatement != null)
			elseStatement.resolve(scope);
	}

	public String toString(int tab) {

		String inFront, s = tabString(tab);
		inFront = s;
		s = s + "if (" + condition.toStringExpression() + ") \n";	//$NON-NLS-1$ //$NON-NLS-2$
		s = s + thenStatement.toString(tab + 2) + ";"; //$NON-NLS-1$
		if (elseStatement != null)
			s = s + "\n" + inFront + "else\n" + elseStatement.toString(tab + 2) + ";"; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			condition.traverse(visitor, blockScope);
			if (thenStatement != null)
				thenStatement.traverse(visitor, blockScope);
			if (elseStatement != null)
				elseStatement.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}