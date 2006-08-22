package bar;

public aspect MyBar {

	before() : call(* main(..)) {
		System.out.println("about to call a main method");
	}
	
}
