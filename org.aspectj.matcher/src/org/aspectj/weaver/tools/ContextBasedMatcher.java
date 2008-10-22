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
 * Pointcut expression interface for pointcut
 * expressions returned by a
 * PointcutDesignatorHandler. Provides an additional
 * matching method for matching based on context
 * information over and above that normally used
 * by AspectJ.
 * 
 * @see MatchingContext
 *
 */
public interface ContextBasedMatcher {

	/**
	 * return true iff this matcher could ever match
	 * a join point in the given type
	 * @deprecated use couldMatchJoinPointsInType(Class,MatchingContext) instead
	 */
	boolean couldMatchJoinPointsInType(Class aClass);
	
	/**
	 * return true iff this matcher could ever match
	 * a join point in the given type, may also use any
	 * match context information available
	 * @since 1.5.1
	 */
	boolean couldMatchJoinPointsInType(Class aClass, MatchingContext matchContext);
	
	/**
	 * return true if matchesStatically can ever return
	 * FuzzyBoolean.MAYBE (necessitating a per-join point test
	 * to determine matching at a given join point).
	 */
	boolean mayNeedDynamicTest();

	/**
	 * Return FuzzyBoolean.YES if a join point with the given
	 * matching context is always matched. 
	 * Return FuzzyBoolean.NO if a join point with the given
	 * matching context is never matched.
	 * Return FuzzyBoolean.MAYBE if a match cannot be determined
	 * statically (whilst generating a ShadowMatch), and must
	 * be determined on a per-join point basis. 
	 */
	FuzzyBoolean matchesStatically(MatchingContext matchContext);

	/**
	 * Called during processing of ShadowMatch.matchesJoinPoint
	 * when matchesStatically returned FuzzyBoolean.MAYBE. 
	 */
	boolean matchesDynamically(MatchingContext matchContext);
}
