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

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;

/**
 * Number literal nodes.
 * 
 * @since 2.0
 */
public class NumberLiteral extends Expression {

	/**
	 * The token string; defaults to the integer literal "0".
	 */
	private String tokenValue = "0";//$NON-NLS-1$

	/**
	 * Creates a new unparented number literal node owned by the given AST.
	 * By default, the number literal is the token "<code>0</code>".
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	NumberLiteral(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return NUMBER_LITERAL;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		NumberLiteral result = new NumberLiteral(target);
		result.setToken(getToken());
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
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the token of this number literal node. The value is the sequence
	 * of characters that would appear in the source program.
	 * 
	 * @return the numeric literal token
	 */ 
	public String getToken() {
		return tokenValue;
	}
		
	/**
	 * Sets the token of this number literal node. The value is the sequence
	 * of characters that would appear in the source program.
	 * 
	 * @param token the numeric literal token
	 * @exception IllegalArgumentException if the argument is incorrect
	 */ 
	public void setToken(String token) {
		if (token == null || token.length() == 0) {
			throw new IllegalArgumentException();
		}
		Scanner scanner = getAST().scanner;
		char[] source = token.toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length);
		try {
			int tokenType = scanner.getNextToken();
			switch(tokenType) {
				case Scanner.TokenNameDoubleLiteral:
				case Scanner.TokenNameIntegerLiteral:
				case Scanner.TokenNameFloatingPointLiteral:
				case Scanner.TokenNameLongLiteral:
					break;
				case Scanner.TokenNameMINUS :
					tokenType = scanner.getNextToken();
					switch(tokenType) {
						case Scanner.TokenNameDoubleLiteral:
						case Scanner.TokenNameIntegerLiteral:
						case Scanner.TokenNameFloatingPointLiteral:
						case Scanner.TokenNameLongLiteral:
							break;
						default:
							throw new IllegalArgumentException();
					}
					break;		
				default:
					throw new IllegalArgumentException();
			}
		} catch(InvalidInputException e) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.tokenValue = token;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		if (tokenValue != null) {
			size += HEADERS + 2 * 4 + HEADERS + 2 * tokenValue.length();
		}
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}
