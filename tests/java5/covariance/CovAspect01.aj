aspect CovAspect01 {
	
  pointcut p(): call(* getCar());

  before(): p() {
  	System.out.println("[call(* getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

