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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.*;

// AspectJ - increased member visibilities
public class JavaBuilder extends IncrementalProjectBuilder {

public IProject currentProject;
public IJavaProject javaProject;
public IWorkspaceRoot workspaceRoot;
public ClasspathLocation[] classpath;
public IContainer outputFolder;
public IContainer[] sourceFolders;
public SimpleLookupTable binaryResources; // maps a project to its binary resources (output folder, class folders, zip/jar files)
public State lastState;
public BuildNotifier notifier;
char[][] fileFilters;
String[] folderFilters;

public static final String JAVA_EXTENSION = "java"; //$NON-NLS-1$
public static final String CLASS_EXTENSION = "class"; //$NON-NLS-1$
public static final String JAR_EXTENSION = "jar"; //$NON-NLS-1$
public static final String ZIP_EXTENSION = "zip"; //$NON-NLS-1$

public static boolean DEBUG = false;

static final String ProblemMarkerTag = IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER;
/**
 * A list of project names that have been built.
 * This list is used to reset the JavaModel.existingExternalFiles cache when a build cycle begins
 * so that deleted external jars are discovered.
 */
static ArrayList builtProjects = null;

public static IMarker[] getProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			return resource.findMarkers(ProblemMarkerTag, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there are no problems
	return new IMarker[0];
}

public static void removeProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			resource.deleteMarkers(ProblemMarkerTag, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there were no problems
}

public static State readState(DataInputStream in) throws IOException {
	return State.read(in);
}

public static void writeState(Object state, DataOutputStream out) throws IOException {
	((State) state).write(out);
}

public JavaBuilder() {
}

protected IProject[] build(int kind, Map ignored, IProgressMonitor monitor) throws CoreException {
	this.currentProject = getProject();
	if (currentProject == null || !currentProject.isAccessible()) return new IProject[0];

	if (DEBUG)
		System.out.println("\nStarting build of " + currentProject.getName() //$NON-NLS-1$
			+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
	this.notifier = new BuildNotifier(monitor, currentProject);
	notifier.begin();
	boolean ok = false;
	try {
		notifier.checkCancel();
		initializeBuilder();

		if (isWorthBuilding()) {
			if (kind == FULL_BUILD) {
				buildAll();
			} else {
				if ((this.lastState = getLastState(currentProject)) == null) {
					if (DEBUG)
						System.out.println("Performing full build since last saved state was not found"); //$NON-NLS-1$
					buildAll();
				} else if (hasClasspathChanged() || hasOutputLocationChanged()) {
					// if the output location changes, do not delete the binary files from old location
					// the user may be trying something
					buildAll();
				} else if (sourceFolders.length > 0) { // if there is no source to compile & no classpath changes then we are done
					SimpleLookupTable deltas = findDeltas();
					if (deltas == null)
						buildAll();
					else if (deltas.elementSize > 0)
						buildDeltas(deltas);
					else if (DEBUG)
						System.out.println("Nothing to build since deltas were empty"); //$NON-NLS-1$
				} else {
					if (hasBinaryDelta()) { // double check that a jar file didn't get replaced
						buildAll();
					} else {
						if (DEBUG)
							System.out.println("Nothing to build since there are no source folders and no deltas"); //$NON-NLS-1$
						this.lastState.tagAsNoopBuild();
					}
				}
			}
			ok = true;
		}
	} catch (CoreException e) {
		Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
		IMarker marker = currentProject.createMarker(ProblemMarkerTag);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject")); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} catch (ImageBuilderInternalException e) {
		Util.log(e.getThrowable(), "JavaBuilder handling ImageBuilderInternalException"); //$NON-NLS-1$
		IMarker marker = currentProject.createMarker(ProblemMarkerTag);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject")); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} catch (MissingClassFileException e) {
		// do not log this exception since its thrown to handle aborted compiles because of missing class files
		if (DEBUG)
			System.out.println(Util.bind("build.incompleteClassPath", e.missingClassFile)); //$NON-NLS-1$
		IMarker marker = currentProject.createMarker(ProblemMarkerTag);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.incompleteClassPath", e.missingClassFile)); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} catch (MissingSourceFileException e) {
		// do not log this exception since its thrown to handle aborted compiles because of missing source files
		if (DEBUG)
			System.out.println(Util.bind("build.missingSourceFile", e.missingSourceFile)); //$NON-NLS-1$
		removeProblemsFor(currentProject); // make this the only problem for this project
		IMarker marker = currentProject.createMarker(ProblemMarkerTag);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.missingSourceFile", e.missingSourceFile)); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} finally {
		if (!ok)
			// If the build failed, clear the previously built state, forcing a full build next time.
			clearLastState();
		notifier.done();
		cleanup();
	}
	IProject[] requiredProjects = getRequiredProjects(true);
	if (DEBUG)
		System.out.println("Finished build of " + currentProject.getName() //$NON-NLS-1$
			+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
	return requiredProjects;
}

private void buildAll() {
	notifier.checkCancel();
	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	if (DEBUG && this.lastState != null)
		System.out.println("Clearing last state : " + this.lastState); //$NON-NLS-1$
	clearLastState();
	BatchImageBuilder imageBuilder = new BatchImageBuilder(this);
	imageBuilder.build();
	recordNewState(imageBuilder.newState);
}

private void buildDeltas(SimpleLookupTable deltas) {
	notifier.checkCancel();
	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	if (DEBUG && this.lastState != null)
		System.out.println("Clearing last state : " + this.lastState); //$NON-NLS-1$
	clearLastState(); // clear the previously built state so if the build fails, a full build will occur next time
	IncrementalImageBuilder imageBuilder = new IncrementalImageBuilder(this);
	if (imageBuilder.build(deltas))
		recordNewState(imageBuilder.newState);
	else
		buildAll();
}

private void cleanup() {
	this.classpath = null;
	this.outputFolder = null;
	this.sourceFolders = null;
	this.lastState = null;
	this.notifier = null;
}

private void clearLastState() {
	JavaModelManager.getJavaModelManager().setLastBuiltState(currentProject, null);
}

private void createFolder(IContainer folder) throws CoreException {
	if (!folder.exists()) {
		IContainer parent = folder.getParent();
		if (currentProject.getFullPath() != parent.getFullPath())
			createFolder(parent);
		((IFolder) folder).create(true, true, null);
	}
}

boolean filterResource(IResource resource) {
	if (fileFilters != null) {
		char[] name = resource.getName().toCharArray();
		for (int i = 0, length = fileFilters.length; i < length; i++)
			if (CharOperation.match(fileFilters[i], name, true))
				return true;
	}
	if (folderFilters != null) {
		IPath path = resource.getProjectRelativePath();
		String pathName = path.toString();
		int count = path.segmentCount();
		if (resource.getType() == IResource.FILE) count--;
		for (int i = 0, l = folderFilters.length; i < l; i++)
			if (pathName.indexOf(folderFilters[i]) != -1)
				for (int j = 0; j < count; j++)
					if (folderFilters[i].equals(path.segment(j)))
						return true;
	}
	return false;
}

private SimpleLookupTable findDeltas() {
	notifier.subTask(Util.bind("build.readingDelta", currentProject.getName())); //$NON-NLS-1$
	IResourceDelta delta = getDelta(currentProject);
	SimpleLookupTable deltas = new SimpleLookupTable(3);
	if (delta != null) {
		if (delta.getKind() != IResourceDelta.NO_CHANGE) {
			if (DEBUG)
				System.out.println("Found source delta for: " + currentProject.getName()); //$NON-NLS-1$
			deltas.put(currentProject, delta);
		}
	} else {
		if (DEBUG)
			System.out.println("Missing delta for: " + currentProject.getName()); //$NON-NLS-1$
		notifier.subTask(""); //$NON-NLS-1$
		return null;
	}

	Object[] keyTable = binaryResources.keyTable;
	Object[] valueTable = binaryResources.valueTable;
	nextProject : for (int i = 0, l = keyTable.length; i < l; i++) {
		IProject p = (IProject) keyTable[i];
		if (p != null && p != currentProject) {
			State s = getLastState(p);
			if (!lastState.wasStructurallyChanged(p, s)) { // see if we can skip its delta
				if (s.wasNoopBuild())
					continue nextProject; // project has no source folders and can be skipped
				IResource[] classFoldersAndJars = (IResource[]) valueTable[i];
				if (classFoldersAndJars.length <= 1)
					continue nextProject; // project has no structural changes in its output folder
				classFoldersAndJars[0] = null; // skip the output folder
			}

			notifier.subTask(Util.bind("build.readingDelta", p.getName())); //$NON-NLS-1$
			delta = getDelta(p);
			if (delta != null) {
				if (delta.getKind() != IResourceDelta.NO_CHANGE) {
					if (DEBUG)
						System.out.println("Found binary delta for: " + p.getName()); //$NON-NLS-1$
					deltas.put(p, delta);
				}
			} else {
				if (DEBUG)
					System.out.println("Missing delta for: " + p.getName());	 //$NON-NLS-1$
				notifier.subTask(""); //$NON-NLS-1$
				return null;
			}
		}
	}
	notifier.subTask(""); //$NON-NLS-1$
	return deltas;
}

private State getLastState(IProject project) {
	return (State) JavaModelManager.getJavaModelManager().getLastBuiltState(project, notifier.monitor);
}

/* Return the list of projects for which it requires a resource delta. This builder's project
* is implicitly included and need not be specified. Builders must re-specify the list 
* of interesting projects every time they are run as this is not carried forward
* beyond the next build. Missing projects should be specified but will be ignored until
* they are added to the workspace.
*/
private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites) {
	if (javaProject == null || workspaceRoot == null) return new IProject[0];

	ArrayList projects = new ArrayList();
	try {
		IClasspathEntry[] entries = ((JavaProject) javaProject).getExpandedClasspath(true);
		for (int i = 0, length = entries.length; i < length; i++) {
			IClasspathEntry entry = JavaCore.getResolvedClasspathEntry(entries[i]);
			if (entry != null) {
				IPath path = entry.getPath();
				IProject p = null;
				switch (entry.getEntryKind()) {
					case IClasspathEntry.CPE_PROJECT :
						p = workspaceRoot.getProject(path.lastSegment());
						break;
					case IClasspathEntry.CPE_LIBRARY :
						if (includeBinaryPrerequisites && path.segmentCount() > 1) {
							// some binary resources on the class path can come from projects that are not included in the project references
							IResource resource = workspaceRoot.findMember(path.segment(0));
							if (resource instanceof IProject)
								p = (IProject) resource;
						}
				}
				if (p != null && !projects.contains(p))
					projects.add(p);
			}
		}
	} catch(JavaModelException e) {
		return new IProject[0];
	}
	IProject[] result = new IProject[projects.size()];
	projects.toArray(result);
	return result;
}

private boolean hasClasspathChanged() {
	ClasspathLocation[] oldClasspathLocations = lastState.classpathLocations;
	int newLength = classpath.length;
	int oldLength = oldClasspathLocations.length;
	int diff = newLength - oldLength;
	if (diff == 0) {
		for (int i = 0; i < newLength; i++) {
			if (classpath[i].equals(oldClasspathLocations[i])) continue;
			if (DEBUG)
				System.out.println(classpath[i] + " != " + oldClasspathLocations[i]); //$NON-NLS-1$
			return true;
		}
		return false;
	} else if (diff == 1) {
		ClasspathMultiDirectory newSourceDirectory = null;
		int n = 0, o = 0;
		for (; n < newLength && o < oldLength; n++, o++) {
			if (classpath[n].equals(oldClasspathLocations[o])) continue;
			if (diff == 1 && classpath[n] instanceof ClasspathMultiDirectory) { // added a new source folder
				newSourceDirectory = (ClasspathMultiDirectory) classpath[n];
				o--;
				diff = 0; // found new element
				continue;
			}
			if (DEBUG)
				System.out.println(classpath[n] + " != " + oldClasspathLocations[o]); //$NON-NLS-1$
			return true;
		}

		if (diff == 1 && classpath[n] instanceof ClasspathMultiDirectory) // added a new source folder at the end
			newSourceDirectory = (ClasspathMultiDirectory) classpath[n];
		if (newSourceDirectory != null) {
			IContainer sourceFolder = workspaceRoot.getContainerForLocation(new Path(newSourceDirectory.sourcePath));
			if (sourceFolder != null && sourceFolder.exists()) {
				try {
					if (sourceFolder.members().length == 0) return false; // added a new empty source folder
				} catch (CoreException ignore) {}
			}
		}
	}

	if (DEBUG)
		System.out.println("Class path size changed"); //$NON-NLS-1$
	return true;
}

private boolean hasOutputLocationChanged() {
	if (outputFolder.getLocation().toString().equals(lastState.outputLocationString))
		return false;

	if (DEBUG)
		System.out.println(outputFolder.getLocation().toString() + " != " + lastState.outputLocationString); //$NON-NLS-1$
	return true;
} 

private boolean hasBinaryDelta() {
	IResourceDelta delta = getDelta(currentProject);
	if (delta != null && delta.getKind() != IResourceDelta.NO_CHANGE) {
		IResource[] classFoldersAndJars = (IResource[]) binaryResources.get(currentProject);
		if (classFoldersAndJars != null) {
			for (int i = 0, l = classFoldersAndJars.length; i < l; i++) {
				IResource binaryResource = classFoldersAndJars[i]; // either a .class file folder or a zip/jar file
				if (binaryResource != null) {
					IResourceDelta binaryDelta = delta.findMember(binaryResource.getProjectRelativePath());
					if (binaryDelta != null) return true;
				}
			}
		}
	}
	return false;
}

private void initializeBuilder() throws CoreException {
	this.javaProject = JavaCore.create(currentProject);
	this.workspaceRoot = currentProject.getWorkspace().getRoot();
	this.outputFolder = (IContainer) workspaceRoot.findMember(javaProject.getOutputLocation());
	if (this.outputFolder == null) {
		this.outputFolder = workspaceRoot.getFolder(javaProject.getOutputLocation());
		createFolder(this.outputFolder);
	}

	// Flush the existing external files cache if this is the beginning of a build cycle
	String projectName = this.currentProject.getName();
	if (builtProjects == null || builtProjects.contains(projectName)) {
		JavaModel.flushExternalFileCache();
		builtProjects = new ArrayList();
	}
	builtProjects.add(projectName);

	ArrayList sourceList = new ArrayList();
	this.binaryResources = new SimpleLookupTable(3);
	this.classpath = NameEnvironment.computeLocations(
		workspaceRoot,
		javaProject,
		outputFolder.getLocation().toString(),
		sourceList,
		binaryResources);
	this.sourceFolders = new IContainer[sourceList.size()];
	sourceList.toArray(this.sourceFolders);

	String filterSequence = JavaCore.getOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);
	char[][] filters = filterSequence != null && filterSequence.length() > 0
		? CharOperation.splitAndTrimOn(',', filterSequence.toCharArray())
		: null;
	if (filters == null) {
		this.fileFilters = null;
		this.folderFilters = null;
	} else {
		int fileCount = 0, folderCount = 0;
		for (int i = 0, length = filters.length; i < length; i++) {
			char[] f = filters[i];
			if (f.length == 0) continue;
			if (f[f.length - 1] == '/') folderCount++; else fileCount++;
		}
		this.fileFilters = new char[fileCount][];
		this.folderFilters = new String[folderCount];
		for (int i = 0, length = filters.length; i < length; i++) {
			char[] f = filters[i];
			if (f.length == 0) continue;
			if (f[f.length - 1] == '/')
				folderFilters[--folderCount] = new String(CharOperation.subarray(f, 0, f.length - 1));
			else
				fileFilters[--fileCount] = f;
		}
	}
}

private boolean isWorthBuilding() throws CoreException {
	boolean abortBuilds = JavaCore.ABORT.equals(JavaCore.getOption(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH));
	if (!abortBuilds) return true;

	IMarker[] markers =
		currentProject.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
	if (markers.length > 0) {
		if (DEBUG)
			System.out.println("Aborted build because project is involved in a cycle or has classpath problems"); //$NON-NLS-1$

		// remove all existing class files... causes all dependent projects to do the same
		new BatchImageBuilder(this).scrubOutputFolder();

		removeProblemsFor(currentProject); // remove all compilation problems

		IMarker marker = currentProject.createMarker(ProblemMarkerTag);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.abortDueToClasspathProblems")); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		return false;
	}

	// make sure all prereq projects have valid build states... only when aborting builds since projects in cycles do not have build states
	IProject[] requiredProjects = getRequiredProjects(false);
	next : for (int i = 0, length = requiredProjects.length; i < length; i++) {
		IProject p = requiredProjects[i];
		if (getLastState(p) == null)  {
			if (DEBUG)
				System.out.println("Aborted build because prereq project " + p.getName() //$NON-NLS-1$
					+ " was not built"); //$NON-NLS-1$

			// remove all existing class files... causes all dependent projects to do the same
			new BatchImageBuilder(this).scrubOutputFolder();

			removeProblemsFor(currentProject); // make this the only problem for this project
			IMarker marker = currentProject.createMarker(ProblemMarkerTag);
			marker.setAttribute(IMarker.MESSAGE, Util.bind("build.prereqProjectWasNotBuilt", p.getName())); //$NON-NLS-1$
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			return false;
		}
	}
	return true;
}

private void recordNewState(State state) {
	Object[] keyTable = binaryResources.keyTable;
	for (int i = 0, l = keyTable.length; i < l; i++) {
		IProject prereqProject = (IProject) keyTable[i];
		if (prereqProject != null && prereqProject != currentProject)
			state.recordStructuralDependency(prereqProject, getLastState(prereqProject));
	}

	if (DEBUG)
		System.out.println("Recording new state : " + state); //$NON-NLS-1$
	// state.dump();
	JavaModelManager.getJavaModelManager().setLastBuiltState(currentProject, state);
}

/**
 * String representation for debugging purposes
 */
public String toString() {
	return "JavaBuilder for " + currentProject.getName(); //$NON-NLS-1$
}
}