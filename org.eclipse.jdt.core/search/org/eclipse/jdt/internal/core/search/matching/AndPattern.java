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
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

/**
 * Query the index multiple times and do an 'and' on the results.
 */
public abstract class AndPattern extends SearchPattern {
public AndPattern(int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	
	/* narrow down a set of entries using prefix criteria */
	long[] potentialRefs = null;
	int maxRefs = -1;
	this.resetQuery();
	do {
		IEntryResult[] entries = input.queryEntriesPrefixedBy(indexEntryPrefix());
		if (entries == null) break;

		int numFiles = input.getNumFiles();
		long[] references = null;
		int referencesLength = -1;
		for (int i = 0, max = entries.length; i < max; i++){

			if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

			/* retrieve and decode entry */	
			IEntryResult entry = entries[i];
			decodeIndexEntry(entry);
			if (matchIndexEntry()) {
				/* accumulate references in an array of bits : 1 if the reference is present, 0 otherwise */
				int[] fileReferences = entry.getFileReferences();
				for (int j = 0, refLength = fileReferences.length; j < refLength; j++) {
					int fileReference = fileReferences[j];
					int vectorIndex = fileReference / 64; // a long has 64 bits
					if (references == null) {
						referencesLength = (numFiles / 64) + 1;
						references = new long[referencesLength];
					}
					long mask = 1L << (fileReference % 64);
					references[vectorIndex] |= mask;
				}
			}
		}
		
		/* only select entries which actually match the entire search pattern */
		if (references == null) {
			/* no references */
			return;
		} else {
			if (potentialRefs == null) {
				/* first query : these are the potential references */
				potentialRefs = references;
				maxRefs = numFiles;
			} else {
				/* eliminate potential references that don't match the current references */
				for (int i = 0, length = references.length; i < length; i++) {
					if (i < potentialRefs.length) {
						potentialRefs[i] &= references[i];
					} else {
						potentialRefs[i] = 0;
					}
				}
			}				
		}
	} while (this.hasNextQuery());

	/* report potential references that remain */
	if (potentialRefs != null) {
		int[] refs = new int[maxRefs];
		int refsLength = 0;
		for (int reference = 1; reference <= maxRefs; reference++) {
			int vectorIndex = reference / 64; // a long has 64 bits
			if ((potentialRefs[vectorIndex] & (1L << (reference % 64))) != 0) {
				refs[refsLength++] = reference;
			}
						
		}
		System.arraycopy(refs, 0, refs = new int[refsLength], 0, refsLength);
		this.feedIndexRequestor(requestor, detailLevel, refs, input, scope);
	}
}
/**
 * Returns whether another query must be done.
 */
protected abstract boolean hasNextQuery();
/**
 * Resets the query and prepares this pattern to be queried.
 */
protected abstract void resetQuery();
}
