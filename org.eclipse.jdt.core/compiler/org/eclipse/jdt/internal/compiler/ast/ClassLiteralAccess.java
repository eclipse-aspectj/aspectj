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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ClassLiteralAccess extends Expression {
	
	public TypeReference type;
	public TypeBinding targetType;
	FieldBinding syntheticField;

	public ClassLiteralAccess(int sourceEnd, TypeReference t) {
		type = t;
		this.sourceStart = t.sourceStart;
		this.sourceEnd = sourceEnd;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// if reachable, request the addition of a synthetic field for caching the class descriptor
		SourceTypeBinding sourceType =
			currentScope.outerMostMethodScope().enclosingSourceType();
		if (!(sourceType.isInterface()
			// no field generated in interface case (would'nt verify) see 1FHHEZL
			|| sourceType.isBaseType())) {
			syntheticField = sourceType.addSyntheticField(targetType, currentScope);
		}
		return flowInfo;
	}

	/**
	 * MessageSendDotClass code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
		int pc = codeStream.position;

		// in interface case, no caching occurs, since cannot make a cache field for interface
		if (valueRequired)
			codeStream.generateClassLiteralAccessForType(type.binding, syntheticField);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		if ((targetType = type.resolveType(scope)) == null)
			return null;

		if (targetType.isArrayType()
			&& ((ArrayBinding) targetType).leafComponentType == VoidBinding) {
			scope.problemReporter().cannotAllocateVoidArray(this);
			return null;
		}

		return scope.getJavaLangClass();
	}

	public String toStringExpression() {

		String s = ""; //$NON-NLS-1$
		s = s + type.toString(0) + ".class"; //$NON-NLS-1$
		return s;
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			type.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}