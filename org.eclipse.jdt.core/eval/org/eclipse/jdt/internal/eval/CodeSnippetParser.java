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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

/**
 * A parser for code snippets.
 */
public class CodeSnippetParser extends Parser implements EvaluationConstants {
	int codeSnippetStart, codeSnippetEnd;
	boolean hasRecoveredOnExpression;
	int problemCountBeforeRecovery = 0;
	int lastStatement = -1; // end of last top level statement

	EvaluationContext evaluationContext;
/**
 * Creates a new code snippet parser.
 */
public CodeSnippetParser(ProblemReporter problemReporter, EvaluationContext evaluationContext, boolean optimizeStringLiterals, boolean assertMode, int codeSnippetStart, int codeSnippetEnd) {
	super(problemReporter, optimizeStringLiterals, assertMode);
	this.codeSnippetStart = codeSnippetStart;
	this.codeSnippetEnd = codeSnippetEnd;
	this.evaluationContext = evaluationContext;
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
			alloc = new CodeSnippetAllocationExpression(evaluationContext);
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
		if (anonymousTypeDeclaration.allocation != null) {
			anonymousTypeDeclaration.allocation.sourceEnd = endStatementPosition;
		}		
		astPtr--;
		astLengthPtr--;

		// mark fields and initializer with local type mark if needed
		markFieldsWithLocalType(anonymousTypeDeclaration);
	}
}
protected void consumeClassDeclaration() {
	super.consumeClassDeclaration();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeClassHeaderName() {
	// ClassHeaderName ::= Modifiersopt 'class' 'Identifier'
	TypeDeclaration typeDecl;
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl = new MemberTypeDeclaration(this.compilationUnit.compilationResult);
		} else {
			typeDecl = new CodeSnippetTypeDeclaration(this.compilationUnit.compilationResult);
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
	typeDecl.declarationSourceStart = intStack[intPtr--]; 
	intPtr--;
	// 'class' and 'interface' push an int position
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
protected void consumeEmptyStatement() {
	super.consumeEmptyStatement();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeExpressionStatement() {
	super.consumeExpressionStatement();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	FieldReference fr =
		new CodeSnippetFieldReference(
			identifierStack[identifierPtr],
			identifierPositionStack[identifierPtr--],
			evaluationContext);
	identifierLengthPtr--;
	if (isSuperAccess) {
		//considerates the fieldReference beginning at the 'super' ....	
		fr.sourceStart = intStack[intPtr--];
		problemReporter().codeSnippetMissingClass(null,0, 0);
		fr.receiver = new CodeSnippetSuperReference(fr.sourceStart, endPosition, evaluationContext);
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
protected void consumeInterfaceHeaderName() {
	// InterfaceHeaderName ::= Modifiersopt 'interface' 'Identifier'
	TypeDeclaration typeDecl;
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl = new MemberTypeDeclaration(this.compilationUnit.compilationResult);
		} else {
			typeDecl = new CodeSnippetTypeDeclaration(this.compilationUnit.compilationResult);
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
	typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--;
	// 'class' and 'interface' push an int position
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
protected void consumeLocalVariableDeclarationStatement() {
	super.consumeLocalVariableDeclarationStatement();
	/* recovery */
	recordLastStatementIfNeeded();
}
/**
 * In case emulating local variables, wrap the (recovered) statements inside a 
 * try statement so as to achieve local state commiting (copy local vars back to fields).
 * The CSToCuMapper could not be used, since it could have interfered with
 * the syntax recovery specific to code snippets.
 */
protected void consumeMethodDeclaration(boolean isNotAbstract) {
	// MethodDeclaration ::= MethodHeader MethodBody
	// AbstractMethodDeclaration ::= MethodHeader ';'

	super.consumeMethodDeclaration(isNotAbstract);
	
	// now we know that we have a method declaration at the top of the ast stack
	MethodDeclaration methodDecl = (MethodDeclaration) astStack[astPtr];

	// automatically wrap the last statement inside a return statement, if it is an expression
	// support have to be defined at toplevel only
	if (this.isTopLevelType()) {
		int last = methodDecl.statements == null ? -1 : methodDecl.statements.length - 1;
		if (last >= 0 && methodDecl.statements[last] instanceof Expression){
			Expression lastExpression = (Expression) methodDecl.statements[last];
			methodDecl.statements[last] = new CodeSnippetReturnStatement(
											lastExpression, 
											lastExpression.sourceStart, 
											lastExpression.sourceEnd,
											evaluationContext);
		}
	}
	
	int start = methodDecl.bodyStart-1, end = start;
	long position = (start << 32) + end;
	long[] positions = new long[]{position};
	if (this.evaluationContext.localVariableNames != null) {

		int varCount = this.evaluationContext.localVariableNames.length; // n local decls+ try statement

		// generate n local variable declarations: [type] [name] = val$[name];
		Statement[] newStatements = new Statement[varCount+1];
		for (int i = 0; i < varCount; i++){
			char[] trimmedTypeName = this.evaluationContext.localVariableTypeNames[i];
			int nameEnd = CharOperation.indexOf('[', trimmedTypeName);
			if (nameEnd >= 0) trimmedTypeName = CharOperation.subarray(trimmedTypeName, 0, nameEnd);
			nameEnd = CharOperation.indexOf(' ', trimmedTypeName);
			if (nameEnd >= 0) trimmedTypeName = CharOperation.subarray(trimmedTypeName, 0, nameEnd);
			
			TypeReference typeReference = new QualifiedTypeReference(
				CharOperation.splitOn('.', trimmedTypeName),
				positions);
			int dimCount = CharOperation.occurencesOf('[', this.evaluationContext.localVariableTypeNames[i]);
			if (dimCount > 0) {
				typeReference = this.copyDims(typeReference, dimCount);
			}
			NameReference init = new SingleNameReference(
									CharOperation.concat(LOCAL_VAR_PREFIX, this.evaluationContext.localVariableNames[i]), position);
			LocalDeclaration declaration = new LocalDeclaration(init, this.evaluationContext.localVariableNames[i], start, end);
			declaration.type = typeReference;
			declaration.modifiers = this.evaluationContext.localVariableModifiers[i];
			newStatements[i] = declaration;
		}

		// generate try { [snippet] } finally { [save locals to fields] }
		// try block
		TryStatement tryStatement = new TryStatement();
		Block tryBlock = new Block(methodDecl.explicitDeclarations);
		tryBlock.sourceStart = start;
		tryBlock.sourceEnd = end;
		tryBlock.statements = methodDecl.statements; // snippet statements
		tryStatement.tryBlock = tryBlock;
		// finally block
		Block finallyBlock = new Block(0);
		finallyBlock.sourceStart = start;
		finallyBlock.sourceEnd = end;
		finallyBlock.statements = new Statement[varCount];
		for (int i = 0; i < varCount; i++){
			finallyBlock.statements[i] = new Assignment(
				new SingleNameReference(CharOperation.concat(LOCAL_VAR_PREFIX, this.evaluationContext.localVariableNames[i]), position),
				new SingleNameReference(this.evaluationContext.localVariableNames[i], position),
				(int) position);
		}
		tryStatement.finallyBlock = finallyBlock;

		newStatements[varCount] = tryStatement;
		methodDecl.statements = newStatements;
	}
}

protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	if (scanner.startPosition >= this.codeSnippetStart
		&& scanner.startPosition <= this.codeSnippetEnd + 1 + Util.LINE_SEPARATOR_CHARS.length // 14838
		&& isTopLevelType()) {
			
		// when the name is only an identifier...we have a message send to "this" (implicit)

		MessageSend m = newMessageSend();
		m.sourceEnd = rParenPos;
		m.sourceStart = 
			(int) ((m.nameSourcePosition = identifierPositionStack[identifierPtr]) >>> 32); 
		m.selector = identifierStack[identifierPtr--];
		if (identifierLengthStack[identifierLengthPtr] == 1) {
			m.receiver = new CodeSnippetThisReference(0,0,evaluationContext, true);
			identifierLengthPtr--;
		} else {
			identifierLengthStack[identifierLengthPtr]--;
			m.receiver = getUnspecifiedReference();
			m.sourceStart = m.receiver.sourceStart;		
		}
		pushOnExpressionStack(m);
	} else {
		super.consumeMethodInvocationName();
	}
}

protected void consumeMethodInvocationSuper() {
	// MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'

	MessageSend m = newMessageSend();
	m.sourceStart = intStack[intPtr--];
	m.sourceEnd = rParenPos;
	m.nameSourcePosition = identifierPositionStack[identifierPtr];
	m.selector = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	m.receiver = new CodeSnippetSuperReference(m.sourceStart, endPosition, this.evaluationContext);
	pushOnExpressionStack(m);
}

protected void consumePrimaryNoNewArrayThis() {
	// PrimaryNoNewArray ::= 'this'

	if (scanner.startPosition >= this.codeSnippetStart
		&& scanner.startPosition <= this.codeSnippetEnd + 1 + Util.LINE_SEPARATOR_CHARS.length // 14838
		&& isTopLevelType()) {
		pushOnExpressionStack(
			new CodeSnippetThisReference(intStack[intPtr--], endPosition, this.evaluationContext, false));
	} else {
		super.consumePrimaryNoNewArrayThis();
	}
}
protected void consumeStatementBreak() {
	super.consumeStatementBreak();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementBreakWithLabel() {
	super.consumeStatementBreakWithLabel();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementCatch() {
	super.consumeStatementCatch();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementContinue() {
	super.consumeStatementContinue();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementContinueWithLabel() {
	super.consumeStatementContinueWithLabel();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementDo() {
	super.consumeStatementDo();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementFor() {
	super.consumeStatementFor();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementIfNoElse() {
	super.consumeStatementIfNoElse();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementIfWithElse() {
	super.consumeStatementIfWithElse();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementLabel() {
	super.consumeStatementLabel();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementReturn() {
	// ReturnStatement ::= 'return' Expressionopt ';'

	// returned value intercepted by code snippet 
	// support have to be defined at toplevel only
	if ((this.hasRecoveredOnExpression
			|| (scanner.startPosition >= codeSnippetStart && scanner.startPosition <= codeSnippetEnd+1+Util.LINE_SEPARATOR_CHARS.length /* 14838*/))
		&& this.expressionLengthStack[this.expressionLengthPtr] != 0
		&& isTopLevelType()) {
		this.expressionLengthPtr--;
		Expression expression = this.expressionStack[this.expressionPtr--];
		pushOnAstStack(
			new CodeSnippetReturnStatement(
				expression, 
				expression.sourceStart, 
				expression.sourceEnd,
				evaluationContext));
	} else {
		super.consumeStatementReturn();
	}
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementSwitch() {
	super.consumeStatementSwitch();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementSynchronized() {
	super.consumeStatementSynchronized();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementThrow() {
	super.consumeStatementThrow();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementTry(boolean arg_0) {
	super.consumeStatementTry(arg_0);
	/* recovery */
	recordLastStatementIfNeeded();
}
protected void consumeStatementWhile() {
	super.consumeStatementWhile();
	/* recovery */
	recordLastStatementIfNeeded();
}
protected CompilationUnitDeclaration endParse(int act) {
	if (this.hasRecoveredOnExpression) {
		CompilationResult unitResult = this.compilationUnit.compilationResult;
		if (act != ERROR_ACTION) { // expression recovery worked
			// flush previously recorded problems
			for (int i = 0; i < unitResult.problemCount; i++) {
				unitResult.problems[i] = null; // discard problem
			}
			unitResult.problemCount = 0;
			if (this.referenceContext instanceof AbstractMethodDeclaration) {
				((AbstractMethodDeclaration)this.referenceContext).ignoreFurtherInvestigation = false;
			}
			if (this.referenceContext instanceof CompilationUnitDeclaration) {
				((CompilationUnitDeclaration)this.referenceContext).ignoreFurtherInvestigation = false;
			}

			// consume expresion as a return statement
			consumeStatementReturn();
			int fieldsCount = 
				(this.evaluationContext.localVariableNames == null ? 0 : this.evaluationContext.localVariableNames.length)
				+ (this.evaluationContext.declaringTypeName == null ? 0 : 1);
			if (this.astPtr > (this.diet ? 0 : 2 + fieldsCount)) { 
					// in diet mode, the ast stack was empty when we went for method body
					// otherwise it contained the type, the generated fields for local variables, 
					// the generated field for 'this' and the method
				consumeBlockStatements();
			}
			consumeMethodBody();
			if (!this.diet) {
				consumeMethodDeclaration(true);
				if (fieldsCount > 0) {
					consumeClassBodyDeclarations();
				}
				consumeClassBodyDeclarationsopt();
				consumeClassDeclaration();
				consumeTypeDeclarationsopt();
				consumeCompilationUnit();
			}
			this.lastAct = ACCEPT_ACTION;
		} else {
			// might have more than one error recorded:
			// 1. during regular parse
			// 2. during expression recovery
			// -> must filter out one of them, the earliest one is less accurate
			int maxRegularPos = 0, problemCount = unitResult.problemCount;
			for (int i = 0; i < this.problemCountBeforeRecovery; i++) {
				// skip unmatched bracket problems
				if (unitResult.problems[i].getID() == IProblem.UnmatchedBracket) continue;
				
				int start = unitResult.problems[i].getSourceStart();
				if (start > maxRegularPos && start <= this.codeSnippetEnd) {
					maxRegularPos = start;
				}
			}
			int maxRecoveryPos = 0;
			for (int i = this.problemCountBeforeRecovery; i < problemCount; i++) {
				// skip unmatched bracket problems
				if (unitResult.problems[i].getID() == IProblem.UnmatchedBracket) continue;
				
				int start = unitResult.problems[i].getSourceStart();
				if (start > maxRecoveryPos && start <= this.codeSnippetEnd) {
					maxRecoveryPos = start;
				}
			}
			if (maxRecoveryPos > maxRegularPos) {
				System.arraycopy(unitResult.problems, this.problemCountBeforeRecovery, unitResult.problems, 0, problemCount - this.problemCountBeforeRecovery);
				unitResult.problemCount -= this.problemCountBeforeRecovery;				
			} else {
				unitResult.problemCount -= (problemCount - this.problemCountBeforeRecovery);
			}
			for (int i = unitResult.problemCount; i < problemCount; i++) {
				unitResult.problems[i] = null; // discard problem
			}

		}
	}
	return super.endParse(act);
}
protected NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	if (scanner.startPosition >= codeSnippetStart 
		&& scanner.startPosition <= codeSnippetEnd+1+Util.LINE_SEPARATOR_CHARS.length /*14838*/){
		int length;
		NameReference ref;
		if ((length = identifierLengthStack[identifierLengthPtr--]) == 1)
			// single variable reference
			ref = 
				new CodeSnippetSingleNameReference(
					identifierStack[identifierPtr], 
					identifierPositionStack[identifierPtr--],
					this.evaluationContext); 
		else
			//Qualified variable reference
			{
			char[][] tokens = new char[length][];
			identifierPtr -= length;
			System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
			ref = 
				new CodeSnippetQualifiedNameReference(tokens, 
					(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
					(int) identifierPositionStack[identifierPtr + length],
					evaluationContext); // sourceEnd
		};
		return ref;
	} else {
		return super.getUnspecifiedReference();
	}
}
protected NameReference getUnspecifiedReferenceOptimized() {
	/* build a (unspecified) NameReference which may be qualified
	The optimization occurs for qualified reference while we are
	certain in this case the last item of the qualified name is
	a field access. This optimization is IMPORTANT while it results
	that when a NameReference is build, the type checker should always
	look for that it is not a type reference */

	if (scanner.startPosition >= codeSnippetStart 
		&& scanner.startPosition <= codeSnippetEnd+1+Util.LINE_SEPARATOR_CHARS.length /*14838*/){
		int length;
		NameReference ref;
		if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
			// single variable reference
			ref = 
				new CodeSnippetSingleNameReference(
					identifierStack[identifierPtr], 
					identifierPositionStack[identifierPtr--],
					this.evaluationContext); 
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
		ref = new CodeSnippetQualifiedNameReference(
				tokens, 
				(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
				(int) identifierPositionStack[identifierPtr + length],
				evaluationContext); // sourceEnd
		ref.bits &= ~AstNode.RestrictiveFlagMASK;
		ref.bits |= LOCAL | FIELD;
		return ref;
	} else {
		return super.getUnspecifiedReferenceOptimized();
	}
}
protected void ignoreExpressionAssignment() {
	super.ignoreExpressionAssignment();
	/* recovery */
	recordLastStatementIfNeeded();
}
/**
 * Returns whether we are parsing a top level type or not.
 */
private boolean isTopLevelType() {
	return this.nestedType == (this.diet ? 0 : 1);
}
protected MessageSend newMessageSend() {
	// '(' ArgumentListopt ')'
	// the arguments are on the expression stack

	CodeSnippetMessageSend m = new CodeSnippetMessageSend(evaluationContext);
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
/**
 * Records the scanner position if we're parsing a top level type.
 */
private void recordLastStatementIfNeeded() {
	if ((isTopLevelType()) && (this.scanner.startPosition <= this.codeSnippetEnd+Util.LINE_SEPARATOR_CHARS.length /*14838*/)) {
		this.lastStatement = this.scanner.startPosition;
	}
}
protected void reportSyntaxError(int act, int currentKind, int stateStackTop) {
	if (!this.diet) {
		this.scanner.initialPosition = this.codeSnippetStart; // for correct bracket match diagnosis
		this.scanner.eofPosition = this.codeSnippetEnd + 1; // stop after expression 
	}
	super.reportSyntaxError(act, currentKind, stateStackTop);
}
/*
 * A syntax error was detected. If a method is being parsed, records the number of errors and
 * attempts to restart from the last statement by going for an expression.
 */
protected boolean resumeOnSyntaxError() {
	if (this.diet || this.hasRecoveredOnExpression) { // no reentering inside expression recovery
		return super.resumeOnSyntaxError();
	}
	
	// record previous error, in case more accurate than potential one in expression recovery
	// e.g. "return foo(a a); 1+3"
	this.problemCountBeforeRecovery = this.compilationUnit.compilationResult.problemCount;

	// reposition for expression parsing
	if (this.lastStatement < 0) {
		this.lastStatement = this.codeSnippetStart; // no statement reduced prior to error point
	}
	this.scanner.initialPosition = this.lastStatement;
	this.scanner.startPosition = this.lastStatement;
	this.scanner.currentPosition = this.lastStatement;
	this.scanner.eofPosition = this.codeSnippetEnd < Integer.MAX_VALUE ? this.codeSnippetEnd + 1 : this.codeSnippetEnd; // stop after expression 
	this.scanner.commentPtr = -1;

	// reset stacks in consistent state
	this.expressionPtr = -1;
	this.identifierPtr = -1;
	this.identifierLengthPtr = -1;

	// go for the exprssion
	goForExpression();
	this.hasRecoveredOnExpression = true;
	this.hasReportedError = false;
	return true;
}
}
