package ma.aspect3;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class Aspect3 {

    @Around("execution(@ma.Annotation1 * *(..))")
    public Object inTimeLimiterAspect(ProceedingJoinPoint pjp) throws Throwable {
       	new InnerClass2();
        System.out.println(">In Aspect3");
        Object returnedObject = pjp.proceed();
        System.out.println("<In Aspect3");
        return returnedObject;
    }

    private static class InnerClass2 {

    }
}
