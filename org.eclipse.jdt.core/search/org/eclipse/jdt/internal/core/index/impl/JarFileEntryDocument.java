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
package org.eclipse.jdt.internal.core.index.impl;

import java.io.IOException;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.Path;


/**
 * An <code>JarFileEntryDocument</code> represents an jar file.
 */

public class JarFileEntryDocument extends PropertyDocument {
	protected ZipEntry zipEntry;
	protected byte[] byteContents;
	protected Path zipFilePath;
	public static final String JAR_FILE_ENTRY_SEPARATOR = "|"; //$NON-NLS-1$
/**
 * JarFileEntryDocument constructor comment.
 */
public JarFileEntryDocument(ZipEntry entry, byte[] contents, Path zipFilePath) {
	this.zipEntry = entry;
	this.byteContents = contents;
	this.zipFilePath = zipFilePath;
}
/**
 * This API always return null for a JarFileDocument
 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
 */
public byte[] getByteContent() throws IOException {
	return this.byteContents;
}
/**
 * This API always return null for a JarFileDocument
 * @see org.eclipse.jdt.internal.core.index.IDocument#getCharContent()
 */
public char[] getCharContent() throws IOException {
	return null;
}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getName()
	 */
	public String getName() {
		return zipFilePath + JAR_FILE_ENTRY_SEPARATOR + zipEntry.getName();
	}
/**
 * This API always return null for a JarFileDocument
 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
 */
public String getStringContent() throws java.io.IOException {
	return null;
}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getType()
	 */
	public String getType() {
		return "class"; //$NON-NLS-1$
	}
public void setBytes(byte[] byteContents) {
	this.byteContents = byteContents;
}
}
