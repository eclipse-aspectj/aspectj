package a;
aspect A {
  before():staticinitialization(!A) {
    System.out.println("intercepted "+thisJoinPoint.getSignature().getDeclaringType());
  }
}
