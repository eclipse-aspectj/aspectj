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
package org.eclipse.jdt.internal.codeassist.complete;

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

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.codeassist.impl.*;

public class CompletionParser extends AssistParser {

	/* public fields */

	public int cursorLocation;
	public char[][] labels; // the visible labels up to the cursor location
	public AstNode assistNodeParent; // the parent node of assist node
	/* the following fields are internal flags */
	
	boolean betweenNewAndLeftBraket; // whether we are between the keyword 'new' and the following left braket, ie. '[', '(' or '{'
	boolean betweenCatchAndRightParen; // whether we are between the keyword 'catch' and the following ')'
	boolean completionBehindDot; // true when completion identifier immediately follows a dot
	
	boolean nextTypeReferenceIsClass;
	boolean nextTypeReferenceIsException;
	boolean nextTypeReferenceIsInterface;
	
	int bracketDepth;
	int throwBracketDepth;

	// the stacks of types and qualifiers for invocations (ie. method invocations, allocation expressions and
	// explicit constructor invocations). They use the same stack pointer as the selector stack (ie. invocationPtr)
	// the invocation type stack contains one of the invocation type constants below
	// the qualifier stack contains pointers to the expression stack or -1 if there is no qualifier
	// (a qualifier is the expression that qualifies a 'new', a 'super' constructor or a 'this' constructor
	//  or it is the receiver of a message send)
	int[] invocationTypeStack = new int[StackIncrement];
	int[] qualifierStack = new int[StackIncrement];

	// invocation type constants
	static final int EXPLICIT_RECEIVER = 0;
	static final int NO_RECEIVER = -1;
	static final int SUPER_RECEIVER = -2;
	static final int NAME_RECEIVER = -3;
	static final int ALLOCATION = -4;
	static final int QUALIFIED_ALLOCATION = -5;

	// the type of the current invocation (one of the invocation type constants)
	int invocationType;

	// a pointer in the expression stack to the qualifier of a invocation
	int qualifier;

	// a stack of label counters
	// a new counter is pushed on the stack each time when a method (or a constructor) is entered, 
	// it is poped when the method (or constructor) is exited,
	// it is incremented when a new label is defined
	int labelCounterPtr;
	int[] labelCounterStack = new int[StackIncrement];

	// a stack of invocationPtr: contains the first invocationPtr of a block
	// the current invocationPtr+1 is pushed when a block is entered
	// it is poped when a block is exited 
	int blockInvocationPtr;
	int[] blockInvocationStack = new int[StackIncrement];
	
	// last modifiers info
	int lastModifiers = AccDefault;
	int lastModifiersStart = -1;
	
public CompletionParser(ProblemReporter problemReporter, boolean assertMode) {
	super(problemReporter, assertMode);
}
public char[] assistIdentifier(){
	return ((CompletionScanner)scanner).completionIdentifier;
}
protected void attachOrphanCompletionNode(){
	if (this.isOrphanCompletionNode) {
		AstNode orphan = this.assistNode;
		this.isOrphanCompletionNode = false;
		
		/* if in context of a type, then persists the identifier into a fake field return type */
		if (currentElement instanceof RecoveredType){
			RecoveredType recoveredType = (RecoveredType)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (recoveredType.foundOpeningBrace) {
				/* generate a pseudo field with a completion on type reference */	
				if (orphan instanceof TypeReference){
					CompletionOnFieldType fieldDeclaration = new CompletionOnFieldType((TypeReference)orphan, false);

					// retrieve available modifiers if any
					if (intPtr >= 2 && intStack[intPtr-1] == this.lastModifiersStart && intStack[intPtr-2] == this.lastModifiers){
						fieldDeclaration.modifiersSourceStart = intStack[intPtr-1];
						fieldDeclaration.modifiers = intStack[intPtr-2];
					}

					currentElement = currentElement.add(fieldDeclaration, 0);
					return;
				}
			}
		}
		/* if in context of a method, persists if inside arguments as a type */
		if (currentElement instanceof RecoveredMethod){
			RecoveredMethod recoveredMethod = (RecoveredMethod)currentElement;
			/* only consider if inside method header */
			if (!recoveredMethod.foundOpeningBrace) {
				//if (rParenPos < lParenPos){ // inside arguments
				if (orphan instanceof TypeReference){
					currentElement = currentElement.parent.add(
						new CompletionOnFieldType((TypeReference)orphan, true), 0);
					return;
				}
			}
		}

		// add the completion node to the method declaration or constructor declaration
		if (orphan instanceof Statement) {
			/* check for completion at the beginning of method body
				behind an invalid signature
			 */
			RecoveredMethod method = currentElement.enclosingMethod();
			if (method != null){
				AbstractMethodDeclaration methodDecl = method.methodDeclaration;
				if ((methodDecl.bodyStart == methodDecl.sourceEnd+1) // was missing opening brace
					&& (scanner.getLineNumber(orphan.sourceStart) == scanner.getLineNumber(methodDecl.sourceEnd))){
					return;
				}
			}
			// add the completion node as a statement to the list of block statements
			currentElement = currentElement.add((Statement)orphan, 0);
			return;
		} 
	}
	
	// the following code applies only in methods, constructors or initializers
	if ((!this.inMethodStack[this.inMethodPtr] && !this.inFieldInitializationStack[this.inFieldInitializationPtr])) { 
		return;
	}
	
	// push top expression on ast stack if it contains the completion node
	Expression expression;
	if (this.expressionPtr > -1 && containsCompletionNode(expression = this.expressionStack[this.expressionPtr])) {
		/* check for completion at the beginning of method body
			behind an invalid signature
		 */
		RecoveredMethod method = currentElement.enclosingMethod();
		if (method != null){
			AbstractMethodDeclaration methodDecl = method.methodDeclaration;
			if ((methodDecl.bodyStart == methodDecl.sourceEnd+1) // was missing opening brace
				&& (scanner.getLineNumber(expression.sourceStart) == scanner.getLineNumber(methodDecl.sourceEnd))){
				return;
			}
		}
		if (expression instanceof AllocationExpression) {
			// keep the context if it is an allocation expression
			Statement statement = (Statement)wrapWithExplicitConstructorCallIfNeeded(expression);
			currentElement = currentElement.add(statement, 0);
		} else {
			Statement statement = (Statement)wrapWithExplicitConstructorCallIfNeeded(this.assistNode);
			currentElement = currentElement.add(statement, 0);
		}
	}
}
public int bodyEnd(AbstractMethodDeclaration method){
	return cursorLocation;
}
public int bodyEnd(Initializer initializer){
	return cursorLocation;
}
/**
 * Checks if the completion is on the exception type of a catch clause.
 * Returns whether we found a completion node.
 */
private boolean checkCatchClause() {
	if (this.betweenCatchAndRightParen && this.identifierPtr > -1) { 
		// NB: if the cursor is on the variable, then it has been reduced (so identifierPtr is -1), 
		//     thus this can only be a completion on the type of the catch clause
		this.assistNode = getTypeReference(0);
		this.lastCheckPoint = this.assistNode.sourceEnd + 1;
		this.isOrphanCompletionNode = true;
		return true;
	}
	return false;
}
/**
 * Checks if the completion is on the type following a 'new'.
 * Returns whether we found a completion node.
 */
private boolean checkClassInstanceCreation() {
	if (this.betweenNewAndLeftBraket) {
		// completion on type inside an allocation expression
		
		if(this.throwBracketDepth != -1 && this.throwBracketDepth == this.bracketDepth) {
			this.nextTypeReferenceIsException = true;	
		}
		TypeReference type = getTypeReference(0);
		this.nextTypeReferenceIsException = false;
		this.assistNode = type;
		this.lastCheckPoint = type.sourceEnd + 1;
		if (this.invocationType == ALLOCATION) {
			// non qualified allocation expression
			AllocationExpression allocExpr = new AllocationExpression();
			allocExpr.type = type;
			allocExpr.sourceStart = type.sourceStart;
			allocExpr.sourceEnd = type.sourceEnd;
			pushOnExpressionStack(allocExpr);
			this.isOrphanCompletionNode = false;
		} else {
			// qualified allocation expression
			QualifiedAllocationExpression allocExpr = new QualifiedAllocationExpression();
			allocExpr.type = type;
			allocExpr.enclosingInstance = this.expressionStack[this.qualifier];
			allocExpr.sourceStart = this.intStack[this.intPtr--];
			allocExpr.sourceEnd = type.sourceEnd;
			this.expressionStack[this.qualifier] = allocExpr; // attach it now (it replaces the qualifier expression)
			this.isOrphanCompletionNode = false;
		}
		return true;
	}
	return false;
}
/**
 * Checks if the completion is on the dot following an array type,
 * a primitive type or an primitive array type.
 * Returns whether we found a completion node.
 */
private boolean checkClassLiteralAccess() {
	if (this.identifierLengthPtr >= 1 && this.previousToken == TokenNameDOT) { // (NB: the top id length is 1 and it is for the completion identifier)
		int length;
		// if the penultimate id length is negative, 
		// the completion is after a primitive type or a primitive array type
		if ((length = this.identifierLengthStack[this.identifierLengthPtr-1]) < 0) {
			// build the primitive type node
			int dim = this.isAfterArrayType() ? this.intStack[this.intPtr--] : 0;
			SingleTypeReference typeRef = (SingleTypeReference)TypeReference.baseTypeReference(-length, dim);
			typeRef.sourceStart = this.intStack[this.intPtr--];
			if (dim == 0) {
				typeRef.sourceEnd = this.intStack[this.intPtr--];
			} else {
				this.intPtr--;
				typeRef.sourceEnd = this.endPosition;
			}
			//typeRef.sourceEnd = typeRef.sourceStart + typeRef.token.length; // NB: It's ok to use the length of the token since it doesn't contain any unicode

			// find the completion identifier and its source positions
			char[] source = identifierStack[identifierPtr];
			long pos = this.identifierPositionStack[this.identifierPtr--];
			this.identifierLengthPtr--; // it can only be a simple identifier (so its length is one)

			// build the completion on class literal access node
			CompletionOnClassLiteralAccess access = new CompletionOnClassLiteralAccess(pos, typeRef);
			access.completionIdentifier = source;
			this.identifierLengthPtr--; // pop the length that was used to say it is a primitive type
			this.assistNode = access;
			this.isOrphanCompletionNode = true;
			return true;
		}

		// if the completion is after a regular array type
		if (isAfterArrayType()) {
			// find the completion identifier and its source positions
			char[] source = identifierStack[identifierPtr];
			long pos = this.identifierPositionStack[this.identifierPtr--];
			this.identifierLengthPtr--; // it can only be a simple identifier (so its length is one)
			
			// get the type reference
			TypeReference typeRef = getTypeReference(this.intPtr--);
			
			// build the completion on class literal access node
			CompletionOnClassLiteralAccess access = new CompletionOnClassLiteralAccess(pos, typeRef);
			access.completionIdentifier = source;
			this.assistNode = access;
			this.isOrphanCompletionNode = true;
			return true;
		}

	}
	return false;
}
/**
 * Checks if the completion is inside a method invocation or a constructor invocation.
 * Returns whether we found a completion node.
 */
private boolean checkInvocation() {
	Expression topExpression = this.expressionPtr >= 0 ? 
		this.expressionStack[this.expressionPtr] :
		null;
	boolean isEmptyNameCompletion = false;
	boolean isEmptyAssistIdentifier = false;
	int startInvocationPtr = this.blockInvocationPtr >= 0 ? this.blockInvocationStack[this.blockInvocationPtr] : 0;
	if (this.invocationPtr >= startInvocationPtr
		&& ((isEmptyNameCompletion = topExpression == this.assistNode && this.isEmptyNameCompletion()) // eg. it is something like "this.fred([cursor]" but it is not something like "this.fred(1 + [cursor]"
			|| (isEmptyAssistIdentifier = this.indexOfAssistIdentifier() >= 0 && this.identifierStack[this.identifierPtr].length == 0))) { // eg. it is something like "this.fred(1 [cursor]"
				
		// pop empty name completion
		if (isEmptyNameCompletion) {
			this.expressionPtr--;
			this.expressionLengthStack[this.expressionLengthPtr]--;
		} else if (isEmptyAssistIdentifier) {
			this.identifierPtr--;
			this.identifierLengthPtr--;
		}

		// find receiver and qualifier
		int invocationType = this.invocationTypeStack[this.invocationPtr];
		int qualifierExprPtr = this.qualifierStack[this.invocationPtr];

		// find arguments
		int numArgs = this.expressionPtr - qualifierExprPtr;
		int argStart = qualifierExprPtr + 1;
		Expression[] arguments = null;
		if (numArgs > 0) {
			// remember the arguments
			arguments = new Expression[numArgs];
			System.arraycopy(this.expressionStack, argStart, arguments, 0, numArgs);

			// consume the expression arguments
			this.expressionPtr -= numArgs;
			int count = numArgs;
			while (count > 0) {
				count -= this.expressionLengthStack[this.expressionLengthPtr--];
			} 
		}

		// build ast node
		if (invocationType != ALLOCATION && invocationType != QUALIFIED_ALLOCATION) {
			// creates completion on message send	
			CompletionOnMessageSend messageSend = new CompletionOnMessageSend();
			messageSend.arguments = arguments;
			switch (invocationType) {
				case NO_RECEIVER:
					// implicit this
					messageSend.receiver = ThisReference.ThisImplicit;
					break;
				case NAME_RECEIVER:
					// remove special flags for primitive types
					while (this.identifierLengthPtr >= 0 && this.identifierLengthStack[this.identifierLengthPtr] < 0) {
						this.identifierLengthPtr--;
					}
				
					// remove selector 
					this.identifierPtr--; 
					this.identifierLengthStack[this.identifierLengthPtr]--;
					// consume the receiver
					messageSend.receiver = this.getUnspecifiedReference();
					break;
				case SUPER_RECEIVER:
					messageSend.receiver = SuperReference.Super;
					break;
				case EXPLICIT_RECEIVER:
					messageSend.receiver = this.expressionStack[qualifierExprPtr];
			}

			// set selector
			int selectorPtr = this.selectorStack[this.invocationPtr];
			messageSend.selector = this.identifierStack[selectorPtr];
			// remove selector
			if (this.identifierLengthPtr >=0 && this.identifierLengthStack[this.identifierLengthPtr] == 1) {
				this.identifierPtr--; 
				this.identifierLengthPtr--;
			}
		
			// the entire message may be replaced in case qualification is needed
			messageSend.sourceStart = (int)(this.identifierPositionStack[selectorPtr] >> 32); //this.cursorLocation + 1;
			messageSend.sourceEnd = this.cursorLocation;

			// remember the message send as an orphan completion node
			this.assistNode = messageSend;
			this.lastCheckPoint = messageSend.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		} else {
			int selectorPtr = this.selectorStack[this.invocationPtr];
			if (selectorPtr == THIS_CONSTRUCTOR || selectorPtr == SUPER_CONSTRUCTOR) {
				// creates an explicit constructor call
				CompletionOnExplicitConstructorCall call = new CompletionOnExplicitConstructorCall(
					(selectorPtr == THIS_CONSTRUCTOR) ? ExplicitConstructorCall.This : ExplicitConstructorCall.Super);
				call.arguments = arguments;
				if (invocationType == QUALIFIED_ALLOCATION) {
					call.qualification = this.expressionStack[qualifierExprPtr];
				}
		
				// no source is going to be replaced
				call.sourceStart = this.cursorLocation + 1;
				call.sourceEnd = this.cursorLocation;

				// remember the explicit constructor call as an orphan completion node
				this.assistNode = call;
				this.lastCheckPoint = call.sourceEnd + 1;
				this.isOrphanCompletionNode = true;
				return true;
			} else {
				// creates an allocation expression 
				CompletionOnQualifiedAllocationExpression allocExpr = new CompletionOnQualifiedAllocationExpression();
				allocExpr.arguments = arguments;
				allocExpr.type = super.getTypeReference(0); // we don't want a completion node here, so call super
				if (invocationType == QUALIFIED_ALLOCATION) {
					allocExpr.enclosingInstance = this.expressionStack[qualifierExprPtr];
				}
				// no source is going to be replaced
				allocExpr.sourceStart = this.cursorLocation + 1;
				allocExpr.sourceEnd = this.cursorLocation;
				
				// remember the allocation expression as an orphan completion node
				this.assistNode = allocExpr;
				this.lastCheckPoint = allocExpr.sourceEnd + 1;
				this.isOrphanCompletionNode = true;
				return true;
			}
		}
	}
	return false;
}
/**
 * Checks if the completion is on a member access (ie. in an identifier following a dot).
 * Returns whether we found a completion node.
 */
private boolean checkMemberAccess() {
	if (this.previousToken == TokenNameDOT && this.qualifier > -1 && this.expressionPtr == this.qualifier) {
		// the receiver is an expression
		pushCompletionOnMemberAccessOnExpressionStack(false);
		return true;
	}
	return false;
}
/**
 * Checks if the completion is on a name reference.
 * Returns whether we found a completion node.
 */
private boolean checkNameCompletion() {
	/* 
		We didn't find any other completion, but the completion identifier is on the identifier stack,
		so it can only be a completion on name.
		Note that we allow the completion on a name even if nothing is expected (eg. foo() b[cursor] would
		be a completion on 'b'). This policy gives more to the user than he/she would expect, but this 
		simplifies the problem. To fix this, the recovery must be changed to work at a 'statement' granularity
		instead of at the 'expression' granularity as it does right now.
	*/ 
	
	// NB: at this point the completion identifier is on the identifier stack
	this.assistNode = getUnspecifiedReferenceOptimized();
	this.lastCheckPoint = this.assistNode.sourceEnd + 1;
	this.isOrphanCompletionNode = true;
	return true;
}
/**
 * Checks if the completion is in the context of a method and on the type of one of its arguments
 * Returns whether we found a completion node.
 */
private boolean checkRecoveredMethod() {
	if (currentElement instanceof RecoveredMethod){
		/* check if current awaiting identifier is the completion identifier */
		if (this.indexOfAssistIdentifier() < 0) return false;

		/* check if on line with an error already - to avoid completing inside 
			illegal type names e.g.  int[<cursor> */
		if (lastErrorEndPosition <= cursorLocation+1
			&& scanner.getLineNumber(lastErrorEndPosition) 
				== scanner.getLineNumber(((CompletionScanner)scanner).completedIdentifierStart)){
			return false;
		}		
 		RecoveredMethod recoveredMethod = (RecoveredMethod)currentElement;
		/* only consider if inside method header */
		if (!recoveredMethod.foundOpeningBrace
			&& lastIgnoredToken == -1) {
			//if (rParenPos < lParenPos){ // inside arguments
			this.assistNode = this.getTypeReference(0);
			this.lastCheckPoint = this.assistNode.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		}
	}
	return false;
}
/**
 * Checks if the completion is in the context of a type and on a type reference in this type.
 * Persists the identifier into a fake field return type
 * Returns whether we found a completion node.
 */
private boolean checkRecoveredType() {
	if (currentElement instanceof RecoveredType){
		/* check if current awaiting identifier is the completion identifier */
		if (this.indexOfAssistIdentifier() < 0) return false;

		/* check if on line with an error already - to avoid completing inside 
			illegal type names e.g.  int[<cursor> */
		if ((lastErrorEndPosition <= cursorLocation+1)
			&& scanner.getLineNumber(lastErrorEndPosition) 
				== scanner.getLineNumber(((CompletionScanner)scanner).completedIdentifierStart)){
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
/* 
 * Check whether about to shift beyond the completion token.
 * If so, depending on the context, a special node might need to be created
 * and attached to the existing recovered structure so as to be remember in the
 * resulting parsed structure.
 */
public void completionIdentifierCheck(){

	if (checkRecoveredType()) return;
	if (checkRecoveredMethod()) return;

	// if not in a method in non diet mode and if not inside a field initializer, only record references attached to types
	if (!(this.inMethodStack[this.inMethodPtr] && !this.diet)
		&& !insideFieldInitialization()) return; 

	/*
	 	In some cases, the completion identifier may not have yet been consumed,
	 	e.g.  int.[cursor]
	 	This is because the grammar does not allow any (empty) identifier to follow
	 	a base type. We thus have to manually force the identifier to be consumed
	 	(i.e. pushed).
	 */
	if (assistIdentifier() == null && this.currentToken == TokenNameIdentifier) { // Test below copied from CompletionScanner.getCurrentIdentifierSource()
		if (cursorLocation < this.scanner.startPosition && this.scanner.currentPosition == this.scanner.startPosition){ // fake empty identifier got issued
			this.pushIdentifier();					
		} else if (cursorLocation+1 >= this.scanner.startPosition && cursorLocation < this.scanner.currentPosition){
			this.pushIdentifier();
		}
	}

	// check for different scenarii
	try {
		// no need to go further if we found a non empty completion node
		// (we still need to store labels though)
		if (this.assistNode != null) {
			// however inside an invocation, the completion identifier may already have been consumed into an empty name 
			// completion, so this check should be before we check that we are at the cursor location
			if (!isEmptyNameCompletion() || checkInvocation()) return;
		}

		// no need to check further if we are not at the cursor location
		if (this.indexOfAssistIdentifier() < 0) return;

		if (checkClassInstanceCreation()) return;
		if (checkCatchClause()) return;
		if (checkMemberAccess()) return;
		if (checkClassLiteralAccess()) return;

		// if the completion was not on an empty name, it can still be inside an invocation (eg. this.fred("abc"[cursor])
		// (NB: Put this check before checkNameCompletion() because the selector of the invocation can be on the identifier stack)
		if (checkInvocation()) return;

		if (checkNameCompletion()) return;
	} finally {
		storeLabelsIfNeeded();
	}
}
protected void consumeCaseLabel() {
	Expression caseExpression = this.expressionStack[this.expressionPtr];
	if (caseExpression instanceof SingleNameReference || caseExpression instanceof QualifiedNameReference) {
		// label counter was wrongly incremented in consumeToken
		if (this.labelCounterPtr >= 0) this.labelCounterStack[this.labelCounterPtr]--;
	}
	super.consumeCaseLabel();
}
protected void consumeClassHeaderExtends() {
	this.nextTypeReferenceIsClass = true;
	super.consumeClassHeaderExtends();
	this.nextTypeReferenceIsClass = false;
}
protected void consumeClassTypeElt() {
	this.nextTypeReferenceIsException = true;
	super.consumeClassTypeElt();
	this.nextTypeReferenceIsException = false;
}
protected void consumeConditionalExpression(int op) {
	Expression valueIfTrue = this.expressionStack[this.expressionPtr - 1];
	if (valueIfTrue instanceof SingleNameReference || valueIfTrue instanceof QualifiedNameReference) {
		// label counter was wrongly incremented in consumeToken
		if (this.labelCounterPtr >= 0) this.labelCounterStack[this.labelCounterPtr]--;
	}
	super.consumeConditionalExpression(op);
}
protected void consumeConstructorBody() {
	super.consumeConstructorBody();
	this.labelCounterPtr--;
	if (this.blockInvocationPtr >= 0) this.blockInvocationPtr--;
}
protected void consumeConstructorHeader() {
	super.consumeConstructorHeader();
	pushBlockInvocationPtr();
}
protected void consumeConstructorHeaderName() {

	/* no need to take action if not inside assist identifiers */
	if (indexOfAssistIdentifier() < 0) {
		super.consumeConstructorHeaderName();
		return;
	}
		
	/* force to start recovering in order to get fake field behavior */
	if (currentElement == null){
		this.hasReportedError = true; // do not report any error
	}
	this.restartRecovery = true;
}
protected void consumeEnterVariable() {
	identifierPtr--;
	identifierLengthPtr--;

	boolean isLocalDeclaration = nestedMethod[nestedType] != 0;
	int variableIndex = variablesCounter[nestedType];
	int extendedDimension = intStack[intPtr + 1];
	
	if(isLocalDeclaration || indexOfAssistIdentifier() < 0 || variableIndex != 0 || extendedDimension != 0) {
		identifierPtr++;
		identifierLengthPtr++;
		super.consumeEnterVariable();
	} else {
		restartRecovery = true;
		
		// recovery
		if (currentElement != null) {
			int nameSourceStart = (int)(identifierPositionStack[identifierPtr] >>> 32);
			intPtr--;
			
			TypeReference type = getTypeReference(intStack[intPtr--]);
			intPtr--;
			
			if (!(currentElement instanceof RecoveredType)
				&& (currentToken == TokenNameDOT
					|| (scanner.getLineNumber(type.sourceStart)
							!= scanner.getLineNumber(nameSourceStart)))){
				lastCheckPoint = nameSourceStart;
				restartRecovery = true;
				return;
			}
			
			FieldDeclaration completionFieldDecl = new CompletionOnFieldType(type, false);
			completionFieldDecl.modifiers = intStack[intPtr--];
			assistNode = completionFieldDecl;
			lastCheckPoint = type.sourceEnd + 1;
			currentElement = currentElement.add(completionFieldDecl, 0);
			lastIgnoredToken = -1;
		}
	}
}
protected void consumeExitVariableWithInitialization() {
	super.consumeExitVariableWithInitialization();
	
	// does not keep the initialization if completion is not inside
	AbstractVariableDeclaration variable = (AbstractVariableDeclaration) astStack[astPtr];
	if (cursorLocation + 1 < variable.initialization.sourceStart ||
		cursorLocation > variable.initialization.sourceEnd) {
		variable.initialization = null;
	}
}

/*
 * Copy of code from superclass with the following change:
 * If the cursor location is on the field access, then create a 
 * CompletionOnMemberAccess instead.
 */
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	// potential receiver is being poped, so reset potential receiver
	this.invocationType = NO_RECEIVER;

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFieldAccess(isSuperAccess);
	} else {
		this.pushCompletionOnMemberAccessOnExpressionStack(isSuperAccess);
	}
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
			new CompletionOnArgumentName(
				name, 
				namePositions, 
				type, 
				intStack[intPtr + 1] & ~AccDeprecated); // modifiers
		pushOnAstStack(arg);
		
		assistNode = arg;
		this.lastCheckPoint = (int) namePositions;
		isOrphanCompletionNode = true;

		/* if incomplete method header, listLength counter will not have been reset,
			indicating that some arguments are available on the stack */
		listLength++;
	} 	
}
protected void consumeInterfaceType() {
	this.nextTypeReferenceIsInterface = true;
	super.consumeInterfaceType();
	this.nextTypeReferenceIsInterface = false;
}
protected void consumeMethodHeaderName() {
	if(this.indexOfAssistIdentifier() < 0) {
		identifierPtr--;
		identifierLengthPtr--;
		if(this.indexOfAssistIdentifier() != 0) {
			identifierPtr++;
			identifierLengthPtr++;
			super.consumeMethodHeaderName();
		} else {
			restartRecovery = true;
			
			// recovery
			if (currentElement != null) {
				//name
				char[] selector = identifierStack[identifierPtr + 1];
				long selectorSource = identifierPositionStack[identifierPtr + 1];
				
				//type
				TypeReference type = getTypeReference(intStack[intPtr--]);
				((CompletionOnSingleTypeReference)type).isCompletionNode = false;
				//modifiers
				int declarationSourceStart = intStack[intPtr--];
				int modifiers = intStack[intPtr--];
				
				if(scanner.getLineNumber(type.sourceStart) != scanner.getLineNumber((int) (selectorSource >>> 32))) {
					FieldDeclaration completionFieldDecl = new CompletionOnFieldType(type, false);
					completionFieldDecl.modifiers = modifiers;
					assistNode = completionFieldDecl;
					lastCheckPoint = type.sourceEnd + 1;
					currentElement = currentElement.add(completionFieldDecl, 0);
					lastIgnoredToken = -1;
				} else {
					CompletionOnMethodReturnType md = new CompletionOnMethodReturnType(type, this.compilationUnit.compilationResult);
					md.selector = selector;
					md.declarationSourceStart = declarationSourceStart;
					md.modifiers = modifiers;
					md.bodyStart = lParenPos+1;
					listLength = 0; // initialize listLength before reading parameters/throws
					assistNode = md;
					this.lastCheckPoint = md.bodyStart;
					currentElement = currentElement.add(md, 0);
					lastIgnoredToken = -1;
				}
			}
		}
	} else {
		// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
		CompletionOnMethodName md = new CompletionOnMethodName(this.compilationUnit.compilationResult);
	
		//name
		md.selector = identifierStack[identifierPtr];
		long selectorSource = identifierPositionStack[identifierPtr--];
		//type
		md.returnType = getTypeReference(intStack[intPtr--]);
		//modifiers
		md.declarationSourceStart = intStack[intPtr--];
		md.modifiers = intStack[intPtr--];
	
		//highlight starts at selector start
		md.sourceStart = (int) (selectorSource >>> 32);
		md.selectorEnd = (int) selectorSource;
		pushOnAstStack(md);
		md.sourceEnd = lParenPos;
		md.bodyStart = lParenPos+1;
		listLength = 0; // initialize listLength before reading parameters/throws
		
		this.assistNode = md;	
		this.lastCheckPoint = md.sourceEnd;
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
}


protected void consumeMethodBody() {
	super.consumeMethodBody();
	this.labelCounterPtr--;
	if (this.blockInvocationPtr >= 0) this.blockInvocationPtr--;
}

protected void consumeMethodHeader() {
	super.consumeMethodHeader();
	pushBlockInvocationPtr();
}
protected void consumeModifiers() {
	super.consumeModifiers();
	// save from stack values
	this.lastModifiersStart = intStack[intPtr];
	this.lastModifiers = 	intStack[intPtr-1];
}
protected void consumeNestedMethod() {
	super.consumeNestedMethod();
	this.pushNewLabelCounter();
}
protected void consumeStatementLabel() {
	super.consumeStatementLabel();
	if (this.labelCounterPtr >= 0) this.labelCounterStack[this.labelCounterPtr]--;
}
protected void consumeToken(int token) {
	int previous = this.previousToken;
	int previousIdentifierPtr = this.previousIdentifierPtr;
	super.consumeToken(token);

	// if in field initializer (directly or not), on the completion identifier and not in recovery mode yet
	// then position end of file at cursor location (so that we have the same behavior as
	// in method bodies)
	if (token == TokenNameIdentifier
			&& this.identifierStack[this.identifierPtr] == assistIdentifier()
			&& this.currentElement == null
			&& this.insideFieldInitialization()) {
		this.scanner.eofPosition = cursorLocation < Integer.MAX_VALUE ? cursorLocation+1 : cursorLocation;
	}
	
	// if in a method or if in a field initializer 
	if (this.inMethodStack[this.inMethodPtr] || this.inFieldInitializationStack[this.inFieldInitializationPtr]) {
		switch (token) {
			case TokenNameDOT:
				switch (previous) {
					case TokenNamethis: // eg. this[.]fred()
						this.invocationType = EXPLICIT_RECEIVER;
						break;
					case TokenNamesuper: // eg. super[.]fred()
						this.invocationType = SUPER_RECEIVER;
						break;
					case TokenNameIdentifier: // eg. bar[.]fred()
						if (!this.betweenNewAndLeftBraket) { // eg. not new z.y[.]X()
							if (this.identifierPtr != previousIdentifierPtr) { // if identifier has been consumed, eg. this.x[.]fred()
								this.invocationType = EXPLICIT_RECEIVER;
							} else {
								this.invocationType = NAME_RECEIVER;
							}
						}
						break;
				}
				break;
			case TokenNameIdentifier:
				if (previous == TokenNameDOT) { // eg. foo().[fred]()
					// if current identifier is the empty completion one
					if (identifierStack[identifierPtr] == CompletionScanner.EmptyCompletionIdentifier){
						this.completionBehindDot = true;
					}
					if (this.invocationType != SUPER_RECEIVER // eg. not super.[fred]()
						&& this.invocationType != NAME_RECEIVER // eg. not bar.[fred]()
						&& this.invocationType != ALLOCATION // eg. not new foo.[Bar]()
						&& this.invocationType != QUALIFIED_ALLOCATION) { // eg. not fred().new foo.[Bar]()

						this.invocationType = EXPLICIT_RECEIVER;
						this.qualifier = this.expressionPtr;
					}
				}
				break;	
			case TokenNamenew:
				this.betweenNewAndLeftBraket = true;
				this.qualifier = this.expressionPtr; // NB: even if there is no qualification, set it to the expression ptr so that the number of arguments are correctly computed
				if (previous == TokenNameDOT) { // eg. fred().[new] X()
					this.invocationType = QUALIFIED_ALLOCATION;
				} else { // eg. [new] X()
					this.invocationType = ALLOCATION;
				}
				break;
			case TokenNamethis:
				if (previous == TokenNameDOT) { // eg. fred().[this]()
					this.invocationType = QUALIFIED_ALLOCATION;
					this.qualifier = this.expressionPtr;
				}
				break;
			case TokenNamesuper:
				if (previous == TokenNameDOT) { // eg. fred().[super]()
					this.invocationType = QUALIFIED_ALLOCATION;
					this.qualifier = this.expressionPtr;
				}
				break;
			case TokenNamecatch:
				this.betweenCatchAndRightParen = true;
				break;
			case TokenNameLPAREN:
				this.betweenNewAndLeftBraket = false;
				this.bracketDepth++;
				if (this.invocationType == NO_RECEIVER || this.invocationType == NAME_RECEIVER) {
					this.qualifier = this.expressionPtr; // remenber the last expression so that arguments are correctly computed
				}
				switch (previous) {
					case TokenNameIdentifier: // eg. fred[(]) or foo.fred[(])
						this.pushOnInvocationStacks(this.invocationType, this.qualifier);
						this.invocationType = NO_RECEIVER;
						break;
					case TokenNamethis: // explicit constructor invocation, eg. this[(]1, 2)
						this.pushOnInvocationStacks(
							(this.invocationType == QUALIFIED_ALLOCATION) ? QUALIFIED_ALLOCATION : ALLOCATION, 
							this.qualifier);
						this.invocationType = NO_RECEIVER;
						break;
					case TokenNamesuper: // explicit constructor invocation, eg. super[(]1, 2)
						this.pushOnInvocationStacks(
							(this.invocationType == QUALIFIED_ALLOCATION) ? QUALIFIED_ALLOCATION : ALLOCATION, 
							this.qualifier);
						this.invocationType = NO_RECEIVER;
						break;
				}
				break;
			case TokenNameLBRACE:
				this.betweenNewAndLeftBraket = false;
				this.bracketDepth++;
				this.pushBlockInvocationPtr();
				break;
			case TokenNameLBRACKET:
				this.betweenNewAndLeftBraket = false;
				this.bracketDepth++;
				break; 
			case TokenNameRBRACE:
				this.bracketDepth--;
				if (this.blockInvocationPtr >= 0) this.blockInvocationPtr--;
				break;
			case TokenNameRBRACKET:
				this.bracketDepth--;
				break; 
			case TokenNameRPAREN:
				this.betweenCatchAndRightParen = false;
				this.bracketDepth--;
				break;
			case TokenNameCOLON:
				if (previous == TokenNameIdentifier) {
					if (this.labelCounterPtr >= 0) this.labelCounterStack[this.labelCounterPtr]++;
				}
				break;
			case TokenNamethrow:
				this.throwBracketDepth= bracketDepth;
				break;
		}
	}
}
/**
 * Return whether the given ast node contains the completion node.
 */
private boolean containsCompletionNode(AstNode ast) {
	if (this.assistNode == null || ast instanceof Literal) {
		return false;
	}
	if (this.assistNode == ast) {
		return true;
	}
	if (ast instanceof Reference || ast instanceof TypeReference) {
		return ast == this.assistNode;
	}
	if (ast instanceof Assignment) {
		Assignment assign = (Assignment)ast;
		return containsCompletionNode(assign.lhs) || containsCompletionNode(assign.expression);
	}
	if (ast instanceof UnaryExpression) {
		UnaryExpression unary = (UnaryExpression)ast;
		return containsCompletionNode(unary.expression);
	}
	if (ast instanceof BinaryExpression) {
		BinaryExpression binary = (BinaryExpression)ast;
		return containsCompletionNode(binary.left) || containsCompletionNode(binary.right);
	}
	if (ast instanceof InstanceOfExpression) {
		InstanceOfExpression instanceOfExpr = (InstanceOfExpression)ast;
		return containsCompletionNode(instanceOfExpr.expression) || containsCompletionNode(instanceOfExpr.type);
	}
	if (ast instanceof ConditionalExpression) {
		ConditionalExpression conditional = (ConditionalExpression)ast;
		return containsCompletionNode(conditional.condition) || containsCompletionNode(conditional.valueIfTrue) || containsCompletionNode(conditional.valueIfFalse);
	}
	if (ast instanceof AllocationExpression) {
		AllocationExpression alloc = (AllocationExpression)ast;
		return containsCompletionNode(alloc.type);
	}
	if (ast instanceof CastExpression) {
		CastExpression cast = (CastExpression)ast;
		return containsCompletionNode(cast.expression) || containsCompletionNode(cast.type);
	}
	if (ast instanceof ExplicitConstructorCall) {
		ExplicitConstructorCall call = (ExplicitConstructorCall)ast;
		Expression[] arguments = call.arguments;
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (containsCompletionNode(arguments[i])) {
					return true;
				}
			}
			return false;
		}
	}
	return false;
}
public ImportReference createAssistImportReference(char[][] tokens, long[] positions){
	return new CompletionOnImportReference(tokens, positions);
}
public ImportReference createAssistPackageReference(char[][] tokens, long[] positions){
	return new CompletionOnPackageReference(tokens, positions);
}
public NameReference createQualifiedAssistNameReference(char[][] previousIdentifiers, char[] name, long[] positions){
	return new CompletionOnQualifiedNameReference(
					previousIdentifiers, 
					name, 
					positions); 	
}
public TypeReference createQualifiedAssistTypeReference(char[][] previousIdentifiers, char[] name, long[] positions){
	return this.betweenCatchAndRightParen || this.nextTypeReferenceIsException // check for exception scenario 
				? new CompletionOnQualifiedExceptionReference(
					previousIdentifiers,  
					name, 
					positions)
				: this.nextTypeReferenceIsInterface
					? new CompletionOnQualifiedInterfaceReference(
						previousIdentifiers, 
						name, 
						positions)
					: this.nextTypeReferenceIsClass
						? new CompletionOnQualifiedClassReference(
							previousIdentifiers, 
							name, 
							positions)
						: new CompletionOnQualifiedTypeReference(
							previousIdentifiers, 
							name, 
							positions); 	
}
public NameReference createSingleAssistNameReference(char[] name, long position) {
	return new CompletionOnSingleNameReference(name, position);
}
public TypeReference createSingleAssistTypeReference(char[] name, long position) {
	return this.betweenCatchAndRightParen || this.nextTypeReferenceIsException // check for exception scenario 
		? new CompletionOnExceptionReference(name, position) 
		: this.nextTypeReferenceIsInterface
			? new CompletionOnInterfaceReference(name, position) 
			: this.nextTypeReferenceIsClass
				? new CompletionOnClassReference(name, position) 
				: new CompletionOnSingleTypeReference(name, position);
}
public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int cursorLocation) {

	this.cursorLocation = cursorLocation;
	CompletionScanner completionScanner = (CompletionScanner)this.scanner;
	completionScanner.completionIdentifier = null;
	completionScanner.cursorLocation = cursorLocation;
	return this.dietParse(sourceUnit, compilationResult);
}
/*
 * Flush parser/scanner state regarding to code assist
 */
public void flushAssistState() {

	super.flushAssistState();
	this.isOrphanCompletionNode = false;
	CompletionScanner completionScanner = (CompletionScanner)this.scanner;
	completionScanner.completedIdentifierStart = 0;
	completionScanner.completedIdentifierEnd = -1;
}
protected NameReference getUnspecifiedReferenceOptimized() {
	if (this.identifierLengthStack[this.identifierLengthPtr] > 1) { // reducing a qualified name
		// potential receiver is being poped, so reset potential receiver
		this.invocationType = NO_RECEIVER;
	}
	return super.getUnspecifiedReferenceOptimized();
}
/**
 * Return whether the given ast node has information interresting for code completion.
 */
private boolean hasCompletionInformation(AstNode ast) {
	return (
		ast instanceof AbstractMethodDeclaration ||
		ast instanceof AbstractVariableDeclaration ||
		ast instanceof LabeledStatement ||
		ast instanceof TypeDeclaration);
}
public void initialize() {
	super.initialize();
	this.initializeForBlockStatements();
	this.labelCounterPtr = -1;
}
/*
 * Initializes the state of the parser that is about to go for BlockStatements.
 */
private void initializeForBlockStatements() {
	this.previousToken = -1;
	this.previousIdentifierPtr = -1;
	this.completionBehindDot = false;
	this.betweenNewAndLeftBraket = false;
	this.betweenCatchAndRightParen = false;
	this.bracketDepth = 0;
	this.throwBracketDepth = -1;
	this.invocationType = NO_RECEIVER;
	this.qualifier = -1;
	this.blockInvocationPtr = -1;
}
public void initializeScanner(){
	this.scanner = new CompletionScanner(this.assertMode);
}
/**
 * Returns whether the completion is just after an array type
 * eg. String[].[cursor]
 */
private boolean isAfterArrayType() {
	// TBD: The following relies on the fact that array dimensions are small: it says that if the
	//      top of the intStack is less than 11, then it must be a dimension 
	//      (smallest position of array type in a compilation unit is 11 as in "class X{Y[]")
	if ((this.intPtr > -1) && (this.intStack[this.intPtr] < 11)) {
		return true;
	}
	return false;
}
private boolean isEmptyNameCompletion() {
	return
		this.assistNode != null && 
		this.assistNode instanceof CompletionOnSingleNameReference &&
		(((CompletionOnSingleNameReference)this.assistNode).token.length == 0);
}
public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int cursorLocation) {

	this.cursorLocation = cursorLocation;
	CompletionScanner completionScanner = (CompletionScanner)this.scanner;
	completionScanner.completionIdentifier = null;
	completionScanner.cursorLocation = cursorLocation;
	return this.parse(sourceUnit, compilationResult);
}
/*
 * Prepares the state of the parser to go for BlockStatements.
 */
protected void prepareForBlockStatements() {
	super.prepareForBlockStatements();
	this.initializeForBlockStatements();
}
protected void pushBlockInvocationPtr() {
	try {
		this.blockInvocationStack[++this.blockInvocationPtr] = this.invocationPtr+1;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = this.blockInvocationStack.length;
		int[] oldStack = this.blockInvocationStack;
		this.blockInvocationStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, this.blockInvocationStack, 0, oldStackLength);
		this.blockInvocationStack[this.blockInvocationPtr] = this.invocationPtr+1;
	}
}
/**
 * Creates a completion on member access node and push it
 * on the expression stack.
 */
private void pushCompletionOnMemberAccessOnExpressionStack(boolean isSuperAccess) {
	char[] source = identifierStack[identifierPtr];
	long pos = identifierPositionStack[identifierPtr--];
	CompletionOnMemberAccess fr = new CompletionOnMemberAccess(source, pos);
	this.assistNode = fr;
	this.lastCheckPoint = fr.sourceEnd + 1;
	identifierLengthPtr--;
	if (isSuperAccess) { //considerates the fieldReference beginning at the 'super' ....	
		fr.sourceStart = intStack[intPtr--];
		fr.receiver = new SuperReference(fr.sourceStart, endPosition);
		pushOnExpressionStack(fr);
	} else { //optimize push/pop
		if ((fr.receiver = expressionStack[expressionPtr]).isThis()) { //fieldreference begins at the this
			fr.sourceStart = fr.receiver.sourceStart;
		}
		expressionStack[expressionPtr] = fr;
	}
}
protected void pushNewLabelCounter() {
	try {
		this.labelCounterStack[++this.labelCounterPtr] = 0;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = this.labelCounterStack.length;
		int[] oldStack = this.labelCounterStack;
		this.labelCounterStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, this.labelCounterStack, 0, oldStackLength);
		this.labelCounterStack[this.labelCounterPtr] = 0;
	}
}
/**
 * Pushes the given invocation type (one of the invocation type constants) on the invocation type stack,
 * and the given qualifier (an expression pointer to the expression stack) on the qualifier stack.
 */
protected void pushOnInvocationStacks(int invocationType, int qualifierExprPtr) {
	// NB: invocationPtr has already been incremented by a call to pushOnSelectorStack()
	try {
		this.invocationTypeStack[this.invocationPtr] = invocationType;
		this.qualifierStack[this.invocationPtr] = qualifierExprPtr;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = this.invocationTypeStack.length;
		int oldInvocationTypeStack[] = this.invocationTypeStack;
		int oldQualifierStack[] = this.qualifierStack;
		this.invocationTypeStack = new int[oldStackLength + StackIncrement];
		this.qualifierStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldInvocationTypeStack, 0, this.invocationTypeStack, 0, oldStackLength);
		System.arraycopy(oldQualifierStack, 0, this.qualifierStack, 0, oldStackLength);
		this.invocationTypeStack[this.invocationPtr] = invocationType;
		this.qualifierStack[this.invocationPtr] = qualifierExprPtr;
	}
}
public void recordCompletionOnReference(){

	if (currentElement instanceof RecoveredType){
		RecoveredType recoveredType = (RecoveredType)currentElement;

		/* filter out cases where scanner is still inside type header */
		if (!recoveredType.foundOpeningBrace) return;
		
		/* generate a pseudo field with a completion on type reference */	
		currentElement.add(
			new CompletionOnFieldType(this.getTypeReference(0), false), 0);
		return;
	}
	if (!diet) return; // only record references attached to types

}
protected void reportSyntaxError(int act, int currentKind, int stateStackTop) {

	/* Intercept error state on EOF inside method bodies, due to 
	   cursor location being used as an EOF position.
	*/
	if (!diet && currentToken == TokenNameEOF) return;
	super.reportSyntaxError(act, currentKind, stateStackTop);
}
/*
 * Reset internal state after completion is over
 */
 
public void reset() {
	super.reset();
	this.cursorLocation = 0;
}
/*
 * Reset internal state after completion is over
 */
 
public void resetAfterCompletion() {
	this.cursorLocation = 0;
	this.flushAssistState();
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {
	if (this.assistNode != null) {
		/* if reached [eof] inside method body, but still inside nested type,
			or inside a field initializer, should continue in diet mode until 
			the end of the method body or compilation unit */
		if ((scanner.eofPosition == cursorLocation+1)
			&& (!(referenceContext instanceof CompilationUnitDeclaration) 
				|| insideFieldInitialization())) {

			/*	disabled since does not handle possible field/message refs, i.e. Obj[ASSIST HERE]ect.registerNatives()		    
			// consume extra tokens which were part of the qualified reference
			//   so that the replaced source comprises them as well 
			if (this.assistNode instanceof NameReference){
				int oldEof = scanner.eofPosition;
				scanner.eofPosition = currentElement.topElement().sourceEnd()+1;
				scanner.currentPosition = this.cursorLocation+1;
				int token = -1;
				try {
					do {
						// first token might not have to be a dot
						if (token >= 0 || !this.completionBehindDot){
							if ((token = scanner.getNextToken()) != TokenNameDOT) break;
						}
						if ((token = scanner.getNextToken()) != TokenNameIdentifier) break;
						this.assistNode.sourceEnd = scanner.currentPosition - 1;
					} while (token != TokenNameEOF);
				} catch (InvalidInputException e){
				} finally {
					scanner.eofPosition = oldEof;
				}
			}
			*/			
			/* restart in diet mode for finding sibling constructs */
			if (currentElement.enclosingType() != null){
				lastCheckPoint = this.assistNode.sourceEnd+1;
				int end = currentElement.topElement().sourceEnd();
				scanner.eofPosition = end < Integer.MAX_VALUE ? end + 1 : end;
			} else {
				this.resetStacks();
				return false;	
			}
		}
	}
	return super.resumeAfterRecovery();
}
public void setAssistIdentifier(char[] assistIdent){
	((CompletionScanner)scanner).completionIdentifier = assistIdent;
}
/**
 * Stores the labels left on the identifier stack if they have not been stored yet.
 */
private void storeLabelsIfNeeded() {
//	int counter = this.labelCounterPtr >= 0 ? this.labelCounterStack[this.labelCounterPtr] : 0;
//	if (this.labels == null && this.identifierPtr >= 0) {
//		this.labels = new char[counter][];
//		System.arraycopy(this.identifierStack, this.identifierPtr - counter + 1, this.labels, 0, counter);
//	}
//	this.identifierPtr -= counter;
//	this.identifierLengthPtr -= counter; // labels have not been concatenated yet
}
/*
 * Update recovery state based on current parser/scanner state
 */
protected void updateRecoveryState() {

	/* expose parser state to recovery state */
	currentElement.updateFromParserState();

	/* may be able to retrieve completionNode as an orphan, and then attach it */
	this.completionIdentifierCheck();
	this.attachOrphanCompletionNode();
	
	// if an assist node has been found and a recovered element exists,
	// mark enclosing blocks as to be preserved
	if (this.assistNode != null && this.currentElement != null) {
		currentElement.preserveEnclosingBlocks();
	}
	
	/* check and update recovered state based on current token,
		this action is also performed when shifting token after recovery
		got activated once. 
	*/
	this.recoveryTokenCheck();
}

protected LocalDeclaration createLocalDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	if (this.indexOfAssistIdentifier() < 0) {
		return super.createLocalDeclaration(initialization, name, sourceStart, sourceEnd);
	} else {
		CompletionOnLocalName local = new CompletionOnLocalName(initialization, name, sourceStart, sourceEnd);
		this.assistNode = local;
		this.lastCheckPoint = sourceEnd + 1;
		return local;
	}
}

protected FieldDeclaration createFieldDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	if (this.indexOfAssistIdentifier() < 0) {
		return super.createFieldDeclaration(initialization, name, sourceStart, sourceEnd);
	} else {
		CompletionOnFieldName field = new CompletionOnFieldName(initialization, name, sourceStart, sourceEnd);
		this.assistNode = field;
		this.lastCheckPoint = sourceEnd + 1;
		return field;
	}
}

}
