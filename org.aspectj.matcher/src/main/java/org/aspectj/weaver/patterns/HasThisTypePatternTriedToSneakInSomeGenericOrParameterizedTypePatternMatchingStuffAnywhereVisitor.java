/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import org.aspectj.weaver.UnresolvedType;

/**
 * @author colyer
 *
 */
public class HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor
		extends AbstractPatternNodeVisitor {

	boolean ohYesItHas = false;
	
	/**
	 * Is the Exact type parameterized?
	 * Generic is ok as that just means we resolved a simple type pattern to a generic type
	 */
	public Object visit(ExactTypePattern node, Object data) {
		UnresolvedType theExactType = node.getExactType();
		if (theExactType.isParameterizedType()) ohYesItHas = true;
		//if (theExactType.isGenericType()) ohYesItHas = true;
		return data;
	}

	/**
	 * Any type bounds are bad.
	 * Type parameters are right out.
	 */
	public Object visit(WildTypePattern node, Object data) {
		if (node.getUpperBound() != null) ohYesItHas = true;
		if (node.getLowerBound() != null) ohYesItHas = true;
		if (node.getTypeParameters().size() != 0) ohYesItHas = true;
		return data;
	}
	
	public boolean wellHasItThen/*?*/() {
		return ohYesItHas;
	}
}
