/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.internal.tools;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.reflect.ReflectionBasedReferenceTypeDelegateFactory;
import org.aspectj.weaver.tools.TypePatternMatcher;

public class TypePatternMatcherImpl implements TypePatternMatcher {

	private final TypePattern pattern;
	private final World world;

	public TypePatternMatcherImpl(TypePattern pattern, World world) {
		this.pattern = pattern;
		this.world = world;		
	}
	
	public boolean matches(Class aClass) {
		ResolvedType rt = 
			ReflectionBasedReferenceTypeDelegateFactory.resolveTypeInWorld(aClass,world);
		return pattern.matchesStatically(rt);
	}

}
