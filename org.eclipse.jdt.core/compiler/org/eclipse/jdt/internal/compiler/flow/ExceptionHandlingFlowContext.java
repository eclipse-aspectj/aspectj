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
package org.eclipse.jdt.internal.compiler.flow;

import java.util.ArrayList;

import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.codegen.ObjectCache;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Reflects the context of code analysis, keeping track of enclosing
 *	try statements, exception handlers, etc...
 */
public class ExceptionHandlingFlowContext extends FlowContext {
	
	ReferenceBinding[] handledExceptions;
	
	public final static int BitCacheSize = 32; // 32 bits per int
	int[] isReached;
	int[] isNeeded;
	UnconditionalFlowInfo[] initsOnExceptions;
	ObjectCache indexes = new ObjectCache();
	boolean isMethodContext;

	public UnconditionalFlowInfo initsOnReturn;

	// for dealing with anonymous constructor thrown exceptions
	public ArrayList extendedExceptions;
	
	public ExceptionHandlingFlowContext(
		FlowContext parent,
		AstNode associatedNode,
		ReferenceBinding[] handledExceptions,
		BlockScope scope,
		UnconditionalFlowInfo flowInfo) {

		super(parent, associatedNode);
		isMethodContext = scope == scope.methodScope();
		this.handledExceptions = handledExceptions;
		int count = handledExceptions.length, cacheSize = (count / BitCacheSize) + 1;
		this.isReached = new int[cacheSize]; // none is reached by default
		this.isNeeded = new int[cacheSize]; // none is needed by default
		this.initsOnExceptions = new UnconditionalFlowInfo[count];
		for (int i = 0; i < count; i++) {
			this.indexes.put(handledExceptions[i], i); // key type  -> value index
			boolean isUnchecked =
				(scope.compareUncheckedException(handledExceptions[i]) != NotRelated);
			int cacheIndex = i / BitCacheSize, bitMask = 1 << (i % BitCacheSize);
			if (isUnchecked) {
				isReached[cacheIndex] |= bitMask;
				this.initsOnExceptions[i] = flowInfo.copy().unconditionalInits();
			} else {
				this.initsOnExceptions[i] = FlowInfo.DeadEnd;
			}
		}
		System.arraycopy(this.isReached, 0, this.isNeeded, 0, cacheSize);
		this.initsOnReturn = FlowInfo.DeadEnd;	
	}

	public void complainIfUnusedExceptionHandlers(
		AstNode[] exceptionHandlers,
		BlockScope scope,
		TryStatement tryStatement) {
		// report errors for unreachable exception handlers
		for (int i = 0, count = handledExceptions.length; i < count; i++) {
			int index = indexes.get(handledExceptions[i]);
			int cacheIndex = index / BitCacheSize;
			int bitMask = 1 << (index % BitCacheSize);
			if ((isReached[cacheIndex] & bitMask) == 0) {
				scope.problemReporter().unreachableExceptionHandler(
					handledExceptions[index],
					exceptionHandlers[index]);
			} else {
				if ((isNeeded[cacheIndex] & bitMask) == 0) {
					scope.problemReporter().maskedExceptionHandler(
						handledExceptions[index],
						exceptionHandlers[index]);
				}
			}
		}
		// will optimized out unnecessary catch block during code gen
		tryStatement.preserveExceptionHandler = isNeeded;
	}

	public String individualToString() {
		
		StringBuffer buffer = new StringBuffer("Exception flow context"); //$NON-NLS-1$
		int length = handledExceptions.length;
		for (int i = 0; i < length; i++) {
			int cacheIndex = i / BitCacheSize;
			int bitMask = 1 << (i % BitCacheSize);
			buffer.append('[').append(handledExceptions[i].readableName());
			if ((isReached[cacheIndex] & bitMask) != 0) {
				if ((isNeeded[cacheIndex] & bitMask) == 0) {
					buffer.append("-masked"); //$NON-NLS-1$
				} else {
					buffer.append("-reached"); //$NON-NLS-1$
				}
			} else {
				buffer.append("-not reached"); //$NON-NLS-1$
			}
			buffer.append('-').append(initsOnExceptions[i].toString()).append(']');
		}
		return buffer.toString();
	}

	public UnconditionalFlowInfo initsOnException(ReferenceBinding exceptionType) {
		
		int index;
		if ((index = indexes.get(exceptionType)) < 0) {
			return FlowInfo.DeadEnd;
		}
		return initsOnExceptions[index];
	}

	public void recordHandlingException(
		ReferenceBinding exceptionType,
		UnconditionalFlowInfo flowInfo,
		TypeBinding raisedException,
		AstNode invocationSite,
		boolean wasAlreadyDefinitelyCaught) {
			
		int index = indexes.get(exceptionType);
		// if already flagged as being reached (unchecked exception handler)
		int cacheIndex = index / BitCacheSize;
		int bitMask = 1 << (index % BitCacheSize);
		if (!wasAlreadyDefinitelyCaught) {
			this.isNeeded[cacheIndex] |= bitMask;
		}
		this.isReached[cacheIndex] |= bitMask;
		initsOnExceptions[index] =
			initsOnExceptions[index] == FlowInfo.DeadEnd
				? flowInfo.copy().unconditionalInits()
				: initsOnExceptions[index].mergedWith(flowInfo);
	}
	
	public void recordReturnFrom(UnconditionalFlowInfo flowInfo) {
		
		// record initializations which were performed at the return point
		initsOnReturn = initsOnReturn.mergedWith(flowInfo);
	}
	
	/*
	 * Compute a merged list of unhandled exception types (keeping only the most generic ones).
	 * This is necessary to add synthetic thrown exceptions for anonymous type constructors (JLS 8.6).
	 */
	public void mergeUnhandledException(TypeBinding newException){
		
		if (this.extendedExceptions == null){
			this.extendedExceptions = new ArrayList(5);
			for (int i = 0; i < this.handledExceptions.length; i++){
				this.extendedExceptions.add(this.handledExceptions[i]);
			}
		}
		
		boolean isRedundant = false;
		
		for(int i = this.extendedExceptions.size()-1; i >= 0; i--){
			switch(Scope.compareTypes(newException, (TypeBinding)this.extendedExceptions.get(i))){
				case MoreGeneric :
					this.extendedExceptions.remove(i);
					break;
				case EqualOrMoreSpecific :
					isRedundant = true;
					break;
				case NotRelated :
					break;
			}
		}
		if (!isRedundant){
			this.extendedExceptions.add(newException);
		}
	}
}