/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.lang.reflect.Member;

import org.aspectj.util.FuzzyBoolean;


/**
 * Interface used by PointcutExpressionImpl to determine matches.
 */
public interface PointcutExpressionMatching {

	FuzzyBoolean matchesStatically(
			String joinpointKind,
			Member member, 
			Class thisClass, 
			Class targetClass,
			Member withinCode);
	
	/**
	 * Only considers this, target, and args primitives, returns
	 * true for all others.
	 * @param thisObject
	 * @param targetObject
	 * @param args
	 * @return
	 */
	boolean matchesDynamically(
			Object thisObject,
			Object targetObject,
			Object[] args);
}
