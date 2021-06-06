/*******************************************************************************
 * Copyright (c) 2006 IBM
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
