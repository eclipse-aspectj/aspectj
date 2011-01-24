package ma2.aspect1;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


public aspect Aspect1 {

    Object around(): execution(@ma2.Annotation1 * *(..)) {
        new InternalClass();
        System.out.println(">In Aspect1");
    	proceed();
        System.out.println("=In Aspect1");
        Object o = proceed();
        System.out.println("<In Aspect1");
        return o;
    }

    private static class InternalClass {
    }

}
