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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.core.index.IDocument;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.IIndexer;
import org.eclipse.jdt.internal.core.index.IQueryResult;

/**
 * An Index is used to create an index on the disk, and to make queries. It uses a set of 
 * indexers and a mergeFactory. The index fills an inMemoryIndex up 
 * to it reaches a certain size, and then merges it with a main index on the disk.
 * <br> <br>
 * The changes are only taken into account by the queries after a merge.
 */

public class Index implements IIndex {
	/**
	 * Maximum size of the index in memory.
	 */
	public static final int MAX_FOOTPRINT= 10000000;

	/**
	 * Index in memory, who is merged with mainIndex each times it 
	 * reaches a certain size.
	 */
	protected InMemoryIndex addsIndex;
	protected IndexInput addsIndexInput;

	/**
	 * State of the indexGenerator: addsIndex empty <=> MERGED, or
	 * addsIndex not empty <=> CAN_MERGE
	 */
	protected int state;

	/**
	 * Files removed form the addsIndex.
	 */
	protected Map removedInAdds;

	/**
	 * Files removed form the oldIndex.
	 */
	protected Map removedInOld;
	protected static final int CAN_MERGE= 0;
	protected static final int MERGED= 1;
	private File indexFile;

	/**
	 * String representation of this index.
	 */
	private String toString;
	public Index(File indexDirectory, boolean reuseExistingFile) throws IOException {
		this(indexDirectory,".index", reuseExistingFile); //$NON-NLS-1$
	}
	public Index(File indexDirectory, String indexName, boolean reuseExistingFile) throws IOException {
		super();
		state= MERGED;
		indexFile= new File(indexDirectory, indexName);
		initialize(reuseExistingFile);
	}
	public Index(String indexName, boolean reuseExistingFile) throws IOException {
		this(indexName, null, reuseExistingFile);
	}
	public Index(String indexName, String toString, boolean reuseExistingFile) throws IOException {
		super();
		state= MERGED;
		indexFile= new File(indexName);
		this.toString = toString;
		initialize(reuseExistingFile);
	}
	/**
	 * Indexes the given document, using the appropriate indexer registered in the indexerRegistry.
	 * If the document already exists in the index, it overrides the previous one. The changes will be 
	 * taken into account after a merge.
	 */
	public void add(IDocument document, IIndexer indexer) throws IOException {
		if (timeToMerge()) {
			merge();
		}
		IndexedFile indexedFile= addsIndex.getIndexedFile(document.getName());
		if (indexedFile != null /*&& removedInAdds.get(document.getName()) == null*/
			)
			remove(indexedFile, MergeFactory.ADDS_INDEX);
		IndexerOutput output= new IndexerOutput(addsIndex);
		indexer.index(document, output);
		state= CAN_MERGE;
	}
	/**
	 * Returns true if the index in memory is not empty, so 
	 * merge() can be called to fill the mainIndex with the files and words
	 * contained in the addsIndex. 
	 */
	protected boolean canMerge() {
		return state == CAN_MERGE;
	}
	/**
	 * Initialises the indexGenerator.
	 */
	public void empty() throws IOException {

		if (indexFile.exists()){
			indexFile.delete();
			//initialisation of mainIndex
			InMemoryIndex mainIndex= new InMemoryIndex();
			IndexOutput mainIndexOutput= new BlocksIndexOutput(indexFile);
			if (!indexFile.exists())
				mainIndex.save(mainIndexOutput);
		}

		//initialisation of addsIndex
		addsIndex= new InMemoryIndex();
		addsIndexInput= new SimpleIndexInput(addsIndex);

		//vectors who keep track of the removed Files
		removedInAdds= new HashMap(11);
		removedInOld= new HashMap(11);
	}
	/**
	 * @see IIndex#getIndexFile
	 */
	public File getIndexFile() {
		return indexFile;
	}
	/**
	 * @see IIndex#getNumDocuments
	 */
	public int getNumDocuments() throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			return input.getNumFiles();
		} finally {
			input.close();
		}		
	}
	/**
	 * @see IIndex#getNumWords
	 */
	public int getNumWords() throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			return input.getNumWords();
		} finally {
			input.close();
		}		
	}
	/**
	 * Returns the path corresponding to a given document number
	 */
	public String getPath(int documentNumber) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			IndexedFile file = input.getIndexedFile(documentNumber);
			if (file == null) return null;
			return file.getPath();
		} finally {
			input.close();
		}		
	}
	/**
	 * see IIndex.hasChanged
	 */
	public boolean hasChanged() {
		return canMerge();
	}
	/**
	 * Initialises the indexGenerator.
	 */
	public void initialize(boolean reuseExistingFile) throws IOException {
		
		//initialisation of addsIndex
		addsIndex= new InMemoryIndex();
		addsIndexInput= new SimpleIndexInput(addsIndex);

		//vectors who keep track of the removed Files
		removedInAdds= new HashMap(11);
		removedInOld= new HashMap(11);

		// check whether existing index file can be read
		if (reuseExistingFile && indexFile.exists()) {
			IndexInput mainIndexInput= new BlocksIndexInput(indexFile);
			try {
				mainIndexInput.open();
			} catch(IOException e) {
				BlocksIndexInput input = (BlocksIndexInput)mainIndexInput;
				try {
					input.opened = true;
					input.close();
				} finally {
					input.opened = false;
				}
				indexFile.delete();
				if (org.eclipse.jdt.internal.core.search.Util.bind("exception.wrongFormat").equals(e.getMessage())) { //$NON-NLS-1$
					InMemoryIndex mainIndex= new InMemoryIndex();			
					IndexOutput mainIndexOutput= new BlocksIndexOutput(indexFile);
					mainIndex.save(mainIndexOutput);
				} else {
					mainIndexInput = null;
					throw e;
				}
			}
			mainIndexInput.close();
		} else {
			InMemoryIndex mainIndex= new InMemoryIndex();			
			IndexOutput mainIndexOutput= new BlocksIndexOutput(indexFile);
			mainIndex.save(mainIndexOutput);
		}
	}
	/**
	 * Merges the in memory index and the index on the disk, and saves the results on the disk.
	 */
	protected void merge() throws IOException {
		//System.out.println("merge");

		//initialisation of tempIndex
		File tempFile= new File(indexFile.getAbsolutePath() + "TempVA"); //$NON-NLS-1$

		IndexInput mainIndexInput= new BlocksIndexInput(indexFile);
		BlocksIndexOutput tempIndexOutput= new BlocksIndexOutput(tempFile);

		try {
			//invoke a mergeFactory
			new MergeFactory(
				mainIndexInput, 
				addsIndexInput, 
				tempIndexOutput, 
				removedInOld, 
				removedInAdds).merge();
			
			//rename the file created to become the main index
			File mainIndexFile= (File) mainIndexInput.getSource();
			File tempIndexFile= (File) tempIndexOutput.getDestination();
			mainIndexFile.delete();
			tempIndexFile.renameTo(mainIndexFile);
		} finally {		
			//initialise remove vectors and addsindex, and change the state
			removedInAdds.clear();
			removedInOld.clear();
			addsIndex.init();
			addsIndexInput= new SimpleIndexInput(addsIndex);
			state= MERGED;
		}
	}
	/**
	 * @see IIndex#query
	 */
	public IQueryResult[] query(String word) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			return input.query(word);
		} finally {
			input.close();
		}
	}
	public IEntryResult[] queryEntries(char[] prefix) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			return input.queryEntriesPrefixedBy(prefix);
		} finally {
			input.close();
		}
	}
	/**
	 * @see IIndex#queryInDocumentNames
	 */
	public IQueryResult[] queryInDocumentNames(String word) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			return input.queryInDocumentNames(word);
		} finally {
			input.close();
		}
	}
	/**
	 * @see IIndex#queryPrefix
	 */
	public IQueryResult[] queryPrefix(char[] prefix) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			return input.queryFilesReferringToPrefix(prefix);
		} finally {
			input.close();
		}
	}
	/**
	 * @see IIndex#remove
	 */
	public void remove(String documentName) throws IOException {
		IndexedFile file= addsIndex.getIndexedFile(documentName);
		if (file != null) {
			//the file is in the adds Index, we remove it from this one
			Int lastRemoved= (Int) removedInAdds.get(documentName);
			if (lastRemoved != null) {
				int fileNum= file.getFileNumber();
				if (lastRemoved.value < fileNum)
					lastRemoved.value= fileNum;
			} else
				removedInAdds.put(documentName, new Int(file.getFileNumber()));
		} else {
			//we remove the file from the old index
			removedInOld.put(documentName, new Int(1));
		}
		state= CAN_MERGE;
	}
	/**
	 * Removes the given document from the given index (MergeFactory.ADDS_INDEX for the
	 * in memory index, MergeFactory.OLD_INDEX for the index on the disk).
	 */
	protected void remove(IndexedFile file, int index) throws IOException {
		String name= file.getPath();
		if (index == MergeFactory.ADDS_INDEX) {
			Int lastRemoved= (Int) removedInAdds.get(name);
			if (lastRemoved != null) {
				if (lastRemoved.value < file.getFileNumber())
					lastRemoved.value= file.getFileNumber();
			} else
				removedInAdds.put(name, new Int(file.getFileNumber()));
		} else if (index == MergeFactory.OLD_INDEX)
			removedInOld.put(name, new Int(1));
		else
			throw new Error();
		state= CAN_MERGE;
	}
	/**
	 * @see IIndex#save
	 */
	public void save() throws IOException {
		if (canMerge())
			merge();
	}
	/**
	 * Returns true if the in memory index reaches a critical size, 
	 * to merge it with the index on the disk.
	 */
	protected boolean timeToMerge() {
		return (addsIndex.getFootprint() >= MAX_FOOTPRINT);
	}
public String toString() {
	if (this.toString == null) return super.toString();
	return this.toString;
}
}
