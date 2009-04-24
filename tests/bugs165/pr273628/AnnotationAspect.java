package p;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AnnotationAspect
{
    @Pointcut("if() && execution(@MonitorableMethod public * p..*(..)) && @annotation(MonitorableMethod(api))")
    public static boolean adviseIfMonitoringPartialAnnotation(ApiDescriptor api) {
    	return true;
    }

    @Around("adviseIfMonitoringPartialAnnotation(api)")
    public Object monitorMethodPerformancePartialAnnotation(ProceedingJoinPoint pjp, ApiDescriptor api)
	    throws Throwable {
		System.out.println("Descriptor value: " + api.number);
		return pjp.proceed();
    } 
    
//	
//  pointcut p(ApiDescriptor api): if(true) && execution(@MonitorableMethod public * p..*(..)) && @annotation(MonitorableMethod(api));
//
//  Object around(ApiDescriptor api): p(api) {
//		System.out.println("Descriptor value: " + api.number);
//		return proceed();
//  }
	
}
