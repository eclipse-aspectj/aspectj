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
package org.aspectj.ajde;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class DuplicateManifestTest extends AjdeTestCase {


	public static final String PROJECT_DIR = "DuplicateManifestTest";

	public static final String injarName  = "injar.jar";
	public static final String aspectjarName  = "aspectjar.jar";
	public static final String outjarName = "outjar.jar";


	/*
	 * Ensure the output directpry in clean
	 */
	protected void setUp() throws Exception {
		super.setUp(PROJECT_DIR);
	}
	
	public void testWeave () {
		Set injars = new HashSet();
		injars.add(openFile(injarName));
		ideManager.getProjectProperties().setInJars(injars);
		Set aspectpath = new HashSet();
		aspectpath.add(openFile(aspectjarName));
		ideManager.getProjectProperties().setAspectPath(aspectpath);
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed", doSynchronousBuild("build.lst"));
		assertTrue(
			"Build warnings",
			ideManager.getCompilationSourceLineTasks().isEmpty());
		compareManifests(openFile(injarName),openFile(outjarName));
		outjar.delete(); // Tidy up !
		File rogueSymFile = new File(currTestDataPath + File.separatorChar + "build.ajsym");
		if (rogueSymFile.exists()) rogueSymFile.delete();
	}
	
	private void compareManifests (File inFile, File outFile) {

		try {
			JarFile inJar = new JarFile(inFile);
			Manifest inManifest = inJar.getManifest();
			inJar.close();
			JarFile outJar = new JarFile(outFile);
			Manifest outManifest = outJar.getManifest();
			outJar.close();
			assertTrue("The manifests in '" + inFile.getCanonicalPath() + "' and '" + outFile.getCanonicalPath() + "' sould be the same",inManifest.equals(outManifest));
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}

}
