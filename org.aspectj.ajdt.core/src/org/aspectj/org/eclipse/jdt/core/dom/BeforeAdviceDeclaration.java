/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.aspectj.org.eclipse.jdt.core.dom;

/**
 * BeforeAdviceDeclaration DOM AST node.
 * @author ajh02
 */

public class BeforeAdviceDeclaration extends AdviceDeclaration {
	
	BeforeAdviceDeclaration(AST ast) {
		super(ast);
	}
	
	ASTNode clone0(AST target) {
		BeforeAdviceDeclaration result = new BeforeAdviceDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		result.thrownExceptions().addAll(
			ASTNode.copySubtrees(target, thrownExceptions()));
		result.setPointcut(getPointcut());
		result.setBody(
			(Block) ASTNode.copySubtree(target, getBody()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return ((AjASTMatcher)matcher).match(this, other);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getJavadoc());
				acceptChildren(visitor, this.parameters);
				acceptChild(visitor, getPointcut());
				acceptChildren(visitor, this.thrownExceptions);
				acceptChild(visitor, getBody());
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}
}
