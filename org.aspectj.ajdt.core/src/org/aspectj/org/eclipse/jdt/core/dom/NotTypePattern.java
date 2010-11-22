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

public class NotTypePattern extends TypePattern {

	private TypePattern negatedPattern;

	/**
	 * The negated type pattern cannot be null
	 * 
	 * @param ast
	 *            not null
	 * @param negatedPattern
	 *            not null
	 */
	NotTypePattern(AST ast, TypePattern negatedPattern) {
		super(ast, "!");
		this.negatedPattern = negatedPattern;
	}

	List<?> internalStructuralPropertiesForType(int apiLevel) {
		return null;
	}

	public TypePattern getNegatedTypePattern() {
		return negatedPattern;
	}

	ASTNode clone0(AST target) {
		ASTNode node = new NotTypePattern(target,
				(TypePattern) getNegatedTypePattern().clone(target));
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
