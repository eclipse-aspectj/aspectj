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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A jar entry that represents a non-java resource found in a JAR.
 *
 * @see IStorage
 */
public class JarEntryFile extends PlatformObject implements IStorage {
	private String entryName;
	private String zipName;
	private IPath path;
	
	public JarEntryFile(String entryName, String zipName){
		this.entryName = entryName;
		this.zipName = zipName;
		this.path = new Path(this.entryName);
	}
public InputStream getContents() throws CoreException {

	try {
		if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
			System.out.println("(" + Thread.currentThread() + ") [JarEntryFile.getContents()] Creating ZipFile on " + this.zipName); //$NON-NLS-1$	//$NON-NLS-2$
		}
		ZipFile zipFile = new ZipFile(this.zipName); 
		ZipEntry zipEntry = zipFile.getEntry(this.entryName);
		if (zipEntry == null){
			throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, this.entryName));
		}
		return zipFile.getInputStream(zipEntry);
	} catch (IOException e){
		throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
	}
}
/**
 * @see IStorage#getFullPath
 */
public IPath getFullPath() {
	return this.path;
}
/**
 * @see IStorage#getName
 */
public String getName() {
	return this.path.lastSegment();
}
/**
 * @see IStorage#isReadOnly()
 */
public boolean isReadOnly() {
	return true;
}
/**
 * @see IStorage#isReadOnly()
 */
public String toString() {
	return "JarEntryFile["+this.zipName+"::"+this.entryName+"]"; //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
}
}
