public aspect MyAspect {
  before() : execution(*.new(..)) && !within(MyAspect) {
    System.out.println(thisJoinPoint);
  }
}
