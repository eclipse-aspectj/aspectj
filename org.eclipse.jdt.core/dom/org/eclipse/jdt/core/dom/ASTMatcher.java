/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.Iterator;
import java.util.List;

/**
 * Concrete superclass and default implementation of an AST subtree matcher.
 * <p>
 * For example, to compute whether two ASTs subtrees are structurally 
 * isomorphic, use <code>n1.subtreeMatch(new ASTMatcher(), n2)</code> where 
 * <code>n1</code> and <code>n2</code> are the AST root nodes of the subtrees.
 * </p>
 * <p>
 * For each different concrete AST node type <it>T</it> there is a
 * <code>public boolean match(<it>T</it> node, Object other)</code> method
 * that matches the given node against another object (typically another
 * AST node, although this is not essential). The default implementations
 * provided by this class tests whether the other object is a node of the
 * same type with structurally isomorphic child subtrees. For nodes with 
 * list-valued properties, the child nodes within the list are compared in
 * order. For nodes with multiple properties, the child nodes are compared
 * in the order that most closely corresponds to the lexical reading order
 * of the source program. For instance, for a type declaration node, the 
 * child ordering is: name, superclass, superinterfaces, and body 
 * declarations.
 * </p>
 * <p>
 * Subclasses may override (extend or reimplement) some or all of the 
 * <code>match</code> methods in order to define more specialized subtree
 * matchers.
 * </p>
 * 
 * @see ASTNode#subtreeMatch
 * @since 2.0
 */
public class ASTMatcher {

	/**
	 * Creates a new AST matcher.
	 */
	public ASTMatcher() {
	}

	/**
	 * Returns whether the given lists of AST nodes match pair wise according
	 * to <code>ASTNode.subtreeMatch</code>.
	 * <p>
	 * Note that this is a convenience method, useful for writing recursive
	 * subtree matchers.
	 * </p>
	 * 
	 * @param list1 the first list of AST nodes
	 *    (element type: <code>ASTNode</code>)
	 * @param list2 the second list of AST nodes
	 *    (element type: <code>ASTNode</code>)
	 * @return <code>true</code> if the lists have the same number of elements
	 *    and match pair-wise according to <code>ASTNode.subtreeMatch</code> 
	 * @see ASTNode#subtreeMatch(ASTMatcher matcher, Object other)
	 */
	public final boolean safeSubtreeListMatch(List list1, List list2) {
		int size1 = list1.size();
		int size2 = list2.size();
		if (size1 != size2) {
			return false;
		}
		for (Iterator it1 = list1.iterator(), it2 = list2.iterator(); it1.hasNext();) {
			ASTNode n1 = (ASTNode) it1.next();
			ASTNode n2 = (ASTNode) it2.next();
			if (!n1.subtreeMatch(this, n2)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether the given nodes match according to
	 * <code>AST.subtreeMatch</code>. Returns <code>false</code> if one or
	 * the other of the nodes are <code>null</code>. Returns <code>true</code>
	 * if both nodes are <code>null</code>.
	 * <p>
	 * Note that this is a convenience method, useful for writing recursive
	 * subtree matchers.
	 * </p>
	 * 
	 * @param node1 the first AST node, or <code>null</code>; must be an
	 *    instance of <code>ASTNode</code>
	 * @param node2 the second AST node, or <code>null</code>; must be an
	 *    instance of <code>ASTNode</code>
	 * @return <code>true</code> if the nodes match according
	 *    to <code>AST.subtreeMatch</code> or both are <code>null</code>, and 
	 *    <code>false</code> otherwise
	 * @see ASTNode#subtreeMatch(ASTMatcher, Object)
	 */
	public final boolean safeSubtreeMatch(Object node1, Object node2) {
		if (node1 == null && node2 == null) {
			return true;
		}
		if (node1 == null || node2 == null) {
			return false;
		}
		// N.B. call subtreeMatch even node1==node2!=null
		return ((ASTNode) node1).subtreeMatch(this, (ASTNode) node2);
	}

	/**
	 * Returns whether the given objects are equal according to
	 * <code>equals</code>. Returns <code>false</code> if either
	 * node is <code>null</code>.
	 * 
	 * @param o1 the first object, or <code>null</code>
	 * @param o2 the second object, or <code>null</code>
	 * @return <code>true</code> if the nodes are equal according to
	 *    <code>equals</code> or both <code>null</code>, and 
	 *    <code>false</code> otherwise
	 */
	public static boolean safeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return o1.equals(o2);
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(AnonymousClassDeclaration node, Object other) {
		if (!(other instanceof AnonymousClassDeclaration)) {
			return false;
		}
		AnonymousClassDeclaration o = (AnonymousClassDeclaration) other;
		return safeSubtreeListMatch(node.bodyDeclarations(), o.bodyDeclarations());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayAccess node, Object other) {
		if (!(other instanceof ArrayAccess)) {
			return false;
		}
		ArrayAccess o = (ArrayAccess) other;
		return (
			safeSubtreeMatch(node.getArray(), o.getArray())
				&& safeSubtreeMatch(node.getIndex(), o.getIndex()));
	}

	/**
	 * Returns whether the given node and the other object object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayCreation node, Object other) {
		if (!(other instanceof ArrayCreation)) {
			return false;
		}
		ArrayCreation o = (ArrayCreation) other;
		return (
			safeSubtreeMatch(node.getType(), o.getType())
				&& safeSubtreeListMatch(node.dimensions(), o.dimensions())
				&& safeSubtreeMatch(node.getInitializer(), o.getInitializer()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayInitializer node, Object other) {
		if (!(other instanceof ArrayInitializer)) {
			return false;
		}
		ArrayInitializer o = (ArrayInitializer) other;
		return safeSubtreeListMatch(node.expressions(), o.expressions());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayType node, Object other) {
		if (!(other instanceof ArrayType)) {
			return false;
		}
		ArrayType o = (ArrayType) other;
		return safeSubtreeMatch(node.getComponentType(), o.getComponentType());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(AssertStatement node, Object other) {
		if (!(other instanceof AssertStatement)) {
			return false;
		}
		AssertStatement o = (AssertStatement) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getMessage(), o.getMessage()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(Assignment node, Object other) {
		if (!(other instanceof Assignment)) {
			return false;
		}
		Assignment o = (Assignment) other;
		return (
			node.getOperator().equals(o.getOperator())
				&& safeSubtreeMatch(node.getLeftHandSide(), o.getLeftHandSide())
				&& safeSubtreeMatch(node.getRightHandSide(), o.getRightHandSide()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(Block node, Object other) {
		if (!(other instanceof Block)) {
			return false;
		}
		Block o = (Block) other;
		return safeSubtreeListMatch(node.statements(), o.statements());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(BooleanLiteral node, Object other) {
		if (!(other instanceof BooleanLiteral)) {
			return false;
		}
		BooleanLiteral o = (BooleanLiteral) other;
		return node.booleanValue() == o.booleanValue();
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(BreakStatement node, Object other) {
		if (!(other instanceof BreakStatement)) {
			return false;
		}
		BreakStatement o = (BreakStatement) other;
		return safeSubtreeMatch(node.getLabel(), o.getLabel());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CastExpression node, Object other) {
		if (!(other instanceof CastExpression)) {
			return false;
		}
		CastExpression o = (CastExpression) other;
		return (
			safeSubtreeMatch(node.getType(), o.getType())
				&& safeSubtreeMatch(node.getExpression(), o.getExpression()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CatchClause node, Object other) {
		if (!(other instanceof CatchClause)) {
			return false;
		}
		CatchClause o = (CatchClause) other;
		return (
			safeSubtreeMatch(node.getException(), o.getException())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CharacterLiteral node, Object other) {
		if (!(other instanceof CharacterLiteral)) {
			return false;
		}
		CharacterLiteral o = (CharacterLiteral) other;
		return safeEquals(node.getEscapedValue(), o.getEscapedValue());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ClassInstanceCreation node, Object other) {
		if (!(other instanceof ClassInstanceCreation)) {
			return false;
		}
		ClassInstanceCreation o = (ClassInstanceCreation) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getName(), o.getName())
				&& safeSubtreeListMatch(node.arguments(), o.arguments())
				&& safeSubtreeMatch(
					node.getAnonymousClassDeclaration(),
					o.getAnonymousClassDeclaration()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CompilationUnit node, Object other) {
		if (!(other instanceof CompilationUnit)) {
			return false;
		}
		CompilationUnit o = (CompilationUnit) other;
		return (
			safeSubtreeMatch(node.getPackage(), o.getPackage())
				&& safeSubtreeListMatch(node.imports(), o.imports())
				&& safeSubtreeListMatch(node.types(), o.types()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ConditionalExpression node, Object other) {
		if (!(other instanceof ConditionalExpression)) {
			return false;
		}
		ConditionalExpression o = (ConditionalExpression) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getThenExpression(), o.getThenExpression())
				&& safeSubtreeMatch(node.getElseExpression(), o.getElseExpression()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ConstructorInvocation node, Object other) {
		if (!(other instanceof ConstructorInvocation)) {
			return false;
		}
		ConstructorInvocation o = (ConstructorInvocation) other;
		return safeSubtreeListMatch(node.arguments(), o.arguments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ContinueStatement node, Object other) {
		if (!(other instanceof ContinueStatement)) {
			return false;
		}
		ContinueStatement o = (ContinueStatement) other;
		return safeSubtreeMatch(node.getLabel(), o.getLabel());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(DoStatement node, Object other) {
		if (!(other instanceof DoStatement)) {
			return false;
		}
		DoStatement o = (DoStatement) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(EmptyStatement node, Object other) {
		if (!(other instanceof EmptyStatement)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ExpressionStatement node, Object other) {
		if (!(other instanceof ExpressionStatement)) {
			return false;
		}
		ExpressionStatement o = (ExpressionStatement) other;
		return safeSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(FieldAccess node, Object other) {
		if (!(other instanceof FieldAccess)) {
			return false;
		}
		FieldAccess o = (FieldAccess) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getName(), o.getName()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(FieldDeclaration node, Object other) {
		if (!(other instanceof FieldDeclaration)) {
			return false;
		}
		FieldDeclaration o = (FieldDeclaration) other;
		return node.getModifiers() == o.getModifiers()
			&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
			&& safeSubtreeMatch(node.getType(), o.getType())
			&& safeSubtreeListMatch(node.fragments(), o.fragments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ForStatement node, Object other) {
		if (!(other instanceof ForStatement)) {
			return false;
		}
		ForStatement o = (ForStatement) other;
		return (
			safeSubtreeListMatch(node.initializers(), o.initializers())
				&& safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeListMatch(node.updaters(), o.updaters())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(IfStatement node, Object other) {
		if (!(other instanceof IfStatement)) {
			return false;
		}
		IfStatement o = (IfStatement) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getThenStatement(), o.getThenStatement())
				&& safeSubtreeMatch(node.getElseStatement(), o.getElseStatement()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ImportDeclaration node, Object other) {
		if (!(other instanceof ImportDeclaration)) {
			return false;
		}
		ImportDeclaration o = (ImportDeclaration) other;
		return (
			safeSubtreeMatch(node.getName(), o.getName())
				&& node.isOnDemand() == o.isOnDemand());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(InfixExpression node, Object other) {
		if (!(other instanceof InfixExpression)) {
			return false;
		}
		InfixExpression o = (InfixExpression) other;
		// be careful not to trigger lazy creation of extended operand lists
		if (node.hasExtendedOperands() && o.hasExtendedOperands()) {
			if (!safeSubtreeListMatch(node.extendedOperands(), o.extendedOperands())) {
				return false;
			}
		}
		if (node.hasExtendedOperands() != o.hasExtendedOperands()) {
			return false;
		}
		return (
			node.getOperator().equals(o.getOperator())
				&& safeSubtreeMatch(node.getLeftOperand(), o.getLeftOperand())
				&& safeSubtreeMatch(node.getRightOperand(), o.getRightOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(InstanceofExpression node, Object other) {
		if (!(other instanceof InstanceofExpression)) {
			return false;
		}
		InstanceofExpression o = (InstanceofExpression) other;
		return (
				safeSubtreeMatch(node.getLeftOperand(), o.getLeftOperand())
				&& safeSubtreeMatch(node.getRightOperand(), o.getRightOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(Initializer node, Object other) {
		if (!(other instanceof Initializer)) {
			return false;
		}
		Initializer o = (Initializer) other;
		return (
			(node.getModifiers() == o.getModifiers())
				&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(Javadoc node, Object other) {
		if (!(other instanceof Javadoc)) {
			return false;
		}
		Javadoc o = (Javadoc) other;
		return safeEquals(node.getComment(), o.getComment());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(LabeledStatement node, Object other) {
		if (!(other instanceof LabeledStatement)) {
			return false;
		}
		LabeledStatement o = (LabeledStatement) other;
		return (
			safeSubtreeMatch(node.getLabel(), o.getLabel())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(MethodDeclaration node, Object other) {
		if (!(other instanceof MethodDeclaration)) {
			return false;
		}
		MethodDeclaration o = (MethodDeclaration) other;
		return (
			(node.getModifiers() == o.getModifiers())
				&& (node.isConstructor() == o.isConstructor())
				&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& safeSubtreeMatch(node.getReturnType(), o.getReturnType())
				&& safeSubtreeMatch(node.getName(), o.getName())
				&& safeSubtreeListMatch(node.parameters(), o.parameters())
				&& safeSubtreeListMatch(node.thrownExceptions(), o.thrownExceptions())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(MethodInvocation node, Object other) {
		if (!(other instanceof MethodInvocation)) {
			return false;
		}
		MethodInvocation o = (MethodInvocation) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getName(), o.getName())
				&& safeSubtreeListMatch(node.arguments(), o.arguments()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(NullLiteral node, Object other) {
		if (!(other instanceof NullLiteral)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(NumberLiteral node, Object other) {
		if (!(other instanceof NumberLiteral)) {
			return false;
		}
		NumberLiteral o = (NumberLiteral) other;
		return safeEquals(node.getToken(), o.getToken());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PackageDeclaration node, Object other) {
		if (!(other instanceof PackageDeclaration)) {
			return false;
		}
		PackageDeclaration o = (PackageDeclaration) other;
		return safeSubtreeMatch(node.getName(), o.getName());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ParenthesizedExpression node, Object other) {
		if (!(other instanceof ParenthesizedExpression)) {
			return false;
		}
		ParenthesizedExpression o = (ParenthesizedExpression) other;
		return safeSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PostfixExpression node, Object other) {
		if (!(other instanceof PostfixExpression)) {
			return false;
		}
		PostfixExpression o = (PostfixExpression) other;
		return (
			node.getOperator().equals(o.getOperator())
				&& safeSubtreeMatch(node.getOperand(), o.getOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PrefixExpression node, Object other) {
		if (!(other instanceof PrefixExpression)) {
			return false;
		}
		PrefixExpression o = (PrefixExpression) other;
		return (
			node.getOperator().equals(o.getOperator())
				&& safeSubtreeMatch(node.getOperand(), o.getOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PrimitiveType node, Object other) {
		if (!(other instanceof PrimitiveType)) {
			return false;
		}
		PrimitiveType o = (PrimitiveType) other;
		return (node.getPrimitiveTypeCode() == o.getPrimitiveTypeCode());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(QualifiedName node, Object other) {
		if (!(other instanceof QualifiedName)) {
			return false;
		}
		QualifiedName o = (QualifiedName) other;
		return (
			safeSubtreeMatch(node.getQualifier(), o.getQualifier())
				&& safeSubtreeMatch(node.getName(), o.getName()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ReturnStatement node, Object other) {
		if (!(other instanceof ReturnStatement)) {
			return false;
		}
		ReturnStatement o = (ReturnStatement) other;
		return safeSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SimpleName node, Object other) {
		if (!(other instanceof SimpleName)) {
			return false;
		}
		SimpleName o = (SimpleName) other;
		return node.getIdentifier().equals(o.getIdentifier());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SimpleType node, Object other) {
		if (!(other instanceof SimpleType)) {
			return false;
		}
		SimpleType o = (SimpleType) other;
		return safeSubtreeMatch(node.getName(), o.getName());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SingleVariableDeclaration node, Object other) {
		if (!(other instanceof SingleVariableDeclaration)) {
			return false;
		}
		SingleVariableDeclaration o = (SingleVariableDeclaration) other;
		return (
			(node.getModifiers() == o.getModifiers())
				&& safeSubtreeMatch(node.getType(), o.getType())
				&& safeSubtreeMatch(node.getName(), o.getName())
				&& safeSubtreeMatch(node.getInitializer(), o.getInitializer()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(StringLiteral node, Object other) {
		if (!(other instanceof StringLiteral)) {
			return false;
		}
		StringLiteral o = (StringLiteral) other;
		return safeEquals(node.getEscapedValue(), o.getEscapedValue());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SuperConstructorInvocation node, Object other) {
		if (!(other instanceof SuperConstructorInvocation)) {
			return false;
		}
		SuperConstructorInvocation o = (SuperConstructorInvocation) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeListMatch(node.arguments(), o.arguments()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SuperFieldAccess node, Object other) {
		if (!(other instanceof SuperFieldAccess)) {
			return false;
		}
		SuperFieldAccess o = (SuperFieldAccess) other;
		return (
			safeSubtreeMatch(node.getName(), o.getName())
				&& safeSubtreeMatch(node.getQualifier(), o.getQualifier()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SuperMethodInvocation node, Object other) {
		if (!(other instanceof SuperMethodInvocation)) {
			return false;
		}
		SuperMethodInvocation o = (SuperMethodInvocation) other;
		return (
			safeSubtreeMatch(node.getQualifier(), o.getQualifier())
				&& safeSubtreeMatch(node.getName(), o.getName())
				&& safeSubtreeListMatch(node.arguments(), o.arguments()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SwitchCase node, Object other) {
		if (!(other instanceof SwitchCase)) {
			return false;
		}
		SwitchCase o = (SwitchCase) other;
		return safeSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SwitchStatement node, Object other) {
		if (!(other instanceof SwitchStatement)) {
			return false;
		}
		SwitchStatement o = (SwitchStatement) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeListMatch(node.statements(), o.statements()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SynchronizedStatement node, Object other) {
		if (!(other instanceof SynchronizedStatement)) {
			return false;
		}
		SynchronizedStatement o = (SynchronizedStatement) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ThisExpression node, Object other) {
		if (!(other instanceof ThisExpression)) {
			return false;
		}
		ThisExpression o = (ThisExpression) other;
		return safeSubtreeMatch(node.getQualifier(), o.getQualifier());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ThrowStatement node, Object other) {
		if (!(other instanceof ThrowStatement)) {
			return false;
		}
		ThrowStatement o = (ThrowStatement) other;
		return safeSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TryStatement node, Object other) {
		if (!(other instanceof TryStatement)) {
			return false;
		}
		TryStatement o = (TryStatement) other;
		return (
			safeSubtreeMatch(node.getBody(), o.getBody())
				&& safeSubtreeListMatch(node.catchClauses(), o.catchClauses())
				&& safeSubtreeMatch(node.getFinally(), o.getFinally()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TypeDeclaration node, Object other) {
		if (!(other instanceof TypeDeclaration)) {
			return false;
		}
		TypeDeclaration o = (TypeDeclaration) other;
		return (
			(node.getModifiers() == o.getModifiers())
				&& (node.isInterface() == o.isInterface())
				&& safeSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& safeSubtreeMatch(node.getName(), o.getName())
				&& safeSubtreeMatch(node.getSuperclass(), o.getSuperclass())
				&& safeSubtreeListMatch(node.superInterfaces(), o.superInterfaces())
				&& safeSubtreeListMatch(node.bodyDeclarations(), o.bodyDeclarations()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TypeDeclarationStatement node, Object other) {
		if (!(other instanceof TypeDeclarationStatement)) {
			return false;
		}
		TypeDeclarationStatement o = (TypeDeclarationStatement) other;
		return safeSubtreeMatch(node.getTypeDeclaration(), o.getTypeDeclaration());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TypeLiteral node, Object other) {
		if (!(other instanceof TypeLiteral)) {
			return false;
		}
		TypeLiteral o = (TypeLiteral) other;
		return safeSubtreeMatch(node.getType(), o.getType());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(VariableDeclarationExpression node, Object other) {
		if (!(other instanceof VariableDeclarationExpression)) {
			return false;
		}
		VariableDeclarationExpression o = (VariableDeclarationExpression) other;
		return node.getModifiers() == o.getModifiers()
			&& safeSubtreeMatch(node.getType(), o.getType())
			&& safeSubtreeListMatch(node.fragments(), o.fragments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(VariableDeclarationFragment node, Object other) {
		if (!(other instanceof VariableDeclarationFragment)) {
			return false;
		}
		VariableDeclarationFragment o = (VariableDeclarationFragment) other;
		return safeSubtreeMatch(node.getName(), o.getName())
			&& node.getExtraDimensions() == o.getExtraDimensions()
			&& safeSubtreeMatch(node.getInitializer(), o.getInitializer());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(VariableDeclarationStatement node, Object other) {
		if (!(other instanceof VariableDeclarationStatement)) {
			return false;
		}
		VariableDeclarationStatement o = (VariableDeclarationStatement) other;
		return node.getModifiers() == o.getModifiers()
			&& safeSubtreeMatch(node.getType(), o.getType())
			&& safeSubtreeListMatch(node.fragments(), o.fragments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * 
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or 
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(WhileStatement node, Object other) {
		if (!(other instanceof WhileStatement)) {
			return false;
		}
		WhileStatement o = (WhileStatement) other;
		return (
			safeSubtreeMatch(node.getExpression(), o.getExpression())
				&& safeSubtreeMatch(node.getBody(), o.getBody()));
	}

}