package pkg;

aspect A {
	
	
	before() : execution(* *.*(..)) {
	}
	after() : callPCD(){
	}
	after() : execPCD(){
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
