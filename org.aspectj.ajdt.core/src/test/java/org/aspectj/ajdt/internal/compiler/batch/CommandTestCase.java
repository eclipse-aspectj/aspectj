/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.*;

import junit.framework.TestCase;

import org.aspectj.ajdt.ajc.AjdtCommand;
import org.aspectj.ajdt.ajc.Constants;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.bcel.LazyClassGen;

public abstract class CommandTestCase extends TestCase {

	/**
	 * Constructor for CommandTestCase.
	 * 
	 * @param name
	 */
	public CommandTestCase(String name) {
		super(name);
	}

	public static final int[] NO_ERRORS = new int[0];
	public static final int[] TOP_ERROR = new int[0];

	private File sandbox;

	public void checkCompile(String source, int[] expectedErrors) {
		checkCompile(source, new String[0], expectedErrors, getSandboxName());
	}

	protected void runMain(String className) {
		TestUtil.runMain(getSandboxName(), className);
	}
	
	public void checkCompile(String source, String[] extraArgs, int[] expectedErrors) {
		checkCompile(source,extraArgs,expectedErrors,getSandboxName());
	}

	public static void checkCompile(String source, String[] extraArgs, int[] expectedErrors, String sandboxName) {
		List<String> args = new ArrayList<>();
		args.add("-verbose");

		args.add("-d");
		args.add(sandboxName);

		args.add("-classpath");

		args.add(getRuntimeClasspath() + File.pathSeparator + "../lib/junit/junit.jar");

		args.add("-g"); // XXX need this to get sourcefile and line numbers, shouldn't

		Collections.addAll(args, extraArgs);

		args.add(Constants.TESTDATA_PATH + "/" + source);

		runCompiler(args, expectedErrors);
	}

	public void testEmptyForAntJUnitSupport() {
	}

	public void checkMultipleCompile(String source) throws InterruptedException {
		List<String> args = new ArrayList<>();
		args.add("-verbose");

		args.add("-d");
		args.add(getSandboxName());

		args.add("-classpath");
		args.add(getRuntimeClasspath());

		args.add(Constants.TESTDATA_PATH + "/" + source);

		ICommand compiler = runCompiler(args, NO_ERRORS);
		Thread.sleep(100);

		rerunCompiler(compiler);
	}

	public void rerunCompiler(ICommand command) {
		MessageHandler myHandler = new MessageHandler();
		// List recompiledFiles = new ArrayList();
		if (!command.repeatCommand(myHandler)) {
			assertTrue("recompile failed", false);
		}
		assertEquals(0, myHandler.numMessages(IMessage.ERROR, true));
	}

	public static ICommand runCompiler(List<String> args, int[] expectedErrors) {
		ICommand command = new AjdtCommand();
		MessageHandler myHandler = new MessageHandler();
		myHandler.setInterceptor(org.aspectj.tools.ajc.Main.MessagePrinter.TERSE);
		boolean result = command.runCommand((String[]) args.toArray(new String[0]), myHandler);
		System.out.println("result: " + result);
		// System.out.println("errors: " + Arrays.asList(myHandler.getErrors()));
		// System.out.println("warnings: " + Arrays.asList(myHandler.getWarnings()));

		int nErrors = myHandler.numMessages(IMessage.ERROR, IMessageHolder.EQUAL);
		if (expectedErrors == NO_ERRORS) {
			if (0 != nErrors) {
				String s = "" + Arrays.asList(myHandler.getErrors());
				assertTrue("unexpected errors: " + s, false);
			}
		} else if (expectedErrors == TOP_ERROR) { // ?? what is this?
			assertTrue("expected error", nErrors > 0);
		} else {
			List errors = new ArrayList(Arrays.asList(myHandler.getErrors()));
			for (int line : expectedErrors) {
				boolean found = false;
				for (Iterator iter = errors.iterator(); iter.hasNext(); ) {
					IMessage m = (IMessage) iter.next();
					if (m.getSourceLocation() != null && m.getSourceLocation().getLine() == line) {
						found = true;
						iter.remove();
					}
				}
				assertTrue("didn't find error on line " + line, found);
			}
			if (errors.size() > 0) {
				assertTrue("didn't expect errors: " + errors, false);
			}
		}
		return command;
	}

	public static void printGenerated(String path, String name) throws IOException {
		String fullpath = Constants.TESTDATA_PATH + "/" + path;
		LazyClassGen.disassemble(fullpath, name, System.out);
	}

	/** incremental test case adapter to JUnit */
	public class IncCase extends IncrementalCase {
		protected void fail(IMessageHandler handler, String mssg) {
			assertTrue(mssg, false);
		}

		protected void message(IMessage.Kind kind, String mssg, IMessageHandler handler) {
			if ((kind == IMessage.FAIL) || (kind == IMessage.ABORT)) {
				assertTrue(mssg, false);
			} else {
				System.err.println("IncCase " + kind + ": " + mssg); // XXX
			}
			super.message(kind, mssg, handler);
		}

	}

	/** get the location of the org.aspectj.lang & runtime classes */
	protected static String getRuntimeClasspath() {
		StringBuilder classpath = new StringBuilder();
		if (LangUtil.is19VMOrGreater()) {
			classpath.append(LangUtil.getJrtFsFilePath()).append(File.pathSeparator);
		}
		classpath.append(Constants.aspectjrtClasspath());
		return classpath.toString();
	}

	protected String getSandboxName() {
		return sandbox.getAbsolutePath();
	}

	protected void setUp() throws Exception {
		super.setUp();

		sandbox = TestUtil.createEmptySandbox();
	}

}
