/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler;

import java.util.Iterator;
import java.util.Map;

import org.aspectj.asm.internal.CharOperation;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IProgressListener;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.aspectj.weaver.IClassFileProvider;
import org.aspectj.weaver.IUnwovenClassFile;
import org.aspectj.weaver.IWeaveRequestor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author colyer This class provides the weaver with a source of class files to weave (via the iterator and IClassFileProvider
 *         interfaces). It receives results back from the weaver via the IWeaveRequestor interface.
 */
public class WeaverAdapter implements IClassFileProvider, IWeaveRequestor, Iterator {

	private final AbstractCompilerAdapter compilerAdapter;
	private Iterator resultIterator;
	private int classFileIndex = 0;
	private InterimCompilationResult nowProcessing;
	private InterimCompilationResult lastReturnedResult;
	private final WeaverMessageHandler weaverMessageHandler;
	private final IProgressListener progressListener;
	private boolean finalPhase = false;
	private int localIteratorCounter;

	// Fields related to progress monitoring
	private int progressMaxTypes;
	private String progressPhasePrefix;
	private double fromPercent;
	private final double toPercent = 100.0;
	private int progressCompletionCount;

	public WeaverAdapter(AbstractCompilerAdapter forCompiler, WeaverMessageHandler weaverMessageHandler,
			IProgressListener progressListener) {
		this.compilerAdapter = forCompiler;
		this.weaverMessageHandler = weaverMessageHandler;
		this.progressListener = progressListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.IClassFileProvider#getClassFileIterator()
	 */
	public Iterator getClassFileIterator() {
		classFileIndex = 0;
		localIteratorCounter = 0;
		nowProcessing = null;
		lastReturnedResult = null;
		resultIterator = compilerAdapter.getResultsPendingWeave().iterator();
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.IClassFileProvider#getRequestor()
	 */
	public IWeaveRequestor getRequestor() {
		return this;
	}

	public boolean isApplyAtAspectJMungersOnly() {
		return false;
	}

	// Iteration
	// ================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (nowProcessing == null) {
			if (!resultIterator.hasNext())
				return false;
			nowProcessing = (InterimCompilationResult) resultIterator.next();
			classFileIndex = 0;
		}
		while (nowProcessing.unwovenClassFiles().length == 0) {
			if (!resultIterator.hasNext())
				return false;
			nowProcessing = (InterimCompilationResult) resultIterator.next();
		}
		if (classFileIndex < nowProcessing.unwovenClassFiles().length) {
			return true;
		} else {
			classFileIndex = 0;
			if (!resultIterator.hasNext())
				return false;
			nowProcessing = (InterimCompilationResult) resultIterator.next();
			while (nowProcessing.unwovenClassFiles().length == 0) {
				if (!resultIterator.hasNext())
					return false;
				nowProcessing = (InterimCompilationResult) resultIterator.next();
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		if (!hasNext())
			return null; // sets up indices correctly
		if (finalPhase) {
			if ((lastReturnedResult != null) && (lastReturnedResult != nowProcessing)) {
				// we're done with the lastReturnedResult
				finishedWith(lastReturnedResult);
			}
		}
		localIteratorCounter++;
		lastReturnedResult = nowProcessing;
		weaverMessageHandler.setCurrentResult(nowProcessing.result());
		// weaverMessageHandler.handleMessage(new Message("weaving " + nowProcessing.fileName(),IMessage.INFO, null, null));
		return nowProcessing.unwovenClassFiles()[classFileIndex++];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	// IWeaveRequestor
	// =====================================================================================

	// weave phases as indicated by bcelWeaver...
	public void processingReweavableState() {

		// progress reporting logic
		fromPercent = 50.0; // Assume weaving takes 50% of the progress bar...
		// recordProgress("processing reweavable state");
	}

	public void addingTypeMungers() {

		// progress reporting logic
		// At this point we have completed one iteration through all the classes/aspects
		// we'll be dealing with, so let us remember this max value for localIteratorCounter
		// (for accurate progress reporting)
		// recordProgress("adding type mungers");
		progressMaxTypes = localIteratorCounter;
	}

	public void weavingAspects() {

		// progress reporting logic
		progressPhasePrefix = "woven aspect ";
		progressCompletionCount = 0; // Start counting from *now*
	}

	public void weavingClasses() {
		finalPhase = true;

		// progress reporting logic
		progressPhasePrefix = "woven class ";
	}

	public void weaveCompleted() {
		if ((lastReturnedResult != null) && (!lastReturnedResult.result().hasBeenAccepted)) {
			finishedWith(lastReturnedResult);
		}
		lastReturnedResult = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aspectj.weaver.IWeaveRequestor#acceptResult(org.aspectj.weaver.bcel.UnwovenClassFile)
	 */
	public void acceptResult(IUnwovenClassFile result) {
		char[] key = result.getClassNameAsChars();
		removeFromMap(lastReturnedResult.result().compiledTypes, key);
		AjClassFile ajcf = new AjClassFile(key, result.getBytes());
		lastReturnedResult.result().record(ajcf.fileName(), ajcf);
		if (!weaverMessageHandler.isIgnoring(IMessage.INFO) || progressListener != null) {
			StringBuffer msg = new StringBuffer();
			msg.append(progressPhasePrefix).append(result.getClassName()).append(" (from ").append(nowProcessing.fileName())
					.append(")");
			weaverMessageHandler.handleMessage(MessageUtil.info(msg.toString()));
			if (progressListener != null) {
				progressCompletionCount++;

				// Smoothly take progress from 'fromPercent' to 'toPercent'
				recordProgress(fromPercent + ((progressCompletionCount / (double) progressMaxTypes) * (toPercent - fromPercent)),
						msg.toString());
				// progressPhasePrefix+result.getClassName()+" (from "+nowProcessing.fileName()+")");

				if (progressListener.isCancelledRequested()) {
					throw new AbortCompilation(true, new OperationCanceledException("Weaving cancelled as requested"));
				}
			}
		}
	}

	// helpers...
	// =================================================================

	private void finishedWith(InterimCompilationResult result) {
		compilerAdapter.acceptResult(result.result());
	}

	private boolean removeFromMap(Map aMap, char[] key) {
		// jdt uses char[] as a key in the hashtable, which is not very useful as equality is based on being
		// the same array, not having the same content.
		// String skey = new String(key);
		// OPTIMIZE what is this code for?
		if (aMap.remove(key) != null) {
			return true;
		}
		char[] victim = null;
		for (Object o : aMap.keySet()) {
			char[] thisKey = (char[]) o;
			if (CharOperation.equals(thisKey, key)) {
				// if (skey.equals(new String(thisKey))) {
				victim = thisKey;
				break;
			}
		}
		if (victim != null) {
			aMap.remove(victim);
			return true;
		}
		return false;
	}

	private void recordProgress(double percentage, String message) {
		if (progressListener != null) {
			progressListener.setProgress(percentage / 100);
			progressListener.setText(message);
		}
	}
}