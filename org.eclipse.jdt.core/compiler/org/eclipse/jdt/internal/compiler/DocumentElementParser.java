/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler;

/*
 * A document element parser extracts structural information
 * from a piece of source, providing detailed source positions info.
 *
 * also see @IDocumentElementRequestor
 *
 * The structural investigation includes:
 * - the package statement
 * - import statements
 * - top-level types: package member, member types (member types of member types...)
 * - fields
 * - methods
 *
 * Any (parsing) problem encountered is also provided.
 */
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class DocumentElementParser extends Parser {
	IDocumentElementRequestor requestor;
	private int localIntPtr;
	private int lastFieldEndPosition;
	private int lastFieldBodyEndPosition;	
	private int typeStartPosition;
	private long selectorSourcePositions;
	private int typeDims;
	private int extendsDim;
	private int declarationSourceStart;

	/* int[] stack for storing javadoc positions */
	int[][] intArrayStack;
	int intArrayPtr;
	
	CompilerOptions options;
	
public DocumentElementParser(
	final IDocumentElementRequestor requestor, 
	IProblemFactory problemFactory,
	CompilerOptions options) {
	super(new ProblemReporter(
		DefaultErrorHandlingPolicies.exitAfterAllProblems(), 
		options, 
		problemFactory) {
		public void record(IProblem problem, CompilationResult unitResult) {
			requestor.acceptProblem(problem);
		}
	},
	false,
	options.assertMode);
	this.requestor = requestor;
	intArrayStack = new int[30][];
	this.options = options;
}

/**
 *
 * INTERNAL USE-ONLY
 */
protected void adjustInterfaceModifiers() {
	intStack[intPtr - 2] |= AccInterface;
}
/*
 * Will clear the comment stack when looking
 * for a potential JavaDoc which might contain @deprecated.
 *
 * Additionally, before investigating for @deprecated, retrieve the positions
 * of the JavaDoc comments so as to notify requestor with them.
 */
public void checkAnnotation() {

	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());
	boolean deprecated = false;
	int lastAnnotationIndex = -1;

	//since jdk1.2 look only in the last java doc comment...
	found : {
		if ((lastAnnotationIndex = scanner.commentPtr) >= 0) { //look for @deprecated
			scanner.commentPtr = -1;
			// reset the comment stack, since not necessary after having checked
			int commentSourceStart = scanner.commentStarts[lastAnnotationIndex];
			// javadoc only (non javadoc comment have negative end positions.)
			int commentSourceEnd = scanner.commentStops[lastAnnotationIndex] - 1;
			//stop is one over
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
					// ensure the tag is properly ended: either followed by a space, line end or asterisk.
					int nextPos = i + 11;
					deprecated = 
						(comment[nextPos] == ' ')
							|| (comment[nextPos] == '\n')
							|| (comment[nextPos] == '\r')
							|| (comment[nextPos] == '*'); 
					break found;
				}
			}
		}
	}
	if (deprecated) {
		checkAndSetModifiers(AccDeprecated);
	}
	// modify the modifier source start to point at the first comment
	if (lastAnnotationIndex >= 0) {
		declarationSourceStart = scanner.commentStarts[0];
	}
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeClassBodyDeclaration() {
	// ClassBodyDeclaration ::= Diet Block
	//push an Initializer
	//optimize the push/pop

	super.consumeClassBodyDeclaration();
	Initializer initializer = (Initializer) astStack[astPtr];
	requestor.acceptInitializer(
		initializer.declarationSourceStart,
		initializer.declarationSourceEnd,
		intArrayStack[intArrayPtr--], 
		0,
		modifiersSourceStart, 
		initializer.block.sourceStart,
		initializer.block.sourceEnd);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeClassDeclaration() {
	super.consumeClassDeclaration();
	// we know that we have a TypeDeclaration on the top of the astStack
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	requestor.exitClass(endStatementPosition, // '}' is the end of the body 
	 ((TypeDeclaration) astStack[astPtr]).declarationSourceEnd);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeClassHeader() {
	//ClassHeader ::= $empty
	super.consumeClassHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		intArrayPtr--;
		return;
	}
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	TypeReference[] superInterfaces = typeDecl.superInterfaces;
	char[][] interfaceNames = null;
	int[] interfaceNameStarts = null;
	int[] interfaceNameEnds = null;
	if (superInterfaces != null) {
		int superInterfacesLength = superInterfaces.length;
		interfaceNames = new char[superInterfacesLength][];
		interfaceNameStarts = new int[superInterfacesLength];
		interfaceNameEnds = new int[superInterfacesLength];
		for (int i = 0; i < superInterfacesLength; i++) {
			TypeReference superInterface = superInterfaces[i];
			interfaceNames[i] = CharOperation.concatWith(superInterface.getTypeName(), '.'); 
			interfaceNameStarts[i] = superInterface.sourceStart;
			interfaceNameEnds[i] = superInterface.sourceEnd;
		}
	}
	// flush the comments related to the class header
	scanner.commentPtr = -1;
	TypeReference superclass = typeDecl.superclass;
	if (superclass == null) {
		requestor.enterClass(
			typeDecl.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			typeDecl.modifiers, 
			typeDecl.modifiersSourceStart, 
			typeStartPosition, 
			typeDecl.name, 
			typeDecl.sourceStart, 
			typeDecl.sourceEnd, 
			null, 
			-1, 
			-1, 
			interfaceNames, 
			interfaceNameStarts, 
			interfaceNameEnds, 
			scanner.currentPosition - 1); 
	} else {
		requestor.enterClass(
			typeDecl.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			typeDecl.modifiers, 
			typeDecl.modifiersSourceStart, 
			typeStartPosition, 
			typeDecl.name, 
			typeDecl.sourceStart, 
			typeDecl.sourceEnd, 
			CharOperation.concatWith(superclass.getTypeName(), '.'), 
			superclass.sourceStart, 
			superclass.sourceEnd, 
			interfaceNames, 
			interfaceNameStarts, 
			interfaceNameEnds, 
			scanner.currentPosition - 1); 

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
	// 'class' and 'interface' push an int position
	typeStartPosition = typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--;
	int declarationSourceStart = intStack[intPtr--];
	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.declarationSourceStart > declarationSourceStart) {
		typeDecl.declarationSourceStart = declarationSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeCompilationUnit() {
	// CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt
	requestor.exitCompilationUnit(scanner.source.length - 1);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeConstructorDeclaration() {
	// ConstructorDeclaration ::= ConstructorHeader ConstructorBody
	super.consumeConstructorDeclaration();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];
	requestor.exitConstructor(endStatementPosition, cd.declarationSourceEnd);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeConstructorHeader() {
	// ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt
	super.consumeConstructorHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		intArrayPtr--;
		return;
	}
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];
	Argument[] arguments = cd.arguments;
	char[][] argumentTypes = null;
	char[][] argumentNames = null;
	int[] argumentTypeStarts = null;
	int[] argumentTypeEnds = null;
	int[] argumentNameStarts = null;
	int[] argumentNameEnds = null;
	if (arguments != null) {
		int argumentLength = arguments.length;
		argumentTypes = new char[argumentLength][];
		argumentNames = new char[argumentLength][];
		argumentNameStarts = new int[argumentLength];
		argumentNameEnds = new int[argumentLength];
		argumentTypeStarts = new int[argumentLength];
		argumentTypeEnds = new int[argumentLength];
		for (int i = 0; i < argumentLength; i++) {
			Argument argument = arguments[i];
			TypeReference argumentType = argument.type;
			argumentTypes[i] = returnTypeName(argumentType);
			argumentNames[i] = argument.name;
			argumentNameStarts[i] = argument.sourceStart;
			argumentNameEnds[i] = argument.sourceEnd;
			argumentTypeStarts[i] = argumentType.sourceStart;
			argumentTypeEnds[i] = argumentType.sourceEnd;
		}
	}
	TypeReference[] thrownExceptions = cd.thrownExceptions;
	char[][] exceptionTypes = null;
	int[] exceptionTypeStarts = null;
	int[] exceptionTypeEnds = null;
	if (thrownExceptions != null) {
		int thrownExceptionLength = thrownExceptions.length;
		exceptionTypes = new char[thrownExceptionLength][];
		exceptionTypeStarts = new int[thrownExceptionLength];
		exceptionTypeEnds = new int[thrownExceptionLength];
		for (int i = 0; i < thrownExceptionLength; i++) {
			TypeReference exception = thrownExceptions[i];
			exceptionTypes[i] = CharOperation.concatWith(exception.getTypeName(), '.');
			exceptionTypeStarts[i] = exception.sourceStart;
			exceptionTypeEnds[i] = exception.sourceEnd;
		}
	}
	requestor
		.enterConstructor(
			cd.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			cd.modifiers,
			cd.modifiersSourceStart, 
			cd.selector, 
			cd.sourceStart, 
			(int) (selectorSourcePositions & 0xFFFFFFFFL), 
			// retrieve the source end of the name
			argumentTypes, 
			argumentTypeStarts, 
			argumentTypeEnds, 
			argumentNames, 
			argumentNameStarts, 
			argumentNameEnds, 
			rParenPos, 
			// right parenthesis
			exceptionTypes, 
			exceptionTypeStarts, 
			exceptionTypeEnds, 
			scanner.currentPosition - 1); 
}
protected void consumeConstructorHeaderName() {
	// ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('
	ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiersSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];

	//highlight starts at the selector starts
	cd.sourceStart = (int) (selectorSourcePositions >>> 32);
	pushOnAstStack(cd);

	cd.sourceEnd = lParenPos;
	cd.bodyStart = lParenPos + 1;
}
protected void consumeDefaultModifiers() {
	checkAnnotation(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(-1);
	pushOnIntStack(
		declarationSourceStart >= 0 ? declarationSourceStart : scanner.startPosition); 
	resetModifiers();
}
protected void consumeDiet() {
	// Diet ::= $empty
	super.consumeDiet();
	/* persisting javadoc positions
	 * Will be consume in consumeClassBodyDeclaration
	 */
	pushOnIntArrayStack(this.getJavaDocPositions());	
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeEnterCompilationUnit() {
	// EnterCompilationUnit ::= $empty
	requestor.enterCompilationUnit();
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	boolean isLocalDeclaration = isLocalDeclaration();
	if (!isLocalDeclaration && (variablesCounter[nestedType] != 0)) {
		requestor.exitField(lastFieldBodyEndPosition, lastFieldEndPosition);
	}
	char[] name = identifierStack[identifierPtr];
	long namePosition = identifierPositionStack[identifierPtr--];
	int extendedTypeDimension = intStack[intPtr--];

	AbstractVariableDeclaration declaration;
	if (nestedMethod[nestedType] != 0) {
		// create the local variable declarations
		declaration = 
			new LocalDeclaration(null, name, (int) (namePosition >>> 32), (int) namePosition); 
	} else {
		// create the field declaration
		declaration = 
			new FieldDeclaration(null, name, (int) (namePosition >>> 32), (int) namePosition); 
	}
	identifierLengthPtr--;
	TypeReference type;
	int variableIndex = variablesCounter[nestedType];
	int typeDim = 0;
	if (variableIndex == 0) {
		// first variable of the declaration (FieldDeclaration or LocalDeclaration)
		if (nestedMethod[nestedType] != 0) {
			// local declaration
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiersSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			pushOnAstStack(type);
		} else {
			// field declaration
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			pushOnAstStack(type);
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiersSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
		}
	} else {
		type = (TypeReference) astStack[astPtr - variableIndex];
		typeDim = type.dimensions();
		AbstractVariableDeclaration previousVariable = 
			(AbstractVariableDeclaration) astStack[astPtr]; 
		declaration.declarationSourceStart = previousVariable.declarationSourceStart;
		declaration.modifiers = previousVariable.modifiers;
		declaration.modifiersSourceStart = previousVariable.modifiersSourceStart;
	}

	localIntPtr = intPtr;

	if (extendedTypeDimension == 0) {
		declaration.type = type;
	} else {
		int dimension = typeDim + extendedTypeDimension;
		//on the identifierLengthStack there is the information about the type....
		int baseType;
		if ((baseType = identifierLengthStack[identifierLengthPtr + 1]) < 0) {
			//it was a baseType
			declaration.type = TypeReference.baseTypeReference(-baseType, dimension);
			declaration.type.sourceStart = type.sourceStart;
			declaration.type.sourceEnd = type.sourceEnd;
		} else {
			declaration.type = this.copyDims(type, dimension);
		}
	}
	variablesCounter[nestedType]++;
	nestedMethod[nestedType]++;
	pushOnAstStack(declaration);

	int[] javadocPositions = intArrayStack[intArrayPtr];
	if (!isLocalDeclaration) {
		requestor
			.enterField(
				declaration.declarationSourceStart, 
				javadocPositions, 
				declaration.modifiers, 
				declaration.modifiersSourceStart, 
				returnTypeName(declaration.type), 
				type.sourceStart, 
				type.sourceEnd, 
				typeDims, 
				name, 
				(int) (namePosition >>> 32), 
				(int) namePosition, 
				extendedTypeDimension, 
				extendedTypeDimension == 0 ? -1 : endPosition); 
	}
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// the scanner is located after the comma or the semi-colon.
	// we want to include the comma or the semi-colon
	super.consumeExitVariableWithInitialization();
	nestedMethod[nestedType]--;	
	lastFieldEndPosition = scanner.currentPosition - 1;
	lastFieldBodyEndPosition = 	((AbstractVariableDeclaration) astStack[astPtr]).initialization.sourceEnd;
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	super.consumeExitVariableWithoutInitialization();
	nestedMethod[nestedType]--;	
	lastFieldEndPosition = scanner.currentPosition - 1;
	lastFieldBodyEndPosition = scanner.startPosition - 1;
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeFieldDeclaration() {
	// See consumeLocalVariableDeclarationDefaultModifier() in case of change: duplicated code
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'
	// the super.consumeFieldDeclaration will reinitialize the variableCounter[nestedType]	
	int variableIndex = variablesCounter[nestedType];
	super.consumeFieldDeclaration();
	intArrayPtr--;
	if (isLocalDeclaration())
		return;
	if (variableIndex != 0) {
		requestor.exitField(lastFieldBodyEndPosition, lastFieldEndPosition);
	}
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
	intPtr -= 3;
	Argument arg = 
		new Argument(
			name, 
			namePositions, 
			type, 
			intStack[intPtr + 1]); // modifiers
	pushOnAstStack(arg);
	intArrayPtr--;
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeInterfaceDeclaration() {
	super.consumeInterfaceDeclaration();
	// we know that we have a TypeDeclaration on the top of the astStack
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	requestor.exitInterface(endStatementPosition, // the '}' is the end of the body
	 ((TypeDeclaration) astStack[astPtr]).declarationSourceEnd);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeInterfaceHeader() {
	//InterfaceHeader ::= $empty
	super.consumeInterfaceHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		intArrayPtr--;
		return;
	}
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	TypeReference[] superInterfaces = typeDecl.superInterfaces;
	char[][] interfaceNames = null;
	int[] interfaceNameStarts = null;
	int[] interfacenameEnds = null;
	int superInterfacesLength = 0;
	if (superInterfaces != null) {
		superInterfacesLength = superInterfaces.length;
		interfaceNames = new char[superInterfacesLength][];
		interfaceNameStarts = new int[superInterfacesLength];
		interfacenameEnds = new int[superInterfacesLength];
	}
	if (superInterfaces != null) {
		for (int i = 0; i < superInterfacesLength; i++) {
			TypeReference superInterface = superInterfaces[i];
			interfaceNames[i] = CharOperation.concatWith(superInterface.getTypeName(), '.'); 
			interfaceNameStarts[i] = superInterface.sourceStart;
			interfacenameEnds[i] = superInterface.sourceEnd;
		}
	}
	// flush the comments related to the interface header
	scanner.commentPtr = -1;
	requestor.enterInterface(
		typeDecl.declarationSourceStart, 
		intArrayStack[intArrayPtr--], 
		typeDecl.modifiers, 
		typeDecl.modifiersSourceStart, 
		typeStartPosition, 
		typeDecl.name, 
		typeDecl.sourceStart, 
		typeDecl.sourceEnd, 
		interfaceNames, 
		interfaceNameStarts, 
		interfacenameEnds, 
		scanner.currentPosition - 1); 
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
	// 'class' and 'interface' push an int position
	typeStartPosition = typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--;
	int declarationSourceStart = intStack[intPtr--];
	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.declarationSourceStart > declarationSourceStart) {
		typeDecl.declarationSourceStart = declarationSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeLocalVariableDeclaration() {
	// See consumeLocalVariableDeclarationDefaultModifier() in case of change: duplicated code
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'

	super.consumeLocalVariableDeclaration();
	intArrayPtr--;
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodDeclaration(boolean isNotAbstract) {
	// MethodDeclaration ::= MethodHeader MethodBody
	// AbstractMethodDeclaration ::= MethodHeader ';'
	super.consumeMethodDeclaration(isNotAbstract);
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		return;
	}
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	requestor.exitMethod(endStatementPosition, md.declarationSourceEnd);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodHeader() {
	// MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims ThrowsClauseopt
	super.consumeMethodHeader();
	if (isLocalDeclaration()) {
		// we ignore the local variable declarations
		intArrayPtr--;
		return;
	}
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];

	TypeReference returnType = md.returnType;
	char[] returnTypeName = returnTypeName(returnType);
	Argument[] arguments = md.arguments;
	char[][] argumentTypes = null;
	char[][] argumentNames = null;
	int[] argumentTypeStarts = null;
	int[] argumentTypeEnds = null;
	int[] argumentNameStarts = null;
	int[] argumentNameEnds = null;
	if (arguments != null) {
		int argumentLength = arguments.length;
		argumentTypes = new char[argumentLength][];
		argumentNames = new char[argumentLength][];
		argumentNameStarts = new int[argumentLength];
		argumentNameEnds = new int[argumentLength];
		argumentTypeStarts = new int[argumentLength];
		argumentTypeEnds = new int[argumentLength];
		for (int i = 0; i < argumentLength; i++) {
			Argument argument = arguments[i];
			TypeReference argumentType = argument.type;
			argumentTypes[i] = returnTypeName(argumentType);
			argumentNames[i] = argument.name;
			argumentNameStarts[i] = argument.sourceStart;
			argumentNameEnds[i] = argument.sourceEnd;
			argumentTypeStarts[i] = argumentType.sourceStart;
			argumentTypeEnds[i] = argumentType.sourceEnd;
		}
	}
	TypeReference[] thrownExceptions = md.thrownExceptions;
	char[][] exceptionTypes = null;
	int[] exceptionTypeStarts = null;
	int[] exceptionTypeEnds = null;
	if (thrownExceptions != null) {
		int thrownExceptionLength = thrownExceptions.length;
		exceptionTypeStarts = new int[thrownExceptionLength];
		exceptionTypeEnds = new int[thrownExceptionLength];
		exceptionTypes = new char[thrownExceptionLength][];
		for (int i = 0; i < thrownExceptionLength; i++) {
			TypeReference exception = thrownExceptions[i];
			exceptionTypes[i] = CharOperation.concatWith(exception.getTypeName(), '.');
			exceptionTypeStarts[i] = exception.sourceStart;
			exceptionTypeEnds[i] = exception.sourceEnd;
		}
	}
	requestor
		.enterMethod(
			md.declarationSourceStart, 
			intArrayStack[intArrayPtr--], 
			md.modifiers, 
			md.modifiersSourceStart, 
			returnTypeName, 
			returnType.sourceStart, 
			returnType.sourceEnd, 
			typeDims, 
			md.selector, 
			md.sourceStart, 
			(int) (selectorSourcePositions & 0xFFFFFFFFL), 
			argumentTypes, 
			argumentTypeStarts, 
			argumentTypeEnds, 
			argumentNames, 
			argumentNameStarts, 
			argumentNameEnds, 
			rParenPos, 
			extendsDim, 
			extendsDim == 0 ? -1 : endPosition, 
			exceptionTypes, 
			exceptionTypeStarts, 
			exceptionTypeEnds, 
			scanner.currentPosition - 1); 
}
protected void consumeMethodHeaderExtendedDims() {
	// MethodHeaderExtendedDims ::= Dimsopt
	// now we update the returnType of the method
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	int extendedDims = intStack[intPtr--];
	extendsDim = extendedDims;
	if (extendedDims != 0) {
		TypeReference returnType = md.returnType;
		md.sourceEnd = endPosition;
		int dims = returnType.dimensions() + extendedDims;
		int baseType;
		if ((baseType = identifierLengthStack[identifierLengthPtr + 1]) < 0) {
			//it was a baseType
			int sourceStart = returnType.sourceStart;
			int sourceEnd = returnType.sourceEnd;
			returnType = TypeReference.baseTypeReference(-baseType, dims);
			returnType.sourceStart = sourceStart;
			returnType.sourceEnd = sourceEnd;
			md.returnType = returnType;
		} else {
			md.returnType = this.copyDims(md.returnType, dims);
		}
		if (currentToken == TokenNameLBRACE) {
			md.bodyStart = endPosition + 1;
		}
	}
}
protected void consumeMethodHeaderName() {
	// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	MethodDeclaration md = new MethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = identifierStack[identifierPtr];
	selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(typeDims = intStack[intPtr--]);
	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiersSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSourcePositions >>> 32);
	pushOnAstStack(md);
	md.bodyStart = scanner.currentPosition-1;
}
protected void consumeModifiers() {
	checkAnnotation(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(modifiersSourceStart);
	pushOnIntStack(
		declarationSourceStart >= 0 ? declarationSourceStart : modifiersSourceStart); 
	resetModifiers();
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumePackageDeclarationName() {
	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumePackageDeclarationName();
	ImportReference importReference = compilationUnit.currentPackage;

	requestor.acceptPackage(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd, 
		intArrayStack[intArrayPtr--], 
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart);
}
protected void consumePushModifiers() {
	checkAnnotation(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	if (modifiersSourceStart < 0) {
		pushOnIntStack(-1);
		pushOnIntStack(
			declarationSourceStart >= 0 ? declarationSourceStart : scanner.startPosition); 
	} else {
		pushOnIntStack(modifiersSourceStart);
		pushOnIntStack(
			declarationSourceStart >= 0 ? declarationSourceStart : modifiersSourceStart); 
	}
	resetModifiers();
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name

	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumeSingleTypeImportDeclarationName();
	ImportReference importReference = (ImportReference) astStack[astPtr];
	requestor.acceptImport(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'),
		importReference.sourceStart,
		false);
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeStaticInitializer() {
	// StaticInitializer ::=  StaticOnly Block
	//push an Initializer
	//optimize the push/pop
	super.consumeStaticInitializer();
	Initializer initializer = (Initializer) astStack[astPtr];
	requestor.acceptInitializer(
		initializer.declarationSourceStart,
		initializer.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
		AccStatic, 
		intStack[intPtr--], 
		initializer.block.sourceStart,
		initializer.declarationSourceEnd);
}
protected void consumeStaticOnly() {
	// StaticOnly ::= 'static'
	checkAnnotation(); // might update declaration source start
	pushOnIntStack(modifiersSourceStart);
	pushOnIntStack(
		declarationSourceStart >= 0 ? declarationSourceStart : modifiersSourceStart); 
	jumpOverMethodBody();
	nestedMethod[nestedType]++;
	resetModifiers();
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'

	/* persisting javadoc positions */
	pushOnIntArrayStack(this.getJavaDocPositions());

	super.consumeTypeImportOnDemandDeclarationName();
	ImportReference importReference = (ImportReference) astStack[astPtr];
	requestor.acceptImport(
		importReference.declarationSourceStart, 
		importReference.declarationSourceEnd,
		intArrayStack[intArrayPtr--],
		CharOperation.concatWith(importReference.getImportName(), '.'), 
		importReference.sourceStart,
		true);
}
public CompilationUnitDeclaration endParse(int act) {
	if (scanner.recordLineSeparator) {
		requestor.acceptLineSeparatorPositions(scanner.getLineEnds());
	}
	return super.endParse(act);
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

	return lastFieldEndPosition = super.flushAnnotationsDefinedPriorTo(position);
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
public void initialize() {
	//positionning the parser for a new compilation unit
	//avoiding stack reallocation and all that....
	super.initialize();
	intArrayPtr = -1;
}
/**
 *
 * INTERNAL USE-ONLY
 */
private boolean isLocalDeclaration() {
	int nestedDepth = nestedType;
	while (nestedDepth >= 0) {
		if (nestedMethod[nestedDepth] != 0) {
			return true;
		}
		nestedDepth--;
	}
	return false;
}
/*
 * Investigate one entire unit.
 */
public void parseCompilationUnit(ICompilationUnit unit) {
	char[] regionSource = unit.getContents();
	try {
		initialize();
		goForCompilationUnit();
		referenceContext =
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}
}
/*
 * Investigate one constructor declaration.
 */
public void parseConstructor(char[] regionSource) {
	try {
		initialize();
		goForClassBodyDeclarations();
		referenceContext = 
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}
}
/*
 * Investigate one field declaration statement (might have multiple declarations in it).
 */
public void parseField(char[] regionSource) {
	try {
		initialize();
		goForFieldDeclaration();
		referenceContext = 
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}

}
/*
 * Investigate one import statement declaration.
 */
public void parseImport(char[] regionSource) {
	try {
		initialize();
		goForImportDeclaration();
		referenceContext = 
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}

}
/*
 * Investigate one initializer declaration.
 * regionSource need to content exactly an initializer declaration.
 * e.g: static { i = 4; }
 * { name = "test"; }
 */
public void parseInitializer(char[] regionSource) {
	try {
		initialize();
		goForInitializer();
		referenceContext = 
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}

}
/*
 * Investigate one method declaration.
 */
public void parseMethod(char[] regionSource) {
	try {
		initialize();
		goForGenericMethodDeclaration();
		referenceContext = 
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}

}
/*
 * Investigate one package statement declaration.
 */
public void parsePackage(char[] regionSource) {
	try {
		initialize();
		goForPackageDeclaration();
		referenceContext = 
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}

}
/*
 * Investigate one type declaration, its fields, methods and member types.
 */
public void parseType(char[] regionSource) {
	try {
		initialize();
		goForTypeDeclaration();
		referenceContext = 
			compilationUnit = 
				compilationUnit = 
					new CompilationUnitDeclaration(
						problemReporter(), 
						new CompilationResult(regionSource, 0, 0, this.options.maxProblemsPerUnit), 
						regionSource.length); 
		scanner.resetTo(0, regionSource.length);
		scanner.setSource(regionSource);
		parse();
	} catch (AbortCompilation ex) {
	}

}
/**
 * Returns this parser's problem reporter initialized with its reference context.
 * Also it is assumed that a problem is going to be reported, so initializes
 * the compilation result's line positions.
 */
public ProblemReporter problemReporter() {
	problemReporter.referenceContext = referenceContext;
	return problemReporter;
}
protected void pushOnIntArrayStack(int[] positions) {

	try {
		intArrayStack[++intArrayPtr] = positions;
	} catch (IndexOutOfBoundsException e) {
		//intPtr is correct 
		int oldStackLength = intArrayStack.length;
		int oldStack[][] = intArrayStack;
		intArrayStack = new int[oldStackLength + StackIncrement][];
		System.arraycopy(oldStack, 0, intArrayStack, 0, oldStackLength);
		intArrayStack[intArrayPtr] = positions;
	}
}
protected void resetModifiers() {
	super.resetModifiers();
	declarationSourceStart = -1;
}
/*
 * Syntax error was detected. Will attempt to perform some recovery action in order
 * to resume to the regular parse loop.
 */
protected boolean resumeOnSyntaxError() {
	return false;
}
/*
 * Answer a char array representation of the type name formatted like:
 * - type name + dimensions
 * Example:
 * "A[][]".toCharArray()
 * "java.lang.String".toCharArray()
 */
private char[] returnTypeName(TypeReference type) {
	int dimension = type.dimensions();
	if (dimension != 0) {
		char[] dimensionsArray = new char[dimension * 2];
		for (int i = 0; i < dimension; i++) {
			dimensionsArray[i*2] = '[';
			dimensionsArray[(i*2) + 1] = ']';
		}
		return CharOperation.concat(
			CharOperation.concatWith(type.getTypeName(), '.'), 
			dimensionsArray); 
	}
	return CharOperation.concatWith(type.getTypeName(), '.');
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("intArrayPtr = " + intArrayPtr + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	buffer.append(super.toString());
	return buffer.toString();
}
/**
 * INTERNAL USE ONLY
 */
protected TypeReference typeReference(
	int dim,
	int localIdentifierPtr, 
	int localIdentifierLengthPtr) {
	/* build a Reference on a variable that may be qualified or not
	 * This variable is a type reference and dim will be its dimensions.
	 * We don't have any side effect on the stacks' pointers.
	 */

	int length;
	TypeReference ref;
	if ((length = identifierLengthStack[localIdentifierLengthPtr]) == 1) {
		// single variable reference
		if (dim == 0) {
			ref = 
				new SingleTypeReference(
					identifierStack[localIdentifierPtr], 
					identifierPositionStack[localIdentifierPtr--]); 
		} else {
			ref = 
				new ArrayTypeReference(
					identifierStack[localIdentifierPtr], 
					dim, 
					identifierPositionStack[localIdentifierPtr--]); 
			ref.sourceEnd = endPosition;				
		}
	} else {
		if (length < 0) { //flag for precompiled type reference on base types
			ref = TypeReference.baseTypeReference(-length, dim);
			ref.sourceStart = intStack[localIntPtr--];
			if (dim == 0) {
				ref.sourceEnd = intStack[localIntPtr--];
			} else {
				localIntPtr--;
				ref.sourceEnd = endPosition;
			}
		} else { //Qualified variable reference
			char[][] tokens = new char[length][];
			localIdentifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(identifierStack, localIdentifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				identifierPositionStack, 
				localIdentifierPtr + 1, 
				positions, 
				0, 
				length); 
			if (dim == 0)
				ref = new QualifiedTypeReference(tokens, positions);
			else
				ref = new ArrayQualifiedTypeReference(tokens, dim, positions);
		}
	};
	return ref;
}
}
