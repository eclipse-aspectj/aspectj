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


package org.aspectj.runtime.reflect;

import org.aspectj.lang.reflect.SourceLocation;

class SourceLocationImpl implements SourceLocation {
    Class withinType;
    String fileName;
    int line;
    int column;
    
    SourceLocationImpl(Class withinType, String fileName, int line, int column) {
        this.withinType = withinType;
        this.fileName = fileName;
        this.line = line;
        this.column = column;
    }
    
    public Class getWithinType() { return withinType; }
    public String getFileName() { return fileName; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    
    public String toString() {
    	return getFileName() + ":" + getLine() +
    		((getColumn() == -1) ? ""  : ":" + getColumn());
    }
}

