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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

import java.io.*;

// AspectJ - increased member visibilities
class ClasspathDirectory extends ClasspathLocation {

String binaryPath; // includes .class files for a single directory
SimpleLookupTable directoryCache;
String[] missingPackageHolder = new String[1];

ClasspathDirectory(String binaryPath) {
	this.binaryPath = binaryPath;
	if (!binaryPath.endsWith("/")) //$NON-NLS-1$
		this.binaryPath += "/"; //$NON-NLS-1$

	this.directoryCache = new SimpleLookupTable(5);
}

public void cleanup() {
	this.directoryCache = null;
}

String[] directoryList(String qualifiedPackageName) {
	String[] dirList = (String[]) directoryCache.get(qualifiedPackageName);
	if (dirList == missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirList != null) return dirList;

	File dir = new File(binaryPath + qualifiedPackageName);
	notFound : if (dir != null && dir.isDirectory()) {
		// must protect against a case insensitive File call
		// walk the qualifiedPackageName backwards looking for an uppercase character before the '/'
		int index = qualifiedPackageName.length();
		int last = qualifiedPackageName.lastIndexOf('/');
		while (--index > last && !Character.isUpperCase(qualifiedPackageName.charAt(index))) {}
		if (index > last) {
			if (last == -1) {
				if (!doesFileExist(qualifiedPackageName, "")) //$NON-NLS-1$ 
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

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathDirectory)) return false;

	return binaryPath.equals(((ClasspathDirectory) o).binaryPath);
} 

public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!doesFileExist(binaryFileName, qualifiedPackageName)) return null; // most common case

	try {
		ClassFileReader reader = ClassFileReader.read(binaryPath + qualifiedBinaryFileName);
		if (reader != null) return new NameEnvironmentAnswer(reader);
	} catch (Exception e) {} // treat as if class file is missing
	return null;
}

public boolean isPackage(String qualifiedPackageName) {
	return directoryList(qualifiedPackageName) != null;
}

public void reset() {
	this.directoryCache = new SimpleLookupTable(5);
}

public String toString() {
	return "Binary classpath directory " + binaryPath; //$NON-NLS-1$
}
}