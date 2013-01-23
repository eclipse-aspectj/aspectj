/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.weaver.patterns.IToken;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;

/**
 */
public class PseudoToken extends ASTNode implements IToken {
    public String value;
    public boolean isIdentifier;
    public String literalKind = null;
    public Parser parser;
	/**
	 * Constructor for PointcutDesignatorToken.
	 */
	public PseudoToken(Parser parser, String value, boolean isIdentifier) {
		this.parser = parser;
		this.value = value;
		this.isIdentifier = isIdentifier;
	}

	public String toString(int tab) {
		return "<" + value + "@" + getStart() + ":" + getEnd() + ">";
	}
	/**
	 * @see org.aspectj.weaver.patterns.IToken#getString()
	 */
	public String getString() {
		return value;
	}

	/**
	 * @see org.aspectj.weaver.patterns.IToken#isIdentifier()
	 */
	public boolean isIdentifier() {
		return isIdentifier;
	}
	
	/**
	 * returns null if this isn't a literal
	 */
	public String getLiteralKind() {
		return literalKind;
	}
	
	public Pointcut maybeGetParsedPointcut() {
		return null;
	}
	
	
	public int getStart() {
		return sourceStart;
	}
	
	/**
	 * 
	 */
	public int getEnd() {
		return sourceEnd;
	}
	
	public String getFileName() {
		return "unknown";
	}
	
	public int postParse(TypeDeclaration typeDec, MethodDeclaration enclosingDec, int tokenNumber) {
		// nothing to do typically
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		output.append("PseudoToken<" + getString() + ">");
		return output;
	}

}
