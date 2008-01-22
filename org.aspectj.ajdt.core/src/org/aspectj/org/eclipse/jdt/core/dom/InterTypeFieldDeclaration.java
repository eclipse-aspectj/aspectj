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
 * InterTypeFieldDeclaration DOM AST node.
 * has:
 *   everything FieldDeclarations have
 *   
 * Refused Bequest:
 *   has the variableDeclarationFragments list
 *   it redundantly inherits from FieldDeclaration
 *   as fields can be declared like "int b; int a = b = 5;"
 *   but ITD fields can't.
 * Note:
 *   should also have the name of the type it was declared on!
 * @author ajh02
 */

public class InterTypeFieldDeclaration extends FieldDeclaration {
	private String onType;

	InterTypeFieldDeclaration(AST ast) {
		super(ast);
	}

	public String getOnType() { 
		return onType;
	}
	
	public void setOnType(String onType) {
		this.onType = onType;
	}
	
	ASTNode clone0(AST target) {
		InterTypeFieldDeclaration result = new InterTypeFieldDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			result.internalSetModifiers(getModifiers());
		}
		if (this.ast.apiLevel >= AST.JLS3) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		}
		result.setType((Type) getType().clone(target));
		result.fragments().addAll(
			ASTNode.copySubtrees(target, fragments()));
		return result;
	}
	
	void accept0(ASTVisitor visitor) {
		if (visitor instanceof AjASTVisitor) {
			boolean visitChildren = ((AjASTVisitor)visitor).visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(visitor, getJavadoc());
				if (this.ast.apiLevel >= AST.JLS3) {
					acceptChildren(visitor, this.modifiers);
				}
				acceptChild(visitor, getType());
				acceptChildren(visitor, this.variableDeclarationFragments);
			}
			((AjASTVisitor)visitor).endVisit(this);
		}
	}
}