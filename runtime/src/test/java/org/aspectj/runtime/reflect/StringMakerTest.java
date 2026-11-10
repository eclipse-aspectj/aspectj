/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.runtime.reflect;

import java.lang.reflect.Proxy;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.reflect.p$q.Plain;

import junit.framework.TestCase;

/**
 * A '$' in a class name is shown as '.' only when it separates a nested class from the class it is nested in.
 */
public class StringMakerTest extends TestCase {

	static class Nested {
		class Deeper {
		}
	}

	private static String longString(Class<?> type) {
		return new MethodSignatureImpl(0, "run", type, new Class[0], new String[0], new Class[0], void.class).toLongString();
	}

	private static String shortString(Class<?> type) {
		return new MethodSignatureImpl(0, "run", type, new Class[0], new String[0], new Class[0], void.class).toShortString();
	}

	public void testNestedClassesUseDots() {
		assertEquals("void org.aspectj.runtime.reflect.StringMakerTest.Nested.run()", longString(Nested.class));
		assertEquals("void org.aspectj.runtime.reflect.StringMakerTest.Nested.Deeper.run()", longString(Nested.Deeper.class));
		assertEquals("StringMakerTest.Nested.Deeper.run()", shortString(Nested.Deeper.class));
	}

	public void testAnonymousClassUsesDot() {
		Object anonymous = new Object() {
		};
		assertEquals("void org.aspectj.runtime.reflect.StringMakerTest.1.run()", longString(anonymous.getClass()));
	}

	public void testClassWithDollarsThatIsNotNestedKeepsThem() {
		assertEquals("void org.aspectj.runtime.reflect.Target$$SpringCGLIB$$0.run()", longString(Target$$SpringCGLIB$$0.class));
		assertEquals("Target$$SpringCGLIB$$0.run()", shortString(Target$$SpringCGLIB$$0.class));
	}

	public void testOnlyTheDollarThatSeparatesNestedFromEnclosingClassBecomesDot() {
		assertEquals("void org.aspectj.runtime.reflect.Foo$$$$Bar.Boo.run()", longString(Foo$$$$Bar.Boo.class));
		assertEquals("void org.aspectj.runtime.reflect.Foo$$$$Bar.Boo.Deeper.run()", longString(Foo$$$$Bar.Boo.Deeper.class));
		assertEquals("Foo$$$$Bar.Boo.Deeper.run()", shortString(Foo$$$$Bar.Boo.Deeper.class));
	}

	public void testDollarInPackageName() {
		assertEquals("void org.aspectj.runtime.reflect.p$q.Plain.run()", longString(Plain.class));
		assertEquals("void org.aspectj.runtime.reflect.p$q.Plain.Inner.run()", longString(Plain.Inner.class));
		assertEquals("Plain.Inner.run()", shortString(Plain.Inner.class));
	}

	public void testJdkProxyKeepsLeadingDollar() {
		Class<?> proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Runnable.class },
				(instance, method, args) -> null).getClass();
		String name = proxy.getName();
		assertTrue(name, name.contains(".$Proxy"));
		assertEquals("void " + name + ".run()", longString(proxy));
		assertEquals(name.substring(name.lastIndexOf('.') + 1) + ".run()", shortString(proxy));
	}

	public interface Counter {
		long getCount();
	}

	/**
	 * https://github.com/eclipse-aspectj/aspectj/issues/351: the join point of an execution of a method of a JDK proxy, as the
	 * generated code builds it and as an aspect logs it.
	 */
	public void testExecutionJoinPointOfJdkProxy() {
		Class<?> proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Counter.class },
				(instance, method, args) -> 0L).getClass();
		Factory factory = new Factory("StringMakerTest.java", StringMakerTest.class);
		Signature signature = factory.makeMethodSig("1", "getCount", proxy.getName(), "", "", "", "long");
		JoinPoint.StaticPart joinPoint = factory.makeSJP(JoinPoint.METHOD_EXECUTION, signature, 1);
		assertEquals("execution(long " + proxy.getName() + ".getCount())", joinPoint.toString());
		assertEquals("execution(public long " + proxy.getName() + ".getCount())", joinPoint.toLongString());
		assertTrue(joinPoint.toString(), joinPoint.toString().contains(".$Proxy"));
	}

	public void testArrayOfClassWithDollars() {
		MethodSignatureImpl msi = new MethodSignatureImpl(0, "run", StringMakerTest.class,
				new Class[] { Target$$SpringCGLIB$$0[].class, Nested[].class }, new String[] { "a", "b" }, new Class[0], void.class);
		assertEquals("void org.aspectj.runtime.reflect.StringMakerTest.run(org.aspectj.runtime.reflect.Target$$SpringCGLIB$$0[],"
				+ " org.aspectj.runtime.reflect.StringMakerTest.Nested[])", msi.toLongString());
	}
}
