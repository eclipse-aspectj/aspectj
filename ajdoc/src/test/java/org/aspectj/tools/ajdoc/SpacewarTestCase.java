/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
 package org.aspectj.tools.ajdoc;

import java.io.File;


/**
 * @author Mik Kersten
 */
public class SpacewarTestCase extends AjdocTestCase {
	
	private String[] dirs = {"spacewar","coordination"};
	
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("spacewar");
	}
    
	public void testSimpleExample() {
		runAjdoc(dirs);
	}
	
	public void testPublicModeExample() {
		runAjdoc("public",dirs);
	}

	public void testPr134063() {
		String lstFile = "spacewar" + File.separatorChar + "demo.lst";
		runAjdoc("private",lstFile);
	}
}
