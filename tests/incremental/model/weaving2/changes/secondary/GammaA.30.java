package primary;

public aspect GammaA {
  pointcut handlers(): handler(Throwable);
  before(): handlers() {
    System.err.println("xxx");
  }
}
