import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Test !if() pointcuts
 */
public class E {

	public static void main(String []argv) {
		new E().run();
	}

	public void run() {
		System.out.println("E.run() executing");
	}

}

@Aspect class Azpect {

	@Pointcut("!bar()") 
	public static void foo() {}
	
	@Pointcut("if()") public static boolean bar() { return false; }

	@Before("foo() && execution(* E.run(..))") public void beforeAdvice() {
		System.out.println("advice running");
	}
}