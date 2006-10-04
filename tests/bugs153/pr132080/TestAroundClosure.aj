public aspect TestAroundClosure {
	void around () : execution(public new()) && within(!TestAroundClosure) {
		System.out.println("> " + thisJoinPoint.getSignature());
		proceed();
		System.out.println("< " + thisJoinPoint.getSignature());
	}
}