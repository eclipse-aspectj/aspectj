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

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class BranchStatement extends Statement {
	public char[] label;
	public Label targetLabel;
	public AstNode[] subroutines;
/**
 * BranchStatement constructor comment.
 */
public BranchStatement(char[] l, int s,int e) {
	label = l ;
	sourceStart = s;
	sourceEnd = e;
}
/**
 * Branch code generation
 *
 *   generate the finallyInvocationSequence.
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {

	if ((bits & IsReachableMASK) == 0) {
		return;
	}
	int pc = codeStream.position;

	// generation of code responsible for invoking the finally 
	// blocks in sequence
	if (subroutines != null){
		for (int i = 0, max = subroutines.length; i < max; i++){
			AstNode sub;
			if ((sub = subroutines[i]) instanceof SynchronizedStatement){
				codeStream.load(((SynchronizedStatement)sub).synchroVariable);
				codeStream.monitorexit(); 
			} else {
				TryStatement trySub = (TryStatement) sub;
				if (trySub.subRoutineCannotReturn)	{
					codeStream.goto_(trySub.subRoutineStartLabel);
					codeStream.recordPositionsFrom(pc, this.sourceStart);
					return;
				} else {
					codeStream.jsr(trySub.subRoutineStartLabel);
				}
			}
		}
	}
	codeStream.goto_(targetLabel);
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public void resetStateForCodeGeneration() {
	this.targetLabel.resetStateForCodeGeneration();
}
}
