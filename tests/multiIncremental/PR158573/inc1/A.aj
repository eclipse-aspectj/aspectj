public aspect A {
	
	public static int i = 1;
	
	before() : execution(* *.*(..)) {
	}
	
}
