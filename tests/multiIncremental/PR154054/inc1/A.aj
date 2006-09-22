public aspect A {
	before(): execution(* *(..)) { System.out.println("def");}
	void around(): execution(* *(..)) { proceed();}
}
