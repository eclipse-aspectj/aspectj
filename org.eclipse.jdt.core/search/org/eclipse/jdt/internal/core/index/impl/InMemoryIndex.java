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

import org.eclipse.jdt.internal.core.index.IDocument;

/**
 * This index stores the document names in an <code>ObjectVector</code>, and the words in
 * an <code>HashtableOfObjects</code>.
 */

public class InMemoryIndex {

	/**
	 * hashtable of WordEntrys = words+numbers of the files they appear in.
	 */
	protected WordEntryHashedArray words;

	/**
	 * List of IndexedFiles = file name + a unique number.
	 */
	protected IndexedFileHashedArray files;

	/**
	 * Size of the index.
	 */
	protected long footprint;

	private WordEntry[] sortedWordEntries;
	private IndexedFile[] sortedFiles;
	public InMemoryIndex() {
		init();
	}

	public IndexedFile addDocument(IDocument document) {
		IndexedFile indexedFile= this.files.add(document);
		this.footprint += indexedFile.footprint() + 4;
		this.sortedFiles = null;
		return indexedFile;
	}
	/**
	 * Adds the references of the word to the index (reference = number of the file the word belongs to).
	 */
	protected void addRef(char[] word, int[] references) {
		int size= references.length;
		int i= 0;
		while (i < size) {
			if (references[i] != 0)
				addRef(word, references[i]);
			i++;
		}
	}
	/**
	 * Looks if the word already exists in the index and add the fileNum to this word.
	 * If the word does not exist, it adds it in the index.
	 */
	protected void addRef(char[] word, int fileNum) {
		word= preprocessWord(word);
		WordEntry entry= (WordEntry) this.words.get(word);
		if (entry == null) {
			entry= new WordEntry(word);
			entry.addRef(fileNum);
			this.words.add(entry);
			this.sortedWordEntries= null;
			this.footprint += entry.footprint();
		} else {
			this.footprint += entry.addRef(fileNum);
		}
	}

	public void addRef(IndexedFile indexedFile, char[] word) {
		addRef(word, indexedFile.getFileNumber());
	}

	public void addRef(IndexedFile indexedFile, String word) {
		addRef(word.toCharArray(), indexedFile.getFileNumber());
	}

	/**
	 * Returns the footprint of the index.
	 */
	public long getFootprint() {
		return this.footprint;
	}

	/**
	 * Returns the indexed file with the given path, or null if such file does not exist.
	 */
	public IndexedFile getIndexedFile(String path) {
		return files.get(path);
	}

	/**
	 * @see IIndex#getNumDocuments()
	 */
	public int getNumFiles() {
		return files.size();
	}

	/**
	 * @see IIndex#getNumWords()
	 */
	public int getNumWords() {
		return words.elementSize;
	}
	
	/**
	 * Returns the words contained in the hashtable of words, sorted by alphabetical order.
	 */
	protected IndexedFile[] getSortedFiles() {
		if (this.sortedFiles == null) {
			IndexedFile[] indexedFiles= files.asArray();
			Util.sort(indexedFiles);
			this.sortedFiles= indexedFiles;
		}
		return this.sortedFiles;
	}
	/**
	 * Returns the word entries contained in the hashtable of words, sorted by alphabetical order.
	 */
	protected WordEntry[] getSortedWordEntries() {
		if (this.sortedWordEntries == null) {
			WordEntry[] words= this.words.asArray();
			Util.sort(words);
			this.sortedWordEntries= words;
		}
		return this.sortedWordEntries;
	}
	/**
	 * Returns the word entry corresponding to the given word.
	 */
	protected WordEntry getWordEntry(char[] word) {
		return (WordEntry) words.get(word);
	}
	/**
	 * Initialises the fields of the index
	 */
	public void init() {
		words= new WordEntryHashedArray(501);
		files= new IndexedFileHashedArray(101);
		footprint= 0;
		sortedWordEntries= null;
		sortedFiles= null;
	}
	protected char[] preprocessWord(char[] word) {
		if (word.length > 255) {
			System.arraycopy(word, 0, word= new char[255], 0, 255);
		}
		return word;
	}
	/**
	 * Saves the index in the given file.
	 * Structure of the saved Index :
	 *   - IndexedFiles in sorted order.
	 *		+ example: 
	 *			"c:/com/Test.java 1"
	 *			"c:/com/UI.java 2"
	 *   - References with the words in sorted order
	 *		+ example: 
	 *			"classDecl/Test 1"
	 *			"classDecl/UI 2"
	 *			"ref/String 1 2"
	 */

	public void save(File file) throws IOException {
		BlocksIndexOutput output= new BlocksIndexOutput(file);
		save(output);
	}
	/**
	 * Saves the index in the given IndexOutput.
	 * Structure of the saved Index :
	 *   - IndexedFiles in sorted order.
	 *		+ example: 
	 *			"c:/com/Test.java 1"
	 *			"c:/com/UI.java 2"
	 *   - References with the words in sorted order
	 *		+ example: 
	 *			"classDecl/Test 1"
	 *			"classDecl/UI 2"
	 *			"ref/String 1 2"
	 */

	protected void save(IndexOutput output) throws IOException {
		boolean ok= false;
		try {
			output.open();
			IndexedFile[] indexedFiles= files.asArray();
			for (int i= 0, length = indexedFiles.length; i < length; ++i)
				output.addFile(indexedFiles[i]); // written out in order BUT not alphabetical
			getSortedWordEntries(); // init the slot
			for (int i= 0, numWords= sortedWordEntries.length; i < numWords; ++i)
				output.addWord(sortedWordEntries[i]);
			output.flush();
			output.close();
			ok= true;
		} finally {
			if (!ok && output != null)
				output.close();
		}
	}
}
