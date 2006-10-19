package pkg;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect()
public class AtAspectJAspect {

	@Before("execution(* *.*())")
	public void execOfEverything() {
	}
	
}
