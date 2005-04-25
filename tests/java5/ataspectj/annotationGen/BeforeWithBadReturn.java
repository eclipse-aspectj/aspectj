import org.aspectj.lang.annotation.*;

@Aspect
public class BeforeWithBadReturn {
	
	@Before("execution(* *(..))")
	public String logEntry() {}
	
}