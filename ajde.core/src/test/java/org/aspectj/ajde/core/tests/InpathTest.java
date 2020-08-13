/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation
 *    Andy Clement     Copied/changed for -inpath testing 
 *    Helen Hawkins    Changed to use new ajde interface (bug 148190)
 * ******************************************************************/
package org.aspectj.ajde.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import org.aspectj.ajde.core.AjdeCoreTestCase;
import org.aspectj.ajde.core.TestCompilerConfiguration;
import org.aspectj.ajde.core.TestMessageHandler;
import org.aspectj.util.FileUtil;

public class InpathTest extends AjdeCoreTestCase {

	public static final FileFilter aspectjResourceFileFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			return (!name.endsWith(".class") && !name.endsWith(".java") && !name.endsWith(".aj"));

		}
	};

	public static final String indir1Name = "indir1";
	public static final String indir2Name = "indir2";
	public static final String injarName = "injar.jar";
	public static final String outjarName = "/bin/output.jar";

	private String[] build1 = new String[] { "src1" + File.separator + "Main.java" };
	private String[] build2 = new String[] { "src2" + File.separator + "Aspect.java" };

	private TestMessageHandler handler;
	private TestCompilerConfiguration compilerConfig;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("InpathTest");
		handler = (TestMessageHandler) getCompiler().getMessageHandler();
		compilerConfig = (TestCompilerConfiguration) getCompiler().getCompilerConfiguration();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		handler = null;
		compilerConfig = null;
	}

	/**
	 * Inputs to the compiler: inpath = 'indir1/' source = 'src' output = a jar file
	 * 
	 * Expected result = output jar file contains contents of indir1 and class file for source that was in src
	 */
	public void testInpathToOutjar() {
		Set<File> inpath = new HashSet<>();
		File indir1 = openFile(indir1Name);
		inpath.add(indir1);
		compilerConfig.setInpath(inpath);
		File outjar = openFile(outjarName);
		compilerConfig.setOutjar(outjar.getAbsolutePath());

		compilerConfig.setProjectSourceFiles(getSourceFileList(build1));

		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());

		Set<String> expectedOutputJarContents = new HashSet<>();
		// From indir1
		// If we don't copy resources, these next three files won't make it.
		// expectedOutputJarContents.add("META-INF/MANIFEST.MF");
		// expectedOutputJarContents.add("META-INF/test.xml");
		// expectedOutputJarContents.add("test/test.props");
		expectedOutputJarContents.add("test/TestProperties.class");
		// From src
		expectedOutputJarContents.add("Main.class");
		compareJars(indir1, "src", outjar, expectedOutputJarContents);
	}

	/**
	 * Similar to the first test but outputs to a directory rather than a jar.
	 * 
	 */
	public void testInpathToBin() {
		Set<File> inpath = new HashSet<>();
		File indir1 = openFile(indir1Name);
		inpath.add(indir1);
		compilerConfig.setInpath(inpath);
		compilerConfig.setProjectSourceFiles(getSourceFileList(build1));

		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());

		Set<String> expectedBindirContents = new HashSet<>();
		// From indir1
		// If we don't copy resources, these next three files won't make it
		// expectedBindirContents.add("META-INF/MANIFEST.MF");
		// expectedBindirContents.add("META-INF/test.xml");
		// expectedBindirContents.add("test/test.props");
		expectedBindirContents.add("test/TestProperties.class");
		// From src
		expectedBindirContents.add("Main.class");

		compareIndirToBin(indir1, "src", "bin", expectedBindirContents);

	}

	/**
	 * Inputs to the compiler: inpath is 'indir2' that contains a helloworld source file and class file. source is 'src2' which
	 * contains Aspect.java which weaves before advice into the HelloWorld code from 'indir2'
	 * 
	 * Expected result: HelloWorld copied through to output jar and 'weaved'. Compiled version of Aspect.java put into the output
	 * jar. The HelloWorld.java source file is also copied through to the output jar.
	 * 
	 * An extra check is done at the end of this test to verify that HelloWorld has changed size (due to the weaving).
	 */
	public void testInpathToOutjar2() {
		Set<File> inpath = new HashSet<>();
		File indir2 = openFile(indir2Name);
		inpath.add(indir2);
		compilerConfig.setInpath(inpath);
		File outjar = openFile(outjarName);
		compilerConfig.setOutjar(outjar.getAbsolutePath());

		compilerConfig.setProjectSourceFiles(getSourceFileList(build2));

		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());

		Set<String> expectedOutputJarContents = new HashSet<>();
		// From indir1
		expectedOutputJarContents.add("example/HelloWorld.class");

		// If we don't copy resources, this file won't make it
		// expectedOutputJarContents.add("example/HelloWorld.java");
		// From src
		expectedOutputJarContents.add("Aspect.class");

		compareJars(indir2, "src", outjar, expectedOutputJarContents);

		// Extra test. The HelloWorld class from the input directory should have been woven
		// by the aspect - verify that the size of the HelloWorld class in the output directory
		// is a different size to the input version.
		int outputsize = fetchFromJar(outjar, "example/HelloWorld.class");
		try {
			FileInputStream fis = new FileInputStream(openFile(indir2Name + "/example/HelloWorld.class"));
			byte[] filedata = FileUtil.readAsByteArray(fis);
			int inputsize = filedata.length;
			assertTrue("Weaving of Aspect should have occurred but the input and output size for HelloWorld.class are the same",
					(inputsize != outputsize));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * More complex inpath - a jar and a directory
	 * 
	 * Inputs: -inpath injar.jar;indir2 source is 'src2' which contains Aspect.java
	 * 
	 * Expected result: Result should be a directory containing the contents of injar.jar and indir2 and the Aspect.class file.
	 * 
	 */
	public void testInpathAndInjarToBin() {
		Set<File> inpath = new HashSet<>();
		File indir2 = openFile(indir2Name);
		inpath.add(indir2);
		inpath.add(openFile(injarName));
		compilerConfig.setInpath(inpath);
		compilerConfig.setProjectSourceFiles(getSourceFileList(build2));

		doBuild(true);
		assertTrue("Expected no compiler errors or warnings but found " + handler.getMessages(), handler.getMessages().isEmpty());

		Set<String> expectedBindirContents = new HashSet<>();

		// From indir1
		expectedBindirContents.add("example/HelloWorld.class");

		// If we don't copy resources, this file won't make it
		// expectedBindirContents.add("example/HelloWorld.java");
		// From injar.jar
		expectedBindirContents.add("props/resources.properties");
		// From src
		expectedBindirContents.add("Aspect.class");

		compareIndirToBin(indir2, "src", "bin", expectedBindirContents);

		// Check the input and output versions of HelloWorld.class are different sizes
		try {
			FileInputStream fis1 = new FileInputStream(openFile("indir2/example/HelloWorld.class"));
			byte[] filedata1 = FileUtil.readAsByteArray(fis1);
			int inputsize = filedata1.length;
			FileInputStream fis2 = new FileInputStream(openFile("bin/example/HelloWorld.class"));
			byte[] filedata2 = FileUtil.readAsByteArray(fis2);
			int outputsize = filedata2.length;
			assertTrue("Weaving of Aspect should have occurred but the input and output size for HelloWorld.class are the same",
					(outputsize != inputsize));

			fis1.close();
			fis2.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	/*
	 * Ensure -outjar contains all non-Java resouces from injars
	 */
	public void compareJars(File dirFile, String sourceDir, File outjarFile, Set<String> expectedOutputJarContents) {

		try {
			assertTrue(
					"outjar older than injar: outjarLastMod=" + outjarFile.lastModified() + " injarLastMod="
							+ dirFile.lastModified(), (outjarFile.lastModified() >= dirFile.lastModified()));

			// Go through the output jar file, for each element, remove it from
			// the expectedOutputJarContents - when we finish, the expectedOutputJarContents
			// set should be empty!
			JarInputStream outjar = new JarInputStream(new java.io.FileInputStream(outjarFile));
			ZipEntry entry;
			while (null != (entry = outjar.getNextEntry())) {
				String fileName = entry.getName();
				fileName = fileName.replace('\\', '/');
				if (!fileName.contains("CVS")) {
					boolean b = expectedOutputJarContents.remove(fileName);
					assertTrue("Unexpectedly found : " + fileName + " in outjar", b);
				}
				outjar.closeEntry();
			}
			outjar.close();

			assertTrue("Didnt make it into the output jar: " + expectedOutputJarContents.toString(),
					expectedOutputJarContents.isEmpty());
		} catch (IOException ex) {
			fail(ex.toString());
		}
	}

	/*
	 * Ensure -outjar contains all non-Java resouces from source and injars
	 */
	public void compareSourceToOutjar(String indirName, File outjarFile) {
		HashSet<String> resources = new HashSet<>();
		listSourceResources(indirName, resources);

		try {

			JarInputStream outjar = new JarInputStream(new java.io.FileInputStream(outjarFile));
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
	public void compareIndirToBin(File indirFile, String sourceDir, String outdirName, Set<String> expectedOutdirContents) {

		// byte[] inManifest = null;

		File binBase = openFile(outdirName);
		String[] toResources = FileUtil.listFiles(binBase);
		for (String fileName : toResources) {
			if (!fileName.contains("CVS")) {
				boolean b = expectedOutdirContents.remove(fileName);
				assertTrue("Extraneous resources: " + fileName, b);
			}
		}

		assertTrue("Missing resources: " + expectedOutdirContents.toString(), expectedOutdirContents.isEmpty());
	}

	private void listSourceResources(String indirName, Set<String> resources) {
		File srcBase = openFile(indirName);
		File[] fromResources = FileUtil.listFiles(srcBase, aspectjResourceFileFilter);
		for (File fromResource : fromResources) {
			String name = FileUtil.normalizedPath(fromResource, srcBase);
			// System.err.println("Checking "+name);
			if (!name.startsWith("CVS/") && (!name.contains("/CVS/")) && !name.endsWith("/CVS")) {
				resources.add(name);
			}
		}
	}

	// Return the size of specified entry from the output jar file
	public int fetchFromJar(File outjarFile, String filename) {
		int ret = -1;
		try {
			JarInputStream outjar;

			outjar = new JarInputStream(new java.io.FileInputStream(outjarFile));

			ZipEntry entry;
			while (null != (entry = outjar.getNextEntry())) {
				String zipentryname = entry.getName();
				if (zipentryname.equals(filename)) {
					byte[] filedata = FileUtil.readAsByteArray(outjar);
					ret = filedata.length;
					outjar.closeEntry();
					break;
				}
				outjar.closeEntry();
			}
			outjar.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

}
