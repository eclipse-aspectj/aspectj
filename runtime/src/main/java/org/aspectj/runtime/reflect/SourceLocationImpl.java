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


package org.aspectj.runtime.reflect;

import org.aspectj.lang.reflect.SourceLocation;

class SourceLocationImpl implements SourceLocation {
    Class withinType;
    String fileName;
    int line;
    
    SourceLocationImpl(Class withinType, String fileName, int line) {
        this.withinType = withinType;
        this.fileName = fileName;
        this.line = line;
    }
    
    public Class getWithinType() { return withinType; }
    public String getFileName() { return fileName; }
    public int getLine() { return line; }
    public int getColumn() { return -1; }
    
    public String toString() {
    	return getFileName() + ":" + getLine();
    }
}

