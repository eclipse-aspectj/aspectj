/********************************************************************
 * Copyright (c) 2003 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten      initial implementation 
 *     Helen Hawkins    Converted to new interface (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.util.FileUtil;

public class ResourceCopyTest extends AjdeCoreTestCase {

	public static final String PROJECT_DIR = "bug-36071a";
	public static final String srcDir = PROJECT_DIR + "/src";
	public static final String binDir = "bin";

	public static final String injar1Name = "input1.jar";
	public static final String injar2Name = "input2.jar";
	public static final String outjarName = "/bin/output.jar";

	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	private String[] config1 = new String[] { "src" + File.separator + "Main.java",
			"src" + File.separator + "testsrc" + File.separator + "TestProperties.java" };

	private String[] config2 = new String[] { "src" + File.separator + "aspects" + File.separator + "Logging.java" };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject(PROJECT_DIR);
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}

	public void testSrcToBin() {
		assertTrue("Expected there to be no compiler messages but found " + handler.getMessages(), handler.getMessages().isEmpty());
		compilerConfig.setProjectSourceFiles(getSourceFileList(config1));
		doBuild(true);
		compareDirs("src", "bin");
	}

	public void testInjarsToOutjar() {
		Set<File> injars = new HashSet<>();
		File injar1 = openFile(injar1Name);
		injars.add(injar1);
		compilerConfig.setInpath(injars);
		File outjar = openFile(outjarName);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(config2));
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());
		compareJars(injar1, "src", outjar);
	}

	public void testDuplicateResources() {
		Set<File> injars = new HashSet<>();
		File injar1 = openFile(injar1Name);
		File injar2 = openFile(injar2Name);
		injars.add(injar1);
		injars.add(injar2);
		compilerConfig.setInpath(injars);
		File outjar = openFile(outjarName);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(config2));
		doBuild(true);
		assertFalse("Expected compiler errors or warnings but didn't find any", handler.getMessages().isEmpty());

		List<TestMessageHandler.TestMessage> msgs = handler.getMessages();
		String exp = "duplicate resource: ";
		String found = msgs.get(0).getContainedMessage().getMessage();
		assertTrue("Expected message to start with 'duplicate resource:' but found" + " message " + found, found.startsWith(exp));
		compareJars(injar1, "src", outjar);
	}

	public void testSrcToOutjar() {
		File outjar = openFile(outjarName);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(config1));
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());
		compareSourceToOutjar("src", outjar);
	}

	public void testInjarsToBin() {
		Set<File> injars = new HashSet<>();
		File injar1 = openFile(injar1Name);
		injars.add(injar1);
		compilerConfig.setInpath(injars);
		compilerConfig.setProjectSourceFiles(getSourceFileList(config2));
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());
		compareInjarsToBin(injar1, "src", "bin");
	}

	// BAH! keeps whinging about CVS extraneous resources
	// public void testInjarsToOddBin () {
	// Set injars = new HashSet();
	// File injar1 = openFile(injar1Name);
	// injars.add(injar1);
	// ideManager.getProjectProperties().setOutputPath("crazy.jar");
	// ideManager.getProjectProperties().setInJars(injars);
	// assertTrue("Build failed",doSynchronousBuild("config2.lst"));
	// assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
	// compareInjarsToBin(injar1,"src","crazy.jar");
	// }

	public void testInjarsToOutjarOddNames() {
		Set<File> injars = new HashSet<>();
		File injar1 = openFile("input1");
		File outjar = openFile(outjarName + ".fozout");
		injars.add(injar1);
		compilerConfig.setInpath(injars);
		compilerConfig.setOutjar(outjar.getAbsolutePath());
		compilerConfig.setProjectSourceFiles(getSourceFileList(config2));
		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());
		compareJars(injar1, "src", outjar);
	}

	/*
	 * Ensure bin contains all non-Java resouces from source and injars
	 */
	public void compareDirs(String indirName, String outdirName) {
		File binBase = openFile(outdirName);
		File[] toResources = FileUtil.listFiles(binBase, aspectjResourceFileFilter);

		HashSet<String> resources = new HashSet<>();
		listSourceResources(indirName, resources);

		for (File toResource : toResources) {
			String fileName = FileUtil.normalizedPath(toResource, binBase);
			boolean b = resources.remove(fileName);
			assertTrue("Extraneous resources: " + fileName, b);
		}

		assertTrue("Missing resources: " + resources.toString(), resources.isEmpty());
	}

	private void listSourceResources(String indirName, Set<String> resources) {
		File srcBase = openFile(indirName);
		File[] fromResources = FileUtil.listFiles(srcBase, aspectjResourceFileFilter);
		for (File fromResource : fromResources) {
			String name = FileUtil.normalizedPath(fromResource, srcBase);
			if (!name.startsWith("CVS/") && (!name.contains("/CVS/")) && !name.endsWith("/CVS")) {
				resources.add(name);
			}
		}
	}

	public static final FileFilter aspectjResourceFileFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			boolean isCVSRelated = name.contains("/cvs/");
			return (!isCVSRelated && !name.endsWith(".class") && !name.endsWith(".java") && !name.endsWith(".aj"));
		}
	};

	/*
	 * Ensure -outjar contains all non-Java resouces from injars
	 */
	public void compareJars(File injarFile, String indirName, File outjarFile) {

		HashSet<String> resources = new HashSet<>();

		try {
			assertTrue(
					"outjar older than injar: outjarLastMod=" + outjarFile.lastModified() + " injarLastMod="
							+ injarFile.lastModified(), (outjarFile.lastModified() >= injarFile.lastModified()));
			byte[] inManifest = listJarResources(injarFile, resources, true);
			listSourceResources(indirName, resources);

			ZipInputStream outjar = new ZipInputStream(new java.io.FileInputStream(outjarFile));
			ZipEntry entry;
			while (null != (entry = outjar.getNextEntry())) {
				String fileName = entry.getName();
				if (!fileName.endsWith(".class")) {

					/* Ensure we copied right JAR manifest */
					if (fileName.equalsIgnoreCase("meta-inf/Manifest.mf")) {
						byte[] outManifest = FileUtil.readAsByteArray(outjar);
						assertTrue("Wrong manifest has been copied", Arrays.equals(inManifest, outManifest));
					}

					boolean b = resources.remove(fileName);
					assertTrue(fileName, b);
				}
				outjar.closeEntry();
			}
			outjar.close();
			resources.remove("META-INF/");
			assertTrue(resources.toString(), resources.isEmpty());
		} catch (IOException ex) {
			fail(ex.toString());
		}
	}

	/*
	 * Ensure -outjar conatins all non-Java resouces from source and injars
	 */
	public void compareSourceToOutjar(String indirName, File outjarFile) {
		HashSet<String> resources = new HashSet<>();
		listSourceResources(indirName, resources);

		try {

			ZipInputStream outjar = new JarInputStream(new java.io.FileInputStream(outjarFile));
			ZipEntry entry;
			while (null != (entry = outjar.getNextEntry())) {
				String fileName = entry.getName();
				if (!fileName.endsWith(".class")) {
					boolean b = resources.remove(fileName);
					assertTrue(fileName, b);
				}
				outjar.closeEntry();
			}
			outjar.close();

			assertTrue("Missing resources: " + resources.toString(), resources.isEmpty());
		} catch (IOException ex) {
			fail(ex.toString());
		}
	}

	/*
	 * Ensure bin contains all non-Java resouces from source and injars
	 */
	public void compareInjarsToBin(File injarFile, String indirName, String outdirName) {

		HashSet<String> resources = new HashSet<>();

		try {
			byte[] inManifest = listJarResources(injarFile, resources, false);
			listSourceResources(indirName, resources);

			File binBase = openFile(outdirName);
			File[] toResources = FileUtil.listFiles(binBase, aspectjResourceFileFilter);
			for (File toResource : toResources) {
				String fileName = FileUtil.normalizedPath(toResource, binBase);

				/* Ensure we copied the right JAR manifest */
				if (fileName.equalsIgnoreCase("meta-inf/Manifest.mf")) {
					byte[] outManifest = FileUtil.readAsByteArray(toResource);
					assertTrue("Wrong manifest has been copied", Arrays.equals(inManifest, outManifest));
				}
				boolean b = resources.remove(fileName);
				assertTrue("Extraneous resources: " + fileName, b);
			}

			assertTrue("Missing resources: " + resources.toString(), resources.isEmpty());
		} catch (IOException ex) {
			fail(ex.toString());
		}
	}

	/**
	 * Look in the specified jar file for resources (anything not .class) and add it the resources Set.
	 * 
	 * @param injarFile jar file to open up
	 * @param resources the set where resources should be accumulated
	 * @param wantDirectories should any directories found in the jar be included
	 * @return the byte data for any discovered manifest
	 */
	private byte[] listJarResources(File injarFile, Set<String> resources, boolean wantDirectories) {
		byte[] manifest = null;

		try {
			ZipInputStream injar = new ZipInputStream(new java.io.FileInputStream(injarFile));
			ZipEntry entry;
			while (null != (entry = injar.getNextEntry())) {
				String fileName = entry.getName();
				if (entry.isDirectory()) {
					if (wantDirectories) {
						resources.add(fileName);
					}
				} else if (!fileName.endsWith(".class")) {

					/* JAR manifests shouldn't be copied */
					if (fileName.equalsIgnoreCase("meta-inf/Manifest.mf")) {
						manifest = FileUtil.readAsByteArray(injar);
					}
					resources.add(fileName);
				}
				injar.closeEntry();
			}
			injar.close();
		} catch (IOException ex) {
			fail(ex.toString());
		}

		return manifest;
	}
}
