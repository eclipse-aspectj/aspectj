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

/*
 * Part of the source element parser responsible for building the output.
 * It gets notified of structural information as they are detected, relying
 * on the requestor to assemble them together, based on the notifications it got.
 *
 * The structural investigation includes:
 * - package statement
 * - import statements
 * - top-level types: package member, member types (member types of member types...)
 * - fields
 * - methods
 *
 * If reference information is requested, then all source constructs are
 * investigated and type, field & method references are provided as well.
 *
 * Any (parsing) problem encountered is also provided.
 *
 * All positions are relative to the exact source fed to the parser.
 *
 * Elements which are complex are notified in two steps:
 * - enter<Element> : once the element header has been identified
 * - exit<Element> : once the element has been fully consumed
 *
 * other simpler elements (package, import) are read all at once:
 * - accept<Element>
 */
 
public interface ISourceElementRequestor {
void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition);
void acceptFieldReference(char[] fieldName, int sourcePosition);
/**
 * @param declarationStart This is the position of the first character of the
 *		  				   import keyword.
 * @param declarationEnd This is the position of the ';' ending the import statement
 *						 or the end of the comment following the import.
 * @param name This is the name of the import like specified in the source including the dots. The '.*'
 *             is never included in the name.
 * @param onDemand set to true if the import is an import on demand (e.g. import java.io.*). False otherwise.
 */
void acceptImport(
	int declarationStart,
	int declarationEnd,
	char[] name,
	boolean onDemand);
/*
 * Table of line separator position. This table is passed once at the end
 * of the parse action, so as to allow computation of normalized ranges.
 *
 * A line separator might corresponds to several characters in the source,
 * 
 */
void acceptLineSeparatorPositions(int[] positions);
void acceptMethodReference(char[] methodName, int argCount, int sourcePosition);
void acceptPackage(
	int declarationStart,
	int declarationEnd,
	char[] name);
void acceptProblem(IProblem problem);
void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd);
void acceptTypeReference(char[] typeName, int sourcePosition);
void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd);
void acceptUnknownReference(char[] name, int sourcePosition);
void enterClass(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclass,
	char[][] superinterfaces);
void enterCompilationUnit();
void enterConstructor(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,	
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes);
void enterField(
	int declarationStart,
	int modifiers,
	char[] type,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd);
void enterInitializer(
	int declarationStart,
	int modifiers);
void enterInterface(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] superinterfaces);
void enterMethod(
	int declarationStart,
	int modifiers,
	char[] returnType,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,	
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes);
void exitClass(int declarationEnd);
void exitCompilationUnit(int declarationEnd);
void exitConstructor(int declarationEnd);
/*
 * - No initialization source for now -
 * initializationSource denotes the source of the expression used for initializing
 * the field if any (if no source, then it is null).
 *
 * Note: the initializationSource will be used in case we do need to type check
 *	against source models, and thus the only interesting use for it is field
 *  constant propagation. Therefore, the initializationSource will only be non
 *  null for final fields (so as to minimize char[] allocations).
 */
void exitField(/*char[] initializationSource, */int declarationEnd);
void exitInitializer(int declarationEnd);
void exitInterface(int declarationEnd);
void exitMethod(int declarationEnd);
}
