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

/**
 * An indexOutput is used to write an index into a different object (a File, ...). 
 */
public abstract class IndexOutput {
	/**
	 * Adds a File to the destination.
	 */
	public abstract void addFile(IndexedFile file) throws IOException;
	/**
	 * Adds a word to the destination.
	 */
	public abstract void addWord(WordEntry word) throws IOException;
	/**
	 * Closes the output, releasing the resources it was using.
	 */
	public abstract void close() throws IOException;
	/**
	 * Flushes the output.
	 */
	public abstract void flush() throws IOException;
	/**
	 * Returns the Object the output is writing to. It can be a file, another type of index, ... 
	 */
	public abstract Object getDestination();
	/**
	 * Opens the output, before writing any information.
	 */
	public abstract void open() throws IOException;
}
