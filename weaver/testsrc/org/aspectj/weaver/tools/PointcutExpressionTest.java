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
package org.aspectj.weaver.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class PointcutExpressionTest extends TestCase {

	PointcutParser p;
	Constructor asCons;
	Constructor bsCons;
	Constructor bsStringCons;
	Method a;
	Method aa;
	Method aaa;
	Field x;
	Field y;
	Method b;
	Method bsaa;
	Field n;
	Method foo;
	Method bar;
	
	public void testMatchesMethodCall() {
		PointcutExpression ex = p.parsePointcutExpression("call(* *..A.a*(..))");
		assertEquals("Should match call to A.a()",FuzzyBoolean.YES,ex.matchesMethodCall(a,Client.class,A.class,null));
		assertEquals("Should match call to A.aaa()",FuzzyBoolean.YES,ex.matchesMethodCall(aaa,Client.class,A.class,null));
		assertEquals("Should match call to B.aa()",FuzzyBoolean.YES,ex.matchesMethodCall(bsaa,Client.class,A.class,null));
		assertEquals("Should not match call to B.b()",FuzzyBoolean.NO,ex.matchesMethodCall(b,Client.class,A.class,null));
		ex = p.parsePointcutExpression("call(* *..A.a*(int))");
		assertEquals("Should match call to A.aa()",FuzzyBoolean.YES,ex.matchesMethodCall(aa,Client.class,A.class,null));
		assertEquals("Should not match call to A.a()",FuzzyBoolean.NO,ex.matchesMethodCall(a,Client.class,A.class,null));
		ex = p.parsePointcutExpression("call(void aaa(..)) && this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should match call to A.aaa() from Client",FuzzyBoolean.YES,ex.matchesMethodCall(aaa,Client.class,A.class,null));
		ex = p.parsePointcutExpression("call(void aaa(..)) && this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Should match call to A.aaa() from B",FuzzyBoolean.YES,ex.matchesMethodCall(aaa,B.class,A.class,null));
		assertEquals("May match call to A.aaa() from A",FuzzyBoolean.MAYBE,ex.matchesMethodCall(aaa,A.class,A.class,null));
		ex = p.parsePointcutExpression("execution(* *.*(..))");
		assertEquals("Should not match call to A.aa",FuzzyBoolean.NO,ex.matchesMethodCall(aa,A.class,A.class,null));
		// this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should match Client",FuzzyBoolean.YES,ex.matchesMethodCall(a,Client.class,A.class,null));
		assertEquals("Should not match A",FuzzyBoolean.NO,ex.matchesMethodCall(a,A.class,A.class,null));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Should maybe match B",FuzzyBoolean.MAYBE,ex.matchesMethodCall(bsaa,A.class,B.class,null));
		// target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should not match Client",FuzzyBoolean.NO,ex.matchesMethodCall(a,Client.class,A.class,null));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesMethodCall(a,Client.class,A.class,null));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Should maybe match A",FuzzyBoolean.MAYBE,ex.matchesMethodCall(aa,A.class,A.class,null));		
		// test args
		ex = p.parsePointcutExpression("args(..,int)");
		assertEquals("Should match A.aa",FuzzyBoolean.YES,ex.matchesMethodCall(aa,A.class,A.class,null));
		assertEquals("Should match A.aaa",FuzzyBoolean.YES,ex.matchesMethodCall(aaa,A.class,A.class,null));
		assertEquals("Should not match A.a",FuzzyBoolean.NO,ex.matchesMethodCall(a,A.class,A.class,null));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesMethodCall(a,A.class,A.class,null));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesMethodCall(a,B.class,A.class,null));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Should match",FuzzyBoolean.YES,ex.matchesMethodCall(b,B.class,B.class,bsaa));
		assertEquals("Should not match",FuzzyBoolean.NO,ex.matchesMethodCall(b,B.class,B.class,b));
	}

	public void testMatchesMethodExecution() {
		PointcutExpression ex = p.parsePointcutExpression("execution(* *..A.aa(..))");
		assertEquals("Should match execution of A.aa",FuzzyBoolean.YES,ex.matchesMethodExecution(aa,A.class));
		assertEquals("Should match execution of B.aa",FuzzyBoolean.YES,ex.matchesMethodExecution(bsaa,B.class));
		assertEquals("Should not match execution of A.a",FuzzyBoolean.NO,ex.matchesMethodExecution(a,B.class));
		ex = p.parsePointcutExpression("call(* *..A.a*(int))");
		assertEquals("Should not match execution of A.a",FuzzyBoolean.NO,ex.matchesMethodExecution(a,B.class));
		// test this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesMethodExecution(a,A.class));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesMethodExecution(a,A.class));
		assertEquals("Should match B",FuzzyBoolean.YES,ex.matchesMethodExecution(a,B.class));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesMethodExecution(a,Client.class));
		// test target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesMethodExecution(a,A.class));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesMethodExecution(a,A.class));
		assertEquals("Should match B",FuzzyBoolean.YES,ex.matchesMethodExecution(a,B.class));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesMethodExecution(a,Client.class));
		// test args
		ex = p.parsePointcutExpression("args(..,int)");
		assertEquals("Should match A.aa",FuzzyBoolean.YES,ex.matchesMethodExecution(aa,A.class));
		assertEquals("Should match A.aaa",FuzzyBoolean.YES,ex.matchesMethodExecution(aaa,A.class));
		assertEquals("Should not match A.a",FuzzyBoolean.NO,ex.matchesMethodExecution(a,A.class));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesMethodExecution(a,A.class));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesMethodExecution(bsaa,B.class));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Should not match",FuzzyBoolean.NO,ex.matchesMethodExecution(a,A.class));
	}

	public void testMatchesConstructorCall() {
		PointcutExpression ex = p.parsePointcutExpression("call(new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesConstructorCall(asCons,A.class,null));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesConstructorCall(bsStringCons,Client.class,null));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesConstructorCall(bsCons,Client.class,null));
		ex = p.parsePointcutExpression("call(*..A.new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesConstructorCall(asCons,A.class,null));
		assertEquals("Should not match B(String)", FuzzyBoolean.NO, ex.matchesConstructorCall(bsStringCons,Client.class,null));
		// this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should match Client",FuzzyBoolean.YES,ex.matchesConstructorCall(asCons,Client.class,null));
		assertEquals("Should not match A",FuzzyBoolean.NO,ex.matchesConstructorCall(asCons,A.class,null));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Should maybe match B",FuzzyBoolean.MAYBE,ex.matchesConstructorCall(asCons,A.class,null));
		// target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should not match Client",FuzzyBoolean.NO,ex.matchesConstructorCall(asCons,Client.class,null));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesConstructorCall(asCons,A.class,null));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Should maybe match A",FuzzyBoolean.MAYBE,ex.matchesConstructorCall(asCons,A.class,null));		
		// args
		ex = p.parsePointcutExpression("args(String)");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesConstructorCall(asCons,A.class,null));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesConstructorCall(bsStringCons,Client.class,null));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesConstructorCall(bsCons,Client.class,null));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesConstructorCall(asCons,A.class,null));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesConstructorCall(asCons,B.class,null));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Should match",FuzzyBoolean.YES,ex.matchesConstructorCall(bsCons,B.class,aa));
		assertEquals("Should not match",FuzzyBoolean.NO,ex.matchesConstructorCall(bsCons,B.class,b));
	}

	public void testMatchesConstructorExecution() {
		PointcutExpression ex = p.parsePointcutExpression("execution(new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesConstructorExecution(asCons,A.class));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesConstructorExecution(bsStringCons,Client.class));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesConstructorExecution(bsCons,Client.class));
		ex = p.parsePointcutExpression("execution(*..A.new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesConstructorExecution(asCons,A.class));
		assertEquals("Should not match B(String)", FuzzyBoolean.NO, ex.matchesConstructorExecution(bsStringCons,Client.class));
		// test this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesConstructorExecution(asCons,A.class));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesConstructorExecution(asCons,A.class));
		assertEquals("Should match B",FuzzyBoolean.YES,ex.matchesConstructorExecution(asCons,B.class));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesConstructorExecution(asCons,Client.class));
		// test target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesConstructorExecution(asCons,A.class));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesConstructorExecution(asCons,A.class));
		assertEquals("Should match B",FuzzyBoolean.YES,ex.matchesConstructorExecution(asCons,B.class));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesConstructorExecution(asCons,Client.class));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesConstructorExecution(asCons,B.class));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesConstructorExecution(bsCons,B.class));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Does not match",FuzzyBoolean.NO,ex.matchesConstructorExecution(bsCons,B.class));
		// args
		ex = p.parsePointcutExpression("args(String)");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesConstructorExecution(asCons,A.class));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesConstructorExecution(bsStringCons,Client.class));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesConstructorExecution(bsCons,Client.class));
	}

	public void testMatchesAdviceExecution() {
		PointcutExpression ex = p.parsePointcutExpression("adviceexecution()");
		assertEquals("Should match (advice) A.a",FuzzyBoolean.YES,ex.matchesAdviceExecution(a,A.class));
		// test this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should match Client",FuzzyBoolean.YES,ex.matchesAdviceExecution(a,Client.class));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesAdviceExecution(a,A.class));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesAdviceExecution(a,Client.class));
		// test target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should match Client",FuzzyBoolean.YES,ex.matchesAdviceExecution(a,Client.class));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesAdviceExecution(a,A.class));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesAdviceExecution(a,Client.class));
		// test within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesAdviceExecution(a,A.class));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesAdviceExecution(b,B.class));		
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Does not match",FuzzyBoolean.NO,ex.matchesAdviceExecution(a,A.class));
		// test args
		ex = p.parsePointcutExpression("args(..,int)");
		assertEquals("Should match A.aa",FuzzyBoolean.YES,ex.matchesAdviceExecution(aa,A.class));
		assertEquals("Should match A.aaa",FuzzyBoolean.YES,ex.matchesAdviceExecution(aaa,A.class));
		assertEquals("Should not match A.a",FuzzyBoolean.NO,ex.matchesAdviceExecution(a,A.class));
	}

	public void testMatchesHandler() {
		PointcutExpression ex = p.parsePointcutExpression("handler(Exception)");
		assertEquals("Should match catch(Exception)",FuzzyBoolean.YES,ex.matchesHandler(Exception.class,Client.class,null));
		assertEquals("Should not match catch(Throwable)",FuzzyBoolean.NO,ex.matchesHandler(Throwable.class,Client.class,null));
		// test this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should match Client",FuzzyBoolean.YES,ex.matchesHandler(Exception.class,Client.class,null));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesHandler(Exception.class,A.class,null));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesHandler(Exception.class,Client.class,null));
		// target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("Should match Client",FuzzyBoolean.YES,ex.matchesHandler(Exception.class,Client.class,null));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesHandler(Exception.class,A.class,null));
		assertEquals("Does not match client",FuzzyBoolean.NO,ex.matchesHandler(Exception.class,Client.class,null));
		// args
		ex = p.parsePointcutExpression("args(Exception)");
		assertEquals("Should match Exception",FuzzyBoolean.YES, ex.matchesHandler(Exception.class,Client.class,null));
		assertEquals("Should match RuntimeException",FuzzyBoolean.YES, ex.matchesHandler(RuntimeException.class,Client.class,null));
		assertEquals("Should not match String",FuzzyBoolean.NO,ex.matchesHandler(String.class,Client.class,null));
		assertEquals("Maybe matches Throwable",FuzzyBoolean.MAYBE,ex.matchesHandler(Throwable.class,Client.class,null));
		// within
		ex = p.parsePointcutExpression("within(*..Client)");
		assertEquals("Matches in class Client",FuzzyBoolean.YES,ex.matchesHandler(Exception.class,Client.class,null));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesHandler(Exception.class,B.class,null));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Matches within aa",FuzzyBoolean.YES,ex.matchesHandler(Exception.class,Client.class,aa));
		assertEquals("Does not match within b",FuzzyBoolean.NO,ex.matchesHandler(Exception.class,Client.class,b));
	}

	public void testMatchesInitialization() {
		PointcutExpression ex = p.parsePointcutExpression("initialization(new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesInitialization(asCons));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesInitialization(bsStringCons));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesInitialization(bsCons));
		ex = p.parsePointcutExpression("initialization(*..A.new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesInitialization(asCons));
		assertEquals("Should not match B(String)", FuzzyBoolean.NO, ex.matchesInitialization(bsStringCons));
		// test this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesInitialization(asCons));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesInitialization(asCons));
		// test target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesInitialization(asCons));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesInitialization(asCons));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesInitialization(asCons));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesInitialization(bsCons));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Does not match",FuzzyBoolean.NO,ex.matchesInitialization(bsCons));
		// args
		ex = p.parsePointcutExpression("args(String)");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesInitialization(asCons));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesInitialization(bsStringCons));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesInitialization(bsCons));
	}

	public void testMatchesPreInitialization() {
		PointcutExpression ex = p.parsePointcutExpression("preinitialization(new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesPreInitialization(asCons));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesPreInitialization(bsStringCons));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesPreInitialization(bsCons));
		ex = p.parsePointcutExpression("preinitialization(*..A.new(String))");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesPreInitialization(asCons));
		assertEquals("Should not match B(String)", FuzzyBoolean.NO, ex.matchesPreInitialization(bsStringCons));
		// test this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesPreInitialization(asCons));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesPreInitialization(asCons));
		// test target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("Should match A",FuzzyBoolean.YES,ex.matchesPreInitialization(asCons));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("Maybe matches B",FuzzyBoolean.MAYBE,ex.matchesPreInitialization(asCons));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesPreInitialization(asCons));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesPreInitialization(bsCons));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Does not match",FuzzyBoolean.NO,ex.matchesPreInitialization(bsCons));
		// args
		ex = p.parsePointcutExpression("args(String)");
		assertEquals("Should match A(String)",FuzzyBoolean.YES, ex.matchesPreInitialization(asCons));
		assertEquals("Should match B(String)", FuzzyBoolean.YES, ex.matchesPreInitialization(bsStringCons));
		assertEquals("Should not match B()", FuzzyBoolean.NO,ex.matchesPreInitialization(bsCons));	}

	public void testMatchesStaticInitialization() {
		// staticinit
		PointcutExpression ex = p.parsePointcutExpression("staticinitialization(*..A+)");
		assertEquals("Matches A",FuzzyBoolean.YES,ex.matchesStaticInitialization(A.class));
		assertEquals("Matches B",FuzzyBoolean.YES,ex.matchesStaticInitialization(B.class));
		assertEquals("Doesn't match Client",FuzzyBoolean.NO,ex.matchesStaticInitialization(Client.class));
		// this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("No this",FuzzyBoolean.NO,ex.matchesStaticInitialization(A.class));
		// target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertEquals("No target",FuzzyBoolean.NO,ex.matchesStaticInitialization(A.class));		
		// args
		ex = p.parsePointcutExpression("args()");
		assertEquals("No args",FuzzyBoolean.NO,ex.matchesStaticInitialization(A.class));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesStaticInitialization(A.class));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesStaticInitialization(B.class));		
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Does not match",FuzzyBoolean.NO,ex.matchesStaticInitialization(A.class));
	}

	public void testMatchesFieldSet() {
		PointcutExpression ex = p.parsePointcutExpression("set(* *..A+.*)");
		assertEquals("matches x",FuzzyBoolean.YES,ex.matchesFieldSet(x,Client.class,A.class,null));
		assertEquals("matches y",FuzzyBoolean.YES,ex.matchesFieldSet(y,Client.class,B.class,null));
		assertEquals("does not match n",FuzzyBoolean.NO,ex.matchesFieldSet(n,A.class,Client.class,null));
		// this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("matches Client",FuzzyBoolean.YES,ex.matchesFieldSet(x,Client.class,A.class,null));
		assertEquals("does not match A",FuzzyBoolean.NO,ex.matchesFieldSet(n,A.class,Client.class,null));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("maybe matches A",FuzzyBoolean.MAYBE,ex.matchesFieldSet(x,A.class,A.class,null));
		// target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("matches B",FuzzyBoolean.YES,ex.matchesFieldSet(y,Client.class,B.class,null));
		assertEquals("maybe matches A",FuzzyBoolean.MAYBE,ex.matchesFieldSet(x,Client.class,A.class,null));		
		// args
		ex = p.parsePointcutExpression("args(int)");
		assertEquals("matches x",FuzzyBoolean.YES,ex.matchesFieldSet(x,Client.class,A.class,null));
		assertEquals("matches y",FuzzyBoolean.YES,ex.matchesFieldSet(y,Client.class,B.class,null));
		assertEquals("does not match n",FuzzyBoolean.NO,ex.matchesFieldSet(n,A.class,Client.class,null));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesFieldSet(x,A.class,A.class,null));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesFieldSet(x,B.class,A.class,null));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Should match",FuzzyBoolean.YES,ex.matchesFieldSet(x,A.class,A.class,aa));
		assertEquals("Should not match",FuzzyBoolean.NO,ex.matchesFieldSet(x,A.class,A.class,b));
	}

	public void testMatchesFieldGet() {
		PointcutExpression ex = p.parsePointcutExpression("get(* *..A+.*)");
		assertEquals("matches x",FuzzyBoolean.YES,ex.matchesFieldGet(x,Client.class,A.class,null));
		assertEquals("matches y",FuzzyBoolean.YES,ex.matchesFieldGet(y,Client.class,B.class,null));
		assertEquals("does not match n",FuzzyBoolean.NO,ex.matchesFieldGet(n,A.class,Client.class,null));
		// this
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		assertEquals("matches Client",FuzzyBoolean.YES,ex.matchesFieldGet(x,Client.class,A.class,null));
		assertEquals("does not match A",FuzzyBoolean.NO,ex.matchesFieldGet(n,A.class,Client.class,null));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("maybe matches A",FuzzyBoolean.MAYBE,ex.matchesFieldGet(x,A.class,A.class,null));
		// target
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		assertEquals("matches B",FuzzyBoolean.YES,ex.matchesFieldGet(y,Client.class,B.class,null));
		assertEquals("maybe matches A",FuzzyBoolean.MAYBE,ex.matchesFieldGet(x,Client.class,A.class,null));		
		// args
		ex = p.parsePointcutExpression("args(int)");
		assertEquals("matches x",FuzzyBoolean.NO,ex.matchesFieldGet(x,Client.class,A.class,null));
		assertEquals("matches y",FuzzyBoolean.NO,ex.matchesFieldGet(y,Client.class,B.class,null));
		assertEquals("does not match n",FuzzyBoolean.NO,ex.matchesFieldGet(n,A.class,Client.class,null));
		// within
		ex = p.parsePointcutExpression("within(*..A)");
		assertEquals("Matches in class A",FuzzyBoolean.YES,ex.matchesFieldGet(x,A.class,A.class,null));
		assertEquals("Does not match in class B",FuzzyBoolean.NO,ex.matchesFieldGet(x,B.class,A.class,null));
		// withincode
		ex = p.parsePointcutExpression("withincode(* a*(..))");
		assertEquals("Should match",FuzzyBoolean.YES,ex.matchesFieldGet(x,A.class,A.class,aa));
		assertEquals("Should not match",FuzzyBoolean.NO,ex.matchesFieldGet(x,A.class,A.class,b));
	}
	
	public void testArgsMatching() {
		// too few args
		PointcutExpression ex = p.parsePointcutExpression("args(*,*,*,*)");
		assertEquals("Too few args",FuzzyBoolean.NO,ex.matchesMethodExecution(foo,Client.class));
		assertEquals("Matching #args",FuzzyBoolean.YES,ex.matchesMethodExecution(bar,Client.class));
		// one too few + ellipsis
		ex = p.parsePointcutExpression("args(*,*,*,..)");
		assertEquals("Matches with ellipsis",FuzzyBoolean.YES,ex.matchesMethodExecution(foo,Client.class));
		// exact number + ellipsis
		assertEquals("Matches with ellipsis",FuzzyBoolean.YES,ex.matchesMethodExecution(bar,Client.class));
		assertEquals("Does not match with ellipsis",FuzzyBoolean.NO,ex.matchesMethodExecution(a,A.class));		
		// too many + ellipsis
		ex = p.parsePointcutExpression("args(*,..,*)");
		assertEquals("Matches with ellipsis",FuzzyBoolean.YES,ex.matchesMethodExecution(bar,Client.class));
		assertEquals("Does not match with ellipsis",FuzzyBoolean.NO,ex.matchesMethodExecution(a,A.class));		
		assertEquals("Matches with ellipsis",FuzzyBoolean.YES,ex.matchesMethodExecution(aaa,A.class));
		// exact match
		ex = p.parsePointcutExpression("args(String,int,Number)");
		assertEquals("Matches exactly",FuzzyBoolean.YES,ex.matchesMethodExecution(foo,Client.class));
		// maybe match
		ex = p.parsePointcutExpression("args(String,int,Double)");
		assertEquals("Matches maybe",FuzzyBoolean.MAYBE,ex.matchesMethodExecution(foo,Client.class));
		// never match
		ex = p.parsePointcutExpression("args(String,Integer,Number)");
		assertEquals("Does not match",FuzzyBoolean.NO,ex.matchesMethodExecution(foo,Client.class));
	}
	
	public void testMatchesDynamically() {
		// everything other than this,target,args should just return true
		PointcutExpression ex = p.parsePointcutExpression("call(* *.*(..)) && execution(* *.*(..)) &&" +
				"get(* *) && set(* *) && initialization(new(..)) && preinitialization(new(..)) &&" +
				"staticinitialization(X) && adviceexecution() && within(Y) && withincode(* *.*(..)))");
		assertTrue("Matches dynamically",ex.matchesDynamically(a,b,new Object[0]));		
		// this
		ex = p.parsePointcutExpression("this(String)");
		assertTrue("String matches",ex.matchesDynamically("",this,new Object[0]));
		assertFalse("Object doesn't match",ex.matchesDynamically(new Object(),this,new Object[0]));
		ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertTrue("A matches",ex.matchesDynamically(new A(""),this,new Object[0]));
		assertTrue("B matches",ex.matchesDynamically(new B(""),this,new Object[0]));
		// target
		ex = p.parsePointcutExpression("target(String)");
		assertTrue("String matches",ex.matchesDynamically(this,"",new Object[0]));
		assertFalse("Object doesn't match",ex.matchesDynamically(this,new Object(),new Object[0]));
		ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
		assertTrue("A matches",ex.matchesDynamically(this,new A(""),new Object[0]));
		assertTrue("B matches",ex.matchesDynamically(this,new B(""),new Object[0]));		
		// args
		ex = p.parsePointcutExpression("args(*,*,*,*)");
		assertFalse("Too few args",ex.matchesDynamically(null,null,new Object[]{a,b}));
		assertTrue("Matching #args",ex.matchesDynamically(null,null,new Object[]{a,b,aa,aaa}));
		// one too few + ellipsis
		ex = p.parsePointcutExpression("args(*,*,*,..)");
		assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b,aa,aaa}));
		// exact number + ellipsis
		assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b,aa}));
		assertFalse("Does not match with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b}));		
		// too many + ellipsis
		ex = p.parsePointcutExpression("args(*,..,*)");
		assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b,aa,aaa}));
		assertFalse("Does not match with ellipsis",ex.matchesDynamically(null,null,new Object[]{a}));		
		assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b}));
		// exact match
		ex = p.parsePointcutExpression("args(String,int,Number)");
		assertTrue("Matches exactly",ex.matchesDynamically(null,null,new Object[]{"",new Integer(5),new Double(5.0)}));
		ex = p.parsePointcutExpression("args(String,Integer,Number)");
		assertTrue("Matches exactly",ex.matchesDynamically(null,null,new Object[]{"",new Integer(5),new Double(5.0)}));
		// never match
		ex = p.parsePointcutExpression("args(String,Integer,Number)");
		assertFalse("Does not match",ex.matchesDynamically(null,null,new Object[]{a,b,aa}));		
}

	public void testGetPointcutExpression() {
		PointcutExpression ex = p.parsePointcutExpression("staticinitialization(*..A+)");
		assertEquals("staticinitialization(*..A+)",ex.getPointcutExpression());
	}

	protected void setUp() throws Exception {
		super.setUp();
		p = new PointcutParser();
		asCons = A.class.getConstructor(new Class[]{String.class});
		bsCons = B.class.getConstructor(new Class[0]);
		bsStringCons = B.class.getConstructor(new Class[]{String.class});
		a = A.class.getMethod("a",new Class[0]);
		aa = A.class.getMethod("aa",new Class[]{int.class});
		aaa = A.class.getMethod("aaa",new Class[]{String.class,int.class});
		x = A.class.getDeclaredField("x");
		y = B.class.getDeclaredField("y");
		b = B.class.getMethod("b",new Class[0]);
		bsaa = B.class.getMethod("aa",new Class[]{int.class});
		n = Client.class.getDeclaredField("n");
		foo = Client.class.getDeclaredMethod("foo",new Class[]{String.class,int.class,Number.class});
		bar = Client.class.getDeclaredMethod("bar",new Class[]{String.class,int.class,Integer.class,Number.class});
	}
	
	static class A {
	  public A(String s) {}
	  public void a() {}
	  public void aa(int i) {}
	  public void aaa(String s, int i) {}
	  int x;
	}
	
	static class B extends A {
		public B() {super("");}
		public B(String s) {super(s);}
		public String b() { return null; }
		public void aa(int i) {}
		int y;
	}
	
	static class Client {
		Number n;
		public void foo(String s, int i, Number n) {}
		public void bar(String s, int i, Integer i2, Number n) {}
	}
}
