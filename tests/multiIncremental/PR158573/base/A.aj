public aspect A {
	
	public static int i = 0;
	
	before() : execution(* *.*(..)) {
	}
	
}
