package ma.aspect2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class Aspect2 {

    @Around("(execution(@ma.aspect2.Annotation2 * *(..)) || execution(@ma.aspect2.Annotation2 *.new(..)))")
    public Object inExceptionTranslatorAspect(ProceedingJoinPoint pjp) throws Throwable {

        System.out.println(">In Aspect2");

        try {
            Object returnedObject = pjp.proceed();
            System.out.println("<In Aspect2");
            return returnedObject;
        } catch (Throwable thrownThrowable) {
            System.out.println("<In Aspect2");
            throw thrownThrowable;
        }

    }

}
