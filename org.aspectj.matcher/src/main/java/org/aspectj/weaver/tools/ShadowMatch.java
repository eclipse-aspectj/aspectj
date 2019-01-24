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
 * The result of asking a PointcutExpression to match at a shadow (method execution,
 * handler, constructor call, and so on).
 *
 */
public interface ShadowMatch {

	/**
	 * True iff the pointcut expression will match any join point at this
	 * shadow (for example, any call to the given method).
	 */
	boolean alwaysMatches();
	
	/**
	 * True if the pointcut expression may match some join points at this
	 * shadow (for example, some calls to the given method may match, depending
	 * on the type of the caller).
	 * <p>If alwaysMatches is true, then maybeMatches is always true.</p>
	 */
	boolean maybeMatches();
	
	/**
	 * True iff the pointcut expression can never match any join point at this
	 * shadow (for example, the pointcut will never match a call to the given
	 * method).
	 */
	boolean neverMatches();
	
	/**
	 * Return the result of matching a join point at this shadow with the given
	 * this, target, and args.
	 * @param thisObject  the object bound to this at the join point
	 * @param targetObject the object bound to target at the join point
	 * @param args the arguments at the join point
	 * @return
	 */
	JoinPointMatch matchesJoinPoint(Object thisObject, Object targetObject, Object[] args);
	
	/**
	 * Set a matching context to be used when matching
	 * join points.
	 * @see MatchingContext
	 */
	void setMatchingContext(MatchingContext aMatchContext);
}
