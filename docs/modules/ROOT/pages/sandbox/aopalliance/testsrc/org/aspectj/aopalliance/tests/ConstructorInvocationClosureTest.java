package org.aspectj.aopalliance.tests;

import java.lang.reflect.Constructor;

import org.aspectj.aopalliance.ConstructorInvocationClosure;

import junit.framework.TestCase;

public class ConstructorInvocationClosureTest extends TestCase {
	

	public void testGetArguments() {
		MockMethodSignature sig = new MockMethodSignature("toString",Object.class,
				new Class[] {});
		Object[] args = new Object[] {this};
		MockJoinPoint jp = new MockJoinPoint(this,sig,args);
		ConstructorInvocationClosure cic = new ConstructorInvocationClosure(jp) {
			  public Object execute() {return null;}
		  };
		assertEquals(args,cic.getArguments());
	}
	
	public void testGetConstructor() {
		MockConstructorSignature sig = new MockConstructorSignature("new",StringBuffer.class,
				new Class[] {String.class});
		MockJoinPoint jp = new MockJoinPoint(this,sig,null);
		ConstructorInvocationClosure cic = new ConstructorInvocationClosure(jp) {
			  public Object execute() {return null;}
		  };
		Constructor c = cic.getConstructor();
		try {
			assertEquals("Should find StringBuffer constructor",StringBuffer.class.getConstructor(new Class[]{String.class}),
					c);
		} catch (NoSuchMethodException e) {
			fail("Duff test:" + e);
		}

	}
}
