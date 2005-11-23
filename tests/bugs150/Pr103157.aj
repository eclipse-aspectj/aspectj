public aspect Pr103157 {
	
	// verify after returning behaviour with join points that have no "return" value
	
	// these are: 
	// ConstructorExecution
	// FieldSet
	// StaticInitialization
	// Initialization
	// PreInitialization
	// ExceptionHandler  -- but handler can't have after returning advice anyway
	// arguably all adviceexecution join points except for around, but allow this for now
	
	after() returning(Object obj) : execution(C.new(..)) {
		System.out.println("returning obj on cons exe " + obj);
	}
	
	after() returning : execution(C.new(..)) {
		System.out.println("returning from cons exe");
	}
	
	after() returning(Object obj) : set(* C.*) {
		System.out.println("returning obj on set " + obj);		
	}
	
	after() returning : set(* C.*) {
		System.out.println("returning from set");		
	}
	
	after() returning(Object obj) : staticinitialization(C) {
		System.out.println("returning obj on staticinit " + obj);		
	}
	
	after() returning : staticinitialization(C) {
		System.out.println("returning from staticinit");		
	}	
	
	after() returning(Object obj) : initialization(C.new(..)) {
		System.out.println("returning obj on init " + obj);
	}
	
	after() returning : initialization(C.new(..)) {
		System.out.println("returning from init");
	}

	after() returning(Object obj) : preinitialization(C.new(..)) {
		System.out.println("returning obj on preinit " + obj);
	}
	
	after() returning : preinitialization(C.new(..)) {
		System.out.println("returning from preinit");
	}
	
	public static void main(String[] args) {
		new C();
	}

}

class C {

	String s;
	
	public C() { this.s = "xxx"; }
	
}