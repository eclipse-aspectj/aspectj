/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

// todo: non-distribution license?

package org.aspectj.testing.util;

/**
 * Visitor interface for String
*/
public interface StringVisitor {
    /**
     * @param input the String to evaluate - may be null
     * @return true if input is accepted and/or process should continue
     */
	boolean accept(String input);
}

