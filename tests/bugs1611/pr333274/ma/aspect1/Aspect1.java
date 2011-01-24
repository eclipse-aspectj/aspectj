package ma.aspect1;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


@Aspect
public class Aspect1 {

    @Around("execution(@ma.Annotation1 * *(..))")
    public Object inRetryAspect(ProceedingJoinPoint pjp) throws Throwable {
        new InternalClass();
        System.out.println(">In Aspect1");
    	pjp.proceed();
        System.out.println("=In Aspect1");
        Object o = pjp.proceed();
        System.out.println("<In Aspect1");
        return o;
    }

    private static class InternalClass {
    }

}
