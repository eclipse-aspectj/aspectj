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
package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

public class CompletionOnMethodReturnType extends MethodDeclaration {
	public CompletionOnMethodReturnType(TypeReference returnType, CompilationResult compilationResult){
		super(compilationResult);
		this.returnType = returnType;
		this.sourceStart = returnType.sourceStart;
		this.sourceEnd = returnType.sourceEnd;
	}
	
	public void resolveStatements(ClassScope upperScope) {
			throw new CompletionNodeFound(this, upperScope);
	}
	
	public String toString(int tab) {
		return returnType.toString(tab);
	}

}