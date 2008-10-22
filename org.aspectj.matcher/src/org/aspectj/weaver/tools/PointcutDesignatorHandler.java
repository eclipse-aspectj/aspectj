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
 * The PointcutDesignator interface allows extension of the
 * AspectJ pointcut language so that third-party tools integrating
 * with AspectJ can add easily their own custom 
 * domain-specific designators and have them interoperate seamlessly
 * with the standard AspectJ designators.
 *
 * A pointcut designator can only be used for matching, not for
 * binding.
 */
public interface PointcutDesignatorHandler {

	/**
	 * The name of this pointcut designator. For example,
	 * if this designator handles a "bean(&lt;NamePattern&gt;)
	 * format designator, this method would return "bean".
	 * @return
	 */
	String getDesignatorName() ;
	
	/**
	 * Parse the given expression string
	 * and return a ContextBasedMatcher that can be used
	 * for matching.
	 * @param expression  the body of the pointcut expression. 
	 * For example, given the expression "bean(*DAO)" the parse
	 * method will be called with the argument "*DAO".
	 * @return a pointcut expression that can be used for
	 * matching.
	 * @throws IllegalArgumentException if the expression
	 * is ill-formed.
	 */
	ContextBasedMatcher parse(String expression);
	
}
