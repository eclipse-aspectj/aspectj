/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nieraj Singh
 *******************************************************************************/

package org.aspectj.org.eclipse.jdt.core.dom;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeConstructorDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.core.compiler.InvalidInputException;
import org.aspectj.org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.JavadocArgumentExpression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.weaver.patterns.DeclareParents;
import org.aspectj.weaver.patterns.DeclarePrecedence;
import org.aspectj.weaver.patterns.DeclareSoft;
import org.aspectj.weaver.patterns.ISignaturePattern;
import org.aspectj.weaver.patterns.PatternNode;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.TypePattern;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Internal class for converting internal compiler ASTs into public ASTs.
 */
@SuppressWarnings("unchecked")
public class AjASTConverter extends ASTConverter {

	public AjASTConverter(Map options, boolean resolveBindings, IProgressMonitor monitor) {
		super(options, resolveBindings, monitor);
	}

	public ASTNode convert(AdviceDeclaration adviceDeclaration) {
		// ajh02: method added
		org.aspectj.org.eclipse.jdt.core.dom.AdviceDeclaration adviceDecl = null;
		if (adviceDeclaration.kind.equals(AdviceKind.Before)) {
			adviceDecl = new org.aspectj.org.eclipse.jdt.core.dom.BeforeAdviceDeclaration(this.ast);
		} else if (adviceDeclaration.kind.equals(AdviceKind.After)) {
			adviceDecl = new org.aspectj.org.eclipse.jdt.core.dom.AfterAdviceDeclaration(this.ast);
		} else if (adviceDeclaration.kind.equals(AdviceKind.AfterThrowing)) {
			adviceDecl = new AfterThrowingAdviceDeclaration(this.ast);
			if (adviceDeclaration.extraArgument != null) {
				SingleVariableDeclaration throwing = convert(adviceDeclaration.extraArgument);
				((AfterThrowingAdviceDeclaration) adviceDecl).setThrowing(throwing);
			}
		} else if (adviceDeclaration.kind.equals(AdviceKind.AfterReturning)) {
			adviceDecl = new AfterReturningAdviceDeclaration(this.ast);
			if (adviceDeclaration.extraArgument != null) {
				SingleVariableDeclaration returning = convert(adviceDeclaration.extraArgument);
				((AfterReturningAdviceDeclaration) adviceDecl).setReturning(returning);
			}
		} else if (adviceDeclaration.kind.equals(AdviceKind.Around)) {
			adviceDecl = new AroundAdviceDeclaration(this.ast);
			// set the returnType
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = adviceDeclaration.returnType;
			if (typeReference != null) {
				Type returnType = convertType(typeReference);
				// get the positions of the right parenthesis
				setTypeForAroundAdviceDeclaration((AroundAdviceDeclaration) adviceDecl, returnType);
			}
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter[] typeParameters = adviceDeclaration.typeParameters();
			if (typeParameters != null) {
				switch (this.ast.apiLevel) {
				case AST.JLS2_INTERNAL:
					adviceDecl.setFlags(adviceDecl.getFlags() | ASTNode.MALFORMED);
					break;
				case AST.JLS3:
					for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParameter : typeParameters) {
						((AroundAdviceDeclaration) adviceDecl).typeParameters().add(convert(typeParameter));
					}
				}
			}
		}
		// set its javadoc, parameters, throws, pointcut and body
		org.aspectj.weaver.patterns.Pointcut pointcut = adviceDeclaration.pointcutDesignator.getPointcut();
		adviceDecl.setPointcut(convert(pointcut));
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference[] thrownExceptions = adviceDeclaration.thrownExceptions;
		if (thrownExceptions != null) {
			int thrownExceptionsLength = thrownExceptions.length;
			for (TypeReference thrownException : thrownExceptions) {
				adviceDecl.thrownExceptions().add(convert(thrownException));
			}
		}
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument[] parameters = adviceDeclaration.arguments;
		if (parameters != null) {
			int parametersLength = parameters.length;
			for (Argument parameter : parameters) {
				adviceDecl.parameters().add(convert(parameter));
			}
		}
		int start = adviceDeclaration.sourceStart;
		int end = retrieveIdentifierEndPosition(start, adviceDeclaration.sourceEnd);

		int declarationSourceStart = adviceDeclaration.declarationSourceStart;
		int declarationSourceEnd = adviceDeclaration.bodyEnd;
		adviceDecl.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
		int closingPosition = retrieveRightBraceOrSemiColonPosition(adviceDeclaration.bodyEnd + 1,
				adviceDeclaration.declarationSourceEnd);
		if (closingPosition != -1) {
			int startPosition = adviceDecl.getStartPosition();
			adviceDecl.setSourceRange(startPosition, closingPosition - startPosition + 1);

			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement[] statements = adviceDeclaration.statements;

			start = retrieveStartBlockPosition(adviceDeclaration.sourceStart, declarationSourceEnd);
			end = retrieveEndBlockPosition(adviceDeclaration.sourceStart, adviceDeclaration.declarationSourceEnd);
			Block block = null;
			if (start != -1 && end != -1) {
				/*
				 * start or end can be equal to -1 if we have an interface's method.
				 */
				block = new Block(this.ast);
				block.setSourceRange(start, end - start + 1);
				adviceDecl.setBody(block);
			}
			if (block != null && statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++) {
					if (statements[i] instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
						checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
					} else {
						block.statements().add(convert(statements[i]));
					}
				}
			}
			if (block != null) {
				adviceDecl.setFlags(adviceDecl.getFlags() | ASTNode.MALFORMED);
			}
		} else {
			// syntax error in this advice declaration
			start = retrieveStartBlockPosition(adviceDeclaration.sourceStart, declarationSourceEnd);
			end = adviceDeclaration.bodyEnd;
			// try to get the best end position
			IProblem[] problems = adviceDeclaration.compilationResult().problems;
			if (problems != null) {
				for (int i = 0, max = adviceDeclaration.compilationResult().problemCount; i < max; i++) {
					IProblem currentProblem = problems[i];
					if (currentProblem.getSourceStart() == start && currentProblem.getID() == IProblem.ParsingErrorInsertToComplete) {
						end = currentProblem.getSourceEnd();
						break;
					}
				}
			}
			int startPosition = adviceDecl.getStartPosition();
			adviceDecl.setSourceRange(startPosition, end - startPosition + 1);
			if (start != -1 && end != -1) {
				/*
				 * start or end can be equal to -1 if we have an interface's method.
				 */
				Block block = new Block(this.ast);
				block.setSourceRange(start, end - start + 1);
				adviceDecl.setBody(block);
			}
		}

		// The javadoc comment is now got from list store in compilation unit declaration
		if (this.resolveBindings) {
			recordNodes(adviceDecl, adviceDeclaration);
			// if (adviceDecl.resolveBinding() != null) {
			// // ajh02: what is resolveBinding()?
			// convert(adviceDeclaration.javadoc, adviceDecl);
			// }
		} else {
			convert(adviceDeclaration.javadoc, adviceDecl);
		}
		return adviceDecl;
	}

	//public ASTNode convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration methodDeclaration) {
		public ASTNode convert(boolean isInterface, org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration methodDeclaration) {
			checkCanceled();
			if (methodDeclaration instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration) {
				return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration) methodDeclaration);
			}
			MethodDeclaration methodDecl = new MethodDeclaration(this.ast);
			boolean isConstructor = methodDeclaration.isConstructor();
			methodDecl.setConstructor(isConstructor);

			// //////////////// ajh02: added. ugh, polymorphism! Where are you!
			if (methodDeclaration instanceof DeclareDeclaration) {
				return convert((DeclareDeclaration) methodDeclaration);
			} else if (methodDeclaration instanceof InterTypeFieldDeclaration) {
				return convert((InterTypeFieldDeclaration) methodDeclaration);
			} else if (methodDeclaration instanceof InterTypeMethodDeclaration) {
				methodDecl = new org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration(this.ast);
				((org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration) methodDecl)
						.setOnType(((InterTypeMethodDeclaration) methodDeclaration).getOnType().toString());
			} else if (methodDeclaration instanceof InterTypeConstructorDeclaration) {
				methodDecl = new org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration(this.ast);
				((org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration) methodDecl)
						.setOnType(((InterTypeConstructorDeclaration) methodDeclaration).getOnType().toString());
				methodDecl.setConstructor(true);
			} else if (methodDeclaration instanceof PointcutDeclaration) {
				return convert((PointcutDeclaration) methodDeclaration);
			} else if (methodDeclaration instanceof AdviceDeclaration) {
				return convert((AdviceDeclaration) methodDeclaration);
			}
			// ///////////////////////

			// set modifiers after checking whether we're an itd, otherwise
			// the modifiers are not set on the correct object.
			setModifiers(methodDecl, methodDeclaration);
			final SimpleName methodName = new SimpleName(this.ast);
			// AspectJ Extension - for ITD's use the declaredSelector
			if (methodDeclaration instanceof InterTypeDeclaration) {
				InterTypeDeclaration itd = (InterTypeDeclaration) methodDeclaration;
				methodName.internalSetIdentifier(new String(itd.getDeclaredSelector()));
			} else {
				methodName.internalSetIdentifier(new String(methodDeclaration.selector));
			}
			// AspectJ Extension end
			int start = methodDeclaration.sourceStart;
			int end = retrieveIdentifierEndPosition(start, methodDeclaration.sourceEnd);
			methodName.setSourceRange(start, end - start + 1);
			methodDecl.setName(methodName);
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
			int methodHeaderEnd = methodDeclaration.sourceEnd;
			int thrownExceptionsLength = thrownExceptions == null ? 0 : thrownExceptions.length;
			if (thrownExceptionsLength > 0) {
				Name thrownException;
				int i = 0;
				do {
					thrownException = convert(thrownExceptions[i++]);
					methodDecl.thrownExceptions().add(thrownException);
				} while (i < thrownExceptionsLength);
				methodHeaderEnd = thrownException.getStartPosition() + thrownException.getLength();
			}
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument[] parameters = methodDeclaration.arguments;
			int parametersLength = parameters == null ? 0 : parameters.length;
			if (parametersLength > 0) {
				SingleVariableDeclaration parameter;
				int i = 0;
				do {
					parameter = convert(parameters[i++]);
					methodDecl.parameters().add(parameter);
				} while (i < parametersLength);
				if (thrownExceptionsLength == 0) {
					methodHeaderEnd = parameter.getStartPosition() + parameter.getLength();
				}
			}
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall explicitConstructorCall = null;
			if (isConstructor) {
				if (isInterface) {
					// interface cannot have a constructor
					methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
				}
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration constructorDeclaration = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration) methodDeclaration;
				explicitConstructorCall = constructorDeclaration.constructorCall;
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						// set the return type to VOID
						PrimitiveType returnType = new PrimitiveType(this.ast);
						returnType.setPrimitiveTypeCode(PrimitiveType.VOID);
						returnType.setSourceRange(methodDeclaration.sourceStart, 0);
						methodDecl.internalSetReturnType(returnType);
						break;
					default :
						methodDecl.setReturnType2(null);
				}
			} else if (methodDeclaration instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) {
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration method = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) methodDeclaration;
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = method.returnType;
				if (typeReference != null) {
					Type returnType = convertType(typeReference);
					// get the positions of the right parenthesis
					int rightParenthesisPosition = retrieveEndOfRightParenthesisPosition(end, method.bodyEnd);
					int extraDimensions = retrieveExtraDimension(rightParenthesisPosition, method.bodyEnd);
					methodDecl.setExtraDimensions(extraDimensions);
					setTypeForMethodDeclaration(methodDecl, returnType, extraDimensions);
				} else {
					// no return type for a method that is not a constructor
					methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
					switch(this.ast.apiLevel) {
						case AST.JLS2_INTERNAL :
							break;
						default :
							methodDecl.setReturnType2(null);
					}
				}
			}
			int declarationSourceStart = methodDeclaration.declarationSourceStart;
			int bodyEnd = methodDeclaration.bodyEnd;
			methodDecl.setSourceRange(declarationSourceStart, bodyEnd - declarationSourceStart + 1);
			int declarationSourceEnd = methodDeclaration.declarationSourceEnd;
			int rightBraceOrSemiColonPositionStart = bodyEnd == declarationSourceEnd ? bodyEnd : bodyEnd + 1;
			int closingPosition = retrieveRightBraceOrSemiColonPosition(rightBraceOrSemiColonPositionStart, declarationSourceEnd);
			if (closingPosition != -1) {
				int startPosition = methodDecl.getStartPosition();
				methodDecl.setSourceRange(startPosition, closingPosition - startPosition + 1);

				org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement[] statements = methodDeclaration.statements;

				start = retrieveStartBlockPosition(methodHeaderEnd, methodDeclaration.bodyStart);
				if (start == -1) start = methodDeclaration.bodyStart; // use recovery position for body start
				end = retrieveRightBrace(methodDeclaration.bodyEnd, declarationSourceEnd);
				Block block = null;
				if (start != -1 && end != -1) {
					/*
					 * start or end can be equal to -1 if we have an interface's method.
					 */
					block = new Block(this.ast);
					block.setSourceRange(start, closingPosition - start + 1);
					methodDecl.setBody(block);
				}
				if (block != null && (statements != null || explicitConstructorCall != null)) {
					if (explicitConstructorCall != null && explicitConstructorCall.accessMode != org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall.ImplicitSuper) {
						block.statements().add(convert(explicitConstructorCall));
					}
					int statementsLength = statements == null ? 0 : statements.length;
					for (int i = 0; i < statementsLength; i++) {
						if (statements[i] instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
							checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
						} else {
							final Statement statement = convert(statements[i]);
							if (statement != null) {
								block.statements().add(statement);
							}
						}
					}
				}
				if (block != null
						&& (Modifier.isAbstract(methodDecl.getModifiers())
								|| Modifier.isNative(methodDecl.getModifiers())
								|| isInterface)) {
					methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
				}
			} else {
				// syntax error in this method declaration
				methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
				if (!methodDeclaration.isNative() && !methodDeclaration.isAbstract()) {
					start = retrieveStartBlockPosition(methodHeaderEnd, bodyEnd);
					if (start == -1) start = methodDeclaration.bodyStart; // use recovery position for body start
					end = methodDeclaration.bodyEnd;
					// try to get the best end position
					CategorizedProblem[] problems = methodDeclaration.compilationResult().problems;
					if (problems != null) {
						for (int i = 0, max = methodDeclaration.compilationResult().problemCount; i < max; i++) {
							CategorizedProblem currentProblem = problems[i];
							if (currentProblem.getSourceStart() == start && currentProblem.getID() == IProblem.ParsingErrorInsertToComplete) {
								end = currentProblem.getSourceEnd();
								break;
							}
						}
					}
					int startPosition = methodDecl.getStartPosition();
					methodDecl.setSourceRange(startPosition, end - startPosition + 1);
					if (start != -1 && end != -1) {
						/*
						 * start or end can be equal to -1 if we have an interface's method.
						 */
						Block block = new Block(this.ast);
						block.setSourceRange(start, end - start + 1);
						methodDecl.setBody(block);
					}
				}
			}

			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter[] typeParameters = methodDeclaration.typeParameters();
			if (typeParameters != null) {
				switch(this.ast.apiLevel) {
					case AST.JLS2_INTERNAL :
						methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
						break;
					default :
						for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParameter : typeParameters) {
							methodDecl.typeParameters().add(convert(typeParameter));
						}
				}
			}

			// The javadoc comment is now got from list store in compilation unit declaration
			convert(methodDeclaration.javadoc, methodDecl);
			if (this.resolveBindings) {
				recordNodes(methodDecl, methodDeclaration);
				recordNodes(methodName, methodDeclaration);
				methodDecl.resolveBinding();
			}
			return methodDecl;
		}
	
//		checkCanceled();
//		if (methodDeclaration instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration) {
//			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration) methodDeclaration);
//		}
//		MethodDeclaration methodDecl = new MethodDeclaration(this.ast);
//		boolean isConstructor = methodDeclaration.isConstructor();
//		methodDecl.setConstructor(isConstructor);
//
//		// //////////////// ajh02: added. ugh, polymorphism! Where are you!
//		if (methodDeclaration instanceof DeclareDeclaration) {
//			return convert((DeclareDeclaration) methodDeclaration);
//		} else if (methodDeclaration instanceof InterTypeFieldDeclaration) {
//			return convert((InterTypeFieldDeclaration) methodDeclaration);
//		} else if (methodDeclaration instanceof InterTypeMethodDeclaration) {
//			methodDecl = new org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration(this.ast);
//			((org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration) methodDecl)
//					.setOnType(((InterTypeMethodDeclaration) methodDeclaration).getOnType().toString());
//		} else if (methodDeclaration instanceof InterTypeConstructorDeclaration) {
//			methodDecl = new org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration(this.ast);
//			((org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration) methodDecl)
//					.setOnType(((InterTypeConstructorDeclaration) methodDeclaration).getOnType().toString());
//			methodDecl.setConstructor(true);
//		} else if (methodDeclaration instanceof PointcutDeclaration) {
//			return convert((PointcutDeclaration) methodDeclaration);
//		} else if (methodDeclaration instanceof AdviceDeclaration) {
//			return convert((AdviceDeclaration) methodDeclaration);
//		}
//		// ///////////////////////
//
//		// set modifiers after checking whether we're an itd, otherwise
//		// the modifiers are not set on the correct object.
//		setModifiers(methodDecl, methodDeclaration);
//
//		// for ITD's use the declaredSelector
//		final SimpleName methodName = new SimpleName(this.ast);
//		if (methodDeclaration instanceof InterTypeDeclaration) {
//			InterTypeDeclaration itd = (InterTypeDeclaration) methodDeclaration;
//			methodName.internalSetIdentifier(new String(itd.getDeclaredSelector()));
//		} else {
//			methodName.internalSetIdentifier(new String(methodDeclaration.selector));
//		}
//		int start = methodDeclaration.sourceStart;
//		int end = retrieveIdentifierEndPosition(start, methodDeclaration.sourceEnd);
//		methodName.setSourceRange(start, end - start + 1);
//		methodDecl.setName(methodName);
//
//		org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
//		if (thrownExceptions != null) {
//			int thrownExceptionsLength = thrownExceptions.length;
//			for (int i = 0; i < thrownExceptionsLength; i++) {
//				methodDecl.thrownExceptions().add(convert(thrownExceptions[i]));
//			}
//		}
//		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument[] parameters = methodDeclaration.arguments;
//		if (parameters != null) {
//			int parametersLength = parameters.length;
//			for (int i = 0; i < parametersLength; i++) {
//				methodDecl.parameters().add(convert(parameters[i]));
//			}
//		}
//		org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall explicitConstructorCall = null;
//		if (isConstructor) {
//			org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration constructorDeclaration = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration) methodDeclaration;
//			explicitConstructorCall = constructorDeclaration.constructorCall;
//			switch (this.ast.apiLevel) {
//			case AST.JLS2_INTERNAL:
//				// set the return type to VOID
//				PrimitiveType returnType = new PrimitiveType(this.ast);
//				returnType.setPrimitiveTypeCode(PrimitiveType.VOID);
//				returnType.setSourceRange(methodDeclaration.sourceStart, 0);
//				methodDecl.internalSetReturnType(returnType);
//				break;
//			case AST.JLS3:
//				methodDecl.setReturnType2(null);
//			}
//		} else if (methodDeclaration instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) {
//			org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration method = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) methodDeclaration;
//			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = method.returnType;
//			if (typeReference != null) {
//				Type returnType = convertType(typeReference);
//				// get the positions of the right parenthesis
//				int rightParenthesisPosition = retrieveEndOfRightParenthesisPosition(end, method.bodyEnd);
//				int extraDimensions = retrieveExtraDimension(rightParenthesisPosition, method.bodyEnd);
//				methodDecl.setExtraDimensions(extraDimensions);
//				setTypeForMethodDeclaration(methodDecl, returnType, extraDimensions);
//			}
//		}
//		int declarationSourceStart = methodDeclaration.declarationSourceStart;
//		int declarationSourceEnd = methodDeclaration.bodyEnd;
//		methodDecl.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
//		int closingPosition = retrieveRightBraceOrSemiColonPosition(methodDeclaration.bodyEnd + 1,
//				methodDeclaration.declarationSourceEnd);
//		if (closingPosition != -1) {
//			int startPosition = methodDecl.getStartPosition();
//			methodDecl.setSourceRange(startPosition, closingPosition - startPosition + 1);
//
//			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement[] statements = methodDeclaration.statements;
//
//			start = retrieveStartBlockPosition(methodDeclaration.sourceStart, declarationSourceEnd);
//			end = retrieveEndBlockPosition(methodDeclaration.sourceStart, methodDeclaration.declarationSourceEnd);
//			Block block = null;
//			if (start != -1 && end != -1) {
//				/*
//				 * start or end can be equal to -1 if we have an interface's method.
//				 */
//				block = new Block(this.ast);
//				block.setSourceRange(start, end - start + 1);
//				methodDecl.setBody(block);
//			}
//			if (block != null && (statements != null || explicitConstructorCall != null)) {
//				if (explicitConstructorCall != null
//						&& explicitConstructorCall.accessMode != org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall.ImplicitSuper) {
//					block.statements().add(super.convert(explicitConstructorCall));
//				}
//				int statementsLength = statements == null ? 0 : statements.length;
//				for (int i = 0; i < statementsLength; i++) {
//					if (statements[i] instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
//						checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
//					} else {
//						block.statements().add(convert(statements[i]));
//					}
//				}
//			}
//			if (block != null && (Modifier.isAbstract(methodDecl.getModifiers()) || Modifier.isNative(methodDecl.getModifiers()))) {
//				methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
//			}
//		} else {
//			// syntax error in this method declaration
//			if (!methodDeclaration.isNative() && !methodDeclaration.isAbstract()) {
//				start = retrieveStartBlockPosition(methodDeclaration.sourceStart, declarationSourceEnd);
//				end = methodDeclaration.bodyEnd;
//				// try to get the best end position
//				IProblem[] problems = methodDeclaration.compilationResult().problems;
//				if (problems != null) {
//					for (int i = 0, max = methodDeclaration.compilationResult().problemCount; i < max; i++) {
//						IProblem currentProblem = problems[i];
//						if (currentProblem.getSourceStart() == start
//								&& currentProblem.getID() == IProblem.ParsingErrorInsertToComplete) {
//							end = currentProblem.getSourceEnd();
//							break;
//						}
//					}
//				}
//				int startPosition = methodDecl.getStartPosition();
//				methodDecl.setSourceRange(startPosition, end - startPosition + 1);
//				if (start != -1 && end != -1) {
//					/*
//					 * start or end can be equal to -1 if we have an interface's method.
//					 */
//					Block block = new Block(this.ast);
//					block.setSourceRange(start, end - start + 1);
//					methodDecl.setBody(block);
//				}
//			}
//		}
//
//		org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter[] typeParameters = methodDeclaration.typeParameters();
//		if (typeParameters != null) {
//			switch (this.ast.apiLevel) {
//			case AST.JLS2_INTERNAL:
//				methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
//				break;
//			case AST.JLS3:
//				for (int i = 0, max = typeParameters.length; i < max; i++) {
//					methodDecl.typeParameters().add(convert(typeParameters[i]));
//				}
//			}
//		}
//
//		// The javadoc comment is now got from list store in compilation unit declaration
//		if (this.resolveBindings) {
//			recordNodes(methodDecl, methodDeclaration);
//			recordNodes(methodName, methodDeclaration);
//			if (methodDecl.resolveBinding() != null) {
//				convert(methodDeclaration.javadoc, methodDecl);
//			}
//		} else {
//			convert(methodDeclaration.javadoc, methodDecl);
//		}
//		return methodDecl;
//	}

	public ASTNode convert(DeclareDeclaration declareDecl) {
		checkCanceled(); // is this line needed?
		org.aspectj.org.eclipse.jdt.core.dom.DeclareDeclaration declareDeclaration = null;
		Declare declare = declareDecl.declareDecl;
		if (declare instanceof DeclareAnnotation) {
			DeclareAnnotation da = (DeclareAnnotation) declare;
			if (da.getKind().equals(DeclareAnnotation.AT_TYPE)) {
				declareDeclaration = new DeclareAtTypeDeclaration(this.ast);
				((DeclareAtTypeDeclaration) declareDeclaration).setPatternNode(convert(da.getTypePattern()));
				SimpleName annotationName = new SimpleName(this.ast);
				annotationName.setSourceRange(da.getAnnotationSourceStart(),
						da.getAnnotationSourceEnd() - da.getAnnotationSourceStart());
				annotationName.internalSetIdentifier(da.getAnnotationString());
				((DeclareAtTypeDeclaration) declareDeclaration).setAnnotationName(annotationName);
			} else if (da.getKind().equals(DeclareAnnotation.AT_CONSTRUCTOR)) {
				declareDeclaration = new DeclareAtConstructorDeclaration(this.ast);
				((DeclareAtConstructorDeclaration) declareDeclaration).setPatternNode(convertSignature(da.getSignaturePattern()));
				SimpleName annotationName = new SimpleName(this.ast);
				annotationName.setSourceRange(da.getAnnotationSourceStart(),
						da.getAnnotationSourceEnd() - da.getAnnotationSourceStart());
				annotationName.internalSetIdentifier(da.getAnnotationString());
				((DeclareAtConstructorDeclaration) declareDeclaration).setAnnotationName(annotationName);
			} else if (da.getKind().equals(DeclareAnnotation.AT_FIELD)) {
				declareDeclaration = new DeclareAtFieldDeclaration(this.ast);
				((DeclareAtFieldDeclaration) declareDeclaration).setPatternNode(convertSignature(da.getSignaturePattern()));
				SimpleName annotationName = new SimpleName(this.ast);
				annotationName.setSourceRange(da.getAnnotationSourceStart(),
						da.getAnnotationSourceEnd() - da.getAnnotationSourceStart());
				annotationName.internalSetIdentifier(da.getAnnotationString());
				((DeclareAtFieldDeclaration) declareDeclaration).setAnnotationName(annotationName);
			} else if (da.getKind().equals(DeclareAnnotation.AT_METHOD)) {
				declareDeclaration = new DeclareAtMethodDeclaration(this.ast);
				((DeclareAtMethodDeclaration) declareDeclaration).setPatternNode(convertSignature(da.getSignaturePattern()));
				SimpleName annotationName = new SimpleName(this.ast);
				annotationName.setSourceRange(da.getAnnotationSourceStart(),
						da.getAnnotationSourceEnd() - da.getAnnotationSourceStart());
				annotationName.internalSetIdentifier(da.getAnnotationString());
				((DeclareAtMethodDeclaration) declareDeclaration).setAnnotationName(annotationName);
			}
		} else if (declare instanceof DeclareErrorOrWarning) {
			DeclareErrorOrWarning deow = (DeclareErrorOrWarning) declare;
			if (deow.isError()) {
				declareDeclaration = new DeclareErrorDeclaration(this.ast);
				((DeclareErrorDeclaration) declareDeclaration).setPointcut(convert(deow.getPointcut()));
				StringLiteral message = new StringLiteral(this.ast);
				message.setEscapedValue(updateString(deow.getMessage()));
				((DeclareErrorDeclaration) declareDeclaration).setMessage(message);
			} else {
				declareDeclaration = new DeclareWarningDeclaration(this.ast);
				((DeclareWarningDeclaration) declareDeclaration).setPointcut(convert(deow.getPointcut()));
				StringLiteral message = new StringLiteral(this.ast);
				message.setEscapedValue(updateString(deow.getMessage()));
				((DeclareWarningDeclaration) declareDeclaration).setMessage(message);
			}
		} else if (declare instanceof DeclareParents) {
			DeclareParents dp = (DeclareParents) declare;
			declareDeclaration = new org.aspectj.org.eclipse.jdt.core.dom.DeclareParentsDeclaration(this.ast, dp.isExtends());
			org.aspectj.org.eclipse.jdt.core.dom.PatternNode pNode = convert(dp.getChild());
			if (pNode instanceof org.aspectj.org.eclipse.jdt.core.dom.TypePattern) {
				((DeclareParentsDeclaration) declareDeclaration)
						.setChildTypePattern((org.aspectj.org.eclipse.jdt.core.dom.TypePattern) pNode);
			}
			TypePattern[] weaverTypePatterns = dp.getParents().getTypePatterns();
			List typePatterns = ((DeclareParentsDeclaration) declareDeclaration).parentTypePatterns();
			for (TypePattern weaverTypePattern : weaverTypePatterns) {
				typePatterns.add(convert(weaverTypePattern));
			}
		} else if (declare instanceof DeclarePrecedence) {
			declareDeclaration = new org.aspectj.org.eclipse.jdt.core.dom.DeclarePrecedenceDeclaration(this.ast);
			DeclarePrecedence dp = (DeclarePrecedence) declare;
			TypePattern[] weaverTypePatterns = dp.getPatterns().getTypePatterns();
			List typePatterns = ((DeclarePrecedenceDeclaration) declareDeclaration).typePatterns();
			for (TypePattern weaverTypePattern : weaverTypePatterns) {
				typePatterns.add(convert(weaverTypePattern));
			}
		} else if (declare instanceof DeclareSoft) {
			declareDeclaration = new DeclareSoftDeclaration(this.ast);
			DeclareSoft ds = (DeclareSoft) declare;
			((DeclareSoftDeclaration) declareDeclaration).setPointcut(convert(ds.getPointcut()));
			org.aspectj.org.eclipse.jdt.core.dom.PatternNode pNode = convert(ds.getException());
			if (pNode instanceof org.aspectj.org.eclipse.jdt.core.dom.TypePattern) {
				((DeclareSoftDeclaration) declareDeclaration)
						.setTypePattern((org.aspectj.org.eclipse.jdt.core.dom.TypePattern) pNode);
			}
		}
		
		if (declareDeclaration != null) {
			declareDeclaration.setSourceRange(declareDecl.declarationSourceStart, declareDecl.declarationSourceEnd
					- declareDecl.declarationSourceStart + 1);
		}
		return declareDeclaration;
	}

	private String updateString(String message) {
		StringBuffer sb = new StringBuffer(message);
		int nextQuote = sb.toString().indexOf("\"");
		while (nextQuote != -1) {
			sb.insert(nextQuote, "\\");
			nextQuote = sb.toString().indexOf("\"");
		}
		int nextNewLine = sb.toString().indexOf("\n");
		while (nextNewLine != -1) {
			sb.insert(nextNewLine, "\\");
			nextNewLine = sb.toString().indexOf("\n");
		}
		if (!sb.toString().startsWith("\"")) {
			sb.insert(0, "\"");
		}
		if (!sb.toString().endsWith("\"")) {
			sb.insert(sb.toString().length(), "\"");
		}
		return sb.toString();
	}

	public ASTNode convert(InterTypeFieldDeclaration fieldDecl) {
		// ajh02: method added
		checkCanceled(); // ajh02: is this line needed?
		VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(fieldDecl);
		final org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration fieldDeclaration = new org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration(
				this.ast);
		fieldDeclaration.fragments().add(variableDeclarationFragment);
		IVariableBinding binding = null;
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, fieldDecl);
			binding = variableDeclarationFragment.resolveBinding();
		}
		fieldDeclaration.setSourceRange(fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd
				- fieldDecl.declarationSourceStart + 1);
		Type type = convertType(fieldDecl.returnType);
		setTypeForField(fieldDeclaration, type, variableDeclarationFragment.getExtraDimensions());
		setModifiers(fieldDeclaration, fieldDecl);
		if (!(this.resolveBindings && binding == null)) {
			convert(fieldDecl.javadoc, fieldDeclaration);
		}
		fieldDeclaration.setOnType(fieldDecl.getOnType().toString());
		return fieldDeclaration;
	}

	public ASTNode convert(PointcutDeclaration pointcutDeclaration) {
		// ajh02: method added
		checkCanceled();
		org.aspectj.org.eclipse.jdt.core.dom.PointcutDeclaration pointcutDecl = new org.aspectj.org.eclipse.jdt.core.dom.PointcutDeclaration(
				this.ast);
		setModifiers(pointcutDecl, pointcutDeclaration);
		final SimpleName pointcutName = new SimpleName(this.ast);
		pointcutName.internalSetIdentifier(new String(pointcutDeclaration.selector));
		int start = pointcutDeclaration.sourceStart;
		int end = retrieveIdentifierEndPosition(start, pointcutDeclaration.sourceEnd);
		pointcutName.setSourceRange(start, end - start + 1);
		pointcutDecl.setSourceRange(pointcutDeclaration.declarationSourceStart, (pointcutDeclaration.bodyEnd
				- pointcutDeclaration.declarationSourceStart + 1));
		pointcutDecl.setName(pointcutName);
		if (pointcutDeclaration.pointcutDesignator != null) {
			pointcutDecl.setDesignator(convert(pointcutDeclaration.pointcutDesignator.getPointcut()));
		} else {
			pointcutDecl.setDesignator(new org.aspectj.org.eclipse.jdt.core.dom.DefaultPointcut(this.ast, pointcutDeclaration
					.toString()));
		}
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument[] parameters = pointcutDeclaration.arguments;
		if (parameters != null) {
			int parametersLength = parameters.length;
			for (Argument parameter : parameters) {
				pointcutDecl.parameters().add(convert(parameter));
			}
		}

		// The javadoc comment is now got from list store in compilation unit declaration
		if (this.resolveBindings) {
			recordNodes(pointcutDecl, pointcutDeclaration);
			recordNodes(pointcutName, pointcutDeclaration);
		} else {
			convert(pointcutDeclaration.javadoc, pointcutDecl);
		}
		return pointcutDecl;
	}

	public org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator convert(org.aspectj.weaver.patterns.Pointcut pointcut) {
		// ajh02: this could do with being seperate methods
		// rather than a huge if.elseif..elseif.. thing
		org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator pointcutDesi = null;
		if (pointcut instanceof org.aspectj.weaver.patterns.ReferencePointcut) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.ReferencePointcut(this.ast);
			final SimpleName pointcutName = new SimpleName(this.ast);
			int start = pointcut.getStart();
			int end = retrieveIdentifierEndPosition(start, pointcut.getEnd());
			pointcutName.setSourceRange(start, end - start + 1);
			pointcutName.internalSetIdentifier(((org.aspectj.weaver.patterns.ReferencePointcut) pointcut).name);
			((org.aspectj.org.eclipse.jdt.core.dom.ReferencePointcut) pointcutDesi).setName(pointcutName);
		} else if (pointcut instanceof org.aspectj.weaver.patterns.NotPointcut) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.NotPointcut(this.ast);
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator body = convert(((org.aspectj.weaver.patterns.NotPointcut) pointcut)
					.getNegatedPointcut());
			((org.aspectj.org.eclipse.jdt.core.dom.NotPointcut) pointcutDesi).setBody(body);
		} else if (pointcut instanceof org.aspectj.weaver.patterns.PerObject) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.PerObject(this.ast);
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator body = convert(((org.aspectj.weaver.patterns.PerObject) pointcut)
					.getEntry());
			((org.aspectj.org.eclipse.jdt.core.dom.PerObject) pointcutDesi).setBody(body);
		} else if (pointcut instanceof org.aspectj.weaver.patterns.PerCflow) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.PerCflow(this.ast);
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator body = convert(((org.aspectj.weaver.patterns.PerCflow) pointcut)
					.getEntry());
			((org.aspectj.org.eclipse.jdt.core.dom.PerCflow) pointcutDesi).setBody(body);
		} else if (pointcut instanceof org.aspectj.weaver.patterns.PerTypeWithin) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.PerTypeWithin(this.ast);
			// should set its type pattern here
		} else if (pointcut instanceof org.aspectj.weaver.patterns.CflowPointcut) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.CflowPointcut(this.ast);
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator body = convert(((org.aspectj.weaver.patterns.CflowPointcut) pointcut)
					.getEntry());
			((org.aspectj.org.eclipse.jdt.core.dom.CflowPointcut) pointcutDesi).setBody(body);
			((org.aspectj.org.eclipse.jdt.core.dom.CflowPointcut) pointcutDesi)
					.setIsCflowBelow(((org.aspectj.weaver.patterns.CflowPointcut) pointcut).isCflowBelow());
		} else if (pointcut instanceof org.aspectj.weaver.patterns.AndPointcut) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.AndPointcut(this.ast);
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator left = convert(((org.aspectj.weaver.patterns.AndPointcut) pointcut)
					.getLeft());
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator right = convert(((org.aspectj.weaver.patterns.AndPointcut) pointcut)
					.getRight());
			((org.aspectj.org.eclipse.jdt.core.dom.AndPointcut) pointcutDesi).setLeft(left);
			((org.aspectj.org.eclipse.jdt.core.dom.AndPointcut) pointcutDesi).setRight(right);
		} else if (pointcut instanceof org.aspectj.weaver.patterns.OrPointcut) {
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.OrPointcut(this.ast);
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator left = convert(((org.aspectj.weaver.patterns.OrPointcut) pointcut)
					.getLeft());
			final org.aspectj.org.eclipse.jdt.core.dom.PointcutDesignator right = convert(((org.aspectj.weaver.patterns.OrPointcut) pointcut)
					.getRight());
			((org.aspectj.org.eclipse.jdt.core.dom.OrPointcut) pointcutDesi).setLeft(left);
			((org.aspectj.org.eclipse.jdt.core.dom.OrPointcut) pointcutDesi).setRight(right);
		} else {
			// ajh02: default stub until I make all the concrete PointcutDesignator types
			pointcutDesi = new org.aspectj.org.eclipse.jdt.core.dom.DefaultPointcut(this.ast, pointcut.toString());
		}
		pointcutDesi.setSourceRange(pointcut.getStart(), (pointcut.getEnd() - pointcut.getStart() + 1));
		return pointcutDesi;
	}

	public org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern convertSignature(ISignaturePattern patternNode) {
		org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern pNode = null;
		if (patternNode instanceof SignaturePattern) {
			SignaturePattern sigPat = (SignaturePattern) patternNode;
			pNode = new org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern(this.ast, sigPat.toString());
			pNode.setSourceRange(sigPat.getStart(), (sigPat.getEnd() - sigPat.getStart() + 1));
		} else {
			throw new IllegalStateException("Not yet implemented for " + patternNode.getClass());
		}
		return pNode;
	}

	public org.aspectj.org.eclipse.jdt.core.dom.PatternNode convert(
			PatternNode patternNode) {
		org.aspectj.org.eclipse.jdt.core.dom.PatternNode pNode = null;
		if (patternNode instanceof TypePattern) {
			TypePattern weaverTypePattern = (TypePattern) patternNode;
			return convert(weaverTypePattern);

		} else if (patternNode instanceof SignaturePattern) {
			SignaturePattern sigPat = (SignaturePattern) patternNode;
			pNode = new org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern(this.ast, sigPat.toString());
			pNode.setSourceRange(sigPat.getStart(), (sigPat.getEnd() - sigPat.getStart() + 1));
		}
		return pNode;

	}

	public org.aspectj.org.eclipse.jdt.core.dom.TypePattern convert(
			TypePattern weaverNode) {

		// First check if the node is a Java type (WildType, ExactType,
		// BindingType)
		org.aspectj.org.eclipse.jdt.core.dom.TypePattern domNode = createIdentifierTypePattern(weaverNode);

		if (domNode == null) {
			if (weaverNode instanceof org.aspectj.weaver.patterns.EllipsisTypePattern) {
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.EllipsisTypePattern(
						ast);
			} else if (weaverNode instanceof org.aspectj.weaver.patterns.NoTypePattern) {
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.NoTypePattern(
						ast);
			} else if (weaverNode instanceof org.aspectj.weaver.patterns.AnyTypePattern) {
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.AnyTypePattern(
						ast);
			} else if (weaverNode instanceof org.aspectj.weaver.patterns.AnyWithAnnotationTypePattern) {
				// For now construct the node with just the annotation
				// expression
				String annotationExpression = ((org.aspectj.weaver.patterns.AnyWithAnnotationTypePattern) weaverNode)
						.toString();
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.AnyWithAnnotationTypePattern(
						ast, annotationExpression);

			} else if (weaverNode instanceof org.aspectj.weaver.patterns.OrTypePattern) {
				org.aspectj.weaver.patterns.OrTypePattern compilerOrNode = (org.aspectj.weaver.patterns.OrTypePattern) weaverNode;
				domNode = new OrTypePattern(this.ast,
						convert(compilerOrNode.getLeft()),
						convert(compilerOrNode.getRight()));
			} else if (weaverNode instanceof org.aspectj.weaver.patterns.AndTypePattern) {
				org.aspectj.weaver.patterns.AndTypePattern compilerAndType = (org.aspectj.weaver.patterns.AndTypePattern) weaverNode;
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.AndTypePattern(
						this.ast, convert(compilerAndType.getLeft()),
						convert(compilerAndType.getRight()));
			} else if (weaverNode instanceof org.aspectj.weaver.patterns.NotTypePattern) {
				//NOTE: the source range for not type patterns is the source range of the negated type pattern
				// EXCLUDING the "!" character. Example: !A. If A starts at 1, the source starting point for the
				// nottypepattern is 1, NOT 0.
				TypePattern negatedTypePattern = ((org.aspectj.weaver.patterns.NotTypePattern) weaverNode)
						.getNegatedPattern();
				org.aspectj.org.eclipse.jdt.core.dom.TypePattern negatedDomTypePattern = convert(negatedTypePattern);
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.NotTypePattern(
						ast, negatedDomTypePattern);
			} else if (weaverNode instanceof org.aspectj.weaver.patterns.TypeCategoryTypePattern) {
				org.aspectj.weaver.patterns.TypeCategoryTypePattern typeCategoryWeaverNode = (org.aspectj.weaver.patterns.TypeCategoryTypePattern) weaverNode;
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.TypeCategoryTypePattern(
						ast, typeCategoryWeaverNode.getTypeCategory());

			} else if (weaverNode instanceof org.aspectj.weaver.patterns.HasMemberTypePattern) {
				ISignaturePattern weaverSignature = ((org.aspectj.weaver.patterns.HasMemberTypePattern) weaverNode)
						.getSignaturePattern();
				org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern signature = convertSignature(weaverSignature);
				domNode = new org.aspectj.org.eclipse.jdt.core.dom.HasMemberTypePattern(
						ast, signature);
			} else {
				// Handle any cases that are not yet implemented. Create a
				// default node for
				// them.
				domNode = new DefaultTypePattern(this.ast,
						weaverNode.toString());
			}
		}

		if (domNode != null) {
			domNode.setSourceRange(weaverNode.getStart(), (weaverNode.getEnd()
					- weaverNode.getStart() + 1));
		}
		return domNode;
	}

	/**
	 * Creates an ExactType, WildType, or BindingType, or null if none of the
	 * three can be created
	 * 
	 * @param weaverTypePattern
	 *            to convert to a DOM equivalent
	 * @return DOM node or null if it was not created
	 */
	protected org.aspectj.org.eclipse.jdt.core.dom.TypePattern createIdentifierTypePattern(
			TypePattern weaverTypePattern) {
		String typeExpression = weaverTypePattern.toString();

		org.aspectj.org.eclipse.jdt.core.dom.TypePattern domTypePattern = null;
		if (weaverTypePattern instanceof org.aspectj.weaver.patterns.WildTypePattern) {
			// Use the expression for wild type patterns as a Name may not be
			// constructed
			// for a Type with a unresolved typeExpression
			domTypePattern = new org.aspectj.org.eclipse.jdt.core.dom.WildTypePattern(
					ast, typeExpression);
		} else {
			// TODO: At this point, the type pattern should be resolved. Type
			// information
			// may be able to be obtained from the exact type in the weaver
			// pattern, therefore
			// replace using the expression to construct the Type and use more
			// appropriate
			// information obtained from the exact type

			if (weaverTypePattern instanceof org.aspectj.weaver.patterns.ExactTypePattern) {
				Type type = this.ast.newSimpleType(this.ast
						.newSimpleName(typeExpression));
				domTypePattern = new ExactTypePattern(ast, type);
			} else if (weaverTypePattern instanceof org.aspectj.weaver.patterns.BindingTypePattern) {
				Type type = this.ast.newSimpleType(this.ast
						.newSimpleName(typeExpression));
				String binding = ((org.aspectj.weaver.patterns.BindingTypePattern) weaverTypePattern)
						.getBindingName();
				FormalBinding formalBinding = new FormalBinding(type, binding,
						ast);
				domTypePattern = new org.aspectj.org.eclipse.jdt.core.dom.BindingTypePattern(
						ast, formalBinding);
			}
		}
		return domTypePattern;
	}

	public ASTNode convert(
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration annotationTypeMemberDeclaration) {
		checkCanceled();
		if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
			return null;
		}
		AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration2 = new AnnotationTypeMemberDeclaration(this.ast);
		setModifiers(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
		final SimpleName methodName = new SimpleName(this.ast);
		methodName.internalSetIdentifier(new String(annotationTypeMemberDeclaration.selector));
		int start = annotationTypeMemberDeclaration.sourceStart;
		int end = retrieveIdentifierEndPosition(start, annotationTypeMemberDeclaration.sourceEnd);
		methodName.setSourceRange(start, end - start + 1);
		annotationTypeMemberDeclaration2.setName(methodName);
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = annotationTypeMemberDeclaration.returnType;
		if (typeReference != null) {
			Type returnType = convertType(typeReference);
			setTypeForMethodDeclaration(annotationTypeMemberDeclaration2, returnType, 0);
		}
		int declarationSourceStart = annotationTypeMemberDeclaration.declarationSourceStart;
		int declarationSourceEnd = annotationTypeMemberDeclaration.bodyEnd;
		annotationTypeMemberDeclaration2.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
		// The javadoc comment is now got from list store in compilation unit declaration
		convert(annotationTypeMemberDeclaration.javadoc, annotationTypeMemberDeclaration2);
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression memberValue = annotationTypeMemberDeclaration.defaultValue;
		if (memberValue != null) {
			annotationTypeMemberDeclaration2.setDefault(super.convert(memberValue));
		}
		if (this.resolveBindings) {
			recordNodes(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
			recordNodes(methodName, annotationTypeMemberDeclaration);
			annotationTypeMemberDeclaration2.resolveBinding();
		}
		return annotationTypeMemberDeclaration2;
	}

	public SingleVariableDeclaration convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument argument) {
		SingleVariableDeclaration variableDecl = new SingleVariableDeclaration(this.ast);
		setModifiers(variableDecl, argument);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(argument.name));
		int start = argument.sourceStart;
		int nameEnd = argument.sourceEnd;
		name.setSourceRange(start, nameEnd - start + 1);
		variableDecl.setName(name);
		final int typeSourceEnd = argument.type.sourceEnd;
		final int extraDimensions = retrieveExtraDimension(nameEnd + 1, typeSourceEnd);
		variableDecl.setExtraDimensions(extraDimensions);
		final boolean isVarArgs = argument.isVarArgs();
		if (isVarArgs && extraDimensions == 0) {
			// remove the ellipsis from the type source end
			argument.type.sourceEnd = retrieveEllipsisStartPosition(argument.type.sourceStart, typeSourceEnd);
		}
		Type type = convertType(argument.type);
		int typeEnd = type.getStartPosition() + type.getLength() - 1;
		int rightEnd = Math.max(typeEnd, argument.declarationSourceEnd);
		/*
		 * There is extra work to do to set the proper type positions See PR http://bugs.eclipse.org/bugs/show_bug.cgi?id=23284
		 */
		if (isVarArgs) {
			setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions + 1);
			if (extraDimensions != 0) {
				variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
			}
		} else {
			setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions);
		}
		variableDecl.setSourceRange(argument.declarationSourceStart, rightEnd - argument.declarationSourceStart + 1);

		if (isVarArgs) {
			switch (this.ast.apiLevel) {
			case AST.JLS2_INTERNAL:
				variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
				break;
			case AST.JLS3:
				variableDecl.setVarargs(true);
			}
		}
		if (this.resolveBindings) {
			recordNodes(name, argument);
			recordNodes(variableDecl, argument);
			variableDecl.resolveBinding();
		}
		return variableDecl;
	}

	// public Annotation convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
	// if (annotation instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) annotation);
	// } else if (annotation instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) {
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation ma =
	// (org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) annotation;
	// return convert( ma);//(org.aspectj.org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) annotation);
	// } else {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation) annotation);
	// }
	// }

	// public ArrayCreation convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression expression) {
	// ArrayCreation arrayCreation = new ArrayCreation(this.ast);
	// if (this.resolveBindings) {
	// recordNodes(arrayCreation, expression);
	// }
	// arrayCreation.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression[] dimensions = expression.dimensions;
	//
	// int dimensionsLength = dimensions.length;
	// for (int i = 0; i < dimensionsLength; i++) {
	// if (dimensions[i] != null) {
	// Expression dimension = convert(dimensions[i]);
	// if (this.resolveBindings) {
	// recordNodes(dimension, dimensions[i]);
	// }
	// arrayCreation.dimensions().add(dimension);
	// }
	// }
	// Type type = convertType(expression.type);
	// if (this.resolveBindings) {
	// recordNodes(type, expression.type);
	// }
	// ArrayType arrayType = null;
	// if (type.isArrayType()) {
	// arrayType = (ArrayType) type;
	// } else {
	// arrayType = this.ast.newArrayType(type, dimensionsLength);
	// if (this.resolveBindings) {
	// completeRecord(arrayType, expression);
	// }
	// int start = type.getStartPosition();
	// int end = type.getStartPosition() + type.getLength();
	// int previousSearchStart = end;
	// ArrayType componentType = (ArrayType) type.getParent();
	// for (int i = 0; i < dimensionsLength; i++) {
	// previousSearchStart = retrieveRightBracketPosition(previousSearchStart + 1, this.compilationUnitSourceLength);
	// componentType.setSourceRange(start, previousSearchStart - start + 1);
	// componentType = (ArrayType) componentType.getParent();
	// }
	// }
	// arrayCreation.setType(arrayType);
	// if (this.resolveBindings) {
	// recordNodes(arrayType, expression);
	// }
	// if (expression.initializer != null) {
	// arrayCreation.setInitializer(convert(expression.initializer));
	// }
	// return arrayCreation;
	// }

	public ArrayInitializer convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer expression) {
		ArrayInitializer arrayInitializer = new ArrayInitializer(this.ast);
		if (this.resolveBindings) {
			recordNodes(arrayInitializer, expression);
		}
		arrayInitializer.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression[] expressions = expression.expressions;
		if (expressions != null) {
			int length = expressions.length;
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression value : expressions) {
				Expression expr = super.convert(value);
				if (this.resolveBindings) {
					recordNodes(expr, value);
				}
				arrayInitializer.expressions().add(expr);
			}
		}
		return arrayInitializer;
	}

	// public ArrayAccess convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayReference reference) {
	// ArrayAccess arrayAccess = new ArrayAccess(this.ast);
	// if (this.resolveBindings) {
	// recordNodes(arrayAccess, reference);
	// }
	// arrayAccess.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
	// arrayAccess.setArray(convert(reference.receiver));
	// arrayAccess.setIndex(convert(reference.position));
	// return arrayAccess;
	// }

	// public AssertStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.AssertStatement statement) {
	// AssertStatement assertStatement = new AssertStatement(this.ast);
	// int end = statement.assertExpression.sourceEnd + 1;
	// assertStatement.setExpression(convert(statement.assertExpression));
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression exceptionArgument = statement.exceptionArgument;
	// if (exceptionArgument != null) {
	// end = exceptionArgument.sourceEnd + 1;
	// assertStatement.setMessage(convert(exceptionArgument));
	// }
	// int start = statement.sourceStart;
	// int sourceEnd = retrieveEndingSemiColonPosition(end, this.compilationUnitSourceLength);
	// assertStatement.setSourceRange(start, sourceEnd - start + 1);
	// return assertStatement;
	// }

	// public Assignment convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Assignment expression) {
	// Assignment assignment = new Assignment(this.ast);
	// if (this.resolveBindings) {
	// recordNodes(assignment, expression);
	// }
	// Expression lhs = convert(expression.lhs);
	// assignment.setLeftHandSide(lhs);
	// assignment.setOperator(Assignment.Operator.ASSIGN);
	// assignment.setRightHandSide(convert(expression.expression));
	// int start = lhs.getStartPosition();
	// assignment.setSourceRange(start, expression.sourceEnd - start + 1);
	// return assignment;
	// }

	/*
	 * Internal use only Used to convert class body declarations
	 */
//	public TypeDeclaration convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode[] nodes) {
//		final TypeDeclaration typeDecl = TypeDeclaration.getTypeDeclaration(this.ast);
//		typeDecl.setInterface(false);
//		int nodesLength = nodes.length;
//		for (int i = 0; i < nodesLength; i++) {
//			org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode node = nodes[i];
//			if (node instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.Initializer) {
//				org.aspectj.org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Initializer) node;
//				Initializer initializer = new Initializer(this.ast);
//				initializer.setBody(convert(oldInitializer.block));
//				setModifiers(initializer, oldInitializer);
//				initializer.setSourceRange(oldInitializer.declarationSourceStart, oldInitializer.sourceEnd
//						- oldInitializer.declarationSourceStart + 1);
//				// setJavaDocComment(initializer);
//				// initializer.setJavadoc(convert(oldInitializer.javadoc));
//				convert(oldInitializer.javadoc, initializer);
//				typeDecl.bodyDeclarations().add(initializer);
//			} else if (node instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) {
//				org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) node;
//				if (i > 0
//						&& (nodes[i - 1] instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration)
//						&& ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration) nodes[i - 1]).declarationSourceStart == fieldDeclaration.declarationSourceStart) {
//					// we have a multiple field declaration
//					// We retrieve the existing fieldDeclaration to add the new VariableDeclarationFragment
//					FieldDeclaration currentFieldDeclaration = (FieldDeclaration) typeDecl.bodyDeclarations().get(
//							typeDecl.bodyDeclarations().size() - 1);
//					currentFieldDeclaration.fragments().add(convertToVariableDeclarationFragment(fieldDeclaration));
//				} else {
//					// we can create a new FieldDeclaration
//					typeDecl.bodyDeclarations().add(convertToFieldDeclaration(fieldDeclaration));
//				}
//			} else if (node instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration) {
//				AbstractMethodDeclaration nextMethodDeclaration = (AbstractMethodDeclaration) node;
//				if (!nextMethodDeclaration.isDefaultConstructor() && !nextMethodDeclaration.isClinit()) {
//					typeDecl.bodyDeclarations().add(convert(nextMethodDeclaration));
//				}
//			} else if (node instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
//				org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration nextMemberDeclaration = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) node;
//				ASTNode nextMemberDeclarationNode = convert(nextMemberDeclaration);
//				if (nextMemberDeclarationNode == null) {
//					typeDecl.setFlags(typeDecl.getFlags() | ASTNode.MALFORMED);
//				} else {
//					typeDecl.bodyDeclarations().add(nextMemberDeclarationNode);
//				}
//			}
//		}
//		return typeDecl;
//	}

	// public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression expression) {
	// InfixExpression infixExpression = new InfixExpression(this.ast);
	// if (this.resolveBindings) {
	// this.recordNodes(infixExpression, expression);
	// }
	//
	// int expressionOperatorID = (expression.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >>
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT;
	// switch (expressionOperatorID) {
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.EQUAL_EQUAL :
	// infixExpression.setOperator(InfixExpression.Operator.EQUALS);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.LESS_EQUAL :
	// infixExpression.setOperator(InfixExpression.Operator.LESS_EQUALS);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.GREATER_EQUAL :
	// infixExpression.setOperator(InfixExpression.Operator.GREATER_EQUALS);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.NOT_EQUAL :
	// infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.LEFT_SHIFT :
	// infixExpression.setOperator(InfixExpression.Operator.LEFT_SHIFT);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.RIGHT_SHIFT :
	// infixExpression.setOperator(InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.UNSIGNED_RIGHT_SHIFT :
	// infixExpression.setOperator(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR_OR :
	// infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND_AND :
	// infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS :
	// infixExpression.setOperator(InfixExpression.Operator.PLUS);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS :
	// infixExpression.setOperator(InfixExpression.Operator.MINUS);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.REMAINDER :
	// infixExpression.setOperator(InfixExpression.Operator.REMAINDER);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.XOR :
	// infixExpression.setOperator(InfixExpression.Operator.XOR);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND :
	// infixExpression.setOperator(InfixExpression.Operator.AND);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MULTIPLY :
	// infixExpression.setOperator(InfixExpression.Operator.TIMES);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR :
	// infixExpression.setOperator(InfixExpression.Operator.OR);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.DIVIDE :
	// infixExpression.setOperator(InfixExpression.Operator.DIVIDE);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.GREATER :
	// infixExpression.setOperator(InfixExpression.Operator.GREATER);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.LESS :
	// infixExpression.setOperator(InfixExpression.Operator.LESS);
	// }
	//
	// if (expression.left instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression
	// && ((expression.left.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0)) {
	// // create an extended string literal equivalent => use the extended operands list
	// infixExpression.extendedOperands().add(convert(expression.right));
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression leftOperand = expression.left;
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression rightOperand = null;
	// do {
	// rightOperand = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).right;
	// if ((((leftOperand.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >>
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID
	// && ((leftOperand.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))
	// || ((rightOperand instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression
	// && ((rightOperand.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorMASK) >>
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.OperatorSHIFT) != expressionOperatorID)
	// && ((rightOperand.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0))) {
	// List extendedOperands = infixExpression.extendedOperands();
	// InfixExpression temp = new InfixExpression(this.ast);
	// if (this.resolveBindings) {
	// this.recordNodes(temp, expression);
	// }
	// temp.setOperator(getOperatorFor(expressionOperatorID));
	// Expression leftSide = convert(leftOperand);
	// temp.setLeftOperand(leftSide);
	// temp.setSourceRange(leftSide.getStartPosition(), leftSide.getLength());
	// int size = extendedOperands.size();
	// for (int i = 0; i < size - 1; i++) {
	// Expression expr = temp;
	// temp = new InfixExpression(this.ast);
	//
	// if (this.resolveBindings) {
	// this.recordNodes(temp, expression);
	// }
	// temp.setLeftOperand(expr);
	// temp.setOperator(getOperatorFor(expressionOperatorID));
	// temp.setSourceRange(expr.getStartPosition(), expr.getLength());
	// }
	// infixExpression = temp;
	// for (int i = 0; i < size; i++) {
	// Expression extendedOperand = (Expression) extendedOperands.remove(size - 1 - i);
	// temp.setRightOperand(extendedOperand);
	// int startPosition = temp.getLeftOperand().getStartPosition();
	// temp.setSourceRange(startPosition, extendedOperand.getStartPosition() + extendedOperand.getLength() - startPosition);
	// if (temp.getLeftOperand().getNodeType() == ASTNode.INFIX_EXPRESSION) {
	// temp = (InfixExpression) temp.getLeftOperand();
	// }
	// }
	// int startPosition = infixExpression.getLeftOperand().getStartPosition();
	// infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
	// if (this.resolveBindings) {
	// this.recordNodes(infixExpression, expression);
	// }
	// return infixExpression;
	// }
	// infixExpression.extendedOperands().add(0, convert(rightOperand));
	// leftOperand = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression) leftOperand).left;
	// } while (leftOperand instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression && ((leftOperand.bits &
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0));
	// Expression leftExpression = convert(leftOperand);
	// infixExpression.setLeftOperand(leftExpression);
	// infixExpression.setRightOperand((Expression)infixExpression.extendedOperands().remove(0));
	// int startPosition = leftExpression.getStartPosition();
	// infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
	// return infixExpression;
	// } else if (expression.left instanceof StringLiteralConcatenation
	// && ((expression.left.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) == 0)) {
	// StringLiteralConcatenation literal = (StringLiteralConcatenation) expression.left;
	// final org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression[] stringLiterals = literal.literals;
	// infixExpression.setLeftOperand(convert(stringLiterals[0]));
	// infixExpression.setRightOperand(convert(stringLiterals[1]));
	// for (int i = 2; i < literal.counter; i++) {
	// infixExpression.extendedOperands().add(convert(stringLiterals[i]));
	// }
	// infixExpression.extendedOperands().add(convert(expression.right));
	// int startPosition = literal.sourceStart;
	// infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
	// return infixExpression;
	// }
	// Expression leftExpression = convert(expression.left);
	// infixExpression.setLeftOperand(leftExpression);
	// infixExpression.setRightOperand(convert(expression.right));
	// int startPosition = leftExpression.getStartPosition();
	// infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
	// return infixExpression;
	// }

	public Block convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Block statement) {
		Block block = new Block(this.ast);
		if (statement.sourceEnd > 0) {
			block.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		}
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement[] statements = statement.statements;
		if (statements != null) {
			int statementsLength = statements.length;
			for (int i = 0; i < statementsLength; i++) {
				if (statements[i] instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
					checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
				} else {
					block.statements().add(convert(statements[i]));
				}
			}
		}
		return block;
	}

	public BreakStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.BreakStatement statement) {
		BreakStatement breakStatement = new BreakStatement(this.ast);
		breakStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.label != null) {
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(statement.label));
			retrieveIdentifierAndSetPositions(statement.sourceStart, statement.sourceEnd, name);
			breakStatement.setLabel(name);
		}
		retrieveSemiColonPosition(breakStatement);
		return breakStatement;
	}

	// public SwitchCase convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.CaseStatement statement) {
	// SwitchCase switchCase = new SwitchCase(this.ast);
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression constantExpression = statement.constantExpression;
	// if (constantExpression == null) {
	// switchCase.setExpression(null);
	// } else {
	// switchCase.setExpression(convert(constantExpression));
	// }
	// switchCase.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
	// retrieveColonPosition(switchCase);
	// return switchCase;
	// }

	// public CastExpression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.CastExpression expression) {
	// CastExpression castExpression = new CastExpression(this.ast);
	// castExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression type = expression.type;
	// trimWhiteSpacesAndComments(type);
	// if (type instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference ) {
	// castExpression.setType(convertType((org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference)type));
	// } else if (type instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.NameReference) {
	// castExpression.setType(convertToType((org.aspectj.org.eclipse.jdt.internal.compiler.ast.NameReference)type));
	// }
	// castExpression.setExpression(convert(expression.expression));
	// if (this.resolveBindings) {
	// recordNodes(castExpression, expression);
	// }
	// return castExpression;
	// }

	public CharacterLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.CharLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		CharacterLiteral literal = new CharacterLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.internalSetEscapedValue(new String(this.compilationUnitSource, sourceStart, length));
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess expression) {
		TypeLiteral typeLiteral = new TypeLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(typeLiteral, expression);
		}
		typeLiteral.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		typeLiteral.setType(convertType(expression.type));
		return typeLiteral;
	}

	public CompilationUnit convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration unit, char[] source) {
		this.compilationUnitSource = source;
		this.compilationUnitSourceLength = source.length;
		this.scanner.setSource(source, unit.compilationResult);
		CompilationUnit compilationUnit = new CompilationUnit(this.ast);

		// Parse comments
		int[][] comments = unit.comments;
		if (comments != null) {
			buildCommentsTable(compilationUnit, comments);
		}

		// handle the package declaration immediately
		// There is no node corresponding to the package declaration
		if (this.resolveBindings) {
			recordNodes(compilationUnit, unit);
		}
		if (unit.currentPackage != null) {
			PackageDeclaration packageDeclaration = convertPackage(unit);
			compilationUnit.setPackage(packageDeclaration);
		}
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.ImportReference[] imports = unit.imports;
		if (imports != null) {
			int importLength = imports.length;
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.ImportReference anImport : imports) {
				compilationUnit.imports().add(convertImport(anImport));
			}
		}

		org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = unit.types;
		if (types != null) {
			int typesLength = types.length;
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration declaration : types) {
				if (CharOperation.equals(declaration.name, TypeConstants.PACKAGE_INFO_NAME)) {
					continue;
				}
				ASTNode type = convert(declaration);
				if (type == null) {
					compilationUnit.setFlags(compilationUnit.getFlags() | ASTNode.MALFORMED);
				} else {
					compilationUnit.types().add(type);
				}
			}
		}
		compilationUnit.setSourceRange(unit.sourceStart, unit.sourceEnd - unit.sourceStart + 1);

		int problemLength = unit.compilationResult.problemCount;
		if (problemLength != 0) {
			CategorizedProblem[] resizedProblems = null;
			final CategorizedProblem[] problems = unit.compilationResult.getProblems();
			final int realProblemLength = problems.length;
			if (realProblemLength == problemLength) {
				resizedProblems = problems;
			} else {
				System.arraycopy(problems, 0, (resizedProblems = new CategorizedProblem[realProblemLength]), 0, realProblemLength);
			}
			ASTSyntaxErrorPropagator syntaxErrorPropagator = new ASTSyntaxErrorPropagator(resizedProblems);
			compilationUnit.accept(syntaxErrorPropagator);
			compilationUnit.setProblems(resizedProblems);
		}
		if (this.resolveBindings) {
			lookupForScopes();
		}
		compilationUnit.initCommentMapper(this.scanner);
		return compilationUnit;
	}

	// public Assignment convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompoundAssignment expression) {
	// Assignment assignment = new Assignment(this.ast);
	// Expression lhs = convert(expression.lhs);
	// assignment.setLeftHandSide(lhs);
	// int start = lhs.getStartPosition();
	// assignment.setSourceRange(start, expression.sourceEnd - start + 1);
	// switch (expression.operator) {
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS :
	// assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS :
	// assignment.setOperator(Assignment.Operator.MINUS_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MULTIPLY :
	// assignment.setOperator(Assignment.Operator.TIMES_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.DIVIDE :
	// assignment.setOperator(Assignment.Operator.DIVIDE_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND :
	// assignment.setOperator(Assignment.Operator.BIT_AND_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR :
	// assignment.setOperator(Assignment.Operator.BIT_OR_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.XOR :
	// assignment.setOperator(Assignment.Operator.BIT_XOR_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.REMAINDER :
	// assignment.setOperator(Assignment.Operator.REMAINDER_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.LEFT_SHIFT :
	// assignment.setOperator(Assignment.Operator.LEFT_SHIFT_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.RIGHT_SHIFT :
	// assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN);
	// break;
	// case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.UNSIGNED_RIGHT_SHIFT :
	// assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN);
	// break;
	// }
	// assignment.setRightHandSide(convert(expression.expression));
	// if (this.resolveBindings) {
	// recordNodes(assignment, expression);
	// }
	// return assignment;
	// }

	// public ConditionalExpression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConditionalExpression expression) {
	// ConditionalExpression conditionalExpression = new ConditionalExpression(this.ast);
	// if (this.resolveBindings) {
	// recordNodes(conditionalExpression, expression);
	// }
	// conditionalExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
	// conditionalExpression.setExpression(convert(expression.condition));
	// conditionalExpression.setThenExpression(convert(expression.valueIfTrue));
	// conditionalExpression.setElseExpression(convert(expression.valueIfFalse));
	// return conditionalExpression;
	// }

	// public Statement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall statement) {
	// Statement newStatement;
	// int sourceStart = statement.sourceStart;
	// if (statement.isSuperAccess() || statement.isSuper()) {
	// SuperConstructorInvocation superConstructorInvocation = new SuperConstructorInvocation(this.ast);
	// if (statement.qualification != null) {
	// superConstructorInvocation.setExpression(convert(statement.qualification));
	// }
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = statement.arguments;
	// if (arguments != null) {
	// int length = arguments.length;
	// for (int i = 0; i < length; i++) {
	// superConstructorInvocation.arguments().add(convert(arguments[i]));
	// }
	// }
	// if (statement.typeArguments != null) {
	// if (sourceStart > statement.typeArgumentsSourceStart) {
	// sourceStart = statement.typeArgumentsSourceStart;
	// }
	// switch(this.ast.apiLevel) {
	// case AST.JLS2_INTERNAL :
	// superConstructorInvocation.setFlags(superConstructorInvocation.getFlags() | ASTNode.MALFORMED);
	// break;
	// case AST.JLS3 :
	// for (int i = 0, max = statement.typeArguments.length; i < max; i++) {
	// superConstructorInvocation.typeArguments().add(convertType(statement.typeArguments[i]));
	// }
	// break;
	// }
	// }
	// newStatement = superConstructorInvocation;
	// } else {
	// ConstructorInvocation constructorInvocation = new ConstructorInvocation(this.ast);
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = statement.arguments;
	// if (arguments != null) {
	// int length = arguments.length;
	// for (int i = 0; i < length; i++) {
	// constructorInvocation.arguments().add(convert(arguments[i]));
	// }
	// }
	// if (statement.typeArguments != null) {
	// if (sourceStart > statement.typeArgumentsSourceStart) {
	// sourceStart = statement.typeArgumentsSourceStart;
	// }
	// switch(this.ast.apiLevel) {
	// case AST.JLS2_INTERNAL :
	// constructorInvocation.setFlags(constructorInvocation.getFlags() | ASTNode.MALFORMED);
	// break;
	// case AST.JLS3 :
	// for (int i = 0, max = statement.typeArguments.length; i < max; i++) {
	// constructorInvocation.typeArguments().add(convertType(statement.typeArguments[i]));
	// }
	// break;
	// }
	// }
	// if (statement.qualification != null) {
	// // this is an error
	// constructorInvocation.setFlags(constructorInvocation.getFlags() | ASTNode.MALFORMED);
	// }
	// newStatement = constructorInvocation;
	// }
	// newStatement.setSourceRange(sourceStart, statement.sourceEnd - sourceStart + 1);
	// retrieveSemiColonPosition(newStatement);
	// if (this.resolveBindings) {
	// recordNodes(newStatement, statement);
	// }
	// return newStatement;
	// }

	// public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression expression) {
	// if ((expression.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) != 0) {
	// return convertToParenthesizedExpression(expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.CastExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.CastExpression) expression);
	// }
	// // switch between all types of expression
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.AllocationExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.AllocationExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayInitializer) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.PrefixExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.PrefixExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.PostfixExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.PostfixExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompoundAssignment) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompoundAssignment) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.Assignment) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.Assignment) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.NullLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.NullLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.CharLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.CharLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.DoubleLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.DoubleLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.FloatLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.FloatLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LongLiteralMinValue) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.LongLiteralMinValue) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LongLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.LongLiteral) expression);
	// }
	// if (expression instanceof StringLiteralConcatenation) {
	// return convert((StringLiteralConcatenation) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.EqualExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.EqualExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.BinaryExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.UnaryExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.UnaryExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConditionalExpression) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConditionalExpression) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.Reference) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.Reference) expression);
	// }
	// if (expression instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference) {
	// return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference) expression);
	// }
	// return null;
	// }

	public StringLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral expression) {
		expression.computeConstant();
		StringLiteral literal = new StringLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setLiteralValue(expression.constant.stringValue());
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public BooleanLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral expression) {
		final BooleanLiteral literal = new BooleanLiteral(this.ast);
		literal.setBooleanValue(false);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldReference reference) {
		if (reference.receiver.isSuper()) {
			final SuperFieldAccess superFieldAccess = new SuperFieldAccess(this.ast);
			if (this.resolveBindings) {
				recordNodes(superFieldAccess, reference);
			}
			if (reference.receiver instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) {
				Name qualifier = convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) reference.receiver);
				superFieldAccess.setQualifier(qualifier);
				if (this.resolveBindings) {
					recordNodes(qualifier, reference.receiver);
				}
			}
			final SimpleName simpleName = new SimpleName(this.ast);
			simpleName.internalSetIdentifier(new String(reference.token));
			int sourceStart = (int) (reference.nameSourcePosition >>> 32);
			int length = (int) (reference.nameSourcePosition & 0xFFFFFFFF) - sourceStart + 1;
			simpleName.setSourceRange(sourceStart, length);
			superFieldAccess.setName(simpleName);
			if (this.resolveBindings) {
				recordNodes(simpleName, reference);
			}
			superFieldAccess.setSourceRange(reference.receiver.sourceStart, reference.sourceEnd - reference.receiver.sourceStart
					+ 1);
			return superFieldAccess;
		} else {
			final FieldAccess fieldAccess = new FieldAccess(this.ast);
			if (this.resolveBindings) {
				recordNodes(fieldAccess, reference);
			}
			Expression receiver = super.convert(reference.receiver);
			fieldAccess.setExpression(receiver);
			final SimpleName simpleName = new SimpleName(this.ast);
			simpleName.internalSetIdentifier(new String(reference.token));
			int sourceStart = (int) (reference.nameSourcePosition >>> 32);
			int length = (int) (reference.nameSourcePosition & 0xFFFFFFFF) - sourceStart + 1;
			simpleName.setSourceRange(sourceStart, length);
			fieldAccess.setName(simpleName);
			if (this.resolveBindings) {
				recordNodes(simpleName, reference);
			}
			fieldAccess.setSourceRange(receiver.getStartPosition(), reference.sourceEnd - receiver.getStartPosition() + 1);
			return fieldAccess;
		}
	}

	public NumberLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.FloatLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public Statement convert(ForeachStatement statement) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			return createFakeEmptyStatement(statement);
		case AST.JLS3:
			EnhancedForStatement enhancedForStatement = new EnhancedForStatement(this.ast);
			enhancedForStatement.setParameter(convertToSingleVariableDeclaration(statement.elementVariable));
			enhancedForStatement.setExpression(super.convert(statement.collection));
			enhancedForStatement.setBody(convert(statement.action));
			int start = statement.sourceStart;
			int end = statement.sourceEnd;
			enhancedForStatement.setSourceRange(start, end - start + 1);
			return enhancedForStatement;
		default:
			return createFakeEmptyStatement(statement);
		}
	}

	public ForStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ForStatement statement) {
		ForStatement forStatement = new ForStatement(this.ast);
		forStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement[] initializations = statement.initializations;
		if (initializations != null) {
			// we know that we have at least one initialization
			if (initializations[0] instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
				VariableDeclarationExpression variableDeclarationExpression = convertToVariableDeclarationExpression((org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) initializations[0]);
				int initializationsLength = initializations.length;
				for (int i = 1; i < initializationsLength; i++) {
					variableDeclarationExpression
							.fragments()
							.add(convertToVariableDeclarationFragment((org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) initializations[i]));
				}
				if (initializationsLength != 1) {
					int start = variableDeclarationExpression.getStartPosition();
					int end = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) initializations[initializationsLength - 1]).declarationSourceEnd;
					variableDeclarationExpression.setSourceRange(start, end - start + 1);
				}
				forStatement.initializers().add(variableDeclarationExpression);
			} else {
				int initializationsLength = initializations.length;
				for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement initialization : initializations) {
					Expression initializer = convertToExpression(initialization);
					if (initializer != null) {
						forStatement.initializers().add(initializer);
					} else {
						forStatement.setFlags(forStatement.getFlags() | ASTNode.MALFORMED);
					}
				}
			}
		}
		if (statement.condition != null) {
			forStatement.setExpression(super.convert(statement.condition));
		}
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement[] increments = statement.increments;
		if (increments != null) {
			int incrementsLength = increments.length;
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement increment : increments) {
				forStatement.updaters().add(convertToExpression(increment));
			}
		}
		forStatement.setBody(convert(statement.action));
		return forStatement;
	}

	public IfStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.IfStatement statement) {
		IfStatement ifStatement = new IfStatement(this.ast);
		ifStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		ifStatement.setExpression(super.convert(statement.condition));
		ifStatement.setThenStatement(convert(statement.thenStatement));
		if (statement.elseStatement != null) {
			ifStatement.setElseStatement(convert(statement.elseStatement));
		}
		return ifStatement;
	}

	public InstanceofExpression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression expression) {
		InstanceofExpression instanceOfExpression = new InstanceofExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(instanceOfExpression, expression);
		}
		Expression leftExpression = super.convert(expression.expression);
		instanceOfExpression.setLeftOperand(leftExpression);
		instanceOfExpression.setRightOperand(convertType(expression.type));
		int startPosition = leftExpression.getStartPosition();
		instanceOfExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		return instanceOfExpression;
	}

	public NumberLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		final NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		NumberLiteral literal = new NumberLiteral(this.ast);
		literal.internalSetToken(new String(this.compilationUnitSource, sourceStart, length));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public void convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc, BodyDeclaration bodyDeclaration) {
		if (bodyDeclaration.getJavadoc() == null) {
			if (javadoc != null) {
				if (this.commentMapper == null || !this.commentMapper.hasSameTable(this.commentsTable)) {
					this.commentMapper = new DefaultCommentMapper(this.commentsTable);
				}
				Comment comment = this.commentMapper.getComment(javadoc.sourceStart);
				if (comment != null && comment.isDocComment() && comment.getParent() == null) {
					Javadoc docComment = (Javadoc) comment;
					if (this.resolveBindings) {
						recordNodes(docComment, javadoc);
						// resolve member and method references binding
						for (Object o : docComment.tags()) {
							recordNodes(javadoc, (TagElement) o);
						}
					}
					bodyDeclaration.setJavadoc(docComment);
				}
			}
		}
	}

	public void convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc, PackageDeclaration packageDeclaration) {
		if (ast.apiLevel == AST.JLS3 && packageDeclaration.getJavadoc() == null) {
			if (javadoc != null) {
				if (this.commentMapper == null || !this.commentMapper.hasSameTable(this.commentsTable)) {
					this.commentMapper = new DefaultCommentMapper(this.commentsTable);
				}
				Comment comment = this.commentMapper.getComment(javadoc.sourceStart);
				if (comment != null && comment.isDocComment() && comment.getParent() == null) {
					Javadoc docComment = (Javadoc) comment;
					if (this.resolveBindings) {
						recordNodes(docComment, javadoc);
						// resolve member and method references binding
						for (Object o : docComment.tags()) {
							recordNodes(javadoc, (TagElement) o);
						}
					}
					packageDeclaration.setJavadoc(docComment);
				}
			}
		}
	}

	public LabeledStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.LabeledStatement statement) {
		LabeledStatement labeledStatement = new LabeledStatement(this.ast);
		labeledStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement body = statement.statement;
		labeledStatement.setBody(convert(body));
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(statement.label));
		retrieveIdentifierAndSetPositions(statement.sourceStart, statement.sourceEnd, name);
		labeledStatement.setLabel(name);
		return labeledStatement;
	}

	public InfixExpression convert(StringLiteralConcatenation expression) {
		expression.computeConstant();
		final InfixExpression infixExpression = new InfixExpression(this.ast);
		infixExpression.setOperator(InfixExpression.Operator.PLUS);
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression[] stringLiterals = expression.literals;
		infixExpression.setLeftOperand(super.convert(stringLiterals[0]));
		infixExpression.setRightOperand(super.convert(stringLiterals[1]));
		for (int i = 2; i < expression.counter; i++) {
			infixExpression.extendedOperands().add(super.convert(stringLiterals[i]));
		}
		if (this.resolveBindings) {
			this.recordNodes(infixExpression, expression);
		}
		infixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return infixExpression;
	}

	public NormalAnnotation convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.NormalAnnotation annotation) {
		final NormalAnnotation normalAnnotation = new NormalAnnotation(this.ast);
		setTypeNameForAnnotation(annotation, normalAnnotation);
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair[] memberValuePairs = annotation.memberValuePairs;
		if (memberValuePairs != null) {
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.MemberValuePair memberValuePair : memberValuePairs) {
				normalAnnotation.values().add(convert(memberValuePair));
			}
		}
		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;
		normalAnnotation.setSourceRange(start, end - start + 1);
		if (this.resolveBindings) {
			recordNodes(normalAnnotation, annotation);
		}
		return normalAnnotation;
	}

	public NullLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.NullLiteral expression) {
		final NullLiteral literal = new NullLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression expression) {
		final InfixExpression infixExpression = new InfixExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		Expression leftExpression = super.convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(super.convert(expression.right));
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		int sourceStart = leftExpression.getStartPosition();
		infixExpression.setSourceRange(sourceStart, expression.sourceEnd - sourceStart + 1);
		return infixExpression;
	}

	public PostfixExpression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.PostfixExpression expression) {
		final PostfixExpression postfixExpression = new PostfixExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(postfixExpression, expression);
		}
		postfixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		postfixExpression.setOperand(super.convert(expression.lhs));
		switch (expression.operator) {
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS:
			postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
			break;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS:
			postfixExpression.setOperator(PostfixExpression.Operator.DECREMENT);
			break;
		}
		return postfixExpression;
	}

	public PrefixExpression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.PrefixExpression expression) {
		final PrefixExpression prefixExpression = new PrefixExpression(this.ast);
		if (this.resolveBindings) {
			recordNodes(prefixExpression, expression);
		}
		prefixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		prefixExpression.setOperand(super.convert(expression.lhs));
		switch (expression.operator) {
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS:
			prefixExpression.setOperator(PrefixExpression.Operator.INCREMENT);
			break;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS:
			prefixExpression.setOperator(PrefixExpression.Operator.DECREMENT);
			break;
		}
		return prefixExpression;
	}

	public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression allocation) {
		final ClassInstanceCreation classInstanceCreation = new ClassInstanceCreation(this.ast);
		if (allocation.enclosingInstance != null) {
			classInstanceCreation.setExpression(super.convert(allocation.enclosingInstance));
		}
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			classInstanceCreation.internalSetName(convert(allocation.type));
			break;
		case AST.JLS3:
			classInstanceCreation.setType(convertType(allocation.type));
		}
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = allocation.arguments;
		if (arguments != null) {
			int length = arguments.length;
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression expression : arguments) {
				Expression argument = super.convert(expression);
				if (this.resolveBindings) {
					recordNodes(argument, expression);
				}
				classInstanceCreation.arguments().add(argument);
			}
		}
		if (allocation.typeArguments != null) {
			switch (this.ast.apiLevel) {
			case AST.JLS2_INTERNAL:
				classInstanceCreation.setFlags(classInstanceCreation.getFlags() | ASTNode.MALFORMED);
				break;
			case AST.JLS3:
				for (int i = 0, max = allocation.typeArguments.length; i < max; i++) {
					classInstanceCreation.typeArguments().add(convertType(allocation.typeArguments[i]));
				}
			}
		}
		if (allocation.anonymousType != null) {
			int declarationSourceStart = allocation.sourceStart;
			classInstanceCreation.setSourceRange(declarationSourceStart, allocation.anonymousType.bodyEnd - declarationSourceStart
					+ 1);
			final AnonymousClassDeclaration anonymousClassDeclaration = new AnonymousClassDeclaration(this.ast);
			int start = retrieveStartBlockPosition(allocation.anonymousType.sourceEnd, allocation.anonymousType.bodyEnd);
			anonymousClassDeclaration.setSourceRange(start, allocation.anonymousType.bodyEnd - start + 1);
			classInstanceCreation.setAnonymousClassDeclaration(anonymousClassDeclaration);
			buildBodyDeclarations(allocation.anonymousType, anonymousClassDeclaration);
			if (this.resolveBindings) {
				recordNodes(classInstanceCreation, allocation.anonymousType);
				recordNodes(anonymousClassDeclaration, allocation.anonymousType);
				anonymousClassDeclaration.resolveBinding();
			}
			return classInstanceCreation;
		} else {
			final int start = allocation.sourceStart;
			classInstanceCreation.setSourceRange(start, allocation.sourceEnd - start + 1);
			if (this.resolveBindings) {
				recordNodes(classInstanceCreation, allocation);
			}
//			removeTrailingCommentFromExpressionEndingWithAParen(classInstanceCreation);
			return classInstanceCreation;
		}
	}

	public Name convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference nameReference) {
		return setQualifiedNameNameAndSourceRanges(nameReference.tokens, nameReference.sourcePositions, nameReference);
	}

	public Name convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference reference) {
		return convert(reference.qualification);
	}

	public ThisExpression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference reference) {
		final ThisExpression thisExpression = new ThisExpression(this.ast);
		thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
		thisExpression.setQualifier(convert(reference.qualification));
		if (this.resolveBindings) {
			recordNodes(thisExpression, reference);
			recordPendingThisExpressionScopeResolution(thisExpression);
		}
		return thisExpression;
	}

	public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Reference reference) {
		if (reference instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.NameReference) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.NameReference) reference);
		}
		if (reference instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThisReference) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThisReference) reference);
		}
		if (reference instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayReference) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ArrayReference) reference);
		}
		if (reference instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldReference) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldReference) reference);
		}
		return null; // cannot be reached
	}

	public ReturnStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement statement) {
		final ReturnStatement returnStatement = new ReturnStatement(this.ast);
		returnStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.expression != null) {
			returnStatement.setExpression(super.convert(statement.expression));
		}
		retrieveSemiColonPosition(returnStatement);
		return returnStatement;
	}

	public SingleMemberAnnotation convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation annotation) {
		final SingleMemberAnnotation singleMemberAnnotation = new SingleMemberAnnotation(this.ast);
		setTypeNameForAnnotation(annotation, singleMemberAnnotation);
		singleMemberAnnotation.setValue(super.convert(annotation.memberValue));
		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;
		singleMemberAnnotation.setSourceRange(start, end - start + 1);
		if (this.resolveBindings) {
			recordNodes(singleMemberAnnotation, annotation);
		}
		return singleMemberAnnotation;
	}

	public SimpleName convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleNameReference nameReference) {
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(nameReference.token));
		if (this.resolveBindings) {
			recordNodes(name, nameReference);
		}
		name.setSourceRange(nameReference.sourceStart, nameReference.sourceEnd - nameReference.sourceStart + 1);
		return name;
	}

	public Statement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof ForeachStatement) {
			return convert((ForeachStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) {
			return convertToVariableDeclarationStatement((org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.AssertStatement) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.AssertStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.Block) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.Block) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.BreakStatement) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.BreakStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ContinueStatement) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ContinueStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.CaseStatement) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.CaseStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.DoStatement) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.DoStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.EmptyStatement) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.EmptyStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ForStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ForStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.IfStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.IfStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.LabeledStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.LabeledStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ReturnStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.SwitchStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.SwitchStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThrowStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThrowStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.TryStatement) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.TryStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
			ASTNode result = convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) statement);
			if (result == null) {
				return createFakeEmptyStatement(statement);
			}
			switch (result.getNodeType()) {
			case ASTNode.ENUM_DECLARATION:
				switch (this.ast.apiLevel) {
				case AST.JLS2_INTERNAL:
					return createFakeEmptyStatement(statement);
				case AST.JLS3:
					final TypeDeclarationStatement typeDeclarationStatement = new TypeDeclarationStatement(this.ast);
					typeDeclarationStatement.setDeclaration((EnumDeclaration) result);
					AbstractTypeDeclaration typeDecl = typeDeclarationStatement.getDeclaration();
					typeDeclarationStatement.setSourceRange(typeDecl.getStartPosition(), typeDecl.getLength());
					return typeDeclarationStatement;
				}
				break;
			case ASTNode.ANNOTATION_TYPE_DECLARATION:
				switch (this.ast.apiLevel) {
				case AST.JLS2_INTERNAL:
					return createFakeEmptyStatement(statement);
				case AST.JLS3:
					TypeDeclarationStatement typeDeclarationStatement = new TypeDeclarationStatement(this.ast);
					typeDeclarationStatement.setDeclaration((AnnotationTypeDeclaration) result);
					AbstractTypeDeclaration typeDecl = typeDeclarationStatement.getDeclaration();
					typeDeclarationStatement.setSourceRange(typeDecl.getStartPosition(), typeDecl.getLength());
					return typeDeclarationStatement;
				}
				break;
			default:
				TypeDeclaration typeDeclaration = (TypeDeclaration) result;
				if (typeDeclaration == null) {
					return createFakeEmptyStatement(statement);
				} else {
					TypeDeclarationStatement typeDeclarationStatement = new TypeDeclarationStatement(this.ast);
					typeDeclarationStatement.setDeclaration(typeDeclaration);
					switch (this.ast.apiLevel) {
					case AST.JLS2_INTERNAL:
						TypeDeclaration typeDecl = typeDeclarationStatement.internalGetTypeDeclaration();
						typeDeclarationStatement.setSourceRange(typeDecl.getStartPosition(), typeDecl.getLength());
						break;
					case AST.JLS3:
						AbstractTypeDeclaration typeDeclAST3 = typeDeclarationStatement.getDeclaration();
						typeDeclarationStatement.setSourceRange(typeDeclAST3.getStartPosition(), typeDeclAST3.getLength());
						break;
					}
					return typeDeclarationStatement;
				}
			}
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.WhileStatement) {
			return super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.WhileStatement) statement);
		}
		if (statement instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression) {
			final Expression expr = super.convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression) statement);
			final ExpressionStatement stmt = new ExpressionStatement(this.ast);
			stmt.setExpression(expr);
			stmt.setSourceRange(expr.getStartPosition(), expr.getLength());
			retrieveSemiColonPosition(stmt);
			return stmt;
		}
		return createFakeEmptyStatement(statement);
	}

	public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.StringLiteral expression) {
		if (expression instanceof StringLiteralConcatenation) {
			return convert((StringLiteralConcatenation) expression);
		}
		int length = expression.sourceEnd - expression.sourceStart + 1;
		int sourceStart = expression.sourceStart;
		StringLiteral literal = new StringLiteral(this.ast);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.internalSetEscapedValue(new String(this.compilationUnitSource, sourceStart, length));
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public SwitchStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.SwitchStatement statement) {
		SwitchStatement switchStatement = new SwitchStatement(this.ast);
		switchStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		switchStatement.setExpression(super.convert(statement.expression));
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement[] statements = statement.statements;
		if (statements != null) {
			int statementsLength = statements.length;
			for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement value : statements) {
				switchStatement.statements().add(convert(value));
			}
		}
		return switchStatement;
	}

	public SynchronizedStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement statement) {
		SynchronizedStatement synchronizedStatement = new SynchronizedStatement(this.ast);
		synchronizedStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		synchronizedStatement.setBody(convert(statement.block));
		synchronizedStatement.setExpression(super.convert(statement.expression));
		return synchronizedStatement;
	}

	public Expression convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThisReference reference) {
		if (reference.isImplicitThis()) {
			// There is no source associated with an implicit this
			return null;
		} else if (reference instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference) reference);
		} else if (reference instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference) {
			return convert((org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference) reference);
		} else {
			ThisExpression thisExpression = new ThisExpression(this.ast);
			thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
			if (this.resolveBindings) {
				recordNodes(thisExpression, reference);
				recordPendingThisExpressionScopeResolution(thisExpression);
			}
			return thisExpression;
		}
	}

	public ThrowStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.ThrowStatement statement) {
		final ThrowStatement throwStatement = new ThrowStatement(this.ast);
		throwStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		throwStatement.setExpression(super.convert(statement.exception));
		retrieveSemiColonPosition(throwStatement);
		return throwStatement;
	}

	public BooleanLiteral convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral expression) {
		final BooleanLiteral literal = new BooleanLiteral(this.ast);
		literal.setBooleanValue(true);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public TryStatement convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TryStatement statement) {
		final TryStatement tryStatement = new TryStatement(this.ast);
		tryStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);

		tryStatement.setBody(convert(statement.tryBlock));
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument[] catchArguments = statement.catchArguments;
		if (catchArguments != null) {
			int catchArgumentsLength = catchArguments.length;
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Block[] catchBlocks = statement.catchBlocks;
			int start = statement.tryBlock.sourceEnd;
			for (int i = 0; i < catchArgumentsLength; i++) {
				CatchClause catchClause = new CatchClause(this.ast);
				int catchClauseSourceStart = retrieveStartingCatchPosition(start, catchArguments[i].sourceStart);
				catchClause.setSourceRange(catchClauseSourceStart, catchBlocks[i].sourceEnd - catchClauseSourceStart + 1);
				catchClause.setBody(convert(catchBlocks[i]));
				catchClause.setException(convert(catchArguments[i]));
				tryStatement.catchClauses().add(catchClause);
				start = catchBlocks[i].sourceEnd;
			}
		}
		if (statement.finallyBlock != null) {
			tryStatement.setFinally(convert(statement.finallyBlock));
		}
		return tryStatement;
	}

	public ASTNode convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		int kind = org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.kind(typeDeclaration.modifiers);
		switch (kind) {
			case org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.ENUM_DECL :
				if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
					return null;
				} else {
					return convertToEnumDeclaration(typeDeclaration);
				}
			case org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.ANNOTATION_TYPE_DECL :
				if (this.ast.apiLevel == AST.JLS2_INTERNAL) {
					return null;
				} else {
					return convertToAnnotationDeclaration(typeDeclaration);
				}
		}

		checkCanceled();
		TypeDeclaration typeDecl = TypeDeclaration.getTypeDeclaration(this.ast);

		// ////////////// ajh02: added
		if (typeDeclaration instanceof AspectDeclaration) {
			org.aspectj.weaver.patterns.PerClause perClause = ((AspectDeclaration) typeDeclaration).perClause;
			boolean isPrivileged = ((AspectDeclaration) typeDeclaration).isPrivileged;
			if (perClause == null) {
				typeDecl = new org.aspectj.org.eclipse.jdt.core.dom.AspectDeclaration(this.ast, null, isPrivileged);
			} else {
				typeDecl = new org.aspectj.org.eclipse.jdt.core.dom.AspectDeclaration(this.ast, convert(perClause), isPrivileged);
			}
		}
		// /////////////////////////////
		if (typeDeclaration.modifiersSourceStart != -1) {
			setModifiers(typeDecl, typeDeclaration);
		}
		boolean isInterface = kind == org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration.INTERFACE_DECL;
		typeDecl.setInterface(isInterface);
		final SimpleName typeName = new SimpleName(this.ast);
		typeName.internalSetIdentifier(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(typeName);
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);

		// need to set the superclass and super interfaces here since we cannot distinguish them at
		// the type references level.
		if (typeDeclaration.superclass != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					typeDecl.internalSetSuperclass(convert(typeDeclaration.superclass));
					break;
				default :
					typeDecl.setSuperclassType(convertType(typeDeclaration.superclass));
					break;
			}
		}

		org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					for (TypeReference anInterface : superInterfaces) {
						typeDecl.internalSuperInterfaces().add(convert(anInterface));
					}
					break;
				default :
					for (TypeReference superInterface : superInterfaces) {
						typeDecl.superInterfaceTypes().add(convertType(superInterface));
					}
			}
		}
		org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter[] typeParameters = typeDeclaration.typeParameters;
		if (typeParameters != null) {
			switch(this.ast.apiLevel) {
				case AST.JLS2_INTERNAL :
					typeDecl.setFlags(typeDecl.getFlags() | ASTNode.MALFORMED);
					break;
				default :
					for (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParameter : typeParameters) {
						typeDecl.typeParameters().add(convert(typeParameter));
					}
			}
		}
		buildBodyDeclarations(typeDeclaration, typeDecl, isInterface);
		if (this.resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			typeDecl.resolveBinding();
		}
		return typeDecl;
	}

	public TypeParameter convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParameter) {
		final TypeParameter typeParameter2 = new TypeParameter(this.ast);
		final SimpleName simpleName = new SimpleName(this.ast);
		simpleName.internalSetIdentifier(new String(typeParameter.name));
		int start = typeParameter.sourceStart;
		int end = typeParameter.sourceEnd;
		simpleName.setSourceRange(start, end - start + 1);
		typeParameter2.setName(simpleName);
		final TypeReference superType = typeParameter.type;
		end = typeParameter.declarationSourceEnd;
		if (superType != null) {
			Type type = convertType(superType);
			typeParameter2.typeBounds().add(type);
			end = type.getStartPosition() + type.getLength() - 1;
		}
		TypeReference[] bounds = typeParameter.bounds;
		if (bounds != null) {
			Type type = null;
			for (TypeReference bound : bounds) {
				type = convertType(bound);
				typeParameter2.typeBounds().add(type);
				end = type.getStartPosition() + type.getLength() - 1;
			}
		}
		start = typeParameter.declarationSourceStart;
		end = retrieveClosingAngleBracketPosition(end);
		typeParameter2.setSourceRange(start, end - start + 1);
		if (this.resolveBindings) {
			recordName(simpleName, typeParameter);
			recordNodes(typeParameter2, typeParameter);
			typeParameter2.resolveBinding();
		}
		return typeParameter2;
	}

	public Name convert(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference) {
		char[][] typeName = typeReference.getTypeName();
		int length = typeName.length;
		if (length > 1) {
			// QualifiedName
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference qualifiedTypeReference = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference;
			final long[] positions = qualifiedTypeReference.sourcePositions;
			return setQualifiedNameNameAndSourceRanges(typeName, positions, typeReference);
		} else {
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(typeName[0]));
			name.setSourceRange(typeReference.sourceStart, typeReference.sourceEnd - typeReference.sourceStart + 1);
			if (this.resolveBindings) {
				recordNodes(name, typeReference);
			}
			return name;
		}
	}

	protected FieldDeclaration convertToFieldDeclaration(
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDecl) {
		VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(fieldDecl);
		final FieldDeclaration fieldDeclaration = new FieldDeclaration(this.ast);
		fieldDeclaration.fragments().add(variableDeclarationFragment);
		IVariableBinding binding = null;
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, fieldDecl);
			binding = variableDeclarationFragment.resolveBinding();
		}
		fieldDeclaration.setSourceRange(fieldDecl.declarationSourceStart, fieldDecl.declarationEnd
				- fieldDecl.declarationSourceStart + 1);
		Type type = convertType(fieldDecl.type);
		setTypeForField(fieldDeclaration, type, variableDeclarationFragment.getExtraDimensions());
		setModifiers(fieldDeclaration, fieldDecl);
		if (!(this.resolveBindings && binding == null)) {
			convert(fieldDecl.javadoc, fieldDeclaration);
		}
		return fieldDeclaration;
	}

	// public ParenthesizedExpression convertToParenthesizedExpression(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression
	// expression) {
	// final ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(this.ast);
	// if (this.resolveBindings) {
	// recordNodes(parenthesizedExpression, expression);
	// }
	// parenthesizedExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
	// adjustSourcePositionsForParent(expression);
	// trimWhiteSpacesAndComments(expression);
	// // decrement the number of parenthesis
	// int numberOfParenthesis = (expression.bits & org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK) >>
	// org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedSHIFT;
	// expression.bits &= ~org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedMASK;
	// expression.bits |= (numberOfParenthesis - 1) << org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode.ParenthesizedSHIFT;
	// parenthesizedExpression.setExpression(convert(expression));
	// return parenthesizedExpression;
	// }

	// public Type convertToType(org.aspectj.org.eclipse.jdt.internal.compiler.ast.NameReference reference) {
	// Name name = convert(reference);
	// final SimpleType type = new SimpleType(this.ast);
	// type.setName(name);
	// type.setSourceRange(name.getStartPosition(), name.getLength());
	// if (this.resolveBindings) {
	// this.recordNodes(type, reference);
	// }
	// return type;
	// }

	protected VariableDeclarationExpression convertToVariableDeclarationExpression(
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration localDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(localDeclaration);
		final VariableDeclarationExpression variableDeclarationExpression = new VariableDeclarationExpression(this.ast);
		variableDeclarationExpression.fragments().add(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
		}
		variableDeclarationExpression.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd
				- localDeclaration.declarationSourceStart + 1);
		Type type = convertType(localDeclaration.type);
		setTypeForVariableDeclarationExpression(variableDeclarationExpression, type,
				variableDeclarationFragment.getExtraDimensions());
		if (localDeclaration.modifiersSourceStart != -1) {
			setModifiers(variableDeclarationExpression, localDeclaration);
		}
		return variableDeclarationExpression;
	}

	protected SingleVariableDeclaration convertToSingleVariableDeclaration(LocalDeclaration localDeclaration) {
		final SingleVariableDeclaration variableDecl = new SingleVariableDeclaration(this.ast);
		setModifiers(variableDecl, localDeclaration);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(localDeclaration.name));
		int start = localDeclaration.sourceStart;
		int nameEnd = localDeclaration.sourceEnd;
		name.setSourceRange(start, nameEnd - start + 1);
		variableDecl.setName(name);
		final int extraDimensions = retrieveExtraDimension(nameEnd + 1, localDeclaration.type.sourceEnd);
		variableDecl.setExtraDimensions(extraDimensions);
		Type type = convertType(localDeclaration.type);
		int typeEnd = type.getStartPosition() + type.getLength() - 1;
		int rightEnd = Math.max(typeEnd, localDeclaration.declarationSourceEnd);
		/*
		 * There is extra work to do to set the proper type positions See PR http://bugs.eclipse.org/bugs/show_bug.cgi?id=23284
		 */
		setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions);
		variableDecl
				.setSourceRange(localDeclaration.declarationSourceStart, rightEnd - localDeclaration.declarationSourceStart + 1);
		if (this.resolveBindings) {
			recordNodes(name, localDeclaration);
			recordNodes(variableDecl, localDeclaration);
			variableDecl.resolveBinding();
		}
		return variableDecl;
	}

	protected VariableDeclarationFragment convertToVariableDeclarationFragment(InterTypeFieldDeclaration fieldDeclaration) {
		// ajh02: method added
		final VariableDeclarationFragment variableDeclarationFragment = new VariableDeclarationFragment(this.ast);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(fieldDeclaration.getDeclaredSelector()));
		name.setSourceRange(fieldDeclaration.sourceStart, fieldDeclaration.sourceEnd - fieldDeclaration.sourceStart + 1);
		variableDeclarationFragment.setName(name);
		int start = fieldDeclaration.sourceEnd;
		if (fieldDeclaration.initialization != null) {
			final Expression expression = super.convert(fieldDeclaration.initialization);
			variableDeclarationFragment.setInitializer(expression);
			start = expression.getStartPosition() + expression.getLength();
		}
		int end = retrievePositionBeforeNextCommaOrSemiColon(start, fieldDeclaration.declarationSourceEnd);
		if (end == -1) {
			variableDeclarationFragment.setSourceRange(fieldDeclaration.sourceStart, fieldDeclaration.declarationSourceEnd
					- fieldDeclaration.sourceStart + 1);
			variableDeclarationFragment.setFlags(variableDeclarationFragment.getFlags() | ASTNode.MALFORMED);
		} else {
			variableDeclarationFragment.setSourceRange(fieldDeclaration.sourceStart, end - fieldDeclaration.sourceStart + 1);
		}
		variableDeclarationFragment.setExtraDimensions(retrieveExtraDimension(fieldDeclaration.sourceEnd + 1,
				fieldDeclaration.declarationSourceEnd));
		if (this.resolveBindings) {
			recordNodes(name, fieldDeclaration);
			recordNodes(variableDeclarationFragment, fieldDeclaration);
			variableDeclarationFragment.resolveBinding();
		}
		return variableDeclarationFragment;
	}

	protected VariableDeclarationFragment convertToVariableDeclarationFragment(
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = new VariableDeclarationFragment(this.ast);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(fieldDeclaration.name));
		name.setSourceRange(fieldDeclaration.sourceStart, fieldDeclaration.sourceEnd - fieldDeclaration.sourceStart + 1);
		variableDeclarationFragment.setName(name);
		int start = fieldDeclaration.sourceEnd;
		if (fieldDeclaration.initialization != null) {
			final Expression expression = super.convert(fieldDeclaration.initialization);
			variableDeclarationFragment.setInitializer(expression);
			start = expression.getStartPosition() + expression.getLength();
		}
		int end = retrievePositionBeforeNextCommaOrSemiColon(start, fieldDeclaration.declarationSourceEnd);
		if (end == -1) {
			variableDeclarationFragment.setSourceRange(fieldDeclaration.sourceStart, fieldDeclaration.declarationSourceEnd
					- fieldDeclaration.sourceStart + 1);
			variableDeclarationFragment.setFlags(variableDeclarationFragment.getFlags() | ASTNode.MALFORMED);
		} else {
			variableDeclarationFragment.setSourceRange(fieldDeclaration.sourceStart, end - fieldDeclaration.sourceStart + 1);
		}
		variableDeclarationFragment.setExtraDimensions(retrieveExtraDimension(fieldDeclaration.sourceEnd + 1,
				fieldDeclaration.declarationSourceEnd));
		if (this.resolveBindings) {
			recordNodes(name, fieldDeclaration);
			recordNodes(variableDeclarationFragment, fieldDeclaration);
			variableDeclarationFragment.resolveBinding();
		}
		return variableDeclarationFragment;
	}

	protected VariableDeclarationFragment convertToVariableDeclarationFragment(
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration localDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = new VariableDeclarationFragment(this.ast);
		final SimpleName name = new SimpleName(this.ast);
		name.internalSetIdentifier(new String(localDeclaration.name));
		name.setSourceRange(localDeclaration.sourceStart, localDeclaration.sourceEnd - localDeclaration.sourceStart + 1);
		variableDeclarationFragment.setName(name);
		int start = localDeclaration.sourceEnd;
		if (localDeclaration.initialization != null) {
			final Expression expression = super.convert(localDeclaration.initialization);
			variableDeclarationFragment.setInitializer(expression);
			start = expression.getStartPosition() + expression.getLength();
		}
		int end = retrievePositionBeforeNextCommaOrSemiColon(start, localDeclaration.declarationSourceEnd);
		if (end == -1) {
			if (localDeclaration.initialization != null) {
				variableDeclarationFragment.setSourceRange(localDeclaration.sourceStart, localDeclaration.initialization.sourceEnd
						- localDeclaration.sourceStart + 1);
			} else {
				variableDeclarationFragment.setSourceRange(localDeclaration.sourceStart, localDeclaration.sourceEnd
						- localDeclaration.sourceStart + 1);
			}
		} else {
			variableDeclarationFragment.setSourceRange(localDeclaration.sourceStart, end - localDeclaration.sourceStart + 1);
		}
		variableDeclarationFragment.setExtraDimensions(retrieveExtraDimension(localDeclaration.sourceEnd + 1,
				this.compilationUnitSourceLength));
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
			recordNodes(name, localDeclaration);
			variableDeclarationFragment.resolveBinding();
		}
		return variableDeclarationFragment;
	}

	protected VariableDeclarationStatement convertToVariableDeclarationStatement(
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.LocalDeclaration localDeclaration) {
		final VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(localDeclaration);
		final VariableDeclarationStatement variableDeclarationStatement = new VariableDeclarationStatement(this.ast);
		variableDeclarationStatement.fragments().add(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
		}
		variableDeclarationStatement.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd
				- localDeclaration.declarationSourceStart + 1);
		Type type = convertType(localDeclaration.type);
		setTypeForVariableDeclarationStatement(variableDeclarationStatement, type, variableDeclarationFragment.getExtraDimensions());
		if (localDeclaration.modifiersSourceStart != -1) {
			setModifiers(variableDeclarationStatement, localDeclaration);
		}
		return variableDeclarationStatement;
	}

	public Type convertType(TypeReference typeReference) {
		if (typeReference instanceof Wildcard) {
			final Wildcard wildcard = (Wildcard) typeReference;
			final WildcardType wildcardType = new WildcardType(this.ast);
			if (wildcard.bound != null) {
				final Type bound = convertType(wildcard.bound);
				wildcardType.setBound(bound, wildcard.kind == Wildcard.EXTENDS);
				int start = wildcard.sourceStart;
				wildcardType.setSourceRange(start, bound.getStartPosition() + bound.getLength() - start);
			} else {
				final int start = wildcard.sourceStart;
				final int end = wildcard.sourceEnd;
				wildcardType.setSourceRange(start, end - start + 1);
			}
			if (this.resolveBindings) {
				recordNodes(wildcardType, typeReference);
			}
			return wildcardType;
		}
		Type type = null;
		int sourceStart = -1;
		int length = 0;
		int dimensions = typeReference.dimensions();
		if (typeReference instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference) {
			// this is either an ArrayTypeReference or a SingleTypeReference
			char[] name = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.SingleTypeReference) typeReference).getTypeName()[0];
			sourceStart = typeReference.sourceStart;
			length = typeReference.sourceEnd - typeReference.sourceStart + 1;
			// need to find out if this is an array type of primitive types or not
			if (isPrimitiveType(name)) {
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length)[1];
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				final PrimitiveType primitiveType = new PrimitiveType(this.ast);
				primitiveType.setPrimitiveTypeCode(getPrimitiveTypeCode(name));
				primitiveType.setSourceRange(sourceStart, end - sourceStart + 1);
				type = primitiveType;
			} else if (typeReference instanceof ParameterizedSingleTypeReference) {
				ParameterizedSingleTypeReference parameterizedSingleTypeReference = (ParameterizedSingleTypeReference) typeReference;
				final SimpleName simpleName = new SimpleName(this.ast);
				simpleName.internalSetIdentifier(new String(name));
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length)[1];
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				simpleName.setSourceRange(sourceStart, end - sourceStart + 1);
				switch (this.ast.apiLevel) {
				case AST.JLS2_INTERNAL:
					SimpleType simpleType = new SimpleType(this.ast);
					simpleType.setName(simpleName);
					simpleType.setFlags(simpleType.getFlags() | ASTNode.MALFORMED);
					simpleType.setSourceRange(sourceStart, end - sourceStart + 1);
					type = simpleType;
					if (this.resolveBindings) {
						this.recordNodes(simpleName, typeReference);
					}
					break;
				case AST.JLS3:
					simpleType = new SimpleType(this.ast);
					simpleType.setName(simpleName);
					simpleType.setSourceRange(simpleName.getStartPosition(), simpleName.getLength());
					final ParameterizedType parameterizedType = new ParameterizedType(this.ast);
					parameterizedType.setType(simpleType);
					type = parameterizedType;
					TypeReference[] typeArguments = parameterizedSingleTypeReference.typeArguments;
					if (typeArguments != null) {
						Type type2 = null;
						for (TypeReference typeArgument : typeArguments) {
							type2 = convertType(typeArgument);
							((ParameterizedType) type).typeArguments().add(type2);
							end = type2.getStartPosition() + type2.getLength() - 1;
						}
						end = retrieveClosingAngleBracketPosition(end + 1);
						type.setSourceRange(sourceStart, end - sourceStart + 1);
					} else {
						type.setSourceRange(sourceStart, end - sourceStart + 1);
					}
					if (this.resolveBindings) {
						this.recordNodes(simpleName, typeReference);
						this.recordNodes(simpleType, typeReference);
					}
				}
			} else {
				final SimpleName simpleName = new SimpleName(this.ast);
				simpleName.internalSetIdentifier(new String(name));
				// we need to search for the starting position of the first brace in order to set the proper length
				// PR http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length)[1];
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				simpleName.setSourceRange(sourceStart, end - sourceStart + 1);
				final SimpleType simpleType = new SimpleType(this.ast);
				simpleType.setName(simpleName);
				type = simpleType;
				type.setSourceRange(sourceStart, end - sourceStart + 1);
				type = simpleType;
				if (this.resolveBindings) {
					this.recordNodes(simpleName, typeReference);
				}
			}
			if (dimensions != 0) {
				type = this.ast.newArrayType(type, dimensions);
				type.setSourceRange(sourceStart, length);
				ArrayType subarrayType = (ArrayType) type;
				int index = dimensions - 1;
				while (index > 0) {
					subarrayType = (ArrayType) subarrayType.getComponentType();
					int end = retrieveProperRightBracketPosition(index, sourceStart);
					subarrayType.setSourceRange(sourceStart, end - sourceStart + 1);
					index--;
				}
				if (this.resolveBindings) {
					// store keys for inner types
					completeRecord((ArrayType) type, typeReference);
				}
			}
		} else {
			if (typeReference instanceof ParameterizedQualifiedTypeReference) {
				ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference = (ParameterizedQualifiedTypeReference) typeReference;
				char[][] tokens = parameterizedQualifiedTypeReference.tokens;
				TypeReference[][] typeArguments = parameterizedQualifiedTypeReference.typeArguments;
				long[] positions = parameterizedQualifiedTypeReference.sourcePositions;
				sourceStart = (int) (positions[0] >>> 32);
				switch (this.ast.apiLevel) {
				case AST.JLS2_INTERNAL: {
					char[][] name = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference)
							.getTypeName();
					int nameLength = name.length;
					sourceStart = (int) (positions[0] >>> 32);
					length = (int) (positions[nameLength - 1] & 0xFFFFFFFF) - sourceStart + 1;
					Name qualifiedName = this.setQualifiedNameNameAndSourceRanges(name, positions, typeReference);
					final SimpleType simpleType = new SimpleType(this.ast);
					simpleType.setName(qualifiedName);
					simpleType.setSourceRange(sourceStart, length);
					type = simpleType;
				}
					break;
				case AST.JLS3:
					if (typeArguments != null) {
						int numberOfEnclosingType = 0;
						int startingIndex = 0;
						int endingIndex = 0;
						for (TypeReference[] typeArgument : typeArguments) {
							if (typeArgument != null) {
								numberOfEnclosingType++;
							} else if (numberOfEnclosingType == 0) {
								endingIndex++;
							}
						}
						Name name = null;
						if (endingIndex - startingIndex == 0) {
							final SimpleName simpleName = new SimpleName(this.ast);
							simpleName.internalSetIdentifier(new String(tokens[startingIndex]));
							recordPendingNameScopeResolution(simpleName);
							int start = (int) (positions[startingIndex] >>> 32);
							int end = (int) positions[startingIndex];
							simpleName.setSourceRange(start, end - start + 1);
							simpleName.index = 1;
							name = simpleName;
							if (this.resolveBindings) {
								recordNodes(simpleName, typeReference);
							}
						} else {
							name = this.setQualifiedNameNameAndSourceRanges(tokens, positions, endingIndex, typeReference);
						}
						SimpleType simpleType = new SimpleType(this.ast);
						simpleType.setName(name);
						int start = (int) (positions[startingIndex] >>> 32);
						int end = (int) positions[endingIndex];
						simpleType.setSourceRange(start, end - start + 1);
						ParameterizedType parameterizedType = new ParameterizedType(this.ast);
						parameterizedType.setType(simpleType);
						if (this.resolveBindings) {
							recordNodes(simpleType, typeReference);
							recordNodes(parameterizedType, typeReference);
						}
						start = simpleType.getStartPosition();
						end = start + simpleType.getLength() - 1;
						for (int i = 0, max = typeArguments[endingIndex].length; i < max; i++) {
							final Type type2 = convertType(typeArguments[endingIndex][i]);
							parameterizedType.typeArguments().add(type2);
							end = type2.getStartPosition() + type2.getLength() - 1;
						}
						int indexOfEnclosingType = 1;
						parameterizedType.index = indexOfEnclosingType;
						end = retrieveClosingAngleBracketPosition(end + 1);
						length = end + 1;
						parameterizedType.setSourceRange(start, end - start + 1);
						startingIndex = endingIndex + 1;
						Type currentType = parameterizedType;
						while (startingIndex < typeArguments.length) {
							SimpleName simpleName = new SimpleName(this.ast);
							simpleName.internalSetIdentifier(new String(tokens[startingIndex]));
							simpleName.index = startingIndex + 1;
							start = (int) (positions[startingIndex] >>> 32);
							end = (int) positions[startingIndex];
							simpleName.setSourceRange(start, end - start + 1);
							recordPendingNameScopeResolution(simpleName);
							QualifiedType qualifiedType = new QualifiedType(this.ast);
							qualifiedType.setQualifier(currentType);
							qualifiedType.setName(simpleName);
							if (this.resolveBindings) {
								recordNodes(simpleName, typeReference);
								recordNodes(qualifiedType, typeReference);
							}
							start = currentType.getStartPosition();
							end = simpleName.getStartPosition() + simpleName.getLength() - 1;
							qualifiedType.setSourceRange(start, end - start + 1);
							indexOfEnclosingType++;
							if (typeArguments[startingIndex] != null) {
								qualifiedType.index = indexOfEnclosingType;
								ParameterizedType parameterizedType2 = new ParameterizedType(this.ast);
								parameterizedType2.setType(qualifiedType);
								parameterizedType2.index = indexOfEnclosingType;
								if (this.resolveBindings) {
									recordNodes(parameterizedType2, typeReference);
								}
								for (int i = 0, max = typeArguments[startingIndex].length; i < max; i++) {
									final Type type2 = convertType(typeArguments[startingIndex][i]);
									parameterizedType2.typeArguments().add(type2);
									end = type2.getStartPosition() + type2.getLength() - 1;
								}
								end = retrieveClosingAngleBracketPosition(end + 1);
								length = end + 1;
								parameterizedType2.setSourceRange(start, end - start + 1);
								currentType = parameterizedType2;
							} else {
								currentType = qualifiedType;
								qualifiedType.index = indexOfEnclosingType;
							}
							startingIndex++;
						}
						if (this.resolveBindings) {
							this.recordNodes(currentType, typeReference);
						}
						type = currentType;
						length -= sourceStart;
					}
				}
			} else {
				char[][] name = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference)
						.getTypeName();
				int nameLength = name.length;
				long[] positions = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference).sourcePositions;
				sourceStart = (int) (positions[0] >>> 32);
				length = (int) (positions[nameLength - 1] & 0xFFFFFFFF) - sourceStart + 1;
				final Name qualifiedName = this.setQualifiedNameNameAndSourceRanges(name, positions, typeReference);
				final SimpleType simpleType = new SimpleType(this.ast);
				simpleType.setName(qualifiedName);
				type = simpleType;
				type.setSourceRange(sourceStart, length);
			}

			if (dimensions != 0) {
				type = this.ast.newArrayType(type, dimensions);
				if (this.resolveBindings) {
					completeRecord((ArrayType) type, typeReference);
				}
				int end = retrieveEndOfDimensionsPosition(sourceStart + length, this.compilationUnitSourceLength);
				if (end != -1) {
					type.setSourceRange(sourceStart, end - sourceStart + 1);
				} else {
					type.setSourceRange(sourceStart, length);
				}
				ArrayType subarrayType = (ArrayType) type;
				int index = dimensions - 1;
				while (index > 0) {
					subarrayType = (ArrayType) subarrayType.getComponentType();
					end = retrieveProperRightBracketPosition(index, sourceStart);
					subarrayType.setSourceRange(sourceStart, end - sourceStart + 1);
					index--;
				}
			}
		}
		if (this.resolveBindings) {
			this.recordNodes(type, typeReference);
		}
		return type;
	}

	protected Comment createComment(int[] positions) {
		// Create comment node
		Comment comment = null;
		int start = positions[0];
		int end = positions[1];
		if (positions[1] > 0) { // Javadoc comments have positive end position
			Javadoc docComment = this.docParser.parse(positions);
			if (docComment == null) {
				return null;
			}
			comment = docComment;
		} else {
			end = -end;
			if (positions[0] > 0) { // Block comment have positive start position
				comment = new BlockComment(this.ast);
			} else { // Line comment have negative start and end position
				start = -start;
				comment = new LineComment(this.ast);
			}
			comment.setSourceRange(start, end - start);
		}
		return comment;
	}

	protected Statement createFakeEmptyStatement(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		EmptyStatement emptyStatement = new EmptyStatement(this.ast);
		emptyStatement.setFlags(emptyStatement.getFlags() | ASTNode.MALFORMED);
		int start = statement.sourceStart;
		int end = statement.sourceEnd;
		emptyStatement.setSourceRange(start, end - start + 1);
		return emptyStatement;
	}

	/**
	 * @return a new modifier
	 */
	private Modifier createModifier(ModifierKeyword keyword) {
		final Modifier modifier = new Modifier(this.ast);
		modifier.setKeyword(keyword);
		int start = this.scanner.getCurrentTokenStartPosition();
		int end = this.scanner.getCurrentTokenEndPosition();
		modifier.setSourceRange(start, end - start + 1);
		return modifier;
	}

	protected InfixExpression.Operator getOperatorFor(int operatorID) {
		switch (operatorID) {
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.EQUAL_EQUAL:
			return InfixExpression.Operator.EQUALS;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.LESS_EQUAL:
			return InfixExpression.Operator.LESS_EQUALS;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.GREATER_EQUAL:
			return InfixExpression.Operator.GREATER_EQUALS;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.NOT_EQUAL:
			return InfixExpression.Operator.NOT_EQUALS;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.LEFT_SHIFT:
			return InfixExpression.Operator.LEFT_SHIFT;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.RIGHT_SHIFT:
			return InfixExpression.Operator.RIGHT_SHIFT_SIGNED;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.UNSIGNED_RIGHT_SHIFT:
			return InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR_OR:
			return InfixExpression.Operator.CONDITIONAL_OR;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND_AND:
			return InfixExpression.Operator.CONDITIONAL_AND;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.PLUS:
			return InfixExpression.Operator.PLUS;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MINUS:
			return InfixExpression.Operator.MINUS;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.REMAINDER:
			return InfixExpression.Operator.REMAINDER;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.XOR:
			return InfixExpression.Operator.XOR;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.AND:
			return InfixExpression.Operator.AND;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.MULTIPLY:
			return InfixExpression.Operator.TIMES;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.OR:
			return InfixExpression.Operator.OR;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.DIVIDE:
			return InfixExpression.Operator.DIVIDE;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.GREATER:
			return InfixExpression.Operator.GREATER;
		case org.aspectj.org.eclipse.jdt.internal.compiler.ast.OperatorIds.LESS:
			return InfixExpression.Operator.LESS;
		}
		return null;
	}

	protected PrimitiveType.Code getPrimitiveTypeCode(char[] name) {
		switch (name[0]) {
		case 'i':
			if (name.length == 3 && name[1] == 'n' && name[2] == 't') {
				return PrimitiveType.INT;
			}
			break;
		case 'l':
			if (name.length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g') {
				return PrimitiveType.LONG;
			}
			break;
		case 'd':
			if (name.length == 6 && name[1] == 'o' && name[2] == 'u' && name[3] == 'b' && name[4] == 'l' && name[5] == 'e') {
				return PrimitiveType.DOUBLE;
			}
			break;
		case 'f':
			if (name.length == 5 && name[1] == 'l' && name[2] == 'o' && name[3] == 'a' && name[4] == 't') {
				return PrimitiveType.FLOAT;
			}
			break;
		case 'b':
			if (name.length == 4 && name[1] == 'y' && name[2] == 't' && name[3] == 'e') {
				return PrimitiveType.BYTE;
			} else if (name.length == 7 && name[1] == 'o' && name[2] == 'o' && name[3] == 'l' && name[4] == 'e' && name[5] == 'a'
					&& name[6] == 'n') {
				return PrimitiveType.BOOLEAN;
			}
			break;
		case 'c':
			if (name.length == 4 && name[1] == 'h' && name[2] == 'a' && name[3] == 'r') {
				return PrimitiveType.CHAR;
			}
			break;
		case 's':
			if (name.length == 5 && name[1] == 'h' && name[2] == 'o' && name[3] == 'r' && name[4] == 't') {
				return PrimitiveType.SHORT;
			}
			break;
		case 'v':
			if (name.length == 4 && name[1] == 'o' && name[2] == 'i' && name[3] == 'd') {
				return PrimitiveType.VOID;
			}
		}
		return null; // cannot be reached
	}

	protected boolean isPrimitiveType(char[] name) {
		switch (name[0]) {
		case 'i':
			if (name.length == 3 && name[1] == 'n' && name[2] == 't') {
				return true;
			}
			return false;
		case 'l':
			if (name.length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g') {
				return true;
			}
			return false;
		case 'd':
			if (name.length == 6 && name[1] == 'o' && name[2] == 'u' && name[3] == 'b' && name[4] == 'l' && name[5] == 'e') {
				return true;
			}
			return false;
		case 'f':
			if (name.length == 5 && name[1] == 'l' && name[2] == 'o' && name[3] == 'a' && name[4] == 't') {
				return true;
			}
			return false;
		case 'b':
			if (name.length == 4 && name[1] == 'y' && name[2] == 't' && name[3] == 'e') {
				return true;
			} else if (name.length == 7 && name[1] == 'o' && name[2] == 'o' && name[3] == 'l' && name[4] == 'e' && name[5] == 'a'
					&& name[6] == 'n') {
				return true;
			}
			return false;
		case 'c':
			if (name.length == 4 && name[1] == 'h' && name[2] == 'a' && name[3] == 'r') {
				return true;
			}
			return false;
		case 's':
			if (name.length == 5 && name[1] == 'h' && name[2] == 'o' && name[3] == 'r' && name[4] == 't') {
				return true;
			}
			return false;
		case 'v':
			if (name.length == 4 && name[1] == 'o' && name[2] == 'i' && name[3] == 'd') {
				return true;
			}
			return false;
		}
		return false;
	}

	private void lookupForScopes() {
		if (this.pendingNameScopeResolution != null) {
			for (Object o : this.pendingNameScopeResolution) {
				Name name = (Name) o;
				this.ast.getBindingResolver().recordScope(name, lookupScope(name));
			}
		}
		if (this.pendingThisExpressionScopeResolution != null) {
			for (Object o : this.pendingThisExpressionScopeResolution) {
				ThisExpression thisExpression = (ThisExpression) o;
				this.ast.getBindingResolver().recordScope(thisExpression, lookupScope(thisExpression));
			}
		}

	}

	private BlockScope lookupScope(ASTNode node) {
		ASTNode currentNode = node;
		while (currentNode != null && !(currentNode instanceof MethodDeclaration) && !(currentNode instanceof Initializer)
				&& !(currentNode instanceof FieldDeclaration)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode == null) {
			return null;
		}
		if (currentNode instanceof Initializer) {
			Initializer initializer = (Initializer) currentNode;
			while (!(currentNode instanceof AbstractTypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			if (currentNode instanceof TypeDeclaration || currentNode instanceof EnumDeclaration
					|| currentNode instanceof AnnotationTypeDeclaration) {
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.ast
						.getBindingResolver().getCorrespondingNode(currentNode);
				if ((initializer.getModifiers() & Modifier.STATIC) != 0) {
					return typeDecl.staticInitializerScope;
				} else {
					return typeDecl.initializerScope;
				}
			}
		} else if (currentNode instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) currentNode;
			while (!(currentNode instanceof AbstractTypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			if (currentNode instanceof TypeDeclaration || currentNode instanceof EnumDeclaration
					|| currentNode instanceof AnnotationTypeDeclaration) {
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.ast
						.getBindingResolver().getCorrespondingNode(currentNode);
				if ((fieldDeclaration.getModifiers() & Modifier.STATIC) != 0) {
					return typeDecl.staticInitializerScope;
				} else {
					return typeDecl.initializerScope;
				}
			}
		}
		AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) this.ast.getBindingResolver()
				.getCorrespondingNode(currentNode);
		return abstractMethodDeclaration.scope;
	}

	protected void recordName(Name name, org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode) {
		if (compilerNode != null) {
			recordNodes(name, compilerNode);
			if (compilerNode instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference) {
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference typeRef = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference) compilerNode;
				if (name.isQualifiedName()) {
					SimpleName simpleName = null;
					while (name.isQualifiedName()) {
						simpleName = ((QualifiedName) name).getName();
						recordNodes(simpleName, typeRef);
						name = ((QualifiedName) name).getQualifier();
						recordNodes(name, typeRef);
					}
				}
			}
		}
	}

	protected void recordNodes(ASTNode node, org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode oldASTNode) {
		this.ast.getBindingResolver().store(node, oldASTNode);
	}

	protected void recordNodes(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Javadoc javadoc, TagElement tagElement) {
		for (Object value : tagElement.fragments()) {
			ASTNode node = (ASTNode) value;
			if (node.getNodeType() == ASTNode.MEMBER_REF) {
				MemberRef memberRef = (MemberRef) node;
				Name name = memberRef.getName();
				// get compiler node and record nodes
				int start = name.getStartPosition();
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode = javadoc.getNodeStartingAt(start);
				if (compilerNode != null) {
					recordNodes(name, compilerNode);
					recordNodes(node, compilerNode);
				}
				// Replace qualifier to have all nodes recorded
				if (memberRef.getQualifier() != null) {
					TypeReference typeRef = null;
					if (compilerNode instanceof JavadocFieldReference) {
						org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression expression = ((JavadocFieldReference) compilerNode).receiver;
						if (expression instanceof TypeReference) {
							typeRef = (TypeReference) expression;
						}
					} else if (compilerNode instanceof JavadocMessageSend) {
						org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression expression = ((JavadocMessageSend) compilerNode).receiver;
						if (expression instanceof TypeReference) {
							typeRef = (TypeReference) expression;
						}
					}
					if (typeRef != null) {
						recordName(memberRef.getQualifier(), typeRef);
					}
				}
			} else if (node.getNodeType() == ASTNode.METHOD_REF) {
				MethodRef methodRef = (MethodRef) node;
				Name name = methodRef.getName();
				// get compiler node and record nodes
				int start = name.getStartPosition();
				// get compiler node and record nodes
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode = javadoc.getNodeStartingAt(start);
				// record nodes
				if (compilerNode != null) {
					recordNodes(methodRef, compilerNode);
					// get type ref
					TypeReference typeRef = null;
					if (compilerNode instanceof org.aspectj.org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression) {
						typeRef = ((org.aspectj.org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression) compilerNode).type;
						if (typeRef != null) {
							recordNodes(name, compilerNode);
						}
					} else if (compilerNode instanceof JavadocMessageSend) {
						org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression expression = ((JavadocMessageSend) compilerNode).receiver;
						if (expression instanceof TypeReference) {
							typeRef = (TypeReference) expression;
						}
						// TODO (frederic) remove following line to fix bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=62650
						recordNodes(name, compilerNode);
					}
					// record name and qualifier
					if (typeRef != null && methodRef.getQualifier() != null) {
						recordName(methodRef.getQualifier(), typeRef);
					}
				}
				// Resolve parameters
				for (Object o : methodRef.parameters()) {
					MethodRefParameter param = (MethodRefParameter) o;
					org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression expression = (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression) javadoc
							.getNodeStartingAt(param.getStartPosition());
					if (expression != null) {
						recordNodes(param, expression);
						if (expression instanceof JavadocArgumentExpression) {
							JavadocArgumentExpression argExpr = (JavadocArgumentExpression) expression;
							TypeReference typeRef = argExpr.argument.type;
							if (this.ast.apiLevel >= AST.JLS3) {
								param.setVarargs(argExpr.argument.isVarArgs());
							}
							recordNodes(param.getType(), typeRef);
							if (param.getType().isSimpleType()) {
								recordName(((SimpleType) param.getType()).getName(), typeRef);
							} else if (param.getType().isArrayType()) {
								Type type = ((ArrayType) param.getType()).getElementType();
								recordNodes(type, typeRef);
								if (type.isSimpleType()) {
									recordName(((SimpleType) type).getName(), typeRef);
								}
							}
						}
					}
				}
			} else if (node.getNodeType() == ASTNode.SIMPLE_NAME || node.getNodeType() == ASTNode.QUALIFIED_NAME) {
				org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode compilerNode = javadoc.getNodeStartingAt(node
						.getStartPosition());
				recordName((Name) node, compilerNode);
			} else if (node.getNodeType() == ASTNode.TAG_ELEMENT) {
				// resolve member and method references binding
				recordNodes(javadoc, (TagElement) node);
			}
		}
	}

	protected void recordPendingNameScopeResolution(Name name) {
		if (this.pendingNameScopeResolution == null) {
			this.pendingNameScopeResolution = new HashSet();
		}
		this.pendingNameScopeResolution.add(name);
	}

	protected void recordPendingThisExpressionScopeResolution(ThisExpression thisExpression) {
		if (this.pendingThisExpressionScopeResolution == null) {
			this.pendingThisExpressionScopeResolution = new HashSet();
		}
		this.pendingThisExpressionScopeResolution.add(thisExpression);
	}

	// /**
	// * Remove whitespaces and comments before and after the expression.
	// */
	// private void trimWhiteSpacesAndComments(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression expression) {
	// int start = expression.sourceStart;
	// int end = expression.sourceEnd;
	// int token;
	// int trimLeftPosition = expression.sourceStart;
	// int trimRightPosition = expression.sourceEnd;
	// boolean first = true;
	// Scanner removeBlankScanner = this.ast.scanner;
	// try {
	// removeBlankScanner.setSource(this.compilationUnitSource);
	// removeBlankScanner.resetTo(start, end);
	// while (true) {
	// token = removeBlankScanner.getNextToken();
	// switch (token) {
	// case TerminalTokens.TokenNameCOMMENT_JAVADOC :
	// case TerminalTokens.TokenNameCOMMENT_LINE :
	// case TerminalTokens.TokenNameCOMMENT_BLOCK :
	// if (first) {
	// trimLeftPosition = removeBlankScanner.currentPosition;
	// }
	// break;
	// case TerminalTokens.TokenNameWHITESPACE :
	// if (first) {
	// trimLeftPosition = removeBlankScanner.currentPosition;
	// }
	// break;
	// case TerminalTokens.TokenNameEOF :
	// expression.sourceStart = trimLeftPosition;
	// expression.sourceEnd = trimRightPosition;
	// return;
	// default :
	// /*
	// * if we find something else than a whitespace or a comment,
	// * then we reset the trimRigthPosition to the expression
	// * source end.
	// */
	// trimRightPosition = removeBlankScanner.currentPosition - 1;
	// first = false;
	// }
	// }
	// } catch (InvalidInputException e){
	// // ignore
	// }
	// }

	protected int retrieveEndingSemiColonPosition(int start, int end) {
		int count = 0;
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameSEMICOLON:
					if (count == 0) {
						return this.scanner.currentPosition - 1;
					}
					break;
				case TerminalTokens.TokenNameLBRACE:
					count++;
					break;
				case TerminalTokens.TokenNameRBRACE:
					count--;
					break;
				case TerminalTokens.TokenNameLPAREN:
					count++;
					break;
				case TerminalTokens.TokenNameRPAREN:
					count--;
					break;
				case TerminalTokens.TokenNameLBRACKET:
					count++;
					break;
				case TerminalTokens.TokenNameRBRACKET:
					count--;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the ending position for a type declaration when the dimension is right after the type name.
	 * For example: int[] i; &rarr; return 5, but int i[] &rarr; return -1;
	 * 
	 * @return int the dimension found
	 */
	protected int retrieveEndOfDimensionsPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		int foundPosition = -1;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameLBRACKET:
				case TerminalTokens.TokenNameCOMMENT_BLOCK:
				case TerminalTokens.TokenNameCOMMENT_JAVADOC:
				case TerminalTokens.TokenNameCOMMENT_LINE:
					break;
				case TerminalTokens.TokenNameRBRACKET:// 166
					foundPosition = this.scanner.currentPosition - 1;
					break;
				default:
					return foundPosition;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return foundPosition;
	}

	/**
	 * This method is used to retrieve the start and end position of a name or primitive type token.
	 * 
	 * @return int[] a single dimensional array, with two elements, for the start and end positions of the name respectively
	 */
	protected int[] retrieveEndOfElementTypeNamePosition(int start, int end) {
		this.scanner.resetTo(start, end);
		boolean isAnnotation = false;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameAT:
						isAnnotation = true;
						break;
					case TerminalTokens.TokenNameIdentifier:
						if (isAnnotation) {
							isAnnotation = false;
							break;
						}
						//$FALL-THROUGH$
					case TerminalTokens.TokenNamebyte:
					case TerminalTokens.TokenNamechar:
					case TerminalTokens.TokenNamedouble:
					case TerminalTokens.TokenNamefloat:
					case TerminalTokens.TokenNameint:
					case TerminalTokens.TokenNamelong:
					case TerminalTokens.TokenNameshort:
					case TerminalTokens.TokenNameboolean:
						return new int[]{this.scanner.startPosition, this.scanner.currentPosition - 1};
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		return new int[]{-1, -1};
	}

	/**
	 * This method is used to retrieve the position after the right parenthesis.
	 * 
	 * @return int the position found
	 */
	protected int retrieveEndOfRightParenthesisPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameRPAREN:
					return this.scanner.currentPosition;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the array dimension declared after the name of a local or a field declaration. For example:
	 * int i, j[] = null, k[][] = {{}}; It should return 0 for i, 1 for j and 2 for k.
	 * 
	 * @return int the dimension found
	 */
	protected int retrieveExtraDimension(int start, int end) {
		this.scanner.resetTo(start, end);
		int dimensions = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameLBRACKET:
				case TerminalTokens.TokenNameCOMMENT_BLOCK:
				case TerminalTokens.TokenNameCOMMENT_JAVADOC:
				case TerminalTokens.TokenNameCOMMENT_LINE:
					break;
				case TerminalTokens.TokenNameRBRACKET:// 166
					dimensions++;
					break;
				default:
					return dimensions;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return dimensions;
	}

	protected void retrieveIdentifierAndSetPositions(int start, int end, Name name) {
		this.scanner.resetTo(start, end);
		int token;
		try {
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				if (token == TerminalTokens.TokenNameIdentifier) {
					int startName = this.scanner.startPosition;
					int endName = this.scanner.currentPosition - 1;
					name.setSourceRange(startName, endName - startName + 1);
					return;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
	}

	/**
	 * This method is used to retrieve the start position of the block.
	 * 
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveIdentifierEndPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameIdentifier:// 110
					return this.scanner.getCurrentTokenEndPosition();
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next comma or semi-colon.
	 * 
	 * @return int the position found.
	 */
	protected int retrievePositionBeforeNextCommaOrSemiColon(int start, int end) {
		this.scanner.resetTo(start, end);
		int braceCounter = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameLBRACE:
					braceCounter++;
					break;
				case TerminalTokens.TokenNameRBRACE:
					braceCounter--;
					break;
				case TerminalTokens.TokenNameLPAREN:
					braceCounter++;
					break;
				case TerminalTokens.TokenNameRPAREN:
					braceCounter--;
					break;
				case TerminalTokens.TokenNameLBRACKET:
					braceCounter++;
					break;
				case TerminalTokens.TokenNameRBRACKET:
					braceCounter--;
					break;
				case TerminalTokens.TokenNameCOMMA:
				case TerminalTokens.TokenNameSEMICOLON:
					if (braceCounter == 0) {
						return this.scanner.startPosition - 1;
					}
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	protected int retrieveProperRightBracketPosition(int bracketNumber, int start) {
		this.scanner.resetTo(start, this.compilationUnitSourceLength);
		try {
			int token, count = 0;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameRBRACKET:
					count++;
					if (count == bracketNumber) {
						return this.scanner.currentPosition - 1;
					}
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next right brace or semi-colon.
	 * 
	 * @return int the position found.
	 */
	protected int retrieveRightBraceOrSemiColonPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameRBRACE:
					return this.scanner.currentPosition - 1;
				case TerminalTokens.TokenNameSEMICOLON:
					return this.scanner.currentPosition - 1;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next right brace or semi-colon.
	 * 
	 * @return int the position found.
	 */
	protected int retrieveRightBrace(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameRBRACE:
					return this.scanner.currentPosition - 1;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the position of the right bracket.
	 * 
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveRightBracketPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameRBRACKET:
					return this.scanner.currentPosition - 1;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/*
	 * This method is used to set the right end position for expression statement. The actual AST nodes don't include the trailing
	 * semicolon. This method fixes the length of the corresponding node.
	 */
	protected void retrieveSemiColonPosition(ASTNode node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		int end = start + length;
		int count = 0;
		this.scanner.resetTo(end, this.compilationUnitSourceLength);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameSEMICOLON:
					if (count == 0) {
						node.setSourceRange(start, this.scanner.currentPosition - start);
						return;
					}
					break;
				case TerminalTokens.TokenNameLBRACE:
					count++;
					break;
				case TerminalTokens.TokenNameRBRACE:
					count--;
					break;
				case TerminalTokens.TokenNameLPAREN:
					count++;
					break;
				case TerminalTokens.TokenNameRPAREN:
					count--;
					break;
				case TerminalTokens.TokenNameLBRACKET:
					count++;
					break;
				case TerminalTokens.TokenNameRBRACKET:
					count--;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
	}

	/**
	 * This method is used to retrieve the start position of the block.
	 * 
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveStartBlockPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNameLBRACE:// 110
					return this.scanner.startPosition;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the starting position of the catch keyword.
	 * 
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveStartingCatchPosition(int start, int end) {
		this.scanner.resetTo(start, end);
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch (token) {
				case TerminalTokens.TokenNamecatch:// 225
					return this.scanner.startPosition;
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
		return -1;
	}

	public void setAST(AST ast) {
		this.ast = ast;
		this.docParser = new DocCommentParser(this.ast, this.scanner, this.insideComments);
	}

	protected void setModifiers(AnnotationTypeDeclaration typeDecl,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		this.scanner.resetTo(typeDeclaration.declarationSourceStart, typeDeclaration.sourceStart);
		this.setModifiers(typeDecl, typeDeclaration.annotations);
	}

	protected void setModifiers(AnnotationTypeMemberDeclaration annotationTypeMemberDecl,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration annotationTypeMemberDeclaration) {
		this.scanner.resetTo(annotationTypeMemberDeclaration.declarationSourceStart, annotationTypeMemberDeclaration.sourceStart);
		this.setModifiers(annotationTypeMemberDecl, annotationTypeMemberDeclaration.annotations);
	}

	/**
	 * @param bodyDeclaration
	 */
	protected void setModifiers(BodyDeclaration bodyDeclaration,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations) {
		try {
			int token;
			int indexInAnnotations = 0;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				IExtendedModifier modifier = null;
				switch (token) {
				case TerminalTokens.TokenNameabstract:
					modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
					break;
				case TerminalTokens.TokenNamepublic:
					modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
					break;
				case TerminalTokens.TokenNamestatic:
					modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
					break;
				case TerminalTokens.TokenNameprotected:
					modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
					break;
				case TerminalTokens.TokenNameprivate:
					modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
					break;
				case TerminalTokens.TokenNamefinal:
					modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
					break;
				case TerminalTokens.TokenNamenative:
					modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
					break;
				case TerminalTokens.TokenNamesynchronized:
					modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
					break;
				case TerminalTokens.TokenNametransient:
					modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
					break;
				case TerminalTokens.TokenNamevolatile:
					modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
					break;
				case TerminalTokens.TokenNamestrictfp:
					modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
					break;
				case TerminalTokens.TokenNameAT:
					// we have an annotation
					if (annotations != null && indexInAnnotations < annotations.length) {
						org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
						modifier = super.convert(annotation);
						this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
					}
					break;
				case TerminalTokens.TokenNameCOMMENT_BLOCK:
				case TerminalTokens.TokenNameCOMMENT_LINE:
				case TerminalTokens.TokenNameCOMMENT_JAVADOC:
					break;
				default:
					return;
				}
				if (modifier != null) {
					bodyDeclaration.modifiers().add(modifier);
				}
			}
		} catch (InvalidInputException e) {
			// ignore
		}
	}

	protected void setModifiers(EnumDeclaration enumDeclaration,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration enumDeclaration2) {
		this.scanner.resetTo(enumDeclaration2.declarationSourceStart, enumDeclaration2.sourceStart);
		this.setModifiers(enumDeclaration, enumDeclaration2.annotations);
	}

	protected void setModifiers(EnumConstantDeclaration enumConstantDeclaration,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			enumConstantDeclaration.internalSetModifiers(fieldDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag);
			if (fieldDeclaration.annotations != null) {
				enumConstantDeclaration.setFlags(enumConstantDeclaration.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(fieldDeclaration.declarationSourceStart, fieldDeclaration.sourceStart);
			this.setModifiers(enumConstantDeclaration, fieldDeclaration.annotations);
		}
	}

	/**
	 * @param fieldDeclaration
	 * @param fieldDecl
	 */
	protected void setModifiers(FieldDeclaration fieldDeclaration,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDecl) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			fieldDeclaration.internalSetModifiers(fieldDecl.modifiers & ExtraCompilerModifiers.AccJustFlag);
			if (fieldDecl.annotations != null) {
				fieldDeclaration.setFlags(fieldDeclaration.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(fieldDecl.declarationSourceStart, fieldDecl.sourceStart);
			this.setModifiers(fieldDeclaration, fieldDecl.annotations);
		}
	}

	protected void setModifiers(org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration fieldDeclaration,
			InterTypeFieldDeclaration fieldDecl) {
		// ajh02: method added
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			fieldDeclaration.internalSetModifiers(fieldDecl.declaredModifiers & ExtraCompilerModifiers.AccJustFlag);
			if (fieldDecl.annotations != null) {
				fieldDeclaration.setFlags(fieldDeclaration.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(fieldDecl.declarationSourceStart, fieldDecl.sourceStart);
			this.setModifiers(fieldDeclaration, fieldDecl.annotations);
		}
	}

	/**
	 * @param initializer
	 * @param oldInitializer
	 */
	protected void setModifiers(Initializer initializer,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			initializer.internalSetModifiers(oldInitializer.modifiers & ExtraCompilerModifiers.AccJustFlag);
			if (oldInitializer.annotations != null) {
				initializer.setFlags(initializer.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(oldInitializer.declarationSourceStart, oldInitializer.bodyStart);
			this.setModifiers(initializer, oldInitializer.annotations);
		}
	}

	/**
	 * @param methodDecl
	 * @param methodDeclaration
	 */
	protected void setModifiers(MethodDeclaration methodDecl, AbstractMethodDeclaration methodDeclaration) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			if (methodDeclaration instanceof InterTypeDeclaration) {
				methodDecl.internalSetModifiers(((InterTypeDeclaration) methodDeclaration).declaredModifiers
						& ExtraCompilerModifiers.AccJustFlag);
			} else {
				methodDecl.internalSetModifiers(methodDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag);
			}
			if (methodDeclaration.annotations != null) {
				methodDecl.setFlags(methodDecl.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(methodDeclaration.declarationSourceStart, methodDeclaration.sourceStart);
			this.setModifiers(methodDecl, methodDeclaration.annotations);
		}
	}

	protected void setModifiers(org.aspectj.org.eclipse.jdt.core.dom.PointcutDeclaration pointcutDecl,
			PointcutDeclaration pointcutDeclaration) {
		// ajh02: method added
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			pointcutDecl.internalSetModifiers(pointcutDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag);
			if (pointcutDeclaration.annotations != null) {
				pointcutDecl.setFlags(pointcutDecl.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(pointcutDeclaration.declarationSourceStart, pointcutDeclaration.sourceStart);
			this.setModifiers(pointcutDecl, pointcutDeclaration.annotations);
		}
	}

	/**
	 * @param variableDecl
	 * @param argument
	 */
	protected void setModifiers(SingleVariableDeclaration variableDecl, Argument argument) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			variableDecl.internalSetModifiers(argument.modifiers & ExtraCompilerModifiers.AccJustFlag);
			if (argument.annotations != null) {
				variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(argument.declarationSourceStart, argument.sourceStart);
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = argument.annotations;
			int indexInAnnotations = 0;
			try {
				int token;
				while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
					IExtendedModifier modifier = null;
					switch (token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT:
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = super.convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK:
					case TerminalTokens.TokenNameCOMMENT_LINE:
					case TerminalTokens.TokenNameCOMMENT_JAVADOC:
						break;
					default:
						return;
					}
					if (modifier != null) {
						variableDecl.modifiers().add(modifier);
					}
				}
			} catch (InvalidInputException e) {
				// ignore
			}
		}
	}

	protected void setModifiers(SingleVariableDeclaration variableDecl, LocalDeclaration localDeclaration) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			variableDecl.internalSetModifiers(localDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag);
			if (localDeclaration.annotations != null) {
				variableDecl.setFlags(variableDecl.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
			int indexInAnnotations = 0;
			try {
				int token;
				while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
					IExtendedModifier modifier = null;
					switch (token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT:
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = super.convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK:
					case TerminalTokens.TokenNameCOMMENT_LINE:
					case TerminalTokens.TokenNameCOMMENT_JAVADOC:
						break;
					default:
						return;
					}
					if (modifier != null) {
						variableDecl.modifiers().add(modifier);
					}
				}
			} catch (InvalidInputException e) {
				// ignore
			}
		}
	}

	/**
	 * @param typeDecl
	 * @param typeDeclaration
	 */
	protected void setModifiers(TypeDeclaration typeDecl,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			int modifiers = typeDeclaration.modifiers;
			modifiers &= ~ClassFileConstants.AccInterface; // remove AccInterface flags
			modifiers &= ExtraCompilerModifiers.AccJustFlag;
			typeDecl.internalSetModifiers(modifiers);
			if (typeDeclaration.annotations != null) {
				typeDecl.setFlags(typeDecl.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(typeDeclaration.declarationSourceStart, typeDeclaration.sourceStart);
			this.setModifiers(typeDecl, typeDeclaration.annotations);
		}
	}

	/**
	 * @param variableDeclarationExpression
	 * @param localDeclaration
	 */
	protected void setModifiers(VariableDeclarationExpression variableDeclarationExpression, LocalDeclaration localDeclaration) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			int modifiers = localDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag;
			modifiers &= ~ExtraCompilerModifiers.AccBlankFinal;
			variableDeclarationExpression.internalSetModifiers(modifiers);
			if (localDeclaration.annotations != null) {
				variableDeclarationExpression.setFlags(variableDeclarationExpression.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
			int indexInAnnotations = 0;
			try {
				int token;
				while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
					IExtendedModifier modifier = null;
					switch (token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT:
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = super.convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK:
					case TerminalTokens.TokenNameCOMMENT_LINE:
					case TerminalTokens.TokenNameCOMMENT_JAVADOC:
						break;
					default:
						return;
					}
					if (modifier != null) {
						variableDeclarationExpression.modifiers().add(modifier);
					}
				}
			} catch (InvalidInputException e) {
				// ignore
			}
		}
	}

	/**
	 * @param variableDeclarationStatement
	 * @param localDeclaration
	 */
	protected void setModifiers(VariableDeclarationStatement variableDeclarationStatement, LocalDeclaration localDeclaration) {
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			int modifiers = localDeclaration.modifiers & ExtraCompilerModifiers.AccJustFlag;
			modifiers &= ~ExtraCompilerModifiers.AccBlankFinal;
			variableDeclarationStatement.internalSetModifiers(modifiers);
			if (localDeclaration.annotations != null) {
				variableDeclarationStatement.setFlags(variableDeclarationStatement.getFlags() | ASTNode.MALFORMED);
			}
			break;
		case AST.JLS3:
			this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
			int indexInAnnotations = 0;
			try {
				int token;
				while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
					IExtendedModifier modifier = null;
					switch (token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT:
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = super.convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.compilationUnitSourceLength);
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK:
					case TerminalTokens.TokenNameCOMMENT_LINE:
					case TerminalTokens.TokenNameCOMMENT_JAVADOC:
						break;
					default:
						return;
					}
					if (modifier != null) {
						variableDeclarationStatement.modifiers().add(modifier);
					}
				}
			} catch (InvalidInputException e) {
				// ignore
			}
		}
	}

	protected QualifiedName setQualifiedNameNameAndSourceRanges(char[][] typeName, long[] positions,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode node) {
		int length = typeName.length;
		final SimpleName firstToken = new SimpleName(this.ast);
		firstToken.internalSetIdentifier(new String(typeName[0]));
		firstToken.index = 1;
		int start0 = (int) (positions[0] >>> 32);
		int start = start0;
		int end = (int) (positions[0] & 0xFFFFFFFF);
		firstToken.setSourceRange(start, end - start + 1);
		final SimpleName secondToken = new SimpleName(this.ast);
		secondToken.internalSetIdentifier(new String(typeName[1]));
		secondToken.index = 2;
		start = (int) (positions[1] >>> 32);
		end = (int) (positions[1] & 0xFFFFFFFF);
		secondToken.setSourceRange(start, end - start + 1);
		QualifiedName qualifiedName = new QualifiedName(this.ast);
		qualifiedName.setQualifier(firstToken);
		qualifiedName.setName(secondToken);
		if (this.resolveBindings) {
			recordNodes(qualifiedName, node);
			recordPendingNameScopeResolution(qualifiedName);
			recordNodes(firstToken, node);
			recordNodes(secondToken, node);
			recordPendingNameScopeResolution(firstToken);
			recordPendingNameScopeResolution(secondToken);
		}
		qualifiedName.index = 2;
		qualifiedName.setSourceRange(start0, end - start0 + 1);
		SimpleName newPart = null;
		for (int i = 2; i < length; i++) {
			newPart = new SimpleName(this.ast);
			newPart.internalSetIdentifier(new String(typeName[i]));
			newPart.index = i + 1;
			start = (int) (positions[i] >>> 32);
			end = (int) (positions[i] & 0xFFFFFFFF);
			newPart.setSourceRange(start, end - start + 1);
			QualifiedName qualifiedName2 = new QualifiedName(this.ast);
			qualifiedName2.setQualifier(qualifiedName);
			qualifiedName2.setName(newPart);
			qualifiedName = qualifiedName2;
			qualifiedName.index = newPart.index;
			qualifiedName.setSourceRange(start0, end - start0 + 1);
			if (this.resolveBindings) {
				recordNodes(qualifiedName, node);
				recordNodes(newPart, node);
				recordPendingNameScopeResolution(qualifiedName);
				recordPendingNameScopeResolution(newPart);
			}
		}
		QualifiedName name = qualifiedName;
		if (this.resolveBindings) {
			recordNodes(name, node);
			recordPendingNameScopeResolution(name);
		}
		return name;
	}

	protected QualifiedName setQualifiedNameNameAndSourceRanges(char[][] typeName, long[] positions, int endingIndex,
			org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode node) {
		int length = endingIndex + 1;
		final SimpleName firstToken = new SimpleName(this.ast);
		firstToken.internalSetIdentifier(new String(typeName[0]));
		firstToken.index = 1;
		int start0 = (int) (positions[0] >>> 32);
		int start = start0;
		int end = (int) positions[0];
		firstToken.setSourceRange(start, end - start + 1);
		final SimpleName secondToken = new SimpleName(this.ast);
		secondToken.internalSetIdentifier(new String(typeName[1]));
		secondToken.index = 2;
		start = (int) (positions[1] >>> 32);
		end = (int) positions[1];
		secondToken.setSourceRange(start, end - start + 1);
		QualifiedName qualifiedName = new QualifiedName(this.ast);
		qualifiedName.setQualifier(firstToken);
		qualifiedName.setName(secondToken);
		if (this.resolveBindings) {
			recordNodes(qualifiedName, node);
			recordPendingNameScopeResolution(qualifiedName);
			recordNodes(firstToken, node);
			recordNodes(secondToken, node);
			recordPendingNameScopeResolution(firstToken);
			recordPendingNameScopeResolution(secondToken);
		}
		qualifiedName.index = 2;
		qualifiedName.setSourceRange(start0, end - start0 + 1);
		SimpleName newPart = null;
		for (int i = 2; i < length; i++) {
			newPart = new SimpleName(this.ast);
			newPart.internalSetIdentifier(new String(typeName[i]));
			newPart.index = i + 1;
			start = (int) (positions[i] >>> 32);
			end = (int) positions[i];
			newPart.setSourceRange(start, end - start + 1);
			QualifiedName qualifiedName2 = new QualifiedName(this.ast);
			qualifiedName2.setQualifier(qualifiedName);
			qualifiedName2.setName(newPart);
			qualifiedName = qualifiedName2;
			qualifiedName.index = newPart.index;
			qualifiedName.setSourceRange(start0, end - start0 + 1);
			if (this.resolveBindings) {
				recordNodes(qualifiedName, node);
				recordNodes(newPart, node);
				recordPendingNameScopeResolution(qualifiedName);
				recordPendingNameScopeResolution(newPart);
			}
		}
		if (newPart == null && this.resolveBindings) {
			recordNodes(qualifiedName, node);
			recordPendingNameScopeResolution(qualifiedName);
		}
		return qualifiedName;
	}

	protected void setTypeNameForAnnotation(org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation compilerAnnotation,
			Annotation annotation) {
		TypeReference typeReference = compilerAnnotation.type;
		if (typeReference instanceof QualifiedTypeReference) {
			QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) typeReference;
			char[][] tokens = qualifiedTypeReference.tokens;
			long[] positions = qualifiedTypeReference.sourcePositions;
			// QualifiedName
			annotation.setTypeName(setQualifiedNameNameAndSourceRanges(tokens, positions, typeReference));
		} else {
			SingleTypeReference singleTypeReference = (SingleTypeReference) typeReference;
			final SimpleName name = new SimpleName(this.ast);
			name.internalSetIdentifier(new String(singleTypeReference.token));
			int start = singleTypeReference.sourceStart;
			int end = singleTypeReference.sourceEnd;
			name.setSourceRange(start, end - start + 1);
			annotation.setTypeName(name);
			if (this.resolveBindings) {
				recordNodes(name, typeReference);
			}
		}
	}

	protected void setTypeForField(FieldDeclaration fieldDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0) {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					fieldDeclaration.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					fieldDeclaration.setType(subarrayType);
					updateInnerPositions(subarrayType, remainingDimensions);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				fieldDeclaration.setType(type);
			}
		} else {
			if (type.isArrayType()) {
				// update positions of the component types of the array type
				int dimensions = ((ArrayType) type).getDimensions();
				updateInnerPositions(type, dimensions);
			}
			fieldDeclaration.setType(type);
		}
	}

	protected void setTypeForAroundAdviceDeclaration(AroundAdviceDeclaration adviceDeclaration, Type type) {
		// ajh02: method added
		switch (this.ast.apiLevel) {
		case AST.JLS2_INTERNAL:
			adviceDeclaration.internalSetReturnType(type);
			break;
		case AST.JLS3:
			adviceDeclaration.setReturnType2(type);
			break;
		}
	}

	protected void setTypeForMethodDeclaration(MethodDeclaration methodDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0) {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					switch (this.ast.apiLevel) {
					case AST.JLS2_INTERNAL:
						methodDeclaration.internalSetReturnType(elementType);
						break;
					case AST.JLS3:
						methodDeclaration.setReturnType2(elementType);
						break;
					}
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					switch (this.ast.apiLevel) {
					case AST.JLS2_INTERNAL:
						methodDeclaration.internalSetReturnType(subarrayType);
						break;
					case AST.JLS3:
						methodDeclaration.setReturnType2(subarrayType);
						break;
					}
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				switch (this.ast.apiLevel) {
				case AST.JLS2_INTERNAL:
					methodDeclaration.internalSetReturnType(type);
					break;
				case AST.JLS3:
					methodDeclaration.setReturnType2(type);
					break;
				}
			}
		} else {
			switch (this.ast.apiLevel) {
			case AST.JLS2_INTERNAL:
				methodDeclaration.internalSetReturnType(type);
				break;
			case AST.JLS3:
				methodDeclaration.setReturnType2(type);
				break;
			}
		}
	}

	protected void setTypeForMethodDeclaration(AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration, Type type,
			int extraDimension) {
		annotationTypeMemberDeclaration.setType(type);
	}

	protected void setTypeForSingleVariableDeclaration(SingleVariableDeclaration singleVariableDeclaration, Type type,
			int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0) {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					singleVariableDeclaration.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					singleVariableDeclaration.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				singleVariableDeclaration.setType(type);
			}
		} else {
			singleVariableDeclaration.setType(type);
		}
	}

	protected void setTypeForVariableDeclarationExpression(VariableDeclarationExpression variableDeclarationExpression, Type type,
			int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0) {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					variableDeclarationExpression.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					variableDeclarationExpression.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				variableDeclarationExpression.setType(type);
			}
		} else {
			variableDeclarationExpression.setType(type);
		}
	}

	protected void setTypeForVariableDeclarationStatement(VariableDeclarationStatement variableDeclarationStatement, Type type,
			int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0) {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					// cut the child loose from its parent (without creating garbage)
					elementType.setParent(null, null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					variableDeclarationStatement.setType(elementType);
				} else {
					int start = type.getStartPosition();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start);
					subarrayType.setSourceRange(start, end - start + 1);
					// cut the child loose from its parent (without creating garbage)
					subarrayType.setParent(null, null);
					updateInnerPositions(subarrayType, remainingDimensions);
					variableDeclarationStatement.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				variableDeclarationStatement.setType(type);
			}
		} else {
			variableDeclarationStatement.setType(type);
		}
	}

	protected void updateInnerPositions(Type type, int dimensions) {
		if (dimensions > 1) {
			// need to set positions for intermediate array type see 42839
			int start = type.getStartPosition();
			Type currentComponentType = ((ArrayType) type).getComponentType();
			int searchedDimension = dimensions - 1;
			int rightBracketEndPosition = start;
			while (currentComponentType.isArrayType()) {
				rightBracketEndPosition = retrieveProperRightBracketPosition(searchedDimension, start);
				currentComponentType.setSourceRange(start, rightBracketEndPosition - start + 1);
				currentComponentType = ((ArrayType) currentComponentType).getComponentType();
				searchedDimension--;
			}
		}
	}
}
