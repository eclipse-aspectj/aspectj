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

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractFile extends AbstractResource implements IFile {

	public AbstractFile() {
		super();
	}

	public void appendContents(
		InputStream source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void appendContents(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		appendContents(source, isForce(updateFlags), isHistory(updateFlags), monitor);
	}

	public abstract void create(
		InputStream source,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException;

	public void create(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		create(source, isForce(updateFlags), monitor);
	}

	public void delete(
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		delete(force, monitor);
	}

	public InputStream getContents() throws CoreException {
		return getContents(true);
	}

	public abstract InputStream getContents(boolean force) throws CoreException;

	public abstract int getEncoding() throws CoreException;

	public IFileState[] getHistory(IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void move(
		IPath destination,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void setContents(
		IFileState source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		setContents(source.getContents(), force, keepHistory, monitor);
	}

	public void setContents(
		IFileState source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
		setContents(source.getContents(), updateFlags, monitor);
	}

	public abstract void setContents(
		InputStream source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException;

	public void setContents(
		InputStream source,
		int updateFlags,
		IProgressMonitor monitor)
		throws CoreException {
			setContents(source, isForce(updateFlags), isHistory(updateFlags), monitor);
	}

}
