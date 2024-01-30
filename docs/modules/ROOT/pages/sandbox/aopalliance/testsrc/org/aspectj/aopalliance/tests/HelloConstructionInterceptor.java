package org.aspectj.aopalliance.tests;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.ConstructorInvocation;

public class HelloConstructionInterceptor implements ConstructorInterceptor {

	public static int runCount = 0;
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.ConstructorInterceptor#construct(org.aopalliance.intercept.ConstructorInvocation)
	 */
	public Object construct(ConstructorInvocation jp) throws Throwable {
		System.out.println("About to invoke AOP Alliance constructor interceptor");
		Object ret = jp.proceed();
		System.out.println("Invoked AOP Alliance constructor interceptor, return = " + 
				(ret != null ? ret.toString() : "null"));
		runCount++;
		return ret;
	}
}
