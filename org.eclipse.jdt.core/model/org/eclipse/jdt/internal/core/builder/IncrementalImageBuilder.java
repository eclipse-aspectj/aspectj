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

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.Util;

import java.util.*;

/**
 * The incremental image builder
 */
public class IncrementalImageBuilder extends AbstractImageBuilder {

protected ArrayList locations;
protected ArrayList previousLocations;
protected ArrayList typeNames;
protected ArrayList qualifiedStrings;
protected ArrayList simpleStrings;
protected ArrayList secondaryTypesToRemove;

public static int MaxCompileLoop = 5; // perform a full build if it takes more than ? incremental compile loops

protected IncrementalImageBuilder(JavaBuilder javaBuilder) {
	super(javaBuilder);
	this.nameEnvironment.tagAsIncrementalBuild();
	this.newState.copyFrom(javaBuilder.lastState);

	this.locations = new ArrayList(33);
	this.previousLocations = null;
	this.typeNames = new ArrayList(33);
	this.qualifiedStrings = new ArrayList(33);
	this.simpleStrings = new ArrayList(33);
}

public boolean build(SimpleLookupTable deltas) {
	// initialize builder
	// walk this project's deltas, find changed source files
	// walk prereq projects' deltas, find changed class files & add affected source files
	//   use the build state # to skip the deltas for certain prereq projects
	//   ignore changed zip/jar files since they caused a full build
	// compile the source files & acceptResult()
	// compare the produced class files against the existing ones on disk
	// recompile all dependent source files of any type with structural changes or new/removed secondary type
	// keep a loop counter to abort & perform a full build

	if (JavaBuilder.DEBUG)
		System.out.println("INCREMENTAL build"); //$NON-NLS-1$

	try {
		resetCollections();

		notifier.subTask(Util.bind("build.analyzingDeltas")); //$NON-NLS-1$
		IResourceDelta sourceDelta = (IResourceDelta) deltas.get(javaBuilder.currentProject);
		if (sourceDelta != null)
			if (!findSourceFiles(sourceDelta)) return false;
		notifier.updateProgressDelta(0.10f);

		Object[] keyTable = deltas.keyTable;
		Object[] valueTable = deltas.valueTable;
		for (int i = 0, l = keyTable.length; i < l; i++) {
			IResourceDelta delta = (IResourceDelta) valueTable[i];
			if (delta != null) {
				IResource[] binaryResources = (IResource[]) javaBuilder.binaryResources.get(keyTable[i]);
				if (binaryResources != null)
					if (!findAffectedSourceFiles(delta, binaryResources)) return false;
			}
		}
		notifier.updateProgressDelta(0.10f);

		notifier.subTask(Util.bind("build.analyzingSources")); //$NON-NLS-1$
		addAffectedSourceFiles();
		notifier.updateProgressDelta(0.05f);

		int compileLoop = 0;
		float increment = 0.40f;
		while (locations.size() > 0) { // added to in acceptResult
			if (++compileLoop > MaxCompileLoop) {
				if (JavaBuilder.DEBUG)
					System.out.println("ABORTING incremental build... exceeded loop count"); //$NON-NLS-1$
				return false;
			}
			notifier.checkCancel();

			String[] allSourceFiles = new String[locations.size()];
			locations.toArray(allSourceFiles);
			String[] initialTypeStrings = new String[typeNames.size()];
			typeNames.toArray(initialTypeStrings);
			resetCollections();

			workQueue.addAll(allSourceFiles);
			notifier.setProgressPerCompilationUnit(increment / allSourceFiles.length);
			increment = increment / 2;
			compile(allSourceFiles, initialTypeStrings);
			removeSecondaryTypes();
			addAffectedSourceFiles();
		}
	} catch (AbortIncrementalBuildException e) {
		// abort the incremental build and let the batch builder handle the problem
		if (JavaBuilder.DEBUG)
			System.out.println("ABORTING incremental build... cannot find " + e.qualifiedTypeName + //$NON-NLS-1$
				". Could have been renamed inside its existing source file."); //$NON-NLS-1$
		return false;
	} catch (CoreException e) {
		throw internalException(e);
	} finally {
		cleanUp();
	}
	return true;
}

protected void addAffectedSourceFiles() {
	if (qualifiedStrings.isEmpty() && simpleStrings.isEmpty()) return;

	// the qualifiedStrings are of the form 'p1/p2' & the simpleStrings are just 'X'
	char[][][] qualifiedNames = ReferenceCollection.internQualifiedNames(qualifiedStrings);
	// if a well known qualified name was found then we can skip over these
	if (qualifiedNames.length < qualifiedStrings.size())
		qualifiedNames = null;
	char[][] simpleNames = ReferenceCollection.internSimpleNames(simpleStrings);
	// if a well known name was found then we can skip over these
	if (simpleNames.length < simpleStrings.size())
		simpleNames = null;

	Object[] keyTable = newState.references.keyTable;
	Object[] valueTable = newState.references.valueTable;
	next : for (int i = 0, l = keyTable.length; i < l; i++) {
		String sourceLocation = (String) keyTable[i];
		if (sourceLocation != null && !locations.contains(sourceLocation)) {
			if (compiledAllAtOnce && previousLocations != null && previousLocations.contains(sourceLocation))
				continue next; // can skip previously compiled locations since already saw hierarchy related problems

			ReferenceCollection refs = (ReferenceCollection) valueTable[i];
			if (refs.includes(qualifiedNames, simpleNames)) {
				// check that the file still exists... the file or its package may have been deleted
				IResource affectedFile = resourceForLocation(sourceLocation);
				if (affectedFile != null && affectedFile.exists()) {
					if (JavaBuilder.DEBUG)
						System.out.println("  adding affected source file " + sourceLocation); //$NON-NLS-1$
					locations.add(sourceLocation);
					typeNames.add(extractTypeNameFrom(sourceLocation));
				}
			}
		}
	}
}

protected void addDependentsOf(IPath path, boolean hasStructuralChanges) {
	if (hasStructuralChanges)
		newState.tagAsStructurallyChanged();
	// the qualifiedStrings are of the form 'p1/p1' & the simpleStrings are just 'X'
	path = path.setDevice(null);
	String packageName = path.uptoSegment(path.segmentCount() - 1).toString();
	if (!qualifiedStrings.contains(packageName))
		qualifiedStrings.add(packageName);
	String typeName = path.lastSegment();
	int memberIndex = typeName.indexOf('$');
	if (memberIndex > 0)
		typeName = typeName.substring(0, memberIndex);
	if (!simpleStrings.contains(typeName)) {
		if (JavaBuilder.DEBUG)
			System.out.println("  adding dependents of " //$NON-NLS-1$
				+ typeName + " in " + packageName); //$NON-NLS-1$
		simpleStrings.add(typeName);
	}
}

protected void cleanUp() {
	super.cleanUp();

	this.locations = null;
	this.previousLocations = null;
	this.typeNames = null;
	this.qualifiedStrings = null;
	this.simpleStrings = null;
}

protected boolean findAffectedSourceFiles(IResourceDelta delta, IResource[] binaryResources) {
	for (int j = 0, k = binaryResources.length; j < k; j++) {
		IResource binaryResource = binaryResources[j];
		// either a .class file folder or a zip/jar file
		if (binaryResource != null) { // skip unchanged output folder
			IResourceDelta binaryDelta = delta.findMember(binaryResource.getProjectRelativePath());
			if (binaryDelta != null) {
				if (binaryResource instanceof IFile) {
					if (JavaBuilder.DEBUG)
						System.out.println("ABORTING incremental build... found delta to jar/zip file"); //$NON-NLS-1$
					return false; // do full build since jar file was added/removed/changed
				}
				if (binaryDelta.getKind() == IResourceDelta.ADDED || binaryDelta.getKind() == IResourceDelta.REMOVED) {
					if (JavaBuilder.DEBUG)
						System.out.println("ABORTING incremental build... found added/removed binary folder"); //$NON-NLS-1$
					return false; // added/removed binary folder should not make it here, but handle anyways
				}
				int segmentCount = binaryResource.getLocation().segmentCount();
				IResourceDelta[] children = binaryDelta.getAffectedChildren(); // .class files from class folder
				for (int i = 0, length = children.length; i < length; ++i)
					findAffectedSourceFiles(children[i], segmentCount);
				notifier.checkCancel();
			}
		}
	}
	return true;
}

protected void findAffectedSourceFiles(IResourceDelta binaryDelta, int segmentCount) {
	// When a package becomes a type or vice versa, expect 2 deltas,
	// one on the folder & one on the class file
	IResource resource = binaryDelta.getResource();
	IPath location = resource.getLocation();
	switch(resource.getType()) {
		case IResource.PROJECT :
		case IResource.FOLDER :
			switch (binaryDelta.getKind()) {
				case IResourceDelta.ADDED :
				case IResourceDelta.REMOVED :
					IPath packagePath = location.removeFirstSegments(segmentCount).makeRelative().setDevice(null);
					String packageName = packagePath.toString();
					if (binaryDelta.getKind() == IResourceDelta.ADDED) {
						// see if any known source file is from the same package... classpath already includes new package
						if (!newState.isKnownPackage(packageName)) {
							if (JavaBuilder.DEBUG)
								System.out.println("Add dependents of added package " + packageName); //$NON-NLS-1$
							addDependentsOf(packagePath, false);
							return;
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Skipped dependents of added package " + packageName); //$NON-NLS-1$
					} else {
						// see if the package still exists on the classpath
						if (!nameEnvironment.isPackage(packageName)) {
							if (JavaBuilder.DEBUG)
								System.out.println("Add dependents of removed package " + packageName); //$NON-NLS-1$
							addDependentsOf(packagePath, false);
							return;
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Skipped dependents of removed package " + packageName); //$NON-NLS-1$
					}
					// fall thru & traverse the sub-packages and .class files
				case IResourceDelta.CHANGED :
					IResourceDelta[] children = binaryDelta.getAffectedChildren();
					for (int i = 0, length = children.length; i < length; i++)
						findAffectedSourceFiles(children[i], segmentCount);
			}
			return;
		case IResource.FILE :
			if (JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(location.getFileExtension())) {
				IPath typePath = location.removeFirstSegments(segmentCount).removeFileExtension().makeRelative().setDevice(null);
				switch (binaryDelta.getKind()) {
					case IResourceDelta.ADDED :
					case IResourceDelta.REMOVED :
						if (JavaBuilder.DEBUG)
							System.out.println("Add dependents of added/removed class file " + typePath); //$NON-NLS-1$
						addDependentsOf(typePath, false);
						return;
					case IResourceDelta.CHANGED :
						if ((binaryDelta.getFlags() & IResourceDelta.CONTENT) == 0)
							return; // skip it since it really isn't changed
						if (JavaBuilder.DEBUG)
							System.out.println("Add dependents of changed class file " + typePath); //$NON-NLS-1$
						addDependentsOf(typePath, false);
				}
				return;
			}
	}
}

protected boolean findSourceFiles(IResourceDelta delta) throws CoreException {
	for (int i = 0, length = sourceFolders.length; i < length; i++) {
		IResourceDelta sourceDelta = delta.findMember(sourceFolders[i].getProjectRelativePath());
		if (sourceDelta != null) {
			if (sourceDelta.getKind() == IResourceDelta.REMOVED) {
				if (JavaBuilder.DEBUG)
					System.out.println("ABORTING incremental build... found removed source folder"); //$NON-NLS-1$
				return false; // removed source folder should not make it here, but handle anyways (ADDED is supported)
			}
			int segmentCount = sourceFolders[i].getLocation().segmentCount();
			IResourceDelta[] children = sourceDelta.getAffectedChildren();
			for (int c = 0, clength = children.length; c < clength; c++)
				findSourceFiles(children[c], segmentCount);
			notifier.checkCancel();
		}
	}
	return true;
}

protected void findSourceFiles(IResourceDelta sourceDelta, int segmentCount) throws CoreException {
	// When a package becomes a type or vice versa, expect 2 deltas,
	// one on the folder & one on the source file
	IResource resource = sourceDelta.getResource();
	IPath location = resource.getLocation();
	switch(resource.getType()) {
		case IResource.PROJECT :
		case IResource.FOLDER :
			switch (sourceDelta.getKind()) {
				case IResourceDelta.ADDED :
					IPath addedPackagePath = location.removeFirstSegments(segmentCount).makeRelative().setDevice(null);
					getOutputFolder(addedPackagePath); // ensure package exists in the output folder
					// add dependents even when the package thinks it exists to be on the safe side
					if (JavaBuilder.DEBUG)
						System.out.println("Add dependents of added package " + addedPackagePath); //$NON-NLS-1$
					addDependentsOf(addedPackagePath, true);
					// fall thru & collect all the source files
				case IResourceDelta.CHANGED :
					IResourceDelta[] children = sourceDelta.getAffectedChildren();
					for (int i = 0, length = children.length; i < length; i++)
						findSourceFiles(children[i], segmentCount);
					return;
				case IResourceDelta.REMOVED :
					IPath removedPackagePath = location.removeFirstSegments(segmentCount).makeRelative().setDevice(null);
					for (int i = 0, length = sourceFolders.length; i < length; i++) {
						if (sourceFolders[i].findMember(removedPackagePath) != null) {
							// only a package fragment was removed, same as removing multiple source files
							getOutputFolder(removedPackagePath); // ensure package exists in the output folder
							IResourceDelta[] removedChildren = sourceDelta.getAffectedChildren();
							for (int j = 0, rlength = removedChildren.length; j < rlength; j++)
								findSourceFiles(removedChildren[j], segmentCount);
							return;
						}
					}
					IFolder removedPackageFolder = outputFolder.getFolder(removedPackagePath);
					if (removedPackageFolder.exists())
						removedPackageFolder.delete(IResource.FORCE, null);
					// add dependents even when the package thinks it does not exist to be on the safe side
					if (JavaBuilder.DEBUG)
						System.out.println("Add dependents of removed package " + removedPackagePath); //$NON-NLS-1$
					addDependentsOf(removedPackagePath, true);
					newState.removePackage(sourceDelta);
			}
			return;
		case IResource.FILE :
			String extension = location.getFileExtension();
			if (JavaBuilder.JAVA_EXTENSION.equalsIgnoreCase(extension)) {
				IPath typePath = location.removeFirstSegments(segmentCount).removeFileExtension().makeRelative().setDevice(null);
				String sourceLocation = location.toString();
				switch (sourceDelta.getKind()) {
					case IResourceDelta.ADDED :
						if (JavaBuilder.DEBUG)
							System.out.println("Compile this added source file " + sourceLocation); //$NON-NLS-1$
						locations.add(sourceLocation);
						String typeName = typePath.toString();
						typeNames.add(typeName);
						if (!newState.isDuplicateLocation(typeName, sourceLocation)) { // adding dependents results in 2 duplicate errors
							if (JavaBuilder.DEBUG)
								System.out.println("Add dependents of added source file " + typeName); //$NON-NLS-1$
							addDependentsOf(typePath, true);
						}
						return;
					case IResourceDelta.REMOVED :
						char[][] definedTypeNames = newState.getDefinedTypeNamesFor(sourceLocation);
						if (definedTypeNames == null) { // defined a single type matching typePath
							removeClassFile(typePath);
						} else {
							if (JavaBuilder.DEBUG)
								System.out.println("Add dependents of removed source file " + typePath.toString()); //$NON-NLS-1$
							addDependentsOf(typePath, true); // add dependents of the source file since it may be involved in a name collision
							if (definedTypeNames.length > 0) { // skip it if it failed to successfully define a type
								IPath packagePath = typePath.removeLastSegments(1);
								for (int i = 0, length = definedTypeNames.length; i < length; i++)
									removeClassFile(packagePath.append(new String(definedTypeNames[i])));
							}
						}
						newState.remove(sourceLocation);
						return;
					case IResourceDelta.CHANGED :
						if ((sourceDelta.getFlags() & IResourceDelta.CONTENT) == 0)
							return; // skip it since it really isn't changed
						if (JavaBuilder.DEBUG)
							System.out.println("Compile this changed source file " + sourceLocation); //$NON-NLS-1$
						locations.add(sourceLocation);
						typeNames.add(typePath.toString());
				}
				return;
			} else if (JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(extension)) {
				return; // skip class files
			} else if (hasSeparateOutputFolder) {
				if (javaBuilder.filterResource(resource)) return;

				// copy all other resource deltas to the output folder
				IPath resourcePath = location.removeFirstSegments(segmentCount).makeRelative();
				IResource outputFile = outputFolder.getFile(resourcePath);
				switch (sourceDelta.getKind()) {
					case IResourceDelta.ADDED :
						if (outputFile.exists()) {
							if (JavaBuilder.DEBUG)
								System.out.println("Deleting existing file " + resourcePath); //$NON-NLS-1$
							outputFile.delete(IResource.FORCE, null);
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Copying added file " + resourcePath); //$NON-NLS-1$
						getOutputFolder(resourcePath.removeLastSegments(1)); // ensure package exists in the output folder
						resource.copy(outputFile.getFullPath(), IResource.FORCE, null);
						outputFile.setDerived(true);
						return;
					case IResourceDelta.REMOVED :
						if (outputFile.exists()) {
							if (JavaBuilder.DEBUG)
								System.out.println("Deleting removed file " + resourcePath); //$NON-NLS-1$
							outputFile.delete(IResource.FORCE, null);
						}
						return;
					case IResourceDelta.CHANGED :
						if ((sourceDelta.getFlags() & IResourceDelta.CONTENT) == 0)
							return; // skip it since it really isn't changed
						if (outputFile.exists()) {
							if (JavaBuilder.DEBUG)
								System.out.println("Deleting existing file " + resourcePath); //$NON-NLS-1$
							outputFile.delete(IResource.FORCE, null);
						}
						if (JavaBuilder.DEBUG)
							System.out.println("Copying changed file " + resourcePath); //$NON-NLS-1$
						getOutputFolder(resourcePath.removeLastSegments(1)); // ensure package exists in the output folder
						resource.copy(outputFile.getFullPath(), IResource.FORCE, null);
						outputFile.setDerived(true);
				}
				return;
			}
	}
}

protected void finishedWith(String sourceLocation, CompilationResult result, char[] mainTypeName, ArrayList definedTypeNames, ArrayList duplicateTypeNames) throws CoreException {
	char[][] previousTypeNames = newState.getDefinedTypeNamesFor(sourceLocation);
	if (previousTypeNames == null)
		previousTypeNames = new char[][] {mainTypeName};
	IPath packagePath = null;
	next : for (int i = 0, x = previousTypeNames.length; i < x; i++) {
		char[] previous = previousTypeNames[i];
		for (int j = 0, y = definedTypeNames.size(); j < y; j++)
			if (CharOperation.equals(previous, (char[]) definedTypeNames.get(j)))
				continue next;

		if (packagePath == null)
			packagePath = new Path(extractTypeNameFrom(sourceLocation)).removeLastSegments(1);
		if (secondaryTypesToRemove == null)
			this.secondaryTypesToRemove = new ArrayList();
		secondaryTypesToRemove.add(packagePath.append(new String(previous)));
	}
	super.finishedWith(sourceLocation, result, mainTypeName, definedTypeNames, duplicateTypeNames);
}

protected void removeClassFile(IPath typePath) throws CoreException {
	if (typePath.lastSegment().indexOf('$') == -1) { // is not a nested type
		newState.removeTypeLocation(typePath.toString());
		// add dependents even when the type thinks it does not exist to be on the safe side
		if (JavaBuilder.DEBUG)
			System.out.println("Add dependents of removed type " + typePath); //$NON-NLS-1$
		addDependentsOf(typePath, true); // when member types are removed, their enclosing type is structurally changed
	}
	IFile classFile = outputFolder.getFile(typePath.addFileExtension(JavaBuilder.CLASS_EXTENSION));
	if (classFile.exists()) {
		if (JavaBuilder.DEBUG)
			System.out.println("Deleting class file of removed type " + typePath); //$NON-NLS-1$
		classFile.delete(IResource.FORCE, null);
	}
}

protected void removeSecondaryTypes() throws CoreException {
	if (secondaryTypesToRemove != null) { // delayed deleting secondary types until the end of the compile loop
		for (int i = 0, length = secondaryTypesToRemove.size(); i < length; i++)
			removeClassFile((IPath) secondaryTypesToRemove.get(i));
		this.secondaryTypesToRemove = null;
		if (previousLocations != null && previousLocations.size() > 1)
			this.previousLocations = null; // cannot optimize recompile case when a secondary type is deleted
	}
}

protected void resetCollections() {
	previousLocations = locations.isEmpty() ? null : (ArrayList) locations.clone();

	locations.clear();
	typeNames.clear();
	qualifiedStrings.clear();
	simpleStrings.clear();
	workQueue.clear();
}

protected void updateProblemsFor(String sourceLocation, CompilationResult result) throws CoreException {
	IResource resource = resourceForLocation(sourceLocation);
	IMarker[] markers = JavaBuilder.getProblemsFor(resource);
	IProblem[] problems = result.getProblems();
	if (problems == null || problems.length == 0)
		if (markers.length == 0) return;

	notifier.updateProblemCounts(markers, problems);
	JavaBuilder.removeProblemsFor(resource);
	storeProblemsFor(resource, problems);
}

protected boolean writeClassFileCheck(IFile file, String fileName, byte[] newBytes, boolean isSecondaryType) throws CoreException {
	// Before writing out the class file, compare it to the previous file
	// If structural changes occured then add dependent source files
	if (file.exists()) {
		try {
			byte[] oldBytes = Util.getResourceContentsAsByteArray(file);
			notEqual : if (newBytes.length == oldBytes.length) {
				for (int i = newBytes.length; --i >= 0;)
					if (newBytes[i] != oldBytes[i]) break notEqual;
				return false; // bytes are identical so skip them
			}
			ClassFileReader reader = new ClassFileReader(oldBytes, file.getLocation().toString().toCharArray());
			// ignore local types since they're only visible inside a single method
			if (!(reader.isLocal() || reader.isAnonymous()) && reader.hasStructuralChanges(newBytes)) {
				if (JavaBuilder.DEBUG)
					System.out.println("Type has structural changes " + fileName); //$NON-NLS-1$
				addDependentsOf(new Path(fileName), true);
			}
		} catch (ClassFormatException e) {
			addDependentsOf(new Path(fileName), true);
		}

		file.delete(IResource.FORCE, null);
	} else if (isSecondaryType) {
		addDependentsOf(new Path(fileName), true); // new secondary type
	}
	return true;
}

public String toString() {
	return "incremental image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}


/* Debug helper

static void dump(IResourceDelta delta) {
	StringBuffer buffer = new StringBuffer();
	IPath path = delta.getFullPath();
	for (int i = path.segmentCount(); --i > 0;)
		buffer.append("  ");
	switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			buffer.append('+');
			break;
		case IResourceDelta.REMOVED:
			buffer.append('-');
			break;
		case IResourceDelta.CHANGED:
			buffer.append('*');
			break;
		case IResourceDelta.NO_CHANGE:
			buffer.append('=');
			break;
		default:
			buffer.append('?');
			break;
	}
	buffer.append(path);
	System.out.println(buffer.toString());
	IResourceDelta[] children = delta.getAffectedChildren();
	for (int i = 0, length = children.length; i < length; ++i)
		dump(children[i]);
}
*/
}