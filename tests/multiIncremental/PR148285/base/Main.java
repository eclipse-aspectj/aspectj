package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

public class Main {
        public static void main(String[] args) {
                new Main().foo();
        }

        @PerformenceMonitor(expected=1000)
        public void foo() {

        }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface PerformenceMonitor {
        public int expected();
}

@Aspect
class Monitor {
        @Pointcut("execution(@PerformenceMonitor * *(..)) && @annotation(monitoringAnnot)")
        public void monitored(PerformenceMonitor monitoringAnnot) {}

        @Around("monitored(monitoringAnnot)")
        public Object flagExpectationMismatch(ProceedingJoinPoint pjp, PerformenceMonitor monitoringAnnot) {
                long start = System.nanoTime();
                Object ret = pjp.proceed();
                long end = System.nanoTime();

                if(end - start > monitoringAnnot.expected()) {
                        System.out.println("Method " + pjp.getSignature().toShortString() + " took longer than expected\n\t"
                                        + "Max expected = " + monitoringAnnot.expected() + ", actual = " + (end-start));
                }
                return ret;
        }

}
