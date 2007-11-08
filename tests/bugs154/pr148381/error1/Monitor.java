package test;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
class Monitor {
        @Pointcut(value="execution(@PerformanceMonitor * *(..)) && @annotation(monitoringAnnot)", argNames="monitoringAnnot")
        public void monitored(PerformanceMonitor monitoringAnnot) {}

        // Not enough entries in argNames
        @Around(value="monitored(monitoringAnnot)", argNames="pjp")
        public Object flagExpectationMismatch(ProceedingJoinPoint pjp, PerformanceMonitor monitoringAnnot) throws Throwable {
                //long start = System.nanoTime();
                Object ret = pjp.proceed();
                //long end = System.nanoTime();

                //if(end - start > monitoringAnnot.expected()) {
                //        System.out.println("Method " + pjp.getSignature().toShortString() + " took longer than expected\n\t"
                //                        + "Max expected = " + monitoringAnnot.expected() + ", actual = " + (end-start));
                //}

                System.out.println("This method was intercepted by the advice: "+pjp.getSignature().toShortString());
                return ret;
        }       
}

