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

import java.io.File;
import java.net.URL;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.util.FileUtil;

import junit.framework.Test;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjLTWTests extends XMLBasedAjcTestCase {

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(org.aspectj.systemtest.ajc150.ataspectj.AtAjLTWTests.class);
	}

	protected URL getSpecFile() {
    return getClassResource("ltw.xml");
	}

	public void testRunThemAllWithJavacCompiledAndLTW() {
		runTest("RunThemAllWithJavacCompiledAndLTW");
	}

	public void testAjcLTWPerClauseTest_XterminateAfterCompilation() {
		runTest("AjcLTW PerClauseTest -XterminateAfterCompilation");
	}

	public void testAjcLTWPerClauseTest_Xreweavable() {
		runTest("AjcLTW PerClauseTest -Xreweavable");
	}

	public void testJavaCAjcLTWPerClauseTest() {
		runTest("JavaCAjcLTW PerClauseTest");
	}

	public void testAjcLTWAroundInlineMungerTest_XterminateAfterCompilation() {
		runTest("AjcLTW AroundInlineMungerTest -XterminateAfterCompilation");
	}

	public void testAjcLTWAroundInlineMungerTest_Xreweavable() {
		runTest("AjcLTW AroundInlineMungerTest");
	}

	public void testAjcLTWAroundInlineMungerTest() {
		runTest("AjcLTW AroundInlineMungerTest");
	}

	public void testAjcLTWAroundInlineMungerTest_XnoInline_Xreweavable() {
		runTest("AjcLTW AroundInlineMungerTest -XnoInline -Xreweavable");
	}

	public void testAjcLTWAroundInlineMungerTest2() {
		runTest("AjcLTW AroundInlineMungerTest2");
	}

	public void testLTWDumpNone() {
		runTest("LTW DumpTest none");

		File f = new File("_ajdump/ataspectj/DumpTest.class");
		assertFalse(f.exists());
		f = new File("_ajdump/_before/ataspectj/DumpTestTheDump.class");
		assertFalse(f.exists());
		f = new File("_ajdump/ataspectj/DumpTestTheDump.class");
		assertFalse(f.exists());
	}

	public void testLTWDump() {
		runTest("LTW DumpTest");

		File f = new File("_ajdump/ataspectj/DumpTest.class");
		assertFalse(f.exists());
		f = new File("_ajdump/_before/ataspectj/DumpTestTheDump.class");
		assertFalse(f.exists());
		f = new File("_ajdump/ataspectj/DumpTestTheDump.class");
		assertTrue(f.exists());

		// tidy up...
		f = new File("_ajdump");
		FileUtil.deleteContents(f);
		f.delete();
	}

	public void testLTWDumpBeforeAndAfter() {
		runTest("LTW DumpTest before and after");

		// before
		File f = new File("_ajdump/_before/com/foo/bar");
		CountingFilenameFilter cff = new CountingFilenameFilter(".class");
		f.listFiles(cff);
		assertEquals("Expected dump file in " + f.getAbsolutePath(), 1, cff.getCount());

		// after
		f = new File("_ajdump/com/foo/bar");
		cff = new CountingFilenameFilter(".class");
		f.listFiles(cff);
		assertEquals("Expected dump file in " + f.getAbsolutePath(), 1, cff.getCount());

		// tidy up...
		f = new File("_ajdump");
		FileUtil.deleteContents(f);
		f.delete();
	}

	public void testLTWDumpClosure() {
		runTest("LTW DumpTest closure");

		File f = new File("_ajdump/ataspectj/DumpTestTheDump$AjcClosure1.class");
		assertTrue("Missing dump file " + f.getAbsolutePath(), f.exists());

		// tidy up...
		f = new File("_ajdump");
		FileUtil.deleteContents(f);
		f.delete();
	}

	public void testLTWDumpProxy() {
		runTest("LTW DumpTest proxy");

		// The working directory is different because this test must be forked
		File dir = new File("../tests/java5/ataspectj");
		File f = new File(dir, "_ajdump/_before/com/sun/proxy");
		CountingFilenameFilter cff = new CountingFilenameFilter(".class");
		f.listFiles(cff);
		assertEquals("Expected dump file in " + f.getAbsolutePath(), 1, cff.getCount());
		f = new File(dir, "_ajdump/com/sun/proxy");
		cff = new CountingFilenameFilter(".class");
		f.listFiles(cff);
		assertEquals(1, cff.getCount());

		// tidy up...
		f = new File(dir, "_ajdump");
		FileUtil.deleteContents(f);
		f.delete();
	}

	public void testLTWDumpJSP() {
		runTest("LTW DumpTest JSP");

		// The working directory is different because this test must be forked
		File f = new File("_ajdump/_before/com/ibm/_jsp");
		CountingFilenameFilter cff = new CountingFilenameFilter(".class");
		f.listFiles(cff);
		assertEquals("Expected dump file in " + f.getAbsolutePath(), 1, cff.getCount());
		f = new File("_ajdump/com/ibm/_jsp");
		cff = new CountingFilenameFilter(".class");
		f.listFiles(cff);
		assertEquals(1, cff.getCount());

		// tidy up...
		f = new File("_ajdump");
		FileUtil.deleteContents(f);
		f.delete();
	}

	public void testAjcAspect1LTWAspect2_Xreweavable() {
		runTest("Ajc Aspect1 LTW Aspect2 -Xreweavable");
	}

	public void testLTWLogSilent() {
		runTest("LTW Log silent");
	}

	public void testLTWLogVerbose() {
		runTest("LTW Log verbose");
	}

	public void testLTWLogVerboseAndShow() {
		runTest("LTW Log verbose and showWeaveInfo");
	}

	public void testLTWLogMessageHandlerClass() {
		runTest("LTW Log messageHandlerClass");
	}

	public void testLTWUnweavable() {
		// actually test that we do LTW proxy and jit classes
		runTest("LTW Unweavable");
	}

	public void testLTWDecp() {
		runTest("LTW Decp");
	}

	public void testLTWDecp2() {
		runTest("LTW Decp2");
	}

	public void testCompileTimeAspectsDeclaredToLTWWeaver() {
		runTest("Compile time aspects declared to ltw weaver");
	}

	public void testConcreteAtAspect() {
		runTest("Concrete@Aspect");
	}

	public void testConcreteAspect() {
		runTest("ConcreteAspect");
	}

	public void testConcretePrecedenceAspect() {
		runTest("ConcretePrecedenceAspect");
	}

	// public void testAspectOfWhenAspectNotInInclude() {
	// runTest("AspectOfWhenAspectNotInInclude");
	// }
	//
	// public void testAspectOfWhenAspectExcluded_pr152873() {
	// runTest("AspectOfWhenAspectExcluded");
	// }

	public void testAspectOfWhenNonAspectExcluded_pr152873() {
		runTest("AspectOfWhenNonAspectExcluded");
	}

	public void testAppContainer() {
		runTest("AppContainer");
	}

	public void testCflowBelowStack() {
		runTest("CflowBelowStack");
	}
}
