/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.workbench.resources;

import junit.framework.*;

public class WorkspaceResourcesTests {

	public static void main(String[] args) {
	}

	public static Test suite() {
		TestSuite suite =
			new TestSuite("Test for org.aspectj.workbench.resources");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(FilesystemFileTest.class));
		suite.addTest(new TestSuite(FilesystemFolderTest.class));
		//$JUnit-END$
		return suite;
	}
}
