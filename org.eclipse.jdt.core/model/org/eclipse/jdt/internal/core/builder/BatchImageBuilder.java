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

import org.eclipse.jdt.internal.core.Util;

import java.util.*;

public class BatchImageBuilder extends AbstractImageBuilder {

protected BatchImageBuilder(JavaBuilder javaBuilder) {
	super(javaBuilder);
}

public void build() {
	if (JavaBuilder.DEBUG)
		System.out.println("FULL build"); //$NON-NLS-1$

	try {
		notifier.subTask(Util.bind("build.scrubbingOutput")); //$NON-NLS-1$
		JavaBuilder.removeProblemsFor(javaBuilder.currentProject);
		scrubOutputFolder();
		notifier.updateProgressDelta(0.1f);

		notifier.subTask(Util.bind("build.analyzingSources")); //$NON-NLS-1$
		ArrayList locations = new ArrayList(33);
		ArrayList typeNames = new ArrayList(33);
		addAllSourceFiles(locations, typeNames);
		notifier.updateProgressDelta(0.15f);

		if (locations.size() > 0) {
			String[] allSourceFiles = new String[locations.size()];
			locations.toArray(allSourceFiles);
			String[] initialTypeNames = new String[typeNames.size()];
			typeNames.toArray(initialTypeNames);

			notifier.setProgressPerCompilationUnit(0.75f / allSourceFiles.length);
			workQueue.addAll(allSourceFiles);
			compile(allSourceFiles, initialTypeNames);
		}
	} catch (CoreException e) {
		throw internalException(e);
	} finally {
		cleanUp();
	}
}

protected void addAllSourceFiles(final ArrayList locations, final ArrayList typeNames) throws CoreException {
	for (int i = 0, length = sourceFolders.length; i < length; i++) {
		final int srcFolderLength = sourceFolders[i].getLocation().addTrailingSeparator().toString().length();
		sourceFolders[i].accept(
			new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource.getType() == IResource.FILE) {
						if (JavaBuilder.JAVA_EXTENSION.equalsIgnoreCase(resource.getFileExtension())) {
							String sourceLocation = resource.getLocation().toString();
							locations.add(sourceLocation);
							typeNames.add(sourceLocation.substring(srcFolderLength, sourceLocation.length() - 5)); // length of .java
						}
						return false;
					}
					return true;
				}
			}
		);
		notifier.checkCancel();
	}
}

protected void scrubOutputFolder() throws CoreException {
	if (hasSeparateOutputFolder) {
		// outputPath is not on the class path so wipe it clean then copy extra resources back
		IResource[] members = outputFolder.members(); 
		for (int i = 0, length = members.length; i < length; i++)
			members[i].delete(IResource.FORCE, null);
		notifier.checkCancel();
		copyExtraResourcesBack();
	} else {
		// outputPath == a source folder so just remove class files
		outputFolder.accept(
			new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.FILE) {
						if (JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(resource.getFileExtension()))
							resource.delete(IResource.FORCE, null);
						return false;
					}
					return true;
				}
			}
		);
	}
	notifier.checkCancel();
}

protected void copyExtraResourcesBack() throws CoreException {
	// When, if ever, does a builder need to copy resources files (not .java or .class) into the output folder?
	// If we wipe the output folder at the beginning of the build then all 'extra' resources must be copied to the output folder.

	final IPath outputPath = outputFolder.getFullPath();
	for (int i = 0, length = sourceFolders.length; i < length; i++) {
		IContainer sourceFolder = sourceFolders[i];
		final IPath sourcePath = sourceFolder.getFullPath();
		final int segmentCount = sourcePath.segmentCount();
		sourceFolder.accept(
			new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					switch(resource.getType()) {
						case IResource.FILE :
							String extension = resource.getFileExtension();
							if (JavaBuilder.JAVA_EXTENSION.equalsIgnoreCase(extension)) return false;
							if (JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(extension)) return false;
							if (javaBuilder.filterResource(resource)) return false;

							IPath partialPath = resource.getFullPath().removeFirstSegments(segmentCount);
							IResource copiedResource = outputFolder.getFile(partialPath);
							if (copiedResource.exists()) {
								createErrorFor(resource, Util.bind("build.duplicateResource")); //$NON-NLS-1$
							} else {
								resource.copy(copiedResource.getFullPath(), IResource.FORCE, null);
								copiedResource.setDerived(true);
							}
							return false;
						case IResource.FOLDER :
							if (resource.getFullPath().equals(outputPath)) return false;
							if (resource.getFullPath().equals(sourcePath)) return true;
							if (javaBuilder.filterResource(resource)) return false;

							getOutputFolder(resource.getFullPath().removeFirstSegments(segmentCount));
					}
					return true;
				}
			}
		);
	}
}

public String toString() {
	return "batch image builder for:\n\tnew state: " + newState; //$NON-NLS-1$
}
}