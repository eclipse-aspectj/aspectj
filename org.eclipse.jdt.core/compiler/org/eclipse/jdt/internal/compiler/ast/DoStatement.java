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

public class DoStatement extends Statement {

	public Expression condition;
	public Statement action;

	private Label breakLabel, continueLabel;

	// for local variables table attributes
	int mergedInitStateIndex = -1;

	public DoStatement(Expression condition, Statement action, int s, int e) {

		this.sourceStart = s;
		this.sourceEnd = e;
		this.condition = condition;
		this.action = action;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		breakLabel = new Label();
		continueLabel = new Label();
		LoopingFlowContext loopingContext =
			new LoopingFlowContext(
				flowContext,
				this,
				breakLabel,
				continueLabel,
				currentScope);

		Constant conditionConstant = condition.constant;
		Constant conditionalConstant = condition.conditionalConstant();
		boolean isFalseCondition =
			((conditionConstant != NotAConstant)
				&& (conditionConstant.booleanValue() == false))
				|| ((conditionalConstant != NotAConstant)
					&& (conditionalConstant.booleanValue() == false));

		if ((action != null) && !action.isEmptyBlock()) {
			flowInfo = action.analyseCode(currentScope, loopingContext, flowInfo.copy());

			// code generation can be optimized when no need to continue in the loop
			if ((flowInfo == FlowInfo.DeadEnd) || flowInfo.isFakeReachable()) {
				if ((loopingContext.initsOnContinue == FlowInfo.DeadEnd)
					|| loopingContext.initsOnContinue.isFakeReachable()) {
					continueLabel = null;
				} else {
					flowInfo = loopingContext.initsOnContinue; // for condition
					if (isFalseCondition) {
						//	continueLabel = null; - cannot nil the label since may be targeted already by 'continue' statements
					} else {
						loopingContext.complainOnFinalAssignmentsInLoop(currentScope, flowInfo);
					}
				}
			} else {
				if (isFalseCondition) {
					//	continueLabel = null; - cannot nil the label since may be targeted already by 'continue' statements
				} else {
					loopingContext.complainOnFinalAssignmentsInLoop(currentScope, flowInfo);
				}
			}
		}
		LoopingFlowContext condLoopContext;
		flowInfo =
			condition.analyseCode(
				currentScope,
				(condLoopContext =
					new LoopingFlowContext(flowContext, this, null, null, currentScope)),
				(action == null
					? flowInfo
					: (flowInfo.mergedWith(loopingContext.initsOnContinue))));
		condLoopContext.complainOnFinalAssignmentsInLoop(currentScope, flowInfo);

		// infinite loop
		FlowInfo mergedInfo;
		if ((condition.constant != NotAConstant)
			&& (condition.constant.booleanValue() == true)) {
			mergedInfo = loopingContext.initsOnBreak;
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(mergedInfo);
			return mergedInfo;
		}

		// end of loop: either condition false or break
		mergedInfo =
			flowInfo.initsWhenFalse().unconditionalInits().mergedWith(
				loopingContext.initsOnBreak);
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * Do statement code generation
	 *
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;

		// labels management
		Label actionLabel = new Label(codeStream);
		actionLabel.place();
		breakLabel.codeStream = codeStream;
		if (continueLabel != null) {
			continueLabel.codeStream = codeStream;
		}

		// generate action
		if (action != null) {
			action.generateCode(currentScope, codeStream);
		}
		// generate condition
		if (continueLabel != null) {
			continueLabel.place();
			condition.generateOptimizedBoolean(
				currentScope,
				codeStream,
				actionLabel,
				null,
				true);
		}
		breakLabel.place();

		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);

	}

	public void resetStateForCodeGeneration() {

		this.breakLabel.resetStateForCodeGeneration();
		this.continueLabel.resetStateForCodeGeneration();
	}

	public void resolve(BlockScope scope) {

		TypeBinding type = condition.resolveTypeExpecting(scope, BooleanBinding);
		condition.implicitWidening(type, type);
		if (action != null)
			action.resolve(scope);
	}

	public String toString(int tab) {

		String inFront, s = tabString(tab);
		inFront = s;
		s = s + "do"; //$NON-NLS-1$
		if (action == null)
			s = s + " {}\n"; //$NON-NLS-1$
		else if (action instanceof Block)
			s = s + "\n" + action.toString(tab + 1) + "\n"; //$NON-NLS-2$ //$NON-NLS-1$
		else
			s = s + " {\n" + action.toString(tab + 1) + ";}\n"; //$NON-NLS-1$ //$NON-NLS-2$
		s = s + inFront + "while (" + condition.toStringExpression() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (action != null) {
				action.traverse(visitor, scope);
			}
			condition.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}