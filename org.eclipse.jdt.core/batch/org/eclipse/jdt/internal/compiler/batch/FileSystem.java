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
import java.io.IOException;
import java.util.zip.ZipFile;

import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class FileSystem implements INameEnvironment  {
	Classpath[] classpaths;
	String[] knownFileNames;

	interface Classpath {
		NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName);
		boolean isPackage(String qualifiedPackageName); 
		/**
		 * This method resets the environment. The resulting state is equivalent to
		 * a new name environment without creating a new object.
		 */
		void reset();
	}
/*
	classPathNames is a collection is Strings representing the full path of each class path
	initialFileNames is a collection is Strings, the trailing '.java' will be removed if its not already.
*/

public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding) {
	this(classpathNames, initialFileNames, encoding, null);
}
public FileSystem(String[] classpathNames, String[] initialFileNames, String encoding, int[] classpathDirectoryModes) {
	int classpathSize = classpathNames.length;
	classpaths = new Classpath[classpathSize];
	String[] pathNames = new String[classpathSize];
	int problemsOccured = 0;
	for (int i = 0; i < classpathSize; i++) {
		try {
			File file = new File(convertPathSeparators(classpathNames[i]));
			if (file.isDirectory()) {
				if (file.exists()) {
					if (classpathDirectoryModes == null){
						classpaths[i] = new ClasspathDirectory(file, encoding);
					} else {
						classpaths[i] = new ClasspathDirectory(file, encoding, classpathDirectoryModes[i]);
					}
					pathNames[i] = ((ClasspathDirectory) classpaths[i]).path;
				}
			} else if (classpathNames[i].endsWith(".jar") | (classpathNames[i].endsWith(".zip"))) { //$NON-NLS-2$ //$NON-NLS-1$
				classpaths[i] = this.getClasspathJar(file); // will throw an IOException if file does not exist
				pathNames[i] = classpathNames[i].substring(0, classpathNames[i].lastIndexOf('.'));
			}
		} catch (IOException e) {
			classpaths[i] = null;
		}
		if (classpaths[i] == null)
			problemsOccured++;
	}
	if (problemsOccured > 0) {
		Classpath[] newPaths = new Classpath[classpathSize - problemsOccured];
		String[] newNames = new String[classpathSize - problemsOccured];
		for (int i = 0, current = 0; i < classpathSize; i++)
			if (classpaths[i] != null) {
				newPaths[current] = classpaths[i];
				newNames[current++] = pathNames[i];
			}
		classpathSize = newPaths.length;
		classpaths = newPaths;
		pathNames = newNames;
	}

	knownFileNames = new String[initialFileNames.length];
	for (int i = initialFileNames.length; --i >= 0;) {
		String fileName = initialFileNames[i];
		String matchingPathName = null;
		if (fileName.lastIndexOf(".") != -1) //$NON-NLS-1$
			fileName = fileName.substring(0, fileName.lastIndexOf('.')); // remove trailing ".java"

		fileName = convertPathSeparators(fileName);
		for (int j = 0; j < classpathSize; j++)
			if (fileName.startsWith(pathNames[j]))
				matchingPathName = pathNames[j];
		if (matchingPathName == null)
			knownFileNames[i] = fileName; // leave as is...
		else
			knownFileNames[i] = fileName.substring(matchingPathName.length());
	}
}
public void cleanup() {
	for (int i = 0, max = classpaths.length; i < max; i++)
		classpaths[i].reset();
}
private String convertPathSeparators(String path) {
	return File.separatorChar == '/'
		? path.replace('\\', '/')
		 : path.replace('/', '\\');
}
private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName){
	for (int i = 0, length = knownFileNames.length; i < length; i++)
		if (qualifiedTypeName.equals(knownFileNames[i]))
			return null; // looking for a file which we know was provided at the beginning of the compilation

	String qualifiedBinaryFileName = qualifiedTypeName + ".class"; //$NON-NLS-1$
	String qualifiedPackageName =
		qualifiedTypeName.length() == typeName.length
			? "" //$NON-NLS-1$
			: qualifiedBinaryFileName.substring(0, qualifiedTypeName.length() - typeName.length - 1);
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = classpaths.length; i < length; i++) {
			NameEnvironmentAnswer answer = classpaths[i].findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName);
			if (answer != null) return answer;
		}
	} else {
		String qb2 = qualifiedBinaryFileName.replace('/', File.separatorChar);
		for (int i = 0, length = classpaths.length; i < length; i++) {
			Classpath p = classpaths[i];
			NameEnvironmentAnswer answer = (p instanceof ClasspathJar)
				? p.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName)
				: p.findClass(typeName, qp2, qb2);
			if (answer != null) return answer;
		}
	}
	return null;
}
public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName != null)
		return findClass(
			new String(CharOperation.concatWith(compoundName, '/')),
			compoundName[compoundName.length - 1]);
	return null;
}
public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName != null)
		return findClass(
			new String(CharOperation.concatWith(packageName, typeName, '/')),
			typeName);
	return null;
}
public ClasspathJar getClasspathJar(File file) throws IOException {
	return new ClasspathJar(new ZipFile(file), true);
}
public boolean isPackage(char[][] compoundName, char[] packageName) {
	String qualifiedPackageName = new String(CharOperation.concatWith(compoundName, packageName, '/'));
	String qp2 = File.separatorChar == '/' ? qualifiedPackageName : qualifiedPackageName.replace('/', File.separatorChar);
	if (qualifiedPackageName == qp2) {
		for (int i = 0, length = classpaths.length; i < length; i++)
			if (classpaths[i].isPackage(qualifiedPackageName))
				return true;
	} else {
		for (int i = 0, length = classpaths.length; i < length; i++) {
			Classpath p = classpaths[i];
			if ((p instanceof ClasspathJar) ? p.isPackage(qualifiedPackageName) : p.isPackage(qp2))
				return true;
		}
	}
	return false;
}
}