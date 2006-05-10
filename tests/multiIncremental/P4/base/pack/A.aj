package pack;

public aspect A {
	
	pointcut p() : call(* C.method2(..));
	
	before() : p() {
	}
		
}
