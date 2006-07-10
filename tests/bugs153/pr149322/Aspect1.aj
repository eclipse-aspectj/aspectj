public aspect Aspect1 {

	before () : call(public * interfaceMethod(..)) && target(Interface) {
		System.out.println("Aspect1.before() " + thisJoinPoint.getSignature().getName());
	}
}
