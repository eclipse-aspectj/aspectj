/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.util.FuzzyBoolean;

import junit.framework.TestCase;


public class HandlerTestCase extends TestCase {

	private Pointcut hEx;
	private Pointcut hExPlus;
	private Pointcut hIOEx;
	
	public void testHandlerMatch() {
		Factory f = new Factory("HandlerTestCase.java",HandlerTestCase.class);
		
		JoinPoint.StaticPart jpsp1 = f.makeSJP(JoinPoint.EXCEPTION_HANDLER,f.makeCatchClauseSig(HandlerTestCase.class,Exception.class,"ex"),1);
		JoinPoint ex = Factory.makeJP(jpsp1,this,this,new Exception());
		JoinPoint ioex = Factory.makeJP(jpsp1,this,this,new IOException());
		JoinPoint myex = Factory.makeJP(jpsp1,this,this,new MyException());
		
		checkMatches(hEx,ex,null,FuzzyBoolean.YES);
		checkMatches(hEx,ioex,null,FuzzyBoolean.NO);
		checkMatches(hEx,myex,null,FuzzyBoolean.NO);

		checkMatches(hExPlus,ex,null,FuzzyBoolean.YES);
		checkMatches(hExPlus,ioex,null,FuzzyBoolean.YES);
		checkMatches(hExPlus,myex,null,FuzzyBoolean.YES);

		checkMatches(hIOEx,ex,null,FuzzyBoolean.NO);
		checkMatches(hIOEx,ioex,null,FuzzyBoolean.YES);
		checkMatches(hIOEx,myex,null,FuzzyBoolean.NO);

	}
	
	private void checkMatches(Pointcut p, JoinPoint jp, JoinPoint.StaticPart jpsp, FuzzyBoolean expected) {
		assertEquals(expected,p.match(jp,jpsp));
	}
	
	private static class MyException extends Exception {}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		hEx = new PatternParser("handler(Exception)").parsePointcut().resolve();
		hExPlus = new PatternParser("handler(Exception+)").parsePointcut().resolve();
		hIOEx = new PatternParser("handler(java.io.IOException)").parsePointcut().resolve();
	}
}
