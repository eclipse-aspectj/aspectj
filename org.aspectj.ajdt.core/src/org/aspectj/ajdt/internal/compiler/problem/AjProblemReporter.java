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

 
 package org.aspectj.ajdt.internal.compiler.problem;

import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.Proceed;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Extends problem reporter to support compiler-side implementation of declare soft. 
 * Also overrides error reporting for the need to implement abstract methods to
 * account for inter-type declarations and pointcut declarations.  This second
 * job might be better done directly in the SourceTypeBinding/ClassScope classes.
 * 
 * @author Jim Hugunin
 */
public class AjProblemReporter extends ProblemReporter {
	private static final boolean DUMP_STACK = false;
	
	public EclipseFactory factory;

	public AjProblemReporter(
		IErrorHandlingPolicy policy,
		CompilerOptions options,
		IProblemFactory problemFactory) {
		super(policy, options, problemFactory);
	}
	
	

	public void unhandledException(
		TypeBinding exceptionType,
		ASTNode location)
	{
		if (!factory.getWorld().getDeclareSoft().isEmpty()) {
			Shadow callSite = factory.makeShadow(location, referenceContext);
			Shadow enclosingExec = factory.makeShadow(referenceContext);
//			System.err.println("about to show error for unhandled exception: "  + new String(exceptionType.sourceName()) + 
//					" at " + location + " in " + referenceContext);		
			
			for (Iterator i = factory.getWorld().getDeclareSoft().iterator(); i.hasNext(); ) {
				DeclareSoft d = (DeclareSoft)i.next();
				// We need the exceptionType to match the type in the declare soft statement
				// This means it must either be the same type or a subtype
				ResolvedTypeX throwException = factory.fromEclipse((ReferenceBinding)exceptionType);
				FuzzyBoolean isExceptionTypeOrSubtype = 
					d.getException().matchesInstanceof(throwException);
				if (!isExceptionTypeOrSubtype.alwaysTrue() ) continue;

				if (callSite != null) {
					FuzzyBoolean match = d.getPointcut().match(callSite);
					if (match.alwaysTrue()) {
						//System.err.println("matched callSite: "  + callSite + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						//!!! need this check to happen much sooner
						//throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
				if (enclosingExec != null) {
					FuzzyBoolean match = d.getPointcut().match(enclosingExec);
					if (match.alwaysTrue()) {
						//System.err.println("matched enclosingExec: "  + enclosingExec + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						//!!! need this check to happen much sooner
						//throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
			}
		}
		
		//??? is this always correct
		if (location instanceof Proceed) {
			return;
		}

		super.unhandledException(exceptionType, location);
	}

	private boolean isPointcutDeclaration(MethodBinding binding) {
		return CharOperation.prefixEquals(PointcutDeclaration.mangledPrefix, binding.selector);
	}

	public void abstractMethodCannotBeOverridden(
		SourceTypeBinding type,
		MethodBinding concreteMethod)
	{
		if (isPointcutDeclaration(concreteMethod)) {
			return;
		}
		super.abstractMethodCannotBeOverridden(type, concreteMethod);
	}



	public void abstractMethodMustBeImplemented(
		SourceTypeBinding type,
		MethodBinding abstractMethod)
	{
		// if this is a PointcutDeclaration then there is no error
		if (isPointcutDeclaration(abstractMethod)) {
			return;
		}
		
		if (CharOperation.prefixEquals("ajc$interField".toCharArray(), abstractMethod.selector)) {
			//??? think through how this could go wrong
			return;
		}
		
		// if we implemented this method by an inter-type declaration, then there is no error
		//??? be sure this is always right
		ResolvedTypeX onTypeX = factory.fromEclipse(type); //abstractMethod.declaringClass);
		for (Iterator i = onTypeX.getInterTypeMungers().iterator(); i.hasNext(); ) {
			ConcreteTypeMunger m = (ConcreteTypeMunger)i.next();
			if (m.matches(onTypeX)) {
				ResolvedMember sig = m.getSignature();
                if (!Modifier.isAbstract(sig.getModifiers())) {
					if (ResolvedTypeX
						.matches(
							AjcMemberMaker.interMethod(
								sig,
								m.getAspectType(),
								sig.getDeclaringType().isInterface(
									factory.getWorld())),
							EclipseFactory.makeResolvedMember(abstractMethod))) {
						return;
					}
				}
			}
		}

		super.abstractMethodMustBeImplemented(type, abstractMethod);
	}

	public void handle(
		int problemId,
		String[] problemArguments,
		String[] messageArguments,
		int severity,
		int problemStartPosition,
		int problemEndPosition,
		ReferenceContext referenceContext,
		CompilationResult unitResult)
	{
		if (severity != Ignore && DUMP_STACK) {
			Thread.currentThread().dumpStack();
		}
		super.handle(
			problemId,
			problemArguments,
			messageArguments,
			severity,
			problemStartPosition,
			problemEndPosition,
			referenceContext,
			unitResult);
	}

}
