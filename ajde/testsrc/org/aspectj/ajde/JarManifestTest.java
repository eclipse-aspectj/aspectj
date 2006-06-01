/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on 16-Mar-2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.aspectj.ajde;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.aspectj.util.FileUtil;

/**
 * @author websterm
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class JarManifestTest extends AjdeTestCase {
 
	public static final String PROJECT_DIR = "JarManifestTest";   
	public static final String srcDir = PROJECT_DIR + "/src"; 
	public static final String binDir = "bin"; 

	public static final String outjarName = "/bin/output.jar"; 

	/**
	 * Constructor for JarResourceCopyTestCase.
	 * @param arg0
	 */
	public JarManifestTest (String arg0) {
		super(arg0);
	}

	/*
	 * Ensure the output directpry in clean
	 */	
	protected void setUp() throws Exception {
		super.setUp(PROJECT_DIR);
		FileUtil.deleteContents(openFile(binDir));
	}

	public void testWeave () {
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed",doSynchronousBuild("weave.lst"));
		assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
		checkManifest(outjar);
	}

	public void testNoweave () {
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed",doSynchronousBuild("noweave.lst"));
		assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
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
