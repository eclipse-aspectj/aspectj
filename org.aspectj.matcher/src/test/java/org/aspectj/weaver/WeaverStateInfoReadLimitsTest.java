/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

/**
 * Checks that sizes read from the reweavable data in a WeaverState attribute are validated before they are used to allocate space.
 * Valid attributes and diffs are produced by the real writer and diff generator, then a single size is overwritten.
 */
public class WeaverStateInfoReadLimitsTest extends TestCase {

	private interface Action {
		void run() throws Exception;
	}

	private static void assertRejected(Class<? extends Exception> expected, Action action) {
		try {
			action.run();
		} catch (Exception e) {
			assertTrue("expected " + expected.getSimpleName() + " but got " + e, expected.isInstance(e));
			return;
		}
		fail("expected " + expected.getSimpleName());
	}

	private static byte[] bytes(int length) {
		byte[] data = new byte[length];
		for (int i = 0; i < length; i++) {
			data[i] = (byte) i;
		}
		return data;
	}

	/** Writes a reweavable WeaverStateInfo that holds the given data as is (rather than as a diff). */
	private static byte[] write(byte[] unwovenClassFile) throws IOException {
		WeaverStateInfo wsi = new WeaverStateInfo(true);
		wsi.markOverweavingInUse();
		wsi.setUnwovenClassFileData(unwovenClassFile);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bos, null);
		wsi.write(out);
		out.flush();
		return bos.toByteArray();
	}

	private static WeaverStateInfo read(byte[] attribute) throws IOException {
		return WeaverStateInfo.read(new VersionedDataInputStream(new ByteArrayInputStream(attribute), null), null);
	}

	/** The data is the last thing written, preceded by its size as an int. */
	private static byte[] withDeclaredSize(byte[] attribute, int dataLength, int declaredSize) {
		byte[] copy = attribute.clone();
		int at = copy.length - dataLength - 4;
		for (int i = 0; i < 4; i++) {
			copy[at + i] = (byte) (declaredSize >>> (24 - 8 * i));
		}
		return copy;
	}

	/** The legacy compressed form: the kind byte (first byte) has the compression bit set and a zip replaces the data. */
	private static byte[] asCompressed(byte[] attribute, int dataLength, int declaredSize, int zippedLength) throws IOException {
		ByteArrayOutputStream zipped = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(zipped);
		zip.putNextEntry(new ZipEntry("x"));
		zip.write(new byte[zippedLength]);
		zip.closeEntry();
		zip.finish();

		int sizeAt = attribute.length - dataLength - 4;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(attribute, 0, sizeAt);
		new DataOutputStream(bos).writeInt(declaredSize);
		bos.write(zipped.toByteArray());
		byte[] result = bos.toByteArray();
		result[0] |= 1 << 5;
		return result;
	}

	public void testReweavableDataStillRoundTrips() throws IOException {
		for (int length : new int[] { 0, 10, 1000 }) {
			byte[] data = bytes(length);
			assertTrue(Arrays.equals(data, read(write(data)).getUnwovenClassFileData()));
		}
	}

	public void testInvalidUnwovenSizeIsRejected() throws IOException {
		final byte[] attribute = write(bytes(10));
		for (final int size : new int[] { Integer.MAX_VALUE, -1, 11 /* one more than is there */ }) {
			assertRejected(IOException.class, () -> read(withDeclaredSize(attribute, 10, size)));
		}
	}

	public void testInvalidCompressedSizeIsRejected() throws IOException {
		final byte[] attribute = write(bytes(10));
		for (final int size : new int[] { Integer.MAX_VALUE, -1 }) {
			assertRejected(IOException.class, () -> read(asCompressed(attribute, 10, size, 10)));
		}
	}

	public void testCompressedDataShorterThanDeclaredIsRejectedAsAnIOException() throws IOException {
		final byte[] attribute = write(bytes(10));
		assertRejected(IOException.class, () -> read(asCompressed(attribute, 10, 1000, 500)));
	}

	public void testGenuineCompressedDataStillReads() throws IOException {
		byte[] attribute = write(bytes(10));
		assertEquals(1000, read(asCompressed(attribute, 10, 1000, 1000)).getUnwovenClassFileData().length);
	}

	/** A woven class file of 40 bytes, and an unwoven one that starts the same way for the given number of bytes. */
	private static byte[] woven() {
		return bytes(40);
	}

	private static byte[] unwovenSharing(int sharedWithWoven) {
		byte[] unwoven = Arrays.copyOf(woven(), 50);
		for (int i = sharedWithWoven; i < unwoven.length; i++) {
			unwoven[i] = (byte) (i + 1);
		}
		return unwoven;
	}

	/** The length in common is the int at offset 2 of the diff. */
	private static byte[] withLengthInCommon(byte[] diff, int lengthInCommon) {
		byte[] copy = diff.clone();
		for (int i = 0; i < 4; i++) {
			copy[2 + i] = (byte) (lengthInCommon >>> (24 - 8 * i));
		}
		return copy;
	}

	public void testDiffStillRoundTrips() {
		WeaverStateInfo wsi = new WeaverStateInfo(true);
		for (int shared : new int[] { 10, 25, 40 /* all of the woven class file */ }) {
			byte[] unwoven = unwovenSharing(shared);
			assertTrue(Arrays.equals(unwoven, wsi.applyDiff(woven(), wsi.generateDiff(woven(), unwoven))));
		}
	}

	public void testInvalidLengthInCommonIsRejected() {
		final WeaverStateInfo wsi = new WeaverStateInfo(true);
		final byte[] diff = wsi.generateDiff(woven(), unwovenSharing(25));
		for (final int lengthInCommon : new int[] { 0x7ffffff0, -5, 31 /* one more than woven.length - 10 */ }) {
			assertRejected(BCException.class, () -> wsi.applyDiff(woven(), withLengthInCommon(diff, lengthInCommon)));
		}
	}

	public void testDiffOrWovenClassTooShortIsRejected() {
		final WeaverStateInfo wsi = new WeaverStateInfo(true);
		final byte[] diff = wsi.generateDiff(woven(), unwovenSharing(25));
		assertRejected(BCException.class, () -> wsi.applyDiff(woven(), new byte[3]));
		assertRejected(BCException.class, () -> wsi.applyDiff(new byte[5], diff));
	}
}
