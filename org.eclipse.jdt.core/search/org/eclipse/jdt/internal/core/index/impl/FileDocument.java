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

/**
 * A <code>FileDocument</code> represents a java.io.File.
 */

public class FileDocument extends PropertyDocument {
	File file;

	public FileDocument(File file) {
		super();
		this.file= file;
	}
	/**
	 * @see IDocument#getByteContent
	 */
	public byte[] getByteContent() throws IOException {
		return org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(file);
	}
	/**
	 * @see IDocument#getCharContent
	 */
	public char[] getCharContent() throws IOException {
		return org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(file, null);
	}
	/**
	 * @see IDocument#getName
	 */
	public String getName() {
		return file.getAbsolutePath().replace(File.separatorChar, IIndexConstants.FILE_SEPARATOR);
	}
	/**
	 * @see IDocument#getStringContent
	 */
	public String getStringContent() throws IOException {
		return new String(getCharContent());
	}
	/**
	 * @see IDocument#getType
	 */
	public String getType() {
		int lastDot= file.getPath().lastIndexOf('.');
		if (lastDot == -1)
			return ""; //$NON-NLS-1$
		return file.getPath().substring(lastDot + 1);
	}
}
