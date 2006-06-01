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
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.*;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.zip.*;

import org.aspectj.util.FileUtil;

/**
 * @author websterm
 */
public class InpathTestcase extends AjdeTestCase {

	public static final String PROJECT_DIR = "InpathTest";
	public static final String binDir = "bin";

	public static final String indir1Name = "indir1";
	public static final String indir2Name = "indir2";
	public static final String injarName  = "injar.jar";
	public static final String outjarName = "/bin/output.jar";

	/**
	 * Constructor for JarResourceCopyTestCase.
	 * @param arg0
	 */
	public InpathTestcase(String arg0) {
		super(arg0);
	}



	/*
	 * Ensure the output directpry in clean
	 */
	protected void setUp() throws Exception {
		super.setUp(PROJECT_DIR);
		FileUtil.deleteContents(openFile(binDir));
	}



	/**
	 * Inputs to the compiler:
	 *   inpath = 'indir1/'
	 *   source = 'src'
	 *   output = a jar file
	 * 
	 * Expected result = output jar file contains contents of indir1 and 
	 *                   class file for source that was in src
	 */
	public void testInpathToOutjar() {
		Set inpath = new HashSet();
		File indir1 = openFile(indir1Name);
		inpath.add(indir1);
		ideManager.getProjectProperties().setInpath(inpath);
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed", doSynchronousBuild("build.lst"));
		assertTrue("Build warnings", ideManager.getCompilationSourceLineTasks().isEmpty());

		Set expectedOutputJarContents = new HashSet();
		// From indir1
// If we don't copy resources, these next three files won't make it.
//		expectedOutputJarContents.add("META-INF/MANIFEST.MF");
//		expectedOutputJarContents.add("META-INF/test.xml");
//		expectedOutputJarContents.add("test/test.props");
		expectedOutputJarContents.add("test/TestProperties.class");
		// From src
		expectedOutputJarContents.add("Main.class");

		compareJars(indir1, "src", outjar, expectedOutputJarContents);

		// Tidy up
		FileUtil.deleteContents(openFile(binDir));
		openFile(binDir).delete();
		assertFalse(openFile(binDir).exists());
	}



	/**
	 * Similar to the first test but outputs to a directory rather than
	 * a jar.
	 *
	 */
	public void testInpathToBin() {
		Set inpath = new HashSet();
		File indir1 = openFile(indir1Name);
		inpath.add(indir1);
		ideManager.getProjectProperties().setInpath(inpath);
		assertTrue("Build failed", doSynchronousBuild("build.lst"));
		assertTrue(
			"Build warnings",
			ideManager.getCompilationSourceLineTasks().isEmpty());

		Set expectedBindirContents = new HashSet();
		// From indir1
// If we don't copy resources, these next three files won't make it
//		expectedBindirContents.add("META-INF/MANIFEST.MF");
//		expectedBindirContents.add("META-INF/test.xml");
//		expectedBindirContents.add("test/test.props");
		expectedBindirContents.add("test/TestProperties.class");
		// From src
		expectedBindirContents.add("Main.class");

		compareIndirToBin(indir1, "src", "bin", expectedBindirContents);
		
		// Tidy up
		FileUtil.deleteContents(openFile(binDir));
		openFile(binDir).delete();
		assertFalse(openFile(binDir).exists());
	}



	/**
	 * Inputs to the compiler:
	 *   inpath is 'indir2' that contains a helloworld source file and class file.
	 *   source is 'src2' which contains Aspect.java which weaves before advice into the HelloWorld code from 'indir2'
	 * 
	 * Expected result: HelloWorld copied through to output jar and 'weaved'.  Compiled version of Aspect.java put into
	 *                  the output jar.  The HelloWorld.java source file is also copied through to the output jar.
	 * 
	 * An extra check is done at the end of this test to verify that HelloWorld has changed size (due to the weaving).
	 */
	public void testInpathToOutjar2() {
		Set inpath = new HashSet();
		File indir2 = openFile(indir2Name);
		inpath.add(indir2);
		ideManager.getProjectProperties().setInpath(inpath);
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed", doSynchronousBuild("build2.lst"));
		assertTrue(
			"Build warnings",
			ideManager.getCompilationSourceLineTasks().isEmpty());

		Set expectedOutputJarContents = new HashSet();
		// From indir1
		expectedOutputJarContents.add("example/HelloWorld.class");

//		If we don't copy resources, this file won't make it
//		expectedOutputJarContents.add("example/HelloWorld.java");
		// From src
		expectedOutputJarContents.add("Aspect.class");

		compareJars(indir2, "src", outjar, expectedOutputJarContents);

		// Extra test.  The HelloWorld class from the input directory should have been woven
		// by the aspect - verify that the size of the HelloWorld class in the output directory
		// is a different size to the input version.
		int outputsize = fetchFromJar(outjar, "example/HelloWorld.class");				  
		try {
			FileInputStream fis = new FileInputStream(openFile(indir2Name+"/example/HelloWorld.class"));
			byte[] filedata = FileUtil.readAsByteArray(fis);
			int inputsize = filedata.length;
			assertTrue("Weaving of Aspect should have occurred but the input and output size for HelloWorld.class are the same",
				(inputsize!=outputsize));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		FileUtil.deleteContents(openFile(binDir));
		openFile(binDir).delete();
		assertFalse(openFile(binDir).exists());
	}
	
	
	
	/**
	 * More complex inpath - a jar and a directory
	 * 
	 * Inputs:
	 *   -inpath injar.jar;indir2
	 *   source is 'src2' which contains Aspect.java
	 * 
	 * Expected result: Result should be a directory containing the contents of injar.jar and indir2 and the
	 *                  Aspect.class file.
	 *
	 */
	public void testInpathAndInjarToBin() {
		Set inpath = new HashSet();
		File indir2 = openFile(indir2Name);
		inpath.add(indir2);
		inpath.add(openFile(injarName));
		ideManager.getProjectProperties().setInpath(inpath);
		assertTrue("Build failed", doSynchronousBuild("build2.lst"));
		assertTrue("Build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());

		Set expectedBindirContents = new HashSet();

		// From indir1
		expectedBindirContents.add("example/HelloWorld.class");

//		If we don't copy resources, this file won't make it
//		expectedBindirContents.add("example/HelloWorld.java");
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
					  (outputsize!=inputsize));

			fis1.close();
			fis2.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		FileUtil.deleteContents(openFile(binDir));
		openFile(binDir).delete();
		assertFalse(openFile(binDir).exists());
	}



	// Return the size of specified entry from the output jar file
	public int fetchFromJar(File outjarFile, String filename) {
		int ret = -1;
		try {
			JarInputStream outjar;

			outjar =
				new JarInputStream(new java.io.FileInputStream(outjarFile));

			ZipEntry entry;
			while (null != (entry = (ZipEntry)outjar.getNextEntry())) {
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
	/*
	 * Ensure -outjar contains all non-Java resouces from injars
	 */
	public void compareJars(
		File dirFile,
		String sourceDir,
		File outjarFile,
		Set expectedOutputJarContents) {

		try {
			assertTrue(
				"outjar older than injar",
				(outjarFile.lastModified() > dirFile.lastModified()));

			// Go through the output jar file, for each element, remove it from
			// the expectedOutputJarContents - when we finish, the expectedOutputJarContents
			// set should be empty!
			JarInputStream outjar =
				new JarInputStream(new java.io.FileInputStream(outjarFile));
			ZipEntry entry;
			while (null != (entry = outjar.getNextEntry())) {
				String fileName = entry.getName();
				fileName = fileName.replace('\\', '/');
				if (fileName.indexOf("CVS") == -1) {
					boolean b = expectedOutputJarContents.remove(fileName);
					assertTrue(
							"Unexpectedly found : " + fileName + " in outjar",
							b);
				}
				outjar.closeEntry();
			}
			outjar.close();

			assertTrue(
				"Didnt make it into the output jar: "
					+ expectedOutputJarContents.toString(),
				expectedOutputJarContents.isEmpty());
		} catch (IOException ex) {
			fail(ex.toString());
		}
	}

	/*
	 * Ensure -outjar contains all non-Java resouces from source and injars
	 */
	public void compareSourceToOutjar(String indirName, File outjarFile) {
		HashSet resources = new HashSet();
		listSourceResources(indirName, resources);

		try {

			JarInputStream outjar =
				new JarInputStream(new java.io.FileInputStream(outjarFile));
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

			assertTrue(
				"Missing resources: " + resources.toString(),
				resources.isEmpty());
		} catch (IOException ex) {
			fail(ex.toString());
		}
	}

	/*
	 * Ensure bin contains all non-Java resouces from source and injars
	 */
	public void compareIndirToBin(
		File indirFile,
		String sourceDir,
		String outdirName,
		Set expectedOutdirContents) {

//		byte[] inManifest = null;

		File binBase = openFile(outdirName);
		String[] toResources = FileUtil.listFiles(binBase);
		for (int i = 0; i < toResources.length; i++) {
			String fileName = toResources[i];
			if (fileName.indexOf("CVS") == -1) {
				boolean b = expectedOutdirContents.remove(fileName);
				assertTrue("Extraneous resources: " + fileName, b);
			}
		}

		assertTrue(
			"Missing resources: " + expectedOutdirContents.toString(),
			expectedOutdirContents.isEmpty());
	}

	/**
	 * @param resources
	 */
//	private void dumpResources(HashSet resources) {
//		System.err.println("Dump: " + resources.size() + " resources");
//		for (Iterator iter = resources.iterator(); iter.hasNext();) {
//			Object element = (Object) iter.next();
//			System.err.println("  Resource: " + element);
//		}
//	}

	private void listSourceResources(String indirName, Set resources) {
		File srcBase = openFile(indirName);
		File[] fromResources =
			FileUtil.listFiles(srcBase, aspectjResourceFileFilter);
		for (int i = 0; i < fromResources.length; i++) {
			String name = FileUtil.normalizedPath(fromResources[i], srcBase);
			//System.err.println("Checking "+name);
			if (!name.startsWith("CVS/")
				&& (-1 == name.indexOf("/CVS/"))
				&& !name.endsWith("/CVS")) {
				resources.add(name);
			}
		}
	}

//	private byte[] listDirResources(File directory, Set resources) {
//		return listDirResources(
//			directory.getAbsolutePath(),
//			directory,
//			resources);
//	}

//	private byte[] listDirResources(
//		String prefix,
//		File directory,
//		Set resources) {
//		byte[] manifest = null;
//
//		File[] resourceFiles = directory.listFiles(new FileFilter() {
//			public boolean accept(File arg0) {
//				boolean accept =
//					!arg0.getName().endsWith(".class") && !arg0.isDirectory();
//				return accept;
//			}
//		});
//		for (int i = 0; i < resourceFiles.length; i++) {
//			File f = resourceFiles[i];
//			String name = f.getAbsolutePath();
//			if (f.getAbsolutePath().startsWith(prefix))
//				name = name.substring(prefix.length());
//			name = name.replace('\\', '/');
//
//			resources.add(resourceFiles[i]);
//		}
//		File[] subdirs = directory.listFiles(new FileFilter() {
//			public boolean accept(File arg0) {
//				return arg0.isDirectory();
//			}
//		});
//		for (int i = 0; i < subdirs.length; i++) {
//			listDirResources(prefix, subdirs[i], resources);
//		}
//
//		return manifest;
//	}

	public static final FileFilter aspectjResourceFileFilter =
		new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			return (
				!name.endsWith(".class")
					&& !name.endsWith(".java")
					&& !name.endsWith(".aj"));

		}
	};

	/*
	 * Ensure bin contains all non-Java resouces from source and injars
	 */
	public void compareDirs(String indirName, String outdirName) {
		File binBase = openFile(outdirName);
		File[] toResources =
			FileUtil.listFiles(binBase, aspectjResourceFileFilter);

		HashSet resources = new HashSet();
		listSourceResources(indirName, resources);

		for (int i = 0; i < toResources.length; i++) {
			String fileName = FileUtil.normalizedPath(toResources[i], binBase);
			boolean b = resources.remove(fileName);
			assertTrue("Extraneous resources: " + fileName, b);
		}

		assertTrue(
			"Missing resources: " + resources.toString(),
			resources.isEmpty());
	}

}
