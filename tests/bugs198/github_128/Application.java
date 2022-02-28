public class Application {
  @MarkerA
  @MarkerB
  public void doSomething() {
    System.out.println("        Doing something");
  }

  public static void main(String[] args) throws InterruptedException {
    MarkerAAspect.proceedTimes = Integer.parseInt(args[0]);
    MarkerBAspect.proceedTimes = Integer.parseInt(args[1]);
    new Application().doSomething();
    Thread.sleep(500);
  }
}
