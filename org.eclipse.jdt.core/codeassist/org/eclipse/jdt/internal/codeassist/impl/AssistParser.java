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
package org.eclipse.jdt.internal.codeassist.impl;

/*
 * Parser extension for code assist task
 *
 */

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.RecoveredElement;
import org.eclipse.jdt.internal.compiler.parser.RecoveredField;
import org.eclipse.jdt.internal.compiler.parser.RecoveredInitializer;
import org.eclipse.jdt.internal.compiler.parser.RecoveredMethod;
import org.eclipse.jdt.internal.compiler.parser.RecoveredType;
import org.eclipse.jdt.internal.compiler.parser.RecoveredUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public abstract class AssistParser extends Parser {

	public AstNode assistNode;
	public boolean isOrphanCompletionNode;
		
	/* recovery */
	int[] blockStarts = new int[30];

	// the previous token read by the scanner
	protected int previousToken;

	// the index in the identifier stack of the previous identifier
	protected int previousIdentifierPtr;
	
	// the stacks of selectors for invocations (ie. method invocations, allocation expressions and
	// explicit constructor invocations)
	// the selector stack contains pointers to the identifier stack or one of the selector constants below
	protected int invocationPtr;
	protected int[] selectorStack = new int[StackIncrement];

	// selector constants
	protected static final int THIS_CONSTRUCTOR = -1;
	protected static final int SUPER_CONSTRUCTOR = -2;

	// whether the parser is in a field initializer
	// (false is pushed each time a new type is entered,
	//  it is changed to true when the initializer is entered,
	//  it is changed back to false when the initializer is exited,
	//  and it is poped when the type is exited)
	protected int inFieldInitializationPtr;
	protected boolean[] inFieldInitializationStack = new boolean[StackIncrement];

	// whether the parser is in a method, constructor or initializer
	// (false is pushed each time a new type is entered,
	//  it is changed to true when the method is entered,
	//  it is changed back to false when the method is exited,
	//  and it is poped when the type is exited)
	protected int inMethodPtr;
	protected boolean[] inMethodStack = new boolean[StackIncrement];
public AssistParser(ProblemReporter problemReporter, boolean assertMode) {
	super(problemReporter, true, assertMode);
}
public abstract char[] assistIdentifier();
public int bodyEnd(AbstractMethodDeclaration method){
	return method.declarationSourceEnd;
}
public int bodyEnd(Initializer initializer){
	return initializer.declarationSourceEnd;
}
/*
 * Build initial recovery state.
 * Recovery state is inferred from the current state of the parser (reduced node stack).
 */
public RecoveredElement buildInitialRecoveryState(){

	/* recovery in unit structure */
	if (referenceContext instanceof CompilationUnitDeclaration){
		RecoveredElement element = super.buildInitialRecoveryState();
		flushAssistState();
		initInMethodAndInFieldInitializationStack(element);
		return element;
	}

	/* recovery in method body */
	lastCheckPoint = 0;

	RecoveredElement element = null;
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

	if (element == null) return element;

	/* add initial block */
	Block block = new Block(0);
	int lastStart = blockStarts[0];
	block.sourceStart = lastStart;
	element = element.add(block, 1);
	int blockIndex = 1;	// ignore first block start, since manually rebuilt here

	for(int i = 0; i <= astPtr; i++){
		AstNode node = astStack[i];

		/* check for intermediate block creation, so recovery can properly close them afterwards */
		int nodeStart = node.sourceStart;
		for (int j = blockIndex; j <= realBlockPtr; j++){
			if (blockStarts[j] > nodeStart){
				blockIndex = j; // shift the index to the new block
				break;
			}
			if (blockStarts[j] != lastStart){ // avoid multiple block if at same position
				block = new Block(0);
				block.sourceStart = lastStart = blockStarts[j];
				element = element.add(block, 1);
			}
			blockIndex = j+1; // shift the index to the new block
		}
		if (node instanceof LocalDeclaration){
			LocalDeclaration local = (LocalDeclaration) node;
			if (local.declarationSourceEnd == 0){
				element = element.add(local, 0);
				if (local.initialization == null){
					lastCheckPoint = local.sourceEnd + 1;
				} else {
					lastCheckPoint = local.initialization.sourceEnd + 1;
				}
			} else {
				element = element.add(local, 0);
				lastCheckPoint = local.declarationSourceEnd + 1;
			}
			continue;
		}		
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
	if (this.currentToken == TokenNameRBRACE) {
		this.currentToken = 0; // closing brace has already been taken care of
	}

	/* might need some extra block (after the last reduced node) */
	int pos = this.assistNode == null ? lastCheckPoint : this.assistNode.sourceStart;
	for (int j = blockIndex; j <= realBlockPtr; j++){
		if ((blockStarts[j] < pos) && (blockStarts[j] != lastStart)){ // avoid multiple block if at same position
			block = new Block(0);
			block.sourceStart = lastStart = blockStarts[j];
			element = element.add(block, 1);
		}
	}
	
	initInMethodAndInFieldInitializationStack(element);
	return element;
}
protected void consumeClassBodyDeclarationsopt() {
	super.consumeClassBodyDeclarationsopt();
	this.inFieldInitializationPtr--;
	this.inMethodPtr--;
}
protected void consumeClassBodyopt() {
	super.consumeClassBodyopt();
	this.invocationPtr--; // NB: This can be decremented below -1 only if in diet mode and not in field initializer
}
protected void consumeClassHeader() {
	super.consumeClassHeader();
	this.pushNotInInitializer();
	this.pushNotInMethod();
}
protected void consumeConstructorBody() {
	super.consumeConstructorBody();
	this.inMethodStack[this.inMethodPtr] = false;
}
protected void consumeConstructorHeader() {
	super.consumeConstructorHeader();
	this.inMethodStack[this.inMethodPtr] = true;
}
protected void consumeEmptyClassBodyDeclarationsopt() {
	super.consumeEmptyClassBodyDeclarationsopt();
	this.inFieldInitializationPtr--;
	this.inMethodPtr--;
}
protected void consumeEnterAnonymousClassBody() {
	super.consumeEnterAnonymousClassBody();
	this.invocationPtr--; // NB: This can be decremented below -1 only if in diet mode and not in field initializer
	this.pushNotInInitializer();
	this.pushNotInMethod();
}
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
	super.consumeExplicitConstructorInvocation(flag, recFlag);
	this.invocationPtr--; // NB: This can be decremented below -1 only if in diet mode and not in field initializer
}
protected void consumeForceNoDiet() {
	super.consumeForceNoDiet();
	// if we are not in a method (ie. we are not in a local variable initializer)
	// then we are entering a field initializer
	if (!this.inMethodStack[this.inMethodPtr]) {
		this.inFieldInitializationStack[this.inFieldInitializationPtr] = true;
	}
}
protected void consumeInterfaceHeader() {
	super.consumeInterfaceHeader();
	this.pushNotInInitializer();
	this.pushNotInMethod();
}
protected void consumeInterfaceMemberDeclarationsopt() {
	super.consumeInterfaceMemberDeclarationsopt();
	this.inFieldInitializationPtr--;
	this.inMethodPtr--;
}
protected void consumeMethodBody() {
	super.consumeMethodBody();
	this.inMethodStack[this.inMethodPtr] = false;
}
protected void consumeMethodHeader() {
	super.consumeMethodHeader();
	this.inMethodStack[this.inMethodPtr] = true;
}
protected void consumeMethodInvocationName() {
	super.consumeMethodInvocationName();
	this.invocationPtr--; // NB: This can be decremented below -1 only if in diet mode and not in field initializer
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary();
	this.invocationPtr--; // NB: This can be decremented below -1 only if in diet mode and not in field initializer
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeMethodInvocationSuper() {
	super.consumeMethodInvocationSuper();
	this.invocationPtr--; // NB: This can be decremented below -1 only if in diet mode and not in field initializer 
	MessageSend messageSend = (MessageSend)expressionStack[expressionPtr];
	if (messageSend == assistNode){
		this.lastCheckPoint = messageSend.sourceEnd + 1;
	}
}
protected void consumeNestedMethod() {
	super.consumeNestedMethod();
	this.inMethodStack[this.inMethodPtr] = true;
}
protected void consumeOpenBlock() {
	// OpenBlock ::= $empty

	super.consumeOpenBlock();
	try {
		blockStarts[realBlockPtr] = scanner.startPosition;
	} catch (IndexOutOfBoundsException e) {
		//realBlockPtr is correct 
		int oldStackLength = blockStarts.length;
		int oldStack[] = blockStarts;
		blockStarts = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, blockStarts, 0, oldStackLength);
		blockStarts[realBlockPtr] = scanner.startPosition;
	}
}
protected void consumePackageDeclarationName() {
	// PackageDeclarationName ::= 'package' Name
	/* build an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumePackageDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on package statement */
	ImportReference reference = this.createAssistPackageReference(subset, positions);
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;
	compilationUnit.currentPackage = reference; 

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush annotations defined prior to import statements
	reference.declarationSourceEnd = this.flushAnnotationsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumeRestoreDiet() {
	super.consumeRestoreDiet();
	// if we are not in a method (ie. we were not in a local variable initializer)
	// then we are exiting a field initializer
	if (!this.inMethodStack[this.inMethodPtr]) {
		this.inFieldInitializationStack[this.inFieldInitializationPtr] = false;
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumeSingleTypeImportDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on import statement */
	ImportReference reference = this.createAssistImportReference(subset, positions);
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;

	pushOnAstStack(reference);

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush annotations defined prior to import statements
	reference.declarationSourceEnd = this.flushAnnotationsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		currentElement = currentElement.add(reference, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
protected void consumeStaticInitializer() {
	super.consumeStaticInitializer();
	this.inMethodStack[this.inMethodPtr] = false;
}
protected void consumeStaticOnly() {
	super.consumeStaticOnly();
	this.inMethodStack[this.inMethodPtr] = true;
}
protected void consumeToken(int token) {
	super.consumeToken(token);
	// register message send selector only if inside a method or if looking at a field initializer 
	// and if the current token is an open parenthesis
	if ((this.inMethodStack[this.inMethodPtr] || this.inFieldInitializationStack[this.inFieldInitializationPtr]) && token == TokenNameLPAREN) {
		switch (this.previousToken) {
			case TokenNameIdentifier:
				this.pushOnSelectorStack(this.identifierPtr);
				break;
			case TokenNamethis: // explicit constructor invocation, eg. this(1, 2)
				this.pushOnSelectorStack(THIS_CONSTRUCTOR);
				break;
			case TokenNamesuper: // explicit constructor invocation, eg. super(1, 2)
				this.pushOnSelectorStack(SUPER_CONSTRUCTOR);
				break;
		}
	}
	this.previousToken = token;
	if (token == TokenNameIdentifier) {
		this.previousIdentifierPtr = this.identifierPtr;
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumeTypeImportOnDemandDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on import statement */
	ImportReference reference = this.createAssistImportReference(subset, positions);
	reference.onDemand = true;
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;

	pushOnAstStack(reference);

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush annotations defined prior to import statements
	reference.declarationSourceEnd = this.flushAnnotationsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		currentElement = currentElement.add(reference, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
public abstract ImportReference createAssistImportReference(char[][] tokens, long[] positions);
public abstract ImportReference createAssistPackageReference(char[][] tokens, long[] positions);
public abstract NameReference createQualifiedAssistNameReference(char[][] previousIdentifiers, char[] name, long[] positions);
public abstract TypeReference createQualifiedAssistTypeReference(char[][] previousIdentifiers, char[] name, long[] positions);
public abstract NameReference createSingleAssistNameReference(char[] name, long position);
public abstract TypeReference createSingleAssistTypeReference(char[] name, long position);
/*
 * Flush parser/scanner state regarding to code assist
 */
public void flushAssistState(){
	this.assistNode = null;
	this.isOrphanCompletionNode = false;
	this.setAssistIdentifier(null);
}
/*
 * Build specific type reference nodes in case the cursor is located inside the type reference
 */
protected TypeReference getTypeReference(int dim) {

	int index;

	/* no need to take action if not inside completed identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		return super.getTypeReference(dim);
	}

	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index);
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist on type reference */
	TypeReference reference;
	if (index == 0) {
		/* assist inside first identifier */
		reference = this.createSingleAssistTypeReference(
						assistIdentifier(), 
						positions[0]);
	} else {
		/* assist inside subsequent identifier */
		reference =	this.createQualifiedAssistTypeReference(
						subset,  
						assistIdentifier(), 
						positions);
	}
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;
	return reference;
}
/*
 * Copy of code from superclass with the following change:
 * In the case of qualified name reference if the cursor location is on the 
 * qualified name reference, then create a CompletionOnQualifiedNameReference 
 * instead.
 */
protected NameReference getUnspecifiedReferenceOptimized() {

	int completionIndex;

	/* no need to take action if not inside completed identifiers */
	if ((completionIndex = indexOfAssistIdentifier()) < 0) {
		return super.getUnspecifiedReferenceOptimized();
	}

	/* retrieve identifiers subset and whole positions, the completion node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(completionIndex);
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length);

	/* build specific completion on name reference */
	NameReference reference;
	if (completionIndex == 0) {
		/* completion inside first identifier */
		reference = this.createSingleAssistNameReference(assistIdentifier(), positions[0]);
	} else {
		/* completion inside subsequent identifier */
		reference = this.createQualifiedAssistNameReference(subset, assistIdentifier(), positions);
	};
	reference.bits &= ~AstNode.RestrictiveFlagMASK;
	reference.bits |= LOCAL | FIELD;
	
	assistNode = reference;
	lastCheckPoint = reference.sourceEnd + 1;
	return reference;
}
public void goForBlockStatementsopt() {
	//tells the scanner to go for block statements opt parsing

	firstToken = TokenNameTWIDDLE;
	scanner.recordLineSeparator = false;
}
public void goForConstructorBlockStatementsopt() {
	//tells the scanner to go for constructor block statements opt parsing

	firstToken = TokenNameNOT;
	scanner.recordLineSeparator = false;
}
/*
 * Retrieve a partial subset of a qualified name reference up to the completion point.
 * It does not pop the actual awaiting identifiers, so as to be able to retrieve position
 * information afterwards.
 */
protected char[][] identifierSubSet(int subsetLength){

	if (subsetLength == 0) return null;
	
	char[][] subset;
	System.arraycopy(
		identifierStack,
		identifierPtr - identifierLengthStack[identifierLengthPtr] + 1,
		(subset = new char[subsetLength][]),
		0,
		subsetLength);
	return subset;
}
/*
 * Iterate the most recent group of awaiting identifiers (grouped for qualified name reference (eg. aa.bb.cc)
 * so as to check whether one of them is the assist identifier.
 * If so, then answer the index of the assist identifier (0 being the first identifier of the set).
 *	eg. aa(0).bb(1).cc(2)
 * If no assist identifier was found, answers -1.
 */
protected int indexOfAssistIdentifier(){

	if (identifierLengthPtr < 0){
		return -1; // no awaiting identifier
	}

	char[] assistIdentifier ;
	if ((assistIdentifier = this.assistIdentifier()) == null){
		return -1; // no assist identifier found yet
	}

	// iterate awaiting identifiers backwards
	int length = identifierLengthStack[identifierLengthPtr];
	for (int i = 0; i < length; i++){ 
		if (identifierStack[identifierPtr - i] == assistIdentifier){
			return length - i - 1;
		}
	}
	// none of the awaiting identifiers is the completion one
	return -1;
}
public void initialize() {
	super.initialize();
	this.flushAssistState();
	this.invocationPtr = -1;
	this.inMethodStack[this.inMethodPtr = 0] = false;
	this.inFieldInitializationStack[this.inFieldInitializationPtr = 0] = false;
	this.previousIdentifierPtr = -1;
}
public abstract void initializeScanner();

protected void initInMethodAndInFieldInitializationStack(RecoveredElement currentElement) {

	int length = currentElement.depth() + 1;
	int ptr = length;
	boolean[] methodStack = new boolean[length];
	boolean[] fieldInitializationStack = new boolean[length];
	boolean inMethod = false;
	boolean inFieldInitializer = false;
	
	RecoveredElement element = currentElement;
	while(element != null){
		if(element instanceof RecoveredMethod ||
			element instanceof RecoveredInitializer) {
			if(element.parent == null) {
				methodStack[--ptr] = true;
				fieldInitializationStack[ptr] = false;
			}
			inMethod = true;
		} else if(element instanceof RecoveredField){
			inFieldInitializer = element.sourceEnd() == 0;
		} else if(element instanceof RecoveredType){
			methodStack[--ptr] = inMethod;
			fieldInitializationStack[ptr] = inFieldInitializer;
	
			inMethod = false;
			inFieldInitializer = false;
		} else if(element instanceof RecoveredUnit) {
			methodStack[--ptr] = false;
			fieldInitializationStack[ptr] = false;
		}
		element = element.parent;
	}
	
	inMethodPtr = length - ptr - 1;
	inFieldInitializationPtr = inMethodPtr;
	System.arraycopy(methodStack, ptr, inMethodStack, 0, inMethodPtr + 1);
	System.arraycopy(fieldInitializationStack, ptr, inFieldInitializationStack, 0, inFieldInitializationPtr + 1);
	
}

/**
 * Returns whether we are directly or indirectly inside a field initializer.
 */
protected boolean insideFieldInitialization() {
	for (int i = this.inFieldInitializationPtr; i >= 0; i--) {
		if (this.inFieldInitializationStack[i]) {
			return true;
		}
	}
	return false;
}
/**
 * Parse the block statements inside the given method declaration and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(AbstractMethodDeclaration md, CompilationUnitDeclaration unit) {
	if (md instanceof MethodDeclaration) {
		parseBlockStatements((MethodDeclaration) md, unit);
	} else if (md instanceof ConstructorDeclaration) {
		parseBlockStatements((ConstructorDeclaration) md, unit);
	}
}
/**
 * Parse the block statements inside the given constructor declaration and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(ConstructorDeclaration cd, CompilationUnitDeclaration unit) {
	//only parse the method body of cd
	//fill out its statements

	//convert bugs into parse error

	initialize();
	
	// simulate goForConstructorBody except that we don't want to balance brackets because they are not going to be balanced
	goForConstructorBlockStatementsopt();

	referenceContext = cd;
	compilationUnit = unit;

	scanner.resetTo(cd.bodyStart, bodyEnd(cd));
	consumeNestedMethod();
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	}
}
/**
 * Parse the block statements inside the given initializer and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(
	Initializer ini,
	TypeDeclaration type, 
	CompilationUnitDeclaration unit) {

	initialize();

	// simulate goForInitializer except that we don't want to balance brackets because they are not going to be balanced
	goForBlockStatementsopt();
	
	referenceContext = type;
	compilationUnit = unit;

	scanner.resetTo(ini.sourceStart, bodyEnd(ini)); // just after the beginning {
	consumeNestedMethod();
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;
	}
}
/**
 * Parse the block statements inside the given method declaration and try to complete at the
 * cursor location.
 */
public void parseBlockStatements(MethodDeclaration md, CompilationUnitDeclaration unit) {
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

	// simulate goForMethodBody except that we don't want to balance brackets because they are not going to be balanced
	goForBlockStatementsopt();

	referenceContext = md;
	compilationUnit = unit;
	
	scanner.resetTo(md.bodyStart, bodyEnd(md)); // reset the scanner to parser from { down to the cursor location
	consumeNestedMethod();
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;		
	}
}
/*
 * Prepares the state of the parser to go for BlockStatements.
 */
protected void prepareForBlockStatements() {
	this.nestedMethod[this.nestedType = 0] = 1;
	this.variablesCounter[this.nestedType] = 0;
	this.realBlockStack[this.realBlockPtr = 1] = 0;
	this.invocationPtr = -1;
}
/*
 * Pushes 'false' on the inInitializerStack.
 */
protected void pushNotInInitializer() {
	try {
		this.inFieldInitializationStack[++this.inFieldInitializationPtr] = false;
	} catch (IndexOutOfBoundsException e) {
		//except in test's cases, it should never raise
		int oldStackLength = this.inFieldInitializationStack.length;
		System.arraycopy(this.inFieldInitializationStack , 0, (this.inFieldInitializationStack = new boolean[oldStackLength + StackIncrement]), 0, oldStackLength);
		this.inFieldInitializationStack[this.inFieldInitializationPtr] = false;
	}
}
/*
 * Pushes 'false' on the inMethodStack.
 */
protected void pushNotInMethod() {
	try {
		this.inMethodStack[++this.inMethodPtr] = false;
	} catch (IndexOutOfBoundsException e) {
		//except in test's cases, it should never raise
		int oldStackLength = this.inMethodStack.length;
		System.arraycopy(this.inMethodStack , 0, (this.inMethodStack = new boolean[oldStackLength + StackIncrement]), 0, oldStackLength);
		this.inMethodStack[this.inMethodPtr] = false;
	}
}
/**
 * Pushes the given the given selector (an identifier pointer to the identifier stack) on the selector stack.
 */
protected void pushOnSelectorStack(int selectorIdPtr) {
	if (this.invocationPtr < -1) return;
	try {
		this.selectorStack[++this.invocationPtr] = selectorIdPtr;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = this.selectorStack.length;
		int oldSelectorStack[] = this.selectorStack;
		this.selectorStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldSelectorStack, 0, this.selectorStack, 0, oldStackLength);
		this.selectorStack[this.invocationPtr] = selectorIdPtr;
	}
}
public void reset(){
	this.flushAssistState();
}
/*
 * Reset context so as to resume to regular parse loop
 */
protected void resetStacks() {
	super.resetStacks();
	this.inFieldInitializationStack[this.inFieldInitializationPtr = 0] = false;
	this.inMethodStack[this.inMethodPtr = 0] = false;
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
	astPtr = -1;
	astLengthPtr = -1;
	expressionPtr = -1;
	expressionLengthPtr = -1;
	identifierPtr = -1;	
	identifierLengthPtr	= -1;
	intPtr = -1;
	dimensions = 0 ;
	recoveredStaticInitializerStart = 0;

	// if in diet mode, reset the diet counter because we're going to restart outside an initializer.
	if (diet) dietInt = 0;

	/* attempt to move checkpoint location */
	if (!this.moveRecoveryCheckpoint()) return false;

	initInMethodAndInFieldInitializationStack(currentElement);

	// only look for headers
	if (referenceContext instanceof CompilationUnitDeclaration
		|| this.assistNode != null){
		
		if(inMethodStack[inMethodPtr] &&
			insideFieldInitialization() &&
			this.assistNode == null
			){ 
			this.prepareForBlockStatements();
			goForBlockStatementsOrMethodHeaders();
		} else {
			nestedMethod[nestedType = 0] = 0;
			variablesCounter[nestedType] = 0;
			realBlockStack[realBlockPtr = 0] = 0;
			goForHeaders();
			diet = true; // passed this point, will not consider method bodies
		}
		return true;
	}
	if (referenceContext instanceof AbstractMethodDeclaration
		|| referenceContext instanceof TypeDeclaration){
			
		if (currentElement instanceof RecoveredType){
			nestedMethod[nestedType = 0] = 0;
			variablesCounter[nestedType] = 0;
			realBlockStack[realBlockPtr = 0] = 0;
			goForHeaders();
		} else {
			this.prepareForBlockStatements();
			goForBlockStatementsOrMethodHeaders();
		}
		return true;
	}
	// does not know how to restart
	return false;
}
public abstract void setAssistIdentifier(char[] assistIdent);
/**
 * If the given ast node is inside an explicit constructor call
 * then wrap it with a fake constructor call.
 * Returns the wrapped completion node or the completion node itself.
 */
protected AstNode wrapWithExplicitConstructorCallIfNeeded(AstNode ast) {
	int selector;
	if (ast != null && this.invocationPtr >= 0 && ast instanceof Expression &&
			(((selector = this.selectorStack[this.invocationPtr]) == THIS_CONSTRUCTOR) ||
			(selector == SUPER_CONSTRUCTOR))) {
		ExplicitConstructorCall call = new ExplicitConstructorCall(
			(selector == THIS_CONSTRUCTOR) ? 
				ExplicitConstructorCall.This : 
				ExplicitConstructorCall.Super
		);
		call.arguments = new Expression[] {(Expression)ast};
		call.sourceStart = ast.sourceStart;
		call.sourceEnd = ast.sourceEnd;
		return call;
	} else {
		return ast;
	}
}
}
