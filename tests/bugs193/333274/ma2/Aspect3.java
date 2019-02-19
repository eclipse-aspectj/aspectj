package ma2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

public aspect Aspect3 {

    Object around(): execution(@Annotation3 * *(..)) || execution(@Annotation3 *.new(..)) {
/*
    @Around("(execution(@ma.aspect3.Annotation3 * *(..)) || execution(@ma.aspect3.Annotation3 *.new(..)))")
    public Object inTimeLimiterAspect(ProceedingJoinPoint pjp) throws Throwable {
*/

        InnerClass2 c = new InnerClass2();

        System.out.println(">In Aspect3");
        try {
            Object returnedObject =proceed();
            System.out.println("<In Aspect3");
            return returnedObject;
        } catch (Exception e) {
            System.out.println("<In Aspect3");
            throw new RuntimeException(e);
        }

    }

    private static class InnerClass2 {

    }
}
