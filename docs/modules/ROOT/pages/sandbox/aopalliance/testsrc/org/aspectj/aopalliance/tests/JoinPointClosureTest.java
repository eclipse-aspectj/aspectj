/*
 * Created on 07-May-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectj.aopalliance.tests;

import java.lang.reflect.AccessibleObject;

import junit.framework.TestCase;

import org.aspectj.aopalliance.JoinPointClosure;
import org.aspectj.lang.JoinPoint;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JoinPointClosureTest extends TestCase {
	
	public void testGetThis() {
		JoinPoint jp = new MockJoinPoint(this,null,null);
		JoinPointClosure jpc = new JoinPointClosure(jp) {
			public Object execute() {return null;}
			public AccessibleObject getStaticPart() {return null;}};
		assertEquals("getThis returns join point 'this'",this,jpc.getThis());
	}
	
	public void testProceed() {
		JoinPoint jp = new MockJoinPoint(this,null,null);
		JoinPointClosure jpc = new JoinPointClosure(jp) {
			public Object execute() {return this;}
			public AccessibleObject getStaticPart() {return null;}};
		try {
			Object ret = jpc.proceed();
  		    assertTrue("should return value from execute",ret instanceof JoinPointClosure);
		} catch (Throwable e) {
		    fail("Exception proceeding on join point : " + e);
		}
	}
	
}
