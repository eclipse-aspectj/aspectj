/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler;

import java.util.Map;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.bridge.IMessage;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;


public class AjCompiler extends Compiler {

	public AjCompiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory) {
		super(environment, policy, settings, requestor, problemFactory);
	}

	public AjCompiler(
		INameEnvironment environment,
		IErrorHandlingPolicy policy,
		Map settings,
		ICompilerRequestor requestor,
		IProblemFactory problemFactory,
		boolean parseLiteralExpressionsAsConstants) {
		super(
			environment,
			policy,
			settings,
			requestor,
			problemFactory,
			parseLiteralExpressionsAsConstants);
	}
	
	/**
	 * In addition to processing each compilation unit in the normal ways, 
	 * we also need to do weaving for inter-type declarations.  This
	 * must be done before we use the signatures of these types to do any
	 * name binding.
	 */
	protected void process(CompilationUnitDeclaration unit, int i) {
		EclipseFactory world = 
			EclipseFactory.forLookupEnvironment(lookupEnvironment);
		world.showMessage(IMessage.INFO, "compiling " + new String(unit.getFileName()), null, null);
		super.process(unit, i);
				
		world.finishedCompilationUnit(unit);
	}
}
