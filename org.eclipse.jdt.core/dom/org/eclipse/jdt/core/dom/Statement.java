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
 * Abstract base class of AST nodes that represent statements.
 * There are many kinds of statements.
 * <p>
 * The grammar combines both Statement and BlockStatement.
 * <pre>
 * Statement:
 *    Block
 *    IfStatement
 *    ForStatement
 *    WhileStatement
 *    DoStatement
 *    TryStatement
 *    SwitchStatement
 *    SynchronizedStatement
 *    ReturnStatement
 *    ThrowStatement
 *    BreakStatement
 *    ContinueStatement
 *    EmptyStatement
 *    ExpressionStatement
 *    LabeledStatement
 *    AssertStatement
 *    VariableDeclarationStatement
 *    TypeDeclarationStatement
 *    ConstructorInvocation
 *    SuperConstructorInvocation
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public abstract class Statement extends ASTNode {
	
	/**
	 * The leading comment, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private String optionalLeadingComment = null;
	
	/**
	 * Creates a new AST node for a statement owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Statement(AST ast) {
		super(ast);
	}
	
	/**
	 * Returns the leading comment string, including the starting
	 * and ending comment delimiters, and any embedded line breaks.
	 * <p>
	 * A leading comment is one that appears before the statement.
	 * It may be either an end-of-line or a multi-line comment.
	 * Multi-line comments may contain line breaks; end-of-line
	 * comments must not.
	 * </p>
	 * 
	 * @return the comment string, or <code>null</code> if none
	 */
	public String getLeadingComment() {
		return optionalLeadingComment;
	}

	/**
	 * Sets or clears the leading comment string. The comment
	 * string must include the starting and ending comment delimiters,
	 * and any embedded linebreaks.
	 * <p>
	 * A leading comment is one that appears before the statement.
	 * It may be either an end-of-line or a multi-line comment.
	 * Multi-line comments may contain line breaks; end-of-line
	 * comments must not.
	 * </p>
	 * <p>
	 * Examples:
	 * <code>
	 * <pre>
	 * setLeadingComment("/&#42; single-line comment &#42;/") - correct
	 * setLeadingComment("missing comment delimiters") - wrong!
	 * setLeadingComment("/&#42; unterminated comment ") - wrong!
	 * setLeadingComment("// end-of-line comment") - correct
	 * setLeadingComment("/&#42; multi-line\n comment &#42;/")  - correct
	 * setLeadingComment("// broken end-of-line\n comment ") - wrong!
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param comment the comment string, or <code>null</code> if none
	 * @exception IllegalArgumentException if the comment string is invalid
	 */
	public void setLeadingComment(String comment) {
		if (comment != null) {
			if (comment.startsWith("/*") && comment.endsWith("*/") && comment.length() >= 4) {//$NON-NLS-1$//$NON-NLS-2$
				// this is ok
			} else if (comment.startsWith("//") && comment.indexOf('\n') < 0) {//$NON-NLS-1$
				// this is ok too
			} else {
				// but anything else if not good
				throw new IllegalArgumentException();
			}
		}
		modifying();
		this.optionalLeadingComment = comment;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		String s = getLeadingComment();
		if (s != null) {
			size += HEADERS + 2 * 4 + HEADERS + 2 * s.length();
		}
		return size;
	}
}	

