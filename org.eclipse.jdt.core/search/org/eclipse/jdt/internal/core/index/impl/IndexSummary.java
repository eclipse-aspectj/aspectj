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
import java.util.ArrayList;

import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * An indexSummary is used when saving an index into a BlocksIndexOuput or 
 * reading it from a BlocksIndexInput. It contains basic informations about 
 * an index: first files/words in each block, number of files/words.
 */

public class IndexSummary {
	/**
	 * First file for each block.
	 */
	protected ArrayList firstFilesInBlocks= new ArrayList();

	/**
	 * First word for each block.
	 */
	protected ArrayList firstWordsInBlocks= new ArrayList();

	/**
	 * Number of files in the index.
	 */
	protected int numFiles;

	/**
	 * Number of words in the index.
	 */
	protected int numWords;

	static class FirstFileInBlock {
		IndexedFile indexedFile;
		int blockNum;
	}

	static class FirstWordInBlock {
		char[] word;
		int blockNum;
		public String toString(){
			return "FirstWordInBlock: " + new String(word) + ", blockNum: " + blockNum; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected int firstWordBlockNum;
	protected boolean firstWordAdded= true;
	/**
	 * Adds the given file as the first file for the given Block number. 
	 */
	public void addFirstFileInBlock(IndexedFile indexedFile, int blockNum) {
		FirstFileInBlock entry= new FirstFileInBlock();
		entry.indexedFile= indexedFile;
		entry.blockNum= blockNum;
		firstFilesInBlocks.add(entry);
	}
	/**
	 * Adds the given word as the first word for the given Block number. 
	 */
	public void addFirstWordInBlock(char[] word, int blockNum) {
		if (firstWordAdded) {
			firstWordBlockNum= blockNum;
			firstWordAdded= false;
		}
		FirstWordInBlock entry= new FirstWordInBlock();
		entry.word= word;
		entry.blockNum= blockNum;
		firstWordsInBlocks.add(entry);
	}
	/**
	 * Returns the numbers of all the blocks
	 */
	public int[] getAllBlockNums() {

		int max = firstWordsInBlocks.size();
		int[] blockNums = new int[max];
		for (int i = 0; i < max; i++){
			blockNums[i] = ((FirstWordInBlock)firstWordsInBlocks.get(i)).blockNum;
		}
		return blockNums;
	}
public int getBlockNum(int blockLocation) {
	return ((FirstWordInBlock) firstWordsInBlocks.get(blockLocation)).blockNum;
}
	/**
	 * Returns the number of the Block containing the file with the given number. 
	 */
	public int getBlockNumForFileNum(int fileNum) {
		int min= 0;
		int max= firstFilesInBlocks.size() - 1;
		while (min <= max) {
			int mid= (min + max) / 2;
			FirstFileInBlock entry= (FirstFileInBlock) firstFilesInBlocks.get(mid);
			int compare= fileNum - entry.indexedFile.getFileNumber();
			if (compare == 0)
				return entry.blockNum;
			if (compare < 0)
				max= mid - 1;
			else
				min= mid + 1;
		}
		if (max < 0)
			return -1;
		FirstFileInBlock entry= (FirstFileInBlock) firstFilesInBlocks.get(max);
		return entry.blockNum;
	}
	/**
	 * Returns the number of the Block containing the given word. 
	 */
	public int getBlockNumForWord(char[] word) {
		int min= 0;
		int max= firstWordsInBlocks.size() - 1;
		while (min <= max) {
			int mid= (min + max) / 2;
			FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(mid);
			int compare= Util.compare(word, entry.word);
			if (compare == 0)
				return entry.blockNum;
			if (compare < 0)
				max= mid - 1;
			else
				min= mid + 1;
		}
		if (max < 0)
			return -1;
		FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(max);
		return entry.blockNum;
	}
	public int[] getBlockNumsForPrefix(char[] prefix) {
		int min= 0;
		int size= firstWordsInBlocks.size();
		int max=  size - 1;
		int match= -1;
		while (min <= max && match < 0) {
			int mid= (min + max) / 2;
			FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(mid);
			int compare= Util.startsWith(entry.word, prefix);
			if (compare == 0) {
				match= mid;
				break;
			}	
			if (compare >= 0)
				max= mid - 1;
			else
				min= mid + 1;
		}
		if (max < 0)
			return new int[0];
			
		if (match < 0)
			match= max;
		
		int firstBlock= match - 1;
		// Look if previous blocks are affected
		for (; firstBlock >= 0; firstBlock--) {
			FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(firstBlock);
			if (!CharOperation.startsWith(entry.word, prefix))
				break;
		}
		if (firstBlock < 0)
			firstBlock= 0;	
		
		// Look if next blocks are affected
		int firstNotIncludedBlock= match + 1;
		for (; firstNotIncludedBlock < size; firstNotIncludedBlock++) {
			FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(firstNotIncludedBlock);
			if (!CharOperation.startsWith(entry.word, prefix))
				break;
		}
		
		int numberOfBlocks= firstNotIncludedBlock - firstBlock;
		int[] result= new int[numberOfBlocks];
		int pos= firstBlock;
		for (int i= 0; i < numberOfBlocks; i++, pos++) {
			FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(pos);
			result[i]= entry.blockNum;
		}
		return result;
	}
public int getFirstBlockLocationForPrefix(char[] prefix) {
	int min = 0;
	int size = firstWordsInBlocks.size();
	int max = size - 1;
	int match = -1;
	while (min <= max) {
		int mid = (min + max) / 2;
		FirstWordInBlock entry = (FirstWordInBlock) firstWordsInBlocks.get(mid);
		int compare = Util.startsWith(entry.word, prefix);
		if (compare == 0) {
			match = mid;
			break;
		}
		if (compare >= 0) {
			max = mid - 1;
		} else {
			match = mid; // not perfect match, but could be inside
			min = mid + 1;
		}
	}
	if (max < 0) return -1;

	// no match at all, might be some matching entries inside max block
	if (match < 0){
		match = max;
	} else {
		// look for possible matches inside previous blocks
		while (match > 0){
			FirstWordInBlock entry = (FirstWordInBlock) firstWordsInBlocks.get(match);
			if (!CharOperation.startsWith(entry.word, prefix)){
				break;
			}
			match--;
		}
	}
	return match;
}
	/**
	 * Returns the number of the first IndexBlock (containing words).
	 */
	public int getFirstWordBlockNum() {
		return firstWordBlockNum;
	}
/** 
 * Blocks are contiguous, so the next one is a potential candidate if its first word starts with
 * the given prefix
 */
public int getNextBlockLocationForPrefix(char[] prefix, int blockLoc) {
	if (++blockLoc < firstWordsInBlocks.size()){
		FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(blockLoc);
		if (CharOperation.startsWith(entry.word, prefix)) return blockLoc;
	}
	return -1;
}
	/**
	 * Returns the number of files contained in the index.
	 */
	public int getNumFiles() {
		return numFiles;
	}
	/**
	 * Returns the number of words contained in the index.
	 */
	public int getNumWords() {
		return numWords;
	}
	/**
	 * Loads the summary in memory.
	 */
	public void read(RandomAccessFile raf) throws IOException {
		numFiles= raf.readInt();
		numWords= raf.readInt();
		firstWordBlockNum= raf.readInt();
		int numFirstFiles= raf.readInt();
		for (int i= 0; i < numFirstFiles; ++i) {
			FirstFileInBlock entry= new FirstFileInBlock();
			String path= raf.readUTF();
			int fileNum= raf.readInt();
			entry.indexedFile= new IndexedFile(path, fileNum);
			entry.blockNum= raf.readInt();
			firstFilesInBlocks.add(entry);
		}
		int numFirstWords= raf.readInt();
		for (int i= 0; i < numFirstWords; ++i) {
			FirstWordInBlock entry= new FirstWordInBlock();
			entry.word= raf.readUTF().toCharArray();
			entry.blockNum= raf.readInt();
			firstWordsInBlocks.add(entry);
		}
	}
	/**
	 * Sets the number of files of the index.
	 */

	public void setNumFiles(int numFiles) {
		this.numFiles= numFiles;
	}
	/**
	 * Sets the number of words of the index.
	 */

	public void setNumWords(int numWords) {
		this.numWords= numWords;
	}
	/**
	 * Saves the summary on the disk.
	 */
	public void write(RandomAccessFile raf) throws IOException {
		raf.writeInt(numFiles);
		raf.writeInt(numWords);
		raf.writeInt(firstWordBlockNum);
		raf.writeInt(firstFilesInBlocks.size());
		for (int i= 0, size= firstFilesInBlocks.size(); i < size; ++i) {
			FirstFileInBlock entry= (FirstFileInBlock) firstFilesInBlocks.get(i);
			raf.writeUTF(entry.indexedFile.getPath());
			raf.writeInt(entry.indexedFile.getFileNumber());
			raf.writeInt(entry.blockNum);
		}
		raf.writeInt(firstWordsInBlocks.size());
		for (int i= 0, size= firstWordsInBlocks.size(); i < size; ++i) {
			FirstWordInBlock entry= (FirstWordInBlock) firstWordsInBlocks.get(i);
			raf.writeUTF(new String(entry.word));
			raf.writeInt(entry.blockNum);
		}
	}
}
