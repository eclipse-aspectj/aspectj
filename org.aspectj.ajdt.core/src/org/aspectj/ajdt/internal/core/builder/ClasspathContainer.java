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


package org.aspectj.ajdt.internal.core.builder;

import org.eclipse.jdt.internal.core.builder.*;
import org.aspectj.util.FileUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.Util;

import java.io.*;

class ClasspathContainer extends ClasspathLocation {

	IContainer container;

	ClasspathContainer(IContainer container) {
		this.container = container;
	}

	public void cleanup() {	
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ClasspathContainer))
			return false;

		return container.equals(((ClasspathContainer) o).container);
	}

	public NameEnvironmentAnswer findClass(
		String binaryFileName,
		String qualifiedPackageName,
		String qualifiedBinaryFileName)
	{
		IFile file = container.getFile(makePath(qualifiedBinaryFileName));
		if (!file.exists()) return null;
		
		try {
			byte[] classFileBytes = Util.getInputStreamAsByteArray(file.getContents(), -1);
			ClassFileReader reader =
			    new ClassFileReader(classFileBytes, file.getFullPath().toString().toCharArray());
			return new NameEnvironmentAnswer(reader);
		} catch (Exception e) {
		} // treat as if class file is missing
		return null;
	}

	public boolean isPackage(String qualifiedPackageName) {
		return container.getFolder(makePath(qualifiedPackageName)).exists();
	}

	private IPath makePath(String qualifiedPackageName) {
		return new Path(qualifiedPackageName);
	}


	public void reset() {
	}

	public String toString() {
		return "Container classpath directory " + container; //$NON-NLS-1$
	}
}
