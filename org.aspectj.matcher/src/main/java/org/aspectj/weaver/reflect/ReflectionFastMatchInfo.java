/* *******************************************************************
 * Copyright (c) 2006 Contributors.
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

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.FastMatchInfo;
import org.aspectj.weaver.tools.MatchingContext;

/**
 * An implementation of FastMatchInfo that can also expose a MatchingContext.
 * 
 * @author Adrian Colyer
 * @since 1.5.1
 */
public class ReflectionFastMatchInfo extends FastMatchInfo {

	private final MatchingContext context;

	public ReflectionFastMatchInfo(ResolvedType type, Shadow.Kind kind, MatchingContext context, World world) {
		super(type, kind, world);
		this.context = context;
	}

	/**
	 * @return Returns the matching context.
	 */
	public MatchingContext getMatchingContext() {
		return this.context;
	}

}
