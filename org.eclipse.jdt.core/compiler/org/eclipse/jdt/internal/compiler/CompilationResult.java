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

/**
 * A compilation result consists of all information returned by the compiler for 
 * a single compiled compilation source unit.  This includes:
 * <ul>
 * <li> the compilation unit that was compiled
 * <li> for each type produced by compiling the compilation unit, its binary and optionally its principal structure
 * <li> any problems (errors or warnings) produced
 * <li> dependency info
 * </ul>
 *
 * The principle structure and binary may be null if the compiler could not produce them.
 * If neither could be produced, there is no corresponding entry for the type.
 *
 * The dependency info includes type references such as supertypes, field types, method
 * parameter and return types, local variable types, types of intermediate expressions, etc.
 * It also includes the namespaces (packages) in which names were looked up.
 * It does <em>not</em> include finer grained dependencies such as information about
 * specific fields and methods which were referenced, but does contain their 
 * declaring types and any other types used to locate such fields or methods.
 */

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;

import java.util.*;

public class CompilationResult {
	
	public IProblem problems[];
	public int problemCount;
	public ICompilationUnit compilationUnit;
	private Map problemsMap;
	private Map firstErrorsMap;
	private HashSet duplicateProblems;
	private int maxProblemPerUnit;
	public char[][][] qualifiedReferences;
	public char[][] simpleNameReferences;

	public int lineSeparatorPositions[];
	public Hashtable compiledTypes = new Hashtable(11);
	public int unitIndex, totalUnitsKnown;
	public boolean hasBeenAccepted = false;
	public char[] fileName;
	
public CompilationResult(
	char[] fileName,
	int unitIndex, 
	int totalUnitsKnown,
	int maxProblemPerUnit){

	this.fileName = fileName;
	this.unitIndex = unitIndex;
	this.totalUnitsKnown = totalUnitsKnown;
	this.maxProblemPerUnit = maxProblemPerUnit;

}

public CompilationResult(
	ICompilationUnit compilationUnit,
	int unitIndex, 
	int totalUnitsKnown,
	int maxProblemPerUnit){

	this.fileName = compilationUnit.getFileName();
	this.compilationUnit = compilationUnit;
	this.unitIndex = unitIndex;
	this.totalUnitsKnown = totalUnitsKnown;
	this.maxProblemPerUnit = maxProblemPerUnit;

}
private int computePriority(IProblem problem){

	final int P_STATIC = 1000;
	final int P_OUTSIDE_METHOD = 4000;
	final int P_FIRST_ERROR = 2000;
	final int P_ERROR = 10000;
	
	int priority = 1000 - problem.getSourceLineNumber(); // early problems first
	if (priority < 0) priority = 0;
	if (problem.isError()){
		priority += P_ERROR;
	}
	ReferenceContext context = problemsMap == null ? null : (ReferenceContext) problemsMap.get(problem);
	if (context != null){
		if (context instanceof AbstractMethodDeclaration){
			AbstractMethodDeclaration method = (AbstractMethodDeclaration) context;
			if (method.isStatic()) {
				priority += P_STATIC;
			}
		} else {
		priority += P_OUTSIDE_METHOD;
		}
	} else {
		priority += P_OUTSIDE_METHOD;
	}
	if (firstErrorsMap.containsKey(problem)){
		priority += P_FIRST_ERROR;
	}
	return priority;
}
public ClassFile[] getClassFiles() {
	Enumeration enum = compiledTypes.elements();
	ClassFile[] classFiles = new ClassFile[compiledTypes.size()];
	int index = 0;
	while (enum.hasMoreElements()){
		classFiles[index++] = (ClassFile)enum.nextElement();
	}
	return classFiles;	
}
/**
 * Answer the initial compilation unit corresponding to the present compilation result
 */
public ICompilationUnit getCompilationUnit(){
	return compilationUnit;
}
/**
 * Answer the initial file name
 */
public char[] getFileName(){
	return fileName;
}
/**
 * Answer the problems (errors and warnings) encountered during compilation.
 *
 * This is not a compiler internal API - it has side-effects !
 * It is intended to be used only once all problems have been detected,
 * and makes sure the problems slot as the exact size of the number of
 * problems.
 */
public IProblem[] getProblems() {
	
	// Re-adjust the size of the problems if necessary.
	if (problems != null) {

		if (this.problemCount != problems.length) {
			System.arraycopy(problems, 0, (problems = new IProblem[problemCount]), 0, problemCount);
		}

		if (this.maxProblemPerUnit > 0 && this.problemCount > this.maxProblemPerUnit){
			quickPrioritize(problems, 0, problemCount - 1);
			this.problemCount = this.maxProblemPerUnit;
			System.arraycopy(problems, 0, (problems = new IProblem[problemCount]), 0, problemCount);
		}

		// Sort problems per source positions.
		quicksort(problems, 0, problems.length-1);
	}
	return problems;
}

public boolean hasErrors() {
	if (problems != null)
		for (int i = 0; i < problemCount; i++) {
			if (problems[i].isError())
				return true;
		}
	return false;
}
public boolean hasProblems() {
	return problemCount != 0;
}
public boolean hasWarnings() {
	if (problems != null)
		for (int i = 0; i < problemCount; i++) {
			if (problems[i].isWarning())
				return true;
		}
	return false;
}

private static void quicksort(IProblem arr[], int left, int right) {
	int i, last, pos;

	if (left >= right) {
		/* do nothing if array contains fewer than two */
		return;
		/* two elements */
	}

	swap(arr, left, (left + right) / 2);
	last = left;
	pos = arr[left].getSourceStart();

	for (i = left + 1; i <= right; i++) {
		if (arr[i].getSourceStart() < pos) {
			swap(arr, ++last, i);
		}
	}

	swap(arr, left, last);
	quicksort(arr, left, last - 1);
	quicksort(arr, last + 1, right);
}

private void quickPrioritize(IProblem arr[], int left, int right) {
	int i, last, prio;

	if (left >= right) {
		/* do nothing if array contains fewer than two */
		return;
		/* two elements */
	}

	swap(arr, left, (left + right) / 2);
	last = left;
	prio = computePriority(arr[left]);

	for (i = left + 1; i <= right; i++) {
		if (computePriority(arr[i]) > prio) {
			swap(arr, ++last, i);
		}
	}

	swap(arr, left, last);
	quickPrioritize(arr, left, last - 1);
	quickPrioritize(arr, last + 1, right);
}

/**
 * For now, remember the compiled type using its compound name.
 */
public void record(char[] typeName, ClassFile classFile) {
	compiledTypes.put(typeName, classFile);
}
public void record(IProblem newProblem, ReferenceContext referenceContext) {
	if (problemCount == 0) {
		problems = new IProblem[5];
	} else {
		if (problemCount == problems.length)
			System.arraycopy(problems, 0, (problems = new IProblem[problemCount * 2]), 0, problemCount);
	};
	problems[problemCount++] = newProblem;
	if (referenceContext != null){
		if (problemsMap == null) problemsMap = new Hashtable(5);
		if (firstErrorsMap == null) firstErrorsMap = new Hashtable(5);
		if (newProblem.isError() && !referenceContext.hasErrors()) firstErrorsMap.put(newProblem, newProblem);
		problemsMap.put(newProblem, referenceContext);
	}
}
private static void swap(IProblem arr[], int i, int j) {
	IProblem tmp;
	tmp = arr[i];
	arr[i] = arr[j];
	arr[j] = tmp;
}
CompilationResult tagAsAccepted(){
	this.hasBeenAccepted = true;
	this.problemsMap = null; // flush
	return this;
}

public String toString(){
	StringBuffer buffer = new StringBuffer();
	if (this.fileName != null){
		buffer.append("Filename : ").append(this.fileName).append('\n'); //$NON-NLS-1$
	}
	if (this.compiledTypes != null){
		buffer.append("COMPILED type(s)	\n");  //$NON-NLS-1$
		Enumeration typeNames = this.compiledTypes.keys();
		while (typeNames.hasMoreElements()) {
			char[] typeName = (char[]) typeNames.nextElement();
			buffer.append("\t - ").append(typeName).append('\n');   //$NON-NLS-1$
			
		}
	} else {
		buffer.append("No COMPILED type\n");  //$NON-NLS-1$
	}
	if (problems != null){
		buffer.append(this.problemCount).append(" PROBLEM(s) detected \n"); //$NON-NLS-1$//$NON-NLS-2$
		for (int i = 0; i < this.problemCount; i++){
			buffer.append("\t - ").append(this.problems[i]).append('\n'); //$NON-NLS-1$
		}
	} else {
		buffer.append("No PROBLEM\n"); //$NON-NLS-1$
	} 
	return buffer.toString();
}
}
