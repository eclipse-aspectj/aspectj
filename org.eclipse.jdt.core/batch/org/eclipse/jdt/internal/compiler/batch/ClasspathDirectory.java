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
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.util.Hashtable;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

public class ClasspathDirectory implements FileSystem.Classpath {

String path;
Hashtable directoryCache;
String[] missingPackageHolder = new String[1];
String encoding;
public int mode; // ability to only consider one kind of files (source vs. binaries), by default use both

public static final int SOURCE = 1;
public static final int BINARY = 2;

ClasspathDirectory(File directory, String encoding, int mode) {
	this.mode = mode;
	this.path = directory.getAbsolutePath();
	if (!path.endsWith(File.separator))
		this.path += File.separator;
	this.directoryCache = new Hashtable(11);
	this.encoding = encoding;
}

ClasspathDirectory(File directory, String encoding) {
	this(directory, encoding, SOURCE | BINARY); // by default consider both sources and binaries
}

String[] directoryList(String qualifiedPackageName) {
	String[] dirList = (String[]) directoryCache.get(qualifiedPackageName);
	if (dirList == missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirList != null) return dirList;

	File dir = new File(path + qualifiedPackageName);
	notFound : if (dir != null && dir.isDirectory()) {
		// must protect against a case insensitive File call
		// walk the qualifiedPackageName backwards looking for an uppercase character before the '/'
		int index = qualifiedPackageName.length();
		int last = qualifiedPackageName.lastIndexOf(File.separatorChar);
		while (--index > last && !Character.isUpperCase(qualifiedPackageName.charAt(index))) {}
		if (index > last) {
			if (last == -1) {
				if (!doesFileExist(qualifiedPackageName, ""))  //$NON-NLS-1$ 
					break notFound;
			} else {
				String packageName = qualifiedPackageName.substring(last + 1);
				String parentPackage = qualifiedPackageName.substring(0, last);
				if (!doesFileExist(packageName, parentPackage))
					break notFound;
			}
		}
		if ((dirList = dir.list()) == null)
			dirList = new String[0];
		directoryCache.put(qualifiedPackageName, dirList);
		return dirList;
	}
	directoryCache.put(qualifiedPackageName, missingPackageHolder);
	return null;
}
boolean doesFileExist(String fileName, String qualifiedPackageName) {
	String[] dirList = directoryList(qualifiedPackageName);
	if (dirList == null) return false; // most common case

	for (int i = dirList.length; --i >= 0;)
		if (fileName.equals(dirList[i]))
			return true;
	return false;
}
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	String fileName = new String(typeName);
	boolean binaryExists = ((this.mode & BINARY) != 0) && doesFileExist(fileName + ".class", qualifiedPackageName); //$NON-NLS-1$
	boolean sourceExists = ((this.mode & SOURCE) != 0) && doesFileExist(fileName + ".java", qualifiedPackageName); //$NON-NLS-1$
	if (sourceExists) {
		String fullSourcePath = path + qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6)  + ".java"; //$NON-NLS-1$
		if (!binaryExists)
			return new NameEnvironmentAnswer(new CompilationUnit(null, fullSourcePath, this.encoding));

		String fullBinaryPath = path + qualifiedBinaryFileName;
		long binaryModified = new File(fullBinaryPath).lastModified();
		long sourceModified = new File(fullSourcePath).lastModified();
		if (sourceModified > binaryModified)
			return new NameEnvironmentAnswer(new CompilationUnit(null, fullSourcePath, this.encoding));
	}
	if (binaryExists) {
		try {
			ClassFileReader reader = ClassFileReader.read(path + qualifiedBinaryFileName);
			if (reader != null) return new NameEnvironmentAnswer(reader);
		} catch (Exception e) {} // treat as if file is missing
	}
	return null;
}
public boolean isPackage(String qualifiedPackageName) {
	return directoryList(qualifiedPackageName) != null;
}
public void reset() {
	this.directoryCache = new Hashtable(11);
}
public String toString() {
	return "ClasspathDirectory " + path; //$NON-NLS-1$
}
}