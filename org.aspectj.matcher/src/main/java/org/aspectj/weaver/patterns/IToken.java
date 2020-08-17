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


package org.aspectj.weaver.patterns;

import org.aspectj.weaver.IHasPosition;

public interface IToken extends IHasPosition {
	IToken EOF = BasicToken.makeOperator("<eof>", 0, 0);

	/**
	 * Returns the string value of this token.
	 *
	 * If isIdentifier is false, then this string must be intern'd
	 * so that == matching can be used.
	 *
	 * If isIdentifier is true, interning is not required.
	 */
	String getString();

    /**
     * Whether this should be treated as a token or a generic identifier
     */
	boolean isIdentifier();

    /**
     * Whether this should be treated as a literal value
     *
     * Kinds == "string", ???
     *
     * returns null if this isn't a literal
     */
	String getLiteralKind();


    /**
     * If this token represents a pre-parsed Pointcut, then return it;
     * otherwise returns null.
     *
     * Needed for the implementation of 'if'
     */
	Pointcut maybeGetParsedPointcut();
}
