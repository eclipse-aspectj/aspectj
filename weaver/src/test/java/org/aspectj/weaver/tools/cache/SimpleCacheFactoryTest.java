/*******************************************************************************
 * Copyright (c) 2024 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 ********************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import junit.framework.TestCase;

public class SimpleCacheFactoryTest extends TestCase {

	private static boolean isPosix() {
		return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
	}

	public void testCreatesDirectoryPrivateToTheOwner() throws Exception {
		if (!isPosix()) {
			return;
		}
		Path parent = Files.createTempDirectory("ajCacheTest");
		Path cacheDir = parent.resolve("cache");

		assertTrue("a missing cache directory should be created", SimpleCacheFactory.preparePrivateDirectory(cacheDir.toString()));

		Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(cacheDir);
		assertEquals("the cache directory should not be readable or writable by anybody else", "rwx------",
			PosixFilePermissions.toString(permissions));
	}

	public void testRejectsWorldWritableDirectory() throws Exception {
		if (!isPosix()) {
			return;
		}
		Path cacheDir = Files.createTempDirectory("ajCacheTest");
		Files.setPosixFilePermissions(cacheDir, PosixFilePermissions.fromString("rwxrwxrwx"));

		assertFalse("a cache directory any local user can write to should be rejected",
			SimpleCacheFactory.preparePrivateDirectory(cacheDir.toString()));

		Files.setPosixFilePermissions(cacheDir, PosixFilePermissions.fromString("rwx------"));
		assertTrue("a cache directory private to its owner should be accepted",
			SimpleCacheFactory.preparePrivateDirectory(cacheDir.toString()));
	}

	public void testRejectsSymbolicLink() throws Exception {
		if (!isPosix()) {
			return;
		}
		Path parent = Files.createTempDirectory("ajCacheTest");
		Path target = Files.createDirectory(parent.resolve("target"));
		Path link = Files.createSymbolicLink(parent.resolve("link"), target);

		assertFalse("a symbolic link should not be used as the cache directory",
			SimpleCacheFactory.preparePrivateDirectory(link.toString()));
	}

	public void testDefaultPathIsNotTheSharedTemporaryDirectory() {
		String tmpDir = System.getProperty("java.io.tmpdir");
		assertFalse("the default cache directory should not be the shared temporary directory itself",
			tmpDir.equals(SimpleCacheFactory.PATH_DEFAULT) || (tmpDir + "/").equals(SimpleCacheFactory.PATH_DEFAULT));
		assertTrue("the default cache directory should live below the temporary directory",
			SimpleCacheFactory.PATH_DEFAULT.startsWith(tmpDir));
	}
}
