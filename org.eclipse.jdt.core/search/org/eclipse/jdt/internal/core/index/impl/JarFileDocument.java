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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;

/**
 * An <code>JarFileDocument</code> represents an jar file.
 */

public class JarFileDocument extends PropertyDocument {
	protected IFile file;
	/**
	 * JarFileDocument constructor comment.
	 */
	public JarFileDocument(IFile file) {
		this.file = file;
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getByteContent()
	 */
	public byte[] getByteContent() throws IOException {
		return null;
	}
	/**
	 * This API always return null for a JarFileDocument
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getCharContent()
	 */
	public char[] getCharContent() throws IOException {
		return null;
	}
	public File getFile() {
		return file.getLocation().toFile();
	}
	/**
	 * @see org.eclipse.jdt.internal.core.index.IDocument#getName()
	 */
	public String getName() {
		return file.getFullPath().toString();
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
		String extension= file.getFileExtension();
		if (extension == null)
			return ""; //$NON-NLS-1$
		return extension;
	}
}
