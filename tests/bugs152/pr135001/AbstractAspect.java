public abstract aspect AbstractAspect {
	
	abstract pointcut scope();
	
	void around(): scope() {
		System.err.println("In the advice!");
		proceed();
	}
}