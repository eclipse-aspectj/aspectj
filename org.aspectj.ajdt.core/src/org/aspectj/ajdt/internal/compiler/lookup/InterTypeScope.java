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

import java.util.List;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.aspectj.weaver.BCException;

public class InterTypeScope extends ClassScope {
	ReferenceBinding onType;
	List aliases;

	public InterTypeScope(Scope parent, ReferenceBinding onType) {
		super(parent, null);
		referenceContext = new TypeDeclaration(null);
		referenceContext.binding = makeSourceTypeBinding(onType);
		this.onType = onType;
	}

	public InterTypeScope(Scope parent, ReferenceBinding rb, List list) {
		this(parent,rb);
		this.aliases = list;
	}

	// this method depends on the fact that BinaryTypeBinding extends SourceTypeBinding
	private SourceTypeBinding makeSourceTypeBinding(ReferenceBinding onType) {
		if (onType instanceof SourceTypeBinding) return (SourceTypeBinding)onType;
		else if (onType instanceof ParameterizedTypeBinding) {
			ReferenceBinding rb = ((ParameterizedTypeBinding)onType).type;
			if (rb instanceof SourceTypeBinding) return (SourceTypeBinding)rb;
			else throw new BCException("In parameterized type "+onType+", can't handle reference binding "+rb);
		} else if (onType instanceof ProblemReferenceBinding) { 
			return null;
		}
		
		throw new BCException("can't handle: " + onType);
	}

	public SourceTypeBinding invocationType() {
		return parent.enclosingSourceType();
	}
	
	public int addDepth() {
		return 0;
	}
	

	public TypeVariableBinding findTypeVariable(char[] name, SourceTypeBinding sourceType) {
		int aliased = (aliases==null?-1:aliases.indexOf(new String(name)));
		if (aliased!=-1) {
			if (aliased>sourceType.typeVariables.length || sourceType.typeVariables.length==0) {
				TypeVariableBinding tvb = new TypeVariableBinding("fake".toCharArray(),null,0);
				return tvb;
				// error is going to be reported by someone else!
			}
			return sourceType.typeVariables()[aliased];
		} else {
		    return sourceType.getTypeVariable(name);
		}
	}
	

}
