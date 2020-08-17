/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

