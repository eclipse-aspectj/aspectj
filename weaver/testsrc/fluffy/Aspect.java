package fluffy;
import org.aspectj.runtime.internal.AroundClosure;

public class Aspect {

	public static void ignoreMe() {}
	
	public static void before_method_call() {
		System.out.println("before");	
	}
	
	public static void afterReturning_method_call() {
		System.out.println("afterReturning");	
	}

	public static void afterThrowing_method_execution(Throwable t) {
		System.out.println("afterThrowing " + t);	
		t.printStackTrace();
	}
	
	public static Object aroundFun(AroundClosure c) {
		System.out.println("around");
		try {
			return c.run(new Object[0]);
		} catch (Throwable t) {
			return null;
		}
	}
}
