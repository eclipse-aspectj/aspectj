aspect CovAspect02 {
	
  pointcut p(): call(* Super.getCar());

  before(): p() {
  	System.out.println("[call(* Super.getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

