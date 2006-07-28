/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement                 initial implementation
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler;

import java.util.List;

import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

/**
 * 
 * @author AndyClement
 *
 */
public abstract class AbstractCompilerAdapter implements ICompilerAdapter {
	
	public abstract List /*InterimResult*/ getResultsPendingWeave();
	
	public abstract void acceptResult(CompilationResult result);

	public abstract void afterAnalysing(CompilationUnitDeclaration unit);

	public abstract void afterCompiling(CompilationUnitDeclaration[] units);

	public abstract void afterDietParsing(CompilationUnitDeclaration[] units);

	public abstract void afterGenerating(CompilationUnitDeclaration unit);

	public abstract void afterProcessing(CompilationUnitDeclaration unit, int unitIndex);

	public abstract void afterResolving(CompilationUnitDeclaration unit);

	public abstract void beforeAnalysing(CompilationUnitDeclaration unit);

	public abstract void beforeCompiling(ICompilationUnit[] sourceUnits);
	
	public abstract void beforeGenerating(CompilationUnitDeclaration unit);

	public abstract void beforeProcessing(CompilationUnitDeclaration unit);

	public abstract void beforeResolving(CompilationUnitDeclaration unit);

}
