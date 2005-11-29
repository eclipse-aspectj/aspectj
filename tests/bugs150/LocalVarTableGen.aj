public aspect LocalVarTableGen {
	
	int x = 5;
	
	public String foo(String s) {
		String myLocal = "" + x + s;
		return myLocal;
	}
	
	public String bar(String s) {
		String myLocal = "" + x + s;
		return myLocal;		
	}
	
	before() : execution(* foo(..)) {
		System.out.println("before foo");
	}
	
	after(String in) returning(String out) :
		execution(* bar(..)) && args(in) 
	{
		System.out.println("after bar");
	}
	
	
}