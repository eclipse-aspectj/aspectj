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

import java.util.List;

public class NotTypePattern extends AbstractTypePattern {

	private AbstractTypePattern negatedPattern;

	/**
	 * The negated type pattern cannot be null
	 *
	 * @param ast
	 *            not null
	 * @param negatedPattern
	 *            not null
	 */
	NotTypePattern(AST ast, AbstractTypePattern negatedPattern) {
		super(ast, "!");
		this.negatedPattern = negatedPattern;
	}

	List<?> internalStructuralPropertiesForType(int apiLevel) {
		return null;
	}

	public AbstractTypePattern getNegatedTypePattern() {
		return negatedPattern;
	}

	ASTNode clone0(AST target) {
		ASTNode node = new NotTypePattern(target,
				(AbstractTypePattern) getNegatedTypePattern().clone(target));
		node.setSourceRange(getStartPosition(), getLength());
		return node;
	}

	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajVisitor = (AjASTVisitor) visitor;
			boolean visit = ajVisitor.visit(this);
			if (visit) {
				acceptChild(ajVisitor, getNegatedTypePattern());
			}
			ajVisitor.endVisit(this);
		}
	}

	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		if (matcher instanceof AjASTMatcher) {
			AjASTMatcher ajmatcher = (AjASTMatcher) matcher;
			return ajmatcher.match(this, other);
		}
		return false;
	}

	int memSize() {
		return super.memSize() + getNegatedTypePattern().memSize();
	}

}
