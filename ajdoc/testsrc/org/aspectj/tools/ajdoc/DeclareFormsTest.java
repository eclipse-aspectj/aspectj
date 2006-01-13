/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.io.File;
import java.util.List;

/**
 * @author Mik Kersten
 */
public class DeclareFormsTest extends AjdocTestCase {

	public void testCoverage() {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage.java")};
		runAjdoc("private","1.4",files);
	}
	
	public void testDeclareStatments() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private","1.4",files);
		
		// Aspect DeclareCoverage2 should contain within it's declare
	    // detail and summary the 6 different declare statements.
		// Check for this....
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/DeclareCoverage2.html");
		if (htmlFile == null || !htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		// check the contents of the declare detail summary
		String[] strings = { "declare error: quot;Illegal construct..quot",
				"declare warning: quot;Illegal call.quot;",
				"declare parents: implements Serializable",
				"declare parents: extends Observable",
				"declare soft: foo.SizeException",
				"declare precedence: foo.DeclareCoverage2, foo.InterTypeDecCoverage"};

		List missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlFile,strings,"DECLARE DETAIL SUMMARY");
		assertTrue(htmlFile.getName() + " should contain all declare statements in " +
				"the Declare Detail section",missing.isEmpty());
		
		// check the contents of the declare summary - should contain
		// the same strings
		missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlFile,strings,"DECLARE SUMMARY");
		assertTrue(htmlFile.getName() + " should contain all declare statements in " +
				"the Declare Summary section",missing.isEmpty());
	}
	
	public void testDeclareAnnotation() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "AnnotationTest.aj")};
		runAjdoc("private","1.5",files);
			
		// Aspect AnnotationTest should contain within it's declare
	    // detail and summary the declare annotation statement.
		// Check for this....
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/AnnotationTest.html");
		if (htmlFile == null || !htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		// check the contents of the declare detail summary
		String[] strings = { "declare @type: foo.C : @MyAnnotation",
				"declare declare @type: foo.C : @MyAnnotation"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlFile,strings,"DECLARE DETAIL SUMMARY");
		assertEquals("there should be one missing string ",1,missing.size());
		assertEquals("The declare statement shouldn't contain two 'declare's ", 
				"declare declare @type: foo.C : @MyAnnotation",missing.get(0));
		
		// check the contents of the declare summary - should contain
		// the declare @type statement without a return type
		String[] summaryStrings = { "declare @type: foo.C : @MyAnnotation","[]"};
		missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlFile,summaryStrings,"DECLARE SUMMARY");
		assertEquals("there should be one missing string ",1,missing.size());
		assertEquals("The declare statement shouldn't have '[]' as it's return type",
				"[]",missing.get(0));
	}
	
}
