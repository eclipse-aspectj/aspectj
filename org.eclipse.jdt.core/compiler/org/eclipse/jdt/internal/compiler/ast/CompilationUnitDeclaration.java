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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class CompilationUnitDeclaration
	extends AstNode
	implements ProblemSeverities, ReferenceContext {
		
	public ImportReference currentPackage;
	public ImportReference[] imports;
	public TypeDeclaration[] types;
	//public char[][] name;

	public boolean ignoreFurtherInvestigation = false;	// once pointless to investigate due to errors
	public boolean ignoreMethodBodies = false;
	public CompilationUnitScope scope;
	public ProblemReporter problemReporter;
	public CompilationResult compilationResult;

	private LocalTypeBinding[] allLocalTypes;
	public boolean isPropagatingInnerClassEmulation;

	public CompilationUnitDeclaration(
		ProblemReporter problemReporter,
		CompilationResult compilationResult,
		int sourceLength) {

		this.problemReporter = problemReporter;
		this.compilationResult = compilationResult;

		//by definition of a compilation unit....
		sourceStart = 0;
		sourceEnd = sourceLength - 1;

	}

	/*
	 *	We cause the compilation task to abort to a given extent.
	 */
	public void abort(int abortLevel) {

		switch (abortLevel) {
			case AbortType :
				throw new AbortType(compilationResult);
			case AbortMethod :
				throw new AbortMethod(compilationResult);
			default :
				throw new AbortCompilationUnit(compilationResult);
		}
	}

	/*
	 * Dispatch code analysis AND request saturation of inner emulation
	 */
	public void analyseCode() {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (types != null) {
				for (int i = 0, count = types.length; i < count; i++) {
					types[i].analyseCode(scope);
				}
			}
			// request inner emulation propagation
			propagateInnerEmulationForAllLocalTypes();
		} catch (AbortCompilationUnit e) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
	}

	/*
	 * When unit result is about to be accepted, removed back pointers
	 * to compiler structures.
	 */
	public void cleanUp() {

		ClassFile[] classFiles = compilationResult.getClassFiles();
		for (int i = 0, max = classFiles.length; i < max; i++) {
			// clear the classFile back pointer to the bindings
			ClassFile classFile = classFiles[i];
			// null out the type's scope backpointers
			 ((SourceTypeBinding) classFile.referenceBinding).scope = null;
			// null out the classfile backpointer to a type binding
			classFile.referenceBinding = null;
			classFile.codeStream = null; // codeStream holds onto ast and scopes
			classFile.innerClassesBindings = null;
		}
	}

	public void checkUnusedImports(){
		
		if (this.scope.imports != null){
			for (int i = 0, max = this.scope.imports.length; i < max; i++){
				ImportBinding importBinding = this.scope.imports[i];
				ImportReference importReference = importBinding.reference;
				if (importReference != null && !importReference.used){
					scope.problemReporter().unusedImport(importReference);
				}
			}
		}
	}
	
	public CompilationResult compilationResult() {
		return compilationResult;
	}
	
	/*
	 * Finds the matching type amoung this compilation unit types.
	 * Returns null if no type with this name is found.
	 * The type name is a compound name
	 * eg. if we're looking for X.A.B then a type name would be {X, A, B}
	 */
	public TypeDeclaration declarationOfType(char[][] typeName) {

		for (int i = 0; i < this.types.length; i++) {
			TypeDeclaration typeDecl = this.types[i].declarationOfType(typeName);
			if (typeDecl != null) {
				return typeDecl;
			}
		}
		return null;
	}

	/**
	 * Bytecode generation
	 */
	public void generateCode() {

		if (ignoreFurtherInvestigation) {
			if (types != null) {
				for (int i = 0, count = types.length; i < count; i++) {
					types[i].ignoreFurtherInvestigation = true;
					// propagate the flag to request problem type creation
					types[i].generateCode(scope);
				}
			}
			return;
		}
		try {
			if (types != null) {
				for (int i = 0, count = types.length; i < count; i++)
					types[i].generateCode(scope);
			}
		} catch (AbortCompilationUnit e) {
		}
	}

	public char[] getFileName() {

		return compilationResult.getFileName();
	}

	public char[] getMainTypeName() {

		if (compilationResult.compilationUnit == null) {
			char[] fileName = compilationResult.getFileName();

			int start = CharOperation.lastIndexOf('/', fileName) + 1;
			if (start == 0 || start < CharOperation.lastIndexOf('\\', fileName))
				start = CharOperation.lastIndexOf('\\', fileName) + 1;

			int end = CharOperation.lastIndexOf('.', fileName);
			if (end == -1)
				end = fileName.length;

			return CharOperation.subarray(fileName, start, end);
		} else {
			return compilationResult.compilationUnit.getMainTypeName();
		}
	}

	public boolean isEmpty() {

		return (currentPackage == null) && (imports == null) && (types == null);
	}

	public boolean hasErrors() {
		return this.ignoreFurtherInvestigation;
	}

	/*
	 * Force inner local types to update their innerclass emulation
	 */
	public void propagateInnerEmulationForAllLocalTypes() {

		isPropagatingInnerClassEmulation = true;
		if (allLocalTypes != null) {
			for (int i = 0, max = allLocalTypes.length; i < max; i++) {
				allLocalTypes[i].updateInnerEmulationDependents();
			}
		}
	}

	/*
	 * Keep track of all local types, so as to update their innerclass
	 * emulation later on.
	 */
	public void record(LocalTypeBinding localType) {

		if (allLocalTypes == null) {
			allLocalTypes = new LocalTypeBinding[] { localType };
		} else {
			int length = allLocalTypes.length;
			System.arraycopy(
				allLocalTypes,
				0,
				(allLocalTypes = new LocalTypeBinding[length + 1]),
				0,
				length);
			allLocalTypes[length] = localType;
		}
	}

	public void resolve() {

		try {
			if (types != null) {
				for (int i = 0, count = types.length; i < count; i++) {
					types[i].resolve(scope);
				}
			}
			checkUnusedImports();
		} catch (AbortCompilationUnit e) {
			this.ignoreFurtherInvestigation = true;
			return;
		}
	}

	public void tagAsHavingErrors() {
		ignoreFurtherInvestigation = true;
	}

	public String toString(int tab) {

		String s = ""; //$NON-NLS-1$
		if (currentPackage != null)
			s = tabString(tab) + "package " + currentPackage.toString(0, false) + ";\n"; //$NON-NLS-1$ //$NON-NLS-2$

		if (imports != null)
			for (int i = 0; i < imports.length; i++) {
				s += tabString(tab) + "import " + imports[i].toString() + ";\n"; //$NON-NLS-1$ //$NON-NLS-2$
			};

		if (types != null)
			for (int i = 0; i < types.length; i++) {
				s += types[i].toString(tab) + "\n"; //$NON-NLS-1$
			}
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		CompilationUnitScope scope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, scope)) {
				if (imports != null) {
					int importLength = imports.length;
					for (int i = 0; i < importLength; i++)
						imports[i].traverse(visitor, scope);
				}
				if (types != null) {
					int typesLength = types.length;
					for (int i = 0; i < typesLength; i++)
						types[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, scope);
		} catch (AbortCompilationUnit e) {
		}
	}
}