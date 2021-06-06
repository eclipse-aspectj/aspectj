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

/**
 * TODO: Add support for proper AnnotationPatterns instead of the annotation
 * expression
 *
 */
public class AnyWithAnnotationTypePattern extends TypePattern {

	AnyWithAnnotationTypePattern(AST ast, String annotationExpression) {
		// Is this correct? should the "*" be added
		super(ast, annotationExpression);
	}

	ASTNode clone0(AST target) {
		ASTNode node = new AnyWithAnnotationTypePattern(target,
				getTypePatternExpression());
		node.setSourceRange(getStartPosition(), getLength());
		return node;
	}

	List<?> internalStructuralPropertiesForType(int apiLevel) {
		return null;
	}

	boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		if (matcher instanceof AjASTMatcher) {
			AjASTMatcher ajmatcher = (AjASTMatcher) matcher;
			return ajmatcher.match(this, other);
		}
		return false;
	}

	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajVisitor = (AjASTVisitor) visitor;
			ajVisitor.visit(this);
			ajVisitor.endVisit(this);
		}
	}
}
