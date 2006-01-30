import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TestEmptyPointcutAtAspect {

	@Pointcut("")
	protected void scope () {}
	
	@Pointcut
	protected void scope2() {}
}
