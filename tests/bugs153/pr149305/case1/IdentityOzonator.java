package ajtest2;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class IdentityOzonator extends AbstractOzonator 
{
    @Pointcut("execution(public *  ajtest2.User+.get*(..)) ")
	protected void readMethodExecution() {}
}
