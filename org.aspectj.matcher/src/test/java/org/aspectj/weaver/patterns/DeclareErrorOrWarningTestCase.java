/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.VersionedDataInputStream;

public class DeclareErrorOrWarningTestCase extends TestCase {

	public void testParse() throws IOException {
		DeclareErrorOrWarning d = parse("declare error: call(void foo()): \"that is bad\";");
		assertTrue(d.isError());
		assertEquals(d.getPointcut(), new PatternParser("call(void foo())").parsePointcut());
		assertEquals("that is bad", d.getMessage());
		checkSerialization(d);

		d = parse("declare warning: bar() && baz(): \"boo!\";");
		assertTrue(!d.isError());
		assertEquals(d.getPointcut(), new PatternParser("bar() && baz()").parsePointcut());
		assertEquals("boo!", d.getMessage());
		checkSerialization(d);

	}

	public void testStartAndEndPositionSet() throws IOException {
		DeclareErrorOrWarning d = parse("declare error: call(void foo()): \"that is bad\";");
		assertEquals("start position should be 0", 0, d.getStart());
		assertEquals("end position should be 46", 46, d.getEnd());
	}

	private DeclareErrorOrWarning parse(String string) {
		return (DeclareErrorOrWarning) new PatternParser(string).parseDeclare();
	}

	private void checkSerialization(Declare declare) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ConstantPoolSimulator cps = new ConstantPoolSimulator();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bo, cps);
		declare.write(out);
		out.close();

		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi, cps);
		Declare newDeclare = Declare.read(in, null);

		assertEquals("write/read", declare, newDeclare);
	}
}
