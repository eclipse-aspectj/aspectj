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
 * Prefix expression AST node type.
 *
 * <pre>
 * PrefixExpression:
 *    PrefixOperator Expression 
 * </pre>
 * 
 * @since 2.0
 */
public class PrefixExpression extends Expression {

	/**
 	 * Prefix operators (typesafe enumeration).
	 * <pre>
	 * PrefixOperator:
	 *    <b><code>++</code></b>  <code>INCREMENT</code>
	 *    <b><code>--</code></b>  <code>DECREMENT</code>
	 *    <b><code>+</code></b>  <code>PLUS</code>
	 *    <b><code>-</code></b>  <code>MINUS</code>
	 *    <b><code>~</code></b>  <code>COMPLEMENT</code>
	 *    <b><code>!</code></b>  <code>NOT</code>
	 * </pre>
	 */
	public static class Operator {
	
		/**
		 * The token for the operator.
		 */
		private String token;
		
		/**
		 * Creates a new prefix operator with the given token.
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
		
		/** Prefix increment "++" operator. */
		public static final Operator INCREMENT = new Operator("++");//$NON-NLS-1$
		/** Prefix decrement "--" operator. */
		public static final Operator DECREMENT = new Operator("--");//$NON-NLS-1$
		/** Unary plus "+" operator. */
		public static final Operator PLUS = new Operator("+");//$NON-NLS-1$
		/** Unary minus "-" operator. */
		public static final Operator MINUS = new Operator("-");//$NON-NLS-1$
		/** Bitwise complement "~" operator. */
		public static final Operator COMPLEMENT = new Operator("~");//$NON-NLS-1$
		/** Logical complement "!" operator. */
		public static final Operator NOT = new Operator("!");//$NON-NLS-1$
		
		/**
		 * Map from token to operator (key type: <code>String</code>;
		 * value type: <code>Operator</code>).
		 */
		private static final Map CODES;
		static {
			CODES = new HashMap(20);
			Operator[] ops = {
					INCREMENT,
					DECREMENT,
					PLUS,
					MINUS,
					COMPLEMENT,
					NOT,
				};
			for (int i = 0; i < ops.length; i++) {
				CODES.put(ops[i].toString(), ops[i]);
			}
		}

		/**
		 * Returns the prefix operator corresponding to the given string,
		 * or <code>null</code> if none.
		 * <p>
		 * <code>toOperator</code> is the converse of <code>toString</code>:
		 * that is, <code>Operator.toOperator(op.toString()) == op</code> for 
		 * all operators <code>op</code>.
		 * </p>
		 * 
		 * @param token the character sequence for the operator
		 * @return the prefix operator, or <code>null</code> if none
		 */
		public static Operator toOperator(String token) {
			return (Operator) CODES.get(token);
		}
	}
	
	/**
	 * The operator; defaults to an unspecified prefix operator.
	 */
	private PrefixExpression.Operator operator = 
		PrefixExpression.Operator.PLUS;

	/**
	 * The operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression operand = null;

	/**
	 * Creates a new AST node for an prefix expression owned by the given 
	 * AST. By default, the node has unspecified (but legal) operator and 
	 * operand.
	 * 
	 * @param ast the AST that is to own this node
	 */
	PrefixExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return PREFIX_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		PrefixExpression result = new PrefixExpression(target);
		result.setOperator(getOperator());
		result.setOperand((Expression) getOperand().clone(target));
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
			acceptChild(visitor, getOperand());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the operator of this prefix expression.
	 * 
	 * @return the operator
	 */ 
	public PrefixExpression.Operator getOperator() {
		return operator;
	}

	/**
	 * Sets the operator of this prefix expression.
	 * 
	 * @param operator the operator
	 * @exception IllegalArgumentException if the argument is incorrect
	 */ 
	public void setOperator(PrefixExpression.Operator operator) {
		if (operator == null) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.operator = operator;
	}

	/**
	 * Returns the operand of this prefix expression.
	 * 
	 * @return the operand expression node
	 */ 
	public Expression getOperand() {
		if (operand  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setOperand(new SimpleName(getAST()));
		}
		return operand;
	}
		
	/**
	 * Sets the operand of this prefix expression.
	 * 
	 * @param expression the operand expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setOperand(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// a PrefixExpression may occur inside a Expression - must check cycles
		replaceChild(this.operand, expression, true);
		this.operand = expression;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (operand == null ? 0 : getOperand().treeSize());
	}
}

