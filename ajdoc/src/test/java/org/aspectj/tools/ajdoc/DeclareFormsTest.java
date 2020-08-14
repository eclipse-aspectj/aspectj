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
import java.util.List;

import org.aspectj.util.LangUtil;

/**
 * @author Mik Kersten
 */
public class DeclareFormsTest extends AjdocTestCase {

	private String declareError = "declare error: quot;Illegal construct..quot";
	private String declareWarningQuotes = "declare warning: quot;Illegal call.quot;";
	private String declareWarning = "declare warning: \"Illegal call.\"";
	private String declareParentsImpl = "declare parents: implements Serializable";
	private String declareSoft = "declare soft: foo.SizeException2";
	private String declarePrecedence = "declare precedence: foo.DeclareCoverage2, foo.InterTypeDecCoverage2";
	
	private String doItHref = "HREF=\"../foo/Main2.html#doIt()\"";
	private String pointHref = "HREF=\"../foo/Point2.html\"";
	private String cHref = "HREF=\"../foo/C.html\"";
	
	private String doIt = "doIt()";

	
	public void testCoverage() {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage.java")};
		runAjdoc("private","1.6",files);
	}
	
	/**
	 * Test that the declare statements appear in the Declare Detail
	 * and Declare Summary sections of the ajdoc
	 */
	public void testDeclareStatments() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/DeclareCoverage2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		// check the contents of the declare detail summary
		String[] strings = { 
				declareError,
				declareWarning,
				declareParentsImpl,
				declareSoft,
				declarePrecedence};

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
	
	/**
	 * Declare warning's should have the 'matched by' relationship
	 * in the ajdoc for the declaring aspect
	 */
	public void testDeclareWarning() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/DeclareCoverage2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"DECLARE DETAIL SUMMARY",
				declareWarningQuotes,
				HtmlDecorator.HtmlRelationshipKind.MATCHED_BY,
				doItHref);
		assertTrue("Should have '" + declareWarningQuotes + " matched by " + doItHref + 
				"' in the Declare Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"DECLARE SUMMARY",
					declareWarningQuotes,
					HtmlDecorator.HtmlRelationshipKind.MATCHED_BY,
					doItHref);
		assertTrue("Should have '" + declareWarningQuotes + " matched by " + doItHref + 
				"' in the Declare Summary section", b);
	}
	
	/**
	 * The target of a declare warning should have the 'matches 
	 * declare' relationship in the ajdoc - test the case when
	 * the declare warning matches a call join point
	 */
	public void testMatchesDeclareCall() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Main2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName(doIt),
				HtmlDecorator.HtmlRelationshipKind.MATCHES_DECLARE,
				declareWarningQuotes);
		assertTrue("Should have '" + doIt + " matches declare " + 
				declareWarningQuotes + "' in the Declare Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"=== METHOD SUMMARY",
					toName(doIt),
					HtmlDecorator.HtmlRelationshipKind.MATCHES_DECLARE,
					declareWarningQuotes);
		assertTrue("Should have '" + doIt + " matches declare " + 
				declareWarningQuotes + "' in the Declare Summary section", b);
	}
	
	/**
	 * The target of a declare warning should have the 'matches 
	 * declare' relationship in the ajdoc - test the case when
	 * the declare warning matches an execution join point
	 */
	public void testMatchesDeclareExecution() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName("setX(int)"),
//				LangUtil.is18VMOrGreater()?"setX-int-":"setX(int)",
				HtmlDecorator.HtmlRelationshipKind.MATCHES_DECLARE,
				"declare warning: quot;blahquot;");
		assertTrue("Should have 'setX(int) matches declare declare warning: quot;blahquot;" + 
				"' in the Method Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"=== METHOD SUMMARY",
					toName("setX(int)"),
//					LangUtil.is18VMOrGreater()?"setX-int-":"setX(int)",
					HtmlDecorator.HtmlRelationshipKind.MATCHES_DECLARE,
					"declare warning: quot;blahquot;");
		assertTrue("Should have 'setX(int) matches declare declare warning: quot;blahquot;" + 
				"' in the Method Summary section", b);
	}
	
	/**
	 * Declare parents's should have the 'declared on' relationship
	 * in the ajdoc for the declaring aspect
	 */
	public void testDeclareParents() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/DeclareCoverage2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"DECLARE DETAIL SUMMARY",
				declareParentsImpl,
				HtmlDecorator.HtmlRelationshipKind.DECLARED_ON,
				pointHref);
		assertTrue("Should have ' " + declareParentsImpl + " declared on " + 
				pointHref + "' in the Declare Detail section", b);
		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"DECLARE SUMMARY",
				declareParentsImpl,
				HtmlDecorator.HtmlRelationshipKind.DECLARED_ON,
				pointHref);
		assertTrue("Should have ' " + declareParentsImpl + " declared on " + 
				pointHref + "' in the Declare Summary section", b);
	}
	
	/**
	 * The target of a declare parent should have the 'aspect 
	 * declarations' relationship in the ajdoc
	 */
	public void testAspectDeclarations() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		boolean b = AjdocOutputChecker.classDataSectionContainsRel(
				htmlFile,
				HtmlDecorator.HtmlRelationshipKind.ASPECT_DECLARATIONS,
				"declare parents: implements Serializable");
		assertTrue("The class data section should have 'aspect declarations" +
				" declare parents: implements Serializable'",b);

	}
	
	/**
	 * Declare soft's should have the 'softens' relationship
	 * in the ajdoc for the declaring aspect
	 */
	public void testDeclareSoft() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/DeclareCoverage2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"DECLARE DETAIL SUMMARY",
				declareSoft,
				HtmlDecorator.HtmlRelationshipKind.SOFTENS,
				doItHref);
		assertTrue("Should have '" + declareSoft + " softens " + doItHref + 
				"' in the Declare Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"DECLARE SUMMARY",
					declareSoft,
					HtmlDecorator.HtmlRelationshipKind.SOFTENS,
					doItHref);
		assertTrue("Should have '" + declareSoft + " softens " + doItHref + 
				"' in the Declare Summary section", b);
	}
	
	/**
	 * The target of a declare soft should have the 'softened 
	 * by' relationship in the ajdoc
	 */
	public void testSoftenedBy() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareCoverage2.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Main2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName(doIt),
				HtmlDecorator.HtmlRelationshipKind.SOFTENED_BY,
				declareSoft);
		assertTrue("Should have '" + doIt + " softened by " + declareSoft + 
				"' in the Method Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"=== METHOD SUMMARY",
					toName(doIt),
					HtmlDecorator.HtmlRelationshipKind.SOFTENED_BY,
					declareSoft);
		assertTrue("Should have '" + doIt + " softened by " + declareSoft + 
				"' in the Method Summary section", b);
	}
	
	private String toName(String name) {
		if (!LangUtil.is11VMOrGreater()) {
			name = name.replace('(','-');
			name = name.replace(')','-');
		}
		return name;
	}
	
	/**
	 * Declare annotation should have the 'annotates' relationship
	 * in the ajdoc for the declaring aspect
	 */
	public void testDeclareAnnotation() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareAtType.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
			
		// Aspect AnnotationTest should contain within it's declare
	    // detail and summary the declare annotation statement.
		// Check for this....
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/DeclareAtType.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		// check there's no return type for the declare annotation
		// statement in the declare summary section
		String[] returnType = {"[]"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlFile,returnType,"DECLARE SUMMARY");
		assertEquals("there should be no return type for declare annotation" +
				" in the ajdoc",1,missing.size());
		assertEquals("there shouldn't be the '[]' return type for declare annotation" +
				" in the ajdoc","[]",missing.get(0));

		// check that the 'annotates' relationship is there
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"DECLARE DETAIL SUMMARY",
				"declare @type: foo.C : @MyAnnotation",
				HtmlDecorator.HtmlRelationshipKind.ANNOTATES,
				cHref);
		assertTrue("Should have 'declare @type: foo.C : @MyAnnotation annotates " 
				+ cHref + "' in the Declare Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"DECLARE SUMMARY",
					"declare @type: foo.C : @MyAnnotation",
					HtmlDecorator.HtmlRelationshipKind.ANNOTATES,
					cHref);
		assertTrue("Should have 'declare @type: foo.C : @MyAnnotation annotates " 
				+ cHref + "' in the Declare Summary section", b);
	}
	
	/**
	 * The target of a declare method annotation should have the  
	 * 'annotated by' relationship in the ajdoc within the method
	 * information
	 */
	public void testMethodAnnotatedBy() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareAtMethod.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.ANNOTATED_BY,
				"declare @method: public * foo.C.*(..) : @MyAnnotation");
		assertTrue("Should have 'amethod() annotated by " +
				"declare @method: public * foo.C.*(..) : @MyAnnotation" + 
				"' in the Method Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.ANNOTATED_BY,
				"declare @method: public * foo.C.*(..) : @MyAnnotation");
		assertTrue("Should have 'amethod() annotated by " +
				"declare @method: public * foo.C.*(..) : @MyAnnotation" + 
				"' in the Method Summary section", b);
	}
	
	/**
	 * The target of a declare method annotation should have the  
	 * 'annotated by' relationship in the ajdoc within the method
	 * information
	 */
	public void testConstructorAnnotatedBy() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareAtConstructor.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== CONSTRUCTOR DETAIL",
				LangUtil.is11VMOrGreater()?"&lt;init&gt;(java.lang.String)":toName("C(java.lang.String)"),
				HtmlDecorator.HtmlRelationshipKind.ANNOTATED_BY,
				"declare @constructor: foo.C.new(..) : @MyAnnotation");
		assertTrue("Should have '" + doIt + " annotated by " + 
				"declare @constructor: foo.C.new(..) : @MyAnnotation" + 
				"' in the Method Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== CONSTRUCTOR SUMMARY",
				LangUtil.is11VMOrGreater()?"#%3Cinit%3E(java.lang.String)":toName("C(java.lang.String)"),
				HtmlDecorator.HtmlRelationshipKind.ANNOTATED_BY,
				"declare @constructor: foo.C.new(..) : @MyAnnotation");
		assertTrue("Should have '" + doIt + " annotated by " + 
				"declare @constructor: foo.C.new(..) : @MyAnnotation" + 
				"' in the Method Summary section", b);
	}
	
	/**
	 * The target of a declare method annotation should have the  
	 * 'annotated by' relationship in the ajdoc within the method
	 * information
	 */
	public void testFieldAnnotatedBy() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareAtField.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== FIELD DETAIL",
				"x",
				HtmlDecorator.HtmlRelationshipKind.ANNOTATED_BY,
				"declare @field: int foo.C.* : @MyAnnotation");
		assertTrue("Should have '" + doIt + " annotated by " + 
				"declare @field: int foo.C.* : @MyAnnotation" + 
				"' in the Field Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== FIELD SUMMARY",
				"x",
				HtmlDecorator.HtmlRelationshipKind.ANNOTATED_BY,
				"declare @field: int foo.C.* : @MyAnnotation");
		assertTrue("Should have '" + doIt + " annotated by " + 
				"declare @field: int foo.C.* : @MyAnnotation" + 
				"' in the Field Summary section", b);
	}
	
	/**
	 * The target of a declare method annotation should have the  
	 * 'annotated by' relationship in the ajdoc within the method
	 * information
	 */
	public void testTypeAnnotatedBy() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareAtType.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		boolean b = AjdocOutputChecker.classDataSectionContainsRel(
				htmlFile,
				HtmlDecorator.HtmlRelationshipKind.ANNOTATED_BY,
				"declare @type: foo.C : @MyAnnotation");
		assertTrue("The class data section should have 'annotated by" +
				" declare @type: foo.C : @MyAnnotation'",b);
	}
	
	/**
	 * Test that info for both "matches declare" and "advised by"
	 * appear in the ajdoc for a method when the method is affected
	 * by both.
	 */
	public void testMatchesDeclareAndAdvisedBy() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "A.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.MATCHES_DECLARE,
				"declare warning: quot;warningquot;");
		assertTrue("Should have 'amethod() matches declare declare warning: " +
				"quot;warningquot;' in the Method Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"=== METHOD SUMMARY",
					toName("amethod()"),
					HtmlDecorator.HtmlRelationshipKind.MATCHES_DECLARE,
					"declare warning: quot;warningquot;");
		assertTrue("Should have 'amethod() matches declare declare warning: " +
				"quot;warningquot;' in the Method Summary section", b);
		
		b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				"before(): p..");
		assertTrue("the Method Detail should have amethod() advised by before(): p..",b);
		
		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				"before(): p..");
		assertTrue("the Method Summary should have amethod() advised by before(): p..",b);	
	}
	
	/**
	 * Test that if there are two declare parents statements within 
	 * an aspect, one which extends and one which implements, that the
	 * ajdoc shows the correct information
	 */
	public void testTwoDeclareParents() throws Exception {
		initialiseProject("declareForms");
		File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "DeclareParents.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
		
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/DeclareParents.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		
		String[] strings = {
				"declare parents: implements Serializable",
				"HREF=\"../foo/Class1.html\"",
				"declare parents: extends Observable",
				"HREF=\"../foo/Class2.html\""};
		
		// check that the correct declare statements are there
		for (int i = 0; i < strings.length - 1; i = i+2) {
			boolean b = AjdocOutputChecker.detailSectionContainsRel(
					htmlFile,"DECLARE DETAIL SUMMARY",strings[i],
					HtmlDecorator.HtmlRelationshipKind.DECLARED_ON,
					strings[i+1]);
			assertTrue("Should have ' " + strings[i] + " declared on " + strings[i+1] + 
					"' in the Declare Detail section", b);
		}
		
		for (int i = 0; i < strings.length - 1; i = i+2) {
			boolean b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"DECLARE SUMMARY",
					strings[i],
					HtmlDecorator.HtmlRelationshipKind.DECLARED_ON,
					strings[i+1]);
			assertTrue("Should have ' " + strings[i] + " declared on " + strings[i+1] + 
					"' in the Declare Summary section", b);
		}
		
		// check that we don't have declare statements for those that don't 
		// exist in the code
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"DECLARE DETAIL SUMMARY",strings[0],
				HtmlDecorator.HtmlRelationshipKind.DECLARED_ON,
				strings[3]);
		assertFalse("Should not have ' " + strings[0] + " declared on " + strings[3] + 
				"' in the Declare Detail section", b);
		
	}
	
}
