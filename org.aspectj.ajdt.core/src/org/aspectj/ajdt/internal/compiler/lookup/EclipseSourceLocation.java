/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.io.File;

import org.aspectj.bridge.ISourceLocation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.problem.ProblemHandler;

public class EclipseSourceLocation implements ISourceLocation {
	CompilationResult result;
	int startPos, endPos;
	
	public EclipseSourceLocation(CompilationResult result, int startPos, int endPos) {
		super();
		this.result = result;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	public File getSourceFile() {
		return new File(new String(result.fileName));
	}

	public int getLine() {
		return ProblemHandler.searchLineNumber(result.lineSeparatorPositions, startPos);
	}

	public int getColumn() {
		return 0;  //XXX need better search above to get both
	}

	public int getEndLine() {
		return getLine();  //XXX no real need to do better
	}
	
    
    /** @return String {file:}line{:column} */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        if (getSourceFile() != ISourceLocation.NO_FILE) {
            sb.append(getSourceFile().getPath());
            sb.append(":");
        }
        sb.append("" + getLine());
        if (getColumn() != 0) {
            sb.append(":" + getColumn());
        }
        return sb.toString();
    }

}
