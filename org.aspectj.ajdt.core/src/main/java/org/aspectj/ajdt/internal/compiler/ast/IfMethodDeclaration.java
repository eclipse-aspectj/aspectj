/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.org.eclipse.jdt.internal.compiler.ClassFile;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.PerClause;

public class IfMethodDeclaration extends AjMethodDeclaration {
	IfPointcut ifPointcut;

	public IfMethodDeclaration(CompilationResult compilationResult, IfPointcut ifPointcut) {
		super(compilationResult);
		this.ifPointcut = ifPointcut;
	}

	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		// do nothing, we're already fully parsed
	}

	protected int generateInfoAttributes(ClassFile classFile) {
		return classFile.generateMethodInfoAttributes(binding, AstUtil.getAjSyntheticAttribute());
	}

	public void resolveStatements() {
		super.resolveStatements();
		if (binding != null) {
			ThisJoinPointVisitor tjp = new ThisJoinPointVisitor(this);
			ifPointcut.extraParameterFlags |= tjp.removeUnusedExtraArguments();

			// Check for FALSE or TRUE constant reference
			if (statements != null && statements.length == 1 && statements[0] instanceof ReturnStatement) {
				if (tjp.hasConstantReference) {
					if (tjp.constantReferenceValue == true) {
						ifPointcut.setAlways(true);
					} else {
						ifPointcut.setAlways(false);
					}
					return;
				}
			}

			// XXX this is where we should remove unavailable args if we're in a cflow
			EclipseFactory factory = EclipseFactory.fromScopeLookupEnvironment(scope);
			ifPointcut.testMethod = new ResolvedMemberImpl(Member.METHOD, factory.fromBinding(binding.declaringClass),
					this.modifiers, UnresolvedType.BOOLEAN, new String(this.selector),
					factory.fromBindings(this.binding.parameters));
			if (tjp.needsThisAspectInstance && scope.parent instanceof ClassScope) { // really should be
				ClassScope o = (ClassScope) scope.parent;
				if (o.referenceContext instanceof AspectDeclaration) { // really should be
					AspectDeclaration aspectDecl = (AspectDeclaration) o.referenceContext;
					if (aspectDecl.perClause != null && aspectDecl.perClause.getKind() != PerClause.SINGLETON) {
						scope.problemReporter()
								.signalError(sourceStart, sourceEnd,
										"thisAspectInstance can only be used inside an if() clause for singleton aspects (compiler limitation)");
						ignoreFurtherInvestigation = true;
						return;
					}
				}
			}
		}
	}
}
