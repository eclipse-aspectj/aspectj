/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.io.File;

import org.aspectj.ajdt.internal.core.builder.EclipseAdapterUtils;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemHandler;

public class EclipseSourceLocation implements ISourceLocation {
    private static String NO_CONTEXT = "USE_NULL--NO_CONTEXT_AVAILABLE";
	CompilationResult result;    
	int startPos, endPos;
    // lazy but final
    File file;
    int startLine = -1;
    int endLine = -1;
    int column = -1;
    String context;
	
	public EclipseSourceLocation(CompilationResult result, int startPos, int endPos) {
		super();
		this.result = result;
		this.startPos = startPos;
		this.endPos = endPos;
	}
	
	public CompilationResult getCompilationResult() {
		return result;
	}
	
	public int getOffset() {
		return startPos;
	}
	
	public int getStartPos() {
		return startPos;
	}
	
	public int getEndPos() {
		return endPos;
	}

	public File getSourceFile() {
		if (null == file) {
            if ((null == result) 
                || (null == result.fileName)
                || (0 == result.fileName.length)) {
                file = ISourceLocation.NO_FILE;
            } else {
                file = new File(new String(result.fileName)); 
            }
        }
        return file;
	}

	public int getLine() {
		if (-1 == startLine) {
            startLine = ProblemHandler.searchLineNumber(result.lineSeparatorPositions, startPos);
        }
        return startLine;
	}

	public int getColumn() {
        if (-1 == column) {
            int lineNumber = getLine();
            // JJH added check that lineNumber is in legal range to avoid exceptions
            if (0 < lineNumber && lineNumber < result.lineSeparatorPositions.length) {
                int lineStart = result.lineSeparatorPositions[lineNumber];
                int col = startPos - lineStart; // 1-based
                if (0 <= col) { 
                    column = col;
                } else {
                    column = 0;
                }
            }
        }
		return column;
	}

	public int getEndLine() {
        if (-1 == endLine) {
            endLine = ProblemHandler.searchLineNumber(result.lineSeparatorPositions, endPos);
        }
        return endLine;
	}
    
    public String getContext() {
        if (null == context) {
            ICompilationUnit compilationUnit = result.compilationUnit;
            IProblem[] problems = result.problems;
            if ((null == compilationUnit) || (null == problems)
                || (1 != problems.length)) { // ?? which of n>1 problems?
                context = NO_CONTEXT;
            } else {
                context = EclipseAdapterUtils.makeLocationContext(compilationUnit, problems[0]);
            }
        }
        return (NO_CONTEXT == context ? null : context);
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
        if (getOffset()>=0) { sb.append("::").append(getOffset()); }
        return sb.toString();
    }
}
