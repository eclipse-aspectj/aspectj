public abstract aspect Trace {
	public abstract pointcut traced(Object o);
	
	before (Object exc): traced(exc) {
		System.out.println("enter: " /*+ thisJoinPoint.getSignature()*/);
	}
	
    Object around(): execution(* doit(..)) {
    	Object exc = "Hi";
    	System.out.println("start around: " + exc);
    	Object ret = proceed();
    	System.out.println("exiting around with: " + ret);
    	return ret;
    }
    
     Object around(Object exc): traced(exc) {
    	System.out.println("start around(2): " + exc);
    	Object ret = proceed(exc);
    	System.out.println("exiting around with(2): " + ret);
    	return ret;
    }
}
