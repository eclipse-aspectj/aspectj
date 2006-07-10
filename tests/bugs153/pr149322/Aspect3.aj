public aspect Aspect3 {

	before () : call(public * Interface.*(..)) {
		System.out.println("Aspect3.before() " + thisJoinPoint.getSignature().getName());
	}
}
