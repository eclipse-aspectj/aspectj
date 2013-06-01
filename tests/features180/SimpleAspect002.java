// A Java7 aspect we can weave into the Java8 code

aspect SimpleAspect002 {
  before(): execution(* *(..)) && !within(SimpleAspect002) {
    System.out.println("advice running "+thisJoinPoint.getSourceLocation());
  }
}
