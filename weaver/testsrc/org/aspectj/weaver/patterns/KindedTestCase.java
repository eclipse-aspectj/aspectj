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

import java.lang.reflect.Modifier;

import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.util.FuzzyBoolean;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class KindedTestCase extends TestCase {
	
	Pointcut callpc;
	Pointcut exepc;
	Pointcut exepcplus;
	Pointcut exepcCons;
	Pointcut adviceexepc;
	Pointcut initpc;
	Pointcut preinitpc;
	Pointcut staticinitpc;
	Pointcut getpc;
	Pointcut setpc;
	
	public void testKindedMatch() {
		Factory f = new Factory("KindedTestCase.java",KindedTestCase.class);
		
		// JoinPoints to match against...
		JoinPoint.StaticPart calljp1 = f.makeSJP(JoinPoint.METHOD_CALL,f.makeMethodSig(0,"main",Hello.class,new Class[] {String.class},new String[] {"s"},new Class[0],String.class),1);
		JoinPoint.StaticPart calljp2 = f.makeSJP(JoinPoint.METHOD_CALL,f.makeMethodSig(0,"sayHi",Hello.class,new Class[] {String.class},new String[] {"s"},new Class[0],String.class),1);
		JoinPoint.StaticPart exejp1 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"main",Hello.class,new Class[] {String.class},new String[] {"s"},new Class[0],String.class),1);
		JoinPoint.StaticPart exejp2 = f.makeSJP(JoinPoint.METHOD_EXECUTION,f.makeMethodSig(0,"sayHi",Hello.class,new Class[] {String.class},new String[] {"s"},new Class[0],void.class),1);
		JoinPoint.StaticPart execonsjp1 = f.makeSJP(JoinPoint.CONSTRUCTOR_EXECUTION,f.makeConstructorSig(0,Hello.class,new Class[0],new String[0],new Class[0]),1);
		JoinPoint.StaticPart execonsjp2 = f.makeSJP(JoinPoint.CONSTRUCTOR_EXECUTION,f.makeConstructorSig(0,String.class,new Class[] {String.class},new String[]{"s"},new Class[0]),1);
		JoinPoint.StaticPart initjp1 = f.makeSJP(JoinPoint.INITIALIZATION,f.makeConstructorSig(0,Hello.class,new Class[0],new String[0],new Class[0]),1);
		JoinPoint.StaticPart initjp2 = f.makeSJP(JoinPoint.PREINTIALIZATION,f.makeConstructorSig(0,Hello.class,new Class[]{int.class, int.class},new String[]{"a","b"},new Class[0]),1);
		JoinPoint.StaticPart initjp3 = f.makeSJP(JoinPoint.PREINTIALIZATION,f.makeConstructorSig(0,Hello.class,new Class[]{Integer.class, Integer.class},new String[]{"a","b"},new Class[0]),1);
		JoinPoint.StaticPart sinitjp1 = f.makeSJP(JoinPoint.STATICINITIALIZATION,f.makeInitializerSig(Modifier.STATIC,Hello.class),1);
		JoinPoint.StaticPart sinitjp2 = f.makeSJP(JoinPoint.STATICINITIALIZATION,f.makeInitializerSig(Modifier.STATIC,String.class),1);
		JoinPoint.StaticPart getjp1 = f.makeSJP(JoinPoint.FIELD_GET,f.makeFieldSig(0,"x",Hello.class,int.class),1);
		JoinPoint.StaticPart getjp2 = f.makeSJP(JoinPoint.FIELD_GET,f.makeFieldSig(0,"y",String.class,String.class),1);
		JoinPoint.StaticPart setjp1 = f.makeSJP(JoinPoint.FIELD_SET,f.makeFieldSig(0,"x",Hello.class,int.class),1);
		JoinPoint.StaticPart setjp2 = f.makeSJP(JoinPoint.FIELD_SET,f.makeFieldSig(0,"y",String.class,String.class),1);
		JoinPoint.StaticPart advjp = f.makeSJP(JoinPoint.ADVICE_EXECUTION,f.makeAdviceSig(0,"foo",Hello.class,new Class[0],new String[0],new Class[0],void.class),1);
		
		checkMatches(callpc,calljp1,FuzzyBoolean.YES);
		checkMatches(callpc,calljp2,FuzzyBoolean.NO);
		checkMatches(callpc,exejp1,FuzzyBoolean.NO);
		checkMatches(exepc,exejp1,FuzzyBoolean.NO);
		checkMatches(exepc,exejp2,FuzzyBoolean.YES);
		checkMatches(exepcplus,exejp1,FuzzyBoolean.NO);
		checkMatches(exepcplus,exejp2,FuzzyBoolean.YES);
		checkMatches(exepcCons,execonsjp1,FuzzyBoolean.YES);
		checkMatches(exepcCons,execonsjp2,FuzzyBoolean.NO);
		checkMatches(exepcCons,exejp1,FuzzyBoolean.NO);
		checkMatches(initpc,initjp1,FuzzyBoolean.YES);
		checkMatches(initpc,initjp2,FuzzyBoolean.NO);
		checkMatches(preinitpc,initjp1,FuzzyBoolean.NO);
		checkMatches(preinitpc,initjp2,FuzzyBoolean.YES);
		checkMatches(preinitpc,initjp3,FuzzyBoolean.NO);
		checkMatches(staticinitpc,sinitjp1,FuzzyBoolean.YES);
		checkMatches(staticinitpc,sinitjp2,FuzzyBoolean.NO);
		checkMatches(getpc,getjp1,FuzzyBoolean.YES);
		checkMatches(getpc,getjp2,FuzzyBoolean.YES);
		checkMatches(setpc,setjp1,FuzzyBoolean.YES);
		checkMatches(setpc,setjp2,FuzzyBoolean.NO);
		checkMatches(adviceexepc,advjp,FuzzyBoolean.YES);
	}
	
	private void checkMatches(Pointcut p, JoinPoint.StaticPart jpsp, FuzzyBoolean expected) {
		assertEquals(expected,p.match(jpsp));
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		callpc = new PatternParser("call(* main(..))").parsePointcut().resolve();
		exepc = new PatternParser("execution(void org.aspectj.weaver.patterns.KindedTestCase.Hello.sayHi(String))").parsePointcut().resolve();
		exepcplus = new PatternParser("execution(void Object+.sayHi(String))").parsePointcut().resolve();
		exepcCons = new PatternParser("execution(org.aspectj.weaver.patterns.KindedTestCase.Hello.new(..))").parsePointcut().resolve();
		initpc = new PatternParser("initialization(new(..))").parsePointcut().resolve();
		preinitpc = new PatternParser("preinitialization(*..H*.new(int,int))").parsePointcut().resolve();
		staticinitpc = new PatternParser("staticinitialization(org.aspectj.weaver.patterns.KindedTestCase.Hello)").parsePointcut().resolve();
		getpc = new PatternParser("get(* *)").parsePointcut().resolve();
		setpc = new PatternParser("set(int x)").parsePointcut().resolve();
		adviceexepc = new PatternParser("adviceexecution()").parsePointcut().resolve();
	}
	
	private static class Hello {};
}
