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

package org.aspectj.ajdt.internal.core.builder;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

import org.aspectj.bridge.MessageHandler;
import org.aspectj.testing.util.TestUtil;
import org.aspectj.util.*;
import org.aspectj.workbench.resources.FilesystemFolder;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class ClassFileCacheTest extends TestCase {

	private MessageHandler handler = new MessageHandler();

	public ClassFileCacheTest(String name) {
		super(name);
	}

	public void testExists() throws CoreException {
		ClassFileCache cache = new ClassFileCache(new FilesystemFolder(new Path("out")), handler);
		IPath folderPath = new Path("tempfolder");
		IFolder folder = cache.getFolder(folderPath);
		
		assertTrue("default folder always exists", cache.exists(new Path("")));
		assertTrue("default folder always exists", cache.getFolder(new Path("")).exists());
		
		
		assertTrue("" + folder, !cache.exists(folderPath));
		
		folder.create(true, true, null);
		assertTrue("created: " + folderPath, cache.exists(folderPath));
		assertTrue("created: " + folderPath, cache.getFolder(folderPath).exists());
		
		folder.delete(true, null);
		assertTrue("deleted: " + folderPath, !cache.exists(folderPath));
		
		IPath filePath = new Path("tempfolder/TempClass.class");
		IFile file = cache.getFile(filePath);	
		assertTrue("" + file, !cache.exists(filePath));	
		assertTrue("" + file, !cache.exists(folderPath));	
		
		createFile(cache, "tempfolder/TempClass.class");
		assertTrue("" + file, cache.exists(filePath));	
		//XXX should be created when children are
		//XXXassertTrue("" + file, cache.exists(folderPath));	
	}

	public void testFilesAreCached() throws CoreException {
		ClassFileCache cache = new ClassFileCache(new FilesystemFolder(new Path("out")), handler);
		IFolder folder = cache.getFolder(new Path("testpath"));
		IFile file1 = folder.getFile("Foo.class");
		assertTrue("" + file1.getClass(), file1 instanceof DeferredWriteFile);
		
		IFile file2 = cache.getFile(new Path("testpath/Foo.class"));
		assertTrue("" + file2.getClass(), file2 instanceof DeferredWriteFile);
	
		assertTrue("" + file1 + ", " + file2, file1 == file2);
		
		
		folder = cache.getFolder(new Path("testpath"));
		folder = folder.getFolder("p1");
		assertTrue("" + folder, !folder.exists());
	
		
		file1 = folder.getFile(new Path("Bar.class"));
		file2 = cache.getFile(new Path("testpath/p1/Bar.class"));
		
		assertTrue("" + file1.getClass(), file1 instanceof DeferredWriteFile);
		assertTrue("" + file2.getClass(), file2 instanceof DeferredWriteFile);
	
		assertTrue("" + file1 + ", " + file2, file1 == file2);
		
		
		assertTrue(!cache.exists(new Path("testpath/p1/Bar.class")));
		
		InputStream source = new ByteArrayInputStream(new byte[] {0,1,2,3,4,5,6,7,8,9});
		file1.create(source, true, null);
		assertTrue(cache.exists(new Path("testpath/p1/Bar.class")));
		
		file1.delete(true, true, null);
		assertTrue(!cache.exists(new Path("testpath/p1/Bar.class")));
		
		IResource[] members = cache.members();
		assertEquals(members.length, 2);
		
		DeferredWriteFile dwf1 = (DeferredWriteFile) members[0];
		DeferredWriteFile dwf2 = (DeferredWriteFile) members[1];
		
		if (dwf1.getName().endsWith("Bar.class")) {
			DeferredWriteFile tmp = dwf1;
			dwf1 = dwf2; dwf2 = tmp;
		}
		
		assertTrue(!dwf1.exists());
		assertTrue(!dwf2.exists());
		
		assertEquals(dwf1.getName(), "Foo.class");
		assertEquals(dwf2.getName(), "Bar.class");
	}
	
	public void testChange() throws CoreException {
		MessageHandler handler = new MessageHandler();
		ClassFileCache cache = new ClassFileCache(new FilesystemFolder(new Path("out")), handler);
		cache.resetIncrementalInfo();
		String path1 = "testpath/Foo.class";
		String path2 = "testpath/Bar.class";
//		cache.getFolder(new Path("testpath")).delete(true, false, null);
		assertTrue(!cache.getFolder(new Path("testpath")).exists());
		
		createFile(cache, path1);
		createFile(cache, path2);
		
		//XXX assertTrue(cache.getFolder(new Path("testpath")).exists());
	
		checkFileMatch(cache.getAddedOrChanged(),
			new String[] { "out/" + path1, "out/" + path2 });
		checkFileMatch(cache.getDeleted(), new String[0] );
		
		// added
		cache.resetIncrementalInfo();
		String path3 = "testpath/Baz.class";
		createFile(cache, path3);
		checkFileMatch(cache.getAddedOrChanged(), new String[] { "out/" + path3 });
		checkFileMatch(cache.getDeleted(), new String[0] );	
		
		// remove
		cache.resetIncrementalInfo();
		deleteFile(cache, path3);
		checkFileMatch(cache.getDeleted(), new String[] { "out/" + path3 });	
		checkFileMatch(cache.getAddedOrChanged(), new String[0] );	
		
		// change
		cache.resetIncrementalInfo();
		createFile(cache, path1);

		deleteFile(cache, path2);
		createFile(cache, path2);	
		
		checkFileMatch(cache.getAddedOrChanged(),
			new String[] { "out/" + path1, "out/" + path2 });
		checkFileMatch(cache.getDeleted(), new String[0] );	
	}
	
	public void testWrite() throws CoreException {
		MessageHandler handler = new MessageHandler();
		clearDirectory("out");
		checkEmpty("out");
		
		ClassFileCache cache = new ClassFileCache(new FilesystemFolder(new Path("out")), handler);
		cache.resetIncrementalInfo();
		String path1 = "testpath/Foo.class";
		String path2 = "testpath/Bar.class";
		createFile(cache, path1);
		createFile(cache, path2);
		
		checkEmpty("out");
		
		writeCache(cache);
		
		checkContents("out", new String[] {"out/" + path1, "out/" + path2});
		
		deleteFile(cache, path2);
		checkContents("out", new String[] {"out/" + path1, "out/" + path2});
		writeCache(cache);
		
		checkContents("out", new String[] {"out/" + path1});
	}

	private void writeCache(ClassFileCache cache) throws CoreException {
		IResource[] members = cache.members();
		for (int i = 0; i < members.length; i++) {
			IResource iResource = members[i];
			DeferredWriteFile file = (DeferredWriteFile) iResource;
			//System.out.println("about to write: " + file);
			if (file.exists()) {
				file.writeWovenBytes(new byte[] { 0, 1, 2,3});
			} else {
				file.deleteRealFile();
			}
		}
	}

	private void checkContents(String path, String[] files) {
		File dir = new File(path);
		assertTrue(dir.exists());
		List allFiles = new ArrayList();
		listRecursively(new File(path), allFiles, "");
		TestUtil.assertSetEquals(Arrays.asList(files), allFiles);
	}

	private void listRecursively(File file, List accumulator, String prefix) {
		if (file.isDirectory()) {
			if (prefix.length() == 0) prefix = file.getName() + "/";
			else prefix = prefix + file.getName() + "/";
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				listRecursively(files[i], accumulator, prefix);
			}
		} else {
			accumulator.add(prefix + file.getName());	
		}
	}



	private void checkEmpty(String path) {
		checkContents(path, new String[0]);
	}


	private void clearDirectory(String path) {
		FileUtil.deleteContents(new File(path));
	}


	private void checkFileMatch(List list, String[] names) {
		Set found = new HashSet();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			EclipseUnwovenClassFile file = (EclipseUnwovenClassFile) iter.next();
			found.add(file.getFile().getFullPath().toString());
		}
		
		TestUtil.assertSetEquals(Arrays.asList(names), found);
	}
	
	private void createFile(ClassFileCache cache, String path) throws CoreException {
		IFile file = cache.getFile(new Path(path));
		InputStream source = new ByteArrayInputStream(new byte[] {0,1,2,3,4,5,6,7,8,9});
		file.create(source, true, null);
	}
	
	private void deleteFile(ClassFileCache cache, String path) throws CoreException {
		IFile file = cache.getFile(new Path(path));
		file.delete(true, null);
	}

}
