/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

import java.io.*;

public class SourceFile implements ICompilationUnit {

public char[] fileName;
public char[] mainTypeName;
public char[][] packageName;
String encoding;

//XXX AspectJ need a better solution for not looking up encoding in JavaCore
//XXX this breaks eclipse encoding support
public SourceFile(String fileName, String initialTypeName) {
	this.fileName = fileName.toCharArray();
	CharOperation.replace(this.fileName, '\\', '/');

	char[] typeName = initialTypeName.toCharArray();
	int lastIndex = CharOperation.lastIndexOf('/', typeName);
	this.mainTypeName = CharOperation.subarray(typeName, lastIndex + 1, -1);
	this.packageName = CharOperation.splitOn('/', typeName, 0, lastIndex - 1);

	this.encoding = null; //XXXJavaCore.getOption(JavaCore.CORE_ENCODING);
}

public SourceFile(String fileName, char[] mainTypeName, char[][] packageName) {
	this.fileName = fileName.toCharArray();
	CharOperation.replace(this.fileName, '\\', '/');

	this.mainTypeName = mainTypeName;
	this.packageName = packageName;

	this.encoding = null; //XXXJavaCore.getOption(JavaCore.CORE_ENCODING);
}

public char[] getContents() {
	// otherwise retrieve it
	BufferedReader reader = null;
	try {
		File file = new File(new String(fileName));
		InputStreamReader streamReader =
			this.encoding == null
				? new InputStreamReader(new FileInputStream(file))
				: new InputStreamReader(new FileInputStream(file), this.encoding);
		reader = new BufferedReader(streamReader);
		int length = (int) file.length();
		char[] contents = new char[length];
		int len = 0;
		int readSize = 0;
		while ((readSize != -1) && (len != length)) {
			// See PR 1FMS89U
			// We record first the read size. In this case len is the actual read size.
			len += readSize;
			readSize = reader.read(contents, len, length - len);
		}
		reader.close();
		// See PR 1FMS89U
		// Now we need to resize in case the default encoding used more than one byte for each
		// character
		if (len != length)
			System.arraycopy(contents, 0, (contents = new char[len]), 0, len);		
		return contents;
	} catch (FileNotFoundException e) {
		throw new AbortCompilation(true, new MissingSourceFileException(new String(fileName)));
	} catch (IOException e) {
		if (reader != null) {
			try {
				reader.close();
			} catch(IOException ioe) {
			}
		}
		throw new AbortCompilation(true, new MissingSourceFileException(new String(fileName)));
	}
}

public char[] getFileName() {
	return fileName;
}

public char[] getMainTypeName() {
	return mainTypeName;
}

public char[][] getPackageName() {
	return packageName;
}

public String toString() {
	return "SourceFile[" //$NON-NLS-1$
		+ new String(fileName) + "]";  //$NON-NLS-1$
}
}