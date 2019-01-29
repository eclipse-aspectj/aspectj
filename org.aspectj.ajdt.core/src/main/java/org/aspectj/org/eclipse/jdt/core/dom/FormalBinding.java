/********************************************************************
 * Copyright (c) 2010 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: Nieraj Singh - initial implementation
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.List;

public class FormalBinding extends Type {

	private Type type;
	private String binding;

	/**
	 * 
	 * @param type
	 *            must not be null
	 * @param binding
	 *            must not be null
	 * @param ast
	 *            must not be null
	 */
	public FormalBinding(Type type, String binding, AST ast) {
		super(ast);
	}

	public Type getType() {
		return type;
	}

	public String getBinding() {
		return binding;
	}

	@Override
	List<?> internalStructuralPropertiesForType(int apiLevel) {
		return null;
	}

	@Override
	int getNodeType0() {
		return 0;
	}

	@Override
	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		if (matcher instanceof AjASTMatcher) {
			return ((AjASTMatcher) matcher).match(this, other);
		}
		return false;
	}

	@Override
	ASTNode clone0(AST target) {
		ASTNode node = new FormalBinding((Type) getType().clone(target),
				getBinding(), target);
		node.setSourceRange(getStartPosition(), getLength());
		return node;
	}

	@Override
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visited = ((AjASTVisitor) visitor).visit(this);
			if (visited) {
				((AjASTVisitor) visitor).visit(getType());
			}
			((AjASTVisitor) visitor).endVisit(this);
		}

	}

	@Override
	int treeSize() {
		return getType().treeSize();
	}

	@Override
	int memSize() {
		return BASE_NODE_SIZE + (3 * 4) + getType().memSize();
	}

}
