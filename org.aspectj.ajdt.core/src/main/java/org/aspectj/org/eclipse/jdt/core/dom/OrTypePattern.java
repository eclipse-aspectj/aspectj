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

public class OrTypePattern extends AbstractBooleanTypePattern {

	public static final String OR_OPERATOR = "||";

	OrTypePattern(AST ast,
			AbstractTypePattern left,
			AbstractTypePattern right) {
		super(ast, left, right, OR_OPERATOR);
	}

	List<?> internalStructuralPropertiesForType(int apiLevel) {
		return null;
	}

	ASTNode clone0(AST target) {
		OrTypePattern cloned = new OrTypePattern(target,
				(AbstractTypePattern) getLeft().clone(target), (AbstractTypePattern) getRight()
						.clone(target));
		cloned.setSourceRange(getStartPosition(), getLength());
		return cloned;
	}

	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajVisitor = (AjASTVisitor) visitor;
			boolean visit = ajVisitor.visit(this);
			if (visit) {
				acceptChild(visitor, getLeft());
				acceptChild(visitor, getRight());
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
}
