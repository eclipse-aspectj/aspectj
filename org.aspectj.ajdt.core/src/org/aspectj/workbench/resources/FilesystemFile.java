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


package org.aspectj.workbench.resources;

import java.io.*;

import org.aspectj.util.FileUtil;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;

public class FilesystemFile extends AbstractFile implements IFile {

	private java.io.File file;

	public FilesystemFile(String pathname) {
		super();
		file = new java.io.File(pathname);
	}

	public FilesystemFile(IPath path) {
		super();
		file = path.toFile();
	}

	/**
	 * Closes the source stream.
	 */
	public void create(
		InputStream source,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		FileOutputStream destination = null;
		try {
			try {
				byte[] buffer = new byte[Math.max(256, source.available())];	//XXX does available always work?
				//??? is this always right
				file.getParentFile().mkdirs();
				
				destination = new FileOutputStream(file);
		
				while (true) {
					int bytesRead = -1;
					bytesRead = source.read(buffer);
	//				System.out.println("READ: " + bytesRead);
					if (bytesRead == -1)
						break;
					if (bytesRead == 0) {
						throw new RuntimeException("read 0 bytes");
					}
					destination.write(buffer, 0, bytesRead);
					if (monitor != null) monitor.worked(1);
				}
			} finally {
				if (destination != null) destination.close();
				source.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("unexpected: " + e);
			//throw new CoreException(new IStatus() {});
		}
	}

	public InputStream getContents(boolean force) throws CoreException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException fnfe) {
			throw new CoreException(null);	
		}
	}

	public int getEncoding() throws CoreException {
		return 0;
	}

	public void setContents(
		InputStream source,
		boolean force,
		boolean keepHistory,
		IProgressMonitor monitor)
		throws CoreException {
		create(source, force, monitor);
	}

	public void copy(
		IPath destination,
		boolean force,
		IProgressMonitor monitor)
		throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public java.io.File getFile() {
		return file;	
	}

	public void delete(boolean force, IProgressMonitor monitor)
		throws CoreException 
	{
		if (!file.delete()) {
			throw new RuntimeException("couldn't delete: " + this); //XXX should be CoreException 
		}			
	}

	public boolean exists() {
		return file.exists() && file.isFile();
	}

	public String getFileExtension() {
		String fileName = file.getName();
		int dotIndex = fileName.indexOf('.');
		if (dotIndex != -1) {
			return fileName.substring(dotIndex+1);
		} else {
			return null;
		}
	}

	public IPath getFullPath() {
		return new Path(file.getPath());
	}

	public IPath getLocation() {
		return new Path(file.getAbsolutePath());
	}

	public long getModificationStamp() {
		return file.lastModified();
	}

	public String getName() {
		return file.getName();
	}

	/**
	 * This will create a new instance of a folder rather than looking up a new
	 * folder first.
	 * 
	 * ??? is the above correct
	 */
	public IContainer getParent() {
		return new FilesystemFolder(file.getParent());
	}

	public int getType() {
		return IResource.FILE;
	}

	public boolean isDerived() {
		return false;
	}

	public boolean isReadOnly() {
		if (!file.canWrite()) {
			return true;
		} else {
			return false;
		}
	}

	public void setReadOnly(boolean readOnly) {
		if (readOnly) {
			file.setReadOnly();
		} 
	}

	public void touch(IProgressMonitor monitor) throws CoreException {
		throw new RuntimeException("unimplemented");
	}

	public String toString() {
		return getFullPath().toString();	
	}

//	private IStatus genStatus(Throwable t) {
//		return new Status(IStatus.ERROR, "", 0, "file operation failed for: " + file.getName(), t);
//	}


}
