/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import junit.framework.TestCase;

import org.aspectj.weaver.BcweaverTests;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelShadow;
import org.aspectj.weaver.bcel.BcelWorld;

/**
 * @author hugunin
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserTestCase extends TestCase {

	public ParserTestCase(String arg0) {
		super(arg0);
	}
	
	World world = new BcelWorld(BcweaverTests.TESTDATA_PATH + "/testcode.jar");
	
	public void testNamePatterns() {
		
		
//		checkNoMatch("abc *", "abcd");
//		checkNoMatch("* d", "abcd");
	}
	
	
	
	public void testParse() {
		PatternParser parser = new PatternParser("execution(void Hello.*(..))");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		//System.out.println(p);
		assertEquals(p.kind, BcelShadow.MethodExecution);
		assertTrue(p.signature.getName().matches("foobar"));
		
		
		try {
			new PatternParser("initialization(void foo())").parsePointcut();
			fail("should have been a parse error");
		} catch (ParserException pe) {
			// good
		}
	}
	
	public void testParseWithAnnotation() {
		PatternParser parser = new PatternParser("execution(@SimpleAnnotation void Hello.*(..))");
		KindedPointcut p = (KindedPointcut) parser.parsePointcut();
		// XXX - needs finishing...
		p.resolveBindings(makeSimpleScope(),new Bindings(3));
		System.err.println(p);
//		assertEquals(p.kind, BcelShadow.MethodExecution);
//		assertTrue(p.signature.getName().matches("foobar"));
//		p.signature.resolveBindings(makeSimpleScope(),new Bindings(3));		
	}
	
	public TestScope makeSimpleScope() {
		TestScope s = new TestScope(new String[] {"int", "java.lang.String"}, new String[] {"a", "b"}, world);
		s.setImportedPrefixes(new String[]{"p."});
		return s;
	}

}


