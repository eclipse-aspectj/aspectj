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

/**
 * AspectJ runtime representation of the per-clause associated with an aspect.
 */
public interface PerClause {

	/**
	 * @return the kind of per-clause (singleton, perthis, pertarget,...)
	 */
	PerClauseKind getKind();

}
