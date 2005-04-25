import org.aspectj.lang.annotation.*;

@Aspect
public class TwoForThePriceOfOne {
	
	@Before("execution(* *(..))")
	@Pointcut("get(* *)")
	public void logEntry() {}
	
}