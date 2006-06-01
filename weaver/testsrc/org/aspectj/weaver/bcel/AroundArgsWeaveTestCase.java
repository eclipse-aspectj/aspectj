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


package org.aspectj.weaver.bcel;

import java.io.IOException;

import org.aspectj.weaver.ShadowMunger;

public class AroundArgsWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public AroundArgsWeaveTestCase(String name) {
		super(name);
	}
		
    public void testWeave() throws IOException
    {
    	String label = "AroundArgs";
    	ShadowMunger p = 
                makeConcreteAdvice(
					"around(list) : "
					+ "(call(public * add(..)) && target(list)) -> " 
					+ "static boolean Aspect.ajc_around0" 
					+ "(java.util.ArrayList, org.aspectj.runtime.internal.AroundClosure)");
        weaveTest(new String[] {"DynamicHelloWorld"}, label, p);
        
    }
}
