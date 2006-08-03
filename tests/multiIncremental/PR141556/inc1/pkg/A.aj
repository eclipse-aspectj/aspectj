package pkg;

public aspect A {

	point cut p() : execution(* C.method1());
	 
	before(): p() {
		System.out.println(thisJoinPoint);
	}
	
}
