/********************************************************************
 * Copyright (c) 2003 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Webster     initial implementation
 *     Helen Hawkins       Converted to new interface (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.ajde.core.TestMessageHandler.TestMessage;
import org.aspectj.bridge.Constants;

public class OutxmlTest extends AjdeCoreTestCase {

	public static final String PROJECT_DIR = "OutxmlTest";
	public static final String BIN_DIR = "bin";
	public static final String OUTJAR_NAME = "/bin/test.jar";
	public static final String DEFAULT_AOPXML_NAME = Constants.AOP_AJC_XML;
	public static final String CUSTOM_AOPXML_NAME = "custom/aop.xml";

	private String[] files = new String[]{
			"src" + File.separator + "TestAbstractAspect.aj",
			"src" + File.separator + "TestClass.java",
			"src" + File.separator + "TestConcreteAspect.aj",
			"src" + File.separator + "TestInterface.java"
	};

	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject(PROJECT_DIR);
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		compilerConfig = (TestCompilerConfiguration) getCompiler()
				.getCompilerConfiguration();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}

	/**
	 * Aim: Test "-outxml" option produces the correct xml file
	 */
	public void testOutxmlToFile () {
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-outxml");
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found "
				+ handler.getMessages(), handler.getMessages().isEmpty());
		File aopxml = openFile(BIN_DIR + "/" + DEFAULT_AOPXML_NAME);
		assertTrue(DEFAULT_AOPXML_NAME + " missing",aopxml.exists());
	}

	/**
	 * Aim: Test "-outxmlfile filename" option produces the correct
	 * xml file
	 *
	 */
	public void testOutxmlfileToFile () {
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-outxmlfile custom/aop.xml");
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found "
				+ handler.getMessages(), handler.getMessages().isEmpty());

		File aopxml = openFile(BIN_DIR + "/" + CUSTOM_AOPXML_NAME);
		assertTrue(CUSTOM_AOPXML_NAME + " missing",aopxml.exists());
	}

	/**
	 * Aim: Test "-outxml" option produces the correct
	 * xml entry in outjar file
	 *
	 */
	public void testOutxmlToOutjar () {
		File outjar = openFile(OUTJAR_NAME);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-outxml");
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found "
				+ handler.getMessages(), handler.getMessages().isEmpty());

		File aopxml = openFile(BIN_DIR + "/" + DEFAULT_AOPXML_NAME);
		assertFalse(DEFAULT_AOPXML_NAME + " should not exisit",aopxml.exists());
		assertJarContainsEntry(outjar,DEFAULT_AOPXML_NAME);
	}

	/**
	 * Aim: Test "-outxml" option produces a warning if "META-INF/aop.xml
	 * already exists in source
	 *
	 */
	public void testOutxmlToOutjarWithAop_xml () {
		File f = new File( getAbsoluteProjectDir() + File.separator + "src-resources" + File.separator + "testjar.jar");
		Set<File> roots = new HashSet<>();
		roots.add(f);
		compilerConfig.setInpath(roots);

		File outjar = openFile(OUTJAR_NAME);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setNonStandardOptions("-outxml");
		doBuild(true);
		assertFalse("Expected compiler errors or warnings but didn't find any "
				+ handler.getMessages(), handler.getMessages().isEmpty());

		List<TestMessage> msgs = handler.getMessages();
		String msg = msgs.get(0).getContainedMessage().getMessage();
		String exp = "-outxml/-outxmlfile option ignored because resource already exists:";
		assertTrue("Expected message to start with : " + exp + " but found message " + msg,msg.startsWith(exp));

		File aopxml = openFile(BIN_DIR + "/" + DEFAULT_AOPXML_NAME);
		assertFalse(DEFAULT_AOPXML_NAME + " should not exisit",aopxml.exists());
		assertJarContainsEntry(outjar,DEFAULT_AOPXML_NAME);
	}


	/**
	 * Aim: Test "-outxmlfile filename" option produces the correct
	 * xml entry in outjar file
	 */
	public void testOutxmlfileToOutjar () {
		File outjar = openFile(OUTJAR_NAME);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(files));
		compilerConfig.setNonStandardOptions("-outxmlfile custom/aop.xml");
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found "
				+ handler.getMessages(), handler.getMessages().isEmpty());

		File aopxml = openFile(BIN_DIR + "/" + CUSTOM_AOPXML_NAME);
		assertFalse(CUSTOM_AOPXML_NAME + " should not exisit",aopxml.exists());
		assertJarContainsEntry(outjar,CUSTOM_AOPXML_NAME);
	}

	private void assertJarContainsEntry (File file, String entryName) {
		try (JarFile jarFile = new JarFile(file)) {
			JarEntry jarEntry = jarFile.getJarEntry(entryName);
			assertNotNull(entryName + " missing",jarEntry);
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}
}
