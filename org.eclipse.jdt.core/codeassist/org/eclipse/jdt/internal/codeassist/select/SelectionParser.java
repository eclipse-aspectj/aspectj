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
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Parser able to build specific completion parse nodes, given a cursorLocation.
 *
 * Cursor location denotes the position of the last character behind which completion
 * got requested:
 *  -1 means completion at the very beginning of the source
 *	0  means completion behind the first character
 *  n  means completion behind the n-th character
 */
 
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.codeassist.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class SelectionParser extends AssistParser {

	/* public fields */

	public int selectionStart, selectionEnd;

	public static final char[] SUPER = "super".toCharArray(); //$NON-NLS-1$
	public static final char[] THIS = "this".toCharArray(); //$NON-NLS-1$
	
public SelectionParser(ProblemReporter problemReporter, boolean assertMode) {
	super(problemReporter, assertMode);
}
public char[] assistIdentifier(){
	return ((SelectionScanner)scanner).selectionIdentifier;
}
protected void attachOrphanCompletionNode(){
	if (isOrphanCompletionNode){
		AstNode orphan = this.assistNode;
		isOrphanCompletionNode = false;
		
		
		/* if in context of a type, then persists the identifier into a fake field return type */
		if (currentElement instanceof RecoveredType){
			RecoveredType recoveredType = (RecoveredType)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (recoveredType.foundOpeningBrace) {
				/* generate a pseudo field with a completion on type reference */	
				if (orphan instanceof TypeReference){
					currentElement = currentElement.add(new SelectionOnFieldType((TypeReference)orphan), 0);
					return;
				}
			}
		}
		
		Statement statement = (Statement)wrapWithExplicitConstructorCallIfNeeded(orphan);
		currentElement = currentElement.add(statement, 0);
		currentToken = 0; // given we are not on an eof, we do not want side effects caused by looked-ahead token
	}
}

private boolean checkRecoveredType() {
	if (currentElement instanceof RecoveredType){
		/* check if current awaiting identifier is the completion identifier */
		if (this.indexOfAssistIdentifier() < 0) return false;

		if ((lastErrorEndPosition >= selectionStart)
			&& (lastErrorEndPosition <= selectionEnd+1)){
			return false;
		}
		RecoveredType recoveredType = (RecoveredType)currentElement;
		/* filter out cases where scanner is still inside type header */
		if (recoveredType.foundOpeningBrace) {
			this.assistNode = this.getTypeReference(0);
			this.lastCheckPoint = this.assistNode.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		}
	}
	return false;
}
protected void classInstanceCreation(boolean alwaysQualified) {
	
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

	// ClassBodyopt produces a null item on the astStak if it produces NO class body
	// An empty class body produces a 0 on the length stack.....

	int length;
	if (((length = astLengthStack[astLengthPtr]) == 1)
		&& (astStack[astPtr] == null)) {

		if (this.indexOfAssistIdentifier() < 0) {
			super.classInstanceCreation(alwaysQualified);
			return;
		}
		QualifiedAllocationExpression alloc;
		astPtr--;
		astLengthPtr--;
		alloc = new SelectionOnQualifiedAllocationExpression();
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
		// trick to avoid creating a selection on type reference
		char [] oldIdent = this.assistIdentifier();
		this.setAssistIdentifier(null);			
		alloc.type = getTypeReference(0);
		this.setAssistIdentifier(oldIdent);
		
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = intStack[intPtr--];
		pushOnExpressionStack(alloc);

		this.assistNode = alloc;
		this.lastCheckPoint = alloc.sourceEnd + 1;
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		this.isOrphanCompletionNode = true;
	} else {
		super.classInstanceCreation(alwaysQualified);
	}
}

protected void consumeArrayCreationExpression() {
	// ArrayCreationExpression ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializeropt
	// ArrayCreationExpression ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializeropt

	super.consumeArrayCreationExpression();

	ArrayAllocationExpression alloc = (ArrayAllocationExpression)expressionStack[expressionPtr];
	if (alloc.type == assistNode){
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		this.isOrphanCompletionNode = true;
	}
}
protected void consumeEnterAnonymousClassBody() {
	// EnterAnonymousClassBody ::= $empty

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeEnterAnonymousClassBody();
		return;
	}
	QualifiedAllocationExpression alloc;
	AnonymousLocalTypeDeclaration anonymousType = 
		new AnonymousLocalTypeDeclaration(this.compilationUnit.compilationResult); 
	alloc = 
		anonymousType.allocation = new SelectionOnQualifiedAllocationExpression(anonymousType); 
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
	// trick to avoid creating a selection on type reference
	char [] oldIdent = this.assistIdentifier();
	this.setAssistIdentifier(null);			
	alloc.type = getTypeReference(0);
	this.setAssistIdentifier(oldIdent);		

	anonymousType.sourceEnd = alloc.sourceEnd;
	//position at the type while it impacts the anonymous declaration
	anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
	alloc.sourceStart = intStack[intPtr--];
	pushOnExpressionStack(alloc);

	assistNode = alloc;
	this.lastCheckPoint = alloc.sourceEnd + 1;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;	
	}
	this.isOrphanCompletionNode = true;
		
	anonymousType.bodyStart = scanner.currentPosition;	
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = anonymousType.bodyStart;
		currentElement = currentElement.add(anonymousType, 0); // the recoveryTokenCheck will deal with the open brace
		lastIgnoredToken = -1;		
	}
}
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	// do nothing by default

	super.consumeEnterVariable();

	AbstractVariableDeclaration variable = (AbstractVariableDeclaration) astStack[astPtr];
	if (variable.type == assistNode){
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		isOrphanCompletionNode = false; // already attached inside variable decl
	}
}

protected void consumeExitVariableWithInitialization() {
	super.consumeExitVariableWithInitialization();
	
	// does not keep the initialization if selection is not inside
	AbstractVariableDeclaration variable = (AbstractVariableDeclaration) astStack[astPtr];
	int start = variable.initialization.sourceStart;
	int end =  variable.initialization.sourceEnd;
	if ((selectionStart < start) &&  (selectionEnd < start) ||
		(selectionStart > end) && (selectionEnd > end)) {
		variable.initialization = null;
	}
}

protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFieldAccess(isSuperAccess);
		return;
	} 
	FieldReference fieldReference = 
		new SelectionOnFieldReference(
			identifierStack[identifierPtr], 
			identifierPositionStack[identifierPtr--]);
	identifierLengthPtr--;
	if (isSuperAccess) { //considerates the fieldReferenceerence beginning at the 'super' ....	
		fieldReference.sourceStart = intStack[intPtr--];
		fieldReference.receiver = new SuperReference(fieldReference.sourceStart, endPosition);
		pushOnExpressionStack(fieldReference);
	} else { //optimize push/pop
		if ((fieldReference.receiver = expressionStack[expressionPtr]).isThis()) { //fieldReferenceerence begins at the this
			fieldReference.sourceStart = fieldReference.receiver.sourceStart;
		}
		expressionStack[expressionPtr] = fieldReference;
	}
	assistNode = fieldReference;
	this.lastCheckPoint = fieldReference.sourceEnd + 1;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;
	}
	this.isOrphanCompletionNode = true;	
}
protected void consumeFormalParameter() {
	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFormalParameter();
	} else {

		identifierLengthPtr--;
		char[] name = identifierStack[identifierPtr];
		long namePositions = identifierPositionStack[identifierPtr--];
		TypeReference type = getTypeReference(intStack[intPtr--] + intStack[intPtr--]);
		intPtr -= 2;
		Argument arg = 
			new SelectionOnArgumentName(
				name, 
				namePositions, 
				type, 
				intStack[intPtr + 1] & ~AccDeprecated); // modifiers
		pushOnAstStack(arg);
		
		assistNode = arg;
		this.lastCheckPoint = (int) namePositions;
		isOrphanCompletionNode = true;
		
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}

		/* if incomplete method header, listLength counter will not have been reset,
			indicating that some arguments are available on the stack */
		listLength++;
	} 	
}
protected void consumeInstanceOfExpression(int op) {
	if (indexOfAssistIdentifier() < 0) {
		super.consumeInstanceOfExpression(op);
	} else {
		getTypeReference(intStack[intPtr--]);
		this.isOrphanCompletionNode = true;
		this.restartRecovery = true;
		this.lastIgnoredToken = -1;
	}
}
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)

	char[] selector = identifierStack[identifierPtr];
	int accessMode;
	if(selector == this.assistIdentifier()) {
		if(CharOperation.equals(selector, SUPER)) {
			accessMode = ExplicitConstructorCall.Super;
		} else if(CharOperation.equals(selector, THIS)) {
			accessMode = ExplicitConstructorCall.This;
		} else {
			super.consumeMethodInvocationName();
			return;
		}
	} else {
		super.consumeMethodInvocationName();
		return;
	}
	
	final ExplicitConstructorCall constructorCall = new SelectionOnExplicitConstructorCall(accessMode);
	constructorCall.sourceEnd = rParenPos;
	constructorCall.sourceStart = (int) (identifierPositionStack[identifierPtr] >>> 32);
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, constructorCall.arguments = new Expression[length], 0, length);
	}

	if (!diet){
		pushOnAstStack(constructorCall);
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;
	} else {
		pushOnExpressionStack(new Expression(){
			public TypeBinding resolveType(BlockScope scope) {
				constructorCall.resolve(scope);
				return null;
			}
		});
	}
	this.assistNode = constructorCall;	
	this.lastCheckPoint = constructorCall.sourceEnd + 1;
	this.isOrphanCompletionNode = true;
}
protected void consumeMethodInvocationPrimary() {
	//optimize the push/pop
	//MethodInvocation ::= Primary '.' 'Identifier' '(' ArgumentListopt ')'

	char[] selector = identifierStack[identifierPtr];
	int accessMode;
	if(selector == this.assistIdentifier()) {
		if(CharOperation.equals(selector, SUPER)) {
			accessMode = ExplicitConstructorCall.Super;
		} else if(CharOperation.equals(selector, THIS)) {
			accessMode = ExplicitConstructorCall.This;
		} else {
			super.consumeMethodInvocationPrimary();
			return;
		}
	} else {
		super.consumeMethodInvocationPrimary();
		return;
	}
	
	final ExplicitConstructorCall constructorCall = new SelectionOnExplicitConstructorCall(accessMode);
	constructorCall.sourceEnd = rParenPos;
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, constructorCall.arguments = new Expression[length], 0, length);
	}
	constructorCall.qualification = expressionStack[expressionPtr--];
	constructorCall.sourceStart = constructorCall.qualification.sourceStart;
	
	if (!diet){
		pushOnAstStack(constructorCall);
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;
	} else {
		pushOnExpressionStack(new Expression(){
			public TypeBinding resolveType(BlockScope scope) {
				constructorCall.resolve(scope);
				return null;
			}
		});
	}
	
	this.assistNode = constructorCall;
	this.lastCheckPoint = constructorCall.sourceEnd + 1;
	this.isOrphanCompletionNode = true;
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
public ImportReference createAssistImportReference(char[][] tokens, long[] positions){
	return new SelectionOnImportReference(tokens, positions);
}
public ImportReference createAssistPackageReference(char[][] tokens, long[] positions){
	return new SelectionOnPackageReference(tokens, positions);
}
protected LocalDeclaration createLocalDeclaration(Expression initialization,char[] name,int sourceStart,int sourceEnd) {
	if (this.indexOfAssistIdentifier() < 0) {
		return super.createLocalDeclaration(initialization, name, sourceStart, sourceEnd);
	} else {
		SelectionOnLocalName local = new SelectionOnLocalName(initialization, name, sourceStart, sourceEnd);
		this.assistNode = local;
		this.lastCheckPoint = sourceEnd + 1;
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;	
		}
		return local;
	}
}
public NameReference createQualifiedAssistNameReference(char[][] previousIdentifiers, char[] name, long[] positions){
	return new SelectionOnQualifiedNameReference(
					previousIdentifiers, 
					name, 
					positions); 	
}
public TypeReference createQualifiedAssistTypeReference(char[][] previousIdentifiers, char[] name, long[] positions){
	return new SelectionOnQualifiedTypeReference(
					previousIdentifiers, 
					name, 
					positions); 	
}
public NameReference createSingleAssistNameReference(char[] name, long position) {
	return new SelectionOnSingleNameReference(name, position);
}
public TypeReference createSingleAssistTypeReference(char[] name, long position) {
	return new SelectionOnSingleTypeReference(name, position);
}
public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int selectionStart, int selectionEnd) {

	this.selectionStart = selectionStart;
	this.selectionEnd = selectionEnd;	
	SelectionScanner selectionScanner = (SelectionScanner)this.scanner;
	selectionScanner.selectionIdentifier = null;
	selectionScanner.selectionStart = selectionStart;
	selectionScanner.selectionEnd = selectionEnd;	
	return this.dietParse(sourceUnit, compilationResult);
}
protected NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int completionIndex;

	/* no need to take action if not inside completed identifiers */
	if ((completionIndex = indexOfAssistIdentifier()) < 0) {
		return super.getUnspecifiedReference();
	}

	int length = identifierLengthStack[identifierLengthPtr];
	if (CharOperation.equals(assistIdentifier(), SUPER)){
		Reference reference;
		if (completionIndex > 0){ // qualified super
			// discard 'super' from identifier stacks
			identifierLengthStack[identifierLengthPtr] = completionIndex;
			int ptr = identifierPtr -= (length - completionIndex);
			reference = 
				new SelectionOnQualifiedSuperReference(
					getTypeReference(0), 
					(int)(identifierPositionStack[ptr+1] >>> 32),
					(int) identifierPositionStack[ptr+1]);
		} else { // standard super
			identifierPtr -= length;
			identifierLengthPtr--;
			reference = new SelectionOnSuperReference((int)(identifierPositionStack[identifierPtr+1] >>> 32), (int) identifierPositionStack[identifierPtr+1]);
		}
		pushOnAstStack(reference);
		this.assistNode = reference;	
		this.lastCheckPoint = reference.sourceEnd + 1;
		if (!diet || dietInt != 0){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;		
		}
		this.isOrphanCompletionNode = true;
		return new SingleNameReference(new char[0], 0); // dummy reference
	}
	NameReference nameReference;
	/* retrieve identifiers subset and whole positions, the completion node positions
		should include the entire replaced source. */
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
	if (completionIndex == 0) {
		/* completion inside first identifier */
		nameReference = this.createSingleAssistNameReference(assistIdentifier(), positions[0]);
	} else {
		/* completion inside subsequent identifier */
		nameReference = this.createQualifiedAssistNameReference(subset, assistIdentifier(), positions);
	}
	assistNode = nameReference;
	this.lastCheckPoint = nameReference.sourceEnd + 1;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;	
	}
	this.isOrphanCompletionNode = true;
	return nameReference;
}
/*
 * Copy of code from superclass with the following change:
 * In the case of qualified name reference if the cursor location is on the 
 * qualified name reference, then create a CompletionOnQualifiedNameReference 
 * instead.
 */
protected NameReference getUnspecifiedReferenceOptimized() {

	int index = indexOfAssistIdentifier();
	NameReference reference = super.getUnspecifiedReferenceOptimized();

	if (index >= 0){
		if (!diet){
			this.restartRecovery	= true;	// force to restart in recovery mode
			this.lastIgnoredToken = -1;		
		}
		this.isOrphanCompletionNode = true;
	}
	return reference;
}
public void initializeScanner(){
	this.scanner = new SelectionScanner(this.assertMode);
}
protected MessageSend newMessageSend() {
	// '(' ArgumentListopt ')'
	// the arguments are on the expression stack

	char[] selector = identifierStack[identifierPtr];
	if (selector != this.assistIdentifier()){
		return super.newMessageSend();
	}	
	MessageSend messageSend = new SelectionOnMessageSend();
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			messageSend.arguments = new Expression[length], 
			0, 
			length); 
	};
	assistNode = messageSend;
	if (!diet){
		this.restartRecovery	= true;	// force to restart in recovery mode
		this.lastIgnoredToken = -1;	
	}
	
	this.isOrphanCompletionNode = true;
	return messageSend;
}
public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int selectionStart, int selectionEnd) {

	this.selectionStart = selectionStart;
	this.selectionEnd = selectionEnd;	
	SelectionScanner selectionScanner = (SelectionScanner)this.scanner;
	selectionScanner.selectionIdentifier = null;
	selectionScanner.selectionStart = selectionStart;
	selectionScanner.selectionEnd = selectionEnd;	
	return this.parse(sourceUnit, compilationResult);
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {

	/* if reached assist node inside method body, but still inside nested type,
		should continue in diet mode until the end of the method body */
	if (this.assistNode != null
		&& !(referenceContext instanceof CompilationUnitDeclaration)){
		currentElement.preserveEnclosingBlocks();
		if (currentElement.enclosingType() == null){
			this.resetStacks();
			return false;
		}
	}
	return super.resumeAfterRecovery();			
}

public void selectionIdentifierCheck(){
	if (checkRecoveredType()) return;
}
public void setAssistIdentifier(char[] assistIdent){
	((SelectionScanner)scanner).selectionIdentifier = assistIdent;
}
/*
 * Update recovery state based on current parser/scanner state
 */
protected void updateRecoveryState() {

	/* expose parser state to recovery state */
	currentElement.updateFromParserState();

	/* may be able to retrieve completionNode as an orphan, and then attach it */
	this.selectionIdentifierCheck();
	this.attachOrphanCompletionNode();
	
	/* check and update recovered state based on current token,
		this action is also performed when shifting token after recovery
		got activated once. 
	*/
	this.recoveryTokenCheck();
}
}
