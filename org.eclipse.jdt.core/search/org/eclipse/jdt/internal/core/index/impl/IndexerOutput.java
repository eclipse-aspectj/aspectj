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

import org.eclipse.jdt.internal.core.index.IDocument;
import org.eclipse.jdt.internal.core.index.IIndexerOutput;

/**
 * An indexerOutput is used by an indexer to add documents and word references to
 * an inMemoryIndex. It keeps track of the document being indexed and add the
 * word references to this document (so you do not need to precise the document
 * each time you add a word).
 */

public class IndexerOutput implements IIndexerOutput {
	protected InMemoryIndex index;
	protected IndexedFile indexedFile;
	protected IDocument document;
	/**
	 * IndexerOutput constructor comment.
	 */
	public IndexerOutput(InMemoryIndex index) {
		this.index= index;
	}
	/**
	 * Adds the given document to the inMemoryIndex.
	 */

	public void addDocument(IDocument document) {
		if (indexedFile == null) {
			indexedFile= index.addDocument(document);
		} else {
			throw new IllegalStateException();
		}
	}
	/**
	 * Adds a reference to the given word to the inMemoryIndex.
	 */
	public void addRef(char[] word) {
		if (indexedFile == null) {
			throw new IllegalStateException();
		}
		index.addRef(indexedFile, word);
	}
	/**
	 * Adds a reference to the given word to the inMemoryIndex.
	 */
	public void addRef(String word) {
		addRef(word.toCharArray());
	}
}
