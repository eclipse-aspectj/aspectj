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
 * A long way to go until full coverage, but this is the place to add more.
 * 
 * @author Mik Kersten
 */
public class CoverageTestCase extends AjdocTestCase {

	protected File file0,file1,aspect1,file2,file3,file4,file5,file6,file7,file8,file9,file10;
	
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("coverage");
		createFiles();
	}
	
	public void testOptions() {
		String[] args = { 
			"-private",
			"-encoding",
			"EUCJIS",
			"-docencoding",
			"EUCJIS",
			"-charset",
			"UTF-8",
            "-classpath",
            AjdocTests.ASPECTJRT_PATH.getPath(),
			"-d", 
			getAbsolutePathOutdir(),
			file0.getAbsolutePath(), 
		};
		org.aspectj.tools.ajdoc.Main.main(args);
	    assertTrue(true);
	}
	
	/**
	 * Test the "-public" argument 
	 */
    public void testCoveragePublicMode() throws Exception {
    	File[] files = {file3,file9};
    	runAjdoc("public","1.4",files);
        
        // have passed the "public" modifier as well as
        // one public and one package visible class. There
        // should only be ajdoc for the public class
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/PkgVisibleClass.html");
		assertFalse("ajdoc for PkgVisibleClass shouldn't exist because passed" +
				" the 'public' flag to ajdoc",htmlFile.exists());

        htmlFile = new File(getAbsolutePathOutdir() + "/foo/PlainJava.html");
		if (htmlFile == null || !htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
        
		// check there's no private fields within the file, that
		// the file contains the getI() method but doesn't contain
		// the private ClassBar, Bazz and Jazz classes.
		String[] strings = { "private", "getI()","ClassBar", "Bazz", "Jazz"};
		List missing = AjdocOutputChecker.getMissingStringsInFile(htmlFile,strings);
		assertEquals("There should be 4 missing strings",4,missing.size());
		assertTrue(htmlFile.getName() + " should not contain the private modifier",missing.contains("private"));
		assertTrue(htmlFile.getName() + " should not contain the private ClassBar class",missing.contains("ClassBar"));
		assertTrue(htmlFile.getName() + " should not contain the private Bazz class",missing.contains("Bazz"));
		assertTrue(htmlFile.getName() + " should not contain the private Jazz class",missing.contains("Jazz"));
    }
    
    /**
     * Test that the ajdoc for an inner aspect is entitled "Aspect" rather
     * than "Class", but that the enclosing class is still "Class" 
     */
    public void testInnerAspect() throws Exception {
    	File[] files = {file1, file2};
        runAjdoc("private","1.4",files);
            
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/ClassA.InnerAspect.html");
		if (htmlFile == null || !htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
        
		// ensure that the file is entitled "Aspect ClassA.InnerAspect" rather
		// than "Class ClassA.InnerAspect"
		String[] strings = { "Aspect ClassA.InnerAspect",
				"<PRE>static aspect <B>ClassA.InnerAspect</B><DT>extends java.lang.Object</DL>",
				"Class ClassA.InnerAspect",
				"<PRE>static class <B>ClassA.InnerAspect</B><DT>extends java.lang.Object</DL>"};
		List missing = AjdocOutputChecker.getMissingStringsInFile(htmlFile,strings);
		assertEquals("There should be 2 missing strings",2,missing.size());
		assertTrue(htmlFile.getName() + " should not have Class as it's title",missing.contains("Class ClassA.InnerAspect"));
		assertTrue(htmlFile.getName() + " should not have class in its subtitle",missing.contains("<PRE>static class <B>ClassA.InnerAspect</B><DT>extends java.lang.Object</DL>"));
		
		// get the html file for the enclosing class
        File htmlFileClass = new File(getAbsolutePathOutdir() + "/foo/ClassA.html");
		if (htmlFileClass == null || !htmlFileClass.exists()) {
			fail("couldn't find " + htmlFileClass.getAbsolutePath()
					+ " - were there compilation errors?");
		}
        
		// ensure that the file is entitled "Class ClassA" and
		// has not been changed to "Aspect ClassA"
		String[] classStrings = { "Class ClassA</H2>",
				"public abstract class <B>ClassA</B><DT>extends java.lang.Object<DT>",
				"Aspect ClassA</H2>",
				"public abstract aspect <B>ClassA</B><DT>extends java.lang.Object<DT>"};
		List classMissing = AjdocOutputChecker.getMissingStringsInFile(htmlFileClass,classStrings);
		assertEquals("There should be 2 missing strings",2,classMissing.size());
		assertTrue(htmlFileClass.getName() + " should not have Aspect as it's title",classMissing.contains("Aspect ClassA</H2>"));
		assertTrue(htmlFileClass.getName() + " should not have aspect in its subtitle",classMissing.contains("public abstract aspect <B>ClassA</B><DT>extends java.lang.Object<DT>"));
    }
    
    /**
     * Test that all the different types of advice appear
     * with the named pointcut in it's description 
     */
    public void testAdviceNamingCoverage() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private","1.4",files);
    	
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/AdviceNamingCoverage.html");
		if (htmlFile == null || !htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
        
		String[] strings = { 
				"after(): named..",
				"afterReturning(int, int): namedWithArgs..",
				"afterThrowing(): named..",
				"before(): named..",
				"around(int): namedWithOneArg..",
				"before(int):",
				"before(int): named()..",
				"before():"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlFile, strings,"ADVICE DETAIL SUMMARY");
		assertTrue(htmlFile.getName() + " should contain all advice in the Advice Detail section",missing.isEmpty());
		missing = AjdocOutputChecker.getMissingStringsInSection(
				htmlFile,strings,"ADVICE SUMMARY");
		assertTrue(htmlFile.getName() + " should contain all advice in the Advice Summary section",missing.isEmpty());
    }

    /**
     * Test that all the advises relationships appear in the 
     * Advice Detail and Advice Summary sections and that
     * the links are correct 
     */
    public void testAdvisesRelationshipCoverage() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private","1.4",files);
    	
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/AdvisesRelationshipCoverage.html");
		if (htmlFile == null || !htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}
        
		String[] strings = {
				"<B>before(): methodExecutionP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html#setX(int)\"",
				"<B>before(): constructorExecutionP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html#Point()\"",
				"<B>before(): callConstructorP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html#doIt()\"",
				"<B>before(): getP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html#getX()\"",
				"<B>before(): setP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html\"><tt>foo.Point</tt></A>, <A HREF=\"../foo/Point.html#Point()\"><tt>foo.Point.Point()</tt></A>, <A HREF=\"../foo/Point.html#setX(int)\"><tt>foo.Point.setX</tt></A>, <A HREF=\"../foo/Point.html#changeX(int)\"",
				"<B>before(): initializationP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html#Point()\"",
				"<B>before(): staticinitializationP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html\"",
				"<B>before(): handlerP..</B>",
				"Advises:</b></font></td><td><A HREF=\"../foo/Point.html#doIt()\""};
		
		for (int i = 0; i < strings.length - 1; i = i+2) {
			boolean b = AjdocOutputChecker.sectionContainsConsecutiveStrings(htmlFile,strings[i],
					strings[i+1],"ADVICE DETAIL SUMMARY");
			assertTrue(strings[i] + " should have relationship " + strings[i+1] + 
					" in the Advice Detail section", b);
		}
		
		for (int i = 0; i < strings.length - 1; i = i+2) {
			boolean b = AjdocOutputChecker.sectionContainsConsecutiveStrings(htmlFile,strings[i],
					strings[i+1],"ADVICE SUMMARY");
			assertTrue(strings[i] + " should have relationship " + strings[i+1] + 
					" in the Advice Summary section", b);
		}
    }

    /**
     * Test that all the advised by relationships appear in the 
     * various detail and summary sections in the ajdoc for the
     * affected class and that the links are correct 
     */
    public void testAdvisedByRelationshipCoverage() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private","1.4",files);
    	
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (htmlFile == null || !htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}
        
		String[] constructorStrings = {
				"Advised&nbsp;by:",
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): constructorExecutionP..\""};
		String[] methodStrings = {
				"Advised&nbsp;by:",
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): methodExecutionP..\""};
		
		boolean b = AjdocOutputChecker.sectionContainsConsecutiveStrings(
				htmlFile,constructorStrings[0],
				constructorStrings[1],"CONSTRUCTOR SUMMARY");
		assertTrue("the Constructor Summary should have the advised by relationship",b);
		b = AjdocOutputChecker.sectionContainsConsecutiveStrings(
				htmlFile,constructorStrings[0],
				constructorStrings[1],"CONSTRUCTOR DETAIL");
		assertTrue("the Constructor Detail should have the advised by relationship",b);		

		b = AjdocOutputChecker.sectionContainsConsecutiveStrings(
				htmlFile,methodStrings[0],
				methodStrings[1],"=== METHOD SUMMARY");
		assertTrue("the Method Summary should have the advised by relationship",b);

		b = AjdocOutputChecker.sectionContainsConsecutiveStrings(
				htmlFile,methodStrings[0],
				methodStrings[1],"=== METHOD DETAIL");
		assertTrue("the Method Detail should have the advised by relationship",b);
    }
    
    
	public void testCoverage() {
		File[] files = {aspect1,file0,file1,file2,file3,file4,file5,file6,
				file7,file8,file9,file10};
		runAjdoc("private","1.4",files);
	}
	
	private void createFiles() {
		file0 = new File(getAbsoluteProjectDir() + "/InDefaultPackage.java");
		file1 = new File(getAbsoluteProjectDir() + "/foo/ClassA.java");
		aspect1 = new File(getAbsoluteProjectDir() + "/foo/UseThisAspectForLinkCheck.aj");
		file2 = new File(getAbsoluteProjectDir() + "/foo/InterfaceI.java");
		file3 = new File(getAbsoluteProjectDir() + "/foo/PlainJava.java");
		file4 = new File(getAbsoluteProjectDir() + "/foo/ModelCoverage.java");
		file5 = new File(getAbsoluteProjectDir() + "/fluffy/Fluffy.java");
		file6 = new File(getAbsoluteProjectDir() + "/fluffy/bunny/Bunny.java");
		file7 = new File(getAbsoluteProjectDir() + "/fluffy/bunny/rocks/Rocks.java");
		file8 = new File(getAbsoluteProjectDir() + "/fluffy/bunny/rocks/UseThisAspectForLinkCheckToo.java");
		file9 = new File(getAbsoluteProjectDir() + "/foo/PkgVisibleClass.java");
		file10 = new File(getAbsoluteProjectDir() + "/foo/NoMembers.java");
	}
	
//	public void testPlainJava() {
//		String[] args = { "-d", 
//				getAbsolutePathOutdir(),
//				file3.getAbsolutePath() };
//		org.aspectj.tools.ajdoc.Main.main(args);
//	}
	
}
