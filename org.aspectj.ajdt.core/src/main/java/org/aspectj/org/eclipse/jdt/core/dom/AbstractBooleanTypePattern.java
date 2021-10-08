/********************************************************************
 * Copyright (c) 2010 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: Nieraj Singh - initial implementation
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

public abstract class AbstractBooleanTypePattern extends AbstractTypePattern {

	private AbstractTypePattern left;
	private AbstractTypePattern right;

	AbstractBooleanTypePattern(AST ast, AbstractTypePattern left, AbstractTypePattern right,
                             String booleanOperator) {
		super(ast, booleanOperator);
		this.left = left;
		this.right = right;
	}

	public AbstractTypePattern getLeft() {
		return left;
	}

	public AbstractTypePattern getRight() {
		return right;
	}

	int treeSize() {
		return memSize() + (this.left == null ? 0 : getLeft().treeSize())
				+ (this.right == null ? 0 : getRight().treeSize());
	}
}
