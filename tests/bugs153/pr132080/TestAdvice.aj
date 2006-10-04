public aspect TestAdvice {
	before () : execution(public new()) && within(!TestAdvice) {
		System.out.println("? " + thisJoinPoint.getSignature());
	}
}