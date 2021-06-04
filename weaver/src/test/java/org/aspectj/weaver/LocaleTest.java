/* Copyright (c) 2002 Contributors.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 */
package org.aspectj.weaver;

import java.io.IOException;
import java.util.Locale;

import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.util.ByteSequence;

import junit.framework.TestCase;

public class LocaleTest extends TestCase {

	public LocaleTest(String name) {
		super(name);
	}

	public void testNormalLocale() {
		doBipush();
	}

	public void testTurkishLocale() {
		Locale def = Locale.getDefault();
		Locale.setDefault(new Locale("tr", ""));
		try {
			doBipush();
		} finally {
			Locale.setDefault(def);
		}
	}

	private static void doBipush() {
		try {
			Instruction.readInstruction(
						new ByteSequence(new byte[] {
							(byte)16, // bipush
							(byte) 3  // data for bipush
							}));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}

