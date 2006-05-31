/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.aspectj.lang.annotation.Pointcut;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author colyer
 *
 */
public class Java15PointcutExpressionTest extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite("Java15PointcutExpressionTest");
		suite.addTestSuite(Java15PointcutExpressionTest.class);
		return suite;
	}
	
	private PointcutParser parser;
	private Method a;
	private Method b;
	private Method c;
	private Method d;
	
	public void testAtThis() {
		PointcutExpression atThis = parser.parsePointcutExpression("@this(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atThis.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atThis.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[0]);
		assertTrue("matches",jp2.matches());
	}
	
	public void testAtTarget() {
		PointcutExpression atTarget = parser.parsePointcutExpression("@target(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atTarget.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atTarget.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[0]);
		assertTrue("matches",jp2.matches());		
	}
	
	public void testAtThisWithBinding() {
		PointcutParameter param = parser.createPointcutParameter("a",MyAnnotation.class);
		B myB = new B();
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		PointcutExpression atThis = parser.parsePointcutExpression("@this(a)",A.class,new PointcutParameter[] {param});
		ShadowMatch sMatch1 = atThis.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atThis.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(myB, myB, new Object[0]);
		assertTrue("matches",jp2.matches());
		assertEquals(1,jp2.getParameterBindings().length);
		assertEquals("should be myB's annotation",bAnnotation,jp2.getParameterBindings()[0].getBinding());
	}
	
	public void testAtTargetWithBinding() {
		PointcutParameter param = parser.createPointcutParameter("a",MyAnnotation.class);
		B myB = new B();
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		PointcutExpression atThis = parser.parsePointcutExpression("@target(a)",A.class,new PointcutParameter[] {param});
		ShadowMatch sMatch1 = atThis.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atThis.matchesMethodExecution(b);
		assertTrue("maybe matches A",sMatch1.maybeMatches());
		assertTrue("maybe matches B",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertFalse("does not match",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(myB, myB, new Object[0]);
		assertTrue("matches",jp2.matches());
		assertEquals(1,jp2.getParameterBindings().length);
		assertEquals("should be myB's annotation",bAnnotation,jp2.getParameterBindings()[0].getBinding());
	}
	
	public void testAtArgs() {
		PointcutExpression atArgs = parser.parsePointcutExpression("@args(..,org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atArgs.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("never matches A",sMatch1.neverMatches());
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[]{new A(),new B()});
		assertTrue("matches",jp2.matches());	
		
		atArgs = parser.parsePointcutExpression("@args(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation,org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		sMatch1 = atArgs.matchesMethodExecution(a);
		sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("never matches A",sMatch1.neverMatches());
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch2.matchesJoinPoint(new A(), new A(), new Object[] {new A(), new B()});
		assertFalse("does not match",jp1.matches());
		jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[] {new B(),new B()});
		assertTrue("matches",jp2.matches());					
	}
	
	public void testAtArgs2() {
		PointcutExpression atArgs = parser.parsePointcutExpression("@args(*, org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atArgs.matchesMethodExecution(c);
		ShadowMatch sMatch2 = atArgs.matchesMethodExecution(d);
		assertTrue("maybe matches c",sMatch1.maybeMatches());
		assertTrue("maybe matches d",sMatch2.maybeMatches());
		JoinPointMatch jp1 = sMatch1.matchesJoinPoint(new B(), new B(), new Object[] {new A(), new B()});
		assertTrue("matches",jp1.matches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[] {new A(),new A()});
		assertFalse("does not match",jp2.matches());									
	}
	
	public void testAtArgsWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("a",MyAnnotation.class);
		PointcutParameter p2 = parser.createPointcutParameter("b", MyAnnotation.class);
		PointcutExpression atArgs = parser.parsePointcutExpression("@args(..,a)",A.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		JoinPointMatch jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[]{new A(),new B()});
		assertTrue("matches",jp2.matches());
		assertEquals(1,jp2.getParameterBindings().length);
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		assertEquals("annotation on B",bAnnotation,jp2.getParameterBindings()[0].getBinding());
		
		atArgs = parser.parsePointcutExpression("@args(a,b)",A.class,new PointcutParameter[] {p1,p2});
		sMatch2 = atArgs.matchesMethodExecution(c);
		assertTrue("maybe matches C",sMatch2.maybeMatches());
		jp2 = sMatch2.matchesJoinPoint(new B(), new B(), new Object[] {new B(),new B()});
		assertTrue("matches",jp2.matches());							
		assertEquals(2,jp2.getParameterBindings().length);
		assertEquals("annotation on B",bAnnotation,jp2.getParameterBindings()[0].getBinding());
		assertEquals("annotation on B",bAnnotation,jp2.getParameterBindings()[1].getBinding());		
	}
	
	public void testAtWithin() {
		PointcutExpression atWithin = parser.parsePointcutExpression("@within(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atWithin.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atWithin.matchesMethodExecution(b);
		assertTrue("does not match a",sMatch1.neverMatches());
		assertTrue("matches b",sMatch2.alwaysMatches());
	}
	
	public void testAtWithinWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("x",MyAnnotation.class);
		PointcutExpression atWithin = parser.parsePointcutExpression("@within(x)",B.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch1 = atWithin.matchesMethodExecution(a);
		ShadowMatch sMatch2 = atWithin.matchesMethodExecution(b);
		assertTrue("does not match a",sMatch1.neverMatches());
		assertTrue("matches b",sMatch2.alwaysMatches());
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(new B(), new B(), new Object[0]);
		assertTrue(jpm.matches());
		assertEquals(1,jpm.getParameterBindings().length);
		MyAnnotation bAnnotation = B.class.getAnnotation(MyAnnotation.class);
		assertEquals("annotation on B",bAnnotation,jpm.getParameterBindings()[0].getBinding());		
	}
	
	public void testAtWithinCode() {
		PointcutExpression atWithinCode = parser.parsePointcutExpression("@withincode(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atWithinCode.matchesMethodCall(a,b);
		ShadowMatch sMatch2 = atWithinCode.matchesMethodCall(a,a);
		assertTrue("does not match from b",sMatch1.neverMatches());
		assertTrue("matches from a",sMatch2.alwaysMatches());		
	}
	
	public void testAtWithinCodeWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("x",MyAnnotation.class);
		PointcutExpression atWithinCode = parser.parsePointcutExpression("@withincode(x)",A.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch2 = atWithinCode.matchesMethodCall(a,a);
		assertTrue("matches from a",sMatch2.alwaysMatches());
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertEquals(1,jpm.getParameterBindings().length);
		MyAnnotation annOna = a.getAnnotation(MyAnnotation.class);
		assertEquals("MyAnnotation on a",annOna,jpm.getParameterBindings()[0].getBinding());
	}
	
	public void testAtAnnotation() {
		PointcutExpression atAnnotation = parser.parsePointcutExpression("@annotation(org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation)");
		ShadowMatch sMatch1 = atAnnotation.matchesMethodCall(b,a);
		ShadowMatch sMatch2 = atAnnotation.matchesMethodCall(a,a);
		assertTrue("does not match call to b",sMatch1.neverMatches());
		assertTrue("matches call to a",sMatch2.alwaysMatches());				
	}
	
	public void testAtAnnotationWithBinding() {
		PointcutParameter p1 = parser.createPointcutParameter("x",MyAnnotation.class);
		PointcutExpression atAnnotation = parser.parsePointcutExpression("@annotation(x)",A.class,new PointcutParameter[] {p1});
		ShadowMatch sMatch2 = atAnnotation.matchesMethodCall(a,a);
		assertTrue("matches call to a",sMatch2.alwaysMatches());				
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(new A(), new A(), new Object[0]);
		assertTrue(jpm.matches());
		assertEquals(1,jpm.getParameterBindings().length);
		MyAnnotation annOna = a.getAnnotation(MyAnnotation.class);
		assertEquals("MyAnnotation on a",annOna,jpm.getParameterBindings()[0].getBinding());		
	}
	
	public void testReferencePointcutNoParams() {
		PointcutExpression pc = parser.parsePointcutExpression("foo()",C.class,new PointcutParameter[0]);
		ShadowMatch sMatch1 = pc.matchesMethodCall(a,b);
		ShadowMatch sMatch2 = pc.matchesMethodExecution(a);
		assertTrue("no match on call",sMatch1.neverMatches());
		assertTrue("match on execution",sMatch2.alwaysMatches());
		
		pc = parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.C.foo()");
		sMatch1 = pc.matchesMethodCall(a,b);
		sMatch2 = pc.matchesMethodExecution(a);
		assertTrue("no match on call",sMatch1.neverMatches());
		assertTrue("match on execution",sMatch2.alwaysMatches());
	}
	
	public void testReferencePointcutParams() {
		PointcutParameter p1 = parser.createPointcutParameter("x",A.class);
		PointcutExpression pc = parser.parsePointcutExpression("goo(x)",C.class,new PointcutParameter[] {p1});

		ShadowMatch sMatch1 = pc.matchesMethodCall(a,b);
		ShadowMatch sMatch2 = pc.matchesMethodExecution(a);
		assertTrue("no match on call",sMatch1.neverMatches());
		assertTrue("match on execution",sMatch2.maybeMatches());
		A anA = new A();
		JoinPointMatch jpm = sMatch2.matchesJoinPoint(anA, new A(), new Object[0]);
		assertTrue(jpm.matches());
		assertEquals("should be bound to anA",anA,jpm.getParameterBindings()[0].getBinding());

	}
	
	public void testExecutionWithClassFileRetentionAnnotation() {
		PointcutExpression pc1 = parser.parsePointcutExpression("execution(@org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyAnnotation * *(..))");
		PointcutExpression pc2 = parser.parsePointcutExpression("execution(@org.aspectj.weaver.tools.Java15PointcutExpressionTest.MyClassFileRetentionAnnotation * *(..))");
		ShadowMatch sMatch = pc1.matchesMethodExecution(a);
		assertTrue("matches",sMatch.alwaysMatches());
		sMatch = pc2.matchesMethodExecution(a);
		assertTrue("no match",sMatch.neverMatches());
		sMatch = pc1.matchesMethodExecution(b);
		assertTrue("no match",sMatch.neverMatches());
		sMatch = pc2.matchesMethodExecution(b);
		assertTrue("matches",sMatch.alwaysMatches());
	}
	
	public void testGenericMethodSignatures() throws Exception{
		PointcutExpression ex = parser.parsePointcutExpression("execution(* set*(java.util.List<org.aspectj.weaver.tools.Java15PointcutExpressionTest.C>))");
		Method m = TestBean.class.getMethod("setFriends",List.class);
		ShadowMatch sm = ex.matchesMethodExecution(m);
		assertTrue("should match",sm.alwaysMatches());		
	}
	
	public void testAnnotationInExecution() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("execution(@(org.springframework..*) * *(..))");		
	}
	
	public void testVarArgsMatching() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("execution(* *(String...))");
		Method usesVarArgs = D.class.getMethod("varArgs",String[].class);
		Method noVarArgs = D.class.getMethod("nonVarArgs", String[].class);
		ShadowMatch sm1 = ex.matchesMethodExecution(usesVarArgs);
		assertTrue("should match",sm1.alwaysMatches());				
		ShadowMatch sm2 = ex.matchesMethodExecution(noVarArgs);
		assertFalse("should not match",sm2.alwaysMatches());				
	}
	
	public void testJavaLangMatching() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("@within(java.lang.Deprecated)");
		Method foo = GoldenOldie.class.getMethod("foo");
		ShadowMatch sm1 = ex.matchesMethodExecution(foo);
		assertTrue("should match",sm1.alwaysMatches());
	}
	
	public void testReferencePCsInSameType() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.NamedPointcutResolution.c()",NamedPointcutResolution.class,new PointcutParameter[0]);
		ShadowMatch sm = ex.matchesMethodExecution(a);
		assertTrue("should match",sm.alwaysMatches());
		sm = ex.matchesMethodExecution(b);
		assertTrue("does not match",sm.neverMatches());
	}
	
	public void testReferencePCsInOtherType() throws Exception {
		PointcutExpression ex = parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.ExternalReferrer.d()",ExternalReferrer.class,new PointcutParameter[0]);
		ShadowMatch sm = ex.matchesMethodExecution(a);
		assertTrue("should match",sm.alwaysMatches());
		sm = ex.matchesMethodExecution(b);
		assertTrue("does not match",sm.neverMatches());		
	}
	
	public void testArrayTypeInArgs() throws Exception {
		PointcutParameter[] params = new PointcutParameter[3];
		params[0] = parser.createPointcutParameter("d", Date.class);
		params[1] = parser.createPointcutParameter("s", String.class);
		params[2] = parser.createPointcutParameter("ss", String[].class);
		PointcutExpression ex = parser.parsePointcutExpression("org.aspectj.weaver.tools.Java15PointcutExpressionTest.UsesArrays.pc(d,s,ss)",UsesArrays.class,params);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		parser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingSpecifiedClassloaderForResolution(this.getClass().getClassLoader());
		a = A.class.getMethod("a");
		b = B.class.getMethod("b");
		c = B.class.getMethod("c",new Class[] {A.class,B.class});
		d = B.class.getMethod("d",new Class[] {A.class,A.class});
	}

	@Retention(RetentionPolicy.RUNTIME)
	private @interface MyAnnotation {}
	
	private @interface MyClassFileRetentionAnnotation {}
	
	private static class A {
		@MyAnnotation public void a() {}
	}
	
	@MyAnnotation
	private static class B {
		@MyClassFileRetentionAnnotation public void b() {}
		public void c(A anA, B aB) {}
		
		public void d(A anA, A anotherA) {}
	}
	
	private static class C {
		
		@Pointcut("execution(* *(..))")
		public void foo() {}
		
		@Pointcut(value="execution(* *(..)) && this(x)", argNames="x")
		public void goo(A x) {}
	}
	
	private static class D {
		
		public void nonVarArgs(String[] strings) {};
		
		public void varArgs(String... strings) {};
		
	}
	
	static class TestBean {
		public void setFriends(List<C> friends) {}
	}

	@Deprecated
	static class GoldenOldie {
		public void foo() {}
	}
	
	private static class NamedPointcutResolution {
		
		@Pointcut("execution(* *(..))")
		public void a() {}
		
		@Pointcut("this(org.aspectj.weaver.tools.Java15PointcutExpressionTest.A)")
		public void b() {}
		
		@Pointcut("a() && b()")
		public void c() {}
	}
	
	private static class ExternalReferrer {
		
	  @Pointcut("org.aspectj.weaver.tools.Java15PointcutExpressionTest.NamedPointcutResolution.a() && " + 
			    "org.aspectj.weaver.tools.Java15PointcutExpressionTest.NamedPointcutResolution.b())")
	  public void d() {}
		
	}
	
	private static class UsesArrays {
		
		@Pointcut("execution(* *(..)) && args(d,s,ss)")
		public void pc(Date d, String s, String[] ss) {}
		
	}
}


