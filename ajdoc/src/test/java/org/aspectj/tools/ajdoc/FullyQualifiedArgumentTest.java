/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.tools.ajdoc;

import java.io.File;
import java.util.List;

public class FullyQualifiedArgumentTest extends AjdocTestCase {

	/**
	 * Test for pr58520
	 */
    public void testPr58520() throws Exception {
    	initialiseProject("pr119453");
    	File[] files = {
    			new File(getAbsoluteProjectDir() + File.separatorChar +"src/pack/C.java"),
    			new File(getAbsoluteProjectDir() + File.separatorChar + "src/pack/A.aj")};
    	runAjdoc("private",files);

		// check the contents of A.html
		File htmlA = new File(getAbsolutePathOutdir() + "/pack/A.html");
		if (!htmlA.exists()) {
			fail("couldn't find " + getAbsolutePathOutdir()
					+ "/pack/A.html - were there compilation errors?");
		}

		// check the contents of the declare detail summary
		String[] stringsA = { "C.html#method3(java.lang.String)",
				"C.html#method3(String)"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlA,stringsA,"ADVICE SUMMARY");
		assertEquals("There should be one missing string",1,missing.size());
		assertEquals("The fully qualified name should appear in the argument",
				"C.html#method3(String)",missing.get(0));
    }

}
