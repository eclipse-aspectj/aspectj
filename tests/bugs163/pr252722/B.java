import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

// Now the joinpoint is in a different type, does the super call from advice still work OK?


abstract aspect Super {
  void foo(String s) {}
}

@Aspect
public class B extends Super {

	@Around("execution(* m(..))")
	public void invoke(ProceedingJoinPoint pjp) {
		super.foo("hello");
	}

	public static void main(String []argv) {
		new C().m();
	}
}

class C { 

	public void m() {}
}

