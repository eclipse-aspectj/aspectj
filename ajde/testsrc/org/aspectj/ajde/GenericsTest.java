/*******************************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Mik Kersten - initial implementation
 *******************************************************************************/
package org.aspectj.ajde;

//import org.aspectj.asm.AsmManager;

/**
 * @author Mik Kersten
 */
public class GenericsTest extends AjdeTestCase {
    
    //private AsmManager manager = null;
    // TODO-path
	//private static final String CONFIG_FILE_PATH = "../bug-83565/build.lst";
 
	public void testBuild() {	
//	    assertTrue("build success", doSynchronousBuild(CONFIG_FILE_PATH));	
	}
	
	protected void setUp() throws Exception {
		super.setUp("examples");
		//manager = AsmManager.getDefault();
	}
    
}
