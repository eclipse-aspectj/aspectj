package com.codesrc.ozonator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import com.codesrc.ozonator.identity.User;

@Aspect
public abstract class AbstractOzonator
{

  @Pointcut("")
  protected abstract void readMethodExecution();

  @Pointcut("readMethodExecution() && this(ozonated)")
  private void ozonatedReadExecution(Object ozonated){};

  @Around("ozonatedReadExecution(ozonated)")
  public Object aroundGetterCallNoRecurse( ProceedingJoinPoint thisJoinPoint,
      Object ozonated) throws Throwable
  {
    System.out.println("thisJoinPoint="+thisJoinPoint+", ozonated="+ozonated);
      return thisJoinPoint.proceed();
  }

}
