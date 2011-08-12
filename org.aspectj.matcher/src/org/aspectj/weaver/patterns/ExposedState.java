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

package org.aspectj.weaver.patterns;

import java.util.Arrays;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Var;

public class ExposedState {
	public static final boolean[] NO_ERRONEOUS_VARS = new boolean[0];
	public Var[] vars;
	private boolean[] erroneousVars;
	private Expr aspectInstance;
	private UnresolvedType[] expectedVarTypes; // enables us to check that binding is occurring with the *right* types
	private ResolvedType concreteAspect;

	public ExposedState(int size) {
		super();
		if (size == 0) {
			vars = Var.NONE;
			erroneousVars = NO_ERRONEOUS_VARS;
		} else {
			vars = new Var[size];
			erroneousVars = new boolean[size];

		}
	}

	public ExposedState(Member signature) {
		// XXX there maybe something about target for non-static sigs
		this(signature.getParameterTypes().length);
		expectedVarTypes = new UnresolvedType[signature.getParameterTypes().length];
		if (expectedVarTypes.length > 0) {
			for (int i = 0; i < signature.getParameterTypes().length; i++) {
				expectedVarTypes[i] = signature.getParameterTypes()[i];
			}
		}

	}

	public boolean isFullySetUp() {
		for (int i = 0; i < vars.length; i++) {
			if (vars[i] == null)
				return false;
		}
		return true;
	}

	public void set(int i, Var var) {
		// check the type is OK if we can... these are the same rules as in matchesInstanceOf() processing
		if (expectedVarTypes != null) {
			ResolvedType expected = expectedVarTypes[i].resolve(var.getType().getWorld());
			if (!expected.equals(ResolvedType.OBJECT)) {
				if (!expected.isAssignableFrom(var.getType())) {
					if (!var.getType().isCoerceableFrom(expected)) {
						// throw new
						// BCException("Expected type "+expectedVarTypes[i]+" in slot "+i+" but attempt to put "+var.getType()+" into it");
						return;
					}
				}
			}
		}
		vars[i] = var;
	}

	public Var get(int i) {
		return vars[i];
	}

	public int size() {
		return vars.length;
	}

	public Expr getAspectInstance() {
		return aspectInstance;
	}

	public void setAspectInstance(Expr aspectInstance) {
		this.aspectInstance = aspectInstance;
	}

	public String toString() {
		return "ExposedState(#Vars=" + vars.length + ",Vars=" + Arrays.asList(vars) + ",AspectInstance=" + aspectInstance + ")";
	}

	// Set to true if we have reported an error message against it,
	// prevents us blowing up in later code gen.
	public void setErroneousVar(int formalIndex) {
		erroneousVars[formalIndex] = true;
	}

	public boolean isErroneousVar(int formalIndex) {
		return erroneousVars[formalIndex];
	}

	public void setConcreteAspect(ResolvedType concreteAspect) {
		this.concreteAspect = concreteAspect;
	}

	public ResolvedType getConcreteAspect() {
		return this.concreteAspect;
	}
}
