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

import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * A this reference inside a code snippet denotes a remote
 * receiver object (i.e.&nbsp;the one of the context in the stack
 * frame)
 */
public class CodeSnippetThisReference extends ThisReference implements EvaluationConstants, InvocationSite {
	EvaluationContext evaluationContext;
	FieldBinding delegateThis;
	boolean isImplicit;
/**
 * CodeSnippetThisReference constructor comment.
 * @param s int
 * @param sourceEnd int
 */
public CodeSnippetThisReference(int s, int sourceEnd, EvaluationContext evaluationContext, boolean isImplicit) {
	super(s, sourceEnd);
	this.evaluationContext = evaluationContext;
	this.isImplicit = isImplicit;
}
protected boolean checkAccess(MethodScope methodScope) {
	// this/super cannot be used in constructor call
	if (evaluationContext.isConstructorCall) {
		methodScope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
		return false;
	}

	// static may not refer to this/super
	if (this.evaluationContext.declaringTypeName == null || evaluationContext.isStatic) {
		methodScope.problemReporter().errorThisSuperInStatic(this);
		return false;
	}
	return true;
}
public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
	int pc = codeStream.position;
	if (valueRequired) {
		codeStream.aload_0();
		codeStream.getfield(delegateThis);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}
public boolean isSuperAccess(){
	return false;
}
public boolean isTypeAccess(){
	return false;
}
public TypeBinding resolveType(BlockScope scope) {

	// implicit this
	constant = NotAConstant;
	TypeBinding snippetType = null;
	if (this.isImplicit || checkAccess(scope.methodScope())){
		snippetType = scope.enclosingSourceType();
	}
	if (snippetType == null) return null;
	
	delegateThis = scope.getField(snippetType, DELEGATE_THIS, this);
	if (delegateThis == null) return null; // internal error, field should have been found
	if (delegateThis.isValidBinding()) return delegateThis.type;
	return snippetType;
}
public void setActualReceiverType(ReferenceBinding receiverType) {
	// ignored
}
public void setDepth(int depth){
	// ignored
}
public void setFieldIndex(int index){
	// ignored
}

public String toStringExpression(){
	char[] declaringType = this.evaluationContext.declaringTypeName;
	return "(" + (declaringType == null ? "<NO DECLARING TYPE>" : new String(declaringType)) + ")this"; //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-1$
}
}
