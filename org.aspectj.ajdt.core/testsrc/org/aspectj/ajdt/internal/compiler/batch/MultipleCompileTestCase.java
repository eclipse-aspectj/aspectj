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

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.*;

public class MultipleCompileTestCase extends CommandTestCase {

	/**
	 * Constructor for WorkingCommandTestCase.
	 * @param name
	 */
	public MultipleCompileTestCase(String name) {
		super(name);
	}

	public void testA1() throws IOException, InterruptedException {
		checkMultipleCompile("src1/Hello.java");
	}
}

