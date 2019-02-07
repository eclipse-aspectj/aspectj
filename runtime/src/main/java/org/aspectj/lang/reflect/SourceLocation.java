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


package org.aspectj.lang.reflect;

/** For defining code, the class defined and location in a source file. */
public interface SourceLocation {
    Class getWithinType();
    
    String getFileName();
    int getLine();
    
    /**
     * @deprecated can not be implemented for bytecode weaving, may
     * be removed in 1.1gold.
     * @return the column
     */
    int getColumn();
}

