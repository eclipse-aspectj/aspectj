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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.jdom.DOMException;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMCompilationUnit;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.core.jdom.IDOMPackage;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * This operation copies/moves/renames a collection of resources from their current
 * container to a new container, optionally renaming the
 * elements.
 * <p>Notes:<ul>
 *    <li>If there is already an resource with the same name in
 *    the new container, the operation either overwrites or aborts,
 *    depending on the collision policy setting. The default setting is
 *	  abort.
 *
 *    <li>When a compilation unit is copied to a new package, the
 *    package declaration in the compilation unit is automatically updated.
 *
 *    <li>The collection of elements being copied must all share the
 *    same type of container.
 *
 *    <li>This operation can be used to copy and rename elements within
 *    the same container. 
 *
 *    <li>This operation only copies compilation units and package fragments.
 *    It does not copy package fragment roots - a platform operation must be used for that.
 * </ul>
 *
 */
public class CopyResourceElementsOperation extends MultiOperation {
	/**
	 * A collection of renamed compilation units.  These cus do
	 * not need to be saved as they no longer exist.
	 */
	protected ArrayList fRenamedCompilationUnits = null;
	/**
	 * Table specifying deltas for elements being 
	 * copied/moved/renamed. Keyed by elements' project(s), and
	 * values are the corresponding deltas.
	 */
	protected Map fDeltasPerProject= new HashMap(1);
	/**
	 * The <code>DOMFactory</code> used to manipulate the source code of
	 * <code>ICompilationUnit</code>.
	 */
	protected DOMFactory fFactory;
	/**
	 * The list of new resources created during this operation.
	 */
	protected ArrayList fCreatedElements;
/**
 * When executed, this operation will copy the given resources to the
 * given containers.  The resources and destination containers must be in
 * the correct order. If there is > 1 destination, the number of destinations
 * must be the same as the number of resources being copied/moved.
 */
public CopyResourceElementsOperation(IJavaElement[] resourcesToCopy, IJavaElement[] destContainers, boolean force) {
	super(resourcesToCopy, destContainers, force);
	fFactory = new DOMFactory();
}
/**
 * When executed, this operation will copy the given resources to the
 * given container.
 */
public CopyResourceElementsOperation(IJavaElement[] resourcesToCopy, IJavaElement destContainer, boolean force) {
	this(resourcesToCopy, new IJavaElement[]{destContainer}, force);
}
/**
 * Returns the children of <code>source</code> which are affected by this operation.
 * If <code>source</code> is a <code>K_SOURCE</code>, these are the <code>.java</code>
 * files, if it is a <code>K_BINARY</code>, they are the <code>.class</code> files.
 */
private IResource[] collectResourcesOfInterest(IPackageFragment source) throws JavaModelException {
	IJavaElement[] children = source.getChildren();
	int childOfInterest = IJavaElement.COMPILATION_UNIT;
	if (source.getKind() == IPackageFragmentRoot.K_BINARY) {
		childOfInterest = IJavaElement.CLASS_FILE;
	}
	ArrayList correctKindChildren = new ArrayList(children.length);
	for (int i = 0; i < children.length; i++) {
		IJavaElement child = children[i];
		if (child.getElementType() == childOfInterest) {
			correctKindChildren.add(child.getUnderlyingResource());
		}
	}
	// Gather non-java resources
	Object[] nonJavaResources = source.getNonJavaResources();
	int actualNonJavaResourceCount = 0;
	for (int i = 0, max = nonJavaResources.length; i < max; i++){
		if (nonJavaResources[i] instanceof IResource) actualNonJavaResourceCount++;
	}
	IResource[] actualNonJavaResources = new IResource[actualNonJavaResourceCount];
	for (int i = 0, max = nonJavaResources.length, index = 0; i < max; i++){
		if (nonJavaResources[i] instanceof IResource) actualNonJavaResources[index++] = (IResource)nonJavaResources[i];
	}
	
	if (actualNonJavaResourceCount != 0) {
		int correctKindChildrenSize = correctKindChildren.size();
		IResource[] result = new IResource[correctKindChildrenSize + actualNonJavaResourceCount];
		correctKindChildren.toArray(result);
		System.arraycopy(actualNonJavaResources, 0, result, correctKindChildrenSize, actualNonJavaResourceCount);
		return result;
	} else {
		IResource[] result = new IResource[correctKindChildren.size()];
		correctKindChildren.toArray(result);
		return result;
	}
}
/**
 * Creates any destination package fragment(s) which do not exists yet.
 */
private void createNeededPackageFragments(IPackageFragmentRoot root, String newFragName) throws JavaModelException {
	IContainer parentFolder = (IContainer) root.getUnderlyingResource();
	JavaElementDelta projectDelta = getDeltaFor(root.getJavaProject());
	String[] names = Signature.getSimpleNames(newFragName);
	StringBuffer sideEffectPackageName = new StringBuffer();
	for (int i = 0; i < names.length; i++) {
		String subFolderName = names[i];
		sideEffectPackageName.append(subFolderName);
		IResource subFolder = parentFolder.findMember(subFolderName);
		if (subFolder == null) {
			createFolder(parentFolder, subFolderName, fForce);
			parentFolder = parentFolder.getFolder(new Path(subFolderName));
			IPackageFragment sideEffectPackage = root.getPackageFragment(sideEffectPackageName.toString());
			if (i < names.length - 1) { // all but the last one are side effect packages
				projectDelta.added(sideEffectPackage);
			}
			fCreatedElements.add(sideEffectPackage);
		} else {
			parentFolder = (IContainer) subFolder;
		}
		sideEffectPackageName.append('.');
	}
}
/**
 * Returns the <code>JavaElementDelta</code> for <code>javaProject</code>,
 * creating it and putting it in <code>fDeltasPerProject</code> if
 * it does not exist yet.
 */
private JavaElementDelta getDeltaFor(IJavaProject javaProject) {
	JavaElementDelta delta = (JavaElementDelta) fDeltasPerProject.get(javaProject);
	if (delta == null) {
		delta = new JavaElementDelta(javaProject);
		fDeltasPerProject.put(javaProject, delta);
	}
	return delta;
}
/**
 * @see MultiOperation
 */
protected String getMainTaskName() {
	return org.eclipse.jdt.internal.core.Util.bind("operation.copyResourceProgress"); //$NON-NLS-1$
}
/**
 * Sets the deltas to register the changes resulting from this operation
 * for this source element and its destination.
 * If the operation is a cross project operation<ul>
 * <li>On a copy, the delta should be rooted in the dest project
 * <li>On a move, two deltas are generated<ul>
 * 			<li>one rooted in the source project
 *			<li>one rooted in the destination project</ul></ul>
 * If the operation is rooted in a single project, the delta is rooted in that project
 * 	 
 */
protected void prepareDeltas(IJavaElement sourceElement, IJavaElement destinationElement) {
	IJavaProject destProject = destinationElement.getJavaProject();
	if (isMove()) {
		IJavaProject sourceProject = sourceElement.getJavaProject();
		getDeltaFor(sourceProject).movedFrom(sourceElement, destinationElement);
		getDeltaFor(destProject).movedTo(destinationElement, sourceElement);
	} else {
		getDeltaFor(destProject).added(destinationElement);
	}
}
/**
 * Copies/moves a compilation unit with the name <code>newCUName</code>
 * to the destination package.<br>
 * The package statement in the compilation unit is updated if necessary.
 * The main type of the compilation unit is renamed if necessary.
 *
 * @exception JavaModelException if the operation is unable to
 * complete
 */
private void processCompilationUnitResource(ICompilationUnit source, IPackageFragment dest) throws JavaModelException {
	String newCUName = getNewNameFor(source);
	String destName = (newCUName != null) ? newCUName : source.getElementName();
	String newContent = updatedContent(source, dest, newCUName); // null if unchanged

	// copy resource
	IFile sourceResource = (IFile)(source.isWorkingCopy() ? source.getOriginalElement() : source).getCorrespondingResource();
	IContainer destFolder = (IContainer)dest.getCorrespondingResource(); // can be an IFolder or an IProject
	IFile destFile = destFolder.getFile(new Path(destName));
	if (!destFile.equals(sourceResource)) {
		try {
			if (destFile.exists()) {
				if (fForce) {
					// we can remove it
					deleteResource(destFile, IResource.KEEP_HISTORY);
				} else {
					// abort
					throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION));
				}
			}
			int flags = newContent != null ? IResource.NONE : IResource.KEEP_HISTORY;
			if (fForce) flags |= IResource.FORCE;
			if (this.isMove()) {
				sourceResource.move(destFile.getFullPath(), flags, getSubProgressMonitor(1));
			} else {
				sourceResource.copy(destFile.getFullPath(), flags, getSubProgressMonitor(1));
			}
			this.hasModifiedResource = true;
		} catch (JavaModelException e) {
			throw e;
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}

		// update new resource content
		try {
			if (newContent != null){
				String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
				destFile.setContents(
					new ByteArrayInputStream(encoding == null ? newContent.getBytes() : newContent.getBytes(encoding)), 
					fForce ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY,
					getSubProgressMonitor(1));
			}
		} catch(IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	
		// register the correct change deltas
		ICompilationUnit destCU = dest.getCompilationUnit(destName);
		prepareDeltas(source, destCU);
		if (newCUName != null) {
			//the main type has been renamed
			String oldName = source.getElementName();
			oldName = oldName.substring(0, oldName.length() - 5);
			String newName = newCUName;
			newName = newName.substring(0, newName.length() - 5);
			prepareDeltas(source.getType(oldName), destCU.getType(newName));
		}
	} else {
		if (!fForce) {
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION));
		}
		// update new resource content
		// in case we do a saveas on the same resource we have to simply update the contents
		// see http://dev.eclipse.org/bugs/show_bug.cgi?id=9351
		try {
			if (newContent != null){
				String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
				destFile.setContents(
					new ByteArrayInputStream(encoding == null ? newContent.getBytes() : newContent.getBytes(encoding)), 
					fForce ? IResource.FORCE | IResource.KEEP_HISTORY : IResource.KEEP_HISTORY, 
					getSubProgressMonitor(1));
			}
		} catch(IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}
}
/**
 * Process all of the changed deltas generated by this operation.
 */
protected void processDeltas() {
	for (Iterator deltas = this.fDeltasPerProject.values().iterator(); deltas.hasNext();){
		addDelta((IJavaElementDelta) deltas.next());
	}
}
/**
 * @see MultiOperation
 * This method delegates to <code>processCompilationUnitResource</code> or
 * <code>processPackageFragmentResource</code>, depending on the type of
 * <code>element</code>.
 */
protected void processElement(IJavaElement element) throws JavaModelException {
	IJavaElement dest = getDestinationParent(element);
	switch (element.getElementType()) {
		case IJavaElement.COMPILATION_UNIT :
			processCompilationUnitResource((ICompilationUnit) element, (IPackageFragment) dest);
			fCreatedElements.add(((IPackageFragment) dest).getCompilationUnit(element.getElementName()));
			break;
		case IJavaElement.PACKAGE_FRAGMENT :
			processPackageFragmentResource((IPackageFragment) element, (IPackageFragmentRoot) dest, getNewNameFor(element));
			break;
		default :
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element));
	}
}
/**
 * @see MultiOperation
 * Overridden to allow special processing of <code>JavaElementDelta</code>s
 * and <code>fResultElements</code>.
 */
protected void processElements() throws JavaModelException {
	fCreatedElements = new ArrayList(fElementsToProcess.length);
	try {
		super.processElements();
	} catch (JavaModelException jme) {
		throw jme;
	} finally {
		fResultElements = new IJavaElement[fCreatedElements.size()];
		fCreatedElements.toArray(fResultElements);
		processDeltas();
	}
}
/**
 * Copies/moves a package fragment with the name <code>newName</code>
 * to the destination package.<br>
 *
 * @exception JavaModelException if the operation is unable to
 * complete
 */
private void processPackageFragmentResource(IPackageFragment source, IPackageFragmentRoot root, String newName) throws JavaModelException {
	try {
		String newFragName = (newName == null) ? source.getElementName() : newName;
		createNeededPackageFragments(root, newFragName);
		IPackageFragment newFrag = root.getPackageFragment(newFragName);

		// process the leaf resources
		IResource[] resources = collectResourcesOfInterest(source);
		if (resources.length > 0) {
			IPath destPath = newFrag.getUnderlyingResource().getFullPath();
			if (isRename()) {
				if (! destPath.equals(source.getUnderlyingResource().getFullPath())) {
					moveResources(resources, destPath);
				}
			} else if (isMove()) {
				// we need to delete this resource if this operation wants to override existing resources
				for (int i = 0, max = resources.length; i < max; i++) {
					IResource destinationResource = getWorkspace().getRoot().findMember(destPath.append(resources[i].getName()));
					if (destinationResource != null) {
						if (fForce) {
							deleteResource(destinationResource, IResource.KEEP_HISTORY);
						} else {
							throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION));
						}
					}
				}
				moveResources(resources, destPath);
			} else {
				// we need to delete this resource if this operation wants to override existing resources
				for (int i = 0, max = resources.length; i < max; i++) {
					IResource destinationResource = getWorkspace().getRoot().findMember(destPath.append(resources[i].getName()));
					if (destinationResource != null) {
						if (fForce) {
							// we need to delete this resource if this operation wants to override existing resources
							deleteResource(destinationResource, IResource.KEEP_HISTORY);
						} else {
							throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION));
						}
					}
				}
				copyResources(resources, destPath);
			}
			if (!newFrag.getElementName().equals(source.getElementName())) { // if package has been renamed, update the compilation units
				for (int i = 0; i < resources.length; i++) {
					if (resources[i].getName().endsWith(".java")) { //$NON-NLS-1$
						// we only consider potential compilation units
						ICompilationUnit cu = newFrag.getCompilationUnit(resources[i].getName());
						IDOMCompilationUnit domCU = fFactory.createCompilationUnit(cu.getSource(), cu.getElementName());
						if (domCU != null) {
							updatePackageStatement(domCU, newFragName);
							IBuffer buffer = cu.getBuffer();
							if (buffer == null) continue;
							String bufferContents = buffer.getContents();
							if (bufferContents == null) continue;
							String domCUContents = domCU.getContents();
							String cuContents = null;
							if (domCUContents != null) {
								cuContents = org.eclipse.jdt.internal.core.Util.normalizeCRs(domCU.getContents(), bufferContents);
							} else {
								// See PR http://dev.eclipse.org/bugs/show_bug.cgi?id=11285
								cuContents = bufferContents;//$NON-NLS-1$
							}
							buffer.setContents(cuContents);
							cu.save(null, false);
						}
					}
				}
			}
		}

		// discard empty old package (if still empty after the rename)
		if (isMove()) {
			// delete remaining files in this package (.class file in the case where Proj=src=bin)
			IResource[] remaingFiles = ((IContainer)source.getUnderlyingResource()).members();
			boolean isEmpty = true;
			for (int i = 0, length = remaingFiles.length; i < length; i++) {
				IResource file = remaingFiles[i];
				if (file instanceof IFile) {
					this.deleteResource(file, IResource.FORCE | IResource.KEEP_HISTORY);
				} else {
					isEmpty = false;
				}
			}
			if (isEmpty) {
				// delete recursively empty folders
				deleteEmptyPackageFragment(source, false);
			}
		}

		//register the correct change deltas
		prepareDeltas(source, newFrag);
	} catch (DOMException dom) {
		throw new JavaModelException(dom, IJavaModelStatusConstants.DOM_EXCEPTION);
	} catch (JavaModelException e) {
		throw e;
	} catch (CoreException ce) {
		throw new JavaModelException(ce);
	}
}
/**
 * Updates the content of <code>cu</code>, modifying the type name and/or package
 * declaration as necessary.
 *
 * @return the new source
 */
private String updatedContent(ICompilationUnit cu, IPackageFragment dest, String newName) throws JavaModelException {
	String currPackageName = cu.getParent().getElementName();
	String destPackageName = dest.getElementName();
	if (currPackageName.equals(destPackageName) && newName == null) {
		return null; //nothing to change
	} else {
		String typeName = cu.getElementName();
		typeName = typeName.substring(0, typeName.length() - 5);
		IDOMCompilationUnit cuDOM = null;
		IBuffer buffer = cu.getBuffer();
		if (buffer == null) return null;
		char[] contents = buffer.getCharacters();
		if (contents == null) return null;
		cuDOM = fFactory.createCompilationUnit(contents, typeName);
		updateTypeName(cu, cuDOM, cu.getElementName(), newName);
		updatePackageStatement(cuDOM, destPackageName);
		return cuDOM.getContents();
	}
}
/**
 * Makes sure that <code>cu</code> declares to be in the <code>pkgName</code> package.
 */
private void updatePackageStatement(IDOMCompilationUnit domCU, String pkgName) throws JavaModelException {
	boolean defaultPackage = pkgName.equals(IPackageFragment.DEFAULT_PACKAGE_NAME);
	boolean seenPackageNode = false;
	Enumeration enum = domCU.getChildren();
	while (enum.hasMoreElements()) {
		IDOMNode node = (IDOMNode) enum.nextElement();
		if (node.getNodeType() == IDOMNode.PACKAGE) {
			if (! defaultPackage) {
				node.setName(pkgName);
			} else {
				node.remove();
			}
			seenPackageNode = true;
			break;
		}
	}
	if (!seenPackageNode && !defaultPackage) {
		//the cu was in a default package...no package declaration
		//create the new package declaration as the first child of the cu
		IDOMPackage pkg = fFactory.createPackage("package " + pkgName + ";" + Util.LINE_SEPARATOR); //$NON-NLS-1$ //$NON-NLS-2$
		IDOMNode firstChild = domCU.getFirstChild();
		if (firstChild != null) {
			firstChild.insertSibling(pkg);
		} // else the cu was empty: leave it empty
	}
}
	/**
	 * Renames the main type in <code>cu</code>.
	 */
	private void updateTypeName(ICompilationUnit cu, IDOMCompilationUnit domCU, String oldName, String newName) throws JavaModelException {
		if (newName != null) {
			if (fRenamedCompilationUnits == null) {
				fRenamedCompilationUnits= new ArrayList(1);
			}
			fRenamedCompilationUnits.add(cu);
			String oldTypeName= oldName.substring(0, oldName.length() - 5);
			String newTypeName= newName.substring(0, newName.length() - 5);
			// update main type name
			IType[] types = cu.getTypes();
			for (int i = 0, max = types.length; i < max; i++) {
				IType currentType = types[i];
				if (currentType.getElementName().equals(oldTypeName)) {
					IDOMNode typeNode = ((JavaElement) currentType).findNode(domCU);
					if (typeNode != null) {
						typeNode.setName(newTypeName);
					}
				}
			}
		}
	}
/**
 * Possible failures:
 * <ul>
 *  <li>NO_ELEMENTS_TO_PROCESS - no elements supplied to the operation
 *	<li>INDEX_OUT_OF_BOUNDS - the number of renamings supplied to the operation
 *		does not match the number of elements that were supplied.
 * </ul>
 */
protected IJavaModelStatus verify() {
	IJavaModelStatus status = super.verify();
	if (!status.isOK()) {
		return status;
	}

	if (fRenamingsList != null && fRenamingsList.length != fElementsToProcess.length) {
		return new JavaModelStatus(IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
	}
	return JavaModelStatus.VERIFIED_OK;
}
/**
 * @see MultiOperation
 */
protected void verify(IJavaElement element) throws JavaModelException {
	if (element == null || !element.exists())
		error(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, element);
		
	if (element.isReadOnly() && (isRename() || isMove()))
		error(IJavaModelStatusConstants.READ_ONLY, element);

	int elementType = element.getElementType();

	if (elementType == IJavaElement.COMPILATION_UNIT) {
		if (isMove() && ((ICompilationUnit) element).isWorkingCopy())
			error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
	} else if (elementType != IJavaElement.PACKAGE_FRAGMENT) {
		error(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, element);
	}
	
	JavaElement dest = (JavaElement) getDestinationParent(element);
	verifyDestination(element, dest);
	if (fRenamings != null) {
		verifyRenaming(element);
	}
}
}
