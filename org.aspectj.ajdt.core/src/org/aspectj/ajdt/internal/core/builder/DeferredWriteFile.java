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

import java.io.*;

import org.aspectj.workbench.resources.AbstractFile;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public class DeferredWriteFile extends AbstractFile {

	private IFile file;
	private byte[] unwovenBytes;	
	private boolean dirty = false;

	public DeferredWriteFile(IFile file) {
		this.file = file;
	}
	
	public byte[] getUnwovenBytes() {
		return unwovenBytes;
	}
	
	public void writeInnerWovenBytes(String innerName, byte[] bytes) throws CoreException {
		IContainer folder = file.getParent();
		IFile innerFile = folder.getFile(new Path(makeInnerName(innerName)));
		innerFile.create(new ByteArrayInputStream(bytes), true, null);
	
	}

	private String makeInnerName(String innerName) {
		String filename = file.getName();
		String prefix = filename.substring(0, filename.length()-6); // strip the .class
		return prefix + "$" + innerName + ".class";
	}

	
	
	public void writeWovenBytes(byte[] wovenBytes) throws CoreException {
		file.create(new ByteArrayInputStream(wovenBytes), true, null);
	}
	
	public void create(
		InputStream source,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException 
    {
		try {
			dirty = true;
			unwovenBytes =
				Util.getInputStreamAsByteArray(source, -1);
			//??? understand this betterwriteWovenBytes(unwovenBytes);
		} catch (IOException e) {
			throw new JavaModelException(
				e,
				IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}

	public void delete(boolean force, IProgressMonitor monitor)
		throws CoreException {
		//System.out.println("delete: " + this);
		unwovenBytes = null;
		dirty = true;
	}

	public InputStream getContents(boolean force) throws CoreException {
		return new ByteArrayInputStream(unwovenBytes);
	}


	public boolean exists() {
		return unwovenBytes != null;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void deleteRealFile() throws CoreException {
		file.delete(true, null);
	}
	
	public String toString() {
		return "DeferredWriteFile(" + getName() + ", " + exists() + ")";
	}

	public IContainer getParent() {
		throw new RuntimeException("unimplemented");
	}

	public int getEncoding() throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void setContents(
		InputStream source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public void copy(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public String getFileExtension() {
		return file.getFileExtension();
	}

	public IPath getFullPath() {
		return file.getFullPath();
	}

	public IPath getLocation() {
		return file.getLocation();
	}

	public long getModificationStamp() {
		throw new RuntimeException("unimplemented");
	}

	public String getName() {
		return file.getName();
	}

	public int getType() {
		return IResource.FILE;
	}

	public boolean isDerived() {
		return true;
	}
	
	public void setDerived(boolean isDerived) {
		if (isDerived) return;
		throw new RuntimeException("unimplemented");
	}

	public boolean isReadOnly() {
		return false;
	}

	public void setReadOnly(boolean readOnly) {
		throw new RuntimeException("unimplemented");
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}



}
