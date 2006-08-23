/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Matthew Webster     initial implementation 
 * ******************************************************************/
package org.aspectj.ajde;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.aspectj.bridge.Constants;
import org.aspectj.util.FileUtil;

public class OutxmlTest extends AjdeTestCase {

	public static final String PROJECT_DIR = "OutxmlTest";
	public static final String BIN_DIR = "bin";
	public static final String OUTJAR_NAME = "/bin/test.jar"; 
	public static final String DEFAULT_AOPXML_NAME = Constants.AOP_AJC_XML; 
	public static final String CUSTOM_AOPXML_NAME = "custom/aop.xml"; 

	/*
	 * Ensure the output directory is clean
	 */
	protected void setUp() throws Exception {
		super.setUp(PROJECT_DIR);
		FileUtil.deleteContents(openFile(BIN_DIR));
	}

	/*
	 * Clean up afterwards
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		FileUtil.deleteContents(openFile(BIN_DIR));
		openFile(BIN_DIR).delete();
	}


	/**
	 * Aim: Test "-outxml" option produces the correct xml file
	 * 
	 */
	public void testOutxmlToFile () {
//		System.out.println("OutxmlTest.testOutxmlToFile() outputpath='" + ideManager.getProjectProperties().getOutputPath() + "'");
		assertTrue("Build failed",doSynchronousBuild("outxml-to-file.lst"));
		assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
		
		File aopxml = openFile(BIN_DIR + "/" + DEFAULT_AOPXML_NAME);
		assertTrue(DEFAULT_AOPXML_NAME + " missing",aopxml.exists());
	}

	/**
	 * Aim: Test "-outxmlfile filename" option produces the correct 
	 * xml file
	 * 
	 */
	public void testOutxmlfileToFile () {
		assertTrue("Build failed",doSynchronousBuild("outxmlfile-to-file.lst"));
		assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
		
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
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed",doSynchronousBuild("outxml-to-outjar.lst"));
		assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
		
		File aopxml = openFile(BIN_DIR + "/" + DEFAULT_AOPXML_NAME);
		assertFalse(DEFAULT_AOPXML_NAME + " should not exisit",aopxml.exists());
		assertJarContainsEntry(outjar,DEFAULT_AOPXML_NAME);
	}

	/**
	 * Aim: Test "-outxmlfile filename" option produces the correct 
	 * xml entry in outjar file
	 * 
	 */
	public void testOutxmlfileToOutjar () {
//		System.out.println("OutxmlTest.testOutxmlToOutjar() outputpath='" + ideManager.getProjectProperties().getOutputPath() + "'");
		File outjar = openFile(OUTJAR_NAME);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed",doSynchronousBuild("outxmlfile-to-outjar.lst"));
		assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
		
		File aopxml = openFile(BIN_DIR + "/" + CUSTOM_AOPXML_NAME);
		assertFalse(CUSTOM_AOPXML_NAME + " should not exisit",aopxml.exists());
		assertJarContainsEntry(outjar,CUSTOM_AOPXML_NAME);
	}

	/**
	 * Aim: Test "-outxml" option produces a warning if "META-INF/aop.xml 
	 * already exists in source
	 * 
	 */
	public void testOutxmlToOutjarWithAop_xml () {
		File f = new File( AjdeTests.testDataPath(PROJECT_DIR + "/src-resources"));
		Set roots = new HashSet();
		roots.add(f);
		ideManager.getProjectProperties().setSourceRoots(roots);
		File outjar = openFile(OUTJAR_NAME);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed: " + ideManager.getCompilationSourceLineTasks(),doSynchronousBuild("outxml-to-outjar-with-aop_xml.lst"));
//		assertTrue("Build warnings: " + ideManager.getCompilationSourceLineTasks(),ideManager.getCompilationSourceLineTasks().isEmpty());
		assertFalse("Build warnings for exisiting resource expected",ideManager.getCompilationSourceLineTasks().isEmpty());
		List msgs = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		String msg = ((NullIdeTaskListManager.SourceLineTask)msgs.get(0)).message.getMessage();
		assertTrue("Wrong message: " + msg,msg.startsWith("-outxml/-outxmlfile option ignored because resource already exists:"));
		
		File aopxml = openFile(BIN_DIR + "/" + DEFAULT_AOPXML_NAME);
		assertFalse(DEFAULT_AOPXML_NAME + " should not exisit",aopxml.exists());
		assertJarContainsEntry(outjar,DEFAULT_AOPXML_NAME);
	}

	private void assertJarContainsEntry (File file, String entryName) {
	
		try {
			JarFile jarFile = new JarFile(file);
			JarEntry jarEntry = jarFile.getJarEntry(entryName);
			assertNotNull(entryName + " missing",jarEntry);
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}

}
