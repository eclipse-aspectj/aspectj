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
 * Field access expression AST node type.
 *
 * <pre>
 * FieldAccess: 
 * 		Expression <b>.</b> Identifier
 * </pre>
 */
public class FieldAccess extends Expression {
	
	/**
	 * The expression; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression expression = null;

	/**
	 * The field; lazily initialized; defaults to an unspecified,
	 * but legal, simple field name.
	 */
	private SimpleName fieldName = null;

	/**
	 * Creates a new unparented node for a field access expression owned by the
	 * given AST. By default, the expression and field are both unspecified,
	 * but legal, names.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	FieldAccess(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return FIELD_ACCESS;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		FieldAccess result = new FieldAccess(target);
		result.setExpression((Expression) getExpression().clone(target));
		result.setName((SimpleName) getName().clone(target));
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
			acceptChild(visitor, getExpression());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this field access expression.
	 * 
	 * @return the expression node
	 */ 
	public Expression getExpression() {
		if (expression == null) {
			// lazy initialize - use setter to ensure parent link set too
			setExpression(new SimpleName(getAST()));
		}
		return expression;
	}
		
	/**
	 * Sets the expression of this field access expression.
	 * 
	 * @param expression the new expression
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// a FieldAccess may occur inside an Expression - must check cycles
		replaceChild((ASTNode) this.expression, (ASTNode) expression, true);
		this.expression = expression;
	}

	/**
	 * Returns the name of the field accessed in this field access expression.
	 * 
	 * @return the field name
	 */ 
	public SimpleName getName() {
		if (fieldName == null) {
			setName(new SimpleName(getAST()));
		}
		return fieldName;
	}
		
	/**
	 * Sets the name of the field accessed in this field access expression.
	 * 
	 * @param fieldName the field name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName fieldName) {
		if (fieldName == null) {
			throw new IllegalArgumentException();
		}
		// a FieldAccess cannot occur inside a SimpleName
		replaceChild(this.fieldName, fieldName, false);
		this.fieldName = fieldName;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (expression == null ? 0 : getExpression().treeSize())
			+ (fieldName == null ? 0 : getName().treeSize());
	}
}

