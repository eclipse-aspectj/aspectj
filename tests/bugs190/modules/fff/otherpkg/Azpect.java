package otherpkg;

public aspect Azpect {
	before(): execution(* *(..)) && !within(Azpect) {
		System.out.println("Azpect running");
	}
}
