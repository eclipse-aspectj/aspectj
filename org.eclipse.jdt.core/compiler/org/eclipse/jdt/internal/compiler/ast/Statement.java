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

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public abstract class Statement extends AstNode {
	
	/**
	 * Statement constructor comment.
	 */
	public Statement() {
		super();
	}
	
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
		return flowInfo;
	}
	
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
		throw new ShouldNotImplement(Util.bind("ast.missingStatement")); //$NON-NLS-1$
	}
	
	public boolean isEmptyBlock() {
		return false;
	}
	
	public boolean isValidJavaStatement() {
		//the use of this method should be avoid in most cases
		//and is here mostly for documentation purpose.....
		//while the parser is responsable for creating
		//welled formed expression statement, which results
		//in the fact that java-non-semantic-expression-used-as-statement
		//should not be parsable...thus not being built.
		//It sounds like the java grammar as help the compiler job in removing
		//-by construction- some statement that would have no effect....
		//(for example all expression that may do side-effects are valid statement
		// -this is an appromative idea.....-)

		return true;
	}
	
	public void resolve(BlockScope scope) {
	}
	
	public Constant resolveCase(
		BlockScope scope,
		TypeBinding testType,
		SwitchStatement switchStatement) {
		// statement within a switch that are not case are treated as normal statement.... 

		resolve(scope);
		return null;
	}
	
	public void resetStateForCodeGeneration() {
	}
	
	/**
	 * INTERNAL USE ONLY.
	 * Do nothing by default. This is used to redirect inter-statements jumps.
	 */
	public void branchChainTo(Label label) {
	}
}