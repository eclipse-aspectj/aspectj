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

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class Reference extends Expression  {
/**
 * BaseLevelReference constructor comment.
 */
public Reference() {
	super();
}
public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean isCompound) {
	throw new ShouldNotImplement(Util.bind("ast.variableShouldProvide")); //$NON-NLS-1$
}
public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}
public FieldBinding fieldBinding() {
	//this method should be sent one FIELD-tagged references
	//  (ref.bits & BindingIds.FIELD != 0)()
	return null ;
}
public void fieldStore(CodeStream codeStream, FieldBinding fieldBinding, MethodBinding syntheticWriteAccessor, boolean valueRequired) {

	if (fieldBinding.isStatic()) {
		if (valueRequired) {
			if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
				codeStream.dup2();
			} else {
				codeStream.dup();
			}
		}
		if (syntheticWriteAccessor == null) {
			codeStream.putstatic(fieldBinding);
		} else {
			codeStream.invokestatic(syntheticWriteAccessor);
		}
	} else { // Stack:  [owner][new field value]  ---> [new field value][owner][new field value]
		if (valueRequired) {
			if ((fieldBinding.type == LongBinding) || (fieldBinding.type == DoubleBinding)) {
				codeStream.dup2_x1();
			} else {
				codeStream.dup_x1();
			}
		}
		if (syntheticWriteAccessor == null) {
			codeStream.putfield(fieldBinding);
		} else {
			codeStream.invokestatic(syntheticWriteAccessor);
		}
	}
}
public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
	throw new ShouldNotImplement(Util.bind("ast.compoundPreShouldProvide")); //$NON-NLS-1$
}
public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
	throw new ShouldNotImplement(Util.bind("ast.compoundVariableShouldProvide")); //$NON-NLS-1$
}
public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
	throw new ShouldNotImplement(Util.bind("ast.postIncrShouldProvide")); //$NON-NLS-1$
}
}
