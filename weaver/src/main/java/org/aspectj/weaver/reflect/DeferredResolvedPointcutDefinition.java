/* *******************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer          Initial implementation
 * ******************************************************************/

package org.aspectj.weaver.reflect;

import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.UnresolvedType;

/**
 * When a Java15ReflectionBasedDelegate gets the pointcuts for a given class it tries to resolve them before returning. This can
 * cause problems if the resolution of one pointcut in the type depends on another pointcut in the same type. Therefore the
 * algorithm proceeds in two phases, first we create and store instances of this class in the pointcuts array, and once that is
 * done, we come back round and resolve the actual pointcut expression. This means that if we recurse doing resolution, we will find
 * the named pointcut we are looking for!
 * 
 * @author adrian colyer
 * 
 */
public class DeferredResolvedPointcutDefinition extends ResolvedPointcutDefinition {

	public DeferredResolvedPointcutDefinition(UnresolvedType declaringType, int modifiers, String name,
			UnresolvedType[] parameterTypes) {
		super(declaringType, modifiers, name, parameterTypes, UnresolvedType.VOID, null);
	}

}
