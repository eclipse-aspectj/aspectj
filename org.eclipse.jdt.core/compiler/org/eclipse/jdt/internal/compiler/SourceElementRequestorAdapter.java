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
package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.core.compiler.IProblem;

public class SourceElementRequestorAdapter implements ISourceElementRequestor {

	/*
	 * @see ISourceElementRequestor#acceptConstructorReference(char[], int, int)
	 */
	public void acceptConstructorReference(
		char[] typeName,
		int argCount,
		int sourcePosition) {
	}

	/*
	 * @see ISourceElementRequestor#acceptFieldReference(char[], int)
	 */
	public void acceptFieldReference(char[] fieldName, int sourcePosition) {
	}

	/*
	 * @see ISourceElementRequestor#acceptImport(int, int, char[], boolean)
	 */
	public void acceptImport(
		int declarationStart,
		int declarationEnd,
		char[] name,
		boolean onDemand) {
	}

	/*
	 * @see ISourceElementRequestor#acceptLineSeparatorPositions(int[])
	 */
	public void acceptLineSeparatorPositions(int[] positions) {
	}

	/*
	 * @see ISourceElementRequestor#acceptMethodReference(char[], int, int)
	 */
	public void acceptMethodReference(
		char[] methodName,
		int argCount,
		int sourcePosition) {
	}

	/*
	 * @see ISourceElementRequestor#acceptPackage(int, int, char[])
	 */
	public void acceptPackage(
		int declarationStart,
		int declarationEnd,
		char[] name) {
	}

	/*
	 * @see ISourceElementRequestor#acceptProblem(IProblem)
	 */
	public void acceptProblem(IProblem problem) {
	}

	/*
	 * @see ISourceElementRequestor#acceptTypeReference(char[][], int, int)
	 */
	public void acceptTypeReference(
		char[][] typeName,
		int sourceStart,
		int sourceEnd) {
	}

	/*
	 * @see ISourceElementRequestor#acceptTypeReference(char[], int)
	 */
	public void acceptTypeReference(char[] typeName, int sourcePosition) {
	}

	/*
	 * @see ISourceElementRequestor#acceptUnknownReference(char[][], int, int)
	 */
	public void acceptUnknownReference(
		char[][] name,
		int sourceStart,
		int sourceEnd) {
	}

	/*
	 * @see ISourceElementRequestor#acceptUnknownReference(char[], int)
	 */
	public void acceptUnknownReference(char[] name, int sourcePosition) {
	}

	/*
	 * @see ISourceElementRequestor#enterClass(int, int, char[], int, int, char[], char[][])
	 */
	public void enterClass(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[] superclass,
		char[][] superinterfaces) {
	}

	/*
	 * @see ISourceElementRequestor#enterCompilationUnit()
	 */
	public void enterCompilationUnit() {
	}

	/*
	 * @see ISourceElementRequestor#enterConstructor(int, int, char[], int, int, char[][], char[][], char[][])
	 */
	public void enterConstructor(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes) {
	}

	/*
	 * @see ISourceElementRequestor#enterField(int, int, char[], char[], int, int)
	 */
	public void enterField(
		int declarationStart,
		int modifiers,
		char[] type,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd) {
	}

	/*
	 * @see ISourceElementRequestor#enterInitializer(int, int)
	 */
	public void enterInitializer(int declarationStart, int modifiers) {
	}

	/*
	 * @see ISourceElementRequestor#enterInterface(int, int, char[], int, int, char[][])
	 */
	public void enterInterface(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] superinterfaces) {
	}

	/*
	 * @see ISourceElementRequestor#enterMethod(int, int, char[], char[], int, int, char[][], char[][], char[][])
	 */
	public void enterMethod(
		int declarationStart,
		int modifiers,
		char[] returnType,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes) {
	}

	/*
	 * @see ISourceElementRequestor#exitClass(int)
	 */
	public void exitClass(int declarationEnd) {
	}

	/*
	 * @see ISourceElementRequestor#exitCompilationUnit(int)
	 */
	public void exitCompilationUnit(int declarationEnd) {
	}

	/*
	 * @see ISourceElementRequestor#exitConstructor(int)
	 */
	public void exitConstructor(int declarationEnd) {
	}

	/*
	 * @see ISourceElementRequestor#exitField(int)
	 */
	public void exitField(int declarationEnd) {
	}

	/*
	 * @see ISourceElementRequestor#exitInitializer(int)
	 */
	public void exitInitializer(int declarationEnd) {
	}

	/*
	 * @see ISourceElementRequestor#exitInterface(int)
	 */
	public void exitInterface(int declarationEnd) {
	}

	/*
	 * @see ISourceElementRequestor#exitMethod(int)
	 */
	public void exitMethod(int declarationEnd) {
	}

}

