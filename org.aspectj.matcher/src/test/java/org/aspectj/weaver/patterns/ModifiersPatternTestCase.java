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
import java.lang.reflect.Modifier;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.reflect.ReflectionWorld;

public class ModifiersPatternTestCase extends PatternsTestCase {

	public void testMatch() {
		int[] publicMatches = new int[] { Modifier.PUBLIC, Modifier.PUBLIC | Modifier.STATIC,
				Modifier.PUBLIC | Modifier.STATIC | Modifier.STRICT | Modifier.FINAL, };

		int[] publicFailures = new int[] { Modifier.PRIVATE, 0, Modifier.STATIC | Modifier.STRICT | Modifier.FINAL, };

		int[] publicStaticMatches = new int[] { Modifier.PUBLIC | Modifier.STATIC,
				Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL | Modifier.STRICT, };

		int[] publicStaticFailures = new int[] { 0, Modifier.PUBLIC, Modifier.STATIC, };

		int[] trickMatches = new int[] { Modifier.PRIVATE, Modifier.PRIVATE | Modifier.ABSTRACT, Modifier.PRIVATE | Modifier.FINAL, };

		int[] trickFailures = new int[] { Modifier.PUBLIC, Modifier.PRIVATE | Modifier.STATIC, Modifier.PRIVATE | Modifier.STRICT, };

		int[] none = new int[0];

		checkMatch("", publicMatches, none);
		checkMatch("", publicFailures, none);
		checkMatch("!public", publicFailures, publicMatches);
		checkMatch("public", publicMatches, publicFailures);
		checkMatch("public static", none, publicFailures);
		checkMatch("public static", publicStaticMatches, publicStaticFailures);

		checkMatch("private !static !strictfp", trickMatches, trickFailures);
		checkMatch("private !static !strictfp", none, publicMatches);
		checkMatch("private !static !strictfp", none, publicStaticMatches);
	}

	private ModifiersPattern makeModifiersPattern(String pattern) {
		return new PatternParser(pattern).parseModifiersPattern();
	}

	private void checkMatch(String pattern, int[] shouldMatch, int[] shouldFail) {
		ModifiersPattern p = makeModifiersPattern(pattern);
		checkMatch(p, shouldMatch, true);
		checkMatch(p, shouldFail, false);
	}

	private void checkMatch(ModifiersPattern p, int[] matches, boolean shouldMatch) {
		for (int match : matches) {
			boolean result = p.matches(match);
			String msg = "matches " + p + " to " + Modifier.toString(match) + " expected ";
			if (shouldMatch) {
				assertTrue(msg + shouldMatch, result);
			} else {
				assertTrue(msg + shouldMatch, !result);
			}
		}
	}

	public void testSerialization() throws IOException {
		String[] patterns = new String[] { "", "!public", "public", "public static", "private !static !strictfp", };

		for (String pattern : patterns) {
			checkSerialization(pattern);
		}
	}

	/**
	 * Method checkSerialization.
	 * 
	 * @param string
	 */
	private void checkSerialization(String string) throws IOException {
		ModifiersPattern p = makeModifiersPattern(string);
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ConstantPoolSimulator cps = new ConstantPoolSimulator();
		CompressingDataOutputStream out = new CompressingDataOutputStream(bo, cps);
		p.write(out);
		out.close();

		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		VersionedDataInputStream in = new VersionedDataInputStream(bi, cps);
		ModifiersPattern newP = ModifiersPattern.read(in);

		assertEquals("write/read", p, newP);
	}

	public World getWorld() {
		return new ReflectionWorld(true, this.getClass().getClassLoader());
	}

}
