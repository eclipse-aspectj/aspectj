/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class InterTypeScope extends ClassScope {

	public InterTypeScope(Scope parent, TypeBinding onType) {
		super(parent, null);
		if (onType instanceof SourceTypeBinding) {
			referenceContext = new TypeDeclaration(null);
			referenceContext.binding = ((SourceTypeBinding)onType);
		} else {
			throw new RuntimeException("unimplemented");
		}
		//System.out.println("encolsingSource: " + this.enclosingSourceType());
	}

	public FieldBinding findField(
		TypeBinding receiverType,
		char[] fieldName,
		InvocationSite invocationSite)
	{
		//System.out.println("find: " + new String(fieldName));
		return super.findField(receiverType, fieldName, invocationSite);
	}

	public SourceTypeBinding invocationType() {
		return parent.enclosingSourceType();
	}
	
	public int addDepth() {
		return 0;
	}

	

}
