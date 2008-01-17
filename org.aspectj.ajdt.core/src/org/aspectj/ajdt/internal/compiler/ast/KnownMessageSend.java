/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.aspectj.org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.Constant;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class KnownMessageSend extends MessageSend {

	public KnownMessageSend(MethodBinding binding, Expression receiver, Expression[] arguments) {
		super();
		this.binding = this.codegenBinding = binding;
		this.arguments = arguments;
		this.receiver = receiver;
		this.selector = binding.selector;
		constant = Constant.NotAConstant;
	}

	public void manageSyntheticAccessIfNecessary(BlockScope currentScope) {
		return;
	}

	protected void resolveMethodBinding(
		BlockScope scope,
		TypeBinding[] argumentTypes) {
			// we've already resolved this
	}
	
	public String toStringExpression() {
		return "KnownMessageSend";
	}

	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {
		//System.out.println("about to generate: "  +this + " args: " + Arrays.asList(arguments));
		super.generateCode(currentScope, codeStream, valueRequired);
	}

}
