/*******************************************************************************
 * Copyright (c) 2002,2003 Palo Alto Research Center, Incorporated (PARC).
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     PARC initial implementation 
 *     IBM Corporation 
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.parser;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AjConstructorDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AjMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareAnnotationDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.IfPseudoToken;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeConstructorDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.IntertypeMemberClassDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDesignator;
import org.aspectj.ajdt.internal.compiler.ast.Proceed;
import org.aspectj.ajdt.internal.compiler.ast.PseudoToken;
import org.aspectj.ajdt.internal.compiler.ast.PseudoTokens;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Argument;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser.IDeclarationFactory;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.DeclareAnnotation;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class DeclarationFactory implements IDeclarationFactory {

	public MethodDeclaration createMethodDeclaration(CompilationResult result) {
		return new AjMethodDeclaration(result);
	}

	public ConstructorDeclaration createConstructorDeclaration(CompilationResult result) {
		return new AjConstructorDeclaration(result);
	}

	public MessageSend createProceed(MessageSend m) {
		return new Proceed(m);
	}

	public TypeDeclaration createAspect(CompilationResult result) {
		return new AspectDeclaration(result);
	}

	public void setPrivileged(TypeDeclaration aspectDecl, boolean isPrivileged) {
		((AspectDeclaration) aspectDecl).isPrivileged = isPrivileged;
	}

	public void setPerClauseFrom(TypeDeclaration aspectDecl, ASTNode pseudoTokens, Parser parser) {
		AspectDeclaration aspect = (AspectDeclaration) aspectDecl;
		PseudoTokens tok = (PseudoTokens) pseudoTokens;
		aspect.perClause = tok.parsePerClause(parser);
		// For the ast support: currently the below line is not finished! The start is set incorrectly
		((AspectDeclaration) aspectDecl).perClause.setLocation(null, 1, parser.getCurrentTokenStart() + 1);
	}

	public void setDominatesPatternFrom(TypeDeclaration aspectDecl, ASTNode pseudoTokens, Parser parser) {
		AspectDeclaration aspect = (AspectDeclaration) aspectDecl;
		PseudoTokens tok = (PseudoTokens) pseudoTokens;
		aspect.dominatesPattern = tok.maybeParseDominatesPattern(parser);
	}

	public ASTNode createPseudoTokensFrom(ASTNode[] tokens, CompilationResult result) {
		PseudoToken[] psts = new PseudoToken[tokens.length];
		for (int i = 0; i < psts.length; i++) {
			psts[i] = (PseudoToken) tokens[i];
		}
		return new PseudoTokens(psts, new EclipseSourceContext(result));
	}

	public MethodDeclaration createPointcutDeclaration(CompilationResult result) {
		return new PointcutDeclaration(result);
	}

	public MethodDeclaration createAroundAdviceDeclaration(CompilationResult result) {
		return new AdviceDeclaration(result, AdviceKind.Around);
	}

	public MethodDeclaration createAfterAdviceDeclaration(CompilationResult result) {
		return new AdviceDeclaration(result, AdviceKind.After);
	}

	public MethodDeclaration createBeforeAdviceDeclaration(CompilationResult result) {
		return new AdviceDeclaration(result, AdviceKind.Before);
	}

	public ASTNode createPointcutDesignator(Parser parser, ASTNode pseudoTokens) {
		return new PointcutDesignator(parser, (PseudoTokens) pseudoTokens);
	}

	public void setPointcutDesignatorOnAdvice(MethodDeclaration adviceDecl, ASTNode des) {
		((AdviceDeclaration) adviceDecl).pointcutDesignator = (PointcutDesignator) des;
	}

	public void setPointcutDesignatorOnPointcut(MethodDeclaration pcutDecl, ASTNode des) {
		((PointcutDeclaration) pcutDecl).pointcutDesignator = (PointcutDesignator) des;
	}

	public void setExtraArgument(MethodDeclaration adviceDeclaration, Argument arg) {
		((AdviceDeclaration) adviceDeclaration).extraArgument = arg;
	}

	public boolean isAfterAdvice(MethodDeclaration adviceDecl) {
		return ((AdviceDeclaration) adviceDecl).kind != AdviceKind.After;
	}

	public void setAfterThrowingAdviceKind(MethodDeclaration adviceDecl) {
		((AdviceDeclaration) adviceDecl).kind = AdviceKind.AfterThrowing;
	}

	public void setAfterReturningAdviceKind(MethodDeclaration adviceDecl) {
		((AdviceDeclaration) adviceDecl).kind = AdviceKind.AfterReturning;
	}

	public MethodDeclaration createDeclareDeclaration(CompilationResult result, ASTNode pseudoTokens, Parser parser) {
		Declare declare = ((PseudoTokens) pseudoTokens).parseDeclare(parser);
		return new DeclareDeclaration(result, declare);
	}

	public MethodDeclaration createDeclareAnnotationDeclaration(CompilationResult result, ASTNode pseudoTokens,
			Annotation annotation, Parser parser, char kind) {
		DeclareAnnotation declare = (DeclareAnnotation) ((PseudoTokens) pseudoTokens).parseAnnotationDeclare(parser);
		if (declare != null) {
			if (kind == '-') {
				declare.setRemover(true);
			}
		}
		DeclareAnnotationDeclaration decl = new DeclareAnnotationDeclaration(result, declare, annotation);
		return decl;
	}

	public MethodDeclaration createInterTypeFieldDeclaration(CompilationResult result, TypeReference onType) {
		return new InterTypeFieldDeclaration(result, onType);
	}

	public MethodDeclaration createInterTypeMethodDeclaration(CompilationResult result) {
		return new InterTypeMethodDeclaration(result, null);
	}

	public MethodDeclaration createInterTypeConstructorDeclaration(CompilationResult result) {
		return new InterTypeConstructorDeclaration(result, null);
	}

	public void setSelector(MethodDeclaration interTypeDecl, char[] selector) {
		((InterTypeDeclaration) interTypeDecl).setSelector(selector);
	}

	public void setDeclaredModifiers(MethodDeclaration interTypeDecl, int modifiers) {
		((InterTypeDeclaration) interTypeDecl).setDeclaredModifiers(modifiers);
	}

	public void setInitialization(MethodDeclaration itdFieldDecl, Expression initialization) {
		((InterTypeFieldDeclaration) itdFieldDecl).setInitialization(initialization);
	}

	public void setOnType(MethodDeclaration interTypeDecl, TypeReference onType) {
		((InterTypeDeclaration) interTypeDecl).setOnType(onType);
	}

	public ASTNode createPseudoToken(Parser parser, String value, boolean isIdentifier) {
		return new PseudoToken(parser, value, isIdentifier);
	}

	public ASTNode createIfPseudoToken(Parser parser, Expression expr) {
		return new IfPseudoToken(parser, expr);
	}

	public void setLiteralKind(ASTNode pseudoToken, String string) {
		((PseudoToken) pseudoToken).literalKind = string;
	}

	public boolean shouldTryToRecover(ASTNode node) {
		return !(node instanceof AspectDeclaration || node instanceof PointcutDeclaration || node instanceof AdviceDeclaration);
	}

	public TypeDeclaration createIntertypeMemberClassDeclaration(CompilationResult compilationResult) {
		return new IntertypeMemberClassDeclaration(compilationResult);
	}

	public void setOnType(TypeDeclaration interTypeDecl, TypeReference onType) {
		((IntertypeMemberClassDeclaration) interTypeDecl).setOnType(onType);
	}
}
