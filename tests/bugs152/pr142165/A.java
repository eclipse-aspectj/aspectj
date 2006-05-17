abstract aspect A {
  abstract pointcut p();
  before(): p() {
    System.err.println("advice");
  }
}
