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
import org.eclipse.jdt.internal.compiler.codegen.Label;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class SwitchFlowContext extends FlowContext {
	public Label breakLabel;
	public UnconditionalFlowInfo initsOnBreak = FlowInfo.DeadEnd;
	
	public SwitchFlowContext(
		FlowContext parent,
		AstNode associatedNode,
		Label breakLabel) {
		super(parent, associatedNode);
		this.breakLabel = breakLabel;
	}

	public Label breakLabel() {
		return breakLabel;
	}

	public String individualToString() {
		return "Switch flow context"; //$NON-NLS-1$
	}

	public boolean isBreakable() {
		return true;
	}

	public void recordBreakFrom(FlowInfo flowInfo) {
		if (initsOnBreak == FlowInfo.DeadEnd) {
			initsOnBreak = flowInfo.copy().unconditionalInits();
		} else {
			// ignore if not really reachable (1FKEKRP)
			if (flowInfo.isFakeReachable())
				return;
			initsOnBreak.mergedWith(flowInfo.unconditionalInits());
		};
	}
}