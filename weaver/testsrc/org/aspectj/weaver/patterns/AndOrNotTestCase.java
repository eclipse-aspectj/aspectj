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

import java.io.*;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.bcel.*;

import junit.framework.TestCase;
import org.aspectj.weaver.*;

/**
 * @author hugunin
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AndOrNotTestCase extends TestCase {		
	/**
	 * Constructor for PatternTestCase.
	 * @param name
	 */
	public AndOrNotTestCase(String name) {
		super(name);
	}
	
	World world;
	                          	
	
	public void testMatch() throws IOException {
		world = new BcelWorld();
		
		Pointcut foo = makePointcut("this(Foo)");
		Pointcut bar = makePointcut("this(Bar)");
		Pointcut c = makePointcut("this(C)");
		
		checkEquals("this(Foo) && this(Bar)", new AndPointcut(foo, bar));
		checkEquals("this(Foo) && this(Bar) && this(C)", new AndPointcut(foo, new AndPointcut(bar, c)));


		checkEquals("this(Foo) || this(Bar)", new OrPointcut(foo, bar));
		checkEquals("this(Foo) || this(Bar) || this(C)", new OrPointcut(foo, new OrPointcut(bar, c)));
		
		checkEquals("this(Foo) && this(Bar) || this(C)", new OrPointcut(new AndPointcut(foo, bar), c));
		checkEquals("this(Foo) || this(Bar) && this(C)", new OrPointcut(foo, new AndPointcut(bar, c)));
		checkEquals("(this(Foo) || this(Bar)) && this(C)", new AndPointcut(new OrPointcut(foo, bar), c));
		checkEquals("this(Foo) || (this(Bar) && this(C))", new OrPointcut(foo, new AndPointcut(bar, c)));
	
		checkEquals("!this(Foo)", new NotPointcut(foo));
		checkEquals("!this(Foo) && this(Bar)", new AndPointcut(new NotPointcut(foo), bar));
		checkEquals("!(this(Foo) && this(Bar)) || this(C)", new OrPointcut(new NotPointcut(new AndPointcut(foo, bar)), c));
		checkEquals("!!this(Foo)", new NotPointcut(new NotPointcut(foo)));
		
	}
	
	public void testJoinPointMatch() {
		Pointcut foo = makePointcut("this(org.aspectj.weaver.patterns.AndOrNotTestCase.Foo)").resolve();
		Pointcut bar = makePointcut("this(org.aspectj.weaver.patterns.AndOrNotTestCase.Bar)").resolve();
		Pointcut c   = makePointcut("this(org.aspectj.weaver.patterns.AndOrNotTestCase.C)").resolve();
		
		Factory f = new Factory("AndOrNotTestCase.java",AndOrNotTestCase.class);
		
		Signature methodSig = f.makeMethodSig("void aMethod()");
		JoinPoint.StaticPart jpsp = f.makeSJP(JoinPoint.METHOD_EXECUTION,methodSig,1);
		JoinPoint jp = Factory.makeJP(jpsp,new Foo(),new Foo());
		
		checkMatches(new AndPointcut(foo,bar),jp,null,FuzzyBoolean.NO);
		checkMatches(new AndPointcut(foo,foo),jp,null,FuzzyBoolean.YES);
		checkMatches(new AndPointcut(bar,foo),jp,null,FuzzyBoolean.NO);
		checkMatches(new AndPointcut(bar,c),jp,null,FuzzyBoolean.NO);
		
		checkMatches(new OrPointcut(foo,bar),jp,null,FuzzyBoolean.YES);
		checkMatches(new OrPointcut(foo,foo),jp,null,FuzzyBoolean.YES);
		checkMatches(new OrPointcut(bar,foo),jp,null,FuzzyBoolean.YES);
		checkMatches(new OrPointcut(bar,c),jp,null,FuzzyBoolean.NO);
		
		checkMatches(new NotPointcut(foo),jp,null,FuzzyBoolean.NO);
		checkMatches(new NotPointcut(bar),jp,null,FuzzyBoolean.YES);
	}

	private Pointcut makePointcut(String pattern) {
		return new PatternParser(pattern).parsePointcut();
	}
	
	private void checkEquals(String pattern, Pointcut p) throws IOException {
		assertEquals(pattern, p, makePointcut(pattern));
		checkSerialization(pattern);
	}

	private void checkMatches(Pointcut p, JoinPoint jp, JoinPoint.StaticPart jpsp, FuzzyBoolean expected) {
		assertEquals(expected,p.match(jp,jpsp));
	}
	
//	private void checkMatch(Pointcut p, Signature[] matches, boolean shouldMatch) {
//		for (int i=0; i<matches.length; i++) {
//			boolean result = p.matches(matches[i]);
//			String msg = "matches " + p + " to " + matches[i] + " expected ";
//			if (shouldMatch) {
//				assertTrue(msg + shouldMatch, result);
//			} else {
//				assertTrue(msg + shouldMatch, !result);
//			}
//		}
//	}
//	
//	public void testSerialization() throws IOException {
//		String[] patterns = new String[] {
//			"public * *(..)", "void *.foo(A, B)", "A b()"
//		};
//		
//		for (int i=0, len=patterns.length; i < len; i++) {
//			checkSerialization(patterns[i]);
//		}
//	}

	/**
	 * Method checkSerialization.
	 * @param string
	 */
	private void checkSerialization(String string) throws IOException {
		Pointcut p = makePointcut(string);
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bo);
		p.write(out);
		out.close();
		
		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		DataInputStream in = new DataInputStream(bi);
		Pointcut newP = Pointcut.read(in, null);
		
		assertEquals("write/read", p, newP);	
	}
	
	private static class Foo{};
	private static class Bar{};
	private static class C{};
	
}
