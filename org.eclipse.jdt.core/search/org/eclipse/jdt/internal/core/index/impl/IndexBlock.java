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

import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * An indexBlock stores wordEntries.
 */

public abstract class IndexBlock extends Block {

	public IndexBlock(int blockSize) {
		super(blockSize);
	}
	/**
	 * Adds the given wordEntry to the indexBlock.
	 */

	public abstract boolean addEntry(WordEntry entry);
	/**
	 * @see Block#clear()
	 */
	public void clear() {
		reset();
		super.clear();
	}
	public WordEntry findEntryMatching(char[] pattern, boolean isCaseSensitive) {
		reset();
		WordEntry entry= new WordEntry();
		while (nextEntry(entry)) {
			if (CharOperation.match(pattern, entry.getWord(), isCaseSensitive)) {
				return entry;
			}
		}
		return null;
	}
	public WordEntry findEntryPrefixedBy(char[] word, boolean isCaseSensitive) {
		reset();
		WordEntry entry= new WordEntry();
		while (nextEntry(entry)) {
			if (CharOperation.prefixEquals(entry.getWord(), word, isCaseSensitive)) {
				return entry;
			}
		}
		return null;
	}
	public WordEntry findExactEntry(char[] word) {
		reset();
		WordEntry entry= new WordEntry();
		while (nextEntry(entry)) {
			if (CharOperation.equals(entry.getWord(), word)) {
				return entry;
			}
		}
		return null;
	}
	/**
	 * Returns whether the block is empty or not (if it doesn't contain any wordEntry).
	 */
	public abstract boolean isEmpty();

	/**
	 * Finds the next wordEntry and stores it in the given entry.
	 */
	public abstract boolean nextEntry(WordEntry entry);

	public void reset() {
	}
}
