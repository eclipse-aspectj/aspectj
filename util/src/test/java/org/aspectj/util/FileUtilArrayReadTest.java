/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Checks that the array readers reject an invalid length prefix before allocating anything.
 */
public class FileUtilArrayReadTest extends TestCase {

	private interface ArrayReader {
		void read(DataInputStream s) throws IOException;
	}

	/** A length prefix followed by the given number of zero bytes, enough for that many elements of any kind. */
	private static DataInputStream streamStartingWithLength(int length, int bytesOfData) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		new DataOutputStream(bos).writeInt(length);
		bos.write(new byte[bytesOfData]);
		return new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
	}

	private static void assertRejected(ArrayReader reader, int length, int bytesOfData) throws IOException {
		try {
			reader.read(streamStartingWithLength(length, bytesOfData));
			fail("expected length " + length + " to be rejected");
		} catch (IOException expected) {
		}
	}

	private static void assertAllRejected(int length, int bytesOfData) throws IOException {
		assertRejected(FileUtil::readBooleanArray, length, bytesOfData);
		assertRejected(FileUtil::readIntArray, length, bytesOfData);
		assertRejected(FileUtil::readStringArray, length, bytesOfData);
	}

	public void testHugeLengthIsRejected() throws IOException {
		assertAllRejected(Integer.MAX_VALUE, 0);
	}

	public void testNegativeLengthIsRejected() throws IOException {
		assertAllRejected(-1, 0);
	}

	public void testLengthJustOverTheLimitIsRejectedEvenWhenTheDataIsPresent() throws IOException {
		assertAllRejected(0x10000, 4 * 0x10000);
	}

	public void testValidArraysStillRead() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bos);
		FileUtil.writeBooleanArray(new boolean[] { true, false }, out);
		FileUtil.writeIntArray(new int[] { 1, 2, 3 }, out);
		FileUtil.writeStringArray(new String[] { "a", "b" }, out);
		FileUtil.writeBooleanArray(new boolean[0], out);
		FileUtil.writeIntArray(new int[0], out);
		FileUtil.writeStringArray(new String[0], out);

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
		assertEquals(2, FileUtil.readBooleanArray(in).length);
		assertEquals(3, FileUtil.readIntArray(in).length);
		assertEquals(2, FileUtil.readStringArray(in).length);
		assertEquals(0, FileUtil.readBooleanArray(in).length);
		assertEquals(0, FileUtil.readIntArray(in).length);
		assertEquals(0, FileUtil.readStringArray(in).length);
	}
}
