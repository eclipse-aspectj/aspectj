import org.aspectj.lang.annotation.*;

@Aspect
public class AtAj {
	
	@Pointcut("call(* println(..)) && within(C*) && args(s)")
	public void p(String s) {}
	
	@Before("p(s)")
	public void m(String s) {
		System.err.println("a");
	}
}
