aspect CovAspect08 {
	
  pointcut p1(): call(FastCar getCar());

  before(): p1() {
  	System.out.println("[call(FastCar getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
  pointcut p2(): call(FastCar Sub.getCar());

  before(): p2() {
  	System.out.println("[call(FastCar Sub.getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }

}

