public aspect Aspect2 {

	before () : call(public * Interface.interfaceMethod(..)) {
		System.out.println("Aspect2.before() " + thisJoinPoint.getSignature().getName());
	}
}
