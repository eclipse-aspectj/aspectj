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
	}
	

	public ISourceLocation makeSourceLocation(IHasPosition position) {
		String fileName = sourceFileName;
		if (fileName == null) inObject.getJavaClass().getFileName();
		if (fileName == null) fileName = inObject.getName() + ".class";
		
		if (lineBreaks != null) {
			int line = Arrays.binarySearch(lineBreaks, position.getStart());
			if (line < 0) line = -line;
			return new SourceLocation(new File(fileName), line); //??? have more info
		} else {
			return new SourceLocation(new File(fileName), 0);
		}
	}
	
	

	public void addAttributeInfo(SourceContextAttribute sourceContextAttribute) {
		this.sourceFileName = sourceContextAttribute.getSourceFileName();
		this.lineBreaks = sourceContextAttribute.getLineBreaks();
	}

}
