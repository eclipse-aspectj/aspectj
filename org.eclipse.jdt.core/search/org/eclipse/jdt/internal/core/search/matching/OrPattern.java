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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.IInfoConstants;

public class OrPattern extends SearchPattern {

	public SearchPattern leftPattern;
	public SearchPattern rightPattern;
public OrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
	super(-1, false); // values ignored for a OrPattern
		
	this.leftPattern = leftPattern;
	this.rightPattern = rightPattern;

	this.matchMode = Math.min(leftPattern.matchMode, rightPattern.matchMode);
	this.isCaseSensitive = leftPattern.isCaseSensitive || rightPattern.isCaseSensitive;
	this.needsResolve = leftPattern.needsResolve || rightPattern.needsResolve;
}
/**
 * see SearchPattern.decodedIndexEntry
 */
protected void decodeIndexEntry(IEntryResult entry) {

	// will never be directly invoked on a composite pattern
}
/**
 * see SearchPattern.feedIndexRequestor
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope)  throws IOException {
	// will never be directly invoked on a composite pattern
}
/**
 * see SearchPattern.findMatches
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	IIndexSearchRequestor orCombiner;
	if (detailLevel == IInfoConstants.NameInfo) {
		orCombiner = new OrNameCombiner(requestor);
	} else {
		orCombiner = new OrPathCombiner(requestor);
	}
	leftPattern.findIndexMatches(input, orCombiner, detailLevel, progressMonitor, scope);
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	rightPattern.findIndexMatches(input, orCombiner, detailLevel, progressMonitor, scope);
}
/**
 * see SearchPattern.indexEntryPrefix
 */
public char[] indexEntryPrefix() {

	// will never be directly invoked on a composite pattern
	return null;
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	return leftPattern.matchContainer()
			| rightPattern.matchContainer();
}
/**
 * @see SearchPattern#matchesBinary
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	return this.leftPattern.matchesBinary(binaryInfo, enclosingBinaryInfo) 
		|| this.rightPattern.matchesBinary(binaryInfo, enclosingBinaryInfo);
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {

	return this.leftPattern.matchIndexEntry()
			|| this.rightPattern.matchIndexEntry();
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	int leftLevel = this.leftPattern.matchLevel(reference, true);
	if (leftLevel == ACCURATE_MATCH || leftLevel == INACCURATE_MATCH) {
		this.leftPattern.matchReportReference(reference, element, accuracy, locator);
	} else {
		this.rightPattern.matchReportReference(reference, element, accuracy, locator);
	}
}
public String toString(){
	return this.leftPattern.toString() + "\n| " + this.rightPattern.toString(); //$NON-NLS-1$
}

/**
 * see SearchPattern.initializePolymorphicSearch
 */
public void initializePolymorphicSearch(MatchLocator locator, IProgressMonitor progressMonitor) {

	this.leftPattern.initializePolymorphicSearch(locator, progressMonitor);
	this.rightPattern.initializePolymorphicSearch(locator, progressMonitor);
}

/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	switch (this.leftPattern.matchLevel(node, resolve)) {
		case IMPOSSIBLE_MATCH:
			return this.rightPattern.matchLevel(node, resolve);
		case POSSIBLE_MATCH:
			return POSSIBLE_MATCH;
		case INACCURATE_MATCH:
			int rightLevel = this.rightPattern.matchLevel(node, resolve);
			if (rightLevel != IMPOSSIBLE_MATCH) {
				return rightLevel;
			} else {
				return INACCURATE_MATCH;
			}
		case ACCURATE_MATCH:
			return ACCURATE_MATCH;
		default:
			return IMPOSSIBLE_MATCH;
	}
}

/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	switch (this.leftPattern.matchLevel(binding)) {
		case IMPOSSIBLE_MATCH:
			return this.rightPattern.matchLevel(binding);
		case POSSIBLE_MATCH:
			return POSSIBLE_MATCH;
		case INACCURATE_MATCH:
			int rightLevel = this.rightPattern.matchLevel(binding);
			if (rightLevel != IMPOSSIBLE_MATCH) {
				return rightLevel;
			} else {
				return INACCURATE_MATCH;
			}
		case ACCURATE_MATCH:
			return ACCURATE_MATCH;
		default:
			return IMPOSSIBLE_MATCH;
	}
}
}
