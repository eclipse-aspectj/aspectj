import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
class Wibble {

	@Pointcut("if() && call(public * m*(..)) && target(b)")
	public static boolean adviseIfMonitoring(Behavior b) {
	    return true;
	}

	
	@Around("adviseIfMonitoring(b)")
	public Object monitorBehaviorPerformance(ProceedingJoinPoint pjp, Behavior b)
	throws Throwable {
	    return pjp.proceed();
	}
}

