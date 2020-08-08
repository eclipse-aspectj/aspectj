/********************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *      Xerox/PARC     initial implementation
 * 		Helen Hawkins  Converted to new interface (bug 148190) and
 *                     to use a sandbox directory
 *******************************************************************/
package org.aspectj.ajde;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajde.ui.utils.TestBuildProgressMonitor;
import org.aspectj.ajde.ui.utils.TestCompilerConfiguration;
import org.aspectj.ajde.ui.utils.TestEditorAdapter;
import org.aspectj.ajde.ui.utils.TestIdeUIAdapter;
import org.aspectj.ajde.ui.utils.TestMessageHandler;
import org.aspectj.ajde.ui.utils.TestRuntimeProperties;
import org.aspectj.testing.util.TestUtil;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AjdeTestCase extends TestCase {

	public final static String testdataSrcDir = "../ajde/testdata";
	protected static File sandboxDir;

	private String projectDir;

	protected void setUp() throws Exception {
		super.setUp();
		// Create a sandbox in which to work
		sandboxDir = TestUtil.createEmptySandbox();
		// AMC - added this next line as a temporary workaround for
		// listener leakage in AsmManager induced by the Ajde test suite.
		// Ajde.getDefault().getModel().removeAllListeners();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		projectDir = null;
		sandboxDir = null;
	}

	/**
	 * Fill in the working directory with the project files and creates a compiler instance for this project
	 */
	public void initialiseProject(String projectName) {

		File projectSrc = new File(testdataSrcDir + File.separatorChar + projectName);
		File destination = new File(getWorkingDir(), projectName);
		if (!destination.exists()) {
			destination.mkdir();
		}
		copy(projectSrc, destination);
		projectDir = destination.getAbsolutePath();

		// need to initialize via AjdeUIManager
		Ajde.getDefault().init(new TestCompilerConfiguration(projectDir), new TestMessageHandler(), new TestBuildProgressMonitor(),
				new TestEditorAdapter(), new TestIdeUIAdapter(), new IconRegistry(), null, // new JFrame(),
				new TestRuntimeProperties(), true);
	}

	/**
	 * @return the working directory
	 */
	protected File getWorkingDir() {
		return sandboxDir;
	}

	/**
	 * @return the absolute path of the project directory for example c:\temp\ajcSandbox\ajcTest15200.tmp\myProject
	 */
	protected String getAbsoluteProjectDir() {
		return projectDir;
	}

	/**
	 * Copy the contents of some directory to another location - the copy is recursive.
	 */
	private void copy(File from, File to) {
		String contents[] = from.list();
		if (contents == null)
			return;
		for (String string : contents) {
			File f = new File(from, string);
			File t = new File(to, string);

			if (f.isDirectory()) {
				t.mkdir();
				copy(f, t);
			} else if (f.isFile()) {
				try {
					org.aspectj.util.FileUtil.copyFile(f, t);
				} catch (IOException e) {
					throw new AssertionFailedError("Unable to copy " + f + " to " + t);
				}
			}
		}
	}

	protected File openFile(String path) {
		return new File(projectDir + File.separatorChar + path);
	}

	public void doBuild(String configFile) {
		getCompilerForConfigFileWithName(configFile).build();
	}

	public AjCompiler getCompilerForConfigFileWithName(String configFileName) {
		return Ajde.getDefault().getCompilerForConfigFile(projectDir + File.separator + configFileName);
	}

	public List getErrorMessages(String configFileName) {
		return ((TestMessageHandler) getCompilerForConfigFileWithName(configFileName).getMessageHandler()).getErrors();
	}

	public List getMessages(String configFileName) {
		return ((TestMessageHandler) getCompilerForConfigFileWithName(configFileName).getMessageHandler()).getMessages();
	}

	protected String genStructureModelExternFilePath(String configFilePath) {
		return configFilePath.substring(0, configFilePath.lastIndexOf(".lst")) + ".ajsym";
	}
}
