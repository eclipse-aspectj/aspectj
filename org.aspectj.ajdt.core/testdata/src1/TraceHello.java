public aspect TraceHello {
	before(): execution(* Hello.*(..)) {
		System.out.println("entering: " + thisJoinPoint);
	}
}