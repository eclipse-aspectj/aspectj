/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Andy Clement			Initial implementation
 * ******************************************************************/

package org.aspectj.weaver;

/**
 * Tag interface - methods and types can be declaring elements for type variables. See the TypeVariable class which holds onto the
 * declaring element
 */
public interface TypeVariableDeclaringElement {
	public TypeVariable getTypeVariableNamed(String name);
}
