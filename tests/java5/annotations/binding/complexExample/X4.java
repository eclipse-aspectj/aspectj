import a.b.c.A;

public aspect X4 {

  // Error as Color not imported
  @org.aspectj.lang.annotation.SuppressAjWarnings before(A a): execution(@Color * A+.*(..)) && this(a) {
    System.err.println("Before call to "+thisJoinPoint);
  }

}
