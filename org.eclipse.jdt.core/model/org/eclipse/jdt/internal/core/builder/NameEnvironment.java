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
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.*;

public class NameEnvironment implements INameEnvironment {

ClasspathLocation[] classpathLocations;
String[] initialTypeNames; // assumed that each name is of the form "a/b/ClassName"
String[] additionalSourceFilenames; // assumed that each name is of the form "d:/eclipse/Test/a/b/ClassName.java"

boolean isIncrementalBuild;

ClasspathLocation[] binaryLocations;
ClasspathMultiDirectory[] sourceLocations;

public NameEnvironment(ClasspathLocation[] classpathLocations) {
	this.classpathLocations = classpathLocations;
	this.isIncrementalBuild = false;
	splitLocations();
	setNames(new String[0], new String[0]);
}

public NameEnvironment(IJavaProject javaProject) {
	try {
		IWorkspaceRoot workspaceRoot = javaProject.getProject().getWorkspace().getRoot();
		IResource outputFolder = workspaceRoot.findMember(javaProject.getOutputLocation());
		String outputFolderLocation = null;
		if (outputFolder != null && outputFolder.exists())
			outputFolderLocation = outputFolder.getLocation().toString();
		this.classpathLocations = computeLocations(workspaceRoot, javaProject, outputFolderLocation, null, null);
		this.isIncrementalBuild = false;
	} catch(JavaModelException e) {
		this.classpathLocations = new ClasspathLocation[0];
	}
	splitLocations();
	setNames(new String[0], new String[0]);
}

/* Some examples of resolved class path entries.
* Remember to search class path in the order that it was defined.
*
* 1a. typical project with no source folders:
*   /Test[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test
* 1b. project with source folders:
*   /Test/src1[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test/src1
*   /Test/src2[CPE_SOURCE][K_SOURCE] -> D:/eclipse.test/Test/src2
*  NOTE: These can be in any order & separated by prereq projects or libraries
* 1c. project external to workspace (only detectable using getLocation()):
*   /Test/src[CPE_SOURCE][K_SOURCE] -> d:/eclipse.zzz/src
*  Need to search source folder & output folder
*
* 2. zip files:
*   D:/j9/lib/jclMax/classes.zip[CPE_LIBRARY][K_BINARY][sourcePath:d:/j9/lib/jclMax/source/source.zip]
*      -> D:/j9/lib/jclMax/classes.zip
*  ALWAYS want to take the library path as is
*
* 3a. prereq project (regardless of whether it has a source or output folder):
*   /Test[CPE_PROJECT][K_SOURCE] -> D:/eclipse.test/Test
*  ALWAYS want to append the output folder & ONLY search for .class files
*/
public static ClasspathLocation[] computeLocations(
	IWorkspaceRoot workspaceRoot,
	IJavaProject javaProject,
	String outputFolderLocation,
	ArrayList sourceFolders,
	SimpleLookupTable binaryResources) throws JavaModelException {

	IClasspathEntry[] classpathEntries = ((JavaProject) javaProject).getExpandedClasspath(true, true);
	int cpCount = 0;
	int max = classpathEntries.length;
	ClasspathLocation[] classpathLocations = new ClasspathLocation[max];

	boolean firstSourceFolder = true;
	nextEntry : for (int i = 0; i < max; i++) {
		IClasspathEntry entry = classpathEntries[i];
		IPath path = entry.getPath();
		Object target = JavaModel.getTarget(workspaceRoot, path, true);
		if (target == null) continue nextEntry;

		if (target instanceof IResource) {
			IResource resource = (IResource) target;
			switch(entry.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE :
					if (outputFolderLocation == null || !(resource instanceof IContainer)) continue nextEntry;
					if (sourceFolders != null) { // normal builder mode
						sourceFolders.add(resource);
						classpathLocations[cpCount++] =
							ClasspathLocation.forSourceFolder(resource.getLocation().toString(), outputFolderLocation);
					} else if (firstSourceFolder) { // add the output folder only once
						firstSourceFolder = false;
						classpathLocations[cpCount++] = ClasspathLocation.forBinaryFolder(outputFolderLocation);
					}
					continue nextEntry;

				case IClasspathEntry.CPE_PROJECT :
					if (!(resource instanceof IProject)) continue nextEntry;
					IProject prereqProject = (IProject) resource;
					if (!prereqProject.isAccessible()) continue nextEntry;
					IPath outputLocation = JavaCore.create(prereqProject).getOutputLocation();
					IResource prereqOutputFolder;
					if (prereqProject.getFullPath().equals(outputLocation)) {
						prereqOutputFolder = prereqProject;
					} else {
						prereqOutputFolder = workspaceRoot.findMember(outputLocation);
						if (prereqOutputFolder == null || !prereqOutputFolder.exists() || !(prereqOutputFolder instanceof IFolder))
							continue nextEntry;
					}
					if (prereqOutputFolder.getLocation() == null) // sanity check
						continue nextEntry;
					if (binaryResources != null) { // normal builder mode
						IResource[] existingResources = (IResource[]) binaryResources.get(prereqProject);
						if (existingResources == null)
							binaryResources.put(prereqProject, new IResource[] {prereqOutputFolder});
						else
							existingResources[0] = prereqOutputFolder; // project's output folder is always first
					}
					classpathLocations[cpCount++] = ClasspathLocation.forBinaryFolder(prereqOutputFolder.getLocation().toString());
					continue nextEntry;

				case IClasspathEntry.CPE_LIBRARY :
					if (resource.getLocation() == null) // sanity check
						continue nextEntry;
					if (resource instanceof IFile) {
						String extension = path.getFileExtension();
						if (!(JavaBuilder.JAR_EXTENSION.equalsIgnoreCase(extension) || JavaBuilder.ZIP_EXTENSION.equalsIgnoreCase(extension)))
							continue nextEntry;
						classpathLocations[cpCount++] = ClasspathLocation.forLibrary(resource.getLocation().toString());
					} else if (resource instanceof IFolder) {
						classpathLocations[cpCount++] = ClasspathLocation.forBinaryFolder(resource.getLocation().toString());
					}
					if (binaryResources != null) { // normal builder mode
						IProject p = resource.getProject(); // can be the project being built
						IResource[] existingResources = (IResource[]) binaryResources.get(p);
						if (existingResources == null) {
							existingResources = new IResource[] {null, resource}; // project's output folder is always first, null if not included
						} else {
							int size = existingResources.length;
							System.arraycopy(existingResources, 0, existingResources = new IResource[size + 1], 0, size);
							existingResources[size] = resource;
						}
						binaryResources.put(p, existingResources);
					}
					continue nextEntry;
			}
		} else if (target instanceof File) {
			String extension = path.getFileExtension();
			if (!(JavaBuilder.JAR_EXTENSION.equalsIgnoreCase(extension) || JavaBuilder.ZIP_EXTENSION.equalsIgnoreCase(extension)))
				continue nextEntry;
			classpathLocations[cpCount++] = ClasspathLocation.forLibrary(path.toString());
		}
	}
	if (cpCount < max)
		System.arraycopy(classpathLocations, 0, (classpathLocations = new ClasspathLocation[cpCount]), 0, cpCount);
	return classpathLocations;
}

public void cleanup() {
	for (int i = 0, length = classpathLocations.length; i < length; i++)
		classpathLocations[i].cleanup();
}

private NameEnvironmentAnswer findClass(String qualifiedTypeName, char[] typeName) {
	if (initialTypeNames != null) {
		for (int i = 0, length = initialTypeNames.length; i < length; i++) {
			if (qualifiedTypeName.equals(initialTypeNames[i])) {
				if (isIncrementalBuild)
					// catch the case that a type inside a source file has been renamed but other class files are looking for it
					throw new AbortCompilation(true, new AbortIncrementalBuildException(qualifiedTypeName));
				return null; // looking for a file which we know was provided at the beginning of the compilation
			}
		}
	}

	String qBinaryFileName = qualifiedTypeName + ".class"; //$NON-NLS-1$
	String binaryFileName = qBinaryFileName;
	String qPackageName =  ""; //$NON-NLS-1$
	if (qualifiedTypeName.length() > typeName.length) {
		int typeNameStart = qBinaryFileName.length() - typeName.length - 6; // size of ".class"
		qPackageName =  qBinaryFileName.substring(0, typeNameStart - 1);
		binaryFileName = qBinaryFileName.substring(typeNameStart);
	}

	if (sourceLocations != null && sourceLocations[0].isPackage(qPackageName)) { // looks in common output folder
		if (additionalSourceFilenames != null) {
			String qSourceFileName = qualifiedTypeName + ".java"; //$NON-NLS-1$
			for (int i = 0, length = sourceLocations.length; i < length; i++) {
				NameEnvironmentAnswer answer =
					sourceLocations[i].findSourceFile(qSourceFileName, qPackageName, typeName, additionalSourceFilenames);
				if (answer != null) return answer;
			}
		}
		NameEnvironmentAnswer answer = sourceLocations[0].findClass(binaryFileName, qPackageName, qBinaryFileName);
		if (answer != null) return answer;
	}
	for (int i = 0, length = binaryLocations.length; i < length; i++) {
		NameEnvironmentAnswer answer = binaryLocations[i].findClass(binaryFileName, qPackageName, qBinaryFileName);
		if (answer != null) return answer;
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

public boolean isPackage(char[][] compoundName, char[] packageName) {
	return isPackage(new String(CharOperation.concatWith(compoundName, packageName, '/')));
}

public boolean isPackage(String qualifiedPackageName) {
	if (sourceLocations != null && sourceLocations[0].isPackage(qualifiedPackageName)) // looks in common output folder
		return true;
	for (int i = 0, length = binaryLocations.length; i < length; i++)
		if (binaryLocations[i].isPackage(qualifiedPackageName))
			return true;
	return false;
}

public void setNames(String[] initialTypeNames, String[] additionalSourceFilenames) {
	this.initialTypeNames = initialTypeNames;
	this.additionalSourceFilenames = additionalSourceFilenames;
	for (int i = 0, length = classpathLocations.length; i < length; i++)
		classpathLocations[i].reset();
}

private void splitLocations() {
	int length = classpathLocations.length;
	ArrayList sLocations = new ArrayList(length);
	ArrayList bLocations = new ArrayList(length);
	for (int i = 0; i < length; i++) {
		ClasspathLocation classpath = classpathLocations[i];
		if (classpath instanceof ClasspathMultiDirectory)
			sLocations.add(classpath);
		else
			bLocations.add(classpath);
	}

	if (sLocations.isEmpty()) {
		this.sourceLocations = null;
	} else {
		this.sourceLocations = new ClasspathMultiDirectory[sLocations.size()];
		sLocations.toArray(this.sourceLocations);
	}
	this.binaryLocations = new ClasspathLocation[bLocations.size()];
	bLocations.toArray(this.binaryLocations);
}

void tagAsIncrementalBuild() {
	this.isIncrementalBuild = true;
}
}