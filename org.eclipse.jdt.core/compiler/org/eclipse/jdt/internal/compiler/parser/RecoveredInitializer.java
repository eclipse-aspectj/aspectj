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
 * Internal initializer structure for parsing recovery 
 */
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class RecoveredInitializer extends RecoveredField implements CompilerModifiers, ITerminalSymbols, BaseTypes {

	public RecoveredType[] localTypes;
	public int localTypeCount;

	public RecoveredBlock initializerBody;	
public RecoveredInitializer(FieldDeclaration fieldDeclaration, RecoveredElement parent, int bracketBalance){
	this(fieldDeclaration, parent, bracketBalance, null);
}
public RecoveredInitializer(FieldDeclaration fieldDeclaration, RecoveredElement parent, int bracketBalance, Parser parser){
	super(fieldDeclaration, parent, bracketBalance, parser);
	this.foundOpeningBrace = true;
}
/*
 * Record a nested block declaration
 */
public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalance) {

	/* default behavior is to delegate recording to parent if any,
	do not consider elements passed the known end (if set)
	it must be belonging to an enclosing element 
	*/
	if (fieldDeclaration.declarationSourceEnd > 0
		&& nestedBlockDeclaration.sourceStart
			> fieldDeclaration.declarationSourceEnd){
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

	initializerBody = new RecoveredBlock(nestedBlockDeclaration, this, bracketBalance);
	if (nestedBlockDeclaration.sourceEnd == 0) return initializerBody;
	return this;
}
/*
 * Record a field declaration (act like inside method body)
 */
public RecoveredElement add(FieldDeclaration newFieldDeclaration, int bracketBalance) {

	/* local variables inside initializer can only be final and non void */
	char[][] fieldTypeName;
	if ((newFieldDeclaration.modifiers & ~AccFinal) != 0 /* local var can only be final */
		|| (newFieldDeclaration.type == null) // initializer
		|| ((fieldTypeName = newFieldDeclaration.type.getTypeName()).length == 1 // non void
			&& CharOperation.equals(fieldTypeName[0], VoidBinding.sourceName()))){ 
		if (this.parent == null) {
			return this; // ignore
		} else {
			this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(newFieldDeclaration.declarationSourceStart - 1));
			return this.parent.add(newFieldDeclaration, bracketBalance);
		}
	}

	/* default behavior is to delegate recording to parent if any,
	do not consider elements passed the known end (if set)
	it must be belonging to an enclosing element 
	*/
	if (this.fieldDeclaration.declarationSourceEnd > 0
		&& newFieldDeclaration.declarationSourceStart
			> this.fieldDeclaration.declarationSourceEnd){
		if (this.parent == null) {
			return this; // ignore
		} else {
			return this.parent.add(newFieldDeclaration, bracketBalance);
		}
	}
	// still inside initializer, treat as local variable
	return this; // ignore
}
/*
 * Record a local declaration - regular method should have been created a block body
 */
public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalance) {

	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (fieldDeclaration.declarationSourceEnd != 0 
		&& localDeclaration.declarationSourceStart > fieldDeclaration.declarationSourceEnd){
		if (parent == null) {
			return this; // ignore
		} else {
			return this.parent.add(localDeclaration, bracketBalance);
		}
	}
	/* method body should have been created */
	Block block = new Block(0);
	block.sourceStart = ((Initializer)fieldDeclaration).bodyStart;
	RecoveredElement element = this.add(block, 1);
	return element.add(localDeclaration, bracketBalance);	
}
/*
 * Record a statement - regular method should have been created a block body
 */
public RecoveredElement add(Statement statement, int bracketBalance) {

	/* do not consider a statement starting passed the initializer end (if set)
		it must be belonging to an enclosing type */
	if (fieldDeclaration.declarationSourceEnd != 0 
		&& statement.sourceStart > fieldDeclaration.declarationSourceEnd){
		if (parent == null) {
			return this; // ignore
		} else {
			return this.parent.add(statement, bracketBalance);
		}
	}
	/* initializer body should have been created */
	Block block = new Block(0);
	block.sourceStart = ((Initializer)fieldDeclaration).bodyStart;
	RecoveredElement element = this.add(block, 1);
	return element.add(statement, bracketBalance);	
}
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalance) {

	/* do not consider a type starting passed the type end (if set)
		it must be belonging to an enclosing type */
	if (fieldDeclaration.declarationSourceEnd != 0 
		&& typeDeclaration.declarationSourceStart > fieldDeclaration.declarationSourceEnd){
		if (parent == null) {
			return this; // ignore
		} else {
			return this.parent.add(typeDeclaration, bracketBalance);
		}
	}
	if (typeDeclaration instanceof LocalTypeDeclaration){
		/* method body should have been created */
		Block block = new Block(0);
		block.sourceStart = ((Initializer)fieldDeclaration).bodyStart;
		RecoveredElement element = this.add(block, 1);
		return element.add(typeDeclaration, bracketBalance);	
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
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered initializer:\n"); //$NON-NLS-1$
	result.append(this.fieldDeclaration.toString(tab + 1));
	if (this.initializerBody != null) {
		result.append("\n"); //$NON-NLS-1$
		result.append(this.initializerBody.toString(tab + 1));
	}
	return result.toString();
}
public FieldDeclaration updatedFieldDeclaration(){

	if (initializerBody != null){
		Block block = initializerBody.updatedBlock();
		if (block != null){
			((Initializer)fieldDeclaration).block = block;
		}
		if (this.localTypeCount > 0) fieldDeclaration.bits |= AstNode.HasLocalTypeMASK;

	}	
	if (fieldDeclaration.sourceEnd == 0){
		fieldDeclaration.sourceEnd = fieldDeclaration.declarationSourceEnd;
	}
	return fieldDeclaration;
}
/*
 * A closing brace got consumed, might have closed the current element,
 * in which case both the currentElement is exited
 */
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
	if ((--bracketBalance <= 0) && (parent != null)){
		this.updateSourceEndIfNecessary(braceEnd);
		return parent;
	}
	return this;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int currentPosition){
	bracketBalance++;
	return this; // request to restart
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int sourceEnd){
	if (this.fieldDeclaration.declarationSourceEnd == 0) {
		this.fieldDeclaration.sourceEnd = sourceEnd;
		this.fieldDeclaration.declarationSourceEnd = sourceEnd;
		this.fieldDeclaration.declarationEnd = sourceEnd;
	}
}
}
