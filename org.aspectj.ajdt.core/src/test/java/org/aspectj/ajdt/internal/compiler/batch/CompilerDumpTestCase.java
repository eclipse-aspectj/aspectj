/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.compiler.batch;

import java.io.File;

import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;
import org.aspectj.weaver.Dump;

public class CompilerDumpTestCase extends AjcTestCase {

	public static final String PROJECT_DIR = "DumpTestCase";

	private File baseDir;
	private File dumpFile;
	private IMessage.Kind savedDumpCondition;

	protected void setUp() throws Exception {
		super.setUp();

		baseDir = new File("../org.aspectj.ajdt.core/testdata", PROJECT_DIR);
		dumpFile = null;
		savedDumpCondition = Dump.getDumpOnExit();
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		if (dumpFile != null && dumpFile.exists()) {
			boolean deleted = dumpFile.delete();
			assertTrue("Dump file '" + dumpFile.getPath() + "' could not be deleted", deleted);
		}
		Dump.setDumpOnExit(savedDumpCondition);
	}

	/**
	 * Aim: Dump after successful compile to ensure it contains the command line information.
	 * 
	 * Inputs to the compiler: HelloWorld.java Pointcuts.aj Aspect.aj
	 * 
	 * Expected result = Compile succeeds.
	 */
	public void testDump() {
		String[] args = new String[] { "src/HelloWorld.java", "src/Pointcuts.aj", "src/Aspect.aj" };
		CompilationResult result = ajc(baseDir, args);
		assertNoMessages(result);
		String fileName = Dump.dump("DumpTestCase.testDump()");
		dumpFile = new File(fileName);
		DumpTestCase.assertContents(dumpFile, "Command Line", "HelloWorld.java");
	}

	/**
	 * Aim: Dump after successful compile to ensure it contains warning messages.
	 * 
	 * Inputs to the compiler: HelloWorld.java Pointcuts.aj Aspect.aj DeclareWarning.aj
	 * 
	 * Expected result = Compile succeeds.
	 */
	public void testDumpWithWarnings() {
		String[] args = new String[] { "src/HelloWorld.java", "src/Pointcuts.aj", "src/DeclareWarning.aj" };
		Dump.preserveOnNextReset();
		// CompilationResult result =
		ajc(baseDir, args);
		String fileName = Dump.dump("DumpTestCase.testDumpWithWarnings()");
		dumpFile = new File(fileName);
		DumpTestCase.assertContents(dumpFile, "Compiler Messages", "warning");
	}

	/**
	 * Aim: Dump due to errors.
	 * 
	 * Inputs to the compiler: HelloWorld.java Pointcuts.aj Aspect.aj DeclareError.aj
	 * 
	 * Expected result = Compile fails and dump file created.
	 */
	public void testWithErrors() {
		Dump.setDumpOnExit(IMessage.ERROR);
		String previousFileName = Dump.getLastDumpFileName();
		String[] args = new String[] { "src/HelloWorld.java", "src/Pointcuts.aj", "src/DeclareError.aj" };
		// CompilationResult result =
		ajc(baseDir, args);
		String fileName = Dump.getLastDumpFileName();
		assertTrue("Dump file should be created", !fileName.equals(previousFileName));
		dumpFile = new File(fileName);
		DumpTestCase.assertContents(dumpFile, "Compiler Messages", "error");
	}

}
