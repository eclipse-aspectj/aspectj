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

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class MethodDeclaration extends AbstractMethodDeclaration {
	
	public TypeReference returnType;

	/**
	 * MethodDeclaration constructor comment.
	 */
	public MethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}


	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {

		//fill up the method body with statement
		if (ignoreFurtherInvestigation)
			return;
		parser.parse(this, unit);
	}

	public void resolveStatements(ClassScope upperScope) {

		// ========= abort on fatal error =============
		if (this.returnType != null && this.binding != null) {
			this.returnType.binding = this.binding.returnType;
			// record the return type binding
		}
		// look if the name of the method is correct
		if (binding != null && isTypeUseDeprecated(binding.returnType, scope))
			scope.problemReporter().deprecatedType(binding.returnType, returnType);

		if (CharOperation.equals(scope.enclosingSourceType().sourceName, selector))
			scope.problemReporter().methodWithConstructorName(this);

		// by grammatical construction, interface methods are always abstract
		if (!scope.enclosingSourceType().isInterface()){

			// if a method has an semicolon body and is not declared as abstract==>error
			// native methods may have a semicolon body 
			if ((modifiers & AccSemicolonBody) != 0) {
				if ((modifiers & AccNative) == 0)
					if ((modifiers & AccAbstract) == 0)
						scope.problemReporter().methodNeedingAbstractModifier(this);
			} else {
				// the method HAS a body --> abstract native modifiers are forbiden
				if (((modifiers & AccNative) != 0) || ((modifiers & AccAbstract) != 0))
					scope.problemReporter().methodNeedingNoBody(this);
			}
		}
		super.resolveStatements(upperScope); 
	}

	public String returnTypeToString(int tab) {

		if (returnType == null)
			return ""; //$NON-NLS-1$
		return returnType.toString(tab) + " "; //$NON-NLS-1$
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		ClassScope classScope) {

		if (visitor.visit(this, classScope)) {
			if (returnType != null)
				returnType.traverse(visitor, scope);
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (thrownExceptions != null) {
				int thrownExceptionsLength = thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++)
					thrownExceptions[i].traverse(visitor, scope);
			}
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
}