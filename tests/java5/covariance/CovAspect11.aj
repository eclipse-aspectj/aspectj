aspect CovAspect11 {
	
  pointcut p1(): call(Car+ Sub.getCar());

  before(): p1() {
  	System.out.println("[call(Car+ Sub.getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

