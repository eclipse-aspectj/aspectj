/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import org.aspectj.weaver.*;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.SimpleScope;

public class TestScope extends SimpleScope {
	
	public TestScope(
		World world,
		FormalBinding[] bindings) 
	{
		super(world, bindings);
	}

    public TestScope(String[] formalTypes, String[] formalNames, World world) {
		super(world, SimpleScope.makeFormalBindings(UnresolvedType.forNames(formalTypes), formalNames));
	}
}
