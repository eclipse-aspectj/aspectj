public aspect A {
	before(): execution(* *(..)) { System.out.println("abc");}
	void around(): execution(* *(..)) { proceed();}
}
