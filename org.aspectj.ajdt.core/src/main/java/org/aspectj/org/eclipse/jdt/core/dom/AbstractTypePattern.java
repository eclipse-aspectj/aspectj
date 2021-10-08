/********************************************************************
 * Copyright (c) 2006, 2010 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - iniital version
 *               Nieraj Singh
 *******************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;

/**
 * abstract TypePattern DOM AST node.
 */
public abstract class AbstractTypePattern extends PatternNode {

	private String typePatternExpression;

	public static final String EMPTY_EXPRESSION = "";

	AbstractTypePattern(AST ast) {
		super(ast);
	}

	AbstractTypePattern(AST ast, String typePatternExpression) {
		super(ast);
		this.typePatternExpression = typePatternExpression;
	}

	/**
	 * Should be called for internal setting only, if the expression needs to be set
	 * lazily
	 * @param typePatternExpression
	 */
	protected void setTypePatternExpression(String typePatternExpression) {
		this.typePatternExpression = typePatternExpression;
	}

	/**
	 * Return the type pattern in expression form (String representation). In
	 * many cases, this is not null, although it may be null in some cases like
	 * the NoTypePattern
	 *
	 * @return String expression of the type pattern. May be null.
	 */
	public String getTypePatternExpression() {
		return typePatternExpression;
	}

	int treeSize() {
		return memSize();
	}

}
