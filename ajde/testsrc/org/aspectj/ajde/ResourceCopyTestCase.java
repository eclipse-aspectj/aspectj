/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.aspectj.util.FileUtil;

/**
 * @author websterm
 */
public class ResourceCopyTestCase extends AjdeTestCase {

	public static final String PROJECT_DIR = "bug-36071"; 
	public static final String srcDir = PROJECT_DIR + "/src"; 
	public static final String binDir = "bin"; 

	public static final String injar1Name = "input1.jar"; 
	public static final String injar2Name = "input2.jar"; 
	public static final String outjarName = "/bin/output.jar"; 

	/**
	 * Constructor for JarResourceCopyTestCase.
	 * @param arg0
	 */
	public ResourceCopyTestCase(String arg0) {
		super(arg0);
	}

	/*
	 * Ensure the output directpry in clean
	 */	
	protected void setUp() throws Exception {
		super.setUp(PROJECT_DIR);
		FileUtil.deleteContents(openFile(binDir));
	}

	public void testSrcToBin () {
		assertTrue(!Ajde.getDefault().getTaskListManager().hasWarning());
		assertTrue("Build failed",doSynchronousBuild("config1.lst"));
		compareDirs("src","bin");
	}
	
	public void testInjarsToOutjar () {
		Set injars = new HashSet();
		File injar1 = openFile(injar1Name);
		injars.add(injar1);
		ideManager.getProjectProperties().setInJars(injars);
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed",doSynchronousBuild("config2.lst"));
		assertFalse("No build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
		List msgs = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		assertTrue("Wrong message",((NullIdeTaskListManager.SourceLineTask)msgs.get(0)).message.getMessage().startsWith("manifest not copied: "));
		compareJars(injar1,"src",outjar);
	}
	
	public void testDuplicateResources () {
		Set injars = new HashSet();
		File injar1 = openFile(injar1Name);
		File injar2 = openFile(injar2Name);
		injars.add(injar1);
		injars.add(injar2);
		ideManager.getProjectProperties().setInJars(injars);
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertFalse("Build should have failed",doSynchronousBuild("config2.lst"));
		assertFalse("No build errors",ideManager.getCompilationSourceLineTasks().isEmpty());
		List msgs = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		assertTrue("Wrong message",((NullIdeTaskListManager.SourceLineTask)msgs.get(1)).message.getMessage().startsWith("duplicate resource: "));
	}
	
	public void testSrcToOutjar () {
		File outjar = openFile(outjarName);
		ideManager.getProjectProperties().setOutJar(outjar.getAbsolutePath());
		assertTrue("Build failed",doSynchronousBuild("config1.lst"));
		compareSourceToOutjar("src",outjar);
	}
	
	public void testInjarsToBin () {
		Set injars = new HashSet();
		File injar1 = openFile(injar1Name);
		injars.add(injar1);
		ideManager.getProjectProperties().setInJars(injars);
		assertTrue("Build failed",doSynchronousBuild("config2.lst"));
		assertFalse("No build warnings",ideManager.getCompilationSourceLineTasks().isEmpty());
		List msgs = NullIdeManager.getIdeManager().getCompilationSourceLineTasks();
		assertTrue("Wrong message",((NullIdeTaskListManager.SourceLineTask)msgs.get(0)).message.getMessage().startsWith("manifest not copied: "));
		compareInjarsToBin(injar1,"src","bin");
	}
	
	/*
	 * Ensure bin contains all non-Java resouces from source and injars
	 */
	public void compareDirs (String indirName, String outdirName) {
		File binBase = openFile(outdirName);
		File[] toResources = FileUtil.listFiles(binBase,aspectjResourceFileFilter);

		HashSet resources = new HashSet();
		listSourceResources(indirName,resources);		
		
		for (int i = 0; i < toResources.length; i++) {
			String fileName = FileUtil.normalizedPath(toResources[i],binBase);
			boolean b = resources.remove(fileName);
			assertTrue("Extraneous resoures:" + fileName,b);
		}
		
		assertTrue("Missing resources:" + resources.toString(), resources.isEmpty());
	}	
	
	/*
	 * Ensure -outjar contains all non-Java resouces from injars
	 */
	public void compareJars (File injarFile, String indirName, File outjarFile) {
	
		HashSet resources = new HashSet();
	
		try {	
			assertTrue("outjar older than injar",(outjarFile.lastModified() > injarFile.lastModified()));
			byte[] inManifest = listJarResources(injarFile,resources);
			listSourceResources(indirName,resources);		

			ZipInputStream outjar = new ZipInputStream(new java.io.FileInputStream(outjarFile));
			ZipEntry entry;
			while (null != (entry = outjar.getNextEntry())) {
				String fileName = entry.getName();
				if (!fileName.endsWith(".class")) {
					
					/* Ensure we didn't copy any JAR manifests */
					if (fileName.toLowerCase().startsWith("meta-inf")) {
						byte[] outManifest = FileUtil.readAsByteArray(outjar);
						assertFalse("Manifest has been copied",Arrays.equals(inManifest,outManifest));
					}
					
					boolean b = resources.remove(fileName);
					assertTrue(fileName,b);
				}
				outjar.closeEntry();
			}
			outjar.close();

			assertTrue(resources.toString(),resources.isEmpty());
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}
	
	/*
	 * Ensure -outjar conatins all non-Java resouces from source and injars
	 */
	public void compareSourceToOutjar (String indirName, File outjarFile) {
		HashSet resources = new HashSet();		
		listSourceResources(indirName,resources);		
	
		try {	

			ZipInputStream outjar = new ZipInputStream(new java.io.FileInputStream(outjarFile));
			ZipEntry entry;
			while (null != (entry = outjar.getNextEntry())) {
				String fileName = entry.getName();
				if (!fileName.endsWith(".class")) {
					boolean b = resources.remove(fileName);
					assertTrue(fileName,b);
				}
				outjar.closeEntry();
			}
			outjar.close();

			assertTrue("Missing resources:" + resources.toString(), resources.isEmpty());
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}
	
	/*
	 * Ensure bin contains all non-Java resouces from source and injars
	 */
	public void compareInjarsToBin(File injarFile, String indirName, String outdirName) {
	
		HashSet resources = new HashSet();
	
		try {	
			byte[] inManifest = listJarResources(injarFile,resources);
			listSourceResources(indirName,resources);		
			
			File binBase = openFile(outdirName);
			File[] toResources = FileUtil.listFiles(binBase,aspectjResourceFileFilter);
			for (int i = 0; i < toResources.length; i++) {
				String fileName = FileUtil.normalizedPath(toResources[i],binBase);

				/* Ensure we didn't copy any JAR manifests */
				if (fileName.toLowerCase().startsWith("meta-inf")) {
					byte[] outManifest = FileUtil.readAsByteArray(toResources[i]);
					assertFalse("Manifest has been copied",Arrays.equals(inManifest,outManifest));
				}
				boolean b = resources.remove(fileName);
				assertTrue("Extraneous resoures:" + fileName,b);
			}

			assertTrue("Missing resources:" + resources.toString(), resources.isEmpty());
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}
    
    private void listSourceResources (String indirName, Set resources) {
		File srcBase = openFile(indirName);
		File[] fromResources = FileUtil.listFiles(srcBase,aspectjResourceFileFilter);
		for (int i = 0; i < fromResources.length; i++) {
			String name = FileUtil.normalizedPath(fromResources[i],srcBase);
			if (!name.startsWith("CVS/") && (-1 == name.indexOf("/CVS/")) && !name.endsWith("/CVS")) {
				resources.add(name);
			}
		}
    }
    
    private byte[] listJarResources (File injarFile, Set resources) {
		byte[] manifest = null;
	
		try {
			ZipInputStream injar = new ZipInputStream(new java.io.FileInputStream(injarFile));
			ZipEntry entry;
			while (null != (entry = injar.getNextEntry())) {
				String fileName = entry.getName();
				if (!fileName.endsWith(".class")) {
					
					/* JAR manifests shouldn't be copied */
					if (fileName.toLowerCase().startsWith("meta-inf")) {
						manifest = FileUtil.readAsByteArray(injar);
					}
					else {
						resources.add(fileName);
					}
				}
				injar.closeEntry();
			}
			injar.close();
		}	
		catch (IOException ex) {
			fail(ex.toString());
		}
		
		return manifest;
    }
    
	public static final FileFilter aspectjResourceFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			return (!name.endsWith(".class") && !name.endsWith(".java") && !name.endsWith(".aj"));

		}
	};

}
