package pack;

public aspect A2 extends A1 {

	pointcut p() : execution(* C*.log*(..));
	
	before() : p() {
		i = 2;
	}
	
}
