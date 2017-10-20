package aspects;

public aspect Azpect {
	before(): execution(* main(..)) {
		System.out.println("Azpect running");
	}
}
