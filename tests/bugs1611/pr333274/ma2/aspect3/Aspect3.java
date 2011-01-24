package ma2.aspect3;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

public aspect Aspect3 {

    Object around(): execution(@ma2.Annotation1 * *(..)) {
       	new InnerClass2();
        System.out.println(">In Aspect3");
        Object returnedObject = proceed();
        System.out.println("<In Aspect3");
        return returnedObject;
    }

    private static class InnerClass2 {

    }
}
