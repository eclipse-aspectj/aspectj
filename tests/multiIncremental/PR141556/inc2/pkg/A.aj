package pkg;

public aspect A {

	pointcut p() : execution(* C.method1());
	 
	before(): p() {
		System.out.println(thisJoinPoint);
	}
	
}
