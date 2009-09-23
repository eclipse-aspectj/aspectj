package aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@ Aspect
public class TimingAnnotatedAspect extends AbstractTimingAnnotatedAspect {

	@Pointcut("")
	protected void scope() {}


}
