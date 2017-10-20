package extra;

public aspect AnotherAzpect {
		before(): execution(* *(..)) && !within(*Azpect) {
					System.out.println("AnotherAzpect running");
						}
}
