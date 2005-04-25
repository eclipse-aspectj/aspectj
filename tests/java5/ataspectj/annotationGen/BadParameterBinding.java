import org.aspectj.lang.annotation.*;

@Aspect
public class BadParameterBinding {
	
	@SuppressAjWarnings
	@Before("execution(* *(..)) && this(bpb)")
	public void logEntry(BadParameterBinding bpb) {}
	
	@SuppressAjWarnings
	@AfterReturning("execution(* *(..)) && this(bpb)")
	public void logExit() {}
	
	@SuppressAjWarnings
	@AfterThrowing("call(* TheUnknownType.*(..))")
	public void logException() {}
	
}