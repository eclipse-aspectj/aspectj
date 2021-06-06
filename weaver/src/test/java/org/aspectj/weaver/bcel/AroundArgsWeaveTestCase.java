/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
