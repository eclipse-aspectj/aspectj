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
import java.util.Map;

/**
 * A mergeFactory is used to merge 2 indexes into one. One of the indexes 
 * (oldIndex) is on the disk and the other(addsIndex) is in memory.
 * The merge respects the following rules: <br>
 *   - The files are sorted in alphabetical order;<br>
 *   - if a file is in oldIndex and addsIndex, the one which is added 
 * is the one in the addsIndex.<br>
 */
public class MergeFactory {
	/**
	 * Input on the addsIndex.
	 */
	protected IndexInput addsInput;

	/**
	 * Input on the oldIndex. 
	 */
	protected IndexInput oldInput;

	/**
	 * Output to write the result of the merge in.
	 */
	protected BlocksIndexOutput mergeOutput;

	/**
	 * Files removed from oldIndex. 
	 */
	protected Map removedInOld;

	/**
	 * Files removed from addsIndex. 
	 */
	protected Map removedInAdds;
	protected int[] mappingOld;
	protected int[] mappingAdds;
	public static final int ADDS_INDEX= 0;
	public static final int OLD_INDEX= 1;
	/**
	 * MergeFactory constructor comment.
	 * @param directory java.io.File
	 */
	public MergeFactory(IndexInput oldIndexInput, IndexInput addsIndexInput, BlocksIndexOutput mergeIndexOutput, Map removedInOld, Map removedInAdds) {
		oldInput= oldIndexInput;
		addsInput= addsIndexInput;
		mergeOutput= mergeIndexOutput;
		this.removedInOld= removedInOld;
		this.removedInAdds= removedInAdds;
	}
	/**
	 * Initialise the merge.
	 */
	protected void init() {
		mappingOld= new int[oldInput.getNumFiles() + 1];
		mappingAdds= new int[addsInput.getNumFiles() + 1];

	}
	/**
	 * Merges the 2 indexes into a new one on the disk.
	 */
	public void merge() throws IOException {
		try {
			//init
			addsInput.open();
			oldInput.open();
			mergeOutput.open();
			init();
			//merge
			//findChanges();
			mergeFiles();
			mergeReferences();
			mergeOutput.flush();
		} finally {
			//closes everything
			oldInput.close();
			addsInput.close();
			mergeOutput.close();
		}
	}
	/**
	 * Merges the files of the 2 indexes in the new index, removes the files
	 * to be removed, and records the changes made to propagate them to the 
	 * word references.
	 */

	protected void mergeFiles() throws IOException {
		int positionInMerge= 1;
		int compare;
		while (oldInput.hasMoreFiles() || addsInput.hasMoreFiles()) {
			IndexedFile file1= oldInput.getCurrentFile();
			IndexedFile file2= addsInput.getCurrentFile();

			//if the file has been removed we don't take it into account
			while (file1 != null && wasRemoved(file1, OLD_INDEX)) {
				oldInput.moveToNextFile();
				file1= oldInput.getCurrentFile();
			}
			while (file2 != null && wasRemoved(file2, ADDS_INDEX)) {
				addsInput.moveToNextFile();
				file2= addsInput.getCurrentFile();
			}

			//the addsIndex was empty, we just removed files from the oldIndex
			if (file1 == null && file2 == null)
				break;

			//test if we reached the end of one the 2 index
			if (file1 == null)
				compare= 1;
			else if (file2 == null)
				compare= -1;
			else
				compare= file1.getPath().compareTo(file2.getPath());

			//records the changes to Make
			if (compare == 0) {
				//the file has been modified: 
				//we remove it from the oldIndex and add it to the addsIndex
				removeFile(file1, OLD_INDEX);
				mappingAdds[file2.getFileNumber()]= positionInMerge;
				file1.setFileNumber(positionInMerge);
				mergeOutput.addFile(file1);
				oldInput.moveToNextFile();
				addsInput.moveToNextFile();
			} else if (compare < 0) {
				mappingOld[file1.getFileNumber()]= positionInMerge;
				file1.setFileNumber(positionInMerge);
				mergeOutput.addFile(file1);
				oldInput.moveToNextFile();
			} else {
				mappingAdds[file2.getFileNumber()]= positionInMerge;
				file2.setFileNumber(positionInMerge);
				mergeOutput.addFile(file2);
				addsInput.moveToNextFile();
			}
			positionInMerge++;
		}
		mergeOutput.flushFiles();		
	}
	/**
	 * Merges the files of the 2 indexes in the new index, according to the changes
	 * recorded during mergeFiles().
	 */
	protected void mergeReferences() throws IOException {
		int compare;
		while (oldInput.hasMoreWords() || addsInput.hasMoreWords()) {
			WordEntry word1= oldInput.getCurrentWordEntry();
			WordEntry word2= addsInput.getCurrentWordEntry();

			if (word1 == null && word2 == null)
				break;
			
			if (word1 == null)
				compare= 1;
			else if (word2 == null)
				compare= -1;
			else
				compare= Util.compare(word1.getWord(), word2.getWord());
			if (compare < 0) {
				word1.mapRefs(mappingOld);
				mergeOutput.addWord(word1);
				oldInput.moveToNextWordEntry();
			} else if (compare > 0) {
				word2.mapRefs(mappingAdds);
				mergeOutput.addWord(word2);
				addsInput.moveToNextWordEntry();
			} else {
				word1.mapRefs(mappingOld);
				word2.mapRefs(mappingAdds);
				word1.addRefs(word2.getRefs());
				mergeOutput.addWord(word1);
				addsInput.moveToNextWordEntry();
				oldInput.moveToNextWordEntry();
			}
		}
		mergeOutput.flushWords();
	}
	/**
	 * Records the deletion of one file.
	 */
	protected void removeFile(IndexedFile file, int index) {
		if (index == OLD_INDEX)
			mappingOld[file.getFileNumber()]= -1;
		else
			mappingAdds[file.getFileNumber()]= -1;
	}
	/**
	 * Returns whether the given file has to be removed from the given index
	 * (ADDS_INDEX or OLD_INDEX). If it has to be removed, the mergeFactory 
	 * deletes it and records the changes. 
	 */

	protected boolean wasRemoved(IndexedFile indexedFile, int index) {
		String path= indexedFile.getPath();
		if (index == OLD_INDEX) {
			if (removedInOld.remove(path) != null) {
				mappingOld[indexedFile.getFileNumber()]= -1;
				return true;
			}
		} else if (index == ADDS_INDEX) {
			Int lastRemoved= (Int) removedInAdds.get(path);
			if (lastRemoved != null) {
				int fileNum= indexedFile.getFileNumber();
				if (lastRemoved.value >= fileNum) {
					mappingAdds[fileNum]= -1;
					//if (lastRemoved.value == fileNum) // ONLY if files in sorted order for names AND fileNums
					//removedInAdds.remove(path);
					return true;
				}
			}
		}
		return false;
	}
}
