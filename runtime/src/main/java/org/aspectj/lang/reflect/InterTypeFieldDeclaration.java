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
 * Represents an inter-type field declaration declared in an aspect.
 */
public interface InterTypeFieldDeclaration extends InterTypeDeclaration {
	
	/**
	 * The field name
	 */
	String getName();
	
	/**
	 * The field type
	 */
	AjType<?> getType();
	
	/**
	 * The generic field type
	 */
	Type getGenericType();

}
