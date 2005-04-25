import org.aspectj.lang.annotation.*;

@Aspect
public class AfterReturningWithBadPCut {
	
	@AfterReturning("excution(* *.*(..))")
	public void logExit() {}
	
}