public abstract aspect Base2 pertypewithin(*) {
  abstract pointcut scope();

  before(): execution(* *(..)) && !within(Base2+) && scope() { 
    System.err.println("advice fired "+thisJoinPoint.getSourceLocation().getWithinType());
  }
}

