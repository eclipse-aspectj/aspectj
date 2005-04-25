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
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.FormalBinding;
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
	private static final char[] adviceNameSig = "Lorg/aspectj/lang/annotation/AdviceName;".toCharArray();
	private static final char[] orgAspectJLangAnnotation = "org/aspectj/lang/annotation/".toCharArray();
	private static final char[] voidType = "void".toCharArray();
	private static final char[] joinPoint = "Lorg/aspectj/lang/JoinPoint;".toCharArray();
	private static final char[] joinPointStaticPart = "Lorg/aspectj/lang/JoinPoint$StaticPart;".toCharArray();
	private static final char[] joinPointEnclosingStaticPart = "Lorg/aspectj/lang/JoinPoint$EnclosingStaticPart;".toCharArray();
	private static final char[] proceedingJoinPoint = "Lorg/aspectj/lang/ProceedingJoinPoint;".toCharArray();
	private static final char[][] adviceSigs = new char[][] {beforeAdviceSig,afterAdviceSig,afterReturningAdviceSig,afterThrowingAdviceSig,aroundAdviceSig};
	
	
	private CompilationUnitDeclaration unit;
	private Stack typeStack = new Stack();
	
	public ValidateAtAspectJAnnotationsVisitor(CompilationUnitDeclaration unit) {
		this.unit = unit;
	}
	
	public boolean visit(TypeDeclaration localTypeDeclaration, BlockScope scope) {
		typeStack.push(localTypeDeclaration);
		checkTypeDeclaration(localTypeDeclaration);
		return true;
	}
	
	public void endVisit(TypeDeclaration localTypeDeclaration,BlockScope scope) {
		typeStack.pop();
	}
	
	public boolean visit(TypeDeclaration memberTypeDeclaration,ClassScope scope) {
		typeStack.push(memberTypeDeclaration);
		checkTypeDeclaration(memberTypeDeclaration);
		return true;
	}
	
	public void endVisit(TypeDeclaration memberTypeDeclaration,ClassScope scope) {
		typeStack.pop();
	}
	
	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		typeStack.push(typeDeclaration);
		checkTypeDeclaration(typeDeclaration);
		return true;
	}
	
	public void endVisit(TypeDeclaration typeDeclaration,CompilationUnitScope scope) {
		typeStack.pop();
	}
	
	private void checkTypeDeclaration(TypeDeclaration typeDecl) {
		if (!(typeDecl instanceof AspectDeclaration)) {
			if (insideAspect()) {
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
								typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart,typeDecl.sourceEnd,"a class cannot extend an aspect");
							}
						}
					}
				}
			}
		} else {
			// check that aspect doesn't have @Aspect annotation
			boolean foundAspectAnnotation = false;
			for (int i = 0; i < typeDecl.annotations.length; i++) {
				if (typeDecl.annotations[i].resolvedType == null) continue;
				char[] sig = typeDecl.annotations[i].resolvedType.signature();
				if (CharOperation.equals(aspectSig,sig)) {
					if (!foundAspectAnnotation) {
						foundAspectAnnotation = true; // this is the one we added in the first visitor pass
					} else {
						//a second @Aspect annotation, user must have declared one...
						typeDecl.scope.problemReporter().signalError(
								typeDecl.sourceStart,
								typeDecl.sourceEnd,
								"aspects cannot have @Aspect annotation"
								);
					}
				}
			}
		}
	}
	
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		if (!methodDeclaration.getClass().equals(AjMethodDeclaration.class)) {
			// simply test for innapropriate use of annotations on code-style members
			if (!hasAspectJAnnotation(methodDeclaration)) return false;
			int numPointcutAnnotations = 0;
			int numAdviceAnnotations = 0;
			int numAdviceNameAnnotations = 0;
			for (int i=0; i < methodDeclaration.annotations.length; i++) {
				if (isAspectJAnnotation(methodDeclaration.annotations[i])) {
					if (CharOperation.equals(adviceNameSig,methodDeclaration.annotations[i].resolvedType.signature())) {
						numAdviceNameAnnotations++;
					} else if (CharOperation.equals(pointcutSig,methodDeclaration.annotations[i].resolvedType.signature())) {
						numPointcutAnnotations++;
					} else {
						for (int j = 0; j < adviceSigs.length; j++) { 
							if (CharOperation.equals(adviceSigs[j],methodDeclaration.annotations[i].resolvedType.signature())) {
								numAdviceAnnotations++;								
							}
						}
					}
				}
			}
			if (methodDeclaration instanceof PointcutDeclaration) {
				if (numPointcutAnnotations > 1 || numAdviceAnnotations > 0 || numAdviceNameAnnotations > 0) {
					methodDeclaration.scope.problemReporter().signalError(
							methodDeclaration.sourceStart,
							methodDeclaration.sourceEnd,
							"@AspectJ annotations cannot be declared on this aspect member");
				}
			} else if (methodDeclaration instanceof AdviceDeclaration) {
				if (numPointcutAnnotations > 0 || numAdviceAnnotations > 1) {
					methodDeclaration.scope.problemReporter().signalError(
										methodDeclaration.sourceStart,
										methodDeclaration.sourceEnd,
										"Only @AdviceName AspectJ annotation allowed on advice");
				}				
			} else {
				if (numPointcutAnnotations > 0 || numAdviceAnnotations > 0 || numAdviceNameAnnotations > 0) {
					methodDeclaration.scope.problemReporter().signalError(
							methodDeclaration.sourceStart,
							methodDeclaration.sourceEnd,
							"@AspectJ annotations cannot be declared on this aspect member");					
				}
			}
			return false;
		}
		if (isAnnotationStyleAdvice(methodDeclaration.annotations)) {
			validateAdvice(methodDeclaration);
		} else if (isAnnotationStylePointcut(methodDeclaration.annotations)) {
			convertToPointcutDeclaration(methodDeclaration,scope);
		}
		return false;
	}
	
	

	private boolean isAnnotationStyleAdvice(Annotation[] annotations) {
		if (annotations == null) return false;
		for (int i = 0; i < annotations.length; i++) {
			if (annotations[i].resolvedType == null) continue;
			char[] sig = annotations[i].resolvedType.signature();
			if (CharOperation.equals(beforeAdviceSig,sig) ||
				CharOperation.equals(afterAdviceSig,sig) ||
				CharOperation.equals(afterReturningAdviceSig,sig) ||
				CharOperation.equals(aroundAdviceSig,sig) ||
				CharOperation.equals(afterThrowingAdviceSig,sig)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isAnnotationStylePointcut(Annotation[] annotations) {
		if (annotations == null) return false;
		for (int i = 0; i < annotations.length; i++) {
			if (annotations[i].resolvedType == null) continue;
			char[] sig = annotations[i].resolvedType.signature();
			if (CharOperation.equals(pointcutSig,sig)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAspectJAnnotation(MethodDeclaration methodDecl) {
		if (methodDecl.annotations == null) return false;
		for (int i=0; i < methodDecl.annotations.length; i++) {
			if (isAspectJAnnotation(methodDecl.annotations[i])) return true;
		}
		return false;
	}
	
	private boolean isAspectJAnnotation(Annotation ann) {
		if (ann.resolvedType == null) return false;
		char[] sig = ann.resolvedType.signature();
		return CharOperation.contains(orgAspectJLangAnnotation, sig);
	}
	
	private boolean insideAspect() {
		if (typeStack.empty()) return false;
		TypeDeclaration typeDecl = (TypeDeclaration) typeStack.peek();
		return isAspect(typeDecl);
	}
	
	private boolean isAspect(TypeDeclaration typeDecl) {
		if (typeDecl instanceof AspectDeclaration) return true;
		return hasAspectAnnotation(typeDecl);			
	}

	private boolean hasAspectAnnotation(TypeDeclaration typeDecl) {
		if (typeDecl.annotations == null) return false;
		for (int i = 0; i < typeDecl.annotations.length; i++) {
			if (typeDecl.annotations[i].resolvedType == null) continue;
			char[] sig = typeDecl.annotations[i].resolvedType.signature();
			if (CharOperation.equals(aspectSig,sig)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * nested aspect must be static
	 * cannot extend a concrete aspect
	 * pointcut in perclause must be good.
	 */
	private void validateAspectDeclaration(TypeDeclaration typeDecl) {
		if (typeStack.size() > 1) {
			// it's a nested aspect
			if (!Modifier.isStatic(typeDecl.modifiers)) {
				typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart, typeDecl.sourceEnd, "inner aspects must be static");
				return;
			}
		}
		
		SourceTypeBinding binding = typeDecl.binding;
		if (binding != null) {
			if (binding.isEnum() || binding.isInterface() || binding.isAnnotationType()) {
				typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart,typeDecl.sourceEnd,"only classes can have an @Aspect annotation");
			}
		}
		
		TypeReference parentRef = typeDecl.superclass;
		if (parentRef != null) {
			TypeBinding parentBinding = parentRef.resolvedType;
			if (parentBinding instanceof SourceTypeBinding) {
				SourceTypeBinding parentSTB = (SourceTypeBinding) parentBinding;
				TypeDeclaration parentDecl = parentSTB.scope.referenceContext;
				if (isAspect(parentDecl) && !Modifier.isAbstract(parentDecl.modifiers)) {
					typeDecl.scope.problemReporter().signalError(typeDecl.sourceStart,typeDecl.sourceEnd,"cannot extend a concrete aspect");
				}			
			}
		}

		Annotation aspectAnnotation = null;
		for (int i = 0; i < typeDecl.annotations.length; i++) {
			if (typeDecl.annotations[i].resolvedType == null) continue;
			char[] sig = typeDecl.annotations[i].resolvedType.signature();
			if (CharOperation.equals(aspectSig,sig)) {
				aspectAnnotation = typeDecl.annotations[i];
				break;
			}
		}

		int[] pcLoc = new int[2];
		String perClause = getStringLiteralFor("value", aspectAnnotation, pcLoc);
		AspectDeclaration aspectDecl = new AspectDeclaration(typeDecl.compilationResult);

		try {
			if (perClause != null && !perClause.equals("")) {
				ISourceContext context = new EclipseSourceContext(unit.compilationResult,pcLoc[0]);
				Pointcut pc = new PatternParser(perClause,context).maybeParsePerClause();
			    FormalBinding[] bindings = new FormalBinding[0];
				if (pc != null) pc.resolve(new EclipseScope(bindings,typeDecl.scope));
			}
		} catch(ParserException pEx) {
			typeDecl.scope.problemReporter().parseError(
					pcLoc[0] + pEx.getLocation().getStart(),
					pcLoc[0] + pEx.getLocation().getEnd() ,
					-1, 
					perClause.toCharArray(), 
					perClause, 
					new String[] {pEx.getMessage()});
		}
	}
	
	/**
	 * 1) Advice must be public
	 * 2) Advice must have a void return type if not around advice
	 * 3) Advice must not have any other @AspectJ annotations
	 */
	private void validateAdvice(MethodDeclaration methodDeclaration) {
		
		if (!insideAspect()) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.sourceStart,
					  methodDeclaration.sourceEnd, 
					  "Advice must be declared inside an aspect type");			
		}
		
		if (!Modifier.isPublic(methodDeclaration.modifiers)) {
			methodDeclaration.scope.problemReporter()
				.signalError(methodDeclaration.sourceStart,methodDeclaration.sourceEnd,"advice must be public");
		}
				
		AdviceKind kind = null;
		Annotation adviceAnn = null;
		Annotation duplicateAnn = null;
		for(int i = 0; i < methodDeclaration.annotations.length; i++) {
			Annotation ann = methodDeclaration.annotations[i];
			if (isAspectJAnnotation(ann)) {
				if (adviceAnn != null) {
					duplicateAnn = ann;
					break;
				}
				if (CharOperation.equals(afterAdviceSig,ann.resolvedType.signature())) {
					kind = AdviceKind.After;
					adviceAnn = ann;
				} else if (CharOperation.equals(afterReturningAdviceSig,ann.resolvedType.signature())) {
					kind = AdviceKind.AfterReturning;
					adviceAnn = ann;
				} else if (CharOperation.equals(afterThrowingAdviceSig,ann.resolvedType.signature())) {
					kind = AdviceKind.AfterThrowing;
					adviceAnn = ann;
				} else if (CharOperation.equals(beforeAdviceSig,ann.resolvedType.signature())) {
					kind = AdviceKind.Before;
					adviceAnn = ann;
				} else if (CharOperation.equals(aroundAdviceSig,ann.resolvedType.signature())) {
					kind = AdviceKind.Around;
					adviceAnn = ann;
				} else if (CharOperation.equals(adviceNameSig,ann.resolvedType.signature())) {
					methodDeclaration.scope.problemReporter().signalError(
							ann.sourceStart,ann.sourceEnd, "AdviceName annotation cannot be used for advice defined using annotation style");
				}
			}
		}

		if (duplicateAnn != null) {
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(duplicateAnn);
		}

		if (kind != AdviceKind.Around) {
			ensureVoidReturnType(methodDeclaration);
		}	  

		resolveAndSetPointcut(methodDeclaration, adviceAnn);
		
	}

	private void resolveAndSetPointcut(MethodDeclaration methodDeclaration, Annotation adviceAnn) {
		int[] pcLocation = new int[2];
		String pointcutExpression = getStringLiteralFor("pointcut",adviceAnn,pcLocation);
		if (pointcutExpression == null) pointcutExpression = getStringLiteralFor("value",adviceAnn,pcLocation);
		try {
			ISourceContext context = new EclipseSourceContext(unit.compilationResult,pcLocation[0]);
			Pointcut pc = new PatternParser(pointcutExpression,context).parsePointcut();
			FormalBinding[] bindings = buildFormalAdviceBindingsFrom(methodDeclaration);
			pc.resolve(new EclipseScope(bindings,methodDeclaration.scope));
			// now create a ResolvedPointcutDefinition,make an attribute out of it, and add it to the method
			TypeX[] paramTypes = new TypeX[bindings.length];
			for (int i = 0; i < paramTypes.length; i++) paramTypes[i] = bindings[i].getType();
			ResolvedPointcutDefinition resPcutDef = 
				new ResolvedPointcutDefinition(
						EclipseFactory.fromBinding(((TypeDeclaration)typeStack.peek()).binding),
						methodDeclaration.modifiers,
						"anonymous",
						paramTypes,
						pc
				);
			AjAttribute attr = new AjAttribute.PointcutDeclarationAttribute(resPcutDef);
			((AjMethodDeclaration)methodDeclaration).addAttribute(new EclipseAttributeAdapter(attr));
		} catch(ParserException pEx) {
			methodDeclaration.scope.problemReporter().parseError(
					pcLocation[0] + pEx.getLocation().getStart(),
					pcLocation[0] + pEx.getLocation().getEnd() ,
					-1, 
					pointcutExpression.toCharArray(), 
					pointcutExpression, 
					new String[] {pEx.getMessage()});
		}
	}

	private void ensureVoidReturnType(MethodDeclaration methodDeclaration) {
		boolean returnsVoid = true;
		if ((methodDeclaration.returnType instanceof SingleTypeReference)) {
			SingleTypeReference retType = (SingleTypeReference) methodDeclaration.returnType;
			if (!CharOperation.equals(voidType,retType.token)) {
				returnsVoid = false;
			}
		} else {
			returnsVoid = false;
		}
		if (!returnsVoid) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
					  																  methodDeclaration.returnType.sourceEnd, 
			  																		  "This advice must return void");
		}
	}
	
	private FormalBinding[] buildFormalAdviceBindingsFrom(MethodDeclaration mDecl) {
		if (mDecl.arguments == null) return new FormalBinding[0];
		FormalBinding[] ret = new FormalBinding[mDecl.arguments.length];
		for (int i = 0; i < mDecl.arguments.length; i++) {
            Argument arg = mDecl.arguments[i];
            String name = new String(arg.name);
			TypeBinding argTypeBinding = mDecl.binding.parameters[i];
            TypeX type = EclipseFactory.fromBinding(argTypeBinding);
			if  (CharOperation.equals(joinPoint,argTypeBinding.signature()) ||
				 CharOperation.equals(joinPointStaticPart,argTypeBinding.signature()) ||
				 CharOperation.equals(joinPointEnclosingStaticPart,argTypeBinding.signature()) ||
				 CharOperation.equals(proceedingJoinPoint,argTypeBinding.signature())) {
				ret[i] = new FormalBinding.ImplicitFormalBinding(type,name,i);
			} else {
	            ret[i] = new FormalBinding(type, name, i, arg.sourceStart, arg.sourceEnd, "unknown");						
			}
		}
		return ret;
	}

	private String getStringLiteralFor(String memberName, Annotation inAnnotation, int[] location) {
		if (inAnnotation instanceof SingleMemberAnnotation && memberName.equals("value")) {
			SingleMemberAnnotation sma = (SingleMemberAnnotation) inAnnotation;
			if (sma.memberValue instanceof StringLiteral) {
				StringLiteral sv = (StringLiteral) sma.memberValue;
				location[0] = sv.sourceStart;
				location[1] = sv.sourceEnd;
				return new String(sv.source());
			}
		}
		if (! (inAnnotation instanceof NormalAnnotation)) return null;
		NormalAnnotation ann = (NormalAnnotation) inAnnotation;
		MemberValuePair[] mvps = ann.memberValuePairs;
		if (mvps == null) return null;
		for (int i = 0; i < mvps.length; i++) {
			if (CharOperation.equals(memberName.toCharArray(),mvps[i].name)) {
				if (mvps[i].value instanceof StringLiteral) {
					StringLiteral sv = (StringLiteral) mvps[i].value;
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
				methodDeclaration.scope.problemReporter()
					.signalError(methodDeclaration.sourceStart,methodDeclaration.sourceEnd,"pointcuts can only be declared in a class or an aspect");
			}
		}
		
		if (methodDeclaration.thrownExceptions != null && methodDeclaration.thrownExceptions.length > 0) {
			methodDeclaration.scope.problemReporter()
				.signalError(methodDeclaration.sourceStart,methodDeclaration.sourceEnd,"pointcuts cannot throw exceptions!");
		}
		
		PointcutDeclaration pcDecl = new PointcutDeclaration(unit.compilationResult);
		copyAllFields(methodDeclaration,pcDecl);

		Annotation pcutAnn = null;
		Annotation duplicateAnn = null;
		for(int i = 0; i < methodDeclaration.annotations.length; i++) {
			Annotation ann = methodDeclaration.annotations[i];
			if (isAspectJAnnotation(ann)) {
				if (pcutAnn != null) {
					duplicateAnn = ann;
					break;
				}
				if (CharOperation.equals(pointcutSig,ann.resolvedType.signature())) {
					pcutAnn = ann;
				} 
			}
		}

		if (duplicateAnn != null && !CharOperation.equals(pointcutSig,duplicateAnn.resolvedType.signature())) {
			// (duplicate annotations of same type are already reported)
			methodDeclaration.scope.problemReporter().disallowedTargetForAnnotation(duplicateAnn);
		}

		boolean returnsVoid = true;
		if ((methodDeclaration.returnType instanceof SingleTypeReference)) {
			SingleTypeReference retType = (SingleTypeReference) methodDeclaration.returnType;
			if (!CharOperation.equals(voidType,retType.token)) {
				returnsVoid = false;
			}
		} else {
			returnsVoid = false;
		}
		if (!returnsVoid) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
																					  methodDeclaration.returnType.sourceEnd, 
																					  "Methods annotated with @Pointcut must return void");
		}
		
		if (methodDeclaration.statements != null && methodDeclaration.statements.length > 0) {
			methodDeclaration.scope.problemReporter().signalError(methodDeclaration.returnType.sourceStart,
					  methodDeclaration.returnType.sourceEnd, 
					  "Pointcuts should have an empty method body");			
		}
		
		int[] pcLocation = new int[2];
		String pointcutExpression = getStringLiteralFor("value",pcutAnn,pcLocation);
		try {
			ISourceContext context = new EclipseSourceContext(unit.compilationResult,pcLocation[0]);
			Pointcut pc = new PatternParser(pointcutExpression,context).parsePointcut();
			pcDecl.pointcutDesignator = new PointcutDesignator(pc);
			pcDecl.setGenerateSyntheticPointcutMethod();
			TypeDeclaration onType = (TypeDeclaration) typeStack.peek();
			pcDecl.postParse(onType);
			int argsLength = methodDeclaration.arguments == null ? 0 : methodDeclaration.arguments.length;
 		    FormalBinding[] bindings = new FormalBinding[argsLength];
	        for (int i = 0, len = bindings.length; i < len; i++) {
	            Argument arg = methodDeclaration.arguments[i];
	            String name = new String(arg.name);
	            TypeX type = EclipseFactory.fromBinding(methodDeclaration.binding.parameters[i]);
	            bindings[i] = new FormalBinding(type, name, i, arg.sourceStart, arg.sourceEnd, "unknown");
	        }
			swap(onType,methodDeclaration,pcDecl);
			pc.resolve(new EclipseScope(bindings,methodDeclaration.scope));
		} catch(ParserException pEx) {
			methodDeclaration.scope.problemReporter().parseError(
					pcLocation[0] + pEx.getLocation().getStart(),
					pcLocation[0] + pEx.getLocation().getEnd() ,
					-1, 
					pointcutExpression.toCharArray(), 
					pointcutExpression, 
					new String[] {pEx.getMessage()});
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
		to.errorInSignature = from.errorInSignature;
		to.explicitDeclarations = from.explicitDeclarations;
		to.ignoreFurtherInvestigation = from.ignoreFurtherInvestigation;
		to.javadoc = from.javadoc;
		to.modifiers = from.modifiers;
		to.modifiersSourceStart = from.modifiersSourceStart;
		to.needFreeReturn = from.needFreeReturn;
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
			if (inType.methods[i]  == thisDeclaration) {
				inType.methods[i] = forThatDeclaration;
				break;
			}
		}
	}
}
