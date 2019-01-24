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

import java.util.List;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

/**
 * Common super type for Pointcuts that can bind formal parameters.
 * 
 * @author Erik Hilsdale
 * @author Jim Hugunin
 */
public abstract class NameBindingPointcut extends Pointcut {

	public NameBindingPointcut() {
		super();
	}

	protected Test exposeStateForVar(Var var,TypePattern type, ExposedState state, World world) {
		if (type instanceof BindingTypePattern) {
			BindingTypePattern b = (BindingTypePattern)type;
			state.set(b.getFormalIndex(), var);
		}
		ResolvedType myType = type.getExactType().resolve(world);
		if (myType.isParameterizedType()) {
			// unchecked warning already issued...
			myType = (ResolvedType) myType.getRawType();
		}
		return Test.makeInstanceof(var, myType.resolve(world));
	}
	
	public abstract List<BindingTypePattern> getBindingTypePatterns();
	public abstract List<BindingPattern> getBindingAnnotationTypePatterns();


}
