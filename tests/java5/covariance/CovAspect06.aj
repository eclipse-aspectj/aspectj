aspect CovAspect06 {
	
  pointcut p(): call(Car Sub.getCar());
 
  @org.aspectj.lang.annotation.SuppressAjWarnings("adviceDidNotMatch")
  before(): p() {
  	System.out.println("[call(Car Sub.getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

