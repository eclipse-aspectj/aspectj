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
public class ThisOrTargetTestCase extends TestCase {		
	/**
	 * Constructor for PatternTestCase.
	 * @param name
	 */
	public ThisOrTargetTestCase(String name) {
		super(name);
	}
	
	World world;
	                          	
	
	public void testMatch() throws IOException {
		world = new BcelWorld();
		

		
	}
	
	public void testMatchJP() {
		Factory f = new Factory("ThisOrTargetTestCase.java",ThisOrTargetTestCase.class);
		
		Pointcut thisEx = new PatternParser("this(Exception)").parsePointcut().resolve();
		Pointcut thisIOEx = new PatternParser("this(java.io.IOException)").parsePointcut().resolve();

		Pointcut targetEx = new PatternParser("target(Exception)").parsePointcut().resolve();
		Pointcut targetIOEx = new PatternParser("target(java.io.IOException)").parsePointcut().resolve();

		JoinPoint.StaticPart jpsp1 = f.makeSJP(JoinPoint.EXCEPTION_HANDLER,f.makeCatchClauseSig(HandlerTestCase.class,Exception.class,"ex"),1);
		JoinPoint thisExJP = Factory.makeJP(jpsp1,new Exception(),this);
		JoinPoint thisIOExJP = Factory.makeJP(jpsp1,new IOException(),this);
		JoinPoint targetExJP = Factory.makeJP(jpsp1,this,new Exception());
		JoinPoint targetIOExJP = Factory.makeJP(jpsp1,this,new IOException());
		
		checkMatches(thisEx,thisExJP,null,FuzzyBoolean.YES);
		checkMatches(thisIOEx,thisExJP,null,FuzzyBoolean.NO);
		checkMatches(targetEx,thisExJP,null,FuzzyBoolean.NO);
		checkMatches(targetIOEx,thisExJP,null,FuzzyBoolean.NO);

		checkMatches(thisEx,thisIOExJP,null,FuzzyBoolean.YES);
		checkMatches(thisIOEx,thisIOExJP,null,FuzzyBoolean.YES);
		checkMatches(targetEx,thisIOExJP,null,FuzzyBoolean.NO);
		checkMatches(targetIOEx,thisIOExJP,null,FuzzyBoolean.NO);

		checkMatches(thisEx,targetExJP,null,FuzzyBoolean.NO);
		checkMatches(thisIOEx,targetExJP,null,FuzzyBoolean.NO);
		checkMatches(targetEx,targetExJP,null,FuzzyBoolean.YES);
		checkMatches(targetIOEx,targetExJP,null,FuzzyBoolean.NO);

		checkMatches(thisEx,targetIOExJP,null,FuzzyBoolean.NO);
		checkMatches(thisIOEx,targetIOExJP,null,FuzzyBoolean.NO);
		checkMatches(targetEx,targetIOExJP,null,FuzzyBoolean.YES);
		checkMatches(targetIOEx,targetIOExJP,null,FuzzyBoolean.YES);
	}
	
	private void checkMatches(Pointcut p, JoinPoint jp, JoinPoint.StaticPart jpsp, FuzzyBoolean expected) {
		assertEquals(expected,p.match(jp,jpsp));
	}

//	private Pointcut makePointcut(String pattern) {
//		return new PatternParser(pattern).parsePointcut();
//	}
	
//	private void checkEquals(String pattern, Pointcut p) throws IOException {
//		assertEquals(pattern, p, makePointcut(pattern));
//		checkSerialization(pattern);
//	}

	
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
//	private void checkSerialization(String string) throws IOException {
//		Pointcut p = makePointcut(string);
//		ByteArrayOutputStream bo = new ByteArrayOutputStream();
//		DataOutputStream out = new DataOutputStream(bo);
//		p.write(out);
//		out.close();
//		
//		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
//		DataInputStream in = new DataInputStream(bi);
//		Pointcut newP = Pointcut.read(in, null);
//		
//		assertEquals("write/read", p, newP);	
//	}
	
}
