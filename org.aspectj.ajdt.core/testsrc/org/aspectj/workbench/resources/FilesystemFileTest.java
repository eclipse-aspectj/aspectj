/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.workbench.resources;

import java.io.*;
import java.io.FileInputStream;

import junit.framework.TestCase;
import org.aspectj.util.FileUtil;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;

//XXX this *has* to be tested on Linux
public class FilesystemFileTest extends TestCase {

	private static final String PATH = "testdata" + File.separator + "resources" + File.separator;
	
	private static final String TEST = PATH + "test.txt";
	private static final String SOURCE = PATH + "file.txt";
	private static final String EMPTY = PATH + "empty.txt";
	private IProgressMonitor monitor = new NullProgressMonitor();
	private FilesystemFile file;
	private File javaFile;

	protected void setUp() throws Exception {
		super.setUp();
		file = new FilesystemFile(TEST);
		javaFile = new File(TEST);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (file.exists()) file.delete(true, monitor);
	}

	public FilesystemFileTest(String name) {
		super(name);  
	}
  
	public void testCreateExistsContentsDelete() throws FileNotFoundException, CoreException, InterruptedException, IOException {
		if (file.exists()) file.delete(true, monitor);
		assertTrue(!file.exists());
		FileInputStream fis = new FileInputStream(SOURCE);
		
		file.create(fis, 0, monitor);
		fis.close();
		assertTrue(file.exists());
		
		String expected = FileUtil.readAsString(new File(SOURCE));
		String contents = FileUtil.readAsString(file.getContents());
		assertEquals(expected, contents);

		file.setContents(new FileInputStream(EMPTY), 0, monitor);
		assertEquals("", FileUtil.readAsString(file.getContents()));

		file.delete(true, monitor);
		assertTrue(!file.exists());
	}

	public void testGetFileExtension() {
		assertEquals(file.getFileExtension(), "txt");
	}

	public void testGetFullPath() {
		assertEquals(file.getFullPath().toString(), javaFile.getPath().replace('\\', '/'));
	}

	public void testGetLocation() {
		assertEquals(file.getLocation().toString(), javaFile.getAbsolutePath().replace('\\', '/'));
	}

	public void testGetName() {
		assertEquals(file.getName(), javaFile.getName());
	}

	public void testGetModificationStamp() throws IOException, CoreException {
		FileInputStream fis = new FileInputStream(SOURCE);
		file.create(fis, 0, monitor);
		assertEquals(file.getModificationStamp(), javaFile.lastModified());
	}

	public void testGetParent() { 
		assertEquals(file.getParent().getFullPath().toString(), javaFile.getParentFile().getPath().replace('\\', '/'));
	}

	public void testReadOnly() throws CoreException, IOException {
		FileInputStream fis = new FileInputStream(SOURCE);
		file.create(fis, 0, monitor);
		
		assertTrue(!file.isReadOnly());
		file.setReadOnly(true);
		assertTrue(file.isReadOnly());
	}

	//XXX not implemented
	public void testCopy() { }
	
	//XXX not implemented
	public void testTouch() { }

}
