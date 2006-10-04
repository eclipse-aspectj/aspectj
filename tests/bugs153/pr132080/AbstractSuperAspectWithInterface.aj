public abstract aspect AbstractSuperAspectWithInterface /*implements TestInterface*/ {
	
	protected abstract pointcut scope ();
	
	before () : execution(public static void main(String[])) && scope() {
		System.out.println("? " + thisJoinPoint.getSignature());
	}
	
	protected AbstractSuperAspectWithInterface () {
		TestInterface test = (TestInterface)this;
		test.interfaceMethod();
	}
}
