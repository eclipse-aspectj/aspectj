package ma2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

public aspect Aspect2 {

    Object around(): execution(@Annotation2 * *(..)) || execution(@Annotation2 *.new(..)) {
/*
    @Around("(execution(@ma.aspect2.Annotation2 * *(..)) || execution(@ma.aspect2.Annotation2 *.new(..)))")
    public Object inExceptionTranslatorAspect(ProceedingJoinPoint pjp) throws Throwable {
*/

        System.out.println(">In Aspect2");

        try {
            Object returnedObject = proceed();
            System.out.println("<In Aspect2");
            return returnedObject;
        } catch (Throwable thrownThrowable) {
            System.out.println("<In Aspect2");
            throw new RuntimeException(thrownThrowable);
        }

    }

}
