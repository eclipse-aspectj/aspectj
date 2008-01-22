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

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTNode;
import org.aspectj.org.eclipse.jdt.core.dom.ASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.Block;
import org.aspectj.org.eclipse.jdt.core.dom.Javadoc;
import org.aspectj.org.eclipse.jdt.core.dom.Type;

/**
 * InterTypeMethodDeclaration DOM AST node.
 * has:
 *   everything MethodDeclarations have
 *   
 * Note:
 *   should also have the name of the type it's declared on!
 * @author ajh02
 */

public class InterTypeMethodDeclaration extends MethodDeclaration {
	private String onType;
	
	InterTypeMethodDeclaration(AST ast) {
		super(ast);
	}

	public String getOnType() { 
		return onType;
	}
	
	public void setOnType(String onType) {
		this.onType = onType;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		InterTypeMethodDeclaration result = new InterTypeMethodDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			result.internalSetModifiers(getModifiers());
			result.setReturnType(
					(Type) ASTNode.copySubtree(target, getReturnType()));
		}
		if (this.ast.apiLevel >= AST.JLS3) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.typeParameters().addAll(
					ASTNode.copySubtrees(target, typeParameters()));
			result.setReturnType2(
					(Type) ASTNode.copySubtree(target, getReturnType2()));
		}
		result.setConstructor(isConstructor());
		result.setExtraDimensions(getExtraDimensions());
		result.setName((SimpleName) getName().clone(target));
		result.parameters().addAll(
			ASTNode.copySubtrees(target, parameters()));
		result.thrownExceptions().addAll(
			ASTNode.copySubtrees(target, thrownExceptions()));
		result.setBody(
			(Block) ASTNode.copySubtree(target, getBody()));
		return result;
	}
	
	
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) { 
		if (visitor instanceof AjASTVisitor) {
			AjASTVisitor ajvis = (AjASTVisitor)visitor;
			boolean visitChildren = ajvis.visit(this);
			if (visitChildren) {
				// visit children in normal left to right reading order
				acceptChild(ajvis, getJavadoc());
				if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
					acceptChild(ajvis, getReturnType());
				} else {
					acceptChildren(ajvis, this.modifiers);
					acceptChildren(ajvis, (NodeList)this.typeParameters());
					acceptChild(ajvis, getReturnType2());
				}
				// n.b. visit return type even for constructors
				acceptChild(ajvis, getName());
				acceptChildren(ajvis, this.parameters);
				acceptChildren(ajvis, (NodeList)this.thrownExceptions());
				acceptChild(ajvis, getBody());
			}
			ajvis.endVisit(this);
		}
	}
}