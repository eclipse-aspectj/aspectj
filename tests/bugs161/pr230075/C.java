package a;

import a.a.A;
public class C {
  protected A a = new A();
 public void run() {
   a.test();
  }

  public static void main(String []argv) {
    new C().run();
  }
}
