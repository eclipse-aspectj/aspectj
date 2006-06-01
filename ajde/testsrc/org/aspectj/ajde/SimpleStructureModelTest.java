/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajde;

public class SimpleStructureModelTest extends AjdeTestCase {
	
	private String CONFIG_FILE_PATH = "test.lst";

	public SimpleStructureModelTest(String name) {
		super(name);
	}
	
	public void testModel() {
		
	}

	protected void setUp() throws Exception {
		super.setUp("SimpleStructureModelTest");
		doSynchronousBuild(CONFIG_FILE_PATH);	
	}

}
