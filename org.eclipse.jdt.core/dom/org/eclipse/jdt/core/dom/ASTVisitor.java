/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * A visitor for abstract syntax trees.
 * <p>
 * For each different concrete AST node type <it>T</it> there are
 * a pair of methods:
 * <ul>
 * <li><code>public boolean visit(<it>T</it> node)</code> - Visits
 * the given node to perform some arbitrary operation. If <code>true</code>
 * is returned, the given node's child nodes will be visited next; however,
 * if <code>false</code> is returned, the given node's child nodes will 
 * not be visited. The default implementation provided by this class does
 * nothing and returns <code>true</code>. Subclasses may reimplement
 * this method as needed.</li>
 * <li><code>public void endVisit(<it>T</it> node)</code> - Visits
 * the given node to perform some arbitrary operation. When used in the
 * conventional way, this method is called after all of the given node's
 * children have been visited (or immediately, if <code>visit</code> returned
 * <code>false</code>). The default implementation provided by this class does
 * nothing. Subclasses may reimplement this method as needed.</li>
 * </ul>
 * </p>
 * In addition, there are a pair of methods for visiting AST nodes in the 
 * abstract, regardless of node type:
 * <ul>
 * <li><code>public void preVisit(ASTNode node)</code> - Visits
 * the given node to perform some arbitrary operation. 
 * This method is invoked prior to the appropriate type-specific
 * <code>visit</code> method.
 * The default implementation of this method does nothing.
 * Subclasses may reimplement this method as needed.</li>
 * <li><code>public void postVisit(ASTNode node)</code> - Visits
 * the given node to perform some arbitrary operation. 
 * This method is invoked after the appropriate type-specific
 * <code>endVisit</code> method.
 * The default implementation of this method does nothing.
 * Subclasses may reimplement this method as needed.</li>
 * </ul>
 * <p>
 * For nodes with list-valued properties, the child nodes within the list
 * are visited in order. For nodes with multiple properties, the child nodes
 * are visited in the order that most closely corresponds to the lexical
 * reading order of the source program. For instance, for a type declaration
 * node, the child ordering is: name, superclass, superinterfaces, and 
 * body declarations.
 * </p>
 * <p>
 * While it is possible to modify the tree in the visitor, care is required to
 * ensure that the consequences are as expected and desirable.
 * During the course of an ordinary visit starting at a given node, every node
 * in the subtree is visited exactly twice, first with <code>visit</code> and
 * then with <code>endVisit</code>. During a traversal of a stationary tree, 
 * each node is either behind (after <code>endVisit</code>), ahead (before 
 * <code>visit</code>), or in progress (between <code>visit</code> and
 * the matching <code>endVisit</code>). Changes to the "behind" region of the
 * tree are of no consequence to the visit in progress. Changes to the "ahead"
 * region will be taken in stride. Changes to the "in progress" portion are
 * the more interesting cases. With a node, the various properties are arranged
 * in a linear list, with a cursor that separates the properties that have
 * been visited from the ones that are still to be visited (the cursor
 * is between the elements, rather than on an element). The cursor moves from
 * the head to the tail of this list, advancing to the next position just
 * <it>before</it> <code>visit</code> if called for that child. After the child
 * subtree has been completely visited, the visit moves on the child 
 * immediately after the cursor. Removing a child while it is being visited
 * does not alter the course of the visit. But any children added at positions
 * after the cursor are considered in the "ahead" portion and will be visited.
 * </p>
 * <p>
 * Cases to watch out for:
 * <ul>
 * <li>Moving a child node further down the list. This could result in the
 * child subtree being visited multiple times; these visits are sequential.</li>
 * <li>Moving a child node up into an ancestor. If the new home for
 * the node is in the "ahead" portion, the subtree will be visited 
 * a second time; again, these visits are sequential.</li>
 * <li>Moving a node down into a child. If the new home for
 * the node is in the "ahead" portion, the subtree will be visited 
 * a second time; in this case, the visits will be nested. In some cases,
 * this can lead to a stack overflow or out of memory condition.</li>
 * </ul>
 * </p>
 * 
 * @see ASTNode#accept
 */
public abstract class ASTVisitor {
	
	/**
	 * Visits the given AST node prior to the type-specific visit.
	 * (before <code>visit</code>).
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 * 
	 * @param node the node to visit
	 */
	public void preVisit(ASTNode node) {
	}
	
	/**
	 * Visits the given AST node following the type-specific visit
	 * (after <code>endVisit</code>).
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 * 
	 * @param node the node to visit
	 */
	public void postVisit(ASTNode node) {
	}

	public boolean visit(AnonymousClassDeclaration node) {
		return true;
	}
	public boolean visit(ArrayAccess node) {
		return true;
	}
	public boolean visit(ArrayCreation node) {
		return true;
	}
	public boolean visit(ArrayInitializer node) {
		return true;
	}
	public boolean visit(ArrayType node) {
		return true;
	}
	public boolean visit(AssertStatement node) {
		return true;
	}
	public boolean visit(Assignment node) {
		return true;
	}
	public boolean visit(Block node) {
		return true;
	}
	public boolean visit(BooleanLiteral node) {
		return true;
	}
	public boolean visit(BreakStatement node) {
		return true;
	}
	public boolean visit(CastExpression node) {
		return true;
	}
	public boolean visit(CatchClause node) {
		return true;
	}
	public boolean visit(CharacterLiteral node) {
		return true;
	}
	public boolean visit(ClassInstanceCreation node) {
		return true;
	}
	public boolean visit(CompilationUnit node) {
		return true;
	}
	public boolean visit(ConditionalExpression node) {
		return true;
	}
	public boolean visit(ConstructorInvocation node) {
		return true;
	}
	public boolean visit(ContinueStatement node) {
		return true;
	}
	public boolean visit(DoStatement node) {
		return true;
	}
	public boolean visit(EmptyStatement node) {
		return true;
	}
	public boolean visit(ExpressionStatement node) {
		return true;
	}
	public boolean visit(FieldAccess node) {
		return true;
	}
	public boolean visit(FieldDeclaration node) {
		return true;
	}
	public boolean visit(ForStatement node) {
		return true;
	}
	public boolean visit(IfStatement node) {
		return true;
	}
	public boolean visit(ImportDeclaration node) {
		return true;
	}
	public boolean visit(InfixExpression node) {
		return true;
	}
	public boolean visit(InstanceofExpression node) {
		return true;
	}
	public boolean visit(Initializer node) {
		return true;
	}
	public boolean visit(Javadoc node) {
		return true;
	}
	public boolean visit(LabeledStatement node) {
		return true;
	}
	public boolean visit(MethodDeclaration node) {
		return true;
	}
	public boolean visit(MethodInvocation node) {
		return true;
	}
	public boolean visit(NullLiteral node) {
		return true;
	}
	public boolean visit(NumberLiteral node) {
		return true;
	}
	public boolean visit(PackageDeclaration node) {
		return true;
	}
	public boolean visit(ParenthesizedExpression node) {
		return true;
	}
	public boolean visit(PostfixExpression node) {
		return true;
	}
	public boolean visit(PrefixExpression node) {
		return true;
	}
	public boolean visit(PrimitiveType node) {
		return true;
	}
	public boolean visit(QualifiedName node) {
		return true;
	}
	public boolean visit(ReturnStatement node) {
		return true;
	}
	public boolean visit(SimpleName node) {
		return true;
	}
	public boolean visit(SimpleType node) {
		return true;
	}
	public boolean visit(StringLiteral node) {
		return true;
	}
	public boolean visit(SuperConstructorInvocation node) {
		return true;
	}
	public boolean visit(SuperFieldAccess node) {
		return true;
	}
	public boolean visit(SuperMethodInvocation node) {
		return true;
	}
	public boolean visit(SwitchCase node) {
		return true;
	}
	public boolean visit(SwitchStatement node) {
		return true;
	}
	public boolean visit(SynchronizedStatement node) {
		return true;
	}
	public boolean visit(ThisExpression node) {
		return true;
	}
	public boolean visit(ThrowStatement node) {
		return true;
	}
	public boolean visit(TryStatement node) {
		return true;
	}
	public boolean visit(TypeDeclaration node) {
		return true;
	}
	public boolean visit(TypeDeclarationStatement node) {
		return true;
	}
	public boolean visit(TypeLiteral node) {
		return true;
	}
	public boolean visit(SingleVariableDeclaration node) {
		return true;
	}
	public boolean visit(VariableDeclarationExpression node) {
		return true;
	}
	public boolean visit(VariableDeclarationStatement node) {
		return true;
	}
	public boolean visit(VariableDeclarationFragment node) {
		return true;
	}
	public boolean visit(WhileStatement node) {
		return true;
	}

	public void endVisit(AnonymousClassDeclaration node) {
	}
	public void endVisit(ArrayAccess node) {
	}
	public void endVisit(ArrayCreation node) {
	}
	public void endVisit(ArrayInitializer node) {
	}
	public void endVisit(ArrayType node) {
	}
	public void endVisit(AssertStatement node) {
	}
	public void endVisit(Assignment node) {
	}
	public void endVisit(Block node) {
	}
	public void endVisit(BooleanLiteral node) {
	}
	public void endVisit(BreakStatement node) {
	}
	public void endVisit(CastExpression node) {
	}
	public void endVisit(CatchClause node) {
	}
	public void endVisit(CharacterLiteral node) {
	}
	public void endVisit(ClassInstanceCreation node) {
	}
	public void endVisit(CompilationUnit node) {
	}
	public void endVisit(ConditionalExpression node) {
	}
	public void endVisit(ConstructorInvocation node) {
	}
	public void endVisit(ContinueStatement node) {
	}
	public void endVisit(DoStatement node) {
	}
	public void endVisit(EmptyStatement node) {
	}
	public void endVisit(ExpressionStatement node) {
	}
	public void endVisit(FieldAccess node) {
	}
	public void endVisit(FieldDeclaration node) {
	}
	public void endVisit(ForStatement node) {
	}
	public void endVisit(IfStatement node) {
	}
	public void endVisit(ImportDeclaration node) {
	}
	public void endVisit(InfixExpression node) {
	}
	public void endVisit(InstanceofExpression node) {
	}
	public void endVisit(Initializer node) {
	}
	public void endVisit(Javadoc node) {
	}
	public void endVisit(LabeledStatement node) {
	}
	public void endVisit(MethodDeclaration node) {
	}
	public void endVisit(MethodInvocation node) {
	}
	public void endVisit(NullLiteral node) {
	}
	public void endVisit(NumberLiteral node) {
	}
	public void endVisit(PackageDeclaration node) {
	}
	public void endVisit(ParenthesizedExpression node) {
	}
	public void endVisit(PostfixExpression node) {
	}
	public void endVisit(PrefixExpression node) {
	}
	public void endVisit(PrimitiveType node) {
	}
	public void endVisit(QualifiedName node) {
	}
	public void endVisit(ReturnStatement node) {
	}
	public void endVisit(SimpleName node) {
	}
	public void endVisit(SimpleType node) {
	}
	public void endVisit(StringLiteral node) {
	}
	public void endVisit(SuperConstructorInvocation node) {
	}
	public void endVisit(SuperFieldAccess node) {
	}
	public void endVisit(SuperMethodInvocation node) {
	}
	public void endVisit(SwitchCase node) {
	}
	public void endVisit(SwitchStatement node) {
	}
	public void endVisit(SynchronizedStatement node) {
	}
	public void endVisit(ThisExpression node) {
	}
	public void endVisit(ThrowStatement node) {
	}
	public void endVisit(TryStatement node) {
	}
	public void endVisit(TypeDeclaration node) {
	}
	public void endVisit(TypeDeclarationStatement node) {
	}
	public void endVisit(TypeLiteral node) {
	}
	public void endVisit(SingleVariableDeclaration node) {
	}
	public void endVisit(VariableDeclarationExpression node) {
	}
	public void endVisit(VariableDeclarationStatement node) {
	}
	public void endVisit(VariableDeclarationFragment node) {
	}
	public void endVisit(WhileStatement node) {
	}
}