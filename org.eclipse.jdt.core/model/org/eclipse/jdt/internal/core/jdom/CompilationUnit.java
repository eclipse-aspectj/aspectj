/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.jdom;

import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * Implements a very simple version of the ICompilationUnit.
 *
 * <p>Please do not use outside of jdom.</p>
 */
public class CompilationUnit implements ICompilationUnit {
	protected char[] fContents;
	protected char[] fFileName;
	protected char[] fMainTypeName;
public CompilationUnit(char[] contents, char[] filename) {
	fContents = contents;
	fFileName = filename;

	String file = new String(filename);
	int start = file.lastIndexOf("/") + 1; //$NON-NLS-1$
	if (start == 0 || start < file.lastIndexOf("\\")) //$NON-NLS-1$
		start = file.lastIndexOf("\\") + 1; //$NON-NLS-1$

	int end = file.lastIndexOf("."); //$NON-NLS-1$
	if (end == -1)
		end = file.length();

	fMainTypeName = file.substring(start, end).toCharArray();
}
public char[] getContents() {
	return fContents;
}
public char[] getFileName() {
	return fFileName;
}
public char[] getMainTypeName() {
	return fMainTypeName;
}
public char[][] getPackageName() {
	return null;
}
public String toString() {
	return "CompilationUnit[" + new String(fFileName) + "]";  //$NON-NLS-2$ //$NON-NLS-1$
}
}
