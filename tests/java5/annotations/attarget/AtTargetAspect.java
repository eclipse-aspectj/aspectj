public aspect AtTargetAspect {

  before(): call(* *(..)) && @target(MyAnnotation) {
    System.err.println("advice running");
  }
    
}
