aspect A {
	before(): execution(* *(..)) && within(Big) {
		System.out.println(thisJoinPoint);
	}
}