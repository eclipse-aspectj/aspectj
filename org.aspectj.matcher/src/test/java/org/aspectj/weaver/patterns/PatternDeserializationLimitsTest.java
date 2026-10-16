/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.VersionedDataInputStream;

import junit.framework.TestCase;

/**
 * Checks that counts read from serialized pattern data are validated before they are used to size arrays.
 */
public class PatternDeserializationLimitsTest extends TestCase {

	private interface Writer {
		void write(CompressingDataOutputStream s) throws IOException;
	}

	private interface Reader {
		void read(VersionedDataInputStream s) throws IOException;
	}

	private final ConstantPoolSimulator cps = new ConstantPoolSimulator();

	private byte[] bytes(Writer writer) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		CompressingDataOutputStream s = new CompressingDataOutputStream(bos, cps);
		writer.write(s);
		s.flush();
		return bos.toByteArray();
	}

	private VersionedDataInputStream stream(byte[] data) {
		return new VersionedDataInputStream(new ByteArrayInputStream(data), cps);
	}

	private void assertRejected(byte[] data, Reader reader) throws IOException {
		try {
			reader.read(stream(data));
			fail("expected the invalid array length to be rejected");
		} catch (BCException expected) {
		}
	}

	private byte[] wildTypePattern() throws IOException {
		WildTypePattern wtp = new WildTypePattern(Collections.singletonList(new NamePattern("Foo")), false, 0);
		return bytes(wtp::write);
	}

	public void testTypeVariablePatternListWithHugeCountIsRejected() throws IOException {
		assertRejected(bytes(s -> s.writeInt(Integer.MAX_VALUE)), s -> TypeVariablePatternList.read(s, null));
	}

	public void testTypeVariablePatternWithHugeBoundsCountIsRejected() throws IOException {
		assertRejected(bytes(s -> {
			s.writeUTF("T");
			TypePattern.ANY.write(s);
			s.writeInt(Integer.MAX_VALUE);
		}), s -> TypeVariablePattern.read(s, null));
	}

	public void testWildTypePatternWithHugeInterfaceBoundsCountIsRejected() throws IOException {
		byte[] data = wildTypePattern();
		int last = data.length - 4; // the final int written is the (zero) count of additional interface bounds
		assertEquals(0, data[last] | data[last + 1] | data[last + 2] | data[last + 3]);
		data[last] = 0x7f;
		data[last + 1] = data[last + 2] = data[last + 3] = (byte) 0xff;
		assertRejected(data, s -> TypePattern.read(s, null));
	}

	public void testWildTypePatternWithNegativeNameCountIsRejected() throws IOException {
		byte[] data = wildTypePattern();
		data[2] = data[3] = (byte) 0xff; // layout: key byte, version byte, then the short count of name patterns
		assertRejected(data, s -> TypePattern.read(s, null));
	}

	public void testTypePatternListWithNegativeCountIsRejected() throws IOException {
		assertRejected(bytes(s -> s.writeShort(-1)), s -> TypePatternList.read(s, null));
	}

	public void testAnnotationPatternListWithNegativeCountIsRejected() throws IOException {
		assertRejected(bytes(s -> s.writeShort(-1)), s -> AnnotationPatternList.read(s, null));
	}

	public void testValidPatternsStillRead() throws IOException {
		assertTrue(TypePattern.read(stream(wildTypePattern()), null) instanceof WildTypePattern);

		TypeVariablePatternList list = new TypeVariablePatternList(
				new TypeVariablePattern[] { new TypeVariablePattern("T"), new TypeVariablePattern("U") });
		byte[] data = bytes(list::write);
		assertEquals(2, TypeVariablePatternList.read(stream(data), null).getTypeVariablePatterns().length);
	}
}
