import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * This test is exploring situations where an if() pointcut is used with a parameter
 * and yet a reference pointcut referring to it is not binding the parameter.
 */
public class D {
	

	public static void main(String []argv) {
		new D().run();
	}

	public void run() {
		System.out.println("D.run() executing");
	}
	
	public boolean isTrue() {
		return true;
	}

}

@Aspect class Azpect {

	@Pointcut("this(d) && if()") public static boolean method(D d) { return d.isTrue(); }

	@Before("method(*) && execution(* D.run(..))") public void beforeAdvice() {
		System.out.println("advice running");
	}
}