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

import java.util.HashMap;
import java.util.Map;

/**
 * Assignment expression AST node type.
 *
 * <pre>
 * Assignment:
 *    Expression AssignmentOperator Expression
 * </pre>
 * 
 * @since 2.0
 */
public class Assignment extends Expression {
		
	/**
 	 * Assignment operators (typesafe enumeration).
	 * <pre>
	 * AssignmentOperator:<code>
	 *    <b>=</b> ASSIGN
	 *    <b>+=</b> PLUS_ASSIGN
	 *    <b>-=</b> MINUS_ASSIGN
	 *    <b>*=</b> TIMES_ASSIGN
	 *    <b>/=</b> DIVIDE_ASSIGN
	 *    <b>&amp;=</b> BIT_AND_ASSIGN
	 *    <b>|=</b> BIT_OR_ASSIGN
	 *    <b>^=</b> BIT_XOR_ASSIGN
	 *    <b>%=</b> REMAINDER_ASSIGN
	 *    <b>&lt;&lt;=</b> LEFT_SHIFT_ASSIGN
	 *    <b>&gt;&gt;=</b> RIGHT_SHIFT_SIGNED_ASSIGN
	 *    <b>&gt;&gt;&gt;=</b> RIGHT_SHIFT_UNSIGNED_ASSIGN</code>
	 * </pre>
	 */
	public static class Operator {
	
		/**
		 * The name of the operator
		 */
		private String op;
		
		/**
		 * Creates a new assignment operator with the given name.
		 * <p>
		 * Note: this constructor is private. The only instances
		 * ever created are the ones for the standard operators.
		 * </p>
		 * 
		 * @param op the character sequence for the operator
		 */
		private Operator(String op) {
			this.op = op;
		}
		
		/**
		 * Returns the character sequence for the operator.
		 * 
		 * @return the character sequence for the operator
		 */
		public String toString() {
			return op;
		}
		
		/** = operator. */
		public static final Operator ASSIGN = new Operator("=");//$NON-NLS-1$
		/** += operator. */
		public static final Operator PLUS_ASSIGN = new Operator("+=");//$NON-NLS-1$
		/** -= operator. */
		public static final Operator MINUS_ASSIGN = new Operator("-=");//$NON-NLS-1$
		/** *= operator. */
		public static final Operator TIMES_ASSIGN = new Operator("*=");//$NON-NLS-1$
		/** /= operator. */
		public static final Operator DIVIDE_ASSIGN = new Operator("/=");//$NON-NLS-1$
		/** &amp;= operator. */
		public static final Operator BIT_AND_ASSIGN = new Operator("&=");//$NON-NLS-1$
		/** |= operator. */
		public static final Operator BIT_OR_ASSIGN = new Operator("|=");//$NON-NLS-1$
		/** ^= operator. */
		public static final Operator BIT_XOR_ASSIGN = new Operator("^=");//$NON-NLS-1$
		/** %= operator. */
		public static final Operator REMAINDER_ASSIGN = new Operator("%=");//$NON-NLS-1$
		/** &lt;&lt;== operator. */
		public static final Operator LEFT_SHIFT_ASSIGN =
			new Operator("<<=");//$NON-NLS-1$
		/** &gt;&gt;== operator. */
		public static final Operator RIGHT_SHIFT_SIGNED_ASSIGN =
			new Operator(">>=");//$NON-NLS-1$
		/** &gt;&gt;&gt;== operator. */
		public static final Operator RIGHT_SHIFT_UNSIGNED_ASSIGN =
			new Operator(">>>=");//$NON-NLS-1$
		
		/**
		 * Returns the assignment operator corresponding to the given string,
		 * or <code>null</code> if none.
		 * <p>
		 * <code>toOperator</code> is the converse of <code>toString</code>:
		 * that is, <code>Operator.toOperator(op.toString()) == op</code> for all 
		 * operators <code>op</code>.
		 * </p>
		 * 
		 * @param token the character sequence for the operator
		 * @return the assignment operator, or <code>null</code> if none
		 */
		public static Operator toOperator(String token) {
			return (Operator) CODES.get(token);
		}
		
		/**
		 * Map from token to operator (key type: <code>String</code>;
		 * value type: <code>Operator</code>).
		 */
		private static final Map CODES;
		static {
			CODES = new HashMap(20);
			Operator[] ops = {
					ASSIGN,
					PLUS_ASSIGN,
					MINUS_ASSIGN,
					TIMES_ASSIGN,
					DIVIDE_ASSIGN,
					BIT_AND_ASSIGN,
					BIT_OR_ASSIGN,
					BIT_XOR_ASSIGN,
					REMAINDER_ASSIGN,
					LEFT_SHIFT_ASSIGN,
					RIGHT_SHIFT_SIGNED_ASSIGN,
					RIGHT_SHIFT_UNSIGNED_ASSIGN
				};
			for (int i = 0; i < ops.length; i++) {
				CODES.put(ops[i].toString(), ops[i]);
			}
		}
	}
	
	/**
	 * The assignment operator; defaults to Assignment.Operator.ASSIGN
	 */
	private Assignment.Operator assignmentOperator = Assignment.Operator.ASSIGN;

	/**
	 * The left hand side; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression leftHandSide = null;

	/**
	 * The right hand side; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression rightHandSide = null;

	/**
	 * Creates a new AST node for an assignment expression owned by the given 
	 * AST. By default, the node has an assignment operator, and unspecified
	 * left and right hand sides.
	 * 
	 * @param ast the AST that is to own this node
	 */
	Assignment(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ASSIGNMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		Assignment result = new Assignment(target);
		result.setOperator(getOperator());
		result.setLeftHandSide((Expression) getLeftHandSide().clone(target));
		result.setRightHandSide((Expression) getRightHandSide().clone(target));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getLeftHandSide());
			acceptChild(visitor, getRightHandSide());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the operator of this assignment expression.
	 * 
	 * @return the assignment operator
	 */ 
	public Assignment.Operator getOperator() {
		return assignmentOperator;
	}

	/**
	 * Sets the operator of this assignment expression.
	 * 
	 * @param assignmentOperator the assignment operator
	 * @exception IllegalArgumentException if the argument is incorrect
	 */ 
	public void setOperator(Assignment.Operator assignmentOperator) {
		if (assignmentOperator == null) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.assignmentOperator = assignmentOperator;
	}

	/**
	 * Returns the left hand side of this assignment expression.
	 * 
	 * @return the left hand side node
	 */ 
	public Expression getLeftHandSide() {
		if (leftHandSide  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setLeftHandSide(new SimpleName(getAST()));
		}
		return leftHandSide;
	}
		
	/**
	 * Sets the left hand side of this assignment expression.
	 * 
	 * @param expression the left hand side node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setLeftHandSide(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an Assignment may occur inside a Expression - must check cycles
		replaceChild((ASTNode) this.leftHandSide, (ASTNode) expression, true);
		this.leftHandSide = expression;
	}

	/**
	 * Returns the right hand side of this assignment expression.
	 * 
	 * @return the right hand side node
	 */ 
	public Expression getRightHandSide() {
		if (rightHandSide  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setRightHandSide(new SimpleName(getAST()));
		}
		return rightHandSide;
	}
		
	/**
	 * Sets the right hand side of this assignment expression.
	 * 
	 * @param expression the right hand side node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setRightHandSide(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an Assignment may occur inside a Expression - must check cycles
		replaceChild((ASTNode) this.rightHandSide, (ASTNode) expression, true);
		this.rightHandSide = expression;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (leftHandSide == null ? 0 : getLeftHandSide().treeSize())
			+ (rightHandSide == null ? 0 : getRightHandSide().treeSize());
	}
}

