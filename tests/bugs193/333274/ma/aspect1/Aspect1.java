package ma.aspect1;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


@Aspect
public class Aspect1 {

    @Around("(execution(@ma.aspect1.Annotation1 * *(..)) || execution(@ma.aspect1.Annotation1 *.new(..)))")
    public Object inRetryAspect(ProceedingJoinPoint pjp) throws Throwable {

        InternalClass ic = new InternalClass();

        System.out.println(">In Aspect1");

        try {
            pjp.proceed();
        } catch (Exception ignored) {
            
        }
        
        System.out.println("=In Aspect1");
        try {
            return pjp.proceed();
        } finally {
            System.out.println("<In Aspect1");
        }
        
    }

    private static class InternalClass {
    }

}
