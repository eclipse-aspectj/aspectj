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

import junit.framework.TestCase;

import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.util.FuzzyBoolean;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ArgsTestCase extends TestCase {
	
	Pointcut wildcardArgs;
	Pointcut oneA;
	Pointcut oneAandaC;
	Pointcut BthenAnything;
	Pointcut singleArg;
	
	public void testMatchJP() {
		Factory f = new Factory("ArgsTestCase.java",ArgsTestCase.A.class);
		
		JoinPoint.StaticPart jpsp1 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"aMethod",A.class,new Class[] {A.class},new String[] {"a"},new Class[] {},null) ,1);
		JoinPoint.StaticPart jpsp2 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"aMethod",A.class,new Class[] {B.class},new String[] {"b"},new Class[] {},null),1);
		JoinPoint.StaticPart jpsp3 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"aMethod",A.class,new Class[] {A.class,C.class},new String[] {"a","c"},new Class[] {},null),1);
		JoinPoint.StaticPart jpsp4 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"aMethod",A.class,new Class[] {A.class,A.class},new String[] {"a","a2"},new Class[] {},null),1);
		JoinPoint oneAArg = Factory.makeJP(jpsp1,new A(),new A(),new A());
		JoinPoint oneBArg = Factory.makeJP(jpsp2,new A(), new A(), new B());
		JoinPoint acArgs = Factory.makeJP(jpsp3,new A(), new A(), new A(), new C());
		JoinPoint baArgs = Factory.makeJP(jpsp4,new A(), new A(), new B(), new A());
		
		checkMatches(wildcardArgs,oneAArg,null,FuzzyBoolean.YES);
		checkMatches(wildcardArgs,oneBArg,null,FuzzyBoolean.YES);
		checkMatches(wildcardArgs,acArgs,null,FuzzyBoolean.YES);
		checkMatches(wildcardArgs,baArgs,null,FuzzyBoolean.YES);
		
		checkMatches(oneA,oneAArg,null,FuzzyBoolean.YES);
		checkMatches(oneA,oneBArg,null,FuzzyBoolean.YES);
		checkMatches(oneA,acArgs,null,FuzzyBoolean.NO);
		checkMatches(oneA,baArgs,null,FuzzyBoolean.NO);

		checkMatches(oneAandaC,oneAArg,null,FuzzyBoolean.NO);
		checkMatches(oneAandaC,oneBArg,null,FuzzyBoolean.NO);
		checkMatches(oneAandaC,acArgs,null,FuzzyBoolean.YES);
		checkMatches(oneAandaC,baArgs,null,FuzzyBoolean.NO);
		
		checkMatches(BthenAnything,oneAArg,null,FuzzyBoolean.NO);
		checkMatches(BthenAnything,oneBArg,null,FuzzyBoolean.YES);
		checkMatches(BthenAnything,acArgs,null,FuzzyBoolean.NO);
		checkMatches(BthenAnything,baArgs,null,FuzzyBoolean.YES);

		checkMatches(singleArg,oneAArg,null,FuzzyBoolean.YES);
		checkMatches(singleArg,oneBArg,null,FuzzyBoolean.YES);
		checkMatches(singleArg,acArgs,null,FuzzyBoolean.NO);
		checkMatches(singleArg,baArgs,null,FuzzyBoolean.NO);

	}
	
	public void testMatchJPWithPrimitiveTypes() {
		try {
			Factory f = new Factory("ArgsTestCase.java",ArgsTestCase.A.class);
			
			Pointcut oneInt = new PatternParser("args(int)").parsePointcut().resolve();
			Pointcut oneInteger = new PatternParser("args(Integer)").parsePointcut().resolve();

			JoinPoint.StaticPart oneIntjp = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"aMethod",A.class,new Class[] {int.class},new String[] {"i"},new Class[] {},null) ,1);
			JoinPoint.StaticPart oneIntegerjp = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"aMethod",A.class,new Class[] {Integer.class},new String[] {"i"},new Class[] {},null),1);

			JoinPoint oneIntArg = Factory.makeJP(oneIntjp,new A(),new A(),new Integer(3));
			JoinPoint oneIntegerArg = Factory.makeJP(oneIntegerjp,new A(), new A(), new Integer(7));
			
			checkMatches(oneInt,oneIntArg,null,FuzzyBoolean.YES);
			checkMatches(oneInt,oneIntegerArg,null,FuzzyBoolean.NO);
			checkMatches(oneInteger,oneIntArg,null,FuzzyBoolean.NO);
			checkMatches(oneInteger,oneIntegerArg,null,FuzzyBoolean.YES);
			
		} catch( Exception ex) {
			fail("Unexpected exception " + ex);
		}
		
	}
	
	private void checkMatches(Pointcut p, JoinPoint jp, JoinPoint.StaticPart jpsp, FuzzyBoolean expected) {
		assertEquals(expected,p.match(jp,jpsp));
	}
	
	private static class A {};
	private static class B extends A {};
	private static class C {};

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		wildcardArgs = new PatternParser("args(..)").parsePointcut().resolve();
		oneA = new PatternParser("args(org.aspectj.weaver.patterns.ArgsTestCase.A)").parsePointcut().resolve();
		oneAandaC = new PatternParser("args(org.aspectj.weaver.patterns.ArgsTestCase.A,org.aspectj.weaver.patterns.ArgsTestCase.C)").parsePointcut().resolve();
		BthenAnything = new PatternParser("args(org.aspectj.weaver.patterns.ArgsTestCase.B,..)").parsePointcut().resolve();
		singleArg = new PatternParser("args(*)").parsePointcut().resolve();
	}
}
