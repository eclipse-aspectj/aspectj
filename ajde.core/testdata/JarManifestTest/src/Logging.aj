public aspect Logging {
	
	pointcut methods () :
		execution(* *..*(..)) && !within(Logging);
	
	before () : methods () {
		System.err.println("> " + thisJoinPoint.getSignature().toLongString());
	}
	
	after () : methods () {
		System.err.println("< " + thisJoinPoint.getSignature().toLongString());
	}
}
