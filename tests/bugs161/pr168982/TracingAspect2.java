package tracing;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TracingAspect2<T> {
        @Pointcut("execution(* *(..))")
        public void traced() {}

        @Before("traced()")
        public void log(JoinPoint thisJoinPoint) {
                System.out.println("Entering ");
        }
}
