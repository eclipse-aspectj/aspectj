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
    	runAjdoc("public","9",files);

        // have passed the "public" modifier as well as
        // one public and one package visible class. There
        // should only be ajdoc for the public class
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/PkgVisibleClass.html");
		assertFalse("ajdoc for PkgVisibleClass shouldn't exist because passed" +
				" the 'public' flag to ajdoc",htmlFile.exists());

        htmlFile = new File(getAbsolutePathOutdir() + "/foo/PlainJava.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		// check there's no private fields within the file, that
		// the file contains the getI() method but doesn't contain
		// the private ClassBar, Bazz and Jazz classes.
		String[] strings = { "private", "getI()","ClassBar", "Bazz", "Jazz"};
		List<String> missing = AjdocOutputChecker.getMissingStringsInFile(htmlFile,strings);
		assertEquals("There should be 4 missing strings",4,missing.size());
		assertTrue(htmlFile.getName() + " should not contain the private modifier",missing.contains("private"));
		assertTrue(htmlFile.getName() + " should not contain the private ClassBar class",missing.contains("ClassBar"));
		assertTrue(htmlFile.getName() + " should not contain the private Bazz class",missing.contains("Bazz"));
		assertTrue(htmlFile.getName() + " should not contain the private Jazz class",missing.contains("Jazz"));
    }

    /**
     * Test that the ajdoc for an aspect has the title "Aspect"
     */
	public void testAJdocHasAspectTitle() throws Exception {
		File[] files = {new File(getAbsoluteProjectDir() + "/pkg/A.aj")};
		runAjdoc("private",AJDocConstants.VERSION,files);
        File htmlFile = new File(getAbsolutePathOutdir() + "/pkg/A.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()+ " - were there compilation errors?");
		}
		assertTrue(htmlFile.getAbsolutePath() + " should have Aspect A as it's title",
				AjdocOutputChecker.containsString(htmlFile,"Aspect A"));
	}

	/**
	 * Test that the ajdoc for a class has the title "Class"
	 */
	public void testAJdocHasClassTitle() throws Exception {
		File[] files = {new File(getAbsoluteProjectDir() + "/pkg/C.java")};
		runAjdoc("private",AJDocConstants.VERSION,files);
        File htmlFile = new File(getAbsolutePathOutdir() + "/pkg/C.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()+ " - were there compilation errors?");
		}
		assertTrue(htmlFile.getAbsolutePath() + " should have Class C as it's title",
				AjdocOutputChecker.containsString(htmlFile,"Class C"));

	}

    /**
     * Test that the ajdoc for an inner aspect is entitled "Aspect" rather
     * than "Class", but that the enclosing class is still "Class"
     */
    public void testInnerAspect() throws Exception {
    	File[] files = {file1, file2};
        runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/ClassA.InnerAspect.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		// ensure that the file is entitled "Aspect ClassA.InnerAspect" rather
		// than "Class ClassA.InnerAspect"

		String[] strings = null;
		strings = new String[] {
			"Aspect ClassA.InnerAspect",
			"<pre>static aspect <span class=\"typeNameLabel\">ClassA.InnerAspect</span>",
			"Class ClassA.InnerAspect",
			"<pre>static class <span class=\"typeNameLabel\">ClassA.InnerAspect</span>"};
		List<String> missingStrings = AjdocOutputChecker.getMissingStringsInFile(htmlFile,strings);
		StringBuilder buf = new StringBuilder();
		for (String str:missingStrings) {
			buf.append(str).append("\n");
		}
		buf.append("HTMLFILE=\n").append(htmlFile).append("\n");
		assertEquals("There should be 2 missing strings:\n"+buf.toString(), 2, missingStrings.size());
		assertTrue(htmlFile.getName() + " should not have Class as it's title",
				missingStrings.contains("Class ClassA.InnerAspect"));
		assertTrue(htmlFile.getName() + " should not have class in its subtitle",
				missingStrings.contains("<pre>static class <span class=\"typeNameLabel\">ClassA.InnerAspect</span>"));

		// get the html file for the enclosing class
        File htmlFileClass = new File(getAbsolutePathOutdir() + "/foo/ClassA.html");
		if (!htmlFileClass.exists()) {
			fail("couldn't find " + htmlFileClass.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		// ensure that the file is entitled "Class ClassA" and
		// has not been changed to "Aspect ClassA"
		String[] classStrings = null;

		if (LangUtil.is13VMOrGreater()) {
			classStrings = new String[] {
				"Class ClassA</h1>",
				"public abstract class <span class=\"typeNameLabel\">ClassA</span>",
				"Aspect ClassA</H2>",
				"public abstract aspect <span class=\"typeNameLabel\">ClassA</span>"};
		} else {
			classStrings = new String[] {
				"Class ClassA</h2>",
				"public abstract class <span class=\"typeNameLabel\">ClassA</span>",
				"Aspect ClassA</H2>",
				"public abstract aspect <span class=\"typeNameLabel\">ClassA</span>"};
		}
		List<String> classMissing = AjdocOutputChecker.getMissingStringsInFile(htmlFileClass,classStrings);
		assertEquals("There should be 2 missing strings:\n"+classMissing,2,classMissing.size());
		assertTrue(htmlFileClass.getName() + " should not have Aspect as it's title",classMissing.contains("Aspect ClassA</H2>"));
		assertTrue(htmlFileClass.getName() + " should not have aspect in its subtitle",
				classMissing.contains("public abstract aspect <span class=\"typeNameLabel\">ClassA</span>"));
	}

    /**
     * Test that all the different types of advice appear
     * with the named pointcut in it's description
     */
    public void testAdviceNamingCoverage() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/AdviceNamingCoverage.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		String[] strings = {
				"after(): named..",
				"afterReturning(int,int): namedWithArgs..",
				"afterThrowing(): named..",
				"before(): named..",
				"around(int): namedWithOneArg..",
				"before(int):",
				"before(int): named()..",
				"before():"};
		List<String> missing = AjdocOutputChecker.getMissingStringsInSection(
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
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/AdvisesRelationshipCoverage.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				"before(): methodExecutionP..",
				"HREF=\"../foo/Point.html#setX(int)\"",
				"before(): constructorExecutionP..",
				"HREF=\"../foo/Point.html#Point()\"",
				"before(): callMethodP..",
				"HREF=\"../foo/Point.html#changeX(int)\"",
				"before(): callConstructorP..",
				"HREF=\"../foo/Point.html#doIt()\"",
				"before(): getP..",
				"HREF=\"../foo/Point.html#getX()\"",
				"before(): setP..",
				"HREF=\"../foo/Point.html\"><tt>foo.Point</tt></A>, <A HREF=\"../foo/Point.html#Point()\"><tt>foo.Point.Point</tt></A>, <A HREF=\"../foo/Point.html#setX(int)\"",
				"before(): initializationP..",
				"HREF=\"../foo/Point.html#Point()\"",
				"before(): staticinitializationP..",
				"HREF=\"../foo/Point.html\"",
				"before(): handlerP..",
				"HREF=\"../foo/Point.html#doIt()\""
		};

		for (int i = 0; i < strings.length - 1; i = i+2) {
			boolean b = AjdocOutputChecker.detailSectionContainsRel(
					htmlFile,"ADVICE DETAIL SUMMARY",strings[i],
					HtmlDecorator.HtmlRelationshipKind.ADVISES,
					strings[i+1]);
			assertTrue(strings[i] + " should advise " + strings[i+1] +
					" in the Advice Detail section", b);
		}

		for (int i = 0; i < strings.length - 1; i = i+2) {
			boolean b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"ADVICE SUMMARY",strings[i],
					HtmlDecorator.HtmlRelationshipKind.ADVISES,
					strings[i+1]);
			assertTrue(strings[i] + " should advise " + strings[i+1] +
					" in the Advice Summary section", b);
		}
    }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a method execution pointcut
     */
    public void testAdvisedByMethodExecution() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				toName("setX(int)"),
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): methodExecutionP..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Detail should have " + strings[0]+" advised by " + strings[1],b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Summary should have " + strings[0]+" advised by " + strings[1],b);
    }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a constructor execution pointcut
     */
    public void testAdvisedByConstructorExecution() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				LangUtil.is11VMOrGreater()?"&lt;init&gt;()":toName("Point()"),
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): constructorExecutionP..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== CONSTRUCTOR DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Constructor Detail should have " + strings[0]+" advised by " + strings[1],b);

		// Pre-JDK 11:
		// This precedes the line containing strings[1]
		// <td class="colOne"><code><span class="memberNameLink"><a href="../foo/Point.html#Point--">Point</a></span>()</code>
		// On JDK 11:
		// This precedes the line containing strings[1]
		// <th class="colConstructorName" scope="row"><code><span class="memberNameLink"><a href="#%3Cinit%3E()">Point</a></span>()</code></th>
		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== CONSTRUCTOR SUMMARY",
				LangUtil.is11VMOrGreater()?"#%3Cinit%3E()":toName("Point()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Constructor Summary should have " + strings[0]+" advised by " + strings[1],b);
    }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a method call pointcut
     */
    public void testAdvisedByMethodCall() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				toName("changeX(int)"),
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): callMethodP..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Detail should have " + strings[0]+" advised by " + strings[1],b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Summary should have " + strings[0]+" advised by " + strings[1],b);
   }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a constructor call pointcut
     */
    public void testAdvisedByConstructorCall() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				toName("doIt()"),
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): callConstructorP..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Detail should have " + strings[0]+" advised by " + strings[1],b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Summary should have " + strings[0]+" advised by " + strings[1],b);
    }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a get pointcut
     */
    public void testAdvisedByGet() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				toName("getX()"),
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): getP..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Detail should have " + strings[0]+" advised by " + strings[1],b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Summary should have " + strings[0]+" advised by " + strings[1],b);
   }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a set pointcut
     */
    public void testAdvisedBySet() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String href = "HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): setP..\"";
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName("setX(int)"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				href);
		assertTrue("the Method Detail should have setX(int) advised by " + href,b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				toName("setX(int)"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				href);
		assertTrue("the Method Summary should have setX(int) advised by " + href,b);

		b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== CONSTRUCTOR DETAIL",
				LangUtil.is11VMOrGreater()?"&lt;init&gt;()":toName("Point()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				href);
		assertTrue("the Constructor Detail should have advised by " + href,b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== CONSTRUCTOR SUMMARY",
				LangUtil.is11VMOrGreater()?"#%3Cinit%3E()":toName("Point()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				href);
		assertTrue("the Constructor Summary should have advised by " + href,b);

		b = AjdocOutputChecker.classDataSectionContainsRel(
				htmlFile,
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				href);
		assertTrue("The class data section should have 'advised by " + href + "'",b);
    }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with an initialization pointcut
     */
    public void testAdvisedByInitialization() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				LangUtil.is11VMOrGreater()?"&lt;init&gt;()":toName("Point()"),
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): initializationP..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,
				"=== CONSTRUCTOR DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Detail should have 'setX(int) advised by ... before()'",b);
		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,
				"=== CONSTRUCTOR SUMMARY",
				LangUtil.is11VMOrGreater()?"#%3Cinit%3E()":strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Summary should have 'setX(int) advised by ... before()'",b);
   }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a staticinitialization pointcut
     */
    public void testAdvisedByStaticInitialization() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String href = "HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): staticinitializationP..\"";
		boolean b = AjdocOutputChecker.classDataSectionContainsRel(
				htmlFile,
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				href);
		assertTrue("The class data section should have 'advised by " + href + "'",b);
    }

    /**
     * Test that the advised by relationship appears in the ajdoc when the
     * advice is associated with a handler pointcut
     */
    public void testAdvisedByHandler() throws Exception {
    	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Point.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				toName("doIt()"),
				"HREF=\"../foo/AdvisesRelationshipCoverage.html#before(): handlerP..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Detail should have " + strings[0]+" advised by " + strings[1],b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Summary should have " + strings[0]+" advised by " + strings[1],b);
    }

	private String toName(String name) {
		if (!LangUtil.is11VMOrGreater()) {
			name = name.replace('(','-');
			name = name.replace(')','-');
		}
		return name;
	}
    /**
     * Test that if have two before advice blocks from the same
     * aspect affect the same method, then both appear in the ajdoc
     */
    public void testTwoBeforeAdvice() throws Exception {
    	File[] files = {new File(getAbsoluteProjectDir() + "/pkg/A2.aj")};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/pkg/C2.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String[] strings = {
				toName("amethod()"),
				"HREF=\"../pkg/A2.html#before(): p..\"",
				"HREF=\"../pkg/A2.html#before(): p2..\""};
		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Detail should have " + strings[0]+" advised by " + strings[1],b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[1]);
		assertTrue("the Method Summary should have " + strings[0]+" advised by " + strings[1],b);

		b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[2]);
		assertTrue("the Method Detail should have " + strings[0]+" advised by " + strings[2],b);

		b = AjdocOutputChecker.summarySectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				strings[0],
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				strings[2]);
		assertTrue("the Method Summary should have " + strings[0]+" advised by " + strings[2],b);
    }

    /**
     * Test that there are no spurious "advised by" entries
     * against the aspect in the ajdoc
     */
    public void testNoSpuriousAdvisedByRels() throws Exception {
       	File[] files = {file4};
    	runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/AdvisesRelationshipCoverage.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}

		String href = "foo.Point.setX(int)";
		boolean b = AjdocOutputChecker.classDataSectionContainsRel(
				htmlFile,
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				href);
		assertFalse("The class data section should not have 'advised by " + href + "'",b);

    }

	public void testCoverage() {
		File[] files = {aspect1,file0,file1,file2,file3,file4,file5,file6,
				file7,file8,file9,file10};
		runAjdoc("private","1.6",files);
	}

	/**
	 * Test that nested aspects appear with "aspect" in their title
	 * when the ajdoc file is written slightly differently (when it's
	 * written for this apsect, it's different than for testInnerAspect())
	 */
	public void testNestedAspect() throws Exception {
		File[] files = {file9};
		runAjdoc("private",AJDocConstants.VERSION,files);

	       File htmlFile = new File(getAbsolutePathOutdir() + "/PkgVisibleClass.NestedAspect.html");
			if (!htmlFile.exists()) {
				fail("couldn't find " + htmlFile.getAbsolutePath()
						+ " - were there compilation errors?");
			}

		// ensure that the file is entitled "Aspect PkgVisibleClass.NestedAspect" rather
		// than "Class PkgVisibleClass.NestedAspect"
		String[] strings = null;
		strings = new String[] {
				"Aspect PkgVisibleClass.NestedAspect",
				"<pre>static aspect <span class=\"typeNameLabel\">PkgVisibleClass.NestedAspect</span>",
				"Class PkgVisibleClass.NestedAspect",
				"<pre>static class <span class=\"typeNameLabel\">PkgVisibleClass.NestedAspect</span>"};
		List<String> missing = AjdocOutputChecker.getMissingStringsInFile(htmlFile,strings);
		assertEquals("There should be 2 missing strings",2,missing.size());
		assertTrue(htmlFile.getName() + " should not have Class as it's title",missing.contains("Class PkgVisibleClass.NestedAspect"));
		assertTrue(htmlFile.getName() + " should not have class in its subtitle",
				missing.contains("<pre>static class <span class=\"typeNameLabel\">PkgVisibleClass.NestedAspect</span>"));
		// get the html file for the enclosing class
        File htmlFileClass = new File(getAbsolutePathOutdir() + "/PkgVisibleClass.html");
		if (!htmlFileClass.exists()) {
			fail("couldn't find " + htmlFileClass.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		// ensure that the file is entitled "Class PkgVisibleClass" and
		// has not been changed to "Aspect PkgVisibleClass"
		String[] classStrings = null;
		if (LangUtil.is13VMOrGreater()) {
			classStrings = new String[] {
				"Class PkgVisibleClass</h1>",
				"<pre>class <span class=\"typeNameLabel\">PkgVisibleClass</span>",
				"Aspect PkgVisibleClass</h2>",
				"<pre>aspect <span class=\"typeNameLabel\">PkgVisibleClass</span>"};
		} else {
			classStrings = new String[] {
				"Class PkgVisibleClass</h2>",
				"<pre>class <span class=\"typeNameLabel\">PkgVisibleClass</span>",
				"Aspect PkgVisibleClass</h2>",
				"<pre>aspect <span class=\"typeNameLabel\">PkgVisibleClass</span>"};
		}
		List<String> classMissing = AjdocOutputChecker.getMissingStringsInFile(htmlFileClass,classStrings);
		assertEquals("There should be 2 missing strings",2,classMissing.size());
		assertTrue(htmlFileClass.getName() + " should not have Aspect as it's title",
				classMissing.contains("Aspect PkgVisibleClass</h2>"));
		assertTrue(htmlFileClass.getName() + " should not have aspect in its subtitle",
				classMissing.contains("<pre>aspect <span class=\"typeNameLabel\">PkgVisibleClass</span>"));
	}

	/**
	 * Test that in the case when you have a nested aspect whose
	 * name is part of the enclosing class, for example a class called
	 * ClassWithNestedAspect has nested aspect called NestedAspect,
	 * that the titles for the ajdoc are correct.
	 */
	public void testNestedAspectWithSimilarName() throws Exception {
    	File[] files = {new File(getAbsoluteProjectDir() + "/pkg/ClassWithNestedAspect.java")};
        runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/pkg/ClassWithNestedAspect.NestedAspect.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}
		// ensure that the file is entitled "Aspect ClassWithNestedAspect.NestedAspect"
		// rather than "Class ClassWithNestedAspect.NestedAspect"
		String[] strings = null;
		strings = new String [] {
			"Aspect ClassWithNestedAspect.NestedAspect",
			"<pre>static aspect <span class=\"typeNameLabel\">ClassWithNestedAspect.NestedAspect</span>",
			"Class ClassWithNestedAspect.NestedAspect",
			"<pre>static class <span class=\"typeNameLabel\">ClassWithNestedAspect.NestedAspect</span>"};
		List<String> missing = AjdocOutputChecker.getMissingStringsInFile(htmlFile,strings);
		assertEquals("There should be 2 missing strings",2,missing.size());
		assertTrue(htmlFile.getName() + " should not have Class as it's title",missing.contains("Class ClassWithNestedAspect.NestedAspect"));
		assertTrue(htmlFile.getName() + " should not have class in its subtitle",
				missing.contains("<pre>static class <span class=\"typeNameLabel\">ClassWithNestedAspect.NestedAspect</span>"));

		// get the html file for the enclosing class
        File htmlFileClass = new File(getAbsolutePathOutdir() + "/pkg/ClassWithNestedAspect.html");
		if (htmlFileClass == null || !htmlFileClass.exists()) {
			fail("couldn't find " + htmlFileClass.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		// ensure that the file is entitled "Class ClassWithNestedAspect" and
		// has not been changed to "Aspect ClassWithNestedAspect"
		String[] classStrings = null;
		if (LangUtil.is13VMOrGreater()) {
			classStrings = new String[] {
				"Class ClassWithNestedAspect</h1>",
				"public class <span class=\"typeNameLabel\">ClassWithNestedAspect</span>",
				"Aspect ClassWithNestedAspect</h2>",
				"public aspect <span class=\"typeNameLabel\">ClassWithNestedAspect</span>"};
		} else {
			classStrings = new String[] {
				"Class ClassWithNestedAspect</h2>",
				"public class <span class=\"typeNameLabel\">ClassWithNestedAspect</span>",
				"Aspect ClassWithNestedAspect</h2>",
				"public aspect <span class=\"typeNameLabel\">ClassWithNestedAspect</span>"};
		}
		List<String> classMissing = AjdocOutputChecker.getMissingStringsInFile(htmlFileClass,classStrings);
		assertEquals("There should be 2 missing strings",2,classMissing.size());
		assertTrue(htmlFileClass.getName() + " should not have Aspect as it's title",
				classMissing.contains("Aspect ClassWithNestedAspect</h2>"));
		assertTrue(htmlFileClass.getName() + " should not have aspect in its subtitle",
				classMissing.contains("public aspect <span class=\"typeNameLabel\">ClassWithNestedAspect</span>"));
	}

	/**
	 * Test that everythings being decorated correctly within the ajdoc
	 * for the aspect when the aspect is a nested aspect
	 */
	public void testAdviceInNestedAspect() throws Exception {
    	File[] files = {new File(getAbsoluteProjectDir() + "/pkg/ClassWithNestedAspect.java")};
        runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/pkg/ClassWithNestedAspect.NestedAspect.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		boolean b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"ADVICE DETAIL SUMMARY",
				"before(): p..",
				HtmlDecorator.HtmlRelationshipKind.ADVISES,
				"HREF=\"../pkg/ClassWithNestedAspect.html#amethod()\"");
		assertTrue("Should have 'before(): p.. advises HREF=\"../pkg/ClassWithNestedAspect.html#amethod()\"" +
				"' in the Advice Detail section", b);
        b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"ADVICE SUMMARY",
					"before(): p..",
					HtmlDecorator.HtmlRelationshipKind.ADVISES,
					"HREF=\"../pkg/ClassWithNestedAspect.html#amethod()\"");
		assertTrue("Should have 'before(): p.. advises HREF=\"../pkg/ClassWithNestedAspect.html#amethod()\"" +
				"' in the Advice Summary section", b);

	}

	/**
	 * Test that everythings being decorated correctly within the ajdoc
	 * for the advised class when the aspect is a nested aspect
	 */
	public void testAdvisedByInNestedAspect() throws Exception {
    	File[] files = {new File(getAbsoluteProjectDir() + "/pkg/ClassWithNestedAspect.java")};
        runAjdoc("private",AJDocConstants.VERSION,files);

        File htmlFile = new File(getAbsolutePathOutdir() + "/pkg/ClassWithNestedAspect.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath()
					+ " - were there compilation errors?");
		}

		boolean b = AjdocOutputChecker.containsString(htmlFile,"POINTCUT SUMMARY ");
		assertFalse(htmlFile.getName() + " should not contain a pointcut summary section",b);
		b = AjdocOutputChecker.containsString(htmlFile,"ADVICE SUMMARY ");
		assertFalse(htmlFile.getName() + " should not contain an adivce summary section",b);
		b = AjdocOutputChecker.containsString(htmlFile,"POINTCUT DETAIL ");
		assertFalse(htmlFile.getName() + " should not contain a pointcut detail section",b);
		b = AjdocOutputChecker.containsString(htmlFile,"ADVICE DETAIL ");
		assertFalse(htmlFile.getName() + " should not contain an advice detail section",b);

		b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				"HREF=\"../pkg/ClassWithNestedAspect.NestedAspect.html#before(): p..\"");
		assertTrue("Should have 'amethod() advised by " +
				"HREF=\"../pkg/ClassWithNestedAspect.NestedAspect.html#before(): p..\"" +
				"' in the Method Detail section", b);

		b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD DETAIL",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
				"pkg.ClassWithNestedAspect.NestedAspect.NestedAspect.before(): p..");
		assertFalse("Should not have the label " +
				"pkg.ClassWithNestedAspect.NestedAspect.NestedAspect.before(): p.." +
				" in the Method Detail section", b);

		b = AjdocOutputChecker.summarySectionContainsRel(
					htmlFile,"=== METHOD SUMMARY",
					toName("amethod()"),
					HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
					"HREF=\"../pkg/ClassWithNestedAspect.NestedAspect.html#before(): p..\"");
		assertTrue("Should have 'amethod() advised by " +
				"HREF=\"../pkg/ClassWithNestedAspect.NestedAspect.html#before(): p..\"" +
				"' in the Method Summary section", b);

		b = AjdocOutputChecker.detailSectionContainsRel(
				htmlFile,"=== METHOD SUMMARY",
				toName("amethod()"),
				HtmlDecorator.HtmlRelationshipKind.ADVISED_BY,
		"pkg.ClassWithNestedAspect.NestedAspect.NestedAspect.before(): p..");
		assertFalse("Should not have the label " +
				"pkg.ClassWithNestedAspect.NestedAspect.NestedAspect.before(): p.." +
				" in the Method Summary section", b);

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
