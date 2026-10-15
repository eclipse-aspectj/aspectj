/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.io.File;
import java.io.IOException;

import org.aspectj.bridge.IMessageHandler;

import junit.framework.TestCase;

public class AjBuildManagerResourceTest extends TestCase {

	private AjBuildManager buildManager;
	private File destinationDirectory;

	protected void setUp() throws Exception {
		super.setUp();
		buildManager = new AjBuildManager(IMessageHandler.SYSTEM_ERR);
		destinationDirectory = File.createTempFile("ajbuildmanagertest", "dir");
		destinationDirectory.delete();
		destinationDirectory.mkdirs();
	}

	protected void tearDown() throws Exception {
		deleteRecursively(destinationDirectory);
		super.tearDown();
	}

	private static void deleteRecursively(File f) {
		File[] children = f.listFiles();
		if (children != null) {
			for (File c : children) {
				deleteRecursively(c);
			}
		}
		f.delete();
	}

	public void testSimpleFilenameStaysInDestDir() throws IOException {
		File result = buildManager.resolveOutputLocation(destinationDirectory, "Foo.txt");
		assertEquals(new File(destinationDirectory, "Foo.txt").getCanonicalPath(), result.getCanonicalPath());
	}

	public void testNestedFilenameStaysInDestDir() throws IOException {
		File result = buildManager.resolveOutputLocation(destinationDirectory, "META-INF/foo.txt");
		assertEquals(new File(destinationDirectory, "META-INF/foo.txt").getCanonicalPath(), result.getCanonicalPath());
	}

	public void testTraversalFilenameIsRejected() {
		try {
			buildManager.resolveOutputLocation(destinationDirectory, "../../../../tmp/rogue.txt");
			fail("expected a traversal attempt to be rejected");
		} catch (IOException expected) {
		}
	}

	public void testNullDestDirResolvesRelativeToWorkingDirectory() throws IOException {
		File result = buildManager.resolveOutputLocation(null, "Foo.txt");
		assertEquals(new File("Foo.txt").getCanonicalPath(), result.getCanonicalPath());
	}

	public void testNullDestDirStillRejectsTraversal() {
		try {
			buildManager.resolveOutputLocation(null, "../../rogue.txt");
			fail("expected a traversal attempt to be rejected even with no destination directory");
		} catch (IOException expected) {
		}
	}
}