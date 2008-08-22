/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.tools.ajdoc;

import java.io.File;
import java.util.List;

public class ITDTest extends AjdocTestCase {
	
	/**
	 * Test for pr119453
	 */
	public void testITDDeclaredOn() throws Exception {
		initialiseProject("pr119453");
		File[] files = {
				new File(getAbsoluteProjectDir() + "/src/pack/C.java"),
				new File(getAbsoluteProjectDir() + "/src/pack/A.aj")
		};
		runAjdoc("private",files);
		File htmlA = new File(getAbsolutePathOutdir() + "/pack/A.html");
		if (!htmlA.exists()) {
			fail("couldn't find " + getAbsolutePathOutdir() + "/pack/A.html - were there compilation errors?");
		}

		// check field itd appears
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlA,"DECLARE DETAIL SUMMARY",
				"C.y",
				HtmlDecorator.HtmlRelationshipKind.DECLARED_ON,
				"HREF=\"../pack/C.html\"");
		assertTrue("Should have 'C.y declared on HREF=\"../pack/C.html\"" + 
				"' in the Declare Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlA,"DECLARE SUMMARY",
					"C.y",
					HtmlDecorator.HtmlRelationshipKind.DECLARED_ON,
					"HREF=\"../pack/C.html\"");
		assertTrue("Should have 'C.y declared on HREF=\"../pack/C.html\"" + 
				"' in the Declare Summary section", b);
		
		// check the modifiers are correct in the declare detail summary
		String[] stringsA = { "private&nbsp;int",
				"public&nbsp;java.lang.String", 
				"<H3>C.y</H3>",
				"public&nbsp;</TT><B>C.C",
				"package&nbsp;void"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(htmlA,stringsA,"DECLARE DETAIL SUMMARY");
		assertEquals("There should be one missing string ",1,missing.size());
		assertEquals("the 'package' and 'void' modifiers shouldn't appear in the 'Declare Detail' section of the ajdoc",
				"package&nbsp;void", missing.get(0));
		
		// check the modifiers are correct in the declare summary
		String[] stringsA2 = {"private", "int", "public", "String", "package&nbsp;void"};
		missing = AjdocOutputChecker.getMissingStringsInSection(htmlA,stringsA2,"DECLARE SUMMARY");
		assertEquals("There should be two missing strings ",2,missing.size());
		assertTrue("the public modifier shouldn't appear in the 'Declare Summary' section of the ajdoc", missing.contains("public"));
		assertTrue("the 'package' and 'void' modifiers shouldn't appear in the 'Declare Summary' section of the ajdoc", missing.contains("package&nbsp;void"));
		
	}

	/**
	 * Test for pr119453
	 */
	public void testITDMatchesDeclare() throws Exception {
		initialiseProject("pr119453");
		File[] files = {
				new File(getAbsoluteProjectDir() + "/src/pack/C.java"),
				new File(getAbsoluteProjectDir() + "/src/pack/A.aj")
		};
		runAjdoc("private",files);

		// Check the contents of C.html
		File htmlC = new File(getAbsolutePathOutdir() + "/pack/C.html");
		if (!htmlC.exists()) {
			fail("couldn't find " + getAbsolutePathOutdir()
					+ "/pack/C.html - were there compilation errors?");
		}

		// check that the required sections exist
		assertTrue(htmlC.getAbsolutePath() + " should contain an "
				+ "'INTER-TYPE METHOD SUMMARY' section", 
				AjdocOutputChecker.containsString(htmlC, "INTER-TYPE METHOD SUMMARY"));
		assertTrue(htmlC.getAbsolutePath() + " should contain an "
				+ "'INTER-TYPE FIELD SUMMARY' section", 
				AjdocOutputChecker.containsString(htmlC, "INTER-TYPE FIELD SUMMARY"));
		assertTrue(htmlC.getAbsolutePath() + " should contain an "
				+ "'INTER-TYPE CONSTRUCTOR SUMMARY' section",
				AjdocOutputChecker.containsString(htmlC,"INTER-TYPE CONSTRUCTOR SUMMARY"));

		// check the modifier information in the sections is correct
		String[] stringsC = { "public", "String", "pack.A" };
		List missing = AjdocOutputChecker.getMissingStringsInSection(htmlC,stringsC,"INTER-TYPE METHOD SUMMARY");
		assertEquals("There should be one missing string",1,missing.size());
		assertEquals("public itd methods should not have the 'public' modifier in the ajdoc",
				"public",missing.get(0));

		String[] stringsC2 = { "private" };
		missing = AjdocOutputChecker.getMissingStringsInSection(htmlC,stringsC2,"INTER-TYPE FIELD SUMMARY");
		assertTrue("the private modifier for itd methods should appear in the ajdoc ",missing.size() == 0);
	
	}
	
	/**
	 * Test that the ITD's do not appear in as 'aspect declarations' in the
	 * class data information. 
	 */
	public void testNoAspectDeclarations() throws Exception {
		initialiseProject("pr119453");
		File[] files = {
				new File(getAbsoluteProjectDir() + "/src/pack/C.java"),
				new File(getAbsoluteProjectDir() + "/src/pack/A.aj")
		};
		runAjdoc("private",files);

		File htmlC = new File(getAbsolutePathOutdir() + "/pack/C.html");
		if (htmlC == null || !htmlC.exists()) {
			fail("couldn't find " + getAbsolutePathOutdir()
					+ "/pack/C.html - were there compilation errors?");
		}

		boolean b = AjdocOutputChecker.classDataSectionContainsRel(
				htmlC,
				HtmlDecorator.HtmlRelationshipKind.ASPECT_DECLARATIONS,
				"pack.A.C.y");
		assertFalse("The class data section should not have 'aspect declarations" +
				" pack.A.C.y' since this is an ITD",b);
	}

}
