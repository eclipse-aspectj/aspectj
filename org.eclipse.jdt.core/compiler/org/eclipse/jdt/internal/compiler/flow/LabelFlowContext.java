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
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class LabelFlowContext extends SwitchFlowContext {
	public char[] labelName;
	public LabelFlowContext(
		FlowContext parent,
		AstNode associatedNode,
		char[] labelName,
		Label breakLabel,
		BlockScope scope) {
		super(parent, associatedNode, breakLabel);
		this.labelName = labelName;
		checkLabelValidity(scope);
	}

	void checkLabelValidity(BlockScope scope) {
		// check if label was already defined above
		FlowContext current = parent;
		while (current != null) {
			char[] currentLabelName;
			if (((currentLabelName = current.labelName()) != null)
				&& CharOperation.equals(currentLabelName, labelName)) {
				scope.problemReporter().alreadyDefinedLabel(labelName, associatedNode);
			}
			current = current.parent;
		}
	}

	public String individualToString() {
		return "Label flow context [label:" + String.valueOf(labelName) + "]"; //$NON-NLS-2$ //$NON-NLS-1$
	}

	public char[] labelName() {
		return labelName;
	}
}