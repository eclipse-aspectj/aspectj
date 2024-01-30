/*
 * Created on 07-May-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectj.aopalliance.tests;

import java.lang.reflect.AccessibleObject;

import junit.framework.TestCase;

import org.aspectj.aopalliance.InvocationJoinPointClosure;


public class InvocationJoinPointClosureTest extends TestCase {

	public void testGetStaticPartMethod() {
		MockMethodSignature sig = new MockMethodSignature("toString",Object.class,
				new Class[] {});
		MockJoinPoint jp = new MockJoinPoint(this,sig,null);
		InvocationJoinPointClosure mejpc = new InvocationJoinPointClosure(jp) {
			  public Object execute() {return null;}
		  };
		AccessibleObject ao = mejpc.getStaticPart();
		try {
			assertEquals("Should find toString method",Object.class.getMethod("toString",new Class[]{}),
					ao);
		} catch (NoSuchMethodException e) {
			fail("Duff test:" + e);
		}
	}

	public void testGetStaticPartConstructor() {
		MockConstructorSignature sig = new MockConstructorSignature("new",StringBuffer.class,
				new Class[] {String.class});
		MockJoinPoint jp = new MockJoinPoint(this,sig,null);
		InvocationJoinPointClosure mejpc = new InvocationJoinPointClosure(jp) {
			  public Object execute() {return null;}
		  };
		AccessibleObject ao = mejpc.getStaticPart();
		try {
			assertEquals("Should find StringBuffer constructor",StringBuffer.class.getConstructor(new Class[]{String.class}),
					ao);
		} catch (NoSuchMethodException e) {
			fail("Duff test:" + e);
		}
	}
	
	public void testGetStaticPartException() {
		try {
			MockMethodSignature sig = new MockMethodSignature("toKettle",Object.class,
					new Class[] {});
			MockJoinPoint jp = new MockJoinPoint(this,sig,null);
			InvocationJoinPointClosure mejpc = new InvocationJoinPointClosure(jp) {
				  public Object execute() {return null;}
			  };
			AccessibleObject ao = mejpc.getStaticPart();			
			fail("UnsupportedOperationException expected");
		} catch (UnsupportedOperationException unEx) {
			assertEquals("Can't find member long string",unEx.getMessage());
		}
	}

	public void testGetArguments() {
		MockMethodSignature sig = new MockMethodSignature("toString",Object.class,
				new Class[] {});
		Object[] args = new Object[] {this};
		MockJoinPoint jp = new MockJoinPoint(this,sig,args);
		InvocationJoinPointClosure mic = new InvocationJoinPointClosure(jp) {
			  public Object execute() {return null;}
		  };
		assertEquals(args,mic.getArguments());
	}
	
}
