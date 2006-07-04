package com.codesrc.ozonator.identity;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.codesrc.ozonator.AbstractOzonator;

@Aspect
public class IdentityOzonator extends AbstractOzonator 
{
    @Pointcut("execution(public *  com.codesrc.ozonator.identity.User+.get*(..)) ")
	protected void readMethodExecution() {}
}
