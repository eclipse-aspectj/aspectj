package ma.aspect1;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


public aspect Aspect1 {

    Object around(): (execution(@Annotation1 * *(..)) || execution(@Annotation1 *.new(..))) {

        InternalClass ic = new InternalClass();

        System.out.println(">In Aspect1");

        try {
            proceed();
        } catch (Exception ignored) {
            
        }
        
        System.out.println("=In Aspect1");
        try {
            return proceed();
        } finally {
            System.out.println("<In Aspect1");
        }
        
    }

    private static class InternalClass {
    }

}
