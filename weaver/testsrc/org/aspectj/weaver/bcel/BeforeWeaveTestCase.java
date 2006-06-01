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

public class BeforeWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public BeforeWeaveTestCase(String name) {
		super(name);
	}
	
	
	public void testBefore() throws IOException {
		weaveTest(getStandardTargets(), "Before", makeAdviceAll("before"));
	}
}
