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
package org.eclipse.jdt.internal.core.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.jdt.internal.core.search.matching.SearchPattern;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

public class SubTypeSearchJob extends PatternSearchJob {

	Map inputs = new HashMap(5);
public SubTypeSearchJob(SearchPattern pattern, IJavaSearchScope scope, int detailLevel, IIndexSearchRequestor requestor, IndexManager indexManager) {
	super(pattern, scope, detailLevel, requestor, indexManager);
}
public SubTypeSearchJob(SearchPattern pattern, IJavaSearchScope scope, IJavaElement focus, int detailLevel, IIndexSearchRequestor requestor, org.eclipse.jdt.internal.core.search.indexing.IndexManager indexManager) {
	super(pattern, scope, focus, detailLevel, requestor, indexManager);
}
public void closeAll(){

	Iterator openedInputs = inputs.values().iterator();
	while (openedInputs.hasNext()){
		IndexInput input = (IndexInput) openedInputs.next();
		try {
			input.close();
		} catch(IOException e){
		}
	} 
}
/**
 * execute method comment.
 */
public boolean search(IIndex index, IProgressMonitor progressMonitor) {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	if (index == null) return COMPLETE;		
	ReadWriteMonitor monitor = indexManager.getMonitorFor(index);
	if (monitor == null) return COMPLETE; // index got deleted since acquired
	try {
		monitor.enterRead(); // ask permission to read

		/* if index has changed, commit these before querying */
		if (index.hasChanged()){
			try {
				monitor.exitRead(); // free read lock
				monitor.enterWrite(); // ask permission to write
				if (IndexManager.VERBOSE) 
					JobManager.verbose("-> merging index " + index.getIndexFile()); //$NON-NLS-1$
				index.save();
			} catch(IOException e){
				return FAILED;
			} finally {
				monitor.exitWriteEnterRead(); // finished writing and reacquire read permission
			}
		}
		long start = System.currentTimeMillis();

		IndexInput input;
		if ((input = (IndexInput) inputs.get(index)) == null){
			input = new BlocksIndexInput(index.getIndexFile());
			input.open();
			inputs.put(index, input);
			//System.out.println("Acquiring INPUT for "+index);
		}
		pattern.findIndexMatches(input, requestor, detailLevel, progressMonitor, this.scope);
		executionTime += System.currentTimeMillis() - start;
		return COMPLETE;
	} catch(IOException e){
		return FAILED;
	} finally {
		monitor.exitRead(); // finished reading
	}
}
}
