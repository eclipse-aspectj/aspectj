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

package org.aspectj.ajdt.internal.compiler.problem;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareAnnotationDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.IfMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.Proceed;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeMethodBinding;
import org.aspectj.ajdt.internal.compiler.lookup.PrivilegedFieldBinding;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.aspectj.org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.IPrivilegedHandler;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.TypePattern;

/**
 * Extends problem reporter to support compiler-side implementation of declare soft. Also overrides error reporting for the need to
 * implement abstract methods to account for inter-type declarations and pointcut declarations. This second job might be better done
 * directly in the SourceTypeBinding/ClassScope classes.
 * 
 * @author Jim Hugunin
 */
public class AjProblemReporter extends ProblemReporter {

	private static final boolean DUMP_STACK = false;

	public EclipseFactory factory;

	public AjProblemReporter(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
		super(policy, options, problemFactory);
	}

	public void unhandledException(TypeBinding exceptionType, ASTNode location) {
		if (!factory.getWorld().getDeclareSoft().isEmpty()) {
			Shadow callSite = factory.makeShadow(location, referenceContext);
			Shadow enclosingExec = factory.makeShadow(referenceContext);
			// PR 72157 - calls to super / this within a constructor are not part of the cons join point.
			if ((callSite == null) && (enclosingExec.getKind() == Shadow.ConstructorExecution)
					&& (location instanceof ExplicitConstructorCall)) {
				super.unhandledException(exceptionType, location);
				return;
			}
			// System.err.println("about to show error for unhandled exception: " + new String(exceptionType.sourceName()) +
			// " at " + location + " in " + referenceContext);

			for (DeclareSoft d: factory.getWorld().getDeclareSoft()) {
//			for (Iterator<DeclareSoft> i = factory.getWorld().getDeclareSoft().iterator(); i.hasNext();) {
//				DeclareSoft d = (DeclareSoft) i.next();
				// We need the exceptionType to match the type in the declare soft statement
				// This means it must either be the same type or a subtype
				ResolvedType throwException = factory.fromEclipse((ReferenceBinding) exceptionType);
				FuzzyBoolean isExceptionTypeOrSubtype = d.getException().matchesInstanceof(throwException);
				if (!isExceptionTypeOrSubtype.alwaysTrue())
					continue;

				if (callSite != null) {
					FuzzyBoolean match = d.getPointcut().match(callSite);
					if (match.alwaysTrue()) {
						// System.err.println("matched callSite: " + callSite + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						// !!! need this check to happen much sooner
						// throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
				if (enclosingExec != null) {
					FuzzyBoolean match = d.getPointcut().match(enclosingExec);
					if (match.alwaysTrue()) {
						// System.err.println("matched enclosingExec: " + enclosingExec + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						// !!! need this check to happen much sooner
						// throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
			}
		}

		// ??? is this always correct
		if (location instanceof Proceed) {
			return;
		}

		super.unhandledException(exceptionType, location);
	}
	
	public void unhandledExceptionFromAutoClose(TypeBinding exceptionType, ASTNode location) {
		if (!factory.getWorld().getDeclareSoft().isEmpty()) {
			Shadow callSite = factory.makeShadow(location, referenceContext);
			Shadow enclosingExec = factory.makeShadow(referenceContext);
			// PR 72157 - calls to super / this within a constructor are not part of the cons join point.
			if ((callSite == null) && (enclosingExec.getKind() == Shadow.ConstructorExecution)
					&& (location instanceof ExplicitConstructorCall)) {
				super.unhandledException(exceptionType, location);
				return;
			}
			// System.err.println("about to show error for unhandled exception: " + new String(exceptionType.sourceName()) +
			// " at " + location + " in " + referenceContext);

			for (DeclareSoft d: factory.getWorld().getDeclareSoft()) {
//			for (Iterator<DeclareSoft> i = factory.getWorld().getDeclareSoft().iterator(); i.hasNext();) {
//				DeclareSoft d = (DeclareSoft) i.next();
				// We need the exceptionType to match the type in the declare soft statement
				// This means it must either be the same type or a subtype
				ResolvedType throwException = factory.fromEclipse((ReferenceBinding) exceptionType);
				FuzzyBoolean isExceptionTypeOrSubtype = d.getException().matchesInstanceof(throwException);
				if (!isExceptionTypeOrSubtype.alwaysTrue())
					continue;

				if (callSite != null) {
					FuzzyBoolean match = d.getPointcut().match(callSite);
					if (match.alwaysTrue()) {
						// System.err.println("matched callSite: " + callSite + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						// !!! need this check to happen much sooner
						// throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
				if (enclosingExec != null) {
					FuzzyBoolean match = d.getPointcut().match(enclosingExec);
					if (match.alwaysTrue()) {
						// System.err.println("matched enclosingExec: " + enclosingExec + " with " + d);
						return;
					} else if (!match.alwaysFalse()) {
						// !!! need this check to happen much sooner
						// throw new RuntimeException("unimplemented, shouldn't have fuzzy match here");
					}
				}
			}
		}

		// ??? is this always correct
		if (location instanceof Proceed) {
			return;
		}

		super.unhandledExceptionFromAutoClose(exceptionType, location);
	}

	private boolean isPointcutDeclaration(MethodBinding binding) {
		return CharOperation.prefixEquals(PointcutDeclaration.mangledPrefix, binding.selector);
	}

	private boolean isIntertypeDeclaration(MethodBinding binding) {
		return (binding instanceof InterTypeMethodBinding);
	}

	public void abstractMethodCannotBeOverridden(SourceTypeBinding type, MethodBinding concreteMethod) {
		if (isPointcutDeclaration(concreteMethod)) {
			return;
		}
		super.abstractMethodCannotBeOverridden(type, concreteMethod);
	}

	public void inheritedMethodReducesVisibility(SourceTypeBinding type, MethodBinding concreteMethod,
			MethodBinding[] abstractMethods) {
		// if we implemented this method by a public inter-type declaration, then there is no error

		ResolvedType onTypeX = null;
		// If the type is anonymous, look at its supertype
		if (!type.isAnonymousType()) {
			onTypeX = factory.fromEclipse(type);
		} else {
			// Hmmm. If the ITD is on an interface that is being 'instantiated' using an anonymous type,
			// we sort it out elsewhere and don't come into this method -
			// so we don't have to worry about interfaces, just the superclass.
			onTypeX = factory.fromEclipse(type.superclass()); // abstractMethod.declaringClass);
		}
		for (ConcreteTypeMunger m : onTypeX.getInterTypeMungersIncludingSupers()) {
			ResolvedMember sig = m.getSignature();
			if (!Modifier.isAbstract(sig.getModifiers())) {
				if (ResolvedType.matches(
						AjcMemberMaker.interMethod(sig, m.getAspectType(), sig.getDeclaringType().resolve(factory.getWorld())
								.isInterface()), factory.makeResolvedMember(concreteMethod))) {
					return;
				}
			}
		}

		super.inheritedMethodReducesVisibility(type, concreteMethod, abstractMethods);
	}

	// if either of the MethodBinding is an ITD, we have already reported it.
	public void staticAndInstanceConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
		if (currentMethod instanceof InterTypeMethodBinding)
			return;
		if (inheritedMethod instanceof InterTypeMethodBinding)
			return;
		super.staticAndInstanceConflict(currentMethod, inheritedMethod);
	}

	public void abstractMethodMustBeImplemented(SourceTypeBinding type, MethodBinding abstractMethod) {
		// if this is a PointcutDeclaration then there is no error
		if (isPointcutDeclaration(abstractMethod))
			return;

		if (isIntertypeDeclaration(abstractMethod))
			return; // when there is a problem with an ITD not being implemented, it will be reported elsewhere

		if (CharOperation.prefixEquals("ajc$interField".toCharArray(), abstractMethod.selector)) {
			// ??? think through how this could go wrong
			return;
		}

		// if we implemented this method by an inter-type declaration, then there is no error
		// ??? be sure this is always right
		ResolvedType onTypeX = null;

		// If the type is anonymous, look at its supertype
		if (!type.isAnonymousType()) {
			onTypeX = factory.fromEclipse(type);
		} else {
			// Hmmm. If the ITD is on an interface that is being 'instantiated' using an anonymous type,
			// we sort it out elsewhere and don't come into this method -
			// so we don't have to worry about interfaces, just the superclass.
			onTypeX = factory.fromEclipse(type.superclass()); // abstractMethod.declaringClass);
		}

		if (onTypeX.isRawType())
			onTypeX = onTypeX.getGenericType();

		List<ConcreteTypeMunger> mungers = onTypeX.getInterTypeMungersIncludingSupers();
		for (ConcreteTypeMunger m : mungers) {
			ResolvedMember sig = m.getSignature();
			if (sig != null && !Modifier.isAbstract(sig.getModifiers())) {
				ResolvedMember abstractMember = factory.makeResolvedMember(abstractMethod);
				if (abstractMember.getName().startsWith("ajc$interMethodDispatch")) {
					ResolvedType dType = factory.getWorld().resolve(sig.getDeclaringType(), false);
					if (ResolvedType.matches(AjcMemberMaker.interMethod(sig, m.getAspectType(), dType.isInterface()),
							abstractMember)) {
						return;
					}
				} else {
					// In this case we have something like:
					// interface I {}
					// abstract class C implements I { abstract void foo();}
					// class D extends C {}
					// ITD: public void I.foo() {...}
					// The ITD is providing the implementation of foo in the class D but when checking for whether the abstract
					// method is overridden, we won't be looking at whether the ITD overrides ajc$interMethodDispath$...foo but
					// whether it overrides the foo method from class C
					if (ResolvedType.matches(sig, factory.makeResolvedMember(abstractMethod)))
						return;
				}
			}
		}

		super.abstractMethodMustBeImplemented(type, abstractMethod);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemReporter#disallowedTargetForAnnotation(org.aspectj.org.eclipse
	 * .jdt.internal.compiler.ast.Annotation)
	 */
	public void disallowedTargetForAnnotation(Annotation annotation) {
		// if the annotation's recipient is an ITD, it might be allowed after all...
		if (annotation.recipient instanceof MethodBinding) {
			MethodBinding binding = (MethodBinding) annotation.recipient;
			String name = new String(binding.selector);
			if (name.startsWith("ajc$")) {
				long metaTagBits = annotation.resolvedType.getAnnotationTagBits(); // could be forward reference
				if (name.contains("interField")) {
					if ((metaTagBits & TagBits.AnnotationForField) != 0)
						return;
				} else if (name.contains("interConstructor")) {
					if ((metaTagBits & TagBits.AnnotationForConstructor) != 0)
						return;
				} else if (name.contains("interMethod")) {
					if ((metaTagBits & TagBits.AnnotationForMethod) != 0)
						return;
				} else if (name.contains("declare_" + DeclareAnnotation.AT_TYPE + "_")) {
					if ((metaTagBits & TagBits.AnnotationForAnnotationType) != 0 || (metaTagBits & TagBits.AnnotationForType) != 0)
						return;
				} else if (name.contains("declare_" + DeclareAnnotation.AT_FIELD + "_")) {
					if ((metaTagBits & TagBits.AnnotationForField) != 0)
						return;
				} else if (name.contains("declare_" + DeclareAnnotation.AT_CONSTRUCTOR + "_")) {
					if ((metaTagBits & TagBits.AnnotationForConstructor) != 0)
						return;
				} else if (name.contains("declare_eow")) {
					if ((metaTagBits & TagBits.AnnotationForField) != 0)
						return;
				}
			}
		}

		// not our special case, report the problem...
		super.disallowedTargetForAnnotation(annotation);
	}

	public void overridesPackageDefaultMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
		if (new String(localMethod.selector).startsWith("ajc$"))
			return;
		super.overridesPackageDefaultMethod(localMethod, inheritedMethod);
	}

	public void handle(int problemId, String[] problemArguments, String[] messageArguments, int severity, int problemStartPosition,
			int problemEndPosition, ReferenceContext referenceContext, CompilationResult unitResult) {
		if (severity != ProblemSeverities.Ignore && DUMP_STACK) {
			Thread.dumpStack();
		}
		super.handle(problemId, problemArguments, 
				0, // no message elaboration
				messageArguments, severity, problemStartPosition, problemEndPosition,
				referenceContext, unitResult);
	}

	// PR71076
	public void javadocMissingParamTag(char[] name, int sourceStart, int sourceEnd, int modifiers) {
		boolean reportIt = true;
		String sName = new String(name);
		if (sName.startsWith("ajc$"))
			reportIt = false;
		if (sName.equals("thisJoinPoint"))
			reportIt = false;
		if (sName.equals("thisJoinPointStaticPart"))
			reportIt = false;
		if (sName.equals("thisEnclosingJoinPointStaticPart"))
			reportIt = false;
		if (sName.equals("ajc_aroundClosure"))
			reportIt = false;
		if (reportIt)
			super.javadocMissingParamTag(name, sourceStart, sourceEnd, modifiers);
	}

	public void abstractMethodInAbstractClass(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {

		String abstractMethodName = new String(methodDecl.selector);
		if (abstractMethodName.startsWith("ajc$pointcut")) {
			// This will already have been reported, see: PointcutDeclaration.postParse()
			return;
		}
		String[] arguments = new String[] { new String(type.sourceName()), abstractMethodName };
		super.handle(IProblem.AbstractMethodInAbstractClass, arguments, arguments, methodDecl.sourceStart, methodDecl.sourceEnd,
				this.referenceContext, this.referenceContext == null ? null : this.referenceContext.compilationResult());
	}

	/**
	 * Called when there is an ITD marked @override that doesn't override a supertypes method. The method and the binding are passed
	 * - some information is useful from each. The 'method' knows about source offsets for the message, the 'binding' has the
	 * signature of what the ITD is trying to be in the target class.
	 */
	public void itdMethodMustOverride(AbstractMethodDeclaration method, MethodBinding binding) {
		this.handle(IProblem.MethodMustOverride,
				new String[] { new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false),
						new String(binding.declaringClass.readableName()), },
				new String[] { new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true),
						new String(binding.declaringClass.shortReadableName()), }, method.sourceStart, method.sourceEnd,
				this.referenceContext, this.referenceContext == null ? null : this.referenceContext.compilationResult());
	}

	/**
	 * Overrides the implementation in ProblemReporter and is ITD aware. To report a *real* problem with an ITD marked @override,
	 * the other methodMustOverride() method is used.
	 */
	public void methodMustOverride(AbstractMethodDeclaration method, long complianceLevel) {

		// ignore ajc$ methods
		if (new String(method.selector).startsWith("ajc$"))
			return;
		ResolvedMember possiblyErroneousRm = factory.makeResolvedMember(method.binding);

		ResolvedType onTypeX = factory.fromEclipse(method.binding.declaringClass);
		// Can't use 'getInterTypeMungersIncludingSupers()' since that will exclude abstract ITDs
		// on any super classes - so we have to trawl up ourselves.. I wonder if this problem
		// affects other code in the problem reporter that looks through ITDs...
		ResolvedType supertypeToLookAt = onTypeX.getSuperclass();
		while (supertypeToLookAt != null) {
			List<ConcreteTypeMunger> itMungers = supertypeToLookAt.getInterTypeMungers();
			for (ConcreteTypeMunger m : itMungers) {
				if (m.getMunger() != null && m.getMunger().getKind() == ResolvedTypeMunger.PrivilegedAccess) {
					continue;
				}
				ResolvedMember sig = m.getSignature();
				if (sig == null)
					continue; // we aren't interested in other kinds of munger
				UnresolvedType dType = sig.getDeclaringType();
				if (dType == null)
					continue;
				ResolvedType resolvedDeclaringType = dType.resolve(factory.getWorld());
				ResolvedMember rm = AjcMemberMaker.interMethod(sig, m.getAspectType(), resolvedDeclaringType.isInterface());
				if (ResolvedType.matches(rm, possiblyErroneousRm)) {
					// match, so dont need to report a problem!
					return;
				}
			}
			supertypeToLookAt = supertypeToLookAt.getSuperclass();
		}
		// report the error...
		super.methodMustOverride(method,complianceLevel);
	}

	private String typesAsString(boolean isVarargs, TypeBinding[] types, boolean makeShort) {
		StringBuffer buffer = new StringBuffer(10);
		for (int i = 0, length = types.length; i < length; i++) {
			if (i != 0)
				buffer.append(", "); //$NON-NLS-1$
			TypeBinding type = types[i];
			boolean isVarargType = isVarargs && i == length - 1;
			if (isVarargType)
				type = ((ArrayBinding) type).elementsType();
			buffer.append(new String(makeShort ? type.shortReadableName() : type.readableName()));
			if (isVarargType)
				buffer.append("..."); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	public void visibilityConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
		// Not quite sure if the conditions on this test are right - basically I'm saying
		// DONT WORRY if its ITDs since the error will be reported another way...
		if (isIntertypeDeclaration(currentMethod) && isIntertypeDeclaration(inheritedMethod)
				&& Modifier.isPrivate(currentMethod.modifiers) && Modifier.isPrivate(inheritedMethod.modifiers)) {
			return;
		}
		super.visibilityConflict(currentMethod, inheritedMethod);
	}

	public void unusedPrivateType(TypeDeclaration typeDecl) {
		// don't output unused type warnings for aspects!
		if (typeDecl instanceof AspectDeclaration)
			return;
		if (typeDecl.enclosingType != null && (typeDecl.enclosingType instanceof AspectDeclaration)) {
			AspectDeclaration ad = (AspectDeclaration) typeDecl.enclosingType;
			if (ad.concreteName != null) {
				List<Declare> declares = ad.concreteName.declares;
				for (Object dec : declares) {
					if (dec instanceof DeclareParents) {
						DeclareParents decp = (DeclareParents) dec;
						TypePattern[] newparents = decp.getParents().getTypePatterns();
						for (TypePattern pattern : newparents) {
							UnresolvedType ut = pattern.getExactType();
							if (ut == null)
								continue;
							if (CharOperation.compareWith(typeDecl.binding.signature(), ut.getSignature().toCharArray()) == 0)
								return;
						}
					}
				}
			}
		}
		super.unusedPrivateType(typeDecl);
	}
	
	private final static char[] thisJoinPointName = "thisJoinPoint".toCharArray();
	private final static char[] thisJoinPointStaticPartName = "thisJoinPointStaticPart".toCharArray();
	private final static char[] thisEnclosingJoinPointStaticPartName = "thisEnclosingJoinPointStaticPart".toCharArray();
	private final static char[] thisAspectInstanceName = "thisAspectInstance".toCharArray();

	@Override
	public void uninitializedLocalVariable(LocalVariableBinding binding, ASTNode location, Scope scope) {
		if (CharOperation.equals(binding.name, thisJoinPointName) ||
			CharOperation.equals(binding.name, thisJoinPointStaticPartName) ||
			CharOperation.equals(binding.name, thisAspectInstanceName) || 
			CharOperation.equals(binding.name, thisEnclosingJoinPointStaticPartName)) {
			// If in advice, this is not a problem
			if (binding.declaringScope!=null && (binding.declaringScope.referenceContext() instanceof AdviceDeclaration ||
												 binding.declaringScope.referenceContext() instanceof IfMethodDeclaration)) {
				return;
			}
		}			
		super.uninitializedLocalVariable(binding, location, scope);
	}
	
	public void abstractMethodInConcreteClass(SourceTypeBinding type) {
		if (type.scope!=null && type.scope.referenceContext instanceof AspectDeclaration) {
			// TODO could put out an Aspect specific message here
			return;
		} 
		super.abstractMethodInConcreteClass(type);
	}

	// Don't warn if there is an ITD method/ctor from a privileged aspect
	public void unusedPrivateField(FieldDeclaration fieldDecl) {
		if (fieldDecl!=null && fieldDecl.binding != null && fieldDecl.binding.declaringClass != null) {
			ReferenceBinding type = fieldDecl.binding.declaringClass;

			ResolvedType weaverType = null;
			if (!type.isAnonymousType()) {
				weaverType = factory.fromEclipse(type);
			} else {
				weaverType = factory.fromEclipse(type.superclass());
			}
			Set checked = new HashSet();
			for (ConcreteTypeMunger m : weaverType.getInterTypeMungersIncludingSupers()) {
				ResolvedType theAspect = m.getAspectType();
				if (!checked.contains(theAspect)) {
					TypeBinding tb = factory.makeTypeBinding(m.getAspectType());
					// Let's check the privilegedHandler from that aspect
					if (tb instanceof SourceTypeBinding) { // BinaryTypeBinding is also a SourceTypeBinding ;)
						IPrivilegedHandler privilegedHandler = ((SourceTypeBinding) tb).privilegedHandler;
						if (privilegedHandler != null) {
							if (privilegedHandler.definesPrivilegedAccessToField(fieldDecl.binding)) {
								return;
							}
						} else if (theAspect instanceof ReferenceType) {
							// ResolvedMember rm = factory.makeResolvedMember(fieldDecl.binding);
							String fname = new String(fieldDecl.name);
							Collection/* ResolvedMember */privvies = ((ReferenceType) theAspect).getPrivilegedAccesses();
							// On an incremental compile the information is in the bcel delegate
							if (privvies != null) {
								for (Object privvy : privvies) {
									ResolvedMember priv = (ResolvedMember) privvy;
									if (priv.getName().equals(fname)) {
										return;
									}
								}
							}
						}
					}
					checked.add(theAspect);
				}
			}
		}
		super.unusedPrivateField(fieldDecl);
	}

	public void unusedPrivateMethod(AbstractMethodDeclaration methodDecl) {
		// don't output unused warnings for pointcuts...
		if (!(methodDecl instanceof PointcutDeclaration))
			super.unusedPrivateMethod(methodDecl);
	}

	public void caseExpressionMustBeConstant(Expression expression) {
		if (expression instanceof QualifiedNameReference) {
			QualifiedNameReference qnr = (QualifiedNameReference) expression;
			if (qnr.otherBindings != null && qnr.otherBindings.length > 0 && qnr.otherBindings[0] instanceof PrivilegedFieldBinding) {
				super.signalError(expression.sourceStart, expression.sourceEnd,
						"Fields accessible due to an aspect being privileged can not be used in switch statements");
				referenceContext.tagAsHavingErrors();
				return;
			}
		}
		super.caseExpressionMustBeConstant(expression);
	}

	public void unusedArgument(LocalDeclaration localDecl) {
		// don't warn if this is an aj synthetic arg
		String argType = new String(localDecl.type.resolvedType.signature());
		if (argType.startsWith("Lorg/aspectj/runtime/internal"))
			return;

		// If the unused argument is in a pointcut, don't report the problem (for now... pr148219)
		if (localDecl instanceof Argument) {
			Argument arg = (Argument) localDecl;
			if (arg.binding != null && arg.binding.declaringScope != null) {
				ReferenceContext context = arg.binding.declaringScope.referenceContext();
				if (context != null && context instanceof PointcutDeclaration)
					return;
			}
		}
		if (new String(localDecl.name).startsWith("ajc$")) {
			// Do not report problems for infrastructure variables beyond the users control - pr195090
			return;
		}
		super.unusedArgument(localDecl);
	}

	/**
	 * A side-effect of the way that we handle itds on default methods on top-most implementors of interfaces is that a class
	 * acquiring a final default ITD will erroneously report that it can't override its own member. This method detects that
	 * situation.
	 */
	public void finalMethodCannotBeOverridden(MethodBinding currentMethod, MethodBinding inheritedMethod) {
		if (currentMethod == inheritedMethod)
			return;
		super.finalMethodCannotBeOverridden(currentMethod, inheritedMethod);
	}

	/**
	 * The method verifier is a bit 'keen' and doesn't cope well with ITDMs which are of course to be considered a 'default'
	 * implementation if the target type doesn't supply one. This test may not be complete - it is possible that it should read if
	 * *either* is an ITD...but I dont have a testcase that shows that is required. yet. (pr115788)
	 */
	public void duplicateInheritedMethods(SourceTypeBinding type, MethodBinding inheritedMethod1, MethodBinding inheritedMethod2, boolean isJava8) {
		if (inheritedMethod1 instanceof InterTypeMethodBinding || inheritedMethod2 instanceof InterTypeMethodBinding)
			return;
		if ((inheritedMethod1 instanceof ParameterizedMethodBinding)
				&& ((ParameterizedMethodBinding) inheritedMethod1).original() instanceof InterTypeMethodBinding)
			return;
		if ((inheritedMethod2 instanceof ParameterizedMethodBinding)
				&& ((ParameterizedMethodBinding) inheritedMethod2).original() instanceof InterTypeMethodBinding)
			return;
		super.duplicateInheritedMethods(type, inheritedMethod1, inheritedMethod2, isJava8);
	}

	/**
	 * All problems end up routed through here at some point...
	 */
	public IProblem createProblem(char[] fileName, int problemId, String[] problemArguments, String[] messageArguments,
			int severity, int problemStartPosition, int problemEndPosition, int lineNumber) {
		IProblem problem = super.createProblem(fileName, problemId, problemArguments, messageArguments, severity,
				problemStartPosition, problemEndPosition, lineNumber, 0);
		if (factory.getWorld().isInPinpointMode()) {
			MessageIssued ex = new MessageIssued();
			ex.fillInStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			StringBuffer sb = new StringBuffer();
			sb.append(CompilationAndWeavingContext.getCurrentContext());
			sb.append(sw.toString());
			problem = new PinpointedProblem(problem, sb.toString());
		}
		return problem;
	}

	private static class MessageIssued extends RuntimeException {
		public String getMessage() {
			return "message issued...";
		}
	}

	private static class PinpointedProblem implements IProblem {

		private IProblem delegate;
		private String message;

		public PinpointedProblem(IProblem aProblem, String pinpoint) {
			this.delegate = aProblem;
			// if this was a problem that came via the weaver, it will already have
			// pinpoint info, don't do it twice...
			if (!delegate.getMessage().contains("message issued...")) {
				this.message = delegate.getMessage() + "\n" + pinpoint;
			} else {
				this.message = delegate.getMessage();
			}
		}

		public String[] getArguments() {
			return delegate.getArguments();
		}

		public int getID() {
			return delegate.getID();
		}

		public String getMessage() {
			return message;
		}

		public char[] getOriginatingFileName() {
			return delegate.getOriginatingFileName();
		}

		public int getSourceEnd() {
			return delegate.getSourceEnd();
		}

		public int getSourceLineNumber() {
			return delegate.getSourceLineNumber();
		}

		public int getSourceStart() {
			return delegate.getSourceStart();
		}

		public boolean isError() {
			return delegate.isError();
		}

		public boolean isWarning() {
			return delegate.isWarning();
		}

		public void setSourceEnd(int sourceEnd) {
			delegate.setSourceEnd(sourceEnd);
		}

		public void setSourceLineNumber(int lineNumber) {
			delegate.setSourceLineNumber(lineNumber);
		}

		public void setSourceStart(int sourceStart) {
			delegate.setSourceStart(sourceStart);
		}

		public void setSeeAlsoProblems(IProblem[] problems) {
			delegate.setSeeAlsoProblems(problems);
		}

		public IProblem[] seeAlso() {
			return delegate.seeAlso();
		}

		public void setSupplementaryMessageInfo(String msg) {
			delegate.setSupplementaryMessageInfo(msg);
		}

		public String getSupplementaryMessageInfo() {
			return delegate.getSupplementaryMessageInfo();
		}

		@Override
		public boolean isInfo() {
			return delegate.isInfo();
		}
	}

	public void duplicateMethodInType(AbstractMethodDeclaration methodDecl, boolean equalParameters, int severity) {
		if (new String(methodDecl.selector).startsWith("ajc$interMethod")) {
			// this is an ITD clash and will be reported in another way by AspectJ (173602)
			return;
		}
		super.duplicateMethodInType(methodDecl, equalParameters, severity);
	}

	// pr246393 - if we are going to complain about privileged, we clearly don't know what is going on, so don't
	// confuse the user
	public void parseErrorInsertAfterToken(int start, int end, int currentKind, char[] errorTokenSource, String errorTokenName,
			String expectedToken) {
		if (expectedToken.equals("privileged") || expectedToken.equals("around")) {
			super.parseErrorNoSuggestion(start, end, currentKind, errorTokenSource, errorTokenName);
		} else {
			super.parseErrorInsertAfterToken(start, end, currentKind, errorTokenSource, errorTokenName, expectedToken);
		}
	}

	public void missingValueForAnnotationMember(Annotation annotation, char[] memberName) {
		if (referenceContext instanceof DeclareAnnotationDeclaration) {
			// If a remover then the values are not necessary
			if (((DeclareAnnotationDeclaration) referenceContext).isRemover()) {
				return;
			}
		}
		super.missingValueForAnnotationMember(annotation, memberName);
	}

}
