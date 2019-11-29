/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Webster        Move Java 5 dependent tests
 *******************************************************************************/
package org.aspectj.systemtest.xlint;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class XLint5Tests  extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(XLint5Tests.class);
	}

	protected java.net.URL getSpecFile() {
		return getClassResource("xlint.xml");
	}
	  
	public void testBug99136(){
		runTest("Two Xlint warnings wth cflow?");
		if(ajc.getLastCompilationResult().getWarningMessages().size() != 1){
			fail();
		}
	}

}
