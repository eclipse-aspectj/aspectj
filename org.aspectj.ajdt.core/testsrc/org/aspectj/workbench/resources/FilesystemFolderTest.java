/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.workbench.resources;

import java.io.*;
import junit.framework.TestCase;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;

public class FilesystemFolderTest extends TestCase {

	private static final String PATH = "testdata" + File.separator + "resources" + File.separator;
	private static final String DIR = PATH + "dir";
	private IProgressMonitor monitor = new NullProgressMonitor();
	private FilesystemFolder dir;
	private File javaDir;
	
	public FilesystemFolderTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		dir = new FilesystemFolder(DIR);
		javaDir = new File(DIR);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		dir.delete(true, monitor);
	}

	public void testCreateExistsDelete() throws CoreException {
		dir.delete(true, monitor);
		assertTrue(!dir.exists());
		
		dir.create(true, true, monitor);
		assertTrue(dir.exists());
		assertTrue(javaDir.exists());
		
		dir.delete(true, monitor);
		assertTrue(!dir.exists());
		assertTrue(!javaDir.exists());
	}

	public void testGetFullPath() {
		assertEquals(dir.getFullPath().toString(), javaDir.getPath().replace('\\', '/'));
	}

	public void testGetLocation() {
		assertEquals(dir.getLocation().toString(), javaDir.getAbsolutePath().replace('\\', '/'));
	}

	public void testGetName() {
		assertEquals(dir.getName(), javaDir.getName());
	}

	public void testGetModificationStamp() throws CoreException {
		dir.create(true, true, monitor);
		assertEquals(dir.getModificationStamp(), javaDir.lastModified());
	}

	public void testReadOnly() throws CoreException, IOException {
		dir.create(true, true, monitor);
		
		assertTrue(!dir.isReadOnly());
		assertTrue(javaDir.canWrite());
		dir.setReadOnly(true);
		assertTrue(dir.isReadOnly());
		assertTrue(!javaDir.canWrite());
	}

	public void testGetParent() { 
		assertEquals(dir.getParent().getFullPath().toString(), javaDir.getParentFile().getPath().replace('\\', '/'));
	}

	public void testExistsAbsoluteAndRelativeIPath() throws CoreException {
		final String CHILD_PATH = DIR + File.separator + "child";
		IPath childIPath = new Path(CHILD_PATH);
		FilesystemFolder child = new FilesystemFolder(CHILD_PATH);
		
		child.create(true, true, monitor);
		assertTrue("relative", dir.exists(childIPath));
		
		IPath absoluteChildIPath = new Path(new File(CHILD_PATH).getAbsolutePath());
		assertTrue("absolute", dir.exists(absoluteChildIPath));
	}

	public void testGetFileIPath() {
		final String DIRFILE = "dirfile.txt";
		IFile dirfile = dir.getFile(DIRFILE);
		assertEquals(
			dirfile.getLocation().toString(), 
			new File(DIR + File.separator + DIRFILE).getAbsolutePath().replace('\\', '/'));
	}

	public void testGetFolderIPath() {
		final String DIRFOLDER = "dirfolder";
		IFolder dirfile = dir.getFolder(DIRFOLDER);
		assertEquals(
			dirfile.getLocation().toString(), 
			new File(DIR + File.separator + DIRFOLDER).getAbsolutePath().replace('\\', '/'));		
	}

	//XXX not implemented
	public void testCopy() { }
	
	//XXX not implemented
	public void testTouch() { }

	//XXX not implemented
	public void testFindMemberIPath() { }

	//XXX not implemented
	public void testMembers() { }
}
