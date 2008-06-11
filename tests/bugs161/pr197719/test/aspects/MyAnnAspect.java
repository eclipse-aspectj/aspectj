package test.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class MyAnnAspect {
	
	@Pointcut("call(@MyAnn * *(..))")
	void validatedMethod() {}
	

    @Around("validatedMethod()")
    public Object validateMethodImpl(ProceedingJoinPoint thisJoinPoint) throws Throwable {
		return doInvoke(thisJoinPoint);
	}
	
    private Object doInvoke(final ProceedingJoinPoint thisJoinPoint) throws Throwable {
        System.out.println("Invoking : " + thisJoinPoint+ "  "+thisJoinPoint.getTarget().getClass().getName());
        return thisJoinPoint.proceed();
    }
}
