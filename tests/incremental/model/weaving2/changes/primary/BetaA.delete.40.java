package primary;

public aspect BetaA {
  pointcut handlers(): handler(Throwable);
  before(): handlers() {
    System.err.println("xxx");
  }
}
