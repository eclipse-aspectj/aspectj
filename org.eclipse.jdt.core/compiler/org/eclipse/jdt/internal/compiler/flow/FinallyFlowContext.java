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
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class FinallyFlowContext extends FlowContext {
	
	Reference finalAssignments[];
	int assignCount;
	
	public FinallyFlowContext(FlowContext parent, AstNode associatedNode) {
		super(parent, associatedNode);
	}

	/**
	 * Given some contextual initialization info (derived from a try block or a catch block), this 
	 * code will check that the subroutine context does not also initialize a final variable potentially set
	 * redundantly.
	 */
	public void complainOnRedundantFinalAssignments(
		FlowInfo flowInfo,
		BlockScope scope) {
		for (int i = 0; i < assignCount; i++) {
			Reference ref;
			if (((ref = finalAssignments[i]).bits & BindingIds.FIELD) != 0) {
				// final field
				if (flowInfo.isPotentiallyAssigned(ref.fieldBinding())) {
					scope.problemReporter().duplicateInitializationOfBlankFinalField(ref.fieldBinding(), ref);
				}
			} else {
				// final local variable
				if (flowInfo
					.isPotentiallyAssigned((LocalVariableBinding) ((NameReference) ref).binding)) {
					scope.problemReporter().duplicateInitializationOfFinalLocal(
						(LocalVariableBinding) ((NameReference) ref).binding,
						(NameReference) ref);
				}
			}
			// any reference reported at this level is removed from the parent context 
			// where it could also be reported again
			FlowContext currentContext = parent;
			while (currentContext != null) {
				if (currentContext.isSubRoutine()) {
					currentContext.removeFinalAssignmentIfAny(ref);
				}
				currentContext = currentContext.parent;
			}
		}
	}

	public boolean isSubRoutine() {
		return true;
	}

	boolean recordFinalAssignment(
		VariableBinding binding,
		Reference finalAssignment) {
		if (assignCount == 0) {
			finalAssignments = new Reference[5];
		} else {
			if (assignCount == finalAssignments.length)
				System.arraycopy(
					finalAssignments,
					0,
					(finalAssignments = new Reference[assignCount * 2]),
					0,
					assignCount);
		};
		finalAssignments[assignCount++] = finalAssignment;
		return true;
	}
}