public abstract aspect Base pertypewithin(*) {
  before(): execution(* *(..)) && !within(Base+) { 
    System.err.println("advice fired "+thisJoinPoint.getSourceLocation().getWithinType());
  }
}

