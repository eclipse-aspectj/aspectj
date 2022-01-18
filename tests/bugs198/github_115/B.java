import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

public class B {

	public static void main(String []argv) {
		System.out.println("B.main");
	}

}

@Aspect
abstract class AbstractAzpect {

	@Pointcut
	public abstract void isTrue();
	
	@Before("isTrue() && execution(* B.main(..))")
	public void beforeFalse() {
		System.out.println("Azpect.beforeFalse");
	}
}

@Aspect
class Azpect extends AbstractAzpect {
	
	@Override
	@Pointcut("if(true)")
	public void isTrue() { }

}

