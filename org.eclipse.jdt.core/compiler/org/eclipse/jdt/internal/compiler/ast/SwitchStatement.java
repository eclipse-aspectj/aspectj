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

public class SwitchStatement extends Statement {
	public Expression testExpression;
	public Statement[] statements;
	public BlockScope scope;
	public int explicitDeclarations;
	public Label breakLabel;
	public Case[] cases;
	public DefaultCase defaultCase;
	public int caseCount = 0;

	// for local variables table attributes
	int preSwitchInitStateIndex = -1;
	int mergedInitStateIndex = -1;
	/**
	 * SwitchStatement constructor comment.
	 */
	public SwitchStatement() {
		super();
	}
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
		flowInfo = testExpression.analyseCode(currentScope, flowContext, flowInfo);
		SwitchFlowContext switchContext =
			new SwitchFlowContext(flowContext, this, (breakLabel = new Label()));

		// analyse the block by considering specially the case/default statements (need to bind them 
		// to the entry point)
		FlowInfo caseInits = FlowInfo.DeadEnd;
		// in case of statements before the first case
		preSwitchInitStateIndex =
			currentScope.methodScope().recordInitializationStates(flowInfo);
		int caseIndex = 0;
		if (statements != null) {
			for (int i = 0, max = statements.length; i < max; i++) {
				Statement statement = statements[i];
				if ((caseIndex < caseCount)
					&& (statement == cases[caseIndex])) { // statements[i] is a case or a default case
					caseIndex++;
					caseInits = caseInits.mergedWith(flowInfo.copy().unconditionalInits());
				} else {
					if (statement == defaultCase) {
						caseInits = caseInits.mergedWith(flowInfo.copy().unconditionalInits());
					}
				}
				if (!caseInits.complainIfUnreachable(statement, scope)) {
					caseInits = statement.analyseCode(scope, switchContext, caseInits);
				}
			}
		}

		// if no default case, then record it may jump over the block directly to the end
		if (defaultCase == null) {
			// only retain the potential initializations
			flowInfo.addPotentialInitializationsFrom(
				caseInits.mergedWith(switchContext.initsOnBreak));
			mergedInitStateIndex =
				currentScope.methodScope().recordInitializationStates(flowInfo);
			return flowInfo;
		}

		// merge all branches inits
		FlowInfo mergedInfo = caseInits.mergedWith(switchContext.initsOnBreak);
		mergedInitStateIndex =
			currentScope.methodScope().recordInitializationStates(mergedInfo);
		return mergedInfo;
	}
	/**
	 * Switch code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		int[] sortedIndexes = new int[caseCount];
		int[] localKeysCopy;
		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;

		// prepare the labels and constants
		breakLabel.codeStream = codeStream;
		CaseLabel[] caseLabels = new CaseLabel[caseCount];
		int[] constants = new int[caseCount];
		boolean needSwitch = caseCount != 0;
		for (int i = 0; i < caseCount; i++) {
			constants[i] = cases[i].constantExpression.constant.intValue();
			cases[i].targetLabel = (caseLabels[i] = new CaseLabel(codeStream));
		}

		// we sort the keys to be able to generate the code for tableswitch or lookupswitch
		for (int i = 0; i < caseCount; i++) {
			sortedIndexes[i] = i;
		}
		System.arraycopy(
			constants,
			0,
			(localKeysCopy = new int[caseCount]),
			0,
			caseCount);
		CodeStream.sort(localKeysCopy, 0, caseCount - 1, sortedIndexes);
		CaseLabel defaultLabel = new CaseLabel(codeStream);
		if (defaultCase != null) {
			defaultCase.targetLabel = defaultLabel;
		}
		// generate expression testes
		testExpression.generateCode(currentScope, codeStream, needSwitch);

		// generate the appropriate switch table
		if (needSwitch) {
			int max = localKeysCopy[caseCount - 1];
			int min = localKeysCopy[0];
			if ((long) (caseCount * 2.5) > ((long) max - (long) min)) {
				codeStream.tableswitch(
					defaultLabel,
					min,
					max,
					constants,
					sortedIndexes,
					caseLabels);
			} else {
				codeStream.lookupswitch(defaultLabel, constants, sortedIndexes, caseLabels);
			}
			codeStream.updateLastRecordedEndPC(codeStream.position);
		}
		// generate the switch block statements
		int caseIndex = 0;
		if (statements != null) {
			for (int i = 0, maxCases = statements.length; i < maxCases; i++) {
				Statement statement = statements[i];
				if ((caseIndex < caseCount)
					&& (statement == cases[caseIndex])) { // statements[i] is a case
					if (preSwitchInitStateIndex != -1) {
						codeStream.removeNotDefinitelyAssignedVariables(
							currentScope,
							preSwitchInitStateIndex);
						caseIndex++;
					}
				} else {
					if (statement == defaultCase) { // statements[i] is a case or a default case
						if (preSwitchInitStateIndex != -1) {
							codeStream.removeNotDefinitelyAssignedVariables(
								currentScope,
								preSwitchInitStateIndex);
						}
					}
				}
				statement.generateCode(scope, codeStream);
			}
		}
		// place the trailing labels (for break and default case)
		breakLabel.place();
		if (defaultCase == null) {
			defaultLabel.place();
		}
		// May loose some local variable initializations : affecting the local variable attributes
		if (mergedInitStateIndex != -1) {
			codeStream.removeNotDefinitelyAssignedVariables(
				currentScope,
				mergedInitStateIndex);
			codeStream.addDefinitelyAssignedVariables(currentScope, mergedInitStateIndex);
		}
		if (scope != currentScope) {
			codeStream.exitUserScope(scope);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}


	public void resetStateForCodeGeneration() {

		this.breakLabel.resetStateForCodeGeneration();
	}

	public void resolve(BlockScope upperScope) {

		TypeBinding testType = testExpression.resolveType(upperScope);
		if (testType == null)
			return;
		testExpression.implicitWidening(testType, testType);
		if (!(testExpression
			.isConstantValueOfTypeAssignableToType(testType, IntBinding))) {
			if (!upperScope.areTypesCompatible(testType, IntBinding)) {
				upperScope.problemReporter().incorrectSwitchType(testExpression, testType);
				return;
			}
		}
		if (statements != null) {
			scope = explicitDeclarations == 0 ? upperScope : new BlockScope(upperScope);
			int length;
			// collection of cases is too big but we will only iterate until caseCount
			cases = new Case[length = statements.length];
			int[] casesValues = new int[length];
			int counter = 0;
			for (int i = 0; i < length; i++) {
				Constant cst;
				if ((cst = statements[i].resolveCase(scope, testType, this)) != null) {
					//----check for duplicate case statement------------
					if (cst != NotAConstant) {
						int key = cst.intValue();
						for (int j = 0; j < counter; j++) {
							if (casesValues[j] == key) {
								scope.problemReporter().duplicateCase((Case) statements[i], cst);
							}
						}
						casesValues[counter++] = key;
					}
				}
			}
		}
	}
	public String toString(int tab) {

		String inFront, s = tabString(tab);
		inFront = s;
		s = s + "switch (" + testExpression.toStringExpression() + ") "; //$NON-NLS-1$ //$NON-NLS-2$
		if (statements == null) {
			s = s + "{}"; //$NON-NLS-1$
			return s;
		} else
			s = s + "{"; //$NON-NLS-1$
			s = s
					+ (explicitDeclarations != 0
						? "// ---scope needed for " //$NON-NLS-1$
							+ String.valueOf(explicitDeclarations)
							+ " locals------------ \n"//$NON-NLS-1$
						: "// ---NO scope needed------ \n"); //$NON-NLS-1$

		int i = 0;
		String tabulation = "  "; //$NON-NLS-1$
		try {
			while (true) {
				//use instanceof in order not to polluate classes with behavior only needed for printing purpose.
				if (statements[i] instanceof Expression)
					s = s + "\n" + inFront + tabulation; //$NON-NLS-1$
				if (statements[i] instanceof Break)
					s = s + statements[i].toString(0);
				else
					s = s + "\n" + statements[i].toString(tab + 2); //$NON-NLS-1$
				//=============	
				if ((statements[i] instanceof Case)
					|| (statements[i] instanceof DefaultCase)) {
					i++;
					while (!((statements[i] instanceof Case)
						|| (statements[i] instanceof DefaultCase))) {
						if ((statements[i] instanceof Expression) || (statements[i] instanceof Break))
							s = s + statements[i].toString(0) + " ; "; //$NON-NLS-1$
						else
							s = s + "\n" + statements[i].toString(tab + 6) + " ; "; //$NON-NLS-1$ //$NON-NLS-2$
						i++;
					}
				} else {
					s = s + " ;"; //$NON-NLS-1$
					i++;
				}
			}
		} catch (IndexOutOfBoundsException e) {
		};
		s = s + "}"; //$NON-NLS-1$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			testExpression.traverse(visitor, scope);
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
	
	/**
	 * Dispatch the call on its last statement.
	 */
	public void branchChainTo(Label label) {
		
		// in order to improve debug attributes for stepping (11431)
		// we want to inline the jumps to #breakLabel which already got
		// generated (if any), and have them directly branch to a better
		// location (the argument label).
		// we know at this point that the breakLabel already got placed
		if (this.breakLabel.hasForwardReferences()) {
			label.appendForwardReferencesFrom(this.breakLabel);
		}
	}
}