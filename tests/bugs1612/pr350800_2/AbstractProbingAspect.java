package test.aop;

import java.io.Serializable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

public abstract aspect AbstractProbingAspect<T extends Serializable> {
  
    abstract pointcut adapterMethodExecution();
   
 
    T around(): adapterMethodExecution() {
        return (T) proceed();       
    }   
   
    protected abstract String extractFunctionName(T command);
}
