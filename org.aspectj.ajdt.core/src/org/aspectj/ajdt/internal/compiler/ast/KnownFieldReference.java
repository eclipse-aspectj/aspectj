/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class KnownFieldReference extends QualifiedNameReference {

	//XXX handle source locations
	public KnownFieldReference(FieldBinding binding, long pos) {
		super(new char[][] {binding.name},new long[1],  0, 0);
		this.binding = this.codegenBinding = binding;
		this.constant = Constant.NotAConstant;
		this.receiverType = this.actualReceiverType = 
			binding.declaringClass;
		
		this.bits = FIELD;
		//this.receiver = AstUtil.makeTypeReference(binding.declaringClass);
	}

	public TypeBinding resolveType(BlockScope scope) {
		return fieldBinding().type;
	}

}
