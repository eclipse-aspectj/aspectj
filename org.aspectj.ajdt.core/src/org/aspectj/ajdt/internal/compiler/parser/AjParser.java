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


package org.aspectj.ajdt.internal.compiler.parser;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.DeclareDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.IfPseudoToken;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeConstructorDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeFieldDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.InterTypeMethodDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDesignator;
import org.aspectj.ajdt.internal.compiler.ast.Proceed;
import org.aspectj.ajdt.internal.compiler.ast.PseudoToken;
import org.aspectj.ajdt.internal.compiler.ast.PseudoTokens;
import org.aspectj.ajdt.internal.core.builder.EclipseSourceContext;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.patterns.Declare;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.RecoveredType;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.core.compiler.CharOperation;



public class AjParser extends Parser {
	
	static {
		try{
			initTables(AjParser.class);
		} catch(java.io.IOException ex){
			throw new ExceptionInInitializerError(ex.getMessage());
		}
	}
	
//	public final static void initAjTables(Class parserClass)
//		throws java.io.IOException {
//
//		final String prefix = FILEPREFIX;
//		int i = 0;
//		lhsStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		char[] chars = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		check_tableStatic = new short[chars.length];
//		for (int c = chars.length; c-- > 0;) {
//			check_tableStatic[c] = (short) (chars[c] - 32768);
//		}
//		asbStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		asrStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		symbol_indexStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
//		actionStatic = lhsStatic;
//	}

	public void initializeScanner(){
		this.scanner = new AjScanner(
			false /*comment*/, 
			false /*whitespace*/, 
			this.options.getSeverity(CompilerOptions.NonExternalizedString) != ProblemSeverities.Ignore /*nls*/, 
			this.options.sourceLevel /*sourceLevel*/, 
			this.options.taskTags/*taskTags*/,
			this.options.taskPriorites/*taskPriorities*/);
	}
	
	
	
//*************New display debugging method
	private static final boolean AJ_DEBUG = false;

	void println(Object o) {
		if (AJ_DEBUG) System.out.println(o);
	}

	private void printStack(Object[] s, int p) {
		List list = Arrays.asList(s);
		System.out.println("  " + list.subList(0, p+1));
	}
	
	private void printStack(int[] s, int p) {
		StringBuffer buf = new StringBuffer("[");
		for (int i=0; i<p+1; i++) {
			if (i > 0) buf.append(", ");
			buf.append(Integer.toString(s[i]));
		}
		buf.append("]");
		System.out.println("  " + buf);
	}
			
	private void printStack(long[] s, int p) {
		StringBuffer buf = new StringBuffer("[");
		for (int i=0; i<p+1; i++) {
			if (i > 0) buf.append(", ");
			buf.append(Long.toString(s[i]));
		}
		buf.append("]");
		System.out.println("  " + buf);
	}
			
	private void printStack(char[][] s, int p) {
		StringBuffer buf = new StringBuffer("[");
		for (int i=0; i<p+1; i++) {
			if (i > 0) buf.append(", ");
			buf.append(new String(s[i]));
		}
		buf.append("]");
		System.out.println("  " + buf);
	}
			
	public void display() {
		if (!AJ_DEBUG) return;
		System.out.print("astStack: ");
		printStack(astStack, astPtr);
		System.out.print("astLengthStack: ");
		printStack(astLengthStack, astLengthPtr);
		
		System.out.print("expressionStack: ");
		printStack(expressionStack, expressionPtr);
		System.out.print("expressionLengthStack: ");
		printStack(expressionLengthStack, expressionLengthPtr);

		System.out.print("identifierStack: ");
		printStack(identifierStack, identifierPtr);
		System.out.print("identifierLengthStack: ");
		printStack(identifierLengthStack, identifierLengthPtr);
		System.out.print("identifierPositionStack: ");
		printStack(identifierPositionStack, identifierPtr);

		
		System.out.print("intStack:");
		printStack(intStack, intPtr);
		System.out.println();
	}	



//************** Overriding behavior for standard Java rules
	protected void consumeMethodInvocationName() {
		super.consumeMethodInvocationName();

		MessageSend m = (MessageSend)expressionStack[expressionPtr];
		if (CharOperation.equals(m.selector, "proceed".toCharArray())) {
			expressionStack[expressionPtr] = new Proceed(m);
		}
	}
	
	protected void consumeToken(int type) {
		currentTokenStart = scanner.startPosition;
		super.consumeToken(type);
		switch (type) {
			case TokenNameaspect :  // pseudo keyword
			case TokenNameprivileged :  // pseudo keyword
			case TokenNamepointcut :  // pseudo keyword
			case TokenNamebefore :  // pseudo keyword
			case TokenNameafter :  // pseudo keyword
			case TokenNamearound :  // pseudo keyword
			case TokenNamedeclare :  // pseudo keyword
				pushIdentifier();
				scanner.commentPtr = -1;
				break;
		}
	}


//************New AspectJ rules	
	protected void consumeAspectDeclaration() {
	    // AspectDeclaration ::= AspectHeader AspectBody
	    consumeClassDeclaration();
	    //??? post parsing step here
	}
	
	protected void consumeAspectHeader() {
	    // AspectHeader ::= AspectHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt AspectHeaderRest
		consumeClassHeader();
	}

	protected void consumeAspectHeaderName(boolean isPrivileged) {
		// (isPrivileged == false) -> AspectHeaderName ::= Modifiersopt 'aspect' 'Identifier'
		// (isPrivileged == true) -> AspectHeaderName ::= Modifiersopt 'privileged' Modifiersopt 'aspect' 'Identifier'
		AspectDeclaration aspectDecl =
			new AspectDeclaration(this.compilationUnit.compilationResult);

		println("aspect header name: ");
		this.display();

		//highlight the name of the type
		long pos = identifierPositionStack[identifierPtr];
		aspectDecl.sourceEnd = (int) pos;
		aspectDecl.sourceStart = (int) (pos >>> 32);
		aspectDecl.name = identifierStack[identifierPtr--];
		identifierLengthPtr--;

		// pop the aspect pseudo-token
		eatIdentifier();


		// handle modifiers, only without privileged for now
		if (isPrivileged) {
			pos = eatIdentifier(); // eat the privileged
			int end = (int) pos;
		    int start = (int) (pos >>> 32);
		    aspectDecl.isPrivileged = true;
			//problemReporter().signalError(start, end, "privileged is unimplemented in 1.1alpha1");
		}
		aspectDecl.modifiersSourceStart = intStack[intPtr--];
		aspectDecl.modifiers = intStack[intPtr--];
		if (aspectDecl.modifiersSourceStart >= 0) {
			aspectDecl.declarationSourceStart = aspectDecl.modifiersSourceStart;
		}

		println("modifiers: " + aspectDecl.modifiers);

		aspectDecl.bodyStart = aspectDecl.sourceEnd + 1;
		pushOnAstStack(aspectDecl);

		listLength = 0; // will be updated when reading super-interfaces
		// recovery
		if (currentElement != null) {
			lastCheckPoint = aspectDecl.bodyStart;
			currentElement = currentElement.add(aspectDecl, 0);
			lastIgnoredToken = -1;
		}

		this.display();
	}

	private long eatIdentifier() {
		long pos = identifierPositionStack[identifierPtr];
		identifierPtr--;
		identifierLengthPtr--;
		return pos;
	}

	protected void consumeAspectHeaderRest() {
		//--[dominates TypePattern] [persingleton() | percflow(PCD) | perthis(PCD) | pertarget(PCD)]
		//AspectHeaderRest ::= AspectHeaderRestStart PseudoTokens
		concatNodeLists();
		this.display();
		PseudoTokens pseudoTokens = popPseudoTokens("{");
		println("pseudo: " + pseudoTokens);

		AspectDeclaration aspectDecl = (AspectDeclaration) astStack[astPtr];
		
		aspectDecl.dominatesPattern = pseudoTokens.maybeParseDominatesPattern(this);
		aspectDecl.perClause = pseudoTokens.parsePerClause(this);
		// XXX handle dominates
	}
	
	
	protected void consumePointcutDeclaration() {
		consumePointcutDesignatorOnDeclaration();
	}
	
	protected void consumeEmptyPointcutDeclaration() {
		//??? set pcd to non-null
	}
	
	protected void consumePointcutHeader() {
		//PointcutDeclaration ::= Modifiersopt 'pointcut'  JavaIdentifier '('
		
		PointcutDeclaration ret = new PointcutDeclaration(compilationUnit.compilationResult);
		
		//the name
		long pos = identifierPositionStack[identifierPtr];
		int sourceEnd = (int) pos;
		ret.sourceStart = (int) (pos >>> 32);
		ret.selector = identifierStack[identifierPtr--];
		identifierLengthPtr--;
		
		// pop the 'pointcut' keyword
		eatIdentifier();

		// modifiers
		ret.declarationSourceStart = intStack[intPtr--];
		ret.modifiers = intStack[intPtr--];
		
		pushOnAstStack(ret);
	}
	


	protected void consumeAroundDeclaration() {
		// AroundDeclaration ::= AroundHeader MethodBody
		consumeMethodDeclaration(true);
	}

	protected void consumeAroundHeader() {
		consumePointcutDesignatorOnAdvice();
		consumeMethodHeader();
	}

	protected void consumeAroundHeaderName() {
		// AroundHeaderName ::= Modifiersopt Type  'around' '(' 
		
		AdviceDeclaration adviceDecl =
			new AdviceDeclaration(compilationUnit.compilationResult, AdviceKind.Around);
		
		// skip the name of the advice
		long pos = eatIdentifier();
		adviceDecl.sourceStart = (int) (pos >>> 32);
		
		TypeReference returnType = getTypeReference(intStack[intPtr--]);
		
		//modifiers
		adviceDecl.declarationSourceStart = intStack[intPtr--];
		adviceDecl.modifiers = intStack[intPtr--];

		adviceDecl.returnType = returnType;
		
		//XXX get some locations right
		
		pushOnAstStack(adviceDecl);
	}
	
	protected void consumePointcutDesignatorOnAdvice() {
		PointcutDesignator des = popPointcutDesignator("{");
		AdviceDeclaration adviceDecl = (AdviceDeclaration)astStack[astPtr];
		adviceDecl.pointcutDesignator = des;
		adviceDecl.sourceEnd = 	des.sourceEnd;
		adviceDecl.bodyStart = des.sourceEnd+1;
	}
	
	protected void consumePointcutDesignatorOnDeclaration() {
		PointcutDesignator des = popPointcutDesignator(";");
		PointcutDeclaration adviceDecl = (PointcutDeclaration)astStack[astPtr];
		adviceDecl.pointcutDesignator = des;
		adviceDecl.sourceEnd = 	des.sourceEnd;
		adviceDecl.bodyStart = des.sourceEnd+1;
	}
	
	
	protected void consumeBasicAdviceDeclaration() {
		// BasicAdviceDeclaration ::= BasicAdviceHeader MethodBody
		consumeMethodDeclaration(true);
	}

	protected void consumeBasicAdviceHeader() {
		// BasicAdviceHeader ::= BasicAdviceHeaderName MethodHeaderParameters ExtraParamopt MethodHeaderThrowsClauseopt ':' PseudoTokens
		consumePointcutDesignatorOnAdvice();
		
		consumeMethodHeader();
	}
	
	
	protected void consumeBasicAdviceHeaderName(boolean isAfter) {
		// BasicAdviceHeaderName ::= 'before'|'after '(' 
		
		AdviceDeclaration adviceDecl =
			new AdviceDeclaration(compilationUnit.compilationResult, isAfter ? AdviceKind.After : AdviceKind.Before);
		
		// skip the name of the advice
		long pos = eatIdentifier();
		adviceDecl.sourceStart = (int) (pos >>> 32);
		
		//modifiers
		adviceDecl.declarationSourceStart = intStack[intPtr--];
		adviceDecl.modifiers = intStack[intPtr--];
		
		
		//??? get more locations right
		
		pushOnAstStack(adviceDecl);
	}
	
	protected void consumeExtraParameterWithFormal() {
		Argument arg = (Argument)astStack[astPtr--];
		astLengthPtr--;
		
		((AdviceDeclaration)astStack[astPtr]).extraArgument = arg;
		
		consumeExtraParameterNoFormal();
	}

	
	protected void consumeExtraParameterNoFormal() {
		
		
	    long pos = identifierPositionStack[identifierPtr];
	    int end = (int) pos;
		int start = (int) (pos >>> 32);
	    char[] name = identifierStack[identifierPtr--];
	    identifierLengthPtr--;
	    
	    //System.out.println("extra parameter: " + new String(name));
	    
	    AdviceDeclaration adviceDecl = (AdviceDeclaration)astStack[astPtr];
	    if (adviceDecl.kind != AdviceKind.After) {
	    	//XXX error, extra param makes no sense here
	    }
	    
	    if (CharOperation.equals(name, "throwing".toCharArray())) {
	    	adviceDecl.kind = AdviceKind.AfterThrowing;
	    } else if (CharOperation.equals(name, "returning".toCharArray())) {
	    	adviceDecl.kind = AdviceKind.AfterReturning;
	    } else {
			problemReporter().parseError(
				start, 
				end, 
				currentToken,
				name, 
				String.valueOf(name), 
				new String[] {"throwing", "returning", ":"}); 
	    }
	}

	protected void consumeClassBodyDeclarationInAspect() { }
	

	protected void consumeDeclareDeclaration() {
		concatNodeLists();
		PseudoTokens tokens = popPseudoTokens(";");
		Declare declare = tokens.parseDeclare(this);
		println("parsed declare: " + declare);
		display();
		pushOnAstStack(new DeclareDeclaration(this.compilationUnit.compilationResult, declare));
	}




	protected void consumeDeclareHeader() {
		consumePseudoTokenIdentifier();  // name
		consumePseudoTokenIdentifier();  // declare
		swapAstStack();
		consumePseudoTokens();
		
		consumePseudoToken(":", 0, false);
		consumePseudoTokens();

		println(">>>>>>>>>>>>>>>>>>>>>>>declare header");
		display();
	}


	protected void consumeInterTypeFieldDeclaration() {
		// InterTypeFieldDeclaration ::= Modifiersopt Type OnType '.' JavaIdentifier InterTypeFieldBody ';'
		println("about to consume field");
		this.display();
		//	FieldDeclaration field = new FieldDeclaration();

		Expression initialization = null;
		if (expressionPtr >= 0) {
			expressionLengthPtr--;
			initialization = expressionStack[expressionPtr--];
		}

		long pos = identifierPositionStack[identifierPtr];
		int end = (int) pos;
		int start = (int) (pos >>> 32);
		char[] name = identifierStack[identifierPtr--];
		identifierLengthPtr--;

		//	field.name = name;
		//	field.sourceStart = start;
		//	field.sourceEnd = end;

		TypeReference onType = getTypeReference(0);
		TypeReference returnType = getTypeReference(intStack[intPtr--]);
		this.display();

		int decSourceStart = intStack[intPtr--];
		int modifiers = intStack[intPtr--];

		InterTypeFieldDeclaration dec =
			new InterTypeFieldDeclaration(
				this.compilationUnit.compilationResult,
				onType);
		
		dec.returnType = returnType;
		dec.sourceStart = start;
		dec.sourceEnd = end;
		dec.setSelector(name);
		dec.declarationSourceStart = decSourceStart;
		dec.setDeclaredModifiers(modifiers);
		dec.setInitialization(initialization);

		pushOnAstStack(dec);
		println("consumed field: " + dec);
		this.display();
	}

	protected void consumeInterTypeMethodDeclaration(boolean isNotAbstract) {
		consumeMethodDeclaration(isNotAbstract);
	}

	protected void consumeInterTypeMethodHeader() {
		consumeMethodHeader();		
	}

	protected void consumeInterTypeConstructorDeclaration() {
		consumeMethodDeclaration(true);
	}

	protected void consumeInterTypeConstructorHeader() {
		consumeMethodHeader();		
	}

	protected void consumeInterTypeMethodHeaderName() {
		//InterTypeMethodHeaderName ::= Modifiersopt Type OnType '.' JavaIdentifier '('
		this.display();
		InterTypeMethodDeclaration md =
			new InterTypeMethodDeclaration(
				this.compilationUnit.compilationResult,
				null);

		//identifier
		char[] name = identifierStack[identifierPtr];
		long selectorSource = identifierPositionStack[identifierPtr--];
		identifierLengthPtr--;

		//onType
		md.onType = getTypeReference(0);

		//type
		md.returnType = getTypeReference(intStack[intPtr--]);

		//modifiers
		md.declarationSourceStart = intStack[intPtr--];
		md.setDeclaredModifiers(intStack[intPtr--]);

		//highlight starts at selector start
		md.sourceStart = (int) (selectorSource >>> 32);
		pushOnAstStack(md);
		md.sourceEnd = lParenPos;
		md.bodyStart = lParenPos + 1;
		md.setSelector(name);
		listLength = 0;
		// initialize listLength before reading parameters/throws

		// recovery
		if (currentElement != null) {
			if (currentElement instanceof RecoveredType
				//|| md.modifiers != 0
				|| (scanner.getLineNumber(md.returnType.sourceStart)
					== scanner.getLineNumber(md.sourceStart))) {
				lastCheckPoint = md.bodyStart;
				currentElement = currentElement.add(md, 0);
				lastIgnoredToken = -1;
			} else {
				lastCheckPoint = md.sourceStart;
				restartRecovery = true;
			}
		}
	}
	
	protected void consumeInterTypeConstructorHeaderName() {
		//InterTypeConstructorHeaderName ::= Modifiersopt Name '.' 'new' '('
		this.display();
		InterTypeConstructorDeclaration md =
			new InterTypeConstructorDeclaration(
				this.compilationUnit.compilationResult,
				null);

		//identifier
//		md.selector = identifierStack[identifierPtr];
//		long selectorSource = identifierPositionStack[identifierPtr--];
////		identifierLengthPtr--;

		//onType
		md.onType = getTypeReference(0);

		println("got onType: " + md.onType);
		this.display();

		intPtr--; // pop new info
		//type
		md.returnType = TypeReference.baseTypeReference(T_void, 0); //getTypeReference(intStack[intPtr--]);

		//modifiers
		md.declarationSourceStart = intStack[intPtr--];
		md.setDeclaredModifiers(intStack[intPtr--]);
		//md.modifiers = intStack[intPtr--];

		//highlight starts at selector start
		//md.sourceStart = (int) (selectorSource >>> 32);
		md.sourceStart = md.onType.sourceStart;
		pushOnAstStack(md);
		md.sourceEnd = lParenPos;
		md.bodyStart = lParenPos + 1;
		listLength = 0;
		// initialize listLength before reading parameters/throws

		md.setSelector(
			(new String(CharOperation.concatWith(md.onType.getTypeName(), '_')) + "_new").
				toCharArray());
		

		// recovery
		if (currentElement != null) {
			if (currentElement instanceof RecoveredType
				//|| md.modifiers != 0
				//|| (scanner.getLineNumber(md.returnType.sourceStart)
				//	== scanner.getLineNumber(md.sourceStart))
				) {
				//lastCheckPoint = md.bodyStart;
				currentElement = currentElement.add(md, 0);
				lastIgnoredToken = -1;
			} else {
				lastCheckPoint = md.sourceStart;
				restartRecovery = true;
			}
		}
	}




//*********************************************************


	protected void consumePseudoToken(String value) {
		consumePseudoToken(value, 0, false);
	}

	protected void consumePseudoToken(
		String value,
		int popFromIntStack,
		boolean isIdentifier) {
		intPtr -= popFromIntStack;

		int start = currentTokenStart;
		int end = start + value.length() - 1;
		PseudoToken tok = new PseudoToken(this, value, isIdentifier);
		tok.sourceStart = start;
		tok.sourceEnd = end;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenIdentifier() {
		long pos = identifierPositionStack[identifierPtr];
		int end = (int) pos;
		int start = (int) (pos >>> 32);
		char[] name = identifierStack[identifierPtr--];
		identifierLengthPtr--;

		PseudoToken tok = new PseudoToken(this, new String(name), true);
		tok.sourceStart = start;
		tok.sourceEnd = end;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenIf() {
		//this.display();
		Expression expr = (Expression) expressionStack[expressionPtr--];
		expressionLengthPtr--;
		println("expr: " + expr);

		int start = intStack[intPtr--];
		PseudoToken tok = new IfPseudoToken(this, expr);
		tok.sourceStart = start;
		tok.sourceEnd = this.rParenPos;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenLiteral() {
		Literal literal = (Literal) expressionStack[expressionPtr--];
		expressionLengthPtr--;
		//System.out.println("literal: " + new String(literal.source()));

		PseudoToken tok =
			new PseudoToken(this, new String(literal.source()), false);
		tok.literalKind = "string";
		tok.sourceStart = literal.sourceStart;
		tok.sourceEnd = literal.sourceEnd;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokenModifier() {
		//int modifier = modifiers;
		consumePseudoToken(Modifier.toString(modifiers), 0, true);
		modifiers = AccDefault;
	}

	protected void consumePseudoTokenPrimitiveType() {
		TypeReference type = getTypeReference(0);

		PseudoToken tok = new PseudoToken(this, type.toString(), true);
		tok.sourceStart = type.sourceStart;
		tok.sourceEnd = type.sourceEnd;
		pushOnAstStack(tok);
	}

	protected void consumePseudoTokens() {
		optimizedConcatNodeLists();
	}

// Helpers

	protected PointcutDesignator popPointcutDesignator(String terminator) {
		PseudoTokens tokens = popPseudoTokens(terminator);
		return new PointcutDesignator(this, tokens);
	}

	protected PseudoTokens popPseudoTokens(String terminator) {
		consumePseudoToken(terminator);
		consumePseudoTokens();
		//System.out.println("next token is: " + new String(scanner.getCurrentTokenSource()));

		int length = astLengthStack[astLengthPtr--];
		astPtr -= length;

		//arguments
		PseudoToken[] tokens = new PseudoToken[length];
		System.arraycopy(astStack, astPtr + 1, tokens, 0, length);
		//md.bodyStart = rParenPos+1;
		listLength = 0; // reset listLength after having read all parameters

		return new PseudoTokens(tokens, makeSourceContext(this.compilationUnit.compilationResult()));
	}

	private ISourceContext makeSourceContext(CompilationResult compilationResult) {
		return new EclipseSourceContext(compilationResult);
	}


	private void swapAstStack() {
		ASTNode top = astStack[astPtr];
		ASTNode next = astStack[astPtr-1];
		astStack[astPtr] = next;
		astStack[astPtr-1] = top;
	}



	// This method is part of an automatic generation : do NOT edit-modify  
   protected void consumeRule(int act) {
	 switch ( act ) {
	   case 30 : // System.out.println("Type ::= PrimitiveType");  //$NON-NLS-1$
			   consumePrimitiveType();  
			   break ;
 
	   case 44 : // System.out.println("ReferenceType ::= ClassOrInterfaceType");  //$NON-NLS-1$
			   consumeReferenceType();   
			   break ;
 
	   case 62 : // System.out.println("AjQualifiedName ::= AjName DOT SimpleName");  //$NON-NLS-1$
			   consumeQualifiedName();  
			   break ;
 
	   case 66 : // System.out.println("QualifiedName ::= Name DOT JavaIdentifier");  //$NON-NLS-1$
			   consumeQualifiedName();  
			   break ;
 
	   case 67 : // System.out.println("CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt...");  //$NON-NLS-1$
			   consumeCompilationUnit();  
			   break ;
 
	   case 68 : // System.out.println("EnterCompilationUnit ::=");  //$NON-NLS-1$
			   consumeEnterCompilationUnit();  
			   break ;
 
	   case 81 : // System.out.println("CatchHeader ::= catch LPAREN FormalParameter RPAREN LBRACE");  //$NON-NLS-1$
			   consumeCatchHeader();  
			   break ;
 
	   case 83 : // System.out.println("ImportDeclarations ::= ImportDeclarations ImportDeclaration");  //$NON-NLS-1$
			   consumeImportDeclarations();  
			   break ;
 
	   case 85 : // System.out.println("TypeDeclarations ::= TypeDeclarations TypeDeclaration");  //$NON-NLS-1$
			   consumeTypeDeclarations();  
			   break ;
 
	   case 86 : // System.out.println("PackageDeclaration ::= PackageDeclarationName SEMICOLON");  //$NON-NLS-1$
				consumePackageDeclaration();  
			   break ;
 
	   case 87 : // System.out.println("PackageDeclarationName ::= package Name");  //$NON-NLS-1$
				consumePackageDeclarationName();  
			   break ;
 
	   case 90 : // System.out.println("SingleTypeImportDeclaration ::= SingleTypeImportDeclarationName...");  //$NON-NLS-1$
			   consumeSingleTypeImportDeclaration();  
			   break ;
 
	   case 91 : // System.out.println("SingleTypeImportDeclarationName ::= import Name");  //$NON-NLS-1$
			   consumeSingleTypeImportDeclarationName();  
			   break ;
 
	   case 92 : // System.out.println("TypeImportOnDemandDeclaration ::= TypeImportOnDemandDeclarationName");  //$NON-NLS-1$
			   consumeTypeImportOnDemandDeclaration();  
			   break ;
 
	   case 93 : // System.out.println("TypeImportOnDemandDeclarationName ::= import Name DOT MULTIPLY");  //$NON-NLS-1$
			   consumeTypeImportOnDemandDeclarationName();  
			   break ;
 
		case 96 : // System.out.println("TypeDeclaration ::= SEMICOLON");  //$NON-NLS-1$
			   consumeEmptyTypeDeclaration();  
			   break ;
 
	   case 122 : // System.out.println("AspectDeclaration ::= AspectHeader AspectBody");  //$NON-NLS-1$
			   consumeAspectDeclaration();  
			   break ;
 
	   case 123 : // System.out.println("AspectHeader ::= AspectHeaderName ClassHeaderExtendsopt...");  //$NON-NLS-1$
			   consumeAspectHeader();  
			   break ;
 
	   case 124 : // System.out.println("AspectHeaderName ::= Modifiersopt aspect Identifier");  //$NON-NLS-1$
			   consumeAspectHeaderName(false);  
			   break ;
 
	   case 125 : // System.out.println("AspectHeaderName ::= Modifiersopt privileged Modifiersopt aspect...");  //$NON-NLS-1$
			   consumeAspectHeaderName(true);  
			   break ;
 
	   case 127 : // System.out.println("AspectHeaderRest ::= AspectHeaderRestStart PseudoTokens");  //$NON-NLS-1$
			   consumeAspectHeaderRest();  
			   break ;
 
	   case 128 : // System.out.println("AspectHeaderRestStart ::= Identifier");  //$NON-NLS-1$
			   consumePseudoTokenIdentifier();  
			   break ;
 
	   case 131 : // System.out.println("AspectBodyDeclarations ::= AspectBodyDeclarations...");  //$NON-NLS-1$
			   consumeClassBodyDeclarations();  
			   break ;
 
	   case 132 : // System.out.println("AspectBodyDeclarationsopt ::=");  //$NON-NLS-1$
			   consumeEmptyClassBodyDeclarationsopt();  
			   break ;
 
	   case 133 : // System.out.println("AspectBodyDeclarationsopt ::= NestedType AspectBodyDeclarations");  //$NON-NLS-1$
			   consumeClassBodyDeclarationsopt();  
			   break ;
 
	   case 134 : // System.out.println("AspectBodyDeclaration ::= ClassBodyDeclaration");  //$NON-NLS-1$
			   consumeClassBodyDeclarationInAspect();  
			   break ;
 
	   case 135 : // System.out.println("PointcutDeclaration ::= PointcutHeader MethodHeaderParameters...");  //$NON-NLS-1$
			   consumeEmptyPointcutDeclaration();  
			   break ;
 
	   case 136 : // System.out.println("PointcutDeclaration ::= PointcutHeader MethodHeaderParameters COLON");  //$NON-NLS-1$
			   consumePointcutDeclaration();  
			   break ;
 
	   case 137 : // System.out.println("PointcutHeader ::= Modifiersopt pointcut JavaIdentifier LPAREN");  //$NON-NLS-1$
			   consumePointcutHeader();  
			   break ;
 
	   case 140 : // System.out.println("AroundDeclaration ::= AroundHeader MethodBody");  //$NON-NLS-1$
			   consumeAroundDeclaration();  
			   break ;
 
	   case 141 : // System.out.println("AroundHeader ::= AroundHeaderName MethodHeaderParameters...");  //$NON-NLS-1$
			   consumeAroundHeader();  
			   break ;
 
	   case 142 : // System.out.println("AroundHeaderName ::= Modifiersopt Type around LPAREN");  //$NON-NLS-1$
			   consumeAroundHeaderName();  
			   break ;
 
	   case 143 : // System.out.println("BasicAdviceDeclaration ::= BasicAdviceHeader MethodBody");  //$NON-NLS-1$
			   consumeBasicAdviceDeclaration();  
			   break ;
 
	   case 144 : // System.out.println("BasicAdviceHeader ::= BasicAdviceHeaderName MethodHeaderParameters");  //$NON-NLS-1$
			   consumeBasicAdviceHeader();  
			   break ;
 
	   case 145 : // System.out.println("BasicAdviceHeaderName ::= Modifiersopt before LPAREN");  //$NON-NLS-1$
			   consumeBasicAdviceHeaderName(false);  
			   break ;
 
	   case 146 : // System.out.println("BasicAdviceHeaderName ::= Modifiersopt after LPAREN");  //$NON-NLS-1$
			   consumeBasicAdviceHeaderName(true);  
			   break ;
 
	   case 147 : // System.out.println("ExtraParamopt ::= Identifier LPAREN FormalParameter RPAREN");  //$NON-NLS-1$
			   consumeExtraParameterWithFormal();  
			   break ;
 
	   case 148 : // System.out.println("ExtraParamopt ::= Identifier LPAREN RPAREN");  //$NON-NLS-1$
			   consumeExtraParameterNoFormal();  
			   break ;
 
	   case 149 : // System.out.println("ExtraParamopt ::= Identifier");  //$NON-NLS-1$
			   consumeExtraParameterNoFormal();  
			   break ;
 
	   case 152 : // System.out.println("OnType ::= OnType DOT JavaIdentifier");  //$NON-NLS-1$
			   consumeQualifiedName();  
			   break ;
 
	   case 157 : // System.out.println("InterTypeMethodDeclaration ::= InterTypeMethodHeader MethodBody");  //$NON-NLS-1$
			   // set to true to consume a method with a body
	 consumeInterTypeMethodDeclaration(true);   
			   break ;
 
	   case 158 : // System.out.println("InterTypeMethodHeader ::= InterTypeMethodHeaderName...");  //$NON-NLS-1$
			   consumeInterTypeMethodHeader();  
			   break ;
 
	   case 159 : // System.out.println("InterTypeMethodHeaderName ::= Modifiersopt Type OnType DOT...");  //$NON-NLS-1$
			   consumeInterTypeMethodHeaderName();  
			   break ;
 
	   case 160 : // System.out.println("AbstractInterTypeMethodDeclaration ::= InterTypeMethodHeader...");  //$NON-NLS-1$
			   // set to false to consume a method without body
	 consumeInterTypeMethodDeclaration(false);  
			   break ;
 
	   case 161 : // System.out.println("InterTypeConstructorDeclaration ::= InterTypeConstructorHeader...");  //$NON-NLS-1$
			   // set to true to consume a method with a body
	 consumeInterTypeConstructorDeclaration();   
			   break ;
 
	   case 162 : // System.out.println("InterTypeConstructorHeader ::= InterTypeConstructorHeaderName...");  //$NON-NLS-1$
			   consumeInterTypeConstructorHeader();  
			   break ;
 
	   case 163 : // System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt Name DOT new LPAREN");  //$NON-NLS-1$
			   consumeInterTypeConstructorHeaderName();  
			   break ;
 
	   case 164 : // System.out.println("InterTypeFieldDeclaration ::= Modifiersopt Type OnType DOT...");  //$NON-NLS-1$
			   consumeInterTypeFieldDeclaration();  
			   break ;
 
	   case 168 : // System.out.println("DeclareDeclaration ::= DeclareHeader PseudoTokens SEMICOLON");  //$NON-NLS-1$
			   consumeDeclareDeclaration();  
			   break ;
 
	   case 169 : // System.out.println("DeclareHeader ::= declare Identifier COLON");  //$NON-NLS-1$
			   consumeDeclareHeader();  
			   break ;
 
	   case 171 : // System.out.println("PseudoTokens ::= PseudoTokens PseudoToken");  //$NON-NLS-1$
			   consumePseudoTokens();  
			   break ;
 
	   case 172 : // System.out.println("PseudoToken ::= JavaIdentifier");  //$NON-NLS-1$
			   consumePseudoTokenIdentifier();  
			   break ;
 
	   case 173 : // System.out.println("PseudoToken ::= LPAREN");  //$NON-NLS-1$
			   consumePseudoToken("(");  
			   break ;
 
	   case 174 : // System.out.println("PseudoToken ::= RPAREN");  //$NON-NLS-1$
			   consumePseudoToken(")");  
			   break ;
 
	   case 175 : // System.out.println("PseudoToken ::= DOT");  //$NON-NLS-1$
			   consumePseudoToken(".");  
			   break ;
 
	   case 176 : // System.out.println("PseudoToken ::= MULTIPLY");  //$NON-NLS-1$
			   consumePseudoToken("*");  
			   break ;
 
	   case 177 : // System.out.println("PseudoToken ::= PLUS");  //$NON-NLS-1$
			   consumePseudoToken("+");  
			   break ;
 
	   case 178 : // System.out.println("PseudoToken ::= AND_AND");  //$NON-NLS-1$
			   consumePseudoToken("&&");  
			   break ;
 
	   case 179 : // System.out.println("PseudoToken ::= OR_OR");  //$NON-NLS-1$
			   consumePseudoToken("||");  
			   break ;
 
	   case 180 : // System.out.println("PseudoToken ::= NOT");  //$NON-NLS-1$
			   consumePseudoToken("!");  
			   break ;
 
	   case 181 : // System.out.println("PseudoToken ::= COLON");  //$NON-NLS-1$
			   consumePseudoToken(":");  
			   break ;
 
	   case 182 : // System.out.println("PseudoToken ::= COMMA");  //$NON-NLS-1$
			   consumePseudoToken(",");  
			   break ;
 
	   case 183 : // System.out.println("PseudoToken ::= LBRACKET");  //$NON-NLS-1$
			   consumePseudoToken("[");  
			   break ;
 
	   case 184 : // System.out.println("PseudoToken ::= RBRACKET");  //$NON-NLS-1$
			   consumePseudoToken("]");  
			   break ;
 
	   case 185 : // System.out.println("PseudoToken ::= PrimitiveType");  //$NON-NLS-1$
			   consumePseudoTokenPrimitiveType();  
			   break ;
 
	   case 186 : // System.out.println("PseudoToken ::= Modifier");  //$NON-NLS-1$
			   consumePseudoTokenModifier();  
			   break ;
 
	   case 187 : // System.out.println("PseudoToken ::= Literal");  //$NON-NLS-1$
			   consumePseudoTokenLiteral();  
			   break ;
 
	   case 188 : // System.out.println("PseudoToken ::= this");  //$NON-NLS-1$
			   consumePseudoToken("this", 1, true);  
			   break ;
 
	   case 189 : // System.out.println("PseudoToken ::= super");  //$NON-NLS-1$
			   consumePseudoToken("super", 1, true);  
			   break ;
 
	   case 190 : // System.out.println("PseudoToken ::= if LPAREN Expression RPAREN");  //$NON-NLS-1$
			   consumePseudoTokenIf();  
			   break ;
 
	   case 191 : // System.out.println("PseudoToken ::= assert");  //$NON-NLS-1$
			   consumePseudoToken("assert", 1, true);  
			   break ;
 
	   case 192 : // System.out.println("PseudoToken ::= import");  //$NON-NLS-1$
			   consumePseudoToken("import", 1, true);  
			   break ;
 
	   case 193 : // System.out.println("PseudoToken ::= package");  //$NON-NLS-1$
			   consumePseudoToken("package", 1, true);  
			   break ;
 
	   case 194 : // System.out.println("PseudoToken ::= throw");  //$NON-NLS-1$
			   consumePseudoToken("throw", 1, true);  
			   break ;
 
	   case 195 : // System.out.println("PseudoToken ::= new");  //$NON-NLS-1$
			   consumePseudoToken("new", 1, true);  
			   break ;
 
	   case 196 : // System.out.println("PseudoToken ::= do");  //$NON-NLS-1$
			   consumePseudoToken("do", 1, true);  
			   break ;
 
	   case 197 : // System.out.println("PseudoToken ::= for");  //$NON-NLS-1$
			   consumePseudoToken("for", 1, true);  
			   break ;
 
	   case 198 : // System.out.println("PseudoToken ::= switch");  //$NON-NLS-1$
			   consumePseudoToken("switch", 1, true);  
			   break ;
 
	   case 199 : // System.out.println("PseudoToken ::= try");  //$NON-NLS-1$
			   consumePseudoToken("try", 1, true);  
			   break ;
 
	   case 200 : // System.out.println("PseudoToken ::= while");  //$NON-NLS-1$
			   consumePseudoToken("while", 1, true);  
			   break ;
 
	   case 201 : // System.out.println("PseudoToken ::= break");  //$NON-NLS-1$
			   consumePseudoToken("break", 1, true);  
			   break ;
 
	   case 202 : // System.out.println("PseudoToken ::= continue");  //$NON-NLS-1$
			   consumePseudoToken("continue", 1, true);  
			   break ;
 
	   case 203 : // System.out.println("PseudoToken ::= return");  //$NON-NLS-1$
			   consumePseudoToken("return", 1, true);  
			   break ;
 
	   case 204 : // System.out.println("PseudoToken ::= case");  //$NON-NLS-1$
			   consumePseudoToken("case", 1, true);  
			   break ;
 
	   case 205 : // System.out.println("PseudoToken ::= catch");  //$NON-NLS-1$
			   consumePseudoToken("catch", 0, true);  
			   break ;
 
	   case 206 : // System.out.println("PseudoToken ::= instanceof");  //$NON-NLS-1$
			   consumePseudoToken("instanceof", 0, true);  
			   break ;
 
	   case 207 : // System.out.println("PseudoToken ::= else");  //$NON-NLS-1$
			   consumePseudoToken("else", 0, true);  
			   break ;
 
	   case 208 : // System.out.println("PseudoToken ::= extends");  //$NON-NLS-1$
			   consumePseudoToken("extends", 0, true);  
			   break ;
 
	   case 209 : // System.out.println("PseudoToken ::= finally");  //$NON-NLS-1$
			   consumePseudoToken("finally", 0, true);  
			   break ;
 
	   case 210 : // System.out.println("PseudoToken ::= implements");  //$NON-NLS-1$
			   consumePseudoToken("implements", 0, true);  
			   break ;
 
	   case 211 : // System.out.println("PseudoToken ::= throws");  //$NON-NLS-1$
			   consumePseudoToken("throws", 0, true);  
			   break ;
 
	   case 212 : // System.out.println("ClassDeclaration ::= ClassHeader ClassBody");  //$NON-NLS-1$
			   consumeClassDeclaration();  
			   break ;
 
	   case 213 : // System.out.println("ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt...");  //$NON-NLS-1$
			   consumeClassHeader();  
			   break ;
 
	   case 214 : // System.out.println("ClassHeaderName ::= Modifiersopt class JavaIdentifier");  //$NON-NLS-1$
			   consumeClassHeaderName();  
			   break ;
 
	   case 215 : // System.out.println("ClassHeaderExtends ::= extends ClassType");  //$NON-NLS-1$
			   consumeClassHeaderExtends();  
			   break ;
 
	   case 216 : // System.out.println("ClassHeaderImplements ::= implements InterfaceTypeList");  //$NON-NLS-1$
			   consumeClassHeaderImplements();  
			   break ;
 
	   case 218 : // System.out.println("InterfaceTypeList ::= InterfaceTypeList COMMA InterfaceType");  //$NON-NLS-1$
			   consumeInterfaceTypeList();  
			   break ;
 
	   case 219 : // System.out.println("InterfaceType ::= ClassOrInterfaceType");  //$NON-NLS-1$
			   consumeInterfaceType();  
			   break ;
 
	   case 222 : // System.out.println("ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration");  //$NON-NLS-1$
			   consumeClassBodyDeclarations();  
			   break ;
 
	   case 226 : // System.out.println("ClassBodyDeclaration ::= Diet NestedMethod Block");  //$NON-NLS-1$
			   consumeClassBodyDeclaration();  
			   break ;
 
	   case 227 : // System.out.println("Diet ::=");  //$NON-NLS-1$
			   consumeDiet();  
			   break ;

	   case 228 : // System.out.println("Initializer ::= Diet NestedMethod Block");  //$NON-NLS-1$
			   consumeClassBodyDeclaration();  
			   break ;
 
	   case 235 : // System.out.println("ClassMemberDeclaration ::= SEMICOLON");  //$NON-NLS-1$
			   consumeEmptyClassMemberDeclaration();  
			   break ;

	   case 236 : // System.out.println("FieldDeclaration ::= Modifiersopt Type VariableDeclarators SEMICOLON");  //$NON-NLS-1$
			   consumeFieldDeclaration();  
			   break ;
 
	   case 238 : // System.out.println("VariableDeclarators ::= VariableDeclarators COMMA VariableDeclarator");  //$NON-NLS-1$
			   consumeVariableDeclarators();  
			   break ;
 
	   case 241 : // System.out.println("EnterVariable ::=");  //$NON-NLS-1$
			   consumeEnterVariable();  
			   break ;
 
	   case 242 : // System.out.println("ExitVariableWithInitialization ::=");  //$NON-NLS-1$
			   consumeExitVariableWithInitialization();  
			   break ;
 
	   case 243 : // System.out.println("ExitVariableWithoutInitialization ::=");  //$NON-NLS-1$
			   consumeExitVariableWithoutInitialization();  
			   break ;
 
	   case 244 : // System.out.println("ForceNoDiet ::=");  //$NON-NLS-1$
			   consumeForceNoDiet();  
			   break ;
 
	   case 245 : // System.out.println("RestoreDiet ::=");  //$NON-NLS-1$
			   consumeRestoreDiet();  
			   break ;
 
	   case 250 : // System.out.println("MethodDeclaration ::= MethodHeader MethodBody");  //$NON-NLS-1$
			   // set to true to consume a method with a body
	 consumeMethodDeclaration(true);   
			   break ;
 
	   case 251 : // System.out.println("AbstractMethodDeclaration ::= MethodHeader SEMICOLON");  //$NON-NLS-1$
			   // set to false to consume a method without body
	 consumeMethodDeclaration(false);  
			   break ;
 
	   case 252 : // System.out.println("MethodHeader ::= MethodHeaderName MethodHeaderParameters...");  //$NON-NLS-1$
			   consumeMethodHeader();  
			   break ;
 
	   case 253 : // System.out.println("MethodHeaderName ::= Modifiersopt Type JavaIdentifierNoAround LPAREN");  //$NON-NLS-1$
			   consumeMethodHeaderName();  
			   break ;
 
	   case 254 : // System.out.println("MethodHeaderParameters ::= FormalParameterListopt RPAREN");  //$NON-NLS-1$
			   consumeMethodHeaderParameters();  
			   break ;
 
	   case 255 : // System.out.println("MethodHeaderExtendedDims ::= Dimsopt");  //$NON-NLS-1$
			   consumeMethodHeaderExtendedDims();  
			   break ;
 
	   case 256 : // System.out.println("MethodHeaderThrowsClause ::= throws ClassTypeList");  //$NON-NLS-1$
			   consumeMethodHeaderThrowsClause();  
			   break ;
 
	   case 257 : // System.out.println("ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters");  //$NON-NLS-1$
			   consumeConstructorHeader();  
			   break ;
 
	   case 258 : // System.out.println("ConstructorHeaderName ::= Modifiersopt Identifier LPAREN");  //$NON-NLS-1$
			   consumeConstructorHeaderName();  
			   break ;
 
	   case 259 : // System.out.println("ConstructorHeaderName ::= Modifiersopt aspect LPAREN");  //$NON-NLS-1$
			   consumeConstructorHeaderName();  
			   break ;
 
	   case 261 : // System.out.println("FormalParameterList ::= FormalParameterList COMMA FormalParameter");  //$NON-NLS-1$
			   consumeFormalParameterList();  
			   break ;
 
	   case 262 : // System.out.println("FormalParameter ::= Modifiersopt Type VariableDeclaratorId");  //$NON-NLS-1$
			   // the boolean is used to know if the modifiers should be reset
	   consumeFormalParameter();  
			   break ;
 
	   case 264 : // System.out.println("ClassTypeList ::= ClassTypeList COMMA ClassTypeElt");  //$NON-NLS-1$
			   consumeClassTypeList();  
			   break ;
 
	   case 265 : // System.out.println("ClassTypeElt ::= ClassType");  //$NON-NLS-1$
			   consumeClassTypeElt();  
			   break ;
 
	   case 266 : // System.out.println("MethodBody ::= NestedMethod LBRACE BlockStatementsopt RBRACE");  //$NON-NLS-1$
			   consumeMethodBody();  
			   break ;
 
	   case 267 : // System.out.println("NestedMethod ::=");  //$NON-NLS-1$
			   consumeNestedMethod();  
			   break ;
 
	   case 268 : // System.out.println("StaticInitializer ::= StaticOnly Block");  //$NON-NLS-1$
			   consumeStaticInitializer();  
			   break ;

	   case 269 : // System.out.println("StaticOnly ::= static");  //$NON-NLS-1$
			   consumeStaticOnly();  
			   break ;
 
	   case 270 : // System.out.println("ConstructorDeclaration ::= ConstructorHeader MethodBody");  //$NON-NLS-1$
			   consumeConstructorDeclaration() ;  
			   break ;
 
	   case 271 : // System.out.println("ConstructorDeclaration ::= ConstructorHeader SEMICOLON");  //$NON-NLS-1$
			   consumeInvalidConstructorDeclaration() ;  
			   break ;
 
	   case 272 : // System.out.println("ExplicitConstructorInvocation ::= this LPAREN ArgumentListopt RPAREN");  //$NON-NLS-1$
			   consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.This);  
			   break ;
 
	   case 273 : // System.out.println("ExplicitConstructorInvocation ::= super LPAREN ArgumentListopt...");  //$NON-NLS-1$
			   consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.Super);  
			   break ;
 
	   case 274 : // System.out.println("ExplicitConstructorInvocation ::= Primary DOT super LPAREN...");  //$NON-NLS-1$
			   consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.Super);  
			   break ;
 
	   case 275 : // System.out.println("ExplicitConstructorInvocation ::= Name DOT super LPAREN...");  //$NON-NLS-1$
			   consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.Super);  
			   break ;
 
	   case 276 : // System.out.println("ExplicitConstructorInvocation ::= Primary DOT this LPAREN...");  //$NON-NLS-1$
			   consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.This);  
			   break ;
 
	   case 277 : // System.out.println("ExplicitConstructorInvocation ::= Name DOT this LPAREN...");  //$NON-NLS-1$
			   consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.This);  
			   break ;
 
	   case 278 : // System.out.println("InterfaceDeclaration ::= InterfaceHeader InterfaceBody");  //$NON-NLS-1$
			   consumeInterfaceDeclaration();  
			   break ;
 
	   case 279 : // System.out.println("InterfaceHeader ::= InterfaceHeaderName InterfaceHeaderExtendsopt");  //$NON-NLS-1$
			   consumeInterfaceHeader();  
			   break ;
 
	   case 280 : // System.out.println("InterfaceHeaderName ::= Modifiersopt interface JavaIdentifier");  //$NON-NLS-1$
			   consumeInterfaceHeaderName();  
			   break ;
 
	   case 282 : // System.out.println("InterfaceHeaderExtends ::= extends InterfaceTypeList");  //$NON-NLS-1$
			   consumeInterfaceHeaderExtends();  
			   break ;
 
	   case 285 : // System.out.println("InterfaceMemberDeclarations ::= InterfaceMemberDeclarations...");  //$NON-NLS-1$
			   consumeInterfaceMemberDeclarations();  
			   break ;
 
	   case 286 : // System.out.println("InterfaceMemberDeclaration ::= SEMICOLON");  //$NON-NLS-1$
			   consumeEmptyInterfaceMemberDeclaration();  
			   break ;
 
	   case 289 : // System.out.println("InterfaceMemberDeclaration ::= InvalidMethodDeclaration");  //$NON-NLS-1$
			   ignoreMethodBody();  
			   break ;
 
	   case 290 : // System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader MethodBody");  //$NON-NLS-1$
			   ignoreInvalidConstructorDeclaration(true);   
			   break ;
 
	   case 291 : // System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader SEMICOLON");  //$NON-NLS-1$
			   ignoreInvalidConstructorDeclaration(false);   
			   break ;
 
	   case 297 : // System.out.println("ArrayInitializer ::= LBRACE ,opt RBRACE");  //$NON-NLS-1$
			   consumeEmptyArrayInitializer();  
			   break ;
 
	   case 298 : // System.out.println("ArrayInitializer ::= LBRACE VariableInitializers RBRACE");  //$NON-NLS-1$
			   consumeArrayInitializer();  
			   break ;
 
	   case 299 : // System.out.println("ArrayInitializer ::= LBRACE VariableInitializers COMMA RBRACE");  //$NON-NLS-1$
			   consumeArrayInitializer();  
			   break ;
 
	   case 301 : // System.out.println("VariableInitializers ::= VariableInitializers COMMA...");  //$NON-NLS-1$
			   consumeVariableInitializers();  
			   break ;
 
	   case 302 : // System.out.println("Block ::= OpenBlock LBRACE BlockStatementsopt RBRACE");  //$NON-NLS-1$
			   consumeBlock();  
			   break ;
 
	   case 303 : // System.out.println("OpenBlock ::=");  //$NON-NLS-1$
			   consumeOpenBlock() ;  
			   break ;
 
	   case 305 : // System.out.println("BlockStatements ::= BlockStatements BlockStatement");  //$NON-NLS-1$
			   consumeBlockStatements() ;  
			   break ;
 
	   case 309 : // System.out.println("BlockStatement ::= InvalidInterfaceDeclaration");  //$NON-NLS-1$
			   ignoreInterfaceDeclaration();  
			   break ;
 
	   case 310 : // System.out.println("LocalVariableDeclarationStatement ::= LocalVariableDeclaration...");  //$NON-NLS-1$
			   consumeLocalVariableDeclarationStatement();  
			   break ;
 
	   case 311 : // System.out.println("LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators");  //$NON-NLS-1$
			   consumeLocalVariableDeclaration();  
			   break ;
 
	   case 312 : // System.out.println("LocalVariableDeclaration ::= Modifiers Type PushModifiers...");  //$NON-NLS-1$
			   consumeLocalVariableDeclaration();  
			   break ;
 
	   case 313 : // System.out.println("PushModifiers ::=");  //$NON-NLS-1$
			   consumePushModifiers();  
			   break ;
 
	   case 337 : // System.out.println("EmptyStatement ::= SEMICOLON");  //$NON-NLS-1$
			   consumeEmptyStatement();  
			   break ;
 
	   case 338 : // System.out.println("LabeledStatement ::= JavaIdentifier COLON Statement");  //$NON-NLS-1$
			   consumeStatementLabel() ;  
			   break ;
 
	   case 339 : // System.out.println("LabeledStatementNoShortIf ::= JavaIdentifier COLON...");  //$NON-NLS-1$
			   consumeStatementLabel() ;  
			   break ;
 
		case 340 : // System.out.println("ExpressionStatement ::= StatementExpression SEMICOLON");  //$NON-NLS-1$
			   consumeExpressionStatement();  
			   break ;
 
	   case 349 : // System.out.println("IfThenStatement ::= if LPAREN Expression RPAREN Statement");  //$NON-NLS-1$
			   consumeStatementIfNoElse();  
			   break ;
 
	   case 350 : // System.out.println("IfThenElseStatement ::= if LPAREN Expression RPAREN...");  //$NON-NLS-1$
			   consumeStatementIfWithElse();  
			   break ;
 
	   case 351 : // System.out.println("IfThenElseStatementNoShortIf ::= if LPAREN Expression RPAREN...");  //$NON-NLS-1$
			   consumeStatementIfWithElse();  
			   break ;
 
	   case 352 : // System.out.println("SwitchStatement ::= switch LPAREN Expression RPAREN OpenBlock...");  //$NON-NLS-1$
			   consumeStatementSwitch() ;  
			   break ;
 
	   case 353 : // System.out.println("SwitchBlock ::= LBRACE RBRACE");  //$NON-NLS-1$
			   consumeEmptySwitchBlock() ;  
			   break ;
 
	   case 356 : // System.out.println("SwitchBlock ::= LBRACE SwitchBlockStatements SwitchLabels RBRACE");  //$NON-NLS-1$
			   consumeSwitchBlock() ;  
			   break ;
 
	   case 358 : // System.out.println("SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement");  //$NON-NLS-1$
			   consumeSwitchBlockStatements() ;  
			   break ;
 
	   case 359 : // System.out.println("SwitchBlockStatement ::= SwitchLabels BlockStatements");  //$NON-NLS-1$
			   consumeSwitchBlockStatement() ;  
			   break ;
 
	   case 361 : // System.out.println("SwitchLabels ::= SwitchLabels SwitchLabel");  //$NON-NLS-1$
			   consumeSwitchLabels() ;  
			   break ;
 
		case 362 : // System.out.println("SwitchLabel ::= case ConstantExpression COLON");  //$NON-NLS-1$
			   consumeCaseLabel();  
			   break ;
 
		case 363 : // System.out.println("SwitchLabel ::= default COLON");  //$NON-NLS-1$
			   consumeDefaultLabel();  
			   break ;
 
	   case 364 : // System.out.println("WhileStatement ::= while LPAREN Expression RPAREN Statement");  //$NON-NLS-1$
			   consumeStatementWhile() ;  
			   break ;
 
	   case 365 : // System.out.println("WhileStatementNoShortIf ::= while LPAREN Expression RPAREN...");  //$NON-NLS-1$
			   consumeStatementWhile() ;  
			   break ;
 
	   case 366 : // System.out.println("DoStatement ::= do Statement while LPAREN Expression RPAREN...");  //$NON-NLS-1$
			   consumeStatementDo() ;  
			   break ;
 
	   case 367 : // System.out.println("ForStatement ::= for LPAREN ForInitopt SEMICOLON Expressionopt...");  //$NON-NLS-1$
			   consumeStatementFor() ;  
			   break ;
 
	   case 368 : // System.out.println("ForStatementNoShortIf ::= for LPAREN ForInitopt SEMICOLON...");  //$NON-NLS-1$
			   consumeStatementFor() ;  
			   break ;
 
	   case 369 : // System.out.println("ForInit ::= StatementExpressionList");  //$NON-NLS-1$
			   consumeForInit() ;  
			   break ;
 
	   case 373 : // System.out.println("StatementExpressionList ::= StatementExpressionList COMMA...");  //$NON-NLS-1$
			   consumeStatementExpressionList() ;  
			   break ;
 
	   case 374 : // System.out.println("AssertStatement ::= assert Expression SEMICOLON");  //$NON-NLS-1$
			   consumeSimpleAssertStatement() ;  
			   break ;
 
	   case 375 : // System.out.println("AssertStatement ::= assert Expression COLON Expression SEMICOLON");  //$NON-NLS-1$
			   consumeAssertStatement() ;  
			   break ;
 
	   case 376 : // System.out.println("BreakStatement ::= break SEMICOLON");  //$NON-NLS-1$
			   consumeStatementBreak() ;  
			   break ;
 
	   case 377 : // System.out.println("BreakStatement ::= break Identifier SEMICOLON");  //$NON-NLS-1$
			   consumeStatementBreakWithLabel() ;  
			   break ;
 
	   case 378 : // System.out.println("ContinueStatement ::= continue SEMICOLON");  //$NON-NLS-1$
			   consumeStatementContinue() ;  
			   break ;
 
	   case 379 : // System.out.println("ContinueStatement ::= continue Identifier SEMICOLON");  //$NON-NLS-1$
			   consumeStatementContinueWithLabel() ;  
			   break ;
 
	   case 380 : // System.out.println("ReturnStatement ::= return Expressionopt SEMICOLON");  //$NON-NLS-1$
			   consumeStatementReturn() ;  
			   break ;
 
	   case 381 : // System.out.println("ThrowStatement ::= throw Expression SEMICOLON");  //$NON-NLS-1$
			   consumeStatementThrow();
 
			   break ;
 
	   case 382 : // System.out.println("SynchronizedStatement ::= OnlySynchronized LPAREN Expression RPAREN");  //$NON-NLS-1$
			   consumeStatementSynchronized();  
			   break ;
 
	   case 383 : // System.out.println("OnlySynchronized ::= synchronized");  //$NON-NLS-1$
			   consumeOnlySynchronized();  
			   break ;
 
	   case 384 : // System.out.println("TryStatement ::= try TryBlock Catches");  //$NON-NLS-1$
			   consumeStatementTry(false);  
			   break ;
 
	   case 385 : // System.out.println("TryStatement ::= try TryBlock Catchesopt Finally");  //$NON-NLS-1$
			   consumeStatementTry(true);  
			   break ;
 
	   case 387 : // System.out.println("ExitTryBlock ::=");  //$NON-NLS-1$
			   consumeExitTryBlock();  
			   break ;
 
	   case 389 : // System.out.println("Catches ::= Catches CatchClause");  //$NON-NLS-1$
			   consumeCatches();  
			   break ;
 
	   case 390 : // System.out.println("CatchClause ::= catch LPAREN FormalParameter RPAREN Block");  //$NON-NLS-1$
			   consumeStatementCatch() ;  
			   break ;
 
	   case 392 : // System.out.println("PushLPAREN ::= LPAREN");  //$NON-NLS-1$
			   consumeLeftParen();  
			   break ;
 
	   case 393 : // System.out.println("PushRPAREN ::= RPAREN");  //$NON-NLS-1$
			   consumeRightParen();  
			   break ;
 
	   case 398 : // System.out.println("PrimaryNoNewArray ::= this");  //$NON-NLS-1$
			   consumePrimaryNoNewArrayThis();  
			   break ;
 
	   case 399 : // System.out.println("PrimaryNoNewArray ::= PushLPAREN Expression PushRPAREN");  //$NON-NLS-1$
			   consumePrimaryNoNewArray();  
			   break ;
 
	   case 402 : // System.out.println("PrimaryNoNewArray ::= Name DOT this");  //$NON-NLS-1$
			   consumePrimaryNoNewArrayNameThis();  
			   break ;
 
	   case 403 : // System.out.println("PrimaryNoNewArray ::= Name DOT super");  //$NON-NLS-1$
			   consumePrimaryNoNewArrayNameSuper();  
			   break ;
 
	   case 404 : // System.out.println("PrimaryNoNewArray ::= Name DOT class");  //$NON-NLS-1$
			   consumePrimaryNoNewArrayName();  
			   break ;
 
	   case 405 : // System.out.println("PrimaryNoNewArray ::= ArrayType DOT class");  //$NON-NLS-1$
			   consumePrimaryNoNewArrayArrayType();  
			   break ;
 
	   case 406 : // System.out.println("PrimaryNoNewArray ::= PrimitiveType DOT class");  //$NON-NLS-1$
			   consumePrimaryNoNewArrayPrimitiveType();  
			   break ;
 
	   case 409 : // System.out.println("AllocationHeader ::= new ClassType LPAREN ArgumentListopt RPAREN");  //$NON-NLS-1$
			   consumeAllocationHeader();  
			   break ;
 
	   case 410 : // System.out.println("ClassInstanceCreationExpression ::= new ClassType LPAREN...");  //$NON-NLS-1$
			   consumeClassInstanceCreationExpression();  
			   break ;
 
	   case 411 : // System.out.println("ClassInstanceCreationExpression ::= Primary DOT new SimpleName...");  //$NON-NLS-1$
			   consumeClassInstanceCreationExpressionQualified() ;  
			   break ;
 
	   case 412 : // System.out.println("ClassInstanceCreationExpression ::=...");  //$NON-NLS-1$
			   consumeClassInstanceCreationExpressionQualified() ;  
			   break ;
 
	   case 413 : // System.out.println("ClassInstanceCreationExpressionName ::= Name DOT");  //$NON-NLS-1$
			   consumeClassInstanceCreationExpressionName() ;  
			   break ;
 
	   case 414 : // System.out.println("ClassBodyopt ::=");  //$NON-NLS-1$
			   consumeClassBodyopt();  
			   break ;
 
	   case 416 : // System.out.println("EnterAnonymousClassBody ::=");  //$NON-NLS-1$
			   consumeEnterAnonymousClassBody();  
			   break ;
 
	   case 418 : // System.out.println("ArgumentList ::= ArgumentList COMMA Expression");  //$NON-NLS-1$
			   consumeArgumentList();  
			   break ;
 
	   case 419 : // System.out.println("ArrayCreationHeader ::= new PrimitiveType DimWithOrWithOutExprs");  //$NON-NLS-1$
			   consumeArrayCreationHeader();  
			   break ;
 
	   case 420 : // System.out.println("ArrayCreationHeader ::= new ClassOrInterfaceType...");  //$NON-NLS-1$
			   consumeArrayCreationHeader();  
			   break ;
 
	   case 421 : // System.out.println("ArrayCreationWithoutArrayInitializer ::= new PrimitiveType...");  //$NON-NLS-1$
			   consumeArrayCreationExpressionWithoutInitializer();  
			   break ;
 
	   case 422 : // System.out.println("ArrayCreationWithArrayInitializer ::= new PrimitiveType...");  //$NON-NLS-1$
			   consumeArrayCreationExpressionWithInitializer();  
			   break ;
 
	   case 423 : // System.out.println("ArrayCreationWithoutArrayInitializer ::= new ClassOrInterfaceType...");  //$NON-NLS-1$
			   consumeArrayCreationExpressionWithoutInitializer();  
			   break ;
 
	   case 424 : // System.out.println("ArrayCreationWithArrayInitializer ::= new ClassOrInterfaceType...");  //$NON-NLS-1$
			   consumeArrayCreationExpressionWithInitializer();  
			   break ;
 
	   case 426 : // System.out.println("DimWithOrWithOutExprs ::= DimWithOrWithOutExprs DimWithOrWithOutExpr");  //$NON-NLS-1$
			   consumeDimWithOrWithOutExprs();  
			   break ;
 
		case 428 : // System.out.println("DimWithOrWithOutExpr ::= LBRACKET RBRACKET");  //$NON-NLS-1$
			   consumeDimWithOrWithOutExpr();  
			   break ;
 
		case 429 : // System.out.println("Dims ::= DimsLoop");  //$NON-NLS-1$
			   consumeDims();  
			   break ;
 
		case 432 : // System.out.println("OneDimLoop ::= LBRACKET RBRACKET");  //$NON-NLS-1$
			   consumeOneDimLoop();  
			   break ;
 
	   case 433 : // System.out.println("FieldAccess ::= Primary DOT JavaIdentifier");  //$NON-NLS-1$
			   consumeFieldAccess(false);  
			   break ;
 
	   case 434 : // System.out.println("FieldAccess ::= super DOT JavaIdentifier");  //$NON-NLS-1$
			   consumeFieldAccess(true);  
			   break ;
 
	   case 435 : // System.out.println("MethodInvocation ::= NameOrAj LPAREN ArgumentListopt RPAREN");  //$NON-NLS-1$
			   consumeMethodInvocationName();  
			   break ;
 
	   case 436 : // System.out.println("MethodInvocation ::= Primary DOT JavaIdentifier LPAREN...");  //$NON-NLS-1$
			   consumeMethodInvocationPrimary();  
			   break ;
 
	   case 437 : // System.out.println("MethodInvocation ::= super DOT JavaIdentifier LPAREN ArgumentListopt");  //$NON-NLS-1$
			   consumeMethodInvocationSuper();  
			   break ;
 
	   case 438 : // System.out.println("ArrayAccess ::= Name LBRACKET Expression RBRACKET");  //$NON-NLS-1$
			   consumeArrayAccess(true);  
			   break ;
 
	   case 439 : // System.out.println("ArrayAccess ::= PrimaryNoNewArray LBRACKET Expression RBRACKET");  //$NON-NLS-1$
			   consumeArrayAccess(false);  
			   break ;
 
	   case 440 : // System.out.println("ArrayAccess ::= ArrayCreationWithArrayInitializer LBRACKET...");  //$NON-NLS-1$
			   consumeArrayAccess(false);  
			   break ;
 
	   case 442 : // System.out.println("PostfixExpression ::= NameOrAj");  //$NON-NLS-1$
			   consumePostfixExpression();  
			   break ;
 
	   case 445 : // System.out.println("PostIncrementExpression ::= PostfixExpression PLUS_PLUS");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.PLUS,true);  
			   break ;
 
	   case 446 : // System.out.println("PostDecrementExpression ::= PostfixExpression MINUS_MINUS");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.MINUS,true);  
			   break ;
 
	   case 447 : // System.out.println("PushPosition ::=");  //$NON-NLS-1$
			   consumePushPosition();  
			   break ;
 
	   case 450 : // System.out.println("UnaryExpression ::= PLUS PushPosition UnaryExpression");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.PLUS);  
			   break ;
 
	   case 451 : // System.out.println("UnaryExpression ::= MINUS PushPosition UnaryExpression");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.MINUS);  
			   break ;
 
	   case 453 : // System.out.println("PreIncrementExpression ::= PLUS_PLUS PushPosition UnaryExpression");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.PLUS,false);  
			   break ;
 
	   case 454 : // System.out.println("PreDecrementExpression ::= MINUS_MINUS PushPosition UnaryExpression");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.MINUS,false);  
			   break ;
 
	   case 456 : // System.out.println("UnaryExpressionNotPlusMinus ::= TWIDDLE PushPosition UnaryExpression");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.TWIDDLE);  
			   break ;
 
	   case 457 : // System.out.println("UnaryExpressionNotPlusMinus ::= NOT PushPosition UnaryExpression");  //$NON-NLS-1$
			   consumeUnaryExpression(OperatorIds.NOT);  
			   break ;
 
	   case 459 : // System.out.println("CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN...");  //$NON-NLS-1$
			   consumeCastExpression();  
			   break ;
 
	   case 460 : // System.out.println("CastExpression ::= PushLPAREN Name Dims PushRPAREN...");  //$NON-NLS-1$
			   consumeCastExpression();  
			   break ;
 
	   case 461 : // System.out.println("CastExpression ::= PushLPAREN Expression PushRPAREN...");  //$NON-NLS-1$
			   consumeCastExpressionLL1();  
			   break ;
 
	   case 462 : // System.out.println("InsideCastExpression ::=");  //$NON-NLS-1$
			   consumeInsideCastExpression();  
			   break ;
 
	   case 463 : // System.out.println("InsideCastExpressionLL1 ::=");  //$NON-NLS-1$
			   consumeInsideCastExpressionLL1();  
			   break ;
 
	   case 465 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression MULTIPLY...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.MULTIPLY);  
			   break ;
 
	   case 466 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression DIVIDE...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.DIVIDE);  
			   break ;
 
	   case 467 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression REMAINDER...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.REMAINDER);  
			   break ;
 
	   case 469 : // System.out.println("AdditiveExpression ::= AdditiveExpression PLUS...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.PLUS);  
			   break ;
 
	   case 470 : // System.out.println("AdditiveExpression ::= AdditiveExpression MINUS...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.MINUS);  
			   break ;
 
	   case 472 : // System.out.println("ShiftExpression ::= ShiftExpression LEFT_SHIFT AdditiveExpression");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.LEFT_SHIFT);  
			   break ;
 
	   case 473 : // System.out.println("ShiftExpression ::= ShiftExpression RIGHT_SHIFT AdditiveExpression");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.RIGHT_SHIFT);  
			   break ;
 
	   case 474 : // System.out.println("ShiftExpression ::= ShiftExpression UNSIGNED_RIGHT_SHIFT...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.UNSIGNED_RIGHT_SHIFT);  
			   break ;
 
	   case 476 : // System.out.println("RelationalExpression ::= RelationalExpression LESS ShiftExpression");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.LESS);  
			   break ;
 
	   case 477 : // System.out.println("RelationalExpression ::= RelationalExpression GREATER...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.GREATER);  
			   break ;
 
	   case 478 : // System.out.println("RelationalExpression ::= RelationalExpression LESS_EQUAL...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.LESS_EQUAL);  
			   break ;
 
	   case 479 : // System.out.println("RelationalExpression ::= RelationalExpression GREATER_EQUAL...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.GREATER_EQUAL);  
			   break ;
 
	   case 480 : // System.out.println("RelationalExpression ::= RelationalExpression instanceof...");  //$NON-NLS-1$
			   consumeInstanceOfExpression(OperatorIds.INSTANCEOF);  
			   break ;
 
	   case 482 : // System.out.println("EqualityExpression ::= EqualityExpression EQUAL_EQUAL...");  //$NON-NLS-1$
			   consumeEqualityExpression(OperatorIds.EQUAL_EQUAL);  
			   break ;
 
	   case 483 : // System.out.println("EqualityExpression ::= EqualityExpression NOT_EQUAL...");  //$NON-NLS-1$
			   consumeEqualityExpression(OperatorIds.NOT_EQUAL);  
			   break ;
 
	   case 485 : // System.out.println("AndExpression ::= AndExpression AND EqualityExpression");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.AND);  
			   break ;
 
	   case 487 : // System.out.println("ExclusiveOrExpression ::= ExclusiveOrExpression XOR AndExpression");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.XOR);  
			   break ;
 
	   case 489 : // System.out.println("InclusiveOrExpression ::= InclusiveOrExpression OR...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.OR);  
			   break ;
 
	   case 491 : // System.out.println("ConditionalAndExpression ::= ConditionalAndExpression AND_AND...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.AND_AND);  
			   break ;
 
	   case 493 : // System.out.println("ConditionalOrExpression ::= ConditionalOrExpression OR_OR...");  //$NON-NLS-1$
			   consumeBinaryExpression(OperatorIds.OR_OR);  
			   break ;
 
	   case 495 : // System.out.println("ConditionalExpression ::= ConditionalOrExpression QUESTION...");  //$NON-NLS-1$
			   consumeConditionalExpression(OperatorIds.QUESTIONCOLON) ;  
			   break ;
 
	   case 498 : // System.out.println("Assignment ::= PostfixExpression AssignmentOperator...");  //$NON-NLS-1$
			   consumeAssignment();  
			   break ;
 
	   case 500 : // System.out.println("Assignment ::= InvalidArrayInitializerAssignement");  //$NON-NLS-1$
			   ignoreExpressionAssignment(); 
			   break ;
 
	   case 501 : // System.out.println("AssignmentOperator ::= EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(EQUAL);  
			   break ;
 
	   case 502 : // System.out.println("AssignmentOperator ::= MULTIPLY_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(MULTIPLY);  
			   break ;
 
	   case 503 : // System.out.println("AssignmentOperator ::= DIVIDE_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(DIVIDE);  
			   break ;
 
	   case 504 : // System.out.println("AssignmentOperator ::= REMAINDER_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(REMAINDER);  
			   break ;
 
	   case 505 : // System.out.println("AssignmentOperator ::= PLUS_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(PLUS);  
			   break ;
 
	   case 506 : // System.out.println("AssignmentOperator ::= MINUS_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(MINUS);  
			   break ;
 
	   case 507 : // System.out.println("AssignmentOperator ::= LEFT_SHIFT_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(LEFT_SHIFT);  
			   break ;
 
	   case 508 : // System.out.println("AssignmentOperator ::= RIGHT_SHIFT_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(RIGHT_SHIFT);  
			   break ;
 
	   case 509 : // System.out.println("AssignmentOperator ::= UNSIGNED_RIGHT_SHIFT_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(UNSIGNED_RIGHT_SHIFT);  
			   break ;
 
	   case 510 : // System.out.println("AssignmentOperator ::= AND_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(AND);  
			   break ;
 
	   case 511 : // System.out.println("AssignmentOperator ::= XOR_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(XOR);  
			   break ;
 
	   case 512 : // System.out.println("AssignmentOperator ::= OR_EQUAL");  //$NON-NLS-1$
			   consumeAssignmentOperator(OR);  
			   break ;
 
	   case 519 : // System.out.println("Expressionopt ::=");  //$NON-NLS-1$
			   consumeEmptyExpression();  
			   break ;
 
	   case 523 : // System.out.println("ImportDeclarationsopt ::=");  //$NON-NLS-1$
			   consumeEmptyImportDeclarationsopt();  
			   break ;
 
	   case 524 : // System.out.println("ImportDeclarationsopt ::= ImportDeclarations");  //$NON-NLS-1$
			   consumeImportDeclarationsopt();  
			   break ;
 
	   case 525 : // System.out.println("TypeDeclarationsopt ::=");  //$NON-NLS-1$
			   consumeEmptyTypeDeclarationsopt();  
			   break ;
 
	   case 526 : // System.out.println("TypeDeclarationsopt ::= TypeDeclarations");  //$NON-NLS-1$
			   consumeTypeDeclarationsopt();  
			   break ;
 
	   case 527 : // System.out.println("ClassBodyDeclarationsopt ::=");  //$NON-NLS-1$
			   consumeEmptyClassBodyDeclarationsopt();  
			   break ;
 
	   case 528 : // System.out.println("ClassBodyDeclarationsopt ::= NestedType ClassBodyDeclarations");  //$NON-NLS-1$
			   consumeClassBodyDeclarationsopt();  
			   break ;
 
		case 529 : // System.out.println("Modifiersopt ::=");  //$NON-NLS-1$
			   consumeDefaultModifiers();  
			   break ;
 
	   case 530 : // System.out.println("Modifiersopt ::= Modifiers");  //$NON-NLS-1$
			   consumeModifiers();  
			   break ;
 
	   case 531 : // System.out.println("BlockStatementsopt ::=");  //$NON-NLS-1$
			   consumeEmptyBlockStatementsopt();  
			   break ;
 
		case 533 : // System.out.println("Dimsopt ::=");  //$NON-NLS-1$
			   consumeEmptyDimsopt();  
			   break ;
 
		case 535 : // System.out.println("ArgumentListopt ::=");  //$NON-NLS-1$
			   consumeEmptyArgumentListopt();  
			   break ;
 
	   case 539 : // System.out.println("FormalParameterListopt ::=");  //$NON-NLS-1$
			   consumeFormalParameterListopt();  
			   break ;
 
		case 543 : // System.out.println("InterfaceMemberDeclarationsopt ::=");  //$NON-NLS-1$
			   consumeEmptyInterfaceMemberDeclarationsopt();  
			   break ;
 
		case 544 : // System.out.println("InterfaceMemberDeclarationsopt ::= NestedType...");  //$NON-NLS-1$
			   consumeInterfaceMemberDeclarationsopt();  
			   break ;
 
	   case 545 : // System.out.println("NestedType ::=");  //$NON-NLS-1$
			   consumeNestedType();  
			   break ;

		case 546 : // System.out.println("ForInitopt ::=");  //$NON-NLS-1$
			   consumeEmptyForInitopt();  
			   break ;
 
		case 548 : // System.out.println("ForUpdateopt ::=");  //$NON-NLS-1$
			   consumeEmptyForUpdateopt();  
			   break ;
 
		case 552 : // System.out.println("Catchesopt ::=");  //$NON-NLS-1$
			   consumeEmptyCatchesopt();  
			   break ;
 
	   }
   } 


protected void consumeSimpleAssertStatement() {
	super.consumeSimpleAssertStatement();
}
	public AjParser(
		ProblemReporter problemReporter,
		boolean optimizeStringLiterals) {
		super(problemReporter, optimizeStringLiterals);
	}

	// don't try to recover if we're parsing AspectJ constructs
	protected boolean shouldTryToRecover() {
		int index = 0;
		ASTNode node;
		while (index < astStack.length && (node = astStack[index++]) != null) {
			if (node instanceof AspectDeclaration || 
				node instanceof PointcutDeclaration || 
				node instanceof AdviceDeclaration) {
				return false;
			}
		}
		return true;
	}

}
