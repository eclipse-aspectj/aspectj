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
 * Abstract base class of AST nodes that represent expressions.
 * There are several kinds of expressions.
 * <p>
 * <pre>
 * Expression:
 *    Name
 *    IntegerLiteral (includes decimal, hex, and octal forms; and long)
 *    FloatingPointLiteral (includes both float and double)
 *    CharacterLiteral
 *    NullLiteral
 *    BooleanLiteral
 *    StringLiteral
 *    TypeLiteral
 *    ThisExpression
 *    SuperFieldAccess
 *    FieldAccess
 *    Assignment
 *    ParenthesizedExpression
 *    ClassInstanceCreation
 *    ArrayCreation
 *    ArrayInitializer
 *    MethodInvocation
 *    SuperMethodInvocation
 *    ArrayAccess
 *    InfixExpression
 *    InstanceofExpression
 *    ConditionalExpression
 *    PostfixExpression
 *    PrefixExpression
 *    CastExpression
 *    VariableDeclarationExpression
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public abstract class Expression extends ASTNode {
	
	/**
	 * Creates a new AST node for an expression owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Expression(AST ast) {
		super(ast);
	}
	
	/**
	 * Resolves and returns the binding for the type of this expression.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding for the type of this expression, or
	 *    <code>null</code> if the type cannot be resolved
	 */	
	public ITypeBinding resolveTypeBinding() {
		return getAST().getBindingResolver().resolveExpressionType(this);
	}
}

