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
package org.aspectj.weaver.reflect;

import java.lang.reflect.Member;

/**
 * @author Adrian
 *
 */
public interface ArgNameFinder {

	/**
	 * Attempt to discover the parameter names for a reflectively obtained member
	 * @param forMember
	 * @return null if names can't be determined
	 */
	String[] getParameterNames(Member forMember);

}
