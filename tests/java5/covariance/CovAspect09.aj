aspect CovAspect09 {
	
  pointcut p1(): call(FastCar Super.getCar());

  @org.aspectj.lang.annotation.SuppressAjWarnings
  before(): p1() {
  	System.out.println("[call(FastCar Super.getCar()) matched on '"+thisJoinPoint+":"+thisJoinPoint.getSourceLocation()+"']");
  }
  
}

