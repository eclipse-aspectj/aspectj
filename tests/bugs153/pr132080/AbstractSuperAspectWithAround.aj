public abstract aspect AbstractSuperAspectWithAround {
	
	protected abstract pointcut scope ();
	
	void around () : execution(public static void main(String[])) && scope() {
		System.out.println("? " + thisJoinPoint.getSignature());
		proceed();
	}
}
