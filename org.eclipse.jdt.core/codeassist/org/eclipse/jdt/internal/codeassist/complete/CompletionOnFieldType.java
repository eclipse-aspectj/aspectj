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
 * Completion node build by the parser in any case it was intending to
 * reduce an type reference located as a potential return type for a class
 * member, containing the cursor location.
 * This node is only a fake-field wrapper of the actual completion node
 * which is accessible as the fake-field type.
 * e.g.
 *
 *	class X {
 *    Obj[cursor]
 *  }
 *
 *	---> class X {
 *         <CompleteOnType:Obj>;
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the allocation expression are all the arguments defined
 * before the cursor.
 */
 
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnFieldType extends FieldDeclaration {
	public boolean isLocalVariable;
	
public CompletionOnFieldType(TypeReference type, boolean isLocalVariable){
	super();
	this.sourceStart = type.sourceStart;
	this.sourceEnd = type.sourceEnd;
	this.type = type;
	this.name = NoChar;
	this.isLocalVariable = isLocalVariable;
}
public TypeBinding getTypeBinding(Scope scope) {
	if(type instanceof CompletionOnSingleTypeReference)
		throw new CompletionNodeFound(this, scope);
	else // handle the qualified type ref directly
		return type.getTypeBinding(scope);
}
public String toString(int tab) {

	return type.toString(tab);
}
}
