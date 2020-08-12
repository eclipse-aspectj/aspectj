/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation Ltd
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer  initial implementation 
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseScope;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.bridge.context.CompilationAndWeavingContext;
import org.aspectj.bridge.context.ContextToken;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.ParserException;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.Pointcut;

public class ValidateAtAspectJAnnotationsVisitor extends ASTVisitor {

	private static final char[] beforeAdviceSig = "Lorg/aspectj/lang/annotation/Before;".toCharArray();
	private static final char[] afterAdviceSig = "Lorg/aspectj/lang/annotation/After;".toCharArray();
	private static final char[] afterReturningAdviceSig = "Lorg/aspectj/lang/annotation/AfterReturning;".toCharArray();
	private static final char[] afterThrowingAdviceSig = "Lorg/aspectj/lang/annotation/AfterThrowing;".toCharArray();
	private static final char[] aroundAdviceSig = "Lorg/aspectj/lang/annotation/Around;".toCharArray();
	private static final char[] pointcutSig = "Lorg/aspectj/lang/annotation/Pointcut;".toCharArray();
	private static final char[] aspectSig = "Lorg/aspectj/lang/annotation/Aspect;".toCharArray();
	private static final char[] declareParentsSig = "Lorg/aspectj/lang/annotation/DeclareParents;".toCharArray();
	private static final char[] adviceNameSig = "Lorg/aspectj/lang/annotation/AdviceName;".toCharArray();
	// private static final char[] orgAspectJLangAnnotation =
	// "org/aspectj/lang/annotation/".toCharArray();
	private static final char[] voidType = "void".toCharArray();
	private static final char[] booleanType = "boolean".toCharArray();
	private static final char[] joinPoint = "Lorg/aspectj/lang/JoinPoint;".toCharArray();
	private static final char[] joinPointStaticPart = "Lorg/aspectj/lang/JoinPoint$StaticPart;".toCharArray();
	private static final char[] joinPointEnclosingStaticPart = "Lorg/aspectj/lang/JoinPoint$EnclosingStaticPart;".toCharArray();
	private static final char[] proceedingJoinPoint = "Lorg/aspectj/lang/ProceedingJoinPoint;".toCharArray();
	// private static final char[][] adviceSigs = new char[][] {
	// beforeAdviceSig, afterAdviceSig, afterReturningAdviceSig,
	// afterThrowingAdviceSig, aroundAdviceSig };

	private final CompilationUnitDeclaration unit;
	private final Stack typeStack = new Stack();
	private AspectJAnnotations ajAnnotations;

	public ValidateAtAspectJAnnotationsVisitor(CompilationUnitDeclaration unit) {
		this.unit = unit;
	}

	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		typeStack.push(localTypeDeclaration);
		ajAnnotations = new AspectJAnnotations(localTypeDeclaration.annotations);
		checkTypeDeclaration(localTypeDeclaration);
		return true;
	}

	public void endVisit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		typeStack.pop();
	}

	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		typeStack.push(memberTypeDeclaration);
		ajAnnotations = new AspectJAnnotations(memberTypeDeclaration.annotations);
		checkTypeDeclaration(memberTypeDeclaration);
		return true;
	}

	public void endVisit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		typeStack.pop();
	}

	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		typeStack.push(typeDeclaration);
		ajAnnotations = new AspectJAnnotations(typeDeclaration.annotations);
		checkTypeDeclaration(typeDeclaration);
		return true;
	}

	public void endVisit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		typeStack.pop();
	}

	private void checkTypeDeclaration(TypeDeclaration typeDecl) {
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(
				CompilationAndWeavingContext.VALIDATING_AT_ASPECTJ_ANNOTATIONS, typeDecl.name);
		if (!(typeDecl instanceof AspectDeclaration)) {
			if (ajAnnotations.hasAspectAnnotation) {
				validateAspectDeclaration(typeDecl);
			} else {
				// check that class doesn't extend aspect
				TypeReference parentRef = typeDecl.superclass;
				if (parentRef != null) {
					TypeBinding parentBinding = parentRef.resolvedType;
					if (parentBinding instanceof SourceTypeBinding) {
						SourceTypeBinding parentSTB = (SourceTypeBinding) parentBinding;
						if (parentSTB.scope != null) {
							TypeDeclaration parentDecl = parentSTB.scope.referenceContext;
							if (isAspect(parentDecl)) {
								typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart, typeDecl.sourceEnd,
										"a class cannot extend an aspect");
							}
						}
					}
				}
			}
		} else {
			// check that aspect doesn't have @Aspect annotation, we've already
			// added on ourselves.
			if (ajAnnotations.hasMultipleAspectAnnotations) {
				typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart, typeDecl.sourceEnd,
						"aspects cannot have @Aspect annotation");
			}
		}
		CompilationAndWeavingContext.leavingPhase(tok);
	}

	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		ajAnnotations = new AspectJAnnotations(fieldDeclaration.annotations);
		if (ajAnnotations.hasDeclareParents && !insideAspect()) {
			scope.problemReporter().signalError(fieldDeclaration.sourceStart, fieldDeclaration.sourceEnd,
					"DeclareParents can only be used inside an aspect type");
		}
		return true;
	}

	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		if (methodDeclaration.hasErrors()) {
			return false;
		}
		ContextToken tok = CompilationAndWeavingContext.enteringPhase(
				CompilationAndWeavingContext.VALIDATING_AT_ASPECTJ_ANNOTATIONS, methodDeclaration.selector);
		ajAnnotations = new AspectJAnnotations(methodDeclaration.annotations);
		if (!methodDeclaration.getClass().equals(AjMethodDeclaration.class)) {
			// simply test for innapropriate use of annotations on code-style
			// members
			if (methodDeclaration instanceof PointcutDeclaration) {
				if (ajAnnotations.hasMultiplePointcutAnnotations || ajAnnotations.hasAdviceAnnotation
						|| ajAnnotations.hasAspectAnnotation || ajAnnotations.hasAdviceNameAnnotation) {
					methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart,
							methodDeclaration.sourceEnd, "@AspectJ annotations cannot be declared on this aspect member");
				}
			} else if (methodDeclaration instanceof AdviceDeclaration) {
				if (ajAnnotations.hasMultipleAdviceAnnotations || ajAnnotations.hasAspectAnnotation
						|| ajAnnotations.hasPointcutAnnotation) {
					methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart,
							methodDeclaration.sourceEnd, "Only @AdviceName AspectJ annotation allowed on advice");
				}
			} else {
				if (ajAnnotations.hasAspectJAnnotations()) {
					methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart,
							methodDeclaration.sourceEnd, "@AspectJ annotations cannot be declared on this aspect member");
				}
			}
			CompilationAndWeavingContext.leavingPhase(tok);
			return false;
		}

		if (ajAnnotations.hasAdviceAnnotation) {
			validateAdvice(methodDeclaration);
		} else if (ajAnnotations.hasPointcutAnnotation) {
			convertToPointcutDeclaration(methodDeclaration, scope);
		}
		CompilationAndWeavingContext.leavingPhase(tok);
		return false;
	}

	// private boolean isAspectJAnnotation(Annotation ann) {
	// if (ann.resolvedType == null) return false;
	// char[] sig = ann.resolvedType.signature();
	// return CharOperation.contains(orgAspectJLangAnnotation, sig);
	// }

	private boolean insideAspect() {
		if (typeStack.empty())
			return false;
		TypeDeclaration typeDecl = (TypeDeclaration) typeStack.peek();
		return isAspect(typeDecl);
	}

	private boolean isAspect(TypeDeclaration typeDecl) {
		if (typeDecl instanceof AspectDeclaration)
			return true;
		return new AspectJAnnotations(typeDecl.annotations).hasAspectAnnotation;
	}

	/**
	 * aspect must be public nested aspect must be static cannot extend a concrete aspect pointcut in perclause must be good.
	 */
	private void validateAspectDeclaration(TypeDeclaration typeDecl) {
		if (typeStack.size() > 1) {
			// it's a nested aspect
			if (!Modifier.isStatic(typeDecl.modifiers)) {
				typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart, typeDecl.sourceEnd,
						"inner aspects must be static");
				return;
			}
		}

		SourceTypeBinding binding = typeDecl.binding;
		if (binding != null) {
			if (binding.isEnum() || binding.isInterface() || binding.isAnnotationType()) {
				typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart, typeDecl.sourceEnd,
						"only classes can have an @Aspect annotation");
			}
		}

		// FIXME AV - do we really want that
		// if (!Modifier.isPublic(typeDecl.modifiers)) {
		// typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart,
		// typeDecl.sourceEnd,"@Aspect class must be public");
		// }

		TypeReference parentRef = typeDecl.superclass;
		if (parentRef != null) {
			TypeBinding parentBinding = parentRef.resolvedType;
			if (parentBinding instanceof SourceTypeBinding) {
				SourceTypeBinding parentSTB = (SourceTypeBinding) parentBinding;
				if (parentSTB.scope != null) { // scope is null if its a
					// binarytypebinding (in AJ
					// world, thats a subclass of
					// SourceTypeBinding)
					TypeDeclaration parentDecl = parentSTB.scope.referenceContext;
					if (isAspect(parentDecl) && !Modifier.isAbstract(parentDecl.modifiers)) {
						typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart, typeDecl.sourceEnd,
								"cannot extend a concrete aspect");
					}
				}
			}
		}

		Annotation aspectAnnotation = ajAnnotations.aspectAnnotation;

		int[] pcLoc = new int[2];
		String perClause = getStringLiteralFor("value", aspectAnnotation, pcLoc);
		// AspectDeclaration aspectDecl = new
		// AspectDeclaration(typeDecl.compilationResult);

		try {
			if (perClause != null && !perClause.equals("")) {
				ISourceContext context = new EclipseSourceContext(unit.compilationResult, pcLoc[0]);
				Pointcut pc = new PatternParser(perClause, context).maybeParsePerClause();
				FormalBinding[] bindings = new FormalBinding[0];
				if (pc != null)
					pc.resolve(new EclipseScope(bindings, typeDecl.scope));
			}
		} catch (ParserException pEx) {
			typeDecl.scope.problemReporter().parseError(pcLoc[0] + pEx.getLocation().getStart(),
					pcLoc[0] + pEx.getLocation().getEnd(), -1, perClause.toCharArray(), perClause,
					new String[] { pEx.getMessage() });
		}
	}

	/**
	 * 1) Advice must be public 2) Advice must have a void return type if not around advice 3) Advice must not have any other @AspectJ
	 * annotations 4) After throwing advice must declare the thrown formal 5) After returning advice must declare the returning
	 * formal 6) Advice must not be static
	 */
	private void validateAdvice(MethodDeclaration methodDeclaration) {

		if (!insideAspect()) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart, methodDeclaration.sourceEnd,
					"Advice must be declared inside an aspect type");
		}

		if (!Modifier.isPublic(methodDeclaration.modifiers)) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart, methodDeclaration.sourceEnd,
					"advice must be public");
		}

		if (Modifier.isStatic(methodDeclaration.modifiers)) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart, methodDeclaration.sourceEnd,
					"advice can not be declared static");
		}

		if (ajAnnotations.hasMultipleAdviceAnnotations) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(ajAnnotations.duplicateAdviceAnnotation);
		}
		if (ajAnnotations.hasPointcutAnnotation) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(ajAnnotations.pointcutAnnotation);
		}
		if (ajAnnotations.hasAspectAnnotation) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(ajAnnotations.aspectAnnotation);
		}
		if (ajAnnotations.hasAdviceNameAnnotation) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(ajAnnotations.adviceNameAnnotation);
		}

		if (ajAnnotations.adviceKind != AdviceKind.Around) {
			ensureVoidReturnType(methodDeclaration);
		}

		if (ajAnnotations.adviceKind == AdviceKind.AfterThrowing) {
			int[] throwingLocation = new int[2];
			String thrownFormal = getStringLiteralFor("throwing", ajAnnotations.adviceAnnotation, throwingLocation);
			if (thrownFormal != null) {
				// Argument[] arguments = methodDeclaration.arguments;
				if (!toArgumentNames(methodDeclaration.arguments).contains(thrownFormal)) {
					methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart,
							methodDeclaration.sourceEnd,
							"throwing formal '" + thrownFormal + "' must be declared as a parameter in the advice signature");
				}
			}
		}

		if (ajAnnotations.adviceKind == AdviceKind.AfterReturning) {
			int[] throwingLocation = new int[2];
			String returningFormal = getStringLiteralFor("returning", ajAnnotations.adviceAnnotation, throwingLocation);
			if (returningFormal != null) {
				if (!toArgumentNames(methodDeclaration.arguments).contains(returningFormal)) {
					methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart,
							methodDeclaration.sourceEnd,
							"returning formal '" + returningFormal + "' must be declared as a parameter in the advice signature");
				}
			}
		}

		resolveAndSetPointcut(methodDeclaration, ajAnnotations.adviceAnnotation);

	}

	/**
	 * Get the argument names as a string list
	 * 
	 * @param arguments
	 * @return argument names (possibly empty)
	 */
	private List toArgumentNames(Argument[] arguments) {
		List names = new ArrayList();
		if (arguments == null) {
			return names;
		} else {
			for (Argument argument : arguments) {
				names.add(new String(argument.name));
			}
			return names;
		}
	}

	private void resolveAndSetPointcut(MethodDeclaration methodDeclaration, Annotation adviceAnn) {
		int[] pcLocation = new int[2];
		String pointcutExpression = getStringLiteralFor("pointcut", adviceAnn, pcLocation);
		if (pointcutExpression == null)
			pointcutExpression = getStringLiteralFor("value", adviceAnn, pcLocation);
		try {
			// +1 to give first char of pointcut string
			ISourceContext context = new EclipseSourceContext(unit.compilationResult, pcLocation[0] + 1);
			if (pointcutExpression == null) {
				methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart,
						methodDeclaration.sourceEnd, "the advice annotation must specify a pointcut value");
				return;
			}
			PatternParser pp = new PatternParser(pointcutExpression, context);
			Pointcut pc = pp.parsePointcut();
			pp.checkEof();
			FormalBinding[] bindings = buildFormalAdviceBindingsFrom(methodDeclaration);
			pc.resolve(new EclipseScope(bindings, methodDeclaration.scope));
			EclipseFactory factory = EclipseFactory.fromScopeLookupEnvironment(methodDeclaration.scope);
			// now create a ResolvedPointcutDefinition,make an attribute out of
			// it, and add it to the method
			UnresolvedType[] paramTypes = new UnresolvedType[bindings.length];
			for (int i = 0; i < paramTypes.length; i++)
				paramTypes[i] = bindings[i].getType();
			ResolvedPointcutDefinition resPcutDef = new ResolvedPointcutDefinition(factory.fromBinding(((TypeDeclaration) typeStack
					.peek()).binding), methodDeclaration.modifiers, "anonymous", paramTypes, pc);
			AjAttribute attr = new AjAttribute.PointcutDeclarationAttribute(resPcutDef);
			((AjMethodDeclaration) methodDeclaration).addAttribute(new EclipseAttributeAdapter(attr));
		} catch (ParserException pEx) {
			methodDeclaration.scope.problemReporter().parseError(pcLocation[0] + pEx.getLocation().getStart(),
					pcLocation[0] + pEx.getLocation().getEnd(), -1, pointcutExpression.toCharArray(), pointcutExpression,
					new String[] { pEx.getMessage() });
		}
	}

	private void ensureVoidReturnType(MethodDeclaration methodDeclaration) {
		boolean returnsVoid = true;
		if ((methodDeclaration.returnType instanceof SingleTypeReference)) {
			SingleTypeReference retType = (SingleTypeReference) methodDeclaration.returnType;
			if (!CharOperation.equals(voidType, retType.token)) {
				returnsVoid = false;
			}
		} else {
			returnsVoid = false;
		}
		if (!returnsVoid) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
					methodDeclaration.returnType.sourceEnd, "This advice must return void");
		}
	}

	private FormalBinding[] buildFormalAdviceBindingsFrom(MethodDeclaration mDecl) {
		if (mDecl.arguments == null)
			return new FormalBinding[0];
		if (mDecl.binding == null)
			return new FormalBinding[0];
		EclipseFactory factory = EclipseFactory.fromScopeLookupEnvironment(mDecl.scope);
		String extraArgName = maybeGetExtraArgName();
		if (extraArgName == null)
			extraArgName = "";
		FormalBinding[] ret = new FormalBinding[mDecl.arguments.length];
		for (int i = 0; i < mDecl.arguments.length; i++) {
			Argument arg = mDecl.arguments[i];
			String name = new String(arg.name);
			TypeBinding argTypeBinding = mDecl.binding.parameters[i];
			UnresolvedType type = factory.fromBinding(argTypeBinding);
			if (CharOperation.equals(joinPoint, argTypeBinding.signature())
					|| CharOperation.equals(joinPointStaticPart, argTypeBinding.signature())
					|| CharOperation.equals(joinPointEnclosingStaticPart, argTypeBinding.signature())
					|| CharOperation.equals(proceedingJoinPoint, argTypeBinding.signature()) || name.equals(extraArgName)) {
				ret[i] = new FormalBinding.ImplicitFormalBinding(type, name, i);
			} else {
				ret[i] = new FormalBinding(type, name, i, arg.sourceStart, arg.sourceEnd);
			}
		}
		return ret;
	}

	private String maybeGetExtraArgName() {
		String argName = null;
		if (ajAnnotations.adviceKind == AdviceKind.AfterReturning) {
			argName = getStringLiteralFor("returning", ajAnnotations.adviceAnnotation, new int[2]);
		} else if (ajAnnotations.adviceKind == AdviceKind.AfterThrowing) {
			argName = getStringLiteralFor("throwing", ajAnnotations.adviceAnnotation, new int[2]);
		}
		return argName;
	}

	private String getStringLiteralFor(String memberName, Annotation inAnnotation, int[] location) {
		if (inAnnotation instanceof SingleMemberAnnotation && memberName.equals("value")) {
			SingleMemberAnnotation sma = (SingleMemberAnnotation) inAnnotation;
			if (sma.memberValue instanceof StringLiteral) {
				StringLiteral sv = (StringLiteral) sma.memberValue;
				location[0] = sv.sourceStart;
				location[1] = sv.sourceEnd;
				return new String(sv.source());
			} else if (sma.memberValue instanceof NameReference
					&& (((NameReference) sma.memberValue).binding instanceof FieldBinding)) {
				Binding b = ((NameReference) sma.memberValue).binding;
				Constant c = ((FieldBinding) b).constant();
				return c.stringValue();
			}
		}
		if (!(inAnnotation instanceof NormalAnnotation))
			return null;
		NormalAnnotation ann = (NormalAnnotation) inAnnotation;
		MemberValuePair[] mvps = ann.memberValuePairs;
		if (mvps == null)
			return null;
		for (MemberValuePair mvp : mvps) {
			if (CharOperation.equals(memberName.toCharArray(), mvp.name)) {
				if (mvp.value instanceof StringLiteral) {
					StringLiteral sv = (StringLiteral) mvp.value;
					location[0] = sv.sourceStart;
					location[1] = sv.sourceEnd;
					return new String(sv.source());
				}
			}
		}
		return null;
	}

	private void convertToPointcutDeclaration(MethodDeclaration methodDeclaration, ClassScope scope) {
		TypeDeclaration typeDecl = (TypeDeclaration) typeStack.peek();
		if (typeDecl.binding != null) {
			if (!typeDecl.binding.isClass()) {
				methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart, methodDeclaration.sourceEnd,
						"pointcuts can only be declared in a class or an aspect");
			}
		}

		if (methodDeclaration.thrownExceptions != null && methodDeclaration.thrownExceptions.length > 0) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart, methodDeclaration.sourceEnd,
					"pointcuts cannot throw exceptions!");
		}

		PointcutDeclaration pcDecl = new PointcutDeclaration(unit.compilationResult);
		copyAllFields(methodDeclaration, pcDecl);

		if (ajAnnotations.hasAdviceAnnotation) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(ajAnnotations.adviceAnnotation);
		}
		if (ajAnnotations.hasAspectAnnotation) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(ajAnnotations.aspectAnnotation);
		}
		if (ajAnnotations.hasAdviceNameAnnotation) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(ajAnnotations.adviceNameAnnotation);
		}

		boolean noValueSupplied = true;
		boolean containsIfPcd = false;
		int[] pcLocation = new int[2];
		String pointcutExpression = getStringLiteralFor("value", ajAnnotations.pointcutAnnotation, pcLocation);
		try {
			ISourceContext context = new EclipseSourceContext(unit.compilationResult, pcLocation[0]);
			Pointcut pc = null;// abstract
			if (pointcutExpression == null || pointcutExpression.length() == 0) {
				noValueSupplied = true; // matches nothing pointcut
			} else {
				noValueSupplied = false;
				pc = new PatternParser(pointcutExpression, context).parsePointcut();
			}
			pcDecl.pointcutDesignator = (pc == null) ? null : new PointcutDesignator(pc);
			pcDecl.setGenerateSyntheticPointcutMethod();
			TypeDeclaration onType = (TypeDeclaration) typeStack.peek();
			pcDecl.postParse(onType);
			// EclipseFactory factory =
			// EclipseFactory.fromScopeLookupEnvironment
			// (methodDeclaration.scope);
			// int argsLength = methodDeclaration.arguments == null ? 0 :
			// methodDeclaration.arguments.length;
			FormalBinding[] bindings = buildFormalAdviceBindingsFrom(methodDeclaration);
			// FormalBinding[] bindings = new FormalBinding[argsLength];
			// for (int i = 0, len = bindings.length; i < len; i++) {
			// Argument arg = methodDeclaration.arguments[i];
			// String name = new String(arg.name);
			// UnresolvedType type =
			// factory.fromBinding(methodDeclaration.binding.parameters[i]);
			// bindings[i] = new FormalBinding(type, name, i, arg.sourceStart,
			// arg.sourceEnd, "unknown");
			// }
			swap(onType, methodDeclaration, pcDecl);
			if (pc != null) {
				// has an expression
				EclipseScope eScope = new EclipseScope(bindings, methodDeclaration.scope);
				char[] packageName = null;
				if (typeDecl.binding != null && typeDecl.binding.getPackage() != null) {
					packageName = typeDecl.binding.getPackage().readableName();
				}
				eScope.setLimitedImports(packageName);
				pc.resolve(eScope);
				HasIfPCDVisitor ifFinder = new HasIfPCDVisitor();
				pc.traverse(ifFinder, null);
				containsIfPcd = ifFinder.containsIfPcd;
			}
		} catch (ParserException pEx) {
			methodDeclaration.scope.problemReporter().parseError(pcLocation[0] + pEx.getLocation().getStart(),
					pcLocation[0] + pEx.getLocation().getEnd(), -1, pointcutExpression.toCharArray(), pointcutExpression,
					new String[] { pEx.getMessage() });
		}

		boolean returnsVoid = false;
		boolean returnsBoolean = false;
		if ((methodDeclaration.returnType instanceof SingleTypeReference)) {
			SingleTypeReference retType = (SingleTypeReference) methodDeclaration.returnType;
			if (CharOperation.equals(voidType, retType.token))
				returnsVoid = true;
			if (CharOperation.equals(booleanType, retType.token))
				returnsBoolean = true;
		}
		if (!returnsVoid && !containsIfPcd) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
					methodDeclaration.returnType.sourceEnd,
					"Methods annotated with @Pointcut must return void unless the pointcut contains an if() expression");
		}
		if (!returnsBoolean && containsIfPcd) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
					methodDeclaration.returnType.sourceEnd,
					"Methods annotated with @Pointcut must return boolean when the pointcut contains an if() expression");
		}

		if (methodDeclaration.statements != null && methodDeclaration.statements.length > 0 && !containsIfPcd) {
			methodDeclaration.scope.problemReporter()
					.signalError(methodDeclaration.returnType.sourceStart, methodDeclaration.returnType.sourceEnd,
							"Pointcuts without an if() expression should have an empty method body");
		}

		if (pcDecl.pointcutDesignator == null) {
			if (Modifier.isAbstract(methodDeclaration.modifiers) || noValueSupplied // this
			// is
			// a
			// matches
			// nothing
			// pointcut
			// those 2 checks makes sense for aop.xml concretization but NOT for
			// regular abstraction of pointcut
			// && returnsVoid
			// && (methodDeclaration.arguments == null ||
			// methodDeclaration.arguments.length == 0)) {
			) {
				// fine
			} else {
				methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
						methodDeclaration.returnType.sourceEnd,
						"Method annotated with @Pointcut() for abstract pointcut must be abstract");
			}
		} else if (Modifier.isAbstract(methodDeclaration.modifiers)) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
					methodDeclaration.returnType.sourceEnd,
					"Method annotated with non abstract @Pointcut(\"" + pointcutExpression + "\") is abstract");
		}
	}

	private void copyAllFields(MethodDeclaration from, MethodDeclaration to) {
		to.annotations = from.annotations;
		to.arguments = from.arguments;
		to.binding = from.binding;
		to.bits = from.bits;
		to.bodyEnd = from.bodyEnd;
		to.bodyStart = from.bodyStart;
		to.declarationSourceEnd = from.declarationSourceEnd;
		to.declarationSourceStart = from.declarationSourceStart;
		to.explicitDeclarations = from.explicitDeclarations;
		to.ignoreFurtherInvestigation = from.ignoreFurtherInvestigation;
		to.javadoc = from.javadoc;
		to.modifiers = from.modifiers;
		to.modifiersSourceStart = from.modifiersSourceStart;
		to.returnType = from.returnType;
		to.scope = from.scope;
		to.selector = from.selector;
		to.sourceEnd = from.sourceEnd;
		to.sourceStart = from.sourceStart;
		to.statements = from.statements;
		to.thrownExceptions = from.thrownExceptions;
		to.typeParameters = from.typeParameters;
	}

	private void swap(TypeDeclaration inType, MethodDeclaration thisDeclaration, MethodDeclaration forThatDeclaration) {
		for (int i = 0; i < inType.methods.length; i++) {
			if (inType.methods[i] == thisDeclaration) {
				inType.methods[i] = forThatDeclaration;
				break;
			}
		}
	}

	private static class AspectJAnnotations {
		boolean hasAdviceAnnotation = false;
		boolean hasPointcutAnnotation = false;
		boolean hasAspectAnnotation = false;
		boolean hasAdviceNameAnnotation = false;
		boolean hasDeclareParents = false;
		boolean hasMultipleAdviceAnnotations = false;
		boolean hasMultiplePointcutAnnotations = false;
		boolean hasMultipleAspectAnnotations = false;

		AdviceKind adviceKind = null;
		Annotation adviceAnnotation = null;
		Annotation pointcutAnnotation = null;
		Annotation aspectAnnotation = null;
		Annotation adviceNameAnnotation = null;

		Annotation duplicateAdviceAnnotation = null;
		Annotation duplicatePointcutAnnotation = null;
		Annotation duplicateAspectAnnotation = null;

		public AspectJAnnotations(Annotation[] annotations) {
			if (annotations == null)
				return;
			for (Annotation annotation : annotations) {
				if (annotation.resolvedType == null)
					continue; // user messed up annotation declaration
				char[] sig = annotation.resolvedType.signature();
				if (CharOperation.equals(afterAdviceSig, sig)) {
					adviceKind = AdviceKind.After;
					addAdviceAnnotation(annotation);
				} else if (CharOperation.equals(afterReturningAdviceSig, sig)) {
					adviceKind = AdviceKind.AfterReturning;
					addAdviceAnnotation(annotation);
				} else if (CharOperation.equals(afterThrowingAdviceSig, sig)) {
					adviceKind = AdviceKind.AfterThrowing;
					addAdviceAnnotation(annotation);
				} else if (CharOperation.equals(beforeAdviceSig, sig)) {
					adviceKind = AdviceKind.Before;
					addAdviceAnnotation(annotation);
				} else if (CharOperation.equals(aroundAdviceSig, sig)) {
					adviceKind = AdviceKind.Around;
					addAdviceAnnotation(annotation);
				} else if (CharOperation.equals(adviceNameSig, sig)) {
					hasAdviceNameAnnotation = true;
					adviceNameAnnotation = annotation;
				} else if (CharOperation.equals(declareParentsSig, sig)) {
					hasDeclareParents = true;
				} else if (CharOperation.equals(aspectSig, sig)) {
					if (hasAspectAnnotation) {
						hasMultipleAspectAnnotations = true;
						duplicateAspectAnnotation = annotation;
					} else {
						hasAspectAnnotation = true;
						aspectAnnotation = annotation;
					}
				} else if (CharOperation.equals(pointcutSig, sig)) {
					if (hasPointcutAnnotation) {
						hasMultiplePointcutAnnotations = true;
						duplicatePointcutAnnotation = annotation;
					} else {
						hasPointcutAnnotation = true;
						pointcutAnnotation = annotation;
					}

				}
			}
		}

		public boolean hasAspectJAnnotations() {
			return hasAdviceAnnotation || hasPointcutAnnotation || hasAdviceNameAnnotation || hasAspectAnnotation;
		}

		private void addAdviceAnnotation(Annotation annotation) {
			if (!hasAdviceAnnotation) {
				hasAdviceAnnotation = true;
				adviceAnnotation = annotation;
			} else {
				hasMultipleAdviceAnnotations = true;
				duplicateAdviceAnnotation = annotation;
			}
		}
	}

	private static class HasIfPCDVisitor extends AbstractPatternNodeVisitor {
		public boolean containsIfPcd = false;

		public Object visit(IfPointcut node, Object data) {
			containsIfPcd = true;
			return data;
		}
	}
}
