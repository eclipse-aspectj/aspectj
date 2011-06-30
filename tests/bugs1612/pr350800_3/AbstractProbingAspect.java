package test.aop;

import java.io.Serializable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public abstract class AbstractProbingAspect<T extends Serializable> {
  
    @Pointcut("")
    protected abstract void adapterMethodExecution();
   
    @Around("adapterMethodExecution()")
    public T around(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        T t = (T) thisJoinPoint.proceed();       
   System.out.println(">"+extractFunctionName(t));
        return t;
    }   
   
    protected abstract String extractFunctionName(T command);
}
