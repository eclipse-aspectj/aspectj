/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Abraham Nevado (lucierna) initial implementation
 ********************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.aspectj.weaver.Dump;

public class SimpleCacheFactory {

	public static final String CACHE_ENABLED_PROPERTY = "aj.weaving.cache.enabled";
	public static final String CACHE_DIR = "aj.weaving.cache.dir";
	public static final String CACHE_IMPL = "aj.weaving.cache.impl";

	public static final String PATH_DEFAULT = defaultCachePath();
	public static final boolean BYDEFAULT= false;

	private static final Set<PosixFilePermission> NON_OWNER_PERMISSIONS = EnumSet.of(
		PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_EXECUTE,
		PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OTHERS_EXECUTE
	);

	public static String path = PATH_DEFAULT;
	public static Boolean enabled = false;
	private static boolean determinedIfEnabled = false;
	private static SimpleCache lacache=null;

	public static synchronized SimpleCache createSimpleCache(){
		if (lacache==null){
		 	if (!determinedIfEnabled) {
		 		determineIfEnabled();
		 	}

			if (!enabled) {
				return null;
			}

			try {
				path = System.getProperty(CACHE_DIR);
				if (path == null){
					path = PATH_DEFAULT;
				}

			} catch (Throwable t) {
				path=PATH_DEFAULT;
				t.printStackTrace();
				Dump.dumpWithException(t);
			}
			if (!preparePrivateDirectory(path)) {
				System.err.println(
					"Disabling the weaving cache: " + path + " is not a directory that only the current user can write to. " +
					"Point " + CACHE_DIR + " at a private directory."
				);
				enabled = false;
				return null;
			}
			lacache= new SimpleCache(path, enabled);
		}
		return lacache;

	}

	private static void determineIfEnabled() {
		try {
			String property = System.getProperty(CACHE_ENABLED_PROPERTY);
			if (property == null ){
				enabled = BYDEFAULT;
			}
			else if (property.equalsIgnoreCase("true")){

					String impl = System.getProperty(CACHE_IMPL);
					if (SimpleCache.IMPL_NAME.equals(impl)){
						enabled = true;
					}
					else{
						enabled = BYDEFAULT;
					}
			}
			else{
				enabled = BYDEFAULT;
			}

		} catch (Throwable t) {
			enabled=BYDEFAULT;
			System.err.println("Error creating cache");
			t.printStackTrace();
			Dump.dumpWithException(t);
		}
		determinedIfEnabled = true;
	}

	// Should behave ok with two threads going through here, well whoever gets there first will set determinedIfEnabled but only after
	// it has set 'enabled' to the right value.
	public static boolean isEnabled() {
		if (!determinedIfEnabled) {
			determineIfEnabled();
		}
		return enabled;
	}

	/**
	 * Per user directory below the JVM temporary directory. The system temporary directory itself is shared between all
	 * local users on most platforms, and the cache index below it is read back with {@link java.io.ObjectInputStream}
	 * while the cached bytes are handed to {@code ClassLoader.defineClass}.
	 */
	private static String defaultCachePath() {
		StringBuilder name = new StringBuilder("aspectj-cache-");
		String user = System.getProperty("user.name", "");
		for (int i = 0; i < user.length(); i++) {
			char c = user.charAt(i);
			name.append(Character.isLetterOrDigit(c) ? c : '_');
		}
		return new File(System.getProperty("java.io.tmpdir", "."), name.toString()).getPath();
	}

	/**
	 * Create the cache directory if it is missing and check that nobody but its owner can write to it. A directory a
	 * second local user can write to lets that user replace the cached bytes of any class, which the weaver then
	 * defines in this JVM.
	 *
	 * @param path cache directory to create and check
	 * @return true if the directory exists and is private to its owner
	 */
	static boolean preparePrivateDirectory(String path) {
		try {
			Path dir = Paths.get(path);
			if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
				createPrivateDirectory(dir);
			}
			if (!Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS)) {
				return false;
			}
			Set<PosixFilePermission> permissions = readPosixPermissions(dir);
			return permissions == null || Collections.disjoint(permissions, NON_OWNER_PERMISSIONS);
		} catch (IOException | RuntimeException e) {
			return false;
		}
	}

	private static void createPrivateDirectory(Path dir) throws IOException {
		Path parent = dir.getParent();
		if (parent != null && !Files.isDirectory(parent)) {
			Files.createDirectories(parent);
		}
		try {
			Files.createDirectory(dir, PosixFilePermissions.asFileAttribute(
				EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)
			));
		} catch (UnsupportedOperationException e) {
			// no POSIX file attributes, e.g. on Windows
			Files.createDirectory(dir);
		} catch (FileAlreadyExistsException e) {
			// created concurrently, the permission check below still applies
		}
	}

	/**
	 * @return the POSIX permissions of the given directory, or null on a file system that does not report them
	 */
	private static Set<PosixFilePermission> readPosixPermissions(Path dir) throws IOException {
		try {
			return Files.getPosixFilePermissions(dir, LinkOption.NOFOLLOW_LINKS);
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}


}
