package b;

public class B extends a.A {
  protected class Inner {
    public void foo() {
      System.out.println("calling m()");
      m();
    }
  }

  public static void main(String []argv) {
    B b = new B();
    b.run();
  }

  public void run() {
    new Inner().foo();
  }
}
