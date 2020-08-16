/*******************************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement
 *******************************************************************************/
package org.aspectj.matcher.tools;

import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.tools.ShadowMatch;
import org.aspectj.weaver.tools.StandardPointcutExpression;
import org.aspectj.weaver.tools.StandardPointcutParser;

import junit.framework.TestCase;

/**
 * Test the use of the pointcut parser and matching infrastructure. The org.aspectj.matcher.tools infrastructure used should not be
 * aware of what kind of World it is working with and only operate in terms of the type abstraction expressed in the
 * org.aspectj.matcher project (so Members, etc). These tests only use base java types, there is no other testdata.
 *
 * This is based on the Reflection oriented PointcutExpressionTest in the weaver project.
 *
 * @author Andy Clement
 */
public abstract class CommonPointcutExpressionTests extends TestCase {

	private World world;
	private StandardPointcutParser pointcutParser;

	protected abstract World getWorld();

	protected void setUp() throws Exception {
		super.setUp();
		world = getWorld();
		pointcutParser = StandardPointcutParser.getPointcutParserSupportingAllPrimitives(world);
	}

	// -- some very basic stuff, if these don't pass it isn't worth continuing

	public void testResolvingOneType() {
		// do it via name
		ResolvedType type = world.resolve("java.lang.String");
		assertNotNull(type);
		// do it via signature
		type = world.resolve(UnresolvedType.forSignature("Ljava/lang/String;"));
		assertNotNull(type);
	}

	public void testResolveTypeAndRetrieveMethod() {
		ResolvedType type = world.resolve("java.lang.String");
		assertNotNull(type);
		ResolvedMember method = getMethod(type, "valueOf", "(Z)Ljava/lang/String;"); // grab the method 'String valueOf()'
		assertNotNull(method);
	}

	// -- next few tests relate to matching different pointcut expressions against a method-execution join point

	public void testMethodExecutionMatching01() {
		checkAlwaysMatches("execution(String valueOf(boolean))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
	}

	public void testMethodExecutionMatching02() {
		checkAlwaysMatches("execution(* *val*(..))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
		checkAlwaysMatches("execution(String *(..))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
		checkAlwaysMatches("execution(* *(boolean))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
		checkAlwaysMatches("execution(* j*..*.valueOf(..))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
		checkAlwaysMatches("execution(* *(*))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");

		checkNeverMatches("execution(* vulueOf(..))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
		checkNeverMatches("execution(int *(..))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
		checkNeverMatches("execution(* valueOf(String))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
		checkNeverMatches("execution(private * valueOf(..))", "java.lang.String", "valueOf", "(Z)Ljava/lang/String;");
	}

	public void testMethodExecutionMatching03() {
		checkAlwaysMatches("execution(* *())", "java.util.List", "toArray", "()[Ljava/lang/Object;");
		checkAlwaysMatches("execution(*[] *())", "java.util.List", "toArray", "()[Ljava/lang/Object;");
		checkAlwaysMatches("execution(*b*[] *())", "java.util.List", "toArray", "()[Ljava/lang/Object;");
	}

	public void testConstructorExecutionMatching01() {
		checkAlwaysMatches("execution(new())", "java.lang.String", "<init>", "()V");
	}

	public void testConstructorExecutionMatching02() {
		checkAlwaysMatches("execution(new(char[]))", "java.lang.String", "<init>", "([C)V");
		checkAlwaysMatches("execution(new(..))", "java.lang.String", "<init>", "()V");
		checkAlwaysMatches("execution(new(..))", "java.lang.String", "<init>", "(Ljava/lang/String;)V");
		checkAlwaysMatches("execution(new(*))", "java.lang.String", "<init>", "(Ljava/lang/String;)V");
		checkNeverMatches("execution(new(*))", "java.lang.String", "<init>", "()V");
		checkAlwaysMatches("execution(new(*,*,*))", "java.lang.String", "<init>", "([CII)V");
		checkAlwaysMatches("execution(new(*,int,*))", "java.lang.String", "<init>", "([III)V");
		checkNeverMatches("execution(new(..,int[]))", "java.lang.String", "<init>", "([III)V");
	}

	public void testConstructorExecutionMatching03() {

		ResolvedType jlArrayList = world.resolve("java.util.ArrayList");
		ResolvedType juList = world.resolve("java.util.List");

		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("execution(java.util.ArrayList.new(..))");
		assertNotNull(ex);

		// simple constructor
		ShadowMatch shadowMatch = ex.matchesMethodExecution(getMethod(jlArrayList, "<init>", "()V"));
		assertTrue(shadowMatch.alwaysMatches());

		ex = pointcutParser.parsePointcutExpression("execution(ArrayList.new(..))");
		shadowMatch = ex.matchesMethodExecution(getMethod(jlArrayList, "<init>", "()V"));
		assertTrue(shadowMatch.alwaysMatches());

		ex = pointcutParser.parsePointcutExpression("execution(*Arr*.new(..))");
		shadowMatch = ex.matchesMethodExecution(getMethod(jlArrayList, "<init>", "()V"));
		assertTrue(shadowMatch.alwaysMatches());

		ex = pointcutParser.parsePointcutExpression("execution(java.util.*.new(..))");
		shadowMatch = ex.matchesMethodExecution(getMethod(jlArrayList, "<init>", "()V"));
		assertTrue(shadowMatch.alwaysMatches());

		ex = pointcutParser.parsePointcutExpression("execution(java.*.new(..))");
		shadowMatch = ex.matchesMethodExecution(getMethod(jlArrayList, "<init>", "()V"));
		assertTrue(shadowMatch.neverMatches());

		ex = pointcutParser.parsePointcutExpression("execution(java..*.new(..))");
		shadowMatch = ex.matchesMethodExecution(getMethod(jlArrayList, "<init>", "()V"));
		assertTrue(shadowMatch.alwaysMatches());

		ex = pointcutParser.parsePointcutExpression("execution(*..ArrayList.new(..))");
		shadowMatch = ex.matchesMethodExecution(getMethod(jlArrayList, "<init>", "()V"));
		assertTrue(shadowMatch.alwaysMatches());
	}

	public void testMatchingThis01() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("this(java.lang.String)");
		ResolvedType jlString = world.resolve("java.lang.String");
		ResolvedType juList = world.resolve("java.util.List");

		// regular method
		ShadowMatch shadowMatch = ex.matchesMethodExecution(getMethod(jlString, "toLowerCase", "()Ljava/lang/String;"));
		assertTrue(shadowMatch.alwaysMatches());

		// static method
		shadowMatch = ex.matchesMethodExecution(getMethod(jlString, "valueOf", "(Z)Ljava/lang/String;"));
		assertTrue(shadowMatch.neverMatches());

		// maybe match: this could be an ArrayList when clear() is called
		ex = pointcutParser.parsePointcutExpression("this(java.util.ArrayList)");
		shadowMatch = ex.matchesMethodExecution(getMethod(juList, "clear", "()V"));
		assertTrue(shadowMatch.maybeMatches());
		assertFalse(shadowMatch.neverMatches());
		assertFalse(shadowMatch.alwaysMatches());
	}

	public void testMatchingTarget01() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("target(java.lang.String)");
		ResolvedType jlString = world.resolve("java.lang.String");
		ResolvedType juList = world.resolve("java.util.List");

		// regular method
		ShadowMatch shadowMatch = ex.matchesMethodExecution(getMethod(jlString, "toLowerCase", "()Ljava/lang/String;"));
		assertTrue(shadowMatch.alwaysMatches());

		// static method
		shadowMatch = ex.matchesMethodExecution(getMethod(jlString, "valueOf", "(Z)Ljava/lang/String;"));
		assertTrue(shadowMatch.neverMatches());

		// maybe match: target could be an ArrayList when clear() is called
		ex = pointcutParser.parsePointcutExpression("target(java.util.ArrayList)");
		shadowMatch = ex.matchesMethodExecution(getMethod(juList, "clear", "()V"));
		assertTrue(shadowMatch.maybeMatches());
		assertFalse(shadowMatch.neverMatches());
		assertFalse(shadowMatch.alwaysMatches());
	}

	public void testMatchingArgs01() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("args(..,int)");
		ResolvedType jlString = world.resolve("java.lang.String");
		ResolvedType juList = world.resolve("java.util.List");

		ResolvedMember stringSplitMethod = getMethod(jlString, "split", "(Ljava/lang/String;I)[Ljava/lang/String;");
		ResolvedMember stringValueOfIntMethod = getMethod(jlString, "valueOf", "(I)Ljava/lang/String;");
		ResolvedMember stringValueOfLongMethod = getMethod(jlString, "valueOf", "(J)Ljava/lang/String;");

		ShadowMatch shadowMatch = ex.matchesMethodExecution(stringSplitMethod);
		assertTrue(shadowMatch.alwaysMatches());

		shadowMatch = ex.matchesMethodExecution(stringValueOfIntMethod);
		assertTrue(shadowMatch.alwaysMatches());

		shadowMatch = ex.matchesMethodExecution(stringValueOfLongMethod);
		assertTrue(shadowMatch.neverMatches());

		// at List.add(Object) the Object might be a String
		ex = pointcutParser.parsePointcutExpression("args(java.lang.String)");
		shadowMatch = ex.matchesMethodExecution(getMethod(juList, "add", "(Ljava/lang/Object;)Z"));
		assertTrue(shadowMatch.maybeMatches());
	}

	public void testMatchingWithin01() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("within(java.lang.String)");
		ResolvedType jlString = world.resolve("java.lang.String");
		ResolvedType juList = world.resolve("java.util.List");

		ResolvedMember stringSplitMethod = getMethod(jlString, "split", "(Ljava/lang/String;I)[Ljava/lang/String;");
		ResolvedMember stringValueOfIntMethod = getMethod(jlString, "valueOf", "(I)Ljava/lang/String;");
		ResolvedMember listAddMethod = getMethod(juList, "add", "(Ljava/lang/Object;)Z");

		assertTrue(ex.matchesMethodExecution(stringSplitMethod).alwaysMatches());
		assertTrue(ex.matchesMethodExecution(stringValueOfIntMethod).alwaysMatches());
		assertTrue(ex.matchesMethodExecution(listAddMethod).neverMatches());
	}

	public void testMatchingWithinCode01() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("withincode(* *..String.*(..))");
		ResolvedType jlString = world.resolve("java.lang.String");
		ResolvedType juList = world.resolve("java.util.List");

		ResolvedMember stringSplitMethod = getMethod(jlString, "split", "(Ljava/lang/String;I)[Ljava/lang/String;");
		ResolvedMember stringValueOfIntMethod = getMethod(jlString, "valueOf", "(I)Ljava/lang/String;");
		ResolvedMember listAddMethod = getMethod(juList, "add", "(Ljava/lang/Object;)Z");

		assertTrue(ex.matchesMethodExecution(stringSplitMethod).neverMatches());
		assertTrue(ex.matchesMethodExecution(stringValueOfIntMethod).neverMatches());
		assertTrue(ex.matchesMethodExecution(listAddMethod).neverMatches());
	}

	// -- next few tests relate to matching different pointcut expressions against a method-call join point

	public void testCallMatchesMethodCall() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("call(* *..String.*(..))");
		ResolvedType jlString = world.resolve("java.lang.String");
		ResolvedType juList = world.resolve("java.util.List");

		ResolvedMember stringSplitMethod = getMethod(jlString, "split", "(Ljava/lang/String;I)[Ljava/lang/String;");
		ResolvedMember stringValueOfIntMethod = getMethod(jlString, "valueOf", "(I)Ljava/lang/String;");
		ResolvedMember listAddMethod = getMethod(juList, "add", "(Ljava/lang/Object;)Z");

		// call from list to string, should be OK
		assertTrue(ex.matchesMethodCall(stringSplitMethod, listAddMethod).alwaysMatches());
		assertTrue(ex.matchesMethodCall(stringValueOfIntMethod, listAddMethod).alwaysMatches());
		assertTrue(ex.matchesMethodCall(listAddMethod, stringSplitMethod).neverMatches());

		ex = pointcutParser.parsePointcutExpression("call(* *..ArrayList.*(..))");
		assertTrue(ex.matchesMethodCall(listAddMethod, stringSplitMethod).neverMatches());
	}

	public void testCall() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("call(* *..A.a*(..))");
		// assertTrue("Should match call to A.a()", ex.matchesMethodCall(a, a).alwaysMatches());
		// assertTrue("Should match call to A.aaa()", ex.matchesMethodCall(aaa, a).alwaysMatches());
		// assertTrue("Should match call to B.aa()", ex.matchesMethodCall(bsaa, a).alwaysMatches());
		// assertTrue("Should not match call to B.b()", ex.matchesMethodCall(b, a).neverMatches());
		// ex = p.parsePointcutExpression("call(* *..A.a*(int))");
		// assertTrue("Should match call to A.aa()", ex.matchesMethodCall(aa, a).alwaysMatches());
		// assertTrue("Should not match call to A.a()", ex.matchesMethodCall(a, a).neverMatches());
		// ex = p.parsePointcutExpression("call(void aaa(..)) && this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
		// assertTrue("Should match call to A.aaa() from Client", ex.matchesMethodCall(aaa, foo).alwaysMatches());
		// ex = p.parsePointcutExpression("call(void aaa(..)) && this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
		// assertTrue("Should match call to A.aaa() from B", ex.matchesMethodCall(aaa, b).alwaysMatches());
		// assertTrue("May match call to A.aaa() from A", ex.matchesMethodCall(aaa, a).maybeMatches());
		// assertFalse("May match call to A.aaa() from A", ex.matchesMethodCall(aaa, a).alwaysMatches());
		// ex = p.parsePointcutExpression("execution(* *.*(..))");
		// assertTrue("Should not match call to A.aa", ex.matchesMethodCall(aa, a).neverMatches());
	}

	public void testCallAndThisMatchesMethodCall() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("call(* *(..)) && this(java.util.ArrayList)");
		ResolvedType jlString = world.resolve("java.lang.String");
		ResolvedType juList = world.resolve("java.util.List");

		ResolvedMember stringSplitMethod = getMethod(jlString, "split", "(Ljava/lang/String;I)[Ljava/lang/String;");
		ResolvedMember stringValueOfIntMethod = getMethod(jlString, "valueOf", "(I)Ljava/lang/String;");
		ResolvedMember listAddMethod = getMethod(juList, "add", "(Ljava/lang/Object;)Z");

		// Calling from list.add() to string split, the callee *might* be an ArrayList, so possible match
		assertTrue(ex.matchesMethodCall(stringSplitMethod, listAddMethod).maybeMatches());
	}

	// // this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should match Client", ex.matchesMethodCall(a, foo).alwaysMatches());
	// assertTrue("Should not match A", ex.matchesMethodCall(a, a).neverMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Should maybe match B", ex.matchesMethodCall(bsaa, a).maybeMatches());
	// assertFalse("Should maybe match B", ex.matchesMethodCall(bsaa, a).alwaysMatches());
	// // target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should not match Client", ex.matchesMethodCall(a, a).neverMatches());
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("Should match A", ex.matchesMethodCall(a, a).alwaysMatches());
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Should maybe match A", ex.matchesMethodCall(aa, a).maybeMatches());
	// assertFalse("Should maybe match A", ex.matchesMethodCall(aa, a).alwaysMatches());
	// // test args
	// ex = p.parsePointcutExpression("args(..,int)");
	// assertTrue("Should match A.aa", ex.matchesMethodCall(aa, a).alwaysMatches());
	// assertTrue("Should match A.aaa", ex.matchesMethodCall(aaa, a).alwaysMatches());
	// assertTrue("Should not match A.a", ex.matchesMethodCall(a, a).neverMatches());
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesMethodCall(a, a).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesMethodCall(a, b).neverMatches());
	// assertTrue("Matches in class A", ex.matchesMethodCall(a, A.class).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesMethodCall(a, B.class).neverMatches());
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Should match", ex.matchesMethodCall(b, bsaa).alwaysMatches());
	// assertTrue("Should not match", ex.matchesMethodCall(b, b).neverMatches());
	// }
	// public void testMatchesConstructorCall() {
	// PointcutExpression ex = p.parsePointcutExpression("call(new(String))");
	// assertTrue("Should match A(String)", ex.matchesConstructorCall(asCons, b).alwaysMatches());
	// assertTrue("Should match B(String)", ex.matchesConstructorCall(bsStringCons, b).alwaysMatches());
	// assertTrue("Should not match B()", ex.matchesConstructorCall(bsCons, foo).neverMatches());
	// ex = p.parsePointcutExpression("call(*..A.new(String))");
	// assertTrue("Should match A(String)", ex.matchesConstructorCall(asCons, b).alwaysMatches());
	// assertTrue("Should not match B(String)", ex.matchesConstructorCall(bsStringCons, foo).neverMatches());
	// // this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should match Client", ex.matchesConstructorCall(asCons, foo).alwaysMatches());
	// assertTrue("Should not match A", ex.matchesConstructorCall(asCons, a).neverMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Should maybe match B", ex.matchesConstructorCall(asCons, a).maybeMatches());
	// assertFalse("Should maybe match B", ex.matchesConstructorCall(asCons, a).alwaysMatches());
	// // target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should not match Client", ex.matchesConstructorCall(asCons, foo).neverMatches());
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("Should not match A (no target)", ex.matchesConstructorCall(asCons, a).neverMatches());
	// // args
	// ex = p.parsePointcutExpression("args(String)");
	// assertTrue("Should match A(String)", ex.matchesConstructorCall(asCons, b).alwaysMatches());
	// assertTrue("Should match B(String)", ex.matchesConstructorCall(bsStringCons, foo).alwaysMatches());
	// assertTrue("Should not match B()", ex.matchesConstructorCall(bsCons, foo).neverMatches());
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesConstructorCall(asCons, a).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesConstructorCall(asCons, b).neverMatches());
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Should match", ex.matchesConstructorCall(bsCons, aa).alwaysMatches());
	// assertTrue("Should not match", ex.matchesConstructorCall(bsCons, b).neverMatches());
	// }
	//
	//
	// // test this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("Should match A", ex.matchesConstructorExecution(asCons).alwaysMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Maybe matches B", ex.matchesConstructorExecution(asCons).maybeMatches());
	// assertFalse("Maybe matches B", ex.matchesConstructorExecution(asCons).alwaysMatches());
	// assertTrue("Should match B", ex.matchesConstructorExecution(bsCons).alwaysMatches());
	// assertTrue("Does not match client", ex.matchesConstructorExecution(clientCons).neverMatches());
	//
	// // test target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("Should match A", ex.matchesConstructorExecution(asCons).alwaysMatches());
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Maybe matches B", ex.matchesConstructorExecution(asCons).maybeMatches());
	// assertFalse("Maybe matches B", ex.matchesConstructorExecution(asCons).alwaysMatches());
	// assertTrue("Should match B", ex.matchesConstructorExecution(bsCons).alwaysMatches());
	// assertTrue("Does not match client", ex.matchesConstructorExecution(clientCons).neverMatches());
	//
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesConstructorExecution(asCons).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesConstructorExecution(bsCons).neverMatches());
	//
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Does not match", ex.matchesConstructorExecution(bsCons).neverMatches());
	//
	// // args
	// ex = p.parsePointcutExpression("args(String)");
	// assertTrue("Should match A(String)", ex.matchesConstructorExecution(asCons).alwaysMatches());
	// assertTrue("Should match B(String)", ex.matchesConstructorExecution(bsStringCons).alwaysMatches());
	// assertTrue("Should not match B()", ex.matchesConstructorExecution(bsCons).neverMatches());
	// }
	//
	// public void testMatchesAdviceExecution() {
	// PointcutExpression ex = p.parsePointcutExpression("adviceexecution()");
	// assertTrue("Should match (advice) A.a", ex.matchesAdviceExecution(a).alwaysMatches());
	// // test this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should match Client", ex.matchesAdviceExecution(foo).alwaysMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Maybe matches B", ex.matchesAdviceExecution(a).maybeMatches());
	// assertFalse("Maybe matches B", ex.matchesAdviceExecution(a).alwaysMatches());
	// assertTrue("Does not match client", ex.matchesAdviceExecution(foo).neverMatches());
	//
	// // test target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should match Client", ex.matchesAdviceExecution(foo).alwaysMatches());
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Maybe matches B", ex.matchesAdviceExecution(a).maybeMatches());
	// assertFalse("Maybe matches B", ex.matchesAdviceExecution(a).alwaysMatches());
	// assertTrue("Does not match client", ex.matchesAdviceExecution(foo).neverMatches());
	//
	// // test within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesAdviceExecution(a).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesAdviceExecution(b).neverMatches());
	//
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Does not match", ex.matchesAdviceExecution(a).neverMatches());
	//
	// // test args
	// ex = p.parsePointcutExpression("args(..,int)");
	// assertTrue("Should match A.aa", ex.matchesAdviceExecution(aa).alwaysMatches());
	// assertTrue("Should match A.aaa", ex.matchesAdviceExecution(aaa).alwaysMatches());
	// assertTrue("Should not match A.a", ex.matchesAdviceExecution(a).neverMatches());
	// }
	//
	// public void testMatchesHandler() {
	// PointcutExpression ex = p.parsePointcutExpression("handler(Exception)");
	// assertTrue("Should match catch(Exception)", ex.matchesHandler(Exception.class, Client.class).alwaysMatches());
	// assertTrue("Should not match catch(Throwable)", ex.matchesHandler(Throwable.class, Client.class).neverMatches());
	// // test this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should match Client", ex.matchesHandler(Exception.class, foo).alwaysMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Maybe matches B", ex.matchesHandler(Exception.class, a).maybeMatches());
	// assertFalse("Maybe matches B", ex.matchesHandler(Exception.class, a).alwaysMatches());
	// assertTrue("Does not match client", ex.matchesHandler(Exception.class, foo).neverMatches());
	// // target - no target for exception handlers
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("Should match Client", ex.matchesHandler(Exception.class, foo).neverMatches());
	// // args
	// ex = p.parsePointcutExpression("args(Exception)");
	// assertTrue("Should match Exception", ex.matchesHandler(Exception.class, foo).alwaysMatches());
	// assertTrue("Should match RuntimeException", ex.matchesHandler(RuntimeException.class, foo).alwaysMatches());
	// assertTrue("Should not match String", ex.matchesHandler(String.class, foo).neverMatches());
	// assertTrue("Maybe matches Throwable", ex.matchesHandler(Throwable.class, foo).maybeMatches());
	// assertFalse("Maybe matches Throwable", ex.matchesHandler(Throwable.class, foo).alwaysMatches());
	// // within
	// ex = p.parsePointcutExpression("within(*..Client)");
	// assertTrue("Matches in class Client", ex.matchesHandler(Exception.class, foo).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesHandler(Exception.class, b).neverMatches());
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Matches within aa", ex.matchesHandler(Exception.class, aa).alwaysMatches());
	// assertTrue("Does not match within b", ex.matchesHandler(Exception.class, b).neverMatches());
	// }
	// -- next few tests relate to matching different pointcut expressions against a staticinitialization join point

	public void testMethodMatchesStaticInitialization01() {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("staticinitialization(java.lang.String)");
		assertNotNull(ex);

		ResolvedType jlString = world.resolve("java.lang.String");

		boolean b = ex.matchesStaticInitialization(jlString).alwaysMatches();
		assertTrue(b);
	}

	public void testMethodMatchesStaticInitialization02() {
		ResolvedType jlString = world.resolve("java.lang.String");
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("staticinitialization(java..*)");
		assertTrue(ex.matchesStaticInitialization(jlString).alwaysMatches());
		ex = pointcutParser.parsePointcutExpression("staticinitialization(java..*)");
		assertTrue(ex.matchesStaticInitialization(jlString).alwaysMatches());
		ex = pointcutParser.parsePointcutExpression("staticinitialization(java.*)");
		assertTrue(ex.matchesStaticInitialization(jlString).neverMatches());
	}

	public void testMethodMatchesStaticInitialization03() {
		ResolvedType juArrayList = world.resolve("java.util.ArrayList");
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression("staticinitialization(java..*)");
		assertTrue(ex.matchesStaticInitialization(juArrayList).alwaysMatches());
		ex = pointcutParser.parsePointcutExpression("staticinitialization(java.util.ArrayList)");
		assertTrue(ex.matchesStaticInitialization(juArrayList).alwaysMatches());
		ex = pointcutParser.parsePointcutExpression("staticinitialization(java.util.List+)");
		assertTrue(ex.matchesStaticInitialization(juArrayList).alwaysMatches());
		ex = pointcutParser.parsePointcutExpression("staticinitialization(List)");
		assertTrue(ex.matchesStaticInitialization(juArrayList).neverMatches());
	}

	//
	// public void testMatchesInitialization() {
	// PointcutExpression ex = p.parsePointcutExpression("initialization(new(String))");
	// assertTrue("Should match A(String)", ex.matchesInitialization(asCons).alwaysMatches());
	// assertTrue("Should match B(String)", ex.matchesInitialization(bsStringCons).alwaysMatches());
	// assertTrue("Should not match B()", ex.matchesInitialization(bsCons).neverMatches());
	// ex = p.parsePointcutExpression("initialization(*..A.new(String))");
	// assertTrue("Should match A(String)", ex.matchesInitialization(asCons).alwaysMatches());
	// assertTrue("Should not match B(String)", ex.matchesInitialization(bsStringCons).neverMatches());
	// // test this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("Should match A", ex.matchesInitialization(asCons).alwaysMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Maybe matches B", ex.matchesInitialization(asCons).maybeMatches());
	// assertFalse("Maybe matches B", ex.matchesInitialization(asCons).alwaysMatches());
	//
	// // test target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("Should match A", ex.matchesInitialization(asCons).alwaysMatches());
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("Maybe matches B", ex.matchesInitialization(asCons).maybeMatches());
	// assertFalse("Maybe matches B", ex.matchesInitialization(asCons).alwaysMatches());
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesInitialization(asCons).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesInitialization(bsCons).neverMatches());
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Does not match", ex.matchesInitialization(bsCons).neverMatches());
	// // args
	// ex = p.parsePointcutExpression("args(String)");
	// assertTrue("Should match A(String)", ex.matchesInitialization(asCons).alwaysMatches());
	// assertTrue("Should match B(String)", ex.matchesInitialization(bsStringCons).alwaysMatches());
	// assertTrue("Should not match B()", ex.matchesInitialization(bsCons).neverMatches());
	// }
	//
	// public void testMatchesPreInitialization() {
	// PointcutExpression ex = p.parsePointcutExpression("preinitialization(new(String))");
	// assertTrue("Should match A(String)", ex.matchesPreInitialization(asCons).alwaysMatches());
	// assertTrue("Should match B(String)", ex.matchesPreInitialization(bsStringCons).alwaysMatches());
	// assertTrue("Should not match B()", ex.matchesPreInitialization(bsCons).neverMatches());
	// ex = p.parsePointcutExpression("preinitialization(*..A.new(String))");
	// assertTrue("Should match A(String)", ex.matchesPreInitialization(asCons).alwaysMatches());
	// assertTrue("Should not match B(String)", ex.matchesPreInitialization(bsStringCons).neverMatches());
	// // test this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("No match, no this at preinit", ex.matchesPreInitialization(asCons).neverMatches());
	//
	// // test target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("No match, no target at preinit", ex.matchesPreInitialization(asCons).neverMatches());
	//
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesPreInitialization(asCons).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesPreInitialization(bsCons).neverMatches());
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Does not match", ex.matchesPreInitialization(bsCons).neverMatches());
	// // args
	// ex = p.parsePointcutExpression("args(String)");
	// assertTrue("Should match A(String)", ex.matchesPreInitialization(asCons).alwaysMatches());
	// assertTrue("Should match B(String)", ex.matchesPreInitialization(bsStringCons).alwaysMatches());
	// assertTrue("Should not match B()", ex.matchesPreInitialization(bsCons).neverMatches());
	// }
	//
	// public void testMatchesStaticInitialization() {
	// // this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("No this", ex.matchesStaticInitialization(A.class).neverMatches());
	// // target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// assertTrue("No target", ex.matchesStaticInitialization(A.class).neverMatches());
	//
	// // args
	// ex = p.parsePointcutExpression("args()");
	// assertTrue("No args", ex.matchesStaticInitialization(A.class).alwaysMatches());
	// ex = p.parsePointcutExpression("args(String)");
	// assertTrue("No args", ex.matchesStaticInitialization(A.class).neverMatches());
	//
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesStaticInitialization(A.class).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesStaticInitialization(B.class).neverMatches());
	//
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Does not match", ex.matchesStaticInitialization(A.class).neverMatches());
	// }
	//
	// public void testMatchesFieldSet() {
	// PointcutExpression ex = p.parsePointcutExpression("set(* *..A+.*)");
	// assertTrue("matches x", ex.matchesFieldSet(x, a).alwaysMatches());
	// assertTrue("matches y", ex.matchesFieldSet(y, foo).alwaysMatches());
	// assertTrue("does not match n", ex.matchesFieldSet(n, foo).neverMatches());
	// // this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("matches Client", ex.matchesFieldSet(x, foo).alwaysMatches());
	// assertTrue("does not match A", ex.matchesFieldSet(n, a).neverMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("maybe matches A", ex.matchesFieldSet(x, a).maybeMatches());
	// assertFalse("maybe matches A", ex.matchesFieldSet(x, a).alwaysMatches());
	// // target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("matches B", ex.matchesFieldSet(y, foo).alwaysMatches());
	// assertTrue("maybe matches A", ex.matchesFieldSet(x, foo).maybeMatches());
	// assertFalse("maybe matches A", ex.matchesFieldSet(x, foo).alwaysMatches());
	// // args
	// ex = p.parsePointcutExpression("args(int)");
	// assertTrue("matches x", ex.matchesFieldSet(x, a).alwaysMatches());
	// assertTrue("matches y", ex.matchesFieldSet(y, a).alwaysMatches());
	// assertTrue("does not match n", ex.matchesFieldSet(n, a).neverMatches());
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesFieldSet(x, a).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesFieldSet(x, b).neverMatches());
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Should match", ex.matchesFieldSet(x, aa).alwaysMatches());
	// assertTrue("Should not match", ex.matchesFieldSet(x, b).neverMatches());
	// }
	//
	// public void testMatchesFieldGet() {
	// PointcutExpression ex = p.parsePointcutExpression("get(* *..A+.*)");
	// assertTrue("matches x", ex.matchesFieldGet(x, a).alwaysMatches());
	// assertTrue("matches y", ex.matchesFieldGet(y, foo).alwaysMatches());
	// assertTrue("does not match n", ex.matchesFieldGet(n, foo).neverMatches());
	// // this
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.Client)");
	// assertTrue("matches Client", ex.matchesFieldGet(x, foo).alwaysMatches());
	// assertTrue("does not match A", ex.matchesFieldGet(n, a).neverMatches());
	// ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("maybe matches A", ex.matchesFieldGet(x, a).maybeMatches());
	// assertFalse("maybe matches A", ex.matchesFieldGet(x, a).alwaysMatches());
	// // target
	// ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertTrue("matches B", ex.matchesFieldGet(y, foo).alwaysMatches());
	// assertTrue("maybe matches A", ex.matchesFieldGet(x, foo).maybeMatches());
	// assertFalse("maybe matches A", ex.matchesFieldGet(x, foo).alwaysMatches());
	// // args - no args at get join point
	// ex = p.parsePointcutExpression("args(int)");
	// assertTrue("matches x", ex.matchesFieldGet(x, a).neverMatches());
	// // within
	// ex = p.parsePointcutExpression("within(*..A)");
	// assertTrue("Matches in class A", ex.matchesFieldGet(x, a).alwaysMatches());
	// assertTrue("Does not match in class B", ex.matchesFieldGet(x, b).neverMatches());
	// // withincode
	// ex = p.parsePointcutExpression("withincode(* a*(..))");
	// assertTrue("Should match", ex.matchesFieldGet(x, aa).alwaysMatches());
	// assertTrue("Should not match", ex.matchesFieldGet(x, b).neverMatches());
	// }
	//
	// public void testArgsMatching() {
	// // too few args
	// PointcutExpression ex = p.parsePointcutExpression("args(*,*,*,*)");
	// assertTrue("Too few args", ex.matchesMethodExecution(foo).neverMatches());
	// assertTrue("Matching #args", ex.matchesMethodExecution(bar).alwaysMatches());
	// // one too few + ellipsis
	// ex = p.parsePointcutExpression("args(*,*,*,..)");
	// assertTrue("Matches with ellipsis", ex.matchesMethodExecution(foo).alwaysMatches());
	// // exact number + ellipsis
	// assertTrue("Matches with ellipsis", ex.matchesMethodExecution(bar).alwaysMatches());
	// assertTrue("Does not match with ellipsis", ex.matchesMethodExecution(a).neverMatches());
	// // too many + ellipsis
	// ex = p.parsePointcutExpression("args(*,..,*)");
	// assertTrue("Matches with ellipsis", ex.matchesMethodExecution(bar).alwaysMatches());
	// assertTrue("Does not match with ellipsis", ex.matchesMethodExecution(a).neverMatches());
	// assertTrue("Matches with ellipsis", ex.matchesMethodExecution(aaa).alwaysMatches());
	// // exact match
	// ex = p.parsePointcutExpression("args(String,int,Number)");
	// assertTrue("Matches exactly", ex.matchesMethodExecution(foo).alwaysMatches());
	// // maybe match
	// ex = p.parsePointcutExpression("args(String,int,Double)");
	// assertTrue("Matches maybe", ex.matchesMethodExecution(foo).maybeMatches());
	// assertFalse("Matches maybe", ex.matchesMethodExecution(foo).alwaysMatches());
	// // never match
	// ex = p.parsePointcutExpression("args(String,Integer,Number)");
	// if (LangUtil.is15VMOrGreater()) {
	// assertTrue("matches", ex.matchesMethodExecution(foo).alwaysMatches());
	// } else {
	// assertTrue("Does not match", ex.matchesMethodExecution(foo).neverMatches());
	// }
	// }
	//
	// // public void testMatchesDynamically() {
	// // // everything other than this,target,args should just return true
	// // PointcutExpression ex = p.parsePointcutExpression("call(* *.*(..)) && execution(* *.*(..)) &&" +
	// // "get(* *) && set(* *) && initialization(new(..)) && preinitialization(new(..)) &&" +
	// // "staticinitialization(X) && adviceexecution() && within(Y) && withincode(* *.*(..)))");
	// // assertTrue("Matches dynamically",ex.matchesDynamically(a,b,new Object[0]));
	// // // this
	// // ex = p.parsePointcutExpression("this(String)");
	// // assertTrue("String matches",ex.matchesDynamically("",this,new Object[0]));
	// // assertFalse("Object doesn't match",ex.matchesDynamically(new Object(),this,new Object[0]));
	// // ex = p.parsePointcutExpression("this(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// // assertTrue("A matches",ex.matchesDynamically(new A(""),this,new Object[0]));
	// // assertTrue("B matches",ex.matchesDynamically(new B(""),this,new Object[0]));
	// // // target
	// // ex = p.parsePointcutExpression("target(String)");
	// // assertTrue("String matches",ex.matchesDynamically(this,"",new Object[0]));
	// // assertFalse("Object doesn't match",ex.matchesDynamically(this,new Object(),new Object[0]));
	// // ex = p.parsePointcutExpression("target(org.aspectj.weaver.tools.PointcutExpressionTest.A)");
	// // assertTrue("A matches",ex.matchesDynamically(this,new A(""),new Object[0]));
	// // assertTrue("B matches",ex.matchesDynamically(this,new B(""),new Object[0]));
	// // // args
	// // ex = p.parsePointcutExpression("args(*,*,*,*)");
	// // assertFalse("Too few args",ex.matchesDynamically(null,null,new Object[]{a,b}));
	// // assertTrue("Matching #args",ex.matchesDynamically(null,null,new Object[]{a,b,aa,aaa}));
	// // // one too few + ellipsis
	// // ex = p.parsePointcutExpression("args(*,*,*,..)");
	// // assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b,aa,aaa}));
	// // // exact number + ellipsis
	// // assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b,aa}));
	// // assertFalse("Does not match with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b}));
	// // // too many + ellipsis
	// // ex = p.parsePointcutExpression("args(*,..,*)");
	// // assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b,aa,aaa}));
	// // assertFalse("Does not match with ellipsis",ex.matchesDynamically(null,null,new Object[]{a}));
	// // assertTrue("Matches with ellipsis",ex.matchesDynamically(null,null,new Object[]{a,b}));
	// // // exact match
	// // ex = p.parsePointcutExpression("args(String,int,Number)");
	// // assertTrue("Matches exactly",ex.matchesDynamically(null,null,new Object[]{"",new Integer(5),new Double(5.0)}));
	// // ex = p.parsePointcutExpression("args(String,Integer,Number)");
	// // assertTrue("Matches exactly",ex.matchesDynamically(null,null,new Object[]{"",new Integer(5),new Double(5.0)}));
	// // // never match
	// // ex = p.parsePointcutExpression("args(String,Integer,Number)");
	// // assertFalse("Does not match",ex.matchesDynamically(null,null,new Object[]{a,b,aa}));
	// // }
	//
	// public void testGetPointcutExpression() {
	// PointcutExpression ex = p.parsePointcutExpression("staticinitialization(*..A+)");
	// assertEquals("staticinitialization(*..A+)", ex.getPointcutExpression());
	// }
	//
	// public void testCouldMatchJoinPointsInType() {
	// PointcutExpression ex = p.parsePointcutExpression("execution(* org.aspectj.weaver.tools.PointcutExpressionTest.B.*(..))");
	// assertTrue("Could maybe match String (as best we know at this point)", ex.couldMatchJoinPointsInType(String.class));
	// assertTrue("Will always match B", ex.couldMatchJoinPointsInType(B.class));
	// ex = p.parsePointcutExpression("within(org.aspectj.weaver.tools.PointcutExpressionTest.B)");
	// assertFalse("Will never match String", ex.couldMatchJoinPointsInType(String.class));
	// assertTrue("Will always match B", ex.couldMatchJoinPointsInType(B.class));
	// }
	//
	// public void testMayNeedDynamicTest() {
	// PointcutExpression ex = p.parsePointcutExpression("execution(* org.aspectj.weaver.tools.PointcutExpressionTest.B.*(..))");
	// assertFalse("No dynamic test needed", ex.mayNeedDynamicTest());
	// ex = p
	// .parsePointcutExpression("execution(* org.aspectj.weaver.tools.PointcutExpressionTest.B.*(..)) && args(org.aspectj.weaver.tools.PointcutExpressionTest.X)");
	// assertTrue("Dynamic test needed", ex.mayNeedDynamicTest());
	// }

	// -- helpers

	private ResolvedMember getMethod(ResolvedType type, String methodName, String methodSignature) {
		ResolvedMember[] methods = type.getDeclaredMethods();
		for (ResolvedMember method : methods) {
			if (method.getName().equals(methodName)
					&& (methodSignature == null || methodSignature.equals(method.getSignature()))) {
				return method;
			}
		}
		return null;
	}

	private void checkAlwaysMatches(String pointcutExpression, String type, String methodName, String methodSignature) {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression(pointcutExpression);
		assertNotNull(ex);
		ResolvedType resolvedType = world.resolve(type);
		ResolvedMember method = getMethod(resolvedType, methodName, methodSignature);
		assertNotNull("Couldn't find a method with signature " + methodSignature, method);
		boolean b = ex.matchesMethodExecution(method).alwaysMatches();
		assertTrue("Match failed", b);
	}

	private void checkNeverMatches(String pointcutExpression, String type, String methodName, String methodSignature) {
		StandardPointcutExpression ex = pointcutParser.parsePointcutExpression(pointcutExpression);
		assertNotNull(ex);
		ResolvedType resolvedType = world.resolve(type);
		ResolvedMember method = getMethod(resolvedType, methodName, methodSignature);
		assertNotNull(method);
		boolean b = ex.matchesMethodExecution(method).neverMatches();
		assertTrue(b);
	}
}
