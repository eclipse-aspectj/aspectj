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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.codeassist.ISearchableNameEnvironment;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.eval.EvaluationContextWrapper;
import org.eclipse.jdt.internal.eval.EvaluationContext;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Handle for a Java Project.
 *
 * <p>A Java Project internally maintains a devpath that corresponds
 * to the project's classpath. The classpath may include source folders
 * from the current project; jars in the current project, other projects,
 * and the local file system; and binary folders (output location) of other
 * projects. The Java Model presents source elements corresponding to output
 * .class files in other projects, and thus uses the devpath rather than
 * the classpath (which is really a compilation path). The devpath mimics
 * the classpath, except has source folder entries in place of output
 * locations in external projects.
 *
 * <p>Each JavaProject has a NameLookup facility that locates elements
 * on by name, based on the devpath.
 *
 * @see IJavaProject
 */
public class JavaProject
	extends Openable
	implements IJavaProject, IProjectNature {

	/**
	 * Whether the underlying file system is case sensitive.
	 */
	protected static final boolean IS_CASE_SENSITIVE = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES = new String[0];

	/**
	 * The platform project this <code>IJavaProject</code> is based on
	 */
	protected IProject fProject;

	/**
	 * Returns a canonicalized path from the given external path.
	 * Note that the return path contains the same number of segments
	 * and it contains a device only if the given path contained one.
	 * @see java.io.File for the definition of a canonicalized path
	 */
	public static IPath canonicalizedPath(IPath externalPath) {
		
		if (externalPath == null)
			return null;

//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonicalizing " + externalPath.toString()); //$NON-NLS-1$
//		}

		if (IS_CASE_SENSITIVE) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (file system is case sensitive)"); //$NON-NLS-1$
//			}
			return externalPath;
		}

		// if not external path, return original path
		if (ResourcesPlugin.getWorkspace().getRoot().findMember(externalPath) != null) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (member of workspace)"); //$NON-NLS-1$
//			}
			return externalPath;
		}

		IPath canonicalPath = null;
		try {
			canonicalPath =
				new Path(new File(externalPath.toOSString()).getCanonicalPath());
		} catch (IOException e) {
			// default to original path
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (IOException)"); //$NON-NLS-1$
//			}
			return externalPath;
		}
		
		IPath result;
		int canonicalLength = canonicalPath.segmentCount();
		if (canonicalLength == 0) {
			// the java.io.File canonicalization failed
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (canonical path is empty)"); //$NON-NLS-1$
//			}
			return externalPath;
		} else if (externalPath.isAbsolute()) {
			result = canonicalPath;
		} else {
			// if path is relative, remove the first segments that were added by the java.io.File canonicalization
			// e.g. 'lib/classes.zip' was converted to 'd:/myfolder/lib/classes.zip'
			int externalLength = externalPath.segmentCount();
			if (canonicalLength >= externalLength) {
				result = canonicalPath.removeFirstSegments(canonicalLength - externalLength);
			} else {
//				if (JavaModelManager.VERBOSE) {
//					System.out.println("JAVA MODEL - Canonical path is original path (canonical path is " + canonicalPath.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
//				}
				return externalPath;
			}
		}
		
		// keep device only if it was specified (this is because File.getCanonicalPath() converts '/lib/classed.zip' to 'd:/lib/classes/zip')
		if (externalPath.getDevice() == null) {
			result = result.setDevice(null);
		} 
//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonical path is " + result.toString()); //$NON-NLS-1$
//		}
		return result;
	}
	
	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected static Element getEntryAsXMLElement(
		Document document,
		IClasspathEntry entry,
		IPath prefixPath)
		throws JavaModelException {

		Element element = document.createElement("classpathentry"); //$NON-NLS-1$
		element.setAttribute("kind", kindToString(entry.getEntryKind()));	//$NON-NLS-1$
		IPath path = entry.getPath();
		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE && entry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
			// translate to project relative from absolute (unless a device path)
			if (path.isAbsolute()) {
				if (prefixPath != null && prefixPath.isPrefixOf(path)) {
					if (path.segment(0).equals(prefixPath.segment(0))) {
						path = path.removeFirstSegments(1);
						path = path.makeRelative();
					} else {
						path = path.makeAbsolute();
					}
				}
			}
		}
		element.setAttribute("path", path.toString()); //$NON-NLS-1$
		if (entry.getSourceAttachmentPath() != null) {
			element.setAttribute("sourcepath", entry.getSourceAttachmentPath().toString()); //$NON-NLS-1$
		}
		if (entry.getSourceAttachmentRootPath() != null) {
			element.setAttribute(
				"rootpath", //$NON-NLS-1$
				entry.getSourceAttachmentRootPath().toString());
		}
		if (entry.isExported()) {
			element.setAttribute("exported", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return element;
	}

	/**
	 * Returns the kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form.
	 */
	static int kindFromString(String kindStr) {

		if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
			return IClasspathEntry.CPE_PROJECT;
		if (kindStr.equalsIgnoreCase("var")) //$NON-NLS-1$
			return IClasspathEntry.CPE_VARIABLE;
		if (kindStr.equalsIgnoreCase("con")) //$NON-NLS-1$
			return IClasspathEntry.CPE_CONTAINER;
		if (kindStr.equalsIgnoreCase("src")) //$NON-NLS-1$
			return IClasspathEntry.CPE_SOURCE;
		if (kindStr.equalsIgnoreCase("lib")) //$NON-NLS-1$
			return IClasspathEntry.CPE_LIBRARY;
		if (kindStr.equalsIgnoreCase("output")) //$NON-NLS-1$
			return ClasspathEntry.K_OUTPUT;
		return -1;
	}

	/**
	 * Returns a <code>String</code> for the kind of a class path entry.
	 */
	static String kindToString(int kind) {

		switch (kind) {
			case IClasspathEntry.CPE_PROJECT :
				return "src"; // backward compatibility //$NON-NLS-1$
			case IClasspathEntry.CPE_SOURCE :
				return "src"; //$NON-NLS-1$
			case IClasspathEntry.CPE_LIBRARY :
				return "lib"; //$NON-NLS-1$
			case IClasspathEntry.CPE_VARIABLE :
				return "var"; //$NON-NLS-1$
			case IClasspathEntry.CPE_CONTAINER :
				return "con"; //$NON-NLS-1$
			case ClasspathEntry.K_OUTPUT :
				return "output"; //$NON-NLS-1$
			default :
				return "unknown"; //$NON-NLS-1$
		}
	}

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and <code>IProject.addNature()</code>.
	 *
	 * @see #setProject
	 */
	public JavaProject() {
		super(JAVA_PROJECT, null, null);
	}

	public JavaProject(IProject project, IJavaElement parent) {
		super(JAVA_PROJECT, parent, project.getName());
		fProject = project;
	}

	/**
	 * Adds a builder to the build spec for the given project.
	 */
	protected void addToBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = getProject().getDescription();
		ICommand javaCommand = getJavaCommand(description);

		if (javaCommand == null) {

			// Add a Java command to the build spec
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			setJavaCommand(description, command);
		}
	}

	protected void closing(Object info) throws JavaModelException {
		
		// forget source attachment recommendations
		IPackageFragmentRoot[] roots = this.getPackageFragmentRoots();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i] instanceof JarPackageFragmentRoot){
				((JarPackageFragmentRoot) roots[i]).setSourceAttachmentProperty(null); 
			}
		}
		super.closing(info);
	}
	
	/**
	 * Internal computation of an expanded classpath. It will eliminate duplicates, and produce copies
	 * of exported classpath entries to avoid possible side-effects ever after.
	 */			
	private void computeExpandedClasspath(
		JavaProject initialProject, 
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError,
		HashSet visitedProjects, 
		ObjectVector accumulatedEntries) throws JavaModelException {
		
		if (visitedProjects.contains(this)){
			return; // break cycles if any
		}
		visitedProjects.add(this);

		if (generateMarkerOnError && !this.equals(initialProject)){
			generateMarkerOnError = false;
		}
		IClasspathEntry[] immediateClasspath = 
			getResolvedClasspath(ignoreUnresolvedVariable, generateMarkerOnError);
			
		IWorkspaceRoot workspaceRoot = this.getWorkspace().getRoot();
		for (int i = 0, length = immediateClasspath.length; i < length; i++){
			IClasspathEntry entry = immediateClasspath[i];

			boolean isInitialProject = this.equals(initialProject);
			if (isInitialProject || entry.isExported()){
				
				accumulatedEntries.add(entry);
				
				// recurse in project to get all its indirect exports (only consider exported entries from there on)				
				if (entry.getEntryKind() == ClasspathEntry.CPE_PROJECT) {
					IProject projRsc = (IProject) workspaceRoot.findMember(entry.getPath());
					if (projRsc != null && projRsc.isOpen()) {				
						JavaProject project = (JavaProject) JavaCore.create(projRsc);
						project.computeExpandedClasspath(
							initialProject, 
							ignoreUnresolvedVariable, 
							generateMarkerOnError,
							visitedProjects, 
							accumulatedEntries);
					}
				}
			}			
		}
	}
	
	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(IClasspathEntry[] classpath, boolean retrieveExportedRoots) throws JavaModelException {

		ObjectVector accumulatedRoots = new ObjectVector();
		computePackageFragmentRoots(classpath, accumulatedRoots, new HashSet(5), true, true, retrieveExportedRoots);
		IPackageFragmentRoot[] rootArray = new IPackageFragmentRoot[accumulatedRoots.size()];
		accumulatedRoots.copyInto(rootArray);
		return rootArray;
	}

	/**
	 * Returns the package fragment roots identified by the given entry. In case it refers to
	 * a project, it will follow its classpath so as to find exported roots as well.
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry entry,
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		boolean insideOriginalProject,
		boolean checkExistency,
		boolean retrieveExportedRoots) throws JavaModelException {
			
		String rootID = ((ClasspathEntry)entry).rootID();
		if (rootIDs.contains(rootID)) return;

		IPath projectPath = getProject().getFullPath();
		IPath entryPath = entry.getPath();
		IWorkspaceRoot workspaceRoot = getWorkspace().getRoot();
		
		switch(entry.getEntryKind()){
			
			// source folder
			case IClasspathEntry.CPE_SOURCE :

				if (projectPath.isPrefixOf(entryPath)){
					Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
					if (target == null) return;

					if (target instanceof IFolder || target instanceof IProject){
						accumulatedRoots.add(
							new PackageFragmentRoot((IResource)target, this));
						rootIDs.add(rootID);
					}
				}
				break;

			// internal/external JAR or folder
			case IClasspathEntry.CPE_LIBRARY :
			
				if (!insideOriginalProject && !entry.isExported()) return;

				String extension = entryPath.getFileExtension();

				Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
				if (target == null) return;

				if (target instanceof IResource){
					
					// internal target
					IResource resource = (IResource) target;
					switch (resource.getType()){
						case IResource.FOLDER :
							accumulatedRoots.add(
								new PackageFragmentRoot(resource, this));
							rootIDs.add(rootID);
							break;
						case IResource.FILE :
							if ("jar".equalsIgnoreCase(extension) //$NON-NLS-1$
								|| "zip".equalsIgnoreCase(extension)) { //$NON-NLS-1$
								accumulatedRoots.add(
									new JarPackageFragmentRoot(resource, this));
								}
								rootIDs.add(rootID);
						break;
					}
				} else {
					// external target - only JARs allowed
					if (((java.io.File)target).isFile()
						&& ("jar".equalsIgnoreCase(extension) //$NON-NLS-1$
							|| "zip".equalsIgnoreCase(extension))) { //$NON-NLS-1$
						accumulatedRoots.add(
							new JarPackageFragmentRoot(entryPath.toOSString(), this));
						rootIDs.add(rootID);
					}
				}
				break;

			// recurse into required project
			case IClasspathEntry.CPE_PROJECT :

				if (!retrieveExportedRoots) return;
				if (!insideOriginalProject && !entry.isExported()) return;

				JavaProject requiredProject = (JavaProject)getJavaModel().getJavaProject(entryPath.segment(0));
				IProject requiredProjectRsc = requiredProject.getProject();
				if (requiredProjectRsc.exists() && requiredProjectRsc.isOpen()){ // special builder binary output
					rootIDs.add(rootID);
					requiredProject.computePackageFragmentRoots(
						requiredProject.getResolvedClasspath(true), 
						accumulatedRoots, 
						rootIDs, 
						false, 
						checkExistency, 
						retrieveExportedRoots);
				}
				break;
			}
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry[] classpath,
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		boolean insideOriginalProject,
		boolean checkExistency,
		boolean retrieveExportedRoots) throws JavaModelException {

		if (insideOriginalProject){
			rootIDs.add(rootID());
		}	
		for (int i = 0, length = classpath.length; i < length; i++){
			computePackageFragmentRoots(
				classpath[i],
				accumulatedRoots,
				rootIDs,
				insideOriginalProject,
				checkExistency,
				retrieveExportedRoots);
		}
	}

	/**
	 * Compute the file name to use for a given shared property
	 */
	public String computeSharedPropertyFileName(QualifiedName qName) {

		return '.' + qName.getLocalName();
	}
	
	/**
	 * Configure the project with Java nature.
	 */
	public void configure() throws CoreException {

		// register Java builder
		addToBuildSpec(JavaCore.BUILDER_ID);
	}

	/**
	 * Record a new marker denoting a classpath problem 
	 */
	void createClasspathProblemMarker(
		String message,
		int severity,
		boolean isCycleProblem,		
		boolean isClasspathFileFormatProblem) {
		try {
			IMarker marker = getProject().createMarker(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] { 
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IMarker.LOCATION, 
					IJavaModelMarker.CYCLE_DETECTED,
					IJavaModelMarker.CLASSPATH_FILE_FORMAT },
				new Object[] {
					message,
					new Integer(severity), 
					Util.bind("classpath.buildPath"),//$NON-NLS-1$
					isCycleProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					isClasspathFileFormatProblem ? "true" : "false"});//$NON-NLS-1$ //$NON-NLS-2$
		} catch (CoreException e) {
		}
	}

	/**
	 * Returns a new element info for this element.
	 */
	protected OpenableElementInfo createElementInfo() {

		return new JavaProjectElementInfo();
	}

	/**
	/**
	 * Removes the Java nature from the project.
	 */
	public void deconfigure() throws CoreException {

		// deregister Java builder
		removeFromBuildSpec(JavaCore.BUILDER_ID);
	}

	/**
	 * Returns a default class path.
	 * This is the root of the project
	 */
	protected IClasspathEntry[] defaultClasspath() throws JavaModelException {

		return new IClasspathEntry[] {
			 JavaCore.newSourceEntry(getProject().getFullPath())};
	}

	/**
	 * Returns a default output location.
	 * This is the project bin folder
	 */
	protected IPath defaultOutputLocation() throws JavaModelException {
		return getProject().getFullPath().append("bin"); //$NON-NLS-1$
	}

	/**
	 * Returns true if this handle represents the same Java project
	 * as the given handle. Two handles represent the same
	 * project if they are identical or if they represent a project with 
	 * the same underlying resource and occurrence counts.
	 *
	 * @see JavaElement#equals
	 */
	public boolean equals(Object o) {

		if (this == o)
			return true;

		if (!(o instanceof JavaProject))
			return false;

		JavaProject other = (JavaProject) o;
		return getProject().equals(other.getProject())
			&& fOccurrenceCount == other.fOccurrenceCount;
	}

	/**
	 * @see IJavaProject
	 */
	public IJavaElement findElement(IPath path) throws JavaModelException {

		if (path == null || path.isAbsolute()) {
			throw new JavaModelException(
				new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, path));
		}
		try {

			String extension = path.getFileExtension();
			if (extension == null) {
				String packageName = path.toString().replace(IPath.SEPARATOR, '.');

				IPackageFragment[] pkgFragments =
					getNameLookup().findPackageFragments(packageName, false);
				if (pkgFragments == null) {
					return null;

				} else {
					// try to return one that is a child of this project
					for (int i = 0, length = pkgFragments.length; i < length; i++) {

						IPackageFragment pkgFragment = pkgFragments[i];
						if (this.equals(pkgFragment.getParent().getParent())) {
							return pkgFragment;
						}
					}
					// default to the first one
					return pkgFragments[0];
				}
			} else if (
				extension.equalsIgnoreCase("java") //$NON-NLS-1$
					|| extension.equalsIgnoreCase("class")) {  //$NON-NLS-1$
				IPath packagePath = path.removeLastSegments(1);
				String packageName = packagePath.toString().replace(IPath.SEPARATOR, '.');
				String typeName = path.lastSegment();
				typeName = typeName.substring(0, typeName.length() - extension.length() - 1);
				String qualifiedName = null;
				if (packageName.length() > 0) {
					qualifiedName = packageName + "." + typeName; //$NON-NLS-1$
				} else {
					qualifiedName = typeName;
				}
				IType type =
					getNameLookup().findType(
						qualifiedName,
						false,
						NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
				if (type != null) {
					return type.getParent();
				} else {
					return null;
				}
			} else {
				// unsupported extension
				return null;
			}
		} catch (JavaModelException e) {
			if (e.getStatus().getCode()
				== IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
				return null;
			} else {
				throw e;
			}
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment findPackageFragment(IPath path)
		throws JavaModelException {

		return findPackageFragment0(this.canonicalizedPath(path));
	}

	/**
	 * non path canonicalizing version
	 */
	public IPackageFragment findPackageFragment0(IPath path) 
		throws JavaModelException {

		return getNameLookup().findPackageFragment(path);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path)
		throws JavaModelException {

		return findPackageFragmentRoot0(this.canonicalizedPath(path));
	}

	/**
	 * no path canonicalization 
	 */
	public IPackageFragmentRoot findPackageFragmentRoot0(IPath path)
		throws JavaModelException {

		IPackageFragmentRoot[] allRoots = this.getAllPackageFragmentRoots();
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Util.bind("path.mustBeAbsolute")); //$NON-NLS-1$
		}
		for (int i= 0; i < allRoots.length; i++) {
			IPackageFragmentRoot classpathRoot= allRoots[i];
			if (classpathRoot.getPath().equals(path)) {
				return classpathRoot;
			}
		}
		return null;
	}
	
	/**
	 * @see IJavaProject#findType(String)
	 */
	public IType findType(String fullyQualifiedName) throws JavaModelException {
		IType type = 
			this.getNameLookup().findType(
				fullyQualifiedName, 
				false,
				NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
		if (type == null) {
			// try to find enclosing type
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			if (lastDot == -1) return null;
			type = this.findType(fullyQualifiedName.substring(0, lastDot));
			if (type != null) {
				type = type.getType(fullyQualifiedName.substring(lastDot+1));
				if (!type.exists()) {
					return null;
				}
			}
		}
		return type;
	}
	
	/**
	 * @see IJavaProject#findType(String, String)
	 */
	public IType findType(String packageName, String typeQualifiedName) throws JavaModelException {
		return 
			this.getNameLookup().findType(
				typeQualifiedName, 
				packageName,
				false,
				NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES);
	}	
	
	/**
	 * Remove all markers denoting classpath problems
	 */
	protected void flushClasspathProblemMarkers(boolean flushCycleMarkers, boolean flushClasspathFormatMarkers) {
		try {
			IProject project = getProject();
			if (project.exists()) {
				IMarker[] markers = project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
					String classpathFileFormatAttr =  (String)marker.getAttribute(IJavaModelMarker.CLASSPATH_FILE_FORMAT);
					if ((flushCycleMarkers == (cycleAttr != null && cycleAttr.equals("true"))) //$NON-NLS-1$
						&& (flushClasspathFormatMarkers == (classpathFileFormatAttr != null && classpathFileFormatAttr.equals("true")))){ //$NON-NLS-1$
						marker.delete();
					}
				}
			}
		} catch (CoreException e) {
		}
	}

	/**
	 * @see Openable
	 */
	protected boolean generateInfos(
		OpenableElementInfo info,
		IProgressMonitor pm,
		Map newElements,
		IResource underlyingResource)	throws JavaModelException {

		boolean validInfo = false;
		try {
			if (((IProject) getUnderlyingResource()).isOpen()) {
				// put the info now, because setting the classpath requires it
				JavaModelManager.getJavaModelManager().putInfo(this, info);

				// read classpath property (contains actual classpath and output location settings)
				IPath outputLocation = null;
				IClasspathEntry[] classpath = null;

				// read from file
				try {
					String sharedClasspath = loadClasspath();
					if (sharedClasspath != null) {
						classpath = readPaths(sharedClasspath);
					}
				} catch(JavaModelException e) {
					if (JavaModelManager.VERBOSE && this.getProject().isAccessible()){
							Util.log(e, 
								"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
								+"/.classpath, will revert to default classpath"); //$NON-NLS-1$
					}
				} catch(IOException e){
					if (JavaModelManager.VERBOSE && this.getProject().isAccessible()){
						Util.log(e, 
							"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
							+"/.classpath, will revert to default classpath"); //$NON-NLS-1$
					}
				}

				// extract out the output location
				if (classpath != null && classpath.length > 0) {
					IClasspathEntry entry = classpath[classpath.length - 1];
					if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
						outputLocation = entry.getPath();
						IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
						System.arraycopy(classpath, 0, copy, 0, copy.length);
						classpath = copy;
					}
				}
				// restore output location				
				if (outputLocation == null) {
					outputLocation = defaultOutputLocation();
				}
				((JavaProjectElementInfo)info).setOutputLocation(outputLocation);

				// restore classpath
				if (classpath == null) {
					classpath = defaultClasspath();
				}
				setRawClasspath0(classpath);

				// only valid if reaches here				
				validInfo = true;
			}
		} catch (JavaModelException e) {
		} finally {
			if (!validInfo)
				JavaModelManager.getJavaModelManager().removeInfo(this);
		}
		return validInfo;
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getAllPackageFragmentRoots()
		throws JavaModelException {

		return computePackageFragmentRoots(getResolvedClasspath(true), true);
	}

	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected String getClasspathAsXML(
		IClasspathEntry[] classpath,
		IPath outputLocation)
		throws JavaModelException {

		Document doc = new DocumentImpl();
		Element cpElement = doc.createElement("classpath"); //$NON-NLS-1$
		doc.appendChild(cpElement);

		for (int i = 0; i < classpath.length; ++i) {
			Element cpeElement =
				getEntryAsXMLElement(doc, classpath[i], getProject().getFullPath());
			cpElement.appendChild(cpeElement);
		}

		if (outputLocation != null) {
			outputLocation = outputLocation.removeFirstSegments(1);
			outputLocation = outputLocation.makeRelative();
			Element oElement = doc.createElement("classpathentry"); //$NON-NLS-1$
			oElement.setAttribute("kind", kindToString(ClasspathEntry.K_OUTPUT));	//$NON-NLS-1$
			oElement.setAttribute("path", outputLocation.toOSString()); //$NON-NLS-1$
			cpElement.appendChild(oElement);
		}

		// produce a String output
		try {
			ByteArrayOutputStream s= new ByteArrayOutputStream();
			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			format.setLineSeparator(System.getProperty("line.separator"));  //$NON-NLS-1$
			
			Serializer serializer =
				SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
					new OutputStreamWriter(s, "UTF8"), //$NON-NLS-1$
					format);
			serializer.asDOMSerializer().serialize(doc);
			return s.toString("UTF8"); //$NON-NLS-1$
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}

	/**
	 * Returns the classpath entry that refers to the given path
	 * or <code>null</code> if there is no reference to the path.
	 */
	public IClasspathEntry getClasspathEntryFor(IPath path)
		throws JavaModelException {

		IClasspathEntry[] entries = getExpandedClasspath(true);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getPath().equals(path)) {
				return entries[i];
			}
		}
		return null;
	}

	/**
	 * Returns the qualified name for the classpath server property
	 * of this project
	 */
	public QualifiedName getClasspathPropertyName() {
		return new QualifiedName(JavaCore.PLUGIN_ID, "classpath"); //$NON-NLS-1$
	}

	/**
	 * This is a helper method returning the expanded classpath for the project, as a list of classpath entries, 
	 * where all classpath variable entries have been resolved and substituted with their final target entries.
	 * All project exports have been appended to project entries.
	 */
	public IClasspathEntry[] getExpandedClasspath(boolean ignoreUnresolvedVariable)	throws JavaModelException {
			
			return getExpandedClasspath(ignoreUnresolvedVariable, false);
	}
		
	/**
	 * Internal variant which can create marker on project for invalid entries,
	 * it will also perform classpath expansion in presence of project prerequisites
	 * exporting their entries.
	 */
	public IClasspathEntry[] getExpandedClasspath(
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError) throws JavaModelException {
	
		ObjectVector accumulatedEntries = new ObjectVector();		
		computeExpandedClasspath(this, ignoreUnresolvedVariable, generateMarkerOnError, new HashSet(5), accumulatedEntries);
		
		IClasspathEntry[] expandedPath = new IClasspathEntry[accumulatedEntries.size()];
		accumulatedEntries.copyInto(expandedPath);

		return expandedPath;
	}

	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */
	protected char getHandleMementoDelimiter() {

		return JEM_JAVAPROJECT;
	}

	/**
	 * Find the specific Java command amongst the build spec of a given description
	 */
	private ICommand getJavaCommand(IProjectDescription description)
		throws CoreException {

		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
				return commands[i];
			}
		}
		return null;
	}

	/**
	 * @see IJavaElement
	 */
	public IJavaProject getJavaProject() {

		return this;
	}

	/**
	 * Convenience method that returns the specific type of info for a Java project.
	 */
	protected JavaProjectElementInfo getJavaProjectElementInfo()
		throws JavaModelException {

		return (JavaProjectElementInfo) getElementInfo();
	}

	/**
	 * @see IJavaProject
	 */
	public NameLookup getNameLookup() throws JavaModelException {

		JavaProjectElementInfo info = getJavaProjectElementInfo();
		// lock on the project info to avoid race condition
		synchronized(info){
			NameLookup nameLookup;
			if ((nameLookup = info.getNameLookup()) == null){
				info.setNameLookup(nameLookup = new NameLookup(this));
			}
			return nameLookup;
		}
	}

	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {

		return ((JavaProjectElementInfo) getElementInfo()).getNonJavaResources(this);
	}

	/**
	 * @see IJavaProject
	 */
	public IPath getOutputLocation() throws JavaModelException {

		IPath outputLocation = null;
		if (this.isOpen()) {
			JavaProjectElementInfo info = getJavaProjectElementInfo();
			outputLocation = info.getOutputLocation();
			if (outputLocation != null) {
				return outputLocation;
			}
			return defaultOutputLocation();
		}
		// if not already opened, then read from file (avoid populating the model for CP question)
		if (!this.getProject().exists()){
			throw newNotPresentException();
		}
		IClasspathEntry[] classpath = null;
		try {
			String sharedClasspath = loadClasspath();
			if (sharedClasspath != null) {
				classpath = readPaths(sharedClasspath);
			}
		} catch(JavaModelException e) {
			if (JavaModelManager.VERBOSE && this.getProject().isAccessible()){
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will revert to default output location"); //$NON-NLS-1$
			}
		} catch(IOException e){
			if (JavaModelManager.VERBOSE && this.getProject().isAccessible()){
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will revert to default output location"); //$NON-NLS-1$
			}
		}
		// extract out the output location
		if (classpath != null && classpath.length > 0) {
			IClasspathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getPath();
			}
		}
		if (outputLocation != null) {
			return outputLocation;
		}
		return defaultOutputLocation();
	}

	/**
	 * @return A handle to the package fragment root identified by the given path.
	 * This method is handle-only and the element may or may not exist. Returns
	 * <code>null</code> if unable to generate a handle from the path (for example,
	 * an absolute path that has less than 2 segments. The path may be relative or
	 * absolute.
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IPath path) {
		Object target = JavaModel.getTarget(getProject().getWorkspace().getRoot(), path, false);
		if (target == null) {
			if (path.segmentCount() > 0) {
				String ext = path.getFileExtension();
				if (ext == null) {
					return getPackageFragmentRoot(getProject().getFolder(path));
				} else {
					// resource jar
					return getPackageFragmentRoot(getProject().getFile(path));
				}
			} else {
				// default root
				return getPackageFragmentRoot(getProject());
			}
		} else {
			if (target instanceof IResource) {
				return this.getPackageFragmentRoot((IResource)target);
			} else {
				String ext = path.getFileExtension();
				if (((java.io.File)target).isFile()
					&& ("jar".equalsIgnoreCase(ext)  //$NON-NLS-1$
						|| "zip".equalsIgnoreCase(ext))) { //$NON-NLS-1$
					// external jar
					return getPackageFragmentRoot0(path.toOSString());
				} else {
					// unknown path
					return null;
				}
			}
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {

		String name = resource.getName();
		if (resource.getType() == IResource.FILE
			&& (Util.endsWithIgnoreCase(name, ".jar") //$NON-NLS-1$
				|| Util.endsWithIgnoreCase(name, ".zip"))) { //$NON-NLS-1$ 
			return new JarPackageFragmentRoot(resource, this);
		} else {
			return new PackageFragmentRoot(resource, this);
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(String jarPath) {

		return getPackageFragmentRoot0(this.canonicalizedPath(new Path(jarPath)).toString());
	}
	
	/**
	 * no path canonicalization
	 */
	public IPackageFragmentRoot getPackageFragmentRoot0(String jarPath) {

		return new JarPackageFragmentRoot(jarPath, this);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots()
		throws JavaModelException {

		Object[] children;
		int length;
		IPackageFragmentRoot[] roots;

		System.arraycopy(
			children = getChildren(), 
			0, 
			roots = new IPackageFragmentRoot[length = children.length], 
			0, 
			length);
			
		return roots;
	}

	/**
	 * Returns the package fragment roots identified by the given entry.
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {
		
		try {

			IClasspathEntry[] correspondingEntries = this.getResolvedClasspath(new IClasspathEntry[]{entry}, true, false);
			return computePackageFragmentRoots(correspondingEntries, false);

		} catch (JavaModelException e) {
			return new IPackageFragmentRoot[] {};
		}
	}

	/**
	 * Returns the package fragment root prefixed by the given path, or
	 * an empty collection if there are no such elements in the model.
	 */
	protected IPackageFragmentRoot[] getPackageFragmentRoots(IPath path)

		throws JavaModelException {
		IPackageFragmentRoot[] roots = getAllPackageFragmentRoots();
		ArrayList matches = new ArrayList();

		for (int i = 0; i < roots.length; ++i) {
			if (path.isPrefixOf(roots[i].getPath())) {
				matches.add(roots[i]);
			}
		}
		IPackageFragmentRoot[] copy = new IPackageFragmentRoot[matches.size()];
		matches.toArray(copy);
		return copy;
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment[] getPackageFragments() throws JavaModelException {

		IPackageFragmentRoot[] roots = getPackageFragmentRoots();
		return getPackageFragmentsInRoots(roots);
	}

	/**
	 * Returns all the package fragments found in the specified
	 * package fragment roots.
	 */
	public IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots) {

		ArrayList frags = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			try {
				IJavaElement[] rootFragments = root.getChildren();
				for (int j = 0; j < rootFragments.length; j++) {
					frags.add(rootFragments[j]);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[frags.size()];
		frags.toArray(fragments);
		return fragments;
	}
	
	/*
	 * @see IJavaElement
	 */
	public IPath getPath() {
		return this.getProject().getFullPath();
	}
	
	/**
	 * @see IJavaProject
	 */
	public IProject getProject() {

		return fProject;
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getRawClasspath() throws JavaModelException {

		IClasspathEntry[] classpath = null;
		if (this.isOpen()) {
			JavaProjectElementInfo info = getJavaProjectElementInfo();
			classpath = info.getRawClasspath();
			if (classpath != null) {
				return classpath;
			}
			return defaultClasspath();
		}
		// if not already opened, then read from file (avoid populating the model for CP question)
		if (!this.getProject().exists()){
			throw newNotPresentException();
		}
		try {
			String sharedClasspath = loadClasspath();
			if (sharedClasspath != null) {
				classpath = readPaths(sharedClasspath);
			}
		} catch(JavaModelException e) {
			if (JavaModelManager.VERBOSE && this.getProject().isAccessible()){
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will revert to default classpath"); //$NON-NLS-1$
			}
		} catch(IOException e){
			if (JavaModelManager.VERBOSE && this.getProject().isAccessible()){
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will revert to default classpath"); //$NON-NLS-1$
			}
		}
		// extract out the output location
		if (classpath != null && classpath.length > 0) {
			IClasspathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
				System.arraycopy(classpath, 0, copy, 0, copy.length);
				classpath = copy;
			}
		}
		if (classpath != null) {
			return classpath;
		}
		return defaultClasspath();
	}

	/**
	 * @see IJavaProject#getRequiredProjectNames
	 */
	public String[] getRequiredProjectNames() throws JavaModelException {

		return this.projectPrerequisites(getResolvedClasspath(true));
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedVariable)
		throws JavaModelException {

		return 
			this.getResolvedClasspath(
				ignoreUnresolvedVariable, 
				false); // generateMarkerOnError
	}

	/**
	 * Internal variant which can create marker on project for invalid entries
	 */
	public IClasspathEntry[] getResolvedClasspath(
		boolean ignoreUnresolvedEntry,
		boolean generateMarkerOnError)
		throws JavaModelException {

		JavaProjectElementInfo projectInfo;
		if (this.isOpen()){
			projectInfo = getJavaProjectElementInfo();
		} else {
			// avoid populating the model for only retrieving the resolved classpath (13395)
			projectInfo = null;
		}
		
		// reuse cache if not needing to refresh markers or checking bound variables
		if (ignoreUnresolvedEntry && !generateMarkerOnError && projectInfo != null){
			// resolved path is cached on its info
			IClasspathEntry[] infoPath = projectInfo.lastResolvedClasspath;
			if (infoPath != null) return infoPath;
		}

		IClasspathEntry[] resolvedPath = getResolvedClasspath(getRawClasspath(), ignoreUnresolvedEntry, generateMarkerOnError);

		if (projectInfo != null){
			projectInfo.lastResolvedClasspath = resolvedPath;
		}
		return resolvedPath;
	}
	
	/**
	 * Internal variant which can process any arbitrary classpath
	 */
	public IClasspathEntry[] getResolvedClasspath(
		IClasspathEntry[] classpathEntries,
		boolean ignoreUnresolvedEntry,
		boolean generateMarkerOnError)
		throws JavaModelException {

		if (generateMarkerOnError){
			flushClasspathProblemMarkers(false, false);
		}

		int length = classpathEntries.length;
		int index = 0;
		ArrayList resolvedEntries = new ArrayList();
		
		for (int i = 0; i < length; i++) {

			IClasspathEntry rawEntry = classpathEntries[i];

			/* validation if needed */
			if (generateMarkerOnError) {
				IJavaModelStatus status =
					JavaConventions.validateClasspathEntry(this, rawEntry, false);
				if (!status.isOK())
					createClasspathProblemMarker(
						status.getMessage(), 
						IMarker.SEVERITY_ERROR,
						false,
						false);
			}

			switch (rawEntry.getEntryKind()){
				
				case IClasspathEntry.CPE_VARIABLE :
				
					IClasspathEntry resolvedEntry = JavaCore.getResolvedClasspathEntry(rawEntry);
					if (resolvedEntry == null) {
						if (!ignoreUnresolvedEntry) {
							throw new JavaModelException(
								new JavaModelStatus(
									IJavaModelStatusConstants.CP_VARIABLE_PATH_UNBOUND,
									rawEntry.getPath().toString()));
						}
					} else {
						resolvedEntries.add(resolvedEntry);
					}
					break; 

				case IClasspathEntry.CPE_CONTAINER :
				
					IClasspathContainer container = JavaCore.getClasspathContainer(rawEntry.getPath(), this);
					if (container == null){
						// unbound container
						if (!ignoreUnresolvedEntry) {
							throw new JavaModelException(
								new JavaModelStatus(
									IJavaModelStatusConstants.CP_CONTAINER_PATH_UNBOUND,
									rawEntry.getPath().toString()));
						}
						break;
					}

					IClasspathEntry[] containerEntries = container.getClasspathEntries();
					if (containerEntries == null) break;

					// container was bound
					for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++){
						IClasspathEntry containerRawEntry = containerEntries[j];
						
						if (generateMarkerOnError) {
							IJavaModelStatus status =
								JavaConventions.validateClasspathEntry(this, containerRawEntry, false);
							if (!status.isOK())
								createClasspathProblemMarker(
									status.getMessage(), 
									IMarker.SEVERITY_ERROR,
									false,
									false);
						}
						resolvedEntries.add(containerRawEntry);
					}
					break;
										
				default :

					resolvedEntries.add(rawEntry);
				
			}					
		}

		IClasspathEntry[] resolvedPath = new IClasspathEntry[resolvedEntries.size()];
		resolvedEntries.toArray(resolvedPath);

		return resolvedPath;
	}

	/*
	 * @see IJavaElement
	 */
	public IResource getResource() {
		return this.getProject();
	}

	/**
	 * @see IJavaProject
	 */
	public ISearchableNameEnvironment getSearchableNameEnvironment()
		throws JavaModelException {

		JavaProjectElementInfo info = getJavaProjectElementInfo();
		if (info.getSearchableEnvironment() == null) {
			info.setSearchableEnvironment(new SearchableEnvironment(this));
		}
		return info.getSearchableEnvironment();
	}

	/**
	 * Retrieve a shared property on a project. If the property is not defined, answers null.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 *
	 * @see JavaProject#setSharedProperty(QualifiedName, String)
	 */
	public String getSharedProperty(QualifiedName key) throws CoreException {

		String property = null;
		String propertyFileName = computeSharedPropertyFileName(key);
		IFile rscFile = getProject().getFile(propertyFileName);
		if (rscFile.exists()) {
			property = new String(Util.getResourceContentsAsByteArray(rscFile));
		}
		return property;
	}

	/**
	 * @see JavaElement
	 */
	public SourceMapper getSourceMapper() {

		return null;
	}

	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {

		return getProject();
	}

	/**
	 * @see IJavaProject
	 */
	public boolean hasBuildState() {

		return JavaModelManager.getJavaModelManager().getLastBuiltState(this.getProject(), null) != null;
	}

	/**
	 * @see IJavaProject
	 */
	public boolean hasClasspathCycle(IClasspathEntry[] entries) {
		
		HashSet cycleParticipants = new HashSet();
		updateCycleParticipants(entries, new ArrayList(), cycleParticipants, getWorkspace().getRoot());
		return !cycleParticipants.isEmpty();
	}
	
	public boolean hasCycleMarker(){
	
		try {
			IProject project = getProject();
			if (project.exists()) {
				IMarker[] markers = project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ONE);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
					if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
						return true;
					}
				}
			}
		} catch (CoreException e) {
		}
		return false;
	}

	public int hashCode() {
		return fProject.hashCode();
	}

	/**
	 * Answers true if the project potentially contains any source. A project which has no source is immutable.
	 */
	public boolean hasSource() {

		// look if any source folder on the classpath
		// no need for resolved path given source folder cannot be abstracted
		IClasspathEntry[] entries;
		try {
			entries = this.getRawClasspath();
		} catch (JavaModelException e) {
			return true; // unsure
		}
		for (int i = 0, max = entries.length; i < max; i++) {
			if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Compare current classpath with given one to see if any different.
	 * Note that the argument classpath contains its binary output.
	 */
	public boolean isClasspathEqualsTo(IClasspathEntry[] newClasspath, IPath newOutputLocation, IClasspathEntry[] otherClasspathWithOutput)
		throws JavaModelException {

		if (otherClasspathWithOutput != null && otherClasspathWithOutput.length > 0) {

			int length = otherClasspathWithOutput.length;
			if (length == newClasspath.length + 1) {
				// output is amongst file entries (last one)

				// compare classpath entries
				for (int i = 0; i < length - 1; i++) {
					if (!otherClasspathWithOutput[i].equals(newClasspath[i]))
						return false;
				}
				// compare binary outputs
				if (otherClasspathWithOutput[length - 1].getContentKind()
					== ClasspathEntry.K_OUTPUT
					&& otherClasspathWithOutput[length - 1].getPath().equals(newOutputLocation))
					return true;
			}
		}
		return false;
	}
	
	/*
	 * @see IJavaProject
	 */
	public boolean isOnClasspath(IJavaElement element) throws JavaModelException {
		IPath rootPath;
		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			rootPath = ((IJavaProject)element).getProject().getFullPath();
		} else {
			IPackageFragmentRoot root = (IPackageFragmentRoot)element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			if (root == null) {
				return false;
			}
			rootPath = root.getPath();
		}
		return this.findPackageFragmentRoot0(rootPath) != null;
	}

	/**
	 * load the classpath from a shareable format (VCM-wise)
	 */
	public String loadClasspath() throws JavaModelException {

		try {
			return getSharedProperty(getClasspathPropertyName());
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * @see IJavaProject#newEvaluationContext
	 */
	public IEvaluationContext newEvaluationContext() {

		return new EvaluationContextWrapper(new EvaluationContext(), this);
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (region == null) {
			throw new IllegalArgumentException(Util.bind("hierarchy.nullRegion"));//$NON-NLS-1$
		}
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(null, region, this, true);
		runOperation(op, monitor);
		return op.getResult();
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (type == null) {
			throw new IllegalArgumentException(Util.bind("hierarchy.nullFocusType"));//$NON-NLS-1$
		}
		if (region == null) {
			throw new IllegalArgumentException(Util.bind("hierarchy.nullRegion"));//$NON-NLS-1$
		}
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(type, region, this, true);
		runOperation(op, monitor);
		return op.getResult();
	}

	/**
	 * Open project if resource isn't closed
	 */
	protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {

		if (!this.fProject.isOpen()) {
			throw newNotPresentException();
		} else {
			super.openWhenClosed(pm);
		}
	}

	public String[] projectPrerequisites(IClasspathEntry[] entries)
		throws JavaModelException {
			
		ArrayList prerequisites = new ArrayList();
		// need resolution
		entries = getResolvedClasspath(entries, true, false);
		for (int i = 0, length = entries.length; i < length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				prerequisites.add(entry.getPath().lastSegment());
			}
		}
		int size = prerequisites.size();
		if (size == 0) {
			return NO_PREREQUISITES;
		} else {
			String[] result = new String[size];
			prerequisites.toArray(result);
			return result;
		}
	}

	/**
	 * Returns a collection of <code>IClasspathEntry</code>s from the given
	 * classpath string in XML format.
	 *
	 * @exception IOException if the stream cannot be read 
	 */
	protected IClasspathEntry[] readPaths(String xmlClasspath) throws IOException {

		IPath projectPath = getProject().getFullPath();
		StringReader reader = new StringReader(xmlClasspath);
		Element cpElement;

		try {
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			reader.close();
			throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
		} finally {
			reader.close();
		}

		if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) { //$NON-NLS-1$
			throw new IOException(Util.bind("file.badFormat")); //$NON-NLS-1$
		}
		NodeList list = cpElement.getChildNodes();
		ArrayList paths = new ArrayList();
		int length = list.getLength();

		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element cpeElement = (Element) node;

				if (cpeElement.getNodeName().equalsIgnoreCase("classpathentry")) { //$NON-NLS-1$
					String cpeElementKind = cpeElement.getAttribute("kind"); //$NON-NLS-1$
					String pathStr = cpeElement.getAttribute("path"); //$NON-NLS-1$
					// ensure path is absolute
					IPath path = new Path(pathStr);
					int kind = kindFromString(cpeElementKind);
					if (kind != IClasspathEntry.CPE_VARIABLE && kind != IClasspathEntry.CPE_CONTAINER && !path.isAbsolute()) {
						path = projectPath.append(path);
					}
					// source attachment info (optional)
					String sourceAttachmentPathStr = cpeElement.getAttribute("sourcepath");	//$NON-NLS-1$
					IPath sourceAttachmentPath =
						sourceAttachmentPathStr.equals("") ? null : new Path(sourceAttachmentPathStr); //$NON-NLS-1$
					String sourceAttachmentRootPathStr = cpeElement.getAttribute("rootpath"); //$NON-NLS-1$
					IPath sourceAttachmentRootPath =
						sourceAttachmentRootPathStr.equals("") //$NON-NLS-1$
							? null
							: new Path(sourceAttachmentRootPathStr);
					
					// exported flag
					boolean isExported = cpeElement.getAttribute("exported").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$

					// recreate the CP entry
					switch (kind) {
			
						case IClasspathEntry.CPE_PROJECT :
							if (!path.isAbsolute()) return null;
							paths.add(JavaCore.newProjectEntry(path, isExported));
							break;
							
						case IClasspathEntry.CPE_LIBRARY :
							if (!path.isAbsolute()) return null;
							paths.add(JavaCore.newLibraryEntry(
															path,
															sourceAttachmentPath,
															sourceAttachmentRootPath,
															isExported));
							break;
							
						case IClasspathEntry.CPE_SOURCE :
							if (!path.isAbsolute()) return null;
							// must be an entry in this project or specify another project
							String projSegment = path.segment(0);
							if (projSegment != null && projSegment.equals(getElementName())) {
								// this project
								paths.add(JavaCore.newSourceEntry(path));
							} else {
								// another project
								paths.add(JavaCore.newProjectEntry(path, isExported));
							}
							break;
			
						case IClasspathEntry.CPE_VARIABLE :
							paths.add(JavaCore.newVariableEntry(
									path,
									sourceAttachmentPath,
									sourceAttachmentRootPath, 
									isExported));
							break;
							
						case IClasspathEntry.CPE_CONTAINER :
							paths.add(JavaCore.newContainerEntry(
									path,
									isExported));
							break;

						case ClasspathEntry.K_OUTPUT :
							if (!path.isAbsolute()) return null;
							paths.add(new ClasspathEntry(
									ClasspathEntry.K_OUTPUT,
									IClasspathEntry.CPE_LIBRARY,
									path,
									null,
									null,
									false));
							break;
					}
				}
			}
		}
		if (paths.size() > 0) {
			IClasspathEntry[] ips = new IClasspathEntry[paths.size()];
			paths.toArray(ips);
			return ips;
		} else {
			return null;
		}
	}

	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	protected void removeFromBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				getProject().setDescription(description, null);
				return;
			}
		}
	}

	/**
	 * @see JavaElement#rootedAt(IJavaProject)
	 */
	public IJavaElement rootedAt(IJavaProject project) {
		return project;
	
	}
	
	/**
	 * Answers an ID which is used to distinguish project/entries during package
	 * fragment root computations
	 */
	public String rootID(){
		return "[PRJ]"+this.getProject().getFullPath(); //$NON-NLS-1$
	}
	
	/**
	 * Saves the classpath in a shareable format (VCM-wise) if necessary.
	 *  (i.e.&nbsp;semantically different).
	 * Will never write an identical one.
	 * 
	 * @return Return whether the .classpath file was modified.
	 */
	public boolean saveClasspath(IClasspathEntry[] newClasspath, IPath newOutputLocation) throws JavaModelException {

		if (!getProject().exists()) return false;

		QualifiedName classpathProp = getClasspathPropertyName();

		try {
			// attempt to prove the classpath has not change
			String fileClasspathString = getSharedProperty(classpathProp);
			if (fileClasspathString != null) {
				IClasspathEntry[] fileEntries = readPaths(fileClasspathString);
				if (isClasspathEqualsTo(newClasspath, newOutputLocation, fileEntries)) {
					// no need to save it, it is the same
					return false;
				}
			}
		} catch (IOException e) {
		} catch (RuntimeException e) {
		} catch (CoreException e) {
		}

		// actual file saving
		try {
			setSharedProperty(
				classpathProp,
				getClasspathAsXML(newClasspath, newOutputLocation));
			return true;
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * Update the Java command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	private void setJavaCommand(
		IProjectDescription description,
		ICommand newCommand)
		throws CoreException {

		ICommand[] oldCommands = description.getBuildSpec();
		ICommand oldJavaCommand = getJavaCommand(description);
		ICommand[] newCommands;

		if (oldJavaCommand == null) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 1, oldCommands.length);
			newCommands[0] = newCommand;
		} else {
			for (int i = 0, max = oldCommands.length; i < max; i++) {
				if (oldCommands[i] == oldJavaCommand) {
					oldCommands[i] = newCommand;
					break;
				}
			}
			newCommands = oldCommands;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		getProject().setDescription(description, null);
	}

	/**
	 * @see IJavaProject
	 */
	public void setOutputLocation(IPath outputLocation, IProgressMonitor monitor)
		throws JavaModelException {

		if (outputLocation == null) {
			throw new IllegalArgumentException(Util.bind("path.nullpath")); //$NON-NLS-1$
		}
		if (outputLocation.equals(getOutputLocation())) {
			return;
		}
		this.setRawClasspath(SetClasspathOperation.ReuseClasspath, outputLocation, monitor);
	}

	/**
	 * Sets the underlying kernel project of this Java project,
	 * and fills in its parent and name.
	 * Called by IProject.getNature().
	 *
	 * @see IProjectNature#setProject
	 */
	public void setProject(IProject project) {

		fProject = project;
		fParent = JavaModelManager.getJavaModelManager().getJavaModel();
		fName = project.getName();
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IPath outputLocation,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries, 
			outputLocation, 
			monitor, 
			true, // canChangeResource
			true, // forceSave
			getResolvedClasspath(true), // ignoreUnresolvedVariable
			true, // needCycleCheck
			true); // needValidation
	}

	public void setRawClasspath(
		IClasspathEntry[] newEntries,
		IPath newOutputLocation,
		IProgressMonitor monitor,
		boolean canChangeResource,
		boolean forceSave,
		IClasspathEntry[] oldResolvedPath,
		boolean needCycleCheck,
		boolean needValidation)
		throws JavaModelException {

		JavaModelManager manager =
			(JavaModelManager) JavaModelManager.getJavaModelManager();
		try {
			IClasspathEntry[] newRawPath = newEntries;
			if (newRawPath == null) { //are we already with the default classpath
				newRawPath = defaultClasspath();
			}
			SetClasspathOperation op =
				new SetClasspathOperation(
					this, 
					oldResolvedPath, 
					newRawPath, 
					newOutputLocation,
					canChangeResource, 
					forceSave,
					needCycleCheck,
					needValidation);
			runOperation(op, monitor);
			
		} catch (JavaModelException e) {
			manager.flush();
			throw e;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries, 
			SetClasspathOperation.ReuseOutputLocation, 
			monitor, 
			true, // canChangeResource
			true, // forceSave
			getResolvedClasspath(true), // ignoreUnresolvedVariable
			true, // needCycleCheck
			true); // needValidation
	}

	/**
	 * NOTE: <code>null</code> specifies default classpath, and an empty
	 * array specifies an empty classpath.
	 *
	 * @exception NotPresentException if this project does not exist.
	 */
	protected void setRawClasspath0(IClasspathEntry[] rawEntries)
		throws JavaModelException {

		// if not open, will cause opening with default path
		JavaProjectElementInfo info = getJavaProjectElementInfo();
	
		synchronized (info) {
			if (rawEntries == null) {
				rawEntries = defaultClasspath();
			}
			// clear cache of resolved classpath
			info.lastResolvedClasspath = null;
			
			info.setRawClasspath(rawEntries);
				
			// compute the new roots
			updatePackageFragmentRoots();				
		}
	}

	/**
	 * Record a shared persistent property onto a project.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 * 
	 * shared properties end up in resource files, and thus cannot be modified during
	 * delta notifications (a CoreException would then be thrown).
	 * 
	 * @see JavaProject#getSharedProperty(QualifiedName key)
	 */
	public void setSharedProperty(QualifiedName key, String value)
		throws CoreException {

		String propertyName = computeSharedPropertyFileName(key);
		IFile rscFile = getProject().getFile(propertyName);
		InputStream inputStream = new ByteArrayInputStream(value.getBytes());
		// update the resource content
		if (rscFile.exists()) {
			rscFile.setContents(inputStream, IResource.FORCE, null);
		} else {
			rscFile.create(inputStream, IResource.FORCE, null);
		}
	}

	/**
	 * Update cycle markers for all java projects
	 */
	public static void updateAllCycleMarkers() throws JavaModelException {
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IJavaProject[] projects = manager.getJavaModel().getJavaProjects();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		HashSet cycleParticipants = new HashSet();
		ArrayList visited = new ArrayList();
		int length = projects.length;
		for (int i = 0; i < length; i++){
			JavaProject project = (JavaProject)projects[i];
			if (!cycleParticipants.contains(project)){
				visited.clear();
				project.updateCycleParticipants(null, visited, cycleParticipants, workspaceRoot);
			}
		}

		for (int i = 0; i < length; i++){
			JavaProject project = (JavaProject)projects[i];
			
			if (cycleParticipants.contains(project)){
				if (!project.hasCycleMarker()){
					project.createClasspathProblemMarker(
						Util.bind("classpath.cycle"), //$NON-NLS-1$
						IMarker.SEVERITY_ERROR,
						true,
						false); 
				}
			} else {
				project.flushClasspathProblemMarkers(true, false);
			}			
		}
	}
	
	/**
	 * If a cycle is detected, then cycleParticipants contains all the project involved in this cycle (directly),
	 * no cycle if the set is empty (and started empty)
	 */
	public void updateCycleParticipants(IClasspathEntry[] preferredClasspath, ArrayList visited, HashSet cycleParticipants, IWorkspaceRoot workspaceRoot){

		int index = visited.indexOf(this);
		if (index >= 0){
			// only consider direct participants inside the cycle
			for (int i = index, size = visited.size(); i < size; i++){
				cycleParticipants.add(visited.get(i)); 
			}
			return;
		} else {
			visited.add(this);
		}
		
		try {
			IClasspathEntry[] classpath;
			if (preferredClasspath == null) {
				classpath = getResolvedClasspath(true);
			} else {
				classpath = preferredClasspath;
			}
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry = classpath[i];
				
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT){
					String projectName = entry.getPath().lastSegment();
					JavaProject project = (JavaProject)JavaCore.create(workspaceRoot.getProject(projectName));
					project.updateCycleParticipants(null, visited, cycleParticipants, workspaceRoot);
				}
			}
		} catch(JavaModelException e){
		}
		visited.remove(this);
	}
	
	/**
	 * Reset the collection of package fragment roots (local ones) - only if opened.
	 * Need to check *all* package fragment roots in order to reset NameLookup
	 */
	public void updatePackageFragmentRoots(){
		
			if (this.isOpen()) {
				try {
					JavaProjectElementInfo info = getJavaProjectElementInfo();

					IClasspathEntry[] classpath = getResolvedClasspath(true);
					NameLookup lookup = info.getNameLookup();
					if (lookup != null){
						IPackageFragmentRoot[] oldRoots = lookup.fPackageFragmentRoots;
						IPackageFragmentRoot[] newRoots = computePackageFragmentRoots(classpath, true);
						checkIdentical: { // compare all pkg fragment root lists
							if (oldRoots.length == newRoots.length){
								for (int i = 0, length = oldRoots.length; i < length; i++){
									if (!oldRoots[i].equals(newRoots[i])){
										break checkIdentical;
									}
								}
								return; // no need to update
							}	
						}
						info.setNameLookup(null); // discard name lookup (hold onto roots)
					}				
					info.setNonJavaResources(null);
					info.setChildren(
						computePackageFragmentRoots(classpath, false));		

				} catch(JavaModelException e){
					try {
						close(); // could not do better
					} catch(JavaModelException ex){
					}
				}
			}
	}
}