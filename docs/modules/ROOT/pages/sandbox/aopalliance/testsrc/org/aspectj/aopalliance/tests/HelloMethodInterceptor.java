package org.aspectj.aopalliance.tests;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class HelloMethodInterceptor implements MethodInterceptor {

	public static int runCount = 0;
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation jp) throws Throwable {
		System.out.println("About to invoke AOP Alliance method interceptor");
		Object ret = jp.proceed();
		System.out.println("Invoked AOP Alliance method interceptor, return = " + ret.toString());
		runCount++;
		return ret;
	}
}
