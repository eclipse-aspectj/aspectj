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

public abstract class AjASTVisitor extends ASTVisitor {

	public AjASTVisitor() {
		super(false);
	}
	
	public AjASTVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}
	
	public void endVisit(PointcutDeclaration node) {
		// default implementation: do nothing
	}
	
	public void endVisit(ReferencePointcut node) {
		// default implementation: do nothing
	}
	
	public void endVisit(DefaultPointcut node) {
		// default implementation: do nothing
	}
	
	public void endVisit(NotPointcut node) {
		// default implementation: do nothing
	}
	
	public void endVisit(PerObject node) {
		// default implementation: do nothing
	}
	
	public void endVisit(PerCflow node) {
		// default implementation: do nothing
	}
	
	public void endVisit(CflowPointcut node) {
		// default implementation: do nothing
	}
	
	public void endVisit(PerTypeWithin node) {
		// default implementation: do nothing
	}
	
	public void endVisit(AndPointcut node) {
		// default implementation: do nothing
	}
	
	public void endVisit(OrPointcut node) {
		// default implementation: do nothing
	}
	public boolean visit(AdviceDeclaration node) {
		return true;
	}
	
	public boolean visit(AroundAdviceDeclaration node) {
		return true;
	}
	public void endVisit(AroundAdviceDeclaration node) {
		// default: do nothing
	}
	public boolean visit(BeforeAdviceDeclaration node) {
		return true;
	}
	public void endVisit(BeforeAdviceDeclaration node) {
		// default: do nothing
	}
	public boolean visit(AfterAdviceDeclaration node) {
		return true;
	}
	public void endVisit(AfterAdviceDeclaration node) {
		// default: do nothing
	}
	public boolean visit(AfterThrowingAdviceDeclaration node) {
		return true;
	}
	public void endVisit(AfterThrowingAdviceDeclaration node) {
		// default: do nothing
	}
	public boolean visit(AfterReturningAdviceDeclaration node) {
		return true;
	}
	public void endVisit(AfterReturningAdviceDeclaration node) {
		// default: do nothing
	}
	public boolean visit(InterTypeFieldDeclaration node) {
		// ajh02: method added
		return true;
	}
	public boolean visit(InterTypeMethodDeclaration node) {
		// ajh02: method added
		return true;
	}
	public void endVisit(InterTypeFieldDeclaration node) {
		// ajh02: method added
		// default implementation: do nothing
	}
	public void endVisit(InterTypeMethodDeclaration node) {
		// ajh02: method added
		// default implementation: do nothing
	}
	public boolean visit(DeclareDeclaration node) {
		// ajh02: method added
		return true;
	}
	public void endVisit(DeclareDeclaration node) {
		// ajh02: method added
		// default implementation: do nothing
	}
	
	public boolean visit(DeclareAnnotationDeclaration node) {
		return true;
	}
	public void endVisit(DeclareAnnotationDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DeclareAtTypeDeclaration node) {
		return true;
	}
	public void endVisit(DeclareAtTypeDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DeclareAtConstructorDeclaration node) {
		return true;
	}
	public void endVisit(DeclareAtConstructorDeclaration node) {
		// default implementation: do nothing
	}
	public boolean visit(DeclareAtMethodDeclaration node) {
		return true;
	}
	public void endVisit(DeclareAtMethodDeclaration node) {
		// default implementation: do nothing
	}
	public boolean visit(DeclareAtFieldDeclaration node) {
		return true;
	}
	public void endVisit(DeclareAtFieldDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DeclareErrorDeclaration node) {
		return true;
	}
	public void endVisit(DeclareErrorDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DeclareParentsDeclaration node) {
		return true;
	}
	public void endVisit(DeclareParentsDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DeclarePrecedenceDeclaration node) {
		return true;
	}
	public void endVisit(DeclarePrecedenceDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DeclareSoftDeclaration node) {
		return true;
	}
	public void endVisit(DeclareSoftDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DeclareWarningDeclaration node) {
		return true;
	}
	public void endVisit(DeclareWarningDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(PointcutDeclaration node) {
		return true;
	}
	
	public boolean visit(ReferencePointcut node) {
		return true;
	}
	
	public boolean visit(NotPointcut node) {
		return true;
	}
	
	public boolean visit(PerObject node) {
		return true;
	}
	
	public boolean visit(PerCflow node) {
		return true;
	}
	
	public boolean visit(PerTypeWithin node) {
		return true;
	}
	
	public boolean visit(CflowPointcut node) {
		return true;
	}
	
	public boolean visit(AndPointcut node) {
		return true;
	}
	
	public boolean visit(OrPointcut node) {
		return true;
	}
	
	
	public boolean visit(DefaultPointcut node) {
		return true;
	}
	
	public void endVisit(AdviceDeclaration node) {
		// default implementation: do nothing
	}
	
	public boolean visit(DefaultTypePattern node) {
		return true;
	}
	
	public void endVisit(DefaultTypePattern node) {
	}
	public boolean visit(SignaturePattern node) {
		return true;
	}
	
	public void endVisit(SignaturePattern node) {
	}
}
