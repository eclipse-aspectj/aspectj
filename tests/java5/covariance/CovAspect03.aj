aspect CovAspect03 {
	
  pointcut p(): call(Car getCar());

  before(): p() {
  	System.out.println("[call(Car getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

