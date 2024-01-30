
package org.aspectj.aopalliance.tests;

import junit.framework.TestCase;

public class AOPAllianceAdapterTest extends TestCase {
	
	public void testHello() {
		Hello h = new Hello();
		h.sayHello();
		Hello h2 = new Hello("Hello AOP Alliance");
		h2.sayHello();
		assertTrue("Constructor executed", Hello.defaultConsExecuted);
		assertTrue("2nd Constructor executed", Hello.paramConsExecuted);
		assertEquals("sayHello invoked twice",2,Hello.sayHelloCount);
		assertEquals("Constructor interceptor ran twice",2,HelloConstructionInterceptor.runCount);
		assertEquals("Method interceptor ran twice",2,HelloMethodInterceptor.runCount);
	}
}
