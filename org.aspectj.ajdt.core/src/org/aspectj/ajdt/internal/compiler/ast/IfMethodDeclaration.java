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


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseWorld;
import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.IfPointcut;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class IfMethodDeclaration extends MethodDeclaration {
	IfPointcut ifPointcut;

	public IfMethodDeclaration(CompilationResult compilationResult, IfPointcut ifPointcut) {
		super(compilationResult);
		this.ifPointcut = ifPointcut;
	}
	
	public void parseStatements(
		Parser parser,
		CompilationUnitDeclaration unit) {
		// do nothing, we're already fully parsed
	}
	
	protected int generateInfoAttributes(ClassFile classFile) {
		return classFile.generateMethodInfoAttribute(binding, AstUtil.getAjSyntheticAttribute());
	}
	
	public void resolveStatements(ClassScope upperScope) {
		super.resolveStatements(upperScope);
		if (binding != null) {
			ThisJoinPointVisitor tjp = new ThisJoinPointVisitor(this);
			ifPointcut.extraParameterFlags |= tjp.removeUnusedExtraArguments();
			
			//XXX this is where we should remove unavailable args if we're in a cflow
			
			ifPointcut.testMethod = new ResolvedMember(
				Member.METHOD,
				EclipseWorld.fromBinding(binding.declaringClass),
				this.modifiers, ResolvedTypeX.BOOLEAN,  
				new String(this.selector),
				EclipseWorld.fromBindings(this.binding.parameters));
		}	
	}
}
