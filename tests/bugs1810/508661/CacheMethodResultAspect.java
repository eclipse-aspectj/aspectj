aspect CacheMethodResultAspect perthis(cache()) {

  pointcut cache() : execution(@CacheMethodResult * *.*(..));

  Object around() : cache() {
    System.out.println("around: "+thisJoinPointStaticPart.getSignature());
    return proceed();
  }
}

