/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseWorld;
import org.aspectj.weaver.AjcMemberMaker;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

public class AspectClinit extends Clinit {
	public AspectClinit(Clinit old, CompilationResult compilationResult) {
		super(compilationResult);
		this.needFreeReturn = old.needFreeReturn;
		this.sourceEnd = old.sourceEnd;
		this.sourceStart = old.sourceStart;
		this.declarationSourceEnd = old.declarationSourceEnd;
		this.declarationSourceStart = old.declarationSourceStart;
	}

	protected void generateSyntheticCode(
		ClassScope classScope,
		CodeStream codeStream) 
	{
		if (!classScope.referenceContext.binding.isAbstract()) {
			final EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);

			codeStream.invokestatic(world.makeMethodBindingForCall(
				AjcMemberMaker.ajcClinitMethod(
					world.fromBinding(classScope.referenceContext.binding)
				)));
		}
		super.generateSyntheticCode(classScope, codeStream);
	}

}
