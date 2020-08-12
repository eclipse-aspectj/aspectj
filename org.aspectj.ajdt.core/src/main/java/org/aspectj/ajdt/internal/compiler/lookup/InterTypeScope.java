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

package org.aspectj.ajdt.internal.compiler.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.PlainPackageBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.aspectj.weaver.BCException;

public class InterTypeScope extends ClassScope {
	ReferenceBinding onType;
	List aliases;
	Map<TypeVariableBinding, String> /* real type variable > alias letter */usedAliases; // Used later when reconstructing the
																						// resolved member

	public InterTypeScope(Scope parent, ReferenceBinding onType) {
		super(parent, null);
		referenceContext = new TypeDeclaration(null);
		referenceContext.binding = makeSourceTypeBinding(onType);
		this.onType = onType;
	}

	public InterTypeScope(Scope parent, ReferenceBinding rb, List list) {
		this(parent, rb);
		this.aliases = list;
	}

	public String getAnyAliasForTypeVariableBinding(TypeVariableBinding tvb) {
		if (usedAliases == null)
			return null;
		return usedAliases.get(tvb);
	}

	// this method depends on the fact that BinaryTypeBinding extends SourceTypeBinding
	private SourceTypeBinding makeSourceTypeBinding(ReferenceBinding onType) {
		if (onType instanceof SourceTypeBinding)
			return (SourceTypeBinding) onType;
		else if (onType instanceof ParameterizedTypeBinding) {
			ReferenceBinding rb = ((ParameterizedTypeBinding) onType).type;
			if (rb instanceof SourceTypeBinding)
				return (SourceTypeBinding) rb;
			else
				throw new BCException("In parameterized type " + onType + ", can't handle reference binding " + rb);
		} else if (onType instanceof ProblemReferenceBinding) {
			return null;
		} else if (onType instanceof TypeVariableBinding) {
			// Problem will have already been reported, cant ITD on a type variable.
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
		if (sourceType == null) {
			return null;
		}
		String variableName = new String(name);
		int aliased = (aliases == null ? -1 : aliases.indexOf(variableName));
		if (aliased != -1) {
			if (aliased > sourceType.typeVariables.length || sourceType.typeVariables.length == 0) {
				TypeVariableBinding tvb = new TypeVariableBinding("fake".toCharArray(), null, 0,this.environment());
				tvb.superclass = getJavaLangObject();
				tvb.fPackage = new PlainPackageBinding(environment());
				return tvb;
				// error is going to be reported by someone else!
			}
			TypeVariableBinding tvb = sourceType.typeVariables()[aliased];
			tvb.fPackage = sourceType.fPackage;
			if (usedAliases == null)
				usedAliases = new HashMap<>();
			usedAliases.put(tvb, variableName);
			return tvb;
		} else {
			TypeVariableBinding variableBinding = sourceType.getTypeVariable(name);
			if (variableBinding == null) { // GENERICITDFIX
				// Inside generic aspect, might want the type var attached to us
				variableBinding = parent.findTypeVariable(name, ((ClassScope) parent).referenceContext.binding);
			}
			return variableBinding;
		}
	}

	public Map getRecoveryAliases() {
		return usedAliases;
	}

	@Override
	public boolean isInterTypeScope() {
		return true;
	}
}
