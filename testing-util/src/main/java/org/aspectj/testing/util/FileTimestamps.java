/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/
package org.aspectj.testing.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Lets incremental-build tests separate "files written before this build" from "files written after the previous build" without
 * sleeping.
 * <p>
 * The incremental compiler (AjState) treats a file as modified when {@code lastModified() + 1000 > lastBuildStart}. Anything
 * written within a second of the previous build therefore looks modified again, so tests used to sleep for a second before every
 * build. Waiting is only a means of making earlier writes at least {@link #WINDOW} old, so on a file system with millisecond
 * timestamps this class gets the same effect by moving the modification time of recently written files back, which costs
 * microseconds plus a few milliseconds of separation from the previous build. On a file system with coarse timestamps it falls
 * back to sleeping.
 */
public final class FileTimestamps {

	/** The padding AjState applies when comparing file times with build times (ms). */
	private static final long WINDOW = 1000;
	/** Margin so the files are strictly outside the window even for the last, coarse, bit of clock. */
	private static final long MARGIN = 5;
	/**
	 * A build that follows a very quick build (a no-op incremental build takes a few milliseconds) must still start measurably
	 * later than that previous build, otherwise a file changed in between, whose time is moved back to just outside the window,
	 * would fall on the wrong side of it and not be noticed.
	 */
	private static final long SEPARATION = 10;

	private static final Map<Object, Boolean> FINE_GRAINED = new HashMap<>();

	private FileTimestamps() {
	}

	/**
	 * Make sure everything under the directory that was written before now is old enough not to be mistaken for a modification
	 * by the next incremental build, while anything changed after the previous build is still seen as changed.
	 */
	public static void settle(File dir) {
		if (dir == null || !dir.isDirectory() || !hasFineGrainedTimestamps(dir)) {
			sleep(WINDOW);
			return;
		}
		sleep(SEPARATION);
		final long cutoff = System.currentTimeMillis() - WINDOW - MARGIN;
		try {
			Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (attrs.lastModifiedTime().toMillis() > cutoff) {
						file.toFile().setLastModified(cutoff);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			sleep(WINDOW); // could not adjust the files, so wait the old way
		}
	}

	/** @return true if the file system holding the directory keeps modification times to the millisecond (or better) */
	static boolean hasFineGrainedTimestamps(File dir) {
		Object key;
		try {
			FileStore store = Files.getFileStore(dir.toPath());
			key = store.name() + "|" + store.type();
		} catch (IOException e) {
			return false;
		}
		synchronized (FINE_GRAINED) {
			Boolean known = FINE_GRAINED.get(key);
			if (known == null) {
				known = probe(dir);
				FINE_GRAINED.put(key, known);
			}
			return known;
		}
	}

	private static boolean probe(File dir) {
		File probe = null;
		try {
			probe = File.createTempFile("tsprobe", ".tmp", dir);
			long[] samples = { 1_000_000_000_123L, 1_000_000_000_456L, 1_000_000_000_789L };
			for (long sample : samples) {
				if (!probe.setLastModified(sample) || probe.lastModified() != sample) {
					return false;
				}
			}
			return true;
		} catch (IOException | RuntimeException e) {
			return false;
		} finally {
			if (probe != null) {
				probe.delete();
			}
		}
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
