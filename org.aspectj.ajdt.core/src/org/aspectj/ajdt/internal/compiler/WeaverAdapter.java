/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.aspectj.weaver.IClassFileProvider;
import org.aspectj.weaver.IWeaveRequestor;
import org.aspectj.weaver.bcel.UnwovenClassFile;

/**
 * @author colyer
 * This class provides the weaver with a source of class files to weave (via the 
 * iterator and IClassFileProvider interfaces). It receives results back from the
 * weaver via the IWeaveRequestor interface.
 */
public class WeaverAdapter implements IClassFileProvider, IWeaveRequestor, Iterator {
	
	private AjCompilerAdapter compilerAdapter;
	private Iterator resultIterator;
	private int classFileIndex = 0;
	private InterimCompilationResult nowProcessing;
	private InterimCompilationResult lastReturnedResult;
	private WeaverMessageHandler weaverMessageHandler;
	private boolean finalPhase = false;
	
	
	public WeaverAdapter(AjCompilerAdapter forCompiler,
						 WeaverMessageHandler weaverMessageHandler) { 
		this.compilerAdapter = forCompiler; 
		this.weaverMessageHandler = weaverMessageHandler;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.IClassFileProvider#getClassFileIterator()
	 */
	public Iterator getClassFileIterator() {
		classFileIndex = 0;
		nowProcessing = null;
		lastReturnedResult = null;
		resultIterator = compilerAdapter.resultsPendingWeave.iterator();
		return this;
	}
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.IClassFileProvider#getRequestor()
	 */
	public IWeaveRequestor getRequestor() {
		return this;
	}
	
	// Iteration
	// ================================================================
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (nowProcessing == null) {
			if (!resultIterator.hasNext()) return false;
			nowProcessing = (InterimCompilationResult) resultIterator.next();
			classFileIndex = 0;
		}
		while (nowProcessing.unwovenClassFiles().length == 0 ) {
			if (!resultIterator.hasNext()) return false;
			nowProcessing = (InterimCompilationResult) resultIterator.next();
		}
		if (classFileIndex < nowProcessing.unwovenClassFiles().length) {
			return true;
		} else {
			classFileIndex = 0;
			if (!resultIterator.hasNext()) return false;
			nowProcessing = (InterimCompilationResult) resultIterator.next();
			while (nowProcessing.unwovenClassFiles().length == 0 ) {
				if (!resultIterator.hasNext()) return false;
				nowProcessing = (InterimCompilationResult) resultIterator.next();
			} 
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		if (!hasNext()) return null;  // sets up indices correctly
		if (finalPhase) {
			if ((lastReturnedResult != null) && (lastReturnedResult != nowProcessing)) {
				// we're done with the lastReturnedResult
				finishedWith(lastReturnedResult);
			}
		}
		lastReturnedResult = nowProcessing;
		weaverMessageHandler.setCurrentResult(nowProcessing.result());
		return nowProcessing.unwovenClassFiles()[classFileIndex++];
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
	// IWeaveRequestor
	// =====================================================================================
	
	// weave phases as indicated by bcelWeaver...
	public void processingReweavableState() {}
	public void addingTypeMungers() {}
	public void weavingAspects() {}
	public void weavingClasses() {finalPhase = true;}
	
	public void weaveCompleted() {
		if ((lastReturnedResult != null) && (!lastReturnedResult.result().hasBeenAccepted)) {
			finishedWith(lastReturnedResult);
		}
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.IWeaveRequestor#acceptResult(org.aspectj.weaver.bcel.UnwovenClassFile)
	 */
	public void acceptResult(UnwovenClassFile result) {
		char[] key = result.getClassName().replace('.','/').toCharArray();
		removeFromHashtable(lastReturnedResult.result().compiledTypes,key);
		String className = result.getClassName().replace('.', '/');
		AjClassFile ajcf = new AjClassFile(className.toCharArray(),
										   result.getBytes());
		lastReturnedResult.result().record(ajcf.fileName(),ajcf);
	}

	// helpers...
	// =================================================================
	
	private void finishedWith(InterimCompilationResult result) {
		compilerAdapter.acceptResult(result.result());
	}
	
	private void removeFromHashtable(Hashtable table, char[] key) {
		// jdt uses char[] as a key in the hashtable, which is not very useful as equality is based on being
		// the same array, not having the same content.
		String skey = new String(key);
		char[] victim = null;
		for (Enumeration iter = table.keys(); iter.hasMoreElements();) {
			char[] thisKey = (char[]) iter.nextElement();
			if (skey.equals(new String(thisKey))) {
				victim = thisKey;
				break;
			}
		}
		if (victim != null) {
			table.remove(victim);
		}
	}
}