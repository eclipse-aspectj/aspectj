/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*; 
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*; 
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

import java.io.*;
import java.util.ArrayList;

/** 
 * AspectJ - many changes to make this non-static or more accessible to 
 * increase extensibility.  The key extensibility challenge remaining appears
 * to be the static dependencies on ParserBasicInformation and ITerminalSymbols.
 * 
 * These changes from static to non-static might have performance implications.
 * They have not yet been benchmarked in any serious way.
 */
public class Parser implements BindingIds, ParserBasicInformation, ITerminalSymbols, CompilerModifiers, OperatorIds, TypeIds {
	protected ProblemReporter problemReporter;
	public int firstToken ; // handle for multiple parsing goals
	public int lastAct ; //handle for multiple parsing goals
	protected ReferenceContext referenceContext;
	public int currentToken;
	private int synchronizedBlockSourceStart;

	//error recovery management
	protected int lastCheckPoint;
	protected RecoveredElement currentElement;
	public static boolean VERBOSE_RECOVERY = false;
	protected boolean restartRecovery;
	protected int listLength; // for recovering some incomplete list (interfaces, throws or parameters)
	protected boolean hasReportedError;
	protected int recoveredStaticInitializerStart;
	protected int lastIgnoredToken, nextIgnoredToken;
	protected int lastErrorEndPosition;
		
		
	protected int currentTokenStart;
	// 1.4 feature
	protected boolean assertMode = false;
	
	//internal data for the automat 
	protected final static int StackIncrement = 255;
	protected int stateStackTop;
	protected int[] stack = new int[StackIncrement];
	//scanner token 
	public Scanner scanner;
	//ast stack
	final static int AstStackIncrement = 100;
	protected int astPtr;
	protected AstNode[] astStack = new AstNode[AstStackIncrement];
	protected int astLengthPtr;
	protected int[] astLengthStack;
	public CompilationUnitDeclaration compilationUnit; /*the result from parse()*/
	AstNode [] noAstNodes = new AstNode[AstStackIncrement];
	//expression stack
	final static int ExpressionStackIncrement = 100;
	protected int expressionPtr;
	protected Expression[] expressionStack = new Expression[ExpressionStackIncrement];
	protected int expressionLengthPtr;
	protected int[] expressionLengthStack;
	Expression [] noExpressions = new Expression[ExpressionStackIncrement];
	//identifiers stacks 
	protected int identifierPtr;
	protected char[][] identifierStack;
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;
	//positions , dimensions , .... (what ever is int) ..... stack
	protected int intPtr;
	protected int[] intStack;
	protected int endPosition; //accurate only when used ! (the start position is pushed into intStack while the end the current one)
	protected int endStatementPosition;
	protected int lParenPos,rParenPos; //accurate only when used !
	//modifiers dimensions nestedType etc.......
	protected boolean optimizeStringLiterals =true;
	protected int modifiers;
	protected int modifiersSourceStart;
	protected int nestedType, dimensions;
	protected int[] nestedMethod; //the ptr is nestedType
	protected int[] realBlockStack;
	protected int realBlockPtr;
	protected boolean diet = false; //tells the scanner to jump over some parts of the code/expressions like method bodies
	protected int dietInt = 0; // if > 0 force the none-diet-parsing mode (even if diet if requested) [field parsing with anonymous inner classes...]
	protected int[] variablesCounter;
	
	protected final static String FILEPREFIX = "parser"; //$NON-NLS-1$
	protected static final String UNEXPECTED_EOF = "Unexpected End Of File" ; //$NON-NLS-1$
	
	//===DATA===DATA===DATA===DATA===DATA===DATA===//
	
	
    public final static byte rhs[] = {0,
            2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
            2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            2,2,1,1,1,1,3,4,0,1,2,1,1,1,1,
            1,1,1,1,1,5,1,2,1,2,2,2,1,1,2,
            2,2,4,1,1,1,1,2,1,1,1,1,1,1,1,
            1,1,1,1,2,3,3,2,2,1,3,1,3,1,2,
            1,1,1,3,0,3,1,1,1,1,1,1,1,4,1,
            3,3,7,0,0,0,0,0,2,1,1,1,2,2,4,
            4,5,4,4,2,1,2,3,3,1,3,3,1,3,1,
            4,0,2,1,2,2,4,1,1,2,5,5,7,7,7,
            7,2,2,3,2,2,3,1,2,1,2,1,1,2,2,
            1,1,1,1,1,3,3,4,1,3,4,0,1,2,1,
            1,1,1,2,3,4,0,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,3,3,2,1,1,1,1,1,1,1,5,7,7,6,
            2,3,3,4,1,2,2,1,2,3,2,5,5,7,9,
            9,1,1,1,1,3,3,5,2,3,2,3,3,3,5,
            1,3,4,1,2,5,2,1,1,1,1,1,1,3,1,
            1,3,3,3,3,3,1,1,5,6,8,7,2,0,2,
            0,1,3,4,4,1,2,3,2,1,1,2,2,3,3,
            4,6,6,4,4,1,1,1,1,2,2,0,1,1,3,
            3,1,3,3,1,3,3,1,5,5,4,1,3,3,3,
            1,3,3,1,3,3,3,1,3,3,3,3,3,1,3,
            3,1,3,1,3,1,3,1,3,1,3,1,5,1,1,
            3,3,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,0,1,0,1,0,1,0,1,0,1,
            0,1,0,2,0,1,0,1,0,1,0,1,0,1,0,
            1,0,1,0,2,0,0,1,0,1,0,1,0,1,0,
            1
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
            "Type",
            "PrimitiveType",
            "ReferenceType",
            "ClassOrInterfaceType",
            "ArrayType",
            "Name",
            "Dims",
            "ClassType",
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
            "ClassBody",
            "ClassHeaderName",
            "InterfaceTypeList",
            "InterfaceType",
            "ClassBodyDeclarations",
            "Block",
            "VariableDeclarators",
            "VariableDeclarator",
            "VariableDeclaratorId",
            "VariableInitializer",
            "ArrayInitializer",
            "MethodHeaderName",
            "MethodHeaderParameters",
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
            "InterfaceMemberDeclaration",
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
            "FieldAccess",
            "ArrayAccess",
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
            "LeftHandSide",
            "AssignmentOperator"
    };
    
    
	public  static short check_tableStatic[] = null;
	public  static char lhsStatic[] =  null;
	public  static char actionStatic[] = lhsStatic;

	public byte rhsInst[];	

	public char[] asb;
	public char[] asr;
	public char[] symbol_index;
	public String[] nameInst;
	
	public short[] check_table;
	public char[] lhs;
	public char[] action;

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
			initTables(Parser.class);
		} catch(java.io.IOException ex){
			throw new ExceptionInInitializerError(ex.getMessage());
		}
	}

	public static final int RoundBracket = 0;
	public static final int SquareBracket = 1;
	public static final int CurlyBracket = 2;
	public static final int BracketKinds = 3;

public Parser(ProblemReporter problemReporter, boolean optimizeStringLiterals, boolean assertMode) {
	initData();
		
	this.problemReporter = problemReporter;
	this.optimizeStringLiterals = optimizeStringLiterals;
	this.assertMode = assertMode;
	this.initializeScanner();
	astLengthStack = new int[50];
	expressionLengthStack = new int[30];
	intStack = new int[50];
	identifierStack = new char[30][];
	identifierLengthStack = new int[30];
	nestedMethod = new int[30];
	realBlockStack = new int[30];
	identifierPositionStack = new long[30];
	variablesCounter = new int[30];
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void adjustInterfaceModifiers() {
	intStack[intPtr - 1] |= AccInterface;
}
public final void arrayInitializer(int length) {
	//length is the size of the array Initializer
	//expressionPtr points on the last elt of the arrayInitializer
	//i.e. it has not been decremented yet.

	ArrayInitializer ai = new ArrayInitializer();
	if (length != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, ai.expressions = new Expression[length], 0, length);
	}
	pushOnExpressionStack(ai);
	//positionning
	ai.sourceEnd = endStatementPosition;
	int searchPosition = length == 0 ? endPosition : ai.expressions[0].sourceStart;
	try {
		//does not work with comments(that contain '{') nor '{' describes as a unicode....		
		while (scanner.source[--searchPosition] != '{') {
		}
	} catch (IndexOutOfBoundsException ex) {
		//should never occur (except for strange cases like whose describe above)
		searchPosition = (length == 0 ? endPosition : ai.expressions[0].sourceStart) - 1;
	}
	ai.sourceStart = searchPosition;
}
protected int asi(int state) {

	return asb[original_state(state)];
}
protected void blockReal() {
	// See consumeLocalVariableDeclarationStatement in case of change: duplicated code
	// increment the amount of declared variables for this block
	realBlockStack[realBlockPtr]++;
}
private final static void buildFileFor(String filename, String tag, String[] tokens, boolean isShort) throws java.io.IOException {

	//transform the String tokens into chars before dumping then into file

	int i = 0;
	//read upto the tag
	while (!tokens[i++].equals(tag)) {}
	//read upto the }
	char[] chars = new char[tokens.length]; //can't be bigger
	int ic = 0;
	String token;
	while (!(token = tokens[i++]).equals("}")) { //$NON-NLS-1$
		int c = Integer.parseInt(token);
		if (isShort)
			c += 32768;
		chars[ic++] = (char) c;
	}

	//resize
	System.arraycopy(chars, 0, chars = new char[ic], 0, ic);

	buildFileForTable(filename, chars);
}
private final static void buildFileForTable(String filename, char[] chars) throws java.io.IOException {

	byte[] bytes = new byte[chars.length * 2];
	for (int i = 0; i < chars.length; i++) {
		bytes[2 * i] = (byte) (chars[i] >>> 8);
		bytes[2 * i + 1] = (byte) (chars[i] & 0xFF);
	}

	java.io.FileOutputStream stream = new java.io.FileOutputStream(filename);
	stream.write(bytes);
	stream.close();
	System.out.println(filename + " creation complete"); //$NON-NLS-1$
}
public final static void buildFilesFromLPG(String dataFilename)	throws java.io.IOException {

	//RUN THIS METHOD TO GENERATE PARSER*.RSC FILES

	//build from the lpg javadcl.java files that represents the parser tables
	//lhs check_table asb asr symbol_index

	//[org.eclipse.jdt.internal.compiler.parser.Parser.buildFilesFromLPG("d:/leapfrog/grammar/javadcl.java")]

	char[] contents = new char[] {};
	try {
		contents = Util.getFileCharContent(new File(dataFilename), null);
	} catch (IOException ex) {
		System.out.println(Util.bind("parser.incorrectPath")); //$NON-NLS-1$
		return;
	}
	java.util.StringTokenizer st = 
		new java.util.StringTokenizer(new String(contents), " \t\n\r[]={,;");  //$NON-NLS-1$
	String[] tokens = new String[st.countTokens()];
	int i = 0;
	while (st.hasMoreTokens()) {
		tokens[i++] = st.nextToken();
	}
	final String prefix = FILEPREFIX;
	i = 0;
	buildFileFor(prefix + (++i) + ".rsc", "lhs", tokens, false); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileFor(prefix + (++i) + ".rsc", "check_table", tokens, true); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileFor(prefix + (++i) + ".rsc", "asb", tokens, false); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileFor(prefix + (++i) + ".rsc", "asr", tokens, false); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileFor(prefix + (++i) + ".rsc", "symbol_index", tokens, false); //$NON-NLS-2$ //$NON-NLS-1$
	System.out.println(Util.bind("parser.moveFiles")); //$NON-NLS-1$
}
/*
 * Build initial recovery state.
 * Recovery state is inferred from the current state of the parser (reduced node stack).
 */
public RecoveredElement buildInitialRecoveryState(){

	/* initialize recovery by retrieving available reduced nodes 
	 * also rebuild bracket balance 
	 */
	lastCheckPoint = 0;

	RecoveredElement element = null;
	if (referenceContext instanceof CompilationUnitDeclaration){
		element = new RecoveredUnit(compilationUnit, 0, this);
		
		/* ignore current stack state, since restarting from the beginnning 
		   since could not trust simple brace count */
		if (true){ // experimenting restart recovery from scratch
			compilationUnit.currentPackage = null;
			compilationUnit.imports = null;
			compilationUnit.types = null;
			currentToken = 0;
			listLength = 0;
			return element;
		}
		if (compilationUnit.currentPackage != null){
			lastCheckPoint = compilationUnit.currentPackage.declarationSourceEnd+1;
		}
		if (compilationUnit.imports != null){
			lastCheckPoint = compilationUnit.imports[compilationUnit.imports.length -1].declarationSourceEnd+1;		
		}
	} else {
		if (referenceContext instanceof AbstractMethodDeclaration){
			element = new RecoveredMethod((AbstractMethodDeclaration) referenceContext, null, 0, this);
			lastCheckPoint = ((AbstractMethodDeclaration) referenceContext).bodyStart;
		} else {
			/* Initializer bodies are parsed in the context of the type declaration, we must thus search it inside */
			if (referenceContext instanceof TypeDeclaration){
				TypeDeclaration type = (TypeDeclaration) referenceContext;
				for (int i = 0; i < type.fields.length; i++){
					FieldDeclaration field = type.fields[i];					
					if (!field.isField()
						&& field.declarationSourceStart <= scanner.initialPosition
						&& scanner.initialPosition <= field.declarationSourceEnd
						&& scanner.eofPosition <= field.declarationSourceEnd+1){
						element = new RecoveredInitializer((Initializer) field, null, 1, this);
						lastCheckPoint = field.declarationSourceStart;					
						break;
					}
				}
			} 
		}
	}

	if (element == null) return element;
	
	for(int i = 0; i <= astPtr; i++){
		AstNode node = astStack[i];
		if (node instanceof AbstractMethodDeclaration){
			AbstractMethodDeclaration method = (AbstractMethodDeclaration) node;
			if (method.declarationSourceEnd == 0){
				element = element.add(method, 0);
				lastCheckPoint = method.bodyStart;
			} else {
				element = element.add(method, 0);
				lastCheckPoint = method.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof Initializer){
			Initializer initializer = (Initializer) node;
			if (initializer.declarationSourceEnd == 0){
				element = element.add(initializer, 1);
				lastCheckPoint = initializer.bodyStart;				
			} else {
				element = element.add(initializer, 0);
				lastCheckPoint = initializer.declarationSourceEnd + 1;
			}
			continue;
		}		
		if (node instanceof FieldDeclaration){
			FieldDeclaration field = (FieldDeclaration) node;
			if (field.declarationSourceEnd == 0){
				element = element.add(field, 0);
				if (field.initialization == null){
					lastCheckPoint = field.sourceEnd + 1;
				} else {
					lastCheckPoint = field.initialization.sourceEnd + 1;
				}
			} else {
				element = element.add(field, 0);
				lastCheckPoint = field.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof TypeDeclaration){
			TypeDeclaration type = (TypeDeclaration) node;
			if (type.declarationSourceEnd == 0){
				element = element.add(type, 0);	
				lastCheckPoint = type.bodyStart;
			} else {
				element = element.add(type, 0);				
				lastCheckPoint = type.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof ImportReference){
			ImportReference importRef = (ImportReference) node;
			element = element.add(importRef, 0);
			lastCheckPoint = importRef.declarationSourceEnd + 1;
		}
	}
	return element;
}
protected short check(int i) {
	return check_table[i - (NUM_RULES + 1)];
}
/*
 * Reconsider the entire source looking for inconsistencies in {} () []
 */
public boolean checkAndReportBracketAnomalies(ProblemReporter problemReporter) {

	scanner.wasAcr = false;
	boolean anomaliesDetected = false;	
	try {
		char[] source = scanner.source;
		int[] leftCount = {0, 0, 0};
		int[] rightCount = {0, 0, 0};
		int[] depths = {0, 0, 0};
		int[][] leftPositions = new int[][] {new int[10], new int[10], new int[10]};
		int[][] leftDepths = new int[][] {new int[10], new int[10], new int[10]};
		int[][] rightPositions = new int[][] {new int[10], new int[10], new int[10]};
		int[][] rightDepths = new int[][] {new int[10], new int[10], new int[10]};
		scanner.currentPosition = scanner.initialPosition; //starting point (first-zero-based char)
		while (scanner.currentPosition < scanner.eofPosition) { //loop for jumping over comments
			try {
				// ---------Consume white space and handles startPosition---------
				boolean isWhiteSpace;
				do {
					scanner.startPosition = scanner.currentPosition;
					if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
						isWhiteSpace = scanner.jumpOverUnicodeWhiteSpace();
					} else {
						if (scanner.recordLineSeparator && ((scanner.currentCharacter == '\r') || (scanner.currentCharacter == '\n'))) {
							if (scanner.lineEnds[scanner.linePtr] < scanner.startPosition) {
								// only record line positions we have not recorded yet
								scanner.pushLineSeparator();
							}
						}
						isWhiteSpace = Character.isWhitespace(scanner.currentCharacter);
					}
				} while (isWhiteSpace && (scanner.currentPosition < scanner.eofPosition));

				// -------consume token until } is found---------

				switch (scanner.currentCharacter) {
					case '{' :
						{
							int index = leftCount[CurlyBracket] ++;
							if (index == leftPositions[CurlyBracket].length) {
								System.arraycopy(leftPositions[CurlyBracket], 0, (leftPositions[CurlyBracket] = new int[index * 2]), 0, index);
								System.arraycopy(leftDepths[CurlyBracket], 0, (leftDepths[CurlyBracket] = new int[index * 2]), 0, index);
							}
							leftPositions[CurlyBracket][index] = scanner.startPosition;
							leftDepths[CurlyBracket][index] = depths[CurlyBracket] ++;
						}
						break;
					case '}' :
						{
							int index = rightCount[CurlyBracket] ++;
							if (index == rightPositions[CurlyBracket].length) {
								System.arraycopy(rightPositions[CurlyBracket], 0, (rightPositions[CurlyBracket] = new int[index * 2]), 0, index);
								System.arraycopy(rightDepths[CurlyBracket], 0, (rightDepths[CurlyBracket] = new int[index * 2]), 0, index);
							}
							rightPositions[CurlyBracket][index] = scanner.startPosition;
							rightDepths[CurlyBracket][index] = --depths[CurlyBracket];
						}
						break;
					case '(' :
						{
							int index = leftCount[RoundBracket] ++;
							if (index == leftPositions[RoundBracket].length) {
								System.arraycopy(leftPositions[RoundBracket], 0, (leftPositions[RoundBracket] = new int[index * 2]), 0, index);
								System.arraycopy(leftDepths[RoundBracket], 0, (leftDepths[RoundBracket] = new int[index * 2]), 0, index);
							}
							leftPositions[RoundBracket][index] = scanner.startPosition;
							leftDepths[RoundBracket][index] = depths[RoundBracket] ++;
						}
						break;
					case ')' :
						{
							int index = rightCount[RoundBracket] ++;
							if (index == rightPositions[RoundBracket].length) {
								System.arraycopy(rightPositions[RoundBracket], 0, (rightPositions[RoundBracket] = new int[index * 2]), 0, index);
								System.arraycopy(rightDepths[RoundBracket], 0, (rightDepths[RoundBracket] = new int[index * 2]), 0, index);
							}
							rightPositions[RoundBracket][index] = scanner.startPosition;
							rightDepths[RoundBracket][index] = --depths[RoundBracket];
						}
						break;
					case '[' :
						{
							int index = leftCount[SquareBracket] ++;
							if (index == leftPositions[SquareBracket].length) {
								System.arraycopy(leftPositions[SquareBracket], 0, (leftPositions[SquareBracket] = new int[index * 2]), 0, index);
								System.arraycopy(leftDepths[SquareBracket], 0, (leftDepths[SquareBracket] = new int[index * 2]), 0, index);
							}
							leftPositions[SquareBracket][index] = scanner.startPosition;
							leftDepths[SquareBracket][index] = depths[SquareBracket] ++;
						}
						break;
					case ']' :
						{
							int index = rightCount[SquareBracket] ++;
							if (index == rightPositions[SquareBracket].length) {
								System.arraycopy(rightPositions[SquareBracket], 0, (rightPositions[SquareBracket] = new int[index * 2]), 0, index);
								System.arraycopy(rightDepths[SquareBracket], 0, (rightDepths[SquareBracket] = new int[index * 2]), 0, index);
							}
							rightPositions[SquareBracket][index] = scanner.startPosition;
							rightDepths[SquareBracket][index] = --depths[SquareBracket];
						}
						break;
					case '\'' :
						{
							if (scanner.getNextChar('\\')) {
								scanner.scanEscapeCharacter();
							} else { // consume next character
								scanner.unicodeAsBackSlash = false;
								if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
									scanner.getNextUnicodeChar();
								} else {
									if (scanner.withoutUnicodePtr != 0) {
										scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] = scanner.currentCharacter;
									}
								}
							}
							scanner.getNextChar('\'');
							break;
						}
					case '"' : // consume next character
						scanner.unicodeAsBackSlash = false;
						if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
							scanner.getNextUnicodeChar();
						} else {
							if (scanner.withoutUnicodePtr != 0) {
								scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] = scanner.currentCharacter;
							}
						}
						while (scanner.currentCharacter != '"') {
							if (scanner.currentCharacter == '\r') {
								if (source[scanner.currentPosition] == '\n')
									scanner.currentPosition++;
								break; // the string cannot go further that the line
							}
							if (scanner.currentCharacter == '\n') {
								break; // the string cannot go further that the line
							}
							if (scanner.currentCharacter == '\\') {
								scanner.scanEscapeCharacter();
							}
							// consume next character
							scanner.unicodeAsBackSlash = false;
							if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
								scanner.getNextUnicodeChar();
							} else {
								if (scanner.withoutUnicodePtr != 0) {
									scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] = scanner.currentCharacter;
								}
							}
						}
						break;
					case '/' :
						{
							int test;
							if ((test = scanner.getNextChar('/', '*')) == 0) { //line comment 
								//get the next char 
								if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
									scanner.currentPosition++;
									while (source[scanner.currentPosition] == 'u') {
										scanner.currentPosition++;
									}
									if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c1 < 0 || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c2 < 0 || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c3 < 0 || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c4 < 0) { //error don't care of the value
										scanner.currentCharacter = 'A';
									} //something different from \n and \r
									else {
										scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
									}
								}
								while (scanner.currentCharacter != '\r' && scanner.currentCharacter != '\n') {
									//get the next char
									scanner.startPosition = scanner.currentPosition;
									if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										scanner.currentPosition++;
										while (source[scanner.currentPosition] == 'u') {
											scanner.currentPosition++;
										}
										if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c1 < 0 || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c2 < 0 || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c3 < 0 || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c4 < 0) { //error don't care of the value
											scanner.currentCharacter = 'A';
										} //something different from \n and \r
										else {
											scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
									}
								}
								if (scanner.recordLineSeparator && ((scanner.currentCharacter == '\r') || (scanner.currentCharacter == '\n'))) {
									if (scanner.lineEnds[scanner.linePtr] < scanner.startPosition) {
										// only record line positions we have not recorded yet
										scanner.pushLineSeparator();
									}
								}
								break;
							}
							if (test > 0) { //traditional and annotation comment
								boolean star = false;
								// consume next character
								scanner.unicodeAsBackSlash = false;
								if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
									scanner.getNextUnicodeChar();
								} else {
									if (scanner.withoutUnicodePtr != 0) {
										scanner.withoutUnicodeBuffer[++scanner.withoutUnicodePtr] = scanner.currentCharacter;
									}
								}
								if (scanner.currentCharacter == '*') {
									star = true;
								}
								//get the next char 
								if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
									scanner.currentPosition++;
									while (source[scanner.currentPosition] == 'u') {
										scanner.currentPosition++;
									}
									if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c1 < 0 || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c2 < 0 || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c3 < 0 || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c4 < 0) { //error don't care of the value
										scanner.currentCharacter = 'A';
									} //something different from * and /
									else {
										scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
									}
								}
								//loop until end of comment */ 
								while ((scanner.currentCharacter != '/') || (!star)) {
									star = scanner.currentCharacter == '*';
									//get next char
									if (((scanner.currentCharacter = source[scanner.currentPosition++]) == '\\') && (source[scanner.currentPosition] == 'u')) {
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										scanner.currentPosition++;
										while (source[scanner.currentPosition] == 'u') {
											scanner.currentPosition++;
										}
										if ((c1 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c1 < 0 || (c2 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c2 < 0 || (c3 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c3 < 0 || (c4 = Character.getNumericValue(source[scanner.currentPosition++])) > 15 || c4 < 0) { //error don't care of the value
											scanner.currentCharacter = 'A';
										} //something different from * and /
										else {
											scanner.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
									}
								}
								break;
							}
							break;
						}
					default :
						if (Character.isJavaIdentifierStart(scanner.currentCharacter)) {
							scanner.scanIdentifierOrKeyword();
							break;
						}
						if (Character.isDigit(scanner.currentCharacter)) {
							scanner.scanNumber(false);
							break;
						}
				}
				//-----------------end switch while try--------------------
			} catch (IndexOutOfBoundsException e) {
					break; // read until EOF
			} catch (InvalidInputException e) {
				return false; // no clue
			}
		}
		if (scanner.recordLineSeparator) {
			compilationUnit.compilationResult.lineSeparatorPositions = scanner.getLineEnds();
		}

		// check placement anomalies against other kinds of brackets
		for (int kind = 0; kind < BracketKinds; kind++) {
			for (int leftIndex = leftCount[kind] - 1; leftIndex >= 0; leftIndex--) {
				int start = leftPositions[kind][leftIndex]; // deepest first
				// find matching closing bracket
				int depth = leftDepths[kind][leftIndex];
				int end = -1;
				for (int i = 0; i < rightCount[kind]; i++) {
					int pos = rightPositions[kind][i];
					// want matching bracket further in source with same depth
					if ((pos > start) && (depth == rightDepths[kind][i])) {
						end = pos;
						break;
					}
				}
				if (end < 0) { // did not find a good closing match
					problemReporter.unmatchedBracket(start, referenceContext, compilationUnit.compilationResult);
					return true;
				}
				// check if even number of opening/closing other brackets in between this pair of brackets
				int balance = 0;
				for (int otherKind = 0;(balance == 0) && (otherKind < BracketKinds); otherKind++) {
					for (int i = 0; i < leftCount[otherKind]; i++) {
						int pos = leftPositions[otherKind][i];
						if ((pos > start) && (pos < end))
							balance++;
					}
					for (int i = 0; i < rightCount[otherKind]; i++) {
						int pos = rightPositions[otherKind][i];
						if ((pos > start) && (pos < end))
							balance--;
					}
					if (balance != 0) {
						problemReporter.unmatchedBracket(start, referenceContext, compilationUnit.compilationResult); //bracket anomaly
						return true;
					}
				}
			}
			// too many opening brackets ?
			for (int i = rightCount[kind]; i < leftCount[kind]; i++) {
				anomaliesDetected = true;
				problemReporter.unmatchedBracket(leftPositions[kind][leftCount[kind] - i - 1], referenceContext, compilationUnit.compilationResult);
			}
			// too many closing brackets ?
			for (int i = leftCount[kind]; i < rightCount[kind]; i++) {
				anomaliesDetected = true;
				problemReporter.unmatchedBracket(rightPositions[kind][i], referenceContext, compilationUnit.compilationResult);
			}
			if (anomaliesDetected) return true;
		}
		
		return anomaliesDetected;
	} catch (ArrayStoreException e) { // jdk1.2.2 jit bug
		return anomaliesDetected;
	} catch (NullPointerException e) { // jdk1.2.2 jit bug
		return anomaliesDetected;
	}
}
public final void checkAndSetModifiers(int flag){
	/*modify the current modifiers buffer.
	When the startPosition of the modifiers is 0
	it means that the modifier being parsed is the first
	of a list of several modifiers. The startPosition
	is zeroed when a copy of modifiers-buffer is push
	onto the astStack. */

	if ((modifiers & flag) != 0){ // duplicate modifier
		modifiers |= AccAlternateModifierProblem;
	}
	modifiers |= flag;
			
	if (modifiersSourceStart < 0) modifiersSourceStart = scanner.startPosition;
}
public void checkAnnotation() {

	boolean deprecated = false;
	boolean checkDeprecated = false;
	int lastAnnotationIndex = -1;

	//since jdk1.2 look only in the last java doc comment...
	found : for (lastAnnotationIndex = scanner.commentPtr; lastAnnotationIndex >= 0; lastAnnotationIndex--){
		//look for @deprecated into the first javadoc comment preceeding the declaration
		int commentSourceStart = scanner.commentStarts[lastAnnotationIndex];
		// javadoc only (non javadoc comment have negative end positions.)
		if (modifiersSourceStart != -1 && modifiersSourceStart < commentSourceStart) {
			continue;
		}
		if (scanner.commentStops[lastAnnotationIndex] < 0) {
			break found;
		}
		checkDeprecated = true;
		int commentSourceEnd = scanner.commentStops[lastAnnotationIndex] - 1; //stop is one over
		char[] comment = scanner.source;

		for (int i = commentSourceStart + 3; i < commentSourceEnd - 10; i++) {
			if ((comment[i] == '@') 
				&& (comment[i + 1] == 'd')
				&& (comment[i + 2] == 'e')
				&& (comment[i + 3] == 'p')
				&& (comment[i + 4] == 'r')
				&& (comment[i + 5] == 'e')
				&& (comment[i + 6] == 'c')
				&& (comment[i + 7] == 'a')
				&& (comment[i + 8] == 't')
				&& (comment[i + 9] == 'e')
				&& (comment[i + 10] == 'd')) {
				// ensure the tag is properly ended: either followed by a space, a tab, line end or asterisk.
				int nextPos = i+11;
				deprecated = (comment[nextPos] == ' ') || (comment[nextPos] == '\t') || (comment[nextPos] == '\n') || (comment[nextPos] == '\r') || (comment[nextPos] == '*');
				break found;
			}
		}
		break found;
	}
	if (deprecated) {
		checkAndSetModifiers(AccDeprecated);
	}
	// modify the modifier source start to point at the first comment
	if (lastAnnotationIndex >= 0 && checkDeprecated) {
		modifiersSourceStart = scanner.commentStarts[lastAnnotationIndex]; 
	}
}
protected void classInstanceCreation(boolean alwaysQualified) {
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

	// ClassBodyopt produces a null item on the astStak if it produces NO class body
	// An empty class body produces a 0 on the length stack.....

	AllocationExpression alloc;
	int length;
	if (((length = astLengthStack[astLengthPtr--]) == 1)
		&& (astStack[astPtr] == null)) {
		//NO ClassBody
		astPtr--;
		if (alwaysQualified) {
			alloc = new QualifiedAllocationExpression();
		} else {
			alloc = new AllocationExpression();
		}
		alloc.sourceEnd = endPosition; //the position has been stored explicitly

		if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		alloc.type = getTypeReference(0);
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = intStack[intPtr--];
		pushOnExpressionStack(alloc);
	} else {
		dispatchDeclarationInto(length);
		AnonymousLocalTypeDeclaration anonymousTypeDeclaration = (AnonymousLocalTypeDeclaration) astStack[astPtr];
		anonymousTypeDeclaration.declarationSourceEnd = endStatementPosition;
		anonymousTypeDeclaration.bodyEnd = endStatementPosition;
		if (anonymousTypeDeclaration.allocation != null) {
			anonymousTypeDeclaration.allocation.sourceEnd = endStatementPosition;
		}
		astPtr--;
		astLengthPtr--;
		
		// mark fields and initializer with local type mark if needed
		markFieldsWithLocalType(anonymousTypeDeclaration);
	}
}
protected final void concatExpressionLists() {
	expressionLengthStack[--expressionLengthPtr]++;
}
protected final void concatNodeLists() {
	/*
	 * This is a case where you have two sublists into the astStack that you want
	 * to merge in one list. There is no action required on the astStack. The only
	 * thing you need to do is merge the two lengths specified on the astStackLength.
	 * The top two length are for example:
	 * ... p   n
	 * and you want to result in a list like:
	 * ... n+p 
	 * This means that the p could be equals to 0 in case there is no astNode pushed
	 * on the astStack.
	 * Look at the InterfaceMemberDeclarations for an example.
	 */

	astLengthStack[astLengthPtr - 1] += astLengthStack[astLengthPtr--];
}
protected void consumeAllocationHeader() {
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

	// ClassBodyopt produces a null item on the astStak if it produces NO class body
	// An empty class body produces a 0 on the length stack.....

	if (currentElement == null){
		return; // should never occur, this consumeRule is only used in recovery mode
	}
	if (currentToken == TokenNameLBRACE){
		// beginning of an anonymous type
		AnonymousLocalTypeDeclaration anonymousType = new AnonymousLocalTypeDeclaration(this.compilationUnit.compilationResult);
		anonymousType.sourceStart = intStack[intPtr--];
		anonymousType.sourceEnd = rParenPos; // closing parenthesis
		lastCheckPoint = anonymousType.bodyStart = scanner.currentPosition;
		currentElement = currentElement.add(anonymousType, 0);
		lastIgnoredToken = -1;
		currentToken = 0; // opening brace already taken into account
		return;
	}
	lastCheckPoint = scanner.startPosition; // force to restart at this exact position
	restartRecovery = true; // request to restart from here on
}
protected void consumeArgumentList() {
	// ArgumentList ::= ArgumentList ',' Expression
	concatExpressionLists();
}
protected void consumeArrayAccess(boolean unspecifiedReference) {
	// ArrayAccess ::= Name '[' Expression ']' ==> true
	// ArrayAccess ::= PrimaryNoNewArray '[' Expression ']' ==> false


	//optimize push/pop
	Expression exp;
	if (unspecifiedReference) {
		exp = 
			expressionStack[expressionPtr] = 
				new ArrayReference(
					getUnspecifiedReferenceOptimized(),
					expressionStack[expressionPtr]);
	} else {
		expressionPtr--;
		expressionLengthPtr--;
		exp = 
			expressionStack[expressionPtr] = 
				new ArrayReference(
					expressionStack[expressionPtr],
					expressionStack[expressionPtr + 1]);
	}
	exp.sourceEnd = endPosition;
}
protected void consumeArrayCreationExpression() {
	// ArrayCreationExpression ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializeropt
	// ArrayCreationExpression ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializeropt

	int length;
	ArrayAllocationExpression aae = new ArrayAllocationExpression();
	if (expressionLengthStack[expressionLengthPtr] != 0) {
		expressionLengthPtr -- ;
		aae.initializer = (ArrayInitializer) expressionStack[expressionPtr--];
	} else {
		expressionLengthPtr--;
	}
		
	aae.type = getTypeReference(0);
	length = (expressionLengthStack[expressionLengthPtr--]);
	expressionPtr -= length ;
	System.arraycopy(
		expressionStack,
		expressionPtr+1,
		aae.dimensions = new Expression[length],
		0,
		length);
	aae.sourceStart = intStack[intPtr--];
	if (aae.initializer == null) {
		aae.sourceEnd = endPosition;
	} else {
		aae.sourceEnd = aae.initializer.sourceEnd ;
	}
	pushOnExpressionStack(aae);
}
protected void consumeArrayInitializer() {
	// ArrayInitializer ::= '{' VariableInitializers '}'
	// ArrayInitializer ::= '{' VariableInitializers , '}'

	arrayInitializer(expressionLengthStack[expressionLengthPtr--]);
}

protected void consumeAssertStatement() {
	// AssertStatement ::= 'assert' Expression ':' Expression ';'
	expressionLengthPtr-=2;
	pushOnAstStack(new AssertStatement(expressionStack[expressionPtr--], expressionStack[expressionPtr--], intStack[intPtr--]));
}

protected void consumeAssignment() {
	// Assignment ::= LeftHandSide AssignmentOperator AssignmentExpression
	//optimize the push/pop

	int op = intStack[intPtr--] ; //<--the encoded operator
	
	expressionPtr -- ; expressionLengthPtr -- ;
	expressionStack[expressionPtr] =
		(op != EQUAL ) ?
			new CompoundAssignment(
				expressionStack[expressionPtr] ,
				expressionStack[expressionPtr+1], 
				op,
				scanner.startPosition - 1)	:
			new Assignment(
				expressionStack[expressionPtr] ,
				expressionStack[expressionPtr+1],
				scanner.startPosition - 1);
}
protected void consumeAssignmentOperator(int pos) {
	// AssignmentOperator ::= '='
	// AssignmentOperator ::= '*='
	// AssignmentOperator ::= '/='
	// AssignmentOperator ::= '%='
	// AssignmentOperator ::= '+='
	// AssignmentOperator ::= '-='
	// AssignmentOperator ::= '<<='
	// AssignmentOperator ::= '>>='
	// AssignmentOperator ::= '>>>='
	// AssignmentOperator ::= '&='
	// AssignmentOperator ::= '^='
	// AssignmentOperator ::= '|='

	try {
		intStack[++intPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		//intPtr is correct 
		int oldStackLength = intStack.length;
		int oldStack[] = intStack;
		intStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, intStack, 0, oldStackLength);
		intStack[intPtr] = pos;
	}
}
protected void consumeBinaryExpression(int op) {
	// MultiplicativeExpression ::= MultiplicativeExpression '*' UnaryExpression
	// MultiplicativeExpression ::= MultiplicativeExpression '/' UnaryExpression
	// MultiplicativeExpression ::= MultiplicativeExpression '%' UnaryExpression
	// AdditiveExpression ::= AdditiveExpression '+' MultiplicativeExpression
	// AdditiveExpression ::= AdditiveExpression '-' MultiplicativeExpression
	// ShiftExpression ::= ShiftExpression '<<'  AdditiveExpression
	// ShiftExpression ::= ShiftExpression '>>'  AdditiveExpression
	// ShiftExpression ::= ShiftExpression '>>>' AdditiveExpression
	// RelationalExpression ::= RelationalExpression '<'  ShiftExpression
	// RelationalExpression ::= RelationalExpression '>'  ShiftExpression
	// RelationalExpression ::= RelationalExpression '<=' ShiftExpression
	// RelationalExpression ::= RelationalExpression '>=' ShiftExpression
	// AndExpression ::= AndExpression '&' EqualityExpression
	// ExclusiveOrExpression ::= ExclusiveOrExpression '^' AndExpression
	// InclusiveOrExpression ::= InclusiveOrExpression '|' ExclusiveOrExpression
	// ConditionalAndExpression ::= ConditionalAndExpression '&&' InclusiveOrExpression
	// ConditionalOrExpression ::= ConditionalOrExpression '||' ConditionalAndExpression

	//optimize the push/pop

	expressionPtr--;
	expressionLengthPtr--;
	if (op == OR_OR) {
		expressionStack[expressionPtr] = 
			new OR_OR_Expression(
				expressionStack[expressionPtr], 
				expressionStack[expressionPtr + 1], 
				op); 
	} else {
		if (op == AND_AND) {
			expressionStack[expressionPtr] = 
				new AND_AND_Expression(
					expressionStack[expressionPtr], 
					expressionStack[expressionPtr + 1], 
					op);
		} else {
			// look for "string1" + "string2"
			if ((op == PLUS) && optimizeStringLiterals) {
				Expression expr1, expr2;
				expr1 = expressionStack[expressionPtr];
				expr2 = expressionStack[expressionPtr + 1];
				if (expr1 instanceof StringLiteral) {
					if (expr2 instanceof CharLiteral) { // string+char
						expressionStack[expressionPtr] = 
							((StringLiteral) expr1).extendWith((CharLiteral) expr2); 
					} else if (expr2 instanceof StringLiteral) { //string+string
						expressionStack[expressionPtr] = 
							((StringLiteral) expr1).extendWith((StringLiteral) expr2); 
					} else {
						expressionStack[expressionPtr] = new BinaryExpression(expr1, expr2, PLUS);
					}
				} else {
					expressionStack[expressionPtr] = new BinaryExpression(expr1, expr2, PLUS);
				}
			} else {
				expressionStack[expressionPtr] = 
					new BinaryExpression(
						expressionStack[expressionPtr], 
						expressionStack[expressionPtr + 1], 
						op);
			}
		}
	}
}
protected void consumeBlock() {
	// Block ::= OpenBlock '{' BlockStatementsopt '}'
	// simpler action for empty blocks

	int length;
	if ((length = astLengthStack[astLengthPtr--]) == 0) { // empty block 
		pushOnAstStack(Block.EmptyWith(intStack[intPtr--], endStatementPosition));
		realBlockPtr--; // still need to pop the block variable counter
	} else {
		Block bk = new Block(realBlockStack[realBlockPtr--]);
		astPtr -= length;
		System.arraycopy(
			astStack, 
			astPtr + 1, 
			bk.statements = new Statement[length], 
			0, 
			length); 
		pushOnAstStack(bk);
		bk.sourceStart = intStack[intPtr--];
		bk.sourceEnd = endStatementPosition;
	}
}
protected void consumeBlockStatements() {
	// BlockStatements ::= BlockStatements BlockStatement
	concatNodeLists();
}
protected void consumeCaseLabel() {
	// SwitchLabel ::= 'case' ConstantExpression ':'
	expressionLengthPtr--;
	pushOnAstStack(new Case(intStack[intPtr--], expressionStack[expressionPtr--]));
}
protected void consumeCastExpression() {
	// CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN UnaryExpression
	// CastExpression ::= PushLPAREN Name Dims PushRPAREN UnaryExpressionNotPlusMinus

	//intStack : posOfLeftParen dim posOfRightParen

	//optimize the push/pop

	Expression exp, cast, castType;
	int end = intStack[intPtr--];
	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr], castType = getTypeReference(intStack[intPtr--]));
	castType.sourceEnd = end - 1;
	castType.sourceStart = (cast.sourceStart = intStack[intPtr--]) + 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCastExpressionLL1() {
	//CastExpression ::= '(' Expression ')' UnaryExpressionNotPlusMinus
	// Expression is used in order to make the grammar LL1

	//optimize push/pop

	Expression castType,cast,exp;
	expressionPtr--;
	expressionStack[expressionPtr] = 
			cast = new CastExpression(	exp=expressionStack[expressionPtr+1] ,
								castType = getTypeReference(expressionStack[expressionPtr]));
	expressionLengthPtr -- ;
	updateSourcePosition(castType);
	cast.sourceStart=castType.sourceStart;
	cast.sourceEnd=exp.sourceEnd;
	castType.sourceStart++;
	castType.sourceEnd--;
	}
protected void consumeCatches() {
	// Catches ::= Catches CatchClause
	optimizedConcatNodeLists();
}
protected void consumeCatchHeader() {
	// CatchDeclaration ::= 'catch' '(' FormalParameter ')' '{'

	if (currentElement == null){
		return; // should never occur, this consumeRule is only used in recovery mode
	}
	// current element should be a block due to the presence of the opening brace
	if (!(currentElement instanceof RecoveredBlock)){
		return;
	}
	// exception argument is already on astStack
	((RecoveredBlock)currentElement).attach(
		new RecoveredLocalVariable((Argument)astStack[astPtr--], currentElement, 0)); // insert catch variable in catch block
	lastCheckPoint = scanner.startPosition; // force to restart at this exact position
	restartRecovery = true; // request to restart from here on
	lastIgnoredToken = -1;
}
protected void consumeClassBodyDeclaration() {
	// ClassBodyDeclaration ::= Diet Block
	//push an Initializer
	//optimize the push/pop
	nestedMethod[nestedType]--;
	Initializer initializer = new Initializer((Block) astStack[astPtr], 0);
	intPtr--; // pop sourcestart left on the stack by consumeNestedMethod.
	realBlockPtr--; // pop the block variable counter left on the stack by consumeNestedMethod
	int javadocCommentStart = intStack[intPtr--];
	if (javadocCommentStart != -1) {
		initializer.declarationSourceStart = javadocCommentStart;
	}
	astStack[astPtr] = initializer;
	initializer.sourceEnd = endStatementPosition;
	initializer.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition);
}
protected void consumeClassBodyDeclarations() {
	// ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration
	concatNodeLists();
}
protected void consumeClassBodyDeclarationsopt() {
	// ClassBodyDeclarationsopt ::= NestedType ClassBodyDeclarations
	nestedType-- ;
}
protected void consumeClassBodyopt() {
	// ClassBodyopt ::= $empty
	pushOnAstStack(null);
	endPosition = scanner.startPosition - 1;
}
protected void consumeClassDeclaration() {
	// ClassDeclaration ::= ClassHeader ClassBody

	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		//there are length declarations
		//dispatch according to the type of the declarations
		dispatchDeclarationInto(length);
	}

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];

	// mark fields and initializer with local type mark if needed
	markFieldsWithLocalType(typeDecl);

	//convert constructor that do not have the type's name into methods
	boolean hasConstructor = typeDecl.checkConstructors(this);
	
	//add the default constructor when needed (interface don't have it)
	if (!hasConstructor) {
		boolean insideFieldInitializer = false;
		if (diet) {
			for (int i = nestedType; i > 0; i--){
				if (variablesCounter[i] > 0) {
					insideFieldInitializer = true;
					break;
				}
			}
		}
		typeDecl.createsInternalConstructor(!diet || insideFieldInitializer, true);
	}

	//always add <clinit> (will be remove at code gen time if empty)
	if (this.scanner.containsAssertKeyword) {
		typeDecl.bits |= AstNode.AddAssertionMASK;
	}
	typeDecl.addClinit();
	typeDecl.bodyEnd = endStatementPosition;
	typeDecl.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition); 
}
protected void consumeClassHeader() {
	// ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];	
	if (currentToken == TokenNameLBRACE) { 
		typeDecl.bodyStart = scanner.currentPosition;
	}
	if (currentElement != null) {
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	// flush the comments related to the class header
	scanner.commentPtr = -1;
}
protected void consumeClassHeaderExtends() {
	// ClassHeaderExtends ::= 'extends' ClassType
	// There is a class declaration on the top of stack
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	//superclass
	typeDecl.superclass = getTypeReference(0);
	typeDecl.bodyStart = typeDecl.superclass.sourceEnd + 1;
	// recovery
	if (currentElement != null){
		lastCheckPoint = typeDecl.bodyStart;
	}
}
protected void consumeClassHeaderImplements() {
	// ClassHeaderImplements ::= 'implements' InterfaceTypeList
	int length = astLengthStack[astLengthPtr--];
	//super interfaces
	astPtr -= length;
	// There is a class declaration on the top of stack
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	System.arraycopy(
		astStack, 
		astPtr + 1, 
		typeDecl.superInterfaces = new TypeReference[length], 
		0, 
		length); 
	typeDecl.bodyStart = typeDecl.superInterfaces[length-1].sourceEnd + 1;
	listLength = 0; // reset after having read super-interfaces
	// recovery
	if (currentElement != null) { // is recovering
		lastCheckPoint = typeDecl.bodyStart;
	}
}
protected void consumeClassHeaderName() {
	// ClassHeaderName ::= Modifiersopt 'class' 'Identifier'
	TypeDeclaration typeDecl;
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl = new MemberTypeDeclaration(this.compilationUnit.compilationResult);
		} else {
			typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl = new LocalTypeDeclaration(this.compilationUnit.compilationResult);
		markCurrentMethodWithLocalType();
		blockReal();
	}

	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
	// we want to keep the beginning position but get rid of the end position
	// it is only used for the ClassLiteralAccess positions.
	typeDecl.declarationSourceStart = intStack[intPtr--]; 
	intPtr--; // remove the end position of the class token

	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.modifiersSourceStart >= 0) {
		typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);

	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = typeDecl.bodyStart;
		currentElement = currentElement.add(typeDecl, 0);
		lastIgnoredToken = -1;
	}
}
protected void consumeClassInstanceCreationExpression() {
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt
	classInstanceCreation(false);
}
protected void consumeClassInstanceCreationExpressionName() {
	// ClassInstanceCreationExpressionName ::= Name '.'
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
}
protected void consumeClassInstanceCreationExpressionQualified() {
	// ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt

	classInstanceCreation(true); //  <-- push the Qualifed....

	expressionLengthPtr--;
	QualifiedAllocationExpression qae = 
		(QualifiedAllocationExpression) expressionStack[expressionPtr--]; 
	qae.enclosingInstance = expressionStack[expressionPtr];
	expressionStack[expressionPtr] = qae;
	qae.sourceStart = qae.enclosingInstance.sourceStart;
}
protected void consumeClassTypeElt() {
	// ClassTypeElt ::= ClassType
	pushOnAstStack(getTypeReference(0));
	/* if incomplete thrown exception list, listLength counter will not have been reset,
		indicating that some items are available on the stack */
	listLength++; 	
}
protected void consumeClassTypeList() {
	// ClassTypeList ::= ClassTypeList ',' ClassTypeElt
	optimizedConcatNodeLists();
}
protected void consumeCompilationUnit() {
	// CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt
	// do nothing by default
}
protected void consumeConditionalExpression(int op) {
	// ConditionalExpression ::= ConditionalOrExpression '?' Expression ':' ConditionalExpression
	//optimize the push/pop

	expressionPtr -= 2;
	expressionLengthPtr -= 2;
	expressionStack[expressionPtr] =
		new ConditionalExpression(
			expressionStack[expressionPtr],
			expressionStack[expressionPtr + 1],
			expressionStack[expressionPtr + 2]);
}
protected void consumeConstructorBlockStatements() {
	// ConstructorBody ::= NestedMethod '{' ExplicitConstructorInvocation BlockStatements '}'
	concatNodeLists(); // explictly add the first statement into the list of statements 
}
protected void consumeConstructorBody() {
	// ConstructorBody ::= NestedMethod  '{' BlockStatementsopt '}'
	// ConstructorBody ::= NestedMethod  '{' ExplicitConstructorInvocation '}'
	nestedMethod[nestedType] --;
}
protected void consumeConstructorDeclaration() {
	// ConstructorDeclaration ::= ConstructorHeader ConstructorBody

	/*
	astStack : MethodDeclaration statements
	identifierStack : name
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	*/

	//must provide a default constructor call when needed

	int length;

	// pop the position of the {  (body of the method) pushed in block decl
	intPtr--;

	//statements
	realBlockPtr--;
	ExplicitConstructorCall constructorCall = null;
	Statement[] statements = null;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		if (astStack[astPtr + 1] instanceof ExplicitConstructorCall) {
			//avoid a isSomeThing that would only be used here BUT what is faster between two alternatives ?
			System.arraycopy(
				astStack, 
				astPtr + 2, 
				statements = new Statement[length - 1], 
				0, 
				length - 1); 
			constructorCall = (ExplicitConstructorCall) astStack[astPtr + 1];
		} else { //need to add explicitly the super();
			System.arraycopy(
				astStack, 
				astPtr + 1, 
				statements = new Statement[length], 
				0, 
				length); 
			constructorCall = SuperReference.implicitSuperConstructorCall();
		}
	} else {
		if (!diet){
			// add it only in non-diet mode, if diet_bodies, then constructor call will be added elsewhere.
			constructorCall = SuperReference.implicitSuperConstructorCall();
		}
	}

	// now we know that the top of stack is a constructorDeclaration
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];
	cd.constructorCall = constructorCall;
	cd.statements = statements;

	//highlight of the implicit call on the method name
	if (constructorCall != null && cd.constructorCall.sourceEnd == 0) {
		cd.constructorCall.sourceEnd = cd.sourceEnd;
		cd.constructorCall.sourceStart = cd.sourceStart;
	}

	//watch for } that could be given as a unicode ! ( u007D is '}' )
	// store the endPosition (position just before the '}') in case there is
	// a trailing comment behind the end of the method
	cd.bodyEnd = endPosition;
	cd.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition); 
}

protected void consumeInvalidConstructorDeclaration() {
	// ConstructorDeclaration ::= ConstructorHeader ';'
	// now we know that the top of stack is a constructorDeclaration
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];

	cd.bodyEnd = endPosition; // position just before the trailing semi-colon
	cd.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition); 
	// report the problem and continue the parsing - narrowing the problem onto the method
}
protected void consumeConstructorHeader() {
	// ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt

	AbstractMethodDeclaration method = (AbstractMethodDeclaration)astStack[astPtr];

	if (currentToken == TokenNameLBRACE){ 
		method.bodyStart = scanner.currentPosition;
	}
	// recovery
	if (currentElement != null){
		restartRecovery = true; // used to avoid branching back into the regular automaton
	}		
}
protected void consumeConstructorHeaderName() {

	/* recovering - might be an empty message send */
	if (currentElement != null){
		if (lastIgnoredToken == TokenNamenew){ // was an allocation expression
			lastCheckPoint = scanner.startPosition; // force to restart at this exact position				
			restartRecovery = true;
			return;
		}
	}
	
	// ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('
	ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	long selectorSource = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];

	//highlight starts at the selector starts
	cd.sourceStart = (int) (selectorSource >>> 32);
	pushOnAstStack(cd);
	cd.sourceEnd = lParenPos;
	cd.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws

	// recovery
	if (currentElement != null){
		lastCheckPoint = cd.bodyStart;
		if ((currentElement instanceof RecoveredType && lastIgnoredToken != TokenNameDOT)
			|| cd.modifiers != 0){
			currentElement = currentElement.add(cd, 0);
			lastIgnoredToken = -1;
		}
	}	
}
protected void consumeDefaultLabel() {
	// SwitchLabel ::= 'default' ':'
	pushOnAstStack(new DefaultCase(intStack[intPtr--], intStack[intPtr--]));
}
protected void consumeDefaultModifiers() {
	checkAnnotation(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(
		modifiersSourceStart >= 0 ? modifiersSourceStart : scanner.startPosition); 
	resetModifiers();
}
protected void consumeDiet() {
	// Diet ::= $empty
	checkAnnotation();
	pushOnIntStack(modifiersSourceStart); // push the start position of a javadoc comment if there is one
	jumpOverMethodBody();
}
protected void consumeDims() {
	// Dims ::= DimsLoop
	pushOnIntStack(dimensions);
	dimensions = 0;
}
protected void consumeDimWithOrWithOutExpr() {
	// DimWithOrWithOutExpr ::= '[' ']'
	pushOnExpressionStack(null);
}
protected void consumeDimWithOrWithOutExprs() {
	// DimWithOrWithOutExprs ::= DimWithOrWithOutExprs DimWithOrWithOutExpr
	concatExpressionLists();
}
protected void consumeEmptyArgumentListopt() {
	// ArgumentListopt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyArrayInitializer() {
	// ArrayInitializer ::= '{' ,opt '}'
	arrayInitializer(0);
}
protected void consumeEmptyArrayInitializeropt() {
	// ArrayInitializeropt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyBlockStatementsopt() {
	// BlockStatementsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyCatchesopt() {
	// Catchesopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyClassBodyDeclarationsopt() {
	// ClassBodyDeclarationsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyClassMemberDeclaration() {
	// ClassMemberDeclaration ::= ';'
	pushOnAstLengthStack(0);
}
protected void consumeEmptyDimsopt() {
	// Dimsopt ::= $empty
	pushOnIntStack(0);
}
protected void consumeEmptyExpression() {
	// Expressionopt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyForInitopt() {
	// ForInitopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyForUpdateopt() {
	// ForUpdateopt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyImportDeclarationsopt() {
	// ImportDeclarationsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyInterfaceMemberDeclaration() {
	// InterfaceMemberDeclaration ::= ';'
	pushOnAstLengthStack(0);
}
protected void consumeEmptyInterfaceMemberDeclarationsopt() {
	// InterfaceMemberDeclarationsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyStatement() {
	// EmptyStatement ::= ';'
	if (this.scanner.source[endStatementPosition] == ';') {
		pushOnAstStack(new EmptyStatement(endStatementPosition, endStatementPosition));
	} else {
		// we have a Unicode for the ';' (/u003B)
		pushOnAstStack(new EmptyStatement(endStatementPosition - 5, endStatementPosition));
	}
}
protected void consumeEmptySwitchBlock() {
	// SwitchBlock ::= '{' '}'
	pushOnAstLengthStack(0);
}
protected void consumeEmptyTypeDeclaration() {
	// TypeDeclaration ::= ';' 
	pushOnAstLengthStack(0);
}
protected void consumeEmptyTypeDeclarationsopt() {
	// TypeDeclarationsopt ::= $empty
	pushOnAstLengthStack(0); 
}
protected void consumeEnterAnonymousClassBody() {
	// EnterAnonymousClassBody ::= $empty
	QualifiedAllocationExpression alloc;
	AnonymousLocalTypeDeclaration anonymousType = 
		new AnonymousLocalTypeDeclaration(this.compilationUnit.compilationResult); 
	alloc = 
		anonymousType.allocation = new QualifiedAllocationExpression(anonymousType); 
	markCurrentMethodWithLocalType();
	pushOnAstStack(anonymousType);

	alloc.sourceEnd = rParenPos; //the position has been stored explicitly
	int argumentLength;
	if ((argumentLength = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= argumentLength;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			alloc.arguments = new Expression[argumentLength], 
			0, 
			argumentLength); 
	}
	alloc.type = getTypeReference(0);

	anonymousType.sourceEnd = alloc.sourceEnd;
	//position at the type while it impacts the anonymous declaration
	anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
	alloc.sourceStart = intStack[intPtr--];
	pushOnExpressionStack(alloc);

	anonymousType.bodyStart = scanner.currentPosition;	
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = anonymousType.bodyStart;
		// the recoveryTokenCheck will deal with the open brace		
		currentElement = currentElement.add(anonymousType, 0);
		currentToken = 0; // opening brace already taken into account
		lastIgnoredToken = -1;
	}	
}
protected void consumeEnterCompilationUnit() {
	// EnterCompilationUnit ::= $empty
	// do nothing by default
}
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	// do nothing by default

	char[] name = identifierStack[identifierPtr];
	long namePosition = identifierPositionStack[identifierPtr];
	int extendedDimension = intStack[intPtr--];
	AbstractVariableDeclaration declaration;
	// create the ast node
	boolean isLocalDeclaration = nestedMethod[nestedType] != 0; 
	if (isLocalDeclaration) {
		// create the local variable declarations
		declaration = 
			this.createLocalDeclaration(null, name, (int) (namePosition >>> 32), (int) namePosition);
	} else {
		// create the field declaration
		declaration = 
			this.createFieldDeclaration(null, name, (int) (namePosition >>> 32), (int) namePosition); 
	}
	
	identifierPtr--;
	identifierLengthPtr--;
	TypeReference type;
	int variableIndex = variablesCounter[nestedType];
	int typeDim = 0;
	if (variableIndex == 0) {
		// first variable of the declaration (FieldDeclaration or LocalDeclaration)
		if (isLocalDeclaration) {
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			if (declaration.declarationSourceStart == -1) {
				// this is true if there is no modifiers for the local variable declaration
				declaration.declarationSourceStart = type.sourceStart;
			}
			pushOnAstStack(type);
		} else {
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			pushOnAstStack(type);
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
		}
	} else {
		type = (TypeReference) astStack[astPtr - variableIndex];
		typeDim = type.dimensions();
		AbstractVariableDeclaration previousVariable = 
			(AbstractVariableDeclaration) astStack[astPtr]; 
		declaration.declarationSourceStart = previousVariable.declarationSourceStart;
		declaration.modifiers = previousVariable.modifiers;
	}

	if (extendedDimension == 0) {
		declaration.type = type;
	} else {
		int dimension = typeDim + extendedDimension;
		//on the identifierLengthStack there is the information about the type....
		int baseType;
		if ((baseType = identifierLengthStack[identifierLengthPtr + 1]) < 0) {
			//it was a baseType
			int typeSourceStart = type.sourceStart;
			int typeSourceEnd = type.sourceEnd;
			type = TypeReference.baseTypeReference(-baseType, dimension);
			type.sourceStart = typeSourceStart;
			type.sourceEnd = typeSourceEnd;
			declaration.type = type;
		} else {
			declaration.type = this.copyDims(type, dimension);
		}
	}
	variablesCounter[nestedType]++;
	pushOnAstStack(declaration);
	// recovery
	if (currentElement != null) {
		if (!(currentElement instanceof RecoveredType)
			&& (currentToken == TokenNameDOT
				//|| declaration.modifiers != 0
				|| (scanner.getLineNumber(declaration.type.sourceStart)
						!= scanner.getLineNumber((int) (namePosition >>> 32))))){
			lastCheckPoint = (int) (namePosition >>> 32);
			restartRecovery = true;
			return;
		}
		if (isLocalDeclaration){
			LocalDeclaration localDecl = (LocalDeclaration) astStack[astPtr];
			lastCheckPoint = localDecl.sourceEnd + 1;
			currentElement = currentElement.add(localDecl, 0);
		} else {
			FieldDeclaration fieldDecl = (FieldDeclaration) astStack[astPtr];
			lastCheckPoint = fieldDecl.sourceEnd + 1;
			currentElement = currentElement.add(fieldDecl, 0);
		}
		lastIgnoredToken = -1;
	}
}
protected void consumeEqualityExpression(int op) {
	// EqualityExpression ::= EqualityExpression '==' RelationalExpression
	// EqualityExpression ::= EqualityExpression '!=' RelationalExpression

	//optimize the push/pop

	expressionPtr--;
	expressionLengthPtr--;
	expressionStack[expressionPtr] =
		new EqualExpression(
			expressionStack[expressionPtr],
			expressionStack[expressionPtr + 1],
			op);
}
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// do nothing by default
	expressionLengthPtr--;
	AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration) astStack[astPtr];
	variableDecl.initialization = expressionStack[expressionPtr--];
	// we need to update the declarationSourceEnd of the local variable declaration to the
	// source end position of the initialization expression
	variableDecl.declarationSourceEnd = variableDecl.initialization.sourceEnd;
	variableDecl.declarationEnd = variableDecl.initialization.sourceEnd;
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
}
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {

	/* flag allows to distinguish 3 cases :
	(0) :   
	ExplicitConstructorInvocation ::= 'this' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= 'super' '(' ArgumentListopt ')' ';'
	(1) :
	ExplicitConstructorInvocation ::= Primary '.' 'super' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= Primary '.' 'this' '(' ArgumentListopt ')' ';'
	(2) :
	ExplicitConstructorInvocation ::= Name '.' 'super' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= Name '.' 'this' '(' ArgumentListopt ')' ';'
	*/
	int startPosition = intStack[intPtr--];
	ExplicitConstructorCall ecc = new ExplicitConstructorCall(recFlag);
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, ecc.arguments = new Expression[length], 0, length);
	}
	switch (flag) {
		case 0 :
			ecc.sourceStart = startPosition;
			break;
		case 1 :
			expressionLengthPtr--;
			ecc.sourceStart = (ecc.qualification = expressionStack[expressionPtr--]).sourceStart;
			break;
		case 2 :
			ecc.sourceStart = (ecc.qualification = getUnspecifiedReferenceOptimized()).sourceStart;
			break;
	};
	pushOnAstStack(ecc);
	ecc.sourceEnd = endPosition;
}
protected void consumeExpressionStatement() {
	// ExpressionStatement ::= StatementExpression ';'
	expressionLengthPtr--;
	pushOnAstStack(expressionStack[expressionPtr--]);
}
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	FieldReference fr =
		new FieldReference(
			identifierStack[identifierPtr],
			identifierPositionStack[identifierPtr--]);
	identifierLengthPtr--;
	if (isSuperAccess) {
		//considerates the fieldReference beginning at the 'super' ....	
		fr.sourceStart = intStack[intPtr--];
		fr.receiver = new SuperReference(fr.sourceStart, endPosition);
		pushOnExpressionStack(fr);
	} else {
		//optimize push/pop
		if ((fr.receiver = expressionStack[expressionPtr]).isThis()) {
			//fieldreference begins at the this
			fr.sourceStart = fr.receiver.sourceStart;
		}
		expressionStack[expressionPtr] = fr;
	}
}
protected void consumeFieldDeclaration() {
	// See consumeLocalVariableDeclarationDefaultModifier() in case of change: duplicated code
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'

	/*
	astStack : 
	expressionStack: Expression Expression ...... Expression
	identifierStack : type  identifier identifier ...... identifier
	intStack : typeDim      dim        dim               dim
	 ==>
	astStack : FieldDeclaration FieldDeclaration ...... FieldDeclaration
	expressionStack :
	identifierStack : 
	intStack : 
	  
	*/
	int variableDeclaratorsCounter = astLengthStack[astLengthPtr];

	for (int i = variableDeclaratorsCounter - 1; i >= 0; i--) {
		FieldDeclaration fieldDeclaration = (FieldDeclaration) astStack[astPtr - i];
		fieldDeclaration.declarationSourceEnd = endStatementPosition; 
		fieldDeclaration.declarationEnd = endStatementPosition;	// semi-colon included
	}
	updateSourceDeclarationParts(variableDeclaratorsCounter);
	int endPos = flushAnnotationsDefinedPriorTo(endStatementPosition);
	if (endPos != endStatementPosition) {
		for (int i = 0; i < variableDeclaratorsCounter; i++) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) astStack[astPtr - i];
			fieldDeclaration.declarationSourceEnd = endPos;
		}
	}
	// update the astStack, astPtr and astLengthStack
	int startIndex = astPtr - variablesCounter[nestedType] + 1;
	System.arraycopy(
		astStack, 
		startIndex, 
		astStack, 
		startIndex - 1, 
		variableDeclaratorsCounter); 
	astPtr--; // remove the type reference
	astLengthStack[--astLengthPtr] = variableDeclaratorsCounter;

	// recovery
	if (currentElement != null) {
		lastCheckPoint = endPos + 1;
		if (currentElement.parent != null && currentElement instanceof RecoveredField){
			currentElement = currentElement.parent;
		}
		restartRecovery = true;
	}
	variablesCounter[nestedType] = 0;
}
protected void consumeForceNoDiet() {
	// ForceNoDiet ::= $empty
	dietInt++;
}
protected void consumeForInit() {
	// ForInit ::= StatementExpressionList
	pushOnAstLengthStack(-1);
}
protected void consumeFormalParameter() {
	// FormalParameter ::= Type VariableDeclaratorId ==> false
	// FormalParameter ::= Modifiers Type VariableDeclaratorId ==> true
	/*
	astStack : 
	identifierStack : type identifier
	intStack : dim dim
	 ==>
	astStack : Argument
	identifierStack :  
	intStack :  
	*/

	identifierLengthPtr--;
	char[] name = identifierStack[identifierPtr];
	long namePositions = identifierPositionStack[identifierPtr--];
	TypeReference type = getTypeReference(intStack[intPtr--] + intStack[intPtr--]);
	int modifierPositions = intStack[intPtr--];
	intPtr--;
	Argument arg = 
		new Argument(
			name, 
			namePositions, 
			type, 
			intStack[intPtr + 1] & ~AccDeprecated); // modifiers
	arg.declarationSourceStart = modifierPositions;
	pushOnAstStack(arg);

	/* if incomplete method header, listLength counter will not have been reset,
		indicating that some arguments are available on the stack */
	listLength++; 	
}
protected void consumeFormalParameterList() {
	// FormalParameterList ::= FormalParameterList ',' FormalParameter
	optimizedConcatNodeLists();
}
protected void consumeFormalParameterListopt() {
	// FormalParameterListopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeImportDeclarations() {
	// ImportDeclarations ::= ImportDeclarations ImportDeclaration 
	optimizedConcatNodeLists();
}
protected void consumeImportDeclarationsopt() {
	// ImportDeclarationsopt ::= ImportDeclarations
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		System.arraycopy(
			astStack,
			astPtr + 1,
			compilationUnit.imports = new ImportReference[length],
			0,
			length);
	}
}
protected void consumeInstanceOfExpression(int op) {
	// RelationalExpression ::= RelationalExpression 'instanceof' ReferenceType
	//optimize the push/pop

	//by construction, no base type may be used in getTypeReference
	Expression exp;
	expressionStack[expressionPtr] = exp =
		new InstanceOfExpression(
			expressionStack[expressionPtr],
			getTypeReference(intStack[intPtr--]),
			op);
	if (exp.sourceEnd == 0) {
		//array on base type....
		exp.sourceEnd = scanner.startPosition - 1;
	}
	//the scanner is on the next token already....
}
protected void consumeInterfaceDeclaration() {
	// see consumeClassDeclaration in case of changes: duplicated code
	// InterfaceDeclaration ::= InterfaceHeader InterfaceBody
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		//there are length declarations
		//dispatch.....according to the type of the declarations
		dispatchDeclarationInto(length);
	}

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	
	// mark fields and initializer with local type mark if needed
	markFieldsWithLocalType(typeDecl);

	//convert constructor that do not have the type's name into methods
	typeDecl.checkConstructors(this);
	
	//always add <clinit> (will be remove at code gen time if empty)
	if (this.scanner.containsAssertKeyword) {
		typeDecl.bits |= AstNode.AddAssertionMASK;
	}
	typeDecl.addClinit();
	typeDecl.bodyEnd = endStatementPosition;
	typeDecl.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition); 
}
protected void consumeInterfaceHeader() {
	// InterfaceHeader ::= InterfaceHeaderName InterfaceHeaderExtendsopt

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];	
	if (currentToken == TokenNameLBRACE){ 
		typeDecl.bodyStart = scanner.currentPosition;
	}
	if (currentElement != null){
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	// flush the comments related to the interface header
	scanner.commentPtr = -1;	
}
protected void consumeInterfaceHeaderExtends() {
	// InterfaceHeaderExtends ::= 'extends' InterfaceTypeList
	int length = astLengthStack[astLengthPtr--];
	//super interfaces
	astPtr -= length;
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	System.arraycopy(
		astStack, 
		astPtr + 1, 
		typeDecl.superInterfaces = new TypeReference[length], 
		0, 
		length); 
	typeDecl.bodyStart = typeDecl.superInterfaces[length-1].sourceEnd + 1;		
	listLength = 0; // reset after having read super-interfaces		
	// recovery
	if (currentElement != null) { 
		lastCheckPoint = typeDecl.bodyStart;
	}
}
protected void consumeInterfaceHeaderName() {
	// InterfaceHeaderName ::= Modifiersopt 'interface' 'Identifier'
	TypeDeclaration typeDecl;
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl = new MemberTypeDeclaration(this.compilationUnit.compilationResult);
		} else {
			typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl = new LocalTypeDeclaration(this.compilationUnit.compilationResult);
		markCurrentMethodWithLocalType();
		blockReal();
	}

	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
	// we want to keep the beginning position but get rid of the end position
	// it is only used for the ClassLiteralAccess positions.
	typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--; // remove the end position of the class token
	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.modifiersSourceStart >= 0) {
		typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ // is recovering
		lastCheckPoint = typeDecl.bodyStart;
		currentElement = currentElement.add(typeDecl, 0);
		lastIgnoredToken = -1;		
	}
}
protected void consumeInterfaceMemberDeclarations() {
	// InterfaceMemberDeclarations ::= InterfaceMemberDeclarations InterfaceMemberDeclaration
	concatNodeLists();
}
protected void consumeInterfaceMemberDeclarationsopt() {
	// InterfaceMemberDeclarationsopt ::= NestedType InterfaceMemberDeclarations
	nestedType--;
}
protected void consumeInterfaceType() {
	// InterfaceType ::= ClassOrInterfaceType
	pushOnAstStack(getTypeReference(0));
	/* if incomplete type header, listLength counter will not have been reset,
		indicating that some interfaces are available on the stack */
	listLength++; 	
}
protected void consumeInterfaceTypeList() {
	// InterfaceTypeList ::= InterfaceTypeList ',' InterfaceType
	optimizedConcatNodeLists();
}
protected void consumeLeftHandSide() {
	// LeftHandSide ::= Name

	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
}
protected void consumeLeftParen() {
	// PushLPAREN ::= '('
	pushOnIntStack(lParenPos);
}
protected void consumeLocalVariableDeclaration() {
	// LocalVariableDeclaration ::= Modifiers Type VariableDeclarators ';'

	/*
	astStack : 
	expressionStack: Expression Expression ...... Expression
	identifierStack : type  identifier identifier ...... identifier
	intStack : typeDim      dim        dim               dim
	 ==>
	astStack : FieldDeclaration FieldDeclaration ...... FieldDeclaration
	expressionStack :
	identifierStack : 
	intStack : 
	  
	*/
	int variableDeclaratorsCounter = astLengthStack[astLengthPtr];

	// update the astStack, astPtr and astLengthStack
	int startIndex = astPtr - variablesCounter[nestedType] + 1;
	System.arraycopy(
		astStack, 
		startIndex, 
		astStack, 
		startIndex - 1, 
		variableDeclaratorsCounter); 
	astPtr--; // remove the type reference
	astLengthStack[--astLengthPtr] = variableDeclaratorsCounter;
	variablesCounter[nestedType] = 0;
}
protected void consumeLocalVariableDeclarationStatement() {
	// LocalVariableDeclarationStatement ::= LocalVariableDeclaration ';'
	// see blockReal in case of change: duplicated code
	// increment the amount of declared variables for this block
	realBlockStack[realBlockPtr]++;
}
protected void consumeMethodBody() {
	// MethodBody ::= NestedMethod '{' BlockStatementsopt '}' 
	nestedMethod[nestedType] --;
}
protected void consumeMethodDeclaration(boolean isNotAbstract) {
	// MethodDeclaration ::= MethodHeader MethodBody
	// AbstractMethodDeclaration ::= MethodHeader ';'

	/*
	astStack : modifiers arguments throws statements
	identifierStack : type name
	intStack : dim dim dim
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	intStack : 
	*/

	int length;
	if (isNotAbstract) {
		// pop the position of the {  (body of the method) pushed in block decl
		intPtr--;
	}

	int explicitDeclarations = 0;
	Statement[] statements = null;
	if (isNotAbstract) {
		//statements
		explicitDeclarations = realBlockStack[realBlockPtr--];
		if ((length = astLengthStack[astLengthPtr--]) != 0)
			System.arraycopy(
				astStack, 
				(astPtr -= length) + 1, 
				statements = new Statement[length], 
				0, 
				length); 
	}

	// now we know that we have a method declaration at the top of the ast stack
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	md.statements = statements;
	md.explicitDeclarations = explicitDeclarations;

	// cannot be done in consumeMethodHeader because we have no idea whether or not there
	// is a body when we reduce the method header
	if (!isNotAbstract) { //remember the fact that the method has a semicolon body
		md.modifiers |= AccSemicolonBody;
	}
	// store the endPosition (position just before the '}') in case there is
	// a trailing comment behind the end of the method
	md.bodyEnd = endPosition;
	md.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition);
}
protected void consumeMethodHeader() {
	// MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims ThrowsClauseopt
	// retrieve end position of method declarator
	AbstractMethodDeclaration method = (AbstractMethodDeclaration)astStack[astPtr];

	if (currentToken == TokenNameLBRACE){ 
		method.bodyStart = scanner.currentPosition;
	}
	// recovery
	if (currentElement != null){
		if (currentToken == TokenNameSEMICOLON){
			method.modifiers |= AccSemicolonBody;			
			method.declarationSourceEnd = scanner.currentPosition-1;
			method.bodyEnd = scanner.currentPosition-1;
			if (currentElement.parent != null){
				currentElement = currentElement.parent;
			}
		}		
		restartRecovery = true; // used to avoid branching back into the regular automaton
	}		
}
protected void consumeMethodHeaderExtendedDims() {
	// MethodHeaderExtendedDims ::= Dimsopt
	// now we update the returnType of the method
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	int extendedDims = intStack[intPtr--];
	if (extendedDims != 0) {
		TypeReference returnType = md.returnType;
		md.sourceEnd = endPosition;
		int dims = returnType.dimensions() + extendedDims;
		int baseType;
		if ((baseType = identifierLengthStack[identifierLengthPtr + 1]) < 0) {
			//it was a baseType
			int sourceStart = returnType.sourceStart;
			int sourceEnd =  returnType.sourceEnd;
			returnType = TypeReference.baseTypeReference(-baseType, dims);
			returnType.sourceStart = sourceStart;
			returnType.sourceEnd = sourceEnd;
			md.returnType = returnType;
		} else {
			md.returnType = this.copyDims(md.returnType, dims);
		}
		if (currentToken == TokenNameLBRACE){ 
			md.bodyStart = endPosition + 1;
		}
		// recovery
		if (currentElement != null){
			lastCheckPoint = md.bodyStart;
		}		
	}
}
protected void consumeMethodHeaderName() {
	// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	MethodDeclaration md = new MethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = identifierStack[identifierPtr];
	long selectorSource = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(intStack[intPtr--]);
	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSource >>> 32);
	pushOnAstStack(md);
	md.sourceEnd = lParenPos;
	md.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws
	
	// recovery
	if (currentElement != null){
		if (currentElement instanceof RecoveredType 
			//|| md.modifiers != 0
			|| (scanner.getLineNumber(md.returnType.sourceStart)
					== scanner.getLineNumber(md.sourceStart))){
			lastCheckPoint = md.bodyStart;
			currentElement = currentElement.add(md, 0);
			lastIgnoredToken = -1;
		} else {
			lastCheckPoint = md.sourceStart;
			restartRecovery = true;
		}
	}		
}
protected void consumeMethodHeaderParameters() {
	// MethodHeaderParameters ::= FormalParameterListopt ')'
	int length = astLengthStack[astLengthPtr--];
	astPtr -= length;
	AbstractMethodDeclaration md = (AbstractMethodDeclaration) astStack[astPtr];
	md.sourceEnd = 	rParenPos;
	//arguments
	if (length != 0) {
		System.arraycopy(
			astStack, 
			astPtr + 1, 
			md.arguments = new Argument[length], 
			0, 
			length); 
	}
	md.bodyStart = rParenPos+1;
	listLength = 0; // reset listLength after having read all parameters
	// recovery
	if (currentElement != null){
		lastCheckPoint = md.bodyStart;
		if (currentElement.parseTree() == md) return;

		// might not have been attached yet - in some constructor scenarii
		if (md.isConstructor()){
			if ((length != 0)
				|| (currentToken == TokenNameLBRACE) 
				|| (currentToken == TokenNamethrows)){
				currentElement = currentElement.add(md, 0);
				lastIgnoredToken = -1;
			}	
		}	
	}	
}
protected void consumeMethodHeaderThrowsClause() {
	// MethodHeaderThrowsClause ::= 'throws' ClassTypeList
	int length = astLengthStack[astLengthPtr--];
	astPtr -= length;
	AbstractMethodDeclaration md = (AbstractMethodDeclaration) astStack[astPtr];
	System.arraycopy(
		astStack, 
		astPtr + 1, 
		md.thrownExceptions = new TypeReference[length], 
		0, 
		length);
	md.sourceEnd = 	md.thrownExceptions[length-1].sourceEnd;
	md.bodyStart = md.thrownExceptions[length-1].sourceEnd + 1;
	listLength = 0; // reset listLength after having read all thrown exceptions	
	// recovery
	if (currentElement != null){
		lastCheckPoint = md.bodyStart;
	}		
}
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)

	MessageSend m = newMessageSend();
	m.sourceEnd = rParenPos;
	m.sourceStart = 
		(int) ((m.nameSourcePosition = identifierPositionStack[identifierPtr]) >>> 32); 
	m.selector = identifierStack[identifierPtr--];
	if (identifierLengthStack[identifierLengthPtr] == 1) {
		m.receiver = ThisReference.ThisImplicit;
		identifierLengthPtr--;
	} else {
		identifierLengthStack[identifierLengthPtr]--;
		m.receiver = getUnspecifiedReference();
		m.sourceStart = m.receiver.sourceStart;		
	}
	pushOnExpressionStack(m);
}
protected void consumeMethodInvocationPrimary() {
	//optimize the push/pop
	//MethodInvocation ::= Primary '.' 'Identifier' '(' ArgumentListopt ')'

	MessageSend m = newMessageSend();
	m.sourceStart = 
		(int) ((m.nameSourcePosition = identifierPositionStack[identifierPtr]) >>> 32); 
	m.selector = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	m.receiver = expressionStack[expressionPtr];
	m.sourceStart = m.receiver.sourceStart;
	m.sourceEnd = rParenPos;
	expressionStack[expressionPtr] = m;
}
protected void consumeMethodInvocationSuper() {
	// MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'

	MessageSend m = newMessageSend();
	m.sourceStart = intStack[intPtr--];
	m.sourceEnd = rParenPos;
	m.nameSourcePosition = identifierPositionStack[identifierPtr];
	m.selector = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	m.receiver = new SuperReference(m.sourceStart, endPosition);
	pushOnExpressionStack(m);
}
protected void consumeMethodPushModifiersHeaderName() {
	// MethodPushModifiersHeaderName ::= Modifiers Type PushModifiers 'Identifier' '('
	// MethodPushModifiersHeaderName ::= Type PushModifiers 'Identifier' '(' 
	MethodDeclaration md = new MethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = identifierStack[identifierPtr];
	long selectorSource = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];

	//type
	md.returnType = getTypeReference(intStack[intPtr--]);

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSource >>> 32);
	pushOnAstStack(md);
	md.sourceEnd = lParenPos;
	md.bodyStart = lParenPos + 1;
	listLength = 0; // initialize listLength before reading parameters/throws

	// recovery
	if (currentElement != null) {
		lastCheckPoint = md.bodyStart;
		currentElement = currentElement.add(md, 0);
		lastIgnoredToken = -1;		
	}
}
protected void consumeModifiers() {
	int savedModifiersSourceStart = modifiersSourceStart;	
	checkAnnotation(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	if (modifiersSourceStart >= savedModifiersSourceStart) {
		modifiersSourceStart = savedModifiersSourceStart;
	}
	pushOnIntStack(modifiersSourceStart);
	resetModifiers();
}
protected void consumeNestedMethod() {
	// NestedMethod ::= $empty
	jumpOverMethodBody();
	nestedMethod[nestedType] ++;
	consumeOpenBlock();
}
protected void consumeNestedType() {
	// NestedType ::= $empty
	nestedType++;
	try {
		nestedMethod[nestedType] = 0;
	} catch (IndexOutOfBoundsException e) {
		//except in test's cases, it should never raise
		int oldL = nestedMethod.length;
		System.arraycopy(nestedMethod , 0, (nestedMethod = new int[oldL + 30]), 0, oldL);
		nestedMethod[nestedType] = 0;
		// increase the size of the fieldsCounter as well. It has to be consistent with the size of the nestedMethod collection
		System.arraycopy(variablesCounter, 0, (variablesCounter = new int[oldL + 30]), 0, oldL);
	}
	variablesCounter[nestedType] = 0;
}
protected void consumeOneDimLoop() {
	// OneDimLoop ::= '[' ']'
	dimensions++;
}
protected void consumeOnlySynchronized() {
	// OnlySynchronized ::= 'synchronized'
	pushOnIntStack(this.synchronizedBlockSourceStart);
	resetModifiers();
}
protected void consumeOpenBlock() {
	// OpenBlock ::= $empty

	pushOnIntStack(scanner.startPosition);
	try {
		realBlockStack[++realBlockPtr] = 0;
	} catch (IndexOutOfBoundsException e) {
		//realBlockPtr is correct 
		int oldStackLength = realBlockStack.length;
		int oldStack[] = realBlockStack;
		realBlockStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, realBlockStack, 0, oldStackLength);
		realBlockStack[realBlockPtr] = 0;
	}
}
protected void consumePackageDeclaration() {
	// PackageDeclaration ::= 'package' Name ';'
	/* build an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt = compilationUnit.currentPackage;
	// flush annotations defined prior to import statements
	impt.declarationEnd = endStatementPosition;
	impt.declarationSourceEnd = this.flushAnnotationsDefinedPriorTo(impt.declarationSourceEnd);
}
protected void consumePackageDeclarationName() {
	// PackageDeclarationName ::= 'package' Name
	/* build an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = 
		new char[length = identifierLengthStack[identifierLengthPtr--]][]; 
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, ++identifierPtr, tokens, 0, length);
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr--, 
		positions, 
		0, 
		length); 
	compilationUnit.currentPackage = 
		impt = new ImportReference(tokens, positions, true); 

	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumePostfixExpression() {
	// PostfixExpression ::= Name
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
}
protected void consumePrimaryNoNewArray() {
	// PrimaryNoNewArray ::=  PushLPAREN Expression PushRPAREN 
	updateSourcePosition(expressionStack[expressionPtr]);
}
protected void consumePrimaryNoNewArrayArrayType() {
	// PrimaryNoNewArray ::= ArrayType '.' 'class'
	intPtr--;
	pushOnExpressionStack(
		new ClassLiteralAccess(intStack[intPtr--],
		getTypeReference(intStack[intPtr--])));
}
protected void consumePrimaryNoNewArrayName() {
	// PrimaryNoNewArray ::= Name '.' 'class'
	intPtr--;
	pushOnExpressionStack(
		new ClassLiteralAccess(intStack[intPtr--],
		getTypeReference(0)));
}
protected void consumePrimaryNoNewArrayNameSuper() {
	// PrimaryNoNewArray ::= Name '.' 'super'
	pushOnExpressionStack(
		new QualifiedSuperReference(
			getTypeReference(0),
			intStack[intPtr--],
			endPosition));
}
protected void consumePrimaryNoNewArrayNameThis() {
	// PrimaryNoNewArray ::= Name '.' 'this'
	pushOnExpressionStack(
		new QualifiedThisReference(
			getTypeReference(0),
			intStack[intPtr--],
			endPosition));
}
protected void consumePrimaryNoNewArrayPrimitiveType() {
	// PrimaryNoNewArray ::= PrimitiveType '.' 'class'
	intPtr--;
	pushOnExpressionStack(
		new ClassLiteralAccess(intStack[intPtr--],
		getTypeReference(0)));
}
protected void consumePrimaryNoNewArrayThis() {
	// PrimaryNoNewArray ::= 'this'
	pushOnExpressionStack(new ThisReference(intStack[intPtr--], endPosition));
}
protected void consumePrimitiveType() {
	// Type ::= PrimitiveType
	pushOnIntStack(0);
}
protected void consumePushModifiers() {
	if ((modifiers & AccSynchronized) != 0) {
		 /* remove the starting position of the synchronized keyword
		  * we don't need it when synchronized is part of the modifiers
		  */
		intPtr--;
	}
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(modifiersSourceStart);
	resetModifiers();
}
protected void consumePushPosition() {
	// for source managment purpose
	// PushPosition ::= $empty
	pushOnIntStack(endPosition);
}
protected void consumeQualifiedName() {
	// QualifiedName ::= Name '.' SimpleName 
	/*back from the recursive loop of QualifiedName.
	Updates identifier length into the length stack*/

	identifierLengthStack[--identifierLengthPtr]++;
}
protected void consumeReferenceType() {
	// ReferenceType ::= ClassOrInterfaceType
	pushOnIntStack(0);
}
protected void consumeRestoreDiet() {
	// RestoreDiet ::= $empty
	dietInt--;
}
protected void consumeRightParen() {
	// PushRPAREN ::= ')'
	pushOnIntStack(rParenPos);
}
	// This method is part of an automatic generation : do NOT edit-modify  
	  // This method is part of an automatic generation : do NOT edit-modify  
protected void consumeRule(int act) {
  switch ( act ) {
    case 29 : // System.out.println("Type ::= PrimitiveType");
		    consumePrimitiveType();  
			break ;
 
    case 43 : // System.out.println("ReferenceType ::= ClassOrInterfaceType");
		    consumeReferenceType();   
			break ;
 
    case 52 : // System.out.println("QualifiedName ::= Name DOT SimpleName");
		    consumeQualifiedName();  
			break ;
 
    case 53 : // System.out.println("CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt");
		    consumeCompilationUnit();  
			break ;
 
    case 54 : // System.out.println("EnterCompilationUnit ::=");
		    consumeEnterCompilationUnit();  
			break ;
 
    case 66 : // System.out.println("CatchHeader ::= catch LPAREN FormalParameter RPAREN LBRACE");
		    consumeCatchHeader();  
			break ;
 
    case 68 : // System.out.println("ImportDeclarations ::= ImportDeclarations ImportDeclaration");
		    consumeImportDeclarations();  
			break ;
 
    case 70 : // System.out.println("TypeDeclarations ::= TypeDeclarations TypeDeclaration");
		    consumeTypeDeclarations();  
			break ;
 
    case 71 : // System.out.println("PackageDeclaration ::= PackageDeclarationName SEMICOLON");
		     consumePackageDeclaration();  
			break ;
 
    case 72 : // System.out.println("PackageDeclarationName ::= package Name");
		     consumePackageDeclarationName();  
			break ;
 
    case 75 : // System.out.println("SingleTypeImportDeclaration ::= SingleTypeImportDeclarationName SEMICOLON");
		    consumeSingleTypeImportDeclaration();  
			break ;
 
    case 76 : // System.out.println("SingleTypeImportDeclarationName ::= import Name");
		    consumeSingleTypeImportDeclarationName();  
			break ;
 
    case 77 : // System.out.println("TypeImportOnDemandDeclaration ::= TypeImportOnDemandDeclarationName SEMICOLON");
		    consumeTypeImportOnDemandDeclaration();  
			break ;
 
    case 78 : // System.out.println("TypeImportOnDemandDeclarationName ::= import Name DOT MULTIPLY");
		    consumeTypeImportOnDemandDeclarationName();  
			break ;
 
     case 81 : // System.out.println("TypeDeclaration ::= SEMICOLON");
		    consumeEmptyTypeDeclaration();  
			break ;
 
    case 95 : // System.out.println("ClassDeclaration ::= ClassHeader ClassBody");
		    consumeClassDeclaration();  
			break ;
 
    case 96 : // System.out.println("ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt");
		    consumeClassHeader();  
			break ;
 
    case 97 : // System.out.println("ClassHeaderName ::= Modifiersopt class Identifier");
		    consumeClassHeaderName();  
			break ;
 
    case 98 : // System.out.println("ClassHeaderExtends ::= extends ClassType");
		    consumeClassHeaderExtends();  
			break ;
 
    case 99 : // System.out.println("ClassHeaderImplements ::= implements InterfaceTypeList");
		    consumeClassHeaderImplements();  
			break ;
 
    case 101 : // System.out.println("InterfaceTypeList ::= InterfaceTypeList COMMA InterfaceType");
		    consumeInterfaceTypeList();  
			break ;
 
    case 102 : // System.out.println("InterfaceType ::= ClassOrInterfaceType");
		    consumeInterfaceType();  
			break ;
 
    case 105 : // System.out.println("ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration");
		    consumeClassBodyDeclarations();  
			break ;
 
    case 109 : // System.out.println("ClassBodyDeclaration ::= Diet NestedMethod Block");
		    consumeClassBodyDeclaration();  
			break ;
 
    case 110 : // System.out.println("Diet ::=");
		    consumeDiet();  
			break ;

    case 111 : // System.out.println("Initializer ::= Diet NestedMethod Block");
		    consumeClassBodyDeclaration();  
			break ;
 
    case 118 : // System.out.println("ClassMemberDeclaration ::= SEMICOLON");
		    consumeEmptyClassMemberDeclaration();  
			break ;

    case 119 : // System.out.println("FieldDeclaration ::= Modifiersopt Type VariableDeclarators SEMICOLON");
		    consumeFieldDeclaration();  
			break ;
 
    case 121 : // System.out.println("VariableDeclarators ::= VariableDeclarators COMMA VariableDeclarator");
		    consumeVariableDeclarators();  
			break ;
 
    case 124 : // System.out.println("EnterVariable ::=");
		    consumeEnterVariable();  
			break ;
 
    case 125 : // System.out.println("ExitVariableWithInitialization ::=");
		    consumeExitVariableWithInitialization();  
			break ;
 
    case 126 : // System.out.println("ExitVariableWithoutInitialization ::=");
		    consumeExitVariableWithoutInitialization();  
			break ;
 
    case 127 : // System.out.println("ForceNoDiet ::=");
		    consumeForceNoDiet();  
			break ;
 
    case 128 : // System.out.println("RestoreDiet ::=");
		    consumeRestoreDiet();  
			break ;
 
    case 133 : // System.out.println("MethodDeclaration ::= MethodHeader MethodBody");
		    // set to true to consume a method with a body
  consumeMethodDeclaration(true);   
			break ;
 
    case 134 : // System.out.println("AbstractMethodDeclaration ::= MethodHeader SEMICOLON");
		    // set to false to consume a method without body
  consumeMethodDeclaration(false);  
			break ;
 
    case 135 : // System.out.println("MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims");
		    consumeMethodHeader();  
			break ;
 
    case 136 : // System.out.println("MethodPushModifiersHeader ::= MethodPushModifiersHeaderName MethodHeaderParameters");
		    consumeMethodHeader();  
			break ;
 
    case 137 : // System.out.println("MethodPushModifiersHeaderName ::= Modifiers Type PushModifiers Identifier LPAREN");
		    consumeMethodPushModifiersHeaderName();  
			break ;
 
    case 138 : // System.out.println("MethodPushModifiersHeaderName ::= Type PushModifiers Identifier LPAREN");
		    consumeMethodPushModifiersHeaderName();  
			break ;
 
    case 139 : // System.out.println("MethodHeaderName ::= Modifiersopt Type Identifier LPAREN");
		    consumeMethodHeaderName();  
			break ;
 
    case 140 : // System.out.println("MethodHeaderParameters ::= FormalParameterListopt RPAREN");
		    consumeMethodHeaderParameters();  
			break ;
 
    case 141 : // System.out.println("MethodHeaderExtendedDims ::= Dimsopt");
		    consumeMethodHeaderExtendedDims();  
			break ;
 
    case 142 : // System.out.println("MethodHeaderThrowsClause ::= throws ClassTypeList");
		    consumeMethodHeaderThrowsClause();  
			break ;
 
    case 143 : // System.out.println("ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters...");
		    consumeConstructorHeader();  
			break ;
 
    case 144 : // System.out.println("ConstructorHeaderName ::= Modifiersopt Identifier LPAREN");
		    consumeConstructorHeaderName();  
			break ;
 
    case 146 : // System.out.println("FormalParameterList ::= FormalParameterList COMMA FormalParameter");
		    consumeFormalParameterList();  
			break ;
 
    case 147 : // System.out.println("FormalParameter ::= Modifiersopt Type VariableDeclaratorId");
		    // the boolean is used to know if the modifiers should be reset
 	consumeFormalParameter();  
			break ;
 
    case 149 : // System.out.println("ClassTypeList ::= ClassTypeList COMMA ClassTypeElt");
		    consumeClassTypeList();  
			break ;
 
    case 150 : // System.out.println("ClassTypeElt ::= ClassType");
		    consumeClassTypeElt();  
			break ;
 
    case 151 : // System.out.println("MethodBody ::= NestedMethod LBRACE BlockStatementsopt RBRACE");
		    consumeMethodBody();  
			break ;
 
    case 152 : // System.out.println("NestedMethod ::=");
		    consumeNestedMethod();  
			break ;
 
    case 153 : // System.out.println("StaticInitializer ::= StaticOnly Block");
		    consumeStaticInitializer();  
			break ;

    case 154 : // System.out.println("StaticOnly ::= static");
		    consumeStaticOnly();  
			break ;
 
    case 155 : // System.out.println("ConstructorDeclaration ::= ConstructorHeader ConstructorBody");
		    consumeConstructorDeclaration() ;  
			break ;
 
    case 156 : // System.out.println("ConstructorDeclaration ::= ConstructorHeader SEMICOLON");
		    consumeInvalidConstructorDeclaration() ;  
			break ;
 
    case 157 : // System.out.println("ConstructorBody ::= NestedMethod LBRACE ConstructorBlockStatementsopt RBRACE");
		    consumeConstructorBody();  
			break ;
 
    case 160 : // System.out.println("ConstructorBlockStatementsopt ::= ExplicitConstructorInvocation BlockStatements");
		     consumeConstructorBlockStatements();  
			break ;
 
    case 161 : // System.out.println("ExplicitConstructorInvocation ::= this LPAREN ArgumentListopt RPAREN SEMICOLON");
		    consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.This);  
			break ;
 
    case 162 : // System.out.println("ExplicitConstructorInvocation ::= super LPAREN ArgumentListopt RPAREN SEMICOLON");
		    consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.Super);  
			break ;
 
    case 163 : // System.out.println("ExplicitConstructorInvocation ::= Primary DOT super LPAREN ArgumentListopt RPAREN");
		    consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.Super);  
			break ;
 
    case 164 : // System.out.println("ExplicitConstructorInvocation ::= Name DOT super LPAREN ArgumentListopt RPAREN...");
		    consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.Super);  
			break ;
 
    case 165 : // System.out.println("ExplicitConstructorInvocation ::= Primary DOT this LPAREN ArgumentListopt RPAREN...");
		    consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.This);  
			break ;
 
    case 166 : // System.out.println("ExplicitConstructorInvocation ::= Name DOT this LPAREN ArgumentListopt RPAREN...");
		    consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.This);  
			break ;
 
    case 167 : // System.out.println("InterfaceDeclaration ::= InterfaceHeader InterfaceBody");
		    consumeInterfaceDeclaration();  
			break ;
 
    case 168 : // System.out.println("InterfaceHeader ::= InterfaceHeaderName InterfaceHeaderExtendsopt");
		    consumeInterfaceHeader();  
			break ;
 
    case 169 : // System.out.println("InterfaceHeaderName ::= Modifiersopt interface Identifier");
		    consumeInterfaceHeaderName();  
			break ;
 
    case 171 : // System.out.println("InterfaceHeaderExtends ::= extends InterfaceTypeList");
		    consumeInterfaceHeaderExtends();  
			break ;
 
    case 174 : // System.out.println("InterfaceMemberDeclarations ::= InterfaceMemberDeclarations...");
		    consumeInterfaceMemberDeclarations();  
			break ;
 
    case 175 : // System.out.println("InterfaceMemberDeclaration ::= SEMICOLON");
		    consumeEmptyInterfaceMemberDeclaration();  
			break ;
 
    case 178 : // System.out.println("InterfaceMemberDeclaration ::= InvalidMethodDeclaration");
		    ignoreMethodBody();  
			break ;
 
    case 179 : // System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader ConstructorBody");
		    ignoreInvalidConstructorDeclaration(true);   
			break ;
 
    case 180 : // System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader SEMICOLON");
		    ignoreInvalidConstructorDeclaration(false);   
			break ;
 
    case 186 : // System.out.println("ArrayInitializer ::= LBRACE ,opt RBRACE");
		    consumeEmptyArrayInitializer();  
			break ;
 
    case 187 : // System.out.println("ArrayInitializer ::= LBRACE VariableInitializers RBRACE");
		    consumeArrayInitializer();  
			break ;
 
    case 188 : // System.out.println("ArrayInitializer ::= LBRACE VariableInitializers COMMA RBRACE");
		    consumeArrayInitializer();  
			break ;
 
    case 190 : // System.out.println("VariableInitializers ::= VariableInitializers COMMA VariableInitializer");
		    consumeVariableInitializers();  
			break ;
 
    case 191 : // System.out.println("Block ::= OpenBlock LBRACE BlockStatementsopt RBRACE");
		    consumeBlock();  
			break ;
 
    case 192 : // System.out.println("OpenBlock ::=");
		    consumeOpenBlock() ;  
			break ;
 
    case 194 : // System.out.println("BlockStatements ::= BlockStatements BlockStatement");
		    consumeBlockStatements() ;  
			break ;
 
    case 198 : // System.out.println("BlockStatement ::= InvalidInterfaceDeclaration");
		    ignoreInterfaceDeclaration();  
			break ;
 
    case 199 : // System.out.println("LocalVariableDeclarationStatement ::= LocalVariableDeclaration SEMICOLON");
		    consumeLocalVariableDeclarationStatement();  
			break ;
 
    case 200 : // System.out.println("LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators");
		    consumeLocalVariableDeclaration();  
			break ;
 
    case 201 : // System.out.println("LocalVariableDeclaration ::= Modifiers Type PushModifiers VariableDeclarators");
		    consumeLocalVariableDeclaration();  
			break ;
 
    case 202 : // System.out.println("PushModifiers ::=");
		    consumePushModifiers();  
			break ;
 
    case 226 : // System.out.println("EmptyStatement ::= SEMICOLON");
		    consumeEmptyStatement();  
			break ;
 
    case 227 : // System.out.println("LabeledStatement ::= Identifier COLON Statement");
		    consumeStatementLabel() ;  
			break ;
 
    case 228 : // System.out.println("LabeledStatementNoShortIf ::= Identifier COLON StatementNoShortIf");
		    consumeStatementLabel() ;  
			break ;
 
     case 229 : // System.out.println("ExpressionStatement ::= StatementExpression SEMICOLON");
		    consumeExpressionStatement();  
			break ;
 
    case 237 : // System.out.println("IfThenStatement ::= if LPAREN Expression RPAREN Statement");
		    consumeStatementIfNoElse();  
			break ;
 
    case 238 : // System.out.println("IfThenElseStatement ::= if LPAREN Expression RPAREN StatementNoShortIf else...");
		    consumeStatementIfWithElse();  
			break ;
 
    case 239 : // System.out.println("IfThenElseStatementNoShortIf ::= if LPAREN Expression RPAREN StatementNoShortIf...");
		    consumeStatementIfWithElse();  
			break ;
 
    case 240 : // System.out.println("SwitchStatement ::= switch OpenBlock LPAREN Expression RPAREN SwitchBlock");
		    consumeStatementSwitch() ;  
			break ;
 
    case 241 : // System.out.println("SwitchBlock ::= LBRACE RBRACE");
		    consumeEmptySwitchBlock() ;  
			break ;
 
    case 244 : // System.out.println("SwitchBlock ::= LBRACE SwitchBlockStatements SwitchLabels RBRACE");
		    consumeSwitchBlock() ;  
			break ;
 
    case 246 : // System.out.println("SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement");
		    consumeSwitchBlockStatements() ;  
			break ;
 
    case 247 : // System.out.println("SwitchBlockStatement ::= SwitchLabels BlockStatements");
		    consumeSwitchBlockStatement() ;  
			break ;
 
    case 249 : // System.out.println("SwitchLabels ::= SwitchLabels SwitchLabel");
		    consumeSwitchLabels() ;  
			break ;
 
     case 250 : // System.out.println("SwitchLabel ::= case ConstantExpression COLON");
		    consumeCaseLabel();  
			break ;
 
     case 251 : // System.out.println("SwitchLabel ::= default COLON");
		    consumeDefaultLabel();  
			break ;
 
    case 252 : // System.out.println("WhileStatement ::= while LPAREN Expression RPAREN Statement");
		    consumeStatementWhile() ;  
			break ;
 
    case 253 : // System.out.println("WhileStatementNoShortIf ::= while LPAREN Expression RPAREN StatementNoShortIf");
		    consumeStatementWhile() ;  
			break ;
 
    case 254 : // System.out.println("DoStatement ::= do Statement while LPAREN Expression RPAREN SEMICOLON");
		    consumeStatementDo() ;  
			break ;
 
    case 255 : // System.out.println("ForStatement ::= for LPAREN ForInitopt SEMICOLON Expressionopt SEMICOLON...");
		    consumeStatementFor() ;  
			break ;
 
    case 256 : // System.out.println("ForStatementNoShortIf ::= for LPAREN ForInitopt SEMICOLON Expressionopt SEMICOLON");
		    consumeStatementFor() ;  
			break ;
 
    case 257 : // System.out.println("ForInit ::= StatementExpressionList");
		    consumeForInit() ;  
			break ;
 
    case 261 : // System.out.println("StatementExpressionList ::= StatementExpressionList COMMA StatementExpression");
		    consumeStatementExpressionList() ;  
			break ;
 
    case 262 : // System.out.println("AssertStatement ::= assert Expression SEMICOLON");
		    consumeSimpleAssertStatement() ;  
			break ;
 
    case 263 : // System.out.println("AssertStatement ::= assert Expression COLON Expression SEMICOLON");
		    consumeAssertStatement() ;  
			break ;
 
    case 264 : // System.out.println("BreakStatement ::= break SEMICOLON");
		    consumeStatementBreak() ;  
			break ;
 
    case 265 : // System.out.println("BreakStatement ::= break Identifier SEMICOLON");
		    consumeStatementBreakWithLabel() ;  
			break ;
 
    case 266 : // System.out.println("ContinueStatement ::= continue SEMICOLON");
		    consumeStatementContinue() ;  
			break ;
 
    case 267 : // System.out.println("ContinueStatement ::= continue Identifier SEMICOLON");
		    consumeStatementContinueWithLabel() ;  
			break ;
 
    case 268 : // System.out.println("ReturnStatement ::= return Expressionopt SEMICOLON");
		    consumeStatementReturn() ;  
			break ;
 
    case 269 : // System.out.println("ThrowStatement ::= throw Expression SEMICOLON");
		    consumeStatementThrow();
 
			break ;
 
    case 270 : // System.out.println("SynchronizedStatement ::= OnlySynchronized LPAREN Expression RPAREN Block");
		    consumeStatementSynchronized();  
			break ;
 
    case 271 : // System.out.println("OnlySynchronized ::= synchronized");
		    consumeOnlySynchronized();  
			break ;
 
    case 272 : // System.out.println("TryStatement ::= try Block Catches");
		    consumeStatementTry(false);  
			break ;
 
    case 273 : // System.out.println("TryStatement ::= try Block Catchesopt Finally");
		    consumeStatementTry(true);  
			break ;
 
    case 275 : // System.out.println("Catches ::= Catches CatchClause");
		    consumeCatches();  
			break ;
 
    case 276 : // System.out.println("CatchClause ::= catch LPAREN FormalParameter RPAREN Block");
		    consumeStatementCatch() ;  
			break ;
 
    case 278 : // System.out.println("PushLPAREN ::= LPAREN");
		    consumeLeftParen();  
			break ;
 
    case 279 : // System.out.println("PushRPAREN ::= RPAREN");
		    consumeRightParen();  
			break ;
 
    case 283 : // System.out.println("PrimaryNoNewArray ::= this");
		    consumePrimaryNoNewArrayThis();  
			break ;
 
    case 284 : // System.out.println("PrimaryNoNewArray ::= PushLPAREN Expression PushRPAREN");
		    consumePrimaryNoNewArray();  
			break ;
 
    case 287 : // System.out.println("PrimaryNoNewArray ::= Name DOT this");
		    consumePrimaryNoNewArrayNameThis();  
			break ;
 
    case 288 : // System.out.println("PrimaryNoNewArray ::= Name DOT super");
		    consumePrimaryNoNewArrayNameSuper();  
			break ;
 
    case 289 : // System.out.println("PrimaryNoNewArray ::= Name DOT class");
		    consumePrimaryNoNewArrayName();  
			break ;
 
    case 290 : // System.out.println("PrimaryNoNewArray ::= ArrayType DOT class");
		    consumePrimaryNoNewArrayArrayType();  
			break ;
 
    case 291 : // System.out.println("PrimaryNoNewArray ::= PrimitiveType DOT class");
		    consumePrimaryNoNewArrayPrimitiveType();  
			break ;
 
    case 294 : // System.out.println("AllocationHeader ::= new ClassType LPAREN ArgumentListopt RPAREN");
		    consumeAllocationHeader();  
			break ;
 
    case 295 : // System.out.println("ClassInstanceCreationExpression ::= new ClassType LPAREN ArgumentListopt RPAREN...");
		    consumeClassInstanceCreationExpression();  
			break ;
 
    case 296 : // System.out.println("ClassInstanceCreationExpression ::= Primary DOT new SimpleName LPAREN...");
		    consumeClassInstanceCreationExpressionQualified() ;  
			break ;
 
    case 297 : // System.out.println("ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName new...");
		    consumeClassInstanceCreationExpressionQualified() ;  
			break ;
 
    case 298 : // System.out.println("ClassInstanceCreationExpressionName ::= Name DOT");
		    consumeClassInstanceCreationExpressionName() ;  
			break ;
 
    case 299 : // System.out.println("ClassBodyopt ::=");
		    consumeClassBodyopt();  
			break ;
 
    case 301 : // System.out.println("EnterAnonymousClassBody ::=");
		    consumeEnterAnonymousClassBody();  
			break ;
 
    case 303 : // System.out.println("ArgumentList ::= ArgumentList COMMA Expression");
		    consumeArgumentList();  
			break ;
 
    case 304 : // System.out.println("ArrayCreationExpression ::= new PrimitiveType DimWithOrWithOutExprs...");
		    consumeArrayCreationExpression();  
			break ;
 
    case 305 : // System.out.println("ArrayCreationExpression ::= new ClassOrInterfaceType DimWithOrWithOutExprs...");
		    consumeArrayCreationExpression();  
			break ;
 
    case 307 : // System.out.println("DimWithOrWithOutExprs ::= DimWithOrWithOutExprs DimWithOrWithOutExpr");
		    consumeDimWithOrWithOutExprs();  
			break ;
 
     case 309 : // System.out.println("DimWithOrWithOutExpr ::= LBRACKET RBRACKET");
		    consumeDimWithOrWithOutExpr();  
			break ;
 
     case 310 : // System.out.println("Dims ::= DimsLoop");
		    consumeDims();  
			break ;
 
     case 313 : // System.out.println("OneDimLoop ::= LBRACKET RBRACKET");
		    consumeOneDimLoop();  
			break ;
 
    case 314 : // System.out.println("FieldAccess ::= Primary DOT Identifier");
		    consumeFieldAccess(false);  
			break ;
 
    case 315 : // System.out.println("FieldAccess ::= super DOT Identifier");
		    consumeFieldAccess(true);  
			break ;
 
    case 316 : // System.out.println("MethodInvocation ::= Name LPAREN ArgumentListopt RPAREN");
		    consumeMethodInvocationName();  
			break ;
 
    case 317 : // System.out.println("MethodInvocation ::= Primary DOT Identifier LPAREN ArgumentListopt RPAREN");
		    consumeMethodInvocationPrimary();  
			break ;
 
    case 318 : // System.out.println("MethodInvocation ::= super DOT Identifier LPAREN ArgumentListopt RPAREN");
		    consumeMethodInvocationSuper();  
			break ;
 
    case 319 : // System.out.println("ArrayAccess ::= Name LBRACKET Expression RBRACKET");
		    consumeArrayAccess(true);  
			break ;
 
    case 320 : // System.out.println("ArrayAccess ::= PrimaryNoNewArray LBRACKET Expression RBRACKET");
		    consumeArrayAccess(false);  
			break ;
 
    case 322 : // System.out.println("PostfixExpression ::= Name");
		    consumePostfixExpression();  
			break ;
 
    case 325 : // System.out.println("PostIncrementExpression ::= PostfixExpression PLUS_PLUS");
		    consumeUnaryExpression(OperatorExpression.PLUS,true);  
			break ;
 
    case 326 : // System.out.println("PostDecrementExpression ::= PostfixExpression MINUS_MINUS");
		    consumeUnaryExpression(OperatorExpression.MINUS,true);  
			break ;
 
    case 327 : // System.out.println("PushPosition ::=");
		    consumePushPosition();  
			break ;
 
    case 330 : // System.out.println("UnaryExpression ::= PLUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.PLUS);  
			break ;
 
    case 331 : // System.out.println("UnaryExpression ::= MINUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.MINUS);  
			break ;
 
    case 333 : // System.out.println("PreIncrementExpression ::= PLUS_PLUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.PLUS,false);  
			break ;
 
    case 334 : // System.out.println("PreDecrementExpression ::= MINUS_MINUS PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.MINUS,false);  
			break ;
 
    case 336 : // System.out.println("UnaryExpressionNotPlusMinus ::= TWIDDLE PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.TWIDDLE);  
			break ;
 
    case 337 : // System.out.println("UnaryExpressionNotPlusMinus ::= NOT PushPosition UnaryExpression");
		    consumeUnaryExpression(OperatorExpression.NOT);  
			break ;
 
    case 339 : // System.out.println("CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN UnaryExpression");
		    consumeCastExpression();  
			break ;
 
    case 340 : // System.out.println("CastExpression ::= PushLPAREN Name Dims PushRPAREN UnaryExpressionNotPlusMinus");
		    consumeCastExpression();  
			break ;
 
    case 341 : // System.out.println("CastExpression ::= PushLPAREN Expression PushRPAREN UnaryExpressionNotPlusMinus");
		    consumeCastExpressionLL1();  
			break ;
 
    case 343 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression MULTIPLY UnaryExpression");
		    consumeBinaryExpression(OperatorExpression.MULTIPLY);  
			break ;
 
    case 344 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression DIVIDE UnaryExpression");
		    consumeBinaryExpression(OperatorExpression.DIVIDE);  
			break ;
 
    case 345 : // System.out.println("MultiplicativeExpression ::= MultiplicativeExpression REMAINDER UnaryExpression");
		    consumeBinaryExpression(OperatorExpression.REMAINDER);  
			break ;
 
    case 347 : // System.out.println("AdditiveExpression ::= AdditiveExpression PLUS MultiplicativeExpression");
		    consumeBinaryExpression(OperatorExpression.PLUS);  
			break ;
 
    case 348 : // System.out.println("AdditiveExpression ::= AdditiveExpression MINUS MultiplicativeExpression");
		    consumeBinaryExpression(OperatorExpression.MINUS);  
			break ;
 
    case 350 : // System.out.println("ShiftExpression ::= ShiftExpression LEFT_SHIFT AdditiveExpression");
		    consumeBinaryExpression(OperatorExpression.LEFT_SHIFT);  
			break ;
 
    case 351 : // System.out.println("ShiftExpression ::= ShiftExpression RIGHT_SHIFT AdditiveExpression");
		    consumeBinaryExpression(OperatorExpression.RIGHT_SHIFT);  
			break ;
 
    case 352 : // System.out.println("ShiftExpression ::= ShiftExpression UNSIGNED_RIGHT_SHIFT AdditiveExpression");
		    consumeBinaryExpression(OperatorExpression.UNSIGNED_RIGHT_SHIFT);  
			break ;
 
    case 354 : // System.out.println("RelationalExpression ::= RelationalExpression LESS ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.LESS);  
			break ;
 
    case 355 : // System.out.println("RelationalExpression ::= RelationalExpression GREATER ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.GREATER);  
			break ;
 
    case 356 : // System.out.println("RelationalExpression ::= RelationalExpression LESS_EQUAL ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.LESS_EQUAL);  
			break ;
 
    case 357 : // System.out.println("RelationalExpression ::= RelationalExpression GREATER_EQUAL ShiftExpression");
		    consumeBinaryExpression(OperatorExpression.GREATER_EQUAL);  
			break ;
 
    case 358 : // System.out.println("RelationalExpression ::= RelationalExpression instanceof ReferenceType");
		    consumeInstanceOfExpression(OperatorExpression.INSTANCEOF);  
			break ;
 
    case 360 : // System.out.println("EqualityExpression ::= EqualityExpression EQUAL_EQUAL RelationalExpression");
		    consumeEqualityExpression(OperatorExpression.EQUAL_EQUAL);  
			break ;
 
    case 361 : // System.out.println("EqualityExpression ::= EqualityExpression NOT_EQUAL RelationalExpression");
		    consumeEqualityExpression(OperatorExpression.NOT_EQUAL);  
			break ;
 
    case 363 : // System.out.println("AndExpression ::= AndExpression AND EqualityExpression");
		    consumeBinaryExpression(OperatorExpression.AND);  
			break ;
 
    case 365 : // System.out.println("ExclusiveOrExpression ::= ExclusiveOrExpression XOR AndExpression");
		    consumeBinaryExpression(OperatorExpression.XOR);  
			break ;
 
    case 367 : // System.out.println("InclusiveOrExpression ::= InclusiveOrExpression OR ExclusiveOrExpression");
		    consumeBinaryExpression(OperatorExpression.OR);  
			break ;
 
    case 369 : // System.out.println("ConditionalAndExpression ::= ConditionalAndExpression AND_AND InclusiveOrExpression");
		    consumeBinaryExpression(OperatorExpression.AND_AND);  
			break ;
 
    case 371 : // System.out.println("ConditionalOrExpression ::= ConditionalOrExpression OR_OR ConditionalAndExpression");
		    consumeBinaryExpression(OperatorExpression.OR_OR);  
			break ;
 
    case 373 : // System.out.println("ConditionalExpression ::= ConditionalOrExpression QUESTION Expression COLON...");
		    consumeConditionalExpression(OperatorExpression.QUESTIONCOLON) ;  
			break ;
 
    case 376 : // System.out.println("Assignment ::= LeftHandSide AssignmentOperator AssignmentExpression");
		    consumeAssignment();  
			break ;
 
    case 378 : // System.out.println("Assignment ::= InvalidArrayInitializerAssignement");
		    ignoreExpressionAssignment(); 
			break ;
 
    case 379 : // System.out.println("LeftHandSide ::= Name");
		    consumeLeftHandSide();  
			break ;
 
    case 382 : // System.out.println("AssignmentOperator ::= EQUAL");
		    consumeAssignmentOperator(EQUAL);  
			break ;
 
    case 383 : // System.out.println("AssignmentOperator ::= MULTIPLY_EQUAL");
		    consumeAssignmentOperator(MULTIPLY);  
			break ;
 
    case 384 : // System.out.println("AssignmentOperator ::= DIVIDE_EQUAL");
		    consumeAssignmentOperator(DIVIDE);  
			break ;
 
    case 385 : // System.out.println("AssignmentOperator ::= REMAINDER_EQUAL");
		    consumeAssignmentOperator(REMAINDER);  
			break ;
 
    case 386 : // System.out.println("AssignmentOperator ::= PLUS_EQUAL");
		    consumeAssignmentOperator(PLUS);  
			break ;
 
    case 387 : // System.out.println("AssignmentOperator ::= MINUS_EQUAL");
		    consumeAssignmentOperator(MINUS);  
			break ;
 
    case 388 : // System.out.println("AssignmentOperator ::= LEFT_SHIFT_EQUAL");
		    consumeAssignmentOperator(LEFT_SHIFT);  
			break ;
 
    case 389 : // System.out.println("AssignmentOperator ::= RIGHT_SHIFT_EQUAL");
		    consumeAssignmentOperator(RIGHT_SHIFT);  
			break ;
 
    case 390 : // System.out.println("AssignmentOperator ::= UNSIGNED_RIGHT_SHIFT_EQUAL");
		    consumeAssignmentOperator(UNSIGNED_RIGHT_SHIFT);  
			break ;
 
    case 391 : // System.out.println("AssignmentOperator ::= AND_EQUAL");
		    consumeAssignmentOperator(AND);  
			break ;
 
    case 392 : // System.out.println("AssignmentOperator ::= XOR_EQUAL");
		    consumeAssignmentOperator(XOR);  
			break ;
 
    case 393 : // System.out.println("AssignmentOperator ::= OR_EQUAL");
		    consumeAssignmentOperator(OR);  
			break ;
 
    case 400 : // System.out.println("Expressionopt ::=");
		    consumeEmptyExpression();  
			break ;
 
    case 404 : // System.out.println("ImportDeclarationsopt ::=");
		    consumeEmptyImportDeclarationsopt();  
			break ;
 
    case 405 : // System.out.println("ImportDeclarationsopt ::= ImportDeclarations");
		    consumeImportDeclarationsopt();  
			break ;
 
    case 406 : // System.out.println("TypeDeclarationsopt ::=");
		    consumeEmptyTypeDeclarationsopt();  
			break ;
 
    case 407 : // System.out.println("TypeDeclarationsopt ::= TypeDeclarations");
		    consumeTypeDeclarationsopt();  
			break ;
 
    case 408 : // System.out.println("ClassBodyDeclarationsopt ::=");
		    consumeEmptyClassBodyDeclarationsopt();  
			break ;
 
    case 409 : // System.out.println("ClassBodyDeclarationsopt ::= NestedType ClassBodyDeclarations");
		    consumeClassBodyDeclarationsopt();  
			break ;
 
     case 410 : // System.out.println("Modifiersopt ::=");
		    consumeDefaultModifiers();  
			break ;
 
    case 411 : // System.out.println("Modifiersopt ::= Modifiers");
		    consumeModifiers();  
			break ;
 
    case 412 : // System.out.println("BlockStatementsopt ::=");
		    consumeEmptyBlockStatementsopt();  
			break ;
 
     case 414 : // System.out.println("Dimsopt ::=");
		    consumeEmptyDimsopt();  
			break ;
 
     case 416 : // System.out.println("ArgumentListopt ::=");
		    consumeEmptyArgumentListopt();  
			break ;
 
    case 420 : // System.out.println("FormalParameterListopt ::=");
		    consumeFormalParameterListopt();  
			break ;
 
     case 424 : // System.out.println("InterfaceMemberDeclarationsopt ::=");
		    consumeEmptyInterfaceMemberDeclarationsopt();  
			break ;
 
     case 425 : // System.out.println("InterfaceMemberDeclarationsopt ::= NestedType InterfaceMemberDeclarations");
		    consumeInterfaceMemberDeclarationsopt();  
			break ;
 
    case 426 : // System.out.println("NestedType ::=");
		    consumeNestedType();  
			break ;

     case 427 : // System.out.println("ForInitopt ::=");
		    consumeEmptyForInitopt();  
			break ;
 
     case 429 : // System.out.println("ForUpdateopt ::=");
		    consumeEmptyForUpdateopt();  
			break ;
 
     case 433 : // System.out.println("Catchesopt ::=");
		    consumeEmptyCatchesopt();  
			break ;
 
     case 435 : // System.out.println("ArrayInitializeropt ::=");
		    consumeEmptyArrayInitializeropt();  
			break ;
 
	}
} 
protected void consumeSimpleAssertStatement() {
	// AssertStatement ::= 'assert' Expression ';'
	expressionLengthPtr--;
	pushOnAstStack(new AssertStatement(expressionStack[expressionPtr--], intStack[intPtr--]));	
}
	
protected void consumeSingleTypeImportDeclaration() {
	// SingleTypeImportDeclaration ::= SingleTypeImportDeclarationName ';'

	ImportReference impt = (ImportReference) astStack[astPtr];
	// flush annotations defined prior to import statements
	impt.declarationEnd = endStatementPosition;
	impt.declarationSourceEnd = 
		this.flushAnnotationsDefinedPriorTo(impt.declarationSourceEnd); 

	// recovery
	if (currentElement != null) {
		lastCheckPoint = impt.declarationSourceEnd + 1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; 
		// used to avoid branching back into the regular automaton
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = identifierLengthStack[identifierLengthPtr--]][];
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, false));

	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
protected void consumeStatementBreak() {
	// BreakStatement ::= 'break' ';'
	// break pushs a position on intStack in case there is no label

	pushOnAstStack(new Break(null, intStack[intPtr--], endPosition));
}
protected void consumeStatementBreakWithLabel() {
	// BreakStatement ::= 'break' Identifier ';'
	// break pushs a position on intStack in case there is no label

	pushOnAstStack(
		new Break(
			identifierStack[identifierPtr--],
			intStack[intPtr--],
			endPosition)); 
	identifierLengthPtr--;
}
protected void consumeStatementCatch() {
	// CatchClause ::= 'catch' '(' FormalParameter ')'    Block

	//catch are stored directly into the Try
	//has they always comes two by two....
	//we remove one entry from the astlengthPtr.
	//The construction of the try statement must
	//then fetch the catches using  2*i and 2*i + 1

	astLengthPtr--;
	listLength = 0; // reset formalParameter counter (incremented for catch variable)
}
protected void consumeStatementContinue() {
	// ContinueStatement ::= 'continue' ';'
	// continue pushs a position on intStack in case there is no label

	pushOnAstStack(
		new Continue(
			null,
			intStack[intPtr--],
			endPosition));
}
protected void consumeStatementContinueWithLabel() {
	// ContinueStatement ::= 'continue' Identifier ';'
	// continue pushs a position on intStack in case there is no label

	pushOnAstStack(
		new Continue(
			identifierStack[identifierPtr--], 
			intStack[intPtr--], 
			endPosition)); 
	identifierLengthPtr--;
}
protected void consumeStatementDo() {
	// DoStatement ::= 'do' Statement 'while' '(' Expression ')' ';'

	//the 'while' pushes a value on intStack that we need to remove
	intPtr--;

	//optimize the push/pop
	Statement action = (Statement) astStack[astPtr];
	if (action instanceof EmptyStatement
		&& problemReporter.options.complianceLevel <= CompilerOptions.JDK1_3) {
		expressionLengthPtr--;
		astStack[astPtr] = 
			new DoStatement(
				expressionStack[expressionPtr--], 
				null, 
				intStack[intPtr--], 
				endPosition); 
	} else {
		expressionLengthPtr--;
		astStack[astPtr] = 
			new DoStatement(
				expressionStack[expressionPtr--], 
				action, 
				intStack[intPtr--], 
				endPosition); 
	}
}
protected void consumeStatementExpressionList() {
	// StatementExpressionList ::= StatementExpressionList ',' StatementExpression
	concatExpressionLists();
}
protected void consumeStatementFor() {
	// ForStatement ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' Statement
	// ForStatementNoShortIf ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf

	int length;
	Expression cond = null;
	Statement[] inits, updates;
	Statement action;
	boolean scope = true;

	//statements
	astLengthPtr--; // we need to consume it
	action = (Statement) astStack[astPtr--];
	if (action instanceof EmptyStatement
		&& problemReporter.options.complianceLevel <= CompilerOptions.JDK1_3) {
		action = null;
	}

	//updates are on the expresion stack
	if ((length = expressionLengthStack[expressionLengthPtr--]) == 0) {
		updates = null;
	} else {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			updates = new Statement[length], 
			0, 
			length); 
	}

	if (expressionLengthStack[expressionLengthPtr--] != 0)
		cond = expressionStack[expressionPtr--];

	//inits may be on two different stacks
	if ((length = astLengthStack[astLengthPtr--]) == 0) {
		inits = null;
		scope = false;
	} else {
		if (length == -1) { //on expressionStack
			scope = false;
			length = expressionLengthStack[expressionLengthPtr--];
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				inits = new Statement[length], 
				0, 
				length); 
		} else { //on astStack
			astPtr -= length;
			System.arraycopy(
				astStack, 
				astPtr + 1, 
				inits = new Statement[length], 
				0, 
				length); 
		}
	};
	if (action instanceof Block) {
		pushOnAstStack(
			new ForStatement(
				inits, 
				cond, 
				updates, 
				action, 
				scope, 
				intStack[intPtr--], 
				endStatementPosition)); 
	} else {
		pushOnAstStack(
			new ForStatement(
				inits, 
				cond, 
				updates, 
				action, 
				scope, 
				intStack[intPtr--], 
				endPosition)); 
	}
}
protected void consumeStatementIfNoElse() {
	// IfThenStatement ::=  'if' '(' Expression ')' Statement

	//optimize the push/pop
	expressionLengthPtr--;
	Statement thenStatement = (Statement) astStack[astPtr];
	if (thenStatement instanceof Block) {
		astStack[astPtr] = 
			new IfStatement(
				expressionStack[expressionPtr--], 
				thenStatement, 
				intStack[intPtr--], 
				endStatementPosition); 
	} else if (thenStatement instanceof EmptyStatement) {
		astStack[astPtr] = 
			new IfStatement(
				expressionStack[expressionPtr--], 
				Block.None, 
				intStack[intPtr--], 
				endStatementPosition); 
	} else {
		astStack[astPtr] = 
			new IfStatement(
				expressionStack[expressionPtr--], 
				thenStatement, 
				intStack[intPtr--], 
				endStatementPosition); 
	}
}
protected void consumeStatementIfWithElse() {
	// IfThenElseStatement ::=  'if' '(' Expression ')' StatementNoShortIf 'else' Statement
	// IfThenElseStatementNoShortIf ::=  'if' '(' Expression ')' StatementNoShortIf 'else' StatementNoShortIf

	astLengthPtr--; // optimized {..., Then, Else } ==> {..., If }
	expressionLengthPtr--;
	//optimize the push/pop
	Statement elseStatement = (Statement) astStack[astPtr--];
	Statement thenStatement = (Statement) astStack[astPtr];
	if (elseStatement instanceof EmptyStatement) {
		elseStatement = Block.None;
	}
	if (thenStatement instanceof EmptyStatement) {
		thenStatement = Block.None;
	}
	if (elseStatement instanceof Block) {
		astStack[astPtr] = 
			new IfStatement(
				expressionStack[expressionPtr--], 
				thenStatement, 
				elseStatement, 
				intStack[intPtr--], 
				endStatementPosition); 
	} else {
		astStack[astPtr] = 
			new IfStatement(
				expressionStack[expressionPtr--], 
				thenStatement, 
				elseStatement, 
				intStack[intPtr--], 
				endStatementPosition); 
	}
}
protected void consumeStatementLabel() {
	// LabeledStatement ::= 'Identifier' ':' Statement
	// LabeledStatementNoShortIf ::= 'Identifier' ':' StatementNoShortIf

	//optimize push/pop

	Statement stmt = (Statement) astStack[astPtr];
	if (stmt instanceof EmptyStatement) {
		astStack[astPtr] = 
			new LabeledStatement(
				identifierStack[identifierPtr], 
				Block.None, 
				(int) (identifierPositionStack[identifierPtr--] >>> 32), 
				endStatementPosition); 
	} else {
		astStack[astPtr] = 
			new LabeledStatement(
				identifierStack[identifierPtr], 
				stmt, 
				(int) (identifierPositionStack[identifierPtr--] >>> 32), 
				endStatementPosition); 
	}
	identifierLengthPtr--;
}
protected void consumeStatementReturn() {
	// ReturnStatement ::= 'return' Expressionopt ';'
	// return pushs a position on intStack in case there is no expression

	if (expressionLengthStack[expressionLengthPtr--] != 0) {
		pushOnAstStack(
			new ReturnStatement(
				expressionStack[expressionPtr--], 
				intStack[intPtr--], 
				endPosition)
		);
	} else {
		pushOnAstStack(new ReturnStatement(null, intStack[intPtr--], endPosition));
	}
}
protected void consumeStatementSwitch() {
	// SwitchStatement ::= 'switch' OpenBlock '(' Expression ')' SwitchBlock

	//OpenBlock just makes the semantic action blockStart()
	//the block is inlined but a scope need to be created
	//if some declaration occurs.

	int length;
	SwitchStatement s = new SwitchStatement();
	expressionLengthPtr--;
	s.testExpression = expressionStack[expressionPtr--];
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		System.arraycopy(
			astStack, 
			astPtr + 1, 
			s.statements = new Statement[length], 
			0, 
			length); 
	}
	s.explicitDeclarations = realBlockStack[realBlockPtr--];
	pushOnAstStack(s);
	intPtr--; // because of OpenBlock
	s.sourceStart = intStack[intPtr--];
	s.sourceEnd = endStatementPosition;
}
protected void consumeStatementSynchronized() {
	// SynchronizedStatement ::= OnlySynchronized '(' Expression ')' Block
	//optimize the push/pop

	if (astLengthStack[astLengthPtr] == 0) {
		astLengthStack[astLengthPtr] = 1;
		expressionLengthPtr--;
		astStack[++astPtr] = 
			new SynchronizedStatement(
				expressionStack[expressionPtr--], 
				Block.None, 
				intStack[intPtr--], 
				endStatementPosition); 
	} else {
		expressionLengthPtr--;
		astStack[astPtr] = 
			new SynchronizedStatement(
				expressionStack[expressionPtr--], 
				(Block) astStack[astPtr], 
				intStack[intPtr--], 
				endStatementPosition); 
	}
	resetModifiers();
}
protected void consumeStatementThrow() {
	// ThrowStatement ::= 'throw' Expression ';'
	expressionLengthPtr--;
	pushOnAstStack(new ThrowStatement(expressionStack[expressionPtr--], intStack[intPtr--]));
}
protected void consumeStatementTry(boolean withFinally) {
	//TryStatement ::= 'try'  Block Catches
	//TryStatement ::= 'try'  Block Catchesopt Finally

	int length;
	TryStatement tryStmt = new TryStatement();
	//finally
	if (withFinally) {
		astLengthPtr--;
		tryStmt.finallyBlock = (Block) astStack[astPtr--];
	}
	//catches are handle by two <argument-block> [see statementCatch]
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		if (length == 1) {
			tryStmt.catchBlocks = new Block[] {(Block) astStack[astPtr--]};
			tryStmt.catchArguments = new Argument[] {(Argument) astStack[astPtr--]};
		} else {
			Block[] bks = (tryStmt.catchBlocks = new Block[length]);
			Argument[] args = (tryStmt.catchArguments = new Argument[length]);
			while (length-- > 0) {
				bks[length] = (Block) astStack[astPtr--];
				args[length] = (Argument) astStack[astPtr--];
			}
		}
	}
	//try
	astLengthPtr--;
	tryStmt.tryBlock = (Block) astStack[astPtr--];

	//positions
	tryStmt.sourceEnd = endStatementPosition;
	tryStmt.sourceStart = intStack[intPtr--];
	pushOnAstStack(tryStmt);
}
protected void consumeStatementWhile() {
	// WhileStatement ::= 'while' '(' Expression ')' Statement
	// WhileStatementNoShortIf ::= 'while' '(' Expression ')' StatementNoShortIf

	Statement action = (Statement) astStack[astPtr];
	expressionLengthPtr--;
	if (action instanceof Block) {
		astStack[astPtr] = 
			new WhileStatement(
				expressionStack[expressionPtr--], 
				action, 
				intStack[intPtr--], 
				endStatementPosition); 
	} else {
		if (action instanceof EmptyStatement
			&& problemReporter.options.complianceLevel <= CompilerOptions.JDK1_3) {
			astStack[astPtr] = 
				new WhileStatement(
					expressionStack[expressionPtr--], 
					null, 
					intStack[intPtr--], 
					endPosition); 
		} else {
			astStack[astPtr] = 
				new WhileStatement(
					expressionStack[expressionPtr--], 
					action, 
					intStack[intPtr--], 
					endPosition); 
		}
	}
}
protected void consumeStaticInitializer() {
	// StaticInitializer ::=  StaticOnly Block
	//push an Initializer
	//optimize the push/pop
	Initializer initializer = new Initializer((Block) astStack[astPtr], AccStatic);
	astStack[astPtr] = initializer;
	initializer.sourceEnd = endStatementPosition;	
	initializer.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition);
	nestedMethod[nestedType] --;
	initializer.declarationSourceStart = intStack[intPtr--];
	
	// recovery
	if (currentElement != null){
		lastCheckPoint = initializer.declarationSourceEnd;
		currentElement = currentElement.add(initializer, 0);
		lastIgnoredToken = -1;
	}
}
protected void consumeStaticOnly() {
	// StaticOnly ::= 'static'
	int savedModifiersSourceStart = modifiersSourceStart;
	checkAnnotation(); // might update declaration source start
	if (modifiersSourceStart >= savedModifiersSourceStart) {
		modifiersSourceStart = savedModifiersSourceStart;
	}
	pushOnIntStack(
		modifiersSourceStart >= 0 ? modifiersSourceStart : scanner.startPosition);
	jumpOverMethodBody();
	nestedMethod[nestedType]++;
	resetModifiers();

	// recovery
	if (currentElement != null){
		recoveredStaticInitializerStart = intStack[intPtr]; // remember start position only for static initializers
	}
}
protected void consumeSwitchBlock() {
	// SwitchBlock ::= '{' SwitchBlockStatements SwitchLabels '}'
	concatNodeLists();
}
protected void consumeSwitchBlockStatement() {
	// SwitchBlockStatement ::= SwitchLabels BlockStatements
	concatNodeLists();
}
protected void consumeSwitchBlockStatements() {
	// SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement
	concatNodeLists();
}
protected void consumeSwitchLabels() {
	// SwitchLabels ::= SwitchLabels SwitchLabel
	optimizedConcatNodeLists();
}
protected void consumeToken(int type) {
	
	/* remember the last consumed value */
	/* try to minimize the number of build values */
	if (scanner.wasNonExternalizedStringLiteral) {
		StringLiteral[] literals = this.scanner.nonNLSStrings;
		// could not reproduce, but this is the only NPE
		// added preventive null check see PR 9035
		if (literals != null) {
			for (int i = 0, max = literals.length; i < max; i++) {
				problemReporter().nonExternalizedStringLiteral(literals[i]);
			}
		}
		scanner.currentLine = null;
		scanner.wasNonExternalizedStringLiteral = false;
	}
	// clear the commentPtr of the scanner in case we read something different from a modifier
	switch(type) {
		case TokenNameabstract :
		case TokenNamestrictfp :
		case TokenNamefinal :
		case TokenNamenative :
		case TokenNameprivate :
		case TokenNameprotected :
		case TokenNamepublic :
		case TokenNametransient :
		case TokenNamevolatile :
		case TokenNamestatic :
		case TokenNamesynchronized :
			break;
		default:
			scanner.commentPtr = -1;
	}
	//System.out.println(scanner.toStringAction(type));
	switch (type) {
		case TokenNameIdentifier :
			pushIdentifier();
			if (scanner.useAssertAsAnIndentifier) {
				long positions = identifierPositionStack[identifierPtr];
				problemReporter().useAssertAsAnIdentifier((int) (positions >>> 32), (int) positions);
			}
			scanner.commentPtr = -1;
			break;
			
		case TokenNameinterface :
			adjustInterfaceModifiers();
			//'class' is pushing two int (positions) on the stack ==> 'interface' needs to do it too....
			pushOnIntStack(scanner.startPosition);
			pushOnIntStack(scanner.currentPosition - 1);			
			scanner.commentPtr = -1;
			break;
		case TokenNameabstract :
			checkAndSetModifiers(AccAbstract);
			break;
		case TokenNamestrictfp :
			checkAndSetModifiers(AccStrictfp);
			break;
		case TokenNamefinal :
			checkAndSetModifiers(AccFinal);
			break;
		case TokenNamenative :
			checkAndSetModifiers(AccNative);
			break;
		case TokenNameprivate :
			checkAndSetModifiers(AccPrivate);
			break;
		case TokenNameprotected :
			checkAndSetModifiers(AccProtected);
			break;
		case TokenNamepublic :
			checkAndSetModifiers(AccPublic);
			break;
		case TokenNametransient :
			checkAndSetModifiers(AccTransient);
			break;
		case TokenNamevolatile :
			checkAndSetModifiers(AccVolatile);
			break;
		case TokenNamestatic :
			checkAndSetModifiers(AccStatic);
			break;
		case TokenNamesynchronized :
			this.synchronizedBlockSourceStart = scanner.startPosition;	
			checkAndSetModifiers(AccSynchronized);
			break;
			//==============================
		case TokenNamevoid :
			pushIdentifier(-T_void);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);
			scanner.commentPtr = -1;
			break;
			//push a default dimension while void is not part of the primitive
			//declaration baseType and so takes the place of a type without getting into
			//regular type parsing that generates a dimension on intStack
		case TokenNameboolean :
			pushIdentifier(-T_boolean);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);		
			scanner.commentPtr = -1;
			break;
		case TokenNamebyte :
			pushIdentifier(-T_byte);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			scanner.commentPtr = -1;
			break;
		case TokenNamechar :
			pushIdentifier(-T_char);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			scanner.commentPtr = -1;
			break;
		case TokenNamedouble :
			pushIdentifier(-T_double);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			scanner.commentPtr = -1;
			break;
		case TokenNamefloat :
			pushIdentifier(-T_float);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			scanner.commentPtr = -1;
			break;
		case TokenNameint :
			pushIdentifier(-T_int);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			scanner.commentPtr = -1;
			break;
		case TokenNamelong :
			pushIdentifier(-T_long);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			scanner.commentPtr = -1;
			break;
		case TokenNameshort :
			pushIdentifier(-T_short);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			scanner.commentPtr = -1;
			break;
			//==============================
		case TokenNameIntegerLiteral :
			pushOnExpressionStack(
				new IntLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			scanner.commentPtr = -1;
			break;
		case TokenNameLongLiteral :
			pushOnExpressionStack(
				new LongLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			scanner.commentPtr = -1;
			break;
		case TokenNameFloatingPointLiteral :
			pushOnExpressionStack(
				new FloatLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			scanner.commentPtr = -1;
			break;
		case TokenNameDoubleLiteral :
			pushOnExpressionStack(
				new DoubleLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			scanner.commentPtr = -1;
			break;
		case TokenNameCharacterLiteral :
			pushOnExpressionStack(
				new CharLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			scanner.commentPtr = -1;
			break;
		case TokenNameStringLiteral :
			StringLiteral stringLiteral = new StringLiteral(
					scanner.getCurrentTokenSourceString(), 
					scanner.startPosition, 
					scanner.currentPosition - 1); 
			pushOnExpressionStack(stringLiteral); 
			scanner.commentPtr = -1;
			break;
		case TokenNamefalse :
			pushOnExpressionStack(
				new FalseLiteral(scanner.startPosition, scanner.currentPosition - 1)); 
			scanner.commentPtr = -1;
			break;
		case TokenNametrue :
			pushOnExpressionStack(
				new TrueLiteral(scanner.startPosition, scanner.currentPosition - 1)); 
			break;
		case TokenNamenull :
			pushOnExpressionStack(
				new NullLiteral(scanner.startPosition, scanner.currentPosition - 1)); 
			break;
			//============================
		case TokenNamesuper :
		case TokenNamethis :
			endPosition = scanner.currentPosition - 1;
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNameassert :
		case TokenNameimport :
		case TokenNamepackage :
		case TokenNamethrow :
		case TokenNamenew :
		case TokenNamedo :
		case TokenNameif :
		case TokenNamefor :
		case TokenNameswitch :
		case TokenNametry :
		case TokenNamewhile :
		case TokenNamebreak :
		case TokenNamecontinue :
		case TokenNamereturn :
		case TokenNamecase :
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNameclass :
			pushOnIntStack(scanner.currentPosition - 1);
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNamedefault :
			pushOnIntStack(scanner.startPosition);
			pushOnIntStack(scanner.currentPosition - 1);
			break;
			//let extra semantic action decide when to push
		case TokenNameRBRACKET :
		case TokenNamePLUS :
		case TokenNameMINUS :
		case TokenNameNOT :
		case TokenNameTWIDDLE :
			endPosition = scanner.startPosition;
			break;
		case TokenNamePLUS_PLUS :
		case TokenNameMINUS_MINUS :
			endPosition = scanner.startPosition;
			endStatementPosition = scanner.currentPosition - 1;
			break;
		case TokenNameRBRACE:
		case TokenNameSEMICOLON :
			endStatementPosition = scanner.currentPosition - 1;
			endPosition = scanner.startPosition - 1; 
			//the item is not part of the potential futur expression/statement
			break;
			// in order to handle ( expression) ////// (cast)expression///// foo(x)
		case TokenNameRPAREN :
			rParenPos = scanner.currentPosition - 1; // position of the end of right parenthesis (in case of unicode \u0029) lex00101
			break;
		case TokenNameLPAREN :
			lParenPos = scanner.startPosition;
			break;
			//  case TokenNameQUESTION  :
			//  case TokenNameCOMMA :
			//  case TokenNameCOLON  :
			//  case TokenNameEQUAL  :
			//  case TokenNameLBRACKET  :
			//  case TokenNameDOT :
			//  case TokenNameERROR :
			//  case TokenNameEOF  :
			//  case TokenNamecase  :
			//  case TokenNamecatch  :
			//  case TokenNameelse  :
			//  case TokenNameextends  :
			//  case TokenNamefinally  :
			//  case TokenNameimplements  :
			//  case TokenNamethrows  :
			//  case TokenNameinstanceof  :
			//  case TokenNameEQUAL_EQUAL  :
			//  case TokenNameLESS_EQUAL  :
			//  case TokenNameGREATER_EQUAL  :
			//  case TokenNameNOT_EQUAL  :
			//  case TokenNameLEFT_SHIFT  :
			//  case TokenNameRIGHT_SHIFT  :
			//  case TokenNameUNSIGNED_RIGHT_SHIFT :
			//  case TokenNamePLUS_EQUAL  :
			//  case TokenNameMINUS_EQUAL  :
			//  case TokenNameMULTIPLY_EQUAL  :
			//  case TokenNameDIVIDE_EQUAL  :
			//  case TokenNameAND_EQUAL  :
			//  case TokenNameOR_EQUAL  :
			//  case TokenNameXOR_EQUAL  :
			//  case TokenNameREMAINDER_EQUAL  :
			//  case TokenNameLEFT_SHIFT_EQUAL  :
			//  case TokenNameRIGHT_SHIFT_EQUAL  :
			//  case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL  :
			//  case TokenNameOR_OR  :
			//  case TokenNameAND_AND  :
			//  case TokenNameREMAINDER :
			//  case TokenNameXOR  :
			//  case TokenNameAND  :
			//  case TokenNameMULTIPLY :
			//  case TokenNameOR  :
			//  case TokenNameDIVIDE :
			//  case TokenNameGREATER  :
			//  case TokenNameLESS  :
	}
}
protected void consumeTypeDeclarations() {
	// TypeDeclarations ::= TypeDeclarations TypeDeclaration
	concatNodeLists();
}
protected void consumeTypeDeclarationsopt() {
	// TypeDeclarationsopt ::= TypeDeclarations
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		System.arraycopy(astStack, astPtr + 1, compilationUnit.types = new TypeDeclaration[length], 0, length);
	}
}
protected void consumeTypeImportOnDemandDeclaration() {
	// TypeImportOnDemandDeclaration ::= TypeImportOnDemandDeclarationName ';'

	ImportReference impt = (ImportReference) astStack[astPtr];
	// flush annotations defined prior to import statements
	impt.declarationEnd = endStatementPosition;
	impt.declarationSourceEnd = 
		this.flushAnnotationsDefinedPriorTo(impt.declarationSourceEnd); 

	// recovery
	if (currentElement != null) {
		lastCheckPoint = impt.declarationSourceEnd + 1;
		currentElement = currentElement.add(impt, 0);
		restartRecovery = true;
		lastIgnoredToken = -1;
		// used to avoid branching back into the regular automaton
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = identifierLengthStack[identifierLengthPtr--]][];
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, true));

	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumeUnaryExpression(int op) {
	// UnaryExpression ::= '+' PushPosition UnaryExpression
	// UnaryExpression ::= '-' PushPosition UnaryExpression
	// UnaryExpressionNotPlusMinus ::= '~' PushPosition UnaryExpression
	// UnaryExpressionNotPlusMinus ::= '!' PushPosition UnaryExpression

	//optimize the push/pop

	//handle manually the -2147483648 while it is not a real
	//computation of an - and 2147483648 (notice that 2147483648
	//is Integer.MAX_VALUE+1.....)
	//Same for -9223372036854775808L ............

	//intStack have the position of the operator

	Expression r, exp = expressionStack[expressionPtr];
	if (op == MINUS) {
		if ((exp instanceof IntLiteral) && (((IntLiteral) exp).mayRepresentMIN_VALUE())) {
			r = expressionStack[expressionPtr] = new IntLiteralMinValue();
		} else {
			if ((exp instanceof LongLiteral) && (((LongLiteral) exp).mayRepresentMIN_VALUE())) {
				r = expressionStack[expressionPtr] = new LongLiteralMinValue();
			} else {
				r = expressionStack[expressionPtr] = new UnaryExpression(exp, op);
			}
		}
	} else {
		r = expressionStack[expressionPtr] = new UnaryExpression(exp, op);
	}
	r.sourceStart = intStack[intPtr--];
	r.sourceEnd = exp.sourceEnd;
}
protected void consumeUnaryExpression(int op, boolean post) {
	// PreIncrementExpression ::= '++' PushPosition UnaryExpression
	// PreDecrementExpression ::= '--' PushPosition UnaryExpression

	// ++ and -- operators
	//optimize the push/pop

	//intStack has the position of the operator when prefix

	Expression leftHandSide = expressionStack[expressionPtr];
	if (leftHandSide instanceof Reference) {
		// ++foo()++ is unvalid 
		if (post) {
			expressionStack[expressionPtr] = 
				new PostfixExpression(
					leftHandSide,
					IntLiteral.One,
					op,
					endStatementPosition); 
		} else {
			expressionStack[expressionPtr] = 
				new PrefixExpression(
					leftHandSide,
					IntLiteral.One,
					op,
					intStack[intPtr--]); 
		}
	} else {
		//the ++ or the -- is NOT taken into account if code gen proceeds
		if (!post) {
			intPtr--;
		}
		problemReporter().invalidUnaryExpression(leftHandSide);
	}
}
protected void consumeVariableDeclarators() {
	// VariableDeclarators ::= VariableDeclarators ',' VariableDeclarator
	optimizedConcatNodeLists();
}
protected void consumeVariableInitializers() {
	// VariableInitializers ::= VariableInitializers ',' VariableInitializer
	concatExpressionLists();
}
protected TypeReference copyDims(TypeReference typeRef, int dim) {
	return typeRef.copyDims(dim);
}
protected FieldDeclaration createFieldDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	return new FieldDeclaration(null, name, sourceStart, sourceEnd);
}

protected LocalDeclaration createLocalDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	return new LocalDeclaration(null, name, sourceStart, sourceEnd);
}

public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {

	CompilationUnitDeclaration parsedUnit;
	boolean old = diet;
	try {
		diet = true;
		parsedUnit = parse(sourceUnit, compilationResult);
	}
	finally {
		diet = old;
	}
	return parsedUnit;
}
protected void dispatchDeclarationInto(int length) {
	/* they are length on astStack that should go into
	   methods fields constructors lists of the typeDecl

	   Return if there is a constructor declaration in the methods declaration */
	   
	
	// Looks for the size of each array . 

	if (length == 0)
		return;
	int[] flag = new int[length + 1]; //plus one -- see <HERE>
	int size1 = 0, size2 = 0, size3 = 0;
	for (int i = length - 1; i >= 0; i--) {
		AstNode astNode = astStack[astPtr--];
		if (astNode instanceof AbstractMethodDeclaration) {
			//methods and constructors have been regrouped into one single list
			flag[i] = 3;
			size2++;
		} else {
			if (astNode instanceof TypeDeclaration) {
				flag[i] = 4;
				size3++;
			} else {
				//field
				flag[i] = 1;
				size1++;
			}
		}
	}

	//arrays creation
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	if (size1 != 0)
		typeDecl.fields = new FieldDeclaration[size1];
	if (size2 != 0)
		typeDecl.methods = new AbstractMethodDeclaration[size2];
	if (size3 != 0)
		typeDecl.memberTypes = new MemberTypeDeclaration[size3];

	//arrays fill up
	size1 = size2 = size3 = 0;
	int flagI = flag[0], start = 0;
	int length2;
	for (int end = 0; end <= length; end++) //<HERE> the plus one allows to 
		{
		if (flagI != flag[end]) //treat the last element as a ended flag.....
			{ //array copy
			switch (flagI) {
				case 1 :
					size1 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						typeDecl.fields, 
						size1 - length2, 
						length2); 
					break;
				case 3 :
					size2 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						typeDecl.methods, 
						size2 - length2, 
						length2); 
					break;
				case 4 :
					size3 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						typeDecl.memberTypes, 
						size3 - length2, 
						length2); 
					break;
			};
			flagI = flag[start = end];
		}
	}

	if (typeDecl.memberTypes != null) {
		for (int i = typeDecl.memberTypes.length - 1; i >= 0; i--) {
			typeDecl.memberTypes[i].enclosingType = typeDecl;
		}
	}
}
protected CompilationUnitDeclaration endParse(int act) {

	this.lastAct = act;

	if (currentElement != null){
		currentElement.topElement().updateParseTree();
		if (VERBOSE_RECOVERY){
			System.out.print(Util.bind("parser.syntaxRecovery")); //$NON-NLS-1$
			System.out.println("--------------------------");		 //$NON-NLS-1$
			System.out.println(compilationUnit);		
			System.out.println("----------------------------------"); //$NON-NLS-1$
		}		
	} else {
		if (diet & VERBOSE_RECOVERY){
			System.out.print(Util.bind("parser.regularParse"));	 //$NON-NLS-1$
			System.out.println("--------------------------");	 //$NON-NLS-1$
			System.out.println(compilationUnit);		
			System.out.println("----------------------------------"); //$NON-NLS-1$
		}
	}
	if (scanner.recordLineSeparator) {
		compilationUnit.compilationResult.lineSeparatorPositions = scanner.getLineEnds();
	}
	return compilationUnit;
}
/*
 * Flush annotations defined prior to a given positions.
 *
 * Note: annotations are stacked in syntactical order
 *
 * Either answer given <position>, or the end position of a comment line 
 * immediately following the <position> (same line)
 *
 * e.g.
 * void foo(){
 * } // end of method foo
 */
 
public int flushAnnotationsDefinedPriorTo(int position) {

	int lastAnnotationIndex = scanner.commentPtr;
	if (lastAnnotationIndex < 0) return position; // no comment

	// compute the index of the first obsolete comment
	int index = lastAnnotationIndex;
	int validCount = 0;
	while (index >= 0){
		int commentEnd = scanner.commentStops[index];
		if (commentEnd < 0) commentEnd = -commentEnd; // negative end position for non-javadoc comments
		if (commentEnd <= position){
			break;
		}
		index--;
		validCount++;
	}
	// if the source at <position> is immediately followed by a line comment, then
	// flush this comment and shift <position> to the comment end.
	if (validCount > 0){
		int immediateCommentEnd = -scanner.commentStops[index+1]; //non-javadoc comment end positions are negative
		if (immediateCommentEnd > 0){ // only tolerating non-javadoc comments
			// is there any line break until the end of the immediate comment ? (thus only tolerating line comment)
			immediateCommentEnd--; // comment end in one char too far
			if (scanner.getLineNumber(position) == scanner.getLineNumber(immediateCommentEnd)){
				position = immediateCommentEnd;
				validCount--; // flush this comment
				index++;
			}
		}
	}
	// position can be located in the middle of a line break
	// this is a bug on Windows platform only.
	// http://dev.eclipse.org/bugs/show_bug.cgi?id=10557
	char[] source = scanner.source;
	
	if ((position < source.length)
		&& (source[position] == '\r')
	    && ((position + 1) < source.length)
	    && (source[position + 1] == '\n')) {
		position++;
	}
	if (index < 0) return position; // no obsolete comment

	if (validCount > 0){ // move valid comment infos, overriding obsolete comment infos
		System.arraycopy(scanner.commentStarts, index + 1, scanner.commentStarts, 0, validCount);
		System.arraycopy(scanner.commentStops, index + 1, scanner.commentStops, 0, validCount);		
	}
	scanner.commentPtr = validCount - 1;
	return position;
}
public final int getFirstToken() {
	// the first token is a virtual token that
	// allows the parser to parse several goals
	// even if they aren't LALR(1)....
	// Goal ::= '++' CompilationUnit
	// Goal ::= '--' MethodBody
	// Goal ::= '==' ConstructorBody
	// -- Initializer
	// Goal ::= '>>' StaticInitializer
	// Goal ::= '>>' Block
	// -- error recovery
	// Goal ::= '>>>' Headers
	// Goal ::= '*' BlockStatements
	// Goal ::= '*' MethodPushModifiersHeader
	// -- JDOM
	// Goal ::= '&&' FieldDeclaration
	// Goal ::= '||' ImportDeclaration
	// Goal ::= '?' PackageDeclaration
	// Goal ::= '+' TypeDeclaration
	// Goal ::= '/' GenericMethodDeclaration
	// Goal ::= '&' ClassBodyDeclaration
	// -- code snippet
	// Goal ::= '%' Expression
	// -- completion parser
	// Goal ::= '!' ConstructorBlockStatementsopt
	// Goal ::= '~' BlockStatementsopt
	
	return firstToken;
}
/*
 * Answer back an array of sourceStart/sourceEnd positions of the available JavaDoc comments.
 * The array is a flattened structure: 2*n entries with consecutives start and end positions.
 *
 * If no JavaDoc is available, then null is answered instead of an empty array.
 *
 * e.g. { 10, 20, 25, 45 }  --> javadoc1 from 10 to 20, javadoc2 from 25 to 45
 */
public int[] getJavaDocPositions() {

	int javadocCount = 0;
	for (int i = 0, max = scanner.commentPtr; i <= max; i++){
		// javadoc only (non javadoc comment have negative end positions.)
		if (scanner.commentStops[i] > 0){
			javadocCount++;
		}
	}
	if (javadocCount == 0) return null;

	int[] positions = new int[2*javadocCount];
	int index = 0;
	for (int i = 0, max = scanner.commentPtr; i <= max; i++){
		// javadoc only (non javadoc comment have negative end positions.)
		if (scanner.commentStops[i] > 0){
			positions[index++] = scanner.commentStarts[i];
			positions[index++] = scanner.commentStops[i]-1; //stop is one over			
		}
	}
	return positions;
}
	protected void getMethodBodies(CompilationUnitDeclaration unit) {
		//fill the methods bodies in order for the code to be generated

		if (unit == null) return;
		
		if (unit.ignoreMethodBodies) {
			unit.ignoreFurtherInvestigation = true;
			return;
			// if initial diet parse did not work, no need to dig into method bodies.
		}

		//real parse of the method....
		this.scanner.setSource(
			unit.compilationResult.compilationUnit.getContents());
		if (unit.types != null) {
			for (int i = unit.types.length; --i >= 0;)
				unit.types[i].parseMethod(this, unit);
		}
	}
protected TypeReference getTypeReference(int dim) { /* build a Reference on a variable that may be qualified or not
This variable is a type reference and dim will be its dimensions*/

	int length;
	TypeReference ref;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		if (dim == 0) {
			ref = 
				new SingleTypeReference(
					identifierStack[identifierPtr], 
					identifierPositionStack[identifierPtr--]); 
		} else {
			ref = 
				new ArrayTypeReference(
					identifierStack[identifierPtr], 
					dim, 
					identifierPositionStack[identifierPtr--]); 
			ref.sourceEnd = endPosition;			
		}
	} else {
		if (length < 0) { //flag for precompiled type reference on base types
			ref = TypeReference.baseTypeReference(-length, dim);
			ref.sourceStart = intStack[intPtr--];
			if (dim == 0) {
				ref.sourceEnd = intStack[intPtr--];
			} else {
				intPtr--;
				ref.sourceEnd = endPosition;
			}
		} else { //Qualified variable reference
			char[][] tokens = new char[length][];
			identifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				identifierPositionStack, 
				identifierPtr + 1, 
				positions, 
				0, 
				length); 
			if (dim == 0) {
				ref = new QualifiedTypeReference(tokens, positions);
			} else {
				ref = new ArrayQualifiedTypeReference(tokens, dim, positions);
				ref.sourceEnd = endPosition;
			}
		}
	};
	return ref;
}
protected Expression getTypeReference(Expression exp) {
	
	exp.bits &= ~AstNode.RestrictiveFlagMASK;
	exp.bits |= TYPE;
	return exp;
}
protected NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int length;
	NameReference ref;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1)
		// single variable reference
		ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
	else
		//Qualified variable reference
		{
		char[][] tokens = new char[length][];
		identifierPtr -= length;
		System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
		ref = 
			new QualifiedNameReference(tokens, 
				(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
				(int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	};
	return ref;
}
protected NameReference getUnspecifiedReferenceOptimized() {
	/* build a (unspecified) NameReference which may be qualified
	The optimization occurs for qualified reference while we are
	certain in this case the last item of the qualified name is
	a field access. This optimization is IMPORTANT while it results
	that when a NameReference is build, the type checker should always
	look for that it is not a type reference */

	int length;
	NameReference ref;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
		ref.bits &= ~AstNode.RestrictiveFlagMASK;
		ref.bits |= LOCAL | FIELD;
		return ref;
	}

	//Qualified-variable-reference
	//In fact it is variable-reference DOT field-ref , but it would result in a type
	//conflict tha can be only reduce by making a superclass (or inetrface ) between
	//nameReference and FiledReference or putting FieldReference under NameReference
	//or else..........This optimisation is not really relevant so just leave as it is

	char[][] tokens = new char[length][];
	identifierPtr -= length;
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	ref = new QualifiedNameReference(
			tokens, 
			(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
			(int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	ref.bits &= ~AstNode.RestrictiveFlagMASK;
	ref.bits |= LOCAL | FIELD;
	return ref;
}
public void goForBlockStatementsOrMethodHeaders() {
	//tells the scanner to go for block statements or method headers parsing 

	firstToken = TokenNameMULTIPLY;
	scanner.recordLineSeparator = false;
}
public void goForClassBodyDeclarations() {
	//tells the scanner to go for any body declarations parsing

	firstToken = TokenNameAND;
	scanner.recordLineSeparator = true;
}
public void goForCompilationUnit(){
	//tells the scanner to go for compilation unit parsing

	firstToken = TokenNamePLUS_PLUS ;
	scanner.linePtr = -1;	
	scanner.recordLineSeparator = true;
	scanner.currentLine= null;
	scanner.lines= new ArrayList();
}
public void goForConstructorBody(){
	//tells the scanner to go for compilation unit parsing

	firstToken = TokenNameEQUAL_EQUAL ;
	scanner.recordLineSeparator = false;
}
public void goForExpression() {
	//tells the scanner to go for an expression parsing

	firstToken = TokenNameREMAINDER;
	scanner.recordLineSeparator = false;
}
public void goForFieldDeclaration(){
	//tells the scanner to go for field declaration parsing

	firstToken = TokenNameAND_AND ;
	scanner.recordLineSeparator = true;
}
public void goForGenericMethodDeclaration(){
	//tells the scanner to go for generic method declarations parsing

	firstToken = TokenNameDIVIDE;
	scanner.recordLineSeparator = true;
}
public void goForHeaders(){
	//tells the scanner to go for headers only parsing

	firstToken = TokenNameUNSIGNED_RIGHT_SHIFT;
	scanner.recordLineSeparator = true;
}
public void goForImportDeclaration(){
	//tells the scanner to go for import declaration parsing

	firstToken = TokenNameOR_OR ;
	scanner.recordLineSeparator = true;
}
public void goForInitializer(){
	//tells the scanner to go for initializer parsing

	firstToken = TokenNameRIGHT_SHIFT ;
	scanner.recordLineSeparator = false;
}
public void goForMethodBody(){
	//tells the scanner to go for method body parsing

	firstToken = TokenNameMINUS_MINUS ;
	scanner.recordLineSeparator = false;
}
public void goForPackageDeclaration() {
	//tells the scanner to go for package declaration parsing

	firstToken = TokenNameQUESTION;
	scanner.recordLineSeparator = true;
}
public void goForTypeDeclaration() {
	//tells the scanner to go for type (interface or class) declaration parsing

	firstToken = TokenNamePLUS;
	scanner.recordLineSeparator = true;
}
public final static void grammar(){
/*
--main options
%options ACTION, AN=JavaAction.java, GP=java, 
%options FILE-PREFIX=java, ESCAPE=$, PREFIX=TokenName, OUTPUT-SIZE=125 ,
%options NOGOTO-DEFAULT, SINGLE-PRODUCTIONS, LALR=1 , TABLE=TIME , 

--error recovering options.....
%options ERROR_MAPS 

--grammar understanding options
%options first follow
%options TRACE=FULL ,
%options VERBOSE

--Usefull macros helping reading/writing semantic actions
$Define 
$putCase 
/.    case $rule_number : // System.out.println("$rule_text");  
		   ./

$break
/. 
			break ;
./

-- here it starts really ------------------------------------------
$Terminals

	Identifier

	abstract assert boolean break byte case catch char class 
	continue default do double else extends false final finally float
	for if implements import instanceof int
	interface long native new null package private
	protected public return short static strictfp super switch
	synchronized this throw throws transient true try void
	volatile while

	IntegerLiteral
	LongLiteral
	FloatingPointLiteral
	DoubleLiteral
	CharacterLiteral
	StringLiteral

	PLUS_PLUS
	MINUS_MINUS
	EQUAL_EQUAL
	LESS_EQUAL
	GREATER_EQUAL
	NOT_EQUAL
	LEFT_SHIFT
	RIGHT_SHIFT
	UNSIGNED_RIGHT_SHIFT
	PLUS_EQUAL
	MINUS_EQUAL
	MULTIPLY_EQUAL
	DIVIDE_EQUAL
	AND_EQUAL
	OR_EQUAL
	XOR_EQUAL
	REMAINDER_EQUAL
	LEFT_SHIFT_EQUAL
	RIGHT_SHIFT_EQUAL
	UNSIGNED_RIGHT_SHIFT_EQUAL
	OR_OR
	AND_AND
	PLUS
	MINUS
	NOT
	REMAINDER
	XOR
	AND
	MULTIPLY
	OR
	TWIDDLE
	DIVIDE
	GREATER
	LESS
	LPAREN
	RPAREN
	LBRACE
	RBRACE
	LBRACKET
	RBRACKET
	SEMICOLON
	QUESTION
	COLON
	COMMA
	DOT
	EQUAL

--    BodyMarker

$Alias

	'++'   ::= PLUS_PLUS
	'--'   ::= MINUS_MINUS
	'=='   ::= EQUAL_EQUAL
	'<='   ::= LESS_EQUAL
	'>='   ::= GREATER_EQUAL
	'!='   ::= NOT_EQUAL
	'<<'   ::= LEFT_SHIFT
	'>>'   ::= RIGHT_SHIFT
	'>>>'  ::= UNSIGNED_RIGHT_SHIFT
	'+='   ::= PLUS_EQUAL
	'-='   ::= MINUS_EQUAL
	'*='   ::= MULTIPLY_EQUAL
	'/='   ::= DIVIDE_EQUAL
	'&='   ::= AND_EQUAL
	'|='   ::= OR_EQUAL
	'^='   ::= XOR_EQUAL
	'%='   ::= REMAINDER_EQUAL
	'<<='  ::= LEFT_SHIFT_EQUAL
	'>>='  ::= RIGHT_SHIFT_EQUAL
	'>>>=' ::= UNSIGNED_RIGHT_SHIFT_EQUAL
	'||'   ::= OR_OR
	'&&'   ::= AND_AND

	'+'    ::= PLUS
	'-'    ::= MINUS
	'!'    ::= NOT
	'%'    ::= REMAINDER
	'^'    ::= XOR
	'&'    ::= AND
	'*'    ::= MULTIPLY
	'|'    ::= OR
	'~'    ::= TWIDDLE
	'/'    ::= DIVIDE
	'>'    ::= GREATER
	'<'    ::= LESS
	'('    ::= LPAREN
	')'    ::= RPAREN
	'{'    ::= LBRACE
	'}'    ::= RBRACE
	'['    ::= LBRACKET
	']'    ::= RBRACKET
	';'    ::= SEMICOLON
	'?'    ::= QUESTION
	':'    ::= COLON
	','    ::= COMMA
	'.'    ::= DOT
	'='    ::= EQUAL
	
$Start
	Goal

$Rules

/. // This method is part of an automatic generation : do NOT edit-modify  
protected void consumeRule(int act) {
  switch ( act ) {
./



Goal ::= '++' CompilationUnit
Goal ::= '--' MethodBody
Goal ::= '==' ConstructorBody
-- Initializer
Goal ::= '>>' StaticInitializer
Goal ::= '>>' Initializer
-- error recovery
Goal ::= '>>>' Headers
Goal ::= '*' BlockStatements
Goal ::= '*' MethodPushModifiersHeader
Goal ::= '*' CatchHeader
-- JDOM
Goal ::= '&&' FieldDeclaration
Goal ::= '||' ImportDeclaration
Goal ::= '?' PackageDeclaration
Goal ::= '+' TypeDeclaration
Goal ::= '/' GenericMethodDeclaration
Goal ::= '&' ClassBodyDeclaration
-- code snippet
Goal ::= '%' Expression
-- completion parser
Goal ::= '!' ConstructorBlockStatementsopt
Goal ::= '~' BlockStatementsopt

Literal -> IntegerLiteral
Literal -> LongLiteral
Literal -> FloatingPointLiteral
Literal -> DoubleLiteral
Literal -> CharacterLiteral
Literal -> StringLiteral
Literal -> null
Literal -> BooleanLiteral
BooleanLiteral -> true
BooleanLiteral -> false

-------------------------------------------------------------
-------------------------------------------------------------
--a Type results in both a push of its dimension(s) and its name(s).

Type ::= PrimitiveType
 /.$putCase consumePrimitiveType(); $break ./
Type -> ReferenceType

PrimitiveType -> NumericType
NumericType -> IntegralType
NumericType -> FloatingPointType

PrimitiveType -> 'boolean'
PrimitiveType -> 'void'
IntegralType -> 'byte'
IntegralType -> 'short'
IntegralType -> 'int'
IntegralType -> 'long'
IntegralType -> 'char'
FloatingPointType -> 'float'
FloatingPointType -> 'double'

ReferenceType ::= ClassOrInterfaceType
/.$putCase consumeReferenceType();  $break ./
ReferenceType -> ArrayType -- here a push of dimensions is done, that explains the two previous push 0

ClassOrInterfaceType -> Name

--
-- These rules have been rewritten to avoid some conflicts introduced
-- by adding the 1.1 features
--
-- ArrayType ::= PrimitiveType '[' ']'
-- ArrayType ::= Name '[' ']'
-- ArrayType ::= ArrayType '[' ']'
--

ArrayType ::= PrimitiveType Dims
ArrayType ::= Name Dims

ClassType -> ClassOrInterfaceType


--------------------------------------------------------------
--------------------------------------------------------------

Name -> SimpleName
Name -> QualifiedName

SimpleName -> 'Identifier'

QualifiedName ::= Name '.' SimpleName 
/.$putCase consumeQualifiedName(); $break ./

CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt TypeDeclarationsopt
/.$putCase consumeCompilationUnit(); $break ./

EnterCompilationUnit ::= $empty
/.$putCase consumeEnterCompilationUnit(); $break ./

Headers -> Header
Headers ::= Headers Header

Header -> ImportDeclaration
Header -> PackageDeclaration
Header -> ClassHeader
Header -> InterfaceHeader
Header -> StaticInitializer
Header -> MethodHeader
Header -> ConstructorHeader
Header -> FieldDeclaration
Header -> AllocationHeader

CatchHeader ::= 'catch' '(' FormalParameter ')' '{'
/.$putCase consumeCatchHeader(); $break ./

ImportDeclarations -> ImportDeclaration
ImportDeclarations ::= ImportDeclarations ImportDeclaration 
/.$putCase consumeImportDeclarations(); $break ./

TypeDeclarations -> TypeDeclaration
TypeDeclarations ::= TypeDeclarations TypeDeclaration
/.$putCase consumeTypeDeclarations(); $break ./

PackageDeclaration ::= PackageDeclarationName ';'
/.$putCase  consumePackageDeclaration(); $break ./

PackageDeclarationName ::= 'package' Name
/.$putCase  consumePackageDeclarationName(); $break ./

ImportDeclaration -> SingleTypeImportDeclaration
ImportDeclaration -> TypeImportOnDemandDeclaration

SingleTypeImportDeclaration ::= SingleTypeImportDeclarationName ';'
/.$putCase consumeSingleTypeImportDeclaration(); $break ./
			  
SingleTypeImportDeclarationName ::= 'import' Name
/.$putCase consumeSingleTypeImportDeclarationName(); $break ./
			  
TypeImportOnDemandDeclaration ::= TypeImportOnDemandDeclarationName ';'
/.$putCase consumeTypeImportOnDemandDeclaration(); $break ./

TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
/.$putCase consumeTypeImportOnDemandDeclarationName(); $break ./

TypeDeclaration -> ClassDeclaration
TypeDeclaration -> InterfaceDeclaration
-- this declaration in part of a list od declaration and we will
-- use and optimized list length calculation process 
-- thus we decrement the number while it will be incremend.....
TypeDeclaration ::= ';' 
/. $putCase consumeEmptyTypeDeclaration(); $break ./

--18.7 Only in the LALR(1) Grammar

Modifiers ::= Modifier
Modifiers ::= Modifiers Modifier

Modifier -> 'public' 
Modifier -> 'protected'
Modifier -> 'private'
Modifier -> 'static'
Modifier -> 'abstract'
Modifier -> 'final'
Modifier -> 'native'
Modifier -> 'synchronized'
Modifier -> 'transient'
Modifier -> 'volatile'
Modifier -> 'strictfp'

--18.8 Productions from 8: Class Declarations
--ClassModifier ::=
--      'abstract'
--    | 'final'
--    | 'public'
--18.8.1 Productions from 8.1: Class Declarations

ClassDeclaration ::= ClassHeader ClassBody
/.$putCase consumeClassDeclaration(); $break ./

ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt
/.$putCase consumeClassHeader(); $break ./

ClassHeaderName ::= Modifiersopt 'class' 'Identifier'
/.$putCase consumeClassHeaderName(); $break ./

ClassHeaderExtends ::= 'extends' ClassType
/.$putCase consumeClassHeaderExtends(); $break ./

ClassHeaderImplements ::= 'implements' InterfaceTypeList
/.$putCase consumeClassHeaderImplements(); $break ./

InterfaceTypeList -> InterfaceType
InterfaceTypeList ::= InterfaceTypeList ',' InterfaceType
/.$putCase consumeInterfaceTypeList(); $break ./

InterfaceType ::= ClassOrInterfaceType
/.$putCase consumeInterfaceType(); $break ./

ClassBody ::= '{' ClassBodyDeclarationsopt '}'

ClassBodyDeclarations ::= ClassBodyDeclaration
ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration
/.$putCase consumeClassBodyDeclarations(); $break ./

ClassBodyDeclaration -> ClassMemberDeclaration
ClassBodyDeclaration -> StaticInitializer
ClassBodyDeclaration -> ConstructorDeclaration
--1.1 feature
ClassBodyDeclaration ::= Diet NestedMethod Block
/.$putCase consumeClassBodyDeclaration(); $break ./
Diet ::= $empty
/.$putCase consumeDiet(); $break./

Initializer ::= Diet NestedMethod Block
/.$putCase consumeClassBodyDeclaration(); $break ./

ClassMemberDeclaration -> FieldDeclaration
ClassMemberDeclaration -> MethodDeclaration
--1.1 feature
ClassMemberDeclaration -> ClassDeclaration
--1.1 feature
ClassMemberDeclaration -> InterfaceDeclaration

-- Empty declarations are not valid Java ClassMemberDeclarations.
-- However, since the current (2/14/97) Java compiler accepts them 
-- (in fact, some of the official tests contain this erroneous
-- syntax)

GenericMethodDeclaration -> MethodDeclaration
GenericMethodDeclaration -> ConstructorDeclaration

ClassMemberDeclaration ::= ';'
/.$putCase consumeEmptyClassMemberDeclaration(); $break./

--18.8.2 Productions from 8.3: Field Declarations
--VariableModifier ::=
--      'public'
--    | 'protected'
--    | 'private'
--    | 'static'
--    | 'final'
--    | 'transient'
--    | 'volatile'

FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
/.$putCase consumeFieldDeclaration(); $break ./

VariableDeclarators -> VariableDeclarator 
VariableDeclarators ::= VariableDeclarators ',' VariableDeclarator
/.$putCase consumeVariableDeclarators(); $break ./

VariableDeclarator ::= VariableDeclaratorId EnterVariable ExitVariableWithoutInitialization

VariableDeclarator ::= VariableDeclaratorId EnterVariable '=' ForceNoDiet VariableInitializer RestoreDiet ExitVariableWithInitialization

EnterVariable ::= $empty
/.$putCase consumeEnterVariable(); $break ./

ExitVariableWithInitialization ::= $empty
/.$putCase consumeExitVariableWithInitialization(); $break ./

ExitVariableWithoutInitialization ::= $empty
/.$putCase consumeExitVariableWithoutInitialization(); $break ./

ForceNoDiet ::= $empty
/.$putCase consumeForceNoDiet(); $break ./
RestoreDiet ::= $empty
/.$putCase consumeRestoreDiet(); $break ./

VariableDeclaratorId ::= 'Identifier' Dimsopt

VariableInitializer -> Expression
VariableInitializer -> ArrayInitializer

--18.8.3 Productions from 8.4: Method Declarations
--MethodModifier ::=
--      'public'
--    | 'protected'
--    | 'private'
--    | 'static'
--    | 'abstract'
--    | 'final'
--    | 'native'
--    | 'synchronized'
--

MethodDeclaration -> AbstractMethodDeclaration
MethodDeclaration ::= MethodHeader MethodBody 
/.$putCase // set to true to consume a method with a body
  consumeMethodDeclaration(true);  $break ./

AbstractMethodDeclaration ::= MethodHeader ';'
/.$putCase // set to false to consume a method without body
  consumeMethodDeclaration(false); $break ./

MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims MethodHeaderThrowsClauseopt
/.$putCase consumeMethodHeader(); $break ./

MethodPushModifiersHeader ::= MethodPushModifiersHeaderName MethodHeaderParameters MethodHeaderExtendedDims MethodHeaderThrowsClauseopt
/.$putCase consumeMethodHeader(); $break ./

MethodPushModifiersHeaderName ::= Modifiers Type PushModifiers 'Identifier' '(' 
/.$putCase consumeMethodPushModifiersHeaderName(); $break ./

MethodPushModifiersHeaderName ::= Type PushModifiers 'Identifier' '(' 
/.$putCase consumeMethodPushModifiersHeaderName(); $break ./

MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
/.$putCase consumeMethodHeaderName(); $break ./

MethodHeaderParameters ::= FormalParameterListopt ')'
/.$putCase consumeMethodHeaderParameters(); $break ./

MethodHeaderExtendedDims ::= Dimsopt
/.$putCase consumeMethodHeaderExtendedDims(); $break ./

MethodHeaderThrowsClause ::= 'throws' ClassTypeList
/.$putCase consumeMethodHeaderThrowsClause(); $break ./

ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt
/.$putCase consumeConstructorHeader(); $break ./

ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('
/.$putCase consumeConstructorHeaderName(); $break ./

FormalParameterList -> FormalParameter
FormalParameterList ::= FormalParameterList ',' FormalParameter
/.$putCase consumeFormalParameterList(); $break ./

--1.1 feature
FormalParameter ::= Modifiersopt Type VariableDeclaratorId
/.$putCase // the boolean is used to know if the modifiers should be reset
 	consumeFormalParameter(); $break ./

ClassTypeList -> ClassTypeElt
ClassTypeList ::= ClassTypeList ',' ClassTypeElt
/.$putCase consumeClassTypeList(); $break ./

ClassTypeElt ::= ClassType
/.$putCase consumeClassTypeElt(); $break ./


MethodBody ::= NestedMethod '{' BlockStatementsopt '}' 
/.$putCase consumeMethodBody(); $break ./

NestedMethod ::= $empty
/.$putCase consumeNestedMethod(); $break ./

--18.8.4 Productions from 8.5: Static Initializers

StaticInitializer ::=  StaticOnly Block
/.$putCase consumeStaticInitializer(); $break./

StaticOnly ::= 'static'
/.$putCase consumeStaticOnly(); $break ./

--18.8.5 Productions from 8.6: Constructor Declarations
--ConstructorModifier ::=
--      'public'
--    | 'protected'
--    | 'private'
--
--
ConstructorDeclaration ::= ConstructorHeader ConstructorBody
/.$putCase consumeConstructorDeclaration() ; $break ./ 

-- These rules are added to be able to parse constructors with no body
ConstructorDeclaration ::= ConstructorHeader ';'
/.$putCase consumeInvalidConstructorDeclaration() ; $break ./ 

-- the rules ExplicitConstructorInvocationopt has been expanded
-- in the rule below in order to make the grammar lalr(1).
-- ConstructorBody ::= '{' ExplicitConstructorInvocationopt BlockStatementsopt '}'
-- Other inlining has occured into the next rule too....

ConstructorBody ::= NestedMethod  '{' ConstructorBlockStatementsopt '}'
/.$putCase consumeConstructorBody(); $break ./

ConstructorBlockStatementsopt -> BlockStatementsopt

ConstructorBlockStatementsopt -> ExplicitConstructorInvocation

ConstructorBlockStatementsopt ::= ExplicitConstructorInvocation BlockStatements
/.$putCase  consumeConstructorBlockStatements(); $break ./

ExplicitConstructorInvocation ::= 'this' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.This); $break ./

ExplicitConstructorInvocation ::= 'super' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.Super); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Primary '.' 'super' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.Super); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Name '.' 'super' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.Super); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Primary '.' 'this' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.This); $break ./

--1.1 feature
ExplicitConstructorInvocation ::= Name '.' 'this' '(' ArgumentListopt ')' ';'
/.$putCase consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.This); $break ./

--18.9 Productions from 9: Interface Declarations

--18.9.1 Productions from 9.1: Interface Declarations
--InterfaceModifier ::=
--      'public'
--    | 'abstract'
--
InterfaceDeclaration ::= InterfaceHeader InterfaceBody
/.$putCase consumeInterfaceDeclaration(); $break ./

InterfaceHeader ::= InterfaceHeaderName InterfaceHeaderExtendsopt
/.$putCase consumeInterfaceHeader(); $break ./

InterfaceHeaderName ::= Modifiersopt 'interface' 'Identifier'
/.$putCase consumeInterfaceHeaderName(); $break ./

-- This rule will be used to accept inner local interface and then report a relevant error message
InvalidInterfaceDeclaration -> InterfaceHeader InterfaceBody

InterfaceHeaderExtends ::= 'extends' InterfaceTypeList
/.$putCase consumeInterfaceHeaderExtends(); $break ./

InterfaceBody ::= '{' InterfaceMemberDeclarationsopt '}' 

InterfaceMemberDeclarations -> InterfaceMemberDeclaration
InterfaceMemberDeclarations ::= InterfaceMemberDeclarations InterfaceMemberDeclaration
/.$putCase consumeInterfaceMemberDeclarations(); $break ./

--same as for class members
InterfaceMemberDeclaration ::= ';'
/.$putCase consumeEmptyInterfaceMemberDeclaration(); $break ./

-- This rule is added to be able to parse non abstract method inside interface and then report a relevent error message
InvalidMethodDeclaration -> MethodHeader MethodBody

InterfaceMemberDeclaration -> ConstantDeclaration
InterfaceMemberDeclaration ::= InvalidMethodDeclaration
/.$putCase ignoreMethodBody(); $break ./

-- These rules are added to be able to parse constructors inside interface and then report a relevent error message
InvalidConstructorDeclaration ::= ConstructorHeader ConstructorBody
/.$putCase ignoreInvalidConstructorDeclaration(true);  $break ./

InvalidConstructorDeclaration ::= ConstructorHeader ';'
/.$putCase ignoreInvalidConstructorDeclaration(false);  $break ./

InterfaceMemberDeclaration -> AbstractMethodDeclaration
InterfaceMemberDeclaration -> InvalidConstructorDeclaration
 
--1.1 feature
InterfaceMemberDeclaration -> ClassDeclaration
--1.1 feature
InterfaceMemberDeclaration -> InterfaceDeclaration

ConstantDeclaration -> FieldDeclaration

ArrayInitializer ::= '{' ,opt '}'
/.$putCase consumeEmptyArrayInitializer(); $break ./
ArrayInitializer ::= '{' VariableInitializers '}'
/.$putCase consumeArrayInitializer(); $break ./
ArrayInitializer ::= '{' VariableInitializers , '}'
/.$putCase consumeArrayInitializer(); $break ./

VariableInitializers ::= VariableInitializer
VariableInitializers ::= VariableInitializers ',' VariableInitializer
/.$putCase consumeVariableInitializers(); $break ./

Block ::= OpenBlock '{' BlockStatementsopt '}'
/.$putCase consumeBlock(); $break ./
OpenBlock ::= $empty
/.$putCase consumeOpenBlock() ; $break ./

BlockStatements -> BlockStatement
BlockStatements ::= BlockStatements BlockStatement
/.$putCase consumeBlockStatements() ; $break ./

BlockStatement -> LocalVariableDeclarationStatement
BlockStatement -> Statement
--1.1 feature
BlockStatement -> ClassDeclaration
BlockStatement ::= InvalidInterfaceDeclaration
/.$putCase ignoreInterfaceDeclaration(); $break ./

LocalVariableDeclarationStatement ::= LocalVariableDeclaration ';'
/.$putCase consumeLocalVariableDeclarationStatement(); $break ./

LocalVariableDeclaration ::= Type PushModifiers VariableDeclarators
/.$putCase consumeLocalVariableDeclaration(); $break ./

-- 1.1 feature
-- The modifiers part of this rule makes the grammar more permissive. 
-- The only modifier here is final. We put Modifiers to allow multiple modifiers
-- This will require to check the validity of the modifier

LocalVariableDeclaration ::= Modifiers Type PushModifiers VariableDeclarators
/.$putCase consumeLocalVariableDeclaration(); $break ./

PushModifiers ::= $empty
/.$putCase consumePushModifiers(); $break ./

Statement -> StatementWithoutTrailingSubstatement
Statement -> LabeledStatement
Statement -> IfThenStatement
Statement -> IfThenElseStatement
Statement -> WhileStatement
Statement -> ForStatement

StatementNoShortIf -> StatementWithoutTrailingSubstatement
StatementNoShortIf -> LabeledStatementNoShortIf
StatementNoShortIf -> IfThenElseStatementNoShortIf
StatementNoShortIf -> WhileStatementNoShortIf
StatementNoShortIf -> ForStatementNoShortIf

StatementWithoutTrailingSubstatement -> AssertStatement
StatementWithoutTrailingSubstatement -> Block
StatementWithoutTrailingSubstatement -> EmptyStatement
StatementWithoutTrailingSubstatement -> ExpressionStatement
StatementWithoutTrailingSubstatement -> SwitchStatement
StatementWithoutTrailingSubstatement -> DoStatement
StatementWithoutTrailingSubstatement -> BreakStatement
StatementWithoutTrailingSubstatement -> ContinueStatement
StatementWithoutTrailingSubstatement -> ReturnStatement
StatementWithoutTrailingSubstatement -> SynchronizedStatement
StatementWithoutTrailingSubstatement -> ThrowStatement
StatementWithoutTrailingSubstatement -> TryStatement

EmptyStatement ::= ';'
/.$putCase consumeEmptyStatement(); $break ./

LabeledStatement ::= 'Identifier' ':' Statement
/.$putCase consumeStatementLabel() ; $break ./

LabeledStatementNoShortIf ::= 'Identifier' ':' StatementNoShortIf
/.$putCase consumeStatementLabel() ; $break ./

ExpressionStatement ::= StatementExpression ';'
/. $putCase consumeExpressionStatement(); $break ./

StatementExpression ::= Assignment
StatementExpression ::= PreIncrementExpression
StatementExpression ::= PreDecrementExpression
StatementExpression ::= PostIncrementExpression
StatementExpression ::= PostDecrementExpression
StatementExpression ::= MethodInvocation
StatementExpression ::= ClassInstanceCreationExpression

IfThenStatement ::=  'if' '(' Expression ')' Statement
/.$putCase consumeStatementIfNoElse(); $break ./

IfThenElseStatement ::=  'if' '(' Expression ')' StatementNoShortIf 'else' Statement
/.$putCase consumeStatementIfWithElse(); $break ./

IfThenElseStatementNoShortIf ::=  'if' '(' Expression ')' StatementNoShortIf 'else' StatementNoShortIf
/.$putCase consumeStatementIfWithElse(); $break ./

SwitchStatement ::= 'switch' OpenBlock '(' Expression ')' SwitchBlock
/.$putCase consumeStatementSwitch() ; $break ./

SwitchBlock ::= '{' '}'
/.$putCase consumeEmptySwitchBlock() ; $break ./

SwitchBlock ::= '{' SwitchBlockStatements '}'
SwitchBlock ::= '{' SwitchLabels '}'
SwitchBlock ::= '{' SwitchBlockStatements SwitchLabels '}'
/.$putCase consumeSwitchBlock() ; $break ./

SwitchBlockStatements -> SwitchBlockStatement
SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement
/.$putCase consumeSwitchBlockStatements() ; $break ./

SwitchBlockStatement ::= SwitchLabels BlockStatements
/.$putCase consumeSwitchBlockStatement() ; $break ./

SwitchLabels -> SwitchLabel
SwitchLabels ::= SwitchLabels SwitchLabel
/.$putCase consumeSwitchLabels() ; $break ./

SwitchLabel ::= 'case' ConstantExpression ':'
/. $putCase consumeCaseLabel(); $break ./

SwitchLabel ::= 'default' ':'
/. $putCase consumeDefaultLabel(); $break ./

WhileStatement ::= 'while' '(' Expression ')' Statement
/.$putCase consumeStatementWhile() ; $break ./

WhileStatementNoShortIf ::= 'while' '(' Expression ')' StatementNoShortIf
/.$putCase consumeStatementWhile() ; $break ./

DoStatement ::= 'do' Statement 'while' '(' Expression ')' ';'
/.$putCase consumeStatementDo() ; $break ./

ForStatement ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' Statement
/.$putCase consumeStatementFor() ; $break ./
ForStatementNoShortIf ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf
/.$putCase consumeStatementFor() ; $break ./

--the minus one allows to avoid a stack-to-stack transfer
ForInit ::= StatementExpressionList
/.$putCase consumeForInit() ; $break ./
ForInit -> LocalVariableDeclaration

ForUpdate -> StatementExpressionList

StatementExpressionList -> StatementExpression
StatementExpressionList ::= StatementExpressionList ',' StatementExpression
/.$putCase consumeStatementExpressionList() ; $break ./

-- 1.4 feature
AssertStatement ::= 'assert' Expression ';'
/.$putCase consumeSimpleAssertStatement() ; $break ./

AssertStatement ::= 'assert' Expression ':' Expression ';'
/.$putCase consumeAssertStatement() ; $break ./
          
BreakStatement ::= 'break' ';'
/.$putCase consumeStatementBreak() ; $break ./

BreakStatement ::= 'break' Identifier ';'
/.$putCase consumeStatementBreakWithLabel() ; $break ./

ContinueStatement ::= 'continue' ';'
/.$putCase consumeStatementContinue() ; $break ./

ContinueStatement ::= 'continue' Identifier ';'
/.$putCase consumeStatementContinueWithLabel() ; $break ./

ReturnStatement ::= 'return' Expressionopt ';'
/.$putCase consumeStatementReturn() ; $break ./

ThrowStatement ::= 'throw' Expression ';'
/.$putCase consumeStatementThrow();
$break ./

SynchronizedStatement ::= OnlySynchronized '(' Expression ')'    Block
/.$putCase consumeStatementSynchronized(); $break ./
OnlySynchronized ::= 'synchronized'
/.$putCase consumeOnlySynchronized(); $break ./


TryStatement ::= 'try'    Block Catches
/.$putCase consumeStatementTry(false); $break ./
TryStatement ::= 'try'    Block Catchesopt Finally
/.$putCase consumeStatementTry(true); $break ./

Catches -> CatchClause
Catches ::= Catches CatchClause
/.$putCase consumeCatches(); $break ./

CatchClause ::= 'catch' '(' FormalParameter ')'    Block
/.$putCase consumeStatementCatch() ; $break ./

Finally ::= 'finally'    Block

--18.12 Productions from 14: Expressions

--for source positionning purpose
PushLPAREN ::= '('
/.$putCase consumeLeftParen(); $break ./
PushRPAREN ::= ')'
/.$putCase consumeRightParen(); $break ./

Primary -> PrimaryNoNewArray
Primary -> ArrayCreationExpression

PrimaryNoNewArray -> Literal
PrimaryNoNewArray ::= 'this'
/.$putCase consumePrimaryNoNewArrayThis(); $break ./

PrimaryNoNewArray ::=  PushLPAREN Expression PushRPAREN 
/.$putCase consumePrimaryNoNewArray(); $break ./

PrimaryNoNewArray -> ClassInstanceCreationExpression
PrimaryNoNewArray -> FieldAccess
--1.1 feature
PrimaryNoNewArray ::= Name '.' 'this'
/.$putCase consumePrimaryNoNewArrayNameThis(); $break ./
PrimaryNoNewArray ::= Name '.' 'super'
/.$putCase consumePrimaryNoNewArrayNameSuper(); $break ./

--1.1 feature
--PrimaryNoNewArray ::= Type '.' 'class'   
--inline Type in the previous rule in order to make the grammar LL1 instead 
-- of LL2. The result is the 3 next rules.
PrimaryNoNewArray ::= Name '.' 'class'
/.$putCase consumePrimaryNoNewArrayName(); $break ./

PrimaryNoNewArray ::= ArrayType '.' 'class'
/.$putCase consumePrimaryNoNewArrayArrayType(); $break ./

PrimaryNoNewArray ::= PrimitiveType '.' 'class'
/.$putCase consumePrimaryNoNewArrayPrimitiveType(); $break ./

PrimaryNoNewArray -> MethodInvocation
PrimaryNoNewArray -> ArrayAccess

--1.1 feature
--
-- In Java 1.0 a ClassBody could not appear at all in a
-- ClassInstanceCreationExpression.
--

AllocationHeader ::= 'new' ClassType '(' ArgumentListopt ')'
/.$putCase consumeAllocationHeader(); $break ./

ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt
/.$putCase consumeClassInstanceCreationExpression(); $break ./
--1.1 feature

ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
/.$putCase consumeClassInstanceCreationExpressionQualified() ; $break ./

--1.1 feature
ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
/.$putCase consumeClassInstanceCreationExpressionQualified() ; $break ./

ClassInstanceCreationExpressionName ::= Name '.'
/.$putCase consumeClassInstanceCreationExpressionName() ; $break ./

ClassBodyopt ::= $empty --test made using null as contents
/.$putCase consumeClassBodyopt(); $break ./
ClassBodyopt ::= EnterAnonymousClassBody ClassBody

EnterAnonymousClassBody ::= $empty
/.$putCase consumeEnterAnonymousClassBody(); $break ./

ArgumentList ::= Expression
ArgumentList ::= ArgumentList ',' Expression
/.$putCase consumeArgumentList(); $break ./

--Thess rules are re-written in order to be ll1 
--ArrayCreationExpression ::= 'new' ArrayType ArrayInitializer
--ArrayCreationExpression ::= 'new' PrimitiveType DimExprs Dimsopt 
--ArrayCreationExpression ::= 'new' ClassOrInterfaceType DimExprs Dimsopt
--DimExprs ::= DimExpr
--DimExprs ::= DimExprs DimExpr

ArrayCreationExpression ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializeropt
/.$putCase consumeArrayCreationExpression(); $break ./
ArrayCreationExpression ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializeropt
/.$putCase consumeArrayCreationExpression(); $break ./

DimWithOrWithOutExprs ::= DimWithOrWithOutExpr
DimWithOrWithOutExprs ::= DimWithOrWithOutExprs DimWithOrWithOutExpr
/.$putCase consumeDimWithOrWithOutExprs(); $break ./

DimWithOrWithOutExpr ::= '[' Expression ']'
DimWithOrWithOutExpr ::= '[' ']'
/. $putCase consumeDimWithOrWithOutExpr(); $break ./
-- -----------------------------------------------

Dims ::= DimsLoop
/. $putCase consumeDims(); $break ./
DimsLoop -> OneDimLoop
DimsLoop ::= DimsLoop OneDimLoop
OneDimLoop ::= '[' ']'
/. $putCase consumeOneDimLoop(); $break ./

FieldAccess ::= Primary '.' 'Identifier'
/.$putCase consumeFieldAccess(false); $break ./

FieldAccess ::= 'super' '.' 'Identifier'
/.$putCase consumeFieldAccess(true); $break ./

MethodInvocation ::= Name '(' ArgumentListopt ')'
/.$putCase consumeMethodInvocationName(); $break ./

MethodInvocation ::= Primary '.' 'Identifier' '(' ArgumentListopt ')'
/.$putCase consumeMethodInvocationPrimary(); $break ./

MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'
/.$putCase consumeMethodInvocationSuper(); $break ./

ArrayAccess ::= Name '[' Expression ']'
/.$putCase consumeArrayAccess(true); $break ./
ArrayAccess ::= PrimaryNoNewArray '[' Expression ']'
/.$putCase consumeArrayAccess(false); $break ./

PostfixExpression -> Primary
PostfixExpression ::= Name
/.$putCase consumePostfixExpression(); $break ./
PostfixExpression -> PostIncrementExpression
PostfixExpression -> PostDecrementExpression

PostIncrementExpression ::= PostfixExpression '++'
/.$putCase consumeUnaryExpression(OperatorExpression.PLUS,true); $break ./

PostDecrementExpression ::= PostfixExpression '--'
/.$putCase consumeUnaryExpression(OperatorExpression.MINUS,true); $break ./

--for source managment purpose
PushPosition ::= $empty
 /.$putCase consumePushPosition(); $break ./

UnaryExpression -> PreIncrementExpression
UnaryExpression -> PreDecrementExpression
UnaryExpression ::= '+' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.PLUS); $break ./
UnaryExpression ::= '-' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.MINUS); $break ./
UnaryExpression -> UnaryExpressionNotPlusMinus

PreIncrementExpression ::= '++' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.PLUS,false); $break ./

PreDecrementExpression ::= '--' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.MINUS,false); $break ./

UnaryExpressionNotPlusMinus -> PostfixExpression
UnaryExpressionNotPlusMinus ::= '~' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.TWIDDLE); $break ./
UnaryExpressionNotPlusMinus ::= '!' PushPosition UnaryExpression
/.$putCase consumeUnaryExpression(OperatorExpression.NOT); $break ./
UnaryExpressionNotPlusMinus -> CastExpression

CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN UnaryExpression
/.$putCase consumeCastExpression(); $break ./
 CastExpression ::= PushLPAREN Name Dims PushRPAREN UnaryExpressionNotPlusMinus
/.$putCase consumeCastExpression(); $break ./
-- Expression is here only in order to make the grammar LL1
CastExpression ::= PushLPAREN Expression PushRPAREN UnaryExpressionNotPlusMinus
/.$putCase consumeCastExpressionLL1(); $break ./

MultiplicativeExpression -> UnaryExpression
MultiplicativeExpression ::= MultiplicativeExpression '*' UnaryExpression
/.$putCase consumeBinaryExpression(OperatorExpression.MULTIPLY); $break ./
MultiplicativeExpression ::= MultiplicativeExpression '/' UnaryExpression
/.$putCase consumeBinaryExpression(OperatorExpression.DIVIDE); $break ./
MultiplicativeExpression ::= MultiplicativeExpression '%' UnaryExpression
/.$putCase consumeBinaryExpression(OperatorExpression.REMAINDER); $break ./

AdditiveExpression -> MultiplicativeExpression
AdditiveExpression ::= AdditiveExpression '+' MultiplicativeExpression
/.$putCase consumeBinaryExpression(OperatorExpression.PLUS); $break ./
AdditiveExpression ::= AdditiveExpression '-' MultiplicativeExpression
/.$putCase consumeBinaryExpression(OperatorExpression.MINUS); $break ./

ShiftExpression -> AdditiveExpression
ShiftExpression ::= ShiftExpression '<<'  AdditiveExpression
/.$putCase consumeBinaryExpression(OperatorExpression.LEFT_SHIFT); $break ./
ShiftExpression ::= ShiftExpression '>>'  AdditiveExpression
/.$putCase consumeBinaryExpression(OperatorExpression.RIGHT_SHIFT); $break ./
ShiftExpression ::= ShiftExpression '>>>' AdditiveExpression
/.$putCase consumeBinaryExpression(OperatorExpression.UNSIGNED_RIGHT_SHIFT); $break ./

RelationalExpression -> ShiftExpression
RelationalExpression ::= RelationalExpression '<'  ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.LESS); $break ./
RelationalExpression ::= RelationalExpression '>'  ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.GREATER); $break ./
RelationalExpression ::= RelationalExpression '<=' ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.LESS_EQUAL); $break ./
RelationalExpression ::= RelationalExpression '>=' ShiftExpression
/.$putCase consumeBinaryExpression(OperatorExpression.GREATER_EQUAL); $break ./
RelationalExpression ::= RelationalExpression 'instanceof' ReferenceType
/.$putCase consumeInstanceOfExpression(OperatorExpression.INSTANCEOF); $break ./

EqualityExpression -> RelationalExpression
EqualityExpression ::= EqualityExpression '==' RelationalExpression
/.$putCase consumeEqualityExpression(OperatorExpression.EQUAL_EQUAL); $break ./
EqualityExpression ::= EqualityExpression '!=' RelationalExpression
/.$putCase consumeEqualityExpression(OperatorExpression.NOT_EQUAL); $break ./

AndExpression -> EqualityExpression
AndExpression ::= AndExpression '&' EqualityExpression
/.$putCase consumeBinaryExpression(OperatorExpression.AND); $break ./

ExclusiveOrExpression -> AndExpression
ExclusiveOrExpression ::= ExclusiveOrExpression '^' AndExpression
/.$putCase consumeBinaryExpression(OperatorExpression.XOR); $break ./

InclusiveOrExpression -> ExclusiveOrExpression
InclusiveOrExpression ::= InclusiveOrExpression '|' ExclusiveOrExpression
/.$putCase consumeBinaryExpression(OperatorExpression.OR); $break ./

ConditionalAndExpression -> InclusiveOrExpression
ConditionalAndExpression ::= ConditionalAndExpression '&&' InclusiveOrExpression
/.$putCase consumeBinaryExpression(OperatorExpression.AND_AND); $break ./

ConditionalOrExpression -> ConditionalAndExpression
ConditionalOrExpression ::= ConditionalOrExpression '||' ConditionalAndExpression
/.$putCase consumeBinaryExpression(OperatorExpression.OR_OR); $break ./

ConditionalExpression -> ConditionalOrExpression
ConditionalExpression ::= ConditionalOrExpression '?' Expression ':' ConditionalExpression
/.$putCase consumeConditionalExpression(OperatorExpression.QUESTIONCOLON) ; $break ./

AssignmentExpression -> ConditionalExpression
AssignmentExpression -> Assignment

Assignment ::= LeftHandSide AssignmentOperator AssignmentExpression
/.$putCase consumeAssignment(); $break ./

-- this rule is added to parse an array initializer in a assigment and then report a syntax error knowing the exact senario
InvalidArrayInitializerAssignement ::= LeftHandSide AssignmentOperator ArrayInitializer
Assignment ::= InvalidArrayInitializerAssignement
/.$putcase ignoreExpressionAssignment();$break ./

LeftHandSide ::= Name
/.$putCase consumeLeftHandSide(); $break ./
LeftHandSide -> FieldAccess
LeftHandSide -> ArrayAccess

AssignmentOperator ::= '='
/.$putCase consumeAssignmentOperator(EQUAL); $break ./
AssignmentOperator ::= '*='
/.$putCase consumeAssignmentOperator(MULTIPLY); $break ./
AssignmentOperator ::= '/='
/.$putCase consumeAssignmentOperator(DIVIDE); $break ./
AssignmentOperator ::= '%='
/.$putCase consumeAssignmentOperator(REMAINDER); $break ./
AssignmentOperator ::= '+='
/.$putCase consumeAssignmentOperator(PLUS); $break ./
AssignmentOperator ::= '-='
/.$putCase consumeAssignmentOperator(MINUS); $break ./
AssignmentOperator ::= '<<='
/.$putCase consumeAssignmentOperator(LEFT_SHIFT); $break ./
AssignmentOperator ::= '>>='
/.$putCase consumeAssignmentOperator(RIGHT_SHIFT); $break ./
AssignmentOperator ::= '>>>='
/.$putCase consumeAssignmentOperator(UNSIGNED_RIGHT_SHIFT); $break ./
AssignmentOperator ::= '&='
/.$putCase consumeAssignmentOperator(AND); $break ./
AssignmentOperator ::= '^='
/.$putCase consumeAssignmentOperator(XOR); $break ./
AssignmentOperator ::= '|='
/.$putCase consumeAssignmentOperator(OR); $break ./

Expression -> AssignmentExpression

ConstantExpression -> Expression

-- The following rules are for optional nonterminals.
--

PackageDeclarationopt -> $empty 
PackageDeclarationopt -> PackageDeclaration

ClassHeaderExtendsopt ::= $empty
ClassHeaderExtendsopt -> ClassHeaderExtends

Expressionopt ::= $empty
/.$putCase consumeEmptyExpression(); $break ./
Expressionopt -> Expression


---------------------------------------------------------------------------------------
--
-- The rules below are for optional terminal symbols.  An optional comma,
-- is only used in the context of an array initializer - It is a
-- "syntactic sugar" that otherwise serves no other purpose. By contrast,
-- an optional identifier is used in the definition of a break and 
-- continue statement. When the identifier does not appear, a NULL
-- is produced. When the identifier is present, the user should use the
-- corresponding TOKEN(i) method. See break statement as an example.
--
---------------------------------------------------------------------------------------

,opt -> $empty
,opt -> ,

ImportDeclarationsopt ::= $empty
/.$putCase consumeEmptyImportDeclarationsopt(); $break ./
ImportDeclarationsopt ::= ImportDeclarations
/.$putCase consumeImportDeclarationsopt(); $break ./


TypeDeclarationsopt ::= $empty
/.$putCase consumeEmptyTypeDeclarationsopt(); $break ./
TypeDeclarationsopt ::= TypeDeclarations
/.$putCase consumeTypeDeclarationsopt(); $break ./

ClassBodyDeclarationsopt ::= $empty
/.$putCase consumeEmptyClassBodyDeclarationsopt(); $break ./
ClassBodyDeclarationsopt ::= NestedType ClassBodyDeclarations
/.$putCase consumeClassBodyDeclarationsopt(); $break ./

Modifiersopt ::= $empty 
/. $putCase consumeDefaultModifiers(); $break ./
Modifiersopt ::= Modifiers 
/.$putCase consumeModifiers(); $break ./ 

BlockStatementsopt ::= $empty
/.$putCase consumeEmptyBlockStatementsopt(); $break ./
BlockStatementsopt -> BlockStatements

Dimsopt ::= $empty
/. $putCase consumeEmptyDimsopt(); $break ./
Dimsopt -> Dims

ArgumentListopt ::= $empty
/. $putCase consumeEmptyArgumentListopt(); $break ./
ArgumentListopt -> ArgumentList

MethodHeaderThrowsClauseopt ::= $empty
MethodHeaderThrowsClauseopt -> MethodHeaderThrowsClause
   
FormalParameterListopt ::= $empty
/.$putcase consumeFormalParameterListopt(); $break ./
FormalParameterListopt -> FormalParameterList

ClassHeaderImplementsopt ::= $empty
ClassHeaderImplementsopt -> ClassHeaderImplements

InterfaceMemberDeclarationsopt ::= $empty
/. $putCase consumeEmptyInterfaceMemberDeclarationsopt(); $break ./
InterfaceMemberDeclarationsopt ::= NestedType InterfaceMemberDeclarations
/. $putCase consumeInterfaceMemberDeclarationsopt(); $break ./

NestedType ::= $empty 
/.$putCase consumeNestedType(); $break./

ForInitopt ::= $empty
/. $putCase consumeEmptyForInitopt(); $break ./
ForInitopt -> ForInit

ForUpdateopt ::= $empty
/. $putCase consumeEmptyForUpdateopt(); $break ./
 ForUpdateopt -> ForUpdate

InterfaceHeaderExtendsopt ::= $empty
InterfaceHeaderExtendsopt -> InterfaceHeaderExtends

Catchesopt ::= $empty
/. $putCase consumeEmptyCatchesopt(); $break ./
Catchesopt -> Catches

ArrayInitializeropt ::= $empty
/. $putCase consumeEmptyArrayInitializeropt(); $break ./
ArrayInitializeropt -> ArrayInitializer

/.	}
} ./

---------------------------------------------------------------------------------------

$names

-- BodyMarker ::= '"class Identifier { ... MethodHeader "'

-- void ::= 'void'

PLUS_PLUS ::=    '++'   
MINUS_MINUS ::=    '--'   
EQUAL_EQUAL ::=    '=='   
LESS_EQUAL ::=    '<='   
GREATER_EQUAL ::=    '>='   
NOT_EQUAL ::=    '!='   
LEFT_SHIFT ::=    '<<'   
RIGHT_SHIFT ::=    '>>'   
UNSIGNED_RIGHT_SHIFT ::=    '>>>'  
PLUS_EQUAL ::=    '+='   
MINUS_EQUAL ::=    '-='   
MULTIPLY_EQUAL ::=    '*='   
DIVIDE_EQUAL ::=    '/='   
AND_EQUAL ::=    '&='   
OR_EQUAL ::=    '|='   
XOR_EQUAL ::=    '^='   
REMAINDER_EQUAL ::=    '%='   
LEFT_SHIFT_EQUAL ::=    '<<='  
RIGHT_SHIFT_EQUAL ::=    '>>='  
UNSIGNED_RIGHT_SHIFT_EQUAL ::=    '>>>=' 
OR_OR ::=    '||'   
AND_AND ::=    '&&'   

PLUS ::=    '+'    
MINUS ::=    '-'    
NOT ::=    '!'    
REMAINDER ::=    '%'    
XOR ::=    '^'    
AND ::=    '&'    
MULTIPLY ::=    '*'    
OR ::=    '|'    
TWIDDLE ::=    '~'    
DIVIDE ::=    '/'    
GREATER ::=    '>'    
LESS ::=    '<'    
LPAREN ::=    '('    
RPAREN ::=    ')'    
LBRACE ::=    '{'    
RBRACE ::=    '}'    
LBRACKET ::=    '['    
RBRACKET ::=    ']'    
SEMICOLON ::=    ';'    
QUESTION ::=    '?'    
COLON ::=    ':'    
COMMA ::=    ','    
DOT ::=    '.'    
EQUAL ::=    '='    

$end
-- need a carriage return after the $end
*/
}
protected void ignoreExpressionAssignment() {
	// Assignment ::= InvalidArrayInitializerAssignement
	// encoded operator would be: intStack[intPtr]
	intPtr--;
	ArrayInitializer arrayInitializer = (ArrayInitializer) expressionStack[expressionPtr--];
	expressionLengthPtr -- ;
	// report a syntax error and abort parsing
	problemReporter().arrayConstantsOnlyInArrayInitializers(arrayInitializer.sourceStart, arrayInitializer.sourceEnd); 	
}
protected void ignoreInterfaceDeclaration() {
	// BlockStatement ::= InvalidInterfaceDeclaration
	//InterfaceDeclaration ::= Modifiersopt 'interface' 'Identifier' ExtendsInterfacesopt InterfaceHeader InterfaceBody

	// length declarations
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		//there are length declarations
		//dispatch according to the type of the declarations
		dispatchDeclarationInto(length);
	}
	
	flushAnnotationsDefinedPriorTo(endStatementPosition);

	// report the problem and continue parsing
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	typeDecl.bodyEnd = endStatementPosition;
	problemReporter().cannotDeclareLocalInterface(typeDecl.name, typeDecl.sourceStart, typeDecl.sourceEnd);

	// mark fields and initializer with local type mark if needed
	markFieldsWithLocalType(typeDecl);

	// remove the ast node created in interface header
	astPtr--;	
	// Don't create an astnode for this inner interface, but have to push
	// a 0 on the astLengthStack to be consistent with the reduction made
	// at the end of the method:
	// public void parse(MethodDeclaration md, CompilationUnitDeclaration unit)
	pushOnAstLengthStack(0);
}
protected void ignoreInvalidConstructorDeclaration(boolean hasBody) {
	// InvalidConstructorDeclaration ::= ConstructorHeader ConstructorBody ==> true
	// InvalidConstructorDeclaration ::= ConstructorHeader ';' ==> false

	/*
	astStack : modifiers arguments throws statements
	identifierStack : name
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	*/

	//must provide a default constructor call when needed

	if (hasBody) {
		// pop the position of the {  (body of the method) pushed in block decl
		intPtr--;
	}

	//statements
	if (hasBody) {
		realBlockPtr--;
	}

	int length;
	if (hasBody && ((length = astLengthStack[astLengthPtr--]) != 0)) {
		astPtr -= length;
	}
}
protected void ignoreMethodBody() {
	// InterfaceMemberDeclaration ::= InvalidMethodDeclaration

	/*
	astStack : modifiers arguments throws statements
	identifierStack : type name
	intStack : dim dim dim
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	intStack : 
	*/

	// pop the position of the {  (body of the method) pushed in block decl
	intPtr--;
	// retrieve end position of method declarator

	//statements
	realBlockPtr--;
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
	}

	//watch for } that could be given as a unicode ! ( u007D is '}' )
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	md.bodyEnd = endPosition;
	md.declarationSourceEnd = flushAnnotationsDefinedPriorTo(endStatementPosition);

	// report the problem and continue the parsing - narrowing the problem onto the method
	problemReporter().abstractMethodNeedingNoBody(md);
}
public void initialize() {
	//positionning the parser for a new compilation unit
	//avoiding stack reallocation and all that....
	astPtr = -1;
	astLengthPtr = -1;
	expressionPtr = -1;
	expressionLengthPtr = -1;
	identifierPtr = -1;	
	identifierLengthPtr	= -1;
	intPtr = -1;
	nestedMethod[nestedType = 0] = 0; // need to reset for further reuse
	variablesCounter[nestedType] = 0;
	dimensions = 0 ;
	realBlockPtr = -1;
	compilationUnit = null;
	referenceContext = null;
	endStatementPosition = 0;

	//remove objects from stack too, while the same parser/compiler couple is
	//re-used between two compilations ....
	
	int astLength = astStack.length;
	if (noAstNodes.length < astLength){
		noAstNodes = new AstNode[astLength];
		//System.out.println("Resized AST stacks : "+ astLength);
		
	}
	System.arraycopy(noAstNodes, 0, astStack, 0, astLength);

	int expressionLength = expressionStack.length;
	if (noExpressions.length < expressionLength){
		noExpressions = new Expression[expressionLength];
		//System.out.println("Resized EXPR stacks : "+ expressionLength);
	}
	System.arraycopy(noExpressions, 0, expressionStack, 0, expressionLength);

	// reset scanner state
	scanner.commentPtr = -1;
	scanner.eofPosition = Integer.MAX_VALUE;

	resetModifiers();

	// recovery
	lastCheckPoint = -1;
	currentElement = null;
	restartRecovery = false;
	hasReportedError = false;
	recoveredStaticInitializerStart = 0;
	lastIgnoredToken = -1;
	lastErrorEndPosition = -1;
	listLength = 0;
}
public void initializeScanner(){
	this.scanner = new Scanner(false, false, this.problemReporter.options.getNonExternalizedStringLiteralSeverity() != ProblemSeverities.Ignore , this.assertMode);
}
public final static void initTables(Class parserClass) throws java.io.IOException {

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
public final void jumpOverMethodBody() {
	//on diet parsing.....do not buffer method statements

	//the scanner.diet is reinitialized to false
	//automatically by the scanner once it has jumped over
	//the statements

	if (diet && (dietInt == 0))
		scanner.diet = true;
}
protected void markCurrentMethodWithLocalType() {
	if (this.currentElement != null) return; // this is already done in the recovery code
	for (int i = this.astPtr; i >= 0; i--) {
		AstNode node = this.astStack[i];
		if (node instanceof AbstractMethodDeclaration 
				|| node instanceof TypeDeclaration) { // mark type for now: all fields will be marked when added to this type
			node.bits |= AstNode.HasLocalTypeMASK;
			return;
		}
	}
	// default to reference context (case of parse method body)
	if (this.referenceContext instanceof AbstractMethodDeclaration
			|| this.referenceContext instanceof TypeDeclaration) {
		((AstNode)this.referenceContext).bits |= AstNode.HasLocalTypeMASK;
	}
}
protected void markFieldsWithLocalType(TypeDeclaration type) {
	if (type.fields == null || (type.bits & AstNode.HasLocalTypeMASK) == 0) return;
	for (int i = 0, length = type.fields.length; i < length; i++) {
		type.fields[i].bits |= AstNode.HasLocalTypeMASK;
	}
}
/*
 * Move checkpoint location (current implementation is moving it by one token)
 *
 * Answers true if successfully moved checkpoint (i.e. did not attempt to move it
 * beyond end of file).
 */
protected boolean moveRecoveryCheckpoint() {

	int pos = lastCheckPoint;
	/* reset scanner, and move checkpoint by one token */
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.diet = false; // quit jumping over method bodies
	
	/* if about to restart, then no need to shift token */
	if (restartRecovery){
		lastIgnoredToken = -1;
		return true;
	}
	
	/* protect against shifting on an invalid token */
	lastIgnoredToken = nextIgnoredToken;
	nextIgnoredToken = -1;
	do {
		try {
			nextIgnoredToken = scanner.getNextToken();
			if(scanner.currentPosition == scanner.startPosition){
				scanner.currentPosition++; // on fake completion identifier
				nextIgnoredToken = -1;
			}
			
		} catch(InvalidInputException e){
			pos = scanner.currentPosition;
		}
	} while (nextIgnoredToken < 0);
	
	if (nextIgnoredToken == TokenNameEOF) { // no more recovery after this point
		if (currentToken == TokenNameEOF) { // already tried one iteration on EOF
			return false;
		}
	}
	lastCheckPoint = scanner.currentPosition;
	
	/* reset scanner again to previous checkpoint location*/
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.commentPtr = -1;

	return true;

/*
 	The following implementation moves the checkpoint location by one line:
	 
	int pos = lastCheckPoint;
	// reset scanner, and move checkpoint by one token
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.diet = false; // quit jumping over method bodies
	
	// if about to restart, then no need to shift token
	if (restartRecovery){
		lastIgnoredToken = -1;
		return true;
	}
	
	// protect against shifting on an invalid token
	lastIgnoredToken = nextIgnoredToken;
	nextIgnoredToken = -1;
	
	boolean wasTokenizingWhiteSpace = scanner.tokenizeWhiteSpace;
	scanner.tokenizeWhiteSpace = true;
	checkpointMove: 
		do {
			try {
				nextIgnoredToken = scanner.getNextToken();
				switch(nextIgnoredToken){
					case Scanner.TokenNameWHITESPACE :
						if(scanner.getLineNumber(scanner.startPosition)
							== scanner.getLineNumber(scanner.currentPosition)){
							nextIgnoredToken = -1;
							}
						break;
					case TokenNameSEMICOLON :
					case TokenNameLBRACE :
					case TokenNameRBRACE :
						break;
					case TokenNameIdentifier :
						if(scanner.currentPosition == scanner.startPosition){
							scanner.currentPosition++; // on fake completion identifier
						}
					default:						
						nextIgnoredToken = -1;
						break;
					case TokenNameEOF :
						break checkpointMove;
				}
			} catch(InvalidInputException e){
				pos = scanner.currentPosition;
			}
		} while (nextIgnoredToken < 0);
	scanner.tokenizeWhiteSpace = wasTokenizingWhiteSpace;
	
	if (nextIgnoredToken == TokenNameEOF) { // no more recovery after this point
		if (currentToken == TokenNameEOF) { // already tried one iteration on EOF
			return false;
		}
	}
	lastCheckPoint = scanner.currentPosition;
	
	// reset scanner again to previous checkpoint location
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.commentPtr = -1;

	return true;
*/
}
protected MessageSend newMessageSend() {
	// '(' ArgumentListopt ')'
	// the arguments are on the expression stack

	MessageSend m = new MessageSend();
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			m.arguments = new Expression[length], 
			0, 
			length); 
	};
	return m;
}
protected int ntAction(int state, int sym) {
	return action[state + sym];
}
protected final void optimizedConcatNodeLists() {
	/*back from a recursive loop. Virtualy group the
	astNode into an array using astLengthStack*/

	/*
	 * This is a case where you have two sublists into the astStack that you want
	 * to merge in one list. There is no action required on the astStack. The only
	 * thing you need to do is merge the two lengths specified on the astStackLength.
	 * The top two length are for example:
	 * ... p   n
	 * and you want to result in a list like:
	 * ... n+p 
	 * This means that the p could be equals to 0 in case there is no astNode pushed
	 * on the astStack.
	 * Look at the InterfaceMemberDeclarations for an example.
	 * This case optimizes the fact that p == 1.
	 */

	astLengthStack[--astLengthPtr]++;
}
protected int original_state(int state) {
	return -check(state);
}
/*main loop of the automat
When a rule is reduced, the method consumeRule(int) is called with the number
of the consumed rule. When a terminal is consumed, the method consumeToken(int) is 
called in order to remember (when needed) the consumed token */
// (int)asr[asi(act)]
// name[symbol_index[currentKind]]
protected void parse() {

	hasReportedError = false;
	int act = START_STATE;
	stateStackTop = -1;
	currentToken = getFirstToken();
	ProcessTerminals : for (;;) {
		try {
			stack[++stateStackTop] = act;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = stack.length;
			int oldStack[] = stack;
			stack = new int[oldStackLength + StackIncrement];
			System.arraycopy(oldStack, 0, stack, 0, oldStackLength);
			stack[stateStackTop] = act;
		};

		act = tAction(act, currentToken);

		if (act == ERROR_ACTION || restartRecovery) {
			int errorPos = scanner.currentPosition;
			if (!hasReportedError){
				this.reportSyntaxError(ERROR_ACTION, currentToken, stateStackTop);
				hasReportedError = true;
			}
			if (resumeOnSyntaxError()) {
				if (act == ERROR_ACTION) lastErrorEndPosition = errorPos;
				act = START_STATE;
				stateStackTop = -1;
				currentToken = getFirstToken();
				continue ProcessTerminals;
			} else {
				act = ERROR_ACTION;
			}	break ProcessTerminals;
		}
		if (act <= NUM_RULES)
			stateStackTop--;
		else
			if (act > ERROR_ACTION) { /* shift-reduce */
				consumeToken(currentToken);
				if (currentElement != null) this.recoveryTokenCheck();
				try{
					currentToken = scanner.getNextToken();
				} catch(InvalidInputException e){
					if (!hasReportedError){
						this.problemReporter().scannerError(this, e.getMessage());
						hasReportedError = true;
					}
					lastCheckPoint = scanner.currentPosition;
					restartRecovery = true;
				}					
				act -= ERROR_ACTION;
			} else
				if (act < ACCEPT_ACTION) { /* shift */
					consumeToken(currentToken);
					if (currentElement != null) this.recoveryTokenCheck();
					try{
						currentToken = scanner.getNextToken();
					} catch(InvalidInputException e){
						if (!hasReportedError){
							this.problemReporter().scannerError(this, e.getMessage());
							hasReportedError = true;
						}
						lastCheckPoint = scanner.currentPosition;
						restartRecovery = true;
					}					
					continue ProcessTerminals;
				} else
					break ProcessTerminals;

		ProcessNonTerminals : do { /* reduce */
			consumeRule(act);
			stateStackTop -= (rhsInst[act] - 1);
			act = ntAction(stack[stateStackTop], lhs[act]);
		} while (act <= NUM_RULES);
	}
	endParse(act);
}
// A P I

public void parse(ConstructorDeclaration cd, CompilationUnitDeclaration unit) {
	//only parse the method body of cd
	//fill out its statements

	//convert bugs into parse error

	initialize();
	goForConstructorBody();
	nestedMethod[nestedType]++;

	referenceContext = cd;
	compilationUnit = unit;

	scanner.resetTo(cd.sourceEnd + 1, cd.declarationSourceEnd);
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;
	}

	if (lastAct == ERROR_ACTION) {
		initialize();
		return;
	}

	//statements
	cd.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		if (astStack[astPtr + 1] instanceof ExplicitConstructorCall)
			//avoid a isSomeThing that would only be used here BUT what is faster between two alternatives ?
			{
			System.arraycopy(
				astStack, 
				astPtr + 2, 
				cd.statements = new Statement[length - 1], 
				0, 
				length - 1); 
			cd.constructorCall = (ExplicitConstructorCall) astStack[astPtr + 1];
		} else { //need to add explicitly the super();
			System.arraycopy(
				astStack, 
				astPtr + 1, 
				cd.statements = new Statement[length], 
				0, 
				length); 
			cd.constructorCall = SuperReference.implicitSuperConstructorCall();
		}
	} else {
		cd.constructorCall = SuperReference.implicitSuperConstructorCall();
	}

	if (cd.constructorCall.sourceEnd == 0) {
		cd.constructorCall.sourceEnd = cd.sourceEnd;
		cd.constructorCall.sourceStart = cd.sourceStart;
	}
}
// A P I

public void parse(
	Initializer ini, 
	TypeDeclaration type, 
	CompilationUnitDeclaration unit) {
	//only parse the method body of md
	//fill out method statements

	//convert bugs into parse error

	initialize();
	goForInitializer();
	nestedMethod[nestedType]++;

	referenceContext = type;
	compilationUnit = unit;

	scanner.resetTo(ini.sourceStart, ini.sourceEnd); // just on the beginning {
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;
	}

	if (lastAct == ERROR_ACTION) {
		return;
	}

	ini.block = ((Initializer) astStack[astPtr]).block;
	
	// mark initializer with local type if one was found during parsing
	if ((type.bits & AstNode.HasLocalTypeMASK) != 0) {
		ini.bits |= AstNode.HasLocalTypeMASK;
	}	
}
// A P I

public void parse(MethodDeclaration md, CompilationUnitDeclaration unit) {
	//only parse the method body of md
	//fill out method statements

	//convert bugs into parse error

	if (md.isAbstract())
		return;
	if (md.isNative())
		return;
	if ((md.modifiers & AccSemicolonBody) != 0)
		return;

	initialize();
	goForMethodBody();
	nestedMethod[nestedType]++;

	referenceContext = md;
	compilationUnit = unit;

	scanner.resetTo(md.sourceEnd + 1, md.declarationSourceEnd); 
	
	//System.err.println("parsing body of: " + md + " at " + md.sourceEnd + 1);
	// reset the scanner to parser from { down to }
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;		
	}

	if (lastAct == ERROR_ACTION) {
		return;
	}

	//refill statements
	md.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0)
		System.arraycopy(
			astStack, 
			(astPtr -= length) + 1, 
			md.statements = new Statement[length], 
			0, 
			length); 
}


//XXX hack copying above but as a pretend constructor
public void parseAsConstructor(MethodDeclaration md, CompilationUnitDeclaration unit) {
	//only parse the method body of md
	//fill out method statements

	//convert bugs into parse error

	if (md.isAbstract())
		return;
	if (md.isNative())
		return;
	if ((md.modifiers & AccSemicolonBody) != 0)
		return;

	initialize();
	goForConstructorBody();
	nestedMethod[nestedType]++;

	referenceContext = md;
	compilationUnit = unit;

	scanner.resetTo(md.sourceEnd + 1, md.declarationSourceEnd); 
	
	//System.err.println("parsing body of: " + md + " at " + md.sourceEnd + 1);
	// reset the scanner to parser from { down to }
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;		
	}

	if (lastAct == ERROR_ACTION) {
		return;
	}

	//refill statements
	md.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0)
		System.arraycopy(
			astStack, 
			(astPtr -= length) + 1, 
			md.statements = new Statement[length], 
			0, 
			length); 
}



// A P I

public CompilationUnitDeclaration parse(
	ICompilationUnit sourceUnit, 
	CompilationResult compilationResult) {
	// parses a compilation unit and manages error handling (even bugs....)

	CompilationUnitDeclaration unit;
	try {
		/* automaton initialization */
		initialize();
		goForCompilationUnit();

		/* scanner initialization */
		scanner.setSource(sourceUnit.getContents());

		/* unit creation */
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter, 
					compilationResult, 
					scanner.source.length);
		/* run automaton */
		parse();
	} finally {
		unit = compilationUnit;
		compilationUnit = null; // reset parser
	}
	return unit;
}
// A P I

public CompilationUnitDeclaration parse(
	ICompilationUnit sourceUnit, 
	CompilationResult compilationResult,
	int start,
	int end) {
	// parses a compilation unit and manages error handling (even bugs....)

	CompilationUnitDeclaration unit;
	try {
		/* automaton initialization */
		initialize();
		goForCompilationUnit();

		/* scanner initialization */
		scanner.setSource(sourceUnit.getContents());
		scanner.resetTo(start, end);
		/* unit creation */
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter, 
					compilationResult, 
					scanner.source.length);
		/* run automaton */
		parse();
	} finally {
		unit = compilationUnit;
		compilationUnit = null; // reset parser
	}
	return unit;
}
/**
 * Returns this parser's problem reporter initialized with its reference context.
 * Also it is assumed that a problem is going to be reported, so initializes
 * the compilation result's line positions.
 */
public ProblemReporter problemReporter(){
	if (scanner.recordLineSeparator) {
		compilationUnit.compilationResult.lineSeparatorPositions = scanner.getLineEnds();
	}
	problemReporter.referenceContext = referenceContext;
	return problemReporter;
}
protected void pushIdentifier() {
	/*push the consumeToken on the identifier stack.
	Increase the total number of identifier in the stack.
	identifierPtr points on the next top */

	try {
		identifierStack[++identifierPtr] = scanner.getCurrentIdentifierSource();
		identifierPositionStack[identifierPtr] = 
			(((long) scanner.startPosition) << 32) + (scanner.currentPosition - 1); 
	} catch (IndexOutOfBoundsException e) {
		/*---stack reallaocation (identifierPtr is correct)---*/
		int oldStackLength = identifierStack.length;
		char[][] oldStack = identifierStack;
		identifierStack = new char[oldStackLength + 20][];
		System.arraycopy(oldStack, 0, identifierStack, 0, oldStackLength);
		identifierStack[identifierPtr] = scanner.getCurrentTokenSource();
		/*identifier position stack*/
		long[] oldPos = identifierPositionStack;
		identifierPositionStack = new long[oldStackLength + 20];
		System.arraycopy(oldPos, 0, identifierPositionStack, 0, oldStackLength);
		identifierPositionStack[identifierPtr] = 
			(((long) scanner.startPosition) << 32) + (scanner.currentPosition - 1); 
	};

	try {
		identifierLengthStack[++identifierLengthPtr] = 1;
	} catch (IndexOutOfBoundsException e) {
		/*---stack reallocation (identifierLengthPtr is correct)---*/
		int oldStackLength = identifierLengthStack.length;
		int oldStack[] = identifierLengthStack;
		identifierLengthStack = new int[oldStackLength + 10];
		System.arraycopy(oldStack, 0, identifierLengthStack, 0, oldStackLength);
		identifierLengthStack[identifierLengthPtr] = 1;
	};

}
protected void pushIdentifier(int flag) {
	/*push a special flag on the stack :
	-zero stands for optional Name
	-negative number for direct ref to base types.
	identifierLengthPtr points on the top */

	try {
		identifierLengthStack[++identifierLengthPtr] = flag;
	} catch (IndexOutOfBoundsException e) {
		/*---stack reallaocation (identifierLengthPtr is correct)---*/
		int oldStackLength = identifierLengthStack.length;
		int oldStack[] = identifierLengthStack;
		identifierLengthStack = new int[oldStackLength + 10];
		System.arraycopy(oldStack, 0, identifierLengthStack, 0, oldStackLength);
		identifierLengthStack[identifierLengthPtr] = flag;
	};

}
protected void pushOnAstLengthStack(int pos) {
	try {
		astLengthStack[++astLengthPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = astLengthStack.length;
		int[] oldPos = astLengthStack;
		astLengthStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldPos, 0, astLengthStack, 0, oldStackLength);
		astLengthStack[astLengthPtr] = pos;
	}
}
protected void pushOnAstStack(AstNode node) {
	/*add a new obj on top of the ast stack
	astPtr points on the top*/

	try {
		astStack[++astPtr] = node;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = astStack.length;
		AstNode[] oldStack = astStack;
		astStack = new AstNode[oldStackLength + AstStackIncrement];
		System.arraycopy(oldStack, 0, astStack, 0, oldStackLength);
		astPtr = oldStackLength;
		astStack[astPtr] = node;
	}

	try {
		astLengthStack[++astLengthPtr] = 1;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = astLengthStack.length;
		int[] oldPos = astLengthStack;
		astLengthStack = new int[oldStackLength + AstStackIncrement];
		System.arraycopy(oldPos, 0, astLengthStack, 0, oldStackLength);
		astLengthStack[astLengthPtr] = 1;
	}
}
protected void pushOnExpressionStack(Expression expr) {

	try {
		expressionStack[++expressionPtr] = expr;
	} catch (IndexOutOfBoundsException e) {
		//expressionPtr is correct 
		int oldStackLength = expressionStack.length;
		Expression[] oldStack = expressionStack;
		expressionStack = new Expression[oldStackLength + ExpressionStackIncrement];
		System.arraycopy(oldStack, 0, expressionStack, 0, oldStackLength);
		expressionStack[expressionPtr] = expr;
	}

	try {
		expressionLengthStack[++expressionLengthPtr] = 1;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = expressionLengthStack.length;
		int[] oldPos = expressionLengthStack;
		expressionLengthStack = new int[oldStackLength + ExpressionStackIncrement];
		System.arraycopy(oldPos, 0, expressionLengthStack, 0, oldStackLength);
		expressionLengthStack[expressionLengthPtr] = 1;
	}
}
protected void pushOnExpressionStackLengthStack(int pos) {
	try {
		expressionLengthStack[++expressionLengthPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = expressionLengthStack.length;
		int[] oldPos = expressionLengthStack;
		expressionLengthStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldPos, 0, expressionLengthStack, 0, oldStackLength);
		expressionLengthStack[expressionLengthPtr] = pos;
	}
}
protected void pushOnIntStack(int pos) {

	try {
		intStack[++intPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		//intPtr is correct 
		int oldStackLength = intStack.length;
		int oldStack[] = intStack;
		intStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, intStack, 0, oldStackLength);
		intStack[intPtr] = pos;
	}
}
protected static char[] readTable(Class parserClass, String filename) throws java.io.IOException {

	//files are located at Parser.class directory

	InputStream stream = new BufferedInputStream(parserClass.getResourceAsStream(filename));
	if (stream == null) {
		throw new java.io.IOException(Util.bind("parser.missingFile",filename)); //$NON-NLS-1$
	}
	byte[] bytes = null;
	try {
		bytes = Util.getInputStreamAsByteArray(stream, -1);
	} finally {
		try {
			stream.close();
		} catch (IOException e) {
		}
	}

	//minimal integrity check (even size expected)
	int length = bytes.length;
	if (length % 2 != 0)
		throw new java.io.IOException(Util.bind("parser.corruptedFile",filename)); //$NON-NLS-1$

	// convert bytes into chars
	char[] chars = new char[length / 2];
	int i = 0;
	int charIndex = 0;

	while (true) {
		chars[charIndex++] = (char) (((bytes[i++] & 0xFF) << 8) + (bytes[i++] & 0xFF));
		if (i == length)
			break;
	}
	return chars;
}
/* Token check performed on every token shift once having entered
 * recovery mode.
 */
public void recoveryTokenCheck() {
	switch (currentToken) {
		case TokenNameLBRACE : {
			RecoveredElement newElement = 
				currentElement.updateOnOpeningBrace(scanner.currentPosition - 1);
			lastCheckPoint = scanner.currentPosition;				
			if (newElement != null){ // null means nothing happened
				restartRecovery = true; // opening brace detected
				currentElement = newElement;
			}
			break;
		}
		case TokenNameRBRACE : {
			endPosition = this.flushAnnotationsDefinedPriorTo(scanner.currentPosition - 1);
			RecoveredElement newElement =
				currentElement.updateOnClosingBrace(scanner.startPosition, scanner.currentPosition -1);
				lastCheckPoint = scanner.currentPosition;
				if (newElement != currentElement){
				currentElement = newElement;
			}
		}
	}
}
protected void reportSyntaxError(int act, int currentKind, int stateStackTop) {

	/* remember current scanner position */
	int startPos = scanner.startPosition;
	int currentPos = scanner.currentPosition;
	
	String[] expectings;
	String tokenName = nameInst[symbol_index[currentKind]];

	//fetch all "accurate" possible terminals that could recover the error
	int start, end = start = asi(stack[stateStackTop]);
	while (asr[end] != 0)
		end++;
	int length = end - start;
	expectings = new String[length];
	if (length != 0) {
		char[] indexes = new char[length];
		System.arraycopy(asr, start, indexes, 0, length);
		for (int i = 0; i < length; i++) {
			expectings[i] = nameInst[symbol_index[indexes[i]]];
		}
	}

	//if the pb is an EOF, try to tell the user that they are some 
	if (tokenName.equals(UNEXPECTED_EOF)) {
		if (!this.checkAndReportBracketAnomalies(problemReporter())) {
			char[] tokenSource;
			try {
				tokenSource = this.scanner.getCurrentTokenSource();
			} catch (Exception e) {
				tokenSource = new char[] {};
			}
			problemReporter().parseError(
				this.scanner.startPosition, 
				this.scanner.currentPosition - 1, 
				tokenSource, 
				tokenName, 
				expectings); 
		}
	} else { //the next test is HEAVILY grammar DEPENDENT.
		if ((length == 2)
			&& (tokenName.equals(";")) //$NON-NLS-1$
			&& (expectings[0] == "++") //$NON-NLS-1$
			&& (expectings[1] == "--") //$NON-NLS-1$
			&& (expressionPtr > -1)) {
			// the ; is not the expected token ==> it ends a statement when an expression is not ended
			problemReporter().invalidExpressionAsStatement(expressionStack[expressionPtr]);
		} else {
			char[] tokenSource;
			try {
				tokenSource = this.scanner.getCurrentTokenSource();
			} catch (Exception e) {
				tokenSource = new char[] {};
			}
			problemReporter().parseError(
				this.scanner.startPosition, 
				this.scanner.currentPosition - 1, 
				tokenSource, 
				tokenName, 
				expectings); 
			this.checkAndReportBracketAnomalies(problemReporter());
		}
	}
	/* reset scanner where it was */
	scanner.startPosition = startPos;
	scanner.currentPosition = currentPos;
}
protected void resetModifiers() {
	modifiers = AccDefault;
	modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)
	scanner.commentPtr = -1;
}
/*
 * Reset context so as to resume to regular parse loop
 */
protected void resetStacks() {

	astPtr = -1;
	astLengthPtr = -1;
	expressionPtr = -1;
	expressionLengthPtr = -1;
	identifierPtr = -1;	
	identifierLengthPtr	= -1;
	intPtr = -1;
	nestedMethod[nestedType = 0] = 0; // need to reset for further reuse
	variablesCounter[nestedType] = 0;
	dimensions = 0 ;
	realBlockStack[realBlockPtr = 0] = 0;
	recoveredStaticInitializerStart = 0;
	listLength = 0;
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {

	// reset internal stacks 
	this.resetStacks();
	
	/* attempt to move checkpoint location */
	if (!this.moveRecoveryCheckpoint()) return false;

	// only look for headers
	if (referenceContext instanceof CompilationUnitDeclaration){
		goForHeaders();
		diet = true; // passed this point, will not consider method bodies
		return true;
	}
	// does not know how to restart
	return false;
}
/*
 * Syntax error was detected. Will attempt to perform some recovery action in order
 * to resume to the regular parse loop.
 */
protected boolean resumeOnSyntaxError() {

	/* request recovery initialization */
	if (currentElement == null){
		currentElement = 
			this.buildInitialRecoveryState(); // build some recovered elements
	}
	/* do not investigate deeper in recovery when no recovered element */
	if (currentElement == null) return false;
	
	/* manual forced recovery restart - after headers */
	if (restartRecovery){
		restartRecovery = false;
	}
	/* update recovery state with current error state of the parser */
	this.updateRecoveryState();
	
	/* attempt to reset state in order to resume to parse loop */
	return this.resumeAfterRecovery();
}
protected int tAction(int state, int sym) {
	return action[check(state + sym) == sym ? state + sym : state];
}
public String toString() {

	String s = "identifierStack : char[][] = {"; //$NON-NLS-1$
	for (int i = 0; i <= identifierPtr; i++) {
		s = s + "\"" + String.valueOf(identifierStack[i]) + "\","; //$NON-NLS-1$ //$NON-NLS-2$
	};
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "identierLengthStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= identifierLengthPtr; i++) {
		s = s + identifierLengthStack[i] + ","; //$NON-NLS-1$
	};
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "astLengthStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= astLengthPtr; i++) {
		s = s + astLengthStack[i] + ","; //$NON-NLS-1$
	};
	s = s + "}\n"; //$NON-NLS-1$
	s = s + "astPtr : int = " + String.valueOf(astPtr) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

	s = s + "intStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= intPtr; i++) {
		s = s + intStack[i] + ","; //$NON-NLS-1$
	};
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "expressionLengthStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= expressionLengthPtr; i++) {
		s = s + expressionLengthStack[i] + ","; //$NON-NLS-1$
	};
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "expressionPtr : int = " + String.valueOf(expressionPtr) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

	s = s + "\n\n\n----------------Scanner--------------\n" + scanner.toString(); //$NON-NLS-1$
	return s;

}
/*
 * Update recovery state based on current parser/scanner state
 */
protected void updateRecoveryState() {

	/* expose parser state to recovery state */
	currentElement.updateFromParserState();

	/* check and update recovered state based on current token,
		this action is also performed when shifting token after recovery
		got activated once. 
	*/
	this.recoveryTokenCheck();
}
protected void updateSourceDeclarationParts(int variableDeclaratorsCounter) {
	//fields is a definition of fields that are grouped together like in
	//public int[] a, b[], c
	//which results into 3 fields.

	FieldDeclaration field;
	int endTypeDeclarationPosition = 
		-1 + astStack[astPtr - variableDeclaratorsCounter + 1].sourceStart; 
	for (int i = 0; i < variableDeclaratorsCounter - 1; i++) {
		//last one is special(see below)
		field = (FieldDeclaration) astStack[astPtr - i - 1];
		field.endPart1Position = endTypeDeclarationPosition;
		field.endPart2Position = -1 + astStack[astPtr - i].sourceStart;
	}
	//last one
	(field = (FieldDeclaration) astStack[astPtr]).endPart1Position = 
		endTypeDeclarationPosition; 
	field.endPart2Position = field.declarationSourceEnd;

}
protected void updateSourcePosition(Expression exp) {
	//update the source Position of the expression

	//intStack : int int
	//-->
	//intStack : 

	exp.sourceEnd = intStack[intPtr--];
	exp.sourceStart = intStack[intPtr--];
}
}
