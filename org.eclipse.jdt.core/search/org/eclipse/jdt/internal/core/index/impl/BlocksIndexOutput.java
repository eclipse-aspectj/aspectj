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
import java.io.RandomAccessFile;

/**
 * A blocksIndexOutput is used to save an index in a file with the given structure:<br>
 *  - Signature of the file;<br>
 *  - FileListBlocks;<br>
 *  - IndexBlocks;<br>
 *  - Summary of the index.
 */

public class BlocksIndexOutput extends IndexOutput {
	protected RandomAccessFile indexOut;
	protected int blockNum;
	protected boolean opened= false;
	protected File indexFile;
	protected FileListBlock fileListBlock;
	protected IndexBlock indexBlock;
	protected int numWords= 0;
	protected IndexSummary summary;
	protected int numFiles= 0;
	protected boolean firstInBlock;
	protected boolean firstIndexBlock;
	protected boolean firstFileListBlock;

	public BlocksIndexOutput(File indexFile) {
		this.indexFile= indexFile;
		summary= new IndexSummary();
		blockNum= 1;
		firstInBlock= true;
		firstIndexBlock= true;
		firstFileListBlock= true;
	}
	/**
	 * @see IndexOutput#addFile
	 */
	public void addFile(IndexedFile indexedFile) throws IOException {
		if (firstFileListBlock) {
			firstInBlock= true;
			fileListBlock= new FileListBlock(IIndexConstants.BLOCK_SIZE);
			firstFileListBlock= false;
		}
		if (fileListBlock.addFile(indexedFile)) {
			if (firstInBlock) {
				summary.addFirstFileInBlock(indexedFile, blockNum);
				firstInBlock= false;
			}
			numFiles++;
		} else {
			if (fileListBlock.isEmpty()) {
				return;
			}
			flushFiles();
			addFile(indexedFile);
		}
	}
	/**
	 * @see IndexOutput#addWord
	 */
	public void addWord(WordEntry entry) throws IOException {
		if (firstIndexBlock) {
			indexBlock= new GammaCompressedIndexBlock(IIndexConstants.BLOCK_SIZE);
			firstInBlock= true;
			firstIndexBlock= false;
		}
		if (entry.getNumRefs() == 0)
			return;
		if (indexBlock.addEntry(entry)) {
			if (firstInBlock) {
				summary.addFirstWordInBlock(entry.getWord(), blockNum);
				firstInBlock= false;
			}
			numWords++;
		} else {
			if (indexBlock.isEmpty()) {
				return;
			}
			flushWords();
			addWord(entry);
		}
	}
	/**
	 * @see IndexOutput#close
	 */
	public void close() throws IOException {
		if (opened) {
			indexOut.close();
			summary= null;
			numFiles= 0;
			opened= false;
		}
	}
	/**
	 * @see IndexOutput#flush
	 */
	public void flush() throws IOException {
		
		summary.setNumFiles(numFiles);
		summary.setNumWords(numWords);
		indexOut.seek(blockNum * (long) IIndexConstants.BLOCK_SIZE);
		summary.write(indexOut);
		indexOut.seek(0);
		indexOut.writeUTF(IIndexConstants.SIGNATURE);
		indexOut.writeInt(blockNum);
	}
	/**
	 * Writes the current fileListBlock on the disk and initialises it
	 * (when it's full or it's the end of the index).
	 */
	protected void flushFiles() throws IOException {
		if (!firstFileListBlock
				&& fileListBlock != null) {
			fileListBlock.flush();
			fileListBlock.write(indexOut, blockNum++);
			fileListBlock.clear();
			firstInBlock= true;
		}
	}
	/**
	 * Writes the current indexBlock on the disk and initialises it
	 * (when it's full or it's the end of the index).
	 */
	protected void flushWords() throws IOException {
		if (!firstInBlock 
				&& indexBlock != null) { // could have added a document without any indexed word, no block created yet
			indexBlock.flush();
			indexBlock.write(indexOut, blockNum++);
			indexBlock.clear();
			firstInBlock= true;
		}
	}
	/**
	 * @see IndexOutput#getDestination
	 */
	public Object getDestination() {
		return indexFile;
	}
	/**
	 * @see IndexOutput#open
	 */
	public void open() throws IOException {
		if (!opened) {
			summary= new IndexSummary();
			numFiles= 0;
			numWords= 0;
			blockNum= 1;
			firstInBlock= true;
			firstIndexBlock= true;
			firstFileListBlock= true;
			indexOut= new SafeRandomAccessFile(this.indexFile, "rw"); //$NON-NLS-1$
			opened= true;
		}
	}
}
