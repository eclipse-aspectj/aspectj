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

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class InsideSubRoutineFlowContext extends FlowContext {

	public UnconditionalFlowInfo initsOnReturn;
	
	public InsideSubRoutineFlowContext(
		FlowContext parent,
		AstNode associatedNode) {
		super(parent, associatedNode);
		this.initsOnReturn = FlowInfo.DeadEnd;				
	}
	
	public boolean isNonReturningContext() {
		return associatedNode.cannotReturn();
	}
	
	public AstNode subRoutine() {
		return associatedNode;
	}
	
	public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {
		// record initializations which were performed at the return point
		initsOnReturn = initsOnReturn.mergedWith(flowInfo);
	}
}