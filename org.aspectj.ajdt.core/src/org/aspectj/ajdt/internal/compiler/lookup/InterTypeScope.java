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


package org.aspectj.ajdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class InterTypeScope extends ClassScope {
	ReferenceBinding onType;

	public InterTypeScope(Scope parent, ReferenceBinding onType) {
		super(parent, null);
		referenceContext = new TypeDeclaration(null);
		referenceContext.binding = makeSourceTypeBinding(onType);
		this.onType = onType;
	}

	// this method depends on the fact that BinaryTypeBinding extends SourceTypeBinding
	private SourceTypeBinding makeSourceTypeBinding(ReferenceBinding onType) {
		if (onType instanceof SourceTypeBinding) return (SourceTypeBinding)onType;
		else throw new RuntimeException("can't handle: " + onType);
	}

	public SourceTypeBinding invocationType() {
		return parent.enclosingSourceType();
	}
	
	public int addDepth() {
		return 0;
	}

	

}
