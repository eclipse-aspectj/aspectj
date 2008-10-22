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
package org.aspectj.weaver.tools;

/**
 * When extending AspectJ's pointcut parsing and
 * matching with custom PointcutDesignatorHandlers,
 * it may be necessary to match based on context information
 * at a join point not exposed simply by java.lang.reflect
 * member information or argument values. The matching context
 * interface provides an extension point for the specification
 * of additional shadow and join point context that can be
 * taken into account during the matching process.
 * 
 *  @see DefaultMatchingContext
 */
public interface MatchingContext {

	/**
	 * Returns true iff this matching context has a defined
	 * binding for the given context parameter.
	 * @param contextParameterName
	 */
	boolean hasContextBinding(String contextParameterName);
	
	/**
	 * returns the binding associated with the 
	 * given context parameter name (or null if
	 * there is no such context). 
	 * @param contextParameterName
	 * @return
	 */
	Object getBinding(String contextParameterName);
}
