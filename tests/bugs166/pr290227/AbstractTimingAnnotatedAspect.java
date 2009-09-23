package aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@ Aspect
public abstract class AbstractTimingAnnotatedAspect {

	@Pointcut("within(*..AbstractJPivotPortlet+) " +
			"&& (execution(* do*(..))" +
			"|| execution(* processAction*(..))" +
			"|| execution(* serveResource*(..)) )")
	protected final void portletEntryMethods() {}

	@Pointcut("execution(* eu.ibacz.pbns..*.*(..)) || execution(* com.tonbeller..*.*(..))")
	protected final void tracedMethods() {}

	@Pointcut("within(aspects.*) || within(aspects..*)")
	protected final void thisAspectClasses() {}

	@Pointcut("cflow(execution(* TimingAnnotatedAspect.processInvocationFinished(..)))")
	protected final void thisAspectExecution() {}

	@Pointcut
	protected abstract void scope();

	@Before("scope() && portletEntryMethods() && !thisAspectClasses() && !thisAspectExecution()")
	public void logStackTrace(final JoinPoint thisJoinPoint) throws Throwable {
		System.out.println("logStackTrace: Logging the current stack trace prior to " +
				"the execution of " +
				thisJoinPoint.getSignature().toShortString()
				+ new Exception("Current stack trace print out."));
	} /* logStackTrace */

}
