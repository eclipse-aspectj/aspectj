aspect CovAspect10 {
	
  pointcut p1(): call(Car+ getCar());

  before(): p1() {
  	System.out.println("[call(Car+ getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

