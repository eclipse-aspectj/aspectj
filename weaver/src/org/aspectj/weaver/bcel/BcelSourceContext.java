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


package org.aspectj.weaver.bcel;

import java.io.File;
import java.util.Arrays;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.IHasPosition;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.AjAttribute.SourceContextAttribute;

public class BcelSourceContext implements ISourceContext {
	private BcelObjectType inObject;
	private String sourceFileName;
	private int[] lineBreaks;
	
	public BcelSourceContext(BcelObjectType inObject) {
		this.inObject = inObject;
		sourceFileName = inObject.getJavaClass().getSourceFileName();
		
		String pname = inObject.getResolvedTypeX().getPackageName();
		if (pname != null) {
			sourceFileName = pname.replace('.', '/') + '/' + sourceFileName;
		}
	}
	
	private File getSourceFile() {
		//XXX make this work better borrowing code from below
		String fileName = sourceFileName;
		if (fileName == null) inObject.getJavaClass().getFileName();
		if (fileName == null) fileName = inObject.getResolvedTypeX().getName() + ".class";
		
		return new File(fileName);
	}
		
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
	
	public ISourceLocation makeSourceLocation(int line) {
		return new SourceLocation(getSourceFile(), line);
	}

	public void addAttributeInfo(SourceContextAttribute sourceContextAttribute) {
		this.sourceFileName = sourceContextAttribute.getSourceFileName();
		this.lineBreaks = sourceContextAttribute.getLineBreaks();
	}

}
