public aspect pr78707 {
	
	before() returning : execution(* *(..)) {
		// yeah, right!
	}
	
	before() throwing : execution(* *(..)) {
		System.out.println("I'm not flippin' phsycic you know!");
	}
	
}