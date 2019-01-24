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
package org.aspectj.lang.reflect;

import java.lang.reflect.Type;

/**
 * A declare parents member defined inside an aspect
 */
public interface DeclareParents {

	/**
	 * The declaring aspect
	 */
	AjType getDeclaringType();
	
	/**
	 * The target type pattern
	 */
	TypePattern getTargetTypesPattern();
	
	/**
	 * True if this is a declare parents...extends member declaration
	 */
	boolean isExtends();
	
	/**
	 * True if this is a declare parents...implements member declaration
	 */
	boolean isImplements();
	
	/**
	 * The set of types that the types matching getTargetTypesPattern are 
	 * declared to implement or extend
	 */
	Type[] getParentTypes() throws ClassNotFoundException;
	
}
