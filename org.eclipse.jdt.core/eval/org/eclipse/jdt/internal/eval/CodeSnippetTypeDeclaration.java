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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.problem.AbortType;

public class CodeSnippetTypeDeclaration extends TypeDeclaration {

public CodeSnippetTypeDeclaration(CompilationResult compilationResult){
	super(compilationResult);
}

/**
 * Generic bytecode generation for type
 */
public void generateCode(ClassFile enclosingClassFile) {
	if (hasBeenGenerated) return;
	hasBeenGenerated = true;
	
	if (ignoreFurtherInvestigation) {
		if (binding == null)
			return;
		CodeSnippetClassFile.createProblemType(this, scope.referenceCompilationUnit().compilationResult);
		return;
	}
	try {
		// create the result for a compiled type
		ClassFile classFile = new CodeSnippetClassFile(binding, enclosingClassFile, false);
		// generate all fiels
		classFile.addFieldInfos();

		// record the inner type inside its own .class file to be able
		// to generate inner classes attributes
		if (binding.isMemberType())
			classFile.recordEnclosingTypeAttributes(binding);
		if (binding.isLocalType()) {
			enclosingClassFile.recordNestedLocalAttribute(binding);
			classFile.recordNestedLocalAttribute(binding);
		}
		if (memberTypes != null) {
			for (int i = 0, max = memberTypes.length; i < max; i++) {
				// record the inner type inside its own .class file to be able
				// to generate inner classes attributes
				classFile.recordNestedMemberAttribute(memberTypes[i].binding);
				memberTypes[i].generateCode(scope, classFile);
			}
		}
		// generate all methods
		classFile.setForMethodInfos();
		if (methods != null) {
			for (int i = 0, max = methods.length; i < max; i++) {
				methods[i].generateCode(scope, classFile);
			}
		}
		
		// generate all methods
		classFile.addSpecialMethods();

		if (ignoreFurtherInvestigation){ // trigger problem type generation for code gen errors
			throw new AbortType(scope.referenceCompilationUnit().compilationResult);
		}

		// finalize the compiled type result
		classFile.addAttributes();
		scope.referenceCompilationUnit().compilationResult.record(binding.constantPoolName(), classFile);
	} catch (AbortType e) {
		if (binding == null)
			return;
		CodeSnippetClassFile.createProblemType(this, scope.referenceCompilationUnit().compilationResult);
	}
}
}
