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
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.Literal;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.RecoveredType;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.util.CharArrayOps;



public class AjParser extends Parser {
	//===DATA===DATA===DATA===DATA===DATA===DATA===//
    public final static byte rhs[] = {0,
            2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
            2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,2,2,1,1,1,1,1,1,1,1,1,
            1,1,1,1,3,1,1,1,3,4,0,1,2,1,1,
            1,1,1,1,1,1,1,5,1,2,1,2,2,2,1,
            1,2,2,2,4,1,1,1,1,2,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,2,4,3,5,0,2,1,3,1,2,0,2,
            1,3,5,4,1,1,2,5,4,2,6,3,3,4,3,
            1,0,1,3,1,1,1,1,2,4,6,2,2,3,5,
            7,0,4,1,3,3,1,2,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,4,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,2,3,3,2,2,1,3,1,3,1,2,1,
            1,1,3,0,3,1,1,1,1,1,1,1,4,1,3,
            3,7,0,0,0,0,0,2,1,1,1,2,2,4,4,
            5,4,4,2,1,2,3,3,3,1,3,3,1,3,1,
            4,0,2,1,2,2,4,1,1,2,5,5,7,7,7,
            7,2,2,3,2,2,3,1,2,1,2,1,1,2,2,
            1,1,1,1,1,3,3,4,1,3,4,0,1,2,1,
            1,1,1,2,3,4,0,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,3,3,2,1,1,1,1,1,1,1,5,7,7,6,
            2,3,3,4,1,2,2,1,2,3,2,5,5,7,9,
            9,1,1,1,1,3,3,5,2,3,2,3,3,3,5,
            1,3,4,1,2,5,2,1,1,1,1,1,1,1,3,
            1,1,3,3,3,3,3,1,1,5,6,8,7,2,0,
            2,0,1,3,3,4,3,4,1,2,3,2,1,1,2,
            2,3,3,4,6,6,4,4,4,1,1,1,1,2,2,
            0,1,1,3,3,1,3,3,1,3,3,1,6,6,5,
            0,0,1,3,3,3,1,3,3,1,3,3,3,1,3,
            3,3,3,3,1,3,3,1,3,1,3,1,3,1,3,
            1,3,1,5,1,1,3,3,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,0,1,0,1,0,1,0,
            1,0,1,0,1,0,2,0,1,0,1,0,1,0,1,
            0,1,0,1,0,1,0,2,0,0,1,0,1,0,1,
            0,1
    };

		
	public  static char asbStatic[] = null;
	public  static char asrStatic[] = null;
	public  static char symbol_indexStatic[] = null;
	
	public final static String name[] = { null,
            "++",
            "--",
            "==",
            "<=",
            ">=",
            "!=",
            "<<",
            ">>",
            ">>>",
            "+=",
            "-=",
            "*=",
            "/=",
            "&=",
            "|=",
            "^=",
            "%=",
            "<<=",
            ">>=",
            ">>>=",
            "||",
            "&&",
            "+",
            "-",
            "!",
            "%",
            "^",
            "&",
            "*",
            "|",
            "~",
            "/",
            ">",
            "<",
            "(",
            ")",
            "{",
            "}",
            "[",
            "]",
            ";",
            "?",
            ":",
            ",",
            ".",
            "=",
            "",
            "$empty",
            "Identifier",
            "abstract",
            "assert",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "extends",
            "false",
            "final",
            "finally",
            "float",
            "for",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "null",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "true",
            "try",
            "void",
            "volatile",
            "while",
            "aspect",
            "pointcut",
            "around",
            "before",
            "after",
            "declare",
            "privileged",
            "IntegerLiteral",
            "LongLiteral",
            "FloatingPointLiteral",
            "DoubleLiteral",
            "CharacterLiteral",
            "StringLiteral",
            UNEXPECTED_EOF,
            "Invalid Character",
            "Goal",
            "MethodBody",
            "ConstructorBody",
            "StaticInitializer",
            "Initializer",
            "Headers",
            "BlockStatements",
            "MethodPushModifiersHeader",
            "CatchHeader",
            "FieldDeclaration",
            "ImportDeclaration",
            "PackageDeclaration",
            "TypeDeclaration",
            "GenericMethodDeclaration",
            "ClassBodyDeclaration",
            "Expression",
            "JavaIdentifier",
            "AjSimpleName",
            "JavaIdentifierNoAround",
            "AjSimpleNameNoAround",
            "Type",
            "PrimitiveType",
            "ReferenceType",
            "ClassOrInterfaceType",
            "ArrayType",
            "Name",
            "Dims",
            "ClassType",
            "NameOrAj",
            "AjName",
            "SimpleName",
            "Header",
            "ClassHeader",
            "InterfaceHeader",
            "MethodHeader",
            "ConstructorHeader",
            "FormalParameter",
            "ImportDeclarations",
            "TypeDeclarations",
            "PackageDeclarationName",
            "SingleTypeImportDeclarationNam" +
            "e",
            "TypeImportOnDemandDeclarationN" +
            "ame",
            "Modifiers",
            "Modifier",
            "InterfaceMemberDeclaration",
            "AspectHeader",
            "AspectBody",
            "AspectHeaderName",
            "AspectHeaderRestStart",
            "PseudoTokens",
            "AspectBodyDeclarations",
            "AspectBodyDeclaration",
            "PointcutHeader",
            "MethodHeaderParameters",
            "AroundHeader",
            "AroundHeaderName",
            "BasicAdviceHeader",
            "BasicAdviceHeaderName",
            "OnType",
            "InterTypeMethodHeader",
            "InterTypeMethodHeaderName",
            "InterTypeConstructorHeader",
            "InterTypeConstructorHeaderName",
            "VariableInitializer",
            "DeclareHeader",
            "PseudoToken",
            "ClassBody",
            "ClassHeaderName",
            "InterfaceTypeList",
            "InterfaceType",
            "ClassBodyDeclarations",
            "Block",
            "VariableDeclarators",
            "VariableDeclarator",
            "VariableDeclaratorId",
            "ArrayInitializer",
            "MethodHeaderName",
            "MethodPushModifiersHeaderName",
            "ClassTypeList",
            "ConstructorHeaderName",
            "FormalParameterList",
            "ClassTypeElt",
            "StaticOnly",
            "ExplicitConstructorInvocation",
            "Primary",
            "InterfaceBody",
            "InterfaceHeaderName",
            "InterfaceMemberDeclarations",
            "VariableInitializers",
            "BlockStatement",
            "Statement",
            "LocalVariableDeclaration",
            "StatementWithoutTrailingSubsta" +
            "tement",
            "StatementNoShortIf",
            "StatementExpression",
            "PostIncrementExpression",
            "PostDecrementExpression",
            "MethodInvocation",
            "ClassInstanceCreationExpressio" +
            "n",
            "SwitchBlock",
            "SwitchBlockStatements",
            "SwitchLabels",
            "SwitchBlockStatement",
            "SwitchLabel",
            "ConstantExpression",
            "StatementExpressionList",
            "OnlySynchronized",
            "Catches",
            "Finally",
            "CatchClause",
            "PushLPAREN",
            "PushRPAREN",
            "PrimaryNoNewArray",
            "ArrayCreationWithArrayInitiali" +
            "zer",
            "ClassInstanceCreationExpressio" +
            "nName",
            "ArgumentList",
            "DimWithOrWithOutExprs",
            "DimWithOrWithOutExpr",
            "DimsLoop",
            "OneDimLoop",
            "PostfixExpression",
            "UnaryExpression",
            "UnaryExpressionNotPlusMinus",
            "MultiplicativeExpression",
            "AdditiveExpression",
            "ShiftExpression",
            "RelationalExpression",
            "EqualityExpression",
            "AndExpression",
            "ExclusiveOrExpression",
            "InclusiveOrExpression",
            "ConditionalAndExpression",
            "ConditionalOrExpression",
            "ConditionalExpression",
            "AssignmentExpression",
            "AssignmentOperator"
    };
    
	public  static short check_tableStatic[] = null;
	public  static char lhsStatic[] =  null;
	public  static char actionStatic[] = lhsStatic;

	protected void initData() {
		rhsInst = rhs;
		asb = asbStatic;
		asr = asrStatic;
		symbol_index = symbol_indexStatic;
		nameInst = name;
		check_table = check_tableStatic;
		lhs = lhsStatic;
		action = actionStatic;
	}

	static {
		try{
			initAjTables(AjParser.class);
		} catch(java.io.IOException ex){
			throw new ExceptionInInitializerError(ex.getMessage());
		}
	}
	
	public final static void initAjTables(Class parserClass)
		throws java.io.IOException {

		final String prefix = FILEPREFIX;
		int i = 0;
		lhsStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
		char[] chars = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
		check_tableStatic = new short[chars.length];
		for (int c = chars.length; c-- > 0;) {
			check_tableStatic[c] = (short) (chars[c] - 32768);
		}
		asbStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
		asrStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
		symbol_indexStatic = readTable(parserClass, prefix + (++i) + ".rsc"); //$NON-NLS-1$
		actionStatic = lhsStatic;
	}

	public void initializeScanner(){
		CompilerOptions options = this.problemReporter.options;
		this.scanner = new AjScanner(
			false /*comment*/, 
			false /*whitespace*/, 
			options.getSeverity(CompilerOptions.NonExternalizedString) != ProblemSeverities.Ignore /*nls*/, 
			this.assertMode /*assert*/, 
			options.taskTags/*taskTags*/,
			options.taskPriorites/*taskPriorities*/);
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
		if (CharArrayOps.equals(m.selector, "proceed".toCharArray())) {
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
	    	//XXX illegal name here
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
		AstNode top = astStack[astPtr];
		AstNode next = astStack[astPtr-1];
		astStack[astPtr] = next;
		astStack[astPtr-1] = top;
	}



	// This method is part of an automatic generation : do NOT edit-modify  
	                                                                                                                                                                       // This method is part of an automatic generation : do NOT edit-modify  
protected void consumeRule(int act) {
  switch ( act ) {
    case 33 : // System.out.println("Type ::= PrimitiveType");
		    consumePrimitiveType();  
			break ;
 
    case 47 : // System.out.println("ReferenceType ::= ClassOrInterfaceType");
		    consumeReferenceType();   
			break ;
 
    case 65 : // System.out.println("AjQualifiedName ::= AjName DOT SimpleName");
		    consumeQualifiedName();  
			break ;
 
    case 69 : // System.out.println("QualifiedName ::= Name DOT JavaIdentifier");
		    consumeQualifiedName();  
			break ;
 
    case 70 : // System.out.println("CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt");
		    consumeCompilationUnit();  
			break ;
 
    case 71 : // System.out.println("EnterCompilationUnit ::=");
		    consumeEnterCompilationUnit();  
			break ;
 
    case 83 : // System.out.println("CatchHeader ::= catch LPAREN FormalParameter RPAREN LBRACE");
		    consumeCatchHeader();  
			break ;
 
    case 85 : // System.out.println("ImportDeclarations ::= ImportDeclarations ImportDeclaration");
		    consumeImportDeclarations();  
			break ;
 
    case 87 : // System.out.println("TypeDeclarations ::= TypeDeclarations TypeDeclaration");
		    consumeTypeDeclarations();  
			break ;
 
    case 88 : // System.out.println("PackageDeclaration ::= PackageDeclarationName SEMICOLON");
		     consumePackageDeclaration();  
			break ;
 
    case 89 : // System.out.println("PackageDeclarationName ::= package Name");
		     consumePackageDeclarationName();  
			break ;
 
    case 92 : // System.out.println("SingleTypeImportDeclaration ::= SingleTypeImportDeclarationName SEMICOLON");
		    consumeSingleTypeImportDeclaration();  
			break ;
 
    case 93 : // System.out.println("SingleTypeImportDeclarationName ::= import Name");
		    consumeSingleTypeImportDeclarationName();  
			break ;
 
    case 94 : // System.out.println("TypeImportOnDemandDeclaration ::= TypeImportOnDemandDeclarationName SEMICOLON");
		    consumeTypeImportOnDemandDeclaration();  
			break ;
 
    case 95 : // System.out.println("TypeImportOnDemandDeclarationName ::= import Name DOT MULTIPLY");
		    consumeTypeImportOnDemandDeclarationName();  
			break ;
 
     case 98 : // System.out.println("TypeDeclaration ::= SEMICOLON");
		    consumeEmptyTypeDeclaration();  
			break ;
 
    case 124 : // System.out.println("AspectDeclaration ::= AspectHeader AspectBody");
		    consumeAspectDeclaration();  
			break ;
 
    case 125 : // System.out.println("AspectHeader ::= AspectHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt...");
		    consumeAspectHeader();  
			break ;
 
    case 126 : // System.out.println("AspectHeaderName ::= Modifiersopt aspect Identifier");
		    consumeAspectHeaderName(false);  
			break ;
 
    case 127 : // System.out.println("AspectHeaderName ::= Modifiersopt privileged Modifiersopt aspect Identifier");
		    consumeAspectHeaderName(true);  
			break ;
 
    case 129 : // System.out.println("AspectHeaderRest ::= AspectHeaderRestStart PseudoTokens");
		    consumeAspectHeaderRest();  
			break ;
 
    case 130 : // System.out.println("AspectHeaderRestStart ::= Identifier");
		    consumePseudoTokenIdentifier();  
			break ;
 
    case 133 : // System.out.println("AspectBodyDeclarations ::= AspectBodyDeclarations AspectBodyDeclaration");
		    consumeClassBodyDeclarations();  
			break ;
 
    case 134 : // System.out.println("AspectBodyDeclarationsopt ::=");
		    consumeEmptyClassBodyDeclarationsopt();  
			break ;
 
    case 135 : // System.out.println("AspectBodyDeclarationsopt ::= NestedType AspectBodyDeclarations");
		    consumeClassBodyDeclarationsopt();  
			break ;
 
    case 136 : // System.out.println("AspectBodyDeclaration ::= ClassBodyDeclaration");
		    consumeClassBodyDeclarationInAspect();  
			break ;
 
    case 137 : // System.out.println("PointcutDeclaration ::= PointcutHeader MethodHeaderParameters SEMICOLON");
		    consumeEmptyPointcutDeclaration();  
			break ;
 
    case 138 : // System.out.println("PointcutDeclaration ::= PointcutHeader MethodHeaderParameters COLON PseudoTokens...");
		    consumePointcutDeclaration();  
			break ;
 
    case 139 : // System.out.println("PointcutHeader ::= Modifiersopt pointcut JavaIdentifier LPAREN");
		    consumePointcutHeader();  
			break ;
 
    case 142 : // System.out.println("AroundDeclaration ::= AroundHeader MethodBody");
		    consumeAroundDeclaration();  
			break ;
 
    case 143 : // System.out.println("AroundHeader ::= AroundHeaderName MethodHeaderParameters...");
		    consumeAroundHeader();  
			break ;
 
    case 144 : // System.out.println("AroundHeaderName ::= Modifiersopt Type around LPAREN");
		    consumeAroundHeaderName();  
			break ;
 
    case 145 : // System.out.println("BasicAdviceDeclaration ::= BasicAdviceHeader MethodBody");
		    consumeBasicAdviceDeclaration();  
			break ;
 
    case 146 : // System.out.println("BasicAdviceHeader ::= BasicAdviceHeaderName MethodHeaderParameters ExtraParamopt...");
		    consumeBasicAdviceHeader();  
			break ;
 
    case 147 : // System.out.println("BasicAdviceHeaderName ::= Modifiersopt before LPAREN");
		    consumeBasicAdviceHeaderName(false);  
			break ;
 
    case 148 : // System.out.println("BasicAdviceHeaderName ::= Modifiersopt after LPAREN");
		    consumeBasicAdviceHeaderName(true);  
			break ;
 
    case 149 : // System.out.println("ExtraParamopt ::= Identifier LPAREN FormalParameter RPAREN");
		    consumeExtraParameterWithFormal();  
			break ;
 
    case 150 : // System.out.println("ExtraParamopt ::= Identifier LPAREN RPAREN");
		    consumeExtraParameterNoFormal();  
			break ;
 
    case 151 : // System.out.println("ExtraParamopt ::= Identifier");
		    consumeExtraParameterNoFormal();  
			break ;
 
    case 154 : // System.out.println("OnType ::= OnType DOT JavaIdentifier");
		    consumeQualifiedName();  
			break ;
 
    case 159 : // System.out.println("InterTypeMethodDeclaration ::= InterTypeMethodHeader MethodBody");
		    // set to true to consume a method with a body
  consumeInterTypeMethodDeclaration(true);   
			break ;
 
    case 160 : // System.out.println("InterTypeMethodHeader ::= InterTypeMethodHeaderName MethodHeaderParameters...");
		    consumeInterTypeMethodHeader();  
			break ;
 
    case 161 : // System.out.println("InterTypeMethodHeaderName ::= Modifiersopt Type OnType DOT JavaIdentifier LPAREN");
		    consumeInterTypeMethodHeaderName();  
			break ;
 
    case 162 : // System.out.println("AbstractInterTypeMethodDeclaration ::= InterTypeMethodHeader SEMICOLON");
		    // set to false to consume a method without body
  consumeInterTypeMethodDeclaration(false);  
			break ;
 
    case 163 : // System.out.println("InterTypeConstructorDeclaration ::= InterTypeConstructorHeader ConstructorBody");
		    // set to true to consume a method with a body
  consumeInterTypeConstructorDeclaration();   
			break ;
 
    case 164 : // System.out.println("InterTypeConstructorHeader ::= InterTypeConstructorHeaderName...");
		    consumeInterTypeConstructorHeader();  
			break ;
 
    case 165 : // System.out.println("InterTypeConstructorHeaderName ::= Modifiersopt Name DOT new LPAREN");
		    consumeInterTypeConstructorHeaderName();  
			break ;
 
    case 166 : // System.out.println("InterTypeFieldDeclaration ::= Modifiersopt Type OnType DOT JavaIdentifier...");
		    consumeInterTypeFieldDeclaration();  
			break ;
 
    case 170 : // System.out.println("DeclareDeclaration ::= DeclareHeader PseudoTokens SEMICOLON");
		    consumeDeclareDeclaration();  
			break ;
 
    case 171 : // System.out.println("DeclareHeader ::= declare Identifier COLON");
		    consumeDeclareHeader();  
			break ;
 
    case 173 : // System.out.println("PseudoTokens ::= PseudoTokens PseudoToken");
		    consumePseudoTokens();  
			break ;
 
    case 174 : // System.out.println("PseudoToken ::= JavaIdentifier");
		    consumePseudoTokenIdentifier();  
			break ;
 
    case 175 : // System.out.println("PseudoToken ::= LPAREN");
		    consumePseudoToken("(");  
			break ;
 
    case 176 : // System.out.println("PseudoToken ::= RPAREN");
		    consumePseudoToken(")");  
			break ;
 
    case 177 : // System.out.println("PseudoToken ::= DOT");
		    consumePseudoToken(".");  
			break ;
 
    case 178 : // System.out.println("PseudoToken ::= MULTIPLY");
		    consumePseudoToken("*");  
			break ;
 
    case 179 : // System.out.println("PseudoToken ::= PLUS");
		    consumePseudoToken("+");  
			break ;
 
    case 180 : // System.out.println("PseudoToken ::= AND_AND");
		    consumePseudoToken("&&");  
			break ;
 
    case 181 : // System.out.println("PseudoToken ::= OR_OR");
		    consumePseudoToken("||");  
			break ;
 
    case 182 : // System.out.println("PseudoToken ::= NOT");
		    consumePseudoToken("!");  
			break ;
 
    case 183 : // System.out.println("PseudoToken ::= COLON");
		    consumePseudoToken(":");  
			break ;
 
    case 184 : // System.out.println("PseudoToken ::= COMMA");
		    consumePseudoToken(",");  
			break ;
 
    case 185 : // System.out.println("PseudoToken ::= LBRACKET");
		    consumePseudoToken("[");  
			break ;
 
    case 186 : // System.out.println("PseudoToken ::= RBRACKET");
		    consumePseudoToken("]");  
			break ;
 
    case 187 : // System.out.println("PseudoToken ::= PrimitiveType");
		    consumePseudoTokenPrimitiveType();  
			break ;
 
    case 188 : // System.out.println("PseudoToken ::= Modifier");
		    consumePseudoTokenModifier();  
			break ;
 
    case 189 : // System.out.println("PseudoToken ::= Literal");
		    consumePseudoTokenLiteral();  
			break ;
 
    case 190 : // System.out.println("PseudoToken ::= this");
		    consumePseudoToken("this", 1, true);  
			break ;
 
    case 191 : // System.out.println("PseudoToken ::= super");
		    consumePseudoToken("super", 1, true);  
			break ;
 
    case 192 : // System.out.println("PseudoToken ::= if LPAREN Expression RPAREN");
		    consumePseudoTokenIf();  
			break ;
 
    case 193 : // System.out.println("PseudoToken ::= assert");
		    consumePseudoToken("assert", 1, true);  
			break ;
 
    case 194 : // System.out.println("PseudoToken ::= import");
		    consumePseudoToken("import", 1, true);  
			break ;
 
    case 195 : // System.out.println("PseudoToken ::= package");
		    consumePseudoToken("package", 1, true);  
			break ;
 
    case 196 : // System.out.println("PseudoToken ::= throw");
		    consumePseudoToken("throw", 1, true);  
			break ;
 
    case 197 : // System.out.println("PseudoToken ::= new");
		    consumePseudoToken("new", 1, true);  
			break ;
 
    case 198 : // System.out.println("PseudoToken ::= do");
		    consumePseudoToken("do", 1, true);  
			break ;
 
    case 199 : // System.out.println("PseudoToken ::= for");
		    consumePseudoToken("for", 1, true);  
			break ;
 
    case 200 : // System.out.println("PseudoToken ::= switch");
		    consumePseudoToken("switch", 1, true);  
			break ;
 
    case 201 : // System.out.println("PseudoToken ::= try");
		    consumePseudoToken("try", 1, true);  
			break ;
 
    case 202 : // System.out.println("PseudoToken ::= while");
		    consumePseudoToken("while", 1, true);  
			break ;
 
    case 203 : // System.out.println("PseudoToken ::= break");
		    consumePseudoToken("break", 1, true);  
			break ;
 
    case 204 : // System.out.println("PseudoToken ::= continue");
		    consumePseudoToken("continue", 1, true);  
			break ;
 
    case 205 : // System.out.println("PseudoToken ::= return");
		    consumePseudoToken("return", 1, true);  
			break ;
 
    case 206 : // System.out.println("PseudoToken ::= case");
		    consumePseudoToken("case", 1, true);  
			break ;
 
    case 207 : // System.out.println("PseudoToken ::= catch");
		    consumePseudoToken("catch", 0, true);  
			break ;
 
    case 208 : // System.out.println("PseudoToken ::= instanceof");
		    consumePseudoToken("instanceof", 0, true);  
			break ;
 
    case 209 : // System.out.println("PseudoToken ::= else");
		    consumePseudoToken("else", 0, true);  
			break ;
 
    case 210 : // System.out.println("PseudoToken ::= extends");
		    consumePseudoToken("extends", 0, true);  
			break ;
 
    case 211 : // System.out.println("PseudoToken ::= finally");
		    consumePseudoToken("finally", 0, true);  
			break ;
 
    case 212 : // System.out.println("PseudoToken ::= implements");
		    consumePseudoToken("implements", 0, true);  
			break ;
 
    case 213 : // System.out.println("PseudoToken ::= throws");
		    consumePseudoToken("throws", 0, true);  
			break ;
 
    case 214 : // System.out.println("ClassDeclaration ::= ClassHeader ClassBody");
		    consumeClassDeclaration();  
			break ;
 
    case 215 : // System.out.println("ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt");
		    consumeClassHeader();  
			break ;
 
    case 216 : // System.out.println("ClassHeaderName ::= Modifiersopt class JavaIdentifier");
		    consumeClassHeaderName();  
			break ;
 
    case 217 : // System.out.println("ClassHeaderExtends ::= extends ClassType");
		    consumeClassHeaderExtends();  
			break ;
 
    case 218 : // System.out.println("ClassHeaderImplements ::= implements InterfaceTypeList");
		    consumeClassHeaderImplements();  
			break ;
 
    case 220 : // System.out.println("InterfaceTypeList ::= InterfaceTypeList COMMA InterfaceType");
		    consumeInterfaceTypeList();  
			break ;
 
    case 221 : // System.out.println("InterfaceType ::= ClassOrInterfaceType");
		    consumeInterfaceType();  
			break ;
 
    case 224 : // System.out.println("ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration");
		    consumeClassBodyDeclarations();  
			break ;
 
    case 228 : // System.out.println("ClassBodyDeclaration ::= Diet NestedMethod Block");
		    consumeClassBodyDeclaration();  
			break ;
 
    case 229 : // System.out.println("Diet ::=");
		    consumeDiet();  
			break ;

    case 230 : // System.out.println("Initializer ::= Diet NestedMethod Block");
		    consumeClassBodyDeclaration();  
			break ;
 
    case 237 : // System.out.println("ClassMemberDeclaration ::= SEMICOLON");
		    consumeEmptyClassMemberDeclaration();  
			break ;

    case 238 : // System.out.println("FieldDeclaration ::= Modifiersopt Type VariableDeclarators SEMICOLON");
		    consumeFieldDeclaration();  
			break ;
 
    case 240 : // System.out.println("VariableDeclarators ::= VariableDeclarators COMMA VariableDeclarator");
		    consumeVariableDeclarators();  
			break ;
 
    case 243 : // System.out.println("EnterVariable ::=");
		    consumeEnterVariable();  
			break ;
 
    case 244 : // System.out.println("ExitVariableWithInitialization ::=");
		    consumeExitVariableWithInitialization();  
			break ;
 
    case 245 : // System.out.println("ExitVariableWithoutInitialization ::=");
		    consumeExitVariableWithoutInitialization();  
			break ;
 
    case 246 : // System.out.println("ForceNoDiet ::=");
		    consumeForceNoDiet();  
			break ;
 
    case 247 : // System.out.println("RestoreDiet ::=");
		    consumeRestoreDiet();  
			break ;
 
    case 252 : // System.out.println("MethodDeclaration ::= MethodHeader MethodBody");
		    // set to true to consume a method with a body
  consumeMethodDeclaration(true);   
			break ;
 
    case 253 : // System.out.println("AbstractMethodDeclaration ::= MethodHeader SEMICOLON");
		    // set to false to consume a method without body
  consumeMethodDeclaration(false);  
			break ;
 
    case 254 : // System.out.println("MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims");
		    consumeMethodHeader();  
			break ;
 
    case 255 : // System.out.println("MethodPushModifiersHeader ::= MethodPushModifiersHeaderName MethodHeaderParameters");
		    consumeMethodHeader();  
			break ;
 
    case 256 : // System.out.println("MethodPushModifiersHeaderName ::= Modifiers Type PushModifiers...");
		    consumeMethodPushModifiersHeaderName();  
			break ;
 
    case 257 : // System.out.println("MethodPushModifiersHeaderName ::= Type PushModifiers JavaIdentifierNoAround LPAREN");
		    consumeMethodPushModifiersHeaderName();  
			break ;
 
    case 258 : // System.out.println("MethodHeaderName ::= Modifiersopt Type JavaIdentifierNoAround LPAREN");
		    consumeMethodHeaderName();  
			break ;
 
    case 259 : // System.out.println("MethodHeaderParameters ::= FormalParameterListopt RPAREN");
		    consumeMethodHeaderParameters();  
			break ;
 
    case 260 : // System.out.println("MethodHeaderExtendedDims ::= Dimsopt");
		    consumeMethodHeaderExtendedDims();  
			break ;
 
    case 261 : // System.out.println("MethodHeaderThrowsClause ::= throws ClassTypeList");
		    consumeMethodHeaderThrowsClause();  
			break ;
 
    case 262 : // System.out.println("ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters...");
		    consumeConstructorHeader();  
			break ;
 
    case 263 : // System.out.println("ConstructorHeaderName ::= Modifiersopt Identifier LPAREN");
		    consumeConstructorHeaderName();  
			break ;
 
    case 264 : // System.out.println("ConstructorHeaderName ::= Modifiersopt aspect LPAREN");
		    consumeConstructorHeaderName();  
			break ;
 
    case 266 : // System.out.println("FormalParameterList ::= FormalParameterList COMMA FormalParameter");
		    consumeFormalParameterList();  
			break ;
 
    case 267 : // System.out.println("FormalParameter ::= Modifiersopt Type VariableDeclaratorId");
		    // the boolean is used to know if the modifiers should be reset
 	consumeFormalParameter();  
			break ;
 
    case 269 : // System.out.println("ClassTypeList ::= ClassTypeList COMMA ClassTypeElt");
		    consumeClassTypeList();  
			break ;
 
    case 270 : // System.out.println("ClassTypeElt ::= ClassType");
		    consumeClassTypeElt();  
			break ;
 
    case 271 : // System.out.println("MethodBody ::= NestedMethod LBRACE BlockStatementsopt RBRACE");
		    consumeMethodBody();  
			break ;
 
    case 272 : // System.out.println("NestedMethod ::=");
		    consumeNestedMethod();  
			break ;
 
    case 273 : // System.out.println("StaticInitializer ::= StaticOnly Block");
		    consumeStaticInitializer();  
			break ;

    case 274 : // System.out.println("StaticOnly ::= static");
		    consumeStaticOnly();  
			break ;
 
    case 275 : // System.out.println("ConstructorDeclaration ::= ConstructorHeader ConstructorBody");
		    consumeConstructorDeclaration() ;  
			break ;
 
    case 276 : // System.out.println("ConstructorDeclaration ::= ConstructorHeader SEMICOLON");
		    consumeInvalidConstructorDeclaration() ;  
			break ;
 
    case 277 : // System.out.println("ConstructorBody ::= NestedMethod LBRACE ConstructorBlockStatementsopt RBRACE");
		    consumeConstructorBody();  
			break ;
 
    case 280 : // System.out.println("ConstructorBlockStatementsopt ::= ExplicitConstructorInvocation BlockStatements");
		     consumeConstructorBlockStatements();  
			break ;
 
    case 281 : // System.out.println("ExplicitConstructorInvocation ::= this LPAREN ArgumentListopt RPAREN SEMICOLON");
		    consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.This);  
			break ;
 
    case 282 : // System.out.println("ExplicitConstructorInvocation ::= super LPAREN ArgumentListopt RPAREN SEMICOLON");
		    consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.Super);  
			break ;
 
    case 283 : // System.out.println("ExplicitConstructorInvocation ::= Primary DOT super LPAREN ArgumentListopt RPAREN");
		    consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.Super);  
			break ;
 
    case 284 : // System.out.println("ExplicitConstructorInvocation ::= Name DOT super LPAREN ArgumentListopt RPAREN...");
		    consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.Super);  
			break ;
 
    case 285 : // System.out.println("ExplicitConstructorInvocation ::= Primary DOT this LPAREN ArgumentListopt RPAREN...");
		    consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.This);  
			break ;
 
    case 286 : // System.out.println("ExplicitConstructorInvocation ::= Name DOT this LPAREN ArgumentListopt RPAREN...");
		    consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.This);  
			break ;
 
    case 287 : // System.out.println("InterfaceDeclaration ::= InterfaceHeader InterfaceBody");
		    consumeInterfaceDeclaration();  
			break ;
 
    case 288 : // System.out.println("InterfaceHeader ::= InterfaceHeaderName InterfaceHeaderExtendsopt");
		    consumeInterfaceHeader();  
			break ;
 
    case 289 : // System.out.println("InterfaceHeaderName ::= Modifiersopt interface JavaIdentifier");
		    consumeInterfaceHeaderName();  
			break ;
 
    case 291 : // System.out.println("InterfaceHeaderExtends ::= extends InterfaceTypeList");
		    consumeInterfaceHeaderExtends();  
			break ;
 
    case 294 : // System.out.println("InterfaceMemberDeclarations ::= InterfaceMemberDeclarations...");
		    consumeInterfaceMemberDeclarations();  
			break ;
 
    case 295 : // System.out.println("InterfaceMemberDeclaration ::= SEMICOLON");
		    consumeEmptyInterfaceMemberDeclaration();  
			break ;
 
    case 298 : // System.out.println("InterfaceMemberDeclaration ::= InvalidMethodDeclaration");
		    ignoreMethodBody();  
			break ;
 
    case 299 : // System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader ConstructorBody");
		    ignoreInvalidConstructorDeclaration(true);   
			break ;
 
    case 300 : // System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader SEMICOLON");
		    ignoreInvalidConstructorDeclaration(false);   
			break ;
 
    case 306 : // System.out.println("ArrayInitializer ::= LBRACE ,opt RBRACE");
		    consumeEmptyArrayInitializer();  
			break ;
 
    case 307 : // System.out.println("ArrayInitializer ::= LBRACE VariableInitializers RBRACE");
		    consumeArrayInitializer();  
			break ;
 
    case 308 : // System.out.println("ArrayInitializer ::= LBRACE VariableInitializers COMMA RBRACE");
		    consumeArrayInitializer();  
			break ;
 
    case 310 : // System.out.println("VariableInitializers ::= VariableInitializers COMMA VariableInitializer");
		    consumeVariableInitializers();  
			break ;
 
    case 311 : // System.out.println("Block ::= OpenBlock LBRACE BlockStatementsopt RBRACE");
		    consumeBlock();  
			break ;
 
    case 312 : // System.out.println("OpenBlock ::=");
		    consumeOpenBlock() ;  
			break ;
 
    case 314 : // System.out.println("BlockStatements ::= BlockStatements BlockStatement");
		    consumeBlockStatements() ;  
			break ;
 
    case 318 : // System.out.println("BlockStatement ::= InvalidInterfaceDeclaration");
		    ignoreInterfaceDeclaration();  
			break ;
 
    case 319 : // System.out.println("LocalVariableDeclarationStatement ::= LocalVariableDeclaration SEMICOLON");
		    consumeLocalVariableDeclarationStatement();  
			break ;
 
    case 320 : // System.out.println("LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators");
		    consumeLocalVariableDeclaration();  
			break ;
 
    case 321 : // System.out.println("LocalVariableDeclaration ::= Modifiers Type PushModifiers VariableDeclarators");
		    consumeLocalVariableDeclaration();  
			break ;
 
    case 322 : // System.out.println("PushModifiers ::=");
		    consumePushModifiers();  
			break ;
 
    case 346 : // System.out.println("EmptyStatement ::= SEMICOLON");
		    consumeEmptyStatement();  
			break ;
 
    case 347 : // System.out.println("LabeledStatement ::= JavaIdentifier COLON Statement");
		    consumeStatementLabel() ;  
			break ;
 
    case 348 : // System.out.println("LabeledStatementNoShortIf ::= JavaIdentifier COLON StatementNoShortIf");
		    consumeStatementLabel() ;  
			break ;
 
     case 349 : // System.out.println("ExpressionStatement ::= StatementExpression SEMICOLON");
		    consumeExpressionStatement();  
			break ;
 
    case 357 : // System.out.println("IfThenStatement ::= if LPAREN Expression RPAREN Statement");
		    consumeStatementIfNoElse();  
			break ;
 
    case 358 : // System.out.println("IfThenElseStatement ::= if LPAREN Expression RPAREN StatementNoShortIf else...");
		    consumeStatementIfWithElse();  
			break ;
 
    case 359 : // System.out.println("IfThenElseStatementNoShortIf ::= if LPAREN Expression RPAREN StatementNoShortIf...");
		    consumeStatementIfWithElse();  
			break ;
 
    case 360 : // System.out.println("SwitchStatement ::= switch OpenBlock LPAREN Expression RPAREN SwitchBlock");
		    consumeStatementSwitch() ;  
			break ;
 
    case 361 : // System.out.println("SwitchBlock ::= LBRACE RBRACE");
		    consumeEmptySwitchBlock() ;  
			break ;
 
    case 364 : // System.out.println("SwitchBlock ::= LBRACE SwitchBlockStatements SwitchLabels RBRACE");
		    consumeSwitchBlock() ;  
			break ;
 
    case 366 : // System.out.println("SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement");
		    consumeSwitchBlockStatements() ;  
			break ;
 
    case 367 : // System.out.println("SwitchBlockStatement ::= SwitchLabels BlockStatements");
		    consumeSwitchBlockStatement() ;  
			break ;
 
    case 369 : // System.out.println("SwitchLabels ::= SwitchLabels SwitchLabel");
		    consumeSwitchLabels() ;  
			break ;
 
     case 370 : // System.out.println("SwitchLabel ::= case ConstantExpression COLON");
		    consumeCaseLabel();  
			break ;
 
     case 371 : // System.out.println("SwitchLabel ::= default COLON");
		    consumeDefaultLabel();  
			break ;
 
    case 372 : // System.out.println("WhileStatement ::= while LPAREN Expression RPAREN Statement");
		    consumeStatementWhile() ;  
			break ;
 
    case 373 : // System.out.println("WhileStatementNoShortIf ::= while LPAREN Expression RPAREN StatementNoShortIf");
		    consumeStatementWhile() ;  
			break ;
 
    case 374 : // System.out.println("DoStatement ::= do Statement while LPAREN Expression RPAREN SEMICOLON");
		    consumeStatementDo() ;  
			break ;
 
    case 375 : // System.out.println("ForStatement ::= for LPAREN ForInitopt SEMICOLON Expressionopt SEMICOLON...");
		    consumeStatementFor() ;  
			break ;
 
    case 376 : // System.out.println("ForStatementNoShortIf ::= for LPAREN ForInitopt SEMICOLON Expressionopt SEMICOLON");
		    consumeStatementFor() ;  
			break ;
 
    case 377 : // System.out.println("ForInit ::= StatementExpressionList");
		    consumeForInit() ;  
			break ;
 
    case 381 : // System.out.println("StatementExpressionList ::= StatementExpressionList COMMA StatementExpression");
		    consumeStatementExpressionList() ;  
			break ;
 
    case 382 : // System.out.println("AssertStatement ::= assert Expression SEMICOLON");
		    consumeSimpleAssertStatement() ;  
			break ;
 
    case 383 : // System.out.println("AssertStatement ::= assert Expression COLON Expression SEMICOLON");
		    consumeAssertStatement() ;  
			break ;
 
    case 384 : // System.out.println("BreakStatement ::= break SEMICOLON");
		    consumeStatementBreak() ;  
			break ;
 
    case 385 : // System.out.println("BreakStatement ::= break Identifier SEMICOLON");
		    consumeStatementBreakWithLabel() ;  
			break ;
 
    case 386 : // System.out.println("ContinueStatement ::= continue SEMICOLON");
		    consumeStatementContinue() ;  
			break ;
 
    case 387 : // System.out.println("ContinueStatement ::= continue Identifier SEMICOLON");
		    consumeStatementContinueWithLabel() ;  
			break ;
 
    case 388 : // System.out.println("ReturnStatement ::= return Expressionopt SEMICOLON");
		    consumeStatementReturn() ;  
			break ;
 
    case 389 : // System.out.println("ThrowStatement ::= throw Expression SEMICOLON");
		    consumeStatementThrow();
 
			break ;
 
    case 390 : // System.out.println("SynchronizedStatement ::= OnlySynchronized LPAREN Expression RPAREN Block");
		    consumeStatementSynchronized();  
			break ;
 
    case 391 : // System.out.println("OnlySynchronized ::= synchronized");
		    consumeOnlySynchronized();  
			break ;
 
    case 392 : // System.out.println("TryStatement ::= try Block Catches");
		    consumeStatementTry(false);  
			break ;
 
    case 393 : // System.out.println("TryStatement ::= try Block Catchesopt Finally");
		    consumeStatementTry(true);  
			break ;
 
    case 395 : // System.out.println("Catches ::= Catches CatchClause");
		    consumeCatches();  
			break ;
 
    case 396 : // System.out.println("CatchClause ::= catch LPAREN FormalParameter RPAREN Block");
		    consumeStatementCatch() ;  
			break ;
 
    case 398 : // System.out.println("PushLPAREN ::= LPAREN");
		    consumeLeftParen();  
			break ;
 
    case 399 : // System.out.println("PushRPAREN ::= RPAREN");
		    consumeRightParen();  
			break ;
 
    case 404 : // System.out.println("PrimaryNoNewArray ::= this");
		    consumePrimaryNoNewArrayThis();  
			break ;
 
    case 405 : // System.out.println("PrimaryNoNewArray ::= PushLPAREN Expression PushRPAREN");
		    consumePrimaryNoNewArray();  
			break ;
 
    case 408 : // System.out.println("PrimaryNoNewArray ::= Name DOT this");
		    consumePrimaryNoNewArrayNameThis();  
			break ;
 
    case 409 : // System.out.println("PrimaryNoNewArray ::= Name DOT super");
		    consumePrimaryNoNewArrayNameSuper();  
			break ;
 
    case 410 : // System.out.println("PrimaryNoNewArray ::= Name DOT class");
		    consumePrimaryNoNewArrayName();  
			break ;
 
    case 411 : // System.out.println("PrimaryNoNewArray ::= ArrayType DOT class");
		    consumePrimaryNoNewArrayArrayType();  
			break ;
 
    case 412 : // System.out.println("PrimaryNoNewArray ::= PrimitiveType DOT class");
		    consumePrimaryNoNewArrayPrimitiveType();  
			break ;
 
    case 415 : // System.out.println("AllocationHeader ::= new ClassType LPAREN ArgumentListopt RPAREN");
		    consumeAllocationHeader();  
			break ;
 
    case 416 : // System.out.println("ClassInstanceCreationExpression ::= new ClassType LPAREN ArgumentListopt RPAREN...");
		    consumeClassInstanceCreationExpression();  
			break ;
 
    case 417 : // System.out.println("ClassInstanceCreationExpression ::= Primary DOT new SimpleName LPAREN...");
		    consumeClassInstanceCreationExpressionQualified() ;  
			break ;
 
    case 418 : // System.out.println("ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName new...");
		    consumeClassInstanceCreationExpressionQualified() ;  
			break ;
 
    case 419 : // System.out.println("ClassInstanceCreationExpressionName ::= Name DOT");
		    consumeClassInstanceCreationExpressionName() ;  
			break ;
 
    case 420 : // System.out.println("ClassBodyopt ::=");
		    consumeClassBodyopt();  
			break ;
 
    case 422 : // System.out.println("EnterAnonymousClassBody ::=");
		    consumeEnterAnonymousClassBody();  
			break ;
 
    case 424 : // System.out.println("ArgumentList ::= ArgumentList COMMA Expression");
		    consumeArgumentList();  
			break ;
 
    case 425 : // System.out.println("ArrayCreationWithoutArrayInitializer ::= new PrimitiveType DimWithOrWithOutExprs");
		    consumeArrayCreationExpressionWithoutInitializer();  
			break ;
 
    case 426 : // System.out.println("ArrayCreationWithArrayInitializer ::= new PrimitiveType DimWithOrWithOutExprs...");
		    consumeArrayCreationExpressionWithInitializer();  
			break ;
 
    case 427 : // System.out.println("ArrayCreationWithoutArrayInitializer ::= new ClassOrInterfaceType...");
		    consumeArrayCreationExpressionWithoutInitializer();  
			break ;
 
    case 428 : // System.out.println("ArrayCreationWithArrayInitializer ::= new ClassOrInterfaceType...");
		    consumeArrayCreationExpressionWithInitializer();  
			break ;
 
    case 430 : // System.out.println("DimWithOrWithOutExprs ::= DimWithOrWithOutExprs DimWithOrWithOutExpr");
		    consumeDimWithOrWithOutExprs();  
			break ;
 
     case 432 : // System.out.println("DimWithOrWithOutExpr ::= LBRACKET RBRACKET");
		    consumeDimWithOrWithOutExpr();  
			break ;
 
     case 433 : // System.out.println("Dims ::= DimsLoop");
		    consumeDims();  
			break ;
 
     case 436 : // System.out.println("OneDimLoop ::= LBRACKET RBRACKET");
		    consumeOneDimLoop();  
			break ;
 
    case 437 : // System.out.println("FieldAccess ::= Primary DOT JavaIdentifier");
		    consumeFieldAccess(false);  
			break ;
 
    case 438 : // System.out.println("FieldAccess ::= super DOT JavaIdentifier");
		    consumeFieldAccess(true);  
			break ;
 
    case 439 : // System.out.println("MethodInvocation ::= NameOrAj LPAREN ArgumentListopt RPAREN");
		    consumeMethodInvocationName();  
			break ;
 
    case 440 : // System.out.println("MethodInvocation ::= Primary DOT JavaIdentifier LPAREN ArgumentListopt RPAREN");
		    consumeMethodInvocationPrimary();  
			break ;
 
    case 441 : // System.out.println("MethodInvocation ::= super DOT JavaIdentifier LPAREN ArgumentListopt RPAREN");
		    consumeMethodInvocationSuper();  
			break ;
 
    case 442 : // System.out.println("ArrayAccess ::= Name LBRACKET Expression RBRACKET");
		    consumeArrayAccess(true);  
			break ;
 
    case 443 : // System.out.println("ArrayAccess ::= PrimaryNoNewArray LBRACKET Expression RBRACKET");
		    consumeArrayAccess(false);  
			break ;
 
    case 444 : // System.out.println("ArrayAccess ::= ArrayCreationWithArrayInitializer LBRACKET Expression RBRACKET");
		    consumeArrayAccess(false);  
			break ;
 
    case 446 : // System.out.println("PostfixExpression ::= NameOrAj");
		    consumePostfixExpression();  
			break ;
 
    case 449 : // System.out.println("PostIncrementExpression ::= PostfixExpression PLUS_PLUS");
		    consumeUnaryExpression(OperatorExpression.PLUS,true);  
			break ;
 
    case 450 : // System.out.println("PostDecrementExpression ::= PostfixExpression MINUS_MINUS");
		    consumeUnaryExpression(OperatorExpression.MINUS,true);  
			break ;
 
    case 451 : // System.out.println("PushPosition ::=");
		    consumePushPosition();  
			break ;
 
    case 454 : // System.out.println("UnaryExpression ::= PLUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.PLUS);  
			break ;
 
    case 455 : // System.out.println("UnaryExpression ::= MINUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.MINUS);  
			break ;
 
    case 457 : // System.out.println("PreIncrementExpression ::= PLUS_PLUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.PLUS,false);  
			break ;
 
    case 458 : // System.out.println("PreDecrementExpression ::= MINUS_MINUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.MINUS,false);  
			break ;
 
    case 460 : // System.out.println("UnaryExpressionNotPlusMinus ::= TWIDDLE PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.TWIDDLE);  
			break ;
 
    case 461 : // System.out.println("UnaryExpressionNotPlusMinus ::= NOT PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.NOT);  
			break ;
 
    case 463 : // System.out.println("CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression");
		    consumeCastExpression();  
			break ;
 
    case 464 : // System.out.println("CastExpression ::= PushLPAREN Name Dims PushRPAREN InsideCastExpression...");
		    consumeCastExpression();  
			break ;
 
    case 465 : // System.out.println("CastExpression ::= PushLPAREN Expression PushRPAREN InsideCastExpressionLL1...");
		    consumeCastExpressionLL1();  
			break ;
 
    case 466 : // System.out.println("InsideCastExpression ::=");
		    consumeInsideCastExpression();  
			break ;
 
    case 467 : // System.out.println("InsideCastExpressionLL1 ::=");
		    consumeInsideCastExpressionLL1();  
			break ;
 
    case 469 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression MULTIPLY UnaryExpression");
		    consumeBinaryExpression(OperatorExpression.MULTIPLY);  
			break ;
 
    case 470 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression DIVIDE UnaryExpression");
		    consumeBinaryExpression(OperatorExpression.DIVIDE);  
			break ;
 
    case 471 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression REMAINDER UnaryExpression");
		    consumeBinaryExpression(OperatorExpression.REMAINDER);  
			break ;
 
    case 473 : // System.out.println("AdditiveExpression ::= AdditiveExpression PLUS MultiplicativeExpression");
		    consumeBinaryExpression(OperatorExpression.PLUS);  
			break ;
 
    case 474 : // System.out.println("AdditiveExpression ::= AdditiveExpression MINUS MultiplicativeExpression");
		    consumeBinaryExpression(OperatorExpression.MINUS);  
			break ;
 
    case 476 : // System.out.println("ShiftExpression ::= ShiftExpression LEFT_SHIFT AdditiveExpression");
		    consumeBinaryExpression(OperatorExpression.LEFT_SHIFT);  
			break ;
 
    case 477 : // System.out.println("ShiftExpression ::= ShiftExpression RIGHT_SHIFT AdditiveExpression");
		    consumeBinaryExpression(OperatorExpression.RIGHT_SHIFT);  
			break ;
 
    case 478 : // System.out.println("ShiftExpression ::= ShiftExpression UNSIGNED_RIGHT_SHIFT AdditiveExpression");
		    consumeBinaryExpression(OperatorExpression.UNSIGNED_RIGHT_SHIFT);  
			break ;
 
    case 480 : // System.out.println("RelationalExpression ::= RelationalExpression LESS ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.LESS);  
			break ;
 
    case 481 : // System.out.println("RelationalExpression ::= RelationalExpression GREATER ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.GREATER);  
			break ;
 
    case 482 : // System.out.println("RelationalExpression ::= RelationalExpression LESS_EQUAL ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.LESS_EQUAL);  
			break ;
 
    case 483 : // System.out.println("RelationalExpression ::= RelationalExpression GREATER_EQUAL ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.GREATER_EQUAL);  
			break ;
 
    case 484 : // System.out.println("RelationalExpression ::= RelationalExpression instanceof ReferenceType");
		    consumeInstanceOfExpression(OperatorExpression.INSTANCEOF);  
			break ;
 
    case 486 : // System.out.println("EqualityExpression ::= EqualityExpression EQUAL_EQUAL RelationalExpression");
		    consumeEqualityExpression(OperatorExpression.EQUAL_EQUAL);  
			break ;
 
    case 487 : // System.out.println("EqualityExpression ::= EqualityExpression NOT_EQUAL RelationalExpression");
		    consumeEqualityExpression(OperatorExpression.NOT_EQUAL);  
			break ;
 
    case 489 : // System.out.println("AndExpression ::= AndExpression AND EqualityExpression");
		    consumeBinaryExpression(OperatorExpression.AND);  
			break ;
 
    case 491 : // System.out.println("ExclusiveOrExpression ::= ExclusiveOrExpression XOR AndExpression");
		    consumeBinaryExpression(OperatorExpression.XOR);  
			break ;
 
    case 493 : // System.out.println("InclusiveOrExpression ::= InclusiveOrExpression OR ExclusiveOrExpression");
		    consumeBinaryExpression(OperatorExpression.OR);  
			break ;
 
    case 495 : // System.out.println("ConditionalAndExpression ::= ConditionalAndExpression AND_AND InclusiveOrExpression");
		    consumeBinaryExpression(OperatorExpression.AND_AND);  
			break ;
 
    case 497 : // System.out.println("ConditionalOrExpression ::= ConditionalOrExpression OR_OR ConditionalAndExpression");
		    consumeBinaryExpression(OperatorExpression.OR_OR);  
			break ;
 
    case 499 : // System.out.println("ConditionalExpression ::= ConditionalOrExpression QUESTION Expression COLON...");
		    consumeConditionalExpression(OperatorExpression.QUESTIONCOLON) ;  
			break ;
 
    case 502 : // System.out.println("Assignment ::= PostfixExpression AssignmentOperator AssignmentExpression");
		    consumeAssignment();  
			break ;
 
    case 504 : // System.out.println("Assignment ::= InvalidArrayInitializerAssignement");
		    ignoreExpressionAssignment(); 
			break ;
 
    case 505 : // System.out.println("AssignmentOperator ::= EQUAL");
		    consumeAssignmentOperator(EQUAL);  
			break ;
 
    case 506 : // System.out.println("AssignmentOperator ::= MULTIPLY_EQUAL");
		    consumeAssignmentOperator(MULTIPLY);  
			break ;
 
    case 507 : // System.out.println("AssignmentOperator ::= DIVIDE_EQUAL");
		    consumeAssignmentOperator(DIVIDE);  
			break ;
 
    case 508 : // System.out.println("AssignmentOperator ::= REMAINDER_EQUAL");
		    consumeAssignmentOperator(REMAINDER);  
			break ;
 
    case 509 : // System.out.println("AssignmentOperator ::= PLUS_EQUAL");
		    consumeAssignmentOperator(PLUS);  
			break ;
 
    case 510 : // System.out.println("AssignmentOperator ::= MINUS_EQUAL");
		    consumeAssignmentOperator(MINUS);  
			break ;
 
    case 511 : // System.out.println("AssignmentOperator ::= LEFT_SHIFT_EQUAL");
		    consumeAssignmentOperator(LEFT_SHIFT);  
			break ;
 
    case 512 : // System.out.println("AssignmentOperator ::= RIGHT_SHIFT_EQUAL");
		    consumeAssignmentOperator(RIGHT_SHIFT);  
			break ;
 
    case 513 : // System.out.println("AssignmentOperator ::= UNSIGNED_RIGHT_SHIFT_EQUAL");
		    consumeAssignmentOperator(UNSIGNED_RIGHT_SHIFT);  
			break ;
 
    case 514 : // System.out.println("AssignmentOperator ::= AND_EQUAL");
		    consumeAssignmentOperator(AND);  
			break ;
 
    case 515 : // System.out.println("AssignmentOperator ::= XOR_EQUAL");
		    consumeAssignmentOperator(XOR);  
			break ;
 
    case 516 : // System.out.println("AssignmentOperator ::= OR_EQUAL");
		    consumeAssignmentOperator(OR);  
			break ;
 
    case 523 : // System.out.println("Expressionopt ::=");
		    consumeEmptyExpression();  
			break ;
 
    case 527 : // System.out.println("ImportDeclarationsopt ::=");
		    consumeEmptyImportDeclarationsopt();  
			break ;
 
    case 528 : // System.out.println("ImportDeclarationsopt ::= ImportDeclarations");
		    consumeImportDeclarationsopt();  
			break ;
 
    case 529 : // System.out.println("TypeDeclarationsopt ::=");
		    consumeEmptyTypeDeclarationsopt();  
			break ;
 
    case 530 : // System.out.println("TypeDeclarationsopt ::= TypeDeclarations");
		    consumeTypeDeclarationsopt();  
			break ;
 
    case 531 : // System.out.println("ClassBodyDeclarationsopt ::=");
		    consumeEmptyClassBodyDeclarationsopt();  
			break ;
 
    case 532 : // System.out.println("ClassBodyDeclarationsopt ::= NestedType ClassBodyDeclarations");
		    consumeClassBodyDeclarationsopt();  
			break ;
 
     case 533 : // System.out.println("Modifiersopt ::=");
		    consumeDefaultModifiers();  
			break ;
 
    case 534 : // System.out.println("Modifiersopt ::= Modifiers");
		    consumeModifiers();  
			break ;
 
    case 535 : // System.out.println("BlockStatementsopt ::=");
		    consumeEmptyBlockStatementsopt();  
			break ;
 
     case 537 : // System.out.println("Dimsopt ::=");
		    consumeEmptyDimsopt();  
			break ;
 
     case 539 : // System.out.println("ArgumentListopt ::=");
		    consumeEmptyArgumentListopt();  
			break ;
 
    case 543 : // System.out.println("FormalParameterListopt ::=");
		    consumeFormalParameterListopt();  
			break ;
 
     case 547 : // System.out.println("InterfaceMemberDeclarationsopt ::=");
		    consumeEmptyInterfaceMemberDeclarationsopt();  
			break ;
 
     case 548 : // System.out.println("InterfaceMemberDeclarationsopt ::= NestedType InterfaceMemberDeclarations");
		    consumeInterfaceMemberDeclarationsopt();  
			break ;
 
    case 549 : // System.out.println("NestedType ::=");
		    consumeNestedType();  
			break ;

     case 550 : // System.out.println("ForInitopt ::=");
		    consumeEmptyForInitopt();  
			break ;
 
     case 552 : // System.out.println("ForUpdateopt ::=");
		    consumeEmptyForUpdateopt();  
			break ;
 
     case 556 : // System.out.println("Catchesopt ::=");
		    consumeEmptyCatchesopt();  
			break ;
 
	}
} 
protected void consumeSimpleAssertStatement() {
	super.consumeSimpleAssertStatement();
}
	public AjParser(
		ProblemReporter problemReporter,
		boolean optimizeStringLiterals,
		boolean assertMode) {
		super(problemReporter, optimizeStringLiterals, assertMode);
	}

}
