/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.core.builder;

import org.aspectj.workbench.resources.AbstractFolder;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class ClassFileCacheFolder extends AbstractFolder {
	ClassFileCache parent;
	IPath myPath;
	boolean exists;
	IFolder folder;
	
	public ClassFileCacheFolder(ClassFileCache parent, IPath path, IFolder folder) {
		this.folder = folder;
		this.myPath = path;
		this.parent = parent;
	}

	
	public IFile getFile(IPath path) {
		return parent.getFile(myPath.append(path));
	}

	public IFolder getFolder(IPath path) {
		return parent.getFolder(myPath.append(path));
	}

	public IResource[] members() throws CoreException {
		throw new RuntimeException("unimplemented");
	}


	public void create(boolean force, boolean local, IProgressMonitor monitor)
		throws CoreException 
	{
		exists = true;
		//XXX is this sufficient
//		super.create(force, local, monitor);
	}

	public boolean exists() {
		return exists;
	}

	public boolean exists(IPath path) {
		if (exists) {
			return parent.exists(myPath.append(path));
		} else {
			return false;
		}
	}
	
	/**
	 * @see org.eclipse.core.resources.IResource#delete(boolean, IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor)
		throws CoreException {
		exists = false;
	}


	public void move(
		IPath destination,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("not implemented");
	}

	public IResource findMember(IPath path) {
		throw new RuntimeException("unimplemented");
	}

	public void copy(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IPath getFullPath() {
		throw new RuntimeException("unimplemented");
	}

	public IPath getLocation() {
		throw new RuntimeException("unimplemented");
	}

	public long getModificationStamp() {
		throw new RuntimeException("unimplemented");
	}

	public String getName() {
		throw new RuntimeException("unimplemented");
	}

	public IContainer getParent() {
		throw new RuntimeException("unimplemented");
	}

	public int getType() {
		return IResource.FOLDER;
	}

	public boolean isDerived() {
		return true;
	}
	public void setDerived(boolean isDerived) {
		if (isDerived) return;
		throw new RuntimeException("unimplemented");
	}

	public boolean isReadOnly() {
		throw new RuntimeException("unimplemented");
	}

	public void setReadOnly(boolean readOnly) {
		throw new RuntimeException("unimplemented");
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

}
