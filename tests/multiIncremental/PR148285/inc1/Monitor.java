package test;

import org.aspectj.lang.annotation.*;

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
                        System.out.println("Method " + pjp.getSignature().toShortString() + " took longer than expected\n\t" + 
"Max expected = " + monitoringAnnot.expected() + ", actual = " + (end-start));
                }
                return ret;
        }

}
