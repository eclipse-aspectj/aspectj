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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Internal method structure for parsing recovery 
 */

public class RecoveredMethod extends RecoveredElement implements CompilerModifiers, ITerminalSymbols, BaseTypes {

	public AbstractMethodDeclaration methodDeclaration;

	public RecoveredType[] localTypes;
	public int localTypeCount;

	public RecoveredBlock methodBody;
	public boolean discardBody = true;

public RecoveredMethod(AbstractMethodDeclaration methodDeclaration, RecoveredElement parent, int bracketBalance, Parser parser){
	super(parent, bracketBalance, parser);
	this.methodDeclaration = methodDeclaration;
	this.foundOpeningBrace = !bodyStartsAtHeaderEnd();
	if(this.foundOpeningBrace) {
		this.bracketBalance++;
	}
}
/*
 * Record a nested block declaration
 */
public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalance) {

	/* default behavior is to delegate recording to parent if any,
	do not consider elements passed the known end (if set)
	it must be belonging to an enclosing element 
	*/
	if (methodDeclaration.declarationSourceEnd > 0
		&& nestedBlockDeclaration.sourceStart
			> methodDeclaration.declarationSourceEnd){
				if (this.parent == null){
					return this; // ignore
				} else {
					return this.parent.add(nestedBlockDeclaration, bracketBalance);
				}
	}
	/* consider that if the opening brace was not found, it is there */
	if (!foundOpeningBrace){
		foundOpeningBrace = true;
		this.bracketBalance++;
	}

	methodBody = new RecoveredBlock(nestedBlockDeclaration, this, bracketBalance);
	if (nestedBlockDeclaration.sourceEnd == 0) return methodBody;
	return this;
}
/*
 * Record a field declaration
 */
public RecoveredElement add(FieldDeclaration fieldDeclaration, int bracketBalance) {

	/* local variables inside method can only be final and non void */
	char[][] fieldTypeName; 
	if ((fieldDeclaration.modifiers & ~AccFinal) != 0 // local var can only be final 
		|| (fieldDeclaration.type == null) // initializer
		|| ((fieldTypeName = fieldDeclaration.type.getTypeName()).length == 1 // non void
			&& CharOperation.equals(fieldTypeName[0], VoidBinding.sourceName()))){ 

		if (this.parent == null){
			return this; // ignore
		} else {
			this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(fieldDeclaration.declarationSourceStart - 1));
			return this.parent.add(fieldDeclaration, bracketBalance);
		}
	}
	/* default behavior is to delegate recording to parent if any,
	do not consider elements passed the known end (if set)
	it must be belonging to an enclosing element 
	*/
	if (methodDeclaration.declarationSourceEnd > 0
		&& fieldDeclaration.declarationSourceStart
			> methodDeclaration.declarationSourceEnd){
		if (this.parent == null){
			return this; // ignore
		} else {
			return this.parent.add(fieldDeclaration, bracketBalance);
		}
	}
	/* consider that if the opening brace was not found, it is there */
	if (!foundOpeningBrace){
		foundOpeningBrace = true;
		this.bracketBalance++;
	}
	// still inside method, treat as local variable
	return this; // ignore
}
/*
 * Record a local declaration - regular method should have been created a block body
 */
public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalance) {

	/* local variables inside method can only be final and non void */
/*	
	char[][] localTypeName; 
	if ((localDeclaration.modifiers & ~AccFinal) != 0 // local var can only be final 
		|| (localDeclaration.type == null) // initializer
		|| ((localTypeName = localDeclaration.type.getTypeName()).length == 1 // non void
			&& CharOperation.equals(localTypeName[0], VoidBinding.sourceName()))){ 

		if (this.parent == null){
			return this; // ignore
		} else {
			this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(localDeclaration.declarationSourceStart - 1));
			return this.parent.add(localDeclaration, bracketBalance);
		}
	}
*/
	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (methodDeclaration.declarationSourceEnd != 0 
		&& localDeclaration.declarationSourceStart > methodDeclaration.declarationSourceEnd){
			
		if (this.parent == null) {
			return this; // ignore
		} else {
			return this.parent.add(localDeclaration, bracketBalance);
		}
	}
	if (methodBody == null){
		Block block = new Block(0);
		block.sourceStart = methodDeclaration.bodyStart;
		RecoveredElement currentBlock = this.add(block, 1);
		if (this.bracketBalance > 0){
			for (int i = 0; i < this.bracketBalance - 1; i++){
				currentBlock = currentBlock.add(new Block(0), 1);
			}
			this.bracketBalance = 1;
		}
		return currentBlock.add(localDeclaration, bracketBalance);
	}
	return methodBody.add(localDeclaration, bracketBalance, true);
}
/*
 * Record a statement - regular method should have been created a block body
 */
public RecoveredElement add(Statement statement, int bracketBalance) {

	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (methodDeclaration.declarationSourceEnd != 0 
		&& statement.sourceStart > methodDeclaration.declarationSourceEnd){

		if (this.parent == null) {
			return this; // ignore
		} else {
			return this.parent.add(statement, bracketBalance);
		}
	}
	if (methodBody == null){
		Block block = new Block(0);
		block.sourceStart = methodDeclaration.bodyStart;
		RecoveredElement currentBlock = this.add(block, 1);
		if (this.bracketBalance > 0){
			for (int i = 0; i < this.bracketBalance - 1; i++){
				currentBlock = currentBlock.add(new Block(0), 1);
			}
			this.bracketBalance = 1;
		}
		return currentBlock.add(statement, bracketBalance);
	}
	return methodBody.add(statement, bracketBalance, true);	
}
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalance) {

	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (methodDeclaration.declarationSourceEnd != 0 
		&& typeDeclaration.declarationSourceStart > methodDeclaration.declarationSourceEnd){
			
		if (this.parent == null) {
			return this; // ignore
		} else {
			return this.parent.add(typeDeclaration, bracketBalance);
		}
	}
	if (typeDeclaration instanceof LocalTypeDeclaration){
		if (methodBody == null){
			Block block = new Block(0);
			block.sourceStart = methodDeclaration.bodyStart;
			this.add(block, 1);
		}
		return methodBody.add(typeDeclaration, bracketBalance, true);	
	}	
	if (localTypes == null) {
		localTypes = new RecoveredType[5];
		localTypeCount = 0;
	} else {
		if (localTypeCount == localTypes.length) {
			System.arraycopy(
				localTypes, 
				0, 
				(localTypes = new RecoveredType[2 * localTypeCount]), 
				0, 
				localTypeCount); 
		}
	}
	RecoveredType element = new RecoveredType(typeDeclaration, this, bracketBalance);
	localTypes[localTypeCount++] = element;

	/* consider that if the opening brace was not found, it is there */
	if (!foundOpeningBrace){
		foundOpeningBrace = true;
		this.bracketBalance++;
	}
	return element;
}
public boolean bodyStartsAtHeaderEnd(){
	return methodDeclaration.bodyStart == methodDeclaration.sourceEnd+1;
}
/* 
 * Answer the associated parsed structure
 */
public AstNode parseTree(){
	return methodDeclaration;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.methodDeclaration.declarationSourceEnd;
}
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered method:\n"); //$NON-NLS-1$
	result.append(this.methodDeclaration.toString(tab + 1));
	if (this.localTypes != null) {
		for (int i = 0; i < this.localTypeCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.localTypes[i].toString(tab + 1));
		}
	}
	if (this.methodBody != null) {
		result.append("\n"); //$NON-NLS-1$
		result.append(this.methodBody.toString(tab + 1));
	}
	return result.toString();
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void updateBodyStart(int bodyStart){
	this.foundOpeningBrace = true;		
	this.methodDeclaration.bodyStart = bodyStart;
}
public AbstractMethodDeclaration updatedMethodDeclaration(){

	if (methodBody != null){
		Block block = methodBody.updatedBlock();
		if (block != null){
			methodDeclaration.statements = block.statements;

			/* first statement might be an explict constructor call destinated to a special slot */
			if (methodDeclaration.isConstructor()) {
				ConstructorDeclaration constructor = (ConstructorDeclaration)methodDeclaration;
				if (methodDeclaration.statements != null
					&& methodDeclaration.statements[0] instanceof ExplicitConstructorCall){
					constructor.constructorCall = (ExplicitConstructorCall)methodDeclaration.statements[0];
					int length = methodDeclaration.statements.length;
					System.arraycopy(
						methodDeclaration.statements, 
						1, 
						(methodDeclaration.statements = new Statement[length-1]),
						0,
						length-1);
					}
					if (constructor.constructorCall == null){ // add implicit constructor call
						constructor.constructorCall = SuperReference.implicitSuperConstructorCall();
					}
			}
		}
	}
	if (localTypeCount > 0) methodDeclaration.bits |= AstNode.HasLocalTypeMASK;
	return methodDeclaration;
}
/*
 * Update the corresponding parse node from parser state which
 * is about to disappear because of restarting recovery
 */
public void updateFromParserState(){

	if(this.bodyStartsAtHeaderEnd()){
		Parser parser = this.parser();
		/* might want to recover arguments or thrown exceptions */
		if (parser.listLength > 0 && parser.astLengthPtr > 0){ // awaiting interface type references
			/* has consumed the arguments - listed elements must be thrown exceptions */
			if (methodDeclaration.sourceEnd == parser.rParenPos) {
				
				// protection for bugs 15142
				int length = parser.astLengthStack[parser.astLengthPtr];
				int astPtr = parser.astPtr - length;
				boolean canConsume = astPtr >= 0;
				if(canConsume) {
					if((!(parser.astStack[astPtr] instanceof AbstractMethodDeclaration))) {
						canConsume = false;
					}
					for (int i = 1, max = length + 1; i < max; i++) {
						if(!(parser.astStack[astPtr + i ] instanceof TypeReference)) {
							canConsume = false;
						}
					}
				}
				if (canConsume){
					parser.consumeMethodHeaderThrowsClause(); 
					// will reset typeListLength to zero
					// thus this check will only be performed on first errorCheck after void foo() throws X, Y,
				} else {
					parser.listLength = 0;
				}
			} else {
				/* has not consumed arguments yet, listed elements must be arguments */
				if (parser.currentToken == TokenNameLPAREN || parser.currentToken == TokenNameSEMICOLON){
					/* if currentToken is parenthesis this last argument is a method/field signature */
					parser.astLengthStack[parser.astLengthPtr] --; 
					parser.astPtr --; 
					parser.listLength --;
					parser.currentToken = 0;
				}
				int argLength = parser.astLengthStack[parser.astLengthPtr];
				int argStart = parser.astPtr - argLength + 1;
				boolean needUpdateRParenPos = parser.rParenPos < parser.lParenPos; // 12387 : rParenPos will be used
				// to compute bodyStart, and thus used to set next checkpoint.
				int count;
				for (count = 0; count < argLength; count++){
					Argument argument = (Argument)parser.astStack[argStart+count];
					/* cannot be an argument if non final */
					char[][] argTypeName = argument.type.getTypeName();
					if ((argument.modifiers & ~AccFinal) != 0
						|| (argTypeName.length == 1
							&& CharOperation.equals(argTypeName[0], VoidBinding.sourceName()))){
						parser.astLengthStack[parser.astLengthPtr] = count; 
						parser.astPtr = argStart+count-1; 
						parser.listLength = count;
						parser.currentToken = 0;
						break;
					}
					if (needUpdateRParenPos) parser.rParenPos = argument.sourceEnd + 1;
				}
				if (parser.listLength > 0 && parser.astLengthPtr > 0){
					
					// protection for bugs 15142
					int length = parser.astLengthStack[parser.astLengthPtr];
					int astPtr = parser.astPtr - length;
					boolean canConsume = astPtr >= 0;
					if(canConsume) {
						if((!(parser.astStack[astPtr] instanceof AbstractMethodDeclaration))) {
							canConsume = false;
						}
						for (int i = 1, max = length + 1; i < max; i++) {
							if(!(parser.astStack[astPtr + i ] instanceof Argument)) {
								canConsume = false;
							}
						}
					}
					if(canConsume) {
						parser.consumeMethodHeaderParameters();
						/* fix-up positions, given they were updated against rParenPos, which did not get set */
						if (parser.currentElement == this){ // parameter addition might have added an awaiting (no return type) method - see 1FVXQZ4 */
							methodDeclaration.sourceEnd = methodDeclaration.arguments[methodDeclaration.arguments.length-1].sourceEnd;
							methodDeclaration.bodyStart = methodDeclaration.sourceEnd+1;
							parser.lastCheckPoint = methodDeclaration.bodyStart;
						}
					}
				}
			}
		}
	}
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int braceEnd){

	/* in case the opening brace is close enough to the signature */
	if (bracketBalance == 0){
		/*
			if (parser.scanner.searchLineNumber(methodDeclaration.sourceEnd) 
				!= parser.scanner.searchLineNumber(braceEnd)){
		 */
		switch(parser().lastIgnoredToken){
			case -1 :
			case TokenNamethrows :
				break;
			default:
				this.foundOpeningBrace = true;				
				bracketBalance = 1; // pretend the brace was already there
		}
	}	
	return super.updateOnOpeningBrace(braceEnd);
}
public void updateParseTree(){
	this.updatedMethodDeclaration();
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int sourceEnd){
	if (this.methodDeclaration.declarationSourceEnd == 0) {
		this.methodDeclaration.declarationSourceEnd = sourceEnd;
		this.methodDeclaration.bodyEnd = sourceEnd;
	}
}
}
