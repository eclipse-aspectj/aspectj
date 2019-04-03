/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.tools.ajdoc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BugTest extends AjdocTestCase {

	public void testPr160302() throws Exception {
		initialiseProject("pr160302");
		File[] files = {new File(getAbsoluteProjectDir() + "/C.java")};
		runAjdoc(files);
		assertFalse("expected clean build of project but found that build aborted",Main.hasAborted());
		File html = new File(getAbsolutePathOutdir() + File.separator + "C.html");
		if (!html.exists()) {
			fail("couldn't find " + getAbsolutePathOutdir() + File.separator + "C.html - were there javadoc/compilation errors?");
		}
		assertFalse("expected all decorating tags to be removed but found that they" +
				" weren't",AjdocOutputChecker.containsString(html, Config.DECL_ID_STRING));
	}
	
	/**
	 * Passing the "-Xlint:error" option through to the compiler should
	 * cause the ajc build to fail because the advice did not match
	 */
	public void testPr148906_1() {
		initialiseProject("pr148906");
		File[] files = {new File(getAbsoluteProjectDir() + "/AdviceDidNotMatch.aj")};
		String[] ajOptions = {new String("-Xlint:error")};
		runAjdoc(files,"1.5",ajOptions);
		assertTrue("expected ajc to fail but it did not", Main.hasAborted());
		assertEquals("expected ajc to fail with an adviceDidNotMatch error but it" +
				" failed instead with " + Main.getErrors()[0].getMessage(),
				"advice defined in AdviceDidNotMatch has not been applied [Xlint:adviceDidNotMatch]",
				Main.getErrors()[0].getMessage());
	}

	/**
	 * Passing the "-Xlintfile" option through to the compiler should
	 * cause the ajc build to fail because the advice did not match
	 */
	public void testPr148906_2() {
		initialiseProject("pr148906");
		File[] files = {new File(getAbsoluteProjectDir() + "/AdviceDidNotMatch.aj")};
		String[] ajOptions = {new String("-Xlintfile"), new String(getAbsoluteProjectDir() + File.separator + "Xlint.properties")};
		runAjdoc(files,"1.5",ajOptions);
		assertTrue("expected ajc to fail but it did not", Main.hasAborted());
		assertEquals("expected ajc to fail with an adviceDidNotMatch error but it" +
				" failed instead with " + Main.getErrors()[0].getMessage(),
				"advice defined in AdviceDidNotMatch has not been applied [Xlint:adviceDidNotMatch]",
				Main.getErrors()[0].getMessage());
	}
	
	/**
	 * Passing the -aspectpath option though to the compiler should
	 * result in relationships being displayed
	 */
	public void testPr148906_3() throws Exception {
		initialiseProject("pr148906");
		File[] files = {new File(getAbsoluteProjectDir() + "/C.java")};
		String[] ajOptions = {new String("-aspectpath"), new String(getAbsoluteProjectDir() + File.separator + "simple.jar")};
		runAjdoc(files,AJDocConstants.VERSION,ajOptions);
		assertFalse("expected clean build of project but found that build aborted",Main.hasAborted());
		File html = new File(getAbsolutePathOutdir() + File.separator + "C.html");
		if (!html.exists()) {
			fail("couldn't find " + getAbsolutePathOutdir() + File.separator + "C.html - were there javadoc/compilation errors?");
		}
		assertTrue("expected to find 'Advised by' in the html output but did " +
				" not",AjdocOutputChecker.containsString(html, 
						HtmlDecorator.HtmlRelationshipKind.ADVISED_BY.getName()));
	}
	
	/**
	 * Passing an option starting with "-" that doesn't require a second entry 
	 * should mean everything is correctly given to the compiler. For example:
	 * '-outxml -aspectpath <file>" should mean both '-outxml' and the aspectpath
	 * options are given correctly.
	 */
	public void testPr148906_4() throws Exception {
		initialiseProject("pr148906");
		File[] files = {new File(getAbsoluteProjectDir() + "/C.java")};
		String[] ajOptions = {new String("-outxml"),new String("-aspectpath"), new String(getAbsoluteProjectDir() + File.separator + "simple.jar")};
		runAjdoc(files,AJDocConstants.VERSION,ajOptions);
		assertFalse("expected clean build of project but found that build aborted",Main.hasAborted());
		File html = new File(getAbsolutePathOutdir() + File.separator + "C.html");
		if (!html.exists()) {
			fail("couldn't find " + getAbsolutePathOutdir() + File.separator + "C.html - were there javadoc/compilation errors?");
		}
		assertTrue("expected to find 'Advised by' in the html output but did " +
				" not",AjdocOutputChecker.containsString(html, 
						HtmlDecorator.HtmlRelationshipKind.ADVISED_BY.getName()));
		File aopFile = new File(getAbsolutePathOutdir() + File.separator 
				+ "META-INF" + File.separator + "aop-ajc.xml");
		assertTrue("couldn't find " + getAbsolutePathOutdir() + File.separator 
				+ "META-INF" + File.separator + "aop-ajc.xml" , 
				aopFile.exists());
	}	
	
	/**
	 * Passing bogus option to ajc
	 */
	public void testPr148906_5() throws Exception {
		initialiseProject("pr148906");
		File[] files = {new File(getAbsoluteProjectDir() + "/C.java")};
		String[] ajOptions = {new String("-bogus")};
		runAjdoc(files,"1.5",ajOptions);
		assertTrue("expected build of project to abort",Main.hasAborted());
	}
	
	/**
	 * Not passing any files to ajdoc should result in both the ajdoc
	 * and ajc usage messages
	 */
	public void testPr148906_6() throws Exception {
		initialiseProject("pr148906");
		List options = new ArrayList();
		options.add("-verbose");
		runAjdoc(options);
		assertTrue("expected the ajdoc usage message to be reported",Main.hasShownAjdocUsageMessage());
		assertTrue("expected build of project to abort",Main.hasAborted());		
	}
	
	/**
	 * javadoc comments should still appear even if preceded by 
	 * 'normal' comments
	 */
	public void testPr164356() throws Exception {
		initialiseProject("pr164356");
		File[] files = {new File(getAbsoluteProjectDir() + "/C.java")};
		runAjdoc(files);
	    File htmlFile = new File(getAbsolutePathOutdir() + "/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + 
					" (ajc aborted: " + Main.hasAborted() + ")");
		}
		String foo = "description of foo";
		String bar = "description of bar";
		String goo = "description of goo";
		String bas = "description of bas";
		assertTrue("expected method description 'description of foo' to appear" +
				" in ajdoc output but it did not", 
				AjdocOutputChecker.containsString(htmlFile, foo));
		assertTrue("expected method description 'description of bar' to " +
				"appear in ajdoc output but it did not", 
				AjdocOutputChecker.containsString(htmlFile, bar));
		assertFalse("didn't expect method description 'description of goo' to " +
				"appear in ajdoc output but it did not", 
				AjdocOutputChecker.containsString(htmlFile, goo));
		assertTrue("expected method description 'description of bas' to appear" +
				" in ajdoc output but it did not", 
				AjdocOutputChecker.containsString(htmlFile, bas));
	}
	
	/**
	 * Comments for a constructor should be included in the ajdoc output
	 */
	public void testPr164340() throws Exception {
		initialiseProject("pr164340");
		File[] files = {new File(getAbsoluteProjectDir() + "/C.java")};
		runAjdoc(files);
	    File htmlFile = new File(getAbsolutePathOutdir() + "/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + 
					" (ajc aborted: " + Main.hasAborted() + ")");
		}
		String methodDesc = "This is method foo";
		String constDesc = "This is a constructor";
		assertTrue("expected method description 'This is method foo' to appear" +
				" in ajdoc output but it did not", 
				AjdocOutputChecker.containsString(htmlFile, methodDesc));
		assertTrue("expected constructor description 'This is a constructor' to " +
				"appear in ajdoc output but it did not", 
				AjdocOutputChecker.containsString(htmlFile, constDesc));
	}
}
