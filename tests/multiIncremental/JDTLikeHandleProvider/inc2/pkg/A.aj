package pkg;

aspect A {
	
	before() : execution(* *.*(..)) {
	}
	
	after() : execPCD(){
	}
	after() : callPCD(){
	}
	pointcut callPCD(): call(* *.*(..));
	pointcut execPCD(): execution(* *.*(..));
}

class C {
	
	public void m() {
	}
	
    static { 	
    }	
}
