abstract aspect LTWAbstractAspect {

  abstract pointcut p();

  before(): p() {
    System.err.println("Non trivial method executing:"+thisJoinPoint.getSignature());
  }
}

