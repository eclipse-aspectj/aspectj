import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * This test is exploring situations where an if() pointcut is used with a parameter
 * and yet a reference pointcut referring to it is not binding the parameter.
 */
public class C {
	
	int i;

	C(int i) {
		this.i = i;
	}
	
	public static void main(String []argv) {
		new C(1).run();
	}

	public void run() {
		System.out.println("C.run() executing");
	}
	
	public String toString() {
		return "C("+i+")";
	}

}

@Aspect
abstract class Azpect1 {

	@Pointcut("if(false)")
	public void isCondition() {}
	
	@Before("isCondition() && execution(* C.run(..))")
	public void beforeAdvice() {
		System.out.println("Azpect1.beforeAdvice executing");
	}
	
}

@Aspect
class Azpect2 extends Azpect1 {
	@Pointcut("check(*)")
	public void isCondition() { }
	
	@Pointcut("this(c) && if()")
	public static boolean check(C c) {
		System.out.println("check if() pointcut running on "+c.toString());
		return true;
	}
}
//
//abstract aspect A {
//	pointcut isCondition(): if(false);
//	before(): isCondition() && execution(* C.run(..)) { System.out.println("A.before"); }
//}
//
//aspect B extends A {
//	pointcut isCondition(): check(*);
//	pointcut check(Object o): this(o) && if(o.toString().equals("abc"));
//}