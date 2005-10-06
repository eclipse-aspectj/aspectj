abstract aspect DecPrecedenceSuper<X,Y> {
	
	declare precedence: X,Y;
	
}

aspect Sub extends DecPrecedenceSuper<A1,A2> {}

public class DecPrecedenceGenericTest {
	
	public static void main(String[] args) {
		new C().foo();
	}
}


aspect A2 {
	
	before() : execution(* C.*(..)) { System.out.println("A2"); }
	
}


aspect A1 {
	
	before() : execution(* C.*(..)) { System.out.println("A1"); }

}

class C {
	
	void foo() {}
	
}