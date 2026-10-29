/* *******************************************************************
 * Copyright (c) 2026 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.File;

import junit.framework.TestCase;

/**
 * The source file recorded in the class file of an aspect is only the name of the file, without the directory it was built
 * in, see https://github.com/eclipse-aspectj/aspectj/issues/373.
 */
public class AjAttributeFileNameTest extends TestCase {

	public void testPathOfThisMachine() {
		assertEquals("Foo.aj", AjAttribute.fileNameOnly(new File("some" + File.separator + "dir" + File.separator + "Foo.aj").getPath()));
		assertEquals("Foo.aj", AjAttribute.fileNameOnly("/home/me/project/src/main/aspect/Foo.aj"));
	}

	public void testNameWithoutDirectory() {
		assertEquals("Foo.aj", AjAttribute.fileNameOnly("Foo.aj"));
	}

	public void testWindowsPathsEvenWhenNotOnWindows() {
		// A class file (or this attribute, read back from one) can come from a machine with other separators than ours
		assertEquals("Foo.aj", AjAttribute.fileNameOnly("C:\\ci\\work\\src\\Foo.aj"));
		assertEquals("Foo.aj", AjAttribute.fileNameOnly("C:/ci/work/src/Foo.aj"));
		assertEquals("Foo.aj", AjAttribute.fileNameOnly("C:/ci/work\\src/Foo.aj"));
		assertEquals("Foo.aj", AjAttribute.fileNameOnly("\\\\server\\share\\src\\Foo.aj"));
	}

	public void testBackslashIsPartOfTheNameWhereItIsNotASeparator() {
		String path = "/home/me/project/odd\\name.aj";
		if (File.separatorChar == '\\') {
			assertEquals("name.aj", AjAttribute.fileNameOnly(path)); // on Windows it separates
		} else {
			assertEquals("odd\\name.aj", AjAttribute.fileNameOnly(path));
		}
	}

	public void testNothingToCut() {
		assertNull(AjAttribute.fileNameOnly(null));
		assertEquals("/home/me/dir/", AjAttribute.fileNameOnly("/home/me/dir/")); // not a file: leave alone
	}
}
