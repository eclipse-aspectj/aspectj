package test;

public aspect TestAspect {
	Object around(String s): call(* Test.*(..)) && args(s) {
		System.out.println("Around " + thisJoinPoint.toString());
		System.out.println("Captured "+s);
		return proceed(s.toUpperCase());
	}
}
