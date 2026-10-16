/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.weaver.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import junit.framework.TestCase;

public class WeavingAdaptorDumpPathTest extends TestCase {
	private File tempDir, dumpDir;

	protected void setUp() throws Exception {
		super.setUp();
		tempDir = File.createTempFile("dumppathtest", "root");
		tempDir.delete();
		dumpDir = new File(tempDir, "_ajdump");
		dumpDir.mkdirs();
	}

	protected void tearDown() throws Exception {
		dumpDir.delete();
		tempDir.delete();
		super.tearDown();
	}

	public void testNormalNameStaysInDumpDir() throws IOException {
		File f = WeavingAdaptor.resolveDumpFile(dumpDir.getPath(), "ok.Foo");
		assertEquals(new File(dumpDir, "ok/Foo.class").getCanonicalPath(), f.getCanonicalPath());
	}

	public void testTraversalLookingNamesStayInsideBecauseDotsAreReplaced() throws IOException {
		for (String n : new String[] { "../../../tmp/bad", "a/../../b", "/etc/passwd", ".." }) {
			File f = WeavingAdaptor.resolveDumpFile(dumpDir.getPath(), n);
			assertTrue(n, f.getCanonicalFile().toPath().startsWith(dumpDir.getCanonicalFile().toPath()));
		}
	}

	public void testSymlinkOutOfDumpDirIsRejected() throws IOException {
		File outside = new File(tempDir, "outside");
		File link = new File(dumpDir, "link");
		outside.mkdirs();
		try {
			try {
				Files.createSymbolicLink(link.toPath(), outside.toPath());
			} catch (UnsupportedOperationException | IOException e) {
				return; // symlinks unavailable (e.g. Windows without privilege)
			}
			try {
				WeavingAdaptor.resolveDumpFile(dumpDir.getPath(), "link.Foo"); // -> link/Foo.class is outside
				fail("expected a path escaping through a symlink to be rejected");
			} catch (IOException expected) {
			}
		} finally {
			link.delete(); // File.delete() on symlink removes the link not its target
			outside.delete(); // only succeeds if empty
		}
	}
}