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
package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.codegen.Label;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class LoopingFlowContext extends SwitchFlowContext {
	public Label continueLabel;
	public UnconditionalFlowInfo initsOnContinue = FlowInfo.DeadEnd;
	Reference finalAssignments[];
	VariableBinding finalVariables[];
	int assignCount = 0;
	Scope associatedScope;
	public LoopingFlowContext(
		FlowContext parent,
		AstNode associatedNode,
		Label breakLabel,
		Label continueLabel,
		Scope associatedScope) {
		super(parent, associatedNode, breakLabel);
		this.continueLabel = continueLabel;
		this.associatedScope = associatedScope;
	}
	
	public void complainOnFinalAssignmentsInLoop(
		BlockScope scope,
		FlowInfo flowInfo) {
		for (int i = 0; i < assignCount; i++) {
			VariableBinding variable;
			if ((variable = finalVariables[i]) != null) {
				boolean complained; // remember if have complained on this final assignment
				if (variable instanceof FieldBinding) {
					if (complained = flowInfo.isPotentiallyAssigned((FieldBinding) variable)) {
						scope.problemReporter().duplicateInitializationOfBlankFinalField(
							(FieldBinding) variable,
							(NameReference) finalAssignments[i]);
					}
				} else {
					if (complained =
						flowInfo.isPotentiallyAssigned((LocalVariableBinding) variable)) {
						scope.problemReporter().duplicateInitializationOfFinalLocal(
							(LocalVariableBinding) variable,
							(NameReference) finalAssignments[i]);
					}
				}
				// any reference reported at this level is removed from the parent context where it 
				// could also be reported again
				if (complained) {
					FlowContext context = parent;
					while (context != null) {
						context.removeFinalAssignmentIfAny(finalAssignments[i]);
						context = context.parent;
					}
				}
			}
		}
	}

	public Label continueLabel() {
		return continueLabel;
	}

	public String individualToString() {
		return "Looping flow context"; //$NON-NLS-1$
	}

	public boolean isContinuable() {
		return true;
	}

	public boolean isContinuedTo() {
		return initsOnContinue != FlowInfo.DeadEnd;
	}

	public void recordContinueFrom(FlowInfo flowInfo) {
		if (initsOnContinue == FlowInfo.DeadEnd) {
			initsOnContinue = flowInfo.copy().unconditionalInits();
		} else {
			// ignore if not really reachable (1FKEKRP)
			if (flowInfo.isFakeReachable())
				return;
			initsOnContinue.mergedWith(flowInfo.unconditionalInits());
		};
	}

	boolean recordFinalAssignment(
		VariableBinding binding,
		Reference finalAssignment) {
		// do not consider variables which are defined inside this loop
		if (binding instanceof LocalVariableBinding) {
			Scope scope = ((LocalVariableBinding) binding).declaringScope;
			while ((scope = scope.parent) != null) {
				if (scope == associatedScope)
					return false;
			}
		}
		if (assignCount == 0) {
			finalAssignments = new Reference[5];
			finalVariables = new VariableBinding[5];
		} else {
			if (assignCount == finalAssignments.length)
				System.arraycopy(
					finalAssignments,
					0,
					(finalAssignments = new Reference[assignCount * 2]),
					0,
					assignCount);
			System.arraycopy(
				finalVariables,
				0,
				(finalVariables = new VariableBinding[assignCount * 2]),
				0,
				assignCount);
		};
		finalAssignments[assignCount] = finalAssignment;
		finalVariables[assignCount++] = binding;
		return true;
	}

	void removeFinalAssignmentIfAny(Reference reference) {
		for (int i = 0; i < assignCount; i++) {
			if (finalAssignments[i] == reference) {
				finalAssignments[i] = null;
				finalVariables[i] = null;
				return;
			}
		}
	}
}