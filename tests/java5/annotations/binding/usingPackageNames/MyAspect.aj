package test;

public aspect MyAspect {
  after(test.MyAnnotation ma) : set (public String test.MyClass._myField) && @target(ma){
    System.err.println("pointcut matching : " + ma);
  }
}

