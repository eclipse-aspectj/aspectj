aspect CovAspect04 {
	
  pointcut p(): call(Car Super.getCar());

  before(): p() {
  	System.out.println("[call(Car Super.getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

