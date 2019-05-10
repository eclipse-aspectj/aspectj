/* *******************************************************************
 * Copyright (c) 2017 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.ClassPathManager.ClassFile;
import org.aspectj.weaver.bcel.ClassPathManager.Entry;
import org.aspectj.weaver.bcel.ClassPathManager.JImageEntry;

import junit.framework.TestCase;

/**
 * Exercise the JImage handling in @link {@link org.aspectj.weaver.bcel.ClassPathManager}.
 * 
 * @author Andy Clement
 */
public class JImageTestCase extends TestCase {

	ClassPathManager cpm;

	public void setUp() throws Exception {
		List<String> paths = new ArrayList<>();
		paths.add(LangUtil.getJrtFsFilePath());
		cpm = new ClassPathManager(paths,new TestMessageHandler());
	}
	
	public void testOnJava9() {
		if (!LangUtil.is19VMOrGreater()) {
			System.out.println("SKIPPING JIMAGE TESTS AS NOT ON 1.9 OR LATER");
		}
	}
	
	public void testBasicStructureAndCapabilities() {	
		if (!LangUtil.is19VMOrGreater()) return;
		// Should be one entry for finding JRT contents
		List<Entry> entries = cpm.getEntries();
		assertEquals(1,entries.size());
		assertEquals(JImageEntry.class,entries.get(0).getClass());

		ClassFile stringClassFile = cpm.find(UnresolvedType.JL_STRING);
		assertNotNull(stringClassFile);
		assertEquals("java/lang/String.class",stringClassFile.getPath());
	}
	
	public void testBehaviour() throws Exception {
		if (!LangUtil.is19VMOrGreater()) return;
		JImageEntry jie = getJImageEntry();
		
		Map<String, Path> packageCache = jie.getPackageCache();
		assertTrue(packageCache.size()>0);
		// Note: seems to be about 1625 entries in it for Java9
		Path path = packageCache.get("java/lang");
		assertEquals("modules/java.base/java/lang", path.toString());
		path = packageCache.get("java/io");
		assertEquals("modules/java.base/java/io", path.toString());
		
		assertNotNull(jie.find("java/lang/String"));
		assertNotNull(jie.find("java/io/File"));
		// TODO test the filecache, hard because difficult to simulate collection of SoftReferences
	}
	

	static class TestMessageHandler implements IMessageHandler {

		@Override
		public boolean handleMessage(IMessage message) throws AbortException {
			return false;
		}

		@Override
		public boolean isIgnoring(Kind kind) {
			return false;
		}

		@Override
		public void dontIgnore(Kind kind) {
		}

		@Override
		public void ignore(Kind kind) {
		}
		
	}

	// ---
	
	private JImageEntry getJImageEntry() {
		return (JImageEntry) cpm.getEntries().get(0);
	}

	public List<String> getAllTheClasses() {
		final List<String> result = new ArrayList<>();
		URI JRT_URI = URI.create("jrt:/"); //$NON-NLS-1$
		FileSystem fs = FileSystems.getFileSystem(JRT_URI);
		Iterable<java.nio.file.Path> roots = fs.getRootDirectories();
		try {
			for (java.nio.file.Path path : roots) {
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.getNameCount()>3 && file.toString().endsWith(".class")) {
							String withClassSuffix = file.subpath(2, file.getNameCount()).toString();
							result.add(withClassSuffix.substring(0,withClassSuffix.length()-".class".length()));
						}
						return FileVisitResult.CONTINUE;
					}
				});
				}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

}
