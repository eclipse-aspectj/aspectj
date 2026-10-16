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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.aspectj.weaver.AjAttribute.AdviceAttribute;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.patterns.Pointcut;

import junit.framework.TestCase;

/**
 * Checks that a forged length in an Around advice attribute is reported as a malformed attribute rather than crashing.
 */
public class AjAttributeReadLimitsTest extends TestCase {

	private static final String ADVICE = AdviceAttribute.AttributeName;

	/** Ends with an int (length of the boolean array) followed by a short (length of the declared exceptions array), both zero. */
	private static byte[] aroundAdvice() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bos, null);
		new AdviceAttribute(AdviceKind.Around, Pointcut.makeMatchesNothing(Pointcut.RESOLVED), 0, 0, 0, null, false,
				new ResolvedMember[0], new boolean[0], UnresolvedType.NONE).write(out);
		out.flush();
		return bos.toByteArray();
	}

	private static AjAttribute read(byte[] bytes) {
		return AjAttribute.read(new WeaverVersionInfo(), ADVICE, bytes, null, null, null);
	}

	private static void assertMalformed(byte[] bytes) {
		try {
			read(bytes);
			fail("expected the attribute to be rejected");
		} catch (BCException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("malformed " + ADVICE));
		}
	}

	public void testGenuineAroundAdviceStillReads() throws IOException {
		assertTrue(read(aroundAdvice()) instanceof AdviceAttribute);
	}

	public void testHugeBooleanArrayLengthIsReportedAsMalformed() throws IOException {
		byte[] bytes = aroundAdvice();
		int at = bytes.length - 6;
		assertEquals(0, bytes[at] | bytes[at + 1] | bytes[at + 2] | bytes[at + 3]);
		bytes[at] = 0x7f;
		bytes[at + 1] = bytes[at + 2] = bytes[at + 3] = (byte) 0xff;
		assertMalformed(bytes);
	}

	public void testNegativeDeclaredExceptionsLengthIsReportedAsMalformed() throws IOException {
		byte[] bytes = aroundAdvice();
		bytes[bytes.length - 2] = bytes[bytes.length - 1] = (byte) 0xff;
		assertMalformed(bytes);
	}
}
