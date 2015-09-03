package test;

public aspect TestAspect {
	Object around(): call(* Test.*(..)) {
		System.out.println("Around " + thisJoinPoint.toString());
		return proceed();
	}
}
