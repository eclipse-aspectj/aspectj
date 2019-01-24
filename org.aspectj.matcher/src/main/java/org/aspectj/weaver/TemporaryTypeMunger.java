/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Map;

/**
 * Some methods need a temporary type munger (because ConcreteTypeMunger is abstract - dont ask...).
 * 
 * TODO ought to remove the need for this or at least sort out the two methods that are in it, they look weird...
 * 
 * @author AndyClement
 */
public class TemporaryTypeMunger extends ConcreteTypeMunger {

	public TemporaryTypeMunger(ResolvedTypeMunger munger, ResolvedType aspectType) {
		super(munger, aspectType);
	}

	@Override
	public ConcreteTypeMunger parameterizeWith(Map parameterizationMap, World world) {
		throw new UnsupportedOperationException("Cannot be called on a TemporaryTypeMunger");
	}

	@Override
	public ConcreteTypeMunger parameterizedFor(ResolvedType targetType) {
		throw new UnsupportedOperationException("Cannot be called on a TemporaryTypeMunger");
	}

}