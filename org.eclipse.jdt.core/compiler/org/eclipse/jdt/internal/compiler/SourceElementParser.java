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

/**
 * A source element parser extracts structural and reference information
 * from a piece of source.
 *
 * also see @ISourceElementRequestor
 *
 * The structural investigation includes:
 * - the package statement
 * - import statements
 * - top-level types: package member, member types (member types of member types...)
 * - fields
 * - methods
 *
 * If reference information is requested, then all source constructs are
 * investigated and type, field & method references are provided as well.
 *
 * Any (parsing) problem encountered is also provided.
 */

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class SourceElementParser extends Parser {
	
	ISourceElementRequestor requestor;
	private int fieldCount;
	private int localIntPtr;
	private int lastFieldEndPosition;
	private ISourceType sourceType;
	private boolean reportReferenceInfo;
	private char[][] typeNames;
	private char[][] superTypeNames;
	private int nestedTypeIndex;
	private static final char[] JAVA_LANG_OBJECT = "java.lang.Object".toCharArray(); //$NON-NLS-1$
	private NameReference[] unknownRefs;
	private int unknownRefsCounter;
	private LocalDeclarationVisitor localDeclarationVisitor = null;
	private CompilerOptions options;
	
/**
 * An ast visitor that visits local type declarations.
 */
public class LocalDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	public boolean visit(
			AnonymousLocalTypeDeclaration anonymousTypeDeclaration,
			BlockScope scope) {
		notifySourceElementRequestor(anonymousTypeDeclaration, sourceType == null);
		return false; // don't visit members as this was done during notifySourceElementRequestor(...)
	}
	public boolean visit(LocalTypeDeclaration typeDeclaration, BlockScope scope) {
		notifySourceElementRequestor(typeDeclaration, sourceType == null);
		return false; // don't visit members as this was done during notifySourceElementRequestor(...)
	}
	public boolean visit(MemberTypeDeclaration typeDeclaration, ClassScope scope) {
		notifySourceElementRequestor(typeDeclaration, sourceType == null);
		return false; // don't visit members as this was done during notifySourceElementRequestor(...)
	}
	
}

public SourceElementParser(
	final ISourceElementRequestor requestor, 
	IProblemFactory problemFactory,
	CompilerOptions options) {
	// we want to notify all syntax error with the acceptProblem API
	// To do so, we define the record method of the ProblemReporter
	super(new ProblemReporter(
		DefaultErrorHandlingPolicies.exitAfterAllProblems(),
		options, 
		problemFactory) {
		public void record(IProblem problem, CompilationResult unitResult, ReferenceContext referenceContext) {
			unitResult.record(problem, referenceContext);
			requestor.acceptProblem(problem);
		}
	},
	true,
	options.assertMode);
	this.requestor = requestor;
	typeNames = new char[4][];
	superTypeNames = new char[4][];
	nestedTypeIndex = 0;
	this.options = options;
}

/** @deprecated use SourceElementParser(ISourceElementRequestor, IProblemFactory, CompilerOptions) */
public SourceElementParser(
	final ISourceElementRequestor requestor, 
	IProblemFactory problemFactory) {
		this(requestor, problemFactory, new CompilerOptions());
}

public SourceElementParser(
	final ISourceElementRequestor requestor, 
	IProblemFactory problemFactory,
	CompilerOptions options,
	boolean reportLocalDeclarations) {
		this(requestor, problemFactory, options);
		if (reportLocalDeclarations) {
			this.localDeclarationVisitor = new LocalDeclarationVisitor();
		}
}

public void checkAnnotation() {
	int firstCommentIndex = scanner.commentPtr;

	super.checkAnnotation();

	// modify the modifier source start to point at the first comment
	if (firstCommentIndex >= 0) {
		modifiersSourceStart = scanner.commentStarts[0]; 
	}
}

protected void classInstanceCreation(boolean alwaysQualified) {

	boolean previousFlag = reportReferenceInfo;
	reportReferenceInfo = false; // not to see the type reference reported in super call to getTypeReference(...)
	super.classInstanceCreation(alwaysQualified);
	reportReferenceInfo = previousFlag;
	if (reportReferenceInfo){
		AllocationExpression alloc = (AllocationExpression)expressionStack[expressionPtr];
		TypeReference typeRef = alloc.type;
		requestor.acceptConstructorReference(
			typeRef instanceof SingleTypeReference 
				? ((SingleTypeReference) typeRef).token
				: CharOperation.concatWith(alloc.type.getTypeName(), '.'),
			alloc.arguments == null ? 0 : alloc.arguments.length, 
			alloc.sourceStart);
	}
}
protected void consumeConstructorHeaderName() {
	// ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('

	/* recovering - might be an empty message send */
	if (currentElement != null){
		if (lastIgnoredToken == TokenNamenew){ // was an allocation expression
			lastCheckPoint = scanner.startPosition; // force to restart at this exact position				
			restartRecovery = true;
			return;
		}
	}
	SourceConstructorDeclaration cd = new SourceConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	long selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];

	//highlight starts at the selector starts
	cd.sourceStart = (int) (selectorSourcePositions >>> 32);
	cd.selectorSourceEnd = (int) selectorSourcePositions;
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
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// the scanner is located after the comma or the semi-colon.
	// we want to include the comma or the semi-colon
	super.consumeExitVariableWithInitialization();
	if (isLocalDeclaration() || ((currentToken != TokenNameCOMMA) && (currentToken != TokenNameSEMICOLON)))
		return;
	((SourceFieldDeclaration) astStack[astPtr]).fieldEndPosition = scanner.currentPosition - 1;
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	if (isLocalDeclaration() || ((currentToken != TokenNameCOMMA) && (currentToken != TokenNameSEMICOLON)))
		return;
	((SourceFieldDeclaration) astStack[astPtr]).fieldEndPosition = scanner.currentPosition - 1;
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'
	super.consumeFieldAccess(isSuperAccess);
	FieldReference fr = (FieldReference) expressionStack[expressionPtr];
	if (reportReferenceInfo) {
		requestor.acceptFieldReference(fr.token, fr.sourceStart);
	}
}
protected void consumeMethodHeaderName() {
	// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	SourceMethodDeclaration md = new SourceMethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = identifierStack[identifierPtr];
	long selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(intStack[intPtr--]);
	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSourcePositions >>> 32);
	md.selectorSourceEnd = (int) selectorSourcePositions;
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
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)
	super.consumeMethodInvocationName();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationSuper() {
	// MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'
	super.consumeMethodInvocationSuper();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	super.consumeSingleTypeImportDeclarationName();
	ImportReference impt = (ImportReference)astStack[astPtr];
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	super.consumeTypeImportOnDemandDeclarationName();
	ImportReference impt = (ImportReference)astStack[astPtr];
	if (reportReferenceInfo) {
		requestor.acceptUnknownReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected FieldDeclaration createFieldDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	return new SourceFieldDeclaration(null, name, sourceStart, sourceEnd);
}
protected CompilationUnitDeclaration endParse(int act) {
	if (sourceType != null) {
		if (sourceType.isInterface()) {
			consumeInterfaceDeclaration();
		} else {
			consumeClassDeclaration();
		}
	}
	if (compilationUnit != null) {
		CompilationUnitDeclaration result = super.endParse(act);
		return result;
	} else {
		return null;
	}		
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
public TypeReference getTypeReference(int dim) {
	/* build a Reference on a variable that may be qualified or not
	 * This variable is a type reference and dim will be its dimensions
	 */
	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		if (dim == 0) {
			SingleTypeReference ref = 
				new SingleTypeReference(
					identifierStack[identifierPtr], 
					identifierPositionStack[identifierPtr--]);
			if (reportReferenceInfo) {
				requestor.acceptTypeReference(ref.token, ref.sourceStart);
			}
			return ref;
		} else {
			ArrayTypeReference ref = 
				new ArrayTypeReference(
					identifierStack[identifierPtr], 
					dim, 
					identifierPositionStack[identifierPtr--]); 
			ref.sourceEnd = endPosition;
			if (reportReferenceInfo) {
				requestor.acceptTypeReference(ref.token, ref.sourceStart);
			}
			return ref;
		}
	} else {
		if (length < 0) { //flag for precompiled type reference on base types
			TypeReference ref = TypeReference.baseTypeReference(-length, dim);
			ref.sourceStart = intStack[intPtr--];
			if (dim == 0) {
				ref.sourceEnd = intStack[intPtr--];
			} else {
				intPtr--; // no need to use this position as it is an array
				ref.sourceEnd = endPosition;
			}
			if (reportReferenceInfo){
					requestor.acceptTypeReference(ref.getTypeName(), ref.sourceStart, ref.sourceEnd);
			}
			return ref;
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
				QualifiedTypeReference ref = new QualifiedTypeReference(tokens, positions);
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart, ref.sourceEnd);
				}
				return ref;
			} else {
				ArrayQualifiedTypeReference ref = 
					new ArrayQualifiedTypeReference(tokens, dim, positions); 
				ref.sourceEnd = endPosition;					
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart, ref.sourceEnd);
				}
				return ref;
			}
		}
	}
}
public NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		SingleNameReference ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	} else {
		//Qualified variable reference
		char[][] tokens = new char[length][];
		identifierPtr -= length;
		System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
		QualifiedNameReference ref = 
			new QualifiedNameReference(
				tokens, 
				(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
				(int) identifierPositionStack[identifierPtr + length]); // sourceEnd
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	}
}
public NameReference getUnspecifiedReferenceOptimized() {
	/* build a (unspecified) NameReference which may be qualified
	The optimization occurs for qualified reference while we are
	certain in this case the last item of the qualified name is
	a field access. This optimization is IMPORTANT while it results
	that when a NameReference is build, the type checker should always
	look for that it is not a type reference */

	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		SingleNameReference ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
		ref.bits &= ~AstNode.RestrictiveFlagMASK;
		ref.bits |= LOCAL | FIELD;
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
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
	QualifiedNameReference ref = 
		new QualifiedNameReference(
			tokens, 
			(int) (identifierPositionStack[identifierPtr + 1] >> 32), 
	// sourceStart
	 (int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	ref.bits &= ~AstNode.RestrictiveFlagMASK;
	ref.bits |= LOCAL | FIELD;
	if (reportReferenceInfo) {
		this.addUnknownRef(ref);
	}
	return ref;
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
 * Update the bodyStart of the corresponding parse node
 */
public void notifySourceElementRequestor(CompilationUnitDeclaration parsedUnit) {
	if (parsedUnit == null) {
		// when we parse a single type member declaration the compilation unit is null, but we still
		// want to be able to notify the requestor on the created ast node
		if (astStack[0] instanceof AbstractMethodDeclaration) {
			notifySourceElementRequestor((AbstractMethodDeclaration) astStack[0]);
			return;
		}
		return;
	}
	// range check
	boolean isInRange = 
				scanner.initialPosition <= parsedUnit.sourceStart
				&& scanner.eofPosition >= parsedUnit.sourceEnd;
	
	if (reportReferenceInfo) {
		notifyAllUnknownReferences();
	}
	// collect the top level ast nodes
	int length = 0;
	AstNode[] nodes = null;
	if (sourceType == null){
		if (isInRange) {
			requestor.enterCompilationUnit();
		}
		ImportReference currentPackage = parsedUnit.currentPackage;
		ImportReference[] imports = parsedUnit.imports;
		TypeDeclaration[] types = parsedUnit.types;
		length = 
			(currentPackage == null ? 0 : 1) 
			+ (imports == null ? 0 : imports.length)
			+ (types == null ? 0 : types.length);
		nodes = new AstNode[length];
		int index = 0;
		if (currentPackage != null) {
			nodes[index++] = currentPackage;
		}
		if (imports != null) {
			for (int i = 0, max = imports.length; i < max; i++) {
				nodes[index++] = imports[i];
			}
		}
		if (types != null) {
			for (int i = 0, max = types.length; i < max; i++) {
				nodes[index++] = types[i];
			}
		}
	} else {
		TypeDeclaration[] types = parsedUnit.types;
		if (types != null) {
			length = types.length;
			nodes = new AstNode[length];
			for (int i = 0, max = types.length; i < max; i++) {
				nodes[i] = types[i];
			}
		}
	}
	
	// notify the nodes in the syntactical order
	if (nodes != null && length > 0) {
		quickSort(nodes, 0, length-1);
		for (int i=0;i<length;i++) {
			AstNode node = nodes[i];
			if (node instanceof ImportReference) {
				ImportReference importRef = (ImportReference)node;
				if (node == parsedUnit.currentPackage) {
					notifySourceElementRequestor(importRef, true);
				} else {
					notifySourceElementRequestor(importRef, false);
				}
			} else { // instanceof TypeDeclaration
				notifySourceElementRequestor((TypeDeclaration)node, sourceType == null);
			}
		}
	}
	
	if (sourceType == null){
		if (isInRange) {
			requestor.exitCompilationUnit(parsedUnit.sourceEnd);
		}
	}
}

private void notifyAllUnknownReferences() {
	for (int i = 0, max = this.unknownRefsCounter; i < max; i++) {
		NameReference nameRef = this.unknownRefs[i];
		if ((nameRef.bits & BindingIds.VARIABLE) != 0) {
			if ((nameRef.bits & BindingIds.TYPE) == 0) { 
				// variable but not type
				if (nameRef instanceof SingleNameReference) { 
					// local var or field
					requestor.acceptUnknownReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
				} else {
					// QualifiedNameReference
					// The last token is a field reference and the previous tokens are a type/variable references
					char[][] tokens = ((QualifiedNameReference) nameRef).tokens;
					int tokensLength = tokens.length;
					requestor.acceptFieldReference(tokens[tokensLength - 1], nameRef.sourceEnd - tokens[tokensLength - 1].length + 1);
					char[][] typeRef = new char[tokensLength - 1][];
					System.arraycopy(tokens, 0, typeRef, 0, tokensLength - 1);
					requestor.acceptUnknownReference(typeRef, nameRef.sourceStart, nameRef.sourceEnd - tokens[tokensLength - 1].length);
				}
			} else {
				// variable or type
				if (nameRef instanceof SingleNameReference) {
					requestor.acceptUnknownReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
				} else {
					//QualifiedNameReference
					requestor.acceptUnknownReference(((QualifiedNameReference) nameRef).tokens, nameRef.sourceStart, nameRef.sourceEnd);
				}
			}
		} else if ((nameRef.bits & BindingIds.TYPE) != 0) {
			if (nameRef instanceof SingleNameReference) {
				requestor.acceptTypeReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
			} else {
				// it is a QualifiedNameReference
				requestor.acceptTypeReference(((QualifiedNameReference) nameRef).tokens, nameRef.sourceStart, nameRef.sourceEnd);
			}
		}
	}
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void notifySourceElementRequestor(AbstractMethodDeclaration methodDeclaration) {

	// range check
	boolean isInRange = 
				scanner.initialPosition <= methodDeclaration.declarationSourceStart
				&& scanner.eofPosition >= methodDeclaration.declarationSourceEnd;

	if (methodDeclaration.isClinit()) {
		this.visitIfNeeded(methodDeclaration);
		return;
	}

	if (methodDeclaration.isDefaultConstructor()) {
		if (reportReferenceInfo) {
			ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) methodDeclaration;
			ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
			if (constructorCall != null) {
				switch(constructorCall.accessMode) {
					case ExplicitConstructorCall.This :
						requestor.acceptConstructorReference(
							typeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
					case ExplicitConstructorCall.Super :
					case ExplicitConstructorCall.ImplicitSuper :					
						requestor.acceptConstructorReference(
							superTypeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
				}
			}
		}	
		return;	
	}	
	char[][] argumentTypes = null;
	char[][] argumentNames = null;
	Argument[] arguments = methodDeclaration.arguments;
	if (arguments != null) {
		int argumentLength = arguments.length;
		argumentTypes = new char[argumentLength][];
		argumentNames = new char[argumentLength][];
		for (int i = 0; i < argumentLength; i++) {
			argumentTypes[i] = returnTypeName(arguments[i].type);
			argumentNames[i] = arguments[i].name;
		}
	}
	char[][] thrownExceptionTypes = null;
	TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
	if (thrownExceptions != null) {
		int thrownExceptionLength = thrownExceptions.length;
		thrownExceptionTypes = new char[thrownExceptionLength][];
		for (int i = 0; i < thrownExceptionLength; i++) {
			thrownExceptionTypes[i] = 
				CharOperation.concatWith(thrownExceptions[i].getTypeName(), '.'); 
		}
	}
	// by default no selector end position
	int selectorSourceEnd = -1;
	if (methodDeclaration.isConstructor()) {
		if (methodDeclaration instanceof SourceConstructorDeclaration) {
			selectorSourceEnd = 
				((SourceConstructorDeclaration) methodDeclaration).selectorSourceEnd; 
		}
		if (isInRange){
			requestor.enterConstructor(
				methodDeclaration.declarationSourceStart, 
				methodDeclaration.modifiers, 
				methodDeclaration.selector, 
				methodDeclaration.sourceStart, 
				selectorSourceEnd, 
				argumentTypes, 
				argumentNames, 
				thrownExceptionTypes);
		}
		if (reportReferenceInfo) {
			ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) methodDeclaration;
			ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
			if (constructorCall != null) {
				switch(constructorCall.accessMode) {
					case ExplicitConstructorCall.This :
						requestor.acceptConstructorReference(
							typeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
					case ExplicitConstructorCall.Super :
					case ExplicitConstructorCall.ImplicitSuper :
						requestor.acceptConstructorReference(
							superTypeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
				}
			}
		}
		this.visitIfNeeded(methodDeclaration);
		if (isInRange){
			requestor.exitConstructor(methodDeclaration.declarationSourceEnd);
		}
		return;
	}
	if (methodDeclaration instanceof SourceMethodDeclaration) {
		selectorSourceEnd = 
			((SourceMethodDeclaration) methodDeclaration).selectorSourceEnd; 
	}
	if (isInRange){
		requestor.enterMethod(
			methodDeclaration.declarationSourceStart, 
			methodDeclaration.modifiers & AccJustFlag, 
			returnTypeName(((MethodDeclaration) methodDeclaration).returnType), 
			methodDeclaration.selector, 
			methodDeclaration.sourceStart, 
			selectorSourceEnd, 
			argumentTypes, 
			argumentNames, 
			thrownExceptionTypes); 
	}		
	this.visitIfNeeded(methodDeclaration);

	if (isInRange){	
		requestor.exitMethod(methodDeclaration.declarationSourceEnd);
	}
}
/*
* Update the bodyStart of the corresponding parse node
*/
public void notifySourceElementRequestor(FieldDeclaration fieldDeclaration) {
	
	// range check
	boolean isInRange = 
				scanner.initialPosition <= fieldDeclaration.declarationSourceStart
				&& scanner.eofPosition >= fieldDeclaration.declarationSourceEnd;

	if (fieldDeclaration.isField()) {
		int fieldEndPosition = fieldDeclaration.declarationSourceEnd;
		if (fieldDeclaration instanceof SourceFieldDeclaration) {
			fieldEndPosition = ((SourceFieldDeclaration) fieldDeclaration).fieldEndPosition;
			if (fieldEndPosition == 0) {
				// use the declaration source end by default
				fieldEndPosition = fieldDeclaration.declarationSourceEnd;
			}
		}
		if (isInRange) {
			requestor.enterField(
				fieldDeclaration.declarationSourceStart, 
				fieldDeclaration.modifiers & AccJustFlag, 
				returnTypeName(fieldDeclaration.type), 
				fieldDeclaration.name, 
				fieldDeclaration.sourceStart, 
				fieldDeclaration.sourceEnd); 
		}
		this.visitIfNeeded(fieldDeclaration);
		if (isInRange){
			requestor.exitField(fieldEndPosition);
		}

	} else {
		if (isInRange){
			requestor.enterInitializer(
				fieldDeclaration.declarationSourceStart,
				fieldDeclaration.modifiers); 
		}
		this.visitIfNeeded((Initializer)fieldDeclaration);
		if (isInRange){
			requestor.exitInitializer(fieldDeclaration.declarationSourceEnd);
		}
	}
}
public void notifySourceElementRequestor(
	ImportReference importReference, 
	boolean isPackage) {
	if (isPackage) {
		requestor.acceptPackage(
			importReference.declarationSourceStart, 
			importReference.declarationSourceEnd, 
			CharOperation.concatWith(importReference.getImportName(), '.')); 
	} else {
		requestor.acceptImport(
			importReference.declarationSourceStart, 
			importReference.declarationSourceEnd, 
			CharOperation.concatWith(importReference.getImportName(), '.'), 
			importReference.onDemand); 
	}
}
public void notifySourceElementRequestor(TypeDeclaration typeDeclaration, boolean notifyTypePresence) {
	
	// range check
	boolean isInRange = 
				scanner.initialPosition <= typeDeclaration.declarationSourceStart
				&& scanner.eofPosition >= typeDeclaration.declarationSourceEnd;
	
	FieldDeclaration[] fields = typeDeclaration.fields;
	AbstractMethodDeclaration[] methods = typeDeclaration.methods;
	MemberTypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
	int fieldCount = fields == null ? 0 : fields.length;
	int methodCount = methods == null ? 0 : methods.length;
	int memberTypeCount = memberTypes == null ? 0 : memberTypes.length;
	int fieldIndex = 0;
	int methodIndex = 0;
	int memberTypeIndex = 0;
	boolean isInterface = typeDeclaration.isInterface();

	if (notifyTypePresence){
		char[][] interfaceNames = null;
		int superInterfacesLength = 0;
		TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			superInterfacesLength = superInterfaces.length;
			interfaceNames = new char[superInterfacesLength][];
		} else {
			if (typeDeclaration instanceof AnonymousLocalTypeDeclaration) {
				// see PR 3442
				QualifiedAllocationExpression alloc = ((AnonymousLocalTypeDeclaration)typeDeclaration).allocation;
				if (alloc != null && alloc.type != null) {
					superInterfaces = new TypeReference[] { ((AnonymousLocalTypeDeclaration)typeDeclaration).allocation.type};
					superInterfacesLength = 1;
					interfaceNames = new char[1][];
				}
			}
		}
		if (superInterfaces != null) {
			for (int i = 0; i < superInterfacesLength; i++) {
				interfaceNames[i] = 
					CharOperation.concatWith(superInterfaces[i].getTypeName(), '.'); 
			}
		}
		if (isInterface) {
			if (isInRange){
				requestor.enterInterface(
					typeDeclaration.declarationSourceStart, 
					typeDeclaration.modifiers & AccJustFlag, 
					typeDeclaration.name, 
					typeDeclaration.sourceStart, 
					typeDeclaration.sourceEnd, 
					interfaceNames);
			}
			if (nestedTypeIndex == typeNames.length) {
				// need a resize
				System.arraycopy(typeNames, 0, (typeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
				System.arraycopy(superTypeNames, 0, (superTypeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
			}
			typeNames[nestedTypeIndex] = typeDeclaration.name;
			superTypeNames[nestedTypeIndex++] = JAVA_LANG_OBJECT;
		} else {
			TypeReference superclass = typeDeclaration.superclass;
			if (superclass == null) {
				if (isInRange){
					requestor.enterClass(
						typeDeclaration.declarationSourceStart, 
						typeDeclaration.modifiers, 
						typeDeclaration.name, 
						typeDeclaration.sourceStart, 
						typeDeclaration.sourceEnd, 
						null, 
						interfaceNames); 
				}
			} else {
				if (isInRange){
					requestor.enterClass(
						typeDeclaration.declarationSourceStart, 
						typeDeclaration.modifiers, 
						typeDeclaration.name, 
						typeDeclaration.sourceStart, 
						typeDeclaration.sourceEnd, 
						CharOperation.concatWith(superclass.getTypeName(), '.'), 
						interfaceNames); 
				}
			}
			if (nestedTypeIndex == typeNames.length) {
				// need a resize
				System.arraycopy(typeNames, 0, (typeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
				System.arraycopy(superTypeNames, 0, (superTypeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
			}
			typeNames[nestedTypeIndex] = typeDeclaration.name;
			superTypeNames[nestedTypeIndex++] = superclass == null ? JAVA_LANG_OBJECT : CharOperation.concatWith(superclass.getTypeName(), '.');
		}
	}
	while ((fieldIndex < fieldCount)
		|| (memberTypeIndex < memberTypeCount)
		|| (methodIndex < methodCount)) {
		FieldDeclaration nextFieldDeclaration = null;
		AbstractMethodDeclaration nextMethodDeclaration = null;
		TypeDeclaration nextMemberDeclaration = null;

		int position = Integer.MAX_VALUE;
		int nextDeclarationType = -1;
		if (fieldIndex < fieldCount) {
			nextFieldDeclaration = fields[fieldIndex];
			if (nextFieldDeclaration.declarationSourceStart < position) {
				position = nextFieldDeclaration.declarationSourceStart;
				nextDeclarationType = 0; // FIELD
			}
		}
		if (methodIndex < methodCount) {
			nextMethodDeclaration = methods[methodIndex];
			if (nextMethodDeclaration.declarationSourceStart < position) {
				position = nextMethodDeclaration.declarationSourceStart;
				nextDeclarationType = 1; // METHOD
			}
		}
		if (memberTypeIndex < memberTypeCount) {
			nextMemberDeclaration = memberTypes[memberTypeIndex];
			if (nextMemberDeclaration.declarationSourceStart < position) {
				position = nextMemberDeclaration.declarationSourceStart;
				nextDeclarationType = 2; // MEMBER
			}
		}
		switch (nextDeclarationType) {
			case 0 :
				fieldIndex++;
				notifySourceElementRequestor(nextFieldDeclaration);
				break;
			case 1 :
				methodIndex++;
				notifySourceElementRequestor(nextMethodDeclaration);
				break;
			case 2 :
				memberTypeIndex++;
				notifySourceElementRequestor(nextMemberDeclaration, true);
		}
	}
	if (notifyTypePresence){
		if (isInRange){
			if (isInterface) {
				requestor.exitInterface(typeDeclaration.declarationSourceEnd);
			} else {
				requestor.exitClass(typeDeclaration.declarationSourceEnd);
			}
		}
		nestedTypeIndex--;
	}
}
public void parseCompilationUnit(
	ICompilationUnit unit, 
	int start, 
	int end, 
	boolean needReferenceInfo) {

	reportReferenceInfo = needReferenceInfo;
	boolean old = diet;
	if (needReferenceInfo) {
		unknownRefs = new NameReference[10];
		unknownRefsCounter = 0;
	}
	try {
		diet = true;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = parse(unit, compilationUnitResult, start, end);
		if (needReferenceInfo){
			diet = false;
			this.getMethodBodies(parsedUnit);
		}		
		this.scanner.resetTo(start, end);
		notifySourceElementRequestor(parsedUnit);
	} catch (AbortCompilation e) {
	} finally {
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(scanner.getLineEnds());
		}
		diet = old;
	}
}
public void parseCompilationUnit(
	ICompilationUnit unit, 
	boolean needReferenceInfo) {
	boolean old = diet;
	if (needReferenceInfo) {
		unknownRefs = new NameReference[10];
		unknownRefsCounter = 0;
	}
		
	try {
/*		diet = !needReferenceInfo;
		reportReferenceInfo = needReferenceInfo;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0);
		parse(unit, compilationUnitResult);		
*/		diet = true;
		reportReferenceInfo = needReferenceInfo;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = parse(unit, compilationUnitResult);
		int initialStart = this.scanner.initialPosition;
		int initialEnd = this.scanner.eofPosition;
		if (needReferenceInfo){
			diet = false;
			this.getMethodBodies(parsedUnit);
		}
		this.scanner.resetTo(initialStart, initialEnd);
		notifySourceElementRequestor(parsedUnit);
	} catch (AbortCompilation e) {
	} finally {
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(scanner.getLineEnds());
		}
		diet = old;
	}
}
public void parseTypeMemberDeclarations(
	ISourceType sourceType, 
	ICompilationUnit sourceUnit, 
	int start, 
	int end, 
	boolean needReferenceInfo) {
	boolean old = diet;
	if (needReferenceInfo) {
		unknownRefs = new NameReference[10];
		unknownRefsCounter = 0;
	}
	
	try {
		diet = !needReferenceInfo;
		reportReferenceInfo = needReferenceInfo;
		CompilationResult compilationUnitResult = 
			new CompilationResult(sourceUnit, 0, 0, this.options.maxProblemsPerUnit); 
		CompilationUnitDeclaration unit = 
			SourceTypeConverter.buildCompilationUnit(
				new ISourceType[]{sourceType}, 
				false,
				false, 
				problemReporter(), 
				compilationUnitResult); 
		if ((unit == null) || (unit.types == null) || (unit.types.length != 1))
			return;
		this.sourceType = sourceType;
		try {
			/* automaton initialization */
			initialize();
			goForClassBodyDeclarations();
			/* scanner initialization */
			scanner.setSource(sourceUnit.getContents());
			scanner.resetTo(start, end);
			/* unit creation */
			referenceContext = compilationUnit = unit;
			/* initialize the astStacl */
			// the compilationUnitDeclaration should contain exactly one type
			pushOnAstStack(unit.types[0]);
			/* run automaton */
			parse();
			notifySourceElementRequestor(unit);
		} finally {
			unit = compilationUnit;
			compilationUnit = null; // reset parser
		}
	} catch (AbortCompilation e) {
	} finally {
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(scanner.getLineEnds());
		}
		diet = old;
	}
}

public void parseTypeMemberDeclarations(
	char[] contents, 
	int start, 
	int end) {

	boolean old = diet;
	
	try {
		diet = true;

		/* automaton initialization */
		initialize();
		goForClassBodyDeclarations();
		/* scanner initialization */
		scanner.setSource(contents);
		scanner.recordLineSeparator = false;
		scanner.resetTo(start, end);

		/* unit creation */
		referenceContext = null;

		/* initialize the astStacl */
		// the compilationUnitDeclaration should contain exactly one type
		/* run automaton */
		parse();
		notifySourceElementRequestor((CompilationUnitDeclaration)null);
	} catch (AbortCompilation e) {
	} finally {
		diet = old;
	}
}
/**
 * Sort the given ast nodes by their positions.
 */
private static void quickSort(AstNode[] sortedCollection, int left, int right) {
	int original_left = left;
	int original_right = right;
	AstNode mid = sortedCollection[ (left + right) / 2];
	do {
		while (sortedCollection[left].sourceStart < mid.sourceStart) {
			left++;
		}
		while (mid.sourceStart < sortedCollection[right].sourceStart) {
			right--;
		}
		if (left <= right) {
			AstNode tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSort(sortedCollection, original_left, right);
	}
	if (left < original_right) {
		quickSort(sortedCollection, left, original_right);
	}
}
/*
 * Answer a char array representation of the type name formatted like:
 * - type name + dimensions
 * Example:
 * "A[][]".toCharArray()
 * "java.lang.String".toCharArray()
 */
private char[] returnTypeName(TypeReference type) {
	if (type == null)
		return null;
	int dimension = type.dimensions();
	if (dimension != 0) {
		char[] dimensionsArray = new char[dimension * 2];
		for (int i = 0; i < dimension; i++) {
			dimensionsArray[i * 2] = '[';
			dimensionsArray[(i * 2) + 1] = ']';
		}
		return CharOperation.concat(
			CharOperation.concatWith(type.getTypeName(), '.'), 
			dimensionsArray); 
	}
	return CharOperation.concatWith(type.getTypeName(), '.');
}

public void addUnknownRef(NameReference nameRef) {
	if (this.unknownRefs.length == this.unknownRefsCounter) {
		// resize
		System.arraycopy(
			this.unknownRefs,
			0,
			(this.unknownRefs = new NameReference[this.unknownRefsCounter * 2]),
			0,
			this.unknownRefsCounter);
	}
	this.unknownRefs[this.unknownRefsCounter++] = nameRef;
}
private TypeReference typeReference(
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
			if (dim == 0)  {
				ref = new QualifiedTypeReference(tokens, positions);
			} else {
				ref = new ArrayQualifiedTypeReference(tokens, dim, positions);
				ref.sourceEnd = endPosition;
			}
		}
	};
	return ref;
}

private void visitIfNeeded(AbstractMethodDeclaration method) {
	if (this.localDeclarationVisitor != null 
		&& (method.bits & AstNode.HasLocalTypeMASK) != 0) {
			if (method.statements != null) {
				int statementsLength = method.statements.length;
				for (int i = 0; i < statementsLength; i++)
					method.statements[i].traverse(this.localDeclarationVisitor, method.scope);
			}
	}
}

private void visitIfNeeded(FieldDeclaration field) {
	if (this.localDeclarationVisitor != null 
		&& (field.bits & AstNode.HasLocalTypeMASK) != 0) {
			if (field.initialization != null) {
				field.initialization.traverse(this.localDeclarationVisitor, null);
			}
	}
}

private void visitIfNeeded(Initializer initializer) {
	if (this.localDeclarationVisitor != null 
		&& (initializer.bits & AstNode.HasLocalTypeMASK) != 0) {
			if (initializer.block != null) {
				initializer.block.traverse(this.localDeclarationVisitor, null);
			}
	}
}

protected void reportSyntaxError(int act, int currentKind, int stateStackTop) {
	if (compilationUnit == null) return;
	super.reportSyntaxError(act, currentKind,stateStackTop);
}

}
