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

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.*;

/**
 * The abstract superclass of image builders.
 * Provides the building and compilation mechanism
 * in common with the batch and incremental builders.
 * 
 * AspectJ - added makeSourceFile as extension point for command-line builders
 */
public abstract class AbstractImageBuilder implements ICompilerRequestor {

protected JavaBuilder javaBuilder;
protected State newState;

// local copies
protected IContainer outputFolder;
protected IContainer[] sourceFolders;
protected BuildNotifier notifier;

protected boolean hasSeparateOutputFolder;
protected NameEnvironment nameEnvironment;
protected Compiler compiler;
protected WorkQueue workQueue;
protected ArrayList problemTypeLocations;
protected boolean compiledAllAtOnce;

private boolean inCompiler;

// There are memory issues with increasing this, but better memory issues than
// forgetting to compile some files
public static int MAX_AT_ONCE = Integer.MAX_VALUE;

protected AbstractImageBuilder(JavaBuilder javaBuilder) {
	this.javaBuilder = javaBuilder;
	this.newState = new State(javaBuilder);

	// local copies
	this.outputFolder = javaBuilder.outputFolder;
	this.sourceFolders = javaBuilder.sourceFolders;
	this.notifier = javaBuilder.notifier;

	// only perform resource copying if the output location does not match a source folder
	// corresponds to: project == src == bin, or several source folders are contributing resources,
	// but one is the output location too (and would get populated with other source folder resources).
	IPath outputPath = outputFolder.getFullPath();
	int index = sourceFolders.length;
	if (index == 0) {
		// handle case of the last source folder is removed... so no source folders exist but the output folder must still be scrubbed
		this.hasSeparateOutputFolder = !outputPath.equals(javaBuilder.currentProject.getFullPath());
	} else {
		this.hasSeparateOutputFolder = true;
		while (this.hasSeparateOutputFolder && --index >= 0)
			this.hasSeparateOutputFolder = !outputPath.equals(sourceFolders[index].getFullPath());
	}

	this.nameEnvironment = new NameEnvironment(javaBuilder.classpath);
	this.compiler = newCompiler();
	this.workQueue = new WorkQueue();
	this.problemTypeLocations = new ArrayList(3);
}

public void acceptResult(CompilationResult result) {
	// In Batch mode, we write out the class files, hold onto the dependency info
	// & additional types and report problems.

	// In Incremental mode, when writing out a class file we need to compare it
	// against the previous file, remembering if structural changes occured.
	// Before reporting the new problems, we need to update the problem count &
	// remove the old problems. Plus delete additional class files that no longer exist.

	// only need to find resource for the sourceLocation when problems need to be reported against it
	String sourceLocation = new String(result.getFileName()); // the full filesystem path "d:/xyz/eclipse/src1/Test/p1/p2/A.java"
	if (!workQueue.isCompiled(sourceLocation)) {
		try {
			workQueue.finished(sourceLocation);
			updateProblemsFor(sourceLocation, result); // record compilation problems before potentially adding duplicate errors

			ICompilationUnit compilationUnit = result.getCompilationUnit();
			ClassFile[] classFiles = result.getClassFiles();
			int length = classFiles.length;
			ArrayList duplicateTypeNames = null;
			ArrayList definedTypeNames = new ArrayList(length);
			for (int i = 0; i < length; i++) {
				ClassFile classFile = classFiles[i];
				char[][] compoundName = classFile.getCompoundName();
				char[] typeName = compoundName[compoundName.length - 1];
				boolean isNestedType = CharOperation.contains('$', typeName);

				// Look for a possible collision, if one exists, report an error but do not write the class file
				if (isNestedType) {
					String qualifiedTypeName = new String(classFile.outerMostEnclosingClassFile().fileName());
					if (newState.isDuplicateLocation(qualifiedTypeName, sourceLocation))
						continue;
				} else {
					String qualifiedTypeName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
					if (newState.isDuplicateLocation(qualifiedTypeName, sourceLocation)) {
						if (duplicateTypeNames == null)
							duplicateTypeNames = new ArrayList();
						duplicateTypeNames.add(compoundName);
						createErrorFor(resourceForLocation(sourceLocation), Util.bind("build.duplicateClassFile", new String(typeName))); //$NON-NLS-1$
						continue;
					}
					newState.recordLocationForType(qualifiedTypeName, sourceLocation);
				}
				definedTypeNames.add(writeClassFile(classFile, !isNestedType));
			}

			finishedWith(sourceLocation, result, compilationUnit.getMainTypeName(), definedTypeNames, duplicateTypeNames);
			notifier.compiled(compilationUnit);
		} catch (CoreException e) {
			Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
			createErrorFor(resourceForLocation(sourceLocation), Util.bind("build.inconsistentClassFile")); //$NON-NLS-1$
		}
	}
}

protected void cleanUp() {
	this.nameEnvironment.cleanup();

	this.javaBuilder = null;
	this.outputFolder = null;
	this.sourceFolders = null;
	this.notifier = null;
	this.compiler = null;
	this.nameEnvironment = null;
	this.workQueue = null;
	this.problemTypeLocations = null;
}

/* Compile the given elements, adding more elements to the work queue 
* if they are affected by the changes.
*/
protected void compile(String[] filenames, String[] initialTypeNames) {
	int toDo = filenames.length;
	if (this.compiledAllAtOnce = toDo <= MAX_AT_ONCE) {
		// do them all now
		SourceFile[] toCompile = new SourceFile[toDo];
		for (int i = 0; i < toDo; i++) {
			String filename = filenames[i];
			if (JavaBuilder.DEBUG)
				System.out.println("About to compile " + filename); //$NON-NLS-1$
			toCompile[i] = makeSourceFile(filename, initialTypeNames[i]);
		}
		compile(toCompile, initialTypeNames, null);
	} else {
		int i = 0;
		boolean compilingFirstGroup = true;
		while (i < toDo) {
			int doNow = toDo < MAX_AT_ONCE ? toDo : MAX_AT_ONCE;
			int index = 0;
			SourceFile[] toCompile = new SourceFile[doNow];
			String[] initialNamesInLoop = new String[doNow];
			while (i < toDo && index < doNow) {
				String filename = filenames[i];
				// Although it needed compiling when this method was called, it may have
				// already been compiled when it was referenced by another unit.
				if (compilingFirstGroup || workQueue.isWaiting(filename)) {
					if (JavaBuilder.DEBUG)
						System.out.println("About to compile " + filename);//$NON-NLS-1$
					String initialTypeName = initialTypeNames[i];
					initialNamesInLoop[index] = initialTypeName;
					toCompile[index++] = makeSourceFile(filename, initialTypeName);
				}
				i++;
			}
			if (index < doNow) {
				System.arraycopy(toCompile, 0, toCompile = new SourceFile[index], 0, index);
				System.arraycopy(initialNamesInLoop, 0, initialNamesInLoop = new String[index], 0, index);
			}
			String[] additionalFilenames = new String[toDo - i];
			System.arraycopy(filenames, i, additionalFilenames, 0, additionalFilenames.length);
			compilingFirstGroup = false;
			compile(toCompile, initialNamesInLoop, additionalFilenames);
		}
	}
}

/**
 * Extension point for batch building
 */
protected SourceFile makeSourceFile(String filename, String initialTypeName) {
	return new SourceFile(filename, initialTypeName);
}


void compile(SourceFile[] units, String[] initialTypeNames, String[] additionalFilenames) {
	if (units.length == 0) return;
	notifier.aboutToCompile(units[0]); // just to change the message

	// extend additionalFilenames with all hierarchical problem types found during this entire build
	if (!problemTypeLocations.isEmpty()) {
		int toAdd = problemTypeLocations.size();
		int length = additionalFilenames == null ? 0 : additionalFilenames.length;
		if (length == 0)
			additionalFilenames = new String[toAdd];
		else
			System.arraycopy(additionalFilenames, 0, additionalFilenames = new String[length + toAdd], 0, length);
		for (int i = 0; i < toAdd; i++)
			additionalFilenames[length + i] = (String) problemTypeLocations.get(i);
	}
	nameEnvironment.setNames(initialTypeNames, additionalFilenames);
	notifier.checkCancel();
	try {
		inCompiler = true;
		compiler.compile(units);
	} finally {
		inCompiler = false;
	}
	// Check for cancel immediately after a compile, because the compiler may
	// have been cancelled but without propagating the correct exception
	notifier.checkCancel();
}

protected void createErrorFor(IResource resource, String message) {
	try {
		IMarker marker = resource.createMarker(JavaBuilder.ProblemMarkerTag);
		marker.setAttributes(
			new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IMarker.CHAR_START, IMarker.CHAR_END},
			new Object[] {message, new Integer(IMarker.SEVERITY_ERROR), new Integer(0), new Integer(1)});
	} catch (CoreException e) {
		throw internalException(e);
	}
}

protected String extractTypeNameFrom(String sourceLocation) {
	for (int j = 0, k = sourceFolders.length; j < k; j++) {
		String folderLocation = sourceFolders[j].getLocation().addTrailingSeparator().toString();
		if (sourceLocation.startsWith(folderLocation))
			return sourceLocation.substring(folderLocation.length(), sourceLocation.length() - 5); // length of ".java"
	}
	return sourceLocation; // should not reach here
}

protected void finishedWith(String sourceLocation, CompilationResult result, char[] mainTypeName, ArrayList definedTypeNames, ArrayList duplicateTypeNames) throws CoreException {
	if (duplicateTypeNames == null) {
		newState.record(sourceLocation, result.qualifiedReferences, result.simpleNameReferences, mainTypeName, definedTypeNames);
		return;
	}

	char[][][] qualifiedRefs = result.qualifiedReferences;
	char[][] simpleRefs = result.simpleNameReferences;
	// for each duplicate type p1.p2.A, add the type name A (package was already added)
	next : for (int i = 0, dLength = duplicateTypeNames.size(); i < dLength; i++) {
		char[][] compoundName = (char[][]) duplicateTypeNames.get(i);
		char[] typeName = compoundName[compoundName.length - 1];
		int sLength = simpleRefs.length;
		for (int j = 0; j < sLength; j++)
			if (CharOperation.equals(simpleRefs[j], typeName))
				continue next;
		System.arraycopy(simpleRefs, 0, simpleRefs = new char[sLength + 1][], 0, sLength);
		simpleRefs[sLength] = typeName;
	}
	newState.record(sourceLocation, qualifiedRefs, simpleRefs, mainTypeName, definedTypeNames);
}

protected IContainer getOutputFolder(IPath packagePath) throws CoreException {
	IFolder folder = outputFolder.getFolder(packagePath);
	if (!folder.exists()) {
		getOutputFolder(packagePath.removeLastSegments(1));
		folder.create(true, true, null);
		folder.setDerived(true);
	}
	return folder;
}

protected RuntimeException internalException(CoreException t) {
	ImageBuilderInternalException imageBuilderException = new ImageBuilderInternalException(t);
	if (inCompiler)
		return new AbortCompilation(true, imageBuilderException);
	return imageBuilderException;
}

protected Compiler newCompiler() {
	// called once when the builder is initialized... can override if needed
	return new Compiler(
		nameEnvironment,
		DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		JavaCore.getOptions(),
		this,
		ProblemFactory.getProblemFactory(Locale.getDefault()));
}

protected IResource resourceForLocation(String sourceLocation) {
	return javaBuilder.workspaceRoot.getFileForLocation(new Path(sourceLocation));
}

/**
 * Creates a marker from each problem and adds it to the resource.
 * The marker is as follows:
 *   - its type is T_PROBLEM
 *   - its plugin ID is the JavaBuilder's plugin ID
 *	 - its message is the problem's message
 *	 - its priority reflects the severity of the problem
 *	 - its range is the problem's range
 *	 - it has an extra attribute "ID" which holds the problem's id
 */
protected void storeProblemsFor(IResource resource, IProblem[] problems) throws CoreException {
	if (resource == null || problems == null || problems.length == 0) return;

	String missingClassFile = null;
	for (int i = 0, length = problems.length; i < length; i++) {
		IProblem problem = problems[i];
		int id = problem.getID();
		switch (id) {
			case IProblem.IsClassPathCorrect :
				JavaBuilder.removeProblemsFor(javaBuilder.currentProject); // make this the only problem for this project
				String[] args = problem.getArguments();
				missingClassFile = args[0];
				break;
			case IProblem.SuperclassMustBeAClass :
			case IProblem.SuperInterfaceMustBeAnInterface :
			case IProblem.HierarchyCircularitySelfReference :
			case IProblem.HierarchyCircularity :
			case IProblem.HierarchyHasProblems :
			case IProblem.SuperclassNotFound :
			case IProblem.SuperclassNotVisible :
			case IProblem.SuperclassAmbiguous :
			case IProblem.SuperclassInternalNameProvided :
			case IProblem.SuperclassInheritedNameHidesEnclosingName :
			case IProblem.InterfaceNotFound :
			case IProblem.InterfaceNotVisible :
			case IProblem.InterfaceAmbiguous :
			case IProblem.InterfaceInternalNameProvided :
			case IProblem.InterfaceInheritedNameHidesEnclosingName :
				// ensure that this file is always retrieved from source for the rest of the build
				String fileLocation = resource.getLocation().toString();
				if (!problemTypeLocations.contains(fileLocation))
					problemTypeLocations.add(fileLocation);
		}

		IMarker marker = resource.createMarker(JavaBuilder.ProblemMarkerTag);
		marker.setAttributes(
			new String[] {IMarker.MESSAGE, IMarker.SEVERITY, IJavaModelMarker.ID, IMarker.CHAR_START, IMarker.CHAR_END, IMarker.LINE_NUMBER, IJavaModelMarker.ARGUMENTS},
			new Object[] { 
				problem.getMessage(),
				new Integer(problem.isError() ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING), 
				new Integer(id),
				new Integer(problem.getSourceStart()),
				new Integer(problem.getSourceEnd() + 1),
				new Integer(problem.getSourceLineNumber()),
				Util.getProblemArgumentsForMarker(problem.getArguments())
			});

		// compute a user-friendly location
		IJavaElement element = JavaCore.create(resource);
		if (element instanceof org.eclipse.jdt.core.ICompilationUnit) { // try to find a finer grain element
			org.eclipse.jdt.core.ICompilationUnit unit = (org.eclipse.jdt.core.ICompilationUnit) element;
			IJavaElement fragment = unit.getElementAt(problem.getSourceStart());
			if (fragment != null) element = fragment;
		}
		String location = null;
		if (element instanceof JavaElement)
			location = ((JavaElement) element).readableName();
		if (location != null)
			marker.setAttribute(IMarker.LOCATION, location);
		if (missingClassFile != null)
			throw new MissingClassFileException(missingClassFile);
	}
}

protected void updateProblemsFor(String sourceLocation, CompilationResult result) throws CoreException {
	IProblem[] problems = result.getProblems();
	if (problems == null || problems.length == 0) return;

	notifier.updateProblemCounts(problems);
	storeProblemsFor(resourceForLocation(sourceLocation), problems);
}

protected char[] writeClassFile(ClassFile classFile, boolean isSecondaryType) throws CoreException {
	// Before writing out the class file, compare it to the previous file
	// If structural changes occured then add dependent source files
	String fileName = new String(classFile.fileName()); // the qualified type name "p1/p2/A"
	IPath filePath = new Path(fileName);			
	IContainer container = outputFolder;
	if (filePath.segmentCount() > 1) {
		container = getOutputFolder(filePath.removeLastSegments(1));
		filePath = new Path(filePath.lastSegment());
	}

	IFile file = container.getFile(filePath.addFileExtension(JavaBuilder.CLASS_EXTENSION));
	byte[] bytes = classFile.getBytes();
	if (writeClassFileCheck(file, fileName, bytes, isSecondaryType)) {
		if (JavaBuilder.DEBUG)
			System.out.println("Writing class file " + file.getName());//$NON-NLS-1$
		file.create(new ByteArrayInputStream(bytes), IResource.FORCE, null);
		file.setDerived(true);
	} else if (JavaBuilder.DEBUG) {
		System.out.println("Skipped over unchanged class file " + file.getName());//$NON-NLS-1$
	}
	// answer the name of the class file as in Y or Y$M
	return filePath.lastSegment().toCharArray();
}

protected boolean writeClassFileCheck(IFile file, String fileName, byte[] bytes, boolean isSecondaryType) throws CoreException {
	// In Incremental mode, compare the bytes against the previous file for structural changes
	return true;
}
}