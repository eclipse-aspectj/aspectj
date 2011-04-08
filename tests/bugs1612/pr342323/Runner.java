package ppp;
public class Runner {
  public static void main(String[] argv) {
    new Runner().run();
  }

  public void run() {
   Bean c = new Bean();
    System.out.println("Calling regular method");
    c.m();
    System.out.println("Calling itd method");
    c.foo();
  }
}
