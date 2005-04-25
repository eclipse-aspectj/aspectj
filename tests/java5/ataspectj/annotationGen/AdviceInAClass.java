import org.aspectj.lang.annotation.Before;

public class AdviceInAClass {
	
	@Before("execution(* *(..))")
	public void doSomething() {}
	
}