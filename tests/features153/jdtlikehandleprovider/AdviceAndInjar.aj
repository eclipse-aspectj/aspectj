public aspect AdviceAndInjar {
	
	pointcut p() : call(* *.*(..)) && !within(AdviceAndInjar);

	before() : p() {
	}
	
	after() : p() {
	}
	
	pointcut p1() : execution(* *.*(..)) && !within(AdviceAndInjar);
	
	Object around() : p1() {
		return proceed();
	}

}
