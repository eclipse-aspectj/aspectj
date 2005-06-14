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

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.BCException;

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
		else if (onType instanceof ParameterizedTypeBinding) {
			ReferenceBinding rb = ((ParameterizedTypeBinding)onType).type;
			if (rb instanceof SourceTypeBinding) return (SourceTypeBinding)rb;
			else throw new BCException("In parameterized type "+onType+", can't handle reference binding "+rb);
		}
		throw new BCException("can't handle: " + onType);
	}

	public SourceTypeBinding invocationType() {
		return parent.enclosingSourceType();
	}
	
	public int addDepth() {
		return 0;
	}

	

}
