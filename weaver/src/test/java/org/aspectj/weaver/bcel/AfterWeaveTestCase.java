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

public class AfterWeaveTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public AfterWeaveTestCase(String name) {
		super(name);
	}
	
	
	public void testAfter() throws IOException {
		weaveTest(getStandardTargets(), "After", makeAdviceAll("after"));
	}
}
