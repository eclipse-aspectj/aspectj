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
import java.util.List;
import java.util.Map;

/**
 * Infix expression AST node type.
 *
 * Range 0: first character of left operand expression through last character
 * of the last extended operand expression. If there are no extended operands,
 * the range ends after the right operand expression.
 *
 * <pre>
 * InfixExpression:
 *    Expression InfixOperator Expression { InfixOperator Expression } 
 * </pre>
 * 
 * @since 2.0
 */
public class InfixExpression extends Expression {

	/**
 	 * Infix operators (typesafe enumeration).
 	 * <pre>
	 * InfixOperator:<code>
	 *    <b>*</b>	TIMES
	 *    <b>/</b>  DIVIDE
	 *    <b>%</b>  REMAINDER
	 *    <b>+</b>  PLUS
	 *    <b>-</b>  MINUS
	 *    <b>&lt;&lt;</b>  LEFT_SHIFT
	 *    <b>&gt;&gt;</b>  RIGHT_SHIFT_SIGNED
	 *    <b>&gt;&gt;&gt;</b>  RIGHT_SHIFT_UNSIGNED
	 *    <b>&lt;</b>  LESS
	 *    <b>&gt;</b>  GREATER
	 *    <b>&lt;=</b>  LESS_EQUALS
	 *    <b>&gt;=</b>  GREATER_EQUALS
	 *    <b>==</b>  EQUALS
	 *    <b>!=</b>  NOT_EQUALS
	 *    <b>^</b>  XOR
	 *    <b>&amp;</b>  AND
	 *    <b>|</b>  OR
	 *    <b>&amp;&amp;</b>  CONDITIONAL_AND
	 *    <b>||</b>  CONDITIONAL_OR</code>
	 * </pre>
	 */
	public static class Operator {
	
		/**
		 * The token for the operator.
		 */
		private String token;
		
		/**
		 * Creates a new infix operator with the given token.
		 * <p>
		 * Note: this constructor is private. The only instances
		 * ever created are the ones for the standard operators.
		 * </p>
		 * 
		 * @param token the character sequence for the operator
		 */
		private Operator(String token) {
			this.token = token;
		}
		
		/**
		 * Returns the character sequence for the operator.
		 * 
		 * @return the character sequence for the operator
		 */
		public String toString() {
			return token;
		}
		
		/** Multiplication "*" operator. */
		public static final Operator TIMES = new Operator("*");//$NON-NLS-1$
		/** Division "/" operator. */
		public static final Operator DIVIDE = new Operator("/");//$NON-NLS-1$
		/** Remainder "%" operator. */
		public static final Operator REMAINDER = new Operator("%");//$NON-NLS-1$
		/** Addition (or string concatenation) "+" operator. */
		public static final Operator PLUS = new Operator("+");//$NON-NLS-1$
		/** Subtraction "-" operator. */
		public static final Operator MINUS = new Operator("-");//$NON-NLS-1$
		/** Left shift "&lt;&lt;" operator. */
		public static final Operator LEFT_SHIFT = new Operator("<<");//$NON-NLS-1$
		/** Signed right shift "&gt;&gt;" operator. */
		public static final Operator RIGHT_SHIFT_SIGNED = new Operator(">>");//$NON-NLS-1$
		/** Unsigned right shift "&gt;&gt;&gt;" operator. */
		public static final Operator RIGHT_SHIFT_UNSIGNED = 
			new Operator(">>>");//$NON-NLS-1$
		/** Less than "&lt;" operator. */
		public static final Operator LESS = new Operator("<");//$NON-NLS-1$
		/** Greater than "&gt;" operator. */
		public static final Operator GREATER = new Operator(">");//$NON-NLS-1$
		/** Less than or equals "&lt;=" operator. */
		public static final Operator LESS_EQUALS = new Operator("<=");//$NON-NLS-1$
		/** Greater than or equals "&gt=;" operator. */
		public static final Operator GREATER_EQUALS = new Operator(">=");//$NON-NLS-1$
		/** Equals "==" operator. */
		public static final Operator EQUALS = new Operator("==");//$NON-NLS-1$
		/** Not equals "!=" operator. */
		public static final Operator NOT_EQUALS = new Operator("!=");//$NON-NLS-1$
		/** Exclusive OR "^" operator. */
		public static final Operator XOR = new Operator("^");//$NON-NLS-1$
		/** Inclusive OR "|" operator. */
		public static final Operator OR = new Operator("|");//$NON-NLS-1$
		/** AND "&amp;" operator. */
		public static final Operator AND = new Operator("&");//$NON-NLS-1$
		/** Conditional OR "||" operator. */
		public static final Operator CONDITIONAL_OR = new Operator("||");//$NON-NLS-1$
		/** Conditional AND "&amp;&amp;" operator. */
		public static final Operator CONDITIONAL_AND = new Operator("&&");//$NON-NLS-1$
		
		/**
		 * Map from token to operator (key type: <code>String</code>;
		 * value type: <code>Operator</code>).
		 */
		private static final Map CODES;
		static {
			CODES = new HashMap(20);
			Operator[] ops = {
					TIMES,
					DIVIDE,
					REMAINDER,
					PLUS,
					MINUS,
					LEFT_SHIFT,
					RIGHT_SHIFT_SIGNED,
					RIGHT_SHIFT_UNSIGNED,
					LESS,
					GREATER,
					LESS_EQUALS,
					GREATER_EQUALS,
					EQUALS,
					NOT_EQUALS,
					XOR,
					OR,
					AND,
					CONDITIONAL_OR,
					CONDITIONAL_AND,
				};
			for (int i = 0; i < ops.length; i++) {
				CODES.put(ops[i].toString(), ops[i]);
			}
		}

		/**
		 * Returns the infix operator corresponding to the given string,
		 * or <code>null</code> if none.
		 * <p>
		 * <code>toOperator</code> is the converse of <code>toString</code>:
		 * that is, <code>Operator.toOperator(op.toString()) == op</code> for 
		 * all operators <code>op</code>.
		 * </p>
		 * 
		 * @param token the character sequence for the operator
		 * @return the infix operator, or <code>null</code> if none
		 */
		public static Operator toOperator(String token) {
			return (Operator) CODES.get(token);
		}
		
	}
	
	/**
	 * The infix operator; defaults to InfixExpression.Operator.PLUS.
	 */
	private InfixExpression.Operator operator = InfixExpression.Operator.PLUS;

	/**
	 * The left operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression leftOperand = null;

	/**
	 * The right operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression rightOperand = null;

	/**
	 * The list of extended operand expressions (element type: 
	 * <code>Expression</code>). Lazily initialized; defaults to an empty list.
	 */
	private ASTNode.NodeList extendedOperands = null;

	/**
	 * Creates a new AST node for an infix expression owned by the given 
	 * AST. By default, the node has unspecified (but legal) operator,
	 * left and right operands, and an empty list of additional operands.
	 * 
	 * @param ast the AST that is to own this node
	 */
	InfixExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return INFIX_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		InfixExpression result = new InfixExpression(target);
		result.setOperator(getOperator());
		result.setLeftOperand((Expression) getLeftOperand().clone(target));
		result.setRightOperand((Expression) getRightOperand().clone(target));
		if (extendedOperands != null) {
			// be careful not to trigger lazy creation of list
			result.extendedOperands().addAll(
				ASTNode.copySubtrees(target, extendedOperands()));
		}
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
			acceptChild(visitor, getLeftOperand());
			acceptChild(visitor, getRightOperand());
			if (extendedOperands != null) {
				// be careful not to trigger lazy creation of list
				acceptChildren(visitor, extendedOperands);
			}
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the operator of this infix expression.
	 * 
	 * @return the infix operator
	 */ 
	public InfixExpression.Operator getOperator() {
		return operator;
	}

	/**
	 * Sets the operator of this infix expression.
	 * 
	 * @param operator the infix operator
	 * @exception IllegalArgumentException if the argument is incorrect
	 */ 
	public void setOperator(InfixExpression.Operator operator) {
		if (operator == null) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.operator = operator;
	}

	/**
	 * Returns the left operand of this infix expression.
	 * 
	 * @return the left operand node
	 */ 
	public Expression getLeftOperand() {
		if (leftOperand  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setLeftOperand(new SimpleName(getAST()));
		}
		return leftOperand;
	}
		
	/**
	 * Sets the left operand of this infix expression.
	 * 
	 * @param expression the left operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setLeftOperand(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an InfixExpression may occur inside a Expression - must check cycles
		replaceChild(this.leftOperand, expression, true);
		this.leftOperand = expression;
	}

	/**
	 * Returns the right operand of this infix expression.
	 * 
	 * @return the right operand node
	 */ 
	public Expression getRightOperand() {
		if (rightOperand  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setRightOperand(new SimpleName(getAST()));
		}
		return rightOperand;
	}
		
	/**
	 * Sets the right operand of this infix expression.
	 * 
	 * @param expression the right operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setRightOperand(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an InfixExpression may occur inside a Expression - must check cycles
		replaceChild(this.rightOperand, expression, true);
		this.rightOperand = expression;
	}
	
	/**
	 * Returns where there are any extended operands.
	 * 
	 * @return <code>true</code> if there are one or more extended operands,
	 *    and <code>false</code> if there are no extended operands
	 */
	public boolean hasExtendedOperands() {
		return 
			(extendedOperands != null) && extendedOperands.size() > 0;
	}
	
	/**
	 * Returns the live list of extended operands.
	 * <p>
	 * The extended operands is the preferred way of representing deeply nested
	 * expressions of the form <code>L op R op R2 op R3...</code> where
	 * the same operator appears between all the operands (the most 
	 * common case being lengthy string concatenation expressions). Using
	 * the extended operands keeps the trees from getting too deep; this
	 * decreases the risk is running out of thread stack space at runtime
	 * when traversing such trees.
	 * ((a + b) + c) + d would be translated to:
	 * 	leftOperand: a
	 * 	rightOperand: b
	 * 	extendedOperands: {c, d}
	 * 	operator: +
	 * </p>
	 * 
	 * @return the live list of extended operands
	 *   (element type: <code>Expression</code>)
	 */
	public List extendedOperands() {
		if (extendedOperands == null) {
			// lazily initialize
			extendedOperands = new ASTNode.NodeList(true, Expression.class);
		}
		return extendedOperands;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 4 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (leftOperand == null ? 0 : getLeftOperand().treeSize())
			+ (rightOperand == null ? 0 : getRightOperand().treeSize())
			+ (extendedOperands == null ? 0 : extendedOperands.listSize());
	}
}
