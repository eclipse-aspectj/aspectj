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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Implementation of <code>IJavaModel<code>. The Java Model maintains a cache of
 * active <code>IJavaProject</code>s in a workspace. A Java Model is specific to a
 * workspace. To retrieve a workspace's model, use the
 * <code>#getJavaModel(IWorkspace)</code> method.
 *
 * @see IJavaModel
 */
public class JavaModel extends Openable implements IJavaModel {

	/**
	 * A set of java.io.Files used as a cache of external jars that 
	 * are known to be existing.
	 * Note this cache is kept for the whole session.
	 */ 
	public static HashSet existingExternalFiles = new HashSet();
		
/**
 * Constructs a new Java Model on the given workspace.
 * Note that only one instance of JavaModel handle should ever be created.
 * One should only indirect through JavaModelManager#getJavaModel() to get
 * access to it.
 * 
 * @exception Error if called more than once
 */
protected JavaModel() throws Error {
	super(JAVA_MODEL, null, "" /*workspace has empty name*/); //$NON-NLS-1$
}



/**
 * @see IJavaModel
 */
public void copy(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new CopyResourceElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	} else {
		runOperation(new CopyElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	}
}
/**
 * Returns a new element info for this element.
 */
protected OpenableElementInfo createElementInfo() {
	return new JavaModelInfo();
}

/**
 * @see IJavaModel
 */
public void delete(IJavaElement[] elements, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new DeleteResourceElementsOperation(elements, force), monitor);
	} else {
		runOperation(new DeleteElementsOperation(elements, force), monitor);
	}
}
/**
 * Finds the given project in the list of the java model's children.
 * Returns null if not found.
 */
public IJavaProject findJavaProject(IProject project) {
	try {
		IJavaProject[] projects = this.getOldJavaProjectsList();
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject javaProject = projects[i];
			if (project.equals(javaProject.getProject())) {
				return javaProject;
			}
		}
	} catch (JavaModelException e) {
	}
	return null;
}

/**
 * Flushes the cache of external files known to be existing.
 */
public static void flushExternalFileCache() {
	existingExternalFiles = new HashSet();
}

/**
 */
protected boolean generateInfos(
	OpenableElementInfo info,
	IProgressMonitor pm,
	Map newElements,
	IResource underlyingResource)	throws JavaModelException {

	JavaModelManager.getJavaModelManager().putInfo(this, info);
	// determine my children
	try {
		IProject[] projects = this.getWorkspace().getRoot().getProjects();
		for (int i = 0, max = projects.length; i < max; i++) {
			IProject project = projects[i];
			if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
				info.addChild(getJavaProject(project));
			}
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	return true;
}
/**
 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
 * memento.
 * @see getHandleMemento()
 */
protected IJavaElement getHandleFromMementoForBinaryMembers(String memento, IPackageFragmentRoot root, int rootEnd, int end) throws JavaModelException {

	//deal with class file and binary members
	IPackageFragment frag = null;
	if (rootEnd == end - 1) {
		//default package
		frag= root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
	} else {
		frag= root.getPackageFragment(memento.substring(rootEnd + 1, end));
	}
	int oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_TYPE, oldEnd);
	if (end == -1) {
		//we ended with a class file 
		return frag.getClassFile(memento.substring(oldEnd + 1));
	}
	IClassFile cf = frag.getClassFile(memento.substring(oldEnd + 1, end));
	oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_TYPE, oldEnd);
	oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_FIELD, end);
	if (end != -1) {
		//binary field
		IType type = cf.getType();
		return type.getField(memento.substring(end + 1));
	}
	end = memento.indexOf(JavaElement.JEM_METHOD, oldEnd);
	if (end != -1) {
		//binary method
		oldEnd = end;
		IType type = cf.getType();
		String methodName;
		end = memento.lastIndexOf(JavaElement.JEM_METHOD);
		String[] parameterTypes = null;
		if (end == oldEnd) {
			methodName = memento.substring(end + 1);
			//no parameter types
			parameterTypes = new String[] {};
		} else {
			String parameters = memento.substring(oldEnd + 1);
			StringTokenizer tokenizer = new StringTokenizer(parameters, new String(new char[] {JavaElement.JEM_METHOD}));
			parameterTypes = new String[tokenizer.countTokens() - 1];
			methodName= tokenizer.nextToken();
			int i = 0;
			while (tokenizer.hasMoreTokens()) {
				parameterTypes[i] = tokenizer.nextToken();
				i++;
			}
		}
		return type.getMethod(methodName, parameterTypes);
	}

	//binary type
	return cf.getType();
}
/**
 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
 * memento.
 * @see getHandleMemento()
 */
protected IJavaElement getHandleFromMementoForSourceMembers(String memento, IPackageFragmentRoot root, int rootEnd, int end) throws JavaModelException {

	//deal with compilation units and source members
	IPackageFragment frag = null;
	if (rootEnd == end - 1) {
		//default package
		frag= root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
	} else {
		frag= root.getPackageFragment(memento.substring(rootEnd + 1, end));
	}
	int oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_PACKAGEDECLARATION, end);
	if (end != -1) {
		//package declaration
		ICompilationUnit cu = frag.getCompilationUnit(memento.substring(oldEnd + 1, end));
		return cu.getPackageDeclaration(memento.substring(end + 1));
	}
	end = memento.indexOf(JavaElement.JEM_IMPORTDECLARATION, oldEnd);
	if (end != -1) {
		//import declaration
		ICompilationUnit cu = frag.getCompilationUnit(memento.substring(oldEnd + 1, end));
		return cu.getImport(memento.substring(end + 1));
	}
	int typeStart = memento.indexOf(JavaElement.JEM_TYPE, oldEnd);
	if (typeStart == -1) {
		//we ended with a compilation unit
		return frag.getCompilationUnit(memento.substring(oldEnd + 1));
	}

	//source members
	ICompilationUnit cu = frag.getCompilationUnit(memento.substring(oldEnd + 1, typeStart));
	end = memento.indexOf(JavaElement.JEM_FIELD, oldEnd);
	if (end != -1) {
		//source field
		IType type = getHandleFromMementoForSourceType(memento, cu, typeStart, end);
		return type.getField(memento.substring(end + 1));
	}
	end = memento.indexOf(JavaElement.JEM_METHOD, oldEnd);
	if (end != -1) {
		//source method
		IType type = getHandleFromMementoForSourceType(memento, cu, typeStart, end);
		oldEnd = end;
		String methodName;
		end = memento.lastIndexOf(JavaElement.JEM_METHOD);
		String[] parameterTypes = null;
		if (end == oldEnd) {
			methodName = memento.substring(end + 1);
			//no parameter types
			parameterTypes = new String[] {};
		} else {
			String parameters = memento.substring(oldEnd + 1);
			StringTokenizer mTokenizer = new StringTokenizer(parameters, new String(new char[] {JavaElement.JEM_METHOD}));
			parameterTypes = new String[mTokenizer.countTokens() - 1];
			methodName = mTokenizer.nextToken();
			int i = 0;
			while (mTokenizer.hasMoreTokens()) {
				parameterTypes[i] = mTokenizer.nextToken();
				i++;
			}
		}
		return type.getMethod(methodName, parameterTypes);
	}
	
	end = memento.indexOf(JavaElement.JEM_INITIALIZER, oldEnd);
	if (end != -1 ) {
		//initializer
		IType type = getHandleFromMementoForSourceType(memento, cu, typeStart, end);
		return type.getInitializer(Integer.parseInt(memento.substring(end + 1)));
	}
	//source type
	return getHandleFromMementoForSourceType(memento, cu, typeStart, memento.length());
}
/**
 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
 * memento.
 * @see getHandleMemento()
 */
protected IType getHandleFromMementoForSourceType(String memento, ICompilationUnit cu, int typeStart, int typeEnd) throws JavaModelException {
	int end = memento.lastIndexOf(JavaElement.JEM_TYPE);
	IType type = null;
	if (end == typeStart) {
		String typeName = memento.substring(typeStart + 1, typeEnd);
		type = cu.getType(typeName);
		
	} else {
		String typeNames = memento.substring(typeStart + 1, typeEnd);
		StringTokenizer tokenizer = new StringTokenizer(typeNames, new String(new char[] {JavaElement.JEM_TYPE}));
		type = cu.getType(tokenizer.nextToken());
		while (tokenizer.hasMoreTokens()) {
			//deal with inner types
			type= type.getType(tokenizer.nextToken());
		}
	}
	return type;
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	return getElementName();
}
/**
 * Returns the <code>char</code> that marks the start of this handles
 * contribution to a memento.
 */
protected char getHandleMementoDelimiter(){
	Assert.isTrue(false, Util.bind("assert.shouldNotImplement")); //$NON-NLS-1$
	return 0;
}
/**
 * @see IJavaElement
 */
public IJavaModel getJavaModel() {
	return this;
}
/**
 * @see IJavaElement
 */
public IJavaProject getJavaProject() {
	return null;
}
/**
 * @see IJavaModel
 */
public IJavaProject getJavaProject(String name) {
	return new JavaProject(this.getWorkspace().getRoot().getProject(name), this);
}
/**
 * Returns the active Java project associated with the specified
 * resource, or <code>null</code> if no Java project yet exists
 * for the resource.
 *
 * @exception IllegalArgumentException if the given resource
 * is not one of an IProject, IFolder, or IFile.
 */
public IJavaProject getJavaProject(IResource resource) {
	if (resource.getType() == IResource.FOLDER) {
		return new JavaProject(((IFolder)resource).getProject(), this);
	} else if (resource.getType() == IResource.FILE) {
		return new JavaProject(((IFile)resource).getProject(), this);
	} else if (resource.getType() == IResource.PROJECT) {
		return new JavaProject((IProject)resource, this);
	} else {
		throw new IllegalArgumentException(Util.bind("element.invalidResourceForProject")); //$NON-NLS-1$
	}
}
/**
 * @see IJavaModel
 */
public IJavaProject[] getJavaProjects() throws JavaModelException {
	ArrayList list = getChildrenOfType(JAVA_PROJECT);
	IJavaProject[] array= new IJavaProject[list.size()];
	list.toArray(array);
	return array;

}
/**
 * Workaround for bug 15168 circular errors not reported 
 * Returns the list of java projects before resource delta processing
 * has started.
 */
public IJavaProject[] getOldJavaProjectsList() throws JavaModelException {
	JavaModelManager manager = this.getJavaModelManager();
	return 
		manager.javaProjectsCache == null ? 
			this.getJavaProjects() : 
			manager.javaProjectsCache; 
}
/*
 * @see IJavaElement
 */
public IPath getPath() {
	return Path.ROOT;
}
/*
 * @see IJavaElement
 */
public IResource getResource() {
	return ResourcesPlugin.getWorkspace().getRoot();
}
/**
 * @see IOpenable
 */
public IResource getUnderlyingResource() throws JavaModelException {
	return null;
}
/**
 * Returns the workbench associated with this object.
 */
public IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}

/**
 * @see IJavaModel
 */
public void move(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new MoveResourceElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	} else {
		runOperation(new MoveElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	}
}

/**
 * @see IJavaModel#refreshExternalArchives(IJavaElement[], IProgressMonitor)
 */
public void refreshExternalArchives(IJavaElement[] elementsScope, IProgressMonitor monitor) throws JavaModelException {
	if (elementsScope == null){
		elementsScope = new IJavaElement[] { this };
	}
	getJavaModelManager().deltaProcessor.checkExternalArchiveChanges(elementsScope, monitor);
}

/**
 * @see IJavaModel
 */
public void rename(IJavaElement[] elements, IJavaElement[] destinations, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	MultiOperation op;
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		op = new RenameResourceElementsOperation(elements, destinations, renamings, force);
	} else {
		op = new RenameElementsOperation(elements, destinations, renamings, force);
	}
	
	runOperation(op, monitor);
}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	return this;

}
/**
 * Configures and runs the <code>MultiOperation</code>.
 */
protected void runOperation(MultiOperation op, IJavaElement[] elements, IJavaElement[] siblings, String[] renamings, IProgressMonitor monitor) throws JavaModelException {
	op.setRenamings(renamings);
	if (siblings != null) {
		for (int i = 0; i < elements.length; i++) {
			op.setInsertBefore(elements[i], siblings[i]);
		}
	}
	runOperation(op, monitor);
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(this.tabString(tab));
	buffer.append("Java Model"); //$NON-NLS-1$
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}

/**
 * Helper method - returns the targeted item (IResource if internal or java.io.File if external), 
 * or null if unbound
 * Internal items must be referred to using container relative paths.
 */
public static Object getTarget(IContainer container, IPath path, boolean checkResourceExistence) {

	if (path == null) return null;
	
	// lookup - inside the container
	IResource resource = container.findMember(path);
	if (resource != null){
		if (!checkResourceExistence ||resource.exists()) return resource;
		return null;
	}

	// lookup - outside the container
	File externalFile = new File(path.toOSString());
	if (!checkResourceExistence) {
		return externalFile;
	} else if (existingExternalFiles.contains(externalFile)) {
		return externalFile;
	} else { 
		if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
			System.out.println("(" + Thread.currentThread() + ") [JavaModel.getTarget(...)] Checking existence of " + path.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (externalFile.exists()) {
			// cache external file
			existingExternalFiles.add(externalFile);
			return externalFile;
		}
	}
	return null;	
}
}
