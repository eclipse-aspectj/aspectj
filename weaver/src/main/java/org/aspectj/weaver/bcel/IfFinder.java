/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement                 initial implementation
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;

/**
 * Look for an if() pointcut
 */
class IfFinder extends AbstractPatternNodeVisitor {
	boolean hasIf = false;

	public Object visit(IfPointcut node, Object data) {
		if (node.alwaysFalse() || node.alwaysTrue()) {
			// IfFalse / IfTrue
		} else {
			hasIf = true;
		}
		return node;
	}

	public Object visit(AndPointcut node, Object data) {
		if (!hasIf)
			node.getLeft().accept(this, data);
		if (!hasIf)
			node.getRight().accept(this, data);
		return node;
	}

	public Object visit(NotPointcut node, Object data) {
		if (!hasIf)
			node.getNegatedPointcut().accept(this, data);
		return node;
	}

	public Object visit(OrPointcut node, Object data) {
		if (!hasIf)
			node.getLeft().accept(this, data);
		if (!hasIf)
			node.getRight().accept(this, data);
		return node;
	}
}
