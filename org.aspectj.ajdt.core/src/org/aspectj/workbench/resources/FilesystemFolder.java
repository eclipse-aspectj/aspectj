/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.workbench.resources;

import org.apache.bcel.generic.RETURN;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * When lookup for parent or contained paths is done a new instance of a folder or file 
 * is returned instead of using an existing instance.
 * 
 * ??? is the above correct behavior
 */
public class FilesystemFolder extends AbstractFolder implements IFolder {

	private java.io.File dir;

	public FilesystemFolder(IPath path) {
		super();
		dir = path.toFile();
	}
	

	public FilesystemFolder(String pathname) {
		super();
		dir = new java.io.File(pathname);
	}

	public void create(boolean force, boolean local, IProgressMonitor monitor)
		throws CoreException 
	{
		dir.mkdir();
	}

	public void copy(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException 
	{
		throw new RuntimeException("unimplemented");
	}

	public void delete(boolean force, IProgressMonitor monitor)
		throws CoreException {
		dir.delete();
	}

	public boolean exists() {
		return dir.exists() && dir.isDirectory();
	}

	public IPath getFullPath() {
		return new Path(dir.getPath());
	}

	public IPath getLocation() {
		return new Path(dir.getAbsolutePath());
	}

	public long getModificationStamp() {
		return dir.lastModified();
	}

	public String getName() {
		return dir.getName();
	}
	
	public int getType() {
		return IResource.FILE;
	}

	public boolean isReadOnly() {
		return !dir.canWrite();
	}

	public void setReadOnly(boolean readOnly) {
		if (readOnly) dir.setReadOnly();
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public boolean isDerived() {
		return false;
	}

	public IContainer getParent() {
		return new FilesystemFolder(dir.getParent());
	}

	public boolean exists(IPath path) {
		String pathString = new java.io.File(path.toString()).getAbsolutePath();
		String dirPathString = dir.getAbsolutePath();
		return pathString.startsWith(dirPathString);
	}

	public IFile getFile(IPath path) {
		return new FilesystemFile(dir.getPath() + java.io.File.separator + path.toString());	
	}

	public IFolder getFolder(IPath path) {
		return new FilesystemFolder(dir.getPath() + java.io.File.separator + path.toString());	
	}

	public IResource[] members() throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public IResource findMember(IPath path) {
		throw new RuntimeException("unimplemented");
	}
	/**
	 * @return		the full path  
	 * ??? is this wrong
	 */
	public IPath getProjectRelativePath() {
		return getFullPath();
	}

}
