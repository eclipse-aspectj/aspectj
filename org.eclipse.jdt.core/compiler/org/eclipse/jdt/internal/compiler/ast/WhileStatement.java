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

public class WhileStatement extends Statement {
	
	public Expression condition;
	public Statement action;
	private Label breakLabel, continueLabel;
	int preCondInitStateIndex = -1;
	int condIfTrueInitStateIndex = -1;
	int mergedInitStateIndex = -1;

	public WhileStatement(Expression condition, Statement action, int s, int e) {

		this.condition = condition;
		this.action = action;
		sourceStart = s;
		sourceEnd = e;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		breakLabel = new Label();
		continueLabel = new Label();

		preCondInitStateIndex =
			currentScope.methodScope().recordInitializationStates(flowInfo);
		LoopingFlowContext condLoopContext;
		FlowInfo postCondInfo =
			condition.analyseCode(
				currentScope,
				(condLoopContext =
					new LoopingFlowContext(flowContext, this, null, null, currentScope)),
				flowInfo);

		LoopingFlowContext loopingContext;
		if ((action == null) || action.isEmptyBlock()) {
			condLoopContext.complainOnFinalAssignmentsInLoop(currentScope, postCondInfo);
			if ((condition.constant != NotAConstant)
				&& (condition.constant.booleanValue() == true)) {
				return FlowInfo.DeadEnd;
			} else {
				FlowInfo mergedInfo = postCondInfo.initsWhenFalse().unconditionalInits();
				mergedInitStateIndex =
					currentScope.methodScope().recordInitializationStates(mergedInfo);
				return mergedInfo;
			}
		} else {
			// in case the condition was inlined to false, record the fact that there is no way to reach any 
			// statement inside the looping action
			loopingContext =
				new LoopingFlowContext(
					flowContext,
					this,
					breakLabel,
					continueLabel,
					currentScope);
			FlowInfo actionInfo =
				((condition.constant != Constant.NotAConstant)
					&& (condition.constant.booleanValue() == false))
					? FlowInfo.DeadEnd
					: postCondInfo.initsWhenTrue().copy();

			// for computing local var attributes
			condIfTrueInitStateIndex =
				currentScope.methodScope().recordInitializationStates(
					postCondInfo.initsWhenTrue());

			if (!actionInfo.complainIfUnreachable(action, currentScope)) {
				actionInfo = action.analyseCode(currentScope, loopingContext, actionInfo);
			}

			// code generation can be optimized when no need to continue in the loop
			if (((actionInfo == FlowInfo.DeadEnd) || actionInfo.isFakeReachable())
				&& ((loopingContext.initsOnContinue == FlowInfo.DeadEnd)
					|| loopingContext.initsOnContinue.isFakeReachable())) {
				continueLabel = null;
			} else {
				condLoopContext.complainOnFinalAssignmentsInLoop(currentScope, postCondInfo);
				loopingContext.complainOnFinalAssignmentsInLoop(currentScope, actionInfo);
			}
		}

		// infinite loop
		FlowInfo mergedInfo;
		if ((condition.constant != Constant.NotAConstant)
			&& (condition.constant.booleanValue() == true)) {
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(
					mergedInfo = loopingContext.initsOnBreak);
			return mergedInfo;
		}

		// end of loop: either condition false or break
		mergedInfo =
			postCondInfo.initsWhenFalse().unconditionalInits().mergedWith(
				loopingContext.initsOnBreak);
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}

	/**
	 * While code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		breakLabel.codeStream = codeStream;

		// generate condition
		if (continueLabel == null) {
			// no need to reverse condition
			if (condition.constant == NotAConstant) {
				condition.generateOptimizedBoolean(
					currentScope,
					codeStream,
					null,
					breakLabel,
					true);
			}
		} else {
			continueLabel.codeStream = codeStream;
			if (!(((condition.constant != NotAConstant)
				&& (condition.constant.booleanValue() == true))
				|| (action == null)
				|| action.isEmptyBlock())) {
				int jumpPC = codeStream.position;
				codeStream.goto_(continueLabel);
				codeStream.recordPositionsFrom(jumpPC, condition.sourceStart);
			}
		}
		// generate the action
		Label actionLabel;
		(actionLabel = new Label(codeStream)).place();
		if (action != null) {
			// Required to fix 1PR0XVS: LFRE:WINNT - Compiler: variable table for method appears incorrect
			if (condIfTrueInitStateIndex != -1) {
				// insert all locals initialized inside the condition into the action generated prior to the condition
				codeStream.addDefinitelyAssignedVariables(
					currentScope,
					condIfTrueInitStateIndex);
			}
			action.generateCode(currentScope, codeStream);
			// May loose some local variable initializations : affecting the local variable attributes
			if (preCondInitStateIndex != -1) {
				codeStream.removeNotDefinitelyAssignedVariables(
					currentScope,
					preCondInitStateIndex);
			}

		}
		// output condition and branch back to the beginning of the repeated action
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

		String s = tabString(tab);
		s = s + "while (" + condition.toStringExpression() + ")"; 	//$NON-NLS-1$ //$NON-NLS-2$
		if (action == null)
			s = s + " {} ;"; //$NON-NLS-1$ 
		else if (action instanceof Block)
			s = s + "\n" + action.toString(tab + 1); //$NON-NLS-1$
		else
			s = s + " {\n" + action.toString(tab + 1) + "}"; //$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			condition.traverse(visitor, blockScope);
			if (action != null)
				action.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}