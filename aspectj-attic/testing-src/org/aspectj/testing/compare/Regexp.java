/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.compare;

import java.util.Vector;

/** Generalize regular expression interface (to avoid binding to regexp provider)*/
public interface Regexp {
    /** @return the substrings matched in argument by this regular expression */
    public Vector getGroups(String argument);

    /** @return true if argument is matched by this regular expression */
    public boolean matches(String argument);

    /** 
     * Set pattern used in this regular expression.
     * May throw Exception if the pattern can be determined to be illegal 
     * during initialization.
     * @throws Exception if pattern is illegal
     */
    public void setPattern(String pattern) throws Exception;

    /** 
     * @return a string representaion of the pattern 
     * (may not be legal or the input) 
     */
    public String getPattern() ;
}

