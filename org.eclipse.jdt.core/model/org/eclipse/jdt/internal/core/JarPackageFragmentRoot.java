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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A package fragment root that corresponds to a .jar or .zip.
 *
 * <p>NOTE: The only visible entries from a .jar or .zip package fragment root
 * are .class files.
 * <p>NOTE: A jar package fragment root may or may not have an associated resource.
 *
 * @see org.eclipse.jdt.core.IPackageFragmentRoot
 * @see org.eclipse.jdt.internal.core.JarPackageFragmentRootInfo
 */
public class JarPackageFragmentRoot extends PackageFragmentRoot {
	
	public final static String[] NO_STRINGS = new String[0];
	public final static ArrayList EMPTY_LIST = new ArrayList();
	
	/**
	 * The path to the jar file
	 * (a workspace relative path if the jar is internal,
	 * or an OS path if the jar is external)
	 */
	protected final IPath jarPath;

	/**
	 * The delimiter between the zip path and source path in the
	 * attachment server property.
	 */
	protected final static char ATTACHMENT_PROPERTY_DELIMITER= '*';

	/**
	 * The name of the meta-inf directory not to be included as a 
	 * jar package fragment.
	 * @see #computeJarChildren
	 */
	//protected final static String META_INF_NAME = "META-INF/";
	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy 
	 * based on a JAR file that is not contained in a <code>IJavaProject</code> and
	 * does not have an associated <code>IResource</code>.
	 */
	protected JarPackageFragmentRoot(String jarPath, IJavaProject project) {
		super(null, project, jarPath);
		this.jarPath = new Path(jarPath);
	}
	/**
	 * Constructs a package fragment root which is the root of the Java package directory hierarchy 
	 * based on a JAR file.
	 */
	protected JarPackageFragmentRoot(IResource resource, IJavaProject project) {
		super(resource, project);
		this.jarPath = resource.getFullPath();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public void attachSource(IPath zipPath, IPath rootPath, IProgressMonitor monitor) throws JavaModelException {
		try {
			verifyAttachSource(zipPath);
			if (monitor != null) {
				monitor.beginTask(Util.bind("element.attachingSource"), 2); //$NON-NLS-1$
			}
			SourceMapper mapper= null;
			SourceMapper oldMapper= getSourceMapper();
			IWorkspace workspace= getJavaModel().getWorkspace();
			boolean rootNeedsToBeClosed= false;

			if (zipPath == null) {
				//source being detached
				rootNeedsToBeClosed= true;
			/* Disable deltas (see 1GDTUSD)
				// fire a delta to notify the UI about the source detachement.
				JavaModelManager manager = (JavaModelManager) JavaModelManager.getJavaModelManager();
				JavaModel model = (JavaModel) getJavaModel();
				JavaElementDelta attachedSourceDelta = new JavaElementDelta(model);
				attachedSourceDelta .sourceDetached(this); // this would be a JarPackageFragmentRoot
				manager.registerResourceDelta(attachedSourceDelta );
				manager.fire(); // maybe you want to fire the change later. Let us know about it.
			*/
			} else {
			/*
				// fire a delta to notify the UI about the source attachement.
				JavaModelManager manager = (JavaModelManager) JavaModelManager.getJavaModelManager();
				JavaModel model = (JavaModel) getJavaModel();
				JavaElementDelta attachedSourceDelta = new JavaElementDelta(model);
				attachedSourceDelta .sourceAttached(this); // this would be a JarPackageFragmentRoot
				manager.registerResourceDelta(attachedSourceDelta );
				manager.fire(); // maybe you want to fire the change later. Let us know about it.
			 */

				//check if different from the current attachment
				IPath storedZipPath= getSourceAttachmentPath();
				IPath storedRootPath= getSourceAttachmentRootPath();
				if (monitor != null) {
					monitor.worked(1);
				}
				if (storedZipPath != null) {
					if (!(storedZipPath.equals(zipPath) && rootPath.equals(storedRootPath))) {
						rootNeedsToBeClosed= true;
					}
				}
				if ((zipPath.isAbsolute() && workspace.getRoot().findMember(zipPath) != null) || !zipPath.isAbsolute()) {
					// internal to the workbench
					// a resource
					IResource zipFile= workspace.getRoot().findMember(zipPath);
					if (zipFile == null) {
						if (monitor != null) {
							monitor.done();
						}
						throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, zipPath));
					}
					if (!(zipFile.getType() == IResource.FILE)) {
						if (monitor != null) {
							monitor.done();
						}
						throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, zipPath));
					}
				}
				mapper= new SourceMapper(zipPath, rootPath.toOSString());
			}
			setSourceMapper(mapper);
			if (zipPath == null) {
				setSourceAttachmentProperty(null); //remove the property
			} else {
				//set the property to the path of the mapped zip
				setSourceAttachmentProperty(zipPath.toString() + ATTACHMENT_PROPERTY_DELIMITER + rootPath.toString());
			}
			if (rootNeedsToBeClosed) {
				if (oldMapper != null) {
					oldMapper.close();
				}
				BufferManager manager= BufferManager.getDefaultBufferManager();
				Enumeration openBuffers= manager.getOpenBuffers();
				while (openBuffers.hasMoreElements()) {
					IBuffer buffer= (IBuffer) openBuffers.nextElement();
					IOpenable possibleJarMember= buffer.getOwner();
					if (isAncestorOf((IJavaElement) possibleJarMember)) {
						buffer.close();
					}
				}
				if (monitor != null) {
					monitor.worked(1);
				}
			}
		} catch (JavaModelException e) {
			setSourceAttachmentProperty(null); // loose info - will be recomputed
			throw e;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
	/**
	 * Close the associated JAR file stored in the info of this element. If
	 * this jar has an associated ZIP source attachment, close it too.
	 *
	 * @see IOpenable
	 */
	protected void closing(Object info) throws JavaModelException {
		SourceMapper mapper= getSourceMapper();
		if (mapper != null) {
			mapper.close();
		}
		super.closing(info);
	}
	/**
	 * Compute the package fragment children of this package fragment root.
	 * These are all of the directory zip entries, and any directories implied
	 * by the path of class files contained in the jar of this package fragment root.
	 * Has the side effect of opening the package fragment children.
	 */
	protected boolean computeChildren(OpenableElementInfo info) throws JavaModelException {
		ArrayList vChildren= new ArrayList();
		computeJarChildren((JarPackageFragmentRootInfo) info, vChildren);
		IJavaElement[] children= new IJavaElement[vChildren.size()];
		vChildren.toArray(children);
		info.setChildren(children);
		return true;
	}
/**
 * Determine all of the package fragments associated with this package fragment root.
 * Cache the zip entries for each package fragment in the info for the package fragment.
 * The package fragment children are all opened.
 * Add all of the package fragments to vChildren.
 *
 * @exception JavaModelException The resource (the jar) associated with this package fragment root does not exist
 */
protected void computeJarChildren(JarPackageFragmentRootInfo info, ArrayList vChildren) throws JavaModelException {
	final int JAVA = 0;
	final int NON_JAVA = 1;
	ZipFile jar= null;
	try {
		jar= getJar();

		if (isExternal()){
			// remember the timestamp of this library
			long timestamp = DeltaProcessor.getTimeStamp(new File(getPath().toOSString()));
			JavaModelManager.getJavaModelManager().deltaProcessor.externalTimeStamps.put(getPath(), new Long(timestamp));			
		}

		HashMap packageFragToTypes= new HashMap();

		// always create the default package
		packageFragToTypes.put(IPackageFragment.DEFAULT_PACKAGE_NAME, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });

		for (Enumeration e= jar.entries(); e.hasMoreElements();) {
			ZipEntry member= (ZipEntry) e.nextElement();
			String entryName= member.getName();

			if (member.isDirectory()) {
				
				int last = entryName.length() - 1;
				entryName= entryName.substring(0, last);
				entryName= entryName.replace('/', '.');

				// add the package name & all of its parent packages
				while (true) {
					// extract the package name
					if (packageFragToTypes.containsKey(entryName)) break;
					packageFragToTypes.put(entryName, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });
					
					if ((last = entryName.lastIndexOf('.')) < 0) break;
					entryName = entryName.substring(0, last);
				}
			} else {
				//store the class file / non-java rsc entry name to be cached in the appropriate package fragment
				//zip entries only use '/'
				int lastSeparator= entryName.lastIndexOf('/');
				String packageName;
				String fileName;
				if (lastSeparator != -1) { //not in the default package
					entryName= entryName.replace('/', '.');
					fileName= entryName.substring(lastSeparator + 1);
					packageName= entryName.substring(0, lastSeparator);
				} else {
					fileName = entryName;
					packageName =  IPackageFragment.DEFAULT_PACKAGE_NAME;
				}
				
				// add the package name & all of its parent packages
				String currentPackageName = packageName;
				while (true) {
					// extract the package name
					if (packageFragToTypes.containsKey(currentPackageName)) break;
					packageFragToTypes.put(currentPackageName, new ArrayList[] { EMPTY_LIST, EMPTY_LIST });
					
					int last;
					if ((last = currentPackageName.lastIndexOf('.')) < 0) break;
					currentPackageName = currentPackageName.substring(0, last);
				}
				// add classfile info amongst children
				ArrayList[] children = (ArrayList[]) packageFragToTypes.get(packageName);
				if (Util.isClassFileName(entryName)) {
					if (children[JAVA] == EMPTY_LIST) children[JAVA] = new ArrayList();
					children[JAVA].add(fileName);
				} else {
					if (children[NON_JAVA] == EMPTY_LIST) children[NON_JAVA] = new ArrayList();
					children[NON_JAVA].add(fileName);
				}
			}
		}
		//loop through all of referenced packages, creating package fragments if necessary
		// and cache the entry names in the infos created for those package fragments
		Iterator packages = packageFragToTypes.keySet().iterator();
		while (packages.hasNext()) {
			String packName = (String) packages.next();
			
			ArrayList[] entries= (ArrayList[]) packageFragToTypes.get(packName);
			JarPackageFragment packFrag= (JarPackageFragment) getPackageFragment(packName);
			JarPackageFragmentInfo fragInfo= (JarPackageFragmentInfo) packFrag.createElementInfo();
			if (entries[0].size() > 0){
				fragInfo.setEntryNames(entries[JAVA]);
			}
			int resLength= entries[NON_JAVA].size();
			if (resLength == 0) {
				packFrag.computeNonJavaResources(NO_STRINGS, fragInfo, jar.getName());
			} else {
				String[] resNames= new String[resLength];
				entries[NON_JAVA].toArray(resNames);
				packFrag.computeNonJavaResources(resNames, fragInfo, jar.getName());
			}
			packFrag.computeChildren(fragInfo);
			JavaModelManager.getJavaModelManager().putInfo(packFrag, fragInfo);
			vChildren.add(packFrag);
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	} finally {
		JavaModelManager.getJavaModelManager().closeZipFile(jar);
	}
}
	/**
	 * Returns a new element info for this element.
	 */
	protected OpenableElementInfo createElementInfo() {
		return new JarPackageFragmentRootInfo();
	}
	/**
	 * A Jar is always K_BINARY.
	 *
	 * @exception NotPresentException if the project and root do
	 *      not exist.
	 */
	protected int determineKind(IResource underlyingResource) throws JavaModelException {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns true if this handle represents the same jar
	 * as the given handle. Two jars are equal if they share
	 * the same zip file.
	 *
	 * @see Object#equals
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof JarPackageFragmentRoot) {
			JarPackageFragmentRoot other= (JarPackageFragmentRoot) o;
			return this.jarPath.equals(other.jarPath);
		}
		return false;
	}
public IClasspathEntry findSourceAttachmentRecommendation() {

	try {

		IPath rootPath = this.getPath();
		IClasspathEntry entry;
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		// try on enclosing project first
		JavaProject parentProject = (JavaProject) getJavaProject();
		try {
			entry = parentProject.getClasspathEntryFor(rootPath);
			if (entry != null){
				Object target = JavaModel.getTarget(workspaceRoot, entry.getSourceAttachmentPath(), true);
				if (target instanceof IFile){
					IFile file = (IFile) target;
					if ("jar".equalsIgnoreCase(file.getFileExtension()) || "zip".equalsIgnoreCase(file.getFileExtension())){ //$NON-NLS-2$ //$NON-NLS-1$
						return entry;
					}
				}
				if (target instanceof java.io.File){
					java.io.File file = (java.io.File) target;
					String name = file.getName();
					if (Util.endsWithIgnoreCase(name, ".jar") || Util.endsWithIgnoreCase(name, ".zip")){ //$NON-NLS-2$ //$NON-NLS-1$
						return entry;
					}
				}
			}
		} catch(JavaModelException e){
		}
		
		// iterate over all projects
		IJavaModel model = getJavaModel();
		IJavaProject[] jProjects = model.getJavaProjects();
		for (int i = 0, max = jProjects.length; i < max; i++){
			JavaProject jProject = (JavaProject) jProjects[i];
			if (jProject == parentProject) continue; // already done
			try {
				entry = jProject.getClasspathEntryFor(rootPath);
				if (entry != null){
					Object target = JavaModel.getTarget(workspaceRoot, entry.getSourceAttachmentPath(), true);
					if (target instanceof IFile){
						IFile file = (IFile) target;
						String name = file.getName();
						if (Util.endsWithIgnoreCase(name, ".jar") || Util.endsWithIgnoreCase(name, ".zip")){ //$NON-NLS-2$ //$NON-NLS-1$
							return entry;
						}
					}
					if (target instanceof java.io.File){
						java.io.File file = (java.io.File) target;
						String name = file.getName();
						if (Util.endsWithIgnoreCase(name, ".jar") || Util.endsWithIgnoreCase(name, ".zip")){ //$NON-NLS-2$ //$NON-NLS-1$
							return entry;
						}
					}
				}
			} catch(JavaModelException e){
			}
		}
	} catch(JavaModelException e){
	}

	return null;
}



	/**
	 * Returns the underlying ZipFile for this Jar package fragment root.
	 *
	 * @exception CoreException if an error occurs accessing the jar
	 */
	public ZipFile getJar() throws CoreException {
		return JavaModelManager.getJavaModelManager().getZipFile(getPath());
	}
	/**
	 * @see IJavaElement
	 */
	public IJavaProject getJavaProject() {
		IJavaElement parent= getParent();
		if (parent == null) {
			return null;
		} else {
			return parent.getJavaProject();
		}
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public int getKind() {
		return IPackageFragmentRoot.K_BINARY;
	}
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {
		// We want to show non java resources of the default package at the root (see PR #1G58NB8)
		return ((JarPackageFragment) this.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME)).storedNonJavaResources();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPackageFragment getPackageFragment(String packageName) {

		return new JarPackageFragment(this, packageName);
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getPath() {
		if (fResource == null) {
			return this.jarPath;
		} else {
			return super.getPath();
		}
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getSourceAttachmentPath() throws JavaModelException {
		String serverPathString= getSourceAttachmentProperty();
		if (serverPathString == null) {
			return null;
		}
		int index= serverPathString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER);
		if (index < 0) return null;
		String serverZipPathString= serverPathString.substring(0, index);
		return new Path(serverZipPathString);
	}
	/**
	 * Returns the server property for this package fragment root's
	 * source attachement.
	 */
	protected String getSourceAttachmentProperty() throws JavaModelException {
		String propertyString = null;
		QualifiedName qName= getSourceAttachmentPropertyName();
		try {
			propertyString = getWorkspace().getRoot().getPersistentProperty(qName);
			
			// if no existing source attachment information, then lookup a recommendation from classpath entries
			if (propertyString == null || propertyString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER) < 0){
				IClasspathEntry recommendation = findSourceAttachmentRecommendation();
				if (recommendation != null){
					propertyString = recommendation.getSourceAttachmentPath().toString() 
										+ ATTACHMENT_PROPERTY_DELIMITER 
										+ (recommendation.getSourceAttachmentRootPath() == null ? "" : recommendation.getSourceAttachmentRootPath().toString()); //$NON-NLS-1$
					setSourceAttachmentProperty(propertyString);
				}
			}
			return propertyString;
		} catch (CoreException ce) {
			throw new JavaModelException(ce);
		}
	}
	
	public void setSourceAttachmentProperty(String property){
		try {
			getWorkspace().getRoot().setPersistentProperty(this.getSourceAttachmentPropertyName(), property);
		} catch (CoreException ce) {
		}
	}
	
	/**
	 * Returns the qualified name for the source attachment property
	 * of this jar.
	 */
	protected QualifiedName getSourceAttachmentPropertyName() throws JavaModelException {
		return new QualifiedName(JavaCore.PLUGIN_ID, "sourceattachment: " + this.jarPath.toOSString()); //$NON-NLS-1$
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public IPath getSourceAttachmentRootPath() throws JavaModelException {
		String serverPathString= getSourceAttachmentProperty();
		if (serverPathString == null) {
			return null;
		}
		int index= serverPathString.lastIndexOf(ATTACHMENT_PROPERTY_DELIMITER);
		String serverRootPathString= IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
		if (index != serverPathString.length() - 1) {
			serverRootPathString= serverPathString.substring(index + 1);
		}
		return new Path(serverRootPathString);
	}
	/**
	 * @see JavaElement
	 */
	public SourceMapper getSourceMapper() {
		try {
			return ((JarPackageFragmentRootInfo) getElementInfo()).getSourceMapper();
		} catch (JavaModelException e) {
			return null;
		}
	}
	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		if (fResource == null) {
			return null;
		} else {
			return super.getUnderlyingResource();
		}
	}
	/**
	 * If I am not open, return true to avoid parsing.
	 *
	 * @see IParent 
	 */
	public boolean hasChildren() throws JavaModelException {
		if (isOpen()) {
			return getChildren().length > 0;
		} else {
			return true;
		}
	}
	public int hashCode() {
		return this.jarPath.hashCode();
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isArchive() {
		return true;
	}
	/**
	 * @see IPackageFragmentRoot
	 */
	public boolean isExternal() {
		return fResource == null;
	}
	/**
	 * Jars and jar entries are all read only
	 */
	public boolean isReadOnly() {
		return true;
	}
	/**
	 * @see Openable#openWhenClosed()
	 */
	protected void openWhenClosed(IProgressMonitor pm) throws JavaModelException {
		super.openWhenClosed(pm);
		try {
			//restore any stored attached source zip
			IPath zipPath= getSourceAttachmentPath();
			if (zipPath != null) {
				IPath rootPath= getSourceAttachmentRootPath();
				attachSource(zipPath, rootPath, pm);
			}
		} catch(JavaModelException e){ // no attached source
		}
	}
	/**
	 * An archive cannot refresh its children.
	 */
	public void refreshChildren() {
		// do nothing
	}
/*
 * @see JavaElement#rootedAt(IJavaProject)
 */
public IJavaElement rootedAt(IJavaProject project) {
	if (fResource == null) {
		return
			new JarPackageFragmentRoot(
				this.jarPath.toString(),
				project);
	} else {
		return
			new JarPackageFragmentRoot(
				fResource,
				project);
	}
}

	/**
	 * For use by <code>AttachSourceOperation</code> only.
	 * Sets the source mapper associated with this jar.
	 */
	public void setSourceMapper(SourceMapper mapper) throws JavaModelException {
		((JarPackageFragmentRootInfo) getElementInfo()).setSourceMapper(mapper);
	}
	/**
	 * Possible failures: <ul>
	 *  <li>RELATIVE_PATH - the path supplied to this operation must be
	 *      an absolute path
	 *  <li>ELEMENT_NOT_PRESENT - the jar supplied to the operation
	 *      does not exist
	 * </ul>
	 */
	protected void verifyAttachSource(IPath zipPath) throws JavaModelException {
		if (!exists()) {
			throw newNotPresentException();
		} else if (zipPath != null && !zipPath.isAbsolute()) {
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.RELATIVE_PATH, zipPath));
		}
	}

/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	StringBuffer buff= new StringBuffer(((JavaElement)getParent()).getHandleMemento());
	buff.append(getHandleMementoDelimiter());
	buff.append(this.jarPath.toString()); // 1GEP51U
	return buff.toString();
}
}
