package org.aspectj.aopalliance.tests;

import java.lang.reflect.Method;

import org.aspectj.aopalliance.MethodInvocationClosure;

import junit.framework.TestCase;

public class MethodInvocationClosureTest extends TestCase {
	

	public void testGetMethod() {
		MockMethodSignature sig = new MockMethodSignature("toString",Object.class,
				new Class[] {});
		MockJoinPoint jp = new MockJoinPoint(this,sig,null);
		MethodInvocationClosure mic = new MethodInvocationClosure(jp) {
			  public Object execute() {return null;}
		  };
		Method m = mic.getMethod();
		try {
			assertEquals("Should find toString method",Object.class.getMethod("toString",new Class[]{}),
					m);
		} catch (NoSuchMethodException e) {
			fail("Duff test:" + e);
		}		
	}
}
