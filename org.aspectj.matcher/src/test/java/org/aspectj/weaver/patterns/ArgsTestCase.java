/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

/**
 * @author colyer
 * 
 */
public class ArgsTestCase extends TestCase {

	PointcutExpression wildcardArgs;
	PointcutExpression oneA;
	PointcutExpression oneAandaC;
	PointcutExpression BthenAnything;
	PointcutExpression singleArg;

	public void testMatchJP() throws Exception {
		if (needToSkip)
			return;

		Method oneAArg = B.class.getMethod("x", new Class[] { A.class });
		Method oneBArg = B.class.getMethod("y", new Class[] { B.class });
		Method acArgs = C.class.getMethod("z", new Class[] { A.class, C.class });
		Method baArgs = C.class.getMethod("t", new Class[] { B.class, A.class });

		checkMatches(wildcardArgs.matchesMethodExecution(oneAArg), new B(), new B(), new Object[] { new A() });
		checkMatches(wildcardArgs.matchesMethodExecution(oneBArg), new B(), new B(), new Object[] { new B() });
		checkMatches(wildcardArgs.matchesMethodExecution(acArgs), new C(), new C(), new Object[] { new B(), new C() });
		checkMatches(wildcardArgs.matchesMethodExecution(baArgs), new C(), new C(), new Object[] { new B(), new B() });

		checkMatches(oneA.matchesMethodExecution(oneAArg), new B(), new B(), new Object[] { new A() });
		checkMatches(oneA.matchesMethodExecution(oneBArg), new B(), new B(), new Object[] { new B() });
		checkNoMatch(oneA.matchesMethodExecution(acArgs), new C(), new C(), new Object[] { new B(), new C() });
		checkNoMatch(oneA.matchesMethodExecution(baArgs), new C(), new C(), new Object[] { new B(), new B() });

		checkNoMatch(oneAandaC.matchesMethodExecution(oneAArg), new B(), new B(), new Object[] { new A() });
		checkNoMatch(oneAandaC.matchesMethodExecution(oneBArg), new B(), new B(), new Object[] { new B() });
		checkMatches(oneAandaC.matchesMethodExecution(acArgs), new C(), new C(), new Object[] { new B(), new C() });
		checkNoMatch(oneAandaC.matchesMethodExecution(baArgs), new C(), new C(), new Object[] { new B(), new B() });

		checkNoMatch(BthenAnything.matchesMethodExecution(oneAArg), new B(), new B(), new Object[] { new A() });
		checkMatches(BthenAnything.matchesMethodExecution(oneBArg), new B(), new B(), new Object[] { new B() });
		checkNoMatch(BthenAnything.matchesMethodExecution(acArgs), new C(), new C(), new Object[] { new A(), new C() });
		checkMatches(BthenAnything.matchesMethodExecution(baArgs), new C(), new C(), new Object[] { new B(), new B() });

		checkMatches(singleArg.matchesMethodExecution(oneAArg), new B(), new B(), new Object[] { new A() });
		checkMatches(singleArg.matchesMethodExecution(oneBArg), new B(), new B(), new Object[] { new B() });
		checkNoMatch(singleArg.matchesMethodExecution(acArgs), new C(), new C(), new Object[] { new B(), new C() });
		checkNoMatch(singleArg.matchesMethodExecution(baArgs), new C(), new C(), new Object[] { new B(), new B() });

	}

	public void testBinding() throws Exception {
		if (needToSkip)
			return;

		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(A.class.getClassLoader());
		PointcutParameter a = parser.createPointcutParameter("a", A.class);
		A theParameter = new A();
		PointcutExpression bindA = parser.parsePointcutExpression("args(a,*)", A.class, new PointcutParameter[] { a });

		Method acArgs = C.class.getMethod("z", new Class[] { A.class, C.class });
		ShadowMatch sMatch = bindA.matchesMethodExecution(acArgs);
		JoinPointMatch jpMatch = sMatch.matchesJoinPoint(new A(), new A(), new Object[] { theParameter });
		assertTrue("should match", jpMatch.matches());
		PointcutParameter[] bindings = jpMatch.getParameterBindings();
		assertTrue("one parameter", bindings.length == 1);
		assertEquals("should be bound to the arg value", theParameter, bindings[0].getBinding());

		PointcutParameter c = parser.createPointcutParameter("c", C.class);
		C cParameter = new C();
		PointcutExpression bindAandC = parser.parsePointcutExpression("args(a,c)", A.class, new PointcutParameter[] { a, c });
		sMatch = bindAandC.matchesMethodExecution(acArgs);
		jpMatch = sMatch.matchesJoinPoint(new A(), new A(), new Object[] { theParameter, cParameter });
		assertTrue("should match", jpMatch.matches());
		bindings = jpMatch.getParameterBindings();
		assertTrue("two parameters", bindings.length == 2);
		assertEquals("should be bound to the a arg value", theParameter, bindings[0].getBinding());
		assertEquals("should be bound to the c arg value", cParameter, bindings[1].getBinding());
		assertEquals("a", bindings[0].getName());
		assertEquals("c", bindings[1].getName());
	}

	public void testMatchJPWithPrimitiveTypes() throws Exception {
		if (needToSkip)
			return;

		try {

			PointcutParser parser = PointcutParser
					.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(A.class.getClassLoader());
			PointcutExpression oneInt = parser.parsePointcutExpression("args(int)");
			PointcutExpression oneInteger = parser.parsePointcutExpression("args(Integer)");

			Method oneIntM = A.class.getMethod("anInt", new Class[] { int.class });
			Method oneIntegerM = A.class.getMethod("anInteger", new Class[] { Integer.class });

			checkMatches(oneInt.matchesMethodExecution(oneIntM), new A(), new A(), new Object[] {5});
			checkMatches(oneInt.matchesMethodExecution(oneIntegerM), new A(), new A(), new Object[] {5});
			checkMatches(oneInteger.matchesMethodExecution(oneIntM), new A(), new A(), new Object[] {5});
			checkMatches(oneInteger.matchesMethodExecution(oneIntegerM), new A(), new A(), new Object[] {5});

		} catch (Exception ex) {
			fail("Unexpected exception " + ex);
		}

	}

	private void checkMatches(ShadowMatch sMatch, Object thisOjb, Object targetObj, Object[] args) {
		assertTrue("match expected", sMatch.matchesJoinPoint(thisOjb, targetObj, args).matches());
	}

	private void checkNoMatch(ShadowMatch sMatch, Object thisOjb, Object targetObj, Object[] args) {
		assertFalse("no match expected", sMatch.matchesJoinPoint(thisOjb, targetObj, args).matches());
	}

	@SuppressWarnings("unused")
	private static class A {
		public void anInt(int i) {
		}

		public void anInteger(Integer i) {
		}

	}

	@SuppressWarnings("unused")
	private static class B extends A {
		public void x(A a) {
		}

		public void y(B b) {
		}
	}

	@SuppressWarnings("unused")
	private static class C {
		public void z(A a, C c) {
		}

		public void t(B b, A a) {
		}
	}

	private boolean needToSkip = false;

	/** this condition can occur on the build machine only, and is way too complex to fix right now... */
	private boolean needToSkipPointcutParserTests() {
		try {
			Class.forName("org.aspectj.weaver.reflect.Java15ReflectionBasedReferenceTypeDelegate", false, this.getClass()
					.getClassLoader());// ReflectionBasedReferenceTypeDelegate.class.getClassLoader());
		} catch (ClassNotFoundException cnfEx) {
			return true;
		}
		return false;
	}

	protected void setUp() throws Exception {
		super.setUp();
		needToSkip = needToSkipPointcutParserTests();
		if (needToSkip)
			return;
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(A.class.getClassLoader());
		wildcardArgs = parser.parsePointcutExpression("args(..)");
		oneA = parser.parsePointcutExpression("args(org.aspectj.weaver.patterns.ArgsTestCase.A)");
		oneAandaC = parser
				.parsePointcutExpression("args(org.aspectj.weaver.patterns.ArgsTestCase.A,org.aspectj.weaver.patterns.ArgsTestCase.C)");
		BthenAnything = parser.parsePointcutExpression("args(org.aspectj.weaver.patterns.ArgsTestCase.B,..)");
		singleArg = parser.parsePointcutExpression("args(*)");
	}
}
