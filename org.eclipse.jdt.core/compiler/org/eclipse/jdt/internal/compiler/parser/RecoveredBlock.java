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

/**
 * Internal block structure for parsing recovery 
 */
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class RecoveredBlock extends RecoveredStatement implements CompilerModifiers, ITerminalSymbols, BaseTypes {

	public Block blockDeclaration;

	public RecoveredStatement[] statements;
	public int statementCount;

	public boolean preserveContent = false;
	public RecoveredLocalVariable pendingArgument;
public RecoveredBlock(Block block, RecoveredElement parent, int bracketBalance){
	super(block, parent, bracketBalance);
	this.blockDeclaration = block;
	this.foundOpeningBrace = true;
}
/*
 * Record a nested block declaration 
 */
public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalance) {

	/* do not consider a nested block starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (blockDeclaration.sourceEnd != 0 
		&& nestedBlockDeclaration.sourceStart > blockDeclaration.sourceEnd){
		return this.parent.add(nestedBlockDeclaration, bracketBalance);
	}
			
	RecoveredBlock element = new RecoveredBlock(nestedBlockDeclaration, this, bracketBalance);

	// if we have a pending Argument, promote it into the new block
	if (pendingArgument != null){
		element.attach(pendingArgument);
		pendingArgument = null;
	}
	this.attach(element);
	if (nestedBlockDeclaration.sourceEnd == 0) return element;
	return this;	
}
/*
 * Record a local declaration 
 */
public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalance) {
	return this.add(localDeclaration, bracketBalance, false);
}
/*
 * Record a local declaration 
 */
public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalance, boolean delegatedByParent) {

	/* local variables inside method can only be final and non void */
/*	
	char[][] localTypeName; 
	if ((localDeclaration.modifiers & ~AccFinal) != 0 // local var can only be final 
		|| (localDeclaration.type == null) // initializer
		|| ((localTypeName = localDeclaration.type.getTypeName()).length == 1 // non void
			&& CharOperation.equals(localTypeName[0], VoidBinding.sourceName()))){ 

		if (delegatedByParent){
			return this; //ignore
		} else {
			this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(localDeclaration.declarationSourceStart - 1));
			return this.parent.add(localDeclaration, bracketBalance);
		}
	}
*/	
		/* do not consider a local variable starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (blockDeclaration.sourceEnd != 0 
		&& localDeclaration.declarationSourceStart > blockDeclaration.sourceEnd){

		if (delegatedByParent){
			return this; //ignore
		} else {
			return this.parent.add(localDeclaration, bracketBalance);
		}
	}

	RecoveredLocalVariable element = new RecoveredLocalVariable(localDeclaration, this, bracketBalance);

	if (localDeclaration instanceof Argument){
		pendingArgument = element;
		return this;
	}
	
	this.attach(element);
	if (localDeclaration.declarationSourceEnd == 0) return element;
	return this;	
}
/*
 * Record a statement declaration 
 */
public RecoveredElement add(Statement statement, int bracketBalance) {
	return this.add(statement, bracketBalance, false);
}

/*
 * Record a statement declaration 
 */
public RecoveredElement add(Statement statement, int bracketBalance, boolean delegatedByParent) {

	/* do not consider a nested block starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (blockDeclaration.sourceEnd != 0 
		&& statement.sourceStart > blockDeclaration.sourceEnd){
			
		if (delegatedByParent){
			return this; //ignore
		} else {
			return this.parent.add(statement, bracketBalance);
		}			
	}
			
	RecoveredStatement element = new RecoveredStatement(statement, this, bracketBalance);
	this.attach(element);
	if (statement.sourceEnd == 0) return element;
	return this;	
}
/*
 * Addition of a type to an initializer (act like inside method body)
 */
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalance) {
	return this.add(typeDeclaration, bracketBalance, false);
}
/*
 * Addition of a type to an initializer (act like inside method body)
 */
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalance, boolean delegatedByParent) {

	/* do not consider a type starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (blockDeclaration.sourceEnd != 0 
		&& typeDeclaration.declarationSourceStart > blockDeclaration.sourceEnd){
		if (delegatedByParent){
			return this; //ignore
		} else {
			return this.parent.add(typeDeclaration, bracketBalance);
		}
	}
			
	RecoveredStatement element = new RecoveredType(typeDeclaration, this, bracketBalance);
	this.attach(element);
	if (typeDeclaration.declarationSourceEnd == 0) return element;
	return this;
}
/*
 * Attach a recovered statement
 */
void attach(RecoveredStatement recoveredStatement) {

	if (statements == null) {
		statements = new RecoveredStatement[5];
		statementCount = 0;
	} else {
		if (statementCount == statements.length) {
			System.arraycopy(
				statements, 
				0, 
				(statements = new RecoveredStatement[2 * statementCount]), 
				0, 
				statementCount); 
		}
	}
	statements[statementCount++] = recoveredStatement;
}
/* 
 * Answer the associated parsed structure
 */
public AstNode parseTree(){
	return blockDeclaration;
}
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered block:\n"); //$NON-NLS-1$
	result.append(blockDeclaration.toString(tab + 1));
	if (this.statements != null) {
		for (int i = 0; i < this.statementCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.statements[i].toString(tab + 1));
		}
	}
	return result.toString();
}
/*
 * Rebuild a block from the nested structure which is in scope
 */
public Block updatedBlock(){

	// if block was not marked to be preserved or empty, then ignore it
	if (!preserveContent || statementCount == 0) return null;

	Statement[] updatedStatements = new Statement[statementCount];
	int updatedCount = 0;
	
	// only collect the non-null updated statements
	for (int i = 0; i < statementCount; i++){
		Statement updatedStatement = statements[i].updatedStatement();
		if (updatedStatement != null){
			updatedStatements[updatedCount++] = updatedStatement;
		}
	}
	if (updatedCount == 0) return null; // not interesting block

	// resize statement collection if necessary
	if (updatedCount != statementCount){
		blockDeclaration.statements = new Statement[updatedCount];
		System.arraycopy(updatedStatements, 0, blockDeclaration.statements, 0, updatedCount);
	} else {
		blockDeclaration.statements = updatedStatements;
	}

	return blockDeclaration;
}
/*
 * Rebuild a statement from the nested structure which is in scope
 */
public Statement updatedStatement(){

	return this.updatedBlock();
}
/*
 * A closing brace got consumed, might have closed the current element,
 * in which case both the currentElement is exited
 */
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
	if ((--bracketBalance <= 0) && (parent != null)){
		this.updateSourceEndIfNecessary(braceEnd);

		/* if the block is the method body, then it closes the method too */
		RecoveredMethod method = enclosingMethod();
		if (method != null && method.methodBody == this){
			return parent.updateOnClosingBrace(braceStart, braceEnd);
		}
		RecoveredInitializer initializer = enclosingInitializer();
		if (initializer != null && initializer.initializerBody == this){
			return parent.updateOnClosingBrace(braceStart, braceEnd);
		}
		return parent;
	}
	return this;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int currentPosition){

	// create a nested block
	Block block = new Block(0);
	block.sourceStart = parser().scanner.startPosition;
	return this.add(block, 1);
}
/*
 * Final update the corresponding parse node
 */
public void updateParseTree(){

	this.updatedBlock();
}
/*
 * Rebuild a flattened block from the nested structure which is in scope
 */
public Statement updateStatement(){

	// if block was closed or empty, then ignore it
	if (this.blockDeclaration.sourceEnd != 0 || statementCount == 0) return null;

	Statement[] updatedStatements = new Statement[statementCount];
	int updatedCount = 0;
	
	// only collect the non-null updated statements
	for (int i = 0; i < statementCount; i++){
		Statement updatedStatement = statements[i].updatedStatement();
		if (updatedStatement != null){
			updatedStatements[updatedCount++] = updatedStatement;
		}
	}
	if (updatedCount == 0) return null; // not interesting block

	// resize statement collection if necessary
	if (updatedCount != statementCount){
		blockDeclaration.statements = new Statement[updatedCount];
		System.arraycopy(updatedStatements, 0, blockDeclaration.statements, 0, updatedCount);
	} else {
		blockDeclaration.statements = updatedStatements;
	}

	return blockDeclaration;
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
		this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(fieldDeclaration.declarationSourceStart - 1));
		return this.parent.add(fieldDeclaration, bracketBalance);
	}
	
	/* do not consider a local variable starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (blockDeclaration.sourceEnd != 0 
		&& fieldDeclaration.declarationSourceStart > blockDeclaration.sourceEnd){
		return this.parent.add(fieldDeclaration, bracketBalance);
	}

	// ignore the added field, since indicates a local variable behind recovery point
	// which thus got parsed as a field reference. This can happen if restarting after
	// having reduced an assistNode to get the following context (see 1GEK7SG)
	return this;	
}
}
