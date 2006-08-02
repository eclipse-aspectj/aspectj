public abstract aspect CaptureLock {

  abstract pointcut lockPC();

  before(): lockPC() {
    System.out.println("Before a lock or unlock");
  }
}
