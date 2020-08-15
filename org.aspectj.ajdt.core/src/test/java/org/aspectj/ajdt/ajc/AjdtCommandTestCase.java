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

package org.aspectj.ajdt.ajc;

//import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import junit.framework.TestCase;

import org.aspectj.ajdt.StreamPrintWriter;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.CountingMessageHandler;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageWriter;
import org.aspectj.org.eclipse.jdt.core.compiler.InvalidInputException;
import org.aspectj.util.FileUtil;

/**
 * Some black-box test is happening here.
 */
public class AjdtCommandTestCase extends TestCase {

	private List tempFiles = new ArrayList();
	private StreamPrintWriter outputWriter = new StreamPrintWriter(new PrintWriter(System.out));
	// private AjdtCommand command = new AjdtCommand();
	private MessageWriter messageWriter = new MessageWriter(outputWriter, false);
	private CountingMessageHandler counter = new CountingMessageHandler(messageWriter);

	public AjdtCommandTestCase(String name) {
		super(name);
		// command.buildArgParser.out = outputWriter;
	}

	private static boolean delete(File file) {
		if ((null == file) || !file.exists()) {
			return true;
		} else if (!file.canWrite()) {
			return false;
		}
		if (file.isDirectory()) {
			FileUtil.deleteContents(file);
		}
		return file.delete();
	}

	public void testIncrementalHandler() throws IOException {
		// verify that AjdtCommand respects handler parm
		// in runCommand and repeatCommand
		final String sig = getClass().getName() + ".testIncrementalHandler";
		boolean runTest = false;
		try {
			runTest = null != System.getProperty(sig);
		} catch (Throwable t) {
		}
		if (!runTest) {
			System.out.println("warning: to run " + sig + "(), set system property " + sig);
			return;
		}
		// setup initial compile
		File testBase = new File("testdata/ajdtCommand");
		assertTrue(testBase.isDirectory());
		assertTrue(testBase.canWrite());
		File genBase = new File(testBase, "genBase");
		tempFiles.add(genBase);
		if (genBase.exists()) {
			FileUtil.deleteContents(genBase);
		} else {
			genBase.mkdirs();
		}
		assertTrue(genBase.canWrite());
		File classesDir = new File(testBase, "classes");
		tempFiles.add(classesDir);
		assertTrue(classesDir.mkdirs());
		File mainSrc = new File(testBase, "Main-1.java");
		File main = new File(genBase, "Main.java");
		FileUtil.copyFile(mainSrc, main);
		assertTrue(main.canRead());
		long initialSize = main.length();

		// do initial compile
		String[] args = new String[] { "-d", classesDir.getPath(), "-classpath", "../lib/test/aspectjrt.jar", main.getPath() };
		AjdtCommand command = new AjdtCommand();
		IMessageHolder holder = new MessageHandler();
		boolean result = command.runCommand(args, holder);
		assertTrue(result);
		assertTrue(!holder.hasAnyMessage(IMessage.WARNING, true));
		int initialMessages = holder.numMessages(null, true);

		// do repeat compile, introducing an error
		mainSrc = new File(testBase, "Main-2.java");
		FileUtil.copyFile(mainSrc, main);
		assertTrue(main.canRead());
		long nextSize = main.length();
		assertTrue(nextSize > initialSize);
		IMessageHolder newHolder = new MessageHandler();
		result = command.repeatCommand(newHolder);

		// verify failed, no effect on first holder, error in second
		assertFalse(result);
		assertEquals(1, newHolder.numMessages(IMessage.ERROR, false));
		assertEquals(initialMessages, holder.numMessages(null, true));
	}

	public void testIncrementalOption() throws InvalidInputException {
		AjdtCommand.genBuildConfig(new String[] { "-incremental" }, counter);

		assertTrue("didn't specify source root", outputWriter.getContents().contains("specify a source root"));

		outputWriter.flushBuffer();
		AjdtCommand.genBuildConfig(new String[] { "-incremental", "-sourceroots", Constants.TESTDATA_PATH + "/src1" }, counter);

		assertTrue(outputWriter.getContents(), outputWriter.getContents().equals(""));

		outputWriter.flushBuffer();
		AjdtCommand.genBuildConfig(new String[] { "-incremental", "testdata/src1/Hello.java" }, counter);

		assertTrue("specified a file", outputWriter.getContents().contains("incremental mode only handles source files using -sourceroots"));
	}

	public void testBadOptionAndUsagePrinting() throws InvalidInputException {
		try {
			AjdtCommand.genBuildConfig(new String[] { "-mubleBadOption" }, counter);
		} catch (AbortException ae) {
		}
		// usage printed by caller to genBuildConfig now...
		assertTrue(outputWriter.getContents() + " contains? " + "Usage",
				outputWriter.getContents().contains("-mubleBadOption"));

	}

	public void testHelpUsagePrinting() {
		String[] args = new String[] { "-help" };

		PrintStream saveOut = System.out;
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		PrintStream newOut = new PrintStream(byteArrayOut);
		System.setOut(newOut);

		try {
			try {

				AjdtCommand.genBuildConfig(args, counter);
			} catch (AbortException ae) {
			}
		} finally {
			System.setOut(saveOut);
		}

		String text = byteArrayOut.toString();
		assertTrue(text + " contains? " + "Usage", text.contains("Usage"));
	}

	public void q() throws InvalidInputException {
		String[] args = new String[] { "-version" };

		PrintStream saveOut = System.out;
		PrintStream saveErr = System.err;
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		ByteArrayOutputStream byteArrayErr = new ByteArrayOutputStream();
		PrintStream newOut = new PrintStream(byteArrayOut);
		PrintStream newErr = new PrintStream(byteArrayErr);
		System.setOut(newOut);
		System.setErr(newErr);

		try {
			try {

				AjdtCommand.genBuildConfig(args, counter);
			} catch (AbortException ae) {
			}
		} finally {
			System.setOut(saveOut);
			System.setErr(saveErr);
		}

		String text = byteArrayOut.toString();
		// String text2 = byteArrayErr.toString();
		assertTrue("version output does not include 'AspectJ Compiler', output was:\n'" + text + "'", text.contains("AspectJ Compiler"));
	}

	public void testNonExistingLstFile() {
		AjdtCommand.genBuildConfig(new String[] { "@mumbleDoesNotExist" }, counter);

		assertTrue(outputWriter.getContents(), outputWriter.getContents().contains("file does not exist"));
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		outputWriter.flushBuffer();
		for (ListIterator iter = tempFiles.listIterator(); iter.hasNext();) {
			File file = (File) iter.next();
			if (delete(file)) {
				iter.remove();
			}
		}
	}
}
