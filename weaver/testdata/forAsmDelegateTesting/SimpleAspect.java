public aspect SimpleAspect {
  pointcut p(): call(* *(..));

  before(): p() {
    
  }

  int SimpleAspect.i;

  public void SimpleAspect.m() { }

}
