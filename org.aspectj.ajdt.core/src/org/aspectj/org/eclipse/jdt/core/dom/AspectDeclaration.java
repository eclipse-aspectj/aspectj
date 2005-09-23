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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AspectDeclaration DOM AST node.
 * has:
 *   everything a TypeDeclaration has.
 * 
 * This class could probably do with some work.
 * @author ajh02
 *
 */

public class AspectDeclaration extends AjTypeDeclaration {
	
	protected ASTNode perClause = null; // stays null if the aspect is an _implicit_ persingleton()
	
	public static final ChildPropertyDescriptor PERCLAUSE_PROPERTY = 
		new ChildPropertyDescriptor(AspectDeclaration.class, "perClause", ASTNode.class, OPTIONAL, NO_CYCLE_RISK); //$NON-NLS-1$
	
	AspectDeclaration(AST ast, ASTNode perClause) {
		super(ast);
		this.perClause = perClause;
		setAspect(true);
	}
	
	public ASTNode getPerClause(){
		return perClause;
	}

	public void setPerClause(ASTNode perClause) {
		if (perClause == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.perClause;
		preReplaceChild(oldChild, perClause, PERCLAUSE_PROPERTY);
		this.perClause = perClause;
		postReplaceChild(oldChild, perClause, PERCLAUSE_PROPERTY);
	}
	
	ASTNode clone0(AST target) {
		AspectDeclaration result = new AspectDeclaration(target, perClause);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			result.internalSetModifiers(getModifiers());
			result.setSuperclass(
					(Name) ASTNode.copySubtree(target, getSuperclass()));
			result.superInterfaces().addAll(
					ASTNode.copySubtrees(target, superInterfaces()));
		}
		result.setInterface(isInterface());
		result.setAspect(isAspect());
		result.setName((SimpleName) getName().clone(target));
		if (this.ast.apiLevel >= AST.JLS3) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
			result.typeParameters().addAll(
					ASTNode.copySubtrees(target, typeParameters()));
			result.setSuperclassType(
					(Type) ASTNode.copySubtree(target, getSuperclassType()));
			result.superInterfaceTypes().addAll(
					ASTNode.copySubtrees(target, superInterfaceTypes()));
		}
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
		result.setPerClause((ASTNode)getPerClause().clone(target));
		return result;
	}
	
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
				acceptChild(visitor, getJavadoc());
				acceptChild(visitor, getName());
				acceptChild(visitor, getSuperclass());
				acceptChildren(visitor, this.superInterfaceNames);
				acceptChild(visitor, this.perClause);
				acceptChildren(visitor, this.bodyDeclarations);
			}
			if (this.ast.apiLevel >= AST.JLS3) {
				acceptChild(visitor, getJavadoc());
				acceptChildren(visitor, this.modifiers);
				acceptChild(visitor, getName());
				acceptChildren(visitor, this.typeParameters);
				acceptChild(visitor, getSuperclassType());
				acceptChildren(visitor, this.superInterfaceTypes);
				acceptChild(visitor, this.perClause);
				acceptChildren(visitor, this.bodyDeclarations);
			}
		}
		visitor.endVisit(this);
	}
	
	
	public List getAdvice() {
		// ajh02: method added
		List bd = bodyDeclarations();
		List advice = new ArrayList();
		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
			Object decl = it.next();
			if (decl instanceof AdviceDeclaration) {
				advice.add(decl);
			}
		}
		return advice;
	}
	
//	public PointcutDeclaration[] getPointcuts() {
//		// ajh02: method added, currently returning none :-/
//		List bd = bodyDeclarations();
//		// ajh02: 0 bodyDeclarations :-/
//		int pointcutCount = 0;
//		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
//			if (it.next() instanceof PointcutDeclaration) {
//				pointcutCount++;
//			}
//		}
//		PointcutDeclaration[] pointcuts = new PointcutDeclaration[pointcutCount];
//		int next = 0;
//		for (Iterator it = bd.listIterator(); it.hasNext(); ) {
//			Object decl = it.next();
//			if (decl instanceof PointcutDeclaration) {
//				pointcuts[next++] = (PointcutDeclaration) decl;
//			}
//		}
//		return pointcuts;
//	}

}