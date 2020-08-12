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

import junit.framework.TestCase;

/**
 * @author hugunin
 * 
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class NamePatternParserTestCase extends TestCase {
	/**
	 * Constructor for PatternTestCase.
	 * 
	 * @param name
	 */
	public NamePatternParserTestCase(String name) {
		super(name);
	}

	public void testMatch() {
		checkMatch(NamePatternTestCase.matchAll);
		checkMatch(NamePatternTestCase.match1);
		checkMatch(NamePatternTestCase.match2);

		NamePattern p = new PatternParser("abc *").parseNamePattern();
		assertEquals(new NamePattern("abc"), p);
	}

	public void testTypePattern() {
		TypePattern tp = null;
		tp = new PatternParser(" @Ann *  ").parseTypePattern();
		assertEquals(1, tp.start);
		assertEquals(6, tp.end);
		tp = new PatternParser("  (@Ann *)   ").parseTypePattern();
		assertEquals(2, tp.start);
		assertEquals(9, tp.end);
	}

	/**
	 * Method checkMatch.
	 * 
	 * @param string
	 * @param matchAll
	 * @param b
	 */
	private void checkMatch(String[] patterns) {
		for (String pattern : patterns) {
			ITokenSource tokenSource = BasicTokenSource.makeTokenSource(
					pattern, null);
			NamePattern p1 = new PatternParser(tokenSource).parseNamePattern();
			NamePattern p2 = new NamePattern(pattern);
			assertEquals("pattern: " + pattern, p2, p1);
			assertEquals("eof", IToken.EOF, tokenSource.next());
		}
	}
}
