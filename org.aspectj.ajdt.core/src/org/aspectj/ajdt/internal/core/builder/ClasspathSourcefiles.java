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


package org.aspectj.ajdt.internal.core.builder;

import java.util.List;

import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;

public class ClasspathSourcefiles extends ClasspathLocation {
	private List/*File*/ files;
	private String[] knownFileNames;
	
	public ClasspathSourcefiles(List files) {
		this.files = files;
	}
	
	//XXX this doesn't always work
	public NameEnvironmentAnswer findClass(
		String binaryFileName,
		String qualifiedPackageName,
		String qualifiedBinaryFileName)
	{
		return null;
	}

	public boolean isPackage(String qualifiedPackageName) {
		return false;
	}

}
