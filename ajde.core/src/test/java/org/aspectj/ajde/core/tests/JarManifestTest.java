/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - Converted to new interface (bug 148190)
 *******************************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;

public class JarManifestTest extends AjdeCoreTestCase {

	public static final String outjarName = "/bin/output.jar";

	private String[] weave = { "src" + File.separator + "Main.java",
			"src" + File.separator + "Logging.aj" };
	
	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("JarManifestTest");
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		compilerConfig = (TestCompilerConfiguration) getCompiler()
				.getCompilerConfiguration();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}
	
	public void testWeave () {
		File outjar = openFile(outjarName);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(weave));
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found "
				+ handler.getMessages(), handler.getMessages().isEmpty());
		checkManifest(outjar);
	}
	
	public void testNoWeave () {
		File outjar = openFile(outjarName);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(weave));
		compilerConfig.setNonStandardOptions("-XterminateAfterCompilation");
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found "
				+ handler.getMessages(), handler.getMessages().isEmpty());
		checkManifest(outjar);
	}
	
	private void checkManifest (File outjarFile) {
		Manifest manifest = null;

		try {
			JarInputStream outjar = new JarInputStream(new FileInputStream(outjarFile));
			manifest = outjar.getManifest();
			outjar.close();
			assertNotNull("'" + outjarFile.getCanonicalPath() + "' should contain a manifest",manifest);
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}
}
