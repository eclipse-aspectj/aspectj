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
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.internal.compiler.CompilationResult;

/*
 * Special unchecked exception type used 
 * to abort from the compilation process
 *
 * should only be thrown from within problem handlers.
 */
public class AbortCompilation extends RuntimeException {
	public CompilationResult compilationResult;
	public Throwable exception;
	
	public int problemId; 
	public String[] problemArguments;

	/* special fields used to abort silently (e.g. when cancelling build process) */
	public boolean isSilent;
	public RuntimeException silentException;
public AbortCompilation() {
	this((CompilationResult)null);
}
public AbortCompilation(int problemId, String[] problemArguments) {

	this.problemId = problemId;
	this.problemArguments = problemArguments;
}
public AbortCompilation(CompilationResult compilationResult) {
	this(compilationResult, null);
}
public AbortCompilation(CompilationResult compilationResult, Throwable exception) {
	this.compilationResult = compilationResult;
	this.exception = exception;
}
public AbortCompilation(boolean isSilent, RuntimeException silentException) {
	this.isSilent = isSilent;
	this.silentException = silentException;
}
}
