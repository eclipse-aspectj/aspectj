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


package org.aspectj.weaver.patterns;

import org.aspectj.weaver.TypeX;
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
		TypeX myType = type.getExactType(); //should have failed earlier 
		
		return Test.makeInstanceof(var, myType.resolve(world));
	}
	
	


}
