package org.aspectj.aopalliance.tests;

import org.aspectj.aopalliance.AOPAllianceAdapter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.ConstructorInterceptor;


public aspect HelloAOPAllianceAdapter extends AOPAllianceAdapter {

	private MethodInterceptor mInt;
	private ConstructorInterceptor cInt;
	
	public pointcut targetJoinPoint() : within(Hello);
	
	protected MethodInterceptor getMethodInterceptor() {
		return mInt;
	}
	
	protected ConstructorInterceptor getConstructorInterceptor() {
		return cInt;
	}
	
	public HelloAOPAllianceAdapter() {
		mInt = new HelloMethodInterceptor();
		cInt = new HelloConstructionInterceptor();
	}
	
}
