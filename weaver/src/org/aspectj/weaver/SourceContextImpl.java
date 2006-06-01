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


package org.aspectj.weaver;

import java.io.File;
import java.util.Arrays;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;

public class SourceContextImpl implements ISourceContext {
	
	private AbstractReferenceTypeDelegate delegate;
	private int[] lineBreaks;
	
	public SourceContextImpl(AbstractReferenceTypeDelegate delegate) {
		this.delegate = delegate;		
	}
	
	public void configureFromAttribute(String name,int []linebreaks) { 
		this.delegate.setSourcefilename(name);
		this.lineBreaks = linebreaks;
	}
	
	private File getSourceFile() {
		return new File(delegate.getSourcefilename());
	}
	
	public void tidy() {}
	
	public int getOffset() { return 0; }
		
		/*
		// AMC - a temporary "fudge" to give as much information as possible about the identity of the
		// source file this source location points to.
		String internalClassName = getEnclosingClass().getInternalClassName();
		String fileName = getEnclosingClass().getFileName();
		String extension = fileName.substring( fileName.lastIndexOf("."), fileName.length());
		String filePrefix = fileName.substring( 0, fileName.lastIndexOf("."));
		// internal class name is e.g. figures/Point, we don't know whether the file was
		// .aj or .java so we put it together with the file extension of the enclosing class
		// BUT... sometimes internalClassName is a different class (an aspect), so we only use it if it 
		// matches the file name.
		String mostAccurateFileNameGuess;
		if ( internalClassName.endsWith(filePrefix)) {
			mostAccurateFileNameGuess = internalClassName + extension;
		} else {
			mostAccurateFileNameGuess = fileName;
		}
		return new SourceLocation(new File(mostAccurateFileNameGuess), getSourceLine());
		*/



	public ISourceLocation makeSourceLocation(IHasPosition position) {
		if (lineBreaks != null) {
			int line = Arrays.binarySearch(lineBreaks, position.getStart());
			if (line < 0) line = -line;
			return new SourceLocation(getSourceFile(), line); //??? have more info
		} else {
			return new SourceLocation(getSourceFile(), 0);
		}
	}
	
	public ISourceLocation makeSourceLocation(int line, int offset) {
        if (line < 0) line = 0;
		SourceLocation sl = new SourceLocation(getSourceFile(), line);
        if (offset > 0) {
            sl.setOffset(offset);
        } else {
            if (lineBreaks != null) {
                int likelyOffset = 0;
                if (line > 0 && line < lineBreaks.length) {
                    //1st char of given line is next char after previous end of line
                    likelyOffset = lineBreaks[line-1] + 1;
                }
                sl.setOffset(likelyOffset);
            }
        }
        return sl;
	}


	public final static ISourceContext UNKNOWN_SOURCE_CONTEXT = new ISourceContext() {
		public ISourceLocation makeSourceLocation(IHasPosition position) {return null;}
		public ISourceLocation makeSourceLocation(int line, int offset) {return null;}
		public int getOffset() {return 0;}
		public void tidy() {}
	};
}
