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
import java.io.RandomAccessFile;

/**
 * A safe subclass of RandomAccessFile, which ensure that it's closed
 * on finalize.
 */
public class SafeRandomAccessFile extends RandomAccessFile {
	public SafeRandomAccessFile(java.io.File file, String mode) throws java.io.IOException {
		super(file, mode);
	}
	public SafeRandomAccessFile(String name, String mode) throws java.io.IOException {
		super(name, mode);
	}
	protected void finalize() throws IOException {
		close();
	}
}
