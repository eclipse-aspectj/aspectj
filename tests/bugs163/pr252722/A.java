import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;


abstract aspect Super {
  void foo(String s) {}
}

@Aspect
public class A extends Super {

	@Around("execution(* m(..))")
	public void invoke(ProceedingJoinPoint pjp) {
		super.foo("hello");
	}


	public static void main(String []argv) {
		new A().m();
	}

	public void m() {}
}

