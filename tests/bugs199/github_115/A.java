import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

public class A {

	public static void main(String []argv) {
		System.out.println("A.main");
	}

}

@Aspect
class Azpect {
	
	@Pointcut("if(false)")
	public void isFalse() { }

	@Pointcut("if(true)")
	public void isTrue() { }

	@Before("isTrue() && execution(* A.main(..))")
	public void beforeTrue() {
		System.out.println("Azpect.beforeTrue");
	}

	@Before("isFalse() && execution(* A.main(..))")
	public void beforeFalse() {
		System.out.println("Azpect.beforeFalse");
	}
}

