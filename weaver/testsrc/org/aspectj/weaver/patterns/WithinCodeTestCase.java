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

import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.util.FuzzyBoolean;

import junit.framework.TestCase;


public class WithinCodeTestCase extends TestCase {
	Pointcut withinCode1;
	Pointcut withinCode2;
	Pointcut withinCode3;

	public void testMatchJP() {
		Factory f = new Factory("WithinCodeTestCase.java",WithinCodeTestCase.class);
		
		// JoinPoints to match against...
		JoinPoint.StaticPart exejp1 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"toString",Object.class,new Class[] {},new String[] {},new Class[0],String.class),1);
		JoinPoint.StaticPart exejp2 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"sayHi",Hello.class,new Class[] {String.class},new String[] {"s"},new Class[0],void.class),1);
		JoinPoint.StaticPart execonsjp1 = f.makeSJP(JoinPoint.CONSTRUCTOR_EXECUTION,f.makeConstructorSig(0,Object.class,new Class[0],new String[0],new Class[0]),1);
		JoinPoint.StaticPart execonsjp2 = f.makeSJP(JoinPoint.CONSTRUCTOR_EXECUTION,f.makeConstructorSig(0,String.class,new Class[] {String.class},new String[]{"s"},new Class[0]),1);
		
		checkMatches(withinCode1,exejp1,FuzzyBoolean.YES);
		checkMatches(withinCode1,exejp2,FuzzyBoolean.NO);
		checkMatches(withinCode1,execonsjp1,FuzzyBoolean.NO);
		checkMatches(withinCode1,execonsjp2,FuzzyBoolean.NO);

		checkMatches(withinCode2,exejp1,FuzzyBoolean.NO);
		checkMatches(withinCode2,exejp2,FuzzyBoolean.NO);
		checkMatches(withinCode2,execonsjp1,FuzzyBoolean.YES);
		checkMatches(withinCode2,execonsjp2,FuzzyBoolean.YES);

		checkMatches(withinCode3,exejp1,FuzzyBoolean.NO);
		checkMatches(withinCode3,exejp2,FuzzyBoolean.NO);
		checkMatches(withinCode3,execonsjp1,FuzzyBoolean.NO);
		checkMatches(withinCode3,execonsjp2,FuzzyBoolean.YES);

	}
	
	private void checkMatches(Pointcut p, JoinPoint.StaticPart jpsp, FuzzyBoolean expected) {
		assertEquals(expected,p.match(null,jpsp));
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		withinCode1 = new PatternParser("withincode(String Object.toString())").parsePointcut().resolve();
		withinCode2 = new PatternParser("withincode(new(..))").parsePointcut().resolve();
		withinCode3 = new PatternParser("withincode(String.new(..))").parsePointcut().resolve();
	}
	
	private static class Hello {};
}
