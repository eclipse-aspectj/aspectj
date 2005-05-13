/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import org.aspectj.testing.XMLBasedAjcTestCase;
import junit.framework.Test;

import java.io.File;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjLTWTests extends XMLBasedAjcTestCase {

	public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(org.aspectj.systemtest.ajc150.ataspectj.AtAjLTWTests.class);
	}

	protected File getSpecFile() {
	  return new File("../tests/src/org/aspectj/systemtest/ajc150/ataspectj/ltw.xml");
	}

	public void testRunThemAllWithJavacCompiledAndLTW() {
		runTest("RunThemAllWithJavacCompiledAndLTW");
	}

	public void testAjcLTWPerClauseTest_XnoWeave() {
		runTest("AjcLTW PerClauseTest -XnoWeave");
	}

	public void testAjcLTWPerClauseTest_Xreweavable() {
		runTest("AjcLTW PerClauseTest -Xreweavable");
	}
}
