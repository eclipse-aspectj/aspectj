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
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public abstract class MultipleSearchPattern extends AndPattern {

	protected char[] currentTag;
	public boolean foundAmbiguousIndexMatches = false;	
public MultipleSearchPattern(int matchMode, boolean isCaseSensitive) {
	super(matchMode, isCaseSensitive);
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	char[][] possibleTags = getPossibleTags();
	
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	/* narrow down a set of entries using prefix criteria */
	for (int i = 0, max = possibleTags.length; i < max; i++){
		currentTag = possibleTags[i];
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
protected abstract char[][] getPossibleTags();
}
